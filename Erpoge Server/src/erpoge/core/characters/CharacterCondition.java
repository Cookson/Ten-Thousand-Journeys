package erpoge.core.characters;

public abstract class CharacterCondition<ResultClass> {
	public ResultClass[] results;
	public CharacterCondition(ResultClass... results) {
		this.results = results;
	}
	public abstract ResultClass check(Character opponent);
}
