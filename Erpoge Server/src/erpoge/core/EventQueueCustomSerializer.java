package erpoge.core;

import com.google.gson.Gson;

import erpoge.core.ServerEvents.ServerEventCustomGson;

public class EventQueueCustomSerializer extends EventQueue {
	final Gson gson;
	public EventQueueCustomSerializer(Gson gson) {
		this.gson = gson;
	}
	@Override
	String serialize() {
		StringBuilder data = new StringBuilder();
		int lastIndex = events.size()-1;
		data.append("[");
		for (ServerEvent e : events.subList(0, lastIndex)) {
			data.append(gson.toJson((ServerEventCustomGson)e)+",");
		}
		ServerEventCustomGson lastEvent = (ServerEventCustomGson) events.get(lastIndex);
		data.append("{\"e\":\""+lastEvent.name+"\","+gson.toJson(lastEvent)+"}]");
		return data.toString();
	}
}
