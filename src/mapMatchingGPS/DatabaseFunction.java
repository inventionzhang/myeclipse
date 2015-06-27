package mapMatchingGPS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.crypto.provider.RSACipher;

import entity.returnResult;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

/***************************************
 * �����ݿ���в�������غ���
 * *************************************/
public class DatabaseFunction {

	public static final String user = "root";//�û���
	public static final String password = "123456";//����
	public static final String driver = "com.mysql.jdbc.Driver";//����
	public static final String url = "jdbc:mysql://192.168.2.148:3306/dbname";//���ݿ�����
	
	/*���ݿ��л��GPS����:���Ŀ����⳵һ��ʱ���ڵ�GPS����
	 * taxiGPSArrayList���洢���⳵����
	 * targetIDStr:���⳵ID
	 * startTimeStr:��ʼʱ��	��ʽ2013-01-01 00:00:15
	 * endTimeStr:����ʱ��
	 * */
	public static void obtainGPSDataFromDatabase(ArrayList<TaxiGPS> taxiGPSArrayList,
			String targetIDStr, String startTimeStr, String endTimeStr){
		/* ����oracle���ݿ�
		 * String JDBC_URL = "jdbc:oracle:thin:@192.168.2.230:1521:orcl";
		 * Class.forName("oracle.jdbc.driver.OracleDriver");
		 * conn = DriverManager.getConnection(JDBC_URL, "fm", "fm");
		 * *********************************************************
		 * ����mySQL���ݿ�
		 * String driver = "com.mysql.jdbc.Driver";
		 * String url = "jdbc:mysql://192.168.2.148:3306/dbname";
		 * Class.forName(driver);// ������������
		 * conn = DriverManager.getConnection(url, user, password);// �������ݿ�*/		
	 	//��ó��⳵GPS����	 
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMEJANUARY_2014_STRING;
    	if (startTimeStr.substring(0, 7).equals("2014-02")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEFEBRUARY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-03")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMARCH_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-04")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEAPRIAL_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-05")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMAY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-06") || startTimeStr.substring(0, 7).equals("2014-07")) {
    		tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
		}
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
	 	try {	    
	 		Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
//	     		String sqlString = "select distinct * from handleddata where T_TargetID = ? and T_LocalTime between ? and ? order by T_LocalTime";
	     		String sqlString = "select distinct * from " + tableName +" where T_TargetID = ? and T_LocalTime between ? and ? order by T_LocalTime";
	     		pstmt = conn.prepareStatement(sqlString);       	
	        	pstmt.setString(1, targetIDStr);
	        	pstmt.setString(2, startTimeStr);
	        	pstmt.setString(3, endTimeStr);
				rs=pstmt.executeQuery();				
				while(rs.next())
				{
					TaxiGPS tTaxiGPS = new TaxiGPS();
					tTaxiGPS.targetID = rs.getString("T_TargetID");
					tTaxiGPS.localTime = rs.getString("T_LOCALTime");
					tTaxiGPS.longitude = rs.getDouble("T_Longitude");
					tTaxiGPS.latitude = rs.getDouble("T_Latitude");
					tTaxiGPS.speed = rs.getDouble("T_Speed");
					tTaxiGPS.heading = rs.getDouble("T_Heading");
					tTaxiGPS.status = rs.getInt("T_Status");
					taxiGPSArrayList.add(tTaxiGPS);					
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}
	 	}
	 	catch (Exception e) {
			// TODO: handle exception
	 		e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * ����TaxiID����ֹʱ�䡢table name�������
	 * @param taxiGPSArrayList
	 * @param targetIDStr
	 * @param startTimeStr
	 * @param endTimeStr
	 * @param tableName
	 */
	public static void obtainGPSDataFromDatabaseAccordTaxiIDStartEndTimeTripPatternTable(ArrayList<TaxiGPS> taxiGPSArrayList,
			String targetIDStr, String startTimeStr, String endTimeStr, String tableName){		
	 	//��ó��⳵GPS����	 
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
	 	try {	    
	 		Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
	     		String sqlString = "select distinct * from " + tableName +" where T_TargetID = ? and T_LocalTime between ? and ? order by T_LocalTime";
	     		pstmt = conn.prepareStatement(sqlString);       	
	        	pstmt.setString(1, targetIDStr);
	        	pstmt.setString(2, startTimeStr);
	        	pstmt.setString(3, endTimeStr);
				rs=pstmt.executeQuery();				
				while(rs.next())
				{
					TaxiGPS tTaxiGPS = new TaxiGPS();
					tTaxiGPS.targetID = rs.getString("T_TargetID");
					tTaxiGPS.localTime = rs.getString("T_LOCALTime");
					tTaxiGPS.longitude = rs.getDouble("T_Longitude");
					tTaxiGPS.latitude = rs.getDouble("T_Latitude");
					tTaxiGPS.speed = rs.getDouble("T_Speed");
					tTaxiGPS.heading = rs.getDouble("T_Heading");
					tTaxiGPS.status = rs.getInt("T_Status");
					tTaxiGPS.setCorrectLongitude(rs.getDouble("T_CorrectLongitude"));
					tTaxiGPS.setCorrectLatitude(rs.getDouble("T_CorrectLatitude"));
					tTaxiGPS.setBelongLinkName(rs.getString("T_StreetName"));
					tTaxiGPS.setBelongLineID(rs.getInt("T_StreetNo"));
					tTaxiGPS.setChangedStatus(rs.getInt("T_ChangedStatus"));
					taxiGPSArrayList.add(tTaxiGPS);					
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}
	 	}
	 	catch (Exception e) {
			// TODO: handle exception
	 		e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	public static void obtainGPSDataAccordIDTimeSpatialFilter(ArrayList<TaxiGPS> taxiGPSArrayList,
			String targetIDStr, String startTimeStr, String endTimeStr, double minLog, double minLat, double maxLog, double maxLat){
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMEJANUARY_2014_STRING;
    	if (startTimeStr.substring(0, 7).equals("2014-02")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEFEBRUARY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-03")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMARCH_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-04")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEAPRIAL_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-05")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMAY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-06") || startTimeStr.substring(0, 7).equals("2014-07")) {
    		tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
		}
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
	 	try {	    
	 		Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
	     		String sqlString = "select distinct * from " + tableName +" where T_TargetID = ? and T_Longitude > ? and T_Longitude < ? and T_Latitude > ? and " +
 				"T_Latitude < ? and T_LocalTime between ? and ? order by T_LocalTime";   		
	        	pstmt = conn.prepareStatement(sqlString);       	
	        	pstmt.setString(1, targetIDStr);
	        	pstmt.setDouble(2, minLog);
	        	pstmt.setDouble(3, maxLog);
	        	pstmt.setDouble(4, minLat);
	        	pstmt.setDouble(5, maxLat);
	        	pstmt.setString(6, startTimeStr);
	        	pstmt.setString(7, endTimeStr);
				rs=pstmt.executeQuery();				
				while(rs.next())
				{
					TaxiGPS tTaxiGPS = new TaxiGPS();
					tTaxiGPS.targetID = rs.getString("T_TargetID");
					tTaxiGPS.localTime = rs.getString("T_LOCALTime");
					tTaxiGPS.longitude = rs.getDouble("T_Longitude");
					tTaxiGPS.latitude = rs.getDouble("T_Latitude");
					tTaxiGPS.speed = rs.getDouble("T_Speed");
					tTaxiGPS.heading = rs.getDouble("T_Heading");
					tTaxiGPS.status = rs.getInt("T_Status");
					taxiGPSArrayList.add(tTaxiGPS);					
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}
	 	}
	 	catch (Exception e) {
			// TODO: handle exception
	 		e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/*������г��⳵һ��ʱ���ڵ�����
	 * taxiGPSArrayList���洢���⳵����
	 * startTimeStr:��ʼʱ��
	 * endTimeStr:����ʱ��
	 * */
	public static void obtainGPSDataFromDatabaseAccordStartEndTime(ArrayList<TaxiGPS> taxiGPSArrayList,
			String startTimeStr, String endTimeStr){
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMEJANUARY_2014_STRING;
    	if (startTimeStr.substring(0, 7).equals("2014-02")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEFEBRUARY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-03")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMARCH_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-04")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEAPRIAL_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-05")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMAY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-06") || startTimeStr.substring(0, 7).equals("2014-07")) {
    		tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
		}
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
	 	try {	    
	 		Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
	     		String sqlString = "select distinct * from " + tableName + " where T_LocalTime between ? and ? order by T_LocalTime";
	        	pstmt = conn.prepareStatement(sqlString);       	
	        	pstmt.setString(1, startTimeStr);
	        	pstmt.setString(2, endTimeStr);
				rs = pstmt.executeQuery();				
				while(rs.next())
				{
					TaxiGPS tTaxiGPS = new TaxiGPS();
					tTaxiGPS.targetID = rs.getString("T_TargetID");
					tTaxiGPS.localTime = rs.getString("T_LOCALTime");
					tTaxiGPS.longitude = rs.getDouble("T_Longitude");
					tTaxiGPS.latitude = rs.getDouble("T_Latitude");
					tTaxiGPS.speed = rs.getDouble("T_Speed");
					tTaxiGPS.heading = rs.getDouble("T_Heading");
					tTaxiGPS.status = rs.getInt("T_Status");
					taxiGPSArrayList.add(tTaxiGPS);					
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}
	 	}
	 	catch (Exception e) {
			// TODO: handle exception
	 		e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
		
	/*������г��⳵����ʱ��Լ�����������ݣ�
	 * taxiGPSArrayList���洢���⳵����
	 * startTimeStr:��ʼʱ��	��ʽΪ��2013-01-01 00:00:15
	 * endTimeStr:����ʱ��
	 * minLog����С����
	 * minLat����Сγ��
	 * maxLog����󾭶�
	 * maxLat�����γ��
	 * */
	public static void obtainGPSDataAccordTimeSpatialFilter(ArrayList<TaxiGPS> taxiGPSArrayList, String startTimeStr, String endTimeStr,
			double minLog, double minLat, double maxLog, double maxLat){
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMEJANUARY_2014_STRING;
    	if (startTimeStr.substring(0, 7).equals("2014-02")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEFEBRUARY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-03")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMARCH_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-04")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEAPRIAL_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-05")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMAY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-06") || startTimeStr.substring(0, 7).equals("2014-07")) {
    		tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
		}
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
	     		String sqlString = "select distinct * from " + tableName + " where T_Longitude > ? and T_Longitude < ? and T_Latitude > ? and " +
 				"T_Latitude < ? and T_LocalTime between ? and ? order by T_LocalTime";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT); 
	        	pstmt.setDouble(1, minLog);
	        	pstmt.setDouble(2, maxLog);
	        	pstmt.setDouble(3, minLat);
	        	pstmt.setDouble(4, maxLat);	        	
	        	pstmt.setString(5, startTimeStr);
	        	pstmt.setString(6, endTimeStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					TaxiGPS tTaxiGPS = new TaxiGPS();
					tTaxiGPS = new TaxiGPS();
					tTaxiGPS.targetID = rs.getString("T_TargetID");
					tTaxiGPS.localTime = rs.getString("T_LOCALTime");
					tTaxiGPS.longitude = rs.getDouble("T_Longitude");
					tTaxiGPS.latitude = rs.getDouble("T_Latitude");
					tTaxiGPS.speed = rs.getDouble("T_Speed");
					tTaxiGPS.heading = rs.getDouble("T_Heading");
					tTaxiGPS.status = rs.getInt("T_Status");
					taxiGPSArrayList.add(tTaxiGPS);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		finally{
			try {
				if (conn != null) {
					conn.close();					
				}
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
				System.out.print(e2.getMessage());
			}		
		}
	}
	
	
	/*������г��⳵����ʱ��Լ�����������ݣ��̳߳ض��̴߳���
	 * taxiGPSArrayList���洢���⳵����
	 * startTimeStr:��ʼʱ��
	 * endTimeStr:����ʱ��
	 * minLog����С����
	 * minLat����Сγ��
	 * maxLog����󾭶�
	 * maxLat�����γ��
	 * */
	public static void obtainGPSDataAccordTimeSpatialFilterBackUps(ArrayList<TaxiGPS> taxiGPSArrayList, String startTimeStr, String endTimeStr,
			MapMatchEdge curEdge, double minLog, double minLat, double maxLog, double maxLat){
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMEJANUARY_2014_STRING;
    	if (startTimeStr.substring(0, 7).equals("2014-02")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEFEBRUARY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-03")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMARCH_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-04")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEAPRIAL_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-05")) {
    		tableName = PubParameter.MYSQLDBTABLENAMEMAY_2014_STRING;
		}
    	if (startTimeStr.substring(0, 7).equals("2014-06") || startTimeStr.substring(0, 7).equals("2014-07")) {
    		tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
		}
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement����ִ��SQL���
	     		String sqlString = "select distinct * from " + tableName + " where T_Longitude > ? and T_Longitude < ? and T_Latitude > ? and " +
	     				"T_Latitude < ? and T_LocalTime between ? and ? order by T_LocalTime";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT); 
	        	pstmt.setDouble(1, minLog);
	        	pstmt.setDouble(2, maxLog);
	        	pstmt.setDouble(3, minLat);
	        	pstmt.setDouble(4, maxLat);	        	
	        	pstmt.setString(5, startTimeStr);
	        	pstmt.setString(6, endTimeStr);
				rs = pstmt.executeQuery();
				rs.last();//��ָ���ƶ�������������һ����¼,��ȡָ�뵱ǰ���ڵ��кţ���1��ʼ��
				int rowCount = rs.getRow();
				rs.first();
				int threadCount = PubParameter.threadCount;//�߳���Ŀ���̱߳�Ŵ�1��ʼ
				Map<Integer, ArrayList<TaxiGPS>> allEligibleGPSMap = new HashMap<Integer, ArrayList<TaxiGPS>>();//���������GPS��
				//�����ݼ�
				if (rowCount != 0) {
					ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3,  
			                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
			                new ThreadPoolExecutor.DiscardOldestPolicy()); 
					double taxiGPSCountEveryThread = (double)rowCount/threadCount;
					//һ���̴߳���
					if (taxiGPSCountEveryThread < 1) {
						ArrayList<TaxiGPS> tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
						TaxiGPS tTaxiGPS = new TaxiGPS();
						tTaxiGPS.targetID = rs.getString("T_TargetID");
						tTaxiGPS.localTime = rs.getString("T_LOCALTime");
						tTaxiGPS.longitude = rs.getDouble("T_Longitude");
						tTaxiGPS.latitude = rs.getDouble("T_Latitude");
						tTaxiGPS.speed = rs.getDouble("T_Speed");
						tTaxiGPS.heading = rs.getDouble("T_Heading");
						tTaxiGPS.status = rs.getInt("T_Status");
						tempTaxiGPSArrayList.add(tTaxiGPS);
						while(rs.next())
						{		
							tTaxiGPS = new TaxiGPS();
							tTaxiGPS.targetID = rs.getString("T_TargetID");
							tTaxiGPS.localTime = rs.getString("T_LOCALTime");
							tTaxiGPS.longitude = rs.getDouble("T_Longitude");
							tTaxiGPS.latitude = rs.getDouble("T_Latitude");
							tTaxiGPS.speed = rs.getDouble("T_Speed");
							tTaxiGPS.heading = rs.getDouble("T_Heading");
							tTaxiGPS.status = rs.getInt("T_Status");							
						}
						int threadID = 1;
						ArrayList<TaxiGPS> eligibleGPSArrayList = new ArrayList<TaxiGPS>();
						allEligibleGPSMap.put(threadID, eligibleGPSArrayList);
						threadPoolProcess(threadPool,threadID, curEdge, tempTaxiGPSArrayList, eligibleGPSArrayList);
					}
					//����ÿ���̼߳���������
					else {
						//���Ϊ���������ܹ�������
						String taxiGPSCountEveryThreadStr = String.valueOf(taxiGPSCountEveryThread);
						if (PubClass.isInteger(taxiGPSCountEveryThreadStr)) {
							int threadID = 0;
							int tempCount = 0;//��ʱ����������GPS����
							ArrayList<TaxiGPS> tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
							TaxiGPS tTaxiGPS = new TaxiGPS();
							tTaxiGPS.targetID = rs.getString("T_TargetID");
							tTaxiGPS.localTime = rs.getString("T_LOCALTime");
							tTaxiGPS.longitude = rs.getDouble("T_Longitude");
							tTaxiGPS.latitude = rs.getDouble("T_Latitude");
							tTaxiGPS.speed = rs.getDouble("T_Speed");
							tTaxiGPS.heading = rs.getDouble("T_Heading");
							tTaxiGPS.status = rs.getInt("T_Status");
							tempTaxiGPSArrayList.add(tTaxiGPS);
							tempCount++;
							while(rs.next())
							{		
								tTaxiGPS = new TaxiGPS();
								tTaxiGPS.targetID = rs.getString("T_TargetID");
								tTaxiGPS.localTime = rs.getString("T_LOCALTime");
								tTaxiGPS.longitude = rs.getDouble("T_Longitude");
								tTaxiGPS.latitude = rs.getDouble("T_Latitude");
								tTaxiGPS.speed = rs.getDouble("T_Speed");
								tTaxiGPS.heading = rs.getDouble("T_Heading");
								tTaxiGPS.status = rs.getInt("T_Status");
								tempCount++;
								if (tempCount < taxiGPSCountEveryThread) {
									tempTaxiGPSArrayList.add(tTaxiGPS);	
								}
								else {		
									threadID ++;
									ArrayList<TaxiGPS> eligibleGPSArrayList = new ArrayList<TaxiGPS>();
									allEligibleGPSMap.put(threadID, eligibleGPSArrayList);
									tempTaxiGPSArrayList.add(tTaxiGPS);	
									threadPoolProcess(threadPool,threadID, curEdge, tempTaxiGPSArrayList, eligibleGPSArrayList);
									tempCount = 0;//����									
									tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
								}	
							}
						}
						//��Ϊ����
						else {
							int integerPart =PubClass.obtainIntegerPart(taxiGPSCountEveryThreadStr);
							int threadID = 0;
							int tempCount = 0;//��ʱ����������GPS����
							int tempThreadCount = 0;//��ʱ���������㵱ǰ�߳���Ŀ
							ArrayList<TaxiGPS> tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
							TaxiGPS tTaxiGPS = new TaxiGPS();
							tTaxiGPS.targetID = rs.getString("T_TargetID");
							tTaxiGPS.localTime = rs.getString("T_LOCALTime");
							tTaxiGPS.longitude = rs.getDouble("T_Longitude");
							tTaxiGPS.latitude = rs.getDouble("T_Latitude");
							tTaxiGPS.speed = rs.getDouble("T_Speed");
							tTaxiGPS.heading = rs.getDouble("T_Heading");
							tTaxiGPS.status = rs.getInt("T_Status");
							tempTaxiGPSArrayList.add(tTaxiGPS);
							tempCount++;
							while(rs.next())
							{		
								tTaxiGPS = new TaxiGPS();
								tTaxiGPS.targetID = rs.getString("T_TargetID");
								tTaxiGPS.localTime = rs.getString("T_LOCALTime");
								tTaxiGPS.longitude = rs.getDouble("T_Longitude");
								tTaxiGPS.latitude = rs.getDouble("T_Latitude");
								tTaxiGPS.speed = rs.getDouble("T_Speed");
								tTaxiGPS.heading = rs.getDouble("T_Heading");
								tTaxiGPS.status = rs.getInt("T_Status");
								tempCount++;
								if (tempThreadCount == threadCount) {
									tempTaxiGPSArrayList.add(tTaxiGPS);	
									if (rs.last()) {
										ArrayList<TaxiGPS> eligibleGPSArrayList = new ArrayList<TaxiGPS>();
										allEligibleGPSMap.put(threadID, eligibleGPSArrayList);
										tempTaxiGPSArrayList.add(tTaxiGPS);	
										threadPoolProcess(threadPool,threadID, curEdge, tempTaxiGPSArrayList, eligibleGPSArrayList);
									}									
								}
								else {
									if (tempCount < integerPart) {
										tempTaxiGPSArrayList.add(tTaxiGPS);											
									}
									else {		
										threadID ++;
										ArrayList<TaxiGPS> eligibleGPSArrayList = new ArrayList<TaxiGPS>();
										allEligibleGPSMap.put(threadID, eligibleGPSArrayList);
										tempTaxiGPSArrayList.add(tTaxiGPS);	
										threadPoolProcess(threadPool,threadID, curEdge, tempTaxiGPSArrayList, eligibleGPSArrayList);
										tempCount = 0;//����	
										tempThreadCount++;
										tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
									}									
								}	
							}
						}
					}
					threadPool.shutdown(); //�رպ��ܼ������̣߳������е��߳�������ִ����
					while(threadPool.getPoolSize() != 0);
					Set keySet = allEligibleGPSMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
					while (iterator.hasNext()) {
						Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		int key = (Integer)mapEntry.getKey();
						ArrayList<TaxiGPS> tempTempTaxiGPSArrayList = allEligibleGPSMap.get(key);
						for (int i = 0; i < tempTempTaxiGPSArrayList.size(); i++) {
							taxiGPSArrayList.add(tempTempTaxiGPSArrayList.get(i));
						}
					}					
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/*ͨ���̳߳ؼ�����������ĳ��⳵GPS��
	 * threadID���߳�
	 * targetEdge��Ŀ��·��
	 * taxiGPSArrayList�������GPS��
	 * eligibleGPSArrayList:��������������GPS��
	 * */
	private static void threadPoolProcess(ThreadPoolExecutor threadPool, int threadID, MapMatchEdge targetEdge, ArrayList<TaxiGPS>taxiGPSArrayList, 
			ArrayList<TaxiGPS>eligibleGPSArrayList){
		try {
			int produceTaskSleepTime = 2;
			ThreadPoolObtainEligibleGPSPoint threadPoolObtainEligibleGPSPoint = new ThreadPoolObtainEligibleGPSPoint();
			threadPoolObtainEligibleGPSPoint.setThreadID(threadID);
			threadPoolObtainEligibleGPSPoint.setTargetEdge(targetEdge);
			threadPoolObtainEligibleGPSPoint.setTaxiGPSArrayList(taxiGPSArrayList);
			threadPoolObtainEligibleGPSPoint.setEligibleGPSArrayList(eligibleGPSArrayList);
			threadPool.execute(threadPoolObtainEligibleGPSPoint);
	        Thread.sleep(produceTaskSleepTime);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		 
	}
	/*�޳��ٶ�Ϊ���GPS��
	 * ��һ��Ӧ��д�ڵ�ͼƥ���㷨�ڣ�д�ڴ˴�ò�Ʋ�������Ϊȥ���˲���GPS�㣬����Щ���λ��������ʱ�����õ�
	 * �ǳ�Ӱ���·��ƥ����
	 * ��speedΪ��ʱ��ʾ���⳵��ĳ����ʱͣ������ʱGPS��¼��Ϣ��headingҲΪ��
	 * ����headingΪ�㣬��˽��з�����ʱ��Ӱ���·�ķ���÷֣����Ҫ����ЩspeedΪ��ĵ�ȥ��*/
	public static void eliminateZeroSpeedGPSData(ArrayList<TaxiGPS> taxiGPSArrayList, ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList){
		try {
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				if (Math.abs(taxiGPS.speed) > PubParameter.zeroSpeedThreshold) {
					eliminateZeroSpeedGPSDataArrayList.add(taxiGPS);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
	}
	
	/*�޳�δ�ؿ����ݣ�ֻ�����ؿ�����
	 * taxiGPSArrayList����������
	 * carryPassengerGPSDataArrayList���������ؿ�����*/
	public static void eliminateNonCarryPassengerGPSData(ArrayList<TaxiGPS> taxiGPSArrayList, ArrayList<TaxiGPS> carryPassengerGPSDataArrayList){
		try {
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				if (taxiGPS.getStatus() != 0) {
					carryPassengerGPSDataArrayList.add(taxiGPS);
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/*�������ʱ��Լ�������ĳ��⳵ID,taxiID
	 * taxiIDArrayList:taxiID
	 * startTimeStr:��ʼʱ��
	 * endTimeStr������ʱ��
	 * minLog����С����
	 * maxLog����󾭶�
	 * minLat����Сγ��
	 * maxLat�����γ��*/
	public static void obtainTaxiIDAccordTimeSpatialFilte(ArrayList<String> taxiIDArrayList, String startTimeStr, String endTimeStr, 
			double minLog, double maxLog, double minLat, double maxLat){
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	     		String sqlString = "select DISTINCT T_TargetID FROM " + tableName + " where T_Longitude > ? and T_Longitude < ? and "
	     			+"T_Latitude > ? and T_Latitude < ? and T_LocalTime BETWEEN ? AND ? order by T_LocalTime";	     		
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT); 
	        	pstmt.setDouble(1, minLog);
	        	pstmt.setDouble(2, maxLog);
	        	pstmt.setDouble(3, minLat);
	        	pstmt.setDouble(4, maxLat);	        	
	        	pstmt.setString(5, startTimeStr);
	        	pstmt.setString(6, endTimeStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					String targetID = rs.getString("T_TargetID");
					taxiIDArrayList.add(targetID);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}

	/**
	 * ����ʱ��Լ�������������Ψһ�ĳ��⳵ID,taxiID
	 * @param taxiIDArrayList
	 * @param startTimeStr
	 * @param endTimeStr
	 */
	public static void obtainUniqueTaxiIDAccordTime(ArrayList<String> taxiIDArrayList,String startTimeStr, String endTimeStr) {
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
    	String tableName = PubParameter.MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING;
    	if (startTimeStr.substring(0, 4).equals("2013")) {
    		tableName  = PubParameter.MYSQLDBTABLENAME2013_STRING;
		}
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	     		String sqlString = "select DISTINCT T_TargetID FROM " + tableName + " where T_LocalTime BETWEEN ? AND ? order by T_LocalTime";	     		
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);	        	
	        	pstmt.setString(1, startTimeStr);
	        	pstmt.setString(2, endTimeStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					String targetID = rs.getString("T_TargetID");
					taxiIDArrayList.add(targetID);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ������ֹʱ���Լ�������ó��⳵ID
	 * @param taxiIDArrayList
	 * @param startTimeStr
	 * @param endTimeStr
	 * @param tableName
	 */
	public static void obtainUniqueTaxiIDAccordTimeAndTableName(ArrayList<String> taxiIDArrayList,String startTimeStr, String endTimeStr, String tableName) {
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	     		String sqlString = "select DISTINCT T_TargetID FROM " + tableName + " where T_LocalTime BETWEEN ? AND ?";	     		
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);	        	
	        	pstmt.setString(1, startTimeStr);
	        	pstmt.setString(2, endTimeStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					String targetID = rs.getString("T_TargetID");
					taxiIDArrayList.add(targetID);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public static void obtainUniqueTaxiIDAccordTime(ArrayList<String> taxiIDArrayList, String tableName)
	{
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');    
	     		String sqlString = "select T_TargetID FROM " + tableName;
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					String targetID = rs.getString("T_TargetID");
					taxiIDArrayList.add(targetID);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		
		
	
	}
	
	public static void obtainUniqueTaxiIDAccordTime(ArrayList<String> taxiIDArrayList,String startTimeStr, String endTimeStr, String tableName) {
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	     		String sqlString = "select DISTINCT T_TargetID FROM " + tableName + " where T_LocalTime BETWEEN ? AND ? order by T_LocalTime";	     		
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);	        	
	        	pstmt.setString(1, startTimeStr);
	        	pstmt.setString(2, endTimeStr);
				rs = pstmt.executeQuery();
				while(rs.next())
				{		
					String targetID = rs.getString("T_TargetID");
					taxiIDArrayList.add(targetID);
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	
	
	
	
	/****************************************************
	 * дpaper����ʱһ���Ժ���
	 ****************************************************/
	
	/**
	 * �����ݿ�����ȡ�Ͽ͵���
	 */
	public static int obtainPickUpsCount(String startTimeStr, String endTimeStr, String tableName){
		int pickUpsCount = 0;
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		String timeStr = startTimeStr.substring(0,13) + '%';
	     		String sqlString = "SELECT COUNT(*) as count FROM " + tableName + " WHERE T_LocalTime like ? AND T_ChangedStatus = 1";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);       	
	        	pstmt.setString(1, timeStr);
				rs = pstmt.executeQuery();
				rs.next();
				pickUpsCount = rs.getInt("count");//��ȡ������
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return pickUpsCount;		
	}
	
	/**
	 * ������������������Ŀ
	 * @param distanceLowerLimit	��������
	 * @param distanceUpperLimit	��������
	 * @param tableName
	 * @return
	 */
	public static int obtainDistanceRangeCount(double distanceLowerLimit, double distanceUpperLimit, String tableName, String timeStr){
		int distanceRangeCount = 0;
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		String time = timeStr.substring(0,10) + '%';
	     		String sqlString = "SELECT COUNT(*) as count FROM " + tableName + " WHERE T_ChangedStatus = 1 AND T_LocalTime LIKE ? AND T_TripDistance >= ? AND T_TripDistance < ?";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);       	
	        	pstmt.setString(1, time);
	        	pstmt.setDouble(2, distanceLowerLimit);
	        	pstmt.setDouble(3, distanceUpperLimit);
				rs = pstmt.executeQuery();
				rs.next();
				distanceRangeCount = rs.getInt("count");//��ȡ������
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return distanceRangeCount;	
	}
	
	/**
	 * ��ȡĳСʱ�ھ���<= distance �ļ�¼��Ŀ
	 * @param distance
	 * @param tableName
	 * @param dateTimeStr
	 * @return
	 */
	public static int obtainSatisfiedDistancePickupsCountInanHour(double distanceLowerLimit, double distanceUpperLimit, String tableName, String dateTimeStr){
		int distanceRangeCount = 0;
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;		
		try {
			Class.forName(driver);// ������������
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		String time = dateTimeStr.substring(0,13) + '%';
	     		String sqlString = "SELECT COUNT(*) as count FROM " + tableName + " WHERE T_ChangedStatus = 1 AND T_LocalTime LIKE ? AND T_TripDistance > ? AND T_TripDistance <= ?";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);       	
	        	pstmt.setString(1, time);
	        	pstmt.setDouble(2, distanceLowerLimit);
	        	pstmt.setDouble(3, distanceUpperLimit);
				rs = pstmt.executeQuery();
				rs.next();
				distanceRangeCount = rs.getInt("count");//��ȡ������
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}				
	     	}
	     	if (conn != null) {
				conn.close();
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return distanceRangeCount;	
	}
	
	
	
	
	
	
	
	
}
