package latentDirichletAllocation;

import importDataToDatabase.ImportDataToMySQL;
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



public class MainFunctionTripPatterns {
	
	public static void main(String[] args){
//		//��ȡ�Ͽ͵����¿͵�
//		(new MainFunctionTripPatterns()).arrestPointProcess();		
//		System.out.print("��ȡ���!");
//		
		//����ȡ���Ͽ͵㡢�¿͵㴦��,����trip��distance��direction
		//������ӻ�������·�������⴦��������ӻ�������·��
//		(new MainFunctionTripPatterns()).tripPatternsProcess();
//		System.out.print("���ݴ������!");
		
				
//		//���ÿ��ÿСʱ���Ͽ͵������¿͵���
//		(new MainFunctionTripPatterns()).extractPickupsCount();
		
//		��ȡ����ֲ�
//		(new MainFunctionTripPatterns()).extractDistanceDistribution();
		
		//����ֲ���ʱ���ϵĲ�����
//		(new MainFunctionTripPatterns()).extractDistanceTemporalDistribution();
		
		//excel����
//		(new MainFunctionTripPatterns()).excelTripDistanceProcess();//���о���������
//		(new MainFunctionTripPatterns()).excelTripDirectionProcess();//���з���������
		(new MainFunctionTripPatterns()).excelLDADocumentTopicDistributionProcess();//��ȡ�ĵ�����ֲ�excel
//		(new MainFunctionTripPatterns()).processShapeFile();//����shapefile�ļ�
//		(new MainFunctionTripPatterns()).tripDirectionTopicTermDistribution();
		
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
	 * ��ȡĳ���Ͽ͵��Ӧ��·�α���Լ��¿͵��Ӧ·�α��
	 * 
	 */
	public void excelTripDirectionProcess(){
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
	            
	            //д��txt�ļ�
	            String outPutPath = "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\20140602tripDirection.txt";
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
	 * ��ȡ�ĵ�����ֲ�excel�ļ�(xlsx)
	 */
	public void excelLDADocumentTopicDistributionProcess(){
		
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//			String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.xlsx";
		double threshVal = 0.1;//�ĵ�������ֲ����ʵ���ֵ
		XSSFSheet sheet;
        XSSFRow headRow;//������
        XSSFRow row;
        Map<String, String> topicMap = new HashMap<String, String>();//�����Լ������Ӧ��·��ID,�Լ�����
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
						String probability = String.format("%.3f", cellVal);						
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("��ʼд�ĵ�������ʷֲ����ݣ�" + "\n");            
            //д��txt�ļ�
            String outPutPath = "F:\\360����\\paper\\LDA\\tripPatterns\\20140602DocumentTopicDistribution.txt";
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
        		String probabilityDistributionStr = topicMap.get(key);//���ʷֲ�
        		probabilityDistributionStr = key + "��" + probabilityDistributionStr + "\r\n";
        		write = new StringBuffer();				
				write.append(probabilityDistributionStr);
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
	 * ����������ʷֲ����
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
	 * ��Excel�л��term�������и��ʷֲ�
	 * @param topicMap
	 */
	public void obtainLDATopicTermFromExcel(Map<String, String> topicMap){
		XSSFWorkbook wb = null;
		InputStream ifs = null;
		OutputStream ofs = null;
//		String path= "F:\\360����\\paper\\LDA\\tripPatterns\\20140602LDAProcess.xlsx";
		String path= "F:\\360����\\Experiment\\LDA\\tripPatterns\\tripDirection\\tripDirectionTopicTermDistribution.xlsx";
		double threshVal = 0.1;//term�������зֲ����ʵ���ֵ
		XSSFSheet sheet;
        XSSFRow headRow;//������
        XSSFRow row;
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
						String probability = String.format("%.3f", cellVal);//����ֵ					
						double ttlinkIDStr = headRow.getCell(j).getNumericCellValue();
						String linkID = String.valueOf((int)ttlinkIDStr);//ȡ����
						System.out.println(linkID + "," + probability + "\n");
						probDistributionArray[i - 1] = probDistributionArray[i - 1] + linkID + "," + probability + ";";
					}		            		
				}
            	topicMap.put(topicStr, probDistributionArray[i - 1]);
			}
            	            
            System.out.print("��ʼдterm������ʷֲ����ݣ�" + "\n");  
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
