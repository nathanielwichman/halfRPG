package com.mygdx.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByDarkness;
import com.mygdx.game.ActionProperties.EffectedByTerrain;
import com.mygdx.game.RPG.UiAction;
import com.mygdx.game.Strategy.ActionStep;
import com.mygdx.game.Strategy.MoveStep;
import com.mygdx.game.Strategy.Step;
import com.mygdx.game.UiActionActor.SpecialAction;

/**
 * Main class for the rpg game play, including logic and input handeling.
 */
public class RPGStage extends Stage {
	private RPG parent;
	private OrthographicCamera cam; 
	private MapInfo mapInfo;  // stores info map layout and tiles
	//private TiledMap tiledMap;
	//private Integer[][] mapInfo;
	private SelectorActor selected;  // cursor info, should be moved
	private boolean playerTurn;
	private int interactableTurnLock;
	private int interactableAnimationLock;
	Vector2 lastCellMousedOver;
	Strategy consideredStrategy;
	
	public static final int TILE_SIZE = 64;  // tile size in pixel, must match Tiled info
	
	
	//List<PlayerActor> playerActors;  // all actors the player controls
	public boolean playerFocus;  // has a playable actor been focused on
	public PlayerActor focusedPlayer;  // which playable actor is focused on (only valid if playerFocus is true)
	Set<SelectableActionActor> selectableTiles;
	Set<Actor> stepActors;
	Texture stepTexture;
	Map<Vector2, Actor> darknessTiles;
	boolean executingAction;
	float actionDeltaTime = 0f;
	
	PlayerOperator playerOp;
	
	
	/**
	 * Sets up display and logic for the map, characters, user input handling etc.
	 * TODO: Replace moveableTiles with some sort of batching
	 *
	 * @param map a TiledMap representing the map to be loaded
	 * @param cam the camera associated with other stages for panning, etc.
	 */
	public RPGStage(RPG parent, TiledMap map, OrthographicCamera cam) {
		getViewport().setCamera(cam);
		this.parent = parent;
		this.cam = cam;
		this.mapInfo = new MapInfo(map);
		
		RPG.setCurrentMapInfo(mapInfo);
		
		playerOp = new PlayerOperator(this);
		
		//selected = new SelectorActor();
		//selected.setTouchable(Touchable.disabled);
		//addActor(selected);
		 
		playerFocus = false;
		focusedPlayer = null;
		
		// Set up playable actors and add one called moblin
		//playerActors = new ArrayList<>();
		PlayerActor moblin = new PlayerActor(this, CharacterInfo.getCharacterInfo("Moblin"));//new PlayerActor(new Texture(Gdx.files.internal("data/CharacterSprites/moblin.png")), "moblin", 5);
		moblin.setPosition(128, 128);
		moblin.setTouchable(Touchable.enabled);
		
		PlayerActor moblin2 = new PlayerActor(this, CharacterInfo.getCharacterInfo("Moblin"));//new PlayerActor(new Texture(Gdx.files.internal("data/CharacterSprites/moblin.png")), "moblin2", 5);
		moblin2.setPosition(128, 64);
		moblin2.setTouchable(Touchable.enabled);
		
		CharacterActor a = new EnemyActor(this, CharacterInfo.getCharacterInfo("SkeletonPunchingBag"));
		//CharacterActor a = new CharacterActor(CharacterInfo.getCharacterInfo("SkeletonPunchingBag"));
		a.setPosition(192, 64);
		a.setTouchable(Touchable.enabled);
		
		mapInfo.addCharacter(a);
		addActor(a);
		
		//mapInfo.addCharacter(moblin);
		//playerActors.add(moblin);
		playerOp.addActor(moblin);
		addActor(moblin);
		
		playerOp.addActor(moblin2);
		//mapInfo.addCharacter(moblin2);
		//playerActors.add(moblin2);
		addActor(moblin2);
		
		
		// add textures for the move-able tiles image
		selectableTiles = new HashSet<>();
		//Systemom.out.println(moblin.isTouchable());
		
		// darkness 
		darknessTiles = new HashMap<>();
		addDarknessToMap(mapInfo);
		clearDarkness();
		
		playerTurn = true;
		interactableTurnLock = -1;
		interactableAnimationLock = -1;
		
		// move this all to its own class
		stepActors = new HashSet<>();
		Pixmap pm = new Pixmap(26, 26, Format.RGBA8888);
		pm.setBlending(Blending.None);
		pm.setColor(Color.WHITE);
		pm.fillCircle(13, 13, 10);
		stepTexture = new Texture(pm);
		lastCellMousedOver = null;
		executingAction = false;
		
	}
	
