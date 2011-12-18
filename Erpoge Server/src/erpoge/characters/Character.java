package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.charactereffects.CharacterEffect;
import erpoge.inventory.AmmunitionMap;
import erpoge.inventory.ItemMap;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.ItemType;
import erpoge.magic.Spells;
import erpoge.serverevents.*;
import erpoge.terrain.Container;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.World;

public abstract class Character extends Seer {
	public final static int DAMAGE_PLAIN = 1,
		DAMAGE_FIRE = 2,
		DAMAGE_COLD = 3,
		DAMAGE_POISON = 4,
		DAMAGE_MENTAL = 5,
		DAMAGE_ELECTRICITY = 6,
		DAMAGE_ACID = 7,
		
		RACE_HUMAN = 0,
		RACE_ELF = 1,
		RACE_DWARF = 2,
		RACE_ORC = 3;
	
	public final static int
		FRACTION_NEUTRAL = -1,
		FRACTION_PLAYER = 1,
		FRACTION_AGRESSIVE = 0;
	
	public static final Character DUMMY = new NonPlayerCharacter(false);
	public static final String DEFAULT_NAME = "Default Name";
	public static final double VISION_RANGE = 8;
	
	public int hp;
	public int mp;
	public int maxHp;
	public int maxMp;
	public int energy = 0;
	protected int fraction;
	public final String name;
	public final String type;
	public final HashMap<Integer, Character.Effect> effects = new HashMap<Integer, Character.Effect>();
	
	protected ArrayList<Integer> spells = new ArrayList<Integer>();
	
	/**
	 * characterId generates randomly and works as a hash and the only way to
	 * identify the unique character
	 */
	public final int characterId = Chance.rand(0, Integer.MAX_VALUE);
	public final ItemMap inventory = new ItemMap();
	public final AmmunitionMap ammunition = new AmmunitionMap();

	public Character(String t, String n, Location l, int x, int y) {
	// Common character creation: with all attributes, in location
		super(x, y, l);
		name = n;
		type = t;
		location = l;
		hp = 10;
		mp = 100;
		maxHp = 10;
		maxMp = 100;
		fraction = 0;
		location.passability[x][y] = TerrainBasics.PASSABILITY_SEE;
	}
	public Character(String t, String n, int x, int y) {
		// Common character creation: with all attributes, in location
			super(x, y, Location.ABSTRACT_LOCATION);
			name = n;
			type = t;
			hp = 10;
			mp = 100;
			maxHp = 10;
			maxMp = 100;
			fraction = 0;
		}
	public Character(int x, int y, String t, int f) {
		super(x, y, World.ABSTRACT_LOCATION);
		type = t;
		fraction = f;
		name = "Generator character "+type;
		location.passability[x][y] = 3;
	}

	public Character() {
		super(0,0, World.ABSTRACT_LOCATION);
		type = "Dummy";
		name = DEFAULT_NAME;
	}

	public Character(boolean b) {
		/**
		 * Create an empty character, used for constants
		 */
		super(0, 0, World.ABSTRACT_LOCATION);
		type = "Abstract";
		name = "Abstract Character";

	}

	public int mp() {
		return mp;
	}

	public int hp() {
		return hp;
	}

	public Location location() {
		return location;
	}
		
	public void attack(Character aim) {
		location.addEvent(new EventMeleeAttack(characterId, aim.characterId));
		aim.getDamage(10, DAMAGE_PLAIN);
		moveTime(500);
	}

	public void shootMissile(int toX, int toY, ItemPile missile) {
		loseItem(missile);
		Coordinate end = getRayEnd(toX, toY);
		location.addEvent(new EventMissileFlight(x, y, end.x, end.y, 1));
		location.addItem(missile, end.x, end.y);
		if (location.cells[end.x][end.y].character() != null) {
			location.cells[end.x][end.y].character().getDamage(10, DAMAGE_PLAIN);
		}
	}
	
