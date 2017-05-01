package org.lmars.network.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.lmars.network.association.AssociationNetwork;
import org.lmars.network.entity.returnResult;
import org.lmars.network.implement.RoadNetworkAnalysisImpl;
import org.lmars.network.mapMatchingGPS.CorrectedNode;
import org.lmars.network.mapMatchingGPS.MainFunction;
import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.ReturnGPSAndPath;
import org.lmars.network.mapMatchingGPS.ReturnLinkTravelTime;
import org.lmars.network.mapMatchingGPS.ReturnMatchNode;
import org.lmars.network.mapMatchingGPS.TaxiGPS;
import org.lmars.network.service.DBFactory;
import org.lmars.network.webService.CoordinateCorr;



import com.esri.arcgis.interop.AutomationException;

public class RoadNetworkServiceHandle {
	
	public  returnResult ljfx(String pcsName,double jjL,double jjB, double speed,double time) throws AutomationException, IOException
	{
		RoadNetworkAnalysisImpl roadNetworkAnaly = new RoadNetworkAnalysisImpl();
		return roadNetworkAnaly.ljfx(pcsName,jjL,jjB,speed,time);
	}
	
	public  returnResult shortestPath(String pcsName,double jjL, double jjB, double jcL, double jcB)
	{
		RoadNetworkAnalysisImpl roadNetworkAnaly = new RoadNetworkAnalysisImpl();
		return roadNetworkAnaly.shortestPath(pcsName, jjL, jjB, jcL, jcB);
	}
	
	public ReturnGPSAndPath obtainCarrypassGPSAndMatchPath(String targetIDStr, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.obtainCarrypassGPSAndMatchPath(targetIDStr, startTimeStr, endTimeStr);
	}
	
	public ReturnGPSAndPath obtainAllGPSAndMatchPath(String targetIDStr, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.obtainAllGPSAndMatchPath(targetIDStr, startTimeStr, endTimeStr);
	}
	
	public CorrectedNode[] obtainTaxiGPSDataAndCorrection(String targetIDStr, String startTimeStr, String endTimeStr){
		CoordinateCorr correctService = new CoordinateCorr();
		return correctService.obtainTaxiGPSDataAndCorrection(targetIDStr, startTimeStr, endTimeStr);
	}
	
	//路段行程时间统计分析
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatistics(int linkID, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.linkTravelTimeStatisticsAll(linkID, startTimeStr, endTimeStr);
	}
	
	/*路段载客车辆的通行时间统计*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassenger(int targetLinkID, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.linkTravelTimeStatisticsCarryPassenger(targetLinkID, startTimeStr, endTimeStr);
	}
	
	/*关联网络*/
	public ArrayList<ArrayList<double[]>> cameraAssociate(){
		AssociationNetwork associationNetwork = new AssociationNetwork();
		return associationNetwork.cameraAssociate();		
	}
	
	
	
}
