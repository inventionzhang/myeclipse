package org.lmars.network.mapMatchingGPS;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lmars.network.association.Camera;
import org.lmars.network.database.ImportDataToMySQL;
import org.lmars.network.implement.SerializationTest;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;
import org.lmars.network.webService.CoordinateCorr;



import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureWorkspace;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;


public class MainFunctionOptimize {
	//MainFunctionOptimize代替MainFunction函数
	//MainFunctionOptimize是对MainFunction的优化
	public static void main(String[] args) throws AutomationException, IOException{
		/*************************************************************
		 * 测试实验
		 * ***********************************************************/
//		String targetIDStr = "18556";
//		String startTimeStr = "20130101083040";
//		String endTimeStr = "20130101083040";
//		CorrectService ser = new CorrectService();
//		ser.obtainTaxiGPSDataAndCorrectionService("", "20130101083040", "20130101083040");
//		MapMatchAlgorithm tempAlgorithm = new MapMatchAlgorithm();
//		tempAlgorithm.linkTravelTimeStatistics(4660, "20130101000000", "20130101010000");
//		String[] paraArray = new String[3];
//		MapMatchAlgorithm.parameterProc(targetIDStr, startTimeStr, endTimeStr, paraArray);
//		testEntityMap();
//		testLineSegInersect();
//		testStartEndTime();	
				
		/********************************************
		 * 序列化函数
		 * 序列化路网数据
		 * ******************************************/
		saveSeriseRoadNetwork();//保存序列化路网数据
//		readSeriseRoadNetwork();//读取序列化路网数据
//		saveSeriseLinkTravelTimeSingleThread();//保存序列化通行时间数据单线程
//		singleThreadSaveSeriseLinkTravelTime();//保存目标路段序列化通行时间数据单线程
//		saveSeriseLinkTravelTime();//保存序列化通行时间数据
//		readSeriseLinkTravelTime();//读取序列化通行时间数据
//		saveSeriseTaxiInfos();		
		//序列化摄像头文件
//		saveSerializeCameraData();
//		readSerializeCameraData();
		
		/********************************************
		 * 路段通行时间统计
		 * ******************************************/
		
//		singleThreadSaveTravelTimeToTxtFile(88);//单线程，统计目标路段通行时间，保存在txt文件中
		
//		threadPoolSaveTravelTimeToTxtFileAccordLinkID();//线程池：根据路段ID集合求路段通行时间
		//多线程运行一段时间会死掉，还是有问题
		//用此主函数mainFunctionCopy代替mainFunction函数
		threadPoolSaveTravelTimeToTxtFile();//线程池：统计所有路段通行时间,保存在txt文件中	
//		threadPoolSaveTravelTimeToSerializeFile();//行程池：统计所有路段通行时间,保存在序列文件中	
//		System.exit(0);//是正常退出程序
		
		/*******************************************
		 * GPS数据模拟器
		 * *****************************************/
//		readSeriseTaxiInfos();
//		for (int i = 0; i < 2; i++) {
//			taxiGPSSimulator();
//			i = 0;
//		}
		/*******************************************
		 *实验区域GPS纠正数据写入txt文件
		 * *****************************************/
//		saveTaxiInfosToText();
		System.out.print("done!");
	}
	
	/***********************************************************
	 * 路段行程时间统计
	 ***********************************************************/
	
