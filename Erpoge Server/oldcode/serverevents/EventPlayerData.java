package erpoge.core.net.serverevents;

import java.util.Collection;

public class EventPlayerData extends ServerEvent {
	public static final String e = "playerData";
	public Collection data;
	public EventPlayerData(Collection data) {
		this.data = data;
	}
}
