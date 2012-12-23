package erpoge.core;
import java.util.Collection;
class DependencyNotSatisfiedException extends RuntimeException {
	private static final long serialVersionUID = 8665681417317175300L;
	/**
	 * Shows that a {@link erpoge.core.Module} needs some other Modules to be
	 * present in this distribution, but they aren't so this module can't
	 * work without them.
	 * 
	 * @param  missingDependencies Names of the missing modules.
	 */
	Collection<String> missingDependencies;
	DependencyNotSatisfiedException(Collection<String> missingDependencies) {
		super("Can't run modules because it's missing some dependencies: "+missingDependencies);
		this.missingDependencies = missingDependencies;
	}
	Collection<String> getMissingDependencies() {
		return missingDependencies;
	}
}
