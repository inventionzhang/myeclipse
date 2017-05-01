package org.lmars.network.entity;


public class pLineIDFrToNode {
	public int ObjectID = 0; 
	public Node frNode =null;
	public Node toNode =null;
	
	public void setObjectID(int oid){
	    this.ObjectID=oid;
	}
	
	public int getObjectID(){
	    return ObjectID;
	}
	
	public void setFrNode(Node node){
	    this.frNode=node;
	}
	
	public Node getFrNode(){
	    return frNode;
	}
	
	public void setToNode(Node node){
	    this.toNode=node;
	}
	
	public Node getToNode(){
	    return toNode;
	}
	
	

}
