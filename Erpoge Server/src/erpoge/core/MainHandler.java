package erpoge.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * An intermedia between {@link ConnectionServer} and {@link WebSocketServer}:
 * forwards WebSocketServer events to ConnectionServer.
 */
public class MainHandler extends WebSocketServer {
	private static MainHandler instance;
	HashMap<WebSocket, WebSocketConnection> connections = new HashMap<WebSocket, WebSocketConnection>();
	public MainHandler() throws UnknownHostException {
		super(new InetSocketAddress(InetAddress.getByName("localhost"), 8080));
		Main.outln("Start listening on port "+8080);
	}
	public static void startServer() {
		try {
			instance = new MainHandler();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		instance.start();
	}
	@Override
	public void onClose(WebSocket ws, int code, String reason, boolean remote) {
		ConnectionServer.onClose(connections.get(ws));
		connections.remove(ws);
	}

	@Override
	public void onError(WebSocket ws, Exception ex) {
		ConnectionServer.onError(connections.get(ws), ex);
		// TODO: Decide whether connections[ws] should be removed or not.
	}

	@Override
	public void onMessage(WebSocket ws, String message) {
		ConnectionServer.onMessage(connections.get(ws), message);
	}

	@Override
	public void onOpen(WebSocket ws, ClientHandshake arg1) {
		WebSocketConnection connection = new WebSocketConnection(ws, null);
		ConnectionServer.onOpen(connections.get(ws));
		connections.put(ws, connection);
	}
}
