package mapMatchingGPS;

import implement.SerializationTest;
import importDataToDatabase.ImportDataToMySQL;

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

import utilityPackage.PubClass;
import utilityPackage.PubParameter;
import webService.CoordinateCorr;

import association.Camera;

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

import entity.PropertiesUtilJAR;

public class MainFunction {
	public static void main(String[] args) throws AutomationException, IOException{
		/*************************************************************
		 * ����ʵ��
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
		 * ���л�����
		 * ���л�·������
		 * ******************************************/
		saveSeriseRoadNetwork();//�������л�·������
//		readSeriseRoadNetwork();//��ȡ���л�·������
//		saveSeriseLinkTravelTimeSingleThread();//�������л�ͨ��ʱ�����ݵ��߳�
//		singleThreadSaveSeriseLinkTravelTime();//����Ŀ��·�����л�ͨ��ʱ�����ݵ��߳�
//		saveSeriseLinkTravelTime();//�������л�ͨ��ʱ������
//		readSeriseLinkTravelTime();//��ȡ���л�ͨ��ʱ������
//		saveSeriseTaxiInfos();		
		//���л�����ͷ�ļ�
//		saveSerializeCameraData();
//		readSerializeCameraData();
		
		/********************************************
		 * ·��ͨ��ʱ��ͳ��
		 * ******************************************/
		
		
		singleThreadSaveTravelTimeToTxtFile(88);//���̣߳�ͳ��Ŀ��·��ͨ��ʱ�䣬������txt�ļ���
//		threadPoolSaveTravelTimeToTxtFile(1);//�̳߳أ�ͳ��Ŀ��·��ͨ��ʱ�䣬������txt�ļ���
//		threadPoolSaveTravelTimeToTxtFileAccordLinkID();//�̳߳أ�����·��ID������·��ͨ��ʱ��
		threadPoolSaveTravelTimeToTxtFile();//�̳߳أ�ͳ������·��ͨ��ʱ��,������txt�ļ���	
//		System.exit(0);//�������˳�����
		
		
		/*******************************************
		 * GPS����ģ����
		 * *****************************************/
//		readSeriseTaxiInfos();
//		for (int i = 0; i < 2; i++) {
//			taxiGPSSimulator();
//			i = 0;
//		}
		/*******************************************
		 *ʵ������GPS��������д��txt�ļ�
		 * *****************************************/
//		saveTaxiInfosToText();
		System.out.print("done!");
	}
	
	
	/***********************************************************
	 * ·���г�ʱ��ͳ��
	 ***********************************************************/
	
	/**
	 * ͳ������·��ͨ��ʱ�䣺����Ϊ�ı��ļ�
	 * �������·��һ��ʱ�䷶Χ�ڵ�ͨ��ʱ��
	 * ÿ����Ĵ�ͳ���г�ʱ�䣬ÿ��ͳ������·�Σ����ÿһ��·��Ҫ�������
	 * ����㷨�ڻ��link��������η�Χʱ��������Nan���󣬿�����link��������̫���Ե��
	 * ��ˣ���ѡ�������������η�ΧallLinkBoundingRectangleMap
	 */
	public static Map<Integer, ArrayList<TaxiTravelTime>> allTaxiTravelTimeMap = 
		new HashMap<Integer, ArrayList<TaxiTravelTime>>();//���г��⳵ͨ��ʱ��,���߳�IDΪ����
	
	public static void threadPoolSaveTravelTimeToTxtFile(){
		try {		
			double totalStartTime = System.nanoTime();
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");//���ʱ������
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");//�ļ���
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			int threadCount = PubParameter.threadCount;
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, threadCount, 1,  
	                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
	                new ThreadPoolExecutor.DiscardOldestPolicy()); 
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap = new HashMap<Integer, ArrayList<double[]>>();
			obtainAllLinkBoundingRectangle(polylineCollArrayList, allLinkBoundingRectangleMap);
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//������ʼʱ��
			String endTimeStr = taxiGPSTimeIntervalColl[1];//��������ʱ��
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
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
					threadPoolObtainAllTravelTimeAccordTime(threadPool,subStartTimeStr, subEndTimeStr, allLinkTravelTimeMap, allLinkBoundingRectangleMap);
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("��ʼ��������" + '\n');
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("���ݱ���ɹ�" + '\n'); 
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];
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
	