	/**
	 * 统计所有路段通行时间：保存为文本文件
	 * 获得所有路段一定时间范围内的通行时间
	 * 每天分四次统计行程时间，每次统计所有路段，因此每一个路段要遍历多次
	 * 这个算法在获得link的外包矩形范围时经常出现Nan错误，可能是link遍历次数太多的缘故
	 */
	public static void threadPoolSaveTravelTimeToTxtFile(){
		try {		
			double totalStartTime = System.nanoTime();
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");//获得时间区间
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");//文件夹
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap = new HashMap<Integer, ArrayList<double[]>>();
			obtainAllLinkBoundingRectangle(polylineCollArrayList, allLinkBoundingRectangleMap);
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//分析开始时间
			String endTimeStr = taxiGPSTimeIntervalColl[1];//分析结束时间
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
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
					threadPoolObtainAllTravelTimeAccordTime(subStartTimeStr, subEndTimeStr, allLinkTravelTimeMap, allLinkBoundingRectangleMap);
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("开始保存数据" + '\n');
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("数据保存成功" + '\n'); 
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];
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
	
	
	
	/**
	 * 统计某一路段通行时间：保存为文本文件
	 * 根据路段ID、时间范围（一天时间）获得特定路段的通行时间
	 * @param targetLinkID	路段ID
	 */
	public static void threadPoolSaveTravelTimeToTxtFile(int targetLinkID, Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){
		try {		
			double totalStartTime = System.nanoTime();
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;				
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");			
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//分析开始时间
			String endTimeStr = taxiGPSTimeIntervalColl[1];//分析结束时间
			String subStartTimeStr = startTimeStr;
			int threadCount = PubParameter.threadCount;
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, threadCount, 1,  
	                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
	                new ThreadPoolExecutor.DiscardOldestPolicy()); 
			while (!endTimeStr.equals(subStartTimeStr)) {
				double systemStartTime = System.nanoTime();
				int timeInterval = 6 * 3600;//统计每隔6小时的路段出租车通行时间
				String []tempArrayStr = subStartTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				String []tempDateArrayStr = dateStr.split("-");
				String fileFolderNameStr = tempDateArrayStr[0] + tempDateArrayStr[1] + tempDateArrayStr[2] + "-" + String.valueOf(targetLinkID);				
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
					allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
					threadPoolObtainTravelTimeAccordToLinkIDAndTime(targetLinkID, subStartTimeStr, subEndTimeStr, polylineCollArrayList, allLinkTravelTimeMap, allLinkBoundingRectangleMap);
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("开始保存数据" + '\n');
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("数据保存成功：" + '\n');
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("程序运行时间：" + processTime + "s" + '\n');	
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];		
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
	
	public static void writeLinkTravelTimeToText(Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, String directoryPathStr, String fileName){
		try {			
			String outPutPath = directoryPathStr + "\\" + fileName;
			//如果文件夹不存在则创建
			File file = new File(directoryPathStr);		
			if (!file.exists() && !file.isDirectory()) {  
			    file.mkdirs(); 
				writeToText(allLinkTravelTimeMap, outPutPath);				
			}
			else {
				writeToText(allLinkTravelTimeMap, outPutPath);	
			}			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	public static void writeToText(Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, String outPutPath){
		try {
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String TravelTimeDescription = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," +"travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(TravelTimeDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));						
			java.util.Set keySet = allLinkTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<ReturnLinkTravelTime> returnLinkTravelTimeArrayList = allLinkTravelTimeMap.get(key);
        		//按时间排序      		
        		for (int j = 0; j < returnLinkTravelTimeArrayList.size(); j++) {
        			String travelInfoStr = "";
        			ReturnLinkTravelTime returnLinkTravelTime = returnLinkTravelTimeArrayList.get(j);
        			int linkID = returnLinkTravelTime.getLinkID();
        			int enterNodeID = returnLinkTravelTime.getTaxiEnterNodeID();
        			int exitNodeID = returnLinkTravelTime.getTaxiExitNodeID();
        			int travelDirection = returnLinkTravelTime.getTaxiTravelDirection();
        			String taxiID = returnLinkTravelTime.getTaxiID();
        			String startTravelTime = returnLinkTravelTime.getStartTravelTime();
        			double travelTime = returnLinkTravelTime.getTravelTime();
        			double meanSpeed = returnLinkTravelTime.getTaxiMeanSpeed();
        			travelInfoStr = linkID + "," + enterNodeID + "," + exitNodeID + "," + travelDirection + "," + taxiID + "," + startTravelTime + "," + travelTime + "," + meanSpeed+ "\r\n";
        			if (!travelInfoStr.equals("")) {
        				write = new StringBuffer();		
        				write.append(travelInfoStr);
        				bufferStream.write(write.toString().getBytes("UTF-8"));
					}       			
				}
        	}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	/**
	 * 获得路段外包矩形范围
	 * @param polylineCollArrayList
	 * @param allLinkBoundingRectangleMap
	 */
	public static void obtainAllLinkBoundingRectangle(ArrayList<MapMatchEdge> polylineCollArrayList, 
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap) {		
		try {
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				MapMatchEdge targetEdge = polylineCollArrayList.get(i);
				int targetID = targetEdge.getEdgeID();
				double[]leftDownLB = new double[2];
				double[]rightTopLB = new double[2];
				PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
				ArrayList<double[]> tempArraylist = new ArrayList<double[]>();				
				tempArraylist.add(0,leftDownLB);
				tempArraylist.add(1,rightTopLB);
				allLinkBoundingRectangleMap.put(targetID, tempArraylist);	
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 按照时间进行排序
	 * @param returnLinkTravelTimeArrayList
	 * @param processLinkTravelTimeArrayList
	 */
	public static void rankTravleTimeAccordTime(ArrayList<ReturnLinkTravelTime> returnLinkTravelTimeArrayList, ArrayList<ReturnLinkTravelTime> rankLinkTravelTimeArrayList){
		try {
			int count = returnLinkTravelTimeArrayList.size();
			for (int i = 0; i < count; i++) {
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 返回路段的外包矩形
	 * @param linkID
	 * @param allLinkBoundingRectangleMap
	 * @param linkBoundingRectangleArrayList
	 */
	public static ArrayList<double[]> obtainBoundingRectangleAccordLinkID(int linkID, Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap) {
		ArrayList<double[]> linkBoundingRectangleArrayList = new ArrayList<double[]>();
		try {
			Set keySet = allLinkBoundingRectangleMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		if (key == linkID) {
        			linkBoundingRectangleArrayList = allLinkBoundingRectangleMap.get(key);
        			break;
				}      		
        	}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
		return linkBoundingRectangleArrayList;
	}
	
	/**
	 * 根据路段ID、起止时间获得路段的通行时间
	 * @param targetLinkID
	 * @param subStartTimeStr
	 * @param subEndTimeStr
	 * @param polylineCollArrayList
	 * @param allLinkTravelTimeMap
	 */
	public static void threadPoolObtainTravelTimeAccordToLinkIDAndTime(int targetLinkID, String subStartTimeStr, String subEndTimeStr, ArrayList<MapMatchEdge> polylineCollArrayList,
			Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){
		try {
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
			AssistFunction assistFunction = new AssistFunction();
			MapMatchEdge targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//获得目标路段
			ArrayList<double[]> linkBoundingRectangleArrayList = new ArrayList<double[]>();
			linkBoundingRectangleArrayList = obtainBoundingRectangleAccordLinkID(targetLinkID, allLinkBoundingRectangleMap);
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
			int count = taxiGPSArrayList.size();
			System.out.print("结束读数据库：" + "获得数据量：" + count + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
			System.out.print("获取载客数据GPS点：" + '\n');
			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList); 					
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(carryPassengerGPSDataArrayList, taxiSortMap);	
			int taxiTotalCount = taxiSortMap.size();//出租车数目
			int threadCount = PubParameter.threadCount;//线程数目
			int produceTaskSleepTime = 2;      
			double taxiCountEveryThread = (double)taxiTotalCount/threadCount;
			//threadCount个线程进行计算
			//如果小于1，则每个线程计算一辆出租车通行时间,此时线程数小于4
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, threadCount, 1,  
		                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
		                new ThreadPoolExecutor.DiscardOldestPolicy()); 
			if (taxiCountEveryThread < 1) {
				for (int i = 0; i < taxiTotalCount; i++) {
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
		        	while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);      		
		        		Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
		        		tempTaxiMap.put(key, tempTaxiGPSArrayList);
		        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();
		                allTaxiTravelTimeMap.put(i, taxiTravelTimeArrayList);
		                threadPoolProcess(threadPool,allTaxiTravelTimeMap, i, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge); 
		        	}				
				}
			}
			//否则，每个线程计算多辆出租车
			else {
				//如果为整数，即能够被整除
				String taxiCountEveryThreadStr = String.valueOf(taxiCountEveryThread);
				if (PubClass.isInteger(taxiCountEveryThreadStr)) {
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;//临时变量，计算每个线程的出租车数目
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
		        	while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);
		        		tempCount++;
		        		if (tempCount < taxiCountEveryThread ) {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
						}
		        		else {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);	
		        			threadID++;
		        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
			        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
			        		threadPoolProcess(threadPool, allTaxiTravelTimeMap, threadID, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge); 	
		        			tempCount = 0;
		        			tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
						}			        		
		        	}							
				}
				//不为整数
				else {
					int integerPart = PubClass.obtainIntegerPart(taxiCountEveryThreadStr);
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 1;
					int tempCount = 0;
					int tempThreadCount = 1;//临时变量，计算线程数目,必须从1开始
					int taxiCount = 0;//临时变量，每个线程计算的出租车数目
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
					while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);
		        		tempCount++;
		        		taxiCount++;
		        		if (tempThreadCount == threadCount) {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
		        			if (taxiCount == taxiSortMap.size()) {
		        				ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
				        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
				        		threadPoolProcess(threadPool, allTaxiTravelTimeMap,threadID, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge);
				        		System.out.print("");
							}			        			
						}
		        		else {
		        			if (tempCount < integerPart ) {
			        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
							}
			        		else {
			        			tempTaxiMap.put(key, tempTaxiGPSArrayList);			        						        						        			
			        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
				        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
				        		threadPoolProcess(threadPool, allTaxiTravelTimeMap,threadID, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge);
								tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
								tempCount = 0;
								threadID++;	
			        			tempThreadCount++;
							}
						}			        		
					}						
				}
			}		
			threadPool.shutdown(); //关闭后不能加入新线程，队列中的线程则依次执行完
			while (true) {
				if (threadPool.isTerminated()) {
					System.out.print("线程运行结束！" + '\n');
					break;
				}
			}
//			threadPool = null;
			Set keySet = allTaxiTravelTimeMap.entrySet();
			System.out.print("s");
			Iterator iterator = (Iterator) keySet.iterator();
			while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = allTaxiTravelTimeMap.get(key);
        		for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
    	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
    	    		String taxiID = taxiTravelTime.getTaxiID();//出租车ID
    	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//开始进入路段时间
    	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//开始进入路段时间对应的路段通行时间
    	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//开始进入路段时间对应路段上的GPS点
    	    		Map<String, Double> taxiMeanSpeeMap = taxiTravelTime.getTaxiMeanSpeedMap();//平均速度
    	    		Map<String, Integer> taxiTravelDirectionMap = taxiTravelTime.getTaxiTravelDirectionMap();//出租车通行方向与路段方向关系
    	    		Map<String, int[]> taxiEntranceExitNodeIDMap = taxiTravelTime.getTaxiEntranceExitNodeIDMap();
    	    		for (int j = 0; j < startTravelTimeArraylist.size(); j++) {
						String startTravelTimeStr = startTravelTimeArraylist.get(j);
						double travelTime = travelTimeMap.get(startTravelTimeStr);
						double taxiMeanSpeed = taxiMeanSpeeMap.get(startTravelTimeStr);
						ArrayList<MapMatchNode> taxiTraveGPSlArrayList = GPSTravelMap.get(startTravelTimeStr);
						int taxiTravelDirection = taxiTravelDirectionMap.get(startTravelTimeStr);
						int []taxiEnterNodeID = taxiEntranceExitNodeIDMap.get(startTravelTimeStr);
						ReturnLinkTravelTime returnLinkTravelTime = new ReturnLinkTravelTime();
						returnLinkTravelTime.setLinkID(targetLinkID);
	    	    		returnLinkTravelTime.setTaxiID(taxiID);
	    	    		returnLinkTravelTime.setStartTravelTime(startTravelTimeStr);
	    	    		returnLinkTravelTime.setTravelTime(travelTime);
	    	    		returnLinkTravelTime.setTaxiMeanSpeed(taxiMeanSpeed);
	    	    		returnLinkTravelTime.setTaxiTravelDirection(taxiTravelDirection);
	    	    		returnLinkTravelTime.setTaxiEnterNodeID(taxiEnterNodeID[0]);
	    	    		returnLinkTravelTime.setTaxiExitNodeID(taxiEnterNodeID[1]);
	    	    		returnLinkTravelTime.setTaxiLinkTravelArrayList(taxiTraveGPSlArrayList);
	    	    		linkTravelTimeArrayList.add(returnLinkTravelTime);	
					}	
				}	
			}
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 线程池
	 * @param threadPool
	 * @param threadID
	 * @param tempTaxiMap
	 * @param startTimeStr
	 * @param endTimeStr
	 * @param targetLinkID
	 * @param targetEdge
	 */
	public static void threadPoolProcess(ThreadPoolExecutor threadPool, Map<Integer, ArrayList<TaxiTravelTime>> allTaxiTravelTimeMap,
			int threadID, Map<String, ArrayList<TaxiGPS>> tempTaxiMap,
			String startTimeStr, String endTimeStr, int targetLinkID, MapMatchEdge targetEdge){
		try {
			int produceTaskSleepTime = 2;
			ThreadPoolTravelTimeStatistics threadPoolTravelTimeStatistics = new ThreadPoolTravelTimeStatistics();
			threadPoolTravelTimeStatistics.setThreadID(threadID);
			threadPoolTravelTimeStatistics.setTaxiSortMap(tempTaxiMap);
			threadPoolTravelTimeStatistics.setStartTimeStr(startTimeStr);
			threadPoolTravelTimeStatistics.setEndTimeStr(endTimeStr);
			threadPoolTravelTimeStatistics.setTargetLinkID(targetLinkID);
			threadPoolTravelTimeStatistics.setTargetEdge(targetEdge);
			threadPoolTravelTimeStatistics.setSampleThreshold(PubParameter.sampleThreshold);
			threadPoolTravelTimeStatistics.setExpandTime(PubParameter.expandTime);
			threadPoolTravelTimeStatistics.setAllTaxiTravelTimeMap(allTaxiTravelTimeMap);
			Thread thread = new Thread(threadPoolTravelTimeStatistics);
	        threadPool.execute(thread);
//	        Thread.sleep(produceTaskSleepTime); 
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	
	/**
	 * 获得所有路段一定时间范围内的统计行程时间
	 * allLinkTravelTimeMap:统计信息
	 * count：文件编号
	 * startTimeStr:开始时间
	 * @param subStartTimeStr
	 * @param subEndTimeStr
	 * @param allLinkTravelTimeMap
	 * @param allLinkBoundingRectangleMap	路段外包矩形
	 */
	public static void threadPoolObtainAllTravelTimeAccordTime(String subStartTimeStr, String subEndTimeStr, Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap,
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){	
		try {
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
			for (int k = 0; k < polylineCollArrayList.size(); k++) {
				allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
				MapMatchEdge targetEdge = polylineCollArrayList.get(k);
				int targetLinkID = targetEdge.getEdgeID();
				System.out.print("计算路段通行时间：" + k + ":" + (polylineCollArrayList.size() - 1) + '\n');
				threadPoolObtainTravelTimeAccordToLinkIDAndTime(targetLinkID, subStartTimeStr, subEndTimeStr, polylineCollArrayList, allLinkTravelTimeMap, allLinkBoundingRectangleMap);
				allTaxiTravelTimeMap = null;//释放内存
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}	
	
	/**
	 * 单线程测试：目标路段通行时间保存到txt中
	 * @param targetLinkID
	 */
	public static void singleThreadSaveTravelTimeToTxtFile(int targetLinkID){
		try {		
			double totalStartTime = System.nanoTime();
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
			AssistFunction assistFunction = new AssistFunction();
			MapMatchEdge targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//获得目标路段
			double[]leftDownLB = new double[2];
			double[]rightTopLB = new double[2];
			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");
//			String taxiGPSTimeInterval = "2014-06-03 18:00:00,2014-06-04 00:00:00";
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//分析开始时间
			String endTimeStr = taxiGPSTimeIntervalColl[1];//分析结束时间
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
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
					allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
					singleThreadObtainTravelTimeAccordToLinkIDAndTime(targetLinkID,targetEdge, leftDownLB, rightTopLB, subStartTimeStr, subEndTimeStr, allLinkTravelTimeMap);
					String directoryPathStr = "C:\\travelTimeStatistics\\" + fileFolderNameStr;
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("开始保存数据" + '\n');
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("数据保存成功：" + '\n');
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("程序运行时间：" + processTime + "s" + '\n');	
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];		
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
	
	/**
	 * 单线程测试：根据路段ID、起止时间获得路段的通行时间
	 */
	public static void singleThreadObtainTravelTimeAccordToLinkIDAndTime(int targetLinkID, MapMatchEdge targetEdge, double[]leftDownLB, double[]rightTopLB,
			String subStartTimeStr, String subEndTimeStr, Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap){
		try {
			MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
			AssistFunction assistFunction = new AssistFunction();
			double minLog = leftDownLB[0];
			double minLat = leftDownLB[1];
			double maxLog = rightTopLB[0];
			double maxLat = rightTopLB[1];  			
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();  
			//获得满足时空约束条件的数据
			System.out.print("开始读数据库：" + '\n');
			double startReadDatabase = System.nanoTime();
			if(Double.isNaN(minLog) || minLog == 0){
				System.out.print("error");
			}
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, subStartTimeStr, subEndTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("结束读数据库：" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
			System.out.print("获取载客数据：" + '\n');
			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList);					
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(carryPassengerGPSDataArrayList, taxiSortMap);	
			mapMatchAlgorithm.linkTravelTimeProcessSingleThread(targetLinkID, targetEdge, subStartTimeStr, subEndTimeStr, taxiSortMap,linkTravelTimeArrayList);
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public static void singleThreadSaveSeriseLinkTravelTime(){
		double systemStartTime = System.nanoTime();
		String startTimeStr = "2014-06-01 00:00:00";
		String endTimeStr = "2014-06-02 00:00:00";	
		Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		AssistFunction assistFunction = new AssistFunction();
		for (int i = 0; i < polylineCollArrayList.size(); i++) {
			MapMatchEdge targetEdge = polylineCollArrayList.get(i);
			System.out.print("计算路段通行时间：" + i + ":" + (polylineCollArrayList.size() - 1));
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
			int targetLinkID = targetEdge.getEdgeID();	
			double[]leftDownLB = new double[2];
			double[]rightTopLB = new double[2];
			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
			double minLog = leftDownLB[0];
			double minLat = leftDownLB[1];
			double maxLog = rightTopLB[0];
			double maxLat = rightTopLB[1];  			
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
			//获得满足时空约束条件的数据
			System.out.print("开始读数据库：" + '\n');
			double startReadDatabase = System.nanoTime();
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("结束读数据库：" + '\n');
			double readDatabaseTime = (startReadDatabase - endReadDatabase)/Math.pow(10, 9);
			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点
			//根据ID进行分类
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap); 
			mapMatchAlgorithm.linkTravelTimeProcessSingleThread(targetLinkID, targetEdge, startTimeStr, endTimeStr, taxiSortMap,linkTravelTimeArrayList);
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);			
		}
		String filename = "C:\\allLinkTravelTime20130101.bin";
		System.out.print("开始保存数据" + '\n');
		mapMatchAlgorithm.saveSeriseLinkTravelTime(filename, allLinkTravelTimeMap);
		System.out.print("数据保存成功" + '\n');
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("程序运行时间：" + processTime + "s" + '\n');   	
	}
	
	/**
	 * 保存序列化路段通行时间数据
	 */
	public static void saveSeriseLinkTravelTime(){
		double systemStartTime = System.nanoTime();
		Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		for (int i = 0; i < polylineCollArrayList.size(); i++) {
			MapMatchEdge targetEdge = polylineCollArrayList.get(i);
			System.out.print("计算路段通行时间：" + i + ":" + (polylineCollArrayList.size() - 1));
			int targetLinkID = targetEdge.getEdgeID();
			String startTimeStr = "2013-01-01 00:00:00";
			String endTimeStr = "2013-01-01 01:00:00";					
			AssistFunction assistFunction = new AssistFunction();
			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//获得目标路段
			double[]leftDownLB = new double[2];
			double[]rightTopLB = new double[2];
			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
			double minLog = leftDownLB[0];
			double minLat = leftDownLB[1];
			double maxLog = rightTopLB[0];
			double maxLat = rightTopLB[1];  			
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
			//获得满足时空约束条件的数据
			System.out.print("开始读数据库：" + '\n');
			double startReadDatabase = System.nanoTime();
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("结束读数据库：" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList);
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(carryPassengerGPSDataArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//路段通行时间的计算结果
			mapMatchAlgorithm.linkTravelTimeProcessMultiThread(targetLinkID, startTimeStr, endTimeStr, targetEdge, 
					eliminateZeroSpeedGPSDataArrayList, linkTravelTimeArrayList);
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);
		}
			
		String filename = "C:\\allLinkTravelTime20130101.bin";
		System.out.print("开始保存数据" + '\n');
		mapMatchAlgorithm.saveSeriseLinkTravelTime(filename, allLinkTravelTimeMap);
		System.out.print("数据保存成功" + '\n');
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
	}
	
	/**
	 * 读取序列化路段通行时间数据
	 */
	public static void readSeriseLinkTravelTime(){
		try {
			String fileName = PropertiesUtilJAR.getProperties("travelTime");
			System.out.print("开始读序列化数据" + '\n');	
			MapMatchAlgorithm.instance().readSeriseLinkTravelTime(fileName);
//			MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
//			mapMatchAlgorithm.readSeriseLinkTravelTime(fileName);
			System.out.print("序列化数据读取成功" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	private static void initializeArcGISLicenses(AoInitialize aoInit){
        try{
            if (aoInit.isProductCodeAvailable
                (esriLicenseProductCode.esriLicenseProductCodeBasic) ==
                esriLicenseStatus.esriLicenseAvailable){
                aoInit.initialize
                    (esriLicenseProductCode.esriLicenseProductCodeBasic);
            }
            else if (aoInit.isProductCodeAvailable
                (esriLicenseProductCode.esriLicenseProductCodeBasic) ==
                esriLicenseStatus.esriLicenseAvailable){
                aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic)
                    ;
            }
            else{
                System.err.println(
                    "Engine Runtime or Desktop Basic license not initialized.");
                System.err.println("Exiting application.");
                System.exit( - 1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
	}
	
	/**
	 * 保存序列化摄像头点位数据
	 */
	public static void saveSerializeCameraData(){
		try {
			SerializeFunction serializeFunction = new SerializeFunction();
			String cameraFilename = "C:\\cameraInfos.bin";//序列化文件
			String path = "F:\\faming\\esri\\roadNetwork.gdb";
			String cameraShapeFilename = "江岸视频点位处理数据";
			ArrayList<Camera> cameraInfosArrayList = new ArrayList<Camera>();		
			//Step 1: Initialize the Java Componet Object Model (COM) Interop.
	        EngineInitializer.initializeEngine();
	        //Step 2: Initialize an ArcGIS license.
	        AoInitialize aoInit = new AoInitialize();
	        initializeArcGISLicenses(aoInit);
	        IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
	        IFeatureWorkspace pFeatureWorkspace=(IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			IFeatureClass pointFeatureClass = pFeatureWorkspace.openFeatureClass(cameraShapeFilename);		
		    int juncFeatureCount = pointFeatureClass.featureCount(null);
		    IFeatureCursor juncFeatureCursor=pointFeatureClass.search(null, false);	    
		    for (int i = 0; i < juncFeatureCount; i++) {
		    	IFeature feature=juncFeatureCursor.nextFeature();
		    	IGeometry geometry=feature.getShape();  
	 			IPoint juncPoint=(IPoint)geometry; 			
	 	    	//一定要实例化
	 			Camera camera = new Camera();
	 			camera.setID(feature.getOID());
	 			camera.setLongitude(juncPoint.getX());
	 			camera.setLatitude(juncPoint.getY());	 			
	 			IFields fields=new Fields();
				fields=feature.getFields();
				int FOVFiedIndex = fields.findField("Angle1");
				int directAngleFiedIndex = fields.findField("Angle2");
				int distFiedIndex = fields.findField("Distance2");			
				float tempFOV = (Float)feature.getValue(FOVFiedIndex);//视场角
				double FOV = tempFOV;
				float tempDirectAngle = (Float)feature.getValue(directAngleFiedIndex);//方向角
				double directAngle = tempDirectAngle;
				float tempDist = (Float)feature.getValue(distFiedIndex);
	 			double dist = tempDist;
				camera.setFieldOfView(FOV);
				camera.setAngleDirection(directAngle);
				camera.setAngleDist(dist);	 			
	 			cameraInfosArrayList.add(camera);
	 			System.out.println("读取摄像头:" + i + ":" + juncFeatureCount);
			}  
		    System.out.println("摄像头读取结束" + '\n');
			serializeFunction.saveSeriseCameraData(cameraFilename, cameraInfosArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * 读取序列化摄像头点位数据
	 */
	public static void readSerializeCameraData(){
		String cameraFilename = "C:\\cameraInfos.bin";//序列化文件
		ArrayList<Camera> cameraInfosArrayList = new ArrayList<Camera>();
		SerializeFunction serializeFunction = new SerializeFunction();		
		cameraInfosArrayList = serializeFunction.readSeriseCameraData(cameraFilename);
		System.out.println("摄像头读取结束" + '\n');
	}
	
	/**
	 * 保存序列化TaxiGPS数据
	 */
	public static void saveSeriseTaxiInfos(){
		double systemStartTime = System.nanoTime();
		try {
			String filePath = PropertiesUtilJAR.getProperties("taxiFilefolderName");//存储路径
			ArrayList<String> taxiIDArrayList = new ArrayList<String>();
			String startTimeStr = "2013-01-01 00:00:00";
			String endTimeStr = "2013-01-02 00:00:00";
			//实验区域
			double minLog = 114.282;
			double maxLog = 114.297;
			double minLat = 30.599;
			double maxLat = 30.611;
			SerializeFunction serializeFunction = new SerializeFunction();
			System.out.print("开始获得出租车ID" + '\n');
			DatabaseFunction.obtainTaxiIDAccordTimeSpatialFilte(taxiIDArrayList, startTimeStr, endTimeStr, minLog, maxLog, minLat, maxLat);
			System.out.print("结束获得出租车ID" + '\n');
			String metadataFileName = PropertiesUtilJAR.getProperties("metadataFileName");
			System.out.print("开始保存出租车元数据" + '\n');			
			serializeFunction.saveSeriseTaxiMetadata(metadataFileName, taxiIDArrayList);			
			System.out.print("结束保存出租车元数据" + '\n');
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
				System.out.print("开始获得出租车轨迹数据：" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
				String targetIDStr = taxiIDArrayList.get(i);
				Map<String, ArrayList<TaxiGPS>> taxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();			
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataAccordIDTimeSpatialFilter(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
				taxiInfosMap.put(targetIDStr, taxiGPSArrayList);				
				String filename = filePath + targetIDStr + ".bin";
				System.out.print("开始保存数据" + '\n');
				boolean isOK = serializeFunction.saveSeriseTaxiInfos(filename, taxiInfosMap);
				if (isOK) {
					System.out.print("数据保存成功" + '\n');
				}
				else {
					System.out.print("文件名已存在或数据保存失败！" + '\n');
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
	}
	
	/**
	 * 读取序列化TaxiGPS数据
	 */
	public static void readSeriseTaxiInfos(){
		double systemStartTime = System.nanoTime();
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
    	ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
    	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = MapMatchAlgorithm.instance().allGridIndexVerticesMap;
    	Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = MapMatchAlgorithm.instance().allGridJunctionMap;
    	Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = MapMatchAlgorithm.instance().allGridPolylineMap;
		SerializeFunction serializeFunction = new SerializeFunction();
		try {
			System.out.print("开始读出租车元数据" + '\n');
			ArrayList<String> taxiIDArrayList = SerializeFunction.instance().taxiIDArrayList;
			System.out.print("结束读出租车元数据" + '\n');
			System.out.print("开始读序列化文件：" + '\n');
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
				System.out.print("读序列化文件：" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
				String targetIDStr = taxiIDArrayList.get(i);
				String fileName = PropertiesUtilJAR.getProperties("taxiFilefolderName") + targetIDStr + ".bin";
				Map<String, ArrayList<TaxiGPS>> taxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();
				serializeFunction.readSeriseTaxiInfos(fileName);
			}
			System.out.print("结束读序列化文件：" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
	}
	
	/*保存TaxiGPS数据到txt文件中*/
	public static void saveTaxiInfosToText(){
		double startobtainData = System.nanoTime();
		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
		//时间段设置
		String startTimeStr = "2013-01-07 00:00:00";
		String endTimeStr = "2013-01-08 00:00:00";
		//实验区域
		double minLog = 114.282;
		double maxLog = 114.297;
		double minLat = 30.599;
		double maxLat = 30.611;
		SerializeFunction serializeFunction = new SerializeFunction();
		System.out.print("开始获得出租车ID" + '\n');
		DatabaseFunction.obtainTaxiIDAccordTimeSpatialFilte(taxiIDArrayList, startTimeStr, endTimeStr, minLog, maxLog, minLat, maxLat);
//		String filename = PropertiesUtil.getProperties("metadataFileName");
//		taxiIDArrayList = serializeFunction.readSeriseTaxiMetadataArraylist(filename);
		System.out.print("结束获得出租车ID" + '\n');
		Map<String, ArrayList<TaxiGPS>> allOrigianlGPSMap = new HashMap<String, ArrayList<TaxiGPS>>();//原始GPS点坐标
		Map<String, ArrayList<CorrectedNode>> allCorrectGPSMap = new HashMap<String, ArrayList<CorrectedNode>>();//所有纠正后的GPS点坐标
		for (int i = 0; i < taxiIDArrayList.size(); i++) {
			System.out.print("开始获得出租车轨迹数据：" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
			String targetIDStr = taxiIDArrayList.get(i);
			Map<String, ArrayList<TaxiGPS>> taxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();			
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.obtainGPSDataAccordIDTimeSpatialFilter(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			allOrigianlGPSMap.put(targetIDStr, taxiGPSArrayList);
			Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
			taxiTrackMap.put(1, taxiGPSArrayList);
			ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();
			ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
			ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
			MapMatchAlgorithm.coordinateCorr(taxiTrackMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList,GPSCorrectArrayList);
			allCorrectGPSMap.put(targetIDStr, GPSCorrectArrayList);
		}
		//原始坐标与纠正后坐标分别写入txt文件
		String outPutPath = "C:\\originalAndCorrectGPS20130107.txt";//写入Txt文件名
		System.out.print("开始写入数据：" + '\n');
		writeCorrectedCoordToText(allCorrectGPSMap,outPutPath);
		System.out.print("结束写入数据：" + '\n');
		double endObtainData = System.nanoTime();
		double obtainDataTime = (endObtainData - startobtainData)/Math.pow(10, 9);
		System.out.print("获得数据时间：" + obtainDataTime + "s" + '\n');
	}
	
	/**
	 * 将纠正GPS坐标写入到txt文件中
	 * allCorrectGPSMap：纠正GPS坐标
	 * @param allCorrectGPSMap
	 * @param outPutPath
	 */
	public static void writeCorrectedCoordToText(Map<String, ArrayList<CorrectedNode>> allCorrectGPSMap, String outPutPath){
		try {			
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String GPSDescription = "taxiID" + "," + "linkID" + "," + "time" + "," + "originalLongitude" + "," + "originalLatitude" +
				"," +"correctedLongitude" + "," + "correctedLatitude" + "\r\n";
			write.append(GPSDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));						
			java.util.Set keySet = allCorrectGPSMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		String key = (String)mapEntry.getKey();
        		ArrayList<CorrectedNode> correctedGPSArrayList = allCorrectGPSMap.get(key);
        		for (int j = 0; j < correctedGPSArrayList.size(); j++) {
        			String travelInfoStr = "";
        			CorrectedNode correctedNode = correctedGPSArrayList.get(j);
        			String taxiID = key;
        			int linkID = correctedNode.getTargetEdgeID();//fileGeodatabase中ID与shapefile中ID相差1
        			linkID = linkID - 1;
        			String timeStr = correctedNode.getLocalTime();
        			double originalLongitude = correctedNode.getOriginLongitude();
        			double originalLatitude = correctedNode.getOriginLatitude();
        			double correctedLongitude = correctedNode.getCorrectLongitude();
        			double correctedLatitude = correctedNode.getCorrectLatitude();
        			travelInfoStr = taxiID + "," + linkID + "," + timeStr + "," + originalLongitude + "," + originalLatitude + "," + correctedLongitude +
        				"," + correctedLatitude + "\r\n";
        			write = new StringBuffer();				
    				write.append(travelInfoStr);
    				bufferStream.write(write.toString().getBytes("UTF-8"));
				}
        	}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	/**
	 * GPS数据模拟发送器
	 */
	public static void taxiGPSSimulator(){
		Map<String, ArrayList<TaxiGPS>> allTaxiInfosMap = SerializeFunction.instance().allTaxiInfosMap;
		Simulator.taxiGPSSimulativeGenerator(allTaxiInfosMap);	
	}
	
	public static Map<Integer, ArrayList<TaxiTravelTime>> allTaxiTravelTimeMap = 
		new HashMap<Integer, ArrayList<TaxiTravelTime>>();//所有出租车通行时间,以线程ID为索引
	/****************************************
	 * 统计路段通行时间：保存为序列化文件
	 * 获得所有路段一定时间范围内的通行时间
	 * **************************************/
	public static void threadPoolSaveTravelTimeToSerializeFile(){
		try {
			double systemStartTime = System.nanoTime();		    			
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			int threadCount = PubParameter.threadCount;
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, threadCount, 1,  
	                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
	                new ThreadPoolExecutor.DiscardOldestPolicy());
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap = new HashMap<Integer, ArrayList<double[]>>();
			obtainAllLinkBoundingRectangle(polylineCollArrayList, allLinkBoundingRectangleMap);
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];
			String endTimeStr = taxiGPSTimeIntervalColl[1];
			String subStartTimeStr = startTimeStr;
			int timeInterval = 6 * 3600;//统计每隔6小时的路段出租车通行时间
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
				threadPoolObtainAllTravelTimeAccordTime(subStartTimeStr, subEndTimeStr, allLinkTravelTimeMap, allLinkBoundingRectangleMap);		    	
				MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
				String filename = "C:\\allLinkTravelTime" + count + ".bin";
				System.out.print("开始保存数据" + '\n');
				mapMatchAlgorithm.saveSeriseLinkTravelTime(filename, allLinkTravelTimeMap);
				System.out.print("数据保存成功" + '\n');
				double systemEndTime = System.nanoTime();
		    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
		    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 保存序列化路网数据
	 * 路网数据序列化并进行格网划分
	 */
	public static void saveSeriseRoadNetwork(){
		/*路网数据序列化*/
		String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
		String roadNetworkNamecoll[] = roadNetworkName.split(",");
		String geoDatabaseFilePath = roadNetworkNamecoll[0];
		String juncFileName = roadNetworkNamecoll[1];
		String polylineFileName = roadNetworkNamecoll[2];
		String fileName = roadNetworkNamecoll[3];//保存文件名
		File file = new File(fileName);
		//如果文件存在，则删除，重新构建拓扑，保存序列化文件；否则，直接构建拓扑，保存序列化文件
		if (file.exists()) {
			file.delete();
			MapMatchAlgorithm.instance().createConnSetAndTopolygon(geoDatabaseFilePath, juncFileName, polylineFileName);
			System.out.print("开始保存数据" + '\n');	
			MapMatchAlgorithm.instance().saveRoadFile(fileName);
			System.out.print("数据保存成功" + '\n');
		}
		else {
			MapMatchAlgorithm.instance().createConnSetAndTopolygon(geoDatabaseFilePath, juncFileName, polylineFileName);
			System.out.print("开始保存数据" + '\n');	
			MapMatchAlgorithm.instance().saveRoadFile(fileName);
			System.out.print("数据保存成功" + '\n');
		}
		
	}
	
	/*读取序列化路网数据*/
	public static void readSeriseRoadNetwork(){
		try {
			String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[3];//文件名
			System.out.print("开始读序列化数据" + '\n');	
			MapMatchAlgorithm.instance().readRoadFile(fileName);
			System.out.print("序列化数据读取成功" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
	}
	
	/***********************************************************
	 * 测试函数
	 * *********************************************************
	 */
	/*测试时间计算函数*/
	public static void testStartEndTime(){
		try {
			String startTimeStr = "2013-01-01 00:00:00";
			String endTimeStr = "2013-01-02 00:00:00";
			double minLog = 114.30976998613357;
			double minLat = 30.600517896933237;
			double maxLog = 114.32703396043917;
			double maxLat = 30.611858322152624; 
			AssistFunction assistFunction = new AssistFunction();
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>(); 
			System.out.print("开始读数据库：" + '\n');
			double startReadDatabase = System.nanoTime();
//			assistFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("结束读数据库：" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				TaxiGPS taxiGPS = new TaxiGPS();
				taxiGPS = taxiGPSArrayList.get(i);
				String timeStr = taxiGPS.getLocalTime();
				String[] endTimeArray = new String[1];
				String[] startTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(timeStr, 60, endTimeArray);
//				System.out.print(endTimeArray[0] + "\n");
				PubClass.obtainStartTimeAccordEndTime(timeStr, 60, startTimeArray);
//				System.out.print(startTimeArray[0] + "\n");
				double timeInterval = PubClass.obtainTimeInterval(startTimeArray[0], endTimeArray[0]);
				System.out.print(timeInterval + "s" + '\n');
				if (timeInterval > 130) {
					System.out.print('\n');
				}
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	public static void testEntityMap(){
		HashMap hashmap =new HashMap();  
        hashmap.put("Item0", "Value0");  
        hashmap.put("Item1", "Value1");  
        hashmap.put("Item2", "Value2");  
        hashmap.put("Item3", "Value3");  
        Set set=hashmap.entrySet();  
        Iterator iterator=set.iterator();  
        while (iterator.hasNext()) {  
          Map.Entry  mapEntry = (Map.Entry) iterator.next(); 
          Object key = mapEntry.getKey();
	      Object val = mapEntry.getValue();
          System.out.println(key+"/"+ val);         
        }
	}
	
	public static void testLineSegInersect(){
		MapMatchNode aNode = new MapMatchNode();
		MapMatchNode bNode = new MapMatchNode();
		MapMatchNode cNode = new MapMatchNode();
		MapMatchNode dNode = new MapMatchNode();
		aNode.x = 114.304;
		aNode.y = 30.615;
		bNode.x = 114.327;
		bNode.y = 30.601;
		cNode.x = 114.308;
		cNode.y = 30.608;
		dNode.x = 114.313;
		dNode.y = 30.615;
		double tempcxy[] = new double[2];
		double tempdxy[] = new double[2];
		PubClass.coordinateTransToPlaneCoordinate(cNode, 114, tempcxy);
		PubClass.coordinateTransToPlaneCoordinate(dNode, 114, tempdxy);		
		Double []cxy = new Double[]{tempcxy[0],tempcxy[1]};
		Double []dxy = new Double[]{tempdxy[0],tempdxy[1]};
		PubClass.isLineSegmentIntersect(aNode, bNode, cxy, dxy);
		
		System.out.print("");
	}
}
