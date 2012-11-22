package erpoge.core.characters;

import java.util.ArrayList;
import erpoge.core.BodyTree;
import java.util.HashSet;

public class CharacterType {
	private static int lastId = 0;
	private int id;
	private HashSet<String> aspects;
	private String name;
	private double weight;
	private double height;
	private BodyTree bodyTree;
	public CharacterType(String name, HashSet<String> aspects, double weight, double height, BodyTree bodyTree) {
		this.id = ++lastId;
		this.name = name;
		this.aspects = aspects;
		this.bodyTree = bodyTree;
		this.weight = weight;
		this.height = height;
	}
	public int getId() {
		return id;
	}
}
