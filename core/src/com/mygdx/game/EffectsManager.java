package com.mygdx.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.Strategy.MoveStep;
import com.mygdx.game.Strategy.Step;

/**
 * Manages overlay effects used to communicate information to players,
 * such as which tiles can be attacked by a move. Provides an API for
 * adding these tiles both logically and to display.
 */
public class EffectsManager {
	public enum EffectType {
		MOVE_CHOICE, ATTACK_CHOICE, CURSOR, PLAN_STEP;
	}
	
	RPGStage parentStage;
	Actor cursor;  // cursor image
	Vector2 lastCellMousedOver;  // last tile mouse was over
	Strategy displayStrategy;  // strategy to display
	
	Set<SelectableActionActor> selectableTiles;  // overlay tiles for player selection
	Set<Actor> stepTiles;  // overlay tiles for showing strategy
	Map<EffectType, Texture> textureMap;  // save texture for each effect
	
	
	public EffectsManager(RPGStage parent) {
		this.parentStage = parent;
		
		selectableTiles = new HashSet<>();
		stepTiles = new HashSet<>();
		textureMap = new HashMap<>();
		lastCellMousedOver = null;
		
		loadTextures();
		setupCursor();
	}
	
	// visibility
	public void setCursorVisibility(boolean visable) {
		cursor.setVisible(visable);
	}
	
	public void addMoveTiles(CharacterActor origin, Map<Vector2, Integer> tilesWithCost) {
		Texture moveableTexture = textureMap.get(EffectType.MOVE_CHOICE);
		
		for (Vector2 position : tilesWithCost.keySet()) {
			SelectableActionActor a = new SelectableActionActor(
					origin, moveableTexture, tilesWithCost.get(position));
			a.setBounds(a.getX(), a.getY(), moveableTexture.getWidth(), moveableTexture.getHeight());
			a.setPosition(position.x * RPGStage.TILE_SIZE, position.y * RPGStage.TILE_SIZE);
			a.setTouchable(Touchable.enabled);
			selectableTiles.add(a);  // add to set so we can clear when no longer needed
			parentStage.addActor(a);
		}
	}
	
	public void addAttackTiles(CharacterActor origin, Map<Vector2, Integer> tiles, AttackAction a) {
		Texture attackableTexture = textureMap.get(EffectType.ATTACK_CHOICE);
		
		for (Vector2 position : tiles.keySet()) {
			SelectableActionActor newTile = new SelectableActionActor(
					origin, attackableTexture, a);
			newTile.setBounds(newTile.getX(), newTile.getY(),
					attackableTexture.getWidth(), attackableTexture.getHeight());
			newTile.setPosition(position.x * RPGStage.TILE_SIZE, position.y * RPGStage.TILE_SIZE);
			newTile.setTouchable(Touchable.enabled);
			selectableTiles.add(newTile);
			parentStage.addActor(newTile);
		}
	}
	
	public void mouseOver(Vector2 mousePosition) {
		// update cursor
		Vector2 mapPosition = parentStage.screenToStageCoordinates(mousePosition);
		cursor.setPosition(RPGStage.snapToGrid(mapPosition.x), RPGStage.snapToGrid(mapPosition.y));
		
		// add strategy if moving 
		Vector2 gridPosition = new Vector2((int) mapPosition.x/64, (int) mapPosition.y/64);
		if (!gridPosition.epsilonEquals(lastCellMousedOver)) {
			mouseOverTile(gridPosition, parentStage.hit(mapPosition.x, mapPosition.y, true)); 
			lastCellMousedOver = gridPosition;
		}
	}
	
	public void mouseOverTile(Vector2 gridPosition, Actor mousedOver) {
		
		if (RPG.userInputAllowed()) {
			clearDisplayedStrategy();
			clearDisplayedStrategy();
			if (mousedOver instanceof SelectableActionActor) {
				SelectableActionActor sa = (SelectableActionActor) mousedOver;
				if (sa.isMove()) {
					Strategy pathToPosition = Wayfinder.getStrategyToTile(sa.getOrigin().getCell(),
													gridPosition,  sa.getOrigin(),
													ActionProperties.getDefaultMoveProperty(true));
					displayStrategy(pathToPosition);
				}
			}
		}
	}
	
