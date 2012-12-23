package erpoge.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * <p>
 * TimeStream is an instance in which a group of characters acts separated from
 * the rest of the world. This is the key class for in-game turn-based time
 * model: it makes different groups of {@link PlayerCharacter}s around the world
 * act independent. A TimeStream is much like a level in traditional
 * rogue-likes, but it is not a static area as it moves along with
 * PlayerCharacters that form this TimeStream.
 * </p>
 * 
 * <p>
 * A group of PlayerCharacters traveling together forms a TimeStream around
 * them. NonPlayerCharacters close to this group get into this TimeStream,
 * thereby starting acting synchronously with this group of PlayerCharacters. As
 * a group of PlayeCharacters moves, TimeStream moves too, releasing faraway
 * Chunks and adding new Chunks reached by the group. NonPlayerCharacters in the
 * Chunks released by TimeStream also leave the TimeStream, thereby stopping
 * acting until they again are loaded by another (or the same) TimeStream.
 * </p>
 * 
 * <p>
 * Shortly, this class does the following:
 * </p>
 * <ol>
 * <li>Allocates a piece of territory in which time flows synchronously;</li>
 * <li>Determines the order of turns of characters inside this territory;</li>
 * <li>Sends data about what happens to clients if their characters are in this
 * TimeStream;</li>
 * <li>Controls some aspects of {@link NonPlayerCharacter}s' behavior, like how
 * they know about interesting entities around them.</li>
 * </ol>
 * 
 */
public class TimeStream {
	/** How far from character should terrain be loaded (in chunks) */
	private static final byte LOADING_DEPTH = 1;
	public static int BASE_ENERGY = 500;
	/**
	 * All the Characters that take their turns in this TimeStream, both
	 * PlayerCharacters and NonPlayerCharacters.
	 */
	HashSet<Character> characters = new HashSet<Character>();
	/**
	 * What character is currently seen by who is saved here. Personally, each
	 * character himself knows a set of characters he sees; this field contains
	 * backward relation - a set of characters a character is seen by. Only
	 * NonPlayerCharacters are considered the ones who can see - vision of
	 * PlayerCharacters is computed on the client side.
	 * 
	 * @see TimeStream#notifyNeighborsVisiblilty(Character)
	 */
	private HashMap<Character, HashSet<NonPlayerCharacter>> observers = new HashMap<Character, HashSet<NonPlayerCharacter>>();
	/**
	 * Keys are PlayerCharacters in this TimeStream, values are their
	 * WebSockets. If no client is connected for a PlayerCharacter, then value
	 * is null.
	 */
	private HashMap<PlayerHandler, Connection> players = new HashMap<PlayerHandler, Connection>();
	/**
	 * All the NonPlayerCharacters that take their turns in this TimeStream.
	 */
	private HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();
	/**
	 * Events, accumulated here in this ArrayList each turn, ready to send out
	 * to clients.
	 */
	private EventQueue events = new EventQueue();
	/**
	 * Chunks of territory that belong to this TimeStream.
	 */
	HashSet<Chunk> chunks = new HashSet<Chunk>();
	
	/**
	 * Initiate a TimeStream around one PlayerCharacter.
	 * @param character
	 * @param connection
	 */
	public TimeStream(PlayerHandler character, Connection connection) {
		addCharacter(character, connection);
		events.clear();
	}
	
	public void addEvent(ServerEvent event) {
		// Main.console("add event "+event);
		events.add(event);
	}
	public void flushEvents() {
		String data = events.serialize();
		events.clear();
		for (Connection connection : players.values()) {
			if (connection == null) {
				continue;
			}
			connection.send(data);
		}
	}

	/**
	 * Places a PlayerCharacter in this TimeStream, which makes
	 * PlayerCharacter's client receive events from this TimeStream and
	 * determines PlayerCharacter's turn queue.
	 * 
	 * @param character
	 */
	public void addCharacter(PlayerHandler character, Connection connection) {
		characters.add(character);
		players.put(character, connection);
		loadApproachedChunks(character.plane, character.x, character.y);
	}
	public void addNonPlayerCharacter(NonPlayerCharacter character) {
		if (!chunks.contains(character.chunk)) {
			throw new Error(
					character
							+ " must be in a timeStream's chunk to be added to timeStream"
							+ "His chunk is " + character.chunk);
		}
		nonPlayerCharacters.add(character);
		characters.add(character);
	}

