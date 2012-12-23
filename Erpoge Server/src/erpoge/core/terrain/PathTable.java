package erpoge.core.terrain;

import java.util.ArrayList;

import erpoge.core.TerrainBasics;
import erpoge.core.meta.Condition;
import erpoge.core.meta.Coordinate;

public abstract class PathTable {
	public int[][] pathTable;
	private TerrainBasics terrain;
	public Coordinate start;
	public Coordinate dest;
	public abstract boolean isEnd();
	public abstract boolean isCellAppropriate();
	public PathTable(Coordinate start, Coordinate dest, 
			TerrainBasics terrain) {
		this.start = start;
		this.dest = dest;
		this.terrain = terrain;
		this.pathTable = new int[terrain.getWidth()][terrain.getHeight()];
		
		boolean pathFound = false;
		ArrayList<Coordinate> oldFront = new ArrayList<Coordinate>();
		ArrayList<Coordinate> newFront = new ArrayList<Coordinate>();
		newFront.add(start);
		for (int i = 0; i < terrain.getWidth(); i++) {
			for (int j = 0; j < terrain.getHeight(); j++) {
				pathTable[i][j] = 0;
			}
		}
		pathTable[start.x][start.y] = 0;
		int t = 0;
		do {
			oldFront = newFront;
			newFront = new ArrayList<Coordinate>();
			for (int i = 0; i < oldFront.size(); i++) {
				int x = oldFront.get(i).x;
				int y = oldFront.get(i).y;
				int[] adjactentX = new int[]{x+1, x, x, x-1, x+1, x+1, x-1, x-1};
				int[] adjactentY = new int[]{y, y-1, y+1, y, y+1, y-1, y+1, y-1};
				for (int j = 0; j<8; j++) {
					int thisNumX = adjactentX[j];
					int thisNumY = adjactentY[j];
					if (thisNumX < 0 || thisNumX >= terrain.getWidth()
						|| thisNumY < 0 || thisNumY >= terrain.getHeight()
						|| pathTable[thisNumX][thisNumY] != 0) {
						continue;
					}
					if (thisNumX == dest.x && thisNumY == dest.y) {
						pathFound = true;
					}
					if (isCellAppropriate()) {
					// Step to cell if character can see it and it is free
					// or character cannot see it and it is not PASSABILITY_NO
						pathTable[thisNumX][thisNumY] = t + 1;
						newFront.add(new Coordinate(thisNumX, thisNumY));
					}
				}
			}
			t++;
		} while (!isEnd() && newFront.size() > 0 && !pathFound && t < 25);
	}
	public ArrayList<Coordinate> getPath(int x, int y) {
		// �������� ���� �� ������ � ���� ������� ��������� (0 - ������ ��� � �.
		// �.)
		if (x == start.x && y == start.y) {
			throw new Error("Getting path to itself");
		}
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		if (start.isNear(x, y)) {
			path.add(new Coordinate(x, y));
			return path;
		}
		// ���������� ����
		int currentNumX = x;
		int currentNumY = y;
		int cX = currentNumX;
		int cY = currentNumY;
		for (int j=pathTable[currentNumX][currentNumY]; j>0; j = pathTable[currentNumX][currentNumY]) {
			// �������: �� ���-�� ����� �� ������ dest �� ��������� ������ (���
			// 1)
			path.add(0, new Coordinate(currentNumX, currentNumY));
			int[] adjactentX = {cX, cX+1, cX, cX-1, cX+1, cX+1, cX-1, cX-1};
			int[] adjactentY = {cY-1, cY, cY+1, cY, cY+1, cY-1, cY+1, cY-1};
			for (int i=0; i<8; i++) {
				// ��� ������ �� ��������� ������ (�, �, �, �)
				int thisNumX = adjactentX[i];
				if (thisNumX<0 || thisNumX>=terrain.getWidth()) {
					continue;
				}
				int thisNumY = adjactentY[i];
				if (thisNumY<0 || thisNumY>=terrain.getHeight()) {
					continue;
				}
				if (pathTable[thisNumX][thisNumY] == j-1) {
					// ���� ������ � ���� ������� �������� ���������� �����,
					// ������� �� ��
					currentNumX = adjactentX[i];
					currentNumY = adjactentY[i];
					cY = currentNumX;
					cY = currentNumY;
					break;
				}
			}
		}
		return path;
	}
}
