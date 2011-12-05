package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import erpoge.Main;

public class DialoguePoint {
	public String message;
	public CustomCharacterAction action;
	private ArrayList<DialoguePoint> nextPoints = new ArrayList<DialoguePoint>();
	private ArrayList<String> answers = new ArrayList<String>();
	private ArrayList<Boolean> ends = new ArrayList<Boolean>();
	public CharacterCondition<DialoguePoint> condition;
	public DialoguePoint(String message) {
		this.message = message;
	}
	public DialoguePoint(String message, CustomCharacterAction action) {
		this.message = message;
		this.action = action;
	}
	public DialoguePoint(CharacterCondition<DialoguePoint> condition) {
		this.condition = condition;
	}
	public DialoguePoint addAnswer(String answer, DialoguePoint nextPoint) {
		return addAnswer(answer, nextPoint, false);
	}
	public DialoguePoint addAnswer(String answer, DialoguePoint nextPoint, boolean end) {
		nextPoints.add(nextPoint);
		answers.add(answer);
		ends.add(end);
		return nextPoint;
	}
	public ArrayList<String> getAnswers() {
		return answers;
	}
	public String getAnswerText(int index) {
		return answers.get(index);
	}
	public DialoguePoint getNextPoint(int answerIndex, Character opponent) {
		DialoguePoint nextPoint = nextPoints.get(answerIndex);
		if (nextPoint.condition != null) {
			nextPoint = nextPoint.condition.test(opponent);
		}
		return nextPoint;
	}
	public boolean isAnswerEnding(int answerIndex) {
	// Checks whether answer node has any children nodes or not
		return ends.get(answerIndex);
	}
}