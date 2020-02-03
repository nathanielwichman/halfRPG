package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;

public class EnemyOperator extends Operator {

	public EnemyOperator(RPGStage parent) {
		super(parent);
	}
	
	// adds a new enemy to this controllers knowledge
	public void addEnemy(EnemyActor a) {	}
	
	// removes an enemy to this controllers knowledge.
	// If enemy removed returns true, else (enemy not
	// in controller's knowledge) returns false
	public boolean removeEnemy(EnemyActor a) { return false; }
	
	// Plans and executes 
	public void executeStrategy() {}
	
	/*
	// Gets the plans for a specific enemies turn 
	private Strategy getEnemyPlan(EnemyActor a) { 
		if (!a.alerted()) {
			Map<Vector2, Integer> moves = Wayfinder.getAllSelectableTiles2(a, a.getCell(), Math.min(a.getSpeedRemaining(), 3), map, new ActionProperties(
					CanSelect.TILE, CanMoveThrough.ENEMY));
			
		} else {
			PlayerActor target = a.firstTargetAlertedTo();
		}
		return null;
	} 
	*/
	
	/**
	 * Idea of plan
	 * 
	 * GetStradegy
	 *  |_ goes through all enemies, gets them to generate a best plan (Strategy)
	 *  |_ assigns value to each enemy's plans
	 *  |_ execute highest value plan
	 *  |_ replan all remaining enemies (since board state may have changed)
	 *  |_ continue until done
	 *  
	 *  TODO: Get heuristic for value of action
	 */
}
