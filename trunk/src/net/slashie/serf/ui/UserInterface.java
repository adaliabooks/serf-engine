package net.slashie.serf.ui;

import java.util.*;

import net.slashie.serf.action.Action;
import net.slashie.serf.action.Actor;
import net.slashie.serf.action.Message;
import net.slashie.serf.game.Player;
import net.slashie.serf.level.AbstractLevel;
import net.slashie.serf.sound.SFXManager;
import net.slashie.serf.sound.STMusicManagerNew;
import net.slashie.utils.Debug;
import net.slashie.utils.Position;

/** 
 *  Shows the level
 *  Informs the Actions and Commands of the player.
 * 	Must be listening to a System Interface
 */

public abstract class UserInterface implements CommandListener {
	//Interface
	public abstract void doLook();
    public abstract boolean promptChat (String message);
    public abstract void drawEffect(Effect what);
	public abstract void addMessage(Message message);
	public abstract List<Message> getMessageBuffer();
	public abstract boolean isDisplaying(Actor who); //(?)
	public abstract void setTargets(Action a) throws ActionCancelException;
	public abstract void showMessageHistory();
	public abstract void showInventory();
	public abstract int switchChat(String title, String prompt, String... options);
	public abstract String inputBox(String prompt);
	public void shutdown(){}
	
	protected Position getNearestActorPosition(){
		List<Actor> actors = level.getDispatcher().getActors();
		Actor nearActor = null;
		int minDist = 150;
		int maxDist = 15;
		for (Actor actor: actors){
			if (actor.getPosition().z() != level.getPlayer().getPosition().z())
				continue;
			int distance = Position.flatDistance(level.getPlayer().getPosition(), actor.getPosition());
			if (distance < maxDist && distance< minDist && player.sees(actor)){
				minDist = distance;
				nearActor = actor;
			}
		}
		if (nearActor != null)
			return nearActor.getPosition();
		else
			return null;
	}
    
    /**
     * Prompts for Yes or NO
     */
    public abstract boolean prompt ();

	public abstract void refresh();

 	/**
     * Shows a message inmediately; useful for system
     * messages.
     *  
     * @param x the message to be shown
     */
	public abstract void showMessage(String x);

	public abstract void showImportantMessage(String x);
	
	/**
     * Shows a message inmediately; useful for system
     * messages. Waits for a key press or something.
     *  
     * @param x the message to be shown
     */
	public abstract void showSystemMessage(String x);
	
	public abstract void processQuit();
	
	public abstract void processSave();
	
	public abstract void processHelp();
	
	public abstract void onMusicOn();
	
	//Status
	protected Vector monstersOnSight = new Vector();
	protected Vector featuresOnSight = new Vector();
	protected Vector itemsOnSight = new Vector();
	protected Action actionSelectedByCommand;
	
	protected boolean eraseOnArrival; // Erase the buffer upon the arrival of a new msg
   	
	protected String lastMessage; // (?)
	
	protected AbstractLevel level;

	// Reference to the player...
	protected Player player;
	
    public Player getPlayer() {
		return player;
	}
    
    public void setPlayer(Player pPlayer){
		player = pPlayer;
		level = player.getLevel();
	}
    

    //FOVMask provided as convenience for rendering (?)
    private boolean [][] FOVMask;
	public boolean isOnFOVMask(int x, int y){
		return FOVMask[x][y];
	}
	
	

	public void init(UserCommand[] gameCommands){
		FOVMask = new boolean[80][25];
		for (int i = 0; i < gameCommands.length; i++)
			this.gameCommands.put(gameCommands[i].getKeyCode()+"", gameCommands[i]);
		addCommandListener(this);
	}

	protected Command getRelatedCommand(int keyCode){
    	UserCommand uc = (UserCommand ) gameCommands.get(keyCode+"");
    	if (uc == null){
    		return CommandListener.Command.NONE;
    	}
    	return uc.getCommand();
	}
	
    public void levelChange(){
		level = player.getLevel();
	}
    
	protected void informPlayerCommand(Command command) {

	    for (int i =0; i < commandListeners.size(); i++){
	    	commandListeners.get(i).commandSelected(command);
	    }
    }
	
	public void addCommandListener(CommandListener pCl) {
		commandListeners.add(pCl);
    }
	
	public void removeCommandListener(CommandListener pCl){
		commandListeners.remove(pCl);
	}
	
	protected Map<String, UserCommand> gameCommands = new Hashtable<String, UserCommand>();
	
	private List<CommandListener> commandListeners = new ArrayList<CommandListener>(5);

	//Command Listener Implementation
	public void commandSelected (Command commandCode){
		switch (commandCode){
			case PROMPTQUIT:
				processQuit();
				break;
			case PROMPTSAVE:
				processSave();
				break;
			case HELP:
				processHelp();
				break;
			case LOOK:
				doLook();
				break;
			case SHOWINVEN:
				showInventory();
				break;
			case SWITCHMUSIC:
				currentSoundCycle = currentSoundCycle.nextCycle();
				STMusicManagerNew.thus.setVolume(currentSoundCycle.getGain());
				showMessage("Music volume set to "+currentSoundCycle.getDescription());
				break;
			case SWITCHSFX:
				currentSFXCycle = currentSFXCycle.nextCycle();
				SFXManager.setVolume(currentSFXCycle.getGain());
				showMessage("SFX volume set to "+currentSFXCycle.getDescription());
				break;
		}
	}
	
	public void setSoundCycle(SoundCycle cycle){
		this.currentSoundCycle = cycle;
	}
	
	private SoundCycle currentSoundCycle = SoundCycle.FULLGAIN;
	private SoundCycle currentSFXCycle = SoundCycle.FULLGAIN;

	
	public enum SoundCycle {
		FULLGAIN (1.0d, "100%"),
		GAIN_75 (0.75d, "75%"),
		GAIN_50 (0.5d, "50%"),
		GAIN_25 (0.25d, "25%"),
		MUTE (0.0d, "Mute");
		
		public SoundCycle nextCycle(){
			if (ordinal() > 0)
				return values()[ordinal()-1];
			else
				return values()[values().length-1];
		}
		
		private double dblGain;
		private String description;
		
		SoundCycle(double dblGain, String description){
			this.dblGain = dblGain;
			this.description = description;
		}


		public double getGain(){
			return dblGain;
		}


		public String getDescription() {
			return description;
		}
	}
	

	//TODO: Replace this with Game.isOver();
	private boolean gameOver;
	
	public void setGameOver(boolean bal){
		gameOver = bal;
	}
	
	public boolean gameOver(){
		return gameOver;
	}
	
	//	 Singleton
	private static UserInterface singleton;
	
	public static void setSingleton(UserInterface ui){
		singleton = ui;
	}
	public static UserInterface getUI (){
		return singleton;
	}
	
	public abstract void reset();
	public void resetMessages() {
		// TODO Auto-generated method stub
		
	}
	public void onPlayerDeath() {}
}