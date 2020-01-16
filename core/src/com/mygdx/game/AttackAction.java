package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.mygdx.game.ActionProperties.CanSelect;
import com.mygdx.game.ActionProperties.EffectedByTerrain;

/**
 * Contains information about an attack action a character can make.
 * 
 * TODO: Add some rng to hits (i.e. accuracy)
 * TODO: Make more versatile so can handle interesting effects
 */
public class AttackAction {
	
	/**
	 * The type of attack, AttackType.TARGET must be used to target a 
	 * character while AttackType.POINT can target any tile within range
	 */
	
	public final String attackName;  // name of the attack
	public ActionProperties p;
	//private final AttackType type;  // the type of attack (target or point)
	public final int damage;  // the amouont of dmg the attack does
	public final int range;  // the range the 
	public Texture uiImage;   // the ui image to select the attack (if player action)
	
	/**
	 * Initializes a new attack without an associated ui image (i.e. NPC action)
	 * @param name name of the attack, should be unique
	 * @param type  type of the attack, either AttackType.POINT or AttackType.TARGET
	 * @param damage damage done by the attack
	 * @param range range of the attack, in tiles 
	 */
	public AttackAction(String name, ActionProperties p, int damage, int range) {
		this.attackName = name;
		this.p = p;
		this.damage = damage;
		this.range = range;
		this.uiImage = null;
	}
	
	/**
	 * Initializes a new attack with an associated ui image (i.e. player selectable action) 
	 * @param name name of the attack, should be unique
	 * @param type  type of the attack, either AttackType.POINT or AttackType.TARGET
	 * @param damage damage done by the attack
	 * @param range range of the attack, in tiles
	 * @param t texture for the ui icon used to select this attack
	 */
	public AttackAction(String name, ActionProperties p, int damage, int range, Texture t) {
		this(name, p, damage, range);
		this.uiImage = t;
	}
	
	/**
	 * Sets or updates the ui texture associated with this attack
	 * @param newTexture the new ui texture
	 * @return the old texture if one was set or null otherwise
	 */
	public Texture setUiImage(Texture newTexture) {
		Texture oldTexture = uiImage;
		uiImage = newTexture;
		return oldTexture;
	}
	
	/**
	 * Gets a predefined attack action by name
	 */
	public static AttackAction getAttack(String name) {
		switch (name) {
			case "slash":
				return new AttackAction("Slash", new ActionProperties(CanSelect.ENEMY, EffectedByTerrain.IGNORE_TERRAIN), 2, 1);
			default:
				return new AttackAction("Stab", new ActionProperties(CanSelect.ENEMY, EffectedByTerrain.IGNORE_TERRAIN), 2, 1);
		}
	}
}
