package erpoge.core;
import java.awt.Rectangle;

import erpoge.core.characters.CharacterManager;
import erpoge.core.characters.CharacterTypes;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.characters.Race;
import erpoge.core.gui.Window;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.itemtypes.ItemsTypology;
import erpoge.core.net.Account;
import erpoge.core.net.Accounts;
import erpoge.core.net.MainHandler;
import erpoge.core.net.PlayerHandler;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.settlements.Building;

public class Main {
	public static Window window;
	public static final int DEFAULT_PORT = 8787;
	public static int[][] arr;
	public final static boolean DEBUG = true;
	public static final String TEST_LOCATION_TYPE = "Empty";
	public static final int DEFAULT_LOCATION_WIDTH = 30;
	public static final int DEFAULT_LOCATION_HEIGHT = 30;
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
	public static <T> void log(T message) {
		outln(message);
	}
	public static void main(String args[]) {
		Main.window = new Window();
		
		GameObjects.init();
		ItemsTypology.init();
		CharacterTypes.init();
		//		Main.log(GameObjects.jsonGetObjectProperties());
		//		CharacterTypes.jsonTypes();
		//		ItemsTypology.showTypology();
		//		ItemsTypology.jsonTypology();
				
		//		World world = new World(20,20,"TestWorld", "Erpoge World");
		//		world.showWorld();
				
		//		Location loc = world.createLocation(0, 0, 60, 40, TEST_LOCATION_TYPE, "New Location");
		//		loc.showLocation();	
		
		HorizontalPlane plane = new HorizontalPlane();
		plane.generateTerrain(-200,-200,400,400);
		//		plane.generateLocation(-60, -60, 40, 40, "Forest");
		//		plane.generateLocation(-60, -20, 40, 40, "Forest");
		//		plane.generateLocation(-60,  20, 40, 40, "Forest");
		//		plane.generateLocation(-20, -60, 40, 40, "Forest");
		//		plane.generateLocation(-20, -20, 40, 40, "BuildingTest");
		//		plane.generateLocation(-20,  20, 40, 40, "Forest");
		//		plane.generateLocation( 20, -60, 40, 40, "Forest");
		//		plane.generateLocation( 20, -20, 40, 40, "Forest");
		//		plane.generateLocation( 20,  20, 40, 40, "Forest");
				
		//		plane.showTerrain(-20, -20, 100, 100);
		PlayerHandler burok = CharacterManager.createPlayer(plane, 6, 9, "Alvoi", Race.ELF, "Warrior");
		burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SWORD * ItemsTypology.CLASS_LENGTH));
		burok.eventlessGetItem(new ItemPile(ItemType.CLASS_AMMO * ItemsTypology.CLASS_LENGTH,200));
		burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BLUNT * ItemsTypology.CLASS_LENGTH));
		burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BOW * ItemsTypology.CLASS_LENGTH));
		burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SHIELD * ItemsTypology.CLASS_LENGTH));
		burok.eventlessGetItem(new UniqueItem(1204));
		burok.eventlessGetItem(new UniqueItem(1102));
		burok.learnSpell(9);
		burok.learnSpell(10);
		Accounts.addAccount(new Account("1","1"));
		Accounts.addAccount(new Account("Billy","1"));
		Accounts.account("1").addCharacter(burok);
		
		MainHandler.startServer();
		MainHandler.setDefaultPlane(plane);
	}
}
