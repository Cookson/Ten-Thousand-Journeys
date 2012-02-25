package erpoge.core.characters;

import java.util.HashMap;

import erpoge.core.Character;
import erpoge.core.itemtypes.Attribute;
public class EffectsTypology {
	public HashMap<Integer, CharacterEffect> effects = new HashMap<Integer, CharacterEffect>();
	public static final EffectsTypology instance = new EffectsTypology();
	
	public static final int
		EFF_POISON = 1,
		EFF_BERSERK = 2,
		EFF_LIGHT = 3,
		EFF_STINK = 4,
		EFF_SOUND_SOURCE = 5,
		EFF_CONFUSE = 6,
		
		TYPE_ONTURN = 1,	// Computes each turn (most universal type)
		TYPE_ONACTION = 2,	// Computes on each attack, spell, item usage and so on.
		TYPE_ONMOVE = 3;	// Computes with each movement (not magical warp)
	
	public EffectsTypology() {
		addEffect(EFF_POISON, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				ch.changeAttribute(Attribute.HP, -modifier);
			}
		});
		addEffect(EFF_BERSERK, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				
			}
		});
		addEffect(EFF_LIGHT, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				
			}
		});
		addEffect(EFF_STINK, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				
			}
		});
		addEffect(EFF_SOUND_SOURCE, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				
			}
		});
		addEffect(EFF_CONFUSE, TYPE_ONTURN, new CharacterEffect() {
			public void effect(Character ch, int modifier) {
				
			}
		});
	}
	public void addEffect(int id, int type, CharacterEffect effect) {
		effects.put(id, effect);
	}
}
