package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mygdx.game.UiActionActor.SpecialAction;

/**
 * Class for handling miscellaneous input that isn't necessarily related to any
 * particular stage or input handler, such as moving the camera or handling ui
 */
public class UiStage extends Stage {
	OrthographicCamera cam;
	RPG parent;
		
	private int mapPanX, mapPanY;
	
	private Vector2 actionBoxLocation;
	private List<UiActionActor> selectables; 
	private Map<String, Actor> reusableActors;
	
	/**
	 * @param cam the camera other stages are using
	 */
	public UiStage(RPG parent, OrthographicCamera cam) {
		this.parent = parent;
		this.cam = cam;
		mapPanX = 0;
		mapPanY = 0;
		actionBoxLocation = new Vector2(50, 50);
		selectables = new ArrayList<>();
		
		reusableActors = new HashMap<>();
		
		final TextureAtlas ta = new TextureAtlas(Gdx.files.internal("data/MiscSprites/enemyTurn.atlas"));
		Actor enemyTurn = new Actor() {
			private final Animation<AtlasRegion> ani = new Animation<>(1/4f, ta.getRegions());
			private float stateTime = 0;
			
			@Override
			public void act(float delta) {
				super.act(delta);
				stateTime += delta;
			}
			
			@Override
			public void draw(Batch batch, float delta) {
				batch.draw(ani.getKeyFrame(stateTime, true), getX(), getY());
			}
			
		};
		
		reusableActors.put("enemyTurn", enemyTurn);
		
		enemyTurn.setX(getWidth() / 2);
		enemyTurn.setY(getHeight() - 30 - ta.getTextures().first().getHeight());
		enemyTurn.setVisible(false);
		addActor(enemyTurn);
	}
	
	public void toggleVisibility(String name) {
		if (reusableActors.containsKey(name)) {
			Actor a = reusableActors.get(name);
			a.setVisible(!a.isVisible());
		}
	}
	
	public void addActionButtons(PlayerActor origin, Collection<AttackAction> l) {
		for (AttackAction a : l) {
			addActionButtonInternal(new UiActionActor(
					origin, a));
		}
		reorderSelectables();
	}
	
	public void addActionButton(PlayerActor origin, AttackAction a) {
		addActionButtonInternal(new UiActionActor(
				origin, a));
		reorderSelectables();
	}
	
	private void addActionButtonInternal(UiActionActor a) {
		selectables.add(a);
		addActor(a);
		a.setTouchable(Touchable.enabled);
	}
	
	public void removeActionButtons() {
		for (UiActionActor a : selectables) {
			a.remove();
			a.clear();
		}
		selectables.clear();
	}
	
	public void reorderSelectables() {
		int offset = 64;
		int size = 96;
		int maxSize = (size + offset) * 5;
		int currentX = (int) actionBoxLocation.x;
		
		for (UiActionActor a : selectables) {
			System.out.println(currentX + ", " + actionBoxLocation.y);
			a.setPosition(currentX, actionBoxLocation.y);
			a.setBounds(a.getX(), a.getY(), size, size);
			a.setVisible(true);
			currentX += offset + size;
		}
	}

	
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// pans the camera
		// TODO make this a smooth transition 
		if (parent.userInputAllowed()) {
			if (keycode == Input.Keys.LEFT)
		        cam.translate(-32,0);
			else if(keycode == Input.Keys.RIGHT)
		        cam.translate(32,0);
			else if(keycode == Input.Keys.UP)
		        cam.translate(0,32);
			else if(keycode == Input.Keys.DOWN)
		        cam.translate(0,-32);
			else if (keycode == Input.Keys.SPACE)
				parent.passToRPG(new UiActionActor(null, null, SpecialAction.END_TURN));
			else 
				return false;  // didn't get handled here
			
			return true;  // exited if/else, must have handled 
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (parent.userInputAllowed()) {
			Vector2 stageCoords = screenToStageCoordinates(new Vector2(screenX, screenY));
			Actor selected = super.hit(stageCoords.x, stageCoords.y, true);
			
			if (selected instanceof UiActionActor) {
				parent.passToRPG((UiActionActor) selected);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
