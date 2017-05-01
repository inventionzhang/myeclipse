package org.lmars.network.mapMatchingGPS;

import java.util.Comparator;

import org.lmars.network.entity.Node;

public class MapMatchComparator implements Comparator<MapMatchNode>{
	@Override
	public int compare(MapMatchNode o1, MapMatchNode o2){
		if (o1.getF() < o2.getF()) {
			return -1;
		}
		else if (o1.getF() > o2.getF()) {
			return 1;
		}
		else {
			return 0;
		}
    }
}
