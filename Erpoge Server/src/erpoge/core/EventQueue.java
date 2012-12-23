package erpoge.core;

import java.util.ArrayList;

/**
 * EventQueue is an ordered sequence of {@link ServerEvent}s.
 */
class EventQueue {
	protected ArrayList<ServerEvent> events = new ArrayList<ServerEvent>();
	EventQueue add(ServerEvent event) {
		events.add(event);
		return this;
	}
	/**
	 * Serialize this EventQueue into JSON.
	 * @return
	 */
	String serialize() {
		// Form the json string
		// Conversion from object to textual form occurs in e.toString()
		StringBuilder data = new StringBuilder();
		int lastIndex = events.size()-1;
		data.append("[");
		for (ServerEvent e : events.subList(0, lastIndex)) {
			data.append(e.toJson()+",");
		}
		data.append(events.get(lastIndex)+"]");
		return data.toString();
	}
	/**
	 * Remove all the {@link ServerEvent}s from this EventQueue so it can be reused.
	 */
	void clear() {
		events.clear();
	}
}
