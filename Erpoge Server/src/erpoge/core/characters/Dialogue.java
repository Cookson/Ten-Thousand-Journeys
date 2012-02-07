package erpoge.core.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import erpoge.core.graphs.Graph;

public class Dialogue {
	public static final DialoguePoint DIALOGUE_END = new DialoguePoint("");
	private ArrayList<DialoguePoint> dialogue = new ArrayList<DialoguePoint>();
	public DialoguePoint root;
	public Dialogue() {
		
	}
	public DialoguePoint setRoot(DialoguePoint dp) {
	// Sets starting point in dialogue. Returns it's index in graph.
		root = dp;
		return dp;
	}
	public DialoguePoint addPoint(String message) {
		DialoguePoint dp = new DialoguePoint(message);
		dialogue.add(dp);
		return dp;
	}
	public DialoguePoint addPoint(String message, CustomCharacterAction action) {
		DialoguePoint dp = new DialoguePoint(message, action);
		dialogue.add(dp);
		return dp;
	}
	public DialoguePoint addPoint(CharacterCondition<DialoguePoint> condition) {
		DialoguePoint dp = new DialoguePoint(condition);
		dialogue.add(dp);
		return dp;
	}
	
}
