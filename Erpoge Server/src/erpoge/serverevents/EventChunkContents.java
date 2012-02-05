package erpoge.serverevents;

import erpoge.terrain.Chunk;

public class EventChunkContents extends ServerEvent {
	public static final String e = "chunkContents";
	
	public int x;
	public int y;
	public int[] c;
	public EventChunkContents(Chunk chunk) {
		this.x = chunk.getX();
		this.y = chunk.getY();
		this.c = chunk.getContentsAsIntegerArray();
	}
}
