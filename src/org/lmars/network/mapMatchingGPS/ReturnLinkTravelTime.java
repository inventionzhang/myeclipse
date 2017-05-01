package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class ReturnLinkTravelTime implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6777168833507244185L;
	private int linkID;//目标路段ID
	private String taxiID;//出租车ID
	private String startTravelTime;//出租车开始进入某路段的时刻
	private double travelTime;//路段通行时间
	private double taxiMeanSpeed;//出租车平均速度
	private int taxiTravelDirection;//出租车通行方向与路段方向关系
	private int taxiEnterNodeID;//出租车驶入路段端点ID
	private int taxiExitNodeID;//出租车驶出路段端点ID	
	private ArrayList<MapMatchNode> taxiLinkTravelArrayList = new ArrayList<MapMatchNode>();//对应通行时间的GPS
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;
	}
	
	public void setTaxiID(String ttaxiID){
		this.taxiID = ttaxiID;
	}
	
	public String getTaxiID(){
		return taxiID;
	}
	
	public void setStartTravelTime(String startTravelTime){
		this.startTravelTime = startTravelTime;
	}
	
	public String getStartTravelTime(){
		return startTravelTime;
	}
	
	public void setTravelTime(double ttravelTime){
		this.travelTime = ttravelTime;
	}
	
	public double getTravelTime(){
		return travelTime;
	}
	
	public void setTaxiMeanSpeed(double taxiMeanSpeed){
		this.taxiMeanSpeed = taxiMeanSpeed;
	}
	
	public double getTaxiMeanSpeed(){
		return taxiMeanSpeed;
	}
	
	public void setTaxiTravelDirection(int taxiTravelDirection){
		this.taxiTravelDirection = taxiTravelDirection;
	}
	
	public int getTaxiTravelDirection(){
		return taxiTravelDirection;
	}
	
	public void setTaxiEnterNodeID(int taxiEnterNodeID) {
		this.taxiEnterNodeID = taxiEnterNodeID;
	}
	
	public int getTaxiEnterNodeID() {
		return taxiEnterNodeID;
	}
	
	public void setTaxiExitNodeID(int taxiExitNodeID) {
		this.taxiExitNodeID = taxiExitNodeID;
	}
	
	public int getTaxiExitNodeID() {
		return taxiExitNodeID;
	}
	
	public void setTaxiLinkTravelArrayList(ArrayList<MapMatchNode> ttaxiLinkTravelMap){
		this.taxiLinkTravelArrayList = ttaxiLinkTravelMap;
	}
	
	public ArrayList<MapMatchNode> getTaxiLinkTravelArrayList(){
		return taxiLinkTravelArrayList;
	}
}
