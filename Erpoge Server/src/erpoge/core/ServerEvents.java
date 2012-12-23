package erpoge.core;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * ServerEvents is a factory that creates intermediate representations of game
 * obejcts to be serialized into JSON. ServerEvents creates objects of two
 * classes: {@link ServerEventGson} to be serialized using {@link Gson}, and
 * {@link ServerEventPlain} to be serialized using simple text appending.
 */
public class ServerEvents {
	/**
	 * EventTypes registered with
	 * {@link ServerEvents#registerServerEventType(String, String...)}
	 */
	private static HashMap<String, ServerEventType> eventTypes = new HashMap<String, ServerEventType>();
	/**
	 * Instance of singletone.
	 */
	private static final ServerEvents instance = new ServerEvents();
	/**
	 * Tells the factory about a certain class of events with the save.
	 * ServerEventTypes are registered only to be used with
	 * {@link ServerEventGson} - they make no sense with
	 * {@link ServerEventPlain}
	 * 
	 * @param name
	 * @param fields
	 *            Names of JSON object properties.
	 * @example 
	 * ServerEvents.registerServerEventType("fart", "loudness", "flavor", "tone"); 
	 * ServerEvent e = ServerEvents.createEvent("fart", new Loudness(100), new Flovor("mint"), new Tone("D#"));
	 * timeStream.addEvent(e);
	 * timeStream.flushEvents(); // Sends [{e:"fart", "loudness":100, "flavor": "mint", "tone":"D#"}] to clients
	 */
	public ServerEvents() {
		registerServerEventType("chunkContents", "data");
	}
	void registerServerEventType(String name, String... fields) {
		eventTypes.put(name, new ServerEventType(name, fields));
	}

	public static ServerEventGson create(String name,
			GsonForStaticDataSerializable... data) {
		ServerEventType type = eventTypes.get(name);
		if (type == null) {
			throw new NullPointerException("Server event type \"" + name
					+ "\" has not been registered");
		}
		if (data.length != type.fields.length) {
			throw new RuntimeException(
					"Wrong number of parameters: server event type \"" + name
							+ "\" needs " + type.fields.length
							+ " data arguments");
		}
		return instance.new ServerEventGson(type, data);
	}
	
	public static ServerEventCustomGson create(String name, Object object, Gson gson) {
		return instance.new ServerEventCustomGson(name, object, gson);
	}

	public static ServerEventPlain create(String name, String data) {
		return instance.new ServerEventPlain(name, data);
	}

	class ServerEventGson implements ServerEvent, GsonForStaticDataSerializable {
		ServerEventType type;
		GsonForStaticDataSerializable[] data;

		ServerEventGson(ServerEventType type, GsonForStaticDataSerializable... data) {
			this.type = type;
			this.data = data;
		}

		@Override
		public JsonElement serialize(JsonSerializationContext context) {
			JsonObject jObj = new JsonObject();
			jObj.add("e", new JsonPrimitive(type.name));
			for (int i = 0; i < data.length; i++) {
				jObj.add(type.fields[i], data[i].serialize(context));
			}
			return jObj;
		}

		@Override
		public String toJson() {
			return GsonArbitraryArrays.getGson().toJson(this);
		}
	}

	class ServerEventPlain implements ServerEvent {
		private String name;
		private String data;

		public ServerEventPlain(String name, String data) {
			this.name = name;
			this.data = data;
		}

		public String toJson() {
			return "{e:" + name + ",data:" + data + "}";
		}
	}
	
	class ServerEventCustomGson implements ServerEvent {
		String name;
		Object object;
		Gson gson;
		@Override
		public String toJson() {
			return gson.toJson(object);
		}
		public ServerEventCustomGson(String name, Object object, Gson gson) {
			this.name = name;
			this.object = object;
		}
	}

	class ServerEventType {
		String name;
		String[] fields;

		ServerEventType(String name, String... fields) {
			this.name = name;
			this.fields = fields;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EventType:[");
			for (int i = 0; i < fields.length; i++) {
				builder.append(fields[i]);
				if (i < fields.length - 1) {
					builder.append(", ");
				}
			}
			builder.append("]");
			return builder.toString();
		}
	}
}
