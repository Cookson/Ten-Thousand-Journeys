package erpoge.core;
import erpoge.core.characters.CharacterManager;
import erpoge.core.gui.Window;

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
//		Main.window = new Window();
		ModuleLoader.loadModules();
		LoadStaticDataFromXML.loadGameDataFromXml();
		StaticData.showData();
				
		//		World world = new World(20,20,"TestWorld", "Erpoge World");
		//		world.showWorld();
		
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
		PlayerHandler burok = CharacterManager.createPlayer(plane, 6, 9, "Alvoi", StaticData.getCharacterType("elf"), "Warrior");
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SWORD * ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new ItemPile(ItemType.CLASS_AMMO * ItemsTypology.CLASS_LENGTH,200));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BLUNT * ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_BOW * ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(ItemType.CLASS_SHIELD * ItemsTypology.CLASS_LENGTH));
		// burok.eventlessGetItem(new UniqueItem(1204));
		// burok.eventlessGetItem(new UniqueItem(1102));
		burok.learnSpell(9);
		burok.learnSpell(10);
		Accounts.addAccount(new Account("1","1"));
		Accounts.addAccount(new Account("Billy","1"));
		Accounts.account("1").addCharacter(burok);
		
		ConnectionServer.setDefaultPlane(plane);
//		MainHandler.startServer();
	}
}
