package importDataToDatabase;

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

import mapMatchingGPS.CorrectedNode;
import mapMatchingGPS.DatabaseFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.TaxiGPS;

/***
 * ����SQL�ļ����ݵ�mySQL���ݿ�
 * 1.����,�����ݿ��и�����Ӧ�ֶν�����ʱ��t_LogTrack0��t_LogTrack1��t_LogTrack2��t_LogTrack3
 * 2.Ȼ�󣬽�����t_LogTrack0.SQL��t_LogTrack1.SQL��t_LogTrack2.SQL��t_LogTrack3.SQL
 * ������ʱ����t_LogTrack0��t_LogTrack1��t_LogTrack2��t_LogTrack3
 * 3.��󣬶�ȡ��ʱ�������ݣ����뵽Ŀ�����
 * */
public class ImportDataToMySQL {
	
	/**
	 * �˵��빤����148������ʵ�鵼�����ݣ�����Ǩ�Ƶ������������ز���Ӧ���޸���
	 * */
	
	//�������ݿ�����
	public static final String DBDRIVER1 = "org.gjt.mm.mysql.Driver";
	public static final String DBDRIVER = "com.mysql.jdbc.Driver";//����
	//���ݿ����ӣ�����ת��Ϊutf8 "?useUnicode=true&characterEncoding=UTF-8"���������������
	public static final String DBURL = "jdbc:mysql://192.168.2.148:3306/dbname?useUnicode=true&characterEncoding=UTF-8";
	public static final String DBUSER = "root";
	public static final String DBPASSWORD = "123456"; 
	public static final String HOST = "localhost";//�����Ŀ�����ݿ����ڵ�����  
	public static final String PORT = "3306";//ʹ�õĶ˿ں�
	public static final String IMPORTDATABASENAME = "dbname";//�����Ŀ�����ݿ������   
    //�������� 
    public static final int onceInsertRowCount = 100;//ÿ����table����ļ�¼����100��
    public static final int onceObtainRowCount = 50000;//���ݿ���ÿ�ζ�ȡ��¼����50000��
    
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
			//�����ļ���
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
				//t_LogTrack0,t_LogTrack1,t_LogTrack2,t_LogTrack3��ʱ���е�����ɾ��
				for (int i = 0; i < 4; i++) {
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String deleteTableName = tempStr1 + tempStr2;//���ݿ���Ҫ��ȡ�ı���
					double tempStartTime = System.nanoTime();
					deleteDataFromTable(deleteTableName);
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println(fileFolderNameStr + ",ɾ��������" + deleteTableName + "����ʱ�䣺" + tempProcessTime + '\n');
				}
				//SQL���ݵ������ݿ�
				for (int i = 0; i < 4; i++) {
					double tempStartTime = System.nanoTime();
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String readTableName = tempStr1 + tempStr2;//���ݿ���Ҫ�������ݵı���
					String importPath = fileFolderNameStr + "/" +readTableName + ".SQL";//Ҫ�������ݿ��е�SQL�ļ�·��
					importSql(importPath);//�������ݿ�
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println(fileFolderNameStr + "," + readTableName + "�������ʱ�䣺" + tempProcessTime + '\n');
					Thread.sleep(5000);
				}				
				//��ȡ���е����ݲ�����Ŀ�����
				for (int i = 0; i < 4; i++) {
					String tempStr1 = "t_LogTrack";
					String tempStr2 = String.valueOf(i);
					String readTableName = tempStr1 + tempStr2;//���ݿ���Ҫ��ȡ�ı���
					String insertTableName = "sixToSeven_2014";//���ݿ���Ҫ����ı���
					double tempStartTime = System.nanoTime();
					readFromTableAndInsertDataToDatabase(fileFolderNameStr,readTableName, insertTableName);
					double tempEndTime = System.nanoTime();
					double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
					System.out.println("�����" + readTableName + "����ʱ�䣺" + tempProcessTime + '\n');
				}
				double endImportTime = System.nanoTime();
				double importTime = (endImportTime - startImportTime)/Math.pow(10, 9);
				System.out.println("��������" + fileFolderNameStr + "����ʱ�䣺" + importTime + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
			    
	}
	
