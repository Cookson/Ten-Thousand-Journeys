package erpoge.core.itemtypes;

import java.util.HashSet;

import erpoge.core.Aspect;
import erpoge.core.AspectName;
import erpoge.core.Material;

public class ItemType extends ItemSystemMetaInfo {
	private static int lastId = 0; // Used to assign `id`s to `ItemType`s
	private final int id;
	private final String name;
	private final double weight;
	private final double volume;
	private final Material material;
	private final boolean stackable;
	private HashSet<Aspect> aspects;
	public ItemType(String name, HashSet<Aspect> aspects, double weight, double volume, Material material, boolean stackable) {
		this.id = ++lastId;
		this.name = name;
		this.weight = weight;
		this.volume = volume;
		this.material = material;
		this.aspects = aspects;
		this.stackable = stackable;
	}
	public boolean hasAspect(AspectName aspect) {
		for (Aspect a : aspects) {
			if (a.getName() == aspect) {
				return true;
			}
		}
		return false;
	}
	public Aspect getAspect(AspectName aspect) {
		for (Aspect a : aspects) {
			if (a.getName() == aspect) {
				return a;
			}
		}
		return null;
	}
	public boolean isWeapon() {
		return true;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public boolean isStackable() {
		return stackable;
	}
	public String toString() {
		String nString = "Item "+name+" with id "+id+"\n"
			+"weight: "+weight+", volume: "+volume+", material: "+material+", "
			+(stackable ? "stackable" : "non-stackabe")+(aspects.size() > 0 ? "\n" : "");
		for (Aspect aspect : aspects) {
			nString += aspect;
		}
		return nString+"\n";
	}
}
