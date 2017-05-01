package org.lmars.network.database;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.lmars.network.mapMatchingGPS.CorrectedNode;
import org.lmars.network.mapMatchingGPS.DatabaseFunction;
import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.TaxiGPS;


/***
 * 导入SQL文件数据到mySQL数据库
 * 1.首先,在数据库中根据相应字段建立临时表，t_LogTrack0、t_LogTrack1、t_LogTrack2、t_LogTrack3
 * 2.然后，将数据t_LogTrack0.SQL、t_LogTrack1.SQL、t_LogTrack2.SQL、t_LogTrack3.SQL
 * 导入临时表中t_LogTrack0、t_LogTrack1、t_LogTrack2、t_LogTrack3
 * 3.最后，读取临时表中数据，插入到目标表中
 * */
public class ImportDataToMySQL {
	
	/**
	 * 此导入工具在148服务器实验导入数据，后来迁移到本机，因此相关参数应当修改下
	 * */
	
	//定义数据库驱动
	public static final String DBDRIVER1 = "org.gjt.mm.mysql.Driver";
	public static final String DBDRIVER = "com.mysql.jdbc.Driver";//驱动
	//数据库连接，编码转换为utf8 "?useUnicode=true&characterEncoding=UTF-8"解决中文乱码问题
	public static final String DBURL = "jdbc:mysql://192.168.2.148:3306/dbname?useUnicode=true&characterEncoding=UTF-8";
	public static final String DBUSER = "root";
	public static final String DBPASSWORD = "123456"; 
	public static final String HOST = "localhost";//导入的目标数据库所在的主机  
	public static final String PORT = "3306";//使用的端口号
	public static final String IMPORTDATABASENAME = "dbname";//导入的目标数据库的名称   
    //参数设置 
    public static final int onceInsertRowCount = 100;//每次往table插入的记录数，100条
    public static final int onceObtainRowCount = 50000;//数据库中每次读取记录数，50000条
    
    /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub           
//		String impPath = "E:/20140101/t_LogTrack0.SQL";
//		importSql(impPath);
//		deleteDataFromTable("test");
		
//		long utctime1 = 1403193712;
//		long utctime2 = 1403193752;
//		String localformat = "yyyy-MM-dd HH:mm:ss";
//		Date date1 = new Date(utctime1*1000);	
//		Date date2 = new Date(utctime2*1000);
//	  	SimpleDateFormat sdf = new SimpleDateFormat(localformat);
//	  	String processDateStr1 = sdf.format(date1);
//	  	date1.setHours(date1.getHours() + 8);
//	  	processDateStr1 = sdf.format(date1);
//	  	String processDateStr2 = sdf.format(date2);
//	  	date2.setHours(date2.getHours() + 8);
//	  	processDateStr2 = sdf.format(date2);
//		System.out.print(processDateStr1);
		
