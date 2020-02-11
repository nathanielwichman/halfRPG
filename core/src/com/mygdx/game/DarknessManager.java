package com.mygdx.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByDarkness;
import com.mygdx.game.ActionProperties.EffectedByTerrain;

/**
 * Manages the mechanics of darkness / fog of war, providing an API
 * to add/remove tiles of darkness. Tiles are added both to display and 
 * MapInfo
 */
public class DarknessManager {
	private final Texture darknessTexture;
	Map<Vector2, Actor> darknessTiles;
	RPGStage parentStage;
	
	/**
	 * Creates a new DarknessManager
	 * @param parent Link to the stage this is managing darkness for
	 */
	public DarknessManager(RPGStage parent) {
		this.parentStage = parent;
		darknessTiles = new HashMap<>();
		if (RPG.DEBUG) {
			darknessTexture = new Texture(Gdx.files.internal("data/MiscSprites/darknessDebug.png"));
		} else {
			darknessTexture = new Texture(Gdx.files.internal("data/MiscSprites/darkness.png"));
		}
	}
	
	/**
	 * Given a collection of characters clears darkness over there positions,
	 * as well as on all tiles within any characters line of sight
	 * @param chars Characters
	 */
	public void clearDarkness(Collection<CharacterActor> chars) {
		for (CharacterActor p : chars)
			clearDarkness(p);
	}
	
	/**
	 * Given a character, clears darkness over its position as well as all
	 * tiles within its line of sight
	 * @param p the Character
	 */
	public void clearDarkness(CharacterActor p) {
		int range = p.getVisionDistance();
		removeDarkness(p.getCell());
		Set<Vector2> toClear = Wayfinder.getAllSelectableTiles2(
				 p, p.getCell(), range, RPG.getCurrentMapInfo(), new ActionProperties(
						 EffectedByDarkness.IGNORE, EffectedByTerrain.IGNORE_TERRAIN,
						 CanSelect.WALLS, CanSelect.ENEMY, CanSelect.TILE,
						 CanMoveThrough.CHARACTER)).keySet();
		 toClear.removeAll(Wayfinder.getAllOutOfSight(p.getCell(), toClear, RPG.getCurrentMapInfo()));
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
			 RPG.getCurrentMapInfo().removeDarkness(position);
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
	 public void addDarknessToMap() {
		 Vector2 bounds = RPG.getCurrentMapInfo().getMapSize();
		 
		 for (int x = 0; x < bounds.x; x++) {
			 for (int y = 0; y < bounds.y; y++) {
				 Actor darkness = new Actor() {
					 Texture texture = darknessTexture;
					
					 @Override
					 public void draw(Batch batch, float alpha) {
						 batch.draw(texture,  getX(),  getY());
					 }
					
				 };
				 darkness.setBounds(darkness.getX(), darkness.getY(),
						 darknessTexture.getWidth(), darknessTexture.getHeight());
				 darkness.setPosition(x * RPGStage.TILE_SIZE, y * RPGStage.TILE_SIZE);
				 darkness.setTouchable(Touchable.disabled);
				 darknessTiles.put(new Vector2(x,y), darkness);
				 parentStage.addActor(darkness);
				 RPG.getCurrentMapInfo().addDarkness(); 
			}
		}
	}
}
