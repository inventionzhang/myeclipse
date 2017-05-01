package org.lmars.network.latentDirichletAllocation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Output;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lmars.network.database.ImportDataToMySQL;
import org.lmars.network.mapMatchingGPS.DatabaseFunction;
import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.mapMatchingGPS.MapMatchNode;
import org.lmars.network.mapMatchingGPS.TaxiGPS;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.datasourcesraster.KauthThomasFunction;
import com.esri.arcgis.geodatabase.Feature;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureWorkspace;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPointCollection;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;


import jxl.*;


/**
 * 此方法废弃，improveMainFunctionTripPatterns代替此方法
 * @author whu
 *
 */
public class MainFunctionTripPatterns {
	
	public static void main(String[] args){
//		//提取上客点与下客点
//		(new MainFunctionTripPatterns()).arrestPointProcess();		
//		System.out.print("提取完成!");
//		
		//对提取的上客点、下客点处理,计算trip的distance和direction
		//并对天河机场附近路段做特殊处理，更新天河机场附近路段
//		(new MainFunctionTripPatterns()).tripPatternsProcess();
//		System.out.print("数据处理完成!");
		
				
//		//输出每天每小时的上客点数和下客点数
//		(new MainFunctionTripPatterns()).extractPickupsCount();
		
//		提取距离分布
//		(new MainFunctionTripPatterns()).extractDistanceDistribution();
		
		//距离分布在时间上的差异性
//		(new MainFunctionTripPatterns()).extractDistanceTemporalDistribution();
		
		//excel处理
//		(new MainFunctionTripPatterns()).excelTripDistanceProcess();//出行距离相似性
//		(new MainFunctionTripPatterns()).excelTripDirectionProcess();//出行方向相似性
		(new MainFunctionTripPatterns()).excelLDADocumentTopicDistributionProcess();//读取文档主题分布excel
//		(new MainFunctionTripPatterns()).processShapeFile();//处理shapefile文件
//		(new MainFunctionTripPatterns()).tripDirectionTopicTermDistribution();
		
	}

