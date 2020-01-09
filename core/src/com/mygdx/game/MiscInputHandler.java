package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Class for handling miscellaneous input that isn't necessarily related to any
 * particular stage or input handler, such as moving the camera or handling ui
 */
public class MiscInputHandler implements InputProcessor {
	OrthographicCamera cam;
	
	/**
	 * @param cam the camera other stages are using
	 */
	public MiscInputHandler(OrthographicCamera cam) {
		this.cam = cam;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// pans the camera
		// TODO make this a smooth transition 
		if (keycode == Input.Keys.LEFT)
	        cam.translate(-32,0);
		else if(keycode == Input.Keys.RIGHT)
	        cam.translate(32,0);
		else if(keycode == Input.Keys.UP)
	        cam.translate(0,32);
		else if(keycode == Input.Keys.DOWN)
	        cam.translate(0,-32);
		else 
			return false;  // didn't get handled here
		
		return true;  // exited if/else, must have handled 
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
