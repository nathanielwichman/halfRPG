package com.mygdx.game;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.SelectableActionActor.ActionType;
import com.mygdx.game.Strategy.ActionStep;
import com.mygdx.game.Strategy.Step;
import com.mygdx.game.Strategy.StepType;

public class RPGAiBasic extends RPGAi {
	State currentState;
	EnemyActor body;
	Deque<CharacterActor> alertedTo;
	
	
	public RPGAiBasic(EnemyActor body) {
		this.body = body;
		alertedTo = new LinkedList<>();
		currentState = State.UNALERTED;
	}
	
	// return if should interupt
	@Override
	public boolean seeEnemy(CharacterActor a) {
		if (!alertedTo.contains(a)) {
			alertedTo.add(a);
			if (currentState != State.ALERTED) {
				System.out.println(body+ " spotted " + a);
				currentState = State.ALERTED;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean seeEnemyDeath(CharacterActor a) {
		if (alertedTo.contains(a)) {
			alertedTo.remove(a);
			updateState();
			return true;
		}
		return false;
	}
	
	public boolean isAlerted() {
		return !alertedTo.isEmpty();
	}
	
	public void updateState() {
		if (alertedTo.isEmpty()) {
			currentState = State.UNALERTED;
		}
	}
	
	@Override
	public boolean enemyDeath(CharacterActor enemy) {
		System.out.println("enemy killed");
		if (alertedTo.contains(enemy)) {
			alertedTo.remove(enemy);
			return true;
		}
		return false;
	}
	
	
	public State getState() {
		return currentState;
	}
	
	public State setState(State s) {
		State lastState = currentState;
		currentState = s;
		return lastState;
	}
	
	@Override
	public Strategy getStrategy() {
		if (currentState == State.UNALERTED) {
			return getUnalertedStrategy();
		} else {
			return chooseAttackStrategy(getAttackStrategies());
		}
	}
	
	@Override
	public Strategy getUnalertedStrategy() {
		
		Random r = new Random();
		int moveSpaces = r.nextInt(body.getSpeedRemaining()+1) / 2;
		
		
		if (moveSpaces == 0) {
			return new Strategy();
		}
		
		List<Vector2> tiles = new ArrayList<>(Wayfinder.getAdjacentTiles(body.getCell(), moveSpaces));
		
		while (tiles.size() > 0) {
			Vector2 v = tiles.remove(r.nextInt(tiles.size()));
			if (Wayfinder.canMoveTo(v, ActionProperties.getDefaultMoveProperty(false))) {
				Strategy s = Wayfinder.getStrategyToTile(body.getCell(), v, body, ActionProperties.getDefaultMoveProperty(false));
				if (s != null && s.getTotalMoveCost() <= body.getSpeedRemaining()) {
					return s;
				}
			}
		}
		return new Strategy();
	}
	
	public Strategy chooseAttackStrategy(SortedSet<Strategy> strategies) {
		if (strategies.isEmpty()) {
			System.out.println("cant find attack");
			return getUnalertedStrategy();
		} else {
			for (Strategy s : strategies) {
				System.out.println(s) ;
			}
			Strategy choice = strategies.first();
			
			choice.cullToSpeed(body.getSpeedRemaining());
			Step finalStep = choice.getLastStep();
			
			if (finalStep.getType() == StepType.ATTACK) {
				CharacterActor target = ((ActionStep) finalStep).target;				
				if (target != null && !target.equals(alertedTo.peek())) {
					alertedTo.remove(target);
					alertedTo.addFirst(target);
				}
			}
			return choice;
		}
	}
	
	public SortedSet<Strategy> getAttackStrategies() {
		if (!this.isAlerted()) {
			return null;
		}
		
		SortedSet<Strategy> targets = new TreeSet<>();
		int index = 0;
		
		for (CharacterActor target : alertedTo) {
			Strategy possibleStrategy;
			if (Wayfinder.canReach(body.getCell(), target.getCell(), body.getBasicAttack().range,
					ActionProperties.getDefaultAttackProperties(false))) {
				possibleStrategy = new Strategy();
			} else {
				//System.out.println("\n\n");
				//System.out.println(Wayfinder.getAdjacentTiles(target.getCell(), body.getBasicAttack().range, ActionProperties.getDefaultAttackProperties(false)));
				possibleStrategy = Wayfinder.getStrategyToTiles(
						body.getCell(), Wayfinder.getAdjacentTiles(target.getCell(), body.getBasicAttack().range, ActionProperties.getDefaultMoveProperty(false)),
						body, ActionProperties.getDefaultMoveProperty(false));
			}
			
			
			if (possibleStrategy == null) {
				continue;
			}
			
			possibleStrategy.addStep(new Strategy.ActionStep(target, body.getBasicAttack()));
			
			possibleStrategy.addCost(index);
			
			if (possibleStrategy.getTotalMoveCost() > body.getSpeedRemaining()) {
				possibleStrategy.addCost(possibleStrategy.getTotalMoveCost() * 100);
			}
			targets.add(possibleStrategy);
			index++;
			
		}
		
		return targets;
		
	}
}


