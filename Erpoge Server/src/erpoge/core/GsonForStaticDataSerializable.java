package erpoge.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

/**
 * <p>
 * Implementing this interface allows a class to serialize itself into
 * {@link JsonElement} to avoid Gson default serialization algorithm.
 * </p>
 * 
 * <p>
 * In the engine there are two purposes for serialization into JSON:
 * </p>
 * <ol>
 * <li>To send the serialized data to client as event that occurred on the
 * server-side;</li>
 * <li>To build a static data file</li>
 * </ol>
 * <p>
 * This interface is for the second purpose: implementing it makes sure that
 * there is exactly one way to serialize an object for {@link StaticData}, and
 * the object itself defines how it will be serialized.
 * </p>
 * 
 * @see StaticData
 * @see AsyncEventProvider for information on the first purpose of serializaton.
 * @see PlayerHandler for more information on the first purpose of serializaton.
 * 
 */
interface GsonForStaticDataSerializable {
	/**
	 * 
	 * @param context
	 *            Passed to the method by
	 * @return
	 */
	JsonElement serialize(JsonSerializationContext context);
}
