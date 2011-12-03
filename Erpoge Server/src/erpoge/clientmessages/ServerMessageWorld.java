package erpoge.clientmessages;

public class ServerMessageWorld {
	public int x;
	public int y;
	public int[/* cellNum */][/* contents */] c;
	public WorldObjects[] o;
	public ServerMessageWorld() {
		
	}
	public class WorldObjects {
		int x; 
		int y; 
		int[] o;
		public WorldObjects(int x, int y, int[] o) {
			this.x = x;
			this.y = y;
			this.o = o;
		}
	}
	
}
