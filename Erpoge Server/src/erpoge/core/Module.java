package erpoge.core;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
public abstract class Module {
	private Collection<String> dependencies;
	public abstract void buildStaticData();
	public Collection<String> getDependencies() {
		return dependencies;
	}
	/**
	 * Sets an enumeration of names of other modules that are required to be
	 * added to a distribution for this module to work.
	 * 
	 * @param  dependencies Names of modules
	 */
	public void setDependencies(Collection<String> dependencies) {
		if (dependencies == null) {
			this.dependencies = new ArrayList<String>();
		} else {
			this.dependencies = dependencies;
		}
	}
	/**
	 * Checks if all the modules that are required for this module to work
	 * are loaded by {@link erpoge.core.ModuleLoader}.
	 */
	void checkForDependencies() throws DependencyNotSatisfiedException {
		HashSet<String> lol = new HashSet<String>();
		lol.add("aaargh");
		HashSet<String> unsatisfiedDependencies = new HashSet<String>();
		for (String dependency : dependencies) {
			if (!ModuleLoader.moduleNames.contains(dependency)) {
				unsatisfiedDependencies.add(dependency);
			}
		}
		if (unsatisfiedDependencies.size() > 0) {
			throw new DependencyNotSatisfiedException(unsatisfiedDependencies);
		}
	}
}
