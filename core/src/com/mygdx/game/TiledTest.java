package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RotateToAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class TiledTest extends ApplicationAdapter implements InputProcessor {
	
	//Texture img;
	TiledMap tm;
	private TiledMapTileLayer tl;
	private TiledMapTileLayer tl2;
	
	OrthographicCamera cam;
	TiledMapRenderer tiledMapRenderer;
	
	private AssetManager manager;
	
	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		manager = new AssetManager();
		manager.setLoader(TiledMap.class, new TmxMapLoader());
		manager.load("layertest.tmx", TiledMap.class);
		manager.finishLoading();
		tm = manager.get("layertest.tmx", TiledMap.class);
	
		MapLayers mapLayers = tm.getLayers();
		
		tl = (TiledMapTileLayer) mapLayers.get("Tile Layer 1");
		tl2 = (TiledMapTileLayer) mapLayers.get("Tile Layer 2");
		
		cam = new OrthographicCamera(320.f, 180.f);
		cam.position.x = 512 * .5f;
		cam.position.y = 512 * .35f;
		
		
		
		//cam = new OrthographicCamera();
		//cam.setToOrtho(false, w ,h);
		//cam.update();
		//System.out.println(Gdx.files.internal("layertest.tmx").file().getAbsolutePath());
		//tm = new TmxMapLoader().load("layertest.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tm);
		Gdx.input.setInputProcessor(this);
		//img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		//Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		cam.update();
		tiledMapRenderer.setView(cam);
		
		((OrthogonalTiledMapRenderer) tiledMapRenderer).getBatch().begin();
		tiledMapRenderer.renderTileLayer(tl);
		tiledMapRenderer.renderTileLayer(tl2);
		((OrthogonalTiledMapRenderer) tiledMapRenderer).getBatch().end();
	}
	
	@Override
	public void dispose () {
		manager.dispose();
		//batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
      if(keycode == Input.Keys.LEFT)
            cam.translate(-32,0);
        if(keycode == Input.Keys.RIGHT)
            cam.translate(32,0);
        if(keycode == Input.Keys.UP)
            cam.translate(0,-32);
        if(keycode == Input.Keys.DOWN)
            cam.translate(0,32);
        if(keycode == Input.Keys.NUM_1)
            tm.getLayers().get(0).setVisible(!tm.getLayers().get(0).isVisible());
        if(keycode == Input.Keys.NUM_2)
        	tm.getLayers().get(1).setVisible(!tm.getLayers().get(1).isVisible());
        return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
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
