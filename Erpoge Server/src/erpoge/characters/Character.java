package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Side;
import erpoge.Utils;
import erpoge.charactereffects.CharacterEffect;
import erpoge.inventory.AmmunitionMap;
import erpoge.inventory.Item;
import erpoge.inventory.ItemMap;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.Attribute;
import erpoge.itemtypes.ItemType;
import erpoge.magic.Spells;
import erpoge.objects.SoundType;
import erpoge.serverevents.*;
import erpoge.terrain.Container;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.World;

public abstract class Character extends Coordinate {
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
	
	public static final String DEFAULT_NAME = "Default Name";
	public static final double VISION_RANGE = 8;
	
	protected int hp;
	protected int mp;
	protected int ep;
	protected int energy;
	protected int maxHp;
	protected int maxMp;
	protected int maxEp;
	protected int armor;
	protected int evasion;
	protected int fireRes = 0;
	protected int coldRes = 0;
	protected int poisonRes = 0;
	protected int acidRes = 0;
	protected int actionPoints = 0;
	protected int fraction;
	public Location location;
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
	public HashSet<NonPlayerCharacter> observers = new HashSet<NonPlayerCharacter>();
	
	protected CharacterState state = CharacterState.DEFAULT;
	
	
	public Character(String t, String n, int x, int y) {
	// Common character creation: with all attributes, in location
		super(x, y);
		name = n;
		type = t;
		fraction = 0;
	}	
	/* Actions */
	protected void attack(Character aim) {
		location.addEvent(new EventMeleeAttack(characterId, aim.characterId));
		aim.getDamage(1 , DAMAGE_PLAIN);
		moveTime(500);
	}
	protected void shootMissile(int toX, int toY, ItemPile missile) {
		loseItem(missile);
		Coordinate end = getRayEnd(toX, toY);
		location.addEvent(new EventMissileFlight(x, y, end.x, end.y, 1));
		location.addItem(missile, end.x, end.y);
		if (location.cells[end.x][end.y].character() != null) {
			location.cells[end.x][end.y].character().getDamage(10, DAMAGE_PLAIN);
		}
	}
	protected void castSpell(int spellId, int x, int y) {
		location.addEvent(new EventCastSpell(characterId, spellId, x, y));
		Spells.cast(this, spellId, x, y);
		moveTime(500);
	}
	public void learnSpell(int spellId) {
		spells.add(spellId);
	}
	protected void die() {
		for (NonPlayerCharacter character : observers) {
			character.discoverDeath(this);
		}
		location.removeCharacter(this);
		location.addEvent(new EventDeath(characterId));
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
		addItemBonuses(item);
		moveTime(500);
	}
	protected void takeOff(UniqueItem item) {
		ammunition.removeSlot(item.getType().getSlot());
		inventory.add(item);
		if (!this.isOnGlobalMap()) {
		// Sending for mobs. Sending for players is in PlayerCharacter.putOn()
			location.addEvent(new EventTakeOff(characterId, item.getItemId()));
		}
		removeItemBonuses(item);
		moveTime(500);
	}
	protected void pickUp(ItemPile pile) {
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
		location.addEvent(new EventPickUp(characterId, pile.getType().getTypeId(), pile.getAmount()));
		getItem(pile);
		location.removeItem(pile, x, y);
		moveTime(500);
	}
	protected void pickUp(UniqueItem item) {
	/**
	 * Pick up an item lying on the same cell where the character stands.
	 */
		location.addEvent(new EventPickUp(characterId, item.getTypeId(), item.getItemId()));
		getItem(item);
		location.removeItem(item, x, y);
		moveTime(500);
	}
	protected void drop(UniqueItem item) {
		loseItem(item);
		location.addItem(item, x, y);
		location.addEvent(new EventDropItem(characterId, item.getTypeId(), item.getItemId()));
		moveTime(500);
	}
	protected void drop(ItemPile pile) {
		loseItem(pile);
		location.addItem(pile, x, y);
		location.addEvent(new EventDropItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
		moveTime(500);
	}
	protected void takeFromContainer(ItemPile pile, Container container) {
		getItem(pile);
		container.removePile(pile);
		location.addEvent(new EventTakeFromContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	protected void takeFromContainer(UniqueItem item, Container container) {
		getItem(item);
		container.removeUnique(item);
		location.addEvent(new EventTakeFromContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}
	protected void putToContainer(ItemPile pile, Container container) {
		loseItem(pile);
		container.add(pile);
		location.addEvent(new EventPutToContainer(characterId, pile.getTypeId(), pile.getAmount(), x, y));
		moveTime(500);
	}
	protected void putToContainer(UniqueItem item, Container container) {
		loseItem(item);
		container.add(item);
		location.addEvent(new EventPutToContainer(characterId, item.getTypeId(), item.getItemId(), x, y));
		moveTime(500);
	}
	protected void useObject(int x, int y) {
			if (location.isDoor(x, y)) {
				location.openDoor(x,y);
			}
			location.addEvent(new EventUseObject(characterId, x, y));
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
		location.makeSound(x,y,type);
	}
	/* Special actions */
	protected void push(Character character, Side side) {
	/**
	 * Pushes another character so he moves
	 */
		int[] d = side.side2d();
		int nx = character.x+d[0];
		int ny = character.y+d[1];
		if (location.passability[nx][ny] == TerrainBasics.PASSABILITY_FREE) {
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
		// This event is needed for client to correctly 
		// handle characters' new positions in Terrain.cells
		location.addEvent(new EventChangePlaces(characterId, character.characterId));
		moveTime(500);		
	}
	protected void scream() {
		makeSound(SoundType.SCREAM);
	}
	/* Vision */
	protected void notifyNeighborsVisiblilty() {
	/**
	 * This method's name sucks, but I don't know how to call it : (
	 * 
	 * Tell every nearby character about this character's new position,
	 * so nearby characters can update status of this character as
	 * seen / unseen.
	 */
		for (NonPlayerCharacter character : getNearbyNonPlayerCharacters()) {
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
		for (NonPlayerCharacter character : location.nonPlayerCharacters) {
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
	// ���������, ��������� �� ������ ������ �� ����� ���������
		if (this.isNear(x,y) || this.x==x && this.y==y) {
		// ���� ������ ����� ��� �������� �� ��� ����� - �� � ����� �����
			return true;
		}
		if (Math.floor(this.distance(x, y))>Character.VISION_RANGE) {
			return false;
		}
		// �������� ������������� ��������� ������� ��������� ������������ ��������� � ������� ������, 
		// ��������� � ������������ ����� ������� ������ ����� ���������. �������� ��� ������ ������ ��������������� ���������.
		if (x==this.x || y==this.y) {
			// ��� ������, ����� ������� ������ (������� �����������) ����� ������������� ��� 0 
			// (�.�. ����� � ����� � else ����� ���� ������� �� ����, �.�. ������� ��� �������� ����� � ������ �����)
			// � ���� ������ ������� ������� ������ ���� �������� �� ����� (�� ����� �������, ��� � else ��� ������ � tg!=0 � tg!=1)
			if (x==this.x) {
			// ��� ������������ �����
				int dy=Math.abs(y-this.y)/(y-this.y);
				for (int i=this.y+dy; i!=y; i+=dy) {
					if (location.passability[x][i] == 1) {
						return false;
					}
				}
			} else {
			// ��� �������������� �����
				int dx=Math.abs(x-this.x)/(x-this.x);
				for (int i=this.x+dx; i!=x; i+=dx) {
					if (location.passability[i][y]==1) {
						return false;
					}
				}
			}
			return true;
		} else if (Math.abs(x-this.x)==1) {
		// ��� ������, ����� ���������� ����� � ������ ��������� �� ���� �������� ������������ ������
			int yMin=Math.min(y,this.y);
			int yMax=Math.max(y,this.y);
			for (int i=yMin+1; i<yMax; i++) {
				if (location.passability[x][i]==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			for (int i=yMin+1;i<yMax;i++) {
				if (location.passability[this.x][i]==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			return false;
		} else if (Math.abs(y-this.y)==1) {
		// ��� �� ������, ��� � ����������, �� ��� �������������� �����
			int xMin=Math.min(x,this.x);
			int xMax=Math.max(x,this.x);
			for (int i=xMin+1;i<xMax;i++) {
				if (location.passability[i][y]==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			for (int i=xMin+1;i<xMax;i++) {
				if (location.passability[i][this.y]==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			return false;
		} 
		else if (Math.abs(x-this.x) == Math.abs(y-this.y)) {
		// ������, ����� ����� �������� � ����� ���� 45 �������� (abs(tg)==1)
			int dMax=Math.abs(x-this.x);
			int dx=x>this.x ? 1 : -1;
			int dy=y>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (location.passability[cx][cy]==1) {
					return false;
				}
				
			}
			return true;
		} 
		else {
		// ����� ������
			double[][] start = new double[2][2];
			double[] end = new double[4];
			// x � y ������ ������������� x � y ������ ������ ��� � ���������� ���� (�������� ������������ � ����� �� k ������ � ������)
			end[0]=(x>this.x)? x-0.5 : x+0.5;
			end[1]=(y>this.y)? y-0.5 : y+0.5;
			end[2]=x;
			end[3]=y;
			start[0][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			start[0][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			start[1][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			// start[0][1]=this.y;
			// start[1][0]=this.x;
			start[1][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			Coordinate[] rays=rays(this.x,this.y,x,y);
			jump:
			for (int k=0;k<3;k++) {
				int endNumX=(k==0 || k==1)?0:2;
				int endNumY=(k==0 || k==2)?1:3;
				for (int j=0;j<1;j++) {
				// ����� �������� ������� ��������� �������� �� ���, ���� �� �����, 
				// ������� ��������� �����, ��� �� 0.5 ������ �� ������ - ��������� ������� ����, ��� ������ ���������� ��������.
				// �������� � ���� ������ ��������� ������������ � R=0.5 
				// ��� �� ������ ������� ��������� �� ������ ������ �� �����������.
				// � ���� ������ ����� ������� �������� ����� �������� (3 ����� �� k - ����� ����� - � ��� �� j - ����� ������)
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart=start[j][0];
					double yStart=start[j][1];
					for (Coordinate c : rays) {
						try {
							if (location.passability[c.x][c.y]==1) {
							// ��������� ������ ������
								if (c.x==x && c.y==y || c.x==x && c.y==y) {
									continue;
								}
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
								// ���� ���������� �� ����� �� ������ 0.5, ��������� ��������� �� 6 �����
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
		// ���������, ��������� �� ������ ������ �� ����� ���������
		if (this.isNear(endX,endY) || this.x==endX && this.y==endY) {
		// ���� ������ ����� ��� �������� �� ��� ����� - �� � ����� �����
			return new Coordinate(endX, endY);
		}
		// �������� ������������� ��������� ������� ��������� ������������ ��������� � ������� ������, 
		// ��������� � ������������ ����� ������� ������ ����� ���������. �������� ��� ������ ������ ��������������� ���������.
		if (endX==this.x || endY==this.y) {
			// ��� ������, ����� ������� ������ (������� �����������) ����� ������������� ��� 0 
			// (�.�. ����� � ����� � else ����� ���� ������� �� ����, �.�. ������� ��� �������� ����� � ������ �����)
			// � ���� ������ ������� ������� ������ ���� �������� �� ����� (�� ����� �������, ��� � else ��� ������ � tg!=0 � tg!=1)
			if (endX==this.x) {
			// ��� ������������ �����
				int dy=Math.abs(endY-this.y)/(endY-this.y);
				for (int i=this.y+dy; i!=endY+dy; i+=dy) {
					if (location.passability[endX][i] != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(endX, i-dy);
					}
				}
			} else {
			// ��� �������������� �����
				int dx=Math.abs(endX-this.x)/(endX-this.x);
				for (int i=this.x+dx; i!=endX+dx; i+=dx) {
					if (location.passability[i][endY] != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(i-dx, endY);
					}
				}
			}
			return new Coordinate(endX, endY);
		} else if (Math.abs(endX-this.x)==1) {
		// ��� ������, ����� ���������� ����� � ������ ��������� �� ���� �������� ������������ ������
			int dy=Math.abs(endY-this.y)/(endY-this.y);
			int y1 = endY, y2 = endY;
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (location.passability[endX][i] != TerrainBasics.PASSABILITY_FREE) {
					y1 = i-dy;
					break;
				}
				if (i==endY) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (location.passability[this.x][i] != TerrainBasics.PASSABILITY_FREE) {
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
			if (answer.x == this.x && answer.y == y2 && location.passability[endX][endY] == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == this.x && answer.y == y2 && location.passability[endX][endY] == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.y = endY-dy;
			}
			return answer;
		} else if (Math.abs(endY-this.y)==1) {
		// ��� �� ������, ��� � ����������, �� ��� �������������� �����
			int dx=Math.abs(endX-this.x)/(endX-this.x);
			int x1 = endX, x2 = endX;
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (location.passability[i][endY] != TerrainBasics.PASSABILITY_FREE) {
					x1 = i-dx;
					break;
				}
				if (i==endX) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (location.passability[i][this.y] != TerrainBasics.PASSABILITY_FREE) {
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
			if (answer.x == x2 && answer.y == this.y && location.passability[endX][endY] == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == x2 && answer.y == this.y && location.passability[endX][endY] == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.x = endX-dx;
			}
			
			return answer;
		} 
		else if (Math.abs(endX-this.x) == Math.abs(endY-this.y)) {
		// ������, ����� ����� �������� � ����� ���� 45 �������� (abs(tg)==1)
			int dMax=Math.abs(endX-this.x);
			int dx=endX>this.x ? 1 : -1;
			int dy=endY>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<=dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (location.passability[cx][cy]==1) {
					return new Coordinate(cx-dx, cy-dy);
				}
				
			}
			return new Coordinate(endX, endY);
		} 
		else {
		// ����� ������
			double[][] start = new double[2][2];
			double[] end = new double[4];
			// x � y ������ ������������� x � y ������ ������ ��� � ���������� ���� (�������� ������������ � ����� �� k ������ � ������)
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
				// ����� �������� ������� ��������� �������� �� ���, ���� �� �����, 
				// ������� ��������� �����, ��� �� 0.5 ������ �� ������ - ��������� ������� ����, ��� ������ ���������� ��������.
				// �������� � ���� ������ ��������� ������������ � R=0.5 
				// ��� �� ������ ������� ��������� �� ������ ������ �� �����������.
				// � ���� ������ ����� ������� �������� ����� �������� (3 ����� �� k - ����� ����� - � ��� �� j - ����� ������)
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart = start[j][0];
					double yStart = start[j][1];
					for (Coordinate c : rays) {
						try {
							if (location.passability[c.x][c.y]==1) {
							// ��������� ������ ������
								
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
								// ���� ���������� �� ����� �� ������ 0.5, ��������� ��������� �� 6 �����
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
	// ��������������� ������� ��� this->canSee
	// ���������� ����� ��������� ������, ������� ���������� ��������� ��� �������� ���������
		return Utils.concatAll(
			location.vector(startX, startY, endX, endY),
			location.vector(startX,startY+(endY>startY ? 1 : -1),endX+(endX>startX ? -1 : 1),endY),
			location.vector(startX+(endX>startX ? 1 : -1),startY,endX,endY+(endY>startY ? -1 : 1))
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
	public Location location() {
		return location;
	}
	public int hashCode() {
		return characterId;
	}
	public int getFraction() {
		return fraction;
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
		location.passability[this.x][this.y] = TerrainBasics.PASSABILITY_FREE;
		location.cells[this.x][this.y].character(false);
		this.x = x;
		this.y = y;
		location.cells[x][y].character(this);
		location.passability[x][y] = TerrainBasics.PASSABILITY_SEE;
		location.addEvent(new EventMove(characterId, x, y));
		notifyNeighborsVisiblilty();
	}
	protected void setLocation(Location location) {
		this.location = location;
	}
	public void getDamage(int amount, int type) {
		hp -= amount;
		location.addEvent(new EventDamage(characterId, amount, type));
		if (hp <= 0) {
			die();
		}
	}
	protected void increaseHp(int value) {
		hp = (hp + value > maxHp) ? maxHp : hp + value;
	}
	protected void removeEffect(CharacterEffect effect) {
		effects.remove(effect);
	}
	public void getItem(UniqueItem item) {
		inventory.add(item);
		if (isInLocation()) {
			location.addEvent(new EventGetUniqueItem(characterId, item.getTypeId(), item.getItemId()));
		}
	}
	public void getItem(ItemPile pile) {
		inventory.add(pile);
		if (isInLocation()) {
			location.addEvent(new EventGetItemPile(characterId, pile.getTypeId(), pile.getAmount()));
		}
	}
	public void loseItem(UniqueItem item) {
		if (inventory.hasUnique(item.getItemId())) {
			inventory.removeUnique(item);
			if (isInLocation()) {
				location.addEvent(new EventLoseItem(characterId, item.getType().getTypeId(), item.getItemId()));
			}
		} else {
			throw new Error("An attempt to lose an item width id " + item.getItemId()
					+ " that is neither in inventory nor in ammunition");
		}
	}
	public void loseItem(ItemPile pile) {
		inventory.removePile(pile);
		if (isInLocation()) {
			location.addEvent(new EventLoseItem(characterId, pile.getType().getTypeId(), pile.getAmount()));
		}
	}
	public void setFraction(int fraction) {
		this.fraction = fraction;
	}
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
		actionPoints -= amount;
		for (Character.Effect e : effects.values()) {
			e.duration -= amount;
			if (e.duration < 0) {
				removeEffect(e.effectId);
			}
		}
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
		                  location.addEvent(new EventAttributeChange(characterId, Attribute.HP.attr2int(), hp));
		                  break;
		case MAX_MP:      resultValue = (maxMp += value); 
                                         hp += value; 
                          location.addEvent(new EventAttributeChange(characterId, Attribute.MP.attr2int(), mp));
                          break;
		default:
			throw new Error("Unknown attribute");
		}
		location.addEvent(new EventAttributeChange(characterId, attribute.attr2int(), resultValue));
	}
	/* Checks */
	public boolean at(int atX, int atY) {
		return x==atX && y==atY;
	}
	public boolean hasItem(int typeId, int amount) {
		return inventory.hasPile(typeId, amount);
	}
	public boolean isOnGlobalMap() {
		return location == null;
	}
	public boolean isEnemy(Character ch) {
		if (fraction == FRACTION_NEUTRAL) {
			return false;
		}
		return ch.fraction != fraction;
	}
	public boolean isInLocation() {
		return location != null;
	}
	/* Data */
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