	public void removeCharacter(PlayerCharacter player) {
		if (!characters.contains(player)) {
			throw new Error("Player " + player + " is not in this time stream");
		}
		characters.remove(player);
		players.remove(player);
	}

	public void removeCharacter(NonPlayerCharacter character) {
		if (!characters.contains(character)) {
			throw new Error("Character " + character
					+ " is not in this time stream");
		}
		characters.remove(character);
		nonPlayerCharacters.remove(character);
	}

	

	public void checkOut(PlayerHandler player) {
		if (player.checkedOut) {
			throw new Error("Player " + player.name
					+ " has already checked out!");
		} else {
			player.checkedOut = true;
			for (PlayerHandler p : players.keySet()) {
				if (!p.checkedOut) {
					return;
				}
			}
			for (PlayerHandler p : players.keySet()) {
				p.checkedOut = false;
			}
			Character nextCharacter = next();
			while (nextCharacter instanceof NonPlayerCharacter) {
				((NonPlayerCharacter) nextCharacter).action();
				nextCharacter = next();
			}
			final Character finalNextCharacter = nextCharacter;
			addEvent(ServerEvents.create("nextTurn", new GsonForStaticDataSerializable() {
				@Override
				public JsonElement serialize(JsonSerializationContext context) {
					return new JsonPrimitive(finalNextCharacter.getId());
				}
			}));
			flushEvents();
		}
	}

	public void makeSound(int x, int y, SoundType type) {
		addEvent(ServerEvents.create("sound", new Sound(x, y, type)));
	}

	public Character getCharacterById(int characterId) {
		for (Character character : characters) {
			if (character.getId() == characterId) {
				return character;
			}
		}
		throw new Error("No character with id " + characterId);
	}

	/**
	 * Get the next character in turn queue.
	 * 
	 * @return Character
	 */
	public Character next() {
		Character nextCharacter = null;
		// Get the character with the greatest action points left
		for (Character ch : characters) {
			if (nextCharacter == null
					|| ch.getActionPoints() > nextCharacter.getActionPoints()) {
				nextCharacter = ch;
			}
		}
		// If all the characters' energy is less than 0, then here goes the next
		// turn
		if (nextCharacter.getActionPoints() <= 0) {
			for (Character ch : characters) {
				ch.increaseActionPoints(BASE_ENERGY);
			}
			return next();
		}
		return nextCharacter;
	}

	/**
	 * Add a chunk to the TimeStream, set chunk's timeStream pointer to this
	 * TimeStream and add an event for players that this chunk has been added to
	 * the TimeStream.
	 * 
	 * @param chunk
	 */
	void addChunk(Chunk chunk) {
		chunk.setTimeStream(this);
		chunks.add(chunk);
		addEvent(ServerEvents.create("chunkContents", chunk));
	}

	public void excludeChunk(Chunk chunk) {
		if (!chunk.belongsToTimeStream(this)) {
			throw new Error(chunk + " is not in this time stream!");
		}
		chunks.remove(chunk);
		chunk.setTimeStream(null);
		addEvent(ServerEvents.create("excludeChunk", "["+chunk.x+","+chunk.y+"]"));
	}

	/**
	 * Gets a set of characters that are near this character in square with
	 * VISION_RANGE*2+1 side length.
	 * 
	 * @return A set of characters that are close enough to this character.
	 */
	public HashSet<NonPlayerCharacter> getNearbyNonPlayerCharacters(Character character) {
		HashSet<NonPlayerCharacter> answer = new HashSet<NonPlayerCharacter>();
		for (NonPlayerCharacter neighbor : nonPlayerCharacters) {
			// Quickly select characters that could be seen (including this Seer
			// itself)
			if (Math.abs(neighbor.x-character.x) <= Character.VISION_RANGE && Math.abs(neighbor.y-character.y) <= Character.VISION_RANGE) {
				answer.add(neighbor);
			}
		}
		answer.remove(character);
		return answer;
	}

