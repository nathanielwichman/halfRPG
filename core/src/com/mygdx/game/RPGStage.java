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


	private int interactableTurnLock;
	private int interactableAnimationLock;
		
	public static final int TILE_SIZE = 64;  // tile size in pixel, must match Tiled info
	
	
	//List<PlayerActor> playerActors;  // all actors the player controls
	public boolean playerFocus;  // has a playable actor been focused on
	public PlayerActor focusedPlayer;  // which playable actor is focused on (only valid if playerFocus is true)
	
	boolean executingAction;
	float actionDeltaTime = 0f;
	
	EffectsManager effects;
	DarknessManager darkness;
	PlayerOperator playerOp;
	EnemyOperator enemyOp;
	
	Strategy consideredStrategy;
	
	
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
		MapInfo mapInfo = new MapInfo(map);
		
		RPG.setCurrentMapInfo(mapInfo);
		
		effects = new EffectsManager(this);
		playerOp = new PlayerOperator(this);
		enemyOp = new EnemyOperator(this);
		
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
		
		enemyOp.addActor(a);
		
		addActor(a);
		
		//mapInfo.addCharacter(moblin);
		//playerActors.add(moblin);
		playerOp.addActor(moblin);
		addActor(moblin);
		
		playerOp.addActor(moblin2);
		//mapInfo.addCharacter(moblin2);
		//playerActors.add(moblin2);
		addActor(moblin2);
		
		
		// darkness 
		darkness = new DarknessManager(this);
		darkness.addDarknessToMap();
		darkness.clearDarkness(playerOp.getActors());
		
		interactableTurnLock = -1;
		interactableAnimationLock = -1;
		
		executingAction = false;
		
	}
	
	/**
	 * Ends the player's turn starting the enemies (NPC) turns
	 * Should probably be moved to RPG
	 */
	public void endPlayerTurn() {
		effects.clearSelectableTiles();
		effects.clearDisplayedStrategy();
		
		parent.passToUi(UiAction.REMOVE_BUTTONS, null);
		parent.passToUi(UiAction.TOGGLE_VISIBILITY, "enemyTurn");
		RPG.setCurrentGameState(GameState.ENEMY_TURN);
		interactableTurnLock = RPG.blockUserInput();
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
		RPG.unblockUserInput(interactableTurnLock);
		RPG.setCurrentGameState(GameState.PLAYER_TURN);
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
	 * Handles the UI selection of an action tile, executing that action
	 * @param a Action selected
	 * TODO: Shouldn't be passing UIActionActor around, should be seperate
	 */
	public void handleUiSelection(UiActionActor a) {
		effects.clearEffects();
		if (a.isSpecial()) {
			if (a.other == SpecialAction.MOVE) {
				displayMove(a.base);
			} else if (a.other == SpecialAction.END_TURN) {
				endPlayerTurn();
			}
		} else {
			if (!a.base.isExhausted()) {
				displayAttack(a.base, a.attack);
			}
		}
	}
	
	/**
	 * For a given character and attack, displays all selectable targets
	 * for that attack for the player to choose 
	 * @param origin The character making the attack
	 * @param a The attack action
	 */
	private void displayAttack(CharacterActor origin, AttackAction a) {
		
		Map<Vector2, Integer> tiles = Wayfinder.getAllSelectableTiles2(
				origin, origin.getCell(), a.range, RPG.getCurrentMapInfo(), ActionProperties.getDefaultAttackProperties(true));
		effects.addAttackTiles(origin, tiles, a);
	}
	
	
	/**
	 * Displays all tiles a given character can move to for the player to select
	 * @param origin the character to be moved
	 */
	private void displayMove(CharacterActor origin) {
		Map<Vector2, Integer> checkedTiles = Wayfinder.getAllSelectableTiles2(origin, origin.getCell(), origin.getSpeedRemaining(), RPG.getCurrentMapInfo(),
				new ActionProperties(EffectedByTerrain.RESPECT_TERRAIN, CanMoveThrough.PLAYER, CanSelect.TILE));   //Wayfinder.getAllMoveableTiles(target, mapInfo);
		effects.addMoveTiles(origin, checkedTiles);
	}
	
	public void executeStep(CharacterActor actor, Step nextStep) {
		System.out.println("executing step");
		if (nextStep instanceof MoveStep) {
			MoveStep ms = (MoveStep) nextStep;
			Vector2 destination = ms.stepLocation;
			actor.setPosition(destination.x * TILE_SIZE, destination.y * TILE_SIZE);
			actor.moveSpaces(ms.cost);
			darkness.clearDarkness(playerOp.getActors());
			
		} else if (nextStep instanceof ActionStep){
			ActionStep as = (ActionStep) nextStep;
			RPG.getCurrentMapInfo().handleAttack(as.actionLocation, as.action);
			actor.exhaustAction();
		} else {
			throw new IllegalArgumentException("step not supported");
		}
	}
	
	public void executeStrategy(CharacterActor actor, Strategy plan) {
		System.out.println("beggining plan");
		plan.setup();
		interactableAnimationLock = RPG.blockUserInput();
		RPG.setCurrentGameState(GameState.PLAYER_ANIMATION);
		executingAction = true;
		actionDeltaTime = 0f;
	}
	
	@Override
	public void act() {
		
		// if a character is selected, show movement icon
		effects.setCursorVisibility(playerFocus);
		
		
		
		
		Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		
		effects.mouseOver(mousePosition);
		
		
	}
	
	@Override
	public void act(float delta) {
		for (Actor a : this.getActors()) {
			a.act(delta);
		}
		
		if (executingAction) {
			//System.out.println("acting: " + actionDeltaTime);
			actionDeltaTime += delta;
			if (actionDeltaTime > .5) {
				executeStep(focusedPlayer, consideredStrategy.getNextStep());
				if (!consideredStrategy.hasNextStep()) {
					executingAction = false;
					if (RPG.getCurrentGameState() == GameState.PLAYER_ANIMATION) {
						RPG.setCurrentGameState(GameState.PLAYER_TURN);
					}
					RPG.unblockUserInput(interactableAnimationLock);
				} else {
					actionDeltaTime -= .5;
				}
			}
		}
			
		act();
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (!RPG.userInputAllowed()) { return false; }
		
		Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
		Actor selected = super.hit(stageCoords.x, stageCoords.y, true); 
		
		if (selected instanceof PlayerActor) {  // player character hit, give them focus
			//System.out.println(((PlayerActor) selected).getCell());
			giveFocus((PlayerActor) selected);
			return true;
		} else if (selected instanceof SelectableActionActor) {  // movement location selected, move to that location
			SelectableActionActor sa = (SelectableActionActor) selected;
			if (sa.isMove()) {
				Strategy s = Operator.getMovePlan(
						Wayfinder.getPathToTile(sa.getOrigin().getCell(), sa.getCell(),
								RPG.getCurrentMapInfo(), sa.getOrigin(), ActionProperties.getDefaultMoveProperty()));
				consideredStrategy = s;
				executeStrategy(sa.getOrigin(), consideredStrategy); 
			} else if (sa.isAttack()) {
				CharacterActor target = enemyOp.getActorAtCell(sa.getCell());
				if (target != null) {
					String displayText = "" + target.handleAttack(sa.getAttack());
					effects.displayDamage(target, displayText);
				}
				
				sa.getOrigin().exhaustAction();
			}
		} else {	
			removeUiActions();
			playerFocus = false;
		}
		effects.clearEffects();
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
		effects.clearEffects();
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
		RPG.getCurrentMapInfo().removeCharacter(c);
		c.remove();
		c.clear();
	}
	
	/**
	 *  Converts from a screen position z (i.e. from mouse) and returns the closest grid square
	 */
	 public static int snapToGrid(float z) {
		return (int) z / TILE_SIZE * TILE_SIZE;
	 }
}	
	 
	
	