	public void clearSelectableTiles() {
		for (SelectableActionActor a : selectableTiles) {
			a.clear();
			a.remove();
		}
		selectableTiles.clear();
	}
	
	public void displayNewStrategy(Strategy s) {
		clearDisplayedStrategy();
		displayStrategy(s);
	}
	
	public void clearEffects() {
		clearDisplayedStrategy();
		clearSelectableTiles();
	}
	
	public void clearDisplayedStrategy() {
		for (Actor a : stepTiles) {
			a.remove();
			a.clear();
		}
		stepTiles.clear();
	}
	
	public void displayStrategy(Strategy s) {
		s.setup();
		Texture stepTexture = textureMap.get(EffectType.PLAN_STEP);
		
		while (s.hasNextStep()) {
			Step nextStep = s.getNextStep();
			assert nextStep instanceof MoveStep;  // for now
			Vector2 nextMove = ((MoveStep) nextStep).stepLocation;
			
			Actor stepActor = setupActor(stepTexture);
			stepActor.setBounds(stepActor.getX(), stepActor.getY(),
					stepTexture.getWidth(), stepTexture.getHeight());
			stepActor.setPosition(nextMove.x * RPGStage.TILE_SIZE + RPGStage.TILE_SIZE/2-stepTexture.getWidth()/2,
					nextMove.y * RPGStage.TILE_SIZE + RPGStage.TILE_SIZE/2-stepTexture.getHeight()/2);
			stepActor.setTouchable(Touchable.disabled);
			stepTiles.add(stepActor);
			parentStage.addActor(stepActor);
				
		}
	}
	
	public void displayDamage(CharacterActor target, String damage) {
		Skin skin = new Skin(Gdx.files.internal("data/UiData/uiskin.json"));
		
		int textOffset = damage.length() * 5;
		
		int xOffset = RPGStage.TILE_SIZE / 2 - textOffset;
		int yOffset = RPGStage.TILE_SIZE / 4 * 3;
		
		Label dmgNumber = new Label(damage, skin);
		dmgNumber.setPosition(target.getX()+xOffset, target.getY() + yOffset);
		
		dmgNumber.addAction(Actions.moveBy(0f, yOffset/2f, 1.2f));
		dmgNumber.addAction(Actions.delay(.6f, Actions.fadeOut(.55f)));
		dmgNumber.addAction(Actions.delay(1.2f, Actions.removeActor()));
		
		parentStage.addActor(dmgNumber);
	}
	
	
	private void setupCursor() {
		cursor = setupActor(textureMap.get(EffectType.CURSOR));
		cursor.setTouchable(Touchable.disabled);
		parentStage.addActor(cursor);
	}
	
	private Actor setupActor(final Texture texture) {
		Actor genericActor = new Actor() {
			Texture t = texture;
			
			 @Override
			 public void draw(Batch batch, float alpha) {
				 batch.draw(t,  getX(),  getY());
			 }
			
		};
		genericActor.setBounds(genericActor.getX(), genericActor.getY(),
				texture.getWidth(), texture.getHeight());
		return genericActor;
	}
	
	private void loadTextures() {
		// Step texture
		Pixmap pm = new Pixmap(26, 26, Format.RGBA8888);
		pm.setBlending(Blending.None);
		pm.setColor(Color.WHITE);
		pm.fillCircle(13, 13, 10);
		textureMap.put(EffectType.PLAN_STEP, new Texture(pm));
		
		// Cursor texture
		textureMap.put(EffectType.CURSOR,
				new Texture(Gdx.files.internal("data/MiscSprites/selector.png")));
		
		// Plan move texture
		textureMap.put(EffectType.MOVE_CHOICE,
				new Texture(Gdx.files.internal("data/MiscSprites/moveable.png")));
		
		// Plan attack texture
		textureMap.put(EffectType.ATTACK_CHOICE,
				new Texture(Gdx.files.internal("data/MiscSprites/attackable.png")));
	}
	
	
	
}
