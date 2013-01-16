package erpoge.core.terrain;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import erpoge.core.ObjectType;
import erpoge.core.StaticData;
import erpoge.core.TerrainBasics;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;

public class CellCollection {
	private ArrayList<Coordinate> cells;
	ArrayList<Coordinate> unoccupied;
	boolean hasCells = true;
	public TerrainBasics location;

	public CellCollection(Collection<Coordinate> cls, TerrainBasics loc) {
		if (cls.isEmpty()) {
			throw new Error("Can't create an empty cell collection: argument is an empty collection");
		}
		cells = new ArrayList<Coordinate>(cls);
		location = loc;
	}
	public int size() {
		return cells.size();
	}
//	public void placeCharacters(ArrayList<GeneratorCharacterGroup> chs) {
//		/*
//		 * ���������� � ������� ���������� characters - ������ �� ���������,
//		 * ������� ����� ����� ���� �� ���� �����: 1. "type" - ��� ������������
//		 * ��������� 2. ["type",amount(,fraction)] - ��� � ����������
//		 * ����������� ����������, � ����� ������� � ������ ������ ���������
//		 * ������ ���� ��������. ������ �������� ����������� �� ���������
//		 * ������.
//		 */
//		for (GeneratorCharacterGroup ch : chs) {
//			// �������� �� ���� ����������
//			for (int i = 0; i < ch.amount; i++) {
//				if (!hasCells) {
//					// ���� � ������� ������ �� �������� ������ �� �����
//					// ��������� ������, ����� �� ���������� �������
//					// (������ ��������� ����� �������, ��� ��������� ��������
//					// this->cells[0] (��. ����� unsetCell))
//					throw new Error(
//							"� cellCollection �� �������� ����� ��� ����� ���������� - ��������� ������");
//				}
//				int cellIndex = Chance.rand(0, cells.size()-1);
//				Coordinate cell = cells.get(cellIndex);
//				// ��������� ���������
//				location.createCharacter(ch.type, ch.name, cell.x, cell.y);
//				unsetCell(cellIndex);
//			}
//		}
//	}
	public void removeCellsCloseTo(int x, int y, int distance) {
		int size = cells.size();
		for (Coordinate c : cells) {
			if (c.distance(x,y)<=distance) {
				cells.remove(c);
				size--;
			}
		}
	}
	protected void unsetCell(Coordinate cell) {
		cells.remove(cell);
		if (cells.isEmpty()) {
			hasCells = false;
		}
	}
	public void setFloor(int val) {
		for (Coordinate coo : cells) {
			location.setFloor(coo.x, coo.y, val);
		}
	}
	public void clear() {
		Chance ch10 = new Chance(10);
		for (Coordinate coo : cells) {
			if (ch10.roll()) {
				continue;
			}
			location.setObject(coo.x, coo.y, StaticData.VOID);
		}
	}
	/**
	 * Randomly puts some elements 
	 * @param type Type of an element
	 * @param val
	 * @param amount
	 */
	public void setElements(int type, int val, int amount) {
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				throw new RuntimeException("CellCollection has no cells left");
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			location.setElement(cell.x, cell.y, type, val);
			unsetCell(cell);
		}
	}
	/**
	 * 
	 * @param val
	 * @param amount
	 */
	public void setObjects(ObjectType type, int amount) {
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				throw new RuntimeException("CellCollection has no cells left");
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			location.setObject(cell.x, cell.y, type.getId());
			unsetCell(cell);
		}
	}
	
	public Coordinate getRandomCell() {
		return cells.get(Chance.rand(0,cells.size()-1));
	}
	
	public void fillWithElements(int type, int val) {
		// TODO Auto-generated method stub
		for (Coordinate c : cells) {
			location.setElement(c.x, c.y, type, val);
		}
	}
	public ArrayList<Coordinate> setElementsAndReport(int type, int val, int amount) {
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				throw new RuntimeException("CellCollection has no cells left");
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			location.setElement(cell.x, cell.y, type, val);
			unsetCell(cell);
			coords.add(cell);
		}
		return coords;
	}
	public Coordinate setElementAndReport(int type, int val) {
		if (!hasCells) {
			// ���� � ������� ������ �� �������� ������ �� ����� ���������
			// ������, ����� �� ���������� �������
			// (������ ��������� ����� �������, ��� ��������� ��������
			// this->cells[0] (��. ����� unsetCell))
			throw new Error("No more cells");
		}
		int cellIndex = Chance.rand(0, cells.size()-1);
		Coordinate cell = cells.get(cellIndex);
		location.setElement(cell.x, cell.y, type, val);
		unsetCell(cell);
		return cell;
	}
	public Coordinate setObjectAndReport(int val) {
		if (!hasCells) {
			throw new Error("No more cells");
		}
		int cellIndex = Chance.rand(0, cells.size()-1);
		Coordinate cell = cells.get(cellIndex);
		location.setObject(cell.x, cell.y, val);
		unsetCell(cell);
		return cell;
	}
//	public Character setCharacter(String type, String name) {
//		if (!hasCells) {
//			throw new Error("No more cells");
//		}
//		int cellIndex = Chance.rand(0, cells.size()-1);
//		Coordinate cell = cells.get(cellIndex);
//		unsetCell(cellIndex);
//		return location.createCharacter(type, name, cell.x, cell.y);
//	}
	public static ArrayList<Coordinate> rectangleToCellsList(Rectangle r) {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		for (int i=r.x;i<r.x+r.width;i++) {
			for (int j=r.y;j<r.y+r.height;j++) {
				answer.add(new Coordinate(i,j));
			}
		}
		return answer;
	}
	
}
