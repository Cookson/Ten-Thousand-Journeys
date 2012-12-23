package erpoge.core;
import java.util.HashSet;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
public class AspectApparel extends Aspect implements GsonArbitraryArraySerializable {
	private final Graph<BodyPartTypeInstance, DefaultEdge> form;
	private final HashSet<BodyPartType> covers;
	public static final String p = "LALALLA";
	/**
	 * A set of body parts that can't get any more items 
	 * put on on them if this item is put on.
	 */
	private HashSet<BodyPartType> blocks;
	public AspectApparel(Graph<BodyPartTypeInstance, DefaultEdge> form, HashSet<BodyPartType> covers, HashSet<BodyPartType> blocks) {
		super(AspectName.APPAREL);
		this.form = form;
		this.covers = covers;
		this.blocks = blocks;
	}
	public String toString() {
		return "As apparel:\n"
		+"Its form is "+form+"\n"
		+"It covers "+covers+"\n"
		+(blocks.size() > 0 ? "it blocks "+blocks+"\n": "");
	}
	@Override
	public String[] getFieldOrder() {
		return new String[] { "form", "covers", "blocks" };
	}
}
