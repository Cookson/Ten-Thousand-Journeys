package erpoge.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import erpoge.core.characters.CharacterEffect;
import erpoge.core.characters.CharacterState;
import erpoge.core.characters.DamageType;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.meta.Utils;
import erpoge.core.terrain.Container;

public abstract class Character extends UniqueObject implements GsonForStaticDataSerializable {
	public static final long serialVersionUID = 1832389411;
	public final static int FRACTION_NEUTRAL = -1, FRACTION_PLAYER = 1,
			FRACTION_AGRESSIVE = 0;
	protected final CharacterType characterType;
	protected Body body;
	protected static final double VISION_RANGE = 8;
	protected int actionPoints;
	protected int ep;
	protected int energy;
	protected int maxEp;
	protected int fraction;
	protected HorizontalPlane plane;
	protected Chunk chunk;
	protected final String name;
	protected final HashMap<Integer, Character.Effect> effects = new HashMap<Integer, Character.Effect>();
	protected int x;
	protected int y;
	protected ArrayList<Integer> spells = new ArrayList<Integer>();
	protected boolean isAlive;
	public final ItemCollection inventory = new ItemCollection();

	protected CharacterState state = CharacterState.DEFAULT;

	protected TimeStream timeStream;

	public Character(HorizontalPlane plane, CharacterType characterType, int x, int y, String name) {
		// Common character creation: with all attributes, in location.
		super();
		this.name = name;
		this.plane = plane;
		this.chunk = plane.getChunkWithCell(x, y);
		fraction = 0;
		isAlive = true;
		this.characterType = characterType;
	}

	/* Actions */
	protected void attack(Character aim) {
		timeStream.addEvent(ServerEvents.create("meleeAttack", "["+id+","+aim.id+"]"));
		aim.getDamage(7, DamageType.PLAIN);
		moveTime(500);
	}

	protected void shootMissile(int toX, int toY, ItemPile missile) {
		loseItem(missile);
		Coordinate end = getRayEnd(toX, toY);
		timeStream.addEvent(ServerEvents.create("missileFlight", "["+x+","+y+","+end.x+","+end.y+","+1+"]"));
		plane.addItem(missile, end.x, end.y);
		Cell aimCell = plane.getCell(toX, toY);
		if (aimCell.character() != null) {
			aimCell.character().getDamage(10, DamageType.PLAIN);
		}
	}

	protected void shootMissile(int toX, int toY, UniqueItem item) {
		loseItem(item);
		Coordinate end = getRayEnd(toX, toY);
		timeStream.addEvent(ServerEvents.create("missileFlight", "["+x+","+y+","+end.x+","+end.y+","+item.getType().getId()+"]"));
		plane.addItem(item, end.x, end.y);
		Cell aimCell = plane.getCell(toX, toY);
		if (aimCell.character() != null) {
			aimCell.character().getDamage(10, DamageType.PLAIN);
		}
	}

	protected void castSpell(int spellId, int x, int y) {
		timeStream.addEvent(ServerEvents.create("spellCast", "["+id+","+spellId+","+x+","+y+"]"));
		moveTime(500);
		// TODO Implement spellcasting
		throw new Error("Not implemented!");
	}

	public void learnSpell(int spellId) {
		spells.add(spellId);
	}

	protected void die() {
		isAlive = false;
		timeStream.claimCharacterDisappearance(this);
		plane.getChunkWithCell(x, y).removeCharacter(this);
		timeStream.addEvent(ServerEvents.create("death", "["+id+"]"));
	}

	protected void putOn(UniqueItem item, boolean omitEvent) {
		// Main put on function
		body.putOn(item);
		inventory.removeUnique(item);
		if (!omitEvent) {
			// Sending for mobs. Sending for players is in
			// PlayerCharacter.putOn()
			timeStream.addEvent(ServerEvents.create("putOn", "["+id+","+item.getId()+"]"));
		}
		moveTime(500);
	}