		try {
			//遍历文件夹
			for (int m = 1; m <= 31; m++) {
				if (m == 4) {
					continue;
//					System.out.println("");
				}
				double startImportTime = System.nanoTime();
				String tempDayStr = "";
				if (m < 10) {
					tempDayStr = "0" + String.valueOf(m);
				}
				else {
					tempDayStr = String.valueOf(m);
				}
				String fileFolderNameStr = "E:/GPS/201407" + tempDayStr + "035901" + "/Restore4GPS/var/gsvrbk/201407" + tempDayStr + "035901";
				//t_LogTrack0,t_LogTrack1,t_LogTrack2,t_LogTrack3临时表中的数据删掉
				for (int i = 0; i < 4; i++) {
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String deleteTableName = tempStr1 + tempStr2;//数据库中要读取的表名
					double tempStartTime = System.nanoTime();
					deleteDataFromTable(deleteTableName);
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println(fileFolderNameStr + ",删除表数据" + deleteTableName + "所用时间：" + tempProcessTime + '\n');
				}
				//SQL数据导入数据库
				for (int i = 0; i < 4; i++) {
					double tempStartTime = System.nanoTime();
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String readTableName = tempStr1 + tempStr2;//数据库中要导入数据的表名
					String importPath = fileFolderNameStr + "/" +readTableName + ".SQL";//要导入数据库中的SQL文件路径
					importSql(importPath);//导入数据库
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println(fileFolderNameStr + "," + readTableName + "入库所用时间：" + tempProcessTime + '\n');
					Thread.sleep(5000);
				}				
				//读取表中的数据并插入目标表中
				for (int i = 0; i < 4; i++) {
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String readTableName = tempStr1 + tempStr2;//数据库中要读取的表名
					String insertTableName = "sixToSeven_2014";//数据库中要插入的表名
					double tempStartTime = System.nanoTime();
					readFromTableAndInsertDataToDatabase(fileFolderNameStr,readTableName, insertTableName);
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println("插入表" + readTableName + "所用时间：" + tempProcessTime + '\n');
				}
				double endImportTime = System.nanoTime();
				double importTime = (endImportTime - startImportTime)/Math.pow(10, 9);
				System.out.println("导入数据" + fileFolderNameStr + "所用时间：" + importTime + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
			    
	}
	
	/*经纬度转换函数*/
	public static double divMillion(long Value)
	{
		double doubleResult = 0;
		try {
			double longtitudeOrlatitude = Value*0.000001;
			String result = String.format("%.6f", longtitudeOrlatitude);//保留小数点后六位，并进行四舍五入 
			doubleResult = Double.parseDouble(result);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());			
		}		
	  	return doubleResult;
	}
	    
    /*UTC转换为本地时间*/
    public static String utcToLocal(long utctime,String localformat)
    {
    	String processDateStr = "";
    	try {
    		Date date = new Date(utctime*1000);	  	  
    	  	SimpleDateFormat sdf = new SimpleDateFormat(localformat);
    	  	processDateStr = sdf.format(date);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());
		}  	  	  	  
  	  	return processDateStr;
    }
    
