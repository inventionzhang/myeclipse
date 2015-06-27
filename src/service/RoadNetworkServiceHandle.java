package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import mapMatchingGPS.CorrectedNode;
import mapMatchingGPS.MainFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.ReturnGPSAndPath;
import mapMatchingGPS.ReturnLinkTravelTime;
import mapMatchingGPS.ReturnMatchNode;
import mapMatchingGPS.TaxiGPS;

import association.AssociationNetwork;

import com.esri.arcgis.interop.AutomationException;

import service.DBFactory;
import webService.CoordinateCorr;
import implement.RoadNetworkAnalysisImpl;
import entity.returnResult;
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
	
	//·���г�ʱ��ͳ�Ʒ���
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatistics(int linkID, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.linkTravelTimeStatisticsAll(linkID, startTimeStr, endTimeStr);
	}
	
	/*·���ؿͳ�����ͨ��ʱ��ͳ��*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassenger(int targetLinkID, String startTimeStr, String endTimeStr){
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		return mapMatchAlgorithm.linkTravelTimeStatisticsCarryPassenger(targetLinkID, startTimeStr, endTimeStr);
	}
	
	/*��������*/
	public ArrayList<ArrayList<double[]>> cameraAssociate(){
		AssociationNetwork associationNetwork = new AssociationNetwork();
		return associationNetwork.cameraAssociate();		
	}
	
	
	
}
