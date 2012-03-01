package erpoge.core.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.inventory.Item;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.itemtypes.ItemsTypology;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Condition;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.net.serverevents.EventDialogueEnd;
import erpoge.core.net.serverevents.EventDialoguePoint;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.TerrainBasics;

public class NonPlayerCharacter extends Character {
	private static final int PATH_TABLE_WIDTH = 41;
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
	
	public NonPlayerCharacter(HorizontalPlane plane, int x, int y, String type, String name) {
		super(plane, x, y, type, name);
		Chunk chunk = plane.getChunkWithCell(x, y);
		chunk.getCell(x, y).setPassability(TerrainBasics.PASSABILITY_SEE);
		this.chunk = chunk;
		hp = CharacterTypes.getType(type).hp;
		mp = CharacterTypes.getType(type).mp;
		ep = 100;
		maxHp = CharacterTypes.getType(type).hp;
		maxMp = CharacterTypes.getType(type).mp;
		maxEp = 100;
		pathTable = new int[PATH_TABLE_WIDTH][PATH_TABLE_WIDTH];
		destX = x;
		destY = y;
		characterType = CharacterTypes.getType(type);
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
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		if (isNear(entity.x, entity.y)) {
			// ������ �� �����, ���� ���� �� �������� ������
			destX = x;
			destY = y;
			return;
		}
		int curX = entity.x;
		int curY = entity.y;
		int[] dists = new int[]{curX-1, curY, curX+1, curY, curX, curY-1,
				curX, curY+1, curX+1, curY+1, curX-1, curY+1,
				curX+1, curY-1, curX-1, curY-1};
		int dist = Integer.MAX_VALUE;
		int curDestX = -1, curDestY = -1;
		// ����� ��������� �� ���
		for (int i=0; i<8; i++) {
			if (
				plane.getCell(dists[i*2], dists[i*2+1]).getPassability() == TerrainBasics.PASSABILITY_FREE
				&& pathTable[dists[i*2]-dX][dists[i*2+1]-dY] <= dist
				&& (curDestX == -1 || distance(dists[i*2], dists[i*2+1])<distance(curDestX, curDestY)) 
				&& pathTable[dists[i*2]-dX][dists[i*2+1]-dY] > 0
				&& !(dists[i*2] == x && dists[i*2+1] == y)
			) {
				dist = pathTable[dists[i*2]-dX][dists[i*2+1]-dY];
				curDestX = dists[i*2];
				curDestY = dists[i*2+1];
			}
		}
		if (curDestX != -1 || curDestY != -1) {
			destX = curDestX;
			destY = curDestY;
		} else {
			// showPathTable();
			destX = x;
			destY = y;
			throw new Error("Could not set dest for "+name+" at "+x+":"+y+" (dest "+destX+":"+destY+") to entity at "+entity.x+":"+entity.y);
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
			d = Side.int2side(i).side2d();
			if (plane.getCell(x+d[0], y+d[1]).getPassability() != TerrainBasics.PASSABILITY_FREE) {
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
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		return isNear(c.x, c.y) 
			|| pathTable[c.x  -dX][c.y-1-dY] != 0
			|| pathTable[c.x+1-dX][c.y-1-dY] != 0
			|| pathTable[c.x+1-dX][c.y  -dY] != 0
			|| pathTable[c.x+1-dX][c.y+1-dY] != 0
			|| pathTable[c.x  -dX][c.y+1-dY] != 0
			|| pathTable[c.x-1-dX][c.y+1-dY] != 0
			|| pathTable[c.x-1-dX][c.y  -dY] != 0
			|| pathTable[c.x-1-dX][c.y-1-dY] != 0;			
	}
	public void action() {
		getPathTableToAllSeenCharacters();
		if (hp < maxHp/2) {
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
				Main.console(name+" goes to "+destX+" "+destY+" near "+activeEnemy.x+" "+activeEnemy.y);
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
				Main.console("ELSE");
				/* */ // Maybe this part should be main, and main part should be deleted?!
				// If we always use imaginary table.
				int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
				int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
				int [][] imaginaryPathTable = getImaginaryPathTable(activeEnemy.x, activeEnemy.y);
				if (imaginaryPathTable[activeEnemy.x-dX][activeEnemy.y-dY] != 0) {
				// If path is blocked by characters
					ArrayList<Coordinate> imaginaryPath = 
							getPathOnCustomPathTable(imaginaryPathTable, activeEnemy.x, activeEnemy.y);
					Coordinate firstStep = imaginaryPath.get(0);
					if (!plane.getCell(firstStep.x,firstStep.y).hasCharacter()) {
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
				if (!plane.getCell(firstStep.x,firstStep.y).hasCharacter()) {
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
		timeStream.removeCharacter(this);
	}
	/* Pathfinding */
	public void showPathTable() {
		for (int i=0; i<PATH_TABLE_WIDTH; i++) {
			for (int j=0; j<PATH_TABLE_WIDTH; j++) {
				Main.out(pathTable[j][i]%10);
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
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		int[][] imaginaryPathTable = new int[PATH_TABLE_WIDTH][PATH_TABLE_WIDTH];
		for (int i=0; i<PATH_TABLE_WIDTH; i++) {
			for (int j=0; j<PATH_TABLE_WIDTH; j++) {
				imaginaryPathTable[i][j] = 0;
			}
		}
		imaginaryPathTable[x-dX][y-dY] = 0;
		int t = 0;
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x+1, x, x, x-1, x+1, x+1, x-1, x-1};
				int[] adjactentY = new int[]{y, y-1, y+1, y, y+1, y-1, y+1, y-1};
				for (int j=0; j<8; j++) {
					int thisNumX = adjactentX[j]-dX;
					int thisNumY = adjactentY[j]-dY;
					if (
						thisNumX<0 || thisNumX>=PATH_TABLE_WIDTH
						|| thisNumY<0 || thisNumY>=PATH_TABLE_WIDTH
						|| imaginaryPathTable[thisNumX][thisNumY]!=0
					) {
						continue;
					}
					if (adjactentX[j] == destX && adjactentY[j] == destY) {
						isPathFound = true;
					}
					// This next condition is the difference between this method and getPathTable()
					if (
						(plane.getCell(adjactentX[j], adjactentY[j]).hasCharacter() ||
						plane.getCell(adjactentX[j], adjactentY[j]).getPassability()==TerrainBasics.PASSABILITY_FREE)
						&& !(adjactentX[j]==this.x && adjactentY[j]==this.y)
					) {
						imaginaryPathTable[thisNumX][thisNumY] = t+1;
						newFront.add(new Coordinate(adjactentX[j], adjactentY[j]));
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
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		int[][] imaginaryPathTable = new int[PATH_TABLE_WIDTH][PATH_TABLE_WIDTH];
		for (int i=0; i<PATH_TABLE_WIDTH; i++) {
			for (int j=0; j<PATH_TABLE_WIDTH; j++) {
				imaginaryPathTable[i][j] = 0;
			}
		}
		imaginaryPathTable[x-dX][y-dY] = 0;
		int t = 0;
		int charactersLeft = seenCharacters.size();
		HashSet<Character> foundCharacters = new HashSet<Character>();
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x+1, x, x, x-1, x+1, x+1, x-1, x-1};
				int[] adjactentY = new int[]{y, y-1, y+1, y, y+1, y-1, y+1, y-1};
				for (int j=0; j<8; j++) {
					int thisNumX = adjactentX[j]-dX;
					int thisNumY = adjactentY[j]-dY;
					if (
						thisNumX<0 || thisNumX>=PATH_TABLE_WIDTH
						|| thisNumY<0 || thisNumY>=PATH_TABLE_WIDTH
						|| imaginaryPathTable[thisNumX][thisNumY]!=0
					) {
						continue;
					}
					if (thisNumX+dX==destX && thisNumY+dY==destY) {
						isPathFound = true;
					}
					// This next condition is the difference between this method and getPathTable()
					if (
						(plane.getCell(adjactentX[j],adjactentY[j]).hasCharacter() 
						|| plane.getCell(adjactentX[j],adjactentY[j]).getPassability()==TerrainBasics.PASSABILITY_FREE)
						&& !(adjactentX[j]==this.x && adjactentY[j]==this.y)
					) {
						imaginaryPathTable[thisNumX][thisNumY] = t+1;
						newFront.add(new Coordinate(adjactentX[j], adjactentY[j]));
					}
					Character characterInCell = plane.getCell(thisNumX+dX, thisNumY+dY).character();
					if (seenCharacters.contains(characterInCell) && !foundCharacters.contains(characterInCell)) {
						foundCharacters.add(characterInCell);
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
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		for (int i = 0; i<PATH_TABLE_WIDTH; i++) {
			for (int j = 0; j<PATH_TABLE_WIDTH; j++) {
				pathTable[i][j] = 0;
			}
		}
		pathTable[x-dX][y-dY] = 0;
		int t = 0;
		int charactersLeft = seenCharacters.size();
		HashSet<Character> foundCharacters = new HashSet<Character>();
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x+1, x, x, x-1, x+1, x+1, x-1, x-1};
				int[] adjactentY = new int[]{y, y-1, y+1, y, y+1, y-1, y+1, y-1};
				for (int j = 0; j < 8; j++) {
					int thisNumX = adjactentX[j]-dX;
					int thisNumY = adjactentY[j]-dY;
					if (
						thisNumX<0 || thisNumX>=PATH_TABLE_WIDTH
						|| thisNumY < 0 || thisNumY>=PATH_TABLE_WIDTH
						|| pathTable[thisNumX][thisNumY]!=0
						|| (thisNumX+dX == this.x && thisNumY+dY == this.y)
					) {
						continue;
					}
					Cell cell = plane.getCell(thisNumX+dX, thisNumY+dY);
					if (
						(cell.getPassability()==TerrainBasics.PASSABILITY_FREE
						|| !initialCanSee(thisNumX+dX, thisNumY+dY)
						&& cell.getPassability()!=TerrainBasics.PASSABILITY_NO)
					) {
					// Step to cell if character can see it and it is free
					// or character cannot see it and it is not PASSABILITY_NO
						pathTable[thisNumX][thisNumY] = t+1;
						newFront.add(new Coordinate(adjactentX[j], adjactentY[j]));
					} else {
						Character characterInCell = cell.character();
						if (seenCharacters.contains(characterInCell) && !foundCharacters.contains(characterInCell)) {
							foundCharacters.add(characterInCell);
							charactersLeft--;
						}
					}
				}
			}
			t++;
		} while (charactersLeft > 0 && newFront.size() > 0 && t < 25);
		return true;
	}
	public boolean getPathTable() {
		int dX = this.x-(PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(PATH_TABLE_WIDTH-1)/2;
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(new Coordinate(x, y));
		for (int i=0; i<PATH_TABLE_WIDTH; i++) {
			for (int j=0; j<PATH_TABLE_WIDTH; j++) {
				pathTable[i][j] = 0;
			}
		}
		pathTable[x-dX][y-dY] = 0;
		int t = 0;
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x+1, x, x, x-1, x+1, x+1, x-1, x-1};
				int[] adjactentY = new int[]{y, y-1, y+1, y, y+1, y-1, y+1, y-1};
				for (int j=0; j<8; j++) {
					int thisNumX = adjactentX[j]-dX;
					int thisNumY = adjactentY[j]-dY;
					if (thisNumX<0 || thisNumX>=PATH_TABLE_WIDTH
						|| thisNumY<0 || thisNumY>=PATH_TABLE_WIDTH
						|| pathTable[thisNumX][thisNumY]!=0
					) {
						continue;
					}
					if (thisNumX+dX == this.destX && thisNumY+dY == this.destY) {
						isPathFound = true;
					}
					if (
						(plane.getCell(thisNumX+dX, thisNumY+dY).getPassability() == TerrainBasics.PASSABILITY_FREE
						|| !initialCanSee(thisNumX+dX, thisNumY+dY)
						&& plane.getCell(thisNumX+dX, thisNumY+dY).getPassability() != TerrainBasics.PASSABILITY_NO)
						&& !(thisNumX+dX == this.x && thisNumY+dY == this.y)
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
 		return getPathOnCustomPathTable(pathTable, destinationX, destinationY);
	}
 	public ArrayList<Coordinate> getPathOnCustomPathTable(int[][] customPathTable, int destinationX, int destinationY) {
		if (destinationX == this.x && destinationY == this.y) {
			throw new Error("Getting path to itself");
		}
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		if (this.isNear(destinationX, destinationY)) {
			path.add(new Coordinate(destinationX, destinationY));
			return path;
		}
		int currentNumX = destinationX;
		int currentNumY = destinationY;
		int x = currentNumX;
		int y = currentNumY;
		int dX = this.x-(this.PATH_TABLE_WIDTH-1)/2;
		int dY = this.y-(this.PATH_TABLE_WIDTH-1)/2;
		for (int j=customPathTable[currentNumX-dX][currentNumY-dY]; j>0; j=customPathTable[currentNumX-dX][currentNumY-dY]) {
			path.add(0, new Coordinate(currentNumX, currentNumY));
			int[] adjactentX = {x, x+1, x, x-1, x+1, x+1, x-1, x-1};
			int[] adjactentY = {y-1, y, y+1, y, y+1, y-1, y+1, y-1};
			for (int i=0; i<8; i++) {
				int thisNumX = adjactentX[i]-dX;
				if (thisNumX<0 || thisNumX>=PATH_TABLE_WIDTH) {
					continue;
				}
				int thisNumY = adjactentY[i]-dY;
				if (thisNumY<0 || thisNumY>=PATH_TABLE_WIDTH) {
					continue;
				}
				if (customPathTable[thisNumX][thisNumY] == j-1) {
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
		for (Character character : getTimeStream().characters) {
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
			getTimeStream().addEvent(new EventDialogueEnd(player.characterId));
		} else {
		// Continue dialogue
			getTimeStream().addEvent(new EventDialoguePoint(characterId,
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
		getTimeStream().addEvent(new EventDialoguePoint(characterId,
				player.characterId, startDP.message, startDP.getAnswers()
						.toArray(new String[0])));
	}
}
