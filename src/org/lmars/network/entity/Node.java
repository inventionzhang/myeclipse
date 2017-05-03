package org.lmars.network.entity;

import java.io.Serializable;
import java.util.ArrayList;  
  
/* 表示一个节点以及和这个节点相连的所有节点 */ 
public class Node implements Serializable
{  
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5342124530667283644L;
    /*节点的x，y坐标
     *x即为经度L
     *y即为纬度B,最初定义的，懒得改了 */
	public String name = null;
	public double x,y=0; 
	public int EID = 0; //element ID in network  
	public int EdgeID = 0;//相邻边ID
	public double g = 0;//当前点到起点的移动耗费
	public double h = 0;//当前点到终点的估计移动耗费（即到终点估算距离）
	public double f = 0;//f=g+h
	private Node parentNode;//父类节点
	public ArrayList<Node> relationNodes = new ArrayList<Node>();// 定义与节点相关联的节点集合
	public ArrayList<Edge> relationEdges=new ArrayList<Edge>(); //定义与节点直接相连的边集合
    
    public Node(){
	    
	}
    
    public Node(Node parentNode){
	    this.parentNode=parentNode;
	}
    
    public Node getParentNode() {
	    return parentNode;
	}
    
	public void setParentNode(Node parentNode) {
	    this.parentNode = parentNode;
	}
    
    public void setEID(int eid){
    	this.EID=eid;
    }
    
    public int getEID(){
    	return EID;
    }
    
    public void setEdgeID(int eid){
    	this.EdgeID=eid;
    }
    
    public int getEdgeID(){
    	return EdgeID;
    }
       
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
    
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public ArrayList<Node> getRelationNodes() {  
        return relationNodes;  
    }  
  
    public void setRelationNodes(ArrayList<Node> relationNodes) {  
        this.relationNodes = relationNodes;  
    }  
    
    public ArrayList<Edge> getRelationEdge(){
    	return relationEdges;
    }
    
    public void setRelationEdge(ArrayList<Edge> relationEdge) {  
        this.relationEdges = relationEdge;  
    } 
}  