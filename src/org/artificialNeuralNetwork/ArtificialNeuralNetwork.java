package org.artificialNeuralNetwork;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.Perceptron;

import utilityPackage.PubClass;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.ReturnLinkTravelTime;
import mapMatchingGPS.SerializeFunction;
import mapMatchingGPS.TaxiGPS;
import entity.PropertiesUtilJAR;

public class ArtificialNeuralNetwork {
	public static void main(String[] args){	
//		ANNInput();//��������������ļ�
//		ANNExport();//�������������ļ�
		test();
		
	}
	
	/**
	 * ���������������ļ���txt 
	 * */
	public static void ANNInput(){
		String fileFolderStr = PropertiesUtilJAR.getProperties("linkTravelTimeFileFolder");//�洢·��
		String fileNameStr1 = PropertiesUtilJAR.getProperties("linkTravelTimeFileName1");
		String fileNameStr2 = PropertiesUtilJAR.getProperties("linkTravelTimeFileName2");
		String linkTravelTimeFilePath1 = fileFolderStr + fileNameStr1;
		String linkTravelTimeFilePath2 = fileFolderStr + fileNameStr2;
		SerializeFunction serializeFunction = new SerializeFunction();
		String IDStr = "linkTravelTime";
		//���������������ļ�
		Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = 
			SerializeFunction.instance(IDStr, linkTravelTimeFilePath1).allLinkTravelTimeMap;
		String outPutPath = "C:\\ANNInput.txt";//д��Txt�ļ���
		int linkID = 4660;//Ŀ��·��ID
		String startTime = "2013-01-01 08:00:00";
		String endTime = "2013-01-01 09:00:00";
		writeInfosToText(allLinkTravelTimeMap, linkID, startTime, endTime, outPutPath);		
	}
	
	
	/**
	 * ����ļ�,txt:���������������ļ�
	 * 1.��������ļ��е�taxiID
	 * 2.���taxiID��Ŀ��·�Ρ�Ŀ��ʱ����ڵ�ͨ��ʱ��
	 * 3.����Ϣд��TXT�ļ�
	 */
	public static void ANNExport(){
		String fileFolderStr = PropertiesUtilJAR.getProperties("linkTravelTimeFileFolder");//�洢·��
		String targetStartTime = "2013-01-01 08:00:00";//Ŀ��ʱ���08:00:00-09:00:00�ڵ���Ϣ
		String targetEndTime = "2013-01-01 09:00:00";
		int timeInterval = 24 * 3600;//һ���ʱ����
		try {
			String outPutPath = "C:\\tempANNOutput.txt";//��������������ݣ����txt�ļ���
			String inputPath = "C:\\ANNInput.txt";//���������������ݣ�Txt�ļ���
			ArrayList<String> taxiIDArrayList = new ArrayList<String>();//���������������е�taxiID
			readInfosFromText(inputPath, taxiIDArrayList);
			String taxiID = "";
			int linkID = 4660;//Ŀ��·��ID	
			String startTimeStr = "2013-01-01 06:00:00";//�ļ���ʼʱ��
			String endTimeStr = "2013-01-01 12:00:00";//�ļ�����ʱ��
			
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String roadDescription = "taxiID" + "," + "linkID" + "," + "startTravelTimeStr" + "," + "travelTime" + "," + "\r\n";
			write.append(roadDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));			
			String linkTravelTimeFilePath = fileFolderStr + "allLinkTravelTime20130101060000-20130101120000.bin";		
			File f = new File(linkTravelTimeFilePath);
			while (f.exists()) {
				SerializeFunction serializeFunction = new SerializeFunction();				
				String IDStr = "linkTravelTime";
				System.out.print("���ļ���" + linkTravelTimeFilePath + '\n');
				Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = 
					SerializeFunction.instance(IDStr, linkTravelTimeFilePath).allLinkTravelTimeMap;
				serializeFunction.readSeriseLinkTravelTime(linkTravelTimeFilePath);
				java.util.Set keySet = allLinkTravelTimeMap.entrySet();
				Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		Integer key = (Integer)mapEntry.getKey();
	        		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = allLinkTravelTimeMap.get(key);
	        		for (int j = 0; j < linkTravelTimeArrayList.size(); j++) {
	        			String travelInfoStr = "";
	        			ReturnLinkTravelTime tempReturnLinkTravelTime = linkTravelTimeArrayList.get(j);
	        			int tempLinkID = tempReturnLinkTravelTime.getLinkID();
	        			String startTravelTime = tempReturnLinkTravelTime.getStartTravelTime();
	        			String curTaxiID = tempReturnLinkTravelTime.getTaxiID();
	        			for (int i = 0; i < taxiIDArrayList.size(); i++) {
							taxiID = taxiIDArrayList.get(i);
							//���⳵������Ŀ��·��
							if (taxiID.equals(curTaxiID) && tempLinkID == linkID) {
								//Ŀ��ʱ���
								if (isDateBetweenTwoDate(startTravelTime, targetStartTime, targetEndTime)) {       					        			
				        			double travelTime = tempReturnLinkTravelTime.getTravelTime();
				        			travelInfoStr = taxiID + "," + linkID + "," + startTravelTime + "," + travelTime + "\r\n";
				        			write = new StringBuffer();			
				    				write.append(travelInfoStr);
				    				bufferStream.write(write.toString().getBytes("UTF-8"));
								}
							}
							else {
								continue;
							}						
						}
					}
	        	}
	        	String fileTimeArray[] = new String[2];
	        	obtainFileTimeName(startTimeStr, endTimeStr, fileTimeArray);
	        	String[] startTimeArray = new String[1];
	        	String[] endTimeArray = new String[1];
	        	PubClass.parameterProcAboutTime(fileTimeArray[0], startTimeArray);
	        	PubClass.parameterProcAboutTime(fileTimeArray[1], endTimeArray);
	        	startTimeStr = startTimeArray[0];
	        	endTimeStr = endTimeArray[0];
	        	String[] targetStartTimeArray = new String[1];
	        	String[] targetEndTimeArray = new String[1];
	        	PubClass.obtainEndTimeAccordStartTime(targetStartTime, timeInterval, targetStartTimeArray);
	        	PubClass.obtainEndTimeAccordStartTime(targetEndTime, timeInterval, targetEndTimeArray);
	        	targetStartTime = targetStartTimeArray[0];
	        	targetEndTime = targetEndTimeArray[0];
	        	fileFolderStr = PropertiesUtilJAR.getProperties("linkTravelTimeFileFolder");//�洢·��
	        	linkTravelTimeFilePath = fileFolderStr + "allLinkTravelTime" + fileTimeArray[0] + "-" + fileTimeArray[1] + ".bin";		
				f = new File(linkTravelTimeFilePath);	        	
			}
        	bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
			System.out.print("д�����");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**�ļ�����Ӧ�Ŀ�ʼ������ʱ�䣺����ļ���
	 * ���ݵ���Ŀ�ʼ����ʱ������һ��ĵ��ļ�����Ӧ�Ŀ�ʼ������ʱ��
	 * startTimeStr�����쿪ʼʱ�䣬ʱ���ʽΪ��2013-01-01 08:00:00
	 * endTimeStr���������ʱ�䣬ʱ���ʽΪ��2013-01-01 08:00:00 
	 * fileTimeArray���ļ�����Ӧ�Ŀ�ʼ������ʱ��,ʱ���ʽΪ��20130101060000
	 * 
	 * */
	public static void obtainFileTimeName(String startTimeStr, String endTimeStr, String fileTimeArray[]){
		try {
			int timeInterval = 24 * 3600;
			String[] nextStartTimeArray = new String[1];
			String[] nextEndTimeArray = new String[1];
			PubClass.obtainEndTimeAccordStartTime(startTimeStr, timeInterval, nextStartTimeArray);
			PubClass.obtainEndTimeAccordStartTime(endTimeStr, timeInterval, nextEndTimeArray);
			String nextStartYearStr = nextStartTimeArray[0].substring(0, 4);
			String nextStartMonthStr = nextStartTimeArray[0].substring(5, 7);
			String nextStartDayStr = nextStartTimeArray[0].substring(8, 10);
			String nextStartHourStr = nextStartTimeArray[0].substring(11, 13);
			String nextStartMinStr = nextStartTimeArray[0].substring(14, 16);
			String nextStartSecStr = nextStartTimeArray[0].substring(17, 19);		
			String nextStartTimeStr = nextStartYearStr + nextStartMonthStr + nextStartDayStr 
				+ nextStartHourStr + nextStartMinStr + nextStartSecStr;
			String nextEndYearStr = nextEndTimeArray[0].substring(0, 4);
			String nextEndMonthStr = nextEndTimeArray[0].substring(5, 7);
			String nextEndDayStr = nextEndTimeArray[0].substring(8, 10);
			String nextEndHourStr = nextEndTimeArray[0].substring(11, 13);
			String nextEndMinStr = nextEndTimeArray[0].substring(14, 16);
			String nextEndSecStr = nextEndTimeArray[0].substring(17, 19);		
			String nextEndTimeStr = nextEndYearStr + nextEndMonthStr + nextEndDayStr + nextEndHourStr + nextEndMinStr + nextEndSecStr;
			fileTimeArray[0] = nextStartTimeStr;
			fileTimeArray[1] = nextEndTimeStr;
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
			e.printStackTrace();
		}		
	}
	
