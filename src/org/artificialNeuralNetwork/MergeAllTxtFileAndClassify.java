package org.artificialNeuralNetwork;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import utilityPackage.PubClass;

import com.esri.arcgis.geodatabase.Field;

import entity.PropertiesUtilJAR;

/**
 * �ϲ�ͳ��ͨ��ʱ��
 * 1.�ϲ������ļ����ڵ�txt�ļ�������д��һ��txt�ļ���
 * 2.����·��ID��ͳ����Ϣ����,��ͬID·�ε���Ϣ�ۺ���һ��
 * 3.���ݹ�������ǹ����ն�ͳ����Ϣ����
 * @author whu
 *
 */
public class MergeAllTxtFileAndClassify {
	public static void main(String[] args) {
		(new MergeAllTxtFileAndClassify()).readStatisticTravelTime();//����txt�ļ���Ϣ�ϲ���һ��txt�ļ�
//		(new MergeAllTxtFileAndClassify()).classifyMergedFileAccordIDAndDirection();//����ID��·����Ϣ���з��࣬ID��ͬ��·�ι�Ϊһ��
		(new MergeAllTxtFileAndClassify()).classifyStatisticsAccordWhetherWorkday();
		System.out.print("done!");
	}
	
	/**
	 * ÿ�ζ�ȡһ���ļ���������ݣ���д���ļ�txtĩβ��
	 * �ļ����������������磬20130107
	 * @param infosArrayList
	 * @param
	 * 
	 */
	public void readStatisticTravelTime(){
		try {
			String directoryPathStr = PropertiesUtilJAR.getProperties("directoryPathFolder");
			String startFile1 = PropertiesUtilJAR.getProperties("startFile1");
			String endFile1 = PropertiesUtilJAR.getProperties("endFile1");
			String startFile2 = PropertiesUtilJAR.getProperties("startFile2");
			String endFile2 = PropertiesUtilJAR.getProperties("endFile2");
			String mergeFileOutputPath = PropertiesUtilJAR.getProperties("mergeFileOutputPath");
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");		
			//д�ļ�����
			File file = new File(mergeFileOutputPath);
			if (file.exists()) {
				file.delete();
			}
			System.out.print("��ʼд���ļ����⣺" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(mergeFileOutputPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("�ļ�����д�������" + '\n');
			//��ȡstartDate1��endDate1֮����ļ���
			java.util.Date startDate1 = df.parse(startFile1);//��ʼ�ļ�������	
		    java.util.Date endDate1 = df.parse(endFile1);//�����ļ�������
		    endDate1.setDate(endDate1.getDate() + 1);		    
		    String endCondDateStr1 = df.format(endDate1);//������������	
		    String tempEndDateStr1 = df.format(startDate1);
		    
//			do {
//				tempEndDateStr1 = df.format(startDate1);
//				String tempFileNameStr1 = tempEndDateStr1;
//				String fileFolderPathStr1 = directoryPathStr + tempFileNameStr1;
//				System.out.print("��ȡ�ļ��У�" + fileFolderPathStr1 + "���ļ�" + '\n');
//				ArrayList<String> infosArrayList = new ArrayList<String>();
//			    for (int j = 0; j < 4; j++) {
//					String fileNameStr = "allLinkTravelTime" + String.valueOf(j) + ".txt";
//					String filePathStr = fileFolderPathStr1 + "\\" + fileNameStr;
//					readFromTxtFile(filePathStr, infosArrayList);
//				}		
//				writeToTxtFile(mergeFileOutputPath, infosArrayList);
//				startDate1.setDate(startDate1.getDate() + 1);
//		    	tempEndDateStr1 = df.format(startDate1);
//			} while (!tempEndDateStr1.equals(endCondDateStr1));//��ȡ�ļ��Ľ�������
//			
			
			
			
			//��ȡstartDate2��endDate2֮����ļ���
			java.util.Date startDate2 = df.parse(startFile2);//��ʼ�ļ�������	
		    java.util.Date endDate2 = df.parse(endFile2);//�����ļ�������
		    endDate2.setDate(endDate2.getDate() + 1);		    
		    String endCondDateStr2 = df.format(endDate2);//������������		    		    
		    String tempEndDateStr2 = df.format(startDate2);
			do {				
				String tempFileNameStr2 = tempEndDateStr2;
				String fileFolderPathStr2 = directoryPathStr + tempFileNameStr2; 
				System.out.print("��ȡ�ļ��У�" + fileFolderPathStr2 + "���ļ�" + '\n');
				ArrayList<String> infosArrayList = new ArrayList<String>();
			    for (int j = 0; j < 4; j++) {
					String fileNameStr = "allLinkTravelTime" + String.valueOf(j) + ".txt";
					String filePathStr = fileFolderPathStr2 + "\\" + fileNameStr;
					readFromTxtFile(filePathStr, infosArrayList);
				}		
				writeToTxtFile(mergeFileOutputPath, infosArrayList);
				startDate2.setDate(startDate2.getDate() + 1);
		    	tempEndDateStr2 = df.format(startDate2);		    	
			} while (!tempEndDateStr2.equals(endCondDateStr2));//��ȡ�ļ��Ľ�������
		    System.out.print("done!�����ļ������ݺϲ�����!");
		    Thread.sleep(5000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ��ȡTxt�ļ�
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public void readFromTxtFile(String filePathStr, ArrayList<String> infosArrayList){		
		try {
			File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//���������������
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				while (str != null) {
					str = bufferedReader.readLine();
					if (str != null) {
						infosArrayList.add(str);
						System.out.print(str + "\n");
					}							
				}
				reader.close();
				System.out.print("������txt�ļ�:" + filePathStr + "\n");
			}
			else {
				System.out.print("ָ�����ļ������ڣ�" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * д��Txt�ļ�,д���ļ�ĩβ��
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public void writeToTxtFile(String filePathStr, ArrayList<String> infosArrayList){
		try {
			System.out.print("��ʼд��txt�ļ�");	
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//д���ļ���ʼ��
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//д���ļ�ĩβ��
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String tempStr = infosArrayList.get(i);
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("д��txt����" + '\n');	
	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * ����ID��·�ν��з��࣬ID��ͬ��·�ι�Ϊһ��
	 * 1.���ȣ�txt�ļ����ݶ����ڴ���
	 * 2.���·��ID
	 * 3.����·��ID����
	 * @param mergeFileOutputPath	�ϲ�������ļ�·��
	 * @param classifyMergedTxtFileAccordID	����ID���з������ļ�·��
	 */
	public void classifyMergedFileAccordIDAndDirection(){
		String mergeFileOutputPath = PropertiesUtilJAR.getProperties("mergeFileOutputPath");
		String classifyMergedTxtFileAccordID = PropertiesUtilJAR.getProperties("classifyMergedFile");
		ArrayList<String> IDArrayList = new ArrayList<String>();//·��ID����
		try {
			File file = new File(mergeFileOutputPath);
			if (file.exists()) {
				String encoding = "UTF-8";//���������������
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print("������" + str + "\n");
				while (str != null) {
					str = bufferedReader.readLine();
					if (str != null) {
						String[]tempArrayStr = str.split(",");
						if (!PubClass.isArraylistContainsID(IDArrayList, tempArrayStr[0])) {
							IDArrayList.add(tempArrayStr[0]);
//							System.out.print(str + "\n");
						}
					}							
				}
				reader.close();
				System.out.print("������txt�ļ�:" + mergeFileOutputPath + "\n");
				PubClass.sortIDArraylist(IDArrayList);//��ID������������
				System.out.print("�������ID���������" + '\n');
				//��ͬ��ID��Ϣд���ļ���
				file = new File(classifyMergedTxtFileAccordID);
				if (file.exists()) {
					file.delete();
				}
				
				System.out.print("��ʼд���ļ����⣺" + '\n');
				FileOutputStream outputStream = new FileOutputStream(new String(classifyMergedTxtFileAccordID));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
				write.append(description);
				bufferStream.write(write.toString().getBytes("UTF-8"));
				bufferStream.flush();      
				bufferStream.close(); 
				outputStream.close();
				System.out.print("�ļ�����д�������" + '\n');
				
				
				for (int i = 0; i < IDArrayList.size(); i++) {
					String IDStr = IDArrayList.get(i);
					ArrayList<String> infosArrayList = new ArrayList<String>();
					readFromTxtFileAccordIDAndDirection(mergeFileOutputPath, IDStr, infosArrayList);
					writeToTxtFile(classifyMergedTxtFileAccordID, infosArrayList);
				}
				System.out.print("ͬ��ID��Ϣд���ļ��н�����" + '\n');
			}
			else {
				System.out.print("ָ�����ļ������ڣ�" + '\n');
			}
			Thread.sleep(5000);
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 *	����ID,�����ͬID����Ϣ
	 * @param IDStr
	 * @param infosArrayList
	 * @param filePathStr
	 */
	public void readFromTxtFileAccordIDAndDirection(String filePathStr, String IDStr, ArrayList<String> infosArrayList){
		try {
			ArrayList<String> sameDirectionInfosArrayList = new ArrayList<String>();
			ArrayList<String> antiDirectionInfosArrayList = new ArrayList<String>();
			File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//���������������
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				while (str != null) {
					str = bufferedReader.readLine();
					if (str != null) {
						String[]tempArrayStr = str.split(",");
						if (tempArrayStr[0].equals(IDStr) && tempArrayStr[3].equals("1")) {
							sameDirectionInfosArrayList.add(str);
						}	
						if (tempArrayStr[0].equals(IDStr) && tempArrayStr[3].equals("-1")) {
							antiDirectionInfosArrayList.add(str);
						}
						System.out.print(str + "\n");
					}							
				}
				reader.close();
				ArrayList<String> uniqueSameDirectionInfosArraylist = new ArrayList<String>();
				ArrayList<String> uniqueAntiDirectionInfosArraylist = new ArrayList<String>();
				obtainUniqueInfosArraylist(sameDirectionInfosArrayList, uniqueSameDirectionInfosArraylist);
				obtainUniqueInfosArraylist(antiDirectionInfosArrayList, uniqueAntiDirectionInfosArraylist);
				//ͬ��д��
				for (int i = 0; i < uniqueSameDirectionInfosArraylist.size(); i++) {
					String tempStr = uniqueSameDirectionInfosArraylist.get(i);
					infosArrayList.add(tempStr);
				}
				//����д��
				for (int i = 0; i < uniqueAntiDirectionInfosArraylist.size(); i++) {
					String tempStr = uniqueAntiDirectionInfosArraylist.get(i);
					infosArrayList.add(tempStr);
				}
				System.out.print("������txt�ļ�:" + filePathStr + "\n");
			}
			else {
				System.out.print("ָ�����ļ������ڣ�" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}

	/**
	 * ����ʱ����Ϣ��ȥ��InfosArrayList����ͬ��Ϣ�����Ψһ��Ϣ������uniqueInfosArraylist
	 * @param InfosArrayList
	 * @param uniqueInfosArraylist
	 */
	public void obtainUniqueInfosArraylist(ArrayList<String> InfosArrayList, ArrayList<String> uniqueInfosArraylist){
		try {
			int dataCount = InfosArrayList.size();
			for (int i = 0; i < InfosArrayList.size(); i++) {
				String tempStr1 = InfosArrayList.get(i);
				String[]tempStrArray1 = tempStr1.split(",");
				String taxiIDStr1 = tempStrArray1[4];
				String timeStr1 = tempStrArray1[5];
				boolean isTheSame = false;
				for (int j = i + 1; j < InfosArrayList.size(); j++) {
					String tempStr2 = InfosArrayList.get(j);
					String[]tempStrArray2 = tempStr2.split(",");
					String taxiIDStr2 = tempStrArray2[4];
					String timeStr2 = tempStrArray2[5];
					if (taxiIDStr1.equals(taxiIDStr2) && timeStr1.equals(timeStr2)) {
						isTheSame = true;
						break;
					}					
				}
				if (!isTheSame) {
					uniqueInfosArraylist.add(tempStr1);
				}
				System.out.print(i + ":" + dataCount + "�޳���ͬ��¼" + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/**
	 * ·��ͨ��ʱ�䰴�����ա��ǹ����գ������ڼ��գ��ֱ�ͳ��
	 */
	public void classifyStatisticsAccordWhetherWorkday(){
		try {
			String filePathStr = PropertiesUtilJAR.getProperties("classifyMergedFile");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			readFromTxtFile(filePathStr, infosArrayList);
			ArrayList<String> workDayTravelTimeArrayList = new ArrayList<String>();//������ͨ��ʱ��
			ArrayList<String> nonWorkDayTravelTimeArrayList = new ArrayList<String>();//�ǹ�����ͨ��ʱ��
			
			for (int i = 0; i < infosArrayList.size(); i++) {
				System.out.print("�ļ���¼���������жϣ�" + i + "��" + infosArrayList.size() + '\n');
				String travelStr = infosArrayList.get(i);
				String[] travelArrayStr = travelStr.split(",");
				String travelTimeStr = travelArrayStr[5];
				String[]tempArrayStr = travelTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				boolean isWorkday = true;//�Ƿ�Ϊ������				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
				Date date = simpleDateFormat.parse(dateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    //��ĩ������ڳ���
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY
			    		|| dateStr.equals("2014-06-02") || dateStr.equals("2014-05-04")|| dateStr.equals("2014-05-02") || dateStr.equals("2014-05-01")
			    		|| dateStr.equals("2014-04-07")){
			    	isWorkday = false;
				}
			    if (isWorkday) {
					workDayTravelTimeArrayList.add(travelStr);
				}
			    else {
					nonWorkDayTravelTimeArrayList.add(travelStr);
				}
			}
			System.out.print("��ʼ���湤����ͨ��ʱ�䣺" + '\n');
			String workDayTravelTime = PropertiesUtilJAR.getProperties("workDayTravelTime");
			File workDayTravelTimeFile = new File(workDayTravelTime);
			if (workDayTravelTimeFile.exists()) {
				workDayTravelTimeFile.delete();
				writeTxtFileHeading(workDayTravelTime);
				writeToTxtFile(workDayTravelTime, workDayTravelTimeArrayList);
				System.out.print("���湤����ͨ��ʱ�������" + '\n');
			}
			else {
				writeTxtFileHeading(workDayTravelTime);
				writeToTxtFile(workDayTravelTime, workDayTravelTimeArrayList);
				System.out.print("���湤����ͨ��ʱ�������" + '\n');
			}
			System.out.print("��ʼ����ǹ�����ͨ��ʱ�䣺" + '\n');
			String nonWorkDayTravelTime = PropertiesUtilJAR.getProperties("nonWorkDayTravelTime");
			File nonWorkDayTravelTimeFile = new File(nonWorkDayTravelTime);
			if (nonWorkDayTravelTimeFile.exists()) {
				nonWorkDayTravelTimeFile.delete();
				writeTxtFileHeading(nonWorkDayTravelTime);
				writeToTxtFile(nonWorkDayTravelTime, nonWorkDayTravelTimeArrayList);
				System.out.print("����ǹ�����ͨ��ʱ�������" + '\n');
			}
			else {
				writeTxtFileHeading(nonWorkDayTravelTime);
				writeToTxtFile(nonWorkDayTravelTime, nonWorkDayTravelTimeArrayList);
				System.out.print("����ǹ�����ͨ��ʱ�������" + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public void writeTxtFileHeading(String filePathStr){
		try {
			System.out.print("��ʼд���ļ����⣺" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("�������ļ�����д�������" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
}
