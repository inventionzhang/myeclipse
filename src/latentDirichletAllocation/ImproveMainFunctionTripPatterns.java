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
 * �˷����Ƕ�MainFunctionTripPatterns�ĸĽ�
 *
 */
public class ImproveMainFunctionTripPatterns {
	
	public static void main(String[] args){
//		//��ȡ�Ͽ͵����¿͵�
//		(new ImproveMainFunctionTripPatterns()).arrestPointProcess();		
//		System.out.print("��ȡ���!");
//		
		//����ȡ���Ͽ͵㡢�¿͵㴦��,����trip��distance��direction
		//������ӻ�������·�������⴦��������ӻ�������·��
//		(new ImproveMainFunctionTripPatterns()).tripPatternsProcess();
//		System.out.print("���ݴ������!");	
		
//		//���ÿ��ÿСʱ���Ͽ͵������¿͵���
//		(new MainFunctionTripPatterns()).extractPickupsCount();
		
//		��ȡ����ֲ�
//		(new MainFunctionTripPatterns()).extractDistanceDistribution();
		
		//����ֲ���ʱ���ϵĲ�����
//		(new MainFunctionTripPatterns()).extractDistanceTemporalDistribution();
		
		//excel����
//		(new MainFunctionTripPatterns()).excelTripDistanceProcess();//���о���������
//		(new ImproveMainFunctionTripPatterns()).excelTripDirectionProcess();//��ȡtrip������ʱ�䡢��㡢�յ�
//		(new ImproveMainFunctionTripPatterns()).extractTripDestinationFromExcelEveryHour();//ÿ��1Сʱ��ȡtrip�¿͵�	
//		(new ImproveMainFunctionTripPatterns()).extractTripDestinationFromExcelAccordingEvery6Hours();//ÿ��6Сʱ��ȡtrip�¿͵�
		
//		(new ImproveMainFunctionTripPatterns()).extractAllTripDestinationFromExcel();//��ȡĳʱ�������ڵ����е�trip�¿͵�
		(new ImproveMainFunctionTripPatterns()).tripTopicTermDistribution();//��������ʷֲ�
		
//		(new ImproveMainFunctionTripPatterns()).excelTripDirectionProcessEverySixHour();//��ȡtrip������ʱ��(6Сʱ)����㡢�յ�		
//		(new ImproveMainFunctionTripPatterns()).processShapeFile();//����shapefile�ļ�����shapefile�ļ�������ֶ�
		
		System.out.print("done!");
	}

	/**
	 * ��ȡ�Ͽ͵����¿͵㲢��ͼƥ�䡢����Ͽ͵����¿͵�����·�α�š����Ƶ���Ϣ��д�����ݿ�
	 * 1.���һ���ڵĳ��⳵ID
	 * 2.��ÿһ�����⳵ID�Ͽ͵����¿͵�һ������ݽ�����ȡ����д�����ݿ�
	 * 3.ѭ������ÿ�����ݣ�ֱ����ֹ���ڽ���
	 */
	public void arrestPointProcess(){
		String startTimeStr = "2014-06-02 00:00:00";//��ʼʱ��
		String endTimeStr = "2014-06-09 00:00:00";//��ֹʱ��
		String insertTableName = "trippattern";//�������	
		double totalStartTime = System.nanoTime();	
		LDAAssistFunction.obtainRoadNetworkRange(MapMatchAlgorithm.instance().juncCollArrayList);
		LDAAssistFunction ldaAssistFunction = new LDAAssistFunction();
		ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();	
		int timeInterval = 24 * 3600;//ÿ��һ��		
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
    	System.out.print("����������ʱ�䣺" + totalProcessTime + "s" + '\n');		
		System.out.print("");			
	}
	
