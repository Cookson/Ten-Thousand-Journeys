package erpoge.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.core.characters.CharacterEffect;
import erpoge.core.characters.CharacterState;
import erpoge.core.characters.DamageType;
import erpoge.core.characters.NonPlayerCharacter;
import erpoge.core.inventory.EquipmentMap;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.ItemMap;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.itemtypes.Attribute;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.magic.Spells;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.meta.Utils;
import erpoge.core.net.serverevents.EventAttributeChange;
import erpoge.core.net.serverevents.EventCastSpell;
import erpoge.core.net.serverevents.EventChangeEnergy;
import erpoge.core.net.serverevents.EventChangeMana;
import erpoge.core.net.serverevents.EventChangePlaces;
import erpoge.core.net.serverevents.EventDamage;
import erpoge.core.net.serverevents.EventDeath;
import erpoge.core.net.serverevents.EventDropItem;
import erpoge.core.net.serverevents.EventEffectEnd;
import erpoge.core.net.serverevents.EventEffectStart;
import erpoge.core.net.serverevents.EventGetItemPile;
import erpoge.core.net.serverevents.EventGetUniqueItem;
import erpoge.core.net.serverevents.EventJump;
import erpoge.core.net.serverevents.EventLoseItem;
import erpoge.core.net.serverevents.EventMeleeAttack;
import erpoge.core.net.serverevents.EventMissileFlight;
import erpoge.core.net.serverevents.EventMove;
import erpoge.core.net.serverevents.EventPickUp;
import erpoge.core.net.serverevents.EventPutOn;
import erpoge.core.net.serverevents.EventPutToContainer;
import erpoge.core.net.serverevents.EventTakeFromContainer;
import erpoge.core.net.serverevents.EventTakeOff;
import erpoge.core.net.serverevents.EventUseObject;
import erpoge.core.objects.SoundType;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.Container;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.TerrainBasics;

public abstract class Character extends Coordinate {
	public final static int
		FRACTION_NEUTRAL = -1,
		FRACTION_PLAYER = 1,
		FRACTION_AGRESSIVE = 0;
	
	public static final String DEFAULT_NAME = "Default Name";
	public static final double VISION_RANGE = 8;
	
	public int hp;
	public int mp;
	protected int ep;
	protected int energy;
	public int maxHp;
	public int maxMp;
	protected int maxEp;
	protected int armor;
	protected int evasion;
	protected int fireRes = 0;
	protected int coldRes = 0;
	protected int poisonRes = 0;
	protected int acidRes = 0;
	protected int actionPoints = 0;
	public int fraction;
	public HorizontalPlane plane;
	public Chunk chunk;
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
	public final EquipmentMap equipment = new EquipmentMap();
	public HashSet<NonPlayerCharacter> observers = new HashSet<NonPlayerCharacter>();
	
	protected CharacterState state = CharacterState.DEFAULT;

