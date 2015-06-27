package org.artificialNeuralNetwork;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import org.kohsuke.rngom.digested.Main;
import org.neuroph.core.data.DataSetRow;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import entity.PropertiesUtilJAR;

/***
 * 数据归一化
 * 1.将数据离散化为向量形式
 * 2.归一化
 * */
public class MainFunctionNormalize {
	
	public static void main(String[] args) {
//		int[] inputVector = new int[4];
//		(new MainFunctionNormalize()).obtainTimeInputVectorAccordTime(1,5,1,inputVector);
		(new MainFunctionNormalize()).discretizeDayTime("2014-06-02 00:00:00", 5, 211, 0);
		(new MainFunctionNormalize()).readStatisticTravelTimeAndDiscretizeVector();
		(new MainFunctionNormalize()).normalizeAndWriteToTxt();
		System.out.print("done!");
	}

	/**
	 * 1.读取统计通行时间数据
	 * 2.离散化为向量
	 * 3.向量写入txt文件
	 * 4.向量归一化
	 */
	public void readStatisticTravelTimeAndDiscretizeVector(){
		//以路段ID为索引，存储通行时间信息
		Map<Integer, ArrayList<String[]>> infosMap = new HashMap<Integer, ArrayList<String[]>>();
		readStatisticTravelTime(infosMap);
//		discretizeVectorThirtyMinitesAndWriteTotxt(infosMap);//30分钟时间间隔划分
		discretizeVectorFiveMinitesAndWriteTotxt(infosMap);//5分钟时间间隔划分
	 }
	
