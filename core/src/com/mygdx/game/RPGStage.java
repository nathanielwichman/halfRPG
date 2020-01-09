package com.mygdx.game;

import java.util.ArrayList;
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

/**
 * Main class for the rpg game play, including logic and input handeling.
 */
public class RPGStage extends Stage {
	private OrthographicCamera cam; 
	private MapInfo mapInfo;  // stores info map layout and tiles
	//private TiledMap tiledMap;
	//private Integer[][] mapInfo;
	private SelectorActor selected;  // cursor info, should be moved
	private Texture moveableTexture;  // same
	
	public static final int TILE_SIZE = 64;  // tile size in pixel, must match Tiled info
	
	
	List<PlayerActor> playerActors;  // all actors the player controls
	public boolean playerFocus;  // has a playable actor been focused on
	public PlayerActor focusedPlayer;  // which playable actor is focused on (only valid if playerFocus is true)
	Set<Actor> moveableTiles;  // saved info about display, should be changed

	/**
	 * Sets up display and logic for the map, characters, user input handling etc.
	 * TODO: Replace moveableTiles with some sort of batching
	 *
	 * @param map a TiledMap representing the map to be loaded
	 * @param cam the camera associated with other stages for panning, etc.
	 */
	public RPGStage(TiledMap map, OrthographicCamera cam) {
		getViewport().setCamera(cam);
		this.cam = cam;
		this.mapInfo = new MapInfo(map);
		selected = new SelectorActor();
		selected.setTouchable(Touchable.disabled);
		addActor(selected);
		
		playerFocus = false;
		focusedPlayer = null;
		
		// Set up playable actors and add one called moblin
		playerActors = new ArrayList<>();
		PlayerActor moblin = new PlayerActor(new Texture(Gdx.files.internal("data/CharacterSprites/moblin.png")), "moblin", 5);
		moblin.setPosition(128, 128);
		moblin.setTouchable(Touchable.enabled);
		
		PlayerActor moblin2 = new PlayerActor(new Texture(Gdx.files.internal("data/CharacterSprites/moblin.png")), "moblin2", 5);
		moblin2.setPosition(128, 128);
		moblin2.setTouchable(Touchable.enabled);
		
		
		mapInfo.addCharacter(moblin);
		playerActors.add(moblin);
		addActor(moblin);
		
		mapInfo.addCharacter(moblin2);
		playerActors.add(moblin2);
		addActor(moblin2);
		
		// add textures for the move-able tiles image
		moveableTexture = new Texture(Gdx.files.internal("data/MiscSprites/moveable.png"));
		moveableTiles = new HashSet<>();
		//Systemom.out.println(moblin.isTouchable());
	}
	
	/**
	 * Clears all moveable tile actors currently created,
	 * removing them from the set and clearing their actor's
	 */
	private void clearMoveableTiles() {
		for (Actor tile : moveableTiles) {
			tile.remove();
			tile.clear();
		}
		moveableTiles.clear();
	}
	
	/**
	 * Given a playable character, creates moveableActor tiles on every
	 * square they can move to this turn which will effect display and
	 * facilitate moving the character.
	 * 
	 * @param target the playerActor whose is preparing to move
	 */
	private void addMoveableTiles(CharacterActor target) {
		/*
		int speed = target.getSpeed();
		for (int x = -speed ; x <= speed; x++) {
			for (int y = -speed; y <= speed; y++) {
				float drawx = target.getX() + x * TILE_SIZE;
				float drawy = target.getY() + y * TILE_SIZE;
				
			
				if (drawx < 0 || drawy < 0) {
					continue;
				}
				
				int cellx = (int) target.getX() / TILE_SIZE + x;
				int celly = (int) target.getY() / TILE_SIZE + y;
				
				int speedToCross = mapInfo.getTileSpeedToCross(cellx, celly);
				if (speedToCross > 1)
					continue;
				//int cellType = ((TiledMapTileLayer) tiledMap.getLayers().get(0)).getCell(cellx, celly).getTile().getId();
				//System.out.println(cellType);
	
				//if (cellType == 1) 
				//	continue;
			*/
		// Map of locations to speed left, note for now some may be negative (actually unreachable)
		Map<Vector2, Integer> checkedTiles = Wayfinder.getAllMoveableTiles(target, mapInfo);
		
		for (Vector2 position : checkedTiles.keySet()) {
			if (checkedTiles.get(position) >= 0) {  // loop over all positions the player can reach this turn
				//System.out.println(position);
				// create and setup a moveableActor at that point
				Actor moveableActor = new Actor() {
					private final Texture texture = moveableTexture;
					
					@Override
					public void draw(Batch batch, float alpha) {
						batch.draw(texture,  getX(),  getY());
					}
				};
				
				moveableActor.setBounds(moveableActor.getX(), moveableActor.getY(),
					moveableTexture.getWidth(), moveableTexture.getHeight());
				//moveableActor.setPosition(drawx, drawy);
				moveableActor.setPosition(position.x * TILE_SIZE, position.y * TILE_SIZE);
				moveableActor.setTouchable(Touchable.enabled);
				moveableTiles.add(moveableActor);  // add to set so we can clear when no longer needed
				addActor(moveableActor);
				//System.out.println(moveableActor.getX() + ", " + moveableActor.getY());
			}
		}
		//}
				
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
			System.out.println(((PlayerActor) selected).getCell());
			playerFocus = true;
			focusedPlayer = (PlayerActor) selected;
			clearMoveableTiles();
			addMoveableTiles(focusedPlayer);
			return true;
		} else if (moveableTiles.contains(selected)) {  // movement location selected, move to that location
			focusedPlayer.setPosition(selected.getX(), selected.getY());
		}
		
		// no longer moving, so lose focus
		
		playerFocus = false;
		clearMoveableTiles();
		return true;
	}
	 
	
	
	/**
	 *  Converts from a screen position z (i.e. from mouse) and returns the closest grid square
	 */
	 private int snapToGrid(float z) {
		return (int) z / TILE_SIZE * TILE_SIZE;
	}
}
	
	
