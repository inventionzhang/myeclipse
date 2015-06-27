package mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;  
import com.esri.arcgis.geometry.IPolyline;
  
/* ��ʾһ���ڵ��Լ�������ڵ����������нڵ� */ 
public class MapMatchNode implements Serializable
{  
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5342124530667283644L;
    /*�ڵ��x��y����
     *x��Ϊ����L
     *y��Ϊγ��B,�������ģ����ø��� */
	public double x,y=0; 
	public int nodeID; //element ID in network����ʼnodeID��Ϊ-2
//	public int EdgeID;//���ڱ�ID
	public double g = 0;//��ǰ�㵽�����ƶ��ķ�
	public double h = 0;//��ǰ�㵽�յ�Ĺ����ƶ��ķѣ������յ������룩
	public double f = 0;//f=g+h
	private MapMatchNode parentNode;//����ڵ�
	public ArrayList<MapMatchNode> relationNodes = new ArrayList<MapMatchNode>();// ������ڵ�������Ľڵ㼯��
	public ArrayList<MapMatchEdge> relationEdges = new ArrayList<MapMatchEdge>(); //������ڵ�ֱ�������ı߼���
    
    public MapMatchNode(){
	    
	}
    
    public MapMatchNode(MapMatchNode parentNode){
	    this.parentNode=parentNode;
	}
    
    public MapMatchNode getParentNode() {
	    return parentNode;
	}
    
	public void setParentNode(MapMatchNode parentNode) {
	    this.parentNode = parentNode;
	}
    
    public void setNodeID(int nodeID){
    	this.nodeID = nodeID;
    }
    
    public int getNodeID(){
    	return nodeID;
    }
    
//    public void setEdgeID(int eid){
//    	this.EdgeID=eid;
//    }
//    
//    public int getEdgeID(){
//    	return EdgeID;
//    }
       
    public double getX(){
    	return x;
    }
    
    public void setX(double x){
    	this.x = x;
    }
    
    public void setY(double y){
    	this.y =y;
    }
    
    public double getY(){
    	return y;
    }  
    
    public double getG() {
	    return g;
	}
	public void setG(double g) {
	    this.g = g;
	}
	public double getH() {
	    return h;
	}
	public void setH(double h) {
	    this.h = h;
	}
	public double getF() {
	    return f;
	}
	public void setF(double f) {
	    this.f = f;
	}
  
    public ArrayList<MapMatchNode> getRelationNodes() {  
        return relationNodes;  
    }  
  
    public void setRelationNodes(ArrayList<MapMatchNode> relationNodes) {  
        this.relationNodes = relationNodes;  
    }  
    
    public ArrayList<MapMatchEdge> getRelationEdges(){
    	return relationEdges;
    }
    
    public void setRelationEdges(ArrayList<MapMatchEdge> relationEdge) {  
        this.relationEdges = relationEdge;  
    } 
}  