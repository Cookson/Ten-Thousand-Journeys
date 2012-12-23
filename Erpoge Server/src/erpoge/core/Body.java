package erpoge.core;
import java.util.HashMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Body {
	final SimpleGraph<BodyPart, DefaultEdge> graph;
	final HashMap<BodyPart, UniqueItem> covered = new HashMap<BodyPart, UniqueItem>();
	final HashMap<BodyPart, Boolean> blocked = new HashMap<BodyPart, Boolean>();
	final HashMap<Integer, UniqueItem> itemsOn = new HashMap<Integer, UniqueItem>();
	public Body(SimpleGraph<BodyPart, DefaultEdge> graph) {
		this.graph = graph;
	}
	public void putOn(UniqueItem item) throws RuntimeException {
		
	}
	public void takeOff(UniqueItem item) {
		
	}
	public UniqueItem getItem(int itemId) {
		return itemsOn.get(itemId);
	}
	public boolean canPutOn(UniqueItem item) {
		if (!item.getType().hasAspect(AspectName.APPAREL)) {
			return false;
		}
		return true;
	}
	public void wield(UniqueItem item) {
		
	}
	public double getInjuryLevel() {
		throw new Error("Unimpemented");
	}
	/**
	 * Returns a {@link BodyPart} of this Body by its id.
	 * 
	 * @param  id
	 * @return A BodyPart in this Body with a particular id or null if no such BodyPart present.
	 * @see    BodyPart#getId
	 */
	public BodyPart getBodyPartById(int id) {
		for (BodyPart part : graph.vertexSet()) {
			if (part.id == id) {
				return part;
			}
		}
		return null;
	}
	public String toJson() {
		return "";
	}
}
