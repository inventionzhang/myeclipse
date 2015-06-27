package latentDirichletAllocation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.esri.arcgis.system.Array;
import com.ibm.icu.text.BreakDictionary;
import com.sun.org.apache.bcel.internal.generic.NEW;

import entity.retuNode;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import mapMatchingGPS.CorrectedNode;
import mapMatchingGPS.DatabaseFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchNode;
import mapMatchingGPS.ReturnGPSAndPath;
import mapMatchingGPS.TaxiGPS;

public class LDAAssistFunction {
	 
	//路网范围：最小经纬度与最大经纬度
	static int expandTime = 300;//停留点扩展时间 5min
	public static double minLBRoadNetwork[] = new double[2];
	public static double maxLBRoadNetwork[] = new double[2];
	public static void obtainRoadNetworkRange(ArrayList<MapMatchNode> juncCollArrayList) {
		// TODO Auto-generated method stub
		try {
			double minL = juncCollArrayList.get(0).x;
			double minB = juncCollArrayList.get(0).y;
			double maxL = juncCollArrayList.get(0).x;
			double maxB = juncCollArrayList.get(0).y;
			for (int i = 0; i < juncCollArrayList.size(); i++) {
				MapMatchNode tNode = juncCollArrayList.get(i);
				if (minL > tNode.x) {
					minL = tNode.x;
				}
				if (minB > tNode.y) {
					minB = tNode.y;
				}
				if (maxL < tNode.x) {
					maxL = tNode.x;
				}
				if (maxB < tNode.y) {
					maxB = tNode.y;
				}
			}
			minLBRoadNetwork[0] = minL;
			minLBRoadNetwork[1] = minB;
			maxLBRoadNetwork[0] = maxL;
			maxLBRoadNetwork[1] = maxB;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 获得出租车上下客停留点
	 * @param taxiArrayList
	 * @param arrestGPSArrayList
	 */
	public void obtainTaxiPassengerArrestGPSPoint(ArrayList<TaxiGPS> taxiArrayList, ArrayList<TaxiGPS> arrestGPSArrayList){
		try {
			boolean bFirst = true;
			
			TaxiGPS LatentGPS = taxiArrayList.get(0);
			
			for (int i = 0; i < taxiArrayList.size(); i++) {
				
				TaxiGPS currentTaxiGPS = taxiArrayList.get(i);
				
				int curStatus = currentTaxiGPS.getStatus();
				
				//如果当前点不是空载或者载客状态, 直接pass掉
				if ( curStatus != PubParameter.vacant && curStatus != PubParameter.occupied)
				{continue;}
				
				if (bFirst == true)
				{
					 LatentGPS = currentTaxiGPS;
					 bFirst = false;
					 continue;
				}
								
				int changedStatus = obtainChangedStatus(LatentGPS, currentTaxiGPS);//nextTaxiGPS);
				
				//nextTaxiGPS 下客点
				if (changedStatus == 0 && isTaxiGPSNodeInNetworkRange(minLBRoadNetwork, maxLBRoadNetwork, currentTaxiGPS)) {
					currentTaxiGPS.setChangedStatus(changedStatus);
					arrestGPSArrayList.add(currentTaxiGPS);
				}
				
				//nextTaxiGPS 上客点
				if (changedStatus == 1 && isTaxiGPSNodeInNetworkRange(minLBRoadNetwork, maxLBRoadNetwork, currentTaxiGPS)) {
					currentTaxiGPS.setChangedStatus(changedStatus);
					arrestGPSArrayList.add(currentTaxiGPS);
				}		
				
				LatentGPS = currentTaxiGPS;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 判断currentTaxiGPS是否为停留点:速度为零的点
	 * @param currentTaxiGPS
	 * @param nextTaxiGPS
	 * @return 停留点：true；非停留点：false。
	 */
	public boolean isStaticGPSPoint(TaxiGPS currentTaxiGPS, TaxiGPS nextTaxiGPS) {
	    boolean isStaticGPSPoint = false;
		try {
			double currentTaxiLongitude = currentTaxiGPS.getLongitude();
			double currentTaxiLatitude = currentTaxiGPS.getLatitude();
			double currentTaxiSpeed = currentTaxiGPS.getSpeed();
			double nextTaxiLongitude = nextTaxiGPS.getLongitude();
			double nextTaxiLatitude = nextTaxiGPS.getLatitude();
			double nextTaxiSpeed = nextTaxiGPS.getSpeed();
			double deltLongitude = Math.abs(currentTaxiLongitude - nextTaxiLongitude);
			double deltLatitude = Math.abs(currentTaxiLatitude - nextTaxiLatitude);
			if (currentTaxiSpeed < PubParameter.zeroSpeedThreshold) {					
				isStaticGPSPoint = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isStaticGPSPoint;
	}
	
	/**
	 * 判断GPS点是否在minLB,maxLB范围内
	 * @param minLB
	 * @param maxLB
	 * @param taxiGPS
	 * @return
	 */
	public boolean isTaxiGPSNodeInNetworkRange(double minLB[], double maxLB[], TaxiGPS taxiGPS) {
		boolean isTaxiGPSNodeInNetworkRange = false;
		try {
			double longitude = taxiGPS.getLongitude();
			double latitude = taxiGPS.getLatitude();
			if (longitude > minLB[0] && longitude < maxLB[0] && latitude > minLB[1] && latitude < maxLB[1]) {
				isTaxiGPSNodeInNetworkRange = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isTaxiGPSNodeInNetworkRange;
	}
	
	/**
	 * 若当前点为非载客，下一点为载客，则该点为上客点；或者，上一点为非载客，当前点为载客，则该点为载客点
	 * @param previousTaxiGPS 
	 * @param currentTaxiGPS
	 * @param nextTaxiGPS
	 * @return nextTaxiGPS的转换状态 0：下客点；1：上客点；-1：载客状态不转换
	 */
	public int obtainChangedStatus(TaxiGPS currentTaxiGPS, TaxiGPS nextTaxiGPS) {
	    int changedStatus = -1;
		try {
			int currentTaxiStatus = currentTaxiGPS.getStatus();
			int nextTaxiStatus = nextTaxiGPS.getStatus();
			//boolean flag = false;
			
			//下客点
			if (currentTaxiStatus == PubParameter.occupied && nextTaxiStatus == PubParameter.vacant) {	
				//flag = true;
				changedStatus = 0;
			}
			//上客点
			if (currentTaxiStatus == PubParameter.vacant && nextTaxiStatus == PubParameter.occupied) {			
				changedStatus = 1;
			}
			
			
//			//下客点
//			if (previousTaxiStatus == PubParameter.carrayPassenger && currentTaxiStatus == PubParameter.nonCarrayPassenger ||
//					currentTaxiStatus == PubParameter.carrayPassenger && nextTaxiStatus == PubParameter.nonCarrayPassenger &&
//					!PubClass.isTheSameTaxiGPS(currentTaxiGPS, nextTaxiGPS)) {	
//				flag = true;
//				changedStatus = 0;
//			}
//			//上客点   同一个点既可能是下客点、也可能是上客点
//			if (currentTaxiStatus == PubParameter.nonCarrayPassenger && nextTaxiStatus == PubParameter.carrayPassenger ||
//					previousTaxiStatus == PubParameter.nonCarrayPassenger && currentTaxiStatus == PubParameter.carrayPassenger &&
//					!PubClass.isTheSameTaxiGPS(currentTaxiGPS, nextTaxiGPS)) {			
//				changedStatus = 1;
//			}			
								
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return changedStatus;
	}
	

	/**
	 * 停留点地图匹配:获得停留点所属路段名称
	 * 以停留点为中心，时间前后扩展5min，获得停留点所属路段ID
	 * @param taxiArrayList
	 * @param processTaxiArrayList	原始GPS点进行匹配后的GPS点
	 */
	public void arrestGPSPointMapMatch(ArrayList<TaxiGPS> taxiArrayList, ArrayList<TaxiGPS> processTaxiArrayList) {
		try {
			for (int i = 0; i < taxiArrayList.size(); i++) {
				TaxiGPS taxiGPS = taxiArrayList.get(i);
				String timeString = taxiGPS.getLocalTime();
				String taxiIDString = taxiGPS.getTargetID();
				TaxiGPS tempTaxiGPS = new TaxiGPS();
				tempTaxiGPS.setLongitude(taxiGPS.getLongitude());
				tempTaxiGPS.setLatitude(taxiGPS.getLatitude());
				String startTimeString = "";
				String endTimeString = "";
				startTimeString = PubClass.obtainStartTimeAccordEndTime(timeString, expandTime);
				endTimeString = PubClass.obtainEndTimeAccordStartTime(timeString, expandTime);
				ArrayList<TaxiGPS> tempTaxiArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataFromDatabase(tempTaxiArrayList, taxiIDString, startTimeString, endTimeString);
				Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
				taxiTrackMap.put(1, tempTaxiArrayList);
				ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
				ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
				ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();
				MapMatchAlgorithm.coordinateCorr(taxiTrackMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList, GPSCorrectArrayList);
				boolean flag = false;
				for (int j = 0; j < correctedOriginalTaxiTrackArrayList.size(); j++) {
					TaxiGPS correctTaxiGPS = correctedOriginalTaxiTrackArrayList.get(j);					
					if (PubClass.isTheSameTaxiGPS(tempTaxiGPS, correctTaxiGPS)) {
						correctTaxiGPS.setChangedStatus(taxiGPS.getChangedStatus());
						correctTaxiGPS.setLocalTime(timeString);
						correctTaxiGPS.setStatus(taxiGPS.getStatus());
						correctTaxiGPS.setSpeed(taxiGPS.getSpeed());
						correctTaxiGPS.setHeading(taxiGPS.getHeading());
						processTaxiArrayList.add(correctTaxiGPS);
						flag = true;
						break;
					}
				}
				if (!flag) {
					processTaxiArrayList.add(tempTaxiGPS);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 判断ID是否在arraylist中：
	 * @param targetIDString
	 * @param tArrayList
	 * @return
	 */
	public boolean isIDInArraylist(String targetIDString, ArrayList<String> tArrayList) {
		boolean isIDIn = false;
		try {
			for (int i = 0; i < tArrayList.size(); i++) {
				String tempIDString = tArrayList.get(i);
				if (targetIDString.equals(tempIDString)) {
					isIDIn = true;
					break;
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isIDIn;
	}
	

	
}
