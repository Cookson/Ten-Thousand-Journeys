package erpoge;
import java.awt.Rectangle;

import erpoge.characters.CharacterTypes;
import erpoge.characters.PlayerCharacter;
import erpoge.gui.Window;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.ItemType;
import erpoge.itemtypes.ItemsTypology;
import erpoge.objects.GameObjects;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.World;
import erpoge.terrain.settlements.Building;

public class Main {
	public static Window window;
	public static final int DEFAULT_PORT = 8787;
	public static int[][] arr;
	public final static boolean DEBUG = true;
	public static final String TEST_LOCATION_TYPE = "Empty";
	public static final int DEFAULT_LOCATION_WIDTH = 60;
	public static final int DEFAULT_LOCATION_HEIGHT = 40;
	public static <T> void out(T string) {
		System.out.print(string);
	}
	public static <T> void outln(T string) {
		System.out.println(string);
	}

	public static <T> void outln() {
		System.out.println("");
	}
	public static <T> void console(T message) {
		/**
		 * Analogue for outln which only works when constant Main.DEBUG == true.
		 * Otherwise it won't print anything.
		 */
		if (!DEBUG) {
			return;
		}
		outln(message);
	}
	public static void main(String args[]) {
		Main.window = new Window();
		
		GameObjects.init();
		ItemsTypology.init();
		CharacterTypes.init();
//		Main.console(GameObjects.jsonGetObjectProperties());
//		CharacterTypes.jsonTypes();
//		ItemsTypology.showTypology();
//		ItemsTypology.jsonTypology();
		
		World world = new World(40,40,"TestWorld", "Erpoge World");
//		world.showWorld();
		
//		Location loc = world.createLocation(0, 0, 60, 40, TEST_LOCATION_TYPE, "New Location");
//		loc.showLocation();	
		
		PlayerCharacter burok = world.createPlayer("Alvoi", 2, "Warrior", 13, 26);
		burok.getItem(new UniqueItem(ItemType.CLASS_SWORD * ItemsTypology.CLASS_LENGTH));
		burok.getItem(new ItemPile(ItemType.CLASS_AMMO * ItemsTypology.CLASS_LENGTH,200));
		burok.getItem(new UniqueItem(ItemType.CLASS_BLUNT * ItemsTypology.CLASS_LENGTH));
		burok.getItem(new UniqueItem(ItemType.CLASS_BOW * ItemsTypology.CLASS_LENGTH));
		burok.getItem(new UniqueItem(1204));
		burok.getItem(new UniqueItem(1102));
		burok.learnSpell(9);
		burok.learnSpell(10);
		
		Accounts.addAccount(new Account("1","1"));
		Accounts.addAccount(new Account("Billy","1"));
		Accounts.account("1").addCharacter(burok);
		
		MainHandler.assignWorld(world);
		MainHandler.startServer();
	}	
}
