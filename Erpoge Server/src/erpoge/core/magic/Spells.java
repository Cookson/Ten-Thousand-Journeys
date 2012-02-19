package erpoge.core.magic;

import java.util.HashMap;

import erpoge.core.Main;
import erpoge.core.characters.Character;
import erpoge.core.characters.CharacterEffect;
import erpoge.core.characters.EffectsTypology;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.objects.GameObjects;
import erpoge.core.objects.Sound;
import erpoge.core.terrain.Chunk;

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
				if (caster.plane.getCell(x,y).character() != null) {
//					caster.location.cells[x][y].character().getDamage(30, Character.DAMAGE_PLAIN);
					caster.plane.getCell(x,y).character().addEffect(EffectsTypology.EFF_STINK, 5000, 5);
				}				
//				caster.location.fireSound(x, y, Sound.SCREAM);
//				caster.location.fireSound(x, y, Sound.ROAR);
//				caster.location.createCharacter("goblin", "�����", x, y);
			}
		});
		spells.put(SPELL_SUMMON_ITEM, new Spell() {
			public void cast(Character caster, int x, int y) {
				Chunk chunk = caster.plane.getChunkWithCell(x, y);
				chunk.createCharacter(x-chunk.getX(), y-chunk.getY(), "bear", "Чужой миша", 0);
//				if (caster.location.cells[x][y].object() != GameObjects.OBJ_VOID) {
//					caster.location.removeObject(x, y);
//				} else {
//					caster.location.addItem(UniqueItem.createItemByClass(Item.CLASS_SWORD, 0), x, y);
//				}
			}
		});
		spells.put(SPELL_SUMMON_ENEMY, new Spell() {
			public void cast(Character caster, int x, int y) {
				caster.plane.getChunkWithCell(x,y).createCharacter(x, y, "bear", "Мой миша", 1);
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
