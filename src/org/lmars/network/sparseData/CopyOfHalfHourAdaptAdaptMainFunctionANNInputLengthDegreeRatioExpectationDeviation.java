package org.lmars.network.sparseData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.search.FromStringTerm;

import org.lmars.network.dataProcess.CharacteristicCalculation;
import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.neuralNetwork.AssistFunctionNeuralNetwork;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;


import sun.awt.image.ImageWatched.Link;


/**
 * 数据稀疏性问题：
 * 最终实验方案以及效果最好实验方案
 * 最终实验方案以及效果最好实验方案
 * 最终实验方案以及效果最好实验方案
 * 用神经网络进行稀疏路段行程时间预测：
 * 以周为周期性统计，只统计一周工作日的数据，周一至周五（去掉节假日）
 * 1.按照周一，周二等时间分别将数据聚合到一起；
 * 2.按照每天半小时内并且沿着路段同一行驶方向的数据求期望与方差；
 * 3.按照方向获得邻接路段进入路段方向以及驶出路段方向交通信息作为输入
 * 
 * 
 * 输入：
 * 输入层为6个神经元
 * 1.周工作日：1-5
 * 2.时：1-48
 * 3.度数比：目标路段与邻接路段
 * 4.长度比：目标路段与邻接路段
 * 5.速度期望：邻接路段
 * 6.速度标准差：邻接路段
 * 输出：
 * 目标路段与邻接路段行程时间比值：同一时间段半小时内
 * @author whu
 *
 */
public class CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation {

	public static void main(String[] args) {

		new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().obtainLinkTravelInfosOnWeekdays(88);//获得目标路段在周一、二或二者某天的数据
		new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkTravelCharacteristicCalculation();//计算所有路段速度期望、速度标准差		
		new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkANNInputAndNormalizeInput();
		
		
//		new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().ANNEnsembleInput(82, -1);		
//		new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().EnsembleInputUpstreamTargetLink(82, -1, 90, -1, 77, -1);//获得目标路段、上游路段网络集成输入		
//		
		
//		//数据归一化处理
//		String ANNInputPathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88.txt";
//		String metaDataFilePathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88MetaData.txt";
//		String normalizefilePathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88Normalize.txt";
//		new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
		
//		new MainFunctionANN().mergeANNInputTxtFileAndNormalize();
//		new AssistFunctionNeuralNetwork().obtainTargetAndAdjacentLinkSpeedExpectationDeviation(77);
		System.out.print("done!");
		System.exit(0);//是正常退出程序	
	}
	
