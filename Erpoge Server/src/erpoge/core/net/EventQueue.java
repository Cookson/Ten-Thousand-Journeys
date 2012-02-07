package erpoge.core.net;

import java.io.IOException;
import java.util.ArrayList;

import net.tootallnate.websocket.WebSocket;

import erpoge.core.net.serverevents.ServerEvent;

public class EventQueue {
	private ArrayList<ServerEvent> events = new ArrayList<ServerEvent>();
	private WebSocket connection;
	public EventQueue(WebSocket connection) {
		this.connection = connection;
	}
	public void addEvent(ServerEvent event) {
		events.add(event);
	}
	public void flush() {
	// Form the json string
		StringBuilder data = new StringBuilder();
		data.append("[");
		int i = 0;
		int iterations = events.size()-1;
		if (iterations>-1) {
			for (;i<iterations;i++) {
				ServerEvent event = events.get(i);
				data.append(MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+",\n");
			}
			ServerEvent event = events.get(i);
			data.append(MainHandler.gsonIncludesStatic.toJson(event,event.getClass()));
		}
		data.append("]");
		// Send data to all players
		String answer = data.toString();
		try {
			connection.send(answer);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Main.log(answer);
		events.clear();
	}
	
}
