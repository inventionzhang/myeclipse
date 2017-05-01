package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;

import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;


public class ThreadPoolObtainEligibleGPSPoint implements Runnable, Serializable {

	private int threadID;
	private MapMatchEdge targetEdge;
	private MapMatchNode centerNode;
	private ArrayList<TaxiGPS> taxiGPSArrayList;//传入的GPS数据
	private ArrayList<TaxiGPS> eligibleGPSArrayList;//计算后符合条件的GPS点
	private double radius;
	
	public void setThreadID(int threadID){
		this.threadID = threadID;
	}
	
	public int getThreadID(){
		return threadID;
	}
	
	public void setTargetEdge(MapMatchEdge targetEdge){
		this.targetEdge = targetEdge;
	}
	
	public MapMatchEdge getTargetEdge(){
		return targetEdge;
	}
	
	public void setTaxiGPSArrayList(ArrayList<TaxiGPS> taxiGPSArrayList){
		this.taxiGPSArrayList = taxiGPSArrayList;
	}
	
	public ArrayList<TaxiGPS> getTaxiGPSArrayList(){
		return taxiGPSArrayList;
	}
	
	public void setEligibleGPSArrayList(ArrayList<TaxiGPS> eligibleGPSArrayList){
		this.eligibleGPSArrayList = eligibleGPSArrayList;
	}
	
	public ArrayList<TaxiGPS> getEligibleGPSArrayList(){
		return eligibleGPSArrayList;
	}
	
		
	public void run()
	{
		eligibleTaxiGPSPointToArc(threadID, targetEdge, taxiGPSArrayList, eligibleGPSArrayList);
	}
	
	public void eligibleTaxiGPSPointToArc(int threadID, MapMatchEdge targetEdge, ArrayList<TaxiGPS> taxiGPSArrayList,
			ArrayList<TaxiGPS> eligibleGPSArrayList){
		try {
			System.out.print("线程：" + threadID + "：开始运行" + '\n');
			int taxiGPSIndexCount = taxiGPSArrayList.size();
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				System.out.print("线程：" + threadID + "：TaxiGPS点" + i + "过滤:" + taxiGPSIndexCount + '\n');
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				MapMatchNode tNode = new MapMatchNode();
				tNode.setX(taxiGPS.getLongitude());
				tNode.setY(taxiGPS.getLatitude());
				if (PubClass.distancePointToArc(targetEdge, tNode) <= 2 * PubParameter.radius) {
					eligibleGPSArrayList.add(taxiGPS);
				}
			}
			System.out.print("线程：" + threadID + "：运行结束" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
	
}