	/**
	 * ͳ��ĳһ·��ͨ��ʱ�䣺����Ϊ�ı��ļ�
	 * ����·��ID��ʱ�䷶Χ��һ��ʱ�䣩����ض�·�ε�ͨ��ʱ��
	 * @param targetLinkID	·��ID
	 */
	public static void threadPoolSaveTravelTimeToTxtFile(int targetLinkID, Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){
		try {		
			double totalStartTime = System.nanoTime();
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;				
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");
			String directoryPathFolder = PropertiesUtilJAR.getProperties("directoryPathFolder");			
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//������ʼʱ��
			String endTimeStr = taxiGPSTimeIntervalColl[1];//��������ʱ��
			String subStartTimeStr = startTimeStr;
			int threadCount = PubParameter.threadCount;
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, threadCount, 1,  
	                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
	                new ThreadPoolExecutor.DiscardOldestPolicy()); 
			while (!endTimeStr.equals(subStartTimeStr)) {
				double systemStartTime = System.nanoTime();
				int timeInterval = 6 * 3600;//ͳ��ÿ��6Сʱ��·�γ��⳵ͨ��ʱ��
				String []tempArrayStr = subStartTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				String []tempDateArrayStr = dateStr.split("-");
				String fileFolderNameStr = tempDateArrayStr[0] + tempDateArrayStr[1] + tempDateArrayStr[2] + "-" + String.valueOf(targetLinkID);				
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
					allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
					threadPoolObtainTravelTimeAccordToLinkIDAndTime(threadPool,targetLinkID, subStartTimeStr, subEndTimeStr, polylineCollArrayList, allLinkTravelTimeMap, allLinkBoundingRectangleMap);
					String directoryPathStr = directoryPathFolder + fileFolderNameStr;
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("��ʼ��������" + '\n');
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("���ݱ���ɹ���" + '\n');
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');	
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];		
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
	
	public static void writeLinkTravelTimeToText(Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, String directoryPathStr, String fileName){
		try {			
			String outPutPath = directoryPathStr + "\\" + fileName;
			//����ļ��в������򴴽�
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
			String TravelTimeDescription = "linkID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(TravelTimeDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));						
			java.util.Set keySet = allLinkTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<ReturnLinkTravelTime> returnLinkTravelTimeArrayList = allLinkTravelTimeMap.get(key);
        		//��ʱ������      		
        		for (int j = 0; j < returnLinkTravelTimeArrayList.size(); j++) {
        			String travelInfoStr = "";
        			ReturnLinkTravelTime returnLinkTravelTime = returnLinkTravelTimeArrayList.get(j);
        			int linkID = returnLinkTravelTime.getLinkID();//fileGeodatabase��ID��shapefile��ID���1
        			int travelDirection = returnLinkTravelTime.getTaxiTravelDirection();
        			String taxiID = returnLinkTravelTime.getTaxiID();
        			String startTravelTime = returnLinkTravelTime.getStartTravelTime();
        			double travelTime = returnLinkTravelTime.getTravelTime();
        			double meanSpeed = returnLinkTravelTime.getTaxiMeanSpeed();
        			travelInfoStr = linkID + "," + travelDirection + "," + taxiID + "," + startTravelTime + "," + travelTime + "," + meanSpeed+ "\r\n";
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
			System.out.print("д�����");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	/**
	 * ���·��������η�Χ
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
	 * ����ʱ���������
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
	 * ����·�ε��������
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
	 * ����·��ID����ֹʱ����·�ε�ͨ��ʱ��
	 * @param targetLinkID
	 * @param subStartTimeStr
	 * @param subEndTimeStr
	 * @param polylineCollArrayList
	 * @param allLinkTravelTimeMap
	 */
	public static void threadPoolObtainTravelTimeAccordToLinkIDAndTime(ThreadPoolExecutor threadPool,int targetLinkID, String subStartTimeStr, String subEndTimeStr, ArrayList<MapMatchEdge> polylineCollArrayList,
			Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){
		try {
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
			AssistFunction assistFunction = new AssistFunction();
			MapMatchEdge targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//���Ŀ��·��
			ArrayList<double[]> linkBoundingRectangleArrayList = new ArrayList<double[]>();
			linkBoundingRectangleArrayList = obtainBoundingRectangleAccordLinkID(targetLinkID, allLinkBoundingRectangleMap);
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
			int count = taxiGPSArrayList.size();
			System.out.print("���������ݿ⣺" + "�����������" + count + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
			System.out.print("��ȡ�ؿ�����GPS�㣺" + '\n');
			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList); 					
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(carryPassengerGPSDataArrayList, taxiSortMap);	
			int taxiTotalCount = taxiSortMap.size();//���⳵��Ŀ
			int threadCount = PubParameter.threadCount;//�߳���Ŀ
			int produceTaskSleepTime = 2;      
			double taxiCountEveryThread = (double)taxiTotalCount/threadCount;
			//threadCount���߳̽��м���
			//���С��1����ÿ���̼߳���һ�����⳵ͨ��ʱ��,��ʱ�߳���С��4
//			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, threadCount, 1,  
//		                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
//		                new ThreadPoolExecutor.DiscardOldestPolicy()); 
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
		                threadPoolProcess(threadPool, allTaxiTravelTimeMap,i, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge); 
		        	}				
				}
			}
			//����ÿ���̼߳���������⳵
			else {
				//���Ϊ���������ܹ�������
				String taxiCountEveryThreadStr = String.valueOf(taxiCountEveryThread);
				if (PubClass.isInteger(taxiCountEveryThreadStr)) {
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;//��ʱ����������ÿ���̵߳ĳ��⳵��Ŀ
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
		        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
			        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
			        		threadPoolProcess(threadPool,allTaxiTravelTimeMap, threadID, tempTaxiMap, subStartTimeStr, subEndTimeStr, targetLinkID, targetEdge); 	
		        			tempCount = 0;
		        			tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
						}			        		
		        	}							
				}
				//��Ϊ����
				else {
					int integerPart = PubClass.obtainIntegerPart(taxiCountEveryThreadStr);
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 1;
					int tempCount = 0;
					int tempThreadCount = 1;//��ʱ�����������߳���Ŀ,�����1��ʼ
					int taxiCount = 0;//��ʱ������ÿ���̼߳���ĳ��⳵��Ŀ
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
		        				ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
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
			        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
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
			
//			threadPool.shutdown(); //�رպ��ܼ������̣߳������е��߳�������ִ����
//			while (true) {
//				if (threadPool.getPoolSize()==0) {
//					System.out.print("�߳����н�����" + '\n');
//					break;
//					
//				}
////				if (threadPool.isTerminated()) {
////					System.out.print("�߳����н�����" + '\n');
////					break;
////				}
//			}
//			threadPool = null;
			Set keySet = allTaxiTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
			while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = allTaxiTravelTimeMap.get(key);
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
			}
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * �̳߳�
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
	        threadPool.execute(threadPoolTravelTimeStatistics);
	        Thread.sleep(produceTaskSleepTime); 
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * �������·��һ��ʱ�䷶Χ�ڵ�ͳ���г�ʱ��
	 * allLinkTravelTimeMap:ͳ����Ϣ
	 * count���ļ����
	 * startTimeStr:��ʼʱ��
	 * @param subStartTimeStr
	 * @param subEndTimeStr
	 * @param allLinkTravelTimeMap
	 * @param allLinkBoundingRectangleMap	·���������
	 */
	public static void threadPoolObtainAllTravelTimeAccordTime(ThreadPoolExecutor threadPool,String subStartTimeStr, String subEndTimeStr, Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap,
			Map<Integer, ArrayList<double[]>> allLinkBoundingRectangleMap){	
		try {
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
			for (int k = 0; k < polylineCollArrayList.size(); k++) {
				allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
				MapMatchEdge targetEdge = polylineCollArrayList.get(k);
				int targetLinkID = targetEdge.getEdgeID();
				System.out.print("����·��ͨ��ʱ�䣺" + k + ":" + (polylineCollArrayList.size() - 1) + '\n');
				threadPoolObtainTravelTimeAccordToLinkIDAndTime(threadPool,targetLinkID, subStartTimeStr, subEndTimeStr, polylineCollArrayList, allLinkTravelTimeMap, allLinkBoundingRectangleMap);			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}	
	
	/**
	 * ���̲߳��ԣ�Ŀ��·��ͨ��ʱ�䱣�浽txt��
	 * @param targetLinkID
	 */
	public static void singleThreadSaveTravelTimeToTxtFile(int targetLinkID){
		try {		
			double totalStartTime = System.nanoTime();
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
			AssistFunction assistFunction = new AssistFunction();
			MapMatchEdge targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//���Ŀ��·��
			double[]leftDownLB = new double[2];
			double[]rightTopLB = new double[2];
			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
			String taxiGPSTimeInterval = PropertiesUtilJAR.getProperties("taxiGPSTimeInterval");
//			String taxiGPSTimeInterval = "2014-06-03 18:00:00,2014-06-04 00:00:00";
			String taxiGPSTimeIntervalColl[] = taxiGPSTimeInterval.split(",");
			String startTimeStr = taxiGPSTimeIntervalColl[0];//������ʼʱ��
			String endTimeStr = taxiGPSTimeIntervalColl[1];//��������ʱ��
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
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
					allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
					singleThreadObtainTravelTimeAccordToLinkIDAndTime(targetLinkID,targetEdge, leftDownLB, rightTopLB, subStartTimeStr, subEndTimeStr, allLinkTravelTimeMap);
					String directoryPathStr = "C:\\travelTimeStatistics\\" + fileFolderNameStr;
					String filename = "allLinkTravelTime" + count + ".txt";
					System.out.print("��ʼ��������" + '\n');
					writeLinkTravelTimeToText(allLinkTravelTimeMap, directoryPathStr, filename);
					System.out.print("���ݱ���ɹ���" + '\n');
					double systemEndTime = System.nanoTime();
			    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
			    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');	
				}
				subStartTimeStr = subEndTimeStr;
				endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				subEndTimeStr = endTimeArray[0];		
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
	
	/**
	 * ���̲߳��ԣ�����·��ID����ֹʱ����·�ε�ͨ��ʱ��
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
			//�������ʱ��Լ������������
			System.out.print("��ʼ�����ݿ⣺" + '\n');
			double startReadDatabase = System.nanoTime();
			if(Double.isNaN(minLog) || minLog == 0){
				System.out.print("error");
			}
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, subStartTimeStr, subEndTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("���������ݿ⣺" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
			System.out.print("��ȡ�ؿ����ݣ�" + '\n');
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
			System.out.print("����·��ͨ��ʱ�䣺" + i + ":" + (polylineCollArrayList.size() - 1));
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
			//�������ʱ��Լ������������
			System.out.print("��ʼ�����ݿ⣺" + '\n');
			double startReadDatabase = System.nanoTime();
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("���������ݿ⣺" + '\n');
			double readDatabaseTime = (startReadDatabase - endReadDatabase)/Math.pow(10, 9);
			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��
			//����ID���з���
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap); 
			mapMatchAlgorithm.linkTravelTimeProcessSingleThread(targetLinkID, targetEdge, startTimeStr, endTimeStr, taxiSortMap,linkTravelTimeArrayList);
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);			
		}
		String filename = "C:\\allLinkTravelTime20130101.bin";
		System.out.print("��ʼ��������" + '\n');
		mapMatchAlgorithm.saveSeriseLinkTravelTime(filename, allLinkTravelTimeMap);
		System.out.print("���ݱ���ɹ�" + '\n');
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');   	
	}
	
	/**
	 * �������л�·��ͨ��ʱ������
	 */
	public static void saveSeriseLinkTravelTime(){
		double systemStartTime = System.nanoTime();
		Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;	
		MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
		for (int i = 0; i < polylineCollArrayList.size(); i++) {
			MapMatchEdge targetEdge = polylineCollArrayList.get(i);
			System.out.print("����·��ͨ��ʱ�䣺" + i + ":" + (polylineCollArrayList.size() - 1));
			int targetLinkID = targetEdge.getEdgeID();
			String startTimeStr = "2013-01-01 00:00:00";
			String endTimeStr = "2013-01-01 01:00:00";					
			AssistFunction assistFunction = new AssistFunction();
			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//���Ŀ��·��
			double[]leftDownLB = new double[2];
			double[]rightTopLB = new double[2];
			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
			double minLog = leftDownLB[0];
			double minLat = leftDownLB[1];
			double maxLog = rightTopLB[0];
			double maxLat = rightTopLB[1];  			
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
			//�������ʱ��Լ������������
			System.out.print("��ʼ�����ݿ⣺" + '\n');
			double startReadDatabase = System.nanoTime();
			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("���������ݿ⣺" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList);
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(carryPassengerGPSDataArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��
			ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//·��ͨ��ʱ��ļ�����
			mapMatchAlgorithm.linkTravelTimeProcessMultiThread(targetLinkID, startTimeStr, endTimeStr, targetEdge, 
					eliminateZeroSpeedGPSDataArrayList, linkTravelTimeArrayList);
			allLinkTravelTimeMap.put(targetLinkID, linkTravelTimeArrayList);
		}
			
		String filename = "C:\\allLinkTravelTime20130101.bin";
		System.out.print("��ʼ��������" + '\n');
		mapMatchAlgorithm.saveSeriseLinkTravelTime(filename, allLinkTravelTimeMap);
		System.out.print("���ݱ���ɹ�" + '\n');
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
	}
	
	/**
	 * ��ȡ���л�·��ͨ��ʱ������
	 */
	public static void readSeriseLinkTravelTime(){
		try {
			String fileName = PropertiesUtilJAR.getProperties("travelTime");
			System.out.print("��ʼ�����л�����" + '\n');	
			MapMatchAlgorithm.instance().readSeriseLinkTravelTime(fileName);
//			MapMatchAlgorithm mapMatchAlgorithm = new MapMatchAlgorithm();
//			mapMatchAlgorithm.readSeriseLinkTravelTime(fileName);
			System.out.print("���л����ݶ�ȡ�ɹ�" + '\n');	
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
                aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic);
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
	 * �������л�����ͷ��λ����
	 */
	public static void saveSerializeCameraData(){
		try {
			SerializeFunction serializeFunction = new SerializeFunction();
			String cameraFilename = "C:\\cameraInfos.bin";//���л��ļ�
			String path = "F:\\faming\\esri\\roadNetwork.gdb";
			String cameraShapeFilename = "������Ƶ��λ��������";
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
	 	    	//һ��Ҫʵ����
	 			Camera camera = new Camera();
	 			camera.setID(feature.getOID());
	 			camera.setLongitude(juncPoint.getX());
	 			camera.setLatitude(juncPoint.getY());	 			
	 			IFields fields=new Fields();
				fields=feature.getFields();
				int FOVFiedIndex = fields.findField("Angle1");
				int directAngleFiedIndex = fields.findField("Angle2");
				int distFiedIndex = fields.findField("Distance2");			
				float tempFOV = (Float)feature.getValue(FOVFiedIndex);//�ӳ���
				double FOV = tempFOV;
				float tempDirectAngle = (Float)feature.getValue(directAngleFiedIndex);//�����
				double directAngle = tempDirectAngle;
				float tempDist = (Float)feature.getValue(distFiedIndex);
	 			double dist = tempDist;
				camera.setFieldOfView(FOV);
				camera.setAngleDirection(directAngle);
				camera.setAngleDist(dist);	 			
	 			cameraInfosArrayList.add(camera);
	 			System.out.println("��ȡ����ͷ:" + i + ":" + juncFeatureCount);
			}  
		    System.out.println("����ͷ��ȡ����" + '\n');
			serializeFunction.saveSeriseCameraData(cameraFilename, cameraInfosArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * ��ȡ���л�����ͷ��λ����
	 */
	public static void readSerializeCameraData(){
		String cameraFilename = "C:\\cameraInfos.bin";//���л��ļ�
		ArrayList<Camera> cameraInfosArrayList = new ArrayList<Camera>();
		SerializeFunction serializeFunction = new SerializeFunction();		
		cameraInfosArrayList = serializeFunction.readSeriseCameraData(cameraFilename);
		System.out.println("����ͷ��ȡ����" + '\n');
	}
	
	/**
	 * �������л�TaxiGPS����
	 */
	public static void saveSeriseTaxiInfos(){
		double systemStartTime = System.nanoTime();
		try {
			String filePath = PropertiesUtilJAR.getProperties("taxiFilefolderName");//�洢·��
			ArrayList<String> taxiIDArrayList = new ArrayList<String>();
			String startTimeStr = "2013-01-01 00:00:00";
			String endTimeStr = "2013-01-02 00:00:00";
			//ʵ������
			double minLog = 114.282;
			double maxLog = 114.297;
			double minLat = 30.599;
			double maxLat = 30.611;
			SerializeFunction serializeFunction = new SerializeFunction();
			System.out.print("��ʼ��ó��⳵ID" + '\n');
			DatabaseFunction.obtainTaxiIDAccordTimeSpatialFilte(taxiIDArrayList, startTimeStr, endTimeStr, minLog, maxLog, minLat, maxLat);
			System.out.print("������ó��⳵ID" + '\n');
			String metadataFileName = PropertiesUtilJAR.getProperties("metadataFileName");
			System.out.print("��ʼ������⳵Ԫ����" + '\n');			
			serializeFunction.saveSeriseTaxiMetadata(metadataFileName, taxiIDArrayList);			
			System.out.print("����������⳵Ԫ����" + '\n');
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
				System.out.print("��ʼ��ó��⳵�켣���ݣ�" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
				String targetIDStr = taxiIDArrayList.get(i);
				Map<String, ArrayList<TaxiGPS>> taxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();			
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataAccordIDTimeSpatialFilter(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
				taxiInfosMap.put(targetIDStr, taxiGPSArrayList);				
				String filename = filePath + targetIDStr + ".bin";
				System.out.print("��ʼ��������" + '\n');
				boolean isOK = serializeFunction.saveSeriseTaxiInfos(filename, taxiInfosMap);
				if (isOK) {
					System.out.print("���ݱ���ɹ�" + '\n');
				}
				else {
					System.out.print("�ļ����Ѵ��ڻ����ݱ���ʧ�ܣ�" + '\n');
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
	}
	
	/**
	 * ��ȡ���л�TaxiGPS����
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
			System.out.print("��ʼ�����⳵Ԫ����" + '\n');
			ArrayList<String> taxiIDArrayList = SerializeFunction.instance().taxiIDArrayList;
			System.out.print("���������⳵Ԫ����" + '\n');
			System.out.print("��ʼ�����л��ļ���" + '\n');
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
				System.out.print("�����л��ļ���" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
				String targetIDStr = taxiIDArrayList.get(i);
				String fileName = PropertiesUtilJAR.getProperties("taxiFilefolderName") + targetIDStr + ".bin";
				Map<String, ArrayList<TaxiGPS>> taxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();
				serializeFunction.readSeriseTaxiInfos(fileName);
			}
			System.out.print("���������л��ļ���" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		double systemEndTime = System.nanoTime();
    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
	}
	
	/*����TaxiGPS���ݵ�txt�ļ���*/
	public static void saveTaxiInfosToText(){
		double startobtainData = System.nanoTime();
		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
		//ʱ�������
		String startTimeStr = "2013-01-07 00:00:00";
		String endTimeStr = "2013-01-08 00:00:00";
		//ʵ������
		double minLog = 114.282;
		double maxLog = 114.297;
		double minLat = 30.599;
		double maxLat = 30.611;
		SerializeFunction serializeFunction = new SerializeFunction();
		System.out.print("��ʼ��ó��⳵ID" + '\n');
		DatabaseFunction.obtainTaxiIDAccordTimeSpatialFilte(taxiIDArrayList, startTimeStr, endTimeStr, minLog, maxLog, minLat, maxLat);
//		String filename = PropertiesUtil.getProperties("metadataFileName");
//		taxiIDArrayList = serializeFunction.readSeriseTaxiMetadataArraylist(filename);
		System.out.print("������ó��⳵ID" + '\n');
		Map<String, ArrayList<TaxiGPS>> allOrigianlGPSMap = new HashMap<String, ArrayList<TaxiGPS>>();//ԭʼGPS������
		Map<String, ArrayList<CorrectedNode>> allCorrectGPSMap = new HashMap<String, ArrayList<CorrectedNode>>();//���о������GPS������
		for (int i = 0; i < taxiIDArrayList.size(); i++) {
			System.out.print("��ʼ��ó��⳵�켣���ݣ�" + i + ":" + (taxiIDArrayList.size() - 1) + '\n');
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
		//ԭʼ���������������ֱ�д��txt�ļ�
		String outPutPath = "C:\\originalAndCorrectGPS20130107.txt";//д��Txt�ļ���
		System.out.print("��ʼд�����ݣ�" + '\n');
		writeCorrectedCoordToText(allCorrectGPSMap,outPutPath);
		System.out.print("����д�����ݣ�" + '\n');
		double endObtainData = System.nanoTime();
		double obtainDataTime = (endObtainData - startobtainData)/Math.pow(10, 9);
		System.out.print("�������ʱ�䣺" + obtainDataTime + "s" + '\n');
	}
	
	/**
	 * ������GPS����д�뵽txt�ļ���
	 * allCorrectGPSMap������GPS����
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
        			int linkID = correctedNode.getTargetEdgeID();//fileGeodatabase��ID��shapefile��ID���1
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
			System.out.print("д�����");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	/**
	 * GPS����ģ�ⷢ����
	 */
	public static void taxiGPSSimulator(){
		Map<String, ArrayList<TaxiGPS>> allTaxiInfosMap = SerializeFunction.instance().allTaxiInfosMap;
		Simulator.taxiGPSSimulativeGenerator(allTaxiInfosMap);	
	}
	
	/**
	 * �������л�·������
	 * ·���������л������и�������
	 */
	public static void saveSeriseRoadNetwork(){
		/*·���������л�*/
		String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
		String roadNetworkNamecoll[] = roadNetworkName.split(",");
		String geoDatabaseFilePath = roadNetworkNamecoll[0];
		String juncFileName = roadNetworkNamecoll[1];
		String polylineFileName = roadNetworkNamecoll[2];
		String fileName = roadNetworkNamecoll[3];//�����ļ���
		MapMatchAlgorithm.instance().createConnSetAndTopolygon(geoDatabaseFilePath, juncFileName, polylineFileName);
		System.out.print("��ʼ��������" + '\n');	
		MapMatchAlgorithm.instance().saveRoadFile(fileName);
		System.out.print("���ݱ���ɹ�" + '\n');
	}
	
	/*��ȡ���л�·������*/
	public static void readSeriseRoadNetwork(){
		try {
			String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[3];//�ļ���
			System.out.print("��ʼ�����л�����" + '\n');	
			MapMatchAlgorithm.instance().readRoadFile(fileName);
			System.out.print("���л����ݶ�ȡ�ɹ�" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
	}
	
	/***********************************************************
	 * ���Ժ���
	 * *********************************************************
	 */
	/*����ʱ����㺯��*/
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
			System.out.print("��ʼ�����ݿ⣺" + '\n');
			double startReadDatabase = System.nanoTime();
//			assistFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
			double endReadDatabase = System.nanoTime();
			System.out.print("���������ݿ⣺" + '\n');
			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
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
