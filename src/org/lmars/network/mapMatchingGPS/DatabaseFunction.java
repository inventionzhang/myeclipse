package org.lmars.network.mapMatchingGPS;

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

import org.lmars.network.entity.returnResult;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;

import com.sun.crypto.provider.RSACipher;



/***************************************
 * 对数据库进行操作的相关函数
 * *************************************/
public class DatabaseFunction {

	public static final String user = "root";//用户名
	public static final String password = "123456";//密码
	public static final String driver = "com.mysql.jdbc.Driver";//驱动
	public static final String url = "jdbc:mysql://192.168.106.58:3306/dbname";//数据库连接
	
	/*数据库中获得GPS数据:获得目标出租车一定时间内的GPS数据
	 * taxiGPSArrayList：存储出租车数据
	 * targetIDStr:出租车ID
	 * startTimeStr:开始时间	格式2013-01-01 00:00:15
	 * endTimeStr:结束时间
	 * */
	public static void obtainGPSDataFromDatabase(ArrayList<TaxiGPS> taxiGPSArrayList,
			String targetIDStr, String startTimeStr, String endTimeStr){
		/* 连接oracle数据库
		 * String JDBC_URL = "jdbc:oracle:thin:@192.168.2.230:1521:orcl";
		 * Class.forName("oracle.jdbc.driver.OracleDriver");
		 * conn = DriverManager.getConnection(JDBC_URL, "fm", "fm");
		 * *********************************************************
		 * 连接mySQL数据库
		 * String driver = "com.mysql.jdbc.Driver";
		 * String url = "jdbc:mysql://192.168.2.148:3306/dbname";
		 * Class.forName(driver);// 加载驱动程序
		 * conn = DriverManager.getConnection(url, user, password);// 连接数据库*/
	 	//获得出租车GPS数据	 
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
	 		Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
	 * 根据TaxiID、起止时间、table name获得数据
	 * @param taxiGPSArrayList
	 * @param targetIDStr
	 * @param startTimeStr
	 * @param endTimeStr
	 * @param tableName
	 */
	public static void obtainGPSDataFromDatabaseAccordTaxiIDStartEndTimeTripPatternTable(ArrayList<TaxiGPS> taxiGPSArrayList,
			String targetIDStr, String startTimeStr, String endTimeStr, String tableName){		
	 	//获得出租车GPS数据	 
		String user = DatabaseFunction.user;
    	String password = DatabaseFunction.password;
	 	Connection conn = null;	 	
	 	PreparedStatement pstmt = null;
		ResultSet rs = null;
		String driver = DatabaseFunction.driver;
		String url = DatabaseFunction.url;	
	 	try {	    
	 		Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
	 		Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
	
	/*获得所有出租车一定时间内的数据
	 * taxiGPSArrayList：存储出租车数据
	 * startTimeStr:开始时间
	 * endTimeStr:结束时间
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
	 		Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
		
	/*获得所有出租车满足时空约束条件的数据：
	 * taxiGPSArrayList：存储出租车数据
	 * startTimeStr:开始时间	格式为：2013-01-01 00:00:15
	 * endTimeStr:结束时间
	 * minLog：最小经度
	 * minLat：最小纬度
	 * maxLog：最大经度
	 * maxLat：最大纬度
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
			Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
	
	
	/*获得所有出租车满足时空约束条件的数据：线程池多线程处理
	 * taxiGPSArrayList：存储出租车数据
	 * startTimeStr:开始时间
	 * endTimeStr:结束时间
	 * minLog：最小经度
	 * minLat：最小纬度
	 * maxLog：最大经度
	 * maxLat：最大纬度
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
			Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		System.out.println("Succeeded connecting to the Database!" + '\n');
	        	// statement用来执行SQL语句
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
				rs.last();//将指针移动到结果集的最后一条记录,获取指针当前所在的行号（从1开始）
				int rowCount = rs.getRow();
				rs.first();
				int threadCount = PubParameter.threadCount;//线程数目，线程编号从1开始
				Map<Integer, ArrayList<TaxiGPS>> allEligibleGPSMap = new HashMap<Integer, ArrayList<TaxiGPS>>();//处理后合理的GPS点
				//有数据集
				if (rowCount != 0) {
					ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3,  
			                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
			                new ThreadPoolExecutor.DiscardOldestPolicy()); 
					double taxiGPSCountEveryThread = (double)rowCount/threadCount;
					//一个线程处理
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
					//否则，每个线程计算多个数据
					else {
						//如果为整数，即能够被整除
						String taxiGPSCountEveryThreadStr = String.valueOf(taxiGPSCountEveryThread);
						if (PubClass.isInteger(taxiGPSCountEveryThreadStr)) {
							int threadID = 0;
							int tempCount = 0;//临时变量，计算GPS数量
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
									tempCount = 0;//归零									
									tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
								}	
							}
						}
						//不为整数
						else {
							int integerPart =PubClass.obtainIntegerPart(taxiGPSCountEveryThreadStr);
							int threadID = 0;
							int tempCount = 0;//临时变量，计算GPS数量
							int tempThreadCount = 0;//临时变量，计算当前线程数目
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
										tempCount = 0;//归零	
										tempThreadCount++;
										tempTaxiGPSArrayList = new ArrayList<TaxiGPS>();
									}									
								}	
							}
						}
					}
					threadPool.shutdown(); //关闭后不能加入新线程，队列中的线程则依次执行完
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
	
	/*通过线程池计算符合条件的出租车GPS点
	 * threadID：线程
	 * targetEdge：目标路段
	 * taxiGPSArrayList：传入的GPS点
	 * eligibleGPSArrayList:计算后符合条件的GPS点
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
	/*剔除速度为零的GPS点
	 * 这一步应道写在地图匹配算法内，写在此处貌似不合理，因为去掉了部分GPS点，而这些点的位置坐标有时是有用的
	 * 非常影响道路的匹配结果
	 * 当speed为零时表示出租车载某处暂时停留，此时GPS记录信息，heading也为零
	 * 由于heading为零，因此进行方向打分时会影响道路的方向得分，因此要将这些speed为零的点去掉*/
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
	
	/*剔除未载客数据，只保留载客数据
	 * taxiGPSArrayList：输入数据
	 * carryPassengerGPSDataArrayList：处理后的载客数据*/
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
	
	/*获得满足时空约束条件的出租车ID,taxiID
	 * taxiIDArrayList:taxiID
	 * startTimeStr:开始时间
	 * endTimeStr：结束时间
	 * minLog：最小经度
	 * maxLog：最大经度
	 * minLat：最小纬度
	 * maxLat：最大纬度*/
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
			Class.forName(driver);// 加载驱动程序
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
	 * 根据时间约束条件获得所有唯一的出租车ID,taxiID
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
			Class.forName(driver);// 加载驱动程序
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
	 * 根据起止时间以及表名获得出租车ID
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
			Class.forName(driver);// 加载驱动程序
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
			Class.forName(driver);// 加载驱动程序
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
			Class.forName(driver);// 加载驱动程序
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
	 * 写paper的临时一次性函数
	 ****************************************************/
	
	/**
	 * 从数据库中提取上客点数
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
			Class.forName(driver);// 加载驱动程序
			conn = DriverManager.getConnection(url, user, password);
	     	if(!conn.isClosed()){
	     		String timeStr = startTimeStr.substring(0,13) + '%';
	     		String sqlString = "SELECT COUNT(*) as count FROM " + tableName + " WHERE T_LocalTime like ? AND T_ChangedStatus = 1";
	        	pstmt = conn.prepareStatement(sqlString,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);       	
	        	pstmt.setString(1, timeStr);
				rs = pstmt.executeQuery();
				rs.next();
				pickUpsCount = rs.getInt("count");//获取总行数
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
	 * 获得满足距离条件的数目
	 * @param distanceLowerLimit	距离下限
	 * @param distanceUpperLimit	距离上限
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
			Class.forName(driver);// 加载驱动程序
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
				distanceRangeCount = rs.getInt("count");//获取总行数
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
	 * 获取某小时内距离<= distance 的记录数目
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
			Class.forName(driver);// 加载驱动程序
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
				distanceRangeCount = rs.getInt("count");//获取总行数
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
