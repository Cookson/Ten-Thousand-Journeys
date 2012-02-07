package erpoge.core.net.serverevents;

public class EventDropItem extends ServerEvent {
	public static final String e = "drop";
	public int characterId;
	public int typeId;
	public int param;	
	public EventDropItem(int characterId, int typeId, int param) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;		
	}
}