	public void getDamage(int amount, int type) {
		hp -= amount;
		location.addEvent(new EventDamage(characterId, amount, type));
		if (hp <= 0) {
			die();
		}
	}

	public void die() {
		excludeFromSeers();
		location.removeCharacter(this);
		location.addEvent(new EventDeath(characterId));
	}
	
	public void castSpell(int spellId, int x, int y) {
		location.addEvent(new EventCastSpell(characterId, spellId, x, y));
		Spells.cast(this, spellId, x, y);
		moveTime(500);
	}
	
	public void learnSpell(int spellId) {
		spells.add(spellId);
	}

	protected void increaseHp(int value) {
		hp = (hp + value > maxHp) ? maxHp : hp + value;
	}

	protected void removeEffect(CharacterEffect effect) {
		effects.remove(effect);
	}
	
	public boolean isOnGlobalMap() {
		return location == Location.ABSTRACT_LOCATION;
	}
	
	public void putOn(UniqueItem item, boolean omitEvent) {
	// Main put on function
		int cls = item.getType().getCls();
		int slot = item.getType().getSlot();
		if (cls == ItemType.CLASS_RING) {
			// ����������� ������, ����� �������� ������ (�� ����� ����
			// ������������ ���)
			int numOfRings = 0;
			if (numOfRings == 2) {
				throw new Error("Character " + name
						+ " is trying to put on more than 2 rings");
			}
		} else if (ammunition.hasPiece(slot)) {
			// ���� ����� ������� ���� �� ����
			throw new Error("Character " + name
					+ " is trying to put on a piece he is already wearing");
		}
		ammunition.add(item);
		inventory.removeUnique(item);
		if (!this.isOnGlobalMap() && !omitEvent) {
		// Sending for mobs. Sending for players is in PlayerCharacter.putOn()
			location.addEvent(new EventPutOn(characterId, item.getItemId()));
		}
		moveTime(500);
	}
	
	public void takeOff(UniqueItem item) {
	// Main take off function
		ammunition.removeSlot(item.getType().getSlot());
		inventory.add(item);
		if (!this.isOnGlobalMap()) {
		// Sending for mobs. Sending for players is in PlayerCharacter.putOn()
			location.addEvent(new EventTakeOff(characterId, item.getItemId()));
		}
		moveTime(500);
	}

	public void pickUp(ItemPile pile) {
		/*
		 * Pick up an item lying on the same cell where the character stands.
		 */
		location.addEvent(new EventPickUp(characterId, pile.getType().getTypeId(), pile.getAmount()));
		getItem(pile);
		location.removeItem(pile, x, y);
		moveTime(500);
	}
	public void pickUp(UniqueItem item) {
		/*
		 * Pick up an item lying on the same cell where the character stands.
		 */
		location.addEvent(new EventPickUp(characterId, item.getTypeId(), item.getItemId()));
		getItem(item);
		location.removeItem(item, x, y);
		moveTime(500);
	}

	public void drop(UniqueItem item) {
		loseItem(item);
		location.addItem(item, x, y);
		location.addEvent(new EventDropItem(characterId, item.getTypeId(), item.getItemId()));
		moveTime(500);
	}
	