	protected TimeStream timeStream;
	public Character(HorizontalPlane plane, int x, int y, String type, String name) {
	// Common character creation: with all attributes, in location.
		super(x, y);
		this.name = name;
		this.type = type;
		this.plane = plane;
		this.chunk = plane.getChunkWithCell(x, y);
		fraction = 0;
	}
	/* Actions */
	protected void attack(Character aim) {
		getTimeStream().addEvent(new EventMeleeAttack(characterId, aim.characterId));
		aim.getDamage(7 , DamageType.PLAIN);
		moveTime(500);
	}
	protected void shootMissile(int toX, int toY, ItemPile missile) {
		loseItem(missile);
		Coordinate end = getRayEnd(toX, toY);
		getTimeStream().addEvent(new EventMissileFlight(x, y, end.x, end.y, 1));
		plane.getChunkWithCell(end.x, end.y).addItem(missile, end.x, end.y);
		Cell aimCell = plane.getCell(toX, toY);
		if (aimCell.character() != null) {
			aimCell.character().getDamage(10, DamageType.PLAIN);
		}
	}
	protected void castSpell(int spellId, int x, int y) {
		getTimeStream().addEvent(new EventCastSpell(characterId, spellId, x, y));
		Spells.cast(this, spellId, x, y);
		changeMana(-25);
		moveTime(500);
	}
	public void learnSpell(int spellId) {
		spells.add(spellId);
	}
	protected void die() {
		for (NonPlayerCharacter character : observers) {
			character.discoverDeath(this);
		}
		plane.getChunkWithCell(x,y).removeCharacter(this);
		getTimeStream().addEvent(new EventDeath(characterId));
	}
	protected void putOn(UniqueItem item, boolean omitEvent) {
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
		} else if (equipment.hasPiece(slot)) {
			// ���� ����� ������� ���� �� ����
			throw new Error("Character " + name
					+ " is trying to put on a piece he is already wearing");
		}
		equipment.add(item);
		inventory.removeUnique(item);
		if (!omitEvent) {
		// Sending for mobs. Sending for players is in PlayerCharacter.putOn()
			getTimeStream().addEvent(new EventPutOn(characterId, item.getItemId()));
		}
		addItemBonuses(item);
		moveTime(500);
	}
	protected void takeOff(UniqueItem item) {
		equipment.removeSlot(item.getType().getSlot());
		inventory.add(item);
		getTimeStream().addEvent(new EventTakeOff(characterId, item.getItemId()));
		removeItemBonuses(item);
		moveTime(500);
	}
	protected void pickUp(ItemPile pile) {
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
		getTimeStream().addEvent(new EventPickUp(characterId, pile.getType().getTypeId(), pile.getAmount()));
		getItem(pile);
		plane.getChunkWithCell(x,y).removeItem(pile, x, y);
		moveTime(500);
	}
	protected void pickUp(UniqueItem item) {
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
		timeStream.addEvent(new EventPickUp(characterId, item.getTypeId(), item.getItemId()));
		getItem(item);
		Chunk chunk = plane.getChunkWithCell(x,y);
		chunk.removeItem(item, x-chunk.getX(), y-chunk.getY());
		moveTime(500);
	}
	protected void drop(UniqueItem item) {
		loseItem(item);
		Chunk chunk = plane.getChunkWithCell(x,y);
		chunk.addItem(item, x-chunk.getX(), y-chunk.getY());
		timeStream.addEvent(new EventDropItem(characterId, item.getTypeId(), item.getItemId()));
		moveTime(500);
	}
	protected void drop(ItemPile pile) {
		loseItem(pile);
		Chunk chunk = plane.getChunkWithCell(x,y);
		chunk.addItem(pile, x-chunk.getX(), y-chunk.getY());
		timeStream.addEvent(new EventDropItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
		moveTime(500);
	}
	protected void takeFromContainer(ItemPile pile, Container container) {
		getItem(pile);
		container.removePile(pile);
		timeStream.addEvent(new EventTakeFromContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	protected void takeFromContainer(UniqueItem item, Container container) {
		getItem(item);
		container.removeUnique(item);
		timeStream.addEvent(new EventTakeFromContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}
	protected void putToContainer(ItemPile pile, Container container) {
		loseItem(pile);
		container.add(pile);
		timeStream.addEvent(new EventPutToContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	protected void putToContainer(UniqueItem item, Container container) {
		loseItem(item);
		container.add(item);
		timeStream.addEvent(new EventPutToContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}
	protected void useObject(int x, int y) {
		if (plane.getCell(x, y).isDoor()) {
			plane.openDoor(x,y);
		} else {
			throw new Error("Trying to use an object that is not a door");
		}
		getTimeStream().addEvent(new EventUseObject(characterId, x, y));
		moveTime(500);
	}
	protected void idle() {
		moveTime(500);
	}
	protected void step(int x, int y) {
		move(x,y);
		moveTime(500);
	} 
	protected void makeSound(SoundType type) {
		timeStream.makeSound(x,y,type);
	}
	/* Special actions */
	protected void push(Character character, Side side) {
	/**
	 * Pushes another character so he moves
	 */
		int[] d = side.side2d();
		int nx = character.x+d[0];
		int ny = character.y+d[1];
		if (plane.getCell(nx, ny).getPassability() == TerrainBasics.PASSABILITY_FREE) {
			int bufX = character.x;
			int bufY = character.y;
			character.move(nx, ny);
			if (!isNear(nx, ny)) {
				move(bufX, bufY);
			}
		}
		moveTime(500);
	}
	protected void changePlaces(Character character) {
		int prevX = x;
		int prevY = y;
		move(character.x, character.y);
		character.move(prevX,prevY);
		changeEnergy(-30);
		// This event is needed for client to correctly 
		// handle characters' new positions in Terrain.cells
		getTimeStream().addEvent(new EventChangePlaces(characterId, character.characterId));
		moveTime(500);		
	}
	protected void scream() {
		makeSound(SoundType.SCREAM);
	}
	protected void jump(int x, int y) {
		move(x,y);
		getTimeStream().addEvent(new EventJump(characterId));
		changeEnergy(-40);
		moveTime(500);
	}
	protected void shieldBash(Character character) {
		if (!equipment.hasPiece(ItemType.SLOT_LEFT_HAND)) {
			throw new Error(name+" doesn't have a shield");
		}
		character.getDamage(5, DamageType.PLAIN);
		changeEnergy(7);
		timeStream.makeSound(character.x, character.y, SoundType.CRASH);
		moveTime(500);
	}
	protected void shieldBash(int x, int y) {
		if (!equipment.hasPiece(ItemType.SLOT_LEFT_HAND)) {
			throw new Error(name+" doesn't have a shield");
		}
		changeEnergy(7);
		getTimeStream().makeSound(x, y, SoundType.CRASH);
		moveTime(500);
	}
	/* Vision */
	public void notifyNeighborsVisiblilty() {
	/**
	 * This method's name sucks, but I don't know how to call it : (
	 * 
	 * Tell every nearby character about this character's new position,
	 * so nearby characters can update status of this character as
	 * seen / unseen.
	 */
		for (NonPlayerCharacter character : timeStream.nonPlayerCharacters) {
			character.tryToSee(this);
		}
		notifyObservers();
		HashSet<NonPlayerCharacter> observersCopy = new HashSet<NonPlayerCharacter>(observers);
		for (NonPlayerCharacter character : observersCopy) {
			character.tryToUnsee(this);
		}
	}
	public HashSet<NonPlayerCharacter> getNearbyNonPlayerCharacters() {
	/**
	 * Get character that are near this character 
	 * in square with VISION_RANGE*2+1 side length.
	 */
		HashSet<NonPlayerCharacter> answer = new HashSet<NonPlayerCharacter>();
		for (NonPlayerCharacter character : timeStream.nonPlayerCharacters) {
		// Quickly select characters that could be seen (including this Seer itself)
			if (
				Math.abs(character.x - x) <= Character.VISION_RANGE && 
				Math.abs(character.y - y) <= Character.VISION_RANGE
			) {
				answer.add(character);
			}
		}
		answer.remove(this);
		return answer;
	}
	public boolean initialCanSee(int x, int y) {
		if (this.isNear(x,y) || this.x==x && this.y==y) {
			return true;
		}
		if (Math.floor(this.distance(x, y))>Character.VISION_RANGE) {
			return false;
		}
		if (x==this.x || y==this.y) {
			if (x==this.x) {
				int dy=Math.abs(y-this.y)/(y-this.y);
				for (int i=this.y+dy; i!=y; i+=dy) {
					if (plane.getCell(x,i).getPassability() == 1) {
						return false;
					}
				}
			} else {
				int dx=Math.abs(x-this.x)/(x-this.x);
				for (int i=this.x+dx; i!=x; i+=dx) {
					if (plane.getCell(i,y).getPassability()==1) {
						return false;
					}
				}
			}
			return true;
		} else if (Math.abs(x-this.x)==1) {
			int yMin=Math.min(y,this.y);
			int yMax=Math.max(y,this.y);
			for (int i=yMin+1; i<yMax; i++) {
				if (plane.getCell(x,i).getPassability()==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			for (int i=yMin+1;i<yMax;i++) {
				if (plane.getCell(this.x,i).getPassability()==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			return false;
		} else if (Math.abs(y-this.y)==1) {
			int xMin=Math.min(x,this.x);
			int xMax=Math.max(x,this.x);
			for (int i=xMin+1;i<xMax;i++) {
				if (plane.getCell(i,y).getPassability()==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			for (int i=xMin+1;i<xMax;i++) {
				if (plane.getCell(i,this.y).getPassability()==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			return false;
		} 
		else if (Math.abs(x-this.x) == Math.abs(y-this.y)) {
			int dMax=Math.abs(x-this.x);
			int dx=x>this.x ? 1 : -1;
			int dy=y>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (plane.getCell(cx,cy).getPassability()==1) {
					return false;
				}
				
			}
			return true;
		} 
		else {
			double[][] start = new double[2][2];
			double[] end = new double[4];
			end[0]=(x>this.x)? x-0.5 : x+0.5;
			end[1]=(y>this.y)? y-0.5 : y+0.5;
			end[2]=x;
			end[3]=y;
			start[0][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			start[0][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			start[1][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			start[1][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			Coordinate[] rays=rays(this.x,this.y,x,y);
			jump:
			for (int k=0; k<3; k++) {
				int endNumX=(k==0 || k==1)?0:2;
				int endNumY=(k==0 || k==2)?1:3;
				for (int j=0;j<1;j++) {
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart=start[j][0];
					double yStart=start[j][1];
					for (Coordinate c : rays) {
						try {
							if (plane.getCell(c.x, c.y).getPassability()==1) {
								if (c.x==x && c.y==y || c.x==x && c.y==y) {
									continue;
								}
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
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
		if (this.isNear(endX,endY) || this.x==endX && this.y==endY) {
			return new Coordinate(endX, endY);
		}
		if (endX==this.x || endY==this.y) {
			if (endX==this.x) {
				int dy=Math.abs(endY-this.y)/(endY-this.y);
				for (int i=this.y+dy; i!=endY+dy; i+=dy) {
					if (plane.getCell(endX, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(endX, i-dy);
					}
				}
			} else {
				int dx=Math.abs(endX-this.x)/(endX-this.x);
				for (int i=this.x+dx; i!=endX+dx; i+=dx) {
					if (plane.getCell(i, endY).getPassability() != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(i-dx, endY);
					}
				}
			}
			return new Coordinate(endX, endY);
		} else if (Math.abs(endX-this.x)==1) {
			int dy=Math.abs(endY-this.y)/(endY-this.y);
			int y1 = endY, y2 = endY;
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (plane.getCell(endX, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					y1 = i-dy;
					break;
				}
				if (i==endY) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (plane.getCell(this.x, i).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					y2 = i-dy;
					break;
				}
			}
			Coordinate answer;
			if (distance(endX, y1) > distance(this.x, y2)) {
				answer = new Coordinate(endX, y1);
			} else {
				answer = new Coordinate(this.x, y2);
			}
			if (answer.x == this.x && answer.y == y2 && plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == this.x && answer.y == y2 && plane.getCell(endX, endY).getPassability() == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.y = endY-dy;
			}
			return answer;
		} else if (Math.abs(endY-this.y)==1) {
			int dx=Math.abs(endX-this.x)/(endX-this.x);
			int x1 = endX, x2 = endX;
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (plane.getCell(i,endY).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					x1 = i-dx;
					break;
				}
				if (i==endX) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (plane.getCell(i,this.y).getPassability() != TerrainBasics.PASSABILITY_FREE) {
					x2 = i-dx;
					break;
				}
			}
			Coordinate answer;
			if (distance(x1, endY) > distance(x2, this.y)) {
				answer = new Coordinate(x1, endY);
			} else {
				answer = new Coordinate(x2, this.y);
			}
			if (answer.x == x2 && answer.y == this.y && plane.getCell(endX,endY).getPassability() == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == x2 && answer.y == this.y && plane.getCell(endX,endY).getPassability() == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.x = endX-dx;
			}
			
			return answer;
		} 
		else if (Math.abs(endX-this.x) == Math.abs(endY-this.y)) {
			int dMax=Math.abs(endX-this.x);
			int dx=endX>this.x ? 1 : -1;
			int dy=endY>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<=dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (plane.getCell(cx,cy).getPassability()==1) {
					return new Coordinate(cx-dx, cy-dy);
				}
				
			}
			return new Coordinate(endX, endY);
		} 
		else {
			double[][] start = new double[2][2];
			double[] end = new double[4];
			end[0]=(endX>this.x)? endX-0.5 : endX+0.5;
			end[1]=(endY>this.y)? endY-0.5 : endY+0.5;
			end[2]=endX;
			end[3]=endY;
			start[0][0]=(endX>this.x)? this.x+0.5 : this.x-0.5;
			start[0][1]=(endY>this.y)? this.y+0.5 : this.y-0.5;
			start[1][0]=(endX>this.x)? this.x+0.5 : this.x-0.5;
			// start[0][1]=this.y;
			// start[1][0]=this.x;
			start[1][1]=(endY>this.y)? this.y+0.5 : this.y-0.5;
			Coordinate[] rays=rays(this.x,this.y,endX,endY);
			int breakX=this.x, breakY=this.y;
			jump:
			for (int k=0;k<3;k++) {
				int endNumX=(k==0 || k==1)?0:2;
				int endNumY=(k==0 || k==2)?1:3;
				for (int j=0;j<1;j++) {
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart = start[j][0];
					double yStart = start[j][1];
					for (Coordinate c : rays) {
						try {
							if (plane.getCell(c.x,c.y).getPassability()==1) {
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
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
	public Coordinate[] rays (int startX, int startY, int endX, int endY) {
		return Utils.concatAll(
			TerrainBasics.vector(startX, startY, endX, endY),
			TerrainBasics.vector(startX,startY+(endY>startY ? 1 : -1),endX+(endX>startX ? -1 : 1),endY),
			TerrainBasics.vector(startX+(endX>startX ? 1 : -1),startY,endX,endY+(endY>startY ? -1 : 1))
		);
	}
	
	/* Character state observing */
	// NonPlayerCharacters may observe Characters and so track 
	// their coordinates.
	public void addObserver(NonPlayerCharacter character) {
		observers.add(character);
	}
	public void removeObserver(NonPlayerCharacter character) {
		observers.remove(character);
	}
	private void notifyObservers() {
	/**
	 * Send this character's coordinate data to observers
	 */
		for (NonPlayerCharacter character : observers) {
			character.updateObservation(this,x,y);
		}
	}
	/* Getters */
	public int getAttribute(Attribute attribute) {
		switch (attribute) {
		case ARMOR:       return armor;
		case EVASION:     return evasion;
		case MAX_HP:      return maxHp;
		case MAX_MP:      return maxMp;
		case HP:          return hp;
		case MP:          return mp;
		default:
			throw new Error("Unknown attribute");
		}
	}
	public int hashCode() {
		return characterId;
	}
	public int getFraction() {
		return fraction;
	}
	public int getActionPoints() {
		return actionPoints;
	}
	/* Setters */
	public void move(int x, int y) {
	/**
	 * Changes character's position.
	 * 
	 * Note that this is not a character action, this method is
	 * also called when character blinks, being pushed and so on.
	 * For action method, use Character.step.
	 */
		plane.getCell(this.x,this.y).setPassability(TerrainBasics.PASSABILITY_FREE);
		plane.getCell(this.x,this.y).character(false);
		this.x = x;
		this.y = y;
		plane.getCell(x,y).character(this);
		plane.getCell(x,y).setPassability(TerrainBasics.PASSABILITY_SEE);
		timeStream.addEvent(new EventMove(characterId, x, y));
		notifyNeighborsVisiblilty();
	}
	public void getDamage(int amount, DamageType type) {
		hp -= amount;
		getTimeStream().addEvent(new EventDamage(characterId, amount, type.type2int()));
		if (hp <= 0) {
			die();
		}
	}
	protected void increaseHp(int amount) {
		hp = (hp + amount > maxHp) ? maxHp : hp + amount;
	}
	protected void changeEnergy(int amount) {
		amount = Math.min(amount, maxEp-ep);
		if (amount != 0) {
			ep += amount;
			getTimeStream().addEvent(new EventChangeEnergy(characterId, ep));
		}
	}
	protected void changeMana(int amount) {
		amount = Math.min(amount, maxMp-mp);
		if (amount != 0) {
			mp += amount;
			getTimeStream().addEvent(new EventChangeMana(characterId, mp));
		}
	}
	protected void removeEffect(CharacterEffect effect) {
		effects.remove(effect);
	}
	public void getItem(UniqueItem item) {
		inventory.add(item);
		timeStream.addEvent(new EventGetUniqueItem(characterId, item.getTypeId(), item.getItemId()));
	}
	public void eventlessGetItem(UniqueItem item) {
		inventory.add(item);
	}
	public void eventlessGetItem(ItemPile pile) {
		inventory.add(pile);
	}
	public void getItem(ItemPile pile) {
		inventory.add(pile);
		getTimeStream().addEvent(new EventGetItemPile(characterId, pile.getTypeId(), pile.getAmount()));
	}
	public void loseItem(UniqueItem item) {
		if (inventory.hasUnique(item.getItemId())) {
			inventory.removeUnique(item);
			getTimeStream().addEvent(new EventLoseItem(characterId, item.getType().getTypeId(), item.getItemId()));
		} else {
			throw new Error("An attempt to lose an item width id " + item.getItemId()
					+ " that is neither in inventory nor in equipment");
		}
	}
	public void loseItem(ItemPile pile) {
		inventory.removePile(pile);
		getTimeStream().addEvent(new EventLoseItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
	}
	public void setFraction(int fraction) {
		this.fraction = fraction;
	}
	public void addEffect(int effectId, int duration, int modifier) {
		if (effects.containsKey(effectId)) {
			removeEffect(effectId);
		}
		effects.put(effectId, new Character.Effect(effectId, duration, modifier));
		getTimeStream().addEvent(new EventEffectStart(characterId, effectId));
	}
	public void removeEffect(int effectId) {
		effects.remove(effectId);
		getTimeStream().addEvent(new EventEffectEnd(characterId, effectId));
	}
	public void moveTime(int amount) {
		actionPoints -= amount;
		for (Character.Effect e : effects.values()) {
			e.duration -= amount;
			if (e.duration < 0) {
				removeEffect(e.effectId);
			}
		}
		changeEnergy(10);
	}
	private void addItemBonuses(Item item) {
	/**
	 * Add bonuses of item after putting in on
	 */
		item.getType().addBonuses(this);
	}
	private void removeItemBonuses(Item item) {
	/**
	 * Add bonuses of item after putting in on
	 */
		item.getType().removeBonuses(this);
	}
	public void changeAttribute(Attribute attribute, int value) {
		int resultValue = -9000;
		switch (attribute) {
		case ARMOR:       resultValue = (armor += value); break;
		case EVASION:     resultValue = (evasion += value); break;
		case MAX_HP:      resultValue = (maxHp += value); 
		                                 hp += value; 
                          getTimeStream().addEvent(new EventAttributeChange(characterId, Attribute.HP.attr2int(), hp));
		                  break;
		case MAX_MP:      resultValue = (maxMp += value); 
                                         hp += value; 
                          getTimeStream().addEvent(new EventAttributeChange(characterId, Attribute.MP.attr2int(), mp));
                          break;
		default:
			throw new Error("Unknown attribute");
		}
		getTimeStream().addEvent(new EventAttributeChange(characterId, attribute.attr2int(), resultValue));
	}
	protected void increaseActionPoints(int value) {
		actionPoints += value;
	}
	/* Checks */
	public boolean at(int atX, int atY) {
		return x==atX && y==atY;
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
		return equipment.jsonGetEquipment();
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
