package erpoge;
import java.awt.Rectangle;

import erpoge.characters.CharacterTypes;
import erpoge.characters.PlayerCharacter;
import erpoge.gui.Window;
import erpoge.inventory.ItemPile;
import erpoge.inventory.ItemsTypology;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.ItemType;
import erpoge.objects.GameObjects;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.World;

public class Main {
	public static Window window;
	public static final int DEFAULT_PORT = 8787;
	public static int[][] arr;
	public final static boolean DEBUG = true;
	public static final String TEST_LOCATION_TYPE = "Village";
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
//		WebSocketServer w = new MainHandler(8787);
//		w.start();
//		WebSocketServer w2 = new MainHandler(8787);
//		w2.start();
		GameObjects.init();
		ItemsTypology.init();
		CharacterTypes.init();
//		CharacterTypes.jsonTypes();
//		ItemsTypology.showTypology();
//		Main.console(GameObjects.jsonGetObjectProperties());
		World world = new World(40,40,"TestWorld", "Erpoge World");
		
//		world.showWorld();
		Location loc = world.createLocation(0, 0, 60, 70, TEST_LOCATION_TYPE, "New Location");
		loc.showLocation();
				
		PlayerCharacter burok = world.createCharacter("palyer", "Alvoi", 2, "Warrior", 13, 26);
		burok.getItem(new UniqueItem(ItemType.CLASS_SWORD * ItemsTypology.CLASS_LENGTH));
		burok.getItem(new ItemPile(ItemType.CLASS_AMMO * ItemsTypology.CLASS_LENGTH,200));
		burok.getItem(new UniqueItem(ItemType.CLASS_BLUNT * ItemsTypology.CLASS_LENGTH));
		burok.getItem(new UniqueItem(ItemType.CLASS_BOW * ItemsTypology.CLASS_LENGTH));
		burok.learnSpell(9);
		
		Accounts.addAccount(new Account("1","1"));
		Accounts.addAccount(new Account("Billy","1"));
		Accounts.account("1").addCharacter(burok);
		
		MainHandler.assignWorld(world);
		MainHandler.startServer();
	}	
}
