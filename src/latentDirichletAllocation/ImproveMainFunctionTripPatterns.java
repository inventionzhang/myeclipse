package latentDirichletAllocation;

import importDataToDatabase.ImportDataToMySQL;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Output;

import org.apache.axis2.jaxws.description.xml.handler.TrueFalseType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omg.CORBA.INTERNAL;

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

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import mapMatchingGPS.DatabaseFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.MapMatchNode;
import mapMatchingGPS.TaxiGPS;
import jxl.*;


/**
 * 
 * @author whu
 * 此方法是对MainFunctionTripPatterns的改进
 *
 */
public class ImproveMainFunctionTripPatterns {
	
	public static void main(String[] args){
//		//提取上客点与下客点
//		(new ImproveMainFunctionTripPatterns()).arrestPointProcess();		
//		System.out.print("提取完成!");
//		
		//对提取的上客点、下客点处理,计算trip的distance和direction
		//并对天河机场附近路段做特殊处理，更新天河机场附近路段
//		(new ImproveMainFunctionTripPatterns()).tripPatternsProcess();
//		System.out.print("数据处理完成!");	
		
//		//输出每天每小时的上客点数和下客点数
//		(new MainFunctionTripPatterns()).extractPickupsCount();
		
//		提取距离分布
//		(new MainFunctionTripPatterns()).extractDistanceDistribution();
		
		//距离分布在时间上的差异性
//		(new MainFunctionTripPatterns()).extractDistanceTemporalDistribution();
		
		//excel处理
//		(new MainFunctionTripPatterns()).excelTripDistanceProcess();//出行距离相似性
//		(new ImproveMainFunctionTripPatterns()).excelTripDirectionProcess();//提取trip，包括时间、起点、终点
//		(new ImproveMainFunctionTripPatterns()).extractTripDestinationFromExcelEveryHour();//每隔1小时提取trip下客点	
//		(new ImproveMainFunctionTripPatterns()).extractTripDestinationFromExcelAccordingEvery6Hours();//每隔6小时提取trip下客点
		
//		(new ImproveMainFunctionTripPatterns()).extractAllTripDestinationFromExcel();//提取某时间区间内的所有的trip下客点
		(new ImproveMainFunctionTripPatterns()).tripTopicTermDistribution();//处理主题词分布
		
//		(new ImproveMainFunctionTripPatterns()).excelTripDirectionProcessEverySixHour();//提取trip，包括时间(6小时)、起点、终点		
//		(new ImproveMainFunctionTripPatterns()).processShapeFile();//处理shapefile文件，在shapefile文件中添加字段
		
		System.out.print("done!");
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
	 * 根据时空特点提取xls中某天tirp的下客点(dropoffs)：excel中字段格式为taxID,localTime,dropoffsLinkID
	 * 1.excel表首先要按照taxiID进行排序
	 * 2.路段ID为5为数字字符
	 * 处理结果为每个taxiID在每个时段内(此时段指的是1个小时)对应的系列term(下客点)
	 * 按照一个小时时间间隔提取
	 * 00-01:1
	 * 01-02:2
	 * 02-03:3
	 * 03-04:4
	 * ……
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 */
	public void extractTripDestinationFromExcelEveryHour(){
		try {			
			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602\\0602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xls为要读取的excel文件名
	            book = Workbook.getWorkbook(new File(path));            
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后，比较奇葩)
	            //前提是sheet按LinkID分类统计
	            sheet = book.getSheet("0602dropoffsProcess");
	            int rowCount = sheet.getRows();
	            //获取第一行单元格
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(2, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段taxi为索引，存储时间、trip起终点
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//读取excel 文件里的时间格式数据时,24时制会自动转换为12时制,以下为解决办法
	            	//第一次读取时会读到正确的时间，接下来会读到相差8小时的时间，不知到为什么
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,转换为24小时时间制
	            	Date mydate = curDateCell.getDate();
	            	String curLocalTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);            	
	            	String hourStr = curLocalTimeStr.substring(11, 13);
//	            	int intHour = Integer.parseInt(hourStr);
//	            	int timePeriod = obtainHourPeriod(intHour);
//	            	String pickupLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String proPickupStreetName = processStreetName(pickupLinkIDStr);	            	
	            	String dropoffLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String proDropoffStreetName = processStreetName(dropoffLinkIDStr);
	            	String term = hourStr + dropoffLinkIDStr;
	            	
	            	tripDistArraylist.add(term);
	            	for (int j = i + 1; j < rowCount; j++) {	            		
	            		String nextTaxiIDStr = sheet.getCell(0, j).getContents();
		            	String []arrStr2 = nextTaxiIDStr.split("-");
		            	int nextTaxiID = Integer.parseInt(arrStr2[1]);
		            	DateCell nextDdateCell = (DateCell) sheet.getCell(1, j);
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,转换为24小时时间制		            	
		            	String nexthourStr = nextLocalTimeStr.substring(11, 13);
//		            	int nextIntHour = Integer.parseInt(nexthourStr);
//		            	int nextTimePeriod = obtainHourPeriod(nextIntHour);
//		            	String nextpickupLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproPickupStreetName = processStreetName(nextpickupLinkIDStr);		            	
		            	String nextdropoffLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproDropoffStreetName = processStreetName(nextdropoffLinkIDStr);
		            	term = nexthourStr + nextdropoffLinkIDStr;
						if (curTaxiID == nextTaxiID) {
							tripDistArraylist.add(term);
						}
						else {
							i = j - 1;
							break;
						}	            		
					}
	            	tripMap.put(curTaxiID, tripDistArraylist);					
				}
	            
	            //写入txt文件
	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602\\20140602tripDropoffs.txt";
	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String headDescription = "taxiID" + "," + "trip" + "\r\n";
				write.append(headDescription);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
	            
	            java.util.Set keySet = tripMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		int key = (Integer)mapEntry.getKey();
	        		if (key == 23923) {
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
	 * 根据时空特点提取xls中某天tirp的下客点(dropoffs)：excel中字段格式为taxID,localTime,dropoffsLinkID
	 * 1.excel表首先要按照taxiID进行排序
	 * 2.路段ID为5为数字字符
	 * 处理结果为每个taxiID在每个时段内(此时段指的是6个小时)对应的系列term(下客点)
	 * 按照6小时时间间隔提取
	 * 04-10:1
	 * 10-16:2
	 * 16-22:3
	 * 其它:4
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 * 用JAVA处理excel时间有问题，非常有问题，不稳定
	 */
	public void extractTripDestinationFromExcelAccordingEvery6Hours(){
		try {			
			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\0602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xls为要读取的excel文件名
	            book = Workbook.getWorkbook(new File(path));            
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后，比较奇葩)
	            //前提是sheet按LinkID分类统计
	            sheet = book.getSheet("0602dropoffsProcess");
	            int rowCount = sheet.getRows();
	            //获取第一行单元格
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(2, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段taxi为索引，存储时间、trip起终点
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//读取excel 文件里的时间格式数据时,24时制会自动转换为12时制,以下为解决办法
	            	//第一次读取时会读到正确的时间，接下来会读到相差8小时的时间，不知到为什么
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,转换为24小时时间制
	            	Date mydate = curDateCell.getDate();
	            	String curLocalTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);            	
	            	String hourStr = curLocalTimeStr.substring(11, 13);
	            	int intHour = Integer.parseInt(hourStr);
	            	int timePeriod = obtainHourPeriod(intHour);
//	            	String pickupLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String proPickupStreetName = processStreetName(pickupLinkIDStr);	            	
	            	String dropoffLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String proDropoffStreetName = processStreetName(dropoffLinkIDStr);
	            	String term = timePeriod + dropoffLinkIDStr;
	            	
	            	tripDistArraylist.add(term);
	            	for (int j = i + 1; j < rowCount; j++) {	            		
	            		String nextTaxiIDStr = sheet.getCell(0, j).getContents();
		            	String []arrStr2 = nextTaxiIDStr.split("-");
		            	int nextTaxiID = Integer.parseInt(arrStr2[1]);
		            	DateCell nextDdateCell = (DateCell) sheet.getCell(1, j);
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,转换为24小时时间制		            	
		            	String nexthourStr = nextLocalTimeStr.substring(11, 13);
		            	int nextIntHour = Integer.parseInt(nexthourStr);
		            	int nextTimePeriod = obtainHourPeriod(nextIntHour);
//		            	String nextpickupLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproPickupStreetName = processStreetName(nextpickupLinkIDStr);		            	
		            	String nextdropoffLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproDropoffStreetName = processStreetName(nextdropoffLinkIDStr);
		            	term = nextTimePeriod + nextdropoffLinkIDStr;
						if (curTaxiID == nextTaxiID) {
							tripDistArraylist.add(term);
						}
						else {
							i = j - 1;
							break;
						}	            		
					}
	            	tripMap.put(curTaxiID, tripDistArraylist);					
				}
	            
	            //写入txt文件
	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\20140602tripDropoffs.txt";
	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String headDescription = "taxiID" + "," + "trip" + "\r\n";
				write.append(headDescription);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
	            
	            java.util.Set keySet = tripMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		int key = (Integer)mapEntry.getKey();
	        		if (key == 23923) {
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
	 * 从excel中提取所有trip终点
	 * 1.trip终点信息写入txt文件中
	 */
	public void extractAllTripDestinationFromExcel(){
		try {
			String exclePath1= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0602.xls";//读取的excel文件路径
			String txtOutPutPath1 = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0602From17To21.txt";//写入的txt文件路径
			String sheetNameStr1 = "0602dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap1 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath1, sheetNameStr1, txtOutPutPath1, tripMap1);
			
			String exclePath2= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0603.xls";//读取的excel文件路径
			String txtOutPutPath2 = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0603From17To21.txt";//写入的txt文件路径
			String sheetNameStr2 = "0603dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap2 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath2, sheetNameStr2, txtOutPutPath2, tripMap2);
			mergeMap(tripMap1, tripMap2);
			
			String exclePath3= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0604.xls";//读取的excel文件路径
			String txtOutPutPath3 = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0604From17To21.txt";//写入的txt文件路径
			String sheetNameStr3 = "0604dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap3 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath3, sheetNameStr3, txtOutPutPath3, tripMap3);
			mergeMap(tripMap1, tripMap3);
			
			String exclePath4= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0605.xls";//读取的excel文件路径
			String txtOutPutPath4 = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0605From17To21.txt";//写入的txt文件路径
			String sheetNameStr4 = "0605dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap4 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath4, sheetNameStr4, txtOutPutPath4, tripMap4);
			//所有tripMap合并到一个tripMap中mergerTripMap
			Map<Integer, ArrayList<String>> mergerTripMap = new HashMap<Integer, ArrayList<String>>();
			mergeMap(tripMap1, tripMap4);
			
			String mergeTxtOutPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs060205From17To21.txt";//写入的txt文件路径
			tripInfosToTxtFile(tripMap1, mergeTxtOutPutPath);
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
		
		
	}
	
	/**
	 * 所有trip信息都合并到tripMap1中
	 * tripMap2中的任一key与tripMap1中的所有key比较
	 * 如果tripMap2中的key与tripMap1中的key相同，则将tripMap2中key对应的信息合并到tripMap1中相应的key中
	 * 否则，将tripMap2中的key以及对应信息直接加入到tripMap1中
	 * @param tripMap1
	 * @param tripMap2
	 */
	public void mergeMap(Map<Integer, ArrayList<String>> tripMap1, Map<Integer, ArrayList<String>> tripMap2){
		try {
			java.util.Set keySet2 = tripMap2.entrySet();
			Iterator iterator2 = (Iterator) keySet2.iterator();					  
        	while (iterator2.hasNext()) {
        		Map.Entry mapEntry2 = (Map.Entry) iterator2.next();
        		int key2 = (Integer)mapEntry2.getKey();
        		boolean isHasSameKey = false;//判段是否有相同key
        		
        		java.util.Set keySet1 = tripMap1.entrySet();
    			Iterator iterator1 = (Iterator) keySet1.iterator();
    			while (iterator1.hasNext()) {
            		Map.Entry mapEntry1 = (Map.Entry) iterator1.next();
            		int key1 = (Integer)mapEntry1.getKey();
            		if (key1 == key2) {
            			isHasSameKey = true;
            			ArrayList<String> tripDestArrayList1 = tripMap1.get(key1);//tripMap1中信息           			
            			ArrayList<String> tripDestArrayList2 = tripMap2.get(key2);//tripMap2中信息
            			for (int i = 0; i < tripDestArrayList2.size(); i++) {
            				String tempTripDestStr2 = tripDestArrayList2.get(i);
            				tripDestArrayList1.add(tempTripDestStr2);          				
        				}            			
            			break;
					}
    			}
    			//不存在相同key
    			if (!isHasSameKey) { 
    				ArrayList<String> tripDestArrayList2 = tripMap2.get(key2);
    				tripMap1.put(key2, tripDestArrayList2); 				
				}
        	}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 * 提取某时间区间内的trip下客点
	 * 比如：提取在[4,10)内的下客点
	 * 路段编号为5位数字
	 * 
	 * @tripMap 存储通行信息
	 */
	public void extractTripDestinationFromExcelAccordingTimeInterval(String exclePath, String sheetNameStr, String txtOutPutPath,
			Map<Integer, ArrayList<String>> tripMap){
		try {			
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        
            //t.xls为要读取的excel文件名
            book = Workbook.getWorkbook(new File(exclePath));            
            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后，比较奇葩)
            //前提是sheet按LinkID分类统计
            sheet = book.getSheet(sheetNameStr);
            int rowCount = sheet.getRows();
            //获取第一行单元格
            cellTaxiID = sheet.getCell(0, 0);
            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
            cellDropoffsLinkID = sheet.getCell(2, 0); 
            
//            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段taxi为索引，存储时间、trip起终点
            for (int i = 1; i < rowCount; i++) {
            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	
            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
            	String []arrStr = curTaxiIDStr.split("-");
            	int curTaxiID = Integer.parseInt(arrStr[1]);
            	//读取excel 文件里的时间格式数据时,24时制会自动转换为12时制,以下为解决办法
            	//第一次读取时会读到正确的时间，接下来会读到相差8小时的时间，不知到为什么
            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
            	Date mydate = curDateCell.getDate();
            	String curLocalTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);            	
            	String hourStr = curLocalTimeStr.substring(11, 13);
            	int intHour = Integer.parseInt(hourStr);
            	int timePeriod = obtainHourPeriod17To21(intHour);//	            	            	
            	String dropoffLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String proDropoffStreetName = processStreetName(dropoffLinkIDStr);
            	if (timePeriod == 1) {
            		String term = dropoffLinkIDStr;		            	
	            	tripDistArraylist.add(term);
				}
            	
            	for (int j = i + 1; j < rowCount; j++) {	            		
            		String nextTaxiIDStr = sheet.getCell(0, j).getContents();
	            	String []arrStr2 = nextTaxiIDStr.split("-");
	            	int nextTaxiID = Integer.parseInt(arrStr2[1]);
	            	DateCell nextDdateCell = (DateCell) sheet.getCell(1, j);
	            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,转换为24小时时间制		            	
	            	String nexthourStr = nextLocalTimeStr.substring(11, 13);
	            	int nextIntHour = Integer.parseInt(nexthourStr);
	            	int nextTimePeriod = obtainHourPeriod17To21(nextIntHour);
//		            	String nextpickupLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproPickupStreetName = processStreetName(nextpickupLinkIDStr);		            	
	            	String nextdropoffLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextproDropoffStreetName = processStreetName(nextdropoffLinkIDStr);
	            	if (nextTimePeriod == 1) {
	            		String term = nextdropoffLinkIDStr;
	            		if (curTaxiID == nextTaxiID) {
							tripDistArraylist.add(term);
						}
						else {
							i = j - 1;
							break;
						}
					}	            		
				}
            	tripMap.put(curTaxiID, tripDistArraylist);					
			}
            //信息写入txt文件
            tripInfosToTxtFile(tripMap, txtOutPutPath);
            book.close();	
            
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 以路段taxi为索引，将信息写入txt文件
	 */
	public void tripInfosToTxtFile( Map<Integer, ArrayList<String>> tripMap, String outPutPath){
		try {
			//写入txt文件
            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String headDescription = "taxiID" + "," + "trip" + "\r\n";
			write.append(headDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));
            
            java.util.Set keySet = tripMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
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
            
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
	}
	
	
	
	/**
	 * 根据时空特点提取xls中某天tirp：excel中字段格式为taxID,localTime,pickupsLinkID,dropoffsLinkID
	 * excel表首先要按照taxiID进行排序
	 * 处理结果为每个taxiID在每个时段内(此时段指的是1个小时)对应的系列term
	 * 按照一个小时时间间隔提取
	 * 00-06:1
	 * 06-12:2
	 * 12-18:3
	 * 18-24:4
	 * 用JAVA处理exl时间有问题，非常有问题，不稳定
	 * 用JAVA处理exl时间有问题，非常有问题，不稳定
	 * 用JAVA处理exl时间有问题，非常有问题，不稳定
	 */
	public void excelTripDirectionProcess(){
		try {			
			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection0602streetName\\tripDirection0602streetName.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xls为要读取的excel文件名
	            book = Workbook.getWorkbook(new File(path));            
	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后)
	            //前提是sheet按LinkID分类统计
//	            sheet = book.getSheet(3); 
	            sheet = book.getSheet("pickupsAndDropoffsProcess");
	            int rowCount = sheet.getRows();
	            //获取第一行单元格
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(3, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段taxi为索引，存储时间、trip起终点
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//读取excel 文件里的时间格式数据时,24时制会自动转换为12时制,以下为解决办法
	            	//第一次读取时会读到正确的时间，接下来会读到相差8小时的时间，不知到为什么
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,转换为24小时时间制
	            	Date mydate = curDateCell.getDate();
	            	String curLocalTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);            	
	            	String hourStr = curLocalTimeStr.substring(11, 13);
	            	int intHour = Integer.parseInt(hourStr);
	            	int timePeriod = obtainHourPeriod(intHour);
	            	String pickupLinkIDStr = sheet.getCell(2, i).getContents();
	            	String proPickupStreetName = processStreetName(pickupLinkIDStr);	            	
	            	String dropoffLinkIDStr = sheet.getCell(3, i).getContents();
	            	String proDropoffStreetName = processStreetName(dropoffLinkIDStr);
	            	String term = timePeriod + proPickupStreetName + "-" + proDropoffStreetName;
	            	
	            	tripDistArraylist.add(term);
	            	for (int j = i + 1; j < rowCount; j++) {	            		
	            		String nextTaxiIDStr = sheet.getCell(0, j).getContents();
		            	String []arrStr2 = nextTaxiIDStr.split("-");
		            	int nextTaxiID = Integer.parseInt(arrStr2[1]);
		            	DateCell nextDdateCell = (DateCell) sheet.getCell(1, j);
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,转换为24小时时间制		            	
		            	String nexthourStr = nextLocalTimeStr.substring(11, 13);
		            	int nextIntHour = Integer.parseInt(nexthourStr);
		            	int nextTimePeriod = obtainHourPeriod(nextIntHour);
		            	String nextpickupLinkIDStr = sheet.getCell(2, j).getContents();
		            	String nextproPickupStreetName = processStreetName(nextpickupLinkIDStr);		            	
		            	String nextdropoffLinkIDStr = sheet.getCell(3, j).getContents();
		            	String nextproDropoffStreetName = processStreetName(nextdropoffLinkIDStr);
		            	term = nextTimePeriod + nextproPickupStreetName + "-" + nextproDropoffStreetName;
						if (curTaxiID == nextTaxiID) {
							tripDistArraylist.add(term);
						}
						else {
							i = j - 1;
							break;
						}	            		
					}
	            	tripMap.put(curTaxiID, tripDistArraylist);					
				}
	            
	            //写入txt文件
	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection0602streetName\\20140602trip.txt";
	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String headDescription = "taxiID" + "," + "trip" + "\r\n";
				write.append(headDescription);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
	            
	            java.util.Set keySet = tripMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		int key = (Integer)mapEntry.getKey();
	        		if (key == 23923) {
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
	 * 去掉括弧，只考虑一个括弧的情况
	 * @param streetNameStr
	 * @return
	 */
	public String processStreetName(String streetNameStr){
		try {
			//去掉括号
        	if (streetNameStr.contains("（")) {
        		//只考虑有一个括号情况，去掉括号
				String []StrArray = streetNameStr.split("（");
				String leftStr = StrArray[0];
				String rightStr = StrArray[1];					
				//右括弧
				if (rightStr.contains("）")) {
					String []tStrArray = rightStr.split("）");
					String ttStr = "";
					for (int i = 0; i < tStrArray.length; i++) {
						rightStr = ttStr + tStrArray[i];
					}
//					String tleftStr = tStrArray[0];
//					String trightStr = tStrArray[1];
//					rightStr = tleftStr + trightStr;
				}
				streetNameStr = leftStr + rightStr;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		return streetNameStr;
		
	}
	
	/**
	 * 返回时间段信息
	 * 区间为左闭右开
	 * 4-10时:1
	 * 10-16时:2
	 * 16-22:3
	 * 其它:4
	 * @param intHour
	 */
	public int obtainHourPeriod(int intHour){
		int timePeriod = -1;
		try {
			if(intHour >= 4 && intHour < 10){
				timePeriod = 1;
			}
			else if (intHour >= 10 && intHour < 16) {
				timePeriod = 2;				
			}
			else if (intHour >= 16 && intHour < 22) {
				timePeriod = 3;
			}
			else {
				timePeriod = 4;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		return timePeriod;
	}
	
	/**
	 * 判断时间是否为17点到21点之间
	 * 如果是，则返回1，否则返回-1
	 * [17 21)
	 * @param intHour
	 * @return
	 */
	public int obtainHourPeriod17To21(int intHour){
		int timePeriod = -1;
		try {
			if(intHour >= 17 && intHour < 21){
				timePeriod = 1;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		return timePeriod;
	}
	
	/**
	 * 根据时空特点提取xls xls xls中某天tirp：excel中字段格式为taxID,localTime,pickupsLinkID,dropoffsLinkID
	 * excel表首先要按照taxiID进行排序
	 * 处理结果为每个taxiID在每个时段内对应的系列term 
	 */
	public void excelTripDirectionProcessEverySixHour(){
//		try {
//			
//			String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\2014060203.xlsx";
//			int num = 0;
//	        Workbook book;
//	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
//	        XSSFWorkbook workbook = null;
//			InputStream ifs = null;
//			OutputStream ofs = null;
//			XSSFSheet sheet;
//	        XSSFRow headRow;//标题行
//	        XSSFRow row;
//	        
//	        
////	        XSSFWorkbook wb = null;
////			InputStream ifs = null;
////			OutputStream ofs = null;
////			double threshVal = 0.1;//文档中主题分布概率的阈值
////			XSSFSheet sheet;
////	        XSSFRow headRow;//标题行
////	        XSSFRow row;
////	        Map<String, String> topicMap = new HashMap<String, String>();//主题以及主题对应的路段ID,以及概率
////	        try { 
////	        	// 设置要读取的文件路径
////				ifs = new FileInputStream(path);       	
////				// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
////				// 之后版本使用XSSFWorkbook（xlsx）
////				wb = new XSSFWorkbook(ifs);
//////		            sheet = wb.getSheet("documentTopicDistribution"); 
////				sheet = wb.getSheetAt(0);
////	            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
////	            headRow = sheet.getRow(0);// 获得sheet中第0行
////	            int columCount = headRow.getPhysicalNumberOfCells();
////	            String[] probDistributionArray = new String[10];//概率分布
////	            for (int p = 0; p < probDistributionArray.length; p++) {
////	            	probDistributionArray[p] = "";
////				}	            
////	            for (int i = 1; i <= rowCount; i++) {
////	            	row = sheet.getRow(i);// 获得sheet中第i行
////	            	String topicStr = row.getCell(0).getStringCellValue();
////	            	for (int j = 1; j < columCount; j++) {
////	            		// 获得行中的列，即单元格HSSFCell
////						XSSFCell cell = row.getCell(j);
////						// 获得单元格中的值
////						double cellVal = cell.getNumericCellValue();
////						if (cellVal > threshVal) {
////							String probability = String.format("%.3f", cellVal);						
////							double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
////							String linkID = String.valueOf((int)ttlinkIDStr);//取整数
////							System.out.println(linkID + "," + probability + "\n");
////							probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
////						}		            		
////					}
////	            	topicMap.put(topicStr, probDistributionArray[i - 1]);
////				}	            	            
////	            System.out.print("开始写文档主题概率分布数据：" + "\n");	        
////	            
//	            
//	            
//	        
//	            
//	        
//	        try { 
//	            //t.xls为要读取的excel文件名
//	            book = Workbook.getWorkbook(new File(path));            
//	            //获得第一个工作表对象(ecxel中sheet的编号从0开始,0,1,2,3,....,而且列号在前，行号在后)
//	            //前提是sheet按LinkID分类统计
//	            ifs = new FileInputStream(path);       	
//				// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
//				// 之后版本使用XSSFWorkbook（xlsx）
//	            workbook = new XSSFWorkbook(ifs);
//				sheet = workbook.getSheet("pickupDropoffProcess");
//	            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
//	            headRow = sheet.getRow(0);// 获得sheet中第0行
//				
////	            sheet = book.getSheet("pickupsAndDropoffslinkIDProcess");
////	            int rowCount = sheet.getRows();
//	            //获取第一行单元格
//	            cellTaxiID = sheet.getCell(0, 0);
//	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
//	            cellDropoffsLinkID = sheet.getCell(3, 0); 
//	            
//	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//以路段taxi为索引，存储时间、trip起终点
//	            for (int i = 1; i < rowCount; i++) {
//	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//存储通行距离	
//	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
//	            	String []arrStr = curTaxiIDStr.split("-");
//	            	int curTaxiID = Integer.parseInt(arrStr[1]);
//	            	if (curTaxiID ==17568) {
//						System.out.print("");
//					}
//	            	//读取excel 文件里的时间格式数据时,24时制会自动转换为12时制,以下为解决办法
//	            	//第一次读取时会读到正确的时间，接下来会读到相差8小时的时间，不知到为什么
//	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
////	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,转换为24小时时间制
//	            	Date mydate = curDateCell.getDate();
//	            	String curLocalTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);
//	            	
//	            	String hourStr = curLocalTimeStr.substring(11, 13);
//	            	String pickupLinkIDStr = sheet.getCell(2, i).getContents();
//	            	String dropoffLinkIDStr = sheet.getCell(3, i).getContents();
//	            	String term = hourStr + pickupLinkIDStr + dropoffLinkIDStr;
//	            	
//	            	tripDistArraylist.add(term);
//	            	for (int j = i + 1; j < rowCount; j++) {	            		
//	            		String nextTaxiIDStr = sheet.getCell(0, j).getContents();
//		            	String []arrStr2 = nextTaxiIDStr.split("-");
//		            	int nextTaxiID = Integer.parseInt(arrStr2[1]);
//		            	DateCell nextDdateCell = (DateCell) sheet.getCell(1, j);
//		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,转换为24小时时间制
//		            	
//		            	String nexthourStr = nextLocalTimeStr.substring(11, 13);
//		            	String nextpickupLinkIDStr = sheet.getCell(2, j).getContents();
//		            	String nextdropoffLinkIDStr = sheet.getCell(3, j).getContents();
//		            	term = nexthourStr + nextpickupLinkIDStr + nextdropoffLinkIDStr;
//						if (curTaxiID == nextTaxiID) {
//							tripDistArraylist.add(term);
//						}
//						else {
//							i = j - 1;
//							break;
//						}	            		
//					}
//	            	tripMap.put(curTaxiID, tripDistArraylist);					
//				}
//	            
//	            //写入txt文件
//	            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602trip.txt";
//	            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
//				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
//				StringBuffer write = new StringBuffer();
//				String headDescription = "taxiID" + "," + "trip" + "\r\n";
//				write.append(headDescription);
//				bufferStream.write(write.toString().getBytes("UTF-8"));	
//	            
//	            java.util.Set keySet = tripMap.entrySet();
//				Iterator iterator = (Iterator) keySet.iterator();
//	        	while (iterator.hasNext()) {
//	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
//	        		int key = (Integer)mapEntry.getKey();
//	        		if (key == 23923) {
//	        			System.out.print(" ");
//					}
//	        		ArrayList<String> tripDistArrayList = tripMap.get(key);
//	        		String tripDistStr = "";
//	        		for (int i = 0; i < tripDistArrayList.size(); i++) {
//	        			tripDistStr = tripDistArrayList.get(i) + " " + tripDistStr;
//					}
//	        		tripDistStr = key + "," + tripDistStr + "\r\n";
//	        		write = new StringBuffer();				
//    				write.append(tripDistStr);
//    				bufferStream.write(write.toString().getBytes("UTF-8"));	
//	        	}
//	        	bufferStream.flush();      
//				bufferStream.close(); 
//				outputStream.close();
//				System.out.print("数据写入结束" + "\n");
//	            book.close();
//			
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//		  		System.out.println(e.getMessage());
//			}
//		}
//		 catch (Exception e2) {
//			// TODO: handle exception
//			e2.printStackTrace();
//	  		System.out.println(e2.getMessage());
//		}
	}
	
	/**
	 * 时间转换函数：
	 * 读取excel 文件里的时间格式数据时,24时制会自动转化为12时制,解决办法，转换为24小时时间制
	 * @param formatecell
	 * @return
	 */
	public String excelFormateTime(DateCell dateCell ) {
		String dateTimeStr = "";
		try {
    		Date mydate = dateCell.getDate();
        	long time = (mydate.getTime() / 1000) - 60 * 60 * 8;
        	mydate.setTime(time * 1000);
        	dateTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mydate);
    		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		return dateTimeStr;
	 }
	
	/**
	 * 读取文档主题分布excel文件(xlsx)
	 * topicMap 主题以及主题对应的路段ID,以及概率
	 */
	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap , String LDATopicPath){
		
		XSSFWorkbook wb = null;
		InputStream inputStream = null;
		OutputStream ofs = null;
		double threshVal = 0.005;//文档中主题分布概率的阈值
		XSSFSheet sheet;
        XSSFRow headRow;//标题行
        XSSFRow row;
        try { 
        	// 设置要读取的文件路径
        	inputStream = new FileInputStream(LDATopicPath);       	
			// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
			// 之后版本使用XSSFWorkbook（xlsx）
			wb = new XSSFWorkbook(inputStream);
	        sheet = wb.getSheet("topic-term-distributionsProcess"); 
//			sheet = wb.getSheetAt(0);
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
					double cellVal = 0;
					try {
						cellVal = cell.getNumericCellValue();
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					}					
					if (cellVal > threshVal) {
						String probability = String.format("%.3f", cellVal);						
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//取整数
////						//路段ID处理
//						String tempLinkIDStr = linkID.substring(1);
//						int processLinkID = Integer.parseInt(tempLinkIDStr);
//						String processLinkIDStr = String.valueOf(linkID);
						
						int processLinkID = Integer.parseInt(linkID);
//						System.out.println(processLinkIDStr + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + processLinkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	
            /**
             * 写入txt文件
             */
            
//            System.out.print("开始写文档主题概率分布数据：" + "\n");            
//            //写入txt文件
//            String outPutPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\20140602TopicTermDistribution.txt";
//            FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
//			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
//			StringBuffer write = new StringBuffer();
//			String headDescription = "topic" + "," + "linkIDAndTopicDistribution" + "\r\n";
//			write.append(headDescription);
//			bufferStream.write(write.toString().getBytes("UTF-8"));           
//            java.util.Set keySet = topicMap.entrySet();
//			Iterator iterator = (Iterator) keySet.iterator();
//        	while (iterator.hasNext()) {
//        		Map.Entry mapEntry = (Map.Entry) iterator.next();
//        		String key = (String)mapEntry.getKey();
//        		String probabilityDistributionStr = topicMap.get(key);//概率分布
//        		probabilityDistributionStr = key + "：" + probabilityDistributionStr + "\r\n";
//        		write = new StringBuffer();				
//				write.append(probabilityDistributionStr);
//				bufferStream.write(write.toString().getBytes("UTF-8"));	
//        	}
//        	bufferStream.flush();      
//			bufferStream.close(); 
//			outputStream.close();
//			System.out.print("数据写入结束" + "\n");
			
			
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
	 * 处理主题词分布情况
	 * 赋予shapefile中每个路段所属主题
	 * 并给每个主题赋予概率值
	 */
	public void tripTopicTermDistribution(){
		try {
			//Step 1: Initialize the Java Componet Object Model (COM) Interop.
	        EngineInitializer.initializeEngine();
	        //Step 2: Initialize an ArcGIS license.
	        AoInitialize aoInit = new AoInitialize();
	        initializeArcGISLicenses(aoInit);
	        String path = "F:\\faming\\esri\\roadNetwork.gdb";
	        String polylineFileNameString = "WuhanRingExpressway";
//	        //dropoffsEverySixHours
//	        String topicNameFieldStr = "Topic";//主题字段名
//	        String topicProbabilityFieldStr = "TopicProbability";//主题概率名
	        
//	        //dropoffsFrom4To10
//	        String topicNameFieldStr = "TopicInterval";//主题字段名
//	        String topicProbabilityFieldStr = "TopicIntervalProb";//主题概率名
//	        String LDATopicPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffsFrom4To10\\LDAprocess.xlsx";
	        
	        //dropoffs060205From4To10  6月2日到5日
//	        String topicNameFieldStr = "Topic005";//主题字段名 0.05
//	        String topicNameFieldStr = "Topic2";//主题字段名
//	        String topicProbabilityFieldStr = "Topic2Prob";//主题概率名
	        
	        //dropoffs060205From17To21  6月2日到5日
	        String topicNameFieldStr = "Topic1721";//主题字段名
	        String topicProbabilityFieldStr = "Topic1721Prob";//主题概率名
	        String LDATopicPath = "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\LDAprocessFrom17To21.xlsx";
	        
	        
	        IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
            IFeatureWorkspace pFeatureWorkspace = (IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			//get SplitLine            
			IFeatureClass plineFeatureClass = pFeatureWorkspace.openFeatureClass(polylineFileNameString); 
			int lineCount = plineFeatureClass.featureCount(null);							      			
			Map<String, String> topicMap = new HashMap<String, String>();
			obtainLDATopicTermFromExcel(topicMap, LDATopicPath);
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
    				int topicFieldIndex = plineFields.findField(topicNameFieldStr);
    				int topicVal = (Integer)plineFeature.getValue(topicFieldIndex);
    				int topicProbabilityFieldIndex = plineFields.findField(topicProbabilityFieldStr);
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
	
//	/**
//	 * 从Excel中获得term在主题中概率分布
//	 * @param topicMap
//	 */
//	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap){
//		XSSFWorkbook wb = null;
//		InputStream ifs = null;
//		OutputStream ofs = null;
////		String path= "F:\\360云盘\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
//		String path= "F:\\360云盘\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\LDAprocess.xlsx";
//		double threshVal = 0.1;//term在主题中分布概率的阈值
//		XSSFSheet sheet;
//        XSSFRow headRow;//标题行
//        XSSFRow row;
//        try { 
//        	// 设置要读取的文件路径
//			ifs = new FileInputStream(path);       	
//			// HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
//			// 之后版本使用XSSFWorkbook（xlsx）
//			wb = new XSSFWorkbook(ifs);
////	            sheet = wb.getSheet("documentTopicDistribution"); 
//			sheet = wb.getSheetAt(0);
//            int rowCount = sheet.getLastRowNum();// 获取工作薄行数
//            headRow = sheet.getRow(0);// 获得sheet中第0行
//            int columCount = headRow.getPhysicalNumberOfCells();
//            String[] probDistributionArray = new String[10];//概率分布
//            for (int p = 0; p < probDistributionArray.length; p++) {
//            	probDistributionArray[p] = "";
//			}           
//            for (int i = 1; i <= rowCount; i++) {
//            	row = sheet.getRow(i);// 获得sheet中第i行
//            	String topicStr = row.getCell(0).getStringCellValue();
//            	for (int j = 1; j < columCount; j++) {
//            		// 获得行中的列，即单元格HSSFCell
//					XSSFCell cell = row.getCell(j);
//					// 获得单元格中的值
//					double cellVal = -1;
//					try {
//						cellVal = cell.getNumericCellValue();
//					} catch (Exception e) {
//						// TODO: handle exception
//				  		continue;
//					}
//					
//					if (cellVal > threshVal) {
//						
//						String probability = String.format("%.3f", cellVal);						
//						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
//						String linkID = String.valueOf((int)ttlinkIDStr);//取整数
//						//路段ID处理
//						String tempLinkIDStr = linkID.substring(1);
//						int processLinkID = Integer.parseInt(tempLinkIDStr);
//						String processLinkIDStr = String.valueOf(processLinkID);
//						
//						System.out.println(processLinkIDStr + "," + probability + "\n");
//						probDistributionArray[i - 1] = probDistributionArray[i - 1] + processLinkID + "," + probability + ";";
//						
//					}		            		
//				}
//            	topicMap.put(topicStr, probDistributionArray[i - 1]);
//			}
//            	            
//            System.out.print("开始写term主题概率分布数据：" + "\n");  
//        }
//        catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//	  		System.out.println(e.getMessage());
//		}		
//	}
	
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
