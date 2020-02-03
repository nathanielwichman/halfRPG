package com.mygdx.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The main screen for all current game play. Sets up a 
 * tiled map and a main stage, which is where most of the
 * game logic is handled.
 */
public class RPG implements Screen {
	private halfRPG parent;
	private RPGStage mainStage;
	private UiStage uiStage;
	private OrthographicCamera cam;
	
	private TiledMapRenderer mapRenderer;
	private TiledMap map;
	//private TiledMapStage mapStage;
	
	private InputMultiplexer multiplexer;
	
	private int ulockId;
	private Set<Integer> userInputLocks;
	private static MapInfo currentMap;
	private static GameState state;
	
	public static MapInfo getCurrentMapInfo() {
		return currentMap;
	}
	
	public static void setCurrentMapInfo(MapInfo m) {
		RPG.currentMap = m;
	}
	
	public RPG(halfRPG program) {
		parent = program;
		state = GameState.SETUP;
		
		// set up shared camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
				
		OrthographicCamera cam = new OrthographicCamera();
		cam.setToOrtho(false, w, h);
		cam.update();
	
		// setup Tiled map
		map = new TmxMapLoader().load("data/TiledMaps/samplemap.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map);
	
		// create mainStage for sprites, giving it the tiled map and cam for logic
		
		
		mainStage = new RPGStage(this, map, cam); //Stage(new ScreenViewport(cam));
		uiStage = new UiStage(this, cam);
		
		// set up locks for user input (i.e. while animation playing)
		ulockId = 0;
		userInputLocks = new HashSet<>();
		
		
		// setup input passing.
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(uiStage);
		multiplexer.addProcessor(mainStage);
		//multiplexer.addProcessor(mapStage);
		state = GameState.PLAYER_TURN;
		
	}
	
	public static GameState getCurrentGameState() {
		return state;
	}
	
	public static GameState setCurrentGameState(GameState newState) {
		GameState oldState = state;
		state = newState;
		return oldState;
	}
	
	public boolean unblockUserInput(int lockId) {
		if (userInputLocks.contains(lockId)) {
			userInputLocks.remove(lockId);
		}
		if (userInputLocks.size() == 0) {
			ulockId = 0;
			return true;
		}
		return false;
	}
	
	public int blockUserInput() {
		userInputLocks.add(ulockId);
		ulockId++;
		return ulockId - 1;
	}
	
	public boolean userInputAllowed() {
		return userInputLocks.size() == 0;
	}
	
	public enum UiAction {
		ADD_BUTTON, ADD_BUTTONS, REMOVE_BUTTONS, TOGGLE_VISIBILITY;
	}
	
	public boolean passToUi(UiAction action, Object o) {
		assert !action.equals(UiAction.ADD_BUTTON);
		return passToUi(action, o, null);
	}
	
	@SuppressWarnings("unchecked")
	public boolean passToUi(UiAction action, Object o, Object o2) {
		switch (action) {
			case ADD_BUTTON: 
				uiStage.addActionButton((PlayerActor) o, (AttackAction) o2);
				break;
			case ADD_BUTTONS:
				uiStage.addActionButtons((PlayerActor) o, (Collection<AttackAction>) o2);
				break;
			case REMOVE_BUTTONS:
				uiStage.removeActionButtons();
				break;
			case TOGGLE_VISIBILITY:
				uiStage.toggleVisibility((String) o);
				break;
			default:
				return false;
		}
		return true;
	}
	
	public void passToRPG(UiActionActor a) {
		mainStage.handleUiSelection(a);
	}
	
	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void render(float delta) {
		// clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
	// update stage to fit any camera changes
		mainStage.act(delta);
		//mapStage.act();
		mainStage.getCamera().update();  // share camera
		mapRenderer.setView((OrthographicCamera) mainStage.getCamera());
		mapRenderer.render();
		
		uiStage.act(Gdx.graphics.getDeltaTime());
		
		mainStage.draw();
		uiStage.draw();
		//mapStage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		mainStage.getViewport().update(width,  height, true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		mainStage.dispose();
		//mapStage.dispose();
	}
	
}
