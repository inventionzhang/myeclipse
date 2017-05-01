package org.lmars.network.mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class TaxiTravelTime {
	private int linkID;//目标路段ID
	private String taxiID;//出租车ID
	private ArrayList<String> startTravelTimeArraylist = new ArrayList<String>();//出租车开始进入路段的时刻,出租车可能多次进入同一路段
	//以时间为索引，某时刻出租车通过路段的通行时间
	private Map<String, Double> travelTimeMap = new HashMap<String, Double>();
	//以时间为索引，某时刻出租车通过路段的GPS点
	private Map<String, ArrayList<MapMatchNode>> GPSTravelMap = new HashMap<String, ArrayList<MapMatchNode>>();
	//以时间为索引，某时刻出租车在路段上平均速度
	private Map<String, Double> taxiMeanSpeedMap = new HashMap<String, Double>();
	//以时间为索引，某一时间点通过路段时，出租车行驶方向与路段的方向关系
	private Map<String, Integer> taxiTravelDirectionMap = new HashMap<String, Integer>();
	//以时间为索引，某一时间点通过路段时，出租车进入路段端点ID与驶出路段端点ID,0对应进入端点索引，1对应驶出路段端点索引
	private Map<String, int[]> taxiEntranceExitNodeIDMap = new HashMap<String, int[]>();
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;
	}
	
	public void setTaxiID(String taxiID){
		this.taxiID = taxiID;
	}
	
	public String getTaxiID(){
		return taxiID;
	}
	
	public void setStartTravelTimeArraylist(ArrayList<String> startTravelTimeArraylist){
		this.startTravelTimeArraylist = startTravelTimeArraylist;
	}
	
	public ArrayList<String> getStartTravelTimeArraylist(){
		return startTravelTimeArraylist;
	}
	
	public void setTravelTimeMap(Map<String, Double> travelTimeMap){
		this.travelTimeMap = travelTimeMap;
	}
	
	public Map<String, Double> getTravelTimeMap(){
		return travelTimeMap;
	}
	
	public void setGPSTravelMap(Map<String, ArrayList<MapMatchNode>> GPSTravelMap){
		this.GPSTravelMap = GPSTravelMap;
	}
	
	public Map<String, ArrayList<MapMatchNode>> getGPSTravelMap(){
		return GPSTravelMap;
	}
	
	public void setTaxiMeanSpeedMap(Map<String, Double> taxiMeanSpeedMap){
		this.taxiMeanSpeedMap = taxiMeanSpeedMap;
	}
	
	public Map<String, Double> getTaxiMeanSpeedMap(){
		return taxiMeanSpeedMap;
	}
	
	public void setTaxiTravelDirectionMap(Map<String, Integer> taxiTravelDirectionMap){
		this.taxiTravelDirectionMap = taxiTravelDirectionMap;
	}
	
	public Map<String, Integer> getTaxiTravelDirectionMap(){
		return taxiTravelDirectionMap;
	}
	
	public void setTaxiEntranceExitNodeIDMap(Map<String, int[]> taxiEntranceExitNodeIDMap){
		this.taxiEntranceExitNodeIDMap = taxiEntranceExitNodeIDMap;
	}
	
	public Map<String, int[]> getTaxiEntranceExitNodeIDMap(){
		return taxiEntranceExitNodeIDMap;
	}
	
	
}
