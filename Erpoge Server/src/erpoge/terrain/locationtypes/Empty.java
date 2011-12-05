package erpoge.terrain.locationtypes;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.*;
import erpoge.characters.Character;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.graphs.RectangleSystem;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.CellCollection;
import erpoge.terrain.Container;
import erpoge.terrain.Location;
import erpoge.terrain.LocationGenerator;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.World;

import java.util.ArrayList;

public class Empty extends LocationGenerator {
	public Empty(Location location) {
		super(location);
		fillWithCells(1, 0);
		setStartArea(5, 10, 5, 6);
//		CustomRectangleSystem crs = new CustomRectangleSystem(this, 5, 9, 16, 14, 0);
//		crs.cutRectangleFromSide(0, SIDE_W, 3);
//		
//		RectangleSystem mainRS = getGraph(crs);
//		mainRS.drawBorders(ELEMENT_OBJECT, GameObjects.OBJ_WALL_GREY_STONE, false);
		makePeaceful();
		setObject(5,10,GameObjects.OBJ_CHEST_1);
		Container c = createContainer(5,10);
		c.add(new ItemPile(900000, 235));
		c.add(new ItemPile(2300, 467));
		c.add(new UniqueItem(100));
	}
	public void uuu() {
		// Chance ch50 = new Chance(50);
		// ����� ��������� �������
		// int x;
		// int y;
		// if (ch50.roll()) {
		// y = (ch50.roll()) ? 1 : height - 2;
		// x = Chance.rand(1, width - 10);
		// setStartArea(x, y, 8, 1);
		// } else {
		// x = (ch50.roll()) ? 1 : width - 2;
		// y = Chance.rand(1, height - 10);
		// setStartArea(x, y, 1, 8);
		// }
		// createCharacter("dragon","������",5,10);
		// this.square(7, 8, 6, 6, ELEMENT_OBJECT, 4);
		// CellCollection cs = newCellCollection(this.closeCells(8, 9, 20,
		// PASSABILITY_FREE, true));
		// ArrayList<GeneratorCharacterGroup> chs = new
		// ArrayList<GeneratorCharacterGroup>();
		// chs.add(new GeneratorCharacterGroup("goblin", "������", 3, 0));
		// cs.placeCharacters(chs);
		// for (Character ch : characters.values()) {
		// ch.getItem(700, 1);
		// ch.getItem(2300, 20);
		// ch.putOn(700);
		// }
		
		NonPlayerCharacter talker = createCharacter("goblin", "������-������",
				6, 10);
		talker.setFraction(Character.FRACTION_NEUTRAL);
		
	}
}
