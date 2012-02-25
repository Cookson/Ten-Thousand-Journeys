package erpoge.core.characters;

import erpoge.core.Character;

public abstract class CharacterCondition<ResultClass> {
	public ResultClass[] results;
	public CharacterCondition(ResultClass... results) {
		this.results = results;
	}
	public abstract ResultClass check(Character opponent);
}
