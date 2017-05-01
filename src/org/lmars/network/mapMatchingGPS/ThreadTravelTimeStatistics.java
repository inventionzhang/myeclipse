package org.lmars.network.mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** taxiSortMap：按ID进行归类的出租车信息
 	 * threadID：线程ID
	 * startTimeStr：开始时间
	 * endTimeStr：终止时间
	 * targetLinkID:目标路段ID
	 * targetEdge：目标路段
	 * sampleThreshold:采样阈值（默认60s），用于进行轨迹剖分
	 * expandTime：扩展时间（默认60s）
	 * taxiTravelTimeArrayList:目标路段出租车的通行信息
	 * allTaxiTravelTimeArrayList:目标路段所有出租车的通行信息*/
public class ThreadTravelTimeStatistics extends Thread {
	private int threadID;
	private Map<String, ArrayList<TaxiGPS>> taxiSortMap;
	private String startTimeStr;
	private String endTimeStr;
	private int targetLinkID;
	private MapMatchEdge targetEdge;
	private int sampleThreshold;
	private int expandTime;
//	private ArrayList<TaxiTravelTime> taxiTravelTimeArrayList;
	
	public void setThreadID(int threadID){
		this.threadID = threadID;
	}
	
	public int getThreadID(){
		return threadID;
	}
	
	public void setTaxiSortMap(Map<String, ArrayList<TaxiGPS>> taxiSortMap){
		this.taxiSortMap = taxiSortMap;
	}
	
	public Map<String, ArrayList<TaxiGPS>> getTaxiSortMap(){
		return taxiSortMap;
	}
	
	public void setStartTimeStr(String startTimeStr){
		this.startTimeStr = startTimeStr;
	}
	
	public String getStartTimeStr(){
		return startTimeStr;
	}	
	
	public void setEndTimeStr(String endTimeStr){
		this.endTimeStr = endTimeStr;
	}
	
	public String getEndTimeStr(){
		return endTimeStr;
	}
	
	public void setTargetLinkID(int targetLinkID){
		this.targetLinkID = targetLinkID;
	}
	
	public int getTargetLinkID(){
		return targetLinkID;
	}
	
	public void setTargetEdge(MapMatchEdge targetEdge){
		this.targetEdge = targetEdge;
	}
	
	public MapMatchEdge getTargetEdge(){
		return targetEdge;
	}
	
	public void setSampleThreshold(int sampleThreshold){
		this.sampleThreshold = sampleThreshold;
	}
	
	public int getSampleThreshold(){
		return sampleThreshold;
	}
	
	public void setExpandTime(int expandTime){
		this.expandTime = expandTime;
	}
	
	public int getExpandTime(){
		return expandTime;
	}
	
//	public void setTaxiTravelTimeArrayList(ArrayList<TaxiTravelTime> taxiTravelTimeArrayList){
//		this.taxiTravelTimeArrayList = taxiTravelTimeArrayList;
//	}
//	
//	public ArrayList<TaxiTravelTime>  getTaxiTravelTimeArrayList(){
//		return taxiTravelTimeArrayList;
//	}
	
	public void run()
	{
		obtainTaxiTravelTime(threadID, taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
    			sampleThreshold, expandTime);
	}
	
	public void obtainTaxiTravelTime(int threadID, Map<String, ArrayList<TaxiGPS>> taxiSortMap, String startTimeStr, String endTimeStr, int targetLinkID,
			MapMatchEdge targetEdge, int sampleThreshold, int expandTime){
		try {
			System.out.print("线程：" + threadID + "：开始运行" + '\n');
			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = MapMatchAlgorithm.allTaxiTravelTimeMap.get(threadID);
			AssistFunction assistFunction = new AssistFunction();
			assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
	    			sampleThreshold, expandTime, taxiTravelTimeArrayList);	
			System.out.print("线程：" + threadID + "：运行结束" + '\n');
//			this.stop();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
}
