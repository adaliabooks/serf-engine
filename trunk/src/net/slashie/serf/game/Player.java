package net.slashie.serf.game;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.slashie.serf.action.Action;
import net.slashie.serf.action.Actor;
import net.slashie.serf.action.AwareActor;
import net.slashie.serf.baseDomain.AbstractItem;
import net.slashie.serf.fov.FOV;
import net.slashie.serf.level.AbstractCell;
import net.slashie.serf.level.AbstractFeature;
import net.slashie.utils.Position;

public abstract class Player extends AwareActor {
	private SworeGame game;
	private boolean doNotRecordScore = false;
		
	// Attributes
	private String name;
	
	//Status
    private GameSessionInfo gameSessionInfo;
	
	//Relationships
	private transient PlayerEventListener playerEventListener;

	public void addHistoricEvent(String description){
		gameSessionInfo.addHistoryItem(description);
	}
	
	public void informPlayerEvent(int code){
		if (playerEventListener != null)
			playerEventListener.informEvent(code);
	}

	public void informPlayerEvent(int code, Object param){
		playerEventListener.informEvent(code, param);
	}


	private Hashtable<String, Equipment> inventory = new Hashtable<String, Equipment>();

	public void addItem(AbstractItem toAdd, int quantity){
		if (!canCarry(toAdd, quantity)){
			if (level != null)
				level.addMessage("You can't carry anything more");
			return;
		}
		beforeItemsAddition(toAdd, quantity);
		
		if (canCarry(toAdd, quantity)){
			String toAddID = toAdd.getFullID();
			Equipment equipmentx = inventory.get(toAddID);
			if (equipmentx == null)
				inventory.put(toAddID, new Equipment(toAdd, quantity));
			else
				equipmentx.increaseQuantity(quantity);
		}
	}
	
	public void addAllItems(List<Equipment> items){
		for (Equipment equipment: items){
			addItem(equipment.getItem(), equipment.getQuantity());
		}
	}
	
	private void removeItem(Equipment toRemove){
		inventory.remove(toRemove.getItem().getFullID());
	}
	
	protected void removeAllItems(){
		inventory.clear();
	}
	
	public boolean hasItem (AbstractItem item){
		return inventory.containsKey(item.getFullID());
	}
	
	public boolean hasItemByID (String itemID){
		return inventory.containsKey(itemID);
	}
	
	
	public abstract boolean canCarry(AbstractItem item, int quantity);
	
	public List<Equipment> getInventory(){
		List<Equipment> ret = new ArrayList<Equipment>();
		Enumeration<Equipment> x = inventory.elements();
		while (x.hasMoreElements())
			ret.add(x.nextElement());
		return ret;
	}


	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public PlayerEventListener getPlayerEventListener() {
		return playerEventListener;
	}

	public void setPlayerEventListener(PlayerEventListener value) {
		playerEventListener = value;
	}

	public GameSessionInfo getGameSessionInfo() {
		return gameSessionInfo;
	}

	public void setGameSessionInfo(GameSessionInfo value) {
		gameSessionInfo = value;
	}

	public void updateStatus(){
		for (int i = 0; i < counteredItems.size(); i++){
			AbstractItem item = counteredItems.get(i);
			item.reduceCounters(this);
			if (!item.hasCounters()){
				counteredItems.remove(item);
			}
		}
		super.updateStatus();
	}

	//Callback
	public void onItemStep(AbstractItem item){};
	
	/**
	 * 
	 * @deprecated Use beforeItemsAddition instead
	 */
	public void beforeItemAddition(AbstractItem item){};
	public void beforeItemsAddition(AbstractItem item, int quantity){};
	public void onItemsStep(List<AbstractItem> items){};
	public void onNullDestination(){};
	public void onSolidDestination(){};
	public void onActorStep(Actor aActor){};
	public void onFeatureStep(AbstractFeature destinationFeature){};
	public void onCellStep(AbstractCell cell){};

	public Position getFreeSquareAround(Position destinationPoint){
		Position tryP = Position.add(destinationPoint, Action.directionToVariation(Action.UP));
		if (level.getMapCell(tryP) != null && !level.getMapCell(tryP).isSolid()){
			return tryP;
		} 
		
		tryP = Position.add(destinationPoint, Action.directionToVariation(Action.DOWN));
		if (level.getMapCell(tryP) != null && !level.getMapCell(tryP).isSolid()){
			return tryP;
		}
		
		tryP = Position.add(destinationPoint, Action.directionToVariation(Action.LEFT));
		if (level.getMapCell(tryP) != null && !level.getMapCell(tryP).isSolid()){
			return tryP;
		}
					
		tryP = Position.add(destinationPoint, Action.directionToVariation(Action.RIGHT));
		if (level.getMapCell(tryP) != null && !level.getMapCell(tryP).isSolid()){
			return tryP;
		}
		return null;
	}
	
