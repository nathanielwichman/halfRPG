package com.mygdx.game;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

public class RPGAi {
	public PlayerActor getAttackTarget(EnemyActor a, MapInfo m) {
		if (!a.alerted()) { return null; }
		List<PlayerActor> targets = a.alertedTo();
		if (targets.size() == 1) {
			return targets.get(0);
		}
		
		for (PlayerActor target : targets) {
			
		//	Wayfinder.getPathToTile(origin, target, m, actor, p);
		}
		return null;
	}
	
	private void filter(Set<Vector2> possibleTiles, MapInfo m) {
		Iterator<Vector2> it = possibleTiles.iterator();
		while (it.hasNext()) {
			//if (m.)
		}
	
	}
	
}