	/**
	 * Add chunks near certain point to this time stream and send their contents
	 * to characters.
	 * 
	 * @param x
	 * @param y
	 */
	public void loadApproachedChunks(HorizontalPlane plane, int x, int y) {
		// A chunk where character came to
		Chunk chunk = plane.getChunkWithCell(x, y);
		// Bounds where we check for missing chunks
		int endX = chunk.getX() + Chunk.WIDTH * TimeStream.LOADING_DEPTH;
		int endY = chunk.getY() + Chunk.WIDTH * TimeStream.LOADING_DEPTH;
		for (int currY = chunk.getY() - Chunk.WIDTH * TimeStream.LOADING_DEPTH; currY <= endY; currY += Chunk.WIDTH) {
			// Find missing chunks and query them
			for (int currX = chunk.getX()-Chunk.WIDTH*TimeStream.LOADING_DEPTH; currX <= endX; currX += Chunk.WIDTH) {
				Chunk newChunk = plane.getChunkByCoord(currX, currY);
				if (!newChunk.belongsToTimeStream(this)) {
					addChunk(newChunk);
				}
			}
		}
	};

	public void unloadUnusedChunks(HorizontalPlane plane) {
		Set<Chunk> chunksToExclude = new HashSet<Chunk>();
		for (Chunk chunk : chunks) {
			if (chunk.plane != plane) {
				continue;
			}
			for (PlayerCharacter player : players.keySet()) {
				int playerChunkX = plane.getChunkRoundedCoord(player.x);
				int playerChunkY = plane.getChunkRoundedCoord(player.y);
				if (Math.abs(playerChunkX - chunk.getX()) > Chunk.WIDTH
						* TimeStream.LOADING_DEPTH
						|| Math.abs(playerChunkY - chunk.getY()) > Chunk.WIDTH
								* TimeStream.LOADING_DEPTH) {
					chunksToExclude.add(chunk);
				}
			}
		}
		for (Chunk chunk : chunksToExclude) {
			this.excludeChunk(chunk);
		}
	}

	/*
	 * NonPlayerCharacters may observe Characters -  and so track their current
	 * coordinates. TimeStream handles most of the observing routine. However,
	 * characters know about who can they personally see even without a
	 * TimeStream. TimeStream observation methods are used when it is needed to
	 * track not who sees who, but _who is seen by who_. NonPlayerCharacters
	 * themselves tell TimeStream about who it should add as observer to who.
	 * Only NonPlayerCharacters can be observers - it makes no sense tracking
	 * PlayerCharacters as observers since their general thought process is made
	 * by player himself, and their visibility is computed on the cliend-side.
	 */
	/**
	 * Remember that Character aim can now be seen by NonPlayerCharacter
	 * observer.
	 * 
	 * @param aim
	 *            Key; who is observed
	 * @param observer
	 *            Value; who is he observed by;
	 */
	void addObserver(Character aim, NonPlayerCharacter observer) {
		observers.get(aim).add(observer);
	}

	void removeObserver(Character aim, NonPlayerCharacter observer) {
		observers.get(aim).remove(observer);
	}
	/**
	 * Tells every nearby {@link NonPlayerCharacter} about this Character's new
	 * position, so nearby characters can update status of this character as
	 * seen/unseen and remember where they have seen this aim last time.
	 */
	public void notifyNeighborsVisiblilty(Character aim) {
		/*
		 * First each of NonPlayerCharacters in TimeStream tries to see the aim,
		 * then all of the aim's observers try to unsee it. Then all the current
		 * observers remember aim's coordinate.
		 */
		
		for (NonPlayerCharacter neighbor : nonPlayerCharacters) {
			// Need to copy observes because its contents will change in the next
			// for loop.
			neighbor.tryToSee(aim);
		}
		HashSet<NonPlayerCharacter> currentObservers = observers.get(aim);
		// Need to copy observes because its contents will change in the next
		// for loop.
		HashSet<NonPlayerCharacter> observersCopy = new HashSet<NonPlayerCharacter>(currentObservers);
		for (NonPlayerCharacter neighbor : observersCopy) {
			neighbor.tryToUnsee(aim);
		}
		for (NonPlayerCharacter character : currentObservers) {
			character.updateObservation(aim, aim.x, aim.y);
		}
	}

	public void claimCharacterDisappearance(Character character) {
		for (NonPlayerCharacter ch : observers.get(character)) {
			ch.tryToUnsee(character);
		}
	}
}
