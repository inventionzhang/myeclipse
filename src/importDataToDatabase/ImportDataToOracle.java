package importDataToDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utilityPackage.PubParameter;


public class ImportDataToOracle {
	
	//参数设置 
    public static final int onceInsertRowCount = 100;//每次往table插入的记录数，100条
    public static final int onceObtainRowCount = 50000;//数据库中每次读取记录数，50000条

	/**
	 * 1.获得mySQL表中数据:数据库中每次读取记录数,50000条
	 * 2.分批次插入oracle数据库:每次往table插入的记录数，100条
	 * 参考：importDataToMySQL代码
	 * @param args
	 */
	public static void main(String[] args){	
		double sysStartTime = System.nanoTime();
		String orclDriver = PubParameter.ORCLDBDRIVER_STRING;
		String orclUrl = PubParameter.ORCLDBURL_STRING;
		String orclUser = PubParameter.ORCLDBUSER_STRING;
    	String orclPassword = PubParameter.ORCLDBPASSWORD_STRING;		
		String mySQLDriver = PubParameter.MYSQLDBDRIVER_STRING;
		String mySQLUrl = PubParameter.MYSQLDBURL_STRING;
		String mySQLUser = PubParameter.MYSQLDBUSER_STRING;
    	String mySQLPassword = PubParameter.MYSQLDBPASSWORD_STRING;
    	Connection mySQLConn = null;
    	PreparedStatement preStatement = null;
		ResultSet resultSet = null;
    	long totlenumber = 0;//表记录的总行数
    	String readTableName = "sixtoseven_2014_process";//要读取的数据表名
    	String insertTableName = "sixtoseven_2014_process";
		try {
			Class.forName(mySQLDriver);		 	
			mySQLConn = DriverManager.getConnection(mySQLUrl, mySQLUser, mySQLPassword);
			String sqlcount = "SELECT COUNT(*) as count FROM " + readTableName;
			preStatement = mySQLConn.prepareStatement(sqlcount);
			resultSet = preStatement.executeQuery();
			resultSet.next();
			totlenumber = resultSet.getLong("count");//获取总行数
			int temp = Integer.parseInt(String.valueOf(totlenumber));
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
		
		Connection orclConn = null;
		ResultSet mySQLResultSet = null;
	    try {	
	    	Class.forName(orclDriver);	 	
	    	orclConn = DriverManager.getConnection(orclUrl, orclUser, orclPassword);
  		    //获得出租车数量 
  		    long firstRowIndex = 0;//每次要读取的第一行的索引  		    
  		    long runnumbers = totlenumber%onceObtainRowCount==0?(totlenumber/onceObtainRowCount):(totlenumber/onceObtainRowCount)+1;//读取的次数 	
  		    for(int r = 0; r < runnumbers; r++)
  		    {
  		    	PreparedStatement orclPstmt = null;
  		    	Statement mySQLStmt = null;
  		    	double startTime = System.nanoTime();
  			    System.out.println("表" + readTableName + "开始插入第" + r + ":" + (runnumbers-1) + "批数据" + '\n');	    
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
  			    mySQLStmt = mySQLConn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);//statement创建自由移动的指针，prestatement不能创建自由移动的指针
  			    mySQLResultSet = mySQLStmt.executeQuery(SQL);
  			    //待插入表
  			    String insertSql = "INSERT INTO " + insertTableName + " VALUES(?,?, MDSYS.SDO_GEOMETRY(2001,8307,MDSYS.SDO_POINT_TYPE(?,?,0),NULL,NULL),?,?,?)";
  			  	orclPstmt = orclConn.prepareStatement(insertSql);
  			    int tempCount = 0;//当前记录的条数
  			    int curInserCount = 0;//当前插入次数(批次)
  			    int tempTemp = 0;
  			    int exceptionRowNumber = 0;//捕获异常的数据集行号，初始值为零
  			    while(mySQLResultSet.next())//结果集行编号从1开始，第一行为行 1，第二行为行 2……
  			    {
  			    	try {
  			    		tempCount ++;
  			    		tempTemp ++;
  				        String targetID = mySQLResultSet.getString("T_TargetID");		   
  	 			        String T_LocalTime = mySQLResultSet.getString("T_LocalTime");		   
  	 			        double T_Longitude = mySQLResultSet.getDouble("T_Longitude");		   
  	 			        double T_Latitude = mySQLResultSet.getDouble("T_Latitude");    			   			   
  	 			        double speed = mySQLResultSet.getDouble("T_Speed");
  	 			        long status = mySQLResultSet.getLong("T_Status");
  	 			        double heading = mySQLResultSet.getDouble("T_Heading"); 	
  	 			        orclPstmt.setString(1, targetID);
  	 			        orclPstmt.setString(2, T_LocalTime);
		  	 			orclPstmt.setDouble(3, T_Longitude);
		  	 			orclPstmt.setDouble(4, T_Latitude);
		  	 			orclPstmt.setDouble(5, speed);
		  	 			orclPstmt.setDouble(6, heading);
		  	 			orclPstmt.setLong(7, status);
		  	 			orclPstmt.addBatch();
  				        System.out.print(",表" + readTableName + "第" + r + ":" + (runnumbers - 1) + "批次：" + "插入第" + tempCount + ":"
			 			+ curObtainRowCount + ":" + totlenumber + "条数据" + '\n');
  				        //每100条记录，批量插入
  				        if (curInserCount < insertCount) {
  				        	if (tempTemp == onceInsertRowCount) {
  				        		double tempStartTime = System.nanoTime();
  				        		System.out.print("正在插入数据，等待……" + '\n');
  				        		orclConn.setAutoCommit(false);//修改默认的自动提交数据
  				        		orclPstmt.executeBatch();
  				        		orclConn.commit();
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
								orclPstmt.executeBatch();
								orclConn.commit();
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.println("插入数据所用时间：" + tempProcessTime + '\n');
							} 				        	
						} 				        	        
					} catch (SQLException e) {
						// TODO: handle exception
				  		 e.printStackTrace();
				  		 System.out.println(e.getMessage());
				  		if (orclConn != null) { 
				  			System.out.print("数据插入失败，事务回滚……" + '\n');
				  			orclConn.rollback();//出现sql异常，事务回滚
				  			orclConn.commit();				  			
				  			exceptionRowNumber ++;
				  			mySQLResultSet.absolute(exceptionRowNumber);//指针移动到下一行记录				  			
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
  			    System.out.println("插入第" + r + ":" + runnumbers + "批数据所用时间：" + processTime + "s" + '\n');
  			    if (orclPstmt != null) {
    		    	orclPstmt.close();
	  			}
  			    if (mySQLStmt != null) {
  			    	mySQLStmt.close();
  				}
  		    } 		    
		    if (mySQLResultSet != null) {
		    	mySQLResultSet.close();
			}		 
		    if (mySQLConn != null) {
		    	mySQLConn.close();
			}
		    if (orclConn != null) {
		    	orclConn.close();
			}
  	  	}catch(Exception e){
  	  		e.printStackTrace();
  	  		System.out.println(e.getMessage());
  		 
  	  	}finally{
  	  		try
  	  		{
  	  			if (mySQLResultSet != null) {
  	  				mySQLResultSet.close();
  	  			}
	  	  		if (mySQLConn != null) {
			    	mySQLConn.close();
				}
			    if (orclConn != null) {
			    	orclConn.close();
				}
  	  		}catch(Exception e){
		  		e.printStackTrace();
		  		System.out.println(e.getMessage());
  	  		}
  	  	}
  	  	double sysEndTime = System.nanoTime();
	    double processTime = (sysEndTime - sysStartTime)/Math.pow(10, 9);
  	  	System.out.println("数据导入结束,用时" + processTime + "s" + '\n');
  	  	
	}
	
	
	
}
