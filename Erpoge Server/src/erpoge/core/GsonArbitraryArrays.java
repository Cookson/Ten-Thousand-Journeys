package erpoge.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class GsonArbitraryArrays {
	private static final GsonArbitraryArrays instance = new GsonArbitraryArrays();
	private Gson gson;

	private GsonArbitraryArrays() {
		GsonBuilder builder = new GsonBuilder();
		builder
			.registerTypeHierarchyAdapter(GsonArbitraryArraySerializable.class, new JsonSerializer<GsonArbitraryArraySerializable>() {
				@Override
				public JsonElement serialize(GsonArbitraryArraySerializable src, Type typeOfSrc, JsonSerializationContext context) {
					JsonArray array = new JsonArray();
					String[] fields = src.getFieldOrder();
					for (int i = 0, l = fields.length; i < l; i++) {
						try {
							Field field = src.getClass()
								.getDeclaredField(fields[i]);
							field.setAccessible(true);
							array.add(context.serialize(field.get(src)));
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
					return array;
				}
			});
		builder
			.registerTypeHierarchyAdapter(GsonForStaticDataSerializable.class, new JsonSerializer<GsonForStaticDataSerializable>() {
				@Override
				public JsonElement serialize(GsonForStaticDataSerializable src, Type typeOfSrc, JsonSerializationContext context) {
					return src.serialize(context);
				}
			});
		builder
			.registerTypeAdapter(DefaultDirectedGraph.class, new JsonSerializer<DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge>>() {
				DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge> graph;

				@Override
				public JsonElement serialize(DefaultDirectedGraph<BodyPartTypeInstance, DefaultEdge> graph, Type t, JsonSerializationContext context) {
					// Save a graph in a field to use in the buildJson() method.
					this.graph = graph;
					// Find the root body part (usually torso)
					BodyPartTypeInstance root = null;
					for (BodyPartTypeInstance part : graph.vertexSet()) {
						// Check every vertex if it has any edges going into it.
						// If it
						// has not, then it is the root vertex.
						Set<DefaultEdge> edges = graph.edgesOf(part);
						boolean hasIncomingEdges = false;
						for (DefaultEdge edge : edges) {
							if (graph.getEdgeTarget(edge) == part) {
								hasIncomingEdges = true;
								break;
							}
						}
						if (!hasIncomingEdges) {
							root = part;
							break;
						}
					}
					// After we found body part, recursively build the json
					// structure of body.
					if (root == null) {
						throw new RuntimeException("Some character's body doesn't have a root vertex!");
					}
					return buildJson(root);
				}

				/**
				 * <p>
				 * Builds a recursive Json structure:
				 * </p>
				 * 
				 * <pre>
				 * A = [%nameOfBodyPart%, A1, A2, A3 ...]
				 * }
				 * </pre>
				 * <p>
				 * Where An inside A is description of a body part that grows
				 * out of body part A
				 * </p>
				 * 
				 * @param root
				 *            The root body part of a body structure (usually a
				 *            torso)
				 * @return Body structure in Json as described in &lt;pre&gt;
				 */
				private JsonElement buildJson(BodyPartTypeInstance root) {
					JsonArray jObjNew = new JsonArray();
					jObjNew.add(new JsonPrimitive(root.getType().getId()));
					for (DefaultEdge edge : graph.edgesOf(root)) {
						/*
						 * Using all edges of root body part, seek for its
						 * children body parts, and build their structure.
						 */
						if (graph.getEdgeSource(edge) == root) {
							jObjNew.add(buildJson(graph.getEdgeTarget(edge)));
						}
					}
					return jObjNew;
				}
			});
		// builder.setPrettyPrinting();
		gson = builder.create();
	}

	public static Gson getGson() {
		return instance.gson;
	}
}
