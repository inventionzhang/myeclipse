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
 * 合并统计通行时间
 * 1.合并所有文件夹内的txt文件，将其写入一个txt文件中
 * 2.根据路段ID对统计信息分类,相同ID路段的信息聚合在一起
 * 3.根据工作日与非工作日对统计信息分类
 * @author whu
 *
 */
public class MergeAllTxtFileAndClassify {
	public static void main(String[] args) {
		(new MergeAllTxtFileAndClassify()).readStatisticTravelTime();//所有txt文件信息合并到一个txt文件
//		(new MergeAllTxtFileAndClassify()).classifyMergedFileAccordIDAndDirection();//根据ID对路段信息进行分类，ID相同的路段归为一类
		(new MergeAllTxtFileAndClassify()).classifyStatisticsAccordWhetherWorkday();
		System.out.print("done!");
	}
	
	/**
	 * 每次读取一个文件夹里的内容，并写入文件txt末尾处
	 * 文件夹以日期命名：如，20130107
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
			//写文件标题
			File file = new File(mergeFileOutputPath);
			if (file.exists()) {
				file.delete();
			}
			System.out.print("开始写入文件标题：" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(mergeFileOutputPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("文件标题写入结束：" + '\n');
			//读取startDate1到endDate1之间的文件夹
			java.util.Date startDate1 = df.parse(startFile1);//开始文件夹日期	
		    java.util.Date endDate1 = df.parse(endFile1);//结束文件夹日期
		    endDate1.setDate(endDate1.getDate() + 1);		    
		    String endCondDateStr1 = df.format(endDate1);//结束日期条件	
		    String tempEndDateStr1 = df.format(startDate1);
		    
//			do {
//				tempEndDateStr1 = df.format(startDate1);
//				String tempFileNameStr1 = tempEndDateStr1;
//				String fileFolderPathStr1 = directoryPathStr + tempFileNameStr1;
//				System.out.print("读取文件夹：" + fileFolderPathStr1 + "内文件" + '\n');
//				ArrayList<String> infosArrayList = new ArrayList<String>();
//			    for (int j = 0; j < 4; j++) {
//					String fileNameStr = "allLinkTravelTime" + String.valueOf(j) + ".txt";
//					String filePathStr = fileFolderPathStr1 + "\\" + fileNameStr;
//					readFromTxtFile(filePathStr, infosArrayList);
//				}		
//				writeToTxtFile(mergeFileOutputPath, infosArrayList);
//				startDate1.setDate(startDate1.getDate() + 1);
//		    	tempEndDateStr1 = df.format(startDate1);
//			} while (!tempEndDateStr1.equals(endCondDateStr1));//读取文件的结束条件
//			
			
			
			
			//读取startDate2到endDate2之间的文件夹
			java.util.Date startDate2 = df.parse(startFile2);//开始文件夹日期	
		    java.util.Date endDate2 = df.parse(endFile2);//结束文件夹日期
		    endDate2.setDate(endDate2.getDate() + 1);		    
		    String endCondDateStr2 = df.format(endDate2);//结束日期条件		    		    
		    String tempEndDateStr2 = df.format(startDate2);
			do {				
				String tempFileNameStr2 = tempEndDateStr2;
				String fileFolderPathStr2 = directoryPathStr + tempFileNameStr2; 
				System.out.print("读取文件夹：" + fileFolderPathStr2 + "内文件" + '\n');
				ArrayList<String> infosArrayList = new ArrayList<String>();
			    for (int j = 0; j < 4; j++) {
					String fileNameStr = "allLinkTravelTime" + String.valueOf(j) + ".txt";
					String filePathStr = fileFolderPathStr2 + "\\" + fileNameStr;
					readFromTxtFile(filePathStr, infosArrayList);
				}		
				writeToTxtFile(mergeFileOutputPath, infosArrayList);
				startDate2.setDate(startDate2.getDate() + 1);
		    	tempEndDateStr2 = df.format(startDate2);		    	
			} while (!tempEndDateStr2.equals(endCondDateStr2));//读取文件的结束条件
		    System.out.print("done!所有文件夹数据合并结束!");
		    Thread.sleep(5000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 读取Txt文件
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public void readFromTxtFile(String filePathStr, ArrayList<String> infosArrayList){		
		try {
			File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
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
				System.out.print("结束读txt文件:" + filePathStr + "\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 写入Txt文件,写入文件末尾处
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public void writeToTxtFile(String filePathStr, ArrayList<String> infosArrayList){
		try {
			System.out.print("开始写入txt文件");	
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件开始处
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
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
			System.out.print("写入txt结束" + '\n');	
	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 根据ID对路段进行分类，ID相同的路段归为一类
	 * 1.首先，txt文件内容读到内存中
	 * 2.获得路段ID
	 * 3.根据路段ID分类
	 * @param mergeFileOutputPath	合并的输出文件路径
	 * @param classifyMergedTxtFileAccordID	根据ID进行分类后的文件路径
	 */
	public void classifyMergedFileAccordIDAndDirection(){
		String mergeFileOutputPath = PropertiesUtilJAR.getProperties("mergeFileOutputPath");
		String classifyMergedTxtFileAccordID = PropertiesUtilJAR.getProperties("classifyMergedFile");
		ArrayList<String> IDArrayList = new ArrayList<String>();//路段ID集合
		try {
			File file = new File(mergeFileOutputPath);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print("读数据" + str + "\n");
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
				System.out.print("结束读txt文件:" + mergeFileOutputPath + "\n");
				PubClass.sortIDArraylist(IDArrayList);//对ID进行升序排序
				System.out.print("数组根据ID排序结束！" + '\n');
				//将同类ID信息写入文件中
				file = new File(classifyMergedTxtFileAccordID);
				if (file.exists()) {
					file.delete();
				}
				
				System.out.print("开始写入文件标题：" + '\n');
				FileOutputStream outputStream = new FileOutputStream(new String(classifyMergedTxtFileAccordID));
				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
				StringBuffer write = new StringBuffer();
				String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
				write.append(description);
				bufferStream.write(write.toString().getBytes("UTF-8"));
				bufferStream.flush();      
				bufferStream.close(); 
				outputStream.close();
				System.out.print("文件标题写入结束：" + '\n');
				
				
				for (int i = 0; i < IDArrayList.size(); i++) {
					String IDStr = IDArrayList.get(i);
					ArrayList<String> infosArrayList = new ArrayList<String>();
					readFromTxtFileAccordIDAndDirection(mergeFileOutputPath, IDStr, infosArrayList);
					writeToTxtFile(classifyMergedTxtFileAccordID, infosArrayList);
				}
				System.out.print("同类ID信息写入文件中结束！" + '\n');
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
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
	 *	根据ID,获得相同ID的信息
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
				String encoding = "UTF-8";//解决中文乱码问题
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
				//同向写入
				for (int i = 0; i < uniqueSameDirectionInfosArraylist.size(); i++) {
					String tempStr = uniqueSameDirectionInfosArraylist.get(i);
					infosArrayList.add(tempStr);
				}
				//异向写入
				for (int i = 0; i < uniqueAntiDirectionInfosArraylist.size(); i++) {
					String tempStr = uniqueAntiDirectionInfosArraylist.get(i);
					infosArrayList.add(tempStr);
				}
				System.out.print("结束读txt文件:" + filePathStr + "\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}

	/**
	 * 根据时间信息，去掉InfosArrayList中相同信息，获得唯一信息并存入uniqueInfosArraylist
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
				System.out.print(i + ":" + dataCount + "剔除相同记录" + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/**
	 * 路段通行时间按工作日、非工作日（包括节假日）分别统计
	 */
	public void classifyStatisticsAccordWhetherWorkday(){
		try {
			String filePathStr = PropertiesUtilJAR.getProperties("classifyMergedFile");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			readFromTxtFile(filePathStr, infosArrayList);
			ArrayList<String> workDayTravelTimeArrayList = new ArrayList<String>();//工作日通行时间
			ArrayList<String> nonWorkDayTravelTimeArrayList = new ArrayList<String>();//非工作日通行时间
			
			for (int i = 0; i < infosArrayList.size(); i++) {
				System.out.print("文件记录数工作日判断，" + i + "：" + infosArrayList.size() + '\n');
				String travelStr = infosArrayList.get(i);
				String[] travelArrayStr = travelStr.split(",");
				String travelTimeStr = travelArrayStr[5];
				String[]tempArrayStr = travelTimeStr.split(" ");
				String dateStr = tempArrayStr[0];
				boolean isWorkday = true;//是否为工作日				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
				Date date = simpleDateFormat.parse(dateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    //周末、端午节除外
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
			System.out.print("开始保存工作日通行时间：" + '\n');
			String workDayTravelTime = PropertiesUtilJAR.getProperties("workDayTravelTime");
			File workDayTravelTimeFile = new File(workDayTravelTime);
			if (workDayTravelTimeFile.exists()) {
				workDayTravelTimeFile.delete();
				writeTxtFileHeading(workDayTravelTime);
				writeToTxtFile(workDayTravelTime, workDayTravelTimeArrayList);
				System.out.print("保存工作日通行时间结束：" + '\n');
			}
			else {
				writeTxtFileHeading(workDayTravelTime);
				writeToTxtFile(workDayTravelTime, workDayTravelTimeArrayList);
				System.out.print("保存工作日通行时间结束：" + '\n');
			}
			System.out.print("开始保存非工作日通行时间：" + '\n');
			String nonWorkDayTravelTime = PropertiesUtilJAR.getProperties("nonWorkDayTravelTime");
			File nonWorkDayTravelTimeFile = new File(nonWorkDayTravelTime);
			if (nonWorkDayTravelTimeFile.exists()) {
				nonWorkDayTravelTimeFile.delete();
				writeTxtFileHeading(nonWorkDayTravelTime);
				writeToTxtFile(nonWorkDayTravelTime, nonWorkDayTravelTimeArrayList);
				System.out.print("保存非工作日通行时间结束：" + '\n');
			}
			else {
				writeTxtFileHeading(nonWorkDayTravelTime);
				writeToTxtFile(nonWorkDayTravelTime, nonWorkDayTravelTimeArrayList);
				System.out.print("保存非工作日通行时间结束：" + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public void writeTxtFileHeading(String filePathStr){
		try {
			System.out.print("开始写入文件标题：" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("工作日文件标题写入结束：" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
}
