package erpoge.core;

import java.awt.Rectangle;

import erpoge.core.gui.Window;
import erpoge.modules.SuseikaBrowserClientResourceBuilder;

public class Main {
	public static Window window;
	public static final int DEFAULT_PORT = 8787;
	public static int[][] arr;
	public final static boolean DEBUG = true;
	public static final String TEST_LOCATION_TYPE = "Empty";
	public static final int DEFAULT_LOCATION_WIDTH = 30;
	public static final int DEFAULT_LOCATION_HEIGHT = 30;

	/**
	 * <p>
	 * Prints a string without a new line symbol to the main output. It is
	 * recommended to use in methods that are <i>supposed</i> to print something
	 * to the main output, when System.out.println is supposed to be used only
	 * for debugging purposes, So that temporary debugging output won't be mixed
	 * up with a necessary output that doesn't have to be deleted.
	 * </p>
	 * 
	 * @param string
	 */
	public static <T> void out(T string) {
		System.out.print(string);
	}
	/**
	 * <p>
	 * Prints a string with new line symbol to the main output. It is
	 * recommended to use in methods that are <i>supposed</i> to print something
	 * to the main output, when System.out.println is supposed to be used only
	 * for debugging purposes, So that temporary debugging output won't be mixed
	 * up with a necessary output that doesn't have to be deleted.
	 * </p>
	 * 
	 * @param string
	 */
	public static <T> void outln(T string) {
		System.out.println(string);
	}
	/**
	 * <p>
	 * Prints an empty string with a new line symbol to the main output. It is
	 * recommended to use in methods that are <i>supposed</i> to print something
	 * to the main output, when System.out.println is supposed to be used only
	 * for debugging purposes, So that temporary debugging output won't be mixed
	 * up with a necessary output that doesn't have to be deleted.
	 * </p>
	 * 
	 * @param string
	 */
	public static <T> void outln() {
		System.out.println("");
	}
	public static void main(String args[]) {
		// Main.window = new Window();
		if (false) {
			new SuseikaBrowserClientResourceBuilder();
		}
		ModuleLoader.loadModules();
		// StaticData.showData();

		// World world = new World(20,20,"TestWorld", "Erpoge World");
		// world.showWorld();

		HorizontalPlane plane = new HorizontalPlane();
		// plane.generateTerrain(-200,-200,400,400);
		plane.generateLocation(-20, -20, 59, 26, "Forest");
//		plane.showTerrain(-20, -20, 60, 26);

		// PlayerHandler burok = CharacterManager.createPlayer(plane, 6, 9,
		// "Alvoi", StaticData.getCharacterType("elf"), "Warrior");
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SWORD *
		// ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new ItemPile(ItemType.CLASS_AMMO *
		// ItemsTypology.CLASS_LENGTH,200));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BLUNT *
		// ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BOW *
		// ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SHIELD *
		// ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(1204));
		// burok.eventlessGetItem(new UniqueItem(1102));
		// burok.learnSpell(9);
		// burok.learnSpell(10);
		// Accounts.addAccount(new Account("1","1"));
		// Accounts.addAccount(new Account("Billy","1"));
		// Accounts.account("1").addCharacter(burok);

		ConnectionServer.setDefaultPlane(plane);
		// MainHandler.startServer();
	}
}
