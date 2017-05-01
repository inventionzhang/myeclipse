package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lmars.network.entity.Edge;
import org.lmars.network.entity.Node;
import org.lmars.network.entity.roadName;
import org.lmars.network.entity.surface;

public class MapMatchRoadNetworkStorage implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1985667902112911324L;
	public ArrayList<MapMatchNode> juncCollArrayList = null;
	public ArrayList<MapMatchEdge> polylineCollArrayList = null;
	public Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = null;
	public Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = null;
	public Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = null;
}
