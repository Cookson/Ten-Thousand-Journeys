package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.Chance;
import erpoge.Condition;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Side;
import erpoge.inventory.Item;
import erpoge.itemtypes.ItemType;
import erpoge.itemtypes.ItemsTypology;
import erpoge.serverevents.EventDialogueEnd;
import erpoge.serverevents.EventDialoguePoint;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;

public class NonPlayerCharacter extends Character {
	private int destX;
	private int destY;
	private Character activeEnemy; // Enemy in plain sight
	private Character enemyToChase;
	private HashSet<Character> unseenEnemies = new HashSet<Character>();
	private ArrayList<Item> seenItems = new ArrayList<Item>();
	protected HashSet<Character> seenCharacters = new HashSet<Character>();
	private final HashMap<Character, Coordinate> lastSeenEnemyCoord = new HashMap<Character, Coordinate>();
	
	private int[][] pathTable;
	public final CharacterType characterType;
	public ArrayList<CustomCharacterAction> customActions = new ArrayList<CustomCharacterAction>();
	public HashMap<Character, DialoguePoint> dialogues = new HashMap<Character, DialoguePoint>();
	private Dialogue dialogue;
	
	public NonPlayerCharacter(String type, String name, Location location,
			int sx, int sy) {
		super(type, name, sx, sy);
		setLocation(location);
		location.passability[x][y] = TerrainBasics.PASSABILITY_SEE;
		hp = CharacterTypes.getType(type).hp;
		mp = CharacterTypes.getType(type).mp;
		ep = 100;
		maxHp = CharacterTypes.getType(type).hp;
		maxMp = CharacterTypes.getType(type).mp;
		maxEp = 100;
		pathTable = new int[location.width][location.height];
		destX = x;
		destY = y;
		characterType = CharacterTypes.getType(type);
		notifyNeighborsVisiblilty();
		getVisibleEntities();
	}
	/* Observations */
	public void updateObservation(Character character, int x, int y) {
		Coordinate c = lastSeenEnemyCoord.get(character);
		c.x = x;
		c.y = y;
	}
	public void discoverDeath(Character character) {
	/** 
	 * Removes particular craracter from this character's aims private data,
	 * when that particular character is dead
	 */
		if (activeEnemy == character) {
			activeEnemy = null;
		}
		seenCharacters.remove(character);
	}
	/* NPC behaviour functions */
	private void setDestNearEntity(Coordinate entity) {
		// Set character's destX and destY to the closest cell near the given
		// entity
		if (isNear(entity.x, entity.y)) {
			// ������ �� �����, ���� ���� �� �������� ������
			destX = x;
			destY = y;
			return;
		}
		int curX = entity.x;
		int curY = entity.y;
		int[] dists = new int[]{curX - 1, curY, curX + 1, curY, curX, curY - 1,
				curX, curY + 1, curX + 1, curY + 1, curX - 1, curY + 1,
				curX + 1, curY - 1, curX - 1, curY - 1};
		int dist = Integer.MAX_VALUE;
		int curDestX = -1, curDestY = -1;
		// ����� ��������� �� ���
		for (int i = 0; i < 8; i++) {
			curX = dists[i * 2];
			curY = dists[i * 2 + 1];
			if (curX < 0 || curX >= location.width || curY < 0
					|| curY >= location.height) {
				continue;
			}
			if (location.passability[curX][curY] == 0
					&& pathTable[curX][curY] <= dist
					&& (curDestX == -1 || distance(curX, curY) < distance(
							curDestX, curDestY)) && pathTable[curX][curY] > 0
					&& !(curX == x && curY == y)) {
				dist = pathTable[curX][curY];
				curDestX = curX;
				curDestY = curY;
			}
		}
		if (curDestX != -1 || curDestY != -1) {
			destX = curDestX;
			destY = curDestY;
		} else {
			// showPathTable();
			// throw new
			// Error("Could not set dest for "+name+" at "+x+":"+y+" (dest "+destX+":"+destY+") to entity at "+entity.x+":"+entity.y);
			destX = x;
			destY = y;
		}
	}
	private boolean getEnemy() {
	/** 
	 * Searches for enemy in this.seenCharacters and
	 * puts him to this.activeEnemy
	 * 
	 * First priority are enemies who this character 
	 * can get to (not blocked by other characters or objects)
	 * .
	 * If none of such enemies found, then this.activeEnemy
	 * sets to a character who is visible, but not accessible.
	 * 
	 * If no characters found at all, then this.activeEnemy is 
	 * set to null.
	 */
		activeEnemy = null;
		/* If character had activeEnemy, but after getEnemy
		 * he hasn't found any, then activeEnemy will remain null.
		 */
		double distanceToClosestCharacter = 9999999;
		Character unreachableEnemy = null;
		for (Character ch : seenCharacters) {
			if (isEnemy(ch)) {
				double distanceToAnotherCharacter = distance(lastSeenEnemyCoord.get(ch));
				if (canComeTo(ch) && (activeEnemy == null || distanceToAnotherCharacter < distanceToClosestCharacter)) {
					activeEnemy = ch;
					distanceToClosestCharacter = distanceToAnotherCharacter;
				}
				if (activeEnemy == null && (unreachableEnemy == null || distance(ch) < distance(unreachableEnemy))) {
					unreachableEnemy = ch;
				}
			}
		}
		if (activeEnemy == null && unreachableEnemy != null) {
			activeEnemy = unreachableEnemy;
		}
		return activeEnemy != null;
	}
	private boolean getEnemyToChase() {
	/**
	 * Sets one of unseen enemies to enemyToChase
	 */
		enemyToChase = null;
		/* If character had enemyToChase, but after getEnemyToChase
		 * he hasn't found any, then enemyToChase will remain null.
		 */		
		double distanceToClosestCharacter = 9999999;
		for (Character ch : unseenEnemies) {
		// Get closest unseen character position
			double distanceToAnotherCharacter = distance(lastSeenEnemyCoord.get(ch));
			if (enemyToChase == null || distanceToAnotherCharacter < distanceToClosestCharacter) {
				enemyToChase = ch;
				distanceToClosestCharacter = distanceToAnotherCharacter;
			}
		}
		return enemyToChase != null;
	}
	private Coordinate getRetreatCoord() {
	/**
	 * Looks at enemies in line of sight and decides 
	 * on which cell should this character step to retreat
	 */
		/* Each side corresponds to the index in sides[],
		 * 0-7 clockwise from 12 o'clock. If enemy is from that side,
		 * then number at corresponding index ("threat number") increases. Character
		 * will retreat to the side with the least threat number.
		 */
		int[][] pTable = getImaginaryPathTableToAllSeenCharacters();
		int sides[] = new int[] {0,0,0,0,0,0,0,0};
		for (Character ch : seenCharacters) {
			int dx = ch.x-x;
			int dy = ch.y-y;
			double dMax;
			if (Math.max(Math.abs(dx), Math.abs(dy)) == Math.abs(dx)) {
				dMax = (double)Math.abs(dx);
			} else {
				dMax = (double)Math.abs(dy);
			}
			
			// dx2 and dy2 may be -1, 0 or 1
			int dx2 = (int)Math.round((double)dx / dMax);
			int dy2 = (int)Math.round((double)dy / dMax);
			// side is side from which current enemy is
			Side side = Side.d2side(dx2, dy2);
			Main.console("enemy from "+side);
			Side sideR1 = side.ordinalClockwise();
			Side sideR2 = sideR1.ordinalClockwise();
			Side sideR3 = sideR2.ordinalClockwise();
			Side sideL1 = side.ordinalCounterClockwise();
			Side sideL2 = sideL1.ordinalCounterClockwise();
			Side sideL3 = sideL2.ordinalCounterClockwise();
			// Increase threat from all the sides except of opposite side
			sides[side.side2int()]   = 4;
			sides[sideL1.side2int()] = 3;
			sides[sideR1.side2int()] = 3;
			sides[sideL2.side2int()] = 2;
			sides[sideR2.side2int()] = 2;
			sides[sideL3.side2int()] = 1;
			sides[sideR3.side2int()] = 1;
		}
		// Find index with minumum value and go to that side
		int min = Integer.MAX_VALUE;
		int indexMin = -1;
		int[] d;
		for (int i=0; i<8; i++) {
			Main.console(Side.int2side(i)+" - "+sides[i]);
			d = Side.int2side(i).side2d();
			if (location.passability[x+d[0]][y+d[1]] != TerrainBasics.PASSABILITY_FREE) {
				continue;
			}
			if (sides[i] < min) {
				min = sides[i];
				indexMin = i;
			}
		}
		if (indexMin == -1) {
			throw new Error("Could not find least dangerous side to retreat");
		}
		d = Side.int2side(indexMin).side2d();		
		return new Coordinate(x+d[0], y+d[1]);
	}
	private boolean canShoot() {
	/**
	 * Checks if this character is able to shoot an arrow
	 * or other missile.
	 */
		return equipment.hasPiece(Item.SLOT_RIGHT_HAND)
			&& equipment.getItemInSlot(Item.SLOT_RIGHT_HAND).getType().isRanged()
			&& hasItem(ItemsTypology.getMissileType(equipment.getItemInSlot(0)),1);
	}
	private boolean canCast() {
		return characterType.isCaster;
	}
	private boolean canComeTo(Coordinate c) {
		return isNear(c.x, c.y) 
			|| pathTable[c.x  ][c.y-1] != 0
			|| pathTable[c.x+1][c.y-1] != 0
			|| pathTable[c.x+1][c.y  ] != 0
			|| pathTable[c.x+1][c.y+1] != 0
			|| pathTable[c.x  ][c.y+1] != 0
			|| pathTable[c.x-1][c.y+1] != 0
			|| pathTable[c.x-1][c.y  ] != 0
			|| pathTable[c.x-1][c.y-1] != 0;			
	}
	public void action() {
		getPathTableToAllSeenCharacters();
		if (hp < maxHp / 2) {
		// If hp is too low, then retreat
			Coordinate retreatCoord = getRetreatCoord();
			step(retreatCoord.x, retreatCoord.y);
		} else if (getEnemy()) {
			if (canShoot()) {
				// shootMissile();
				idle();
			} else if (canCast()) {
				castSpell(characterType.spells.get(0), activeEnemy.x, activeEnemy.y);
			} else if (isNear(activeEnemy.x, activeEnemy.y)) {
//				changePlaces(activeEnemy);
//				push(activeEnemy, Side.d2side(activeEnemy.x-x, activeEnemy.y-y));				
				attack(activeEnemy);
			} else if (canComeTo(activeEnemy)) {
				// Get next cell and move
				destX = activeEnemy.x;
				destY = activeEnemy.y;
				setDestNearEntity(activeEnemy);
				if (destX == x && destY == y) {
					idle();
				} else {
					ArrayList<Coordinate> dest = getPath(destX, destY);
					if (dest.get(0).x != x || dest.get(0).y != y) {
						step(dest.get(0).x, dest.get(0).y);
					} else {
						idle();
					}
				}
			} else {
			// If sees enemy, but path to him is blocked
				/* */ // Maybe this part should be main, and main part should be deleted?!
				// If we always use imaginary table.
				int [][] imaginaryPathTable = getImaginaryPathTable(activeEnemy.x, activeEnemy.y);
				if (imaginaryPathTable[activeEnemy.x][activeEnemy.y] != 0) {
				// If path is blocked by characters
					ArrayList<Coordinate> imaginaryPath = 
							getPathOnCustomPathTable(imaginaryPathTable, activeEnemy.x, activeEnemy.y);
					Coordinate firstStep = imaginaryPath.get(0);
					if (!location.cells[firstStep.x][firstStep.y].hasCharacter()) {
					// If there is no character on first cell of imaginary path, then step there
						step(firstStep.x, firstStep.y);
					} else {
						idle();
					}
				} else {
					idle();
				}
			}
		} else if (getEnemyToChase()) {
			Coordinate lastSeenCoord = lastSeenEnemyCoord.get(enemyToChase);
			int [][] imaginaryPathTable = getImaginaryPathTable(lastSeenCoord.x, lastSeenCoord.y);
			if (imaginaryPathTable[lastSeenCoord.x][lastSeenCoord.y] != 0) {
			// If path is blocked by characters
				ArrayList<Coordinate> imaginaryPath = 
						getPathOnCustomPathTable(imaginaryPathTable, lastSeenCoord.x, lastSeenCoord.y);
				Coordinate firstStep = imaginaryPath.get(0);
				if (!location.cells[firstStep.x][firstStep.y].hasCharacter()) {
				// If there is no character on first cell of imaginary path, then step there
					step(firstStep.x, firstStep.y);
				} else {
					idle();
				}
			} else {
				idle();
			}
//			// Get next cell and move
//			destX = lastSeenCoord.x;
//			destY = lastSeenCoord.y;
//			ArrayList<Coordinate> dest = getPath(destX, destY);
//			if (!dest.isEmpty() && (dest.get(0).x != x || dest.get(0).y != y)) {
//				move(dest.get(0).x, dest.get(0).y);
//			} else {
//				idle();
//			}
//			
			if (lastSeenCoord != null && x == lastSeenCoord.x && y == lastSeenCoord.y) {
			// If character reached the point where he saw his enemy last time
				lastSeenEnemyCoord.remove(enemyToChase);
				unseenEnemies.remove(enemyToChase);
				getEnemyToChase();
			}
		} else {
			idle();
		}
		// Wandering
		// destX = x + (Chance.roll(50) ? 1 : -1);
		// destY = y + (Chance.roll(50) ? 1 : -1);
		// while (true) {
		// if (
		// destX>=location.width || destX<0 || destY>=location.height ||
		// destY<0 ||
		// location.cells[destX][destY].object()!=0 ||
		// location.cells[destX][destY].character()!=null
		// ) {
		// destX = x + (Chance.roll(50) ? 1 : -1);
		// destY = y + (Chance.roll(50) ? 1 : -1);
		// } else {
		// break;
		// }
		// }
		// getPathTable();
		// move(destX, destY);
	}
	/* Getters */
	public int getProtection(int type) {
		return characterType.protections.get(type);
	}
	public String toString() {
		return type+" "+name;
	}
	/* Overriden methods */
	public void move(int x, int y) {
		super.move(x, y);
		getVisibleEntities();
	}
	public void die() {
		super.die();
		location.nonPlayerCharacters.remove(this);
	}
	/* Pathfinding */
	public void showPathTable() {
		for (int i = 0; i < location.height; i++) {
			for (int j = 0; j < location.width; j++) {
				Main.out(pathTable[j][i]);
			}
			Main.outln();
		}
		Main.outln("------");
	}
	public int[][] getImaginaryPathTable(int destX, int destY) {
	/**
	 * Gets path table using special rules.
	 * There is almost the same code as in getPathTable
	 */
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		int[][] imaginaryPathTable = new int[location.width][location.height];
		for (int i = 0; i < location.width; i++) {
			for (int j = 0; j < location.height; j++) {
				imaginaryPathTable[i][j] = 0;
			}
		}
		imaginaryPathTable[x][y] = 0;
		int t = 0;
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x + 1, x, x, x - 1, x + 1, x + 1,
						x - 1, x - 1};
				int[] adjactentY = new int[]{y, y - 1, y + 1, y, y + 1, y - 1,
						y + 1, y - 1};
				for (int j = 0; j < 8; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (thisNumX < 0 || thisNumX >= location.width
							|| thisNumY < 0 || thisNumY >= location.height
							|| imaginaryPathTable[thisNumX][thisNumY] != 0) {
						continue;
					}
					if (thisNumX == destX && thisNumY == destY) {
						isPathFound = true;
					}
					// This next condition is the difference between this method and getPathTable()
					if (
						(location.cells[thisNumX][thisNumY].hasCharacter() ||
						location.passability[thisNumX][thisNumY] == TerrainBasics.PASSABILITY_FREE)
						&& !(thisNumX == this.x && thisNumY == this.y)
					) {
						imaginaryPathTable[thisNumX][thisNumY] = t + 1;
						newFront.add(new Coordinate(thisNumX, thisNumY));
					}
				}
			}
			t++;
			// if (t>25) {
			// throw new Error("long get path table cycle");
			// }
		} while (newFront.size() > 0 && !isPathFound && t < 25);
		return imaginaryPathTable;
	}
	public int[][] getImaginaryPathTableToAllSeenCharacters() {
	/**
	 * Gets path table using special rules.
	 * There is almost the same code as in getPathTable
	 */
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		int[][] imaginaryPathTable = new int[location.width][location.height];
		for (int i = 0; i < location.width; i++) {
			for (int j = 0; j < location.height; j++) {
				imaginaryPathTable[i][j] = 0;
			}
		}
		imaginaryPathTable[x][y] = 0;
		int t = 0;
		int charactersLeft = seenCharacters.size();
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x + 1, x, x, x - 1, x + 1, x + 1,
						x - 1, x - 1};
				int[] adjactentY = new int[]{y, y - 1, y + 1, y, y + 1, y - 1,
						y + 1, y - 1};
				for (int j = 0; j < 8; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (thisNumX < 0 || thisNumX >= location.width
							|| thisNumY < 0 || thisNumY >= location.height
							|| imaginaryPathTable[thisNumX][thisNumY] != 0) {
						continue;
					}
					if (thisNumX == destX && thisNumY == destY) {
						isPathFound = true;
					}
					// This next condition is the difference between this method and getPathTable()
					if (
						(location.cells[thisNumX][thisNumY].hasCharacter() ||
						location.passability[thisNumX][thisNumY] == TerrainBasics.PASSABILITY_FREE)
						&& !(thisNumX == this.x && thisNumY == this.y)
					) {
						imaginaryPathTable[thisNumX][thisNumY] = t + 1;
						newFront.add(new Coordinate(thisNumX, thisNumY));
					}
					if (seenCharacters.contains(location.cells[thisNumX][thisNumY].character())) {
						charactersLeft--;
					}
				}
			}
			t++;
			// if (t>25) {
			// throw new Error("long get path table cycle");
			// }
		} while (charactersLeft > 0 && newFront.size() > 0 && !isPathFound && t < 25);
		return imaginaryPathTable;
	}
	public boolean getPathTableToAllSeenCharacters() {
	/**
	 * Builds pathTable until paths to all seenCharacters are found
	 * or waves limit is exceeded.
	 */
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		for (int i = 0; i < location.width; i++) {
			for (int j = 0; j < location.height; j++) {
				pathTable[i][j] = 0;
			}
		}
		pathTable[x][y] = 0;
		int t = 0;
		int charactersLeft = seenCharacters.size();
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x + 1, x, x, x - 1, x + 1, x + 1,
						x - 1, x - 1};
				int[] adjactentY = new int[]{y, y - 1, y + 1, y, y + 1, y - 1,
						y + 1, y - 1};
				for (int j = 0; j < 8; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (thisNumX < 0 || thisNumX >= location.width
							|| thisNumY < 0 || thisNumY >= location.height
							|| pathTable[thisNumX][thisNumY] != 0) {
						continue;
					}
					if (
						(location.passability[thisNumX][thisNumY] == TerrainBasics.PASSABILITY_FREE
						|| !initialCanSee(thisNumX, thisNumY)
						&& location.passability[thisNumX][thisNumY] != TerrainBasics.PASSABILITY_NO)
							&& !(thisNumX == this.x && thisNumY == this.y)
					) {
					// Step to cell if character can see it and it is free
					// or character cannot see it and it is not PASSABILITY_NO
						pathTable[thisNumX][thisNumY] = t + 1;
						newFront.add(new Coordinate(thisNumX, thisNumY));
					} else if (seenCharacters.contains(location.cells[thisNumX][thisNumY].character())) {
						charactersLeft--;
					}
				}
			}
			t++;
			// if (t>25) {
			// throw new Error("long get path table cycle");
			// }
		} while (charactersLeft > 0 && newFront.size() > 0 && t < 25);
		return true;
	}
	public boolean getPathTable() {
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		for (int i = 0; i < location.width; i++) {
			for (int j = 0; j < location.height; j++) {
				pathTable[i][j] = 0;
			}
		}
		pathTable[x][y] = 0;
		int t = 0;
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x + 1, x, x, x - 1, x + 1, x + 1,
						x - 1, x - 1};
				int[] adjactentY = new int[]{y, y - 1, y + 1, y, y + 1, y - 1,
						y + 1, y - 1};
				for (int j = 0; j < 8; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (thisNumX < 0 || thisNumX >= location.width
							|| thisNumY < 0 || thisNumY >= location.height
							|| pathTable[thisNumX][thisNumY] != 0) {
						continue;
					}
					if (thisNumX == this.destX && thisNumY == this.destY) {
						isPathFound = true;
					}
					if (
						(location.passability[thisNumX][thisNumY] == TerrainBasics.PASSABILITY_FREE
						|| !initialCanSee(thisNumX, thisNumY)
						&& location.passability[thisNumX][thisNumY] != TerrainBasics.PASSABILITY_NO)
							&& !(thisNumX == this.x && thisNumY == this.y)
					) {
					// Step to cell if character can see it and it is free
					// or character cannot see it and it is not PASSABILITY_NO
						pathTable[thisNumX][thisNumY] = t + 1;
						newFront.add(new Coordinate(thisNumX, thisNumY));
					}
				}
			}
			t++;
			// if (t>25) {
			// throw new Error("long get path table cycle");
			// }
		} while (newFront.size() > 0 && !isPathFound && t < 25);
		return true;
	}
 	public ArrayList<Coordinate> getPath(int destinationX, int destinationY) {
		// �������� ���� �� ������ � ���� ������� ��������� (0 - ������ ��� � �.
		// �.)
		if (destinationX == this.x && destinationY == this.y) {
			throw new Error("Getting path to itself");
		}
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		if (this.isNear(destinationX, destinationY)) {
			path.add(new Coordinate(destinationX, destinationY));
			return path;
		}
		// ���������� ����
		int currentNumX = destinationX;
		int currentNumY = destinationY;
		int x = currentNumX;
		int y = currentNumY;
		for (int j = this.pathTable[currentNumX][currentNumY]; j > 0; j = this.pathTable[currentNumX][currentNumY]) {
			// �������: �� ���-�� ����� �� ������ dest �� ��������� ������ (���
			// 1)
			path.add(0, new Coordinate(currentNumX, currentNumY));
			int[] adjactentX = {x, x + 1, x, x - 1, x + 1, x + 1, x - 1, x - 1};
			int[] adjactentY = {y - 1, y, y + 1, y, y + 1, y - 1, y + 1, y - 1};
			for (int i = 0; i < 8; i++) {
				// ��� ������ �� ��������� ������ (�, �, �, �)
				int thisNumX = adjactentX[i];
				if (thisNumX < 0 || thisNumX >= location.width) {
					continue;
				}
				int thisNumY = adjactentY[i];
				if (thisNumY < 0 || thisNumY >= location.height) {
					continue;
				}
				if (pathTable[thisNumX][thisNumY] == j - 1) {
					// ���� ������ � ���� ������� �������� ���������� �����,
					// ������� �� ��
					currentNumX = adjactentX[i];
					currentNumY = adjactentY[i];
					x = currentNumX;
					y = currentNumY;
					break;
				}
			}
		}
		return path;
	}
 	public ArrayList<Coordinate> getPathOnCustomPathTable(int[][] customPathTable, int destinationX, int destinationY) {
		// �������� ���� �� ������ � ���� ������� ��������� (0 - ������ ��� � �.
		// �.)
		if (destinationX == this.x && destinationY == this.y) {
			throw new Error("Getting path to itself");
		}
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		if (this.isNear(destinationX, destinationY)) {
			path.add(new Coordinate(destinationX, destinationY));
			return path;
		}
		// ���������� ����
		int currentNumX = destinationX;
		int currentNumY = destinationY;
		int x = currentNumX;
		int y = currentNumY;
		for (int j = customPathTable[currentNumX][currentNumY]; j > 0; j = customPathTable[currentNumX][currentNumY]) {
			// �������: �� ���-�� ����� �� ������ dest �� ��������� ������ (���
			// 1)
			path.add(0, new Coordinate(currentNumX, currentNumY));
			int[] adjactentX = {x, x + 1, x, x - 1, x + 1, x + 1, x - 1, x - 1};
			int[] adjactentY = {y - 1, y, y + 1, y, y + 1, y - 1, y + 1, y - 1};
			for (int i = 0; i < 8; i++) {
				// ��� ������ �� ��������� ������ (�, �, �, �)
				int thisNumX = adjactentX[i];
				if (thisNumX < 0 || thisNumX >= location.width) {
					continue;
				}
				int thisNumY = adjactentY[i];
				if (thisNumY < 0 || thisNumY >= location.height) {
					continue;
				}
				if (customPathTable[thisNumX][thisNumY] == j - 1) {
					// ���� ������ � ���� ������� �������� ���������� �����,
					// ������� �� ��
					currentNumX = adjactentX[i];
					currentNumY = adjactentY[i];
					x = currentNumX;
					y = currentNumY;
					break;
				}
			}
		}
		return path;
	}
 	/* Dialogues */
	/* Visibility */
 	public boolean canSee(Coordinate entity) {
	/**
	 * Checks if this Seer can see entitye or not.
	 * This method does not do any computing, it just returns the
	 * result of previous computings.
	 */
		return seenCharacters.contains(entity);
	}
 	public void tryToSee(Character character) {
 		if (initialCanSee(character.x, character.y)) {
 			seenCharacters.add(character);
 			character.observers.add(this);
 			if (unseenEnemies.contains(character)) {
 				unseenEnemies.remove(character);
 			}
 			if (!lastSeenEnemyCoord.containsKey(character)) {
 				lastSeenEnemyCoord.put(character, new Coordinate(character));
 			}
 		}
 	}
 	public void tryToUnsee(Character character) {
 		if (!initialCanSee(character.x, character.y)) {
			seenCharacters.remove(character);
			character.observers.remove(this);
			if (isEnemy(character)) {
				unseenEnemies.add(character);
			}
 		}
	}
 	public void getVisibleEntities() {
 	/**
 	 * Tries to see/unsee all characters whithin vision range
 	 */
		for (Character character : location.characters.values()) {
		// Quickly select characters that could be seen (including this character)
			if (
				Math.abs(character.x - x) <= Character.VISION_RANGE &&
				Math.abs(character.y - y) <= Character.VISION_RANGE
			) {
				tryToSee(character);
			}
		}
		HashSet<Character> seen = new HashSet<Character>(seenCharacters);
		for (Character character : seen) {
			tryToUnsee(character);
		}
		seenCharacters.remove(this);
//		Main.console(name+" gets "+seenCharacters.size()+" visible entities: "+seenCharacters);
	}
 	/* Dialogues */
 	public boolean hasDialogue() {
		return dialogue != null;
	}
	public void setDialogue(Dialogue dialogue) {
		this.dialogue = dialogue;
	}	
	public void proceedToNextDialoguePoint(PlayerCharacter player,
			int answerIndex) {
		DialoguePoint prevDP = dialogues.get(player);
		dialogues.put(player,
				prevDP.getNextPoint(answerIndex, player));
		DialoguePoint curDP = dialogues.get(player);
		if (curDP.action != null) {
			curDP.action.perform(this, player);
		}
		if (prevDP.isAnswerEnding(answerIndex)) {
		// End dialogue
			location.addEvent(new EventDialogueEnd(player.characterId));
		} else {
		// Continue dialogue
			location.addEvent(new EventDialoguePoint(characterId,
					player.characterId, curDP.message, curDP.getAnswers()
							.toArray(new String[0])));
		}
	}
	public void applyConversationStarting(PlayerCharacter player) {
		DialoguePoint startDP;
		if (dialogues.containsKey(player)) {
			startDP = dialogues.get(player);
		} else {
			startDP = dialogue.root;
		}

		dialogues.put(player, startDP);
		location.addEvent(new EventDialoguePoint(characterId,
				player.characterId, startDP.message, startDP.getAnswers()
						.toArray(new String[0])));
	}
}