	public void drop(ItemPile pile) {
		loseItem(pile);
		location.addItem(pile, x, y);
		location.addEvent(new EventDropItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
		moveTime(500);
	}
	
	public void takeFromContainer(ItemPile pile, Container container) {
		getItem(pile);
		container.removePile(pile);
		location.addEvent(new EventTakeFromContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	
	public void takeFromContainer(UniqueItem item, Container container) {
		getItem(item);
		container.removeUnique(item);
		location.addEvent(new EventTakeFromContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}

	public void putToContainer(ItemPile pile, Container container) {
		loseItem(pile);
		container.add(pile);
		location.addEvent(new EventPutToContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	
	public void putToContainer(UniqueItem item, Container container) {
		loseItem(item);
		container.add(item);
		location.addEvent(new EventPutToContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}
	
	public void useObject(int x, int y) {
		if (location.isDoor(x, y)) {
			location.openDoor(x,y);
		}
		location.addEvent(new EventUseObject(characterId, x, y));
		moveTime(500);
	}

	public boolean hasItem(int typeId, int amount) {
		return inventory.hasPile(typeId, amount);
	}

	public void getItem(UniqueItem item) {
		inventory.add(item);
		location.addEvent(new EventGetUniqueItem(characterId, item.getTypeId(), item.getItemId()));
	}

	public void getItem(ItemPile pile) {
		inventory.add(pile);
		location.addEvent(new EventGetItemPile(characterId, pile.getTypeId(), pile.getAmount()));
	}

	public void loseItem(UniqueItem item) {
		if (inventory.hasUnique(item.getItemId())) {
			inventory.removeUnique(item);
			location.addEvent(new EventLoseItem(characterId, item.getType().getTypeId(), item.getItemId()));
		} else {
			throw new Error("An attempt to lose an item width id " + item.getItemId()
					+ " that is neither in inventory nor in ammunition");
		}
	}
	
	public void loseItem(ItemPile pile) {
		inventory.removePile(pile);
		location.addEvent(new EventLoseItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
	}
	
	public void idle() {
		moveTime(500);
	}
	public void move(Integer dir) {
		int dx, dy;
		switch (dir) {
		case 0:
			dx = 0;
			dy = -1;
			break;
		case 1:
			dx = 1;
			dy = -1;
			break;
		case 2:
			dx = 1;
			dy = 0;
			break;
		case 3:
			dx = 1;
			dy = 1;
			break;
		case 4:
			dx = 0;
			dy = 1;
			break;
		case 5:
			dx = -1;
			dy = 1;
			break;
		case 6:
			dx = -1;
			dy = 0;
			break;
		default:
			dx = -1;
			dy = -1;
		}
		move(x + dx, y + dy);
	}
	
	public boolean at(int atX, int atY) {
		return x==atX && y==atY;
	}
	
	public void move(int nx, int ny) {
		location.passability[x][y] = 0;
		location.cells[x][y].character(false);
		x = nx;
		y = ny;
		location.cells[nx][ny].character(this);
		location.passability[nx][ny] = 3;
		location.addEvent(new EventMove(characterId, x, y));
		location.flushEvents(Location.TO_LOCATION, this);
		moveTime(500);
		getVisibleEntities();
	}
		
	public String jsonGetEffects() {
		return "[]";
	}

	public String jsonGetAmmunition() {
		return ammunition.jsonGetAmmunition();
	}

	public int[] getEffects() {
		return new int[0];
	}
	
	public int[][] getAmmunition() {
		return new int[0][2];
	}
	public void setFraction(int f) {
		fraction = f;
	}
	public boolean isEnemy(Character ch) {
		if (fraction == FRACTION_NEUTRAL) {
			return false;
		}
		return ch.fraction != fraction;
	}
	
	public abstract int getArmor();
	public void addEffect(int effectId, int duration, int modifier) {
		if (effects.containsKey(effectId)) {
			removeEffect(effectId);
		}
		effects.put(effectId, new Character.Effect(effectId, duration, modifier));
		location.addEvent(new EventEffectStart(characterId, effectId));
	}
	public void removeEffect(int effectId) {
		effects.remove(effectId);
		location.addEvent(new EventEffectEnd(characterId, effectId));
	}
	
	public void moveTime(int amount) {
		energy -= amount;
		for (Character.Effect e : effects.values()) {
			e.duration -= amount;
			if (e.duration < 0) {
				removeEffect(e.effectId);
			}
		}
	}
	public class Effect {
	// Class that holds description of one current character's effect
		public int duration, modifier, effectId;
		public Effect(int effectId, int duration, int modifier) {
			this.effectId = effectId;
			this.duration = duration;
			this.modifier = modifier;
		}
	}
}