	/**
	 * 提取上客点与下客点并地图匹配、获得上客点与下客点所在路段编号、名称等信息，写入数据库
	 * 1.获得一天内的出租车ID
	 * 2.对每一个出租车ID上客点与下客点一天的数据进行提取，并写入数据库
	 * 3.循环遍历每天数据，直到终止日期结束
	 */
	public void arrestPointProcess(){
		String startTimeStr = "2014-06-02 00:00:00";//开始时间
		String endTimeStr = "2014-06-09 00:00:00";//终止时间
		String insertTableName = "trippattern";//插入表名		
		double totalStartTime = System.nanoTime();	
		LDAAssistFunction.obtainRoadNetworkRange(MapMatchAlgorithm.instance().juncCollArrayList);
		LDAAssistFunction ldaAssistFunction = new LDAAssistFunction();
		ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();	
		int timeInterval = 24 * 3600;//每隔一天		
		String subStartTimeStr = startTimeStr;
		while (!endTimeStr.equals(subStartTimeStr)) {			
			String[] endTimeArray = new String[1];
			PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
			String subEndTimeStr = endTimeArray[0];
			ArrayList<String> taxiIDArrayList = new ArrayList<String>();
			DatabaseFunction.obtainUniqueTaxiIDAccordTime(taxiIDArrayList, subStartTimeStr, subEndTimeStr);			
			int taxiCount = taxiIDArrayList.size();
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
				String targetIDStr = taxiIDArrayList.get(i);
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, subStartTimeStr, subEndTimeStr);
				ArrayList<TaxiGPS> arrestGPSArrayList = new ArrayList<TaxiGPS>();
				ldaAssistFunction.obtainTaxiPassengerArrestGPSPoint(taxiGPSArrayList, arrestGPSArrayList);
				ArrayList<TaxiGPS> processArrestGPSArrayList = new ArrayList<TaxiGPS>();
				ldaAssistFunction.arrestGPSPointMapMatch(arrestGPSArrayList, processArrestGPSArrayList);
				importDataToMySQL.insertCorrectedTaxiDataToDatabase(processArrestGPSArrayList, insertTableName, i, taxiCount);				
			}		
			subStartTimeStr = subEndTimeStr;
			endTimeArray = new String[1];
			PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
			subEndTimeStr = endTimeArray[0];
		}
		double totalEndTime = System.nanoTime();
    	double totalProcessTime = (totalEndTime - totalStartTime)/Math.pow(10, 9);
    	System.out.print("程序总运行时间：" + totalProcessTime + "s" + '\n');		
		System.out.print("");			
	}
	
	/**
	 * 根据提取信息，计算出行距离与出行方向
	 * 出行方向：上客点到下客点之间直线方向的方位角
	 * 出行距离：上客点到下客点之间距离
	 * 1.读表中一天内的出租出数据
	 * 2.对每一辆出租车一天内的数据进行处理，计算出行方向、距离
	 * 3.循环1,2，直到终止时间
	 */
	public void tripPatternsProcess(){		
		String startTimeStr = "2014-06-02 00:00:00";//开始时间
		String endTimeStr = "2014-06-09 00:00:00";//终止时间
		double totalStartTime = System.nanoTime();	
		try {
			String readTableName = "trippattern";//读取表表名
			String insertTableName = "trippatternprocess";//插入表表名
			ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();
			int timeInterval = 24 * 3600;//每隔一天		
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {	
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];			
				ArrayList<String> taxiIDArrayList = new ArrayList<String>();
				DatabaseFunction.obtainUniqueTaxiIDAccordTimeAndTableName(taxiIDArrayList, subStartTimeStr, subEndTimeStr, readTableName);			
				int taxiCount = taxiIDArrayList.size();
				for (int i = 0; i < taxiCount; i++) {
					String targetIDStr = taxiIDArrayList.get(i);
					ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
					DatabaseFunction.obtainGPSDataFromDatabaseAccordTaxiIDStartEndTimeTripPatternTable(taxiGPSArrayList, targetIDStr, subStartTimeStr, subEndTimeStr, readTableName);
					ArrayList<TaxiGPS> processTaxiGPSArrayList = new ArrayList<TaxiGPS>();
					processTaxiGPSArraylist(taxiGPSArrayList, processTaxiGPSArrayList);
					importDataToMySQL.insertCorrectedTaxiDataToDatabase(processTaxiGPSArrayList, insertTableName, i, taxiCount);	
				}		
				subStartTimeStr = subEndTimeStr;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		double totalEndTime = System.nanoTime();
    	double totalProcessTime = (totalEndTime - totalStartTime)/Math.pow(10, 9);
    	System.out.print("程序总运行时间：" + totalProcessTime + "s" + '\n');
	}
	
	/**
	 * 根据taxiGPSArrayList计算每一对上客点、下客点的direction和distance
	 * 满足条件的trip：第一个点为上客点，第二个点为下客点
	 * 1.若连续两个点满足条件第一个点为上客点，第二个点为下客点，则为有效轨迹
	 * 2.对轨迹求direction和distance
	 * @param taxiGPSArrayList
	 * @param processTaxiGPSArrayList
	 */
	public void processTaxiGPSArraylist(ArrayList<TaxiGPS> taxiGPSArrayList, ArrayList<TaxiGPS> processTaxiGPSArrayList) {
		try {
			for (int i = 0; i < taxiGPSArrayList.size() - 1; i++) {
				TaxiGPS curTaxiGPS = taxiGPSArrayList.get(i);
				int curChangedStatus = curTaxiGPS.getChangedStatus();
				TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(i + 1);
				int nextChangedStatus = nextTaxiGPS.getChangedStatus();
				if (curChangedStatus == 1 && nextChangedStatus == 0) {
					MapMatchNode curTaxiNode = PubClass.ConvertTaxiGPSToMapMatchNode(curTaxiGPS);
					MapMatchNode nextTaxiNode = PubClass.ConvertTaxiGPSToMapMatchNode(nextTaxiGPS);					
					double tripDirection = PubClass.obtainAzimuth(curTaxiNode, nextTaxiNode);
					double tripDistance = PubClass.distance(curTaxiNode, nextTaxiNode);
					String tripDirectionStr = String.format("%.4f", tripDirection);//保留小数点后四位，并进行四舍五入
					String tripDistanceStr = String.format("%.4f", tripDistance);
					double processTripDirection = Double.parseDouble(tripDirectionStr);
					double processtripDistance = Double.parseDouble(tripDistanceStr);
					String curTaxiTimeStr = curTaxiGPS.getLocalTime();
					String nextTaxiTimeStr = nextTaxiGPS.getLocalTime();
					
					//天河机场处的GPS点漂移严重，特殊处理，赋予路段编号22921
					int curTaxiStreetNo = curTaxiGPS.getBelongLineID();
					double curTaxiLogi = curTaxiGPS.getLongitude();
					double curTaxiLati = curTaxiGPS.getLatitude();
					int nextTaxiStreetNo = nextTaxiGPS.getBelongLineID();
					double nextTaxiLogi = nextTaxiGPS.getLongitude();
					double nextTaxiLati = nextTaxiGPS.getLatitude();
					if (curTaxiStreetNo == -1 && curTaxiLogi > 114.18 && curTaxiLogi < 114.22 && 
							curTaxiLati > 30.75 && curTaxiLati < 30.78) {
						curTaxiGPS.setBelongLineID(22921);
						curTaxiGPS.setBelongLinkName("机场高速");
					}
					if (nextTaxiStreetNo == -1 && nextTaxiLogi > 114.18 && nextTaxiLogi < 114.22 && 
							nextTaxiLati > 30.75 && nextTaxiLati < 30.78) {
						nextTaxiGPS.setBelongLineID(22921);
						nextTaxiGPS.setBelongLinkName("机场高速");
					}
					
					
					double timeInterval = PubClass.obtainTimeInterval(curTaxiTimeStr, nextTaxiTimeStr);
					if (timeInterval > PubParameter.tripTimeThreshold && processtripDistance > PubParameter.tripDistanceThreshold) {
						curTaxiGPS.setTripDirection(processTripDirection);
						curTaxiGPS.setTripDistance(processtripDistance);
						nextTaxiGPS.setTripDirection(processTripDirection);
						nextTaxiGPS.setTripDistance(processtripDistance);
						processTaxiGPSArrayList.add(curTaxiGPS);
						processTaxiGPSArrayList.add(nextTaxiGPS);
					}
					i++;
				}
				else {
					continue;
				}				
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	/**
	 * 提取上客点数与下客点数
	 * 1.每隔一小时提取上客数
	 */
	public void extractPickupsCount(){
		try {
			String startTimeStr = "2014-06-08 00:00:00";
			String endTimeStr = "2014-06-09 00:00:00";
			String tableName = "trippatternprocess";
			int timeInterval = 3600;//每隔一小时		
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				int pickUpsCount = DatabaseFunction.obtainPickUpsCount(subStartTimeStr, subEndTimeStr, tableName);
				System.out.print(subStartTimeStr + ":" +pickUpsCount + "\n");
				subStartTimeStr = subEndTimeStr;
			}
			System.out.print("done!上客点数提取结束!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
	}
	
	/**
	 * 提取距离分布
	 * 1.距离按照0.5km增加，提取距离数目
	 */
	public void extractDistanceDistribution(){
		try {
//			最大距离为：48466.84m
//			SELECT MAX(T_TripDistance) FROM `trippatternprocess` WHERE T_ChangedStatus = 1;//获得最大距离
			String tableName = "trippatternprocess";
			String timeStr = "2014-06-08 00:00:00";
			double distanceLowerLimit = 0;
			double distanceUpperLimit = 49000;
			double subDistanceLowerLimit = distanceLowerLimit;
			while (subDistanceLowerLimit < distanceUpperLimit) {
//				//1km以内按100m增加距离
//				if (subDistanceLowerLimit < 1000) {
//					double subDistanceUpperLimit = subDistanceLowerLimit + 100;
//					int distanceCount = DatabaseFunction.obtainDistanceRangeCount(subDistanceLowerLimit, subDistanceUpperLimit, tableName);								
//					System.out.print(subDistanceLowerLimit + "-" + subDistanceUpperLimit + ";" +distanceCount + "\n");
//					subDistanceLowerLimit = subDistanceUpperLimit;
//				}
//				else {
					double subDistanceUpperLimit = subDistanceLowerLimit + 500;
					int distanceCount = DatabaseFunction.obtainDistanceRangeCount(subDistanceLowerLimit, subDistanceUpperLimit, tableName, timeStr);								
					System.out.print(subDistanceLowerLimit + "-" + subDistanceUpperLimit + ";" +distanceCount + "\n");
					subDistanceLowerLimit = subDistanceUpperLimit;
//				}		
			}
			System.out.print("done!距离分布提取结束!");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	/**
	 * 提取距离在时间分布上的差异性
	 * 每小时内距离<=distance 的count
	 */
	public void extractDistanceTemporalDistribution(){
		try {
			String tableName = "trippatternprocess";
			double distanceLowerLimit = 20000;
			double distanceUpperLimit = 50000;
			String startTimeStr = "2014-06-02 00:00:00";
			String endTimeStr = "2014-06-09 00:00:00";
			int timeInterval = 3600;//每隔一小时		
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				int distanceCount = DatabaseFunction.obtainSatisfiedDistancePickupsCountInanHour(distanceLowerLimit,distanceUpperLimit, tableName, subStartTimeStr);//左开右闭
				System.out.print(subStartTimeStr + "," + distanceCount + "\n");
				subStartTimeStr = subEndTimeStr;
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
	}
	
	
	/**
	 * 提取某天上客点对应的路段编号以及出行距离
	 * 
	 */
	public void excelTripDistanceProcess(){
		try {			
			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellLinkID, cellTripDist;
	        try { 
	            //t.xls为要读取的excel文件名
	            book = Workbook.getWorkbook(new File(path));            
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后)
	            //前提是sheet按LinkID分类统计
//	            sheet = book.getSheet(3); 
	            sheet = book.getSheet("pickupsAndDropoffslinkID");
	            int rowCount = sheet.getRows();
	            //获取第一行单元格
	            cellLinkID = sheet.getCell(0, 0);
	            cellTripDist = sheet.getCell(1, 0); 
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段ID为索引，存储通行距离
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	            	
	            	int curLinkID = Integer.parseInt(sheet.getCell(0, i).getContents());
	            	String tripDistStr = sheet.getCell(2, i).getContents();
	            	tripDistArraylist.add(tripDistStr);
	            	for (int j = i + 1; j < rowCount; j++) {
	            		int nextLinkID = Integer.parseInt(sheet.getCell(0, j).getContents());
	            		tripDistStr = sheet.getCell(2, j).getContents();
	            		//
						if (curLinkID == nextLinkID) {
							tripDistArraylist.add(tripDistStr);
						}
						else {
							i = j - 1;
							break;
						}	            		
					}
	            	tripMap.put(curLinkID, tripDistArraylist);					
				}
	            
	            //写入txt文件
	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602tripDirection.txt";
	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String headDescription = "linkID" + "," + "tipDistance" + "\r\n";
				write.append(headDescription);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
	            
	            java.util.Set keySet = tripMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		int key = (Integer)mapEntry.getKey();
	        		if (key == 15455) {
	        			System.out.print(" ");
					}
	        		ArrayList<String> tripDistArrayList = tripMap.get(key);
	        		String tripDistStr = "";
	        		for (int i = 0; i < tripDistArrayList.size(); i++) {
	        			tripDistStr = tripDistArrayList.get(i) + " " + tripDistStr;
					}
	        		tripDistStr = key + "," + tripDistStr + "\r\n";
	        		write = new StringBuffer();				
    				write.append(tripDistStr);
    				bufferStream.write(write.toString().getBytes("UTF-8"));	
	        	}
	        	bufferStream.flush();      
				bufferStream.close(); 
				outputStream.close();
				System.out.print("数据写入结束" + "\n");
	            book.close();
			
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
		  		System.out.println(e.getMessage());
			}
		}
		 catch (Exception e2) {
			// TODO: handle exception
			e2.printStackTrace();
	  		System.out.println(e2.getMessage());
		}
	}
	
	/**
	 * 提取某天上客点对应的路段编号以及下客点对应路段编号
	 * 
	 */
	public void excelTripDirectionProcess(){
		try {			
			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellLinkID, cellTripDist;
	        try { 
	            //t.xls为要读取的excel文件名
	            book = Workbook.getWorkbook(new File(path));            
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后)
	            //前提是sheet按LinkID分类统计
//	            sheet = book.getSheet(3); 
	            sheet = book.getSheet("pickupsAndDropoffslinkID");
	            int rowCount = sheet.getRows();
	            //获取第一行单元格
	            cellLinkID = sheet.getCell(0, 0);
	            cellTripDist = sheet.getCell(1, 0); 
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段ID为索引，存储通行距离
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	            	
	            	int curLinkID = Integer.parseInt(sheet.getCell(0, i).getContents());
	            	String tripDirectionLinkIDStr = sheet.getCell(1, i).getContents();
	            	tripDistArraylist.add(tripDirectionLinkIDStr);
	            	for (int j = i + 1; j < rowCount; j++) {
	            		int nextLinkID = Integer.parseInt(sheet.getCell(0, j).getContents());
	            		tripDirectionLinkIDStr = sheet.getCell(1, j).getContents();
	            		//
						if (curLinkID == nextLinkID) {
							tripDistArraylist.add(tripDirectionLinkIDStr);
						}
						else {
							i = j - 1;
							break;
						}	            		
					}
	            	tripMap.put(curLinkID, tripDistArraylist);					
				}
	            
	            //写入txt文件
	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602tripDirection.txt";
	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String headDescription = "linkID" + "," + "tripDirectionLinkIDStr" + "\r\n";
				write.append(headDescription);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
	            
	            java.util.Set keySet = tripMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		int key = (Integer)mapEntry.getKey();
	        		if (key == 15455) {
	        			System.out.print(" ");
					}
	        		ArrayList<String> tripDistArrayList = tripMap.get(key);
	        		String tripDistStr = "";
	        		for (int i = 0; i < tripDistArrayList.size(); i++) {
	        			tripDistStr = tripDistArrayList.get(i) + " " + tripDistStr;
					}
	        		tripDistStr = key + "," + tripDistStr + "\r\n";
	        		write = new StringBuffer();				
    				write.append(tripDistStr);
    				bufferStream.write(write.toString().getBytes("UTF-8"));	
	        	}
	        	bufferStream.flush();      
				bufferStream.close(); 
				outputStream.close();
				System.out.print("数据写入结束" + "\n");
	            book.close();
			
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
		  		System.out.println(e.getMessage());
			}
		}
		 catch (Exception e2) {
			// TODO: handle exception
			e2.printStackTrace();
	  		System.out.println(e2.getMessage());
		}
	}
	
	
	/**
	 * 读取文档主题分布excel文件(xlsx)
	 */
	public void excelLDADocumentTopicDistributionProcess(){
		
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//			String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.xlsx";
		double threshVal = 0.1;//文档中主题分布概率的阈值
		XSSFSheet sheet;
        XSSFRow headRow;//标题行
        XSSFRow row;
        Map<String, String> topicMap = new HashMap<String, String>();//主题以及主题对应的路段ID,以及概率
        try { 
        	// 设置要读取的文件路径
			ifs = new FileInputStream(path);       	
			// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
			// 之后版本使用XSSFWorkbook（xlsx）
			wb = new XSSFWorkbook(ifs);
//	            sheet = wb.getSheet("documentTopicDistribution"); 
			sheet = wb.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
            headRow = sheet.getRow(0);// 获得sheet中第0行
            int columCount = headRow.getPhysicalNumberOfCells();
            String[] probDistributionArray = new String[10];//概率分布
            for (int p = 0; p < probDistributionArray.length; p++) {
            	probDistributionArray[p] = "";
			}
            
            for (int i = 1; i <= rowCount; i++) {
            	row = sheet.getRow(i);// 获得sheet中第i行
            	String topicStr = row.getCell(0).getStringCellValue();
            	for (int j = 1; j < columCount; j++) {
            		// 获得行中的列，即单元格HSSFCell
					XSSFCell cell = row.getCell(j);
					// 获得单元格中的值
					double cellVal = cell.getNumericCellValue();
					if (cellVal > threshVal) {
						String probability = String.format("%.3f", cellVal);						
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//取整数
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("开始写文档主题概率分布数据：" + "\n");            
            //写入txt文件
            String outPutPath = "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.txt";
            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String headDescription = "topic" + "," + "linkIDAndTopicDistribution" + "\r\n";
			write.append(headDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));           
            java.util.Set keySet = topicMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		String key = (String)mapEntry.getKey();
        		String probabilityDistributionStr = topicMap.get(key);//概率分布
        		probabilityDistributionStr = key + "：" + probabilityDistributionStr + "\r\n";
        		write = new StringBuffer();				
				write.append(probabilityDistributionStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
        	}
        	bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("数据写入结束" + "\n");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 从Excel中获得文档主题概率分布
	 * @param topicMap
	 */
	public void obtainLDADocumentTopicFromExcel(Map<String, String> topicMap){
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//		String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.xlsx";
		double threshVal = 0.1;//文档中主题分布概率的阈值
		XSSFSheet sheet;
        XSSFRow headRow;//标题行
        XSSFRow row;
//        Map<String, String> topicMap = new HashMap<String, String>();//主题以及主题对应的路段ID,以及概率
        try { 
        	// 设置要读取的文件路径
			ifs = new FileInputStream(path);       	
			// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
			// 之后版本使用XSSFWorkbook（xlsx）
			wb = new XSSFWorkbook(ifs);
//	            sheet = wb.getSheet("documentTopicDistribution"); 
			sheet = wb.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
            headRow = sheet.getRow(0);// 获得sheet中第0行
            int columCount = headRow.getPhysicalNumberOfCells();
            String[] probDistributionArray = new String[10];//概率分布
            for (int p = 0; p < probDistributionArray.length; p++) {
            	probDistributionArray[p] = "";
			}           
            for (int i = 1; i <= rowCount; i++) {
            	row = sheet.getRow(i);// 获得sheet中第i行
            	String topicStr = row.getCell(0).getStringCellValue();
            	for (int j = 1; j < columCount; j++) {
            		// 获得行中的列，即单元格HSSFCell
					XSSFCell cell = row.getCell(j);
					// 获得单元格中的值
					double cellVal = cell.getNumericCellValue();
					if (cellVal > threshVal) {
						String probability = String.format("%.3f", cellVal);//概率值					
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//取整数
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("开始写文档主题概率分布数据：" + "\n");  
        }
        catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	
	
	/**
	 * 读取shape文件并添加字段
	 */
	public void processShapeFile(){
		try {
			//Step 1: Initialize the Java Componet Object Model (COM) Interop.
	        EngineInitializer.initializeEngine();
	        //Step 2: Initialize an ArcGIS license.
	        AoInitialize aoInit = new AoInitialize();
	        initializeArcGISLicenses(aoInit);
	        String path = "F:\\faming\\esri\\roadNetwork.gdb";
	        String polylineFileNameString = "WuhanRingExpressway";
	        IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
            IFeatureWorkspace pFeatureWorkspace = (IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			//get SplitLine            
			IFeatureClass plineFeatureClass = pFeatureWorkspace.openFeatureClass(polylineFileNameString); 
			int lineCount = plineFeatureClass.featureCount(null);							      			
			Map<String, String> topicMap = new HashMap<String, String>();
			obtainLDADocumentTopicFromExcel(topicMap);
			java.util.Set keySet = topicMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		IFeatureCursor pLineFeatureCursor = plineFeatureClass.search(null, false);//指针
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
    			String topicKey = (String)mapEntry.getKey();
    			int topicNumber = Integer.parseInt(topicKey.substring(5));
    			String probabilityDistributionStr = topicMap.get(topicKey);//概率分布
    			String[]probabilityArray = probabilityDistributionStr.split(";");    			
    			for (int j = 0; j < lineCount; j ++) {
    				IFeature plineFeature = pLineFeatureCursor.nextFeature();
    				IGeometry plineGeometry = plineFeature.getShape(); 
    				IPolyline polyline = (IPolyline)plineGeometry;
    				System.out.print("主题" + topicKey + ":处理第" + j + ":" + lineCount + "行元素\n");
    				//获得道路名字信息				
    				IFields plineFields = new Fields();
    				plineFields = plineFeature.getFields();
    				int LINKIDFiedIndex = plineFields.findField("LINKID");			
    				int LINKID = (Integer)plineFeature.getValue(LINKIDFiedIndex);//路段编号
    				int topicFieldIndex = plineFields.findField("Topic");
    				int topicVal = (Integer)plineFeature.getValue(topicFieldIndex);
    				int topicProbabilityFieldIndex = plineFields.findField("TopicProbability");
    				int topicProbability = (Integer)plineFeature.getValue(topicProbabilityFieldIndex);
    	 			for (int i = 0; i < probabilityArray.length; i++) {
    					String[]tempArray = probabilityArray[i].split(",");
    					int processLinkID = Integer.parseInt(tempArray[0]);
    					double processTopicProbability = Double.parseDouble(tempArray[1]);
    					if (LINKID == processLinkID) {
    						if (processTopicProbability > topicProbability) {
    							plineFeature.setValue(topicFieldIndex, topicNumber);//字段中写入值
    							plineFeature.setValue(topicProbabilityFieldIndex, processTopicProbability);
        						plineFeature.store();
        						break;
							}
    						
    					}	
    	 			}
    			}        		
        	}

        	System.out.print("done!");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 * 处理方向主题词分布情况
	 */
	public void tripDirectionTopicTermDistribution(){
		try {
			//Step 1: Initialize the Java Componet Object Model (COM) Interop.
	        EngineInitializer.initializeEngine();
	        //Step 2: Initialize an ArcGIS license.
	        AoInitialize aoInit = new AoInitialize();
	        initializeArcGISLicenses(aoInit);
	        String path = "F:\\faming\\esri\\roadNetwork.gdb";
	        String polylineFileNameString = "WuhanRingExpressway";
	        IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
            IFeatureWorkspace pFeatureWorkspace = (IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			//get SplitLine            
			IFeatureClass plineFeatureClass = pFeatureWorkspace.openFeatureClass(polylineFileNameString); 
			int lineCount = plineFeatureClass.featureCount(null);							      			
			Map<String, String> topicMap = new HashMap<String, String>();
			obtainLDATopicTermFromExcel(topicMap);
			java.util.Set keySet = topicMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		IFeatureCursor pLineFeatureCursor = plineFeatureClass.search(null, false);//指针
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
    			String topicKey = (String)mapEntry.getKey();
    			int topicNumber = Integer.parseInt(topicKey.substring(5));
    			String probabilityDistributionStr = topicMap.get(topicKey);//概率分布
    			String[]probabilityArray = probabilityDistributionStr.split(";");    			
    			for (int j = 0; j < lineCount; j ++) {
    				IFeature plineFeature = pLineFeatureCursor.nextFeature();
    				IGeometry plineGeometry = plineFeature.getShape(); 
    				IPolyline polyline = (IPolyline)plineGeometry;
    				System.out.print("主题" + topicKey + ":处理第" + j + ":" + lineCount + "行元素\n");
    				//获得道路名字信息				
    				IFields plineFields = new Fields();
    				plineFields = plineFeature.getFields();
    				int LINKIDFiedIndex = plineFields.findField("LINKID");			
    				int LINKID = (Integer)plineFeature.getValue(LINKIDFiedIndex);//路段编号
    				int topicFieldIndex = plineFields.findField("DirectionTopic");
    				int topicVal = (Integer)plineFeature.getValue(topicFieldIndex);
    				int topicProbabilityFieldIndex = plineFields.findField("DirectionTopicProbability");
    				double topicProbability = (Double)plineFeature.getValue(topicProbabilityFieldIndex);
    	 			for (int i = 0; i < probabilityArray.length; i++) {
    					String[]tempArray = probabilityArray[i].split(",");
    					int processLinkID = Integer.parseInt(tempArray[0]);
    					double processTopicProbability = Double.parseDouble(tempArray[1]);
    					if (LINKID == processLinkID) {
    						if (processTopicProbability > topicProbability) {
    							plineFeature.setValue(topicFieldIndex, topicNumber);//字段中写入值
    							plineFeature.setValue(topicProbabilityFieldIndex, processTopicProbability);
        						plineFeature.store();
        						break;
							}
    						
    					}	
    	 			}
    			}        		
        	}

        	System.out.print("done!");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 从Excel中获得term在主题中概率分布
	 * @param topicMap
	 */
	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap){
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//		String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\tripDirectionTopicTermDistribution.xlsx";
		double threshVal = 0.1;//term在主题中分布概率的阈值
		XSSFSheet sheet;
        XSSFRow headRow;//标题行
        XSSFRow row;
        try { 
        	// 设置要读取的文件路径
			ifs = new FileInputStream(path);       	
			// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
			// 之后版本使用XSSFWorkbook（xlsx）
			wb = new XSSFWorkbook(ifs);
//	            sheet = wb.getSheet("documentTopicDistribution"); 
			sheet = wb.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
            headRow = sheet.getRow(0);// 获得sheet中第0行
            int columCount = headRow.getPhysicalNumberOfCells();
            String[] probDistributionArray = new String[10];//概率分布
            for (int p = 0; p < probDistributionArray.length; p++) {
            	probDistributionArray[p] = "";
			}           
            for (int i = 1; i <= rowCount; i++) {
            	row = sheet.getRow(i);// 获得sheet中第i行
            	String topicStr = row.getCell(0).getStringCellValue();
            	for (int j = 1; j < columCount; j++) {
            		// 获得行中的列，即单元格HSSFCell
					XSSFCell cell = row.getCell(j);
					// 获得单元格中的值
					double cellVal = -1;
					try {
						cellVal = cell.getNumericCellValue();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
				  		System.out.println(e.getMessage());
				  		continue;
					}
					
					if (cellVal > threshVal) {
						String probability = String.format("%.3f", cellVal);//概率值					
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//取整数
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("开始写term主题概率分布数据：" + "\n");  
        }
        catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	private void initializeArcGISLicenses(AoInitialize aoInit){
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
	
	
}
