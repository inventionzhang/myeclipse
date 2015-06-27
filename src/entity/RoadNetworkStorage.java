package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import entity.Edge;
import entity.Node;
import entity.surface;
import entity.roadName;

public class RoadNetworkStorage implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1985667902112911324L;
	public Map<String, ArrayList<Node>> allJuncArraylistMap=null;//�洢�ڵ�ID�Լ�����
    public Map<String, Map<Integer,ArrayList<Edge>>> allRelationEdgesMap=null;//�洢�ڵ�ID�Լ����ڱ�
    public Map<String, ArrayList<Node>> allNodesArrayListMap=null;//�洢�ڵ����˹�ϵ
    public Map<String, ArrayList<Edge>> allPolylineCollArraylistMap=null;//�洢polyline��ID�Լ�polyline�еĵ�
    public Map<String, ArrayList<surface>> allSurfaceArrayListMap=null;//�洢��
    public Map<Integer, ArrayList<String>> roadNetworkNameMap=null;
    public Map<String, ArrayList<roadName>>allRoadNameArrayMap=null;//�洢���е�·����Ϣ
    
}
