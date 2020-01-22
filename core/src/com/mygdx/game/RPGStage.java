package com.mygdx.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByDarkness;
import com.mygdx.game.ActionProperties.EffectedByTerrain;
import com.mygdx.game.RPG.UiAction;
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

	
	public static final int TILE_SIZE = 64;  // tile size in pixel, must match Tiled info
	
	
	List<PlayerActor> playerActors;  // all actors the player controls
	public boolean playerFocus;  // has a playable actor been focused on
	public PlayerActor focusedPlayer;  // which playable actor is focused on (only valid if playerFocus is true)
	Set<SelectableActionActor> selectableTiles;
	Map<Vector2, Actor> darknessTiles;
	
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
		selected = new SelectorActor();
		selected.setTouchable(Touchable.disabled);
		addActor(selected);
		 
		playerFocus = false;
		focusedPlayer = null;
		
		// Set up playable actors and add one called moblin
		playerActors = new ArrayList<>();
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
		
		mapInfo.addCharacter(moblin);
		playerActors.add(moblin);
		addActor(moblin);
		
		mapInfo.addCharacter(moblin2);
		playerActors.add(moblin2);
		addActor(moblin2);
		
		
		// add textures for the move-able tiles image
		selectableTiles = new HashSet<>();
		//Systemom.out.println(moblin.isTouchable());
		
		// darkness 
		darknessTiles = new HashMap<>();
		addDarknessToMap(mapInfo);
		clearDarkness(5);
		
		
	}
	
	public void removeUiActions() {
		parent.passToUi(UiAction.REMOVE_BUTTONS, null);
	}
	
	public void addUiAction(PlayerActor origin) {
		parent.passToUi(UiAction.ADD_BUTTON, new UiActionActor(
				new Texture(Gdx.files.internal("data/UiData/attackIcon.png")),
				origin, origin.getBasicAttack()));
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
		for (Actor tile : selectableTiles) {
			tile.remove();
			tile.clear();
		}
		selectableTiles.clear();
	}
	
	public void handleUiSelection(UiActionActor a) {
		clearSelectableTiles();
		if (a.isSpecial()) {
			if (a.other == SpecialAction.MOVE) {
				displayMove(a.base);
			}
		} else {
			displayAttack(a.base, a.attack);
		}
	}
	
	private void displayAttack(CharacterActor origin, AttackAction a) {
		Map<Vector2, Integer> checkedTiles = Wayfinder.getAllSelectableTiles2(origin, origin.getCell(),
				a.range, mapInfo, a.p);
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
	
	@Override
	public void act() {
		if (playerFocus) {  // if a character has been selected, show movement icon
			selected.setVisible(true);
		} else {
			selected.setVisible(false);
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
		act();
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
		Actor selected = super.hit(stageCoords.x, stageCoords.y, true); 
		
		if (selected instanceof PlayerActor) {  // player character hit, give them focus
			//System.out.println(((PlayerActor) selected).getCell());
			giveFocus((PlayerActor) selected);
			return true;
		} else if (selected instanceof SelectableActionActor) {  // movement location selected, move to that location
			SelectableActionActor sa = (SelectableActionActor) selected;
			if (sa.isMove()) {
				focusedPlayer.setPosition(selected.getX(), selected.getY());
				clearDarkness(5);
			} else if (sa.isAttack()) {
				mapInfo.handleAttack(new Vector2(((int) selected.getX())/64,
						((int) selected.getY())/64), sa.getAttack());
			}
		}	
		
		removeUiActions();
		playerFocus = false;
		clearSelectableTiles();
		return true;
	}
	 
	public void giveFocus(PlayerActor origin) {
		playerFocus = true;
		focusedPlayer = origin;
		clearSelectableTiles();
		displayMove(origin);
		removeUiActions();
		addUiAction(origin);
	}
	
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
	 private void clearDarkness(int range) {
		 for (PlayerActor p : playerActors) {
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
}
	
	
