package erpoge.core.net.serverevents;

import erpoge.core.terrain.Chunk;

public class EventExcludeChunk extends ServerEvent {
public static final String e = "excludeChunk";
	public int x;
	public int y;
	public EventExcludeChunk(Chunk chunk) {
		this.x = chunk.getX();
		this.y = chunk.getY();
	}
}
