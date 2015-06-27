package importDataToDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utilityPackage.PubParameter;


public class ImportDataToOracle {
	
	//�������� 
    public static final int onceInsertRowCount = 100;//ÿ����table����ļ�¼����100��
    public static final int onceObtainRowCount = 50000;//���ݿ���ÿ�ζ�ȡ��¼����50000��

	/**
	 * 1.���mySQL��������:���ݿ���ÿ�ζ�ȡ��¼��,50000��
	 * 2.�����β���oracle���ݿ�:ÿ����table����ļ�¼����100��
	 * �ο���importDataToMySQL����
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
    	long totlenumber = 0;//���¼��������
    	String readTableName = "sixtoseven_2014_process";//Ҫ��ȡ�����ݱ���
    	String insertTableName = "sixtoseven_2014_process";
		try {
			Class.forName(mySQLDriver);		 	
			mySQLConn = DriverManager.getConnection(mySQLUrl, mySQLUser, mySQLPassword);
			String sqlcount = "SELECT COUNT(*) as count FROM " + readTableName;
			preStatement = mySQLConn.prepareStatement(sqlcount);
			resultSet = preStatement.executeQuery();
			resultSet.next();
			totlenumber = resultSet.getLong("count");//��ȡ������
			int temp = Integer.parseInt(String.valueOf(totlenumber));
			System.out.println("�ܹ���" + totlenumber + "��" + '\n');
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
  		    //��ó��⳵���� 
  		    long firstRowIndex = 0;//ÿ��Ҫ��ȡ�ĵ�һ�е�����  		    
  		    long runnumbers = totlenumber%onceObtainRowCount==0?(totlenumber/onceObtainRowCount):(totlenumber/onceObtainRowCount)+1;//��ȡ�Ĵ��� 	
  		    for(int r = 0; r < runnumbers; r++)
  		    {
  		    	PreparedStatement orclPstmt = null;
  		    	Statement mySQLStmt = null;
  		    	double startTime = System.nanoTime();
  			    System.out.println("��" + readTableName + "��ʼ�����" + r + ":" + (runnumbers-1) + "������" + '\n');	    
  			    int insertCount = 0;//���ݱ������ݵĲ������ 			      			      			    
  			    int curObtainRowCount = onceObtainRowCount;//��ǰÿ�ζ�ȡ����������
  			    //���һ������ȡ�õ������������ܲ���50000
  			    if (r == runnumbers - 1) {
  			    	int temp = Integer.parseInt(String.valueOf(totlenumber));
  			    	curObtainRowCount = temp - onceObtainRowCount * r;
				}
  			    //�������ݵĲ�������Լ���ô���������
  			    insertCount = curObtainRowCount%onceInsertRowCount==0?(curObtainRowCount/onceInsertRowCount):(curObtainRowCount/onceInsertRowCount)+1;//��ȡ�Ĵ���			    
  			    String SQL = "SELECT * FROM " + readTableName + " LIMIT " + firstRowIndex + "," + curObtainRowCount;//firstRowIndex:��¼��ʼ��������curObtainRowCount��ȡ�ü�¼������
  			    mySQLStmt = mySQLConn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);//statement���������ƶ���ָ�룬prestatement���ܴ��������ƶ���ָ��
  			    mySQLResultSet = mySQLStmt.executeQuery(SQL);
  			    //�������
  			    String insertSql = "INSERT INTO " + insertTableName + " VALUES(?,?, MDSYS.SDO_GEOMETRY(2001,8307,MDSYS.SDO_POINT_TYPE(?,?,0),NULL,NULL),?,?,?)";
  			  	orclPstmt = orclConn.prepareStatement(insertSql);
  			    int tempCount = 0;//��ǰ��¼������
  			    int curInserCount = 0;//��ǰ�������(����)
  			    int tempTemp = 0;
  			    int exceptionRowNumber = 0;//�����쳣�����ݼ��кţ���ʼֵΪ��
  			    while(mySQLResultSet.next())//������б�Ŵ�1��ʼ����һ��Ϊ�� 1���ڶ���Ϊ�� 2����
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
  				        System.out.print(",��" + readTableName + "��" + r + ":" + (runnumbers - 1) + "���Σ�" + "�����" + tempCount + ":"
			 			+ curObtainRowCount + ":" + totlenumber + "������" + '\n');
  				        //ÿ100����¼����������
  				        if (curInserCount < insertCount) {
  				        	if (tempTemp == onceInsertRowCount) {
  				        		double tempStartTime = System.nanoTime();
  				        		System.out.print("���ڲ������ݣ��ȴ�����" + '\n');
  				        		orclConn.setAutoCommit(false);//�޸�Ĭ�ϵ��Զ��ύ����
  				        		orclPstmt.executeBatch();
  				        		orclConn.commit();
								curInserCount ++;
								tempTemp = 0;
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.print("���ݲ���ɹ���" + '\n');
  				  			    System.out.println("����" + onceInsertRowCount + "����������ʱ�䣺" + tempProcessTime + '\n');
								continue;
							}
						} 	        
  				        if (curInserCount == insertCount - 1) {
							if (tempCount == curObtainRowCount) {
								double tempStartTime = System.nanoTime();
								System.out.print("���ڲ������ݣ��ȴ�����" + '\n');
								orclPstmt.executeBatch();
								orclConn.commit();
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.println("������������ʱ�䣺" + tempProcessTime + '\n');
							} 				        	
						} 				        	        
					} catch (SQLException e) {
						// TODO: handle exception
				  		 e.printStackTrace();
				  		 System.out.println(e.getMessage());
				  		if (orclConn != null) { 
				  			System.out.print("���ݲ���ʧ�ܣ�����ع�����" + '\n');
				  			orclConn.rollback();//����sql�쳣������ع�
				  			orclConn.commit();				  			
				  			exceptionRowNumber ++;
				  			mySQLResultSet.absolute(exceptionRowNumber);//ָ���ƶ�����һ�м�¼				  			
				  			tempCount = tempCount - tempTemp;//�Ѿ�����ļ�¼��
				  			tempCount ++;//ÿ�β���ʧ�ܵ�����Ҳ�����һ��
				  			if (tempCount%onceInsertRowCount == 0) {
								curInserCount = tempCount/onceInsertRowCount;
							}
				  			tempTemp = 0;//��ǰ�����¼������Ϊ��
				  		}
					} 			    	
  			    }			    	
  			    int temp =  onceObtainRowCount * ( r + 1 );
  			    firstRowIndex = temp;//ÿ�ζ�ȡ�����һ�е�����
  			    double endTime = System.nanoTime();
  			    double processTime = (endTime - startTime)/Math.pow(10, 9);
  			    System.out.println("�����" + r + ":" + runnumbers + "����������ʱ�䣺" + processTime + "s" + '\n');
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
  	  	System.out.println("���ݵ������,��ʱ" + processTime + "s" + '\n');
  	  	
	}
	
	
	
}
