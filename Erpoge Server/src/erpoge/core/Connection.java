package erpoge.core;

public abstract class Connection {
	private PlayerHandler playerHandler;
	public Connection(PlayerHandler playerHandler) {
		this.playerHandler = playerHandler;
	}
	public void setPlayerHandler(PlayerHandler playerHandler) {
		this.playerHandler = playerHandler;
	}
	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}
	public boolean hasPlayerHandler() {
		return playerHandler != null;
	}
	abstract void send(String string);
	abstract boolean isClosed();
}
