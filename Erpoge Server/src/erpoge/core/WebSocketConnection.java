package erpoge.core;

import org.java_websocket.WebSocket;

public class WebSocketConnection extends Connection {
	WebSocket ws;
	public WebSocketConnection(WebSocket ws, PlayerHandler playerHandler) {
		super(playerHandler);
		this.ws = ws;
	}
	@Override
	void send(String data) {
		ws.send(data);
	}
	@Override
	boolean isClosed() {
		return ws.isClosed();
	}
}
