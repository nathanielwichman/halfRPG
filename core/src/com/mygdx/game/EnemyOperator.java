package com.mygdx.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ActionProperties.CanMoveThrough;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.Strategy.Step;

public class EnemyOperator extends Operator {
	Queue<EnemyActor> readyToAct;
	
	public EnemyOperator(RPGStage parent) {
		super(parent);
		readyToAct = new LinkedList<>();
	}

	@Override
	public boolean executingStep(CharacterActor actor, Step s) {
		if (actor instanceof PlayerActor) {
			for (CharacterActor a : actors) {
				if (checkLineOfSight(a, actor)) {
					((EnemyActor) a).brain.seeEnemy(actor);
				}
					
			}
		}
		return false;
	}
	
	public boolean checkLineOfSight(CharacterActor a, CharacterActor target) {
		assert a instanceof EnemyActor;
		if (Wayfinder.traceLine(a.getCell(), target.getCell(), RPG.getCurrentMapInfo()) &&
				a.getCell().dst(target.getCell()) < a.getVisionDistance()) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void actorDeath(CharacterActor a) {
		if (actors.contains(a)) {
			actors.remove(a);
			for (CharacterActor ally : actors) {
				((EnemyActor) ally).brain.seeAllyDeath(a);
			}
		} else {
			for (CharacterActor enemy : actors) {
				((EnemyActor) enemy).brain.seeEnemyDeath(a);
			}
		}
	}
	
	@Override
	public void beginTurn() {
		for (CharacterActor a : actors) { 
			EnemyActor e = (EnemyActor) a;
			e.refresh();
			for (CharacterActor enemy : otherOp.actors) {
				if (checkLineOfSight(a, enemy)) {
					e.brain.seeEnemy(enemy);
				}
			}
			readyToAct.add(e);
		}
	}
	
	// Plans and executes 
	public Strategy.Origin getStrategy() {
		if (readyToAct.isEmpty()) {
			return null;
		} else {
			EnemyActor next = readyToAct.poll();
			return new Strategy.Origin(next.brain.getStrategy(), next);
		}
	}
		
	
	
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