	/**
	 * Ends the player's turn starting the enemies (NPC) turns
	 * Should probably be moved to RPG
	 */
	public void endPlayerTurn() {
		clearSelectableTiles();
		parent.passToUi(UiAction.REMOVE_BUTTONS, null);
		parent.passToUi(UiAction.TOGGLE_VISIBILITY, "enemyTurn");
		playerTurn = false;
		interactableTurnLock = parent.blockUserInput();
		Timer t = new Timer();
		t.scheduleTask(new Task() {
			@Override
			public void run() {
				endEnemyTurn();
			}
		}, (float)Math.random() * 5);
				
	}
	
	/**
	 * Ends the enemies turn starting the player's turn
	 * Should be moved to RPG
	 */
	public void endEnemyTurn() {
		playerOp.refreshAll();
		playerTurn = true;
		parent.unblockUserInput(interactableTurnLock);
		parent.passToUi(UiAction.TOGGLE_VISIBILITY, "enemyTurn");
	}
	
	/**
	 * Tells the UI to remove all player related ui symbols
	 */
	public void removeUiActions() {
		parent.passToUi(UiAction.REMOVE_BUTTONS, null);
	}
	
	/**
	 * Tells the UI to add a character's basic attack as a selectable icon
	 * @param origin
	 */
	public void addUiAction(PlayerActor origin) {
		parent.passToUi(UiAction.ADD_BUTTON, origin, origin.getBasicAttack());
	}
	
	/**
	 * Clears all moveable tile actors currently created,
	 * removing them from the set and clearing their actor's
	 */
	private void clearSelectableTiles() {
		for (Actor tile : selectableTiles) {
			tile.remove();
			tile.clear();
		}
		clearStrategySteps();
		
		selectableTiles.clear();
	}
	
	/**
	 * Handles the UI selection of an action tile, executing that action
	 * @param a Action selected
	 * TODO: Shouldn't be passing UIActionActor around, should be seperate
	 */
	public void handleUiSelection(UiActionActor a) {
		clearSelectableTiles();
		if (a.isSpecial()) {
			if (a.other == SpecialAction.MOVE) {
				displayMove(a.base);
			} else if (a.other == SpecialAction.END_TURN) {
				endPlayerTurn();
			}
		} else {
			displayAttack(a.base, a.attack);
		}
	}
	
	/**
	 * For a given character and attack, displays all selectable targets
	 * for that attack for the player to choose 
	 * @param origin The character making the attack
	 * @param a The attack action
	 */
	private void displayAttack(CharacterActor origin, AttackAction a) {
		//test
		Set<Vector2> targets = new HashSet<>();
		targets.add(new Vector2(3,1));
		targets.add(new Vector2(4,1));
		SortedMap<Integer, Vector2> checked2Tiles = Wayfinder.getPathToTiles(origin.getCell(),
				targets, mapInfo, origin, new ActionProperties());
		
		
		Map<Vector2, Integer> checkedTiles = new HashMap<>();
		System.out.println(Operator.getMovePlan(checked2Tiles));
		
		for (Integer i : checked2Tiles.keySet()) {
			Vector2 vect = checked2Tiles.get(i);
			//System.out.println(i + ": " + i);
			checkedTiles.put(vect,i);
		}
		
		// test
		
		//Map<Vector2, Integer> checkedTiles = Wayfinder.getAllSelectableTiles2(origin, origin.getCell(),
		//		a.range, mapInfo, a.p);
		Texture attackableTexture = SelectableActionActor.getAttackableTexture();
		
		for (Vector2 position : checkedTiles.keySet()) {
			SelectableActionActor newTile = new SelectableActionActor(
					origin, attackableTexture, a);
			newTile.setBounds(newTile.getX(), newTile.getY(),
					attackableTexture.getWidth(), attackableTexture.getHeight());
			newTile.setPosition(position.x * TILE_SIZE, position.y * TILE_SIZE);
			newTile.setTouchable(Touchable.enabled);
			selectableTiles.add(newTile);
			addActor(newTile);
					
		}
	}
	
	
	/**
	 * Displays all tiles a given character can move to for the player to select
	 * @param origin the character to be moved
	 */
	private void displayMove(CharacterActor origin) {
		Map<Vector2, Integer> checkedTiles = Wayfinder.getAllSelectableTiles2(origin, origin.getCell(), origin.getSpeedRemaining(), mapInfo,
				new ActionProperties(EffectedByTerrain.RESPECT_TERRAIN, CanMoveThrough.PLAYER, CanSelect.TILE));   //Wayfinder.getAllMoveableTiles(target, mapInfo);
		Texture moveableTexture = SelectableActionActor.getMoveableTexture();
		
		for (Vector2 position : checkedTiles.keySet()) {
			SelectableActionActor a = new SelectableActionActor(
					origin, moveableTexture, checkedTiles.get(position));
			a.setBounds(a.getX(), a.getY(), moveableTexture.getWidth(), moveableTexture.getHeight());
			a.setPosition(position.x * TILE_SIZE, position.y * TILE_SIZE);
			a.setTouchable(Touchable.enabled);
			selectableTiles.add(a);  // add to set so we can clear when no longer needed
			addActor(a);
		}
		
	}
	
