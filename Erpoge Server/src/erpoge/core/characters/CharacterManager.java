package erpoge.core.characters;

import java.util.HashMap;
import java.util.HashSet;

import erpoge.core.Character;
import erpoge.core.TimeStream;
import erpoge.core.net.PlayerHandler;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.HorizontalPlane;

public class CharacterManager {
	public static final HashSet<Character> characters = new HashSet<Character>();
	public static final HashMap<Integer, PlayerHandler> players = new HashMap<Integer, PlayerHandler>();
	public static PlayerHandler createPlayer(HorizontalPlane plane, int x, int y, String name, Race race, String cls) {
		PlayerHandler player = new PlayerHandler(plane, x, y, name, race, cls);
		players.put(player.characterId, player);
		player.setTimeStream(new TimeStream(player));
		plane.getCell(x, y).character(player);
		return player;
	}
	public static void createCharacter(HorizontalPlane plane, int x, int y, String type, String name) {
		characters.add(new NonPlayerCharacter(plane, x, y, type, name));
	}
	public static void getCharactersInChunk(Chunk chunk) {
		
	}
	public static PlayerHandler getPlayerById(int characterId) {
		return players.get(characterId);
	}
	public static PlayerCharacter createPlayer(String name, Race int2race,
			String cls, int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}
}
