package com.mygdx.game;

import java.util.HashSet;
import java.util.Set;

public class PlayerOperator extends Operator {
	private boolean playerTurn;
	private PlayerActor focusedPlayer;

	public PlayerOperator(RPGStage parent) {
		super(parent);
		playerTurn = false;
		focusedPlayer = null;
		
	}
}
