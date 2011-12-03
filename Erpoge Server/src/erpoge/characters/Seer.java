package erpoge.characters;

import java.util.ArrayList;
import java.util.Collection;

import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Utils;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;
import erpoge.characters.Character;

public class Seer extends Coordinate {
	public Location location;
	public ArrayList<Seer> seenEntities = new ArrayList<Seer>();
	public Seer(int x, int y, Location location) {
		super(x, y);
		this.location = location;
	}
	public void canSeeTest() {
		for (int i=0;i<location.height;i++) {
			for (int j=0;j<location.width;j++) {
				if (initialCanSee(j,i)) {
					Main.out("*");
				} else {
					location.showCell(j,i);
				}
			}
			Main.outln();
		}
	}
	public void getVisibleEntities() {
		Seer ch;
		for (int i=0;i<seenEntities.size();i++) {
		// Leave only those of them that are actually visible
			ch = seenEntities.get(i);
			if (!initialCanSee(ch.x, ch.y)) {
				i--;
				seenEntities.remove(ch);
				ch.seenEntities.remove(this);
			}
		}
		ArrayList<Character> characters = new ArrayList<Character>(location.characters.values());
		for (int i=0;i<characters.size();i++) {
		// Quickly select characters that could be seen (including this Seer itself)
			ch = characters.get(i);
			if (
				Math.abs(ch.x - x) <= Character.VISION_RANGE && 
				Math.abs(ch.y - y) <= Character.VISION_RANGE && 
				!seenEntities.contains(ch)
			) {
				seenEntities.add(ch);
			}
		}
		
		for (int i=0;i<seenEntities.size();i++) {
		// Leave only those of them that are actually visible
			ch = seenEntities.get(i);
			if (!initialCanSee(ch.x, ch.y) || this == ch) {
				seenEntities.remove(ch);
				i--;
			} else if (!ch.seenEntities.contains(this)){
				ch.seenEntities.add(this);
			}
		}
		for (int i=0;i<seenEntities.size();i++) {
		// Leave only those of them that are actually visible
			ch = seenEntities.get(i);
		}
	}
	public boolean canSee(Coordinate entity) {
		return seenEntities.contains(entity);
	}
	public boolean initialCanSee(int x, int y) {
	// Проверяет, находится ли данная клетка на линии видимости
		if (this.isNear(x,y) || this.x==x && this.y==y) {
		// Если клетка рядом или персонаж на ней стоит - то её точно видно
			return true;
		}
		if (Math.floor(this.distance(x, y))>Character.VISION_RANGE) {
			return false;
		}
		// Алгоритм рассматривает несколько случаев взаимного расположения начальной и целевой клетки, 
		// поскольку в значительной части случаев расчёт можно упростить. Алгоритм для общего случая рассматривается последним.
		if (x==this.x || y==this.y) {
			// Для случая, когда тангенс прямой (угловой коэффициент) равен бесконечности или 0 
			// (т.е. когда в цикле в else может быть деление на ноль, т.к. абсцисы или ординаты конца и начала равны)
			// В этом случае придётся сделать только одну проверку по линии (не таким методом, как в else для прямых с tg!=0 и tg!=1)
			if (x==this.x) {
			// Для вертикальных линий
				int dy=Math.abs(y-this.y)/(y-this.y);
				for (int i=this.y+dy; i!=y; i+=dy) {
					if (location.passability[x][i] == 1) {
						return false;
					}
				}
			} else {
			// Для горизонтальных линий
				int dx=Math.abs(x-this.x)/(x-this.x);
				for (int i=this.x+dx; i!=x; i+=dx) {
					if (location.passability[i][y]==1) {
						return false;
					}
				}
			}
			return true;
		} else if (Math.abs(x-this.x)==1) {
		// Для случая, когда координаты конца и начала находятся на двух соседних вертикальных линиях
			int yMin=Math.min(y,this.y);
			int yMax=Math.max(y,this.y);
			for (int i=yMin+1; i<yMax; i++) {
				if (location.passability[x][i]==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			for (int i=yMin+1;i<yMax;i++) {
				if (location.passability[this.x][i]==1) {
					break;
				}
				if (i==yMax-1) {
					return true;
				}
			}
			return false;
		} else if (Math.abs(y-this.y)==1) {
		// Тот же случай, что и предыдущий, но для горизонтальных линий
			int xMin=Math.min(x,this.x);
			int xMax=Math.max(x,this.x);
			for (int i=xMin+1;i<xMax;i++) {
				if (location.passability[i][y]==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			for (int i=xMin+1;i<xMax;i++) {
				if (location.passability[i][this.y]==1) {
					break;
				}
				if (i==xMax-1) {
					return true;
				}
			}
			return false;
		} 
		else if (Math.abs(x-this.x) == Math.abs(y-this.y)) {
		// Случай, когда линия образует с осями угол 45 градусов (abs(tg)==1)
			int dMax=Math.abs(x-this.x);
			int dx=x>this.x ? 1 : -1;
			int dy=y>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (location.passability[cx][cy]==1) {
					return false;
				}
				
			}
			return true;
		} 
		else {
		// Общий случай
			double[][] start = new double[2][2];
			double[] end = new double[4];
			// x и y концов соответствуют x и y центра клетки или её ближайшего угла (значения перебираются в цикле по k каждое с каждым)
			end[0]=(x>this.x)? x-0.5 : x+0.5;
			end[1]=(y>this.y)? y-0.5 : y+0.5;
			end[2]=x;
			end[3]=y;
			start[0][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			start[0][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			start[1][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
			// start[0][1]=this.y;
			// start[1][0]=this.x;
			start[1][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
			Coordinate[] rays=rays(this.x,this.y,x,y);
			jump:
			for (int k=0;k<3;k++) {
				int endNumX=(k==0 || k==1)?0:2;
				int endNumY=(k==0 || k==2)?1:3;
				for (int j=0;j<1;j++) {
				// Новый алгоритм расчёта видимости строится на том, есть ли точки, 
				// которые находятся ближе, чем на 0.5 клетки от прямой - косвенный признак того, что прямая пересекает преграду.
				// Преграды в этом случае считаются окружностями с R=0.5 
				// Это не мешает расчёту видимости на стыках клеток по горизонтали.
				// В этом случае нужно сделать максимум шесть проверок (3 цикла по k - точки конца - и два по j - точки начала)
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart=start[j][0];
					double yStart=start[j][1];
					for (Coordinate c : rays) {
						try {
							if (location.passability[c.x][c.y]==1) {
							// Проверяем каждую клетку
								if (c.x==x && c.y==y || c.x==x && c.y==y) {
									continue;
								}
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
								// Если расстояние до точки не больше 0.5, проверяем следующую из 6 линий
									continue jump;
								}
							}
						} catch (Exception e) {
							throw new Error();
						}
					}
					return true;
				}
			}
			return false;
		}
	}
	public Coordinate getRayEnd(int endX, int endY) {
		// Проверяет, находится ли данная клетка на линии видимости
		if (this.isNear(endX,endY) || this.x==endX && this.y==endY) {
		// Если клетка рядом или персонаж на ней стоит - то её точно видно
			return new Coordinate(endX, endY);
		}
		// Алгоритм рассматривает несколько случаев взаимного расположения начальной и целевой клетки, 
		// поскольку в значительной части случаев расчёт можно упростить. Алгоритм для общего случая рассматривается последним.
		if (endX==this.x || endY==this.y) {
			// Для случая, когда тангенс прямой (угловой коэффициент) равен бесконечности или 0 
			// (т.е. когда в цикле в else может быть деление на ноль, т.к. абсцисы или ординаты конца и начала равны)
			// В этом случае придётся сделать только одну проверку по линии (не таким методом, как в else для прямых с tg!=0 и tg!=1)
			if (endX==this.x) {
			// Для вертикальных линий
				int dy=Math.abs(endY-this.y)/(endY-this.y);
				for (int i=this.y+dy; i!=endY+dy; i+=dy) {
					if (location.passability[endX][i] != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(endX, i-dy);
					}
				}
			} else {
			// Для горизонтальных линий
				int dx=Math.abs(endX-this.x)/(endX-this.x);
				for (int i=this.x+dx; i!=endX+dx; i+=dx) {
					if (location.passability[i][endY] != TerrainBasics.PASSABILITY_FREE) {
						return new Coordinate(i-dx, endY);
					}
				}
			}
			return new Coordinate(endX, endY);
		} else if (Math.abs(endX-this.x)==1) {
		// Для случая, когда координаты конца и начала находятся на двух соседних вертикальных линиях
			int dy=Math.abs(endY-this.y)/(endY-this.y);
			int y1 = endY, y2 = endY;
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (location.passability[endX][i] != TerrainBasics.PASSABILITY_FREE) {
					y1 = i-dy;
					break;
				}
				if (i==endY) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.y+dy; i!=endY+dy; i+=dy) {
				if (location.passability[this.x][i] != TerrainBasics.PASSABILITY_FREE) {
					y2 = i-dy;
					break;
				}
			}
			Coordinate answer;
			if (distance(endX, y1) > distance(this.x, y2)) {
				answer = new Coordinate(endX, y1);
			} else {
				answer = new Coordinate(this.x, y2);
			}
			if (answer.x == this.x && answer.y == y2 && location.passability[endX][endY] == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == this.x && answer.y == y2 && location.passability[endX][endY] == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.y = endY-dy;
			}
			return answer;
		} else if (Math.abs(endY-this.y)==1) {
		// Тот же случай, что и предыдущий, но для горизонтальных линий
			int dx=Math.abs(endX-this.x)/(endX-this.x);
			int x1 = endX, x2 = endX;
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (location.passability[i][endY] != TerrainBasics.PASSABILITY_FREE) {
					x1 = i-dx;
					break;
				}
				if (i==endX) {
					return new Coordinate(endX, endY);
				}
			}
			for (int i=this.x+dx;i!=endX+dx;i+=dx) {
				if (location.passability[i][this.y] != TerrainBasics.PASSABILITY_FREE) {
					x2 = i-dx;
					break;
				}
			}
			Coordinate answer;
			if (distance(x1, endY) > distance(x2, this.y)) {
				answer = new Coordinate(x1, endY);
			} else {
				answer = new Coordinate(x2, this.y);
			}
			if (answer.x == x2 && answer.y == this.y && location.passability[endX][endY] == TerrainBasics.PASSABILITY_FREE) {
			// If answer is the furthest cell on the same line, but {endX:endY} is free
				answer.x = endX;
				answer.y = endY;
			} else if (answer.x == x2 && answer.y == this.y && location.passability[endX][endY] == TerrainBasics.PASSABILITY_NO) {
			// If answer is the furthest cell on the same line, and {endX:endY} has no passage 
				answer.x = endX-dx;
			}
			
			return answer;
		} 
		else if (Math.abs(endX-this.x) == Math.abs(endY-this.y)) {
		// Случай, когда линия образует с осями угол 45 градусов (abs(tg)==1)
			int dMax=Math.abs(endX-this.x);
			int dx=endX>this.x ? 1 : -1;
			int dy=endY>this.y ? 1 : -1;
			int cx=this.x;
			int cy=this.y;
			for (int i=1;i<=dMax;i++) {
				cx+=dx;
				cy+=dy;
				if (location.passability[cx][cy]==1) {
					return new Coordinate(cx-dx, cy-dy);
				}
				
			}
			return new Coordinate(endX, endY);
		} 
		else {
		// Общий случай
			double[][] start = new double[2][2];
			double[] end = new double[4];
			// x и y концов соответствуют x и y центра клетки или её ближайшего угла (значения перебираются в цикле по k каждое с каждым)
			end[0]=(endX>this.x)? endX-0.5 : endX+0.5;
			end[1]=(endY>this.y)? endY-0.5 : endY+0.5;
			end[2]=endX;
			end[3]=endY;
			start[0][0]=(endX>this.x)? this.x+0.5 : this.x-0.5;
			start[0][1]=(endY>this.y)? this.y+0.5 : this.y-0.5;
			start[1][0]=(endX>this.x)? this.x+0.5 : this.x-0.5;
			// start[0][1]=this.y;
			// start[1][0]=this.x;
			start[1][1]=(endY>this.y)? this.y+0.5 : this.y-0.5;
			Coordinate[] rays=rays(this.x,this.y,endX,endY);
			int breakX=this.x, breakY=this.y;
			jump:
			for (int k=0;k<3;k++) {
				int endNumX=(k==0 || k==1)?0:2;
				int endNumY=(k==0 || k==2)?1:3;
				for (int j=0;j<1;j++) {
				// Новый алгоритм расчёта видимости строится на том, есть ли точки, 
				// которые находятся ближе, чем на 0.5 клетки от прямой - косвенный признак того, что прямая пересекает преграду.
				// Преграды в этом случае считаются окружностями с R=0.5 
				// Это не мешает расчёту видимости на стыках клеток по горизонтали.
				// В этом случае нужно сделать максимум шесть проверок (3 цикла по k - точки конца - и два по j - точки начала)
					if (start[j][0]==this.x && start[j][1]==this.y) {
						continue;
					}
					double xEnd = end[endNumX];
					double yEnd = end[endNumY];
					double xStart = start[j][0];
					double yStart = start[j][1];
					for (Coordinate c : rays) {
						try {
							if (location.passability[c.x][c.y]==1) {
							// Проверяем каждую клетку
								
								if (Math.abs(((yStart-yEnd)*c.x+(xEnd-xStart)*c.y+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
								// Если расстояние до точки не больше 0.5, проверяем следующую из 6 линий
									continue jump;
								}
								
							} else {
								breakX = c.x;
								breakY = c.y;
							}
						} catch (Exception e) {
							throw new Error();
						}
					}
					return new Coordinate(endX, endY);
				}
			}
			return new Coordinate(breakX, breakY);
		}
	}
	public Coordinate[] rays (int startX, int startY, int endX, int endY) {
	// Вспомогательная функция для this->canSee
	// Возвращает набор координат клеток, которые необходимо проверить для проверки видимости
		return Utils.concatAll(
			location.vector(startX, startY, endX, endY),
			location.vector(startX,startY+(endY>startY ? 1 : -1),endX+(endX>startX ? -1 : 1),endY),
			location.vector(startX+(endX>startX ? 1 : -1),startY,endX,endY+(endY>startY ? -1 : 1))
		);
	}
	public void excludeFromSeers() {
		for (Seer seer : seenEntities) {
			seer.seenEntities.remove(this);
		}
	}
}
