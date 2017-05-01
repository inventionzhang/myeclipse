package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;  
import com.esri.arcgis.geometry.IPolyline;
  
/* 表示一个节点以及和这个节点相连的所有节点 */ 
public class MapMatchNode implements Serializable
{  
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5342124530667283644L;
    /*节点的x，y坐标
     *x即为经度L
     *y即为纬度B,最初定义的，懒得改了 */
	public double x,y=0; 
	public int nodeID; //element ID in network，初始nodeID设为-2
//	public int EdgeID;//相邻边ID
	public double g = 0;//当前点到起点的移动耗费
	public double h = 0;//当前点到终点的估计移动耗费（即到终点估算距离）
	public double f = 0;//f=g+h
	private MapMatchNode parentNode;//父类节点
	public ArrayList<MapMatchNode> relationNodes = new ArrayList<MapMatchNode>();// 定义与节点相关联的节点集合
	public ArrayList<MapMatchEdge> relationEdges = new ArrayList<MapMatchEdge>(); //定义与节点直接相连的边集合
    
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