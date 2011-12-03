package erpoge;

import java.io.IOException;

import erpoge.gui.Window;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;

public class TestWebSocket extends net.tootallnate.websocket.WebSocketServer {
	public static Window window;
	public static TestWebSocket instance = new TestWebSocket(8787);
	public TestWebSocket(int port) {
		super(port);
	}
	public void onClientClose(WebSocket conn) {
		// TODO Auto-generated method stub
		Main.console("client close");
	}

	public void onClientMessage(WebSocket conn, String message) {
		// TODO Auto-generated method stub
		Main.console("client message: "+message);
	}

	public void onClientOpen(WebSocket conn) {
		// TODO Auto-generated method stub
		Main.console("client open");
	}

	public void onIOError(IOException ex) {
		// TODO Auto-generated method stub
		
	}
}
