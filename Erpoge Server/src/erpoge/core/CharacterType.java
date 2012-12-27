package erpoge.core;

import java.util.HashSet;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class CharacterType implements GsonForStaticDataSerializable {
	private static int lastId = 0;
	private int id;
	private HashSet<CharacterAspect> aspects;
	private String name;
	private double weight;
	private double height;
	private DirectedGraph<BodyPartTypeInstance, DefaultEdge> bodyGraph;
	public CharacterType(String name, HashSet<CharacterAspect> aspects, double weight, double height, DirectedGraph<BodyPartTypeInstance, DefaultEdge> bodyGraph) {
		this.id = ++lastId;
		this.name = name;
		this.aspects = aspects;
		this.weight = weight;
		this.height = height;
		this.bodyGraph = bodyGraph;
	}
	public int getId() {
		return id;
	}
	/**
	 * @return the aspects
	 */
	public HashSet<CharacterAspect> getAspects() {
		return new HashSet<CharacterAspect>(aspects);
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}
	public String toString() {
		return name;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		JsonArray jAspectsArray = new JsonArray();
		for (CharacterAspect aspect : aspects) {
			jAspectsArray.add(context.serialize(aspect));
		}
		jArray.add(new JsonPrimitive(name));
		jArray.add(jAspectsArray);
		jArray.add(new JsonPrimitive(weight));
		jArray.add(new JsonPrimitive(height));
		jArray.add(context.serialize(bodyGraph));
		return jArray;
	}
}
