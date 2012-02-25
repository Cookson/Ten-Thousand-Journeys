package erpoge.core.net.serverevents;

import java.util.ArrayList;

import erpoge.core.Main;
import erpoge.core.terrain.Chunk;

public class EventChunkContents extends ServerEvent {
	public static final String e = "chunkContents";
	public int x;
	public int y;
	public int[] c;
	public Integer[] i;
	public ArrayList[] ch;
	public EventChunkContents(Chunk chunk) {
		this.x = chunk.getX();
		this.y = chunk.getY();
		this.c = chunk.getContentsAsIntegerArray();
		this.i = chunk.getItemsAsIntegerArray();
		this.ch = chunk.getCharacters();
	}
}