    /**
     * 利用Java进行MySql数据库的导入
     * 总体思想:通过Java来调用命令窗口执行相应的命令
     * */
    public static void importSql(String importPath){		  
//        importPath = "E:/20140101/t_LogTrack0.SQL";//导入的目标文件所在的位置  
    	try {
    		File file1=new File(importPath);
       	 	if(file1.getName().equals("t_LogTrack0.SQL")||file1.getName().equals("t_LogTrack1.SQL")||
   	 			file1.getName().equals("t_LogTrack2.SQL")||file1.getName().equals("t_LogTrack3.SQL")){
       	 		//第一步，获取登录命令语句  
       	        String loginCommand = new StringBuffer().append("mysql -u").append(DBUSER).append(" -p").append(DBPASSWORD).append(" -h").append(HOST)  
       	        .append(" -P").append(PORT).toString();
       	        //第二步，获取切换数据库到目标数据库的命令语句
       	        String switchCommand = new StringBuffer("use ").append(IMPORTDATABASENAME).toString(); 
       	        //第三步，获取导入的命令语句  
       	        String importCommand = new StringBuffer("source ").append(importPath).toString(); 
       	        System.out.println("正在导入数据，等待……" + '\n');
       	        Runtime runtime = Runtime.getRuntime();
	 			//登陆Mysql
	 			Process process = runtime.exec(loginCommand);
	 			OutputStream os = process.getOutputStream();//控制台的输入信息作为输出流
	 			OutputStreamWriter writer = new OutputStreamWriter(os);
	 			writer.write(switchCommand + "\r\n" + importCommand);		
	 			writer.flush();
	 			writer.close();
	 			os.close();
	 			System.out.println("已执行,数据导入成功！" + '\n');       	        
       	 	}
       	 	else {
       	 		System.out.println("文件不存在,数据导入失败！" + '\n'); 
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    		 
    }    
    
    /**
     * 读取数据库表数据，并向数据库表中插入数据：
     * 参数：
     * readTableName：要读取信息的数据表名字
     * insertTableName:待插入信息的数据表名字
     * 流程：
     * 1.每次从数据表readTableName中取onceObtainRowCount数据
     * 2.onceObtainRowCount（50000）数据多次进行插入，每次插入数据数量为curObtainRowCount（100），计算插入次数
     * 3.每一次插入，如果报错则实现回滚，将指针定位到报错的行，从下一行遍历数据集合，取得一定数量的数据curObtainRowCount，进行插入 ，直到数据全部插入
     * */
    public static void readFromTableAndInsertDataToDatabase(String fileFolderNameStr, String readTableName, String insertTableName){   							
    	Connection conn = null;
	    PreparedStatement preStatement = null;
	    ResultSet resultSet = null;
	    System.out.println(readTableName + "，准备插入数据，等待……" + '\n');
	    long totlenumber = 0;//表记录的总行数	
		try {
			//获得出租车数量
			Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
			/**
			 * String sqlcount = "SELECT COUNT(*) as count FROM ?";//是错误的，表名不能作为参数
			 * */			
			String sqlcount = "SELECT COUNT(*) as count FROM " + readTableName;
			preStatement = conn.prepareStatement(sqlcount);
			resultSet = preStatement.executeQuery();
			resultSet.next();
			totlenumber = resultSet.getLong("count");//获取总行数
			System.out.println("总共有" + totlenumber + "行" + '\n');
			if (resultSet != null) {
				resultSet.close();
			}
			if (preStatement != null) {
				preStatement.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preStatement != null) {
					preStatement.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
		  		System.out.println(e.getMessage());
			}			
		}		
		ResultSet rs1 = null;		
		Statement stmt1 = null;
	    PreparedStatement pstmt2 = null;
	    try {								
  		    //获得出租车数量 
  		    long firstRowIndex = 0;//每次要读取的第一行的索引  		    
  		    long runnumbers = totlenumber%onceObtainRowCount==0?(totlenumber/onceObtainRowCount):(totlenumber/onceObtainRowCount)+1;//读取的次数 	
  		    for(int r = 0; r < runnumbers; r++)
  		    {
  		    	double startTime = System.nanoTime();
  			    System.out.println("文件夹" + fileFolderNameStr + ",表" + readTableName + "开始插入第" + r + ":" + (runnumbers-1) + "批数据" + '\n');	    
  			    int insertCount = 0;//数据表中数据的插入次数 			      			      			    
  			    int curObtainRowCount = onceObtainRowCount;//当前每次读取的数据条数
  			    //最后一批数据取得的数据数量可能不是50000
  			    if (r == runnumbers - 1) {
  			    	int temp = Integer.parseInt(String.valueOf(totlenumber));
  			    	curObtainRowCount = temp - onceObtainRowCount * r;
				}
  			    //计算数据的插入次数以及获得待插入数据
  			    insertCount = curObtainRowCount%onceInsertRowCount==0?(curObtainRowCount/onceInsertRowCount):(curObtainRowCount/onceInsertRowCount)+1;//读取的次数			    
  			    String SQL = "SELECT * FROM " + readTableName + " LIMIT " + firstRowIndex + "," + curObtainRowCount;//firstRowIndex:记录开始的索引，curObtainRowCount：取得记录的条数
  			    stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);//statement创建自有移动的指针，prestatement不能创建自由移动的指针
  			    rs1 = stmt1.executeQuery(SQL);
  			    //待插入表
  			    String insertSql = "INSERT INTO " + insertTableName +" (T_TargetID,T_LOCALTime,T_Longitude,T_Latitude,T_Speed,T_Status,T_Heading) VALUES(?, ?, ?, ?, ?, ?, ?)";
  			    pstmt2 = conn.prepareStatement(insertSql,ResultSet.CONCUR_UPDATABLE);
  			    int tempCount = 0;//当前记录的条数
  			    int curInserCount = 0;//当前插入次数
  			    int tempTemp = 0;
  			    int exceptionRowNumber = 0;//捕获异常的数据集行号，初始值为零
  			    while(rs1.next())//结果集行编号从1开始，第一行为行 1，第二行为行 2……
  			    {
  			    	try {
  			    		tempCount ++;
  			    		tempTemp ++;
  				        String targetID = rs1.getString("T_TargetID");		   
  	 			        long T_UTCTime = rs1.getInt("T_UTCTime");
  	 			        String localtime = utcToLocal(T_UTCTime,"yyyy-MM-dd HH:mm:ss");//本地时间 			   
  	 			        long T_Longitude = rs1.getInt("T_Longitude");
  	 			        double longitude = divMillion(T_Longitude);//纬度 			   
  	 			        long T_Latitude = rs1.getInt("T_Latitude");    			   
  	 			        double latitude = divMillion(T_Latitude);//经度			   
  	 			        double speed = rs1.getDouble("T_Speed");
  	 			        long status = rs1.getLong("T_Status");
  	 			        double heading = rs1.getDouble("T_Heading"); 	
  	 			        pstmt2.setString(1, targetID);
		 			    pstmt2.setString(2, localtime);
		 			    pstmt2.setDouble(3, longitude);
			 			pstmt2.setDouble(4, latitude);
			 			pstmt2.setDouble(5, speed);
			 			pstmt2.setLong(6, status);
			 			pstmt2.setDouble(7, heading);
			 			if (longitude != 0 && latitude !=0) {
			 				pstmt2.addBatch();
						}			 			
  				        System.out.print("文件夹" + fileFolderNameStr + ",表" + readTableName + "第" + r + ":" + (runnumbers - 1) + "批次：" + "插入第" + tempCount + ":"
			 			+ curObtainRowCount + ":" + totlenumber + "条数据" + '\n');
  				        //每1000条记录，批量插入
  				        if (curInserCount < insertCount) {
  				        	if (tempTemp == onceInsertRowCount) {
  				        		double tempStartTime = System.nanoTime();
  				        		System.out.print("正在插入数据，等待……" + '\n');
  				        		conn.setAutoCommit(false);//修改默认的自动提交数据
  				        		pstmt2.executeBatch();
  				        		conn.commit();
								curInserCount ++;
								tempTemp = 0;
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.print("数据插入成功！" + '\n');
  				  			    System.out.println("插入" + onceInsertRowCount + "条数据所用时间：" + tempProcessTime + '\n');
								continue;
							}
						} 	        
  				        if (curInserCount == insertCount - 1) {
							if (tempCount == curObtainRowCount) {
								double tempStartTime = System.nanoTime();
								System.out.print("正在插入数据，等待……" + '\n');
								pstmt2.executeBatch();
								conn.commit();
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.println("插入数据所用时间：" + tempProcessTime + '\n');
							} 				        	
						} 				        	        
					} catch (SQLException e) {
						// TODO: handle exception
				  		 e.printStackTrace();
				  		 System.out.println(e.getMessage());
				  		if (conn != null) { 
				  			System.out.print("数据插入失败，事务回滚……" + '\n');
				  			conn.rollback();//出现sql异常，事务回滚
				  			conn.commit();				  			
				  			exceptionRowNumber ++;
				  			rs1.absolute(exceptionRowNumber);				  			
				  			tempCount = tempCount - tempTemp;//已经插入的记录数
				  			tempCount ++;//每次插入失败的数据也算插入一次
				  			if (tempCount%onceInsertRowCount == 0) {
								curInserCount = tempCount/onceInsertRowCount;
							}
				  			tempTemp = 0;//当前插入记录数重置为零
				  		}
					} 			    	
  			    }			    	
  			    int temp =  onceObtainRowCount * ( r + 1 );
  			    firstRowIndex = temp;//每次读取后最后一行的索引
  			    double endTime = System.nanoTime();
  			    double processTime = (endTime - startTime)/Math.pow(10, 9);
  			    System.out.println("插入第" + r + "批数据所用时间：" + processTime + '\n');
  		    }
  		    if (pstmt2 != null) {
  		    	pstmt2.close();
			}
		    if (rs1 != null) {
		    	rs1.close();
			}
		    if (stmt1 != null) {
		    	stmt1.close();
			}
		    if (conn != null) {
		    	conn.close();
			}
  	  	}catch(SQLException e){
  	  		e.printStackTrace();
  	  		System.out.println(e.getMessage());
  		 
  	  	}finally{
  	  		try
  	  		{
  	  			if (pstmt2 != null) {
  	  				pstmt2.close();
  	  			}
  	  			if (rs1 != null) {
  	  				rs1.close();
  	  			}
  	  			if (stmt1 != null) {
  	  				stmt1.close();
  	  			}
  	  			if (conn != null) {
  	  				conn.close();
  	  			}
  	  		}catch(Exception e){
  	  			System.out.println(e);
  	  		}
  	  	}   	
    }
    
    /**
     * 删除表中数据
     * deleteTableName:要删除数据的表名
     * */
    public static void deleteDataFromTable(String deleteTableName){
    	Connection conn = null;
	    Statement stmt = null;
		try {
			Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);		
			String deleteSQL = "DELETE FROM " + deleteTableName;
			stmt = conn.createStatement();
			System.out.println(deleteTableName + "正在删除数据，等待……" + '\n');
			stmt.execute(deleteSQL);
			System.out.println(deleteTableName + "数据删除成功！" + '\n');
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
  				conn.close();
  			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("数据删除失败！" + '\n');
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
  	  				conn.close();
  	  			}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
		  		System.out.println(e.getMessage());
			}			
		}
    } 
    
    /*****************************************************************************
     * 数据库插入数据
     *****************************************************************************/
    
    /**
     * 将纠正后的GPS轨迹坐标插入到数据库中
     * @param correctedOriginalTaxiTrackArrayList
     * @param insertTableName
     */
    public void insertCorrectedTaxiDataToDatabase(ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList, String insertTableName, int currentTaxiIndex, int totalTaxiCount) {
    	Connection conn = null;
	    System.out.println("准备插入数据，等待……" + '\n');	    		
	    PreparedStatement pstmt2 = null;
	    try {       
		    //待插入表
	    	Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
		    String insertSql = "INSERT INTO " + insertTableName +" (T_TargetID,T_LOCALTime,T_Longitude,T_Latitude,T_CorrectLongitude,T_CorrectLatitude," +
		    		"T_Speed,T_Status,T_Heading, T_StreetNo, T_StreetName, T_ChangedStatus, T_TripDirection, T_TripDistance) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		    pstmt2 = conn.prepareStatement(insertSql, ResultSet.CONCUR_UPDATABLE);
		    //结果集行编号从1开始，第一行为行 1，第二行为行 2……
		    for (int i = 0; i < correctedOriginalTaxiTrackArrayList.size(); i++) {
				TaxiGPS correctedOriginalTaxiGPS = correctedOriginalTaxiTrackArrayList.get(i);
				try {
		    		double tempStartTime = System.nanoTime();
			        String targetID = correctedOriginalTaxiGPS.getTargetID(); 	 			        
 			        String localtime = correctedOriginalTaxiGPS.getLocalTime();//本地时间 			   
 			        double originalLongitude = correctedOriginalTaxiGPS.getLongitude();
 			        double correctLongitude = correctedOriginalTaxiGPS.getCorrectLongitude();
 			        double originalLatitude = correctedOriginalTaxiGPS.getLatitude();//经度			   
 			        double correctLatitude = correctedOriginalTaxiGPS.getCorrectLatitude();
 			        double speed = correctedOriginalTaxiGPS.getSpeed();
 			        int status = correctedOriginalTaxiGPS.getStatus();
 			        double heading = correctedOriginalTaxiGPS.getHeading(); 
 			        int linkID = correctedOriginalTaxiGPS.getBelongLineID();
 			        String streetName = correctedOriginalTaxiGPS.getBelongLinkName();
 			        int changedStatus = correctedOriginalTaxiGPS.getChangedStatus();
 			        double tripDirection = correctedOriginalTaxiGPS.getTripDirection();
 			        double tripDistance = correctedOriginalTaxiGPS.getTripDistance();
 			        pstmt2.setString(1, targetID);
	 			    pstmt2.setString(2, localtime);
	 			    pstmt2.setDouble(3, originalLongitude);
		 			pstmt2.setDouble(4, originalLatitude);
		 			pstmt2.setDouble(5, correctLongitude);
		 			pstmt2.setDouble(6, correctLatitude);
		 			pstmt2.setDouble(7, speed);
		 			pstmt2.setInt(8, status);
		 			pstmt2.setDouble(9, heading);
		 			pstmt2.setInt(10, linkID);
		 			pstmt2.setString(11, streetName);
		 			pstmt2.setInt(12, changedStatus);
		 			pstmt2.setDouble(13, tripDirection);
		 			pstmt2.setDouble(14, tripDistance);
		 			pstmt2.addBatch();			        		        			        	        		
	        		conn.setAutoCommit(false);//修改默认的自动提交数据
	        		pstmt2.executeBatch();
	        		conn.commit();
	        		System.out.print("taxi:" + currentTaxiIndex + ":" + (totalTaxiCount - 1)+ "正在插入第" + i + "条数据，等待……" + '\n');	
					double tempEndTime = System.nanoTime();
	  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
	  			    System.out.print("第" + i + "条数据插入成功！插入时间：" + tempProcessTime + "s" + '\n');
					continue;			         				        	        
				} catch (SQLException e) {
					// TODO: handle exception
			  		 e.printStackTrace();
			  		 System.out.println(e.getMessage());
			  		if (conn != null) { 
			  			System.out.print("数据插入失败，事务回滚……" + '\n');
			  			conn.rollback();//出现sql异常，事务回滚
			  			conn.commit();				  			
			  		}
				}	
			}			    	
  		    
  		    if (pstmt2 != null) {
  		    	pstmt2.close();
			}
		    if (conn != null) {
		    	conn.close();
			}
  	  	}catch(Exception e){
  	  		e.printStackTrace();
  	  		System.out.println(e.getMessage());
  		 
  	  	}finally{
  	  		try
  	  		{
  	  			if (pstmt2 != null) {
  	  				pstmt2.close();
  	  			}
  	  			if (conn != null) {
  	  				conn.close();
  	  			}
  	  		}catch(Exception e){
	  	  		e.printStackTrace();
		  		System.out.println(e.getMessage());
  	  		}
  	  	} 
	}
    
    /**
     * 取得某段时间的数据进行纠正并插入数据库中
     * @param startTime	开始时间如，2014-06-05 00:00:00
     * @param endTime
     * @param insertTableName
     */
    public void insertCorrectedCoordinateToMySQLDatabase(String startTimeStr, String endTimeStr, String insertTableName){
    	try {
    		double startTime = System.nanoTime();
    		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
			DatabaseFunction.obtainUniqueTaxiIDAccordTime(taxiIDArrayList, startTimeStr, endTimeStr);
//    		taxiIDArrayList.add("MMC8000GPSANDASYN051113-21640-00000000");
    		int taxiCount = taxiIDArrayList.size();
			for (int i = 0; i < taxiIDArrayList.size(); i++) {
    			double tempStartTime = System.nanoTime();
    			String taxiIDString = taxiIDArrayList.get(i);
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, taxiIDString, startTimeStr, endTimeStr);
				Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
				taxiTrackMap.put(1, taxiGPSArrayList);
				ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
				ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
				ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();
    			MapMatchAlgorithm.coordinateCorr(taxiTrackMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList, GPSCorrectArrayList);
    			insertCorrectedTaxiDataToDatabase(correctedOriginalTaxiTrackArrayList, insertTableName, i, taxiCount);
    			double tempEndTime = System.nanoTime();
  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  			    System.out.print("出租车" + taxiIDString + ":" + i + ":" + taxiCount + ":纠正数据插入数据库时间" + tempProcessTime + "s" + '\n');
			}
			double endTime = System.nanoTime();
			double processTime = (endTime - startTime)/Math.pow(10, 9);
    		System.out.print("纠正时间：" + processTime + '\n' + ":done!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());	
		}   	
    }
    
    
    
    
    
    
    
    
    
}
