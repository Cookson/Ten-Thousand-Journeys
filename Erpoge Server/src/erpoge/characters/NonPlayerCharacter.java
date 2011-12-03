package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.inventory.Item;
import erpoge.inventory.ItemsTypology;
import erpoge.itemtypes.ItemType;
import erpoge.serverevents.EventDialogueEnd;
import erpoge.serverevents.EventDialoguePoint;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;

public class NonPlayerCharacter extends Character {
	public int destX;
	public int destY;
	public int[][] pathTable;
	public CharacterType characterType;
	public ArrayList<CustomCharacterAction> customActions = new ArrayList<CustomCharacterAction>();
	public HashMap<Character, DialoguePoint> dialogues = new HashMap<Character, DialoguePoint>();
	Dialogue dialogue;
	public NonPlayerCharacter(String type, String name, Location location,
			int sx, int sy) {
		super(type, name, location, sx, sy);
		pathTable = new int[location.width][location.height];
		destX = x;
		destY = y;
		this.characterType = CharacterTypes.getType(type);
	}

	public NonPlayerCharacter(boolean b) {
		super(b);
		destX = 0;
		destY = 0;
		pathTable = new int[1][1];
	}

	public void setDestNearEntity(Coordinate entity) {
		// Set character's destX and destY to the closest cell near the given
		// entity
		if (isNear(entity.x, entity.y)) {
			// Стоять на месте, если цель на соседней клетке
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
		// Иначе следовать за ним
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
	public void action() {
		boolean canSeeAnEnemy = false;
		Seer enemy = this;
		for (Seer ch : seenEntities) {
			if (this.isEnemy((Character) ch)) {
				enemy = ch;
				break;
			}
		}
		if (enemy != this) {
			if (ammunition.hasPiece(Item.SLOT_RIGHT_HAND)
					&& ammunition.getItemInSlot(Item.SLOT_RIGHT_HAND).getType().isRanged()
					&& hasItem(
							ItemsTypology.getMissileType(ammunition.getItemInSlot(0)),
							1)) {
				// If this character has ranged weapon
//				shootMissile();
				idle();
			} else if (characterType.isCaster) {
				// If this character can cast a spell
				castSpell(characterType.spells.get(0), enemy.x, enemy.y);
			} else {
				// If this character has melee weapon or is empty-handed
				if (isNear(enemy.x, enemy.y)) {
					attack((Character) enemy);
				} else {
					destX = enemy.x;
					destY = enemy.y;
					getPathTable();
					setDestNearEntity(enemy);
					if (destX == x && destY == y) {
						idle();
					} else {
						ArrayList<Coordinate> dest = getPath(destX, destY);
						if (dest.get(0).x != x || dest.get(0).y != y) {
							move(dest.get(0).x, dest.get(0).y);
						} else {
							idle();
						}
					}
				}
			}
		} else {
			idle();
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
	}
	public void move(int destX, int destY) {
		super.move(destX, destY);
	}
	public void showPathTable() {
		for (int i = 0; i < location.height; i++) {
			for (int j = 0; j < location.width; j++) {
				Main.out(pathTable[j][i]);
			}
			Main.outln();
		}
		Main.outln("------");
	}
	public int getArmor() {
		return characterType.armor;
	}
	public int getEvasion() {
		return characterType.evasion;
	}
	public int getProtection(int type) {
		return characterType.protections.get(type);
	}
	public void getPathTable() {
		// Получает таблицу путей по волновому алгоритму
		// Отключено для возможности использования объектов
		// if (vertex[this.destX][this.destY]==2) {
		// this.destX=this.x;
		// this.destY=this.y;
		// }
		// int
		// destCharacterCoordX=(this.aimCharacter!=-1)?this.aimCharacter.x:this.x;
		// int
		// destCharacterCoordY=(this.aimCharacter!=-1)?this.aimCharacter.y:this.y;
		boolean isPathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		// От какой клетки начинать отсчёт
		newFront.add(new Coordinate(x, y));
		for (int i = 0; i < location.width; i++) {
			for (int j = 0; j < location.height; j++) {
				pathTable[i][j] = 0;
			}
		}
		try {
			pathTable[x][y] = 0;
		} catch (IndexOutOfBoundsException e) {
			Main.outln(name + " " + x + " " + y);
		}
		int t = 0;
		do {

			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				// Двигает фронт на восемь доступных сторон от каждой клетки
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
					// Main.outln(location.passability[thisNumX][thisNumY]);
					if (location.passability[thisNumX][thisNumY] == 0
							&& !(thisNumX == this.x && thisNumY == this.y)) {
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
	};
	public ArrayList<Coordinate> getPath(int destinationX, int destinationY) {
		// Получить путь до клетки в виде массива координат (0 - первый шаг и т.
		// д.)
		if (destinationX == this.x && destinationY == this.y) {
			throw new Error("Getting path to itself");
		}
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		if (this.isNear(destinationX, destinationY)) {
			path.add(new Coordinate(destinationX, destinationY));
			return path;
		}
		// Нахождение пути
		int currentNumX = destinationX;
		int currentNumY = destinationY;
		int x = currentNumX;
		int y = currentNumY;
		for (int j = this.pathTable[currentNumX][currentNumY]; j > 0; j = this.pathTable[currentNumX][currentNumY]) {
			// Счётчик: от кол-ва шагов до клетки dest до начальной клетки (шаг
			// 1)
			path.add(0, new Coordinate(currentNumX, currentNumY));
			int[] adjactentX = {x, x + 1, x, x - 1, x + 1, x + 1, x - 1, x - 1};
			int[] adjactentY = {y - 1, y, y + 1, y, y + 1, y - 1, y + 1, y - 1};
			for (int i = 0; i < 8; i++) {
				// Для каждой из доступных сторон (С, Ю, З, В)
				int thisNumX = adjactentX[i];
				if (thisNumX < 0 || thisNumX >= location.width) {
					continue;
				}
				int thisNumY = adjactentY[i];
				if (thisNumY < 0 || thisNumY >= location.height) {
					continue;
				}
				if (pathTable[thisNumX][thisNumY] == j - 1) {
					// Если клетка в этой стороне является предыдущим шагом,
					// перейти на неё
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
