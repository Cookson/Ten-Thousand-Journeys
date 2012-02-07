package erpoge.core.terrain;

import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.core.Main;
import erpoge.core.characters.Character;
import erpoge.core.characters.GeneratorCharacterGroup;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.objects.GameObjects;

public class CellCollection {
	// �����, ���������� � ��������� �������� - ��������, ��������� �� ���������
	// ������ ������� [[x,y]xN]
	private ArrayList<Coordinate> cells;
	ArrayList<Coordinate> unoccupied;
	boolean hasCells = true;
	public TerrainBasics location;

	public CellCollection(ArrayList<Coordinate> cls, TerrainBasics loc) {
		// cls - ������ ��������� ������� [[x,y]xN]
		// location - ������ Location, ��� ������� ��������� �������� ���
		// CellCollection
		if (cls.isEmpty()) {
			throw new Error("������� ������ ������ ����");
		}
		cells = cls;
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
		for (int i=0;i<size;i++) {
			if (cells.get(i).distance(x,y)<=distance) {
				cells.remove(i);
				i--;
				size--;
			}
		}
	}
	protected void unsetCell(int cellNum) {
		// ������� ������ �� ������� ���������
		// ����� �� ��������� ������� �����, �� ����� �������� ������ ��������
		// ��������� ������ �������
		// ���� ������� ���������, �� ������ �� ��������
		cells.remove(cellNum);
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
			location.setObject(coo.x, coo.y, GameObjects.OBJ_VOID);
		}
	}
	public void forest(int density) {
		// �������� ��������� ���������� ���������
		// density - ����� �� 0 �� 100, ����� ������� ���������� ����� �������
		if (density > 100 || density < 0) {
			throw new Error(
					"�������� ������� ���� ������ ������ � ��������� �� 0 �� 100 (����������� � "
							+ density + ")");
		}
		int amount = Math.round(cells.size() / 100 * density);
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				// ���� � ������� ������ �� �������� ������ �� ����� ���������
				// ������, ����� �� ���������� �������
				// (������ ��������� ����� �������, ��� ��������� ��������
				// this->cells[0] (��. ����� unsetCell))
				return;
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			// ��������� ���������
			location.setObject(cell.x, cell.y, Chance.rand(GameObjects.OBJ_TREE_1, GameObjects.OBJ_TREE_3));
			unsetCell(cellIndex);
		}
	}
	public void setElements(int type, int val, int amount) {
		for (int i = 0; i < amount; i++) {
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
			unsetCell(cellIndex);
		}
	}
	public void setObjects(int val, int amount) {
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				// ���� � ������� ������ �� �������� ������ �� ����� ���������
				// ������, ����� �� ���������� �������
				// (������ ��������� ����� �������, ��� ��������� ��������
				// this->cells[0] (��. ����� unsetCell))
				throw new Error("No more cells");
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			location.setObject(cell.x, cell.y, val);
			unsetCell(cellIndex);
		}
	}
	public void setObjects(ArrayList<Integer> objects, int amount) {
		// ���������� � ������� ��������� amount ��������. �������� �������
		// ���������� ��������� ������� �� objects
		for (int i = 0; i < amount; i++) {
			if (!hasCells) {
				// ���� � ������� ������ �� �������� ������ �� ����� ���������
				// ������, ����� �� ���������� �������
				// (������ ��������� ����� �������, ��� ��������� ��������
				// this->cells[0] (��. ����� unsetCell))
				return;
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);

			int objSize = objects.size() - 1;
			location.setObject(cell.x, cell.y,
					objects.get(Chance.rand(0, objSize)));
			unsetCell(cellIndex);
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
				// ���� � ������� ������ �� �������� ������ �� ����� ���������
				// ������, ����� �� ���������� �������
				// (������ ��������� ����� �������, ��� ��������� ��������
				// this->cells[0] (��. ����� unsetCell))
				throw new Error("No more cells");
			}
			int cellIndex = Chance.rand(0, cells.size()-1);
			Coordinate cell = cells.get(cellIndex);
			location.setElement(cell.x, cell.y, type, val);
			unsetCell(cellIndex);
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
		unsetCell(cellIndex);
		return cell;
	}
	public Coordinate setObjectAndReport(int val) {
		if (!hasCells) {
			throw new Error("No more cells");
		}
		int cellIndex = Chance.rand(0, cells.size()-1);
		Coordinate cell = cells.get(cellIndex);
		location.setObject(cell.x, cell.y, val);
		unsetCell(cellIndex);
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
