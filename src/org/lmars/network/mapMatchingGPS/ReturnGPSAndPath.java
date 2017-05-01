package org.lmars.network.mapMatchingGPS;

import java.util.ArrayList;

public class ReturnGPSAndPath {
	public ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();
	public ArrayList<ArrayList<ReturnMatchNode>> returnMapMatchEdgeArrayList = new ArrayList<ArrayList<ReturnMatchNode>>();
	
	public void setReturnGPSArrayList(ArrayList<ReturnMatchNode> treturnGPSArrayList){
		this.returnGPSArrayList = treturnGPSArrayList;	
	}
	
	public ArrayList<ReturnMatchNode> getReturnGPSArrayList(){
		return returnGPSArrayList;	
	}
	
	public void setReturnMapMatchEdgeArrayList(ArrayList<ArrayList<ReturnMatchNode>> treturnMapMatchEdgeArrayList){
		this.returnMapMatchEdgeArrayList = treturnMapMatchEdgeArrayList;	
	}
	
	public ArrayList<ArrayList<ReturnMatchNode>> getReturnMapMatchEdgeArrayList(){
		return returnMapMatchEdgeArrayList;	
	}
}
