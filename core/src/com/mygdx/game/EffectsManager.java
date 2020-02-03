package com.mygdx.game;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class EffectsManager {
	public enum EffectType {
		MOVE_CHOICE, ATTACK_CHOICE, CURSOR, PLAN_STEP;
	}
	
	RPGStage parentStage;
	SelectorActor cursor;  // cursor image
	Vector2 lastCellMousedOver;  // last tile mouse was over
	Strategy displayStrategy;  // strategy to display
	
	Set<SelectableActionActor> selectableTiles;  // overlay tiles for player selection
	Set<Actor> stepTiles;  // overlay tiles for showing strategy
	Map<EffectType, Texture> textureMap;  // save texture for each effect
	
	public EffectsManager(RPGStage parent) {
		this.cursor = new SelectorActor();
		this.cursor.setTouchable(Touchable.disabled);
		parent.addActor(this.cursor);
		
		selectableTiles = new HashSet<>();
		stepTiles = new HashSet<>();
	}
	
	private void loadTextures() {
		// Step texture
		Pixmap pm = new Pixmap(26, 26, Format.RGBA8888);
		pm.setBlending(Blending.None);
		pm.setColor(Color.WHITE);
		pm.fillCircle(13, 13, 10);
		textureMap.put(EffectType.PLAN_STEP, new Texture(pm));
		
		// decouple selector actor
		
		// Plan move texture
		textureMap.put(EffectType.MOVE_CHOICE,
				new Texture(Gdx.files.internal("data/MiscSprites/moveable.png")));
		
		// Plan attack texture
		textureMap.put(EffectType.ATTACK_CHOICE,
				new Texture(Gdx.files.internal("data/MiscSprites/attackable.png")));
	}
	
	
	
}
