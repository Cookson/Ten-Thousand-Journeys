package erpoge.serverevents;

public class EventAttributeChange extends ServerEvent {
	public static final String e = "attrChange";
	public int characterId;
	public int attrId;
	public int value;
	public EventAttributeChange(int characterId, int attrId, int value) {
		this.characterId = characterId;
		this.attrId = attrId;
		this.value = value;
	}
}
