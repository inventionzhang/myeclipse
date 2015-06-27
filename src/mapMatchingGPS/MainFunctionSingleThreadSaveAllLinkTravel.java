package mapMatchingGPS;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.esri.arcgis.interop.AutomationException;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;
import entity.PropertiesUtilJAR;
import entity.PropertiesUtil;

public class MainFunctionSingleThreadSaveAllLinkTravel {

	public static void main(String[] args) throws AutomationException, IOException{
		new MainFunctionSingleThreadSaveAllLinkTravel().singleThreadSaveAllLinkTravelTimeToTxt();
		
	}
	
	/**
	 * ���̣߳���������·���г�ʱ�䵽Txt�ļ�
	 */
	public void singleThreadSaveAllLinkTravelTimeToTxt() {
		try {		
			double totalStartTime = System.nanoTime();			
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");//���ʱ������
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");//�ļ���
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap = new HashMap<Integer, ArrayList<double[]>>();
			MainFunctionOptimize.obtainAllLinkBoundingRectangle(polylineCollArrayList, allLinkBoundingRectangleMap);
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//������ʼʱ��
			String endTimeStr = taxiGPSTimeIntervalColl[1];//��������ʱ��
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				AssistFunction assistFunction = new AssistFunction();
				double systemStartTime = System.nanoTime();	
				int timeInterval = 6 * 3600;//ͳ��ÿ��6Сʱ��·�γ��⳵ͨ��ʱ��
				String []tempArrayStr = subStartTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				String []tempDateArrayStr = dateStr.split("-");
				String fileFolderNameStr = tempDateArrayStr[0] + tempDateArrayStr[1] + tempDateArrayStr[2];
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				//��4�μ���ÿ��ĳ��⳵����
				for (int count = 0; count < 4; count++) {		
					Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();				
					if (count != 0) {
						subStartTimeStr = subEndTimeStr;
						endTimeArray = new String[1];
						PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
						subEndTimeStr = endTimeArray[0];
					}						
					ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
					for (int k = 0; k < polylineCollArrayList.size(); k++) {
						MapMatchEdge targetEdge = polylineCollArrayList.get(k);
						int targetLinkID = targetEdge.getEdgeID();
						System.out.print("����·��ͨ��ʱ�䣺" + k + ":" + (polylineCollArrayList.size() - 1) + '\n');
						linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();						
						ArrayList<double[]> linkBoundingRectangleArrayList = new ArrayList<double[]>();
						linkBoundingRectangleArrayList = MainFunctionOptimize.obtainBoundingRectangleAccordLinkID(targetLinkID, allLinkBoundingRectangleMap);
						double[]leftDownLB = linkBoundingRectangleArrayList.get(0);
						double[]rightTopLB = linkBoundingRectangleArrayList.get(1);
						double minLog = leftDownLB[0];
						double minLat = leftDownLB[1];
						double maxLog = rightTopLB[0];
						double maxLat = rightTopLB[1];  			
						ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();  
						//�������ʱ��Լ������������
						System.out.print("��ʼ�����ݿ⣺" + '\n');
						double startReadDatabase = System.nanoTime();
						DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, subStartTimeStr, subEndTimeStr, minLog, minLat, maxLog, maxLat);
						double endReadDatabase = System.nanoTime();
						int dataCount = taxiGPSArrayList.size();
						System.out.print("���������ݿ⣺" + "�����������" + dataCount + '\n');
						double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
						System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
						System.out.print("��ȡ�ؿ�����GPS�㣺" + '\n');
						ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
						DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList); 					
						Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
						assistFunction.sortTaxiAccordID(carryPassengerGPSDataArrayList, taxiSortMap);
						ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();
						assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
				    			PubParameter.sampleThreshold, PubParameter.expandTime, taxiTravelTimeArrayList);
						for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
		    	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
		    	    		String taxiID = taxiTravelTime.getTaxiID();//���⳵ID
		    	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//��ʼ����·��ʱ��
		    	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//��ʼ����·��ʱ���Ӧ��·��ͨ��ʱ��
		    	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//��ʼ����·��ʱ���Ӧ·���ϵ�GPS��
		    	    		Map<String, Double> taxiMeanSpeeMap = taxiTravelTime.getTaxiMeanSpeedMap();//ƽ���ٶ�
		    	    		Map<String, Integer> taxiTravelDirectionMap = taxiTravelTime.getTaxiTravelDirectionMap();//���⳵ͨ�з�����·�η����ϵ
		    	    		for (int j = 0; j < startTravelTimeArraylist.size(); j++) {
								String startTravelTimeStr = startTravelTimeArraylist.get(j);
								double travelTime = travelTimeMap.get(startTravelTimeStr);
								double taxiMeanSpeed = taxiMeanSpeeMap.get(startTravelTimeStr);
								ArrayList<MapMatchNode> taxiTraveGPSlArrayList = GPSTravelMap.get(startTravelTimeStr);
								int taxiTravelDirection = taxiTravelDirectionMap.get(startTravelTimeStr);
								ReturnLinkTravelTime returnLinkTravelTime = new ReturnLinkTravelTime();
								returnLinkTravelTime.setLinkID(targetLinkID);
			    	    		returnLinkTravelTime.setTaxiID(taxiID);
			    	    		returnLinkTravelTime.setStartTravelTime(startTravelTimeStr);
			    	    		returnLinkTravelTime.setTravelTime(travelTime);
			    	    		returnLinkTravelTime.setTaxiMeanSpeed(taxiMeanSpeed);
			    	    		returnLinkTravelTime.setTaxiTravelDirection(taxiTravelDirection);
			    	    		returnLinkTravelTime.setTaxiLinkTravelArrayList(taxiTraveGPSlArrayList);
			    	    		linkTravelTimeArrayList.add(returnLinkTravelTime);	
							}	
						}
						allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);	
					}
					if (allLinkTravelTimeMap.size() == 0 || allLinkTravelTimeMap == null) {
						System.out.print("");
					}
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("��ʼ��������" + '\n');
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					MainFunctionOptimize.writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("���ݱ���ɹ�" + '\n'); 
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
			    	allLinkTravelTimeMap = null;
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];
				assistFunction = null;
			}
			double totalEndTime = System.nanoTime();
	    	double totalProcessTime = (totalEndTime - totalStartTime)/Math.pow(10, 9);
	    	System.out.print("����������ʱ�䣺" + totalProcessTime + "s" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
}