	public void executeStep(CharacterActor actor, Step nextStep) {
		System.out.println("executing step");
		if (nextStep instanceof MoveStep) {
			MoveStep ms = (MoveStep) nextStep;
			Vector2 destination = ms.stepLocation;
			actor.setPosition(destination.x * TILE_SIZE, destination.y * TILE_SIZE);
			actor.moveSpaces(ms.cost);
			clearDarkness();
			
		} else if (nextStep instanceof ActionStep){
			ActionStep as = (ActionStep) nextStep;
			mapInfo.handleAttack(as.actionLocation, as.action);
			actor.exhaustAction();
		} else {
			throw new IllegalArgumentException("step not supported");
		}
	}
	
	public void executeStrategy(CharacterActor actor, Strategy plan) {
		System.out.println("beggining plan");
		plan.reset();
		interactableAnimationLock = parent.blockUserInput();
		executingAction = true;
		actionDeltaTime = 0f;
	}
	
	@Override
	public void act() {
		if (playerFocus) {  // if a character has been selected, show movement icon
			selected.setVisible(true);
		} else {
			selected.setVisible(false);
		}
		
		
		if (parent.userInputAllowed()) {
			Vector2 mousePosition = screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
			Vector2 gridPosition = new Vector2((int) mousePosition.x/64, (int) mousePosition.y/64);
			if (!gridPosition.epsilonEquals(lastCellMousedOver)) {
				mouseOverGrid(gridPosition, super.hit(mousePosition.x, mousePosition.y, true)); 
				lastCellMousedOver = gridPosition;
			}
		}
		
		// draw selection icon on the grid mouse is at
		float x = Gdx.input.getX();
		float y = Gdx.input.getY();
		Vector2 drawPosition = screenToStageCoordinates(new Vector2(x, y)); //etLocationOnScreen(x, y);
		selected.setPosition(snapToGrid(drawPosition.x), snapToGrid(drawPosition.y));
		//selected.setPosition(x, y);
	}
	
	@Override
	public void act(float delta) {

		if (executingAction) {
			//System.out.println("acting: " + actionDeltaTime);
			actionDeltaTime += delta;
			if (actionDeltaTime > .5) {
				executeStep(focusedPlayer, consideredStrategy.getNextStep());
				if (!consideredStrategy.hasNextStep()) {
					executingAction = false;
					parent.unblockUserInput(interactableAnimationLock);
				} else {
					actionDeltaTime -= .5;
				}
			}
		}
			
		act();
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (!parent.userInputAllowed()) { return false; }
		
		Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
		Actor selected = super.hit(stageCoords.x, stageCoords.y, true); 
		
		if (selected instanceof PlayerActor) {  // player character hit, give them focus
			//System.out.println(((PlayerActor) selected).getCell());
			giveFocus((PlayerActor) selected);
			return true;
		} else if (selected instanceof SelectableActionActor) {  // movement location selected, move to that location
			SelectableActionActor sa = (SelectableActionActor) selected;
			if (sa.isMove()) {
				executeStrategy(sa.getOrigin(), consideredStrategy); 
				//focusedPlayer.setPosition(selected.getX(), selected.getY());
				//focusedPlayer.setMovesLeft(sa.getCost());
				//clearDarkness(20);
			} else if (sa.isAttack()) {
				mapInfo.handleAttack(new Vector2(((int) selected.getX())/64,
						((int) selected.getY())/64), sa.getAttack());
				focusedPlayer.exhaustAction();
			}
		} else {	
			removeUiActions();
			playerFocus = false;
		}
		clearSelectableTiles();
		return true;
	}
	
	/**
	 * Gives the focus to a character to make actions, 
	 * displaying possible actions on the UI
	 * @param origin The selected player character
	 */
	public void giveFocus(PlayerActor origin) {
		playerFocus = true;
		focusedPlayer = origin;
		clearSelectableTiles();
		displayMove(origin);
		removeUiActions();
		if (!origin.isExhausted())
			addUiAction(origin);
	}
	
	/**
	 * Removes a character from the map and stage
	 * @param c Character to remove, probably has issues if not 
	 * actually on map/stage
	 */
	public void removeCharacter(CharacterActor c) {
		mapInfo.removeCharacter(c);
		c.remove();
		c.clear();
	}
	
	/**
	 *  Converts from a screen position z (i.e. from mouse) and returns the closest grid square
	 */
	 private int snapToGrid(float z) {
		return (int) z / TILE_SIZE * TILE_SIZE;
	 }
	
