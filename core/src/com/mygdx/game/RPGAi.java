package com.mygdx.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import com.badlogic.gdx.math.Vector2;

public abstract class RPGAi {
	public enum State {
		UNALERTED, PATROLLING, ALERTED;
	}
	
	
	public abstract boolean isAlerted();
	
	public abstract Strategy getStrategy();
	
	public Strategy getUnalertedStrategy() { return null; }
	
	public Strategy getPatrolStrategy() { return null; }
	
	public abstract Strategy chooseAttackStrategy(SortedSet<Strategy> strategies);
	
	public abstract SortedSet<Strategy> getAttackStrategies();
	
	public abstract void updateState();
	
	public abstract State getState();
	
	public abstract State setState(State s);
	
	// update methods (on turn)
	// return if changed behavior after hit
	public boolean seeEnemy(CharacterActor a) { return false; } 
	
	public boolean seeEnemyDeath(CharacterActor a) { return false; }
	
	// update methods (not on turn)
	public boolean seeAllyHit(CharacterActor ally, AttackAction a) { return false; }
	
	public boolean seeAlly(CharacterActor ally) { return false; }
	
	public abstract boolean enemyDeath(CharacterActor enemy);
	
	public boolean seeAllyDeath(CharacterActor ally) { return false; }
	
	public boolean attackedBy(CharacterActor origin, AttackAction a, boolean hit) { return false; }	
	
	
}
