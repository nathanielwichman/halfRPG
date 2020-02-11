package com.mygdx.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Class for describing and tracking unique characters to be initialized 
 * into the game world. Static methods return an instance of a pre-set
 * character if one exists which can be further modified before being
 * created in the game world;
 */
public class CharacterInfo {
	// Instance fields (describe a character)
	public Texture t;  // texture describing the character class
	public int Id;  // id for individuals in a class
	public String className;  // name of the character class i.e. orc
	public String name;  // unique name of instance: className + Id
	public int maxSpeed;  // character's max moves per turn
	public int maxHealth;   // character's starting hp
	public int vision;  // character sight, in tiles
	
	public List<AttackAction> actions;  // actions a character can make
	public AttackAction basicAttack;  // the default attack action
		
	// Static field to track characters
	private static Map<String, Integer> existingCharacters;
	
	/**
	 * Sets up static data structures if needed.
	 * Should be called internally before using said structures
	 */
	private static void initialize() {
		if (existingCharacters == null)
			existingCharacters = new HashMap<>();
	}	
	
	public CharacterInfo(String className, int maxSpeed, int maxHealth) {
		this(className, maxSpeed, maxHealth, 8);
	}
	
	/**
	 * Creates a CharacterInfo instance as described, adding its class information
	 * to static id tracking if not used before.
	 * 
	 * @param className name of class
	 * @param maxSpeed character's max moves
	 * @param maxHealth character's full hp
	 * @param vision number of tiles that can be seen
	 */
	public CharacterInfo(String className, int maxSpeed, int maxHealth, int vision) {
		initialize();
		
		// get unique id for class 
		if (existingCharacters.containsKey(className)) {
			this.Id = existingCharacters.get(className);
			assert this.Id != Integer.MAX_VALUE : "Overflow when giving character unique ids";
			existingCharacters.put(className, this.Id + 1);
		} else {
			this.Id = 0;
			existingCharacters.put(className, 1);
		}
		
		// add fields, making name = className + Id
		this.className = className;
		this.name = className + this.Id;
		this.maxSpeed = maxSpeed;
		this.maxHealth = maxHealth;
		this.vision = vision;
		
		this.actions = new ArrayList<>();
	}
	
	/**
	 * Creates a CharacterInfo instance as described with a texture, adding its class information
	 * to static id tracking if not used before.
	 * 
	 * @param t texture for this instance of class
	 * @param className name of class
	 * @param maxSpeed character's max moves
	 * @param maxHealth character's full hp
	 */
	public CharacterInfo(Texture t, String className, int maxSpeed, int maxHealth) {
		this(className, maxSpeed, maxHealth);
		this.t = t;
	}
	
	/**
	 * Removes all id tracking for a class
	 * Call when no instances of class are on screen anymore
	 * @param name name of class
	 * @return true if any instances of class existed before clear, false otherwise
	 */
	public static boolean clearCharacterClass(String name) {
		initialize();
		if (existingCharacters.containsKey(name)) {
			existingCharacters.remove(name);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Removes all id tracking for all classes
	 * Call when moving between screens/levels
	 */
	public static void clearAll() {
		if (existingCharacters != null) {
			existingCharacters.clear();
		}
	}
	
	/**
	 * Gets CharacterInfo for a predefined class
	 * @param classname name of class
	 * @return CharacterInfo describing class or null
	 */
	public static CharacterInfo getCharacterInfo(String classname) {
		switch (classname) {
			case "Moblin":
				CharacterInfo i = new CharacterInfo(new Texture(Gdx.files.internal("data/CharacterSprites/Moblin.png")),
						"Moblin", 10, 5);
				i.addActions(AttackAction.getAttack("Slash"));
				return i;
			case "SkeletonPunchingBag":
				CharacterInfo iv = new  CharacterInfo(new Texture(Gdx.files.internal("data/CharacterSprites/SkeletonPunchingBag.png")),
						"SkeletonPunchingBag", 6, 5);
				iv.addActions(AttackAction.getAttack("Slash"));
				return iv;
		}
		return null;
	}

	/**
	 * Adds an attack action this Character can perform.
	 * Sets it as the basic attack if said character has
	 * no prior actions added
	 * 
	 * @param a Descriptor of said attack
	 */
	public void addActions(AttackAction a) {
		this.actions.add(a);
		if (this.basicAttack == null) {
			this.basicAttack = a;
		}
	}
	
	/**
	 * Adds a list of attack actions this character can perform.
	 * Sets the first item in list as the basic attack if said character
	 * has no prior actions added.
	 * 
	 * @param l List of attack action descriptors
	 */
	public void addActions(List<AttackAction> l) {
		if (l != null && l.size() > 0) {
			actions.addAll(l);
			if (basicAttack == null) {
				basicAttack = actions.get(0);
			}
		}
	}
	
	/**
	 * Sets a given attack as this character's basic attack.
	 * Will add it to available actions if not already added.
	 * @param a The attack to set
	 */
	public void setBasicAttack(AttackAction a) {
		if (!actions.contains(a)) {
			actions.add(a);
		}
		basicAttack = a;
	}
}
