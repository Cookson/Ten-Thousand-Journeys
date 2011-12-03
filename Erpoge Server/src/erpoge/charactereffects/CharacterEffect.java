package erpoge.charactereffects;
import erpoge.characters.Character;
public abstract class CharacterEffect {
	public int id;
	public int effectType;
	
	public CharacterEffect() {
		
	}
	public abstract void effect(Character ch, int modifier);
}