	/**
	 * 获得所有路段神经网络输入信息以及归一化输入信息
	 * 输入：
	 * 1.周工作日：1-5
	 * 2.时：1-48
	 * 3.度数比：目标路段与邻接路段
	 * 4.长度比：目标路段与邻接路段
	 * 5.速度期望：邻接路段
	 * 6.速度标准差：邻接路段
	 * 7.交通流向
	 */
	public void allLinkANNInputAndNormalizeInput() {
//		Integer[] IDArray = {12,21,25,33,44,52,58,66,72,77,82,87,88};
//		Integer[] IDArray = {82};
		Integer[] IDArray = {716};
		for (int i = 0; i < IDArray.length; i++) {
			int linkID = IDArray[i];
			new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().obtainANNInputAndNormalizeInput(linkID);
		}
		System.out.print("done!");
	}
	
//	/**
//	 * 获得路段linkID集成神经网络的输入信息
//	 * 输入信息包括：
//	 * 1.周工作日：1-5
//	 * 2.时段：一天中哪一个时段1-48
//	 * 3.当前时刻前一时段行程时间
//	 * 4.当前时刻前一时段之前时段行程时间
//	 * 5.前两个时段行程时间之差
//	 * 输出：当前时刻行程时间
//	 * @param linkID
//	 * @param direction
//	 */
//	public void ANNEnsembleInput(int linkID, int direction){
//		try {
//			String ANNInputPathStr = "E:\\travelTimeProcess\\EnsembleInput" + linkID + ".txt";
////			String targetAdjacentLinkTravelTimeFilePathStr = "E:\\travelTimeProcess\\linkAdjacentTravelTimeInfos" + linkID + ".txt"; 
////			String headDescriptionStr = "timeStr" + "," + "adjacentLinkID" + "," + "linkTravelTime" + "," + "adjacentLinkTravelTime" + "," + "travelTimeRate" + "\r\n";
////			FileOperateFunction.writeHeadDescriptionToTxtFile(targetAdjacentLinkTravelTimeFilePathStr, headDescriptionStr);
//			//每隔30min的时间间隔
//			ArrayList<String> timeArraylist = new ArrayList<String>();
//			String startDateTimeStr = "2014-06-01 00:00:00";
//			String endDateTimeStr = "2014-06-02 00:00:00";
//			String tempStartDateTimeStr = startDateTimeStr;
//			int timeInterval = 1800;//统计每隔30min
//			while (!tempStartDateTimeStr.equals(endDateTimeStr)) {				
//				String[] endTimeArray = new String[1];
//				PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
//				String ttimeStr = tempStartDateTimeStr.substring(11);
//				timeArraylist.add(ttimeStr);
//				tempStartDateTimeStr = endTimeArray[0];
//			}
//			int size = timeArraylist.size();
//			String []timeStrArray = new String[size];
//			for (int i = 0; i < size; i++) {
//				timeStrArray[i] = new String();
//				timeStrArray[i] = timeArraylist.get(i);
//			}
//			
//			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
//			ArrayList<String> infosArrayList = new ArrayList<String>();//速度期望、速度方差
//			FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
//			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList; 
//			//按照天分类
//			ArrayList<String> mondayInfosArrayList = new ArrayList<String>();//周一
//			ArrayList<String> tuesInfosdayArrayList = new ArrayList<String>();//周二
//			ArrayList<String> wednesInfosdayArrayList = new ArrayList<String>();//周三
//			ArrayList<String> thursdayInfosArrayList = new ArrayList<String>();
//			ArrayList<String> fridayInfosArrayList = new ArrayList<String>();
//			ArrayList<String> weekInfosArrayList = new ArrayList<String>();//按照周分类后的信息
//			String weekdayStr = "";
//			int dataCount = infosArrayList.size();
//			for (int i = 0; i < infosArrayList.size(); i++) {
//				System.out.print("按照周期性分类：" + i + ":" + dataCount + '\n');
//				String str = infosArrayList.get(i);
//				String[]tempArray = str.split(",");
//				String dayStr = tempArray[4].substring(0,4);
//				if (dayStr.equals("Mond")) {
//					mondayInfosArrayList.add(str);
//				}
//				if (dayStr.equals("Tues")) {
//					tuesInfosdayArrayList.add(str);
//				}
//				if (dayStr.equals("Wedn")) {
//					wednesInfosdayArrayList.add(str);
//				}
//				if (dayStr.equals("Thur")) {
//					thursdayInfosArrayList.add(str);
//				}
//				if (dayStr.equals("Frid")) {
//					fridayInfosArrayList.add(str);
//				}			
//			}
//			ArrayList<double[]> allInputArrayList = new ArrayList<double[]>();
//			int continuousTimeInterval = 30 * 60;//连续时间间隔
//			int inputWeek = -1;// 对应一周中那一天
//			//工作日，周一至周五
//			for (int i = 0; i < 5; i++) {
//				if (i == 0) {
//					weekInfosArrayList = mondayInfosArrayList;
//					weekdayStr = "Mond";
//					inputWeek = 1;
//				}
//				if (i == 1) {
//					weekInfosArrayList = tuesInfosdayArrayList;
//					weekdayStr = "Tues";
//					inputWeek = 2;
//				}
//				if (i == 2) {
//					weekInfosArrayList = wednesInfosdayArrayList;
//					weekdayStr = "Wedn";
//					inputWeek = 3;
//				}
//				if (i == 3) {
//					weekInfosArrayList = thursdayInfosArrayList;
//					weekdayStr = "Thur";
//					inputWeek = 4;
//				}
//				if (i == 4) {
//					weekInfosArrayList = fridayInfosArrayList;
//					weekdayStr = "Frid";
//					inputWeek = 5;
//				}
//				ArrayList<String> targetLinkDirectionWeekInfosArrayList = new ArrayList<String>();//目标路段某方向的某天信息
//				for (int j = 0; j < weekInfosArrayList.size(); j++) {
//					String str = weekInfosArrayList.get(j);
//					String []tempStrArray = str.split(",");
//					String linkIDStr = tempStrArray[0];
//					String directionStr = tempStrArray[3];
//					int tempLinkID = Integer.parseInt(linkIDStr);
//					int tempDirection = Integer.parseInt(directionStr);
//					if (tempLinkID == linkID && tempDirection == direction) {
//						targetLinkDirectionWeekInfosArrayList.add(str);			
//					}
//				}
//				sortAccordTime(targetLinkDirectionWeekInfosArrayList);// 按照时间顺序排序
//				ArrayList<double[]> inputArrayList = new ArrayList<double[]>();
//				double inputTime = 0;// 对应一天中的时段
//				double inputPreTravelTime = -1;// 当前时刻前一时刻的行程时间
//				double inputPrePreTravelTime = -1;// 当前时刻前两时刻的行程时间
//				double inputTravelTimeDifference = -1;
//				double outputTravelTime = -1;
//				//当前时刻以及前两个邻接时段都有信息
//				//暂时不考虑太多，假定当前时刻及前两个邻接时段都有信息
//				int targetCount = targetLinkDirectionWeekInfosArrayList.size();
//				for (int j = 0; j < targetCount; j++) {
//					String str = targetLinkDirectionWeekInfosArrayList.get(j);
//					String []tempStrArray = str.split(",");
//					String tempTimeStr = tempStrArray[4];// 时间
//					String proTimeStr = tempTimeStr.substring(4);					
//					String tempSpeedStr = tempStrArray[5];// 速度
//					double tempSpeedDouble = Double.parseDouble(tempSpeedStr);
//					double speedDouble = Double.parseDouble(String.format("%.2f", tempSpeedDouble));
//					String tempLinkLength = tempStrArray[8];//路段长度
//					double tempLinkLengthDouble = Double.parseDouble(tempLinkLength);
//					double linkLengthDouble = Double.parseDouble(String.format("%.2f", tempLinkLengthDouble));
//					double tempTravelTime = linkLengthDouble/speedDouble;						
//					outputTravelTime = Double.parseDouble(String.format("%.2f", tempTravelTime));
//					
//					for (int k = 0; k < timeArraylist.size(); k++) {
//						String temp = timeArraylist.get(k);
//						if (proTimeStr.equals(temp)) {
////							inputTime = (k + 1)/10.0;
//							inputTime = (k + 1);
//							break;
//						}
//					}
//					if (j == 0) {
//						String preStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
//						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 2);
//						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
//					}
//					else if (j == 1) {
//						String preStr = targetLinkDirectionWeekInfosArrayList.get(0);
//						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
//						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
//					}
//					else {
//						String preStr = targetLinkDirectionWeekInfosArrayList.get(j - 1);
//						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(j - 2);
//						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));						
//					}
//					double tempInputTravelTimeDifference = inputPreTravelTime - inputPrePreTravelTime;
//					inputTravelTimeDifference = Double.parseDouble(String.format("%.2f", tempInputTravelTimeDifference));
//					double[] tempInput = new double[6];
//					tempInput[0] = inputWeek;//周几
//					tempInput[1] = inputTime;//半小时对应一个值,1到48,00:00:00用1代表			
//					tempInput[2] = inputPreTravelTime;//当前时刻前一时段的行程时间
//					tempInput[3] = inputPrePreTravelTime;//当前时刻前一时段之前时段的行程时间
//					tempInput[4] = inputTravelTimeDifference;//两个时段行程时间之差
//					tempInput[5] = outputTravelTime;//当前时刻行程时间
//					allInputArrayList.add(tempInput);
//				}			
//			}			
//			String headheadDescriptionStr = "workday" + "," + "time" + "," + "TT(t-1)" + "," + "TT(t-2)" + "," + "delt(t-1)" + "," +
//					"TT(t)" + "\r\n";
//			FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, allInputArrayList);
////			new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//		}
//	}

	
	
	
//	public void obtainInput(ArrayList<String> targetLinkDirectionWeekInfosArrayList, ArrayList<String> timeArraylist, 
//			int inputWeek, double []input, ArrayList<double[]> allInputArrayList ) {
//		try {
//			ArrayList<double[]> inputArrayList = new ArrayList<double[]>();
//			double inputTime = 0;// 对应一天中的时段
//			double inputPreTravelTime = -1;// 当前时刻前一时刻的行程时间
//			double inputPrePreTravelTime = -1;// 当前时刻前两时刻的行程时间
//			double inputTravelTimeDifference = -1;
//			double outputTravelTime = -1;
//			//当前时刻以及前两个邻接时段都有信息
//			//暂时不考虑太多，假定当前时刻及前两个邻接时段都有信息
//			int targetCount = targetLinkDirectionWeekInfosArrayList.size();
//			for (int j = 0; j < targetCount; j++) {
//				String str = targetLinkDirectionWeekInfosArrayList.get(j);
//				String []tempStrArray = str.split(",");
//				String tempTimeStr = tempStrArray[4];// 时间
//				String proTimeStr = tempTimeStr.substring(4);		
//				String tempSpeedStr = tempStrArray[5];//速度
//				double tempSpeedDouble = Double.parseDouble(tempSpeedStr);
//				double speedDouble = Double.parseDouble(String.format("%.2f", tempSpeedDouble));
//				String tempLinkLength = tempStrArray[8];//路段长度
//				double tempLinkLengthDouble = Double.parseDouble(tempLinkLength);
//				double linkLengthDouble = Double.parseDouble(String.format("%.2f", tempLinkLengthDouble));
//				double tempTravelTime = linkLengthDouble/speedDouble;						
//				outputTravelTime = Double.parseDouble(String.format("%.2f", tempTravelTime));					
//				for (int k = 0; k < timeArraylist.size(); k++) {
//					String temp = timeArraylist.get(k);
//					if (proTimeStr.equals(temp)) {
//						inputTime = (k + 1);
//						break;
//					}
//				}
//				if (j == 0) {
//					String preStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
//					String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 2);
//					double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//					double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//					inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//					inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
//				}
//				else if (j == 1) {
//					String preStr = targetLinkDirectionWeekInfosArrayList.get(0);
//					String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
//					double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//					double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//					inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//					inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
//				}
//				else {
//					String preStr = targetLinkDirectionWeekInfosArrayList.get(j - 1);
//					String prePreStr = targetLinkDirectionWeekInfosArrayList.get(j - 2);
//					double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
//					double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
//					inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
//					inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));						
//				}
//				double tempInputTravelTimeDifference = inputPreTravelTime - inputPrePreTravelTime;
//				inputTravelTimeDifference = Double.parseDouble(String.format("%.2f", tempInputTravelTimeDifference));
//				double[] tempInput = new double[6];
//				tempInput[0] = inputWeek;//周几
//				tempInput[1] = inputTime;//半小时对应一个值,1到48,00:00:00用1代表			
//				tempInput[2] = inputPreTravelTime;//当前时刻前一时段的行程时间
//				tempInput[3] = inputPrePreTravelTime;//当前时刻前一时段之前时段的行程时间
//				tempInput[4] = inputTravelTimeDifference;//两个时段行程时间之差
//				tempInput[5] = outputTravelTime;//当前时刻行程时间
//				allInputArrayList.add(tempInput);
//			}
//			
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//		}
//	}
	
	
	/**
	 * 弥补数据
	 * 某些时段没有数据，用邻接时刻数据代替缺失时刻数据
	 * @param timeArraylist	时间信息
	 * @param targetLinkDirectionWeekInfosArrayList	原始数据
	 * @param compensatedInfosArraylist	弥补后的数据
	 */
	public void compensateData(ArrayList<String> timeArraylist, ArrayList<String> targetLinkDirectionWeekInfosArrayList, ArrayList<String> compensatedInfosArraylist) {
		try {
			int infoCount = targetLinkDirectionWeekInfosArrayList.size();
			int timeCount = timeArraylist.size();
			if (infoCount == timeCount) {
				for (int i = 0; i < timeCount; i++) {
					String str = targetLinkDirectionWeekInfosArrayList.get(i);
					compensatedInfosArraylist.add(str);
				}	
			}
			else {
				for (int i = 0; i < timeCount; i++) {
					String timeStr = timeArraylist.get(i);
					for (int j = 0; j < infoCount; j++) {
						
						
						
					}
					
				}
				
				
				
			}
//			for (int i = 0; i < array.length; i++) {
//				
//			}
//			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	
//	/**
//	 * 获得行程时间信息
//	 * @param preStr
//	 */
//	public double obtainPreviousTravelTime(String preStr){
//		double preTempTravelTime = 0;
//		try {
//			String []preTempStrArray = preStr.split(",");
//			String preTempSpeedStr = preTempStrArray[5];// 速度
//			double preTempSpeedDouble = Double.parseDouble(preTempSpeedStr);
//			double preSpeedDouble = Double.parseDouble(String.format("%.2f", preTempSpeedDouble));
//			String preTempLinkLength = preTempStrArray[8];//路段长度
//			double preTempLinkLengthDouble = Double.parseDouble(preTempLinkLength);
//			double preLinkLengthDouble = Double.parseDouble(String.format("%.2f", preTempLinkLengthDouble));
//			preTempTravelTime = preLinkLengthDouble/preSpeedDouble;					
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//		}
//		return preTempTravelTime;	
//	}
	
	
//	/**
//	 * 按照一天的时间先后顺序进行排序
//	 * 冒泡排序
//	 * @param targetLinkDirectionWeekInfosArrayList	原信息
//	 * @param sortTargetLinkDirectionWeekInfosArrayList	排序后的信息
//	 * "2014-01-01 "
//	 */
//	public void sortAccordTime(ArrayList<String> targetLinkDirectionWeekInfosArrayList){
//		try {
//			//冒泡排序
//			String dateStr = "2014-01-01 ";
//			for (int i = 0; i < targetLinkDirectionWeekInfosArrayList.size(); i++) {
//				String str1 = targetLinkDirectionWeekInfosArrayList.get(i);
//				String []tempStrArray1 = str1.split(",");
//				String tempTimeStr1 = tempStrArray1[4];// 时间
//				String proTimeStr1 = tempTimeStr1.substring(4);
//				String dateTimeStr1 = dateStr + proTimeStr1;
//				for (int j = i + 1; j < targetLinkDirectionWeekInfosArrayList.size(); j++) {
//					String str2 = targetLinkDirectionWeekInfosArrayList.get(j);
//					String []tempStrArray2 = str2.split(",");
//					String tempTimeStr2 = tempStrArray2[4];// 时间
//					String proTimeStr2 = tempTimeStr2.substring(4);
//					String dateTimeStr2 = dateStr + proTimeStr2;
//					if (!PubClass.isTime2AfterTime1(dateTimeStr1, dateTimeStr2)) {
//						String tempStr = str1;
//						str1 = str2;
//						targetLinkDirectionWeekInfosArrayList.set(i, str2);
//						targetLinkDirectionWeekInfosArrayList.set(j, tempStr);						
//					}
//				}				
////				String str = targetLinkDirectionWeekInfosArrayList.get(i);
////				String []tempStrArray = str.split(",");
////				String linkIDStr = tempStrArray[0];
////				String enterIDStr = tempStrArray[1];
////				String exitIDStr = tempStrArray[2];
////				String directionStr = tempStrArray[3];
////				String tempTimeStr = tempStrArray[4];// 时间
////				String proTimeStr = tempTimeStr.substring(4);
////				String speedExpectation = tempStrArray[5];
////				String speedStandardDeviationStr = tempStrArray[6];
////				String linkDegreeStr = tempStrArray[7];
////				String linkLengthStr = tempStrArray[8];			
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//		}
//	}
	
	
	/**
	 * 合并神经网络输入文件为一个文件
	 */
	public void mergeANNInputTxtFileAndNormalize() {
		String filePathStr = "C:\\travelTimeProcess";
		ArrayList<String> fileNameArrayList = new ArrayList<String>();
		Integer[]IDArray = {12,21,25,33,44,52,58,66,72,77,82,87,88};
		for (int i = 0; i < IDArray.length; i++) {
			int linkID = IDArray[i];
			String nameStr = "ANNInput" + linkID;
			fileNameArrayList.add(nameStr);
		}
		String mergeANNInputFileStr = "C:\\travelTimeProcess\\mergeANNInputFile.txt";
		String headDescriptionStr = "similarity" + "," + "lengthRate" + "," + "pearsonCorr" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.mergeTxtFile(filePathStr, fileNameArrayList, mergeANNInputFileStr, headDescriptionStr);
		String metaDataFilePathStr = "C:\\travelTimeProcess\\mergeANNInputMetaData.txt";
		String normalizefilePathStr = "C:\\travelTimeProcess\\mergeANNInputNormalize.txt";
		new NormalizeSparseData().dataNormalize(mergeANNInputFileStr, metaDataFilePathStr, normalizefilePathStr);
	}
	
	
	/**
	 * 获得神经网络某一路段输入信息并归一化的输入信息
	 * @param linkID
	 */
	public void obtainANNInputAndNormalizeInput(int linkID) {
		String ANNInputPathStr = "E:\\travelTimeProcess\\ANNInput" + linkID + ".txt";
		String metaDataFilePathStr = "E:\\travelTimeProcess\\ANNInputMetaData" + linkID + ".txt";
		String normalizefilePathStr = "E:\\travelTimeProcess\\ANNInputNormalize" + linkID + ".txt";
		String targetAdjacentLinkTravelTimeFilePathStr = "E:\\travelTimeProcess\\linkAdjacentTravelTimeInfos" + linkID + ".txt"; 
		String headDescriptionStr = "timeStr" + "," + "adjacentLinkID" + "," + "linkTravelTime" + "," + "adjacentLinkTravelTime" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.writeHeadDescriptionToTxtFile(targetAdjacentLinkTravelTimeFilePathStr, headDescriptionStr);
		String timeStr = "";	
		//每隔30min的时间间隔
		ArrayList<String> timeArraylist = new ArrayList<String>();
		String startDateTimeStr = "2014-06-01 00:00:00";
		String endDateTimeStr = "2014-06-02 00:00:00";
		String tempStartDateTimeStr = startDateTimeStr;
		int timeInterval = 1800;//统计每隔30min
		while (!tempStartDateTimeStr.equals(endDateTimeStr)) {				
			String[] endTimeArray = new String[1];
			PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
			String ttimeStr = tempStartDateTimeStr.substring(11);
			timeArraylist.add(ttimeStr);
			tempStartDateTimeStr = endTimeArray[0];
		}
		int size = timeArraylist.size();
		String []timeStrArray = new String[size];
		for (int i = 0; i < size; i++) {
			timeStrArray[i] = new String();
			timeStrArray[i] = timeArraylist.get(i);
		}
		
		String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
		ArrayList<String> infosArrayList = new ArrayList<String>();//速度期望、速度方差
		FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList; 
		//按照天分类
		ArrayList<String> mondayInfosArrayList = new ArrayList<String>();//周一
		ArrayList<String> tuesInfosdayArrayList = new ArrayList<String>();//周二
		ArrayList<String> wednesInfosdayArrayList = new ArrayList<String>();//周三
		ArrayList<String> thursdayInfosArrayList = new ArrayList<String>();
		ArrayList<String> fridayInfosArrayList = new ArrayList<String>();
		ArrayList<String> weekInfosArrayList = new ArrayList<String>();//按照周分类后的信息
		String weekdayStr = "";
		int dataCount = infosArrayList.size();
		for (int i = 0; i < infosArrayList.size(); i++) {
			System.out.print("按照周期性分类：" + i + ":" + dataCount + '\n');
			String str = infosArrayList.get(i);
			String[]tempArray = str.split(",");
			String dayStr = tempArray[4].substring(0,4);
			if (dayStr.equals("Mond")) {
				mondayInfosArrayList.add(str);
			}
			if (dayStr.equals("Tues")) {
				tuesInfosdayArrayList.add(str);
			}
			if (dayStr.equals("Wedn")) {
				wednesInfosdayArrayList.add(str);
			}
			if (dayStr.equals("Thur")) {
				thursdayInfosArrayList.add(str);
			}
			if (dayStr.equals("Frid")) {
				fridayInfosArrayList.add(str);
			}			
		}
		ArrayList<double[]> allInputArrayList = new ArrayList<double[]>();
		//工作日，周一至周五
		for (int i = 0; i < 5; i++) {
			if (i == 0) {
				weekInfosArrayList = mondayInfosArrayList;
				weekdayStr = "Mond";
			}
			if (i == 1) {
				weekInfosArrayList = tuesInfosdayArrayList;
				weekdayStr = "Tues";
			}
			if (i == 2) {
				weekInfosArrayList = wednesInfosdayArrayList;
				weekdayStr = "Wedn";
			}
			if (i == 3) {
				weekInfosArrayList = thursdayInfosArrayList;
				weekdayStr = "Thur";
			}
			if (i == 4) {
				weekInfosArrayList = fridayInfosArrayList;
				weekdayStr = "Frid";
			}		
			ArrayList<double[]> inputArrayList = new ArrayList<double[]>();
			for (int j = 0; j < timeStrArray.length; j++) {
				timeStr = timeStrArray[j];
				double inputTime = (j + 1)/10.0;
				String ptimeStr = weekdayStr + timeStr;
				ANNInput(linkID, ptimeStr, inputTime, targetAdjacentLinkTravelTimeFilePathStr, inputArrayList, weekInfosArrayList, polylineCollArrayList);
				System.out.print(j + ":" + timeStrArray.length);
			}
			for (int j = 0; j < inputArrayList.size(); j++) {
				double[] temp = inputArrayList.get(j);
				double[] proTemp = new double[15];
				proTemp[0] = i + 1;//周几
				proTemp[1] = temp[11];//半小时对应一个值,1到48,00:00:00用1代表			
				proTemp[2] = temp[0];//目标路段ID
				proTemp[3] = temp[1];//邻接路段ID
				proTemp[4] = temp[2];//目标路段长度
				proTemp[5] = temp[3];//邻接路段长度
				proTemp[6] = temp[4];//目标路段行程时间
				proTemp[7] = temp[5];//邻接路段行程时间
				proTemp[8] = temp[6];//similarity
				proTemp[9] = temp[7];//lengthRate
				proTemp[10] = temp[8];//speedExpectation
				proTemp[11] = temp[9];//speedStandardDevi
				proTemp[12] = temp[10];//travelTimeRate
				proTemp[13] = temp[12];//邻接路段编号
				proTemp[14] = temp[13];//邻接路段方向		
				allInputArrayList.add(proTemp);
			}						
		}
		String headheadDescriptionStr = "workday" + "," + "time" + "," + "targetLinkID" + "," + "adjacentLinkID" + "," + "targetLinkLength" + "," +
		"adjacentLinkLength" + "," + "targetLinkTravelTime" + "," +"adjacentLinkTravelTime" + "," +
		"similarity" + "," + "lengthRate" + "," + "speedExpectation" + "," + "speedStandardDevi" + "," + 
		"travelTimeRate" + "," + "adjacentDigitVal" + "," + "adjacentDigitDirection" + "\r\n";
		FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, allInputArrayList);
		new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
		
		
	}
	
	/**
	 * 获得某路段、某时间神经网络的输入信息
	 * 输入：
	 * 路段与邻接路段度数比值
	 * 路段与邻接路段长度比值
	 * 邻接路段速度期望、速度标准差
	 * 上游进入路段方向信息，下游离开路段方向信息
	 * 
	 * 输出：
	 * 路段与邻接路段行程时间比值
	 */
	public void ANNInput(int targetLinkID, String timeStr, double inputTime, String targetAdjacentLinkTravelTimeFilePathStr, 
			ArrayList<double[]> inputArrayList, ArrayList<String> infosArrayList, ArrayList<MapMatchEdge> polylineCollArrayList) {
		try {
			//时刻、目标路段行程时间、邻接路段行程时间以及比值
			ArrayList<String> targetAdjacentLinkTravelTimeArrayList = new ArrayList<String>();
			MapMatchEdge targetEdge = new MapMatchEdge();
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				targetEdge = polylineCollArrayList.get(i);
				if (targetEdge.getEdgeID() == targetLinkID) {
					break;
				}
			}
			int targetLinkDegree = targetEdge.getFirstLevelConnEdgeArray().size();
			double targetLinkLength = targetEdge.getEdgeLength();
			ArrayList<MapMatchEdge> relaEdgeArrayList = targetEdge.getFirstLevelConnEdgeArray();
			int relaEdgeCount = relaEdgeArrayList.size();
			for (int i = 0; i < relaEdgeCount; i++) {
				MapMatchEdge tEdge1 = new MapMatchEdge();
				int link1ID = -1;
				int link1Degree = -1;
				double link1Length = -1;
				tEdge1 = relaEdgeArrayList.get(i);
				link1ID = tEdge1.getEdgeID();				
				int adjacentlinkDigitVal = i + 1;//邻接路段编号数值化
				link1Degree = tEdge1.getFirstLevelConnEdgeArray().size();
				link1Length = tEdge1.getEdgeLength();
				double temp = (double)targetLinkDegree/link1Degree;
				double similarity = Double.parseDouble(String.format("%.4f", temp));
				double lengthRate = Double.parseDouble(String.format("%.4f", targetLinkLength/link1Length));
				double targetAdjacentLinkTravelTimeSameDirection[] = new double[3];//目标路段同方向信息   分别存储目标路段、邻接路段行程时间以及两者之间比值
				double[]expectationStandardDeviSameDirection = new double[2];//目标路段同方向 邻接路段link1ID速度期望与标准差				
				int sameDirection = PubParameter.sameDirection;
				int antiDirection = PubParameter.antiDirection;
				obtainTravelTimeRate(sameDirection, targetLinkID,link1ID, timeStr, infosArrayList, targetAdjacentLinkTravelTimeSameDirection, expectationStandardDeviSameDirection);//行程时间比值
				double targetAdjacentLinkTravelTimeAntiDirection[] = new double[3];//目标路段反方向信息   分别存储目标路段、邻接路段行程时间以及两者之间比值
				double[]expectationStandardDeviAntiDirection = new double[2];//邻接路段link1ID速度期望与标准差
				obtainTravelTimeRate(antiDirection, targetLinkID,link1ID, timeStr, infosArrayList, targetAdjacentLinkTravelTimeAntiDirection, expectationStandardDeviAntiDirection);
				double sameDirectiontravelTimeRate = targetAdjacentLinkTravelTimeSameDirection[2];
				double sameDirectionspeedExpectation = expectationStandardDeviSameDirection[0];				
				double sameDirectionspeedStandardDevi = expectationStandardDeviSameDirection[1];
				if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && 
						Math.abs(sameDirectiontravelTimeRate) > 0.000001 && Math.abs(sameDirectionspeedExpectation) > 0.000001 &&
						Math.abs(sameDirectionspeedStandardDevi) > 0.000001) {
					double[]inputInfos = new double[14];
					inputInfos[0] = targetLinkID;//目标路段ID
					inputInfos[1] = link1ID;//邻接路段ID					
					inputInfos[2] = Double.parseDouble(String.format("%.4f", targetLinkLength));//目标路段长度
					inputInfos[3] = Double.parseDouble(String.format("%.4f", link1Length));//邻接路段长度
					inputInfos[4] = targetAdjacentLinkTravelTimeSameDirection[0];//目标路段行程时间
					inputInfos[5] = targetAdjacentLinkTravelTimeSameDirection[1];//邻接路段行程时间					
					inputInfos[6] = similarity;
					inputInfos[7] = lengthRate;
					inputInfos[8] = sameDirectionspeedExpectation;
					inputInfos[9] = sameDirectionspeedStandardDevi;
					inputInfos[10] = sameDirectiontravelTimeRate;
					inputInfos[11] = inputTime;//对应的小时数
					inputInfos[12] = adjacentlinkDigitVal;//邻接路段编号数值化
					inputInfos[13] = 1;//邻接路段交通流方向，同向					
					inputArrayList.add(inputInfos);
				}				
				double antiDirectiontravelTimeRate = targetAdjacentLinkTravelTimeAntiDirection[2];
				double antiDirectionspeedExpectation = expectationStandardDeviAntiDirection[0];				
				double antiDirectionspeedStandardDevi = expectationStandardDeviAntiDirection[1];
				if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && 
						Math.abs(antiDirectiontravelTimeRate) > 0.000001 && Math.abs(antiDirectionspeedExpectation) > 0.000001 &&
						Math.abs(antiDirectionspeedStandardDevi) > 0.000001) {
					double[]inputInfos = new double[14];					
					inputInfos[0] = targetLinkID;//目标路段
					inputInfos[1] = link1ID;//邻接路段
					inputInfos[2] = Double.parseDouble(String.format("%.4f", targetLinkLength));//目标路段长度
					inputInfos[3] = Double.parseDouble(String.format("%.4f", link1Length));//邻接路段长度
					inputInfos[4] = targetAdjacentLinkTravelTimeAntiDirection[0];//目标路段行程时间	
					inputInfos[5] = targetAdjacentLinkTravelTimeAntiDirection[1];//邻接路段行程时间					
					inputInfos[6] = similarity;
					inputInfos[7] = lengthRate;
					inputInfos[8] = antiDirectionspeedExpectation;
					inputInfos[9] = antiDirectionspeedStandardDevi;
					inputInfos[10] = antiDirectiontravelTimeRate;
					inputInfos[11] =inputTime;
					inputInfos[12] = adjacentlinkDigitVal;//邻接路段编号数值化
					inputInfos[13] = 2;//邻接路段交通流方向，异向
					inputArrayList.add(inputInfos);
				}				
			}			
//			FileOperateFunction.writeStringArraylistToEndTxtFile(targetAdjacentLinkTravelTimeFilePathStr, targetAdjacentLinkTravelTimeArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	//某一路段每天半小时的速度期望计算
	public void expectedSpeedcalculation(int linkID, ArrayList<String> infosArrayList, Map<String, Double> expectedSpeedMap) {
		try {		
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			String startDateTimeStr = "2014-06-01 00:00:00";
			String endDateTimeStr = "2014-08-01 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
			int timeInterval = 1800;//统计每隔30min的路段出租车通行时间
			int count = 0;
			while (!tempStartDateTimeStr.equals(endDateTimeStr)) {				
				ArrayList<String> targetDateTravelInfos = new ArrayList<String>();
				String targetDate = tempStartDateTimeStr.substring(0, 10);
				assistFunctionNeuralNetwork.obtainTargetDateTravelInfosFromTxt(targetDate, linkTravelInfos, targetDateTravelInfos);				
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
				String startTimeStr = tempStartDateTimeStr.substring(11);
				String endTimeStr = endTimeArray[0].substring(11);
				if (startTimeStr.equals("23:30:00")) {
					endTimeStr = "23:59:59";
				}
				ArrayList<String> linkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
				assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(targetDateTravelInfos, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);		
				double expectedSpeed = CharacteristicCalculation.calculateLinkSpeedExpectation(linkTravelInfosInTimeIntervalArrayList);				
				if (expectedSpeed != 0) {
					expectedSpeedMap.put(tempStartDateTimeStr, expectedSpeed);
					System.out.print(tempStartDateTimeStr + "," + expectedSpeed + ":" + '\n');
				}	
				tempStartDateTimeStr = endTimeArray[0];
			}
			System.out.print("");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 计算所有路段速度期望、速度标准差
	 * 每天的数据中，每隔半小时计算求值
	 */
	public void allLinkTravelCharacteristicCalculation() {
		try {
//			//**临时
//			int targetLinkID = 82;
//			Boolean flag = false;
//			//***
			
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			ArrayList<LinkTravelCharacteristic> allLinkSpeedExpectationStandardDeviationArrayList = new ArrayList<LinkTravelCharacteristic>();
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);	
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				System.out.print("路段期望、标准差计算：" + i + ":" + polylineCollArrayList.size() + '\n');
				MapMatchEdge edge = polylineCollArrayList.get(i);
				int linkID = edge.getEdgeID();
				int linkDegree = edge.getFirstLevelConnEdgeArray().size();
				double tempLinkLength = edge.getEdgeLength();
				String tempLinkLengthStr = String.format("%.4f", tempLinkLength);
				double processLinkLength = Double.parseDouble(tempLinkLengthStr);				
				ArrayList<LinkTravelCharacteristic> linkSpeedExpectationStandardDeviationArrayList = new ArrayList<LinkTravelCharacteristic>();
				speedExpectationAndStandardDeviationCalculation(linkID,linkSpeedExpectationStandardDeviationArrayList,infosArrayList);
				for (int j = 0; j < linkSpeedExpectationStandardDeviationArrayList.size(); j++) {
					LinkTravelCharacteristic linkTravelCharacteristic = linkSpeedExpectationStandardDeviationArrayList.get(j);
					linkTravelCharacteristic.setLinkDegree(linkDegree);
					linkTravelCharacteristic.setLinkLength(processLinkLength);
					allLinkSpeedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
				}
			}
			System.out.print("信息开始写入txt文件：" + '\n');
			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
//			String filePathStr = "C:\\travelTimeProcess\\SpeedExpectationStandardDeviationLink" + targetLinkID + ".txt";
			String headDescriptionStr = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "Direction" + "," + "timeStr" + "," + "speedExpectation" + "," + "speedStandardDeviation" + "," + "linkDegree" + "," + "linkLength" + "\r\n";
			FileOperateFunction.writeToTxtFile(filePathStr, headDescriptionStr, allLinkSpeedExpectationStandardDeviationArrayList);
			System.out.print("信息写入txt文件结束：" + '\n');
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * /**
	 * 速度方差计算
	 * 某一路段速度期望、标准差计算，按照周期性以及每天数据每隔半小时、同一行驶方向计算
	 * @param linkID
	 * @param speedExpectationStandardDeviationArrayList	路段通行特征
	 */
	public void speedExpectationAndStandardDeviationCalculation(int linkID, ArrayList<LinkTravelCharacteristic> speedExpectationStandardDeviationArrayList,ArrayList<String> infosArrayList) {
		try {			
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();//目标路段通行信息
			//目标路段周一、周二等的数据分别合并
			ArrayList<String> linkmondayArrayList = new ArrayList<String>();//周一
			ArrayList<String> linktuesdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> linkwednesdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> linkthursdayArrayList = new ArrayList<String>();
			ArrayList<String> linkfridayArrayList = new ArrayList<String>();
			//获得目标路段通行信息
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[5].substring(0,10);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");    
				Date date = simpleDateFormat.parse(tempDateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
			    	linkmondayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
			    	linktuesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
			    	linkwednesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
			    	linkthursdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
			    	linkfridayArrayList.add(str);
				}				
			}						
			int timeInterval = 1800;//统计每隔30min的路段出租车通行时间
			ArrayList<String> weekInfosArraylist = new ArrayList<String>();
			String weekdayStr = "";
			for (int i = 0; i < 5; i++) {
				String startDateTimeStr = "2014-06-01 00:00:00";
				String endDateTimeStr = "2014-06-02 00:00:00";
				String tempStartDateTimeStr = startDateTimeStr;
				if (i == 0) {
					weekInfosArraylist = linkmondayArrayList;
					weekdayStr = "Mond";
				}
				if (i == 1) {
					weekInfosArraylist = linktuesdayArrayList;
					weekdayStr = "Tues";
				}
				if (i == 2) {
					weekInfosArraylist = linkwednesdayArrayList;
					weekdayStr = "Wedn";
				}
				if (i == 3) {
					weekInfosArraylist = linkthursdayArrayList;
					weekdayStr = "Thur";
				}
				if (i == 4) {
					weekInfosArraylist = linkfridayArrayList;
					weekdayStr = "Frid";
				}
				
				//因为有重复的数据，在此将重复的数据剔除掉，保证记录的唯一性
				//相同记录基本是直接相连的,只跟下一条比较
				ArrayList<String> processWeekInfosArraylist = new ArrayList<String>();
				for (int j = 0; j < weekInfosArraylist.size(); j++) {
					String str1 = weekInfosArraylist.get(j);
					processWeekInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[4];
					String timeStr1 = strArray1[5];					
					if (j != weekInfosArraylist.size() - 1) {
						String str2 = weekInfosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[4];
						String timeStr2 = strArray2[5];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//跳过相同记录							
						}
					}					
				}
								
				while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
					String[] endTimeArray = new String[1];
					PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
					String startTimeStr = tempStartDateTimeStr.substring(11);
					String endTimeStr = endTimeArray[0].substring(11);
					if (startTimeStr.equals("23:30:00")) {
						endTimeStr = "23:59:59";
					}
					ArrayList<String> linkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(processWeekInfosArraylist, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);		
					//按照同向与异向分开
					ArrayList<String> sameDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					ArrayList<String> antiDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();
					for (int j = 0; j < linkTravelInfosInTimeIntervalArrayList.size(); j++) {
						String ttStr = linkTravelInfosInTimeIntervalArrayList.get(j);
						String[]tempArrayStr = ttStr.split(",");
						int direction = Integer.parseInt(tempArrayStr[3]);
						if (direction == 1) {
							sameDirectionLinkTravelInfosInTimeIntervalArrayList.add(ttStr);
						}
						else {
							antiDirectionLinkTravelInfosInTimeIntervalArrayList.add(ttStr);
						}
					}	
					
					//数据量大于等于4时求期望、标准差才有意义,
					
					if (sameDirectionLinkTravelInfosInTimeIntervalArrayList.size() >= 4) {
						ArrayList<String> proSameDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();//去掉两端的速度异常值之后的数据
						//去掉两端的速度异常值
						double tempMeanSpeed = Double.parseDouble(sameDirectionLinkTravelInfosInTimeIntervalArrayList.get(0).split(",")[7]);
						double maxVal = tempMeanSpeed;
						int maxIndex = 0;//最大值索引
						int minIndex = 0;//最小值索引
						double minVal = tempMeanSpeed;
						for (int j = 0; j < sameDirectionLinkTravelInfosInTimeIntervalArrayList.size(); j++) {
							String str = sameDirectionLinkTravelInfosInTimeIntervalArrayList.get(j);
							String[] tempArrayStr = str.split(",");
							double meanSpeed = Double.parseDouble(tempArrayStr[7]);
							if (maxVal < meanSpeed) {
								maxVal = meanSpeed;
								maxIndex = j;
							}
							if (minVal > meanSpeed) {
								minVal = meanSpeed;
								minIndex = j;
							}							
						}
						for (int k = 0; k < sameDirectionLinkTravelInfosInTimeIntervalArrayList.size(); k++) {
							String str = sameDirectionLinkTravelInfosInTimeIntervalArrayList.get(k);
							if (k != maxIndex && k != minIndex) {
								proSameDirectionLinkTravelInfosInTimeIntervalArrayList.add(str);
							}	
						}						
						double sameDirectionExpectedSpeed = CharacteristicCalculation.calculateLinkSpeedExpectation(proSameDirectionLinkTravelInfosInTimeIntervalArrayList);
						if (sameDirectionExpectedSpeed != 0) {					
							double sameDirectionSpeedStandardDeviation = CharacteristicCalculation.calculateLinkSpeedStandardDeviation(sameDirectionExpectedSpeed, proSameDirectionLinkTravelInfosInTimeIntervalArrayList);
							tempStartDateTimeStr = endTimeArray[0];
							System.out.print(sameDirectionExpectedSpeed + "," + sameDirectionSpeedStandardDeviation + '\n');
							String sameDirectionInfoString = proSameDirectionLinkTravelInfosInTimeIntervalArrayList.get(0);
							String [] sameDirectionArray = sameDirectionInfoString.split(",");
							int sameDirectionEnterNodeID = Integer.parseInt(sameDirectionArray[1]);
							int sameDirectionExitNodeID = Integer.parseInt(sameDirectionArray[2]);							
							LinkTravelCharacteristic linkTravelCharacteristic = new LinkTravelCharacteristic();
							linkTravelCharacteristic.setWeekdayStr(weekdayStr);
							linkTravelCharacteristic.setLinkID(linkID);
							linkTravelCharacteristic.setDirection(1);
							linkTravelCharacteristic.setEnterNodeID(sameDirectionEnterNodeID);
							linkTravelCharacteristic.setExitNodeID(sameDirectionExitNodeID);
							
							String timeStr = weekdayStr + startTimeStr;
							linkTravelCharacteristic.setTimeStr(timeStr);
							linkTravelCharacteristic.setSpeedExpectation(sameDirectionExpectedSpeed);
							linkTravelCharacteristic.setSpeedStandDeviation(sameDirectionSpeedStandardDeviation);
							speedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
							System.out.print(timeStr + "," + sameDirectionExpectedSpeed + ":" + sameDirectionSpeedStandardDeviation + '\n');						
						}
					}
					
					if (antiDirectionLinkTravelInfosInTimeIntervalArrayList.size() >=4) {
						double tempMeanSpeed = Double.parseDouble(antiDirectionLinkTravelInfosInTimeIntervalArrayList.get(0).split(",")[7]);
						ArrayList<String> proAntiDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();//去掉两端的速度异常值之后的数据
						//去掉两端的速度异常值
						double maxVal = tempMeanSpeed;
						int maxIndex = 0;//最大值索引
						int minIndex = 0;//最小值索引
						double minVal = tempMeanSpeed;
						for (int j1 = 0; j1 < antiDirectionLinkTravelInfosInTimeIntervalArrayList.size(); j1++) {
							String str = antiDirectionLinkTravelInfosInTimeIntervalArrayList.get(j1);
							String[] tempArrayStr = str.split(",");
							double meanSpeed = Double.parseDouble(tempArrayStr[7]);
							if (maxVal < meanSpeed) {
								maxVal = meanSpeed;
								maxIndex = j1;
							}
							if (minVal > meanSpeed) {
								minVal = meanSpeed;
								minIndex = j1;
							}							
						}
						for (int k1 = 0; k1 < antiDirectionLinkTravelInfosInTimeIntervalArrayList.size(); k1++) {
							String str = antiDirectionLinkTravelInfosInTimeIntervalArrayList.get(k1);
							if (k1 != maxIndex && k1 != minIndex) {
								proAntiDirectionLinkTravelInfosInTimeIntervalArrayList.add(str);
							}							
						}
						double antiDirectionExpectedSpeed = CharacteristicCalculation.calculateLinkSpeedExpectation(proAntiDirectionLinkTravelInfosInTimeIntervalArrayList);						
						if (antiDirectionExpectedSpeed != 0) {					
							double antiSpeedStandardDeviation = CharacteristicCalculation.calculateLinkSpeedStandardDeviation(antiDirectionExpectedSpeed, proAntiDirectionLinkTravelInfosInTimeIntervalArrayList);
							tempStartDateTimeStr = endTimeArray[0];
							System.out.print(antiDirectionExpectedSpeed + "," + antiSpeedStandardDeviation + '\n');
							String antiDirectionInfoString = proAntiDirectionLinkTravelInfosInTimeIntervalArrayList.get(0);
							String [] antiDirectionArray = antiDirectionInfoString.split(",");
							int antiDirectionEnterNodeID = Integer.parseInt(antiDirectionArray[1]);
							int antiDirectionExitNodeID = Integer.parseInt(antiDirectionArray[2]);	
							LinkTravelCharacteristic linkTravelCharacteristic = new LinkTravelCharacteristic();
							linkTravelCharacteristic.setWeekdayStr(weekdayStr);
							linkTravelCharacteristic.setLinkID(linkID);
							linkTravelCharacteristic.setDirection(-1);
							linkTravelCharacteristic.setEnterNodeID(antiDirectionEnterNodeID);
							linkTravelCharacteristic.setExitNodeID(antiDirectionExitNodeID);
							
							String timeStr = weekdayStr + startTimeStr;
							linkTravelCharacteristic.setTimeStr(timeStr);
							linkTravelCharacteristic.setSpeedExpectation(antiDirectionExpectedSpeed);
							linkTravelCharacteristic.setSpeedStandDeviation(antiSpeedStandardDeviation);
							speedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
							System.out.print(timeStr + "," + antiDirectionExpectedSpeed + ":" + antiSpeedStandardDeviation + '\n');						
						}
					}					
					tempStartDateTimeStr = endTimeArray[0];
				}				
			}			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 根据目标路段ID、邻接路段ID、目标路段交通流向以及时间，获得交通流方向为目标路段方向 targetDirecton的两路段行程时间比值
	 * @param targetDirection 目标路段方向性
	 * @param link1ID	路段1 ID 目标路段
	 * @param link2ID	路段2 ID
	 * @param expectationStandardDevi	邻接路段速度期望与标准差
	 * @param infosArrayList	所有信息
	 */
	public void obtainTravelTimeRate(int targetDirection, int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList, 
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		try {
			//路段1,2相关信息
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			ArrayList<String> link2InfosArrayList = new ArrayList<String>();
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			MapMatchEdge targetEdge = new MapMatchEdge();
			targetEdge = PubClassSparseData.obtainTargetLinkEdge(link1ID, polylineCollArrayList);
			int targetLinkBeginPointID = targetEdge.getBeginPoint().getNodeID();
			int targetLinkEndPointID = targetEdge.getEndPoint().getNodeID();
			ArrayList<MapMatchEdge> firstLevelConnEdgeArray = targetEdge.getFirstLevelConnEdgeArray();
					
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int linkID = Integer.parseInt(linkIDStr);
				if (linkID == link1ID) {
					link1InfosArrayList.add(str);
				}
				else if (linkID == link2ID) {
					link2InfosArrayList.add(str);
				}
				else {
					continue;
				}
			}
			boolean flag = false;
			for (int i = 0; i < link1InfosArrayList.size(); i++) {
				String str1 = link1InfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				int direction1 = Integer.parseInt(tempStrArray1[3]);
				String timeStr1 = tempStrArray1[4];				
				if (timeStr1.equals(timeStr) && direction1 == targetDirection) {
					for (int j = 0; j < link2InfosArrayList.size(); j++) {
						String str2 = link2InfosArrayList.get(j);
						String []tempStrArray2 = str2.split(",");
						int direction2 = Integer.parseInt(tempStrArray2[3]);
						String timeStr2 = tempStrArray2[4];
						//分别就目标路段交通流为反方向和正方向两种情况求值
						//保证目标路段与邻接路段交通流向都超同一个方向行驶
						//可能出现4种情况的组合，如下所示
						if (timeStr2.equals(timeStr)) {
							//目标路段为负向交通流向
							if (direction1 == PubParameter.antiDirection) {
								for (int k = 0; k < firstLevelConnEdgeArray.size(); k++) {
									MapMatchEdge adjacentEdge = firstLevelConnEdgeArray.get(k);
									int adjacentLinkBeginPointID = adjacentEdge.getBeginPoint().getNodeID();
									int adjacentLinkEndPointID = adjacentEdge.getEndPoint().getNodeID();
									int tempAdjacentLinkID = adjacentEdge.getEdgeID();
									//下游邻接路段交通流向为正向（起点到终点）
									if (tempAdjacentLinkID == link2ID && targetLinkBeginPointID == adjacentLinkBeginPointID && direction2 == PubParameter.sameDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									//下游邻接路段交通流向为负向
									if (tempAdjacentLinkID == link2ID && targetLinkBeginPointID == adjacentLinkEndPointID && direction2 == PubParameter.antiDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									//上游邻接路段交通流向为负向
									if (tempAdjacentLinkID == link2ID && targetLinkEndPointID == adjacentLinkBeginPointID && direction2 == PubParameter.antiDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									//上游邻接路段交通流向为正向
									if (tempAdjacentLinkID == link2ID && targetLinkEndPointID == adjacentLinkEndPointID && direction2 == PubParameter.sameDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									if (flag) {
										break;
									}									
								}
							}
							//正方向行驶
							else {
								for (int k = 0; k < firstLevelConnEdgeArray.size(); k++) {
									MapMatchEdge adjacentEdge = firstLevelConnEdgeArray.get(k);
									int adjacentLinkBeginPointID = adjacentEdge.getBeginPoint().getNodeID();
									int adjacentLinkEndPointID = adjacentEdge.getEndPoint().getNodeID();
									int tempAdjacentLinkID = adjacentEdge.getEdgeID();
									//上游邻接路段交通流向为负向（起点到终点）
									if (tempAdjacentLinkID == link2ID && targetLinkBeginPointID == adjacentLinkBeginPointID && direction2 == PubParameter.antiDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									//上游邻接路段交通流向为正向
									if (tempAdjacentLinkID == link2ID && targetLinkBeginPointID == adjacentLinkEndPointID && direction2 == PubParameter.sameDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									} 
									//下游邻接路段交通流向为正向
									if (tempAdjacentLinkID == link2ID && targetLinkEndPointID == adjacentLinkBeginPointID && direction2 == PubParameter.sameDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									//下游邻接路段交通流向为负向
									if (tempAdjacentLinkID == link2ID && targetLinkEndPointID == adjacentLinkEndPointID && direction2 == PubParameter.antiDirection) {
										flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);										
									}
									if (flag) {
										break;
									}	
								}
							}
							if (flag) {
								break;
							}					
						}
					}
					if (flag) {
						break;
					}
				}					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/***
	 * 此方法仅仅针对目标路段82以及邻接路段76，77，81，88而言
	 * 以上方法为通用的计算方法
	 * @param targetDirection
	 * @param link1ID
	 * @param link2ID
	 * @param timeStr
	 * @param infosArrayList
	 * @param targetAdjacentLinkTravelTime
	 * @param expectationStandardDevi
	 */
	public void obtainTravelTimeRate2222(int targetDirection, int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList, 
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		try {
			//路段1,2相关信息
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			ArrayList<String> link2InfosArrayList = new ArrayList<String>();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int linkID = Integer.parseInt(linkIDStr);
				if (linkID == link1ID) {
					link1InfosArrayList.add(str);
				}
				else if (linkID == link2ID) {
					link2InfosArrayList.add(str);
				}
				else {
					continue;
				}
			}
			boolean flag = false;
			for (int i = 0; i < link1InfosArrayList.size(); i++) {
				String str1 = link1InfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				int direction1 = Integer.parseInt(tempStrArray1[3]);
				String timeStr1 = tempStrArray1[4];			
				if (timeStr1.equals(timeStr) && direction1 == targetDirection) {
					for (int j = 0; j < link2InfosArrayList.size(); j++) {
						String str2 = link2InfosArrayList.get(j);
						String []tempStrArray2 = str2.split(",");
						int direction2 = Integer.parseInt(tempStrArray2[3]);
						String timeStr2 = tempStrArray2[4];
						//同一时间并且同一方向的相关信息    临时处理数据函数   临时处理数据函数   临时处理数据函数
						//仅仅限于路段82的情况
						//负方向行驶
						if (timeStr2.equals(timeStr)) {
							if (direction1 == PubParameter.antiDirection) {
								if (link2ID == 88 && direction2 == PubParameter.antiDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 81 && direction2 == PubParameter.sameDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 77 && direction2 == PubParameter.antiDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 76 && direction2 == PubParameter.sameDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
							}
							//正方向行驶
							else {
								if (link2ID == 88 && direction2 == PubParameter.sameDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 81 && direction2 == PubParameter.antiDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 77 && direction2 == PubParameter.sameDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
								if (link2ID == 76 && direction2 == PubParameter.antiDirection) {
									flag = functionProcess(timeStr1, timeStr2, tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
								}
							}
							if (flag) {
								break;
							}					
						}
					}
					if (flag) {
						break;
					}
				}					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 计算行程时间比值
	 * @param timeStr1	目标路段时刻
	 * @param timeStr2	邻接路段时刻
	 * @param tempStrArray1	目标路段信息
	 * @param tempStrArray2	邻接路段信息
	 * @param targetAdjacentLinkTravelTime	邻接路段信息
	 * @return
	 */
	public boolean functionProcess(String timeStr1, String timeStr2, String []tempStrArray1, String []tempStrArray2,
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		boolean flag = false;
		try {
			double travelTimeRate = 0;
			if (timeStr1.equals(timeStr2)) {
				String speedExpeStr1 = tempStrArray1[5];
				String linkLengthStr1 = tempStrArray1[8];
				String speedExpeStr2 = tempStrArray2[5];
				String speedStandardDeviStr2 = tempStrArray2[6];
				String linkLengthStr2 = tempStrArray2[8];				
				double speedExpe1 = Double.parseDouble(speedExpeStr1);
				double linkLength1 = Double.parseDouble(linkLengthStr1);						
				double speedExpe2 = Double.parseDouble(speedExpeStr2);
				double speedStandardDevi2 = Double.parseDouble(speedStandardDeviStr2);
				double linkLength2 = Double.parseDouble(linkLengthStr2);
				if (Math.abs(speedExpe1) > 0.000001 && Math.abs(speedExpe2) > 0.000001) {
					double temp1 = linkLength1/speedExpe1;//目标路段行程时间
					double temp2 = linkLength2/speedExpe2;//邻接路段行程时间
					if (Math.abs(temp2) > 0.000001) {
						double tempTravelTimeRate = temp1/temp2;
						travelTimeRate = Double.parseDouble(String.format("%.4f", tempTravelTimeRate));
						double ttemp1 = Double.parseDouble(String.format("%.4f", temp1));
						double ttemp2 = Double.parseDouble(String.format("%.4f", temp2));
						targetAdjacentLinkTravelTime[0] = ttemp1;
						targetAdjacentLinkTravelTime[1] = ttemp2;
						targetAdjacentLinkTravelTime[2] = travelTimeRate;
						double prospeedExpe2 = Double.parseDouble(String.format("%.4f", speedExpe2));
						double prospeedStandardDevi2 = Double.parseDouble(String.format("%.4f", speedStandardDevi2));
						expectationStandardDevi[0] = prospeedExpe2;
						expectationStandardDevi[1] = prospeedStandardDevi2;
						flag = true;
					}
				}
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 根据路段ID获得行程时间
	 */
	public double obtainTravelTimeAccordLinkID(int link1ID, String timeStr, ArrayList<String> infosArrayList) {
		double travelTime = 0;
		try {
			//路段1,2相关信息
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int linkID = Integer.parseInt(linkIDStr);
				if (linkID == link1ID) {
					link1InfosArrayList.add(str);
				}
				else {
					continue;
				}
			}
			boolean flag = false;
			for (int i = 0; i < link1InfosArrayList.size(); i++) {
				String str1 = link1InfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String timeStr1 = tempStrArray1[1];
				if (timeStr1.equals(timeStr)) {
					//同一时间的相关信息
					String speedExpeStr1 = tempStrArray1[2];
					String linkLengthStr1 = tempStrArray1[5];							
					double speedExpe1 = Double.parseDouble(speedExpeStr1);
					double linkLength1 = Double.parseDouble(linkLengthStr1);							
					if (Math.abs(speedExpe1) > 0.000001) {
						double tempTravelTime = linkLength1/speedExpe1;								
						travelTime = Double.parseDouble(String.format("%.4f", tempTravelTime));
						flag = true;
						break;
					}					
				}					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return travelTime;
	}
	
	/**
	 * 根据ID、时刻获得期望值、标准差值
	 * @param link1ID
	 * @param timeStr
	 * @param infosArrayList
	 * @param expectationStandardDevi	速度期望、速度方差
	 * @return
	 */
	public void obtainExpectationStandDeviAccordLinkID (int link1ID, String timeStr, ArrayList<String> infosArrayList, double []expectationStandardDevi) {
		try {
			//路段1相关信息
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int direction = Integer.parseInt(tempStrArray[1]);
				int linkID = Integer.parseInt(linkIDStr);
				//-1的方向  临时 -1的方向  临时 -1的方向  临时
				if (linkID == link1ID && direction  == -1) {
					link1InfosArrayList.add(str);
				}
				else {
					continue;
				}
			}
			boolean flag = false;
			for (int i = 0; i < link1InfosArrayList.size(); i++) {
				String str1 = link1InfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String timeStr1 = tempStrArray1[2];
				if (timeStr1.equals(timeStr)) {
					//同一时间的相关信息
					String speedExpeStr1 = tempStrArray1[3];
					String speedStandardDeviStr1 = tempStrArray1[4];
					double speedExpe1 = Double.parseDouble(speedExpeStr1);
					double speedStandardDevi1 = Double.parseDouble(speedStandardDeviStr1);
					if (Math.abs(speedExpe1) > 0.000001 && Math.abs(speedStandardDevi1) > 0.000001) {							
						double speedExpectation = Double.parseDouble(String.format("%.4f", speedExpe1));
						double standardDevi = Double.parseDouble(String.format("%.4f", speedStandardDevi1));
						expectationStandardDevi[0] = speedExpectation;
						expectationStandardDevi[1] = standardDevi;
						flag = true;
						break;
					}					
				}					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 求两路段之间某一时间的pearson系数
	 * @param linkID1
	 * @param linkID2
	 * @param timeStr	时间点,格式为10:30:00
	 */
	public double pearsonCorrCalculation (int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList) {
		double pearsonCorr = 0;
		try {			
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			//路段1,2相关信息
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			ArrayList<String> link2InfosArrayList = new ArrayList<String>();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int linkID = Integer.parseInt(linkIDStr);
				if (linkID == link1ID) {
					link1InfosArrayList.add(str);
				}
				else if (linkID == link2ID) {
					link2InfosArrayList.add(str);
				}
				else {
					continue;
				}
			}
			boolean flag = false;
			for (int i = 0; i < link1InfosArrayList.size(); i++) {
				String str1 = link1InfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String timeStr1 = tempStrArray1[1];
				if (timeStr1.equals(timeStr)) {
					for (int j = 0; j < link2InfosArrayList.size(); j++) {
						String str2 = link2InfosArrayList.get(j);
						String []tempStrArray2 = str2.split(",");
						String timeStr2 = tempStrArray2[1];
						//同一时间的相关信息
						if (timeStr1.equals(timeStr2)) {
							String speedExpeStr1 = tempStrArray1[2];
							String speedDeviStr1 = tempStrArray1[3];
							String linkDegreeStr1 = tempStrArray1[4];
							String speedExpeStr2 = tempStrArray2[2];
							String speedDeviStr2 = tempStrArray2[3];
							String linkDegreeStr2 = tempStrArray2[4];
							double speedExpe1 = Double.parseDouble(speedExpeStr1);
							double speedDevi1 = Double.parseDouble(speedDeviStr1);
							int linkDegree1 = Integer.parseInt(linkDegreeStr1);
							double speedExpe2 = Double.parseDouble(speedExpeStr2);
							double speedDevi2 = Double.parseDouble(speedDeviStr2);
							int linkDegree2 = Integer.parseInt(linkDegreeStr2);
							pearsonCorr = assistFunctionNeuralNetwork.calculatePearsonCorrelation(speedExpe1, speedDevi1, linkDegree1, speedExpe2, speedDevi2, linkDegree2);
							flag = true;
							break;
						}
					}
					if (flag) {
						break;
					}
				}					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return pearsonCorr;
	}
	
	/**
	 * 获得目标路段分别在工作日（周一、周二……五）每天半小时内的数据，并写入Txt文件
	 * @param linkID
	 */
	public void obtainLinkTravelInfosOnWeekdays(int linkID) {
		try {			
			System.out.print("信息开始写入txt文件：" + '\n');
			String filePathStr = "C:\\travelTimeProcess\\workDayTravelTimeLink" + linkID + ".txt";
			String headDescriptionStr = "weekDayTime" + "," + "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "direction" 
			+ "," + "taxiID" + "," + "timeStr" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";			
			FileOperateFunction.writeHeadDescriptionToTxtFile(filePathStr, headDescriptionStr);
			
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);	
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();//目标路段通行信息
			//目标路段周一、周二等的数据分别合并、分别合并分别合并
			ArrayList<String> linkmondayArrayList = new ArrayList<String>();//周一
			ArrayList<String> linktuesdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> linkwednesdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> linkthursdayArrayList = new ArrayList<String>();
			ArrayList<String> linkfridayArrayList = new ArrayList<String>();
			//获得目标路段通行信息
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[5].substring(0,10);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
				Date date = simpleDateFormat.parse(tempDateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
			    	linkmondayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
			    	linktuesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
			    	linkwednesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
			    	linkthursdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
			    	linkfridayArrayList.add(str);
				}				
			}						
			int timeInterval = 1800;//统计每隔30min的路段出租车通行时间
			ArrayList<String> weekInfosArraylist = new ArrayList<String>();
			String weekdayStr = "";
			for (int i = 0; i < 5; i++) {
				String startDateTimeStr = "2014-06-01 00:00:00";
				String endDateTimeStr = "2014-06-02 00:00:00";
				String tempStartDateTimeStr = startDateTimeStr;
				if (i == 0) {
					weekInfosArraylist = linkmondayArrayList;
					weekdayStr = "Mond";
				}
				if (i == 1) {
					weekInfosArraylist = linktuesdayArrayList;
					weekdayStr = "Tues";
				}
				if (i == 2) {
					weekInfosArraylist = linkwednesdayArrayList;
					weekdayStr = "Wedn";
				}
				if (i == 3) {
					weekInfosArraylist = linkthursdayArrayList;
					weekdayStr = "Thur";
				}
				if (i == 4) {
					weekInfosArraylist = linkfridayArrayList;
					weekdayStr = "Frid";
				}
				//因为有重复的数据，在此将重复的数据剔除掉，保证记录的唯一性
				//相同记录基本是直接相连的,只跟下一条比较
				ArrayList<String> processWeekInfosArraylist = new ArrayList<String>();
				for (int j = 0; j < weekInfosArraylist.size(); j++) {
					String str1 = weekInfosArraylist.get(j);
					processWeekInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[4];
					String timeStr1 = strArray1[5];					
					if (j != weekInfosArraylist.size() - 1) {
						String str2 = weekInfosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[4];
						String timeStr2 = strArray2[5];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//跳过相同记录							
						}
					}					
				}
							
				while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
					String[] endTimeArray = new String[1];
					PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
					String startTimeStr = tempStartDateTimeStr.substring(11);
					String endTimeStr = endTimeArray[0].substring(11);
					if (startTimeStr.equals("23:30:00")) {
						endTimeStr = "23:59:59";
					}
					ArrayList<String> linkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(processWeekInfosArraylist, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);				
					ArrayList<String> processlinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					for (int j = 0; j < linkTravelInfosInTimeIntervalArrayList.size(); j++) {
						String str = linkTravelInfosInTimeIntervalArrayList.get(j);
						String tStr = weekdayStr + startTimeStr + "," + str;
						processlinkTravelInfosInTimeIntervalArrayList.add(tStr);
					}					
					FileOperateFunction.writeStringArraylistToEndTxtFile(filePathStr, processlinkTravelInfosInTimeIntervalArrayList);
					tempStartDateTimeStr = endTimeArray[0];
				}				
			}			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	
	
	/**
	 * txt中数据按照时间排序
	 * 1.obtain与某路段相关数据
	 * 2.获得路段半小时内数据
	 * 3.
	 */
	public void sortByTime() {
		try {
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);	
			ArrayList<String> linkTravelInfos = new ArrayList<String>();//目标路段通行信息
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			for (int i = 0; i < 93; i++) {
				int linkID = i + 1;
				assistFunctionNeuralNetwork.obtainLinkTravelInfosAccordLinkID(infosArrayList, linkTravelInfos, linkID);
				
			}
		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/**
	 * 1.目标路段相关数据按照周一到周五分别聚类,并获得每半小时数据
	 * 2.数据写入txt文件
	 * @param linkID
	 * @param linkTravelInfos
	 * @param filePathStr 文件路径
	 */
	public void obtainTimeIntervalInfos(int linkID, ArrayList<String> linkTravelInfos, String filePathStr){
		try {
			System.out.print("信息开始写入txt文件：" + '\n');		
			//目标路段周一到周五的数据分别合并、分别合并分别合并
			ArrayList<String> linkmondayArrayList = new ArrayList<String>();//周一
			ArrayList<String> linktuesdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> linkwednesdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> linkthursdayArrayList = new ArrayList<String>();
			ArrayList<String> linkfridayArrayList = new ArrayList<String>();
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			//获得目标路段通行信息
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[5].substring(0,10);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
				Date date = simpleDateFormat.parse(tempDateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
			    	linkmondayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
			    	linktuesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
			    	linkwednesdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
			    	linkthursdayArrayList.add(str);
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
			    	linkfridayArrayList.add(str);
				}				
			}						
			int timeInterval = 1800;//统计每隔30min的路段出租车通行时间
			ArrayList<String> weekInfosArraylist = new ArrayList<String>();
			String weekdayStr = "";
			for (int i = 0; i < 5; i++) {
				String startDateTimeStr = "2014-06-01 00:00:00";
				String endDateTimeStr = "2014-06-02 00:00:00";
				String tempStartDateTimeStr = startDateTimeStr;
				if (i == 0) {
					weekInfosArraylist = linkmondayArrayList;
					weekdayStr = "Mond";
				}
				if (i == 1) {
					weekInfosArraylist = linktuesdayArrayList;
					weekdayStr = "Tues";
				}
				if (i == 2) {
					weekInfosArraylist = linkwednesdayArrayList;
					weekdayStr = "Wedn";
				}
				if (i == 3) {
					weekInfosArraylist = linkthursdayArrayList;
					weekdayStr = "Thur";
				}
				if (i == 4) {
					weekInfosArraylist = linkfridayArrayList;
					weekdayStr = "Frid";
				}
				//因为有重复的数据，在此将重复的数据剔除掉，保证记录的唯一性
				//相同记录基本是直接相连的,只跟下一条比较
				ArrayList<String> processWeekInfosArraylist = new ArrayList<String>();
				for (int j = 0; j < weekInfosArraylist.size(); j++) {
					String str1 = weekInfosArraylist.get(j);
					processWeekInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[4];
					String timeStr1 = strArray1[5];					
					if (j != weekInfosArraylist.size() - 1) {
						String str2 = weekInfosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[4];
						String timeStr2 = strArray2[5];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//跳过相同记录							
						}
					}					
				}
							
				while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
					String[] endTimeArray = new String[1];
					PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
					String startTimeStr = tempStartDateTimeStr.substring(11);
					String endTimeStr = endTimeArray[0].substring(11);
					if (startTimeStr.equals("23:30:00")) {
						endTimeStr = "23:59:59";
					}
					ArrayList<String> linkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(processWeekInfosArraylist, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);
					ArrayList<String> processlinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					for (int j = 0; j < linkTravelInfosInTimeIntervalArrayList.size(); j++) {
						String str = linkTravelInfosInTimeIntervalArrayList.get(j);
						String tStr = weekdayStr + startTimeStr + "," + str;
						processlinkTravelInfosInTimeIntervalArrayList.add(tStr);
					}					
					FileOperateFunction.writeStringArraylistToEndTxtFile(filePathStr, processlinkTravelInfosInTimeIntervalArrayList);
					tempStartDateTimeStr = endTimeArray[0];
				}				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		
	}
	
	
}
