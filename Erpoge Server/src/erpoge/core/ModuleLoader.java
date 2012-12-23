package erpoge.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.modules.MainModule;
/**
 * Manages loading of various game {@link Module}s
 */
class ModuleLoader {
	static HashMap<String, Module> modules = new HashMap<String, Module>();
	static Collection<String> moduleNames;
	private static final ModuleLoader instance = new ModuleLoader();
	public String test = "hey hey hye";
	/**
	 * Loads modules so the engine can use their data and classes. This
	 * method also checks if the modules' dependencies are satisfied.
	 */
	static void loadModules() {
		MainModule mm = new MainModule();
		moduleNames = new HashSet<String>();
		moduleNames.add("MainModule");
		// Load modules
		for (String moduleName : moduleNames) {
			try {
				modules.put(moduleName, (Module)Class.forName("erpoge.modules."+moduleName).newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Check for dependencies and run modules
		for (Module module : modules.values()) {
			module.checkForDependencies();
			module.buildStaticData();
		}
	}
}