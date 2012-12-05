package erpoge.buildings;

import java.awt.Rectangle;

import erpoge.core.Character;
import erpoge.core.PlayerCharacter;
import erpoge.core.characters.CharacterCondition;
import erpoge.core.characters.CustomCharacterAction;
import erpoge.core.characters.Dialogue;
import erpoge.core.characters.DialoguePoint;
import erpoge.core.characters.Race;
import erpoge.core.inventory.ItemPile;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Side;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.TerrainBasics;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Tavern extends Building {
	public static final long serialVersionUID = 2372987976L;
	public void draw() {
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
		placeFrontDoor(Side.ANY_SIDE);
		Rectangle lobbyRec = rectangleSystem.content.get(lobby);
		for (int sx=lobbyRec.x; sx<lobbyRec.x+lobbyRec.width; sx++) {
			if (!settlement.isDoor(sx, lobbyRec.y-1)) {
				settlement.setObject(sx, lobbyRec.y, GameObjects.OBJ_VINESHELF);
			}
		}
		
		// NonPlayerCharacter innkeeper = settlement.createCharacter("innkeeper", "Christian", lobbyRec.x, lobbyRec.y+1);
		// innkeeper.setFraction(1);
		// Dialogue
		Dialogue dialogue = new Dialogue();
		CustomCharacterAction aGiveMoney = new CustomCharacterAction() {
			public void perform(Character actor, Character reciever) {
				reciever.getItem(new ItemPile(900000,100));
			}
		};
		DialoguePoint hello = dialogue.addPoint("Привет, странник!");
		DialoguePoint giveMoney = dialogue.addPoint("Вот тебе сто денег.", aGiveMoney);
		DialoguePoint youAreHuman = dialogue.addPoint("Ты человек!");
		DialoguePoint youAreElf = dialogue.addPoint("Ты эльф!");
		DialoguePoint youAreAnother = dialogue
				.addPoint("Ты ни человек, ни эльф!");
		DialoguePoint condWhoAmI = dialogue
				.addPoint(new CharacterCondition<DialoguePoint>(youAreHuman,
						youAreElf, youAreAnother) {
					public DialoguePoint check(Character opponent) {
						if (((PlayerCharacter) opponent).race == Race.HUMAN) {
							return results[0];
						} else if (((PlayerCharacter) opponent).race == Race.ELF) {
							return results[1];
						} else {
							return results[2];
						}
					}
				});
		dialogue.setRoot(hello);
		hello.addAnswer("Дай мне сто денег", giveMoney);
		hello.addAnswer("Скажи, добрый трактирщик, я эльф или человек?", condWhoAmI);
		giveMoney.addAnswer("Спасибо, пока!", hello, true);
		youAreHuman.addAnswer("Спасибо, пока!", hello, true);
		youAreElf.addAnswer("Спасибо, пока!", hello, true);
		youAreAnother.addAnswer("Спасибо, пока!", hello, true);
		// innkeeper.setDialogue(dialogue);
		Rectangle tablesArea = new Rectangle(
				lobbyRec.x+1, lobbyRec.y+1, 
				lobbyRec.width-2, lobbyRec.height-2);
		for (int i=tablesArea.x+Chance.rand(0,1); i<tablesArea.x+tablesArea.width-1;i+=Chance.rand(1,2)) {
			for (int j=tablesArea.y+Chance.rand(0,1); j<tablesArea.y+tablesArea.height-1;j+=Chance.rand(1,2)) {
				settlement.setElement(i, j, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_1);
			}
		}
//		CellCollection tablesArea = settlement.newCellCollection(CellCollection.rectangleToCellsList());
//		tablesArea.setElements(TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_1, 4);
//		tablesArea.setElements(TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_2, 4);
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		return place.width > 6 || place.height > 6;
	}
}