	/**��txt�ļ��ж�ȡ��Ϣ
	 * ���taxiID��Ϣ
	 * */
	public static void readInfosFromText(String path, ArrayList<String> taxiIDArrayList){
		System.out.print("��ʼ��txt�ļ�"+"\n");
		try {
			File file = new File(path);
			String encoding = "UTF-8";//���������������
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
			BufferedReader bufferedReader = new BufferedReader(reader);
			String str = bufferedReader.readLine();
			System.out.print(str+"\n");
			while (str != null) {
				str = bufferedReader.readLine();
				if (str != null) {
					String[]tempArrayStr = str.split(",");
					taxiIDArrayList.add(tempArrayStr[0]);
					System.out.print(str+"\n");
				}							
			}
			reader.close();
			System.out.print("������txt�ļ�"+"\n");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO: handle exception
			e.getMessage();
			e.printStackTrace();
		}		
	}
	
	/**�ж�currentDateStr�Ƿ�λ��startDateStr��endDateStr֮��
	 * ���ǣ�����true
	 * ���򣬷���false
	 * ���ڸ�ʽ�磺2013-01-01 08:00:00
	 * currentDateStr����ǰ�����ַ���
	 * startDateStr����ʼ�����ַ���
	 * endDateStr�����������ַ���
	 */
	public static boolean isDateBetweenTwoDate(String currentDateStr, String startDateStr, String endDateStr){			
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDate = df.parse(startDateStr);
            Date endDate = df.parse(endDateStr);
            Date currentDate = df.parse(currentDateStr);
            long startDateTime = startDate.getTime();
            long endDateTime = endDate.getTime();
            long currentDateTime = currentDate.getTime();
            if (startDateTime > endDateTime) {
				System.out.print("��ʼ������С�ڽ������ڣ�" + '\n');
				return false;
			}
            else {
            	if (currentDateTime > startDateTime && currentDateTime < endDateTime) {
                    return true;
                } else {
                	return false;
                }
			}          
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
	
	/*��Ŀ��·�Ρ�ĳʱ��γ��⳵ͨ�������Ϣд�뵽text�ļ���*/
	public static void writeInfosToText(Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap, int linkID,
			String startTime, String endTime, String outPutPath){
		try {			
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String roadDescription = "taxiID" + "," + "linkID" + "," + "startTravelTimeStr" + "," + "travelTime" + "," + "\r\n";
			write.append(roadDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));						
			java.util.Set keySet = allLinkTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		Integer key = (Integer)mapEntry.getKey();
        		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = allLinkTravelTimeMap.get(key);
        		for (int j = 0; j < linkTravelTimeArrayList.size(); j++) {
        			String travelInfoStr = "";
        			ReturnLinkTravelTime tempReturnLinkTravelTime = linkTravelTimeArrayList.get(j);
        			int tempLinkID = tempReturnLinkTravelTime.getLinkID();
        			String startTravelTime = tempReturnLinkTravelTime.getStartTravelTime();
        			if (tempLinkID == linkID && isDateBetweenTwoDate(startTravelTime, startTime, endTime)) {
        				String taxiID = tempReturnLinkTravelTime.getTaxiID();	        			
	        			double travelTime = tempReturnLinkTravelTime.getTravelTime();
	        			travelInfoStr = taxiID + "," + linkID + "," + startTravelTime + "," + travelTime + "\r\n";
	        			write = new StringBuffer();				
	    				write.append(travelInfoStr);
	    				bufferStream.write(write.toString().getBytes("UTF-8"));
					}
				}
        	}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("д�����");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	public static void test(){
		// create new perceptron network
		NeuralNetwork neuralNetwork = new Perceptron(2, 1);
		// create training set
		DataSet trainingSet = new DataSet(2, 1);
		// add training data to training set (logical OR function)
		trainingSet. addRow (new DataSetRow(new double[]{0, 0},
		new double[]{0}));
		trainingSet. addRow (new DataSetRow (new double[]{0, 1},
		new double[]{1}));
		trainingSet. addRow (new DataSetRow (new double[]{1, 0},
		new double[]{1}));
		trainingSet. addRow (new DataSetRow (new double[]{1, 1},
		new double[]{1}));
		// learn the training set
		neuralNetwork.learn(trainingSet);
		// save the trained network into file
		neuralNetwork.save("or_perceptron.nnet");
		
	}
	
	
	
	
	
	
	
	
	
	
}
