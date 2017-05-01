package org.lmars.network.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lmars.network.entity.Edge;
import org.lmars.network.entity.Node;
import org.lmars.network.entity.roadName;
import org.lmars.network.entity.surface;

public class RoadNetworkStorage implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1985667902112911324L;
	public Map<String, ArrayList<Node>> allJuncArraylistMap=null;//存储节点ID以及坐标
    public Map<String, Map<Integer,ArrayList<Edge>>> allRelationEdgesMap=null;//存储节点ID以及相邻边
    public Map<String, ArrayList<Node>> allNodesArrayListMap=null;//存储节点拓扑关系
    public Map<String, ArrayList<Edge>> allPolylineCollArraylistMap=null;//存储polyline的ID以及polyline中的点
    public Map<String, ArrayList<surface>> allSurfaceArrayListMap=null;//存储面
    public Map<Integer, ArrayList<String>> roadNetworkNameMap=null;
    public Map<String, ArrayList<roadName>>allRoadNameArrayMap=null;//存储所有道路名信息
    
}
