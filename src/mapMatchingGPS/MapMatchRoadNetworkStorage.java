package mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import entity.Edge;
import entity.Node;
import entity.surface;
import entity.roadName;

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
