package com.mygdx.game;

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
	private Stage mainStage;
	private OrthographicCamera cam;
	
	private TiledMapRenderer mapRenderer;
	private TiledMap map;
	//private TiledMapStage mapStage;
	
	private InputMultiplexer multiplexer;
	
	public RPG(halfRPG program) {
		parent = program;
		
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
		mainStage = new RPGStage(map, cam); //Stage(new ScreenViewport(cam));
		//mapStage = new TiledMapStage(map);
		//mainStage.getViewport().setCamera(cam);

		// create a misc handler to deal with panning and other general non-game inputs
		MiscInputHandler camControl = new MiscInputHandler(cam);
		
		// setup input passing.
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(camControl);
		multiplexer.addProcessor(mainStage);
		//multiplexer.addProcessor(mapStage);
	
		
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
		mainStage.act();
		//mapStage.act();
		mainStage.getCamera().update();  // share camera
		mapRenderer.setView((OrthographicCamera) mainStage.getCamera());
		mapRenderer.render();
	
		mainStage.draw();
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
