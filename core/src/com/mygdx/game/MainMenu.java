package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Stub main menu for playing game
 */
public class MainMenu implements Screen {
	private halfRPG parent;
	private Stage stage;
	
	/**
	 * @param program the parent program
	 */
	public MainMenu (halfRPG program) {
		parent = program;
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void show() {
		// creat table to hold ui elements in stage
		Table table = new Table();
		table.setFillParent(true);
		// table.setDebug(true);  // shows bounding boxes, etc
		stage.addActor(table);
		
		// create ui elements
		Skin skin = new Skin(Gdx.files.internal("data/UiData/uiskin.json"));
		TextButton startGame = new TextButton("Start Game", skin);
		TextButton exit = new TextButton("Exit", skin);
		
		// add handlers to buttons
		exit.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Gdx.app.exit();
			}
		});
		
		startGame.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				parent.changeScreen(halfRPG.RPG);;
				
			}
		});
		
		// add ui elements to table
		table.add(startGame).fillX().uniformX();
		table.row().pad(10, 0, 0, 0);
		table.add(exit).fillX().uniformX();
	}

	@Override
	public void render(float delta) {
		// clear screen
		Gdx.gl.glClearColor(0,  0,  0,  1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// draw stage
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
		stage.draw();
		
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true); 
		
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
		// TODO Auto-generated method stub
		
	}

}