	/*��γ��ת������*/
	public static double divMillion(long Value)
	{
		double doubleResult = 0;
		try {
			double longtitudeOrlatitude = Value*0.000001;
			String result = String.format("%.6f", longtitudeOrlatitude);//����С�������λ���������������� 
			doubleResult = Double.parseDouble(result);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());			
		}		
	  	return doubleResult;
	}
	    
    /*UTCת��Ϊ����ʱ��*/
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
     * ����Java����MySql���ݿ�ĵ���
     * ����˼��:ͨ��Java�����������ִ����Ӧ������
     * */
    public static void importSql(String importPath){		  
//        importPath = "E:/20140101/t_LogTrack0.SQL";//�����Ŀ���ļ����ڵ�λ��  
    	try {
    		File file1=new File(importPath);
       	 	if(file1.getName().equals("t_LogTrack0.SQL")||file1.getName().equals("t_LogTrack1.SQL")||
   	 			file1.getName().equals("t_LogTrack2.SQL")||file1.getName().equals("t_LogTrack3.SQL")){
       	 		//��һ������ȡ��¼�������  
       	        String loginCommand = new StringBuffer().append("mysql -u").append(DBUSER).append(" -p").append(DBPASSWORD).append(" -h").append(HOST)  
       	        .append(" -P").append(PORT).toString();
       	        //�ڶ�������ȡ�л����ݿ⵽Ŀ�����ݿ���������
       	        String switchCommand = new StringBuffer("use ").append(IMPORTDATABASENAME).toString(); 
       	        //����������ȡ������������  
       	        String importCommand = new StringBuffer("source ").append(importPath).toString(); 
       	        System.out.println("���ڵ������ݣ��ȴ�����" + '\n');
       	        Runtime runtime = Runtime.getRuntime();
	 			//��½Mysql
	 			Process process = runtime.exec(loginCommand);
	 			OutputStream os = process.getOutputStream();//����̨��������Ϣ��Ϊ�����
	 			OutputStreamWriter writer = new OutputStreamWriter(os);
	 			writer.write(switchCommand + "\r\n" + importCommand);		
	 			writer.flush();
	 			writer.close();
	 			os.close();
	 			System.out.println("��ִ��,���ݵ���ɹ���" + '\n');       	        
       	 	}
       	 	else {
       	 		System.out.println("�ļ�������,���ݵ���ʧ�ܣ�" + '\n'); 
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    		 
    }    
    
    /**
     * ��ȡ���ݿ�����ݣ��������ݿ���в������ݣ�
     * ������
     * readTableName��Ҫ��ȡ��Ϣ�����ݱ�����
     * insertTableName:��������Ϣ�����ݱ�����
     * ���̣�
     * 1.ÿ�δ����ݱ�readTableName��ȡonceObtainRowCount����
     * 2.onceObtainRowCount��50000�����ݶ�ν��в��룬ÿ�β�����������ΪcurObtainRowCount��100��������������
     * 3.ÿһ�β��룬���������ʵ�ֻع�����ָ�붨λ��������У�����һ�б������ݼ��ϣ�ȡ��һ������������curObtainRowCount�����в��� ��ֱ������ȫ������
     * */
    public static void readFromTableAndInsertDataToDatabase(String fileFolderNameStr, String readTableName, String insertTableName){   							
    	Connection conn = null;
	    PreparedStatement preStatement = null;
	    ResultSet resultSet = null;
	    System.out.println(readTableName + "��׼���������ݣ��ȴ�����" + '\n');
	    long totlenumber = 0;//���¼��������	
		try {
			//��ó��⳵����
			Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
			/**
			 * String sqlcount = "SELECT COUNT(*) as count FROM ?";//�Ǵ���ģ�����������Ϊ����
			 * */			
			String sqlcount = "SELECT COUNT(*) as count FROM " + readTableName;
			preStatement = conn.prepareStatement(sqlcount);
			resultSet = preStatement.executeQuery();
			resultSet.next();
			totlenumber = resultSet.getLong("count");//��ȡ������
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
		ResultSet rs1 = null;		
		Statement stmt1 = null;
	    PreparedStatement pstmt2 = null;
	    try {								
  		    //��ó��⳵���� 
  		    long firstRowIndex = 0;//ÿ��Ҫ��ȡ�ĵ�һ�е�����  		    
  		    long runnumbers = totlenumber%onceObtainRowCount==0?(totlenumber/onceObtainRowCount):(totlenumber/onceObtainRowCount)+1;//��ȡ�Ĵ��� 	
  		    for(int r = 0; r < runnumbers; r++)
  		    {
  		    	double startTime = System.nanoTime();
  			    System.out.println("�ļ���" + fileFolderNameStr + ",��" + readTableName + "��ʼ�����" + r + ":" + (runnumbers-1) + "������" + '\n');	    
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
  			    stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);//statement���������ƶ���ָ�룬prestatement���ܴ��������ƶ���ָ��
  			    rs1 = stmt1.executeQuery(SQL);
  			    //�������
  			    String insertSql = "INSERT INTO " + insertTableName +" (T_TargetID,T_LOCALTime,T_Longitude,T_Latitude,T_Speed,T_Status,T_Heading) VALUES(?, ?, ?, ?, ?, ?, ?)";
  			    pstmt2 = conn.prepareStatement(insertSql,ResultSet.CONCUR_UPDATABLE);
  			    int tempCount = 0;//��ǰ��¼������
  			    int curInserCount = 0;//��ǰ�������
  			    int tempTemp = 0;
  			    int exceptionRowNumber = 0;//�����쳣�����ݼ��кţ���ʼֵΪ��
  			    while(rs1.next())//������б�Ŵ�1��ʼ����һ��Ϊ�� 1���ڶ���Ϊ�� 2����
  			    {
  			    	try {
  			    		tempCount ++;
  			    		tempTemp ++;
  				        String targetID = rs1.getString("T_TargetID");		   
  	 			        long T_UTCTime = rs1.getInt("T_UTCTime");
  	 			        String localtime = utcToLocal(T_UTCTime,"yyyy-MM-dd HH:mm:ss");//����ʱ�� 			   
  	 			        long T_Longitude = rs1.getInt("T_Longitude");
  	 			        double longitude = divMillion(T_Longitude);//γ�� 			   
  	 			        long T_Latitude = rs1.getInt("T_Latitude");    			   
  	 			        double latitude = divMillion(T_Latitude);//����			   
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
  				        System.out.print("�ļ���" + fileFolderNameStr + ",��" + readTableName + "��" + r + ":" + (runnumbers - 1) + "���Σ�" + "�����" + tempCount + ":"
			 			+ curObtainRowCount + ":" + totlenumber + "������" + '\n');
  				        //ÿ1000����¼����������
  				        if (curInserCount < insertCount) {
  				        	if (tempTemp == onceInsertRowCount) {
  				        		double tempStartTime = System.nanoTime();
  				        		System.out.print("���ڲ������ݣ��ȴ�����" + '\n');
  				        		conn.setAutoCommit(false);//�޸�Ĭ�ϵ��Զ��ύ����
  				        		pstmt2.executeBatch();
  				        		conn.commit();
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
								pstmt2.executeBatch();
								conn.commit();
								double tempEndTime = System.nanoTime();
  				  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
  				  			    System.out.println("������������ʱ�䣺" + tempProcessTime + '\n');
							} 				        	
						} 				        	        
					} catch (SQLException e) {
						// TODO: handle exception
				  		 e.printStackTrace();
				  		 System.out.println(e.getMessage());
				  		if (conn != null) { 
				  			System.out.print("���ݲ���ʧ�ܣ�����ع�����" + '\n');
				  			conn.rollback();//����sql�쳣������ع�
				  			conn.commit();				  			
				  			exceptionRowNumber ++;
				  			rs1.absolute(exceptionRowNumber);				  			
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
  			    System.out.println("�����" + r + "����������ʱ�䣺" + processTime + '\n');
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
     * ɾ����������
     * deleteTableName:Ҫɾ�����ݵı���
     * */
    public static void deleteDataFromTable(String deleteTableName){
    	Connection conn = null;
	    Statement stmt = null;
		try {
			Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);		
			String deleteSQL = "DELETE FROM " + deleteTableName;
			stmt = conn.createStatement();
			System.out.println(deleteTableName + "����ɾ�����ݣ��ȴ�����" + '\n');
			stmt.execute(deleteSQL);
			System.out.println(deleteTableName + "����ɾ���ɹ���" + '\n');
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
  				conn.close();
  			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("����ɾ��ʧ�ܣ�" + '\n');
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
     * ���ݿ��������
     *****************************************************************************/
    
    /**
     * ���������GPS�켣������뵽���ݿ���
     * @param correctedOriginalTaxiTrackArrayList
     * @param insertTableName
     */
    public void insertCorrectedTaxiDataToDatabase(ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList, String insertTableName, int currentTaxiIndex, int totalTaxiCount) {
    	Connection conn = null;
	    System.out.println("׼���������ݣ��ȴ�����" + '\n');	    		
	    PreparedStatement pstmt2 = null;
	    try {       
		    //�������
	    	Class.forName(DBDRIVER).newInstance();
			conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
		    String insertSql = "INSERT INTO " + insertTableName +" (T_TargetID,T_LOCALTime,T_Longitude,T_Latitude,T_CorrectLongitude,T_CorrectLatitude," +
		    		"T_Speed,T_Status,T_Heading, T_StreetNo, T_StreetName, T_ChangedStatus, T_TripDirection, T_TripDistance) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		    pstmt2 = conn.prepareStatement(insertSql, ResultSet.CONCUR_UPDATABLE);
		    //������б�Ŵ�1��ʼ����һ��Ϊ�� 1���ڶ���Ϊ�� 2����
		    for (int i = 0; i < correctedOriginalTaxiTrackArrayList.size(); i++) {
				TaxiGPS correctedOriginalTaxiGPS = correctedOriginalTaxiTrackArrayList.get(i);
				try {
		    		double tempStartTime = System.nanoTime();
			        String targetID = correctedOriginalTaxiGPS.getTargetID(); 	 			        
 			        String localtime = correctedOriginalTaxiGPS.getLocalTime();//����ʱ�� 			   
 			        double originalLongitude = correctedOriginalTaxiGPS.getLongitude();
 			        double correctLongitude = correctedOriginalTaxiGPS.getCorrectLongitude();
 			        double originalLatitude = correctedOriginalTaxiGPS.getLatitude();//����			   
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
	        		conn.setAutoCommit(false);//�޸�Ĭ�ϵ��Զ��ύ����
	        		pstmt2.executeBatch();
	        		conn.commit();
	        		System.out.print("taxi:" + currentTaxiIndex + ":" + (totalTaxiCount - 1)+ "���ڲ����" + i + "�����ݣ��ȴ�����" + '\n');	
					double tempEndTime = System.nanoTime();
	  			    double tempProcessTime = (tempEndTime - tempStartTime)/Math.pow(10, 9);
	  			    System.out.print("��" + i + "�����ݲ���ɹ�������ʱ�䣺" + tempProcessTime + "s" + '\n');
					continue;			         				        	        
				} catch (SQLException e) {
					// TODO: handle exception
			  		 e.printStackTrace();
			  		 System.out.println(e.getMessage());
			  		if (conn != null) { 
			  			System.out.print("���ݲ���ʧ�ܣ�����ع�����" + '\n');
			  			conn.rollback();//����sql�쳣������ع�
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
     * ȡ��ĳ��ʱ������ݽ��о������������ݿ���
     * @param startTime	��ʼʱ���磬2014-06-05 00:00:00
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
  			    System.out.print("���⳵" + taxiIDString + ":" + i + ":" + taxiCount + ":�������ݲ������ݿ�ʱ��" + tempProcessTime + "s" + '\n');
			}
			double endTime = System.nanoTime();
			double processTime = (endTime - startTime)/Math.pow(10, 9);
    		System.out.print("����ʱ�䣺" + processTime + '\n' + ":done!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());	
		}   	
    }
    
    
    
    
    
    
    
    
    
}
