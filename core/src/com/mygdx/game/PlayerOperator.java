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
	
	@Override
	public void actorDeath(CharacterActor a) {
		if (actors.contains(a)) {
			actors.remove(a);
		}
	}
	
	@Override
	public void beginTurn() {
		for (CharacterActor a : actors) {
			a.refresh();
		}
	}
}