	public void landOn (Position destinationPoint){
		AbstractCell destinationCell = level.getMapCell(destinationPoint);
        if (destinationCell == null){
       		onNullDestination();
			return;
       	}
        onCellStep(destinationCell);
        setPosition(destinationPoint);
        
		if (destinationCell.isSolid()){
			Position tryp = getFreeSquareAround(destinationPoint);
			if (tryp == null){
        		onSolidDestination();
				return;
			} else {
				landOn(tryp);
				return;
			}
		}

		List<AbstractItem> destinationItems = level.getItemsAt(destinationPoint);
		if (destinationItems != null){
			if (destinationItems.size() == 1)
				onItemStep(destinationItems.get(0));
			else
				onItemsStep(destinationItems);
		}
		
		Actor aActor = level.getActorAt(destinationPoint);
		if (aActor != null)
			onActorStep(aActor);
		
		AbstractFeature destinationFeature = level.getFeatureAt(destinationPoint);
		
		while (destinationFeature != null){
			onFeatureStep(destinationFeature);
			AbstractFeature previousFeature = destinationFeature;
			destinationFeature = level.getFeatureAt(destinationPoint);
			destinationFeature.onStep(this);
			if (previousFeature == destinationFeature)
				break;
		}
			
		if (level.isExit(getPosition())){
			String exit = level.getExitOn(getPosition());
			if (exit.equals("_START") || exit.startsWith("#")){
				
			} else {
				informPlayerEvent(Player.EVT_GOTO_LEVEL, exit);
			}
			
		}
	}

	public void reduceQuantityOf(AbstractItem what){
		reduceQuantityOf(what.getFullID(), 1);
	}
	
	public void reduceQuantityOf(String itemId, int quantity){
		Equipment equipment = inventory.get(itemId);
		equipment.reduceQuantity(quantity);
		if (equipment.isEmpty())
			removeItem(equipment);
	}

	public void reduceQuantityOf(AbstractItem what, int quantity){
		reduceQuantityOf(what.getFullID(), quantity);
	}

	public final static int DEATH = 0, WIN = 1;

	public final static int
		EVT_CHAT = 11, 
		EVT_GOTO_LEVEL = 15;
	
	public String getStatusString(){
		return "";
	}

	public abstract int getSightRange();
	
	public abstract int getDarkSightRange();

	public void setFOV(FOV fov){
		this.fov = fov;
	}
	
	private transient FOV fov;
	
	public void see(){
		fov.startCircle(getLevel(), getPosition().x, getPosition().y, getDarkSightRange());
	}
	
	public void darken(){
		level.darken();
	}
	
	 public boolean sees(Position p){
		 return level.isVisible(p.x, p.y, p.z);
	 }
	 
	 public boolean sees(Actor m){
		 return sees(m.getPosition());
	 }
	 

	public boolean isDoNotRecordScore() {
		return doNotRecordScore;
	}

	public void setDoNotRecordScore(boolean doNotRecordScore) {
		this.doNotRecordScore = doNotRecordScore;
	}
	
	public SworeGame getGame() {
		return game;
	}

	public void setGame(SworeGame game) {
		this.game = game;
	}
	
	
	public int stareActor(Actor who){
		if (who.getPosition().z != getPosition().z)
			return -1;
		if (who.wasSeen()){
			Position pp = who.getPosition();
			if (pp.x == getPosition().x){
				if (pp.y > getPosition().y){
					return Action.DOWN;
				} else {
                     return Action.UP;
				}
			} else
			if (pp.y == getPosition().y){
				if (pp.x > getPosition().x){
					return Action.RIGHT;
				} else {
					return Action.LEFT;
				}
			} else
			if (pp.x < getPosition().x){
				if (pp.y > getPosition().y)
					return Action.DOWNLEFT;
				else
					return Action.UPLEFT;
			} else {
                if (pp.y > getPosition().y)
					return Action.DOWNRIGHT;
				else
					return Action.UPRIGHT;
			}
		}
		return -1;
	}
	
	private List<AbstractItem> counteredItems = new ArrayList<AbstractItem>();
	
	public void addCounteredItem(AbstractItem i){
		counteredItems.add(i);
	}
	
	public abstract String getSaveFilename();
	
	public abstract List<? extends AbstractItem> getEquippedItems();
	
	@Override
	public String getDescription() {
		return name;
	}
	
}