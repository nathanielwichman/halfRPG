package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.halfRPG;

/**
 *  The desktop (and only) launcher for halfRPG. Doesn't do much right now.
 *	TODO Make aspect ration more customizable
 * @author Nathaniel
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "test";
		config.width = 720;
		config.height = 480;
		
		new LwjglApplication(new halfRPG(), config);
	}
}