	protected void takeOff(UniqueItem item) {
		body.takeOff(item);
		inventory.add(item);
		getTimeStream().addEvent(ServerEvents.create("takeOff", "["+id+","+item.getId()+"]"));
		moveTime(500);
	}
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
	protected void pickUp(ItemPile pile) {
		timeStream.addEvent(ServerEvents.create("pickUp", "["+id+","+pile.getType().getId()+","+pile.getAmount()+"]"));
		getItem(pile);
		plane.removeItem(pile, x, y);
		moveTime(500);
	}
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
	protected void pickUp(UniqueItem item) {
		timeStream.addEvent(ServerEvents.create("pickUp", "["+id+","+item.getType().getId()+","+item.getId()+"]"));
		getItem(item);
		Chunk chunk = plane.getChunkWithCell(x, y);
		chunk.removeItem(item, x - chunk.getX(), y - chunk.getY());
		moveTime(500);
	}

	protected void drop(UniqueItem item) {
		loseItem(item);
		Chunk chunk = plane.getChunkWithCell(x, y);
		chunk.addItem(item, x - chunk.getX(), y - chunk.getY());
		timeStream.addEvent(ServerEvents.create("drop", "["+id+","+item.getType().getId()+","+item.getId()+"]"));
		moveTime(500);
	}

	protected void drop(ItemPile pile) {
		loseItem(pile);
		Chunk chunk = plane.getChunkWithCell(x, y);
		chunk.addItem(pile, x-chunk.getX(), y-chunk.getY());
		timeStream.addEvent(ServerEvents.create("pickUp", "["+id+","+pile.getType().getId()+","+pile.getAmount()+"]"));
		moveTime(500);
	}

	protected void takeFromContainer(ItemPile pile, Container container) {
		getItem(pile);
		container.removePile(pile);
		timeStream.addEvent(ServerEvents.create("takeFromContainer", "["+id+","+pile.getType().getId()+","+pile.getAmount()+","+x+","+y+"]"));
		moveTime(500);
	}

	protected void takeFromContainer(UniqueItem item, Container container) {
		getItem(item);
		container.removeUnique(item);
		timeStream.addEvent(ServerEvents.create("takeFromContainer", "["+id+","+item.getType().getId()+","+item.getId()+","+x+","+y+"]"));
		moveTime(500);
	}

	protected void putToContainer(ItemPile pile, Container container) {
		loseItem(pile);
		container.add(pile);
		timeStream.addEvent(ServerEvents.create("putToContainer", "["+id+","+pile.getType().getId()+","+pile.getAmount()+","+x+","+y+"]"));
		moveTime(500);
	}

	protected void putToContainer(UniqueItem item, Container container) {
		loseItem(item);
		container.add(item);
		timeStream.addEvent(ServerEvents.create("putToContainer", "["+id+","+item.getType().getId()+","+item.getId()+","+x+","+y+"]"));
		moveTime(500);
	}

	protected void useObject(int x, int y) {
		if (plane.getCell(x, y).isDoor()) {
			plane.openDoor(x, y);
		} else {
			throw new Error("Trying to use an object that is not a door");
		}
		timeStream.addEvent(ServerEvents.create("useObject", "["+id+","+x+","+y+"]"));
		moveTime(500);
	}

	protected void idle() {
		moveTime(500);
	}

	protected void step(int x, int y) {
		move(x, y);
		if (state == CharacterState.RUNNING) {
			changeEnergy(-30);
			moveTime(200);
		} else {
			moveTime(500);
		}

	}

	protected void makeSound(SoundType type) {
		timeStream.makeSound(x, y, type);
	}

	protected void enterState(CharacterState state) {
		this.state = state;
		timeStream.addEvent(ServerEvents.create("enterState", "["+id+","+state.state2int()+"]"));
	}

	/* Special actions */
	/**
	 * Pushes another character so he moves to another cell
	 * @param character A Character being pushed.
	 * @param side Side to push relative to the character being pushed.
	 */
	protected void push(Character character, Side side) {
		int[] d = side.side2d();
		int nx = character.x + d[0];
		int ny = character.y + d[1];
		if (plane.getCell(nx, ny).getPassability() == TerrainBasics.PASSABILITY_FREE) {
			int bufX = character.x;
			int bufY = character.y;
			character.move(nx, ny);
			if (!new Coordinate(x, y).isNear(nx, ny)) {
				move(bufX, bufY);
			}
		}
		moveTime(500);
	}

