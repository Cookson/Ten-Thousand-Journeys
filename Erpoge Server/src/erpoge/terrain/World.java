package erpoge.terrain;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import net.tootallnate.websocket.WebSocket;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.MainHandler;
import erpoge.Side;
import erpoge.characters.PlayerCharacter;
import erpoge.graphs.RectangleSystem;
import erpoge.magic.Spells;
import erpoge.serverevents.EventChatMessage;
import erpoge.serverevents.EventDamage;
import erpoge.serverevents.ServerEvent;
import erpoge.terrain.locationtypes.*;

public class World extends Location {
	public final static World ABSTRACT_WORLD = new World(1, 1, "None", "Abstract World");
	public HashMap<Coordinate, Location> locations = new HashMap<Coordinate, Location>();
	protected WorldCell cells[][];
	public final HashMap<Integer,PlayerCharacter> onlinePlayers = new HashMap<Integer, PlayerCharacter>();
	public final HashMap<Integer, PlayerCharacter> players = new HashMap<Integer, PlayerCharacter>();
	private String serialized;
	private boolean needsNewSerialization = true;
	public World(int w, int h, String type, String n) {
		super(w, h, n);
		cells = new WorldCell[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				cells[i][j] = new WorldCell();
			}
		}
		int attempts = 0;
		while (true) {
	 		try {
				tryGeneratingWorld(type);
				break;
			} catch (Error e) {
				Main.console("Failed attempt to generate world");
				if (attempts++ > 10) {
					break;
				}
			}
		}
	}
	public void tryGeneratingWorld(String type) {
		if (type.equals("Continent")) {
			new Continent(this);
		} else if (type.equals("TestWorld")) {
			new TestWorld(this);
		}
	}
	public PlayerCharacter createCharacter(String type, String name, int race,
			String cls, int x, int y) {
		PlayerCharacter ch = new PlayerCharacter(type, name, race, cls, Location.ABSTRACT_LOCATION,
				x, y);
		addPlayer(ch, x, y);
		ch.learnSpell(Spells.SPELL_FIREBALL);
		return ch;
	}
	
	public Location createLocation(int x, int y, int width, int height,
			String type, String name) {
		Location loc = new Location(width, height, type, name, this);
		TerrainBasics f;
		if (type.equals("Forest")) {
			f  = new Forest(loc);
		} else if (type.equals("Empty")) {
			f  = new Empty(loc);
		} else if (type.equals("BuildingTest")) {
			f  = new BuildingTest(loc);
		}else if (type.equals("Village")) {
			f  = new Village(loc);
		} else if (type.equals("DragonLair")) {
			f  = new DragonLair(loc);
		} 
//		else if (type.equals("SchoolOfMartialArts")) {
//			f  = new SchoolOfMartialArts(loc);
//		} else if (type.equals("Graveyard")) {
//			f  = new Graveyard(loc);
//		} 
		else if (type.equals("Rampart")) {
			f  = new Rampart(loc);
		} else {
			throw new Error("No such location type "+type);
		} 
		Coordinate locationCoord = new Coordinate(x, y);
		if (!locations.containsKey(locationCoord)) {
			locations.put(locationCoord, loc);
		}
		loc.setWorldCoordinate(x,y);
		return loc;
	}

	public Location getLocation(int x, int y) {
		return locations.get(new Coordinate(x, y));
	}

	public Location getLocation(Coordinate c) {
		if (locations.containsKey(c)) {
			return locations.get(c);
		} else {
			throw new Error("World " + name + " has no location " + c);
		}
	}

	public void addObject(int x, int y, int type) {
		cells[x][y].objects.add(type);
	}

	public void setRiver(int x, int y, int val) {
		cells[x][y].river = val;
	}

	public void setElement(int x, int y, int type, int val) {
		switch (type) {
		case ELEMENT_GROUND:
			setGround(x, y, val);
			break;
		case ELEMENT_FOREST:
			setForest(x, y, val);
			break;
		case ELEMENT_RIVER:
			setRiver(x, y, val);
			break;
		case ELEMENT_ROAD:
			setRoad(x, y, val);
			break;
		case ELEMENT_RACE:
			setRace(x, y, val);
			break;
		default:
			throw new Error("������� ������� ������� ��������������� ���� "
					+ type);
		}
	}

	public int getElement(int x, int y, int type) {
		switch (type) {
		case ELEMENT_GROUND:
			return cells[x][y].floor;
		case ELEMENT_FOREST:
			return cells[x][y].object;
		case ELEMENT_RIVER:
			return cells[x][y].river;
		case ELEMENT_ROAD:
			return cells[x][y].road;
		case ELEMENT_RACE:
			return cells[x][y].race;
		default:
			throw new Error("Not registered type " + type);
		}
	}

	private void setRace(int x, int y, int val) {
		// TODO Auto-generated method stub
		cells[x][y].race = val;
	}

	private void setRoad(int x, int y, int val) {
		// TODO Auto-generated method stub
		cells[x][y].road = val;
	}

	private void setForest(int x, int y, int val) {
		// TODO Auto-generated method stub
		cells[x][y].object = val;
	}

	protected void setGround(int x, int y, int val) {
		// TODO Auto-generated method stub
		cells[x][y].floor = val;
	}

	public void showWorld() {
		int iSize = cells.length;
		int jSize = cells[0].length;
		for (int j = 0; j < jSize; j++) {
			for (int i = 0; i < iSize; i++) {
				if (cells[i][j].road > 0) {
					Main.out("+");
				} else if (cells[i][j].object > 0) {
					Main.out("T");
				} else if (cells[i][j].river > 0) {
					Main.out("~");
				} else if (cells[i][j].objects.size() > 0) {
					Main.out("o");
				} else if (cells[i][j].floor == 7){
					Main.out("~");
				} else {
					Main.out(".");
				}
			}
			Main.outln();
		}
	}

	public void setRivers(int numOfRivers) {
		// ������� ����
		RectangleSystem graph = getGraph(0, 0, width, height, 20, 0);
		numOfRivers = graph.rectangles.size();
		for (int i = 0; i < numOfRivers; i++) {
			// dir - ����������� (1-8 �� ������� �������)
			Side dir = Side.int2side(Chance.rand(1, 8));
			int x = Chance.rand(0, width);
			int y = Chance.rand(0, height);
			Chance ch1 = new Chance(1);
			Chance ch4 = new Chance(4);
			Chance ch8 = new Chance(8);
			Chance ch50 = new Chance(50);
			while (x >= 0 && x < width && y >= 0 && y < height) {
				// �������� ����
				setRiver(x, y, 1);
				switch (dir) {
				// ��������� �����������
				case N:
					if (ch4.roll()) {
						x += (ch50.roll()) ? 1 : -1;
					} else {
						y++;
					}
					break;
				case NE:
					if (ch50.roll()) {
						x++;
					} else {
						y++;
					}
					break;
				case E:
					if (ch4.roll()) {
						y += (ch50.roll()) ? 1 : -1;
					} else {
						x++;
					}
					break;
				case SE:
					if (ch50.roll()) {
						x++;
					} else {
						y--;
					}
					break;
				case S:
					if (ch4.roll()) {
						x += (ch50.roll()) ? 1 : -1;
					} else {
						y--;
					}
					break;
				case SW:
					if (ch50.roll()) {
						x--;
					} else {
						y--;
					}
					break;
				case W:
					if (ch4.roll()) {
						y += (ch50.roll()) ? 1 : -1;
					} else {
						x--;
					}
					break;
				case NW:
					if (ch50.roll()) {
						x--;
					} else {
						y++;
					}
					break;
				}
				if (ch8.roll()) {
					// ����� ����������� � ��������� ������������
					Side newDir = dir;
					while (newDir == dir || Math.abs(Side.side2int(newDir) - Side.side2int(dir)) >= 2
							&& Math.abs(Side.side2int(newDir) - Side.side2int(dir)) <= 6) {
						newDir = Side.int2side(Chance.rand(1, 8));
					}
					dir = newDir;
				}
			}
		}
	}

	public void setRoads() {
		// ����������� ������ �� ���������� �����
		RectangleSystem graph = getGraph(0, 0, width, height, 20, 0);
		ArrayList<Integer> completedVertexes = new ArrayList<Integer>();// ������,
																		// ����
																		// �����������
																		// ������
																		// ������
																		// �����,
																		// ��
																		// �������
																		// ���������
																		// ���
																		// ������
		Set<Integer> keys = graph.edges.keySet();
		for (Integer k : keys) {
			// �������� �� ���� �������� (� ����� �������) � ��������� ������� �
			// � ��������
			Rectangle s = graph.rectangles.get(k); // ��������� �������������
			int startX = s.x + (int) Math.floor(s.width / 2);
			int startY = s.y + (int) Math.floor(s.height / 2);
			ArrayList<Integer> edge = graph.edges.get(k);
			int size = edge.size();
			for (int i = 0; i < size; i++) {
				// ���� �� ������� (������� ����� �������� ���������������)
				int link = edge.get(i);
				if (completedVertexes.contains(link)) {
					continue;
				}
				Rectangle e = graph.rectangles.get(link); // ��������
															// �������������
				int endX = e.x + (int) Math.floor(e.width / 2);
				int endY = e.y + (int) Math.floor(e.height / 2);

				// �������� ������ ������ ������ (��������� ������
				// ���������������)
				line(startX, startY, endX, endY, 6, 2);
			}
			completedVertexes.add(k);
		}
	}
	public String jsonPartGetWorldContents() {
		/*
			Format: non-valid json data; 
				String "w:xSize,h:ySize,c:[[ground,forest,road,river,race,[objects]]xN]";
		*/
		if (needsNewSerialization) {
			String answer = "\"w\":"+width+",\"h\":"+height+",\"c\":[";
			for (int j = 0;j<height;j++) {
				for (int i=0;i<width;i++) {
					WorldCell c = cells[i][j];
					answer += "["+c.floor+","+c.object+","+c.road+","+c.river+","+c.race;
					int oSize = c.objects.size();
					if (oSize > 0) {
						answer +=",[";
						for (int k=0;k<oSize;k++){
							answer+=c.objects.get(k)+((k<oSize-1)?",":"");
						}
						answer+="]";
					}
					answer+="]"+(i+j<width+height-2 ? "," : "");
				}
			}
			answer+="]";
			serialized = answer;
			needsNewSerialization = false;
			return answer;
		} else {
			// return the cached version
			return serialized;
		}
	}
	public void flushEvent(ServerEvent event) {
		// Send event to all clients
		ArrayList<WebSocket> targetConnections = new ArrayList<WebSocket>();
		
		for (WebSocket conn : MainHandler.instance.connections()) {
			if (conn.character != null && conn.character.isOnGlobalMap()) {
				targetConnections.add(conn);
			}
		}
		
		// Form the json string
		String data = "["+MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+"]";
		// Send data to all players
		for (WebSocket conn : targetConnections) {
			try {
				conn.send(data);
			} catch (IOException e) {
				Main.outln("Data sending error");
			}
		}
		Main.console("flush "+data);
	}
	public void addPlayer(PlayerCharacter player, int x, int y) {
		players.put(player.characterId, player);
		player.world = this;
		player.worldX = x;
		player.worldY = y;
	}

	public PlayerCharacter getPlayerById(int characterId) {
		return players.get(characterId);
	}	
}
