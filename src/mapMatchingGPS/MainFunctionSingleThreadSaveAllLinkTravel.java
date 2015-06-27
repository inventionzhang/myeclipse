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
	 * 单线程：保存所有路段行程时间到Txt文件
	 */
	public void singleThreadSaveAllLinkTravelTimeToTxt() {
		try {		
			double totalStartTime = System.nanoTime();			
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");//获得时间区间
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");//文件夹
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap = new HashMap<Integer, ArrayList<double[]>>();
			MainFunctionOptimize.obtainAllLinkBoundingRectangle(polylineCollArrayList, allLinkBoundingRectangleMap);
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//分析开始时间
			String endTimeStr = taxiGPSTimeIntervalColl[1];//分析结束时间
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				AssistFunction assistFunction = new AssistFunction();
				double systemStartTime = System.nanoTime();	
				int timeInterval = 6 * 3600;//统计每隔6小时的路段出租车通行时间
				String []tempArrayStr = subStartTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				String []tempDateArrayStr = dateStr.split("-");
				String fileFolderNameStr = tempDateArrayStr[0] + tempDateArrayStr[1] + tempDateArrayStr[2];
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				//分4次计算每天的出租车数据
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
						System.out.print("计算路段通行时间：" + k + ":" + (polylineCollArrayList.size() - 1) + '\n');
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
						//获得满足时空约束条件的数据
						System.out.print("开始读数据库：" + '\n');
						double startReadDatabase = System.nanoTime();
						DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, subStartTimeStr, subEndTimeStr, minLog, minLat, maxLog, maxLat);
						double endReadDatabase = System.nanoTime();
						int dataCount = taxiGPSArrayList.size();
						System.out.print("结束读数据库：" + "获得数据量：" + dataCount + '\n');
						double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
						System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
						System.out.print("获取载客数据GPS点：" + '\n');
						ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
						DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList); 					
						Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
						assistFunction.sortTaxiAccordID(carryPassengerGPSDataArrayList, taxiSortMap);
						ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();
						assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
				    			PubParameter.sampleThreshold, PubParameter.expandTime, taxiTravelTimeArrayList);
						for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
		    	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
		    	    		String taxiID = taxiTravelTime.getTaxiID();//出租车ID
		    	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//开始进入路段时间
		    	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//开始进入路段时间对应的路段通行时间
		    	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//开始进入路段时间对应路段上的GPS点
		    	    		Map<String, Double> taxiMeanSpeeMap = taxiTravelTime.getTaxiMeanSpeedMap();//平均速度
		    	    		Map<String, Integer> taxiTravelDirectionMap = taxiTravelTime.getTaxiTravelDirectionMap();//出租车通行方向与路段方向关系
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
					System.out.print("开始保存数据" + '\n');
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					MainFunctionOptimize.writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("数据保存成功" + '\n'); 
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
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
	    	System.out.print("程序总运行时间：" + totalProcessTime + "s" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
}
