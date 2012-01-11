package erpoge.magic;

import java.util.HashMap;

import erpoge.Main;
import erpoge.charactereffects.CharacterEffect;
import erpoge.charactereffects.EffectsTypology;
import erpoge.characters.Character;
import erpoge.inventory.Item;
import erpoge.inventory.UniqueItem;
import erpoge.objects.GameObjects;
import erpoge.objects.Sound;

public final class Spells {
	private static final Spells instance = new Spells();
	public final static int 
	SPELL_SUMMON_ITEM 		= 9,
	SPELL_SUMMON_ENEMY 		= 10,
	SPELL_FIREBALL	 		= 2;
	public final HashMap<Integer, Spell> spells = new HashMap<Integer, Spell>();
	public Spells() {
		spells.put(SPELL_FIREBALL, new Spell() {
			public void cast(Character caster, int x, int y) {
				if (caster.location.cells[x][y].character() != null) {
//					caster.location.cells[x][y].character().getDamage(30, Character.DAMAGE_PLAIN);
					caster.location.cells[x][y].character().addEffect(EffectsTypology.EFF_STINK, 5000, 5);
				}				
//				caster.location.fireSound(x, y, Sound.SCREAM);
//				caster.location.fireSound(x, y, Sound.ROAR);
//				caster.location.createCharacter("goblin", "�����", x, y);
			}
		});
		spells.put(SPELL_SUMMON_ITEM, new Spell() {
			public void cast(Character caster, int x, int y) {
				caster.location.createCharacter("bear", "Чужой миша", x, y, 0);
//				if (caster.location.cells[x][y].object() != GameObjects.OBJ_VOID) {
//					caster.location.removeObject(x, y);
//				} else {
//					caster.location.addItem(UniqueItem.createItemByClass(Item.CLASS_SWORD, 0), x, y);
//				}
			}
		});
		spells.put(SPELL_SUMMON_ENEMY, new Spell() {
			public void cast(Character caster, int x, int y) {
				caster.location.createCharacter("bear", "Мой миша", x, y, 1);
			}
		});
	}
	public static void cast(Character caster, int spellId, int x, int y) {
		instance.spells.get(spellId).cast(caster, x, y);
	}
	public interface Spell {
		public void cast(Character caster, int x, int y);
	}
}
