package stochasticTimeDependentNetwork;

import java.util.ArrayList;
import entity.Edge;

public class DijkstraNode extends BaseNode{

	private String shortestTimeStr;//到当前节点最短路径时间的时刻
	private double shortestTime;//到当前节点最短路径的时间
	private double g = 0;//当前点到起点的移动耗费
	private double h = 0;//当前点到终点的估计移动耗费（即到终点估算距离）
	private double f = 0;//f=g+h
	private DijkstraNode parentNode;//父节点
	private ArrayList<DijkstraNode> relationNodes = new ArrayList<DijkstraNode>();// 定义与节点相关联的节点集合
	private ArrayList<Edge> relationEdges = new ArrayList<Edge>(); //定义与节点直接相连的边集合
	
	public String getShortestTimeStr() {
	    return shortestTimeStr;
	}
	
	public void setShortestTimeStr(String shortestTimeStr) {
	    this.shortestTimeStr = shortestTimeStr;
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
    
    public BaseNode getParentNode() {
	    return parentNode;
	}
    
	public void setParentNode(DijkstraNode parentNode) {
	    this.parentNode = parentNode;
	}
	
	public ArrayList<DijkstraNode> getRelationNodes() {  
	        return relationNodes;  
	}  
	  
    public void setRelationNodes(ArrayList<DijkstraNode> relationNodes) {  
        this.relationNodes = relationNodes;  
    }  
    
    public ArrayList<Edge> getRelationEdge(){
    	return relationEdges;
    }
    
    public void setRelationEdge(ArrayList<Edge> relationEdge) {  
        this.relationEdges = relationEdge;  
    }
}
