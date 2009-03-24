package net.slashie.serf.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


import net.slashie.serf.SworeException;
import net.slashie.serf.action.Actor;
import net.slashie.serf.fov.FOV;
import net.slashie.serf.level.AbstractLevel;
import net.slashie.serf.level.Dispatcher;
import net.slashie.serf.ui.CommandListener;
import net.slashie.serf.ui.UISelector;
import net.slashie.serf.ui.UserInterface;
import net.slashie.utils.FileUtil;
import net.slashie.utils.SerializableChecker;


public abstract class SworeGame implements CommandListener, PlayerEventListener, Serializable{
	//Configuration
	private transient UserInterface ui;
	private transient UISelector uiSelector;
	
	private Dispatcher dispatcher;
	private Player player;
	private AbstractLevel currentLevel;
	private boolean canSave;
	
	public void setCanSave(boolean vl){
		canSave = vl;
	}
	
	public boolean canSave(){
		return canSave;
	}
	
	private Hashtable<String, AbstractLevel> storedLevels = new Hashtable<String, AbstractLevel>();
	
	private boolean endGame;
	
	private long turns;
	
	public void commandSelected (int commandCode){
		if (commandCode == CommandListener.QUIT){
			finishGame();
		} else if (commandCode == CommandListener.SAVE) {
			if (canSave()){
				freezeUniqueRegister();
				saveGame(player);
				exitGame();
			}
		}
	}
	
	public abstract String getFirstMessage(Actor player);

	//Callback
	public abstract void onGameStart(int gameType);
	public abstract void beforeGameStart();
	public abstract void beforePlayerAction();
	public abstract void afterPlayerAction();
	public abstract void onGameResume();
	public abstract void onGameOver();
	public abstract void onGameWon();
	public abstract void onLevelLoad(AbstractLevel level);
	
	public abstract String getDeathMessage();
	public abstract Player generatePlayer(int gameType, SworeGame sworeGame);
	public abstract AbstractLevel createLevel(String levelID);

	
	private void run(){
		player.setFOV(new FOV());
		player.getLevel().addMessage(getFirstMessage(player));
		ui.refresh();
		beforeGameStart();
		while (!endGame){
			Actor actor = dispatcher.getNextActor();
            if (actor == player){
            	player.darken();
            	player.see();
            	ui.refresh();
				player.getGameSessionInfo().increaseTurns();
				player.getLevel().checkUnleashers(this);
			}
            if (endGame)
            	break;
            if (actor == player)
            	beforePlayerAction();
			actor.act();
			if (actor == player)
				afterPlayerAction();
			if (endGame)
            	break;
			actor.getLevel().getDispatcher().returnActor(actor);
			
			if (actor == player){
				if (currentLevel != null)
					currentLevel.updateLevelStatus();
				turns++;
			}
		}
	}
	 	
	
	
	public void resume(){
		player.setSelector(uiSelector);
		ui.setPlayer(player);
		uiSelector.setPlayer(player);
		ui.addCommandListener(this);
		ui.setGameOver(false);
		player.getLevel().addActor(player);
		player.setPlayerEventListener(this);
		endGame = false;
		turns = player.getGameSessionInfo().getTurns();
		syncUniqueRegister();
		onGameResume();
		run();
	}
	
	public void setPlayer(Player p){
		player = p;
		player.setLevel(currentLevel);
		player.setFOV(new FOV());
		currentLevel.setPlayer(player);
		if (player.getGameSessionInfo() == null)
			player.setGameSessionInfo(new GameSessionInfo());
		player.setSelector(uiSelector);
		ui.setPlayer(player);
		uiSelector.setPlayer(player);
		player.setPlayerEventListener(this);
		player.setGame(this);
	}
	
	public void newGame(int gameType){
		player = generatePlayer(gameType, this);
		player.setGameSessionInfo(new GameSessionInfo());
		player.setSelector(uiSelector);
		ui.setPlayer(player);
		uiSelector.setPlayer(player);
		ui.addCommandListener(this);
		ui.setGameOver(false);
		player.setPlayerEventListener(this);
		onGameStart(gameType);
		turns = 0;
		run();
	}
	
	
    public void informEvent(int code){
    	informEvent(code, null);
    }

 	public void informEvent(int code, Object param){
		switch (code){
			case Player.DEATH:
				ui.refresh();
				ui.showSystemMessage(getDeathMessage() +  " [Press Space to continue]");
				finishGame();
				break;
			case Player.EVT_GOTO_LEVEL:
				loadLevel((String)param);
				break;
		}
	}

	private void finishGame(){
		if (!player.isDoNotRecordScore()){
			onGameOver();
		}
		exitGame();
	}
	
	public void exitGame(){
		//levelNumber = -1;
		currentLevel.disableTriggers();
		currentLevel = null;
		ui.removeCommandListener(this);
		ui.setGameOver(true);
		player.setPlayerEventListener(null);
		endGame = true;
	}

	
	public void wonGame(){
		onGameWon();
		finishGame();
	}
	
	protected void loadLevel(String levelID) {
		String formerLevelID = null; 
		if (currentLevel != null){
			formerLevelID = currentLevel.getID();
			AbstractLevel storedLevel = storedLevels.get(formerLevelID);
			if (storedLevel == null){
				storedLevels.put(formerLevelID, currentLevel);
			}  
		} else {
			formerLevelID = "_BACK";
		}
		AbstractLevel storedLevel = storedLevels.get(levelID);
		if (storedLevel != null) {
			currentLevel = storedLevel;
			player.setPosition(currentLevel.getExitFor(formerLevelID));
			onLevelLoad(currentLevel);
		} else {
			try {
				currentLevel = createLevel(levelID);
				currentLevel.setPlayer(player);
				ui.setPlayer(player);
				uiSelector.setPlayer(player);
				onLevelLoad(currentLevel);
			} catch (SworeException sworee){
				crash("Error while creating level "+levelID, sworee);
			}
		}
		player.setLevel(currentLevel);
		
		if(currentLevel.getExitFor(formerLevelID) != null){
			player.setPosition(currentLevel.getExitFor(formerLevelID));
		} else if(currentLevel.getExitFor("_START") != null) {
			player.setPosition(currentLevel.getExitFor("_START"));
		}
		
		dispatcher = currentLevel.getDispatcher();
		
		if (!dispatcher.contains(player)){
			dispatcher.addActor(player);
		}
		ui.levelChange();
	}
	
	public void setLevel(AbstractLevel level){
		currentLevel = level;
		player.setLevel(level);
		dispatcher = currentLevel.getDispatcher();
		ui.levelChange();
		
		
	}
	public Player getPlayer(){
		return player;
	}
	
	public static String getVersion(){
		return "0.3";
	}
	
	public void setInterfaces(UserInterface pui, UISelector ps){
		ui = pui;
		uiSelector = ps;
	}
	
	public static void crash(String message, Throwable exception){
    	System.out.println("SWORE runtime "+ getVersion()+": Unrecoverable Error");
        System.out.println(exception.getMessage());
        exception.printStackTrace();
        System.exit(-1);
    }
	
	public static void crash(String message){
		System.out.println("SWORE runtime "+ getVersion()+": Unrecoverable Error");
        System.out.println(message);
        System.exit(-1);
    }
	
	private static List<String> reports = new ArrayList<String>();
	
	public static void addReport(String report){
		reports.add(report);
	}
	
	public static List<String> getReports(){
		return reports;
	}

	private static List<String> uniqueRegister = new ArrayList<String>();
	private List<String> uniqueRegisterObjectCopy = new ArrayList<String>();
	
	public void syncUniqueRegister(){
		uniqueRegister = uniqueRegisterObjectCopy;
	}
	
	public void freezeUniqueRegister(){
		uniqueRegisterObjectCopy = uniqueRegister;
	}
	
	public static boolean wasUniqueGenerated(String itemID){
		return uniqueRegister.contains(itemID);
	}
	
	public static void registerUniqueGenerated (String itemID){
		uniqueRegister.add(itemID);
	}
	
	public void saveGame(Player p) throws SworeException{
		String filename = "savegame/"+p.getName()+".sav";
		p.setSelector(null);
		try {
			SerializableChecker sc = new SerializableChecker();
			sc.writeObject(this);
			sc.close();
			
			File saveDirectory = new File("savegame");
			if (saveDirectory.exists()){
				if (!saveDirectory.isDirectory()){
					throw new SworeException("Save directory 'savegame' can't be created");
				}
			} else {
				if (!saveDirectory.mkdir()){
					throw new SworeException("Save directory 'savegame' can't be created");
				}
			}
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filename));
			os.writeObject(this);
			os.close();
			
			
		} catch (IOException ioe){
			crash("Error saving the game", ioe);
		}
	}
	
	public static void permadeath(Player p){
		String filename = "savegame/"+p.getSaveFilename()+".sav";
		if (FileUtil.fileExists(filename)) {
			FileUtil.deleteFile(filename);
		}
	}
	
}