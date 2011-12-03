package erpoge.characters;

public abstract class CharacterCondition<ResultClass> {
	public ResultClass[] results;
	public CharacterCondition(ResultClass... results) {
		this.results = results;
	}
	public abstract ResultClass test(Character opponent);
}
