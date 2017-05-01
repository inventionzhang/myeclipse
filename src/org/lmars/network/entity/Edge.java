package org.lmars.network.entity;
import java.io.Serializable;
import java.util.ArrayList;


public class Edge implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7399528492795010157L;
	public int edgeID=0;
	/*存储邻接边中的polyline*/
	public ArrayList<Node> pointCollArrayList=null;
	/*存储邻接边的长度*/
	public double edgeLength=0;
	
	public void setEdgeLength(double length){
		this.edgeLength=length;
	}
	
	public double getEdgeLength(){
		return edgeLength;		
	}
	
	public void setEdgeID(int eid){
	    this.edgeID=eid;
	}
	
	public int getEdgeID(){
	    return edgeID;
	}
	    
	public ArrayList<Node> getPointCollArrayList(){
	    return pointCollArrayList;
	}
	
	public void setPointCollArrayList(ArrayList<Node> pointColl){
	    this.pointCollArrayList=pointColl;
	}	
}
