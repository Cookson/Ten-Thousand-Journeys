package erpoge;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Utils {
	public Utils() {
		
	}
	public static <T> String join(Collection<T> s, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator<T> iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
	
	public static <T> T[] concatAll(T[] first, T[]... rest) {
	  int totalLength = first.length;
	  for (T[] array : rest) {
	    totalLength += array.length;
	  }
	  T[] result = Arrays.copyOf(first, totalLength);
	  int offset = first.length;
	  for (T[] array : rest) {
	    System.arraycopy(array, 0, result, offset, array.length);
	    offset += array.length;
	  }
	  return result;
	}
	
	public static <T> Set<T> intersectArrays(List<T> first, List<T> second) {
        // initialize a return set for intersections
        Set<T> instersection = new HashSet<T>();

        // load first array to a hash
        HashSet<T> array1ToHash = new HashSet<T>();
        for (T fElem : first) {
            array1ToHash.add(fElem);
        }


        // check second array for matches within the hash
        for (T sElem : second) {
            if (array1ToHash.contains(sElem)) {
                // add to the intersect array
            	instersection.add(sElem);
            }
        }

        return instersection;
        
    }
	public static double getLineAngle(Coordinate c1, Coordinate c2) {
		double dx = (double) c2.x - c1.x;
		double dy = (double) c2.y - c1.y;
		double atan;
		if (dx == 0) {
		// Exceptional case of computing atan
			if (dy > 0) {
				atan = Math.PI/2;
			} else {
				atan = Math.PI/2*3;
			}
		} else {
		// Usual case
			atan = Math.atan(dy / dx);
		}
		
		// If line end is in II or III parts of space
		if (dx<0) {
			atan = atan + Math.PI;
		}
		if (atan < 0) {
			atan = Math.PI*2+atan;
		}
		return atan;
	}
}