	 /**
	  * Removes darkness from all tiles inhabited by playerActors as well as all tiles within their
	  * line of sight up to range tiles. Should be called on game start and every time a playerActor's 
	  * position update
	  * TODO: Add method for only updating one actor at once (save computation)
	  * @param range number of tiles away to clear darkness from (if in actor line of sight)
	  */
	 private void clearDarkness() {
		 for (CharacterActor p : playerOp.getActors()) {
			 clearDarkness(p);
		 }
	 }
	 
	 private void clearDarkness(CharacterActor p) {
		 int range = p.getVisionDistance();
		 removeDarkness(p.getCell());
		 Set<Vector2> toClear = Wayfinder.getAllSelectableTiles2(
				 p, p.getCell(), range, mapInfo, new ActionProperties(
						 EffectedByDarkness.IGNORE, EffectedByTerrain.IGNORE_TERRAIN,
						 CanSelect.WALLS, CanSelect.ENEMY, CanSelect.TILE,
						 CanMoveThrough.CHARACTER)).keySet();
		 toClear.removeAll(Wayfinder.getAllOutOfSight(p.getCell(), toClear, mapInfo));
		 for (Vector2 v : toClear) {
			 removeDarkness(v);
		 }
	 }
	 
	 /**
	  * Removes darkness tiles from a specific position, both visually and from mapInfo
	  * @param position tile to remove darkness from
	  * @return true if darkness tile was removed, false if not (wasn't darkness to begin with)
	  */
	 private boolean removeDarkness(Vector2 position) {
		 if (darknessTiles.containsKey(position)) {
			 mapInfo.removeDarkness(position);
			 Actor d = darknessTiles.get(position);
			 darknessTiles.remove(position);
			 d.remove();
			 d.clear();
			 return true;
		 }
		 return false;
	 }
	 
	 /**
	  * Adds darkness tiles to the entire map, updating mapInfo and adding darkness tiles over
	  * entire visual map
	  * @param map
	  */
	 private void addDarknessToMap(MapInfo map) {
		 Vector2 bounds = map.getMapSize();
		 final Texture t = new Texture(Gdx.files.internal("data/MiscSprites/darkness.png"));
		 for (int x = 0; x < bounds.x; x++) {
			 for (int y = 0; y < bounds.y; y++) {
				 Actor darkness = new Actor() {
					 Texture texture = t;
					
					 @Override
					 public void draw(Batch batch, float alpha) {
						 batch.draw(texture,  getX(),  getY());
					 }
					
				 };
				 darkness.setBounds(darkness.getX(), darkness.getY(), t.getWidth(), t.getHeight());
				 darkness.setPosition(x * TILE_SIZE, y * TILE_SIZE);
				 darkness.setTouchable(Touchable.disabled);
				 darknessTiles.put(new Vector2(x,y), darkness);
				 addActor(darkness);
				 map.addDarkness(); 
			}
		}
	}
	 
	public void mouseOverGrid(Vector2 gridPosition, Actor mousedOver) {
		clearStrategySteps();
		if (mousedOver instanceof SelectableActionActor) {
			SelectableActionActor sa = (SelectableActionActor) mousedOver;
			if (sa.isMove()) {
				Strategy pathToPosition = Operator.getMovePlan(
						Wayfinder.getPathToTile(sa.getOrigin().getCell(),
												gridPosition, mapInfo, sa.getOrigin(),
												ActionProperties.getDefaultMoveProperty()));
				displayStrategy(pathToPosition);
				consideredStrategy = pathToPosition;
			}
		}
	}
	 
	
	public void clearStrategySteps() {
		for (Actor step : stepActors) {
			step.clear();
			step.remove();
		}
		stepActors.clear();
		if (!executingAction) 
			consideredStrategy = null;
	}
	
	public void displayStrategy(Strategy s) {
		s.reset();
		while (s.hasNextStep()) {
			Step nextStep = s.getNextStep();
			assert nextStep instanceof MoveStep;  // for now
			Vector2 nextMove = ((MoveStep) nextStep).stepLocation;
			
			Actor newStepActor = new Actor() {
				Texture t = stepTexture;
				
				 @Override
				 public void draw(Batch batch, float alpha) {
					 batch.draw(t,  getX(),  getY());
				 }
				
			};
			newStepActor.setBounds(newStepActor.getX(), newStepActor.getY(),
					stepTexture.getWidth(), stepTexture.getHeight());
			newStepActor.setPosition(nextMove.x * TILE_SIZE + TILE_SIZE/2-stepTexture.getWidth()/2, nextMove.y * TILE_SIZE + TILE_SIZE/2-stepTexture.getHeight()/2);
			newStepActor.setTouchable(Touchable.disabled);
			stepActors.add(newStepActor);
			addActor(newStepActor);
				
		}
		
	
	}
}
	
	
