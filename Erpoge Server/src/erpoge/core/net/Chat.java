package erpoge.core.net;

import java.util.ArrayList;
import java.util.HashMap;

import erpoge.core.characters.Character;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.net.serverevents.EventChatMessage;
import erpoge.core.terrain.Location;

public final class Chat {
	private static final Chat instance = new Chat();
	private static final int MAX_LOCATION_MESSAGES_LIMIT = 50;
	private HashMap<Location, ArrayList<Message>> timeStreamChats = new HashMap<Location, ArrayList<Message>>();
	private ArrayList<Message> worldChat = new ArrayList<Chat.Message>();

	public Chat() {
		
	}

	public static void locationMessage(PlayerCharacter player, String message) {
		ArrayList<Message> locationChat = instance.timeStreamChats
				.get(player.getTimeStream());
		locationChat.add(0, instance.new Message(player, message));
		if (locationChat.size() > MAX_LOCATION_MESSAGES_LIMIT) {
			locationChat.remove(MAX_LOCATION_MESSAGES_LIMIT + 1);
			
		}
	}
	
	public static void initLocationChat(Location location) {
		instance.timeStreamChats.put(location, new ArrayList<Message>());
	}
	
	public static void worldMessage(Character player, String message) {
		instance.worldChat.add(0, instance.new Message(player, message));
		if (instance.worldChat.size() > MAX_LOCATION_MESSAGES_LIMIT) {
			instance.worldChat.remove(MAX_LOCATION_MESSAGES_LIMIT + 1);
		}
	}
	public static Message[] getMessagesAfter(Location location, long time) {
		// ��� �������, �� �� ��������
//		int length=0;
//		ArrayList<Message> locationMessages = instance.locationChats.get(location);
//		while (locationMessages.get(length).timestamp < time) {
//			length++;
//		}
//		Message[] messages = new Message[length];
//		for (int i=0;i<length;i++) {
//			messages[i] = locationMessages.get(i);
//		}
		Message[] messages = {};
		return messages;
	}
	public static Message[] getMessagesAfter(long time) {
//		int length=0;
//		while (instance.worldChat.get(length).timestamp < time) {
//			length++;
//		}
//		Message[] messages = new Message[length];
//		for (int i=0;i<length;i++) {
//			messages[i] = instance.worldChat.get(i);
//		}
		Message[] messages = {};
		return messages;
	}
	public class Message {
		protected int x;
		protected int y;
		public final Character player;
		public final String message;
		protected long timestamp;

		public Message(Character player, String message) {
			this.player = player;
			this.message = message;
			timestamp = System.currentTimeMillis();
		}
	}
}