	/**
	 * 读取统计通行时间文件,并将信息存入infosArrayList
	 * 去掉出租车ID信息
	 * @param infosMap 以路段ID为索引，存储通行时间信息
	 */
	public void readStatisticTravelTime(Map<Integer, ArrayList<String[]>> infosMap){
		String path = PropertiesUtilJAR.getProperties("nonWorkDayTravelTime");
		System.out.print("开始读txt文件：" + "\n");
		try {
			File file = new File(path);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				//如果文件首行为标题，则继续读下一行,存储信息
				//否则，存储信息，继续读下一行
				ArrayList<String[]> infosArrayList = new ArrayList<String[]>();
				String markStr = str.split(",")[0];
				if (markStr.equals("linkID")) {
					str = bufferedReader.readLine();
					int linkID = -1;//当前行记录的路段ID
					int preLinkID = -1;//上一行记录的路段ID
					while (str != null) {
						preLinkID = linkID;
						String[]tempArrayStr = str.split(",");
						linkID = Integer.parseInt(tempArrayStr[0]);
						if (linkID != preLinkID) {
							infosArrayList = new ArrayList<String[]>();
							infosMap.put(linkID, infosArrayList);
						}						
						String[]arrayStr = new String[4];
						arrayStr[0] = tempArrayStr[0];
						arrayStr[1] = tempArrayStr[1];
						arrayStr[2] = tempArrayStr[3];
						arrayStr[3] = tempArrayStr[4];
						infosArrayList.add(arrayStr);
						System.out.print(str + "\n");
						str = bufferedReader.readLine();
					}
					reader.close();
					System.out.print("结束读txt文件"+"\n");
				}
				else {
					int linkID = -1;//当前行记录的路段ID
					int preLinkID = -1;//上一行记录的路段ID
					while (str != null) {	
						if (linkID == 8) {
							System.out.print("\n");
						}
						if (linkID != preLinkID) {
							System.out.print("\n");
						}
						preLinkID = linkID;
						String[]tempArrayStr = str.split(",");
						linkID = Integer.parseInt(tempArrayStr[0]);
						if (linkID != preLinkID) {
							infosArrayList = new ArrayList<String[]>();
							infosMap.put(linkID, infosArrayList);
						}
						String[]arrayStr = new String[4];
						arrayStr[0] = tempArrayStr[0];
						arrayStr[1] = tempArrayStr[1];
						arrayStr[2] = tempArrayStr[3];
						arrayStr[3] = tempArrayStr[4];
						infosArrayList.add(arrayStr);
						System.out.print(str + "\n");
						str = bufferedReader.readLine();
					}
					reader.close();
					System.out.print("结束读txt文件" + "\n");
				}				
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 数据离散化并写入txt文件，离散化的时间间隔为30min
	 * 1.数据离散化为向量形式
	 * 2.写入txt文件
	 * @param infosMap 以路段ID为索引，存储通行时间信息
	 */
	public void discretizeVectorThirtyMinitesAndWriteTotxt(Map<Integer, ArrayList<String[]>> infosMap){
		String outPutPath = PropertiesUtilJAR.getProperties("discretizeVectorPath");//数据离散化为向量形式后保存的文件名
		try {	
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "direction" + ","  + "hour" + "," + "firstHalfH" + "," + "workday" + "," + "travelTime" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			//数据离散化为向量形式				
			java.util.Set keySet = infosMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<String[]> infosArrayList = infosMap.get(key);
				for (int i = 0; i < infosArrayList.size(); i++) {
					int[] inputVector = new int[6];//输入向量有6个分量
					String[]arrayStr = new String[4];
					arrayStr = infosArrayList.get(i);
					int linkID = Integer.valueOf(arrayStr[0]);
					inputVector[0] = linkID;
					inputVector[1] = Integer.valueOf(arrayStr[1]);
					double tempTravelTime = Double.valueOf(arrayStr[3]);
					int travelTime = (int)tempTravelTime;
					inputVector[5] = travelTime;
					String timeStr = arrayStr[2];
					String[]tempArrayStr = timeStr.split(" ");
					String dateStr = tempArrayStr[0];
					boolean isWorkday = true;//是否为工作日
					String[]hourMinSec = tempArrayStr[1].split(":");
					String hourStr = hourMinSec[0];//时
					String minStr = hourMinSec[1];//分				
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
					Date date = simpleDateFormat.parse(dateStr); 
				    Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				    		|| dateStr.equals("2014-06-02")){
				    	isWorkday = false;
					}
				    if (isWorkday) {
						inputVector[4] = 1;
					}
				    else {
				    	inputVector[4] = 0;
					}			    
					int hourInt = Integer.valueOf(hourStr);
					int minInt = Integer.valueOf(minStr);
					switch (hourInt) {
					case 0:
						if (minInt <= 30) {
							inputVector[2] = 0;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 0;
							inputVector[3] = 0;
						}
						break;
					case 1:
						if (minInt <= 30) {
							inputVector[2] = 1;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 1;
							inputVector[3] = 0;
						}
						break;					
					case 2:
						if (minInt <= 30) {
							inputVector[2] = 2;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 2;
							inputVector[3] = 0;
						}
						break;
					case 3:
						if (minInt <= 30) {
							inputVector[2] = 3;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 3;
							inputVector[3] = 0;
						}
						break;
					case 4:
						if (minInt <= 30) {
							inputVector[2] = 4;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 4;
							inputVector[3] = 0;
						}
						break;
					case 5:
						if (minInt <= 30) {
							inputVector[2] = 5;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 5;
							inputVector[3] = 0;
						}
						break;
					case 6:
						if (minInt <= 30) {
							inputVector[2] = 6;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 6;
							inputVector[3] = 0;
						}
						break;
					case 7:
						if (minInt <= 30) {
							inputVector[2] = 7;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 7;
							inputVector[3] = 0;
						}
						break;
					case 8:
						if (minInt <= 30) {
							inputVector[2] = 8;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 8;
							inputVector[3] = 0;
						}
						break;
					case 9:
						if (minInt <= 30) {
							inputVector[2] = 9;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 9;
							inputVector[3] = 0;
						}
						break;
					case 10:
						if (minInt <= 30) {
							inputVector[2] = 10;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 10;
							inputVector[3] = 0;
						}
						break;
					case 11:
						if (minInt <= 30) {
							inputVector[2] = 11;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 11;
							inputVector[3] = 0;
						}
						break;
					case 12:
						if (minInt <= 30) {
							inputVector[2] = 12;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 12;
							inputVector[3] = 0;
						}
						break;
					case 13:
						if (minInt <= 30) {
							inputVector[2] = 13;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 13;
							inputVector[3] = 0;
						}
						break;
					case 14:
						if (minInt <= 30) {
							inputVector[2] = 14;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 14;
							inputVector[3] = 0;
						}
						break;
					case 15:
						if (minInt <= 30) {
							inputVector[2] = 15;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 15;
							inputVector[3] = 0;
						}
						break;
					case 16:
						if (minInt <= 30) {
							inputVector[2] = 16;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 16;
							inputVector[3] = 0;
						}
						break;
					case 17:
						if (minInt <= 30) {
							inputVector[2] = 17;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 17;
							inputVector[3] = 0;
						}
						break;
					case 18:
						if (minInt <= 30) {
							inputVector[2] = 18;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 18;
							inputVector[3] = 0;
						}
						break;
					case 19:
						if (minInt <= 30) {
							inputVector[2] = 19;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 19;
							inputVector[3] = 0;
						}
						break;
					case 20:
						if (minInt <= 30) {
							inputVector[2] = 20;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 20;
							inputVector[3] = 0;
						}
						break;
					case 21:
						if (minInt <= 30) {
							inputVector[2] = 21;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 21;
							inputVector[3] = 0;
						}
						break;
					case 22:
						if (minInt <= 30) {
							inputVector[2] = 22;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 22;
							inputVector[3] = 0;
						}
						break;
					case 23:
						if (minInt <= 30) {
							inputVector[2] = 23;
							inputVector[3] = 1;
						}
						else {
							inputVector[2] = 23;
							inputVector[3] = 0;
						}
						break;
					default:
						break;
					}
					String vectorStr = inputVector[0] + "," + inputVector[1] + "," + inputVector[2] + "," + inputVector[3] + "," + inputVector[4] + "," + inputVector[5] + "\r\n"; 
					System.out.print(vectorStr);
					write = new StringBuffer();		
					write.append(vectorStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));				
				}
        	}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 数据离散化并写入txt文件，离散化的时间间隔为5min
	 * 1.数据离散化为向量形式
	 * 2.写入txt文件
	 * inputVector[] 分别表示为路段ID、出租车通行方向性、时(0-24)、分（5分钟划分,1-12）、是否工作日（是工作日则用1表示）、通行时间
	 * @param infosMap 以路段ID为索引，存储通行时间信息
	 */
	public void discretizeVectorFiveMinitesAndWriteTotxt(Map<Integer, ArrayList<String[]>> infosMap){
		String outPutPath = PropertiesUtilJAR.getProperties("discretizeVectorPath");//数据离散化为向量形式后保存的文件名
		try {	
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "direction" + ","  + "hour" + "," + "whichFiveMin" + ","
				+ "workday" + "," + "travelTime" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			//数据离散化为向量形式				
			java.util.Set keySet = infosMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<String[]> infosArrayList = infosMap.get(key);
				for (int i = 0; i < infosArrayList.size(); i++) {
					int[] inputVector = new int[6];//输入向量有6个分量
					String[]arrayStr = new String[4];
					arrayStr = infosArrayList.get(i);
					int linkID = Integer.valueOf(arrayStr[0]);
					inputVector[0] = linkID;
					inputVector[1] = Integer.valueOf(arrayStr[1]);
					double tempTravelTime = Double.valueOf(arrayStr[3]);
					int travelTime = (int)tempTravelTime;
					inputVector[5] = travelTime;
					String timeStr = arrayStr[2];
					String[]tempArrayStr = timeStr.split(" ");
					String dateStr = tempArrayStr[0];
					boolean isWorkday = true;//是否为工作日
					String[]hourMinSec = tempArrayStr[1].split(":");
					String hourStr = hourMinSec[0];//时
					String minStr = hourMinSec[1];//分				
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
					Date date = simpleDateFormat.parse(dateStr); 
				    Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				    		|| dateStr.equals("2014-06-02")){
				    	isWorkday = false;
					}
				    if (isWorkday) {
						inputVector[4] = 1;
					}
				    else {
				    	inputVector[4] = 0;
					}			    
					int hourInt = Integer.valueOf(hourStr);
					int minInt = Integer.valueOf(minStr);
					switch (hourInt) {
					case 0:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 1:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;					
					case 2:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 3:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 4:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 5:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 6:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 7:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 8:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 9:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 10:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 11:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 12:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 13:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 14:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 15:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 16:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 17:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 18:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 19:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 20:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 21:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 22:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					case 23:
						obtainTimeInputVectorAccordTime(hourInt, minInt, inputVector);
						break;
					default:
						break;
					}
					String vectorStr = inputVector[0] + "," + inputVector[1] + "," + inputVector[2] + "," + inputVector[3] + "," + inputVector[4] + "," + inputVector[5] + "\r\n"; 
					System.out.print(vectorStr);
					write = new StringBuffer();		
					write.append(vectorStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));				
				}
        	}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}

	/**
	 * 将00:00:00 到23:59:59之间时间进行离散化
	 * @param timeStr 离散化时间 如：2014-06-02 00:00:00
	 * @param timeInterval 离散化的时间间隔
	 * @param maxVal 归一化时的最大值
	 * @param minVal 归一化时的最小值
	 */
	public void discretizeDayTime(String timeStr, int timeInterval, double maxVal, double minVal){
		try {
			String outPutPath = "C:\\discretizeAndNormalizeDayTime.txt";
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "hour" + "," + "whichMin" + "," + "workday" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			String []predictEndTimeArray = new String[1];
            PubClass.obtainEndTimeAccordStartTime(timeStr, 3600 * 24, predictEndTimeArray);
            String predictEndTimeStr = predictEndTimeArray[0];
            String []endTimeArray = new String[1];
            PubClass.obtainEndTimeAccordStartTime(timeStr, 5 * 60, endTimeArray);//时间间隔
            String curTimeStr = endTimeArray[0];  
            while (!curTimeStr.equals(predictEndTimeStr)) {
            	double []inputVector = new double[3]; 
            	String[]tempArrayStr = curTimeStr.split(" ");
    			String dateStr = tempArrayStr[0];
    			boolean isWorkday = true;//是否为工作日
    			String[]hourMinSec = tempArrayStr[1].split(":");
    			String hourStr = hourMinSec[0];//时
    			String minStr = hourMinSec[1];//分				
    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
    			Date date = simpleDateFormat.parse(dateStr); 
    		    Calendar cal = Calendar.getInstance();
    		    cal.setTime(date);
    		    if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY || dateStr.equals("2014-06-02")){
    		    	isWorkday = false;
    			}
    		    if (isWorkday) {
    				inputVector[2] = 1;
    			}
    		    else {
    		    	inputVector[2] = 0;
    			}			    
    			int hourInt = Integer.valueOf(hourStr);
    			int minInt = Integer.valueOf(minStr);
    			int []tempVector = new int[4]; 
    			obtainTimeInputVectorAccordTime(hourInt, minInt, timeInterval, tempVector);
    			inputVector[0] = tempVector[2];
    			inputVector[1] = tempVector[3];
    	        endTimeArray = new String[1];
                PubClass.obtainEndTimeAccordStartTime(curTimeStr, 5 * 60, endTimeArray);//时间间隔10min
                curTimeStr = endTimeArray[0];
                
                double normalizeHour = (double)(inputVector[0] - minVal)/(maxVal - minVal);
				String normalizeHourStr = String.format("%.6f", normalizeHour);
				double normalizeMinute = (double)(inputVector[1] - minVal)/(maxVal - minVal);
				String normalizeMinuteStr = String.format("%.6f", normalizeMinute);
				double normalizeWorkday = (double)(inputVector[2] - minVal)/(maxVal - minVal);
				String normalizeWorkdayStr = String.format("%.6f", normalizeWorkday);               
                String vectorStr = curTimeStr + "," + normalizeHourStr + "," + normalizeMinuteStr + "," + normalizeWorkdayStr + "\r\n"; 
				System.out.print(vectorStr);
				write = new StringBuffer();		
				write.append(vectorStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));	
			}
            bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/**
	 * 获得时间输入向量
	 * 根据传入的时、分获得时间的输入向量，以5分钟为时间间隔，时间区间为左开右闭(0,5]...
	 * @param hourInt
	 * @param minInt
	 * @param inputVector
	 */
	public void obtainTimeInputVectorAccordTime(int hourInt, int minInt, int[] inputVector){
		try {
			if (minInt > 0 && minInt <= 5) {
				inputVector[2] = hourInt;
				inputVector[3] = 1;
			}
			else if (minInt > 5 && minInt <= 10) {
				inputVector[2] = hourInt;
				inputVector[3] = 2;
			}
			else if (minInt > 10 && minInt <= 15) {
				inputVector[2] = hourInt;
				inputVector[3] = 3;
			}
			else if (minInt > 15 && minInt <= 20) {
				inputVector[2] = hourInt;
				inputVector[3] = 4;
			}
			else if (minInt > 20 && minInt <= 25) {
				inputVector[2] = hourInt;
				inputVector[3] = 5;
			}
			else if (minInt > 25 && minInt <= 30) {
				inputVector[2] = hourInt;
				inputVector[3] = 6;
			}
			else if (minInt > 30 && minInt <= 35) {
				inputVector[2] = hourInt;
				inputVector[3] = 7;
			}
			else if (minInt > 35 && minInt <= 40) {
				inputVector[2] = hourInt;
				inputVector[3] = 8;
			}
			else if (minInt > 40 && minInt <= 45) {
				inputVector[2] = hourInt;
				inputVector[3] = 9;
			}
			else if (minInt > 45 && minInt <= 50) {
				inputVector[2] = hourInt;
				inputVector[3] = 10;
			}
			else if (minInt > 50 && minInt <= 55) {
				inputVector[2] = hourInt;
				inputVector[3] = 11;
			}
			else if (minInt > 55 && minInt <= 60) {
				inputVector[2] = hourInt;
				inputVector[3] = 12;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 获得时间输入向量
	 * 根据传入的时、分获得时间的输入向量，以timeInterval分钟为时间间隔，时间区间为左开右闭如(0,timeInterval]...
	 * @param hourInt
	 * @param minInt
	 * @param timeInterval 单位为分钟
	 * @param inputVector
	 */
	public void obtainTimeInputVectorAccordTime(int hourInt, int minInt, int timeInterval, int[] inputVector){
		try {
			int intervalCount = 60/timeInterval;
			while (true) {
				if (minInt > timeInterval * (intervalCount - 1)&& minInt <= timeInterval * intervalCount) {
					inputVector[2] = hourInt;
					inputVector[3] = intervalCount;	
					System.out.print("");
					break;
				}
				else {
					intervalCount = intervalCount - 1;
				}				
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/*数据归一化并写入txt文件
	 * 1.读取离散化向量数据，所有数据读入数组中;
	 * 2.检索所有数据中的最大、最小值;
	 * 3.归一化计算x’=(x-xmin)/(xmax - xmin);
	 * */
	public void normalizeAndWriteToTxt(){
		String readDiscretizePath = PropertiesUtilJAR.getProperties("discretizeVectorPath");//要读取的向量保存文件
		String writeNormalizePath = PropertiesUtilJAR.getProperties("normalizePath");//要写入的归一化文件名
		//以路段ID为索引，存储离散化向量
		Map<Integer, ArrayList<ANNInputVector>> ANNInputVectorMap = new HashMap<Integer, ArrayList<ANNInputVector>>();
		readDiscretizeTxt(ANNInputVectorMap, readDiscretizePath);
		try {
			File file = new File(writeNormalizePath);
			if (file.exists()) {
				file.delete();
				writeNormalizeInfosToTxt(ANNInputVectorMap, writeNormalizePath);	
			}
			else {
				writeNormalizeInfosToTxt(ANNInputVectorMap, writeNormalizePath);
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * 读取discretize文件
	 * @param ANNDiscretizeVectorMap 以路段ID为索引，存储离散化向量
	 * @param readDiscretizePath 要读取的向量保存文件路径
	 */
	public void readDiscretizeTxt(Map<Integer, ArrayList<ANNInputVector>> ANNDiscretizeVectorMap, String readDiscretizePath){
		ArrayList<ANNInputVector> discretizeArrayList = new ArrayList<ANNInputVector>();//保存处理的向量离散化数据
		try {
			System.out.print("开始读txt文件:" + "\n");
			File file = new File(readDiscretizePath);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();//文件头
				System.out.print(str + "\n");
				int linkID = -1;//当前行记录的路段ID
				int preLinkID = -1;//上一行记录的路段ID
				int direction = 0;//出租车行驶方向与路段方向关系
				int hour = 0;
				int preHalfHour = 0;
				int workDay = 0;
				int travelTime = 0;
				while (str != null) {
					System.out.print("读取记录：" + str + "\n");
					preLinkID = linkID;
					str = bufferedReader.readLine();
					if (str != null) {
						String[]tempArrayStr = str.split(",");
						ANNInputVector inputVector = new ANNInputVector();
						linkID = Integer.parseInt(tempArrayStr[0]);	
						direction = Integer.parseInt(tempArrayStr[1]);
						hour = Integer.parseInt(tempArrayStr[2]);
						preHalfHour = Integer.parseInt(tempArrayStr[3]);
						workDay = Integer.parseInt(tempArrayStr[4]);
						travelTime = Integer.parseInt(tempArrayStr[5]);
						inputVector.setLinkID(linkID);
						inputVector.setDirection(direction);
						inputVector.setHour(hour);
						inputVector.setPreHalfHour(preHalfHour);
						inputVector.setWorkDay(workDay);
						inputVector.setTravelTime(travelTime);
						discretizeArrayList.add(inputVector);
						if (linkID != preLinkID) {							
							discretizeArrayList = new ArrayList<ANNInputVector>();
							ANNDiscretizeVectorMap.put(linkID, discretizeArrayList);							
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public void testInsertInfosToTxt(){
		String filePathStr = "C:\\testInsertInfosToTxt.txt";
		String oldStr = "txtHeading";
		String replaceStr = "txtHeading,txtHeading,txtHeading,txtHeading";
		String temp = "";
		try {
			//归一化并写入txt文件
			
			
			File file = new File(filePathStr);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // 保存该行前面的内容
            for (int j = 1; (temp = br.readLine())!= null && !temp.equals(oldStr); j++) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
                System.out.print(System.getProperty("line.separator"));
            }

            // 将内容插入
            buf = buf.append(replaceStr);

            // 保存该行后面的内容
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }

            br.close();
            
            FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
//            PrintWriter pw = new PrintWriter(fos);
//            pw.write(buf.toString().toCharArray());
//            pw.flush();
//            pw.close();
            
			write.append(buf);
			bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	
	/**
	 * 每个路段分方向分别进行归一化
	 * 1.取得每个路段的离散化信息
	 * 2.检索每个路段每个方向数据中的最大、最小值;
	 * 3.归一化计算x’=(x-xmin)/(xmax - xmin),路段ID，方向direction不参与归一化计算;
	 * 4.路段ID,方向direction写入txt文件中
	 * @param ANNDiscretizeVectorMap
	 * @param writeNormalizePath 归一化文件路径
	 */
	public void writeNormalizeInfosToTxt(Map<Integer, ArrayList<ANNInputVector>> ANNDiscretizeVectorMap, String writeNormalizePath){
		try {
			//归一化并写入txt文件
			FileOutputStream outputStream = new FileOutputStream(new String(writeNormalizePath),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String metaDataDescription = "metaDataDescription:" + "\r\n";
			String allLinkID = "allLinkID:" + "\r\n";
			String allMetaData = "allMetaData:" + "\r\n";	
			String endMetaDataDescription = "endMetaDataDescription" + "\r\n";
			String txtDescription = "txtDescription:" + "\r\n";
			String txtHeading1 = "txtHeading:"+ "\r\n" +	"linkID" + "," + "direction" + "," + "maxVal" + "," + "minVal" + "," + "dataCount" + "\r\n";
			String txtHeading2 = "normalizeHour" + "," + "normalizeFirstHalfH" + "," + "normalizeWorkday" + "," + "normalizeTravelTime" + "\r\n";
			write.append(metaDataDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			String tempAllLinkIDStr = "";
			//获得文件元数据，并写入txt文件中
			ArrayList<NormalizeMetaData> allMetaDataArrayList = new ArrayList<NormalizeMetaData>();//文件元数据
			java.util.Set keySet1 = ANNDiscretizeVectorMap.entrySet();
			Iterator iterator1 = (Iterator) keySet1.iterator();
	    	while (iterator1.hasNext()) {
	    		Map.Entry mapEntry = (Map.Entry) iterator1.next();
	    		int key = (Integer)mapEntry.getKey();
	    		if (tempAllLinkIDStr.equals("")) {
	    			tempAllLinkIDStr = tempAllLinkIDStr + String.valueOf(key);
				}
	    		else {
	    			tempAllLinkIDStr = tempAllLinkIDStr + "," + String.valueOf(key);
				}
	    		
	    		ArrayList<ANNInputVector> discretizeVectorArrayList = ANNDiscretizeVectorMap.get(key);
	    		ArrayList<ANNInputVector> linkSameDirectionInputArrayList = new ArrayList<ANNInputVector>();//沿路段同方向行程信息
				ArrayList<ANNInputVector> linkAntiDirectionInputArrayList = new ArrayList<ANNInputVector>();
	    		double []sameDireMaxMin = new double[2];
	    		double []antiDireMaxMin = new double[2];
	    		obtainMaxMinValFromDiscretizeInfos(discretizeVectorArrayList, linkSameDirectionInputArrayList, 
	    				linkAntiDirectionInputArrayList, sameDireMaxMin, antiDireMaxMin);
	    		NormalizeMetaData normalizeMetaData = new NormalizeMetaData();
	    		normalizeMetaData.setLinkID(key);
	    		normalizeMetaData.setLinkSameDirectionDataCount(linkSameDirectionInputArrayList.size());
	    		normalizeMetaData.setLinkSameDirectionMax(sameDireMaxMin[0]);
	    		normalizeMetaData.setLinkSameDirectionMin(sameDireMaxMin[1]);
	    		normalizeMetaData.setLinkAntiDirectionDataCount(linkAntiDirectionInputArrayList.size());
	    		normalizeMetaData.setLinkAntiDirectionMax(antiDireMaxMin[0]);
	    		normalizeMetaData.setLinkAntiDirectionMin(antiDireMaxMin[1]);
	    		allMetaDataArrayList.add(normalizeMetaData);
	    	}
	    	write = new StringBuffer();
			write.append(allLinkID);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			write = new StringBuffer();
			write.append(tempAllLinkIDStr + "\r\n");
			bufferStream.write(write.toString().getBytes("UTF-8"));
			write = new StringBuffer();
			write.append(allMetaData);
			bufferStream.write(write.toString().getBytes("UTF-8"));
	    	write = new StringBuffer();
	    	for (int i = 0; i < allMetaDataArrayList.size(); i++) {
				NormalizeMetaData normalizeMetaData = allMetaDataArrayList.get(i);
				String tempLinkID = String.valueOf(normalizeMetaData.getLinkID());
		    	String tempLinkSameDirection = String.valueOf(normalizeMetaData.getLinkSameDirection());
		    	String tempLinkSameDirectionMax = String.valueOf(normalizeMetaData.getLinkSameDirectionMax());
		    	String tempLinkSameDirectionMin = String.valueOf(normalizeMetaData.getLinkSameDirectionMin());
		    	String tempLinkSameDirectionDataCount = String.valueOf(normalizeMetaData.getLinkSameDirectionDataCount());
		    	String tempLinkAntiDirection = String.valueOf(normalizeMetaData.getLinkAntiDirection());
		    	String tempLinkAntiDirectionMax = String.valueOf(normalizeMetaData.getLinkAntiDirectionMax());
		    	String tempLinkAntiDirectionMin = String.valueOf(normalizeMetaData.getLinkAntiDirectionMin());
		    	String tempLinkAntiDirectionDataCount = String.valueOf(normalizeMetaData.getLinkAntiDirectionDataCount());
				String metaDataStr = tempLinkID + "," + tempLinkSameDirection + "," + tempLinkSameDirectionMax + "," + tempLinkSameDirectionMin + "," + 
					tempLinkSameDirectionDataCount + "\r\n" + tempLinkID + ","+ tempLinkAntiDirection + "," + tempLinkAntiDirectionMax + "," +
					tempLinkAntiDirectionMin + "," + tempLinkAntiDirectionDataCount + "\r\n";
				System.out.print("输入向量归一化：" + metaDataStr);
				write = new StringBuffer();		
				write.append(metaDataStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));	     		
			}
	    	write = new StringBuffer();	
	    	String tempStr = endMetaDataDescription + txtDescription + txtHeading1 + txtHeading2;
	    	write.append(tempStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));	
			
			java.util.Set keySet = ANNDiscretizeVectorMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
	    	while (iterator.hasNext()) {
	    		Map.Entry mapEntry = (Map.Entry) iterator.next();
	    		int key = (Integer)mapEntry.getKey();
	    		ArrayList<ANNInputVector> discretizeVectorArrayList = ANNDiscretizeVectorMap.get(key);
	    		ArrayList<ANNInputVector> linkSameDirectionInputArrayList = new ArrayList<ANNInputVector>();//沿路段同方向行程信息
				ArrayList<ANNInputVector> linkAntiDirectionInputArrayList = new ArrayList<ANNInputVector>();
	    		double linkSameDirectionMax = 0;//设置同方向最大、最小值初始值
	    		double linkSameDirectionMin = 0;
	    		//设置异方向最大、最小值初始值
	    		double linkAntiDirectionMax = 0;
	    		double linkAntiDirectionMin = 0;
	    		double []sameDireMaxMin = new double[2];
	    		double []antiDireMaxMin = new double[2];
	    		obtainMaxMinValFromDiscretizeInfos(discretizeVectorArrayList, linkSameDirectionInputArrayList, 
	    				linkAntiDirectionInputArrayList, sameDireMaxMin, antiDireMaxMin);
	    		linkSameDirectionMax = sameDireMaxMin[0];
	    		linkSameDirectionMin = sameDireMaxMin[1];
	    		linkAntiDirectionMax = antiDireMaxMin[0];
	    		linkAntiDirectionMin = antiDireMaxMin[1];
//	    		int direction = 0;
//	    		int hour = 0;
//				int preHalfHour = 0;
//				int workDay = 0;
//				double travelTime = 0;				
//				for (int i = 0; i < discretizeVectorArrayList.size(); i++) {
//					ANNInputVector discretizeVector = discretizeVectorArrayList.get(i);
//					direction = discretizeVector.getDirection();
//					//同方向
//					if (direction == PubParameter.linkSameDirectionConst) {
//						hour = discretizeVector.getHour();
//						preHalfHour = discretizeVector.getPreHalfHour();
//						workDay = discretizeVector.getWorkDay();
//						travelTime = discretizeVector.getTravelTime();
//						if (hour > linkSameDirectionMax) {
//							linkSameDirectionMax = hour;
//						}
//						if (preHalfHour > linkSameDirectionMax) {
//							linkSameDirectionMax = preHalfHour;
//						}
//						if (workDay > linkSameDirectionMax) {
//							linkSameDirectionMax = workDay;
//						}
//						if (travelTime > linkSameDirectionMax) {
//							linkSameDirectionMax = travelTime;
//						}						
//						if (hour < linkSameDirectionMin) {
//							linkSameDirectionMin = hour;
//						}
//						if (preHalfHour < linkSameDirectionMin) {
//							linkSameDirectionMin = preHalfHour;
//						}
//						if (workDay < linkSameDirectionMin) {
//							linkSameDirectionMin = workDay;
//						}
//						if (travelTime < linkSameDirectionMin) {
//							linkSameDirectionMin = travelTime;
//						}
//						linkSameDirectionInputArrayList.add(discretizeVector);
//					}
//					//异方向
//					else {
//						hour = discretizeVector.getHour();
//						preHalfHour = discretizeVector.getPreHalfHour();
//						workDay = discretizeVector.getWorkDay();
//						travelTime = discretizeVector.getTravelTime();
//						if (hour > linkAntiDirectionMax) {
//							linkAntiDirectionMax = hour;
//						}
//						if (preHalfHour > linkAntiDirectionMax) {
//							linkAntiDirectionMax = preHalfHour;
//						}
//						if (workDay > linkAntiDirectionMax) {
//							linkAntiDirectionMax = workDay;
//						}
//						if (travelTime > linkAntiDirectionMax) {
//							linkAntiDirectionMax = travelTime;
//						}						
//						if (hour < linkAntiDirectionMin) {
//							linkAntiDirectionMin = hour;
//						}
//						if (preHalfHour < linkAntiDirectionMin) {
//							linkAntiDirectionMin = preHalfHour;
//						}
//						if (workDay < linkAntiDirectionMin) {
//							linkAntiDirectionMin = workDay;
//						}
//						if (travelTime < linkAntiDirectionMin) {
//							linkAntiDirectionMin = travelTime;
//						}
//						linkAntiDirectionInputArrayList.add(discretizeVector);				
//					}
//				}
	    		
	    		int direction = 0;
	    		int hour = 0;
				int preHalfHour = 0;
				int workDay = 0;
				double travelTime = 0;	
				String linkSameDirecMetaDescriptionStr = "metaData:" + "\r\n" + key + "," + PubParameter.linkSameDirectionConst + "," 
					+ linkSameDirectionMax + "," + linkSameDirectionMin + "," + linkSameDirectionInputArrayList.size() + "\r\n";
				write = new StringBuffer();	
				write.append(linkSameDirecMetaDescriptionStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));
				for (int i = 0; i < linkSameDirectionInputArrayList.size(); i++) {
					ANNInputVector discretizeVector = linkSameDirectionInputArrayList.get(i);
					hour = discretizeVector.getHour();
					preHalfHour = discretizeVector.getPreHalfHour();
					workDay = discretizeVector.getWorkDay();
					travelTime = discretizeVector.getTravelTime();
					//保留小数点后六位，并进行四舍五入
					double normalizeHour = (double)(hour - linkSameDirectionMin)/(linkSameDirectionMax - linkSameDirectionMin);
					String normalizeHourStr = String.format("%.6f", normalizeHour);
					double normalizePreHalfHour = (double)(preHalfHour - linkSameDirectionMin)/(linkSameDirectionMax - linkSameDirectionMin);
					String normalizePreHalfHourStr = String.format("%.6f", normalizePreHalfHour);
					double normalizeWorkDay = (double)(workDay - linkSameDirectionMin)/(linkSameDirectionMax - linkSameDirectionMin);
					String normalizeWorkDayStr = String.format("%.6f", normalizeWorkDay);
					double normalizeTravelTime = (double)(travelTime - linkSameDirectionMin)/(linkSameDirectionMax - linkSameDirectionMin);	
					String normalizeTravelTimeStr = String.format("%.6f", normalizeTravelTime);
					String normalizeStr = normalizeHourStr + "," + normalizePreHalfHourStr + "," 
						+ normalizeWorkDayStr + "," + normalizeTravelTimeStr + "\r\n"; 
					System.out.print("输入向量归一化：" + normalizeStr);
					write = new StringBuffer();		
					write.append(normalizeStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));			
				}
				String linkAntiDirecMetaDescriptionStr = "metaData:" + "\r\n" + key + "," + PubParameter.linkAntiDirectionConst + "," 
					+ linkAntiDirectionMax  + "," + linkAntiDirectionMin + "," + linkAntiDirectionInputArrayList.size() + "\r\n";
				write = new StringBuffer();	
				write.append(linkAntiDirecMetaDescriptionStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));
				for (int i = 0; i < linkAntiDirectionInputArrayList.size(); i++) {
					ANNInputVector discretizeVector = linkAntiDirectionInputArrayList.get(i);
					hour = discretizeVector.getHour();
					preHalfHour = discretizeVector.getPreHalfHour();
					workDay = discretizeVector.getWorkDay();
					travelTime = discretizeVector.getTravelTime();
					//保留小数点后六位，并进行四舍五入
					double normalizeHour = (double)(hour - linkAntiDirectionMin)/(linkAntiDirectionMax - linkAntiDirectionMin);
					String normalizeHourStr = String.format("%.6f", normalizeHour);
					double normalizePreHalfHour = (double)(preHalfHour - linkAntiDirectionMin)/(linkAntiDirectionMax - linkAntiDirectionMin);
					String normalizePreHalfHourStr = String.format("%.6f", normalizePreHalfHour);
					double normalizeWorkDay = (double)(workDay - linkAntiDirectionMin)/(linkAntiDirectionMax - linkAntiDirectionMin);
					String normalizeWorkDayStr = String.format("%.6f", normalizeWorkDay);
					double normalizeTravelTime = (double)(travelTime - linkAntiDirectionMin)/(linkAntiDirectionMax - linkAntiDirectionMin);	
					String normalizeTravelTimeStr = String.format("%.6f", normalizeTravelTime);
					String normalizeStr = normalizeHourStr + "," + normalizePreHalfHourStr + "," 
						+ normalizeWorkDayStr + "," + normalizeTravelTimeStr + "\r\n"; 
					System.out.print("输入向量归一化：" + normalizeStr);
					write = new StringBuffer();		
					write.append(normalizeStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));	
				}
	    	}
	    	String endtxtDescription = "endtxtDescription" + "\r\n";
	    	write = new StringBuffer();
	    	write.append(endtxtDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));	
	    	bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
			System.out.print("写入输入向量归一化结束");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	public void obtainMaxMinValFromDiscretizeInfos(ArrayList<ANNInputVector> discretizeVectorArrayList,
			ArrayList<ANNInputVector> linkSameDirectionInputArrayList, ArrayList<ANNInputVector> linkAntiDirectionInputArrayList,
			double[]sameDireMaxMin, double []antiDireMaxMin){
		try {
			double linkSameDirectionMax = 0;//设置同方向最大、最小值初始值
    		double linkSameDirectionMin = 0;
    		//设置异方向最大、最小值初始值
    		double linkAntiDirectionMax = 0;
    		double linkAntiDirectionMin = 0;
			int direction = 0;
    		int hour = 0;
			int preHalfHour = 0;
			int workDay = 0;
			double travelTime = 0;				
			for (int i = 0; i < discretizeVectorArrayList.size(); i++) {
				ANNInputVector discretizeVector = discretizeVectorArrayList.get(i);
				direction = discretizeVector.getDirection();
				//同方向
				if (direction == PubParameter.linkSameDirectionConst) {
					hour = discretizeVector.getHour();
					preHalfHour = discretizeVector.getPreHalfHour();
					workDay = discretizeVector.getWorkDay();
					travelTime = discretizeVector.getTravelTime();
					if (hour > linkSameDirectionMax) {
						linkSameDirectionMax = hour;
					}
					if (preHalfHour > linkSameDirectionMax) {
						linkSameDirectionMax = preHalfHour;
					}
					if (workDay > linkSameDirectionMax) {
						linkSameDirectionMax = workDay;
					}
					if (travelTime > linkSameDirectionMax) {
						linkSameDirectionMax = travelTime;
					}						
					if (hour < linkSameDirectionMin) {
						linkSameDirectionMin = hour;
					}
					if (preHalfHour < linkSameDirectionMin) {
						linkSameDirectionMin = preHalfHour;
					}
					if (workDay < linkSameDirectionMin) {
						linkSameDirectionMin = workDay;
					}
					if (travelTime < linkSameDirectionMin) {
						linkSameDirectionMin = travelTime;
					}
					linkSameDirectionInputArrayList.add(discretizeVector);
				}
				//异方向
				else {
					hour = discretizeVector.getHour();
					preHalfHour = discretizeVector.getPreHalfHour();
					workDay = discretizeVector.getWorkDay();
					travelTime = discretizeVector.getTravelTime();
					if (hour > linkAntiDirectionMax) {
						linkAntiDirectionMax = hour;
					}
					if (preHalfHour > linkAntiDirectionMax) {
						linkAntiDirectionMax = preHalfHour;
					}
					if (workDay > linkAntiDirectionMax) {
						linkAntiDirectionMax = workDay;
					}
					if (travelTime > linkAntiDirectionMax) {
						linkAntiDirectionMax = travelTime;
					}						
					if (hour < linkAntiDirectionMin) {
						linkAntiDirectionMin = hour;
					}
					if (preHalfHour < linkAntiDirectionMin) {
						linkAntiDirectionMin = preHalfHour;
					}
					if (workDay < linkAntiDirectionMin) {
						linkAntiDirectionMin = workDay;
					}
					if (travelTime < linkAntiDirectionMin) {
						linkAntiDirectionMin = travelTime;
					}
					linkAntiDirectionInputArrayList.add(discretizeVector);				
				}
			}
			sameDireMaxMin[0] = linkSameDirectionMax;
			sameDireMaxMin[1] = linkSameDirectionMin;
			antiDireMaxMin[0] = linkAntiDirectionMax;
			antiDireMaxMin[1] = linkAntiDirectionMin;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 读取归一化数据并写入txt文件
	 * 写入txt文件的输入向量包括三个分量：时（1-23）、分（是否为前半小时）、是否为工作日
	 * 输出向量为：通行时间
	 * @param 
	 */
	public void readNormalizeDataWriteToTxt(){
		String readPath = "C:\\normalize.txt";
		String writePath = "C:\\normalize3Input.txt";
		ArrayList<String[]> infosArrayList = new ArrayList<String[]>();
		System.out.print("开始读txt文件" + "\n");		
		try {
			File file = new File(readPath);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				while (str != null) {					
					String[]tempArrayStr = str.split(",");
					String[]arrayStr = new String[4];
					arrayStr[0] = tempArrayStr[1];
					arrayStr[1] = tempArrayStr[2];
					arrayStr[2] = tempArrayStr[3];
					arrayStr[3] = tempArrayStr[4];
					infosArrayList.add(arrayStr);
					System.out.print(str+"\n");
					str = bufferedReader.readLine();
				}
				reader.close();
				System.out.print("结束读txt文件"+"\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}
			
			//写入文件
			File file2 = new File(writePath);
			if (file2.exists()) {
				file2.delete();
				FileOutputStream outputStream = new FileOutputStream(new String(writePath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				for (int i = 0; i < infosArrayList.size(); i++) {
					String[]tempArray = infosArrayList.get(i);
					String hour = tempArray[0];
					String preHalfHour = tempArray[1];
					String workDay = tempArray[2];
					String travelTime = tempArray[3];
					String normalizeStr = hour + "," + preHalfHour + "," + workDay + "," + travelTime + "\r\n"; 
					System.out.print(normalizeStr);
					write = new StringBuffer();		
					write.append(normalizeStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));		
				}
				bufferStream.flush();  
				bufferStream.close();
				outputStream.close();
				System.out.print("写入结束");				
			}
			else {
				FileOutputStream outputStream = new FileOutputStream(new String(writePath));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				for (int i = 0; i < infosArrayList.size(); i++) {
					String[]tempArray = infosArrayList.get(i);
					String hour = tempArray[0];
					String preHalfHour = tempArray[1];
					String workDay = tempArray[2];
					String travelTime = tempArray[3];
					String normalizeStr = hour + "," + preHalfHour + "," + workDay + "," + travelTime + "\r\n"; 
					System.out.print(normalizeStr);
					write = new StringBuffer();		
					write.append(normalizeStr);
					bufferStream.write(write.toString().getBytes("UTF-8"));		
				}
				bufferStream.flush();  
				bufferStream.close();
				outputStream.close();
				System.out.print("写入结束");
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
}