	protected void changePlaces(Character character) {
		int prevX = x;
		int prevY = y;
		move(character.x, character.y);
		character.move(prevX, prevY);
		changeEnergy(-30);
		// This event is needed for client to correctly
		// handle characters' new positions in Terrain.cells
		timeStream.addEvent(ServerEvents.create("changePlaces", "["+id+","+character.id+"]"));
		moveTime(500);
	}

	protected void scream() {
		makeSound(StaticData.getSoundType("scream"));
	}

	protected void jump(int x, int y) {
		move(x, y);
		timeStream.addEvent(ServerEvents.create("jump", "["+id+"]"));
		changeEnergy(-40);
		moveTime(500);
	}

	protected void shieldBash(Character character) {
		character.getDamage(5, DamageType.PLAIN);
		changeEnergy(7);
		timeStream.makeSound(character.x, character.y, StaticData.getSoundType("crash"));
		moveTime(500);
	}

	protected void shieldBash(int x, int y) {
		changeEnergy(7);
		timeStream.makeSound(x, y, StaticData.getSoundType("crash"));
		moveTime(500);
	}

	/* Vision */
	public boolean initialCanSee(int x, int y) {
		Coordinate characterCoord = new Coordinate(this.x, this.y);
		if (characterCoord.isNear(x, y) || this.x == x && this.y == y) {
			return true;
		}
		if (Math.floor(characterCoord.distance(x, y)) > Character.VISION_RANGE) {
			return false;
		}
		if (x == this.x || y == this.y) {
			if (x == this.x) {
				int dy = Math.abs(y - this.y) / (y - this.y);
				for (int i = this.y + dy; i != y; i += dy) {
					if (plane.getCell(x, i).getPassability() == 1) {
						return false;
					}
				}
			} else {
				int dx = Math.abs(x - this.x) / (x - this.x);
				for (int i = this.x + dx; i != x; i += dx) {
					if (plane.getCell(i, y).getPassability() == 1) {
						return false;
					}
				}
			}
			return true;
		} else if (Math.abs(x - this.x) == 1) {
			int yMin = Math.min(y, this.y);
			int yMax = Math.max(y, this.y);
			for (int i = yMin + 1; i < yMax; i++) {
				if (plane.getCell(x, i).getPassability() == 1) {
					break;
				}
				if (i == yMax - 1) {
					return true;
				}
			}
			for (int i = yMin + 1; i < yMax; i++) {
				if (plane.getCell(this.x, i).getPassability() == 1) {
					break;
				}
				if (i == yMax - 1) {
					return true;
				}
			}
			return false;
		} else if (Math.abs(y - this.y) == 1) {
			int xMin = Math.min(x, this.x);
			int xMax = Math.max(x, this.x);
			for (int i = xMin + 1; i < xMax; i++) {
				if (plane.getCell(i, y).getPassability() == 1) {
					break;
				}
				if (i == xMax - 1) {
					return true;
				}
			}
			for (int i = xMin + 1; i < xMax; i++) {
				if (plane.getCell(i, this.y).getPassability() == 1) {
					break;
				}
				if (i == xMax - 1) {
					return true;
				}
			}
			return false;
		} else if (Math.abs(x - this.x) == Math.abs(y - this.y)) {
			int dMax = Math.abs(x - this.x);
			int dx = x > this.x ? 1 : -1;
			int dy = y > this.y ? 1 : -1;
			int cx = this.x;
			int cy = this.y;
			for (int i = 1; i < dMax; i++) {
				cx += dx;
				cy += dy;
				if (plane.getCell(cx, cy).getPassability() == 1) {
					return false;
				}

			}
			return true;
		} else {
			double[][] start = new double[2][2];
			double[] end = new double[4];
			end[0] = (x > this.x) ? x - 0.5 : x + 0.5;
			end[1] = (y > this.y) ? y - 0.5 : y + 0.5;
			end[2] = x;
			end[3] = y;
			start[0][0] = (x > this.x) ? this.x + 0.5 : this.x - 0.5;
			start[0][1] = (y > this.y) ? this.y + 0.5 : this.y - 0.5;
			start[1][0] = (x > this.x) ? this.x + 0.5 : this.x - 0.5;
			start[1][1] = (y > this.y) ? this.y + 0.5 : this.y - 0.5;
			Coordinate[] rays = rays(this.x, this.y, x, y);
			jump: for (int k = 0; k < 3; k++) {
				int endNumX = (k == 0 || k == 1) ? 0 : 2;
				int endNumY = (k == 0 || k == 2) ? 1 : 3;
				for (int j = 0; j < 1; j++) {
					if (start[j][0] == this.x && start[j][1] == this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart = start[j][0];
					double yStart = start[j][1];
					for (Coordinate c : rays) {
						try {
							if (plane.getCell(c.x, c.y).getPassability() == 1) {
								if (c.x == x && c.y == y || c.x == x
										&& c.y == y) {
									continue;
								}
								if (Math.abs(((yStart - yEnd) * c.x
										+ (xEnd - xStart) * c.y + (xStart
										* yEnd - yStart * xEnd))
										/ Math.sqrt(Math.abs((xEnd - xStart)
												* (xEnd - xStart)
												+ (yEnd - yStart)
												* (yEnd - yStart)))) <= 0.5) {
									continue jump;
								}
							}
						} catch (Exception e) {
							throw new Error();
						}
					}
					return true;
				}
			}
			return false;
		}
	}

	public Coordinate getRayEnd(int endX, int endY) {
		Coordinate characterCoord = new Coordinate(this.x, this.y);
		if (characterCoord.isNear(endX, endY) || this.x == endX && this.y == endY) {
			return new Coordinate(endX, endY);
		}
		if (endX == this.x || endY == this.y) {
			if (endX == this.x) {
				int dy = Math.abs(endY - this.y) / (endY - this.y);
				for (int i = this.y + dy; i != endY + dy; i += dy) {
					if (plane.getCell(endX, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(endX, i - dy);
					}
				}
			} else {
				int dx = Math.abs(endX - this.x) / (endX - this.x);
				for (int i = this.x + dx; i != endX + dx; i += dx) {
					if (plane.getCell(i, endY).getPassability() != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(i - dx, endY);
					}
				}
			}
			return new Coordinate(endX, endY);
		} else if (Math.abs(endX - this.x) == 1) {
			int dy = Math.abs(endY - this.y) / (endY - this.y);
			int y1 = endY, y2 = endY;
			for (int i = this.y + dy; i != endY + dy; i += dy) {
				if (plane.getCell(endX, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					y1 = i - dy;
					break;
				}
				if (i == endY) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i = this.y + dy; i != endY + dy; i += dy) {
				if (plane.getCell(this.x, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					y2 = i - dy;
					break;
				}
			}
			Coordinate answer;
			if (characterCoord.distance(endX, y1) > characterCoord.distance(this.x, y2)) {
				answer = new Coordinate(endX, y1);
			} else {
				answer = new Coordinate(this.x, y2);
			}
			if (answer.x == this.x
					&& answer.y == y2
					&& plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_FREE) {
				// If answer is the furthest cell on the same line, but
				// {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == this.x
					&& answer.y == y2
					&& plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_NO) {
				// If answer is the furthest cell on the same line, and
				// {endX:endY} has no passage
				answer.y = endY - dy;
			}
			return answer;
		} else if (Math.abs(endY - this.y) == 1) {
			int dx = Math.abs(endX - this.x) / (endX - this.x);
			int x1 = endX, x2 = endX;
			for (int i = this.x + dx; i != endX + dx; i += dx) {
				if (plane.getCell(i, endY).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					x1 = i - dx;
					break;
				}
				if (i == endX) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i = this.x + dx; i != endX + dx; i += dx) {
				if (plane.getCell(i, this.y).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					x2 = i - dx;
					break;
				}
			}
			Coordinate answer;
			if (characterCoord.distance(x1, endY) > characterCoord.distance(x2, this.y)) {
				answer = new Coordinate(x1, endY);
			} else {
				answer = new Coordinate(x2, this.y);
			}
			if (answer.x == x2
					&& answer.y == this.y
					&& plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_FREE) {
				// If answer is the furthest cell on the same line, but
				// {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == x2
					&& answer.y == this.y
					&& plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_NO) {
				// If answer is the furthest cell on the same line, and
				// {endX:endY} has no passage
				answer.x = endX - dx;
			}

			return answer;
		} else if (Math.abs(endX - this.x) == Math.abs(endY - this.y)) {
			int dMax = Math.abs(endX - this.x);
			int dx = endX > this.x ? 1 : -1;
			int dy = endY > this.y ? 1 : -1;
			int cx = this.x;
			int cy = this.y;
			for (int i = 1; i <= dMax; i++) {
				cx += dx;
				cy += dy;
				if (plane.getCell(cx, cy).getPassability() == 1) {
					return new Coordinate(cx - dx, cy - dy);
				}

			}
			return new Coordinate(endX, endY);
		} else {
			double[][] start = new double[2][2];
			double[] end = new double[4];
			end[0] = (endX > this.x) ? endX - 0.5 : endX + 0.5;
			end[1] = (endY > this.y) ? endY - 0.5 : endY + 0.5;
			end[2] = endX;
			end[3] = endY;
			start[0][0] = (endX > this.x) ? this.x + 0.5 : this.x - 0.5;
			start[0][1] = (endY > this.y) ? this.y + 0.5 : this.y - 0.5;
			start[1][0] = (endX > this.x) ? this.x + 0.5 : this.x - 0.5;
			// start[0][1]=this.y;
			// start[1][0]=this.x;
			start[1][1] = (endY > this.y) ? this.y + 0.5 : this.y - 0.5;
			Coordinate[] rays = rays(this.x, this.y, endX, endY);
			int breakX = this.x, breakY = this.y;
			jump: for (int k = 0; k < 3; k++) {
				int endNumX = (k == 0 || k == 1) ? 0 : 2;
				int endNumY = (k == 0 || k == 2) ? 1 : 3;
				for (int j = 0; j < 1; j++) {
					if (start[j][0] == this.x && start[j][1] == this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart = start[j][0];
					double yStart = start[j][1];
					for (Coordinate c : rays) {
						try {
							if (plane.getCell(c.x, c.y).getPassability() == 1) {
								if (Math.abs(((yStart - yEnd) * c.x
										+ (xEnd - xStart) * c.y + (xStart
										* yEnd - yStart * xEnd))
										/ Math.sqrt(Math.abs((xEnd - xStart)
												* (xEnd - xStart)
												+ (yEnd - yStart)
												* (yEnd - yStart)))) <= 0.5) {
									continue jump;
								}

							} else {
								breakX = c.x;
								breakY = c.y;
							}
						} catch (Exception e) {
							throw new Error();
						}
					}
					return new Coordinate(endX, endY);
				}
			}
			return new Coordinate(breakX, breakY);
		}
	}

	public Coordinate[] rays(int startX, int startY, int endX, int endY) {
		return Utils.concatAll(
				TerrainBasics.vector(startX, startY, endX, endY), TerrainBasics
						.vector(startX, startY + (endY > startY ? 1 : -1), endX
								+ (endX > startX ? -1 : 1), endY),
				TerrainBasics.vector(startX + (endX > startX ? 1 : -1), startY,
						endX, endY + (endY > startY ? -1 : 1)));
	}

	

	/* Getters */
	public int hashCode() {
		return id;
	}


	public CharacterType getType() {
		return characterType;
	}
	public boolean isAlive() {
		return isAlive;
	}
	/**
	 * @return the actionPoints
	 */
	public int getActionPoints() {
		return actionPoints;
	}

	public int increaseActionPoints(int value) {
		return actionPoints += value;
	}

	public int getFraction() {
		return fraction;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	/* Setters */
	public void move(int x, int y) {
		/**
		 * Changes character's position.
		 * 
		 * Note that this is not a character action, this method is also called
		 * when character blinks, being pushed and so on. For action method, use
		 * Character.step.
		 */
		plane.getCell(this.x, this.y).setPassability(
				TerrainBasics.PASSABILITY_FREE);
		plane.getCell(this.x, this.y).character(false);
		this.x = x;
		this.y = y;
		plane.getCell(x, y).character(this);
		plane.getCell(x, y).setPassability(TerrainBasics.PASSABILITY_SEE);
		timeStream.addEvent(ServerEvents.create("move", "["+id+","+x+","+y+"]"));
		timeStream.notifyNeighborsVisiblilty(this);
	}

	public void getDamage(int amount, DamageType type) {
		timeStream.addEvent(ServerEvents.create("damage", "["+id+","+amount+","+type.type2int()+"]"));
	}

	protected void changeEnergy(int amount) {
		amount = Math.min(amount, maxEp - ep);
		if (amount != 0) {
			ep += amount;
			timeStream.addEvent(ServerEvents.create("changeEnergy", "["+id+","+ep+"]"));
		} else {
			if (state == CharacterState.RUNNING) {
				enterState(CharacterState.DEFAULT);
			}
		}
	}

	protected void removeEffect(CharacterEffect effect) {
		effects.remove(effect);
	}

	public void getItem(UniqueItem item) {
		inventory.add(item);
		timeStream.addEvent(ServerEvents.create("getItem", "["+id+","+item.getType().getId()+","+item.getId()+"]"));
	}

	public void eventlessGetItem(UniqueItem item) {
		inventory.add(item);
	}

	public void eventlessGetItem(ItemPile pile) {
		inventory.add(pile);
	}

	public void getItem(ItemPile pile) {
		inventory.add(pile);
		timeStream.addEvent(ServerEvents.create("getItem", "["+id+","+pile.getType().getId()+","+pile.getAmount()+"]"));
	}

	public void loseItem(UniqueItem item) {
		if (inventory.hasUnique(item.getId())) {
			inventory.removeUnique(item);
			timeStream.addEvent(ServerEvents.create("loseItem", "["+id+","+item.getType().getId()+","+item.getId()+"]"));
		} else {
			throw new Error("An attempt to lose an item width id "
					+ item.getId()
					+ " that is neither in inventory nor in equipment");
		}
	}

	public void loseItem(ItemPile pile) {
		inventory.removePile(pile);
		timeStream.addEvent(ServerEvents.create("loseItem", "["+id+","+pile.getType().getId()+","+pile.getAmount()+"]"));
	}

	public void setFraction(int fraction) {
		this.fraction = fraction;
	}

	public void addEffect(int effectId, int duration, int modifier) {
		if (effects.containsKey(effectId)) {
			removeEffect(effectId);
		}
		effects.put(effectId, new Character.Effect(effectId, duration, modifier));
		timeStream.addEvent(ServerEvents.create("effectStart", "["+id+","+effectId+"]"));
	}

	public void removeEffect(int effectId) {
		effects.remove(effectId);
		timeStream.addEvent(ServerEvents.create("effectEnd", "["+id+","+effectId+"]"));
	}

	protected void moveTime(int amount) {
		for (Character.Effect e : effects.values()) {
			e.duration -= amount;
			if (e.duration < 0) {
				removeEffect(e.effectId);
			}
		}
		changeEnergy(10);
	}

	/* Checks */
	public boolean at(int atX, int atY) {
		return x == atX && y == atY;
	}

	public boolean hasItem(int typeId, int amount) {
		return inventory.hasPile(typeId, amount);
	}

	public boolean isEnemy(Character ch) {
		if (fraction == FRACTION_NEUTRAL) {
			return false;
		}
		return ch.fraction != fraction;
	}

	/* Data */
	public String jsonGetEffects() {
		return "[]";
	}

	public String jsonGetEquipment() {
		return body.toJson();
	}

	public int[] getEffects() {
		return new int[0];
	}

	public int[][] getEquipment() {
		return new int[0][2];
	}

	public void setTimeStream(TimeStream timeStream) {
		this.timeStream = timeStream;
	}

	public TimeStream getTimeStream() {
		return timeStream;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(name));
		jArray.add(new JsonPrimitive(x));
		jArray.add(new JsonPrimitive(y));
		jArray.add(new JsonPrimitive(fraction));
		return jArray;
	}

	/* Nested classes */
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