	/**
	 * ������ȡ��Ϣ��������о�������з���
	 * ���з����Ͽ͵㵽�¿͵�֮��ֱ�߷���ķ�λ��
	 * ���о��룺�Ͽ͵㵽�¿͵�֮�����
	 * 1.������һ���ڵĳ��������
	 * 2.��ÿһ�����⳵һ���ڵ����ݽ��д���������з��򡢾���
	 * 3.ѭ��1,2��ֱ����ֹʱ��
	 */
	public void tripPatternsProcess(){		
		String startTimeStr = "2014-06-02 00:00:00";//��ʼʱ��
		String endTimeStr = "2014-06-09 00:00:00";//��ֹʱ��
		double totalStartTime = System.nanoTime();	
		try {
			String readTableName = "trippattern";//��ȡ�����
			String insertTableName = "trippatternprocess";//��������
			ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();
			int timeInterval = 24 * 3600;//ÿ��һ��		
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
    	System.out.print("����������ʱ�䣺" + totalProcessTime + "s" + '\n');
	}
	
	/**
	 * ����taxiGPSArrayList����ÿһ���Ͽ͵㡢�¿͵��direction��distance
	 * ����������trip����һ����Ϊ�Ͽ͵㣬�ڶ�����Ϊ�¿͵�
	 * 1.����������������������һ����Ϊ�Ͽ͵㣬�ڶ�����Ϊ�¿͵㣬��Ϊ��Ч�켣
	 * 2.�Թ켣��direction��distance
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
					String tripDirectionStr = String.format("%.4f", tripDirection);//����С�������λ����������������
					String tripDistanceStr = String.format("%.4f", tripDistance);
					double processTripDirection = Double.parseDouble(tripDirectionStr);
					double processtripDistance = Double.parseDouble(tripDistanceStr);
					String curTaxiTimeStr = curTaxiGPS.getLocalTime();
					String nextTaxiTimeStr = nextTaxiGPS.getLocalTime();
					
					//��ӻ�������GPS��Ư�����أ����⴦������·�α��22921
					int curTaxiStreetNo = curTaxiGPS.getBelongLineID();
					double curTaxiLogi = curTaxiGPS.getLongitude();
					double curTaxiLati = curTaxiGPS.getLatitude();
					int nextTaxiStreetNo = nextTaxiGPS.getBelongLineID();
					double nextTaxiLogi = nextTaxiGPS.getLongitude();
					double nextTaxiLati = nextTaxiGPS.getLatitude();
					if (curTaxiStreetNo == -1 && curTaxiLogi > 114.18 && curTaxiLogi < 114.22 && 
							curTaxiLati > 30.75 && curTaxiLati < 30.78) {
						curTaxiGPS.setBelongLineID(22921);
						curTaxiGPS.setBelongLinkName("��������");
					}
					if (nextTaxiStreetNo == -1 && nextTaxiLogi > 114.18 && nextTaxiLogi < 114.22 && 
							nextTaxiLati > 30.75 && nextTaxiLati < 30.78) {
						nextTaxiGPS.setBelongLineID(22921);
						nextTaxiGPS.setBelongLinkName("��������");
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
	 * ��ȡ�Ͽ͵������¿͵���
	 * 1.ÿ��һСʱ��ȡ�Ͽ���
	 */
	public void extractPickupsCount(){
		try {
			String startTimeStr = "2014-06-08 00:00:00";
			String endTimeStr = "2014-06-09 00:00:00";
			String tableName = "trippatternprocess";
			int timeInterval = 3600;//ÿ��һСʱ		
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				int pickUpsCount = DatabaseFunction.obtainPickUpsCount(subStartTimeStr, subEndTimeStr, tableName);
				System.out.print(subStartTimeStr + ":" +pickUpsCount + "\n");
				subStartTimeStr = subEndTimeStr;
			}
			System.out.print("done!�Ͽ͵�����ȡ����!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
	}
	
	/**
	 * ��ȡ����ֲ�
	 * 1.���밴��0.5km���ӣ���ȡ������Ŀ
	 */
	public void extractDistanceDistribution(){
		try {
//			������Ϊ��48466.84m
//			SELECT MAX(T_TripDistance) FROM `trippatternprocess` WHERE T_ChangedStatus = 1;//���������
			String tableName = "trippatternprocess";
			String timeStr = "2014-06-08 00:00:00";
			double distanceLowerLimit = 0;
			double distanceUpperLimit = 49000;
			double subDistanceLowerLimit = distanceLowerLimit;
			while (subDistanceLowerLimit < distanceUpperLimit) {
//				//1km���ڰ�100m���Ӿ���
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
			System.out.print("done!����ֲ���ȡ����!");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	/**
	 * ��ȡ������ʱ��ֲ��ϵĲ�����
	 * ÿСʱ�ھ���<=distance ��count
	 */
	public void extractDistanceTemporalDistribution(){
		try {
			String tableName = "trippatternprocess";
			double distanceLowerLimit = 20000;
			double distanceUpperLimit = 50000;
			String startTimeStr = "2014-06-02 00:00:00";
			String endTimeStr = "2014-06-09 00:00:00";
			int timeInterval = 3600;//ÿ��һСʱ		
			String subStartTimeStr = startTimeStr;
			while (!endTimeStr.equals(subStartTimeStr)) {
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(subStartTimeStr, timeInterval, endTimeArray);
				String subEndTimeStr = endTimeArray[0];
				int distanceCount = DatabaseFunction.obtainSatisfiedDistancePickupsCountInanHour(distanceLowerLimit,distanceUpperLimit, tableName, subStartTimeStr);//���ұ�
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
	 * ��ȡĳ���Ͽ͵��Ӧ��·�α���Լ����о���
	 * 
	 */
	public void excelTripDistanceProcess(){
		try {			
			String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellLinkID, cellTripDist;
	        try { 
	            //t.xlsΪҪ��ȡ��excel�ļ���
	            book = Workbook.getWorkbook(new File(path));            
	            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں�)
	            //ǰ����sheet��LinkID����ͳ��
//	            sheet = book.getSheet(3); 
	            sheet = book.getSheet("pickupsAndDropoffslinkID");
	            int rowCount = sheet.getRows();
	            //��ȡ��һ�е�Ԫ��
	            cellLinkID = sheet.getCell(0, 0);
	            cellTripDist = sheet.getCell(1, 0); 
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��IDΪ�������洢ͨ�о���
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	            	
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
	            
	            //д��txt�ļ�
	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602tripDirection.txt";
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
				System.out.print("����д�����" + "\n");
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
	 * ����ʱ���ص���ȡxls��ĳ��tirp���¿͵�(dropoffs)��excel���ֶθ�ʽΪtaxID,localTime,dropoffsLinkID
	 * 1.excel������Ҫ����taxiID��������
	 * 2.·��IDΪ5Ϊ�����ַ�
	 * ������Ϊÿ��taxiID��ÿ��ʱ����(��ʱ��ָ����1��Сʱ)��Ӧ��ϵ��term(�¿͵�)
	 * ����һ��Сʱʱ������ȡ
	 * 00-01:1
	 * 01-02:2
	 * 02-03:3
	 * 03-04:4
	 * ����
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 */
	public void extractTripDestinationFromExcelEveryHour(){
		try {			
			String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602\\0602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xlsΪҪ��ȡ��excel�ļ���
	            book = Workbook.getWorkbook(new File(path));            
	            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں󣬱Ƚ�����)
	            //ǰ����sheet��LinkID����ͳ��
	            sheet = book.getSheet("0602dropoffsProcess");
	            int rowCount = sheet.getRows();
	            //��ȡ��һ�е�Ԫ��
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(2, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��taxiΪ�������洢ʱ�䡢trip���յ�
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����Ϊ����취
	            	//��һ�ζ�ȡʱ�������ȷ��ʱ�䣬��������������8Сʱ��ʱ�䣬��֪��Ϊʲô
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,ת��Ϊ24Сʱʱ����
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
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,ת��Ϊ24Сʱʱ����		            	
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
	            
	            //д��txt�ļ�
	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602\\20140602tripDropoffs.txt";
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
				System.out.print("����д�����" + "\n");
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
	 * ����ʱ���ص���ȡxls��ĳ��tirp���¿͵�(dropoffs)��excel���ֶθ�ʽΪtaxID,localTime,dropoffsLinkID
	 * 1.excel������Ҫ����taxiID��������
	 * 2.·��IDΪ5Ϊ�����ַ�
	 * ������Ϊÿ��taxiID��ÿ��ʱ����(��ʱ��ָ����6��Сʱ)��Ӧ��ϵ��term(�¿͵�)
	 * ����6Сʱʱ������ȡ
	 * 04-10:1
	 * 10-16:2
	 * 16-22:3
	 * ����:4
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����excelʱ�������⣬�ǳ������⣬���ȶ�
	 */
	public void extractTripDestinationFromExcelAccordingEvery6Hours(){
		try {			
			String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\0602.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xlsΪҪ��ȡ��excel�ļ���
	            book = Workbook.getWorkbook(new File(path));            
	            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں󣬱Ƚ�����)
	            //ǰ����sheet��LinkID����ͳ��
	            sheet = book.getSheet("0602dropoffsProcess");
	            int rowCount = sheet.getRows();
	            //��ȡ��һ�е�Ԫ��
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(2, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��taxiΪ�������洢ʱ�䡢trip���յ�
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����Ϊ����취
	            	//��һ�ζ�ȡʱ�������ȷ��ʱ�䣬��������������8Сʱ��ʱ�䣬��֪��Ϊʲô
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,ת��Ϊ24Сʱʱ����
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
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,ת��Ϊ24Сʱʱ����		            	
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
	            
	            //д��txt�ļ�
	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\20140602tripDropoffs.txt";
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
				System.out.print("����д�����" + "\n");
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
	 * ��excel����ȡ����trip�յ�
	 * 1.trip�յ���Ϣд��txt�ļ���
	 */
	public void extractAllTripDestinationFromExcel(){
		try {
			String exclePath1= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0602.xls";//��ȡ��excel�ļ�·��
			String txtOutPutPath1 = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0602From17To21.txt";//д���txt�ļ�·��
			String sheetNameStr1 = "0602dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap1 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath1, sheetNameStr1, txtOutPutPath1, tripMap1);
			
			String exclePath2= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0603.xls";//��ȡ��excel�ļ�·��
			String txtOutPutPath2 = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0603From17To21.txt";//д���txt�ļ�·��
			String sheetNameStr2 = "0603dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap2 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath2, sheetNameStr2, txtOutPutPath2, tripMap2);
			mergeMap(tripMap1, tripMap2);
			
			String exclePath3= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0604.xls";//��ȡ��excel�ļ�·��
			String txtOutPutPath3 = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0604From17To21.txt";//д���txt�ļ�·��
			String sheetNameStr3 = "0604dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap3 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath3, sheetNameStr3, txtOutPutPath3, tripMap3);
			mergeMap(tripMap1, tripMap3);
			
			String exclePath4= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\0605.xls";//��ȡ��excel�ļ�·��
			String txtOutPutPath4 = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs0605From17To21.txt";//д���txt�ļ�·��
			String sheetNameStr4 = "0605dropoffsProcess";
			Map<Integer, ArrayList<String>> tripMap4 = new HashMap<Integer, ArrayList<String>>();
			extractTripDestinationFromExcelAccordingTimeInterval(exclePath4, sheetNameStr4, txtOutPutPath4, tripMap4);
			//����tripMap�ϲ���һ��tripMap��mergerTripMap
			Map<Integer, ArrayList<String>> mergerTripMap = new HashMap<Integer, ArrayList<String>>();
			mergeMap(tripMap1, tripMap4);
			
			String mergeTxtOutPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\tripDropoffs060205From17To21.txt";//д���txt�ļ�·��
			tripInfosToTxtFile(tripMap1, mergeTxtOutPutPath);
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
		
		
	}
	
	/**
	 * ����trip��Ϣ���ϲ���tripMap1��
	 * tripMap2�е���һkey��tripMap1�е�����key�Ƚ�
	 * ���tripMap2�е�key��tripMap1�е�key��ͬ����tripMap2��key��Ӧ����Ϣ�ϲ���tripMap1����Ӧ��key��
	 * ���򣬽�tripMap2�е�key�Լ���Ӧ��Ϣֱ�Ӽ��뵽tripMap1��
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
        		boolean isHasSameKey = false;//�ж��Ƿ�����ͬkey
        		
        		java.util.Set keySet1 = tripMap1.entrySet();
    			Iterator iterator1 = (Iterator) keySet1.iterator();
    			while (iterator1.hasNext()) {
            		Map.Entry mapEntry1 = (Map.Entry) iterator1.next();
            		int key1 = (Integer)mapEntry1.getKey();
            		if (key1 == key2) {
            			isHasSameKey = true;
            			ArrayList<String> tripDestArrayList1 = tripMap1.get(key1);//tripMap1����Ϣ           			
            			ArrayList<String> tripDestArrayList2 = tripMap2.get(key2);//tripMap2����Ϣ
            			for (int i = 0; i < tripDestArrayList2.size(); i++) {
            				String tempTripDestStr2 = tripDestArrayList2.get(i);
            				tripDestArrayList1.add(tempTripDestStr2);          				
        				}            			
            			break;
					}
    			}
    			//��������ͬkey
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
	 * ��ȡĳʱ�������ڵ�trip�¿͵�
	 * ���磺��ȡ��[4,10)�ڵ��¿͵�
	 * ·�α��Ϊ5λ����
	 * 
	 * @tripMap �洢ͨ����Ϣ
	 */
	public void extractTripDestinationFromExcelAccordingTimeInterval(String exclePath, String sheetNameStr, String txtOutPutPath,
			Map<Integer, ArrayList<String>> tripMap){
		try {			
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        
            //t.xlsΪҪ��ȡ��excel�ļ���
            book = Workbook.getWorkbook(new File(exclePath));            
            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں󣬱Ƚ�����)
            //ǰ����sheet��LinkID����ͳ��
            sheet = book.getSheet(sheetNameStr);
            int rowCount = sheet.getRows();
            //��ȡ��һ�е�Ԫ��
            cellTaxiID = sheet.getCell(0, 0);
            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
            cellDropoffsLinkID = sheet.getCell(2, 0); 
            
//            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��taxiΪ�������洢ʱ�䡢trip���յ�
            for (int i = 1; i < rowCount; i++) {
            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	
            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
            	String []arrStr = curTaxiIDStr.split("-");
            	int curTaxiID = Integer.parseInt(arrStr[1]);
            	//��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����Ϊ����취
            	//��һ�ζ�ȡʱ�������ȷ��ʱ�䣬��������������8Сʱ��ʱ�䣬��֪��Ϊʲô
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
	            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,ת��Ϊ24Сʱʱ����		            	
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
            //��Ϣд��txt�ļ�
            tripInfosToTxtFile(tripMap, txtOutPutPath);
            book.close();	
            
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	/**
	 * ��·��taxiΪ����������Ϣд��txt�ļ�
	 */
	public void tripInfosToTxtFile( Map<Integer, ArrayList<String>> tripMap, String outPutPath){
		try {
			//д��txt�ļ�
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
			System.out.print("����д�����" + "\n");
            
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
		
	}
	
	
	
	/**
	 * ����ʱ���ص���ȡxls��ĳ��tirp��excel���ֶθ�ʽΪtaxID,localTime,pickupsLinkID,dropoffsLinkID
	 * excel������Ҫ����taxiID��������
	 * ������Ϊÿ��taxiID��ÿ��ʱ����(��ʱ��ָ����1��Сʱ)��Ӧ��ϵ��term
	 * ����һ��Сʱʱ������ȡ
	 * 00-06:1
	 * 06-12:2
	 * 12-18:3
	 * 18-24:4
	 * ��JAVA����exlʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����exlʱ�������⣬�ǳ������⣬���ȶ�
	 * ��JAVA����exlʱ�������⣬�ǳ������⣬���ȶ�
	 */
	public void excelTripDirectionProcess(){
		try {			
			String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection0602streetName\\tripDirection0602streetName.xls";
			int num = 0;
	        Sheet sheet;
	        Workbook book;
	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
	        try { 
	            //t.xlsΪҪ��ȡ��excel�ļ���
	            book = Workbook.getWorkbook(new File(path));            
	            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں�)
	            //ǰ����sheet��LinkID����ͳ��
//	            sheet = book.getSheet(3); 
	            sheet = book.getSheet("pickupsAndDropoffsProcess");
	            int rowCount = sheet.getRows();
	            //��ȡ��һ�е�Ԫ��
	            cellTaxiID = sheet.getCell(0, 0);
	            cellLocalTime = sheet.getCell(1, 0); 
	            cellPickupsLinkID = sheet.getCell(2, 0); 
	            cellDropoffsLinkID = sheet.getCell(3, 0); 
	            
	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��taxiΪ�������洢ʱ�䡢trip���յ�
	            for (int i = 1; i < rowCount; i++) {
	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	
	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
	            	String []arrStr = curTaxiIDStr.split("-");
	            	int curTaxiID = Integer.parseInt(arrStr[1]);
	            	if (curTaxiID ==17568) {
						System.out.print("");
					}
	            	//��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����Ϊ����취
	            	//��һ�ζ�ȡʱ�������ȷ��ʱ�䣬��������������8Сʱ��ʱ�䣬��֪��Ϊʲô
	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
//	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,ת��Ϊ24Сʱʱ����
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
		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,ת��Ϊ24Сʱʱ����		            	
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
	            
	            //д��txt�ļ�
	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection0602streetName\\20140602trip.txt";
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
				System.out.print("����д�����" + "\n");
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
	 * ȥ��������ֻ����һ�����������
	 * @param streetNameStr
	 * @return
	 */
	public String processStreetName(String streetNameStr){
		try {
			//ȥ������
        	if (streetNameStr.contains("��")) {
        		//ֻ������һ�����������ȥ������
				String []StrArray = streetNameStr.split("��");
				String leftStr = StrArray[0];
				String rightStr = StrArray[1];					
				//������
				if (rightStr.contains("��")) {
					String []tStrArray = rightStr.split("��");
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
	 * ����ʱ�����Ϣ
	 * ����Ϊ����ҿ�
	 * 4-10ʱ:1
	 * 10-16ʱ:2
	 * 16-22:3
	 * ����:4
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
	 * �ж�ʱ���Ƿ�Ϊ17�㵽21��֮��
	 * ����ǣ��򷵻�1�����򷵻�-1
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
	 * ����ʱ���ص���ȡxls xls xls��ĳ��tirp��excel���ֶθ�ʽΪtaxID,localTime,pickupsLinkID,dropoffsLinkID
	 * excel������Ҫ����taxiID��������
	 * ������Ϊÿ��taxiID��ÿ��ʱ���ڶ�Ӧ��ϵ��term 
	 */
	public void excelTripDirectionProcessEverySixHour(){
//		try {
//			
//			String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\2014060203.xlsx";
//			int num = 0;
//	        Workbook book;
//	        Cell cellTaxiID, cellLocalTime, cellPickupsLinkID, cellDropoffsLinkID;
//	        XSSFWorkbook workbook = null;
//			InputStream ifs = null;
//			OutputStream ofs = null;
//			XSSFSheet sheet;
//	        XSSFRow headRow;//������
//	        XSSFRow row;
//	        
//	        
////	        XSSFWorkbook wb = null;
////			InputStream ifs = null;
////			OutputStream ofs = null;
////			double threshVal = 0.1;//�ĵ�������ֲ����ʵ���ֵ
////			XSSFSheet sheet;
////	        XSSFRow headRow;//������
////	        XSSFRow row;
////	        Map<String, String> topicMap = new HashMap<String, String>();//�����Լ������Ӧ��·��ID,�Լ�����
////	        try { 
////	        	// ����Ҫ��ȡ���ļ�·��
////				ifs = new FileInputStream(path);       	
////				// HSSFWorkbook�൱��һ��excel�ļ���HSSFWorkbook�ǽ���excel2007֮ǰ�İ汾��xls��
////				// ֮��汾ʹ��XSSFWorkbook��xlsx��
////				wb = new XSSFWorkbook(ifs);
//////		            sheet = wb.getSheet("documentTopicDistribution"); 
////				sheet = wb.getSheetAt(0);
////	            int rowCount = sheet.getLastRowNum();// ��ȡ����������
////	            headRow = sheet.getRow(0);// ���sheet�е�0��
////	            int columCount = headRow.getPhysicalNumberOfCells();
////	            String[] probDistributionArray = new String[10];//���ʷֲ�
////	            for (int p = 0; p < probDistributionArray.length; p++) {
////	            	probDistributionArray[p] = "";
////				}	            
////	            for (int i = 1; i <= rowCount; i++) {
////	            	row = sheet.getRow(i);// ���sheet�е�i��
////	            	String topicStr = row.getCell(0).getStringCellValue();
////	            	for (int j = 1; j < columCount; j++) {
////	            		// ������е��У�����Ԫ��HSSFCell
////						XSSFCell cell = row.getCell(j);
////						// ��õ�Ԫ���е�ֵ
////						double cellVal = cell.getNumericCellValue();
////						if (cellVal > threshVal) {
////							String probability = String.format("%.3f", cellVal);						
////							double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
////							String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
////							System.out.println(linkID + "," + probability + "\n");
////							probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
////						}		            		
////					}
////	            	topicMap.put(topicStr, probDistributionArray[i - 1]);
////				}	            	            
////	            System.out.print("��ʼд�ĵ�������ʷֲ����ݣ�" + "\n");	        
////	            
//	            
//	            
//	        
//	            
//	        
//	        try { 
//	            //t.xlsΪҪ��ȡ��excel�ļ���
//	            book = Workbook.getWorkbook(new File(path));            
//	            //��õ�һ�����������(ecxel��sheet�ı�Ŵ�0��ʼ,0,1,2,3,....,�����к���ǰ���к��ں�)
//	            //ǰ����sheet��LinkID����ͳ��
//	            ifs = new FileInputStream(path);       	
//				// HSSFWorkbook�൱��һ��excel�ļ���HSSFWorkbook�ǽ���excel2007֮ǰ�İ汾��xls��
//				// ֮��汾ʹ��XSSFWorkbook��xlsx��
//	            workbook = new XSSFWorkbook(ifs);
//				sheet = workbook.getSheet("pickupDropoffProcess");
//	            int rowCount = sheet.getLastRowNum();// ��ȡ����������
//	            headRow = sheet.getRow(0);// ���sheet�е�0��
//				
////	            sheet = book.getSheet("pickupsAndDropoffslinkIDProcess");
////	            int rowCount = sheet.getRows();
//	            //��ȡ��һ�е�Ԫ��
//	            cellTaxiID = sheet.getCell(0, 0);
//	            cellLocalTime = sheet.getCell(1, 0); 
//	            cellPickupsLinkID = sheet.getCell(2, 0); 
//	            cellDropoffsLinkID = sheet.getCell(3, 0); 
//	            
//	            Map<Integer, ArrayList<String>> tripMap = new HashMap<Integer, ArrayList<String>>();//��·��taxiΪ�������洢ʱ�䡢trip���յ�
//	            for (int i = 1; i < rowCount; i++) {
//	            	ArrayList<String> tripDistArraylist = new ArrayList<String>();//�洢ͨ�о���	
//	            	String curTaxiIDStr = sheet.getCell(0, i).getContents();
//	            	String []arrStr = curTaxiIDStr.split("-");
//	            	int curTaxiID = Integer.parseInt(arrStr[1]);
//	            	if (curTaxiID ==17568) {
//						System.out.print("");
//					}
//	            	//��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����Ϊ����취
//	            	//��һ�ζ�ȡʱ�������ȷ��ʱ�䣬��������������8Сʱ��ʱ�䣬��֪��Ϊʲô
//	            	DateCell curDateCell = (DateCell)sheet.getCell(1, i); 	            	
////	            	String curLocalTimeStr = excelFormateTime(curDateCell);//time,ת��Ϊ24Сʱʱ����
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
//		            	String nextLocalTimeStr = excelFormateTime(nextDdateCell);//time,ת��Ϊ24Сʱʱ����
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
//	            //д��txt�ļ�
//	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602trip.txt";
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
//				System.out.print("����д�����" + "\n");
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
	 * ʱ��ת��������
	 * ��ȡexcel �ļ����ʱ���ʽ����ʱ,24ʱ�ƻ��Զ�ת��Ϊ12ʱ��,����취��ת��Ϊ24Сʱʱ����
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
	 * ��ȡ�ĵ�����ֲ�excel�ļ�(xlsx)
	 * topicMap �����Լ������Ӧ��·��ID,�Լ�����
	 */
	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap , String LDATopicPath){
		
		XSSFWorkbook wb = null;
		InputStream inputStream = null;
		OutputStream ofs = null;
		double threshVal = 0.005;//�ĵ�������ֲ����ʵ���ֵ
		XSSFSheet sheet;
        XSSFRow headRow;//������
        XSSFRow row;
        try { 
        	// ����Ҫ��ȡ���ļ�·��
        	inputStream = new FileInputStream(LDATopicPath);       	
			// HSSFWorkbook�൱��һ��excel�ļ���HSSFWorkbook�ǽ���excel2007֮ǰ�İ汾��xls��
			// ֮��汾ʹ��XSSFWorkbook��xlsx��
			wb = new XSSFWorkbook(inputStream);
	        sheet = wb.getSheet("topic-term-distributionsProcess"); 
//			sheet = wb.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();// ��ȡ����������
            headRow = sheet.getRow(0);// ���sheet�е�0��
            int columCount = headRow.getPhysicalNumberOfCells();
            String[] probDistributionArray = new String[10];//���ʷֲ�
            for (int p = 0; p < probDistributionArray.length; p++) {
            	probDistributionArray[p] = "";
			}
            
            for (int i = 1; i <= rowCount; i++) {
            	row = sheet.getRow(i);// ���sheet�е�i��
            	String topicStr = row.getCell(0).getStringCellValue();
            	for (int j = 1; j < columCount; j++) {
            		// ������е��У�����Ԫ��HSSFCell
					XSSFCell cell = row.getCell(j);
					// ��õ�Ԫ���е�ֵ
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
						String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
////						//·��ID����
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
             * д��txt�ļ�
             */
            
//            System.out.print("��ʼд�ĵ�������ʷֲ����ݣ�" + "\n");            
//            //д��txt�ļ�
//            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\20140602TopicTermDistribution.txt";
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
//        		String probabilityDistributionStr = topicMap.get(key);//���ʷֲ�
//        		probabilityDistributionStr = key + "��" + probabilityDistributionStr + "\r\n";
//        		write = new StringBuffer();				
//				write.append(probabilityDistributionStr);
//				bufferStream.write(write.toString().getBytes("UTF-8"));	
//        	}
//        	bufferStream.flush();      
//			bufferStream.close(); 
//			outputStream.close();
//			System.out.print("����д�����" + "\n");
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}
	}
	
	/**
	 * ��Excel�л���ĵ�������ʷֲ�
	 * @param topicMap
	 */
	public void obtainLDADocumentTopicFromExcel(Map<String, String> topicMap){
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//		String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.xlsx";
		double threshVal = 0.1;//�ĵ�������ֲ����ʵ���ֵ
		XSSFSheet sheet;
        XSSFRow headRow;//������
        XSSFRow row;
//        Map<String, String> topicMap = new HashMap<String, String>();//�����Լ������Ӧ��·��ID,�Լ�����
        try { 
        	// ����Ҫ��ȡ���ļ�·��
			ifs = new FileInputStream(path);       	
			// HSSFWorkbook�൱��һ��excel�ļ���HSSFWorkbook�ǽ���excel2007֮ǰ�İ汾��xls��
			// ֮��汾ʹ��XSSFWorkbook��xlsx��
			wb = new XSSFWorkbook(ifs);
//	            sheet = wb.getSheet("documentTopicDistribution"); 
			sheet = wb.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();// ��ȡ����������
            headRow = sheet.getRow(0);// ���sheet�е�0��
            int columCount = headRow.getPhysicalNumberOfCells();
            String[] probDistributionArray = new String[10];//���ʷֲ�
            for (int p = 0; p < probDistributionArray.length; p++) {
            	probDistributionArray[p] = "";
			}           
            for (int i = 1; i <= rowCount; i++) {
            	row = sheet.getRow(i);// ���sheet�е�i��
            	String topicStr = row.getCell(0).getStringCellValue();
            	for (int j = 1; j < columCount; j++) {
            		// ������е��У�����Ԫ��HSSFCell
					XSSFCell cell = row.getCell(j);
					// ��õ�Ԫ���е�ֵ
					double cellVal = cell.getNumericCellValue();
					if (cellVal > threshVal) {
						String probability = String.format("%.3f", cellVal);//����ֵ					
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("��ʼд�ĵ�������ʷֲ����ݣ�" + "\n");  
        }
        catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
	  		System.out.println(e.getMessage());
		}		
	}
	
	
	
	/**
	 * ��ȡshape�ļ�������ֶ�
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
        		IFeatureCursor pLineFeatureCursor = plineFeatureClass.search(null, false);//ָ��
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
    			String topicKey = (String)mapEntry.getKey();
    			int topicNumber = Integer.parseInt(topicKey.substring(5));
    			String probabilityDistributionStr = topicMap.get(topicKey);//���ʷֲ�
    			String[]probabilityArray = probabilityDistributionStr.split(";");    			
    			for (int j = 0; j < lineCount; j ++) {
    				IFeature plineFeature = pLineFeatureCursor.nextFeature();
    				IGeometry plineGeometry = plineFeature.getShape(); 
    				IPolyline polyline = (IPolyline)plineGeometry;
    				System.out.print("����" + topicKey + ":�����" + j + ":" + lineCount + "��Ԫ��\n");
    				//��õ�·������Ϣ				
    				IFields plineFields = new Fields();
    				plineFields = plineFeature.getFields();
    				int LINKIDFiedIndex = plineFields.findField("LINKID");			
    				int LINKID = (Integer)plineFeature.getValue(LINKIDFiedIndex);//·�α��
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
    							plineFeature.setValue(topicFieldIndex, topicNumber);//�ֶ���д��ֵ
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
	 * ��������ʷֲ����
	 * ����shapefile��ÿ��·����������
	 * ����ÿ�����⸳�����ֵ
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
//	        String topicNameFieldStr = "Topic";//�����ֶ���
//	        String topicProbabilityFieldStr = "TopicProbability";//���������
	        
//	        //dropoffsFrom4To10
//	        String topicNameFieldStr = "TopicInterval";//�����ֶ���
//	        String topicProbabilityFieldStr = "TopicIntervalProb";//���������
//	        String LDATopicPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffsFrom4To10\\LDAprocess.xlsx";
	        
	        //dropoffs060205From4To10  6��2�յ�5��
//	        String topicNameFieldStr = "Topic005";//�����ֶ��� 0.05
//	        String topicNameFieldStr = "Topic2";//�����ֶ���
//	        String topicProbabilityFieldStr = "Topic2Prob";//���������
	        
	        //dropoffs060205From17To21  6��2�յ�5��
	        String topicNameFieldStr = "Topic1721";//�����ֶ���
	        String topicProbabilityFieldStr = "Topic1721Prob";//���������
	        String LDATopicPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDropoffs060205From17To21\\LDAprocessFrom17To21.xlsx";
	        
	        
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
        		IFeatureCursor pLineFeatureCursor = plineFeatureClass.search(null, false);//ָ��
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
    			String topicKey = (String)mapEntry.getKey();
    			int topicNumber = Integer.parseInt(topicKey.substring(5));
    			String probabilityDistributionStr = topicMap.get(topicKey);//���ʷֲ�
    			String[]probabilityArray = probabilityDistributionStr.split(";");    			
    			for (int j = 0; j < lineCount; j ++) {
    				IFeature plineFeature = pLineFeatureCursor.nextFeature();
    				IGeometry plineGeometry = plineFeature.getShape(); 
    				IPolyline polyline = (IPolyline)plineGeometry;
    				System.out.print("����" + topicKey + ":�����" + j + ":" + lineCount + "��Ԫ��\n");
    				//��õ�·������Ϣ				
    				IFields plineFields = new Fields();
    				plineFields = plineFeature.getFields();
    				int LINKIDFiedIndex = plineFields.findField("LINKID");			
    				int LINKID = (Integer)plineFeature.getValue(LINKIDFiedIndex);//·�α��
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
    							plineFeature.setValue(topicFieldIndex, topicNumber);//�ֶ���д��ֵ
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
//	 * ��Excel�л��term�������и��ʷֲ�
//	 * @param topicMap
//	 */
//	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap){
//		XSSFWorkbook wb = null;
//		InputStream ifs = null;
//		OutputStream ofs = null;
////		String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
//		String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirectionDropoffs0602EverySixHours\\LDAprocess.xlsx";
//		double threshVal = 0.1;//term�������зֲ����ʵ���ֵ
//		XSSFSheet sheet;
//        XSSFRow headRow;//������
//        XSSFRow row;
//        try { 
//        	// ����Ҫ��ȡ���ļ�·��
//			ifs = new FileInputStream(path);       	
//			// HSSFWorkbook�൱��һ��excel�ļ���HSSFWorkbook�ǽ���excel2007֮ǰ�İ汾��xls��
//			// ֮��汾ʹ��XSSFWorkbook��xlsx��
//			wb = new XSSFWorkbook(ifs);
////	            sheet = wb.getSheet("documentTopicDistribution"); 
//			sheet = wb.getSheetAt(0);
//            int rowCount = sheet.getLastRowNum();// ��ȡ����������
//            headRow = sheet.getRow(0);// ���sheet�е�0��
//            int columCount = headRow.getPhysicalNumberOfCells();
//            String[] probDistributionArray = new String[10];//���ʷֲ�
//            for (int p = 0; p < probDistributionArray.length; p++) {
//            	probDistributionArray[p] = "";
//			}           
//            for (int i = 1; i <= rowCount; i++) {
//            	row = sheet.getRow(i);// ���sheet�е�i��
//            	String topicStr = row.getCell(0).getStringCellValue();
//            	for (int j = 1; j < columCount; j++) {
//            		// ������е��У�����Ԫ��HSSFCell
//					XSSFCell cell = row.getCell(j);
//					// ��õ�Ԫ���е�ֵ
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
//						String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
//						//·��ID����
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
//            System.out.print("��ʼдterm������ʷֲ����ݣ�" + "\n");  
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
