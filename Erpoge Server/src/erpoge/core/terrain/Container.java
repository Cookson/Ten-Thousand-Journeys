package erpoge.core.terrain;
import java.util.Hashtable;

import erpoge.core.AspectContainer;
import erpoge.core.ItemCollection;

public class Container extends ItemCollection {
	private AspectContainer aspect;
	public Container(AspectContainer aspect) {
		this.aspect = aspect;
	}
	public double getVolume() {
		return aspect.getVolume();
	}
}
