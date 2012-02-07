package erpoge.core.net.serverevents;

public class EventPutToContainer extends ServerEvent {
	public static final String e = "putToContainer";
	public int characterId;
	public int typeId;
	public int param;
	public int x;
	public int y;
	public EventPutToContainer(int characterId, int typeId, int param, int x, int y) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;
		this.x = characterId;
		this.y = characterId;
	}
}
