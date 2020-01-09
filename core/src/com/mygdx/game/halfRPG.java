package com.mygdx.game;

import com.badlogic.gdx.Game;
/**
 * Top level game class that handles initializing and switching
 * screens
 * @author Nathaniel
 *
 */

public class halfRPG extends Game {
	private MainMenu mainMenu;
	private RPG rpg;
	
	public final static int MENU = 0;  // main menu
	public final static int RPG = 1;  // rpg game
	
	@Override
	public void create() {
		mainMenu = new MainMenu(this);
		setScreen(mainMenu);
		
	}
	
	// Given a defined screen enum switches to that screen, creating
	// it if necessary 
	public void changeScreen(int screen) {
		switch(screen) {
			case RPG:
				if (rpg == null) {
					rpg = new RPG(this);
					this.setScreen(rpg);
				}
				break;
			case MENU:
				if (mainMenu == null) {
					mainMenu = new MainMenu(this);
					this.setScreen(mainMenu);
				}
				break;
		}
	}
	
}