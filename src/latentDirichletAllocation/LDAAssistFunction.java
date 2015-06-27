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
	 
	//·����Χ����С��γ�������γ��
	static int expandTime = 300;//ͣ������չʱ�� 5min
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
	 * ��ó��⳵���¿�ͣ����
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
				
				//�����ǰ�㲻�ǿ��ػ����ؿ�״̬, ֱ��pass��
				if ( curStatus != PubParameter.vacant && curStatus != PubParameter.occupied)
				{continue;}
				
				if (bFirst == true)
				{
					 LatentGPS = currentTaxiGPS;
					 bFirst = false;
					 continue;
				}
								
				int changedStatus = obtainChangedStatus(LatentGPS, currentTaxiGPS);//nextTaxiGPS);
				
				//nextTaxiGPS �¿͵�
				if (changedStatus == 0 && isTaxiGPSNodeInNetworkRange(minLBRoadNetwork, maxLBRoadNetwork, currentTaxiGPS)) {
					currentTaxiGPS.setChangedStatus(changedStatus);
					arrestGPSArrayList.add(currentTaxiGPS);
				}
				
				//nextTaxiGPS �Ͽ͵�
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
	 * �ж�currentTaxiGPS�Ƿ�Ϊͣ����:�ٶ�Ϊ��ĵ�
	 * @param currentTaxiGPS
	 * @param nextTaxiGPS
	 * @return ͣ���㣺true����ͣ���㣺false��
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
	 * �ж�GPS���Ƿ���minLB,maxLB��Χ��
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
	 * ����ǰ��Ϊ���ؿͣ���һ��Ϊ�ؿͣ���õ�Ϊ�Ͽ͵㣻���ߣ���һ��Ϊ���ؿͣ���ǰ��Ϊ�ؿͣ���õ�Ϊ�ؿ͵�
	 * @param previousTaxiGPS 
	 * @param currentTaxiGPS
	 * @param nextTaxiGPS
	 * @return nextTaxiGPS��ת��״̬ 0���¿͵㣻1���Ͽ͵㣻-1���ؿ�״̬��ת��
	 */
	public int obtainChangedStatus(TaxiGPS currentTaxiGPS, TaxiGPS nextTaxiGPS) {
	    int changedStatus = -1;
		try {
			int currentTaxiStatus = currentTaxiGPS.getStatus();
			int nextTaxiStatus = nextTaxiGPS.getStatus();
			//boolean flag = false;
			
			//�¿͵�
			if (currentTaxiStatus == PubParameter.occupied && nextTaxiStatus == PubParameter.vacant) {	
				//flag = true;
				changedStatus = 0;
			}
			//�Ͽ͵�
			if (currentTaxiStatus == PubParameter.vacant && nextTaxiStatus == PubParameter.occupied) {			
				changedStatus = 1;
			}
			
			
//			//�¿͵�
//			if (previousTaxiStatus == PubParameter.carrayPassenger && currentTaxiStatus == PubParameter.nonCarrayPassenger ||
//					currentTaxiStatus == PubParameter.carrayPassenger && nextTaxiStatus == PubParameter.nonCarrayPassenger &&
//					!PubClass.isTheSameTaxiGPS(currentTaxiGPS, nextTaxiGPS)) {	
//				flag = true;
//				changedStatus = 0;
//			}
//			//�Ͽ͵�   ͬһ����ȿ������¿͵㡢Ҳ�������Ͽ͵�
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
	 * ͣ�����ͼƥ��:���ͣ��������·������
	 * ��ͣ����Ϊ���ģ�ʱ��ǰ����չ5min�����ͣ��������·��ID
	 * @param taxiArrayList
	 * @param processTaxiArrayList	ԭʼGPS�����ƥ����GPS��
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
	 * �ж�ID�Ƿ���arraylist�У�
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
