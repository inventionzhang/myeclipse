package org.lmars.network.dataProcess;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.TimeOperateFunction;
import org.lmars.network.util.setOperateFunction;

public class DataPreprocess {
	
	public static void main(String[] args) {
//		new DataPreprocess().GBRTDataProcess();
		
		String filePathStr = "G:\\travelTimeProcess\\GBRTDataInput2.txt";
		ArrayList<String> infosArrayList = new ArrayList<String>();
		FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
		TimeOperateFunction.bubbleSortAccordTimeCommonMethod(0, infosArrayList);
		String outFilePathStr = "G:\\travelTimeProcess\\GBRTDataInput3.txt";
		String headDescriptionStr = "dateTimeStr" + "," + "linkID" + "," + "timeStr" + "," +"digitWeek" + "," + "periodOfDay" + "," + "UpHTT1" + "," + "UpHTT2" + "," + "deltUpHTT" + "," +
				"UpRTT1" + "," + "UpRTT2" + "," + "deltUpRTT" + "," + "targetHTT1" + "," + "targetHTT2" + ","  + "deltTargetHTT" + "," + "targetRTT1" + "," +
				"targetRTT2" + ","  + "deltTargetRTT" + "," + "downHTT1" + "," + "downHTT2" + "," + "deltDownHTT" + ","  + "targetRTT(t)"+ ","  + 
				"targetHTT(t)" + "\r\n";
		FileOperateFunction.writeStringArraylistToTxtFile(outFilePathStr, headDescriptionStr, infosArrayList);
		System.out.print("done!");
	}
	
	/**
	 * 梯度提升算法数据之预处理
	 * 1.获得特定路段、特定方向、特定时段内的数据，如：获得5、6、7三个月的数据：
	 * 2.提取各个路段间的特征
	 */
	public void GBRTDataProcess(){
		String startTimeStr = "2014-05-01 00:00:00";
		String endTimeStr = "2014-07-30 00:00:00";
		int timeInterval = 1800;
//		//获得特定路段、特定方向、特定时段内的数据
//		getSpecialTimePeriodDataAccordLinkIDAndDirection(startTimeStr, endTimeStr);
//		//提取各个路段间的特征
//		String filePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime88MayJuneJuly.txt";//按照时间顺序排列的通行信息
//		String outputFilePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink88MayJuneJuly.txt";//提取的特征
//		int upStreamLink = 88;//上游路段编号
//		extractCharacteristicAccordTimeInterval(timeInterval,startTimeStr,endTimeStr, filePathStr,outputFilePathStr, upStreamLink);
//		
//		filePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime82MayJuneJuly.txt";//按照时间顺序排列的通行信息
//		outputFilePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink82MayJuneJuly.txt";//提取的特征
//		int targetLinkID = 82;
//		extractCharacteristicAccordTimeInterval(timeInterval,startTimeStr,endTimeStr, filePathStr,outputFilePathStr, targetLinkID);
//		
//		filePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime77MayJuneJuly.txt";//按照时间顺序排列的通行信息
//		outputFilePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink77MayJuneJuly.txt";//提取的特征
//		int downStreamLinkID = 77;
//		extractCharacteristicAccordTimeInterval(timeInterval,startTimeStr,endTimeStr, filePathStr,outputFilePathStr, downStreamLinkID);
		EnsembleInputUpDownstreamTargetLink(82, -1, 88, -1, 77, -1);//获得目标路段、上游路段、下游路段网络集成输入
		
		
	}
	
	
	/**
	 * 根据路段ID、方向以及起止时间获得特定时段的数据
	 * 
	 */
	public void getSpecialTimePeriodDataAccordLinkIDAndDirection(String startTimeStr, String endTimeStr){
		String filePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
//		String filePathStr = "G:\\travelTimeProcess\\tempWorkDayTravelTime.txt";
		ArrayList<String> infosArrayList = new ArrayList<String>();		
		FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
		int linkID = 82;
		int direction = -1;
		getSpecialTimePeriodData(linkID, direction, startTimeStr, endTimeStr, infosArrayList);
		linkID = 88;
		getSpecialTimePeriodData(linkID, direction, startTimeStr, endTimeStr, infosArrayList);	
		linkID = 77;
		getSpecialTimePeriodData(linkID, direction, startTimeStr, endTimeStr, infosArrayList);
		System.out.print("特定时段数据获取结束！");
	}
	
	
	/**
	 * 2016-1-22
	 * 提取某段时间的数据，并按照时间先后顺序排序
	 * 1. 根据路段id、方向、时间提取数据
	 * 2. 按照时间先后顺序排序
	 * @param linkID	
	 * @param direction
	 * @param startTimeStr	开始时间
	 * @param endTimeStr	结束时间
	 * @param infosArrayList
	 */
	public void getSpecialTimePeriodData(int linkID, int direction, String startTimeStr, String endTimeStr, ArrayList<String> infosArrayList) {				
		ArrayList<String> targetTimeInfosArrayList = new ArrayList<String>();
		obtainTargetInfosBetweenTimeDate(linkID, direction, startTimeStr, endTimeStr, infosArrayList, targetTimeInfosArrayList);		
		//每隔30min的时间间隔
		String startDateTimeStr = startTimeStr;
		String endDateTimeStr = endTimeStr;
		String tempStartDateTimeStr = startDateTimeStr;
		int timeInterval = 1800;//统计每隔30min
		String tempFilePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + ".txt";
		String headDescriptionStr = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," +
				"startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
		FileOperateFunction.writeHeadDescriptionToTxtFile(tempFilePathStr, headDescriptionStr);
		while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
			System.out.print(tempStartDateTimeStr + '\n');
			String[] endTimeArray = new String[1];
			PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
			String tempEndDateTimeStr = endTimeArray[0];
			ArrayList<String> tempTargetTimeInfosArrayList = new ArrayList<String>();
			obtainTargetInfosBetweenTimeDate(linkID, direction, tempStartDateTimeStr, tempEndDateTimeStr, targetTimeInfosArrayList, tempTargetTimeInfosArrayList);
			TimeOperateFunction.sortAccordTime(tempTargetTimeInfosArrayList);
			if (tempTargetTimeInfosArrayList.size() != 0) {
				FileOperateFunction.writeStringArraylistToEndTxtFile(tempFilePathStr, tempTargetTimeInfosArrayList);
			}			
			tempStartDateTimeStr = endTimeArray[0];			
		}		
		System.out.print("done!");
	}
	
	/**
	 * 根据路段ID获得位于起止时间之间的数据
	 * @param startTimeStr	起始时间：时间格式："2014-06-01 00:00:00"
	 * @param endTimeStr	结束时间
	 * @param infosArrayList	所有信息
	 * @param targetTimeInfosArrayList	目标路段信息
	 */
	public void obtainTargetInfosBetweenTimeDate(int linkID, int direction, String startTimeStr, String endTimeStr, ArrayList<String> infosArrayList, 
			ArrayList<String> targetTimeInfosArrayList){
		try {
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[]tempArray = str.split(",");
				String linkIDStr = tempArray[0];
				String directionStr = tempArray[3];
				int tempLinkID = Integer.parseInt(linkIDStr);
				int tempDirection = Integer.parseInt(directionStr);
				String dateTimeStr = tempArray[5];
				if (tempLinkID == linkID && tempDirection == direction && PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, dateTimeStr)) {
					targetTimeInfosArrayList.add(str);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}	
	
	
	
	/**
	 * 按照时间加权，提取该时间间隔内特征
	 * @param timeInterval	以秒为单位，如30min,则取值为1800s
	 */
	public void extractCharacteristicAccordTimeInterval(int timeInterval, String startTimeStr,String endTimeStr, String filePathStr, 
			String outputFilePathStr, int linkID){
		try {
//			int linkID = 82;
//			int direction = -1;
//			String filePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime88MayJuneJuly.txt";//按照时间顺序排列的通行信息
//			String outputFilePathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink88MayJuneJuly.txt";//提取的特征
			
			int direction = -1;						
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
			String startDateTimeStr = startTimeStr;
			String endDateTimeStr = endTimeStr;
			String tempStartDateTimeStr = startDateTimeStr;			
			ArrayList<String[]> allInputArrayList = new ArrayList<String[]>();//所有输入信息			
			while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
				System.out.print(tempStartDateTimeStr + '\n');
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
				String tempEndDateTimeStr = endTimeArray[0];
				ArrayList<String> tempTargetTimeInfosArrayList = new ArrayList<String>();
				obtainTargetInfosBetweenTimeDate(linkID, direction, tempStartDateTimeStr, tempEndDateTimeStr, 
						infosArrayList, tempTargetTimeInfosArrayList);
				String tempInputStr[] = new String[9];
				if (tempTargetTimeInfosArrayList.size() != 0) {
					double speedExpectedSpeed = CharacteristicCalculation.calculateLinkSpeedExpectation(tempTargetTimeInfosArrayList);//速度期望
					String str = tempTargetTimeInfosArrayList.get(0);//以第一个记录时刻为准进行计算
					String[]tempArrayStr = str.split(",");
					String linkIDStr = tempArrayStr[0];
					String enterNodeIDStr = tempArrayStr[1];
					String exitNodeIDStr = tempArrayStr[2];
					String directionStr = tempArrayStr[3];
					String dateStr = tempArrayStr[5];
					if (dateStr.equals("2014-07-21 14:30:00")) {
						System.out.print("");
					}
					String tempDateStr = tempArrayStr[5].substring(0,10);
					double travelTime = Double.parseDouble(tempArrayStr[6]);
					double speed = Double.parseDouble(tempArrayStr[7]);
					double linkLength = travelTime * speed;
					double tempAverTravTime = linkLength/speedExpectedSpeed;
					double averTravTime = Double.parseDouble(String.format("%.2f", tempAverTravTime));
					String averageTravelTimeStr = String.valueOf(averTravTime);
					String [] timeStrAndTimePeriod = new String[2];//存储时间和当前时间一天中的时段
					timeStrAndTimePeriod = obtainTimePeriodOfDay(dateStr, timeInterval);
					String periodOfDayStr = timeStrAndTimePeriod[1];										
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");  
					Date date = simpleDateFormat.parse(tempDateStr); 
				    Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    String weekdayStr = "";
				    String digitWeek = "-1";
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
				    	weekdayStr = "Mond";
				    	digitWeek = "1";
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
				    	weekdayStr = "Tues";
				    	digitWeek = "2";
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
				    	weekdayStr = "Wedn";
				    	digitWeek = "3";
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
				    	weekdayStr = "Thur";
				    	digitWeek = "4";
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
				    	weekdayStr = "Frid";
				    	digitWeek = "5";
					}				    
				    tempInputStr[0] = linkIDStr;
				    tempInputStr[1] = enterNodeIDStr;
				    tempInputStr[2] = exitNodeIDStr;
				    tempInputStr[3] = directionStr;
				    tempInputStr[4] = timeStrAndTimePeriod[0];//日期
				    tempInputStr[5] = weekdayStr;//周几
				    tempInputStr[6] = digitWeek;
				    tempInputStr[7] = periodOfDayStr;//一天中时段
				    tempInputStr[8] = averageTravelTimeStr;
				    allInputArrayList.add(tempInputStr);
				}				
				tempStartDateTimeStr = endTimeArray[0];			
			}
			String headheadDescriptionStr = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "date" + "," +
					"week" + "," + "digitWeek" + "," + "periodOfDay" + "," +"averageTravelTime" + "\r\n";
					FileOperateFunction.writeStringArrayToTxtFile(outputFilePathStr, headheadDescriptionStr, allInputArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 根据特定时间间隔判断该时间timeString为一天中的哪个时段
	 * 00:00:00	代表第一个时段
	 * 00:30:00	代表第二个时段
	 * 
	 * 默认为每隔30min的时间间隔，1800s
	 * 简单写死，30min时间间隔
	 * @param timeString	时间，格式为：2014-06-01 00:00:00
	 * @param timeInterval	时间间隔
	 * return timeStrAndTimePeriod	时间和所属一天中时段
	 */
	public String[] obtainTimePeriodOfDay(String timeString, int timeInterval){
		String[] timeStrAndTimePeriod = new String[2];//时间和所属一天中时段
		int timeOfDay = -1;
		try {
			//每隔30min的时间间隔
			ArrayList<String> timeArraylist = new ArrayList<String>();
			String startDateTimeStr = "2014-06-01 00:00:00";
			String endDateTimeStr = "2014-06-02 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
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
			String dayStr = timeString.substring(0, 11);
			String hourStr = timeString.substring(11, 13);
			String minStr = timeString.substring(14, 16);
			int minInt = Integer.valueOf(minStr);
			if (minInt < 30) {
				minInt = 0;
				minStr = "00";
			}
			else {
				minInt = 30;
				minStr = "30";
			}
			String proDateTimeStr = dayStr + hourStr + ":" + minStr + ":" + "00";
			String proTimeStr = hourStr + ":" + minStr + ":" + "00";		
			String tempTimeStr = "";
			for (int j = 0; j < timeStrArray.length; j++) {
				System.out.print(j + ":" + timeStrArray.length + '\n');
				tempTimeStr = timeStrArray[j];
				if (tempTimeStr.equals(proTimeStr)) {
					timeOfDay = j + 1;//时段
					timeStrAndTimePeriod[0] = proDateTimeStr;
					timeStrAndTimePeriod[1] = Integer.toString(timeOfDay);
					break;
				}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeStrAndTimePeriod;		
	}

	
	/**
	 * 2015-11-15
	 * 获得路段linkID集成神经网络的输入信息
	 * 输入信息包括：
	 * 1.周工作日：1-5
	 * 2.时段：一天中哪一个时段1-48
	 * 3.当前时刻前一时段行程时间
	 * 4.当前时刻前一时段之前时段行程时间
	 * 5.前两个时段行程时间之差
	 * 输出：当前时刻行程时间
	 * @param linkID
	 * @param direction
	 */
	public void ANNEnsembleInput(int linkID, int direction){
		try {
			String ANNInputPathStr = "G:\\travelTimeProcess\\EnsembleInput" + linkID + ".txt";
//			String targetAdjacentLinkTravelTimeFilePathStr = "G:\\travelTimeProcess\\linkAdjacentTravelTimeInfos" + linkID + ".txt"; 
//			String headDescriptionStr = "timeStr" + "," + "adjacentLinkID" + "," + "linkTravelTime" + "," + "adjacentLinkTravelTime" + "," + "travelTimeRate" + "\r\n";
//			FileOperateFunction.writeHeadDescriptionToTxtFile(targetAdjacentLinkTravelTimeFilePathStr, headDescriptionStr);
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
			int continuousTimeInterval = 30 * 60;//连续时间间隔
			int inputWeek = -1;// 对应一周中那一天
			//工作日，周一至周五
			for (int i = 0; i < 5; i++) {
				if (i == 0) {
					weekInfosArrayList = mondayInfosArrayList;
					weekdayStr = "Mond";
					inputWeek = 1;
				}
				if (i == 1) {
					weekInfosArrayList = tuesInfosdayArrayList;
					weekdayStr = "Tues";
					inputWeek = 2;
				}
				if (i == 2) {
					weekInfosArrayList = wednesInfosdayArrayList;
					weekdayStr = "Wedn";
					inputWeek = 3;
				}
				if (i == 3) {
					weekInfosArrayList = thursdayInfosArrayList;
					weekdayStr = "Thur";
					inputWeek = 4;
				}
				if (i == 4) {
					weekInfosArrayList = fridayInfosArrayList;
					weekdayStr = "Frid";
					inputWeek = 5;
				}
				ArrayList<String> targetLinkDirectionWeekInfosArrayList = new ArrayList<String>();//目标路段某方向的某天信息
				for (int j = 0; j < weekInfosArrayList.size(); j++) {
					String str = weekInfosArrayList.get(j);
					String []tempStrArray = str.split(",");
					String linkIDStr = tempStrArray[0];
					String directionStr = tempStrArray[3];
					int tempLinkID = Integer.parseInt(linkIDStr);
					int tempDirection = Integer.parseInt(directionStr);
					if (tempLinkID == linkID && tempDirection == direction) {
						targetLinkDirectionWeekInfosArrayList.add(str);			
					}
				}
				TimeOperateFunction.sortAccordTime(targetLinkDirectionWeekInfosArrayList);// 按照时间顺序排序
				ArrayList<double[]> inputArrayList = new ArrayList<double[]>();
				double inputTime = 0;// 对应一天中的时段
				double inputPreTravelTime = -1;// 当前时刻前一时刻的行程时间
				double inputPrePreTravelTime = -1;// 当前时刻前两时刻的行程时间
				double inputTravelTimeDifference = -1;
				double outputTravelTime = -1;
				//当前时刻以及前两个邻接时段都有信息
				//暂时不考虑太多，假定当前时刻及前两个邻接时段都有信息
				int targetCount = targetLinkDirectionWeekInfosArrayList.size();
				for (int j = 0; j < targetCount; j++) {
					String str = targetLinkDirectionWeekInfosArrayList.get(j);
					String []tempStrArray = str.split(",");
					String tempTimeStr = tempStrArray[4];// 时间
					String proTimeStr = tempTimeStr.substring(4);					
					String tempSpeedStr = tempStrArray[5];// 速度
					double tempSpeedDouble = Double.parseDouble(tempSpeedStr);
					double speedDouble = Double.parseDouble(String.format("%.2f", tempSpeedDouble));
					String tempLinkLength = tempStrArray[8];//路段长度
					double tempLinkLengthDouble = Double.parseDouble(tempLinkLength);
					double linkLengthDouble = Double.parseDouble(String.format("%.2f", tempLinkLengthDouble));
					double tempTravelTime = linkLengthDouble/speedDouble;						
					outputTravelTime = Double.parseDouble(String.format("%.2f", tempTravelTime));
					
					for (int k = 0; k < timeArraylist.size(); k++) {
						String temp = timeArraylist.get(k);
						if (proTimeStr.equals(temp)) {
//							inputTime = (k + 1)/10.0;
							inputTime = (k + 1);
							break;
						}
					}
					if (j == 0) {
						String preStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 2);
						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
					}
					else if (j == 1) {
						String preStr = targetLinkDirectionWeekInfosArrayList.get(0);
						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(targetCount - 1);
						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));
					}
					else {
						String preStr = targetLinkDirectionWeekInfosArrayList.get(j - 1);
						String prePreStr = targetLinkDirectionWeekInfosArrayList.get(j - 2);
						double tempInputPreTravelTime = obtainPreviousTravelTime(preStr);						
						double tempInputPrePreTravelTime = obtainPreviousTravelTime(prePreStr);
						inputPreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPreTravelTime));
						inputPrePreTravelTime = Double.parseDouble(String.format("%.2f", tempInputPrePreTravelTime));						
					}
					double tempInputTravelTimeDifference = inputPreTravelTime - inputPrePreTravelTime;
					inputTravelTimeDifference = Double.parseDouble(String.format("%.2f", tempInputTravelTimeDifference));
					double[] tempInput = new double[6];
					tempInput[0] = inputWeek;//周几
					tempInput[1] = inputTime;//半小时对应一个值,1到48,00:00:00用1代表			
					tempInput[2] = inputPreTravelTime;//当前时刻前一时段的行程时间
					tempInput[3] = inputPrePreTravelTime;//当前时刻前一时段之前时段的行程时间
					tempInput[4] = inputTravelTimeDifference;//两个时段行程时间之差
					tempInput[5] = outputTravelTime;//当前时刻行程时间
					allInputArrayList.add(tempInput);
				}			
			}			
			String headheadDescriptionStr = "workday" + "," + "time" + "," + "TT(t-1)" + "," + "TT(t-2)" + "," + "delt(t-1)" + "," +
					"TT(t)" + "\r\n";
			FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, allInputArrayList);
//			new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 2015-11-15
	 * 获得路段与上游路段时空关联关系
	 * 输入信息包括：
	 * 1.周工作日：1-5
	 * 2.时段：一天中哪一个时段1-48, 如00:00:00对应1
	 * 3.目标路段当前时刻前一时段行程时间
	 * 4.目标路段当前时刻前一时段之前时段行程时间
	 * 5.目标路段前两个时段行程时间之差
	 * 6.邻接路段当前时刻前一时段行程时间
	 * 7.邻接路段当前时刻前一时段之前时段行程时间
	 * 8.邻接路段前两个时段行程时间之差
	 * 输出：目标路段当前时刻行程时间
	 * @param targetLinkID	目标路段
	 * @param targetDirection
	 * @param upstreamLinkID	上游路段
	 * @param upstreamDirection
	 * @param downstreamLinkID	下游路段
	 * @param downstreamDirection
	 */
	public void EnsembleInputUpDownstreamTargetLink(int targetLinkID, int targetDirection, int upstreamLinkID, 
			int upstreamDirection, int downstreamLinkID, int downstreamDirection){
		try {
			String ANNInputPathStr = "G:\\travelTimeProcess\\GBRTDataInput2.txt";
			//历史大数据输入路径
			String targetLinkIDHistInputPathStr = "G:\\travelTimeProcess\\compensatedSpeedExpectationStandarDeviation" + targetLinkID + ".txt";
			String upstreamLinkIDHistInputPathStr = "G:\\travelTimeProcess\\compensatedSpeedExpectationStandarDeviation" + upstreamLinkID + ".txt";
			String downstreamLinkIDHistInputPathStr = "G:\\travelTimeProcess\\compensatedSpeedExpectationStandarDeviation" + downstreamLinkID + ".txt";
			//实时信息
			String targetLinkIDRealInputPathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink82MayJuneJuly.txt";
			String upstreamLinkIDRealInputPathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink88MayJuneJuly.txt";
			String downstreamLinkIDRealInputPathStr = "G:\\travelTimeProcess\\targetWorkDayTravelTimeCharacteristicLink77MayJuneJuly.txt";
			
			//每隔30min的时间间隔
			//5点到24点间的信息作为研究目标信息
			int timePeriodCount = 38;//一天中的时段数目
			ArrayList<String> timeArraylist = new ArrayList<String>();
			String startDayTimeInstantStr = "05:00:00";
			String endDayTimeInstantStr = "23:59:59";
			String startDateTimeStr = "2014-06-01 05:00:00";
			String endDateTimeStr = "2014-06-02 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
			int timeIntervalConstant = 1800;//统计每隔30min
			while (!tempStartDateTimeStr.equals(endDateTimeStr)) {			
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeIntervalConstant, endTimeArray);
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
			
			//历史大数据的处理
			//历史大数据的处理
			ArrayList<String> histInfosArrayList = new ArrayList<String>();//历史大数据中的速度期望、速度方差
			
//			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");	
//			FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
			
			//获得三个路段的行程时间
			FileOperateFunction.readFromTxtFile(targetLinkIDHistInputPathStr, histInfosArrayList);
			FileOperateFunction.readFromTxtFile(upstreamLinkIDHistInputPathStr, histInfosArrayList);
			FileOperateFunction.readFromTxtFile(downstreamLinkIDHistInputPathStr, histInfosArrayList);
			
			//历史大数据按照天分类
			ArrayList<String> histmondayInfosArrayList = new ArrayList<String>();//周一
			ArrayList<String> histtuesInfosdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> histwednesInfosdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> histthursdayInfosArrayList = new ArrayList<String>();
			ArrayList<String> histfridayInfosArrayList = new ArrayList<String>();
			ArrayList<String> histweekInfosArrayList = new ArrayList<String>();//按照周分类后的信息
			String weekdayStr = "";
			int dataCount = histInfosArrayList.size();
			for (int i = 0; i < histInfosArrayList.size(); i++) {
				System.out.print("按照周期性分类：" + i + ":" + dataCount + '\n');
				String str = histInfosArrayList.get(i);
				String[]tempArray = str.split(",");
				String dayStr = tempArray[4].substring(0,4);
				if (dayStr.equals("Mond")) {
					histmondayInfosArrayList.add(str);
				}
				if (dayStr.equals("Tues")) {
					histtuesInfosdayArrayList.add(str);
				}
				if (dayStr.equals("Wedn")) {
					histwednesInfosdayArrayList.add(str);
				}
				if (dayStr.equals("Thur")) {
					histthursdayInfosArrayList.add(str);
				}
				if (dayStr.equals("Frid")) {
					histfridayInfosArrayList.add(str);
				}			
			}
			
			//实时数据处理
			//实时数据处理
			ArrayList<String> realInfosArrayList = new ArrayList<String>();//实时数据中的速度期望、速度方差
			//获得三个路段的行程时间
			FileOperateFunction.readFromTxtFile(targetLinkIDRealInputPathStr, realInfosArrayList);
			FileOperateFunction.readFromTxtFile(upstreamLinkIDRealInputPathStr, realInfosArrayList);
			FileOperateFunction.readFromTxtFile(downstreamLinkIDRealInputPathStr, realInfosArrayList);
			
			//实时数据按照天分类
			ArrayList<String> realmondayInfosArrayList = new ArrayList<String>();//周一
			ArrayList<String> realtuesInfosdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> realwednesInfosdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> realthursdayInfosArrayList = new ArrayList<String>();
			ArrayList<String> realfridayInfosArrayList = new ArrayList<String>();
			ArrayList<String> realweekInfosArrayList = new ArrayList<String>();//按照周分类后的信息
			
			int realDataCount = realInfosArrayList.size();
			for (int i = 0; i < realInfosArrayList.size(); i++) {
				System.out.print("按照周期性分类：" + i + ":" + realDataCount + '\n');
				String str = realInfosArrayList.get(i);
				String[]tempArray = str.split(",");
				String dayStr = tempArray[5];
				if (dayStr.equals("Mond")) {
					realmondayInfosArrayList.add(str);
				}
				if (dayStr.equals("Tues")) {
					realtuesInfosdayArrayList.add(str);
				}
				if (dayStr.equals("Wedn")) {
					realwednesInfosdayArrayList.add(str);
				}
				if (dayStr.equals("Thur")) {
					realthursdayInfosArrayList.add(str);
				}
				if (dayStr.equals("Frid")) {
					realfridayInfosArrayList.add(str);
				}			
			}
			
			ArrayList<double[]> allInputArrayList = new ArrayList<double[]>();
			ArrayList<String> allDateTimeArrayList = new ArrayList<String>();//时间戳信息
			int dayOfWeek = -1;//对应一周中那一天（1-5）
			//工作日，周一至周五
			for (int i = 0; i < 5; i++) {
				if (i == 0) {
					histweekInfosArrayList = histmondayInfosArrayList;
					realweekInfosArrayList = realmondayInfosArrayList;
					weekdayStr = "Mond";
					dayOfWeek = 1;
				}
				if (i == 1) {
					histweekInfosArrayList = histtuesInfosdayArrayList;
					realweekInfosArrayList = realtuesInfosdayArrayList;
					weekdayStr = "Tues";
					dayOfWeek = 2;
				}
				if (i == 2) {
					histweekInfosArrayList = histwednesInfosdayArrayList;
					realweekInfosArrayList = realwednesInfosdayArrayList;
					weekdayStr = "Wedn";
					dayOfWeek = 3;
				}
				if (i == 3) {
					histweekInfosArrayList = histthursdayInfosArrayList;
					realweekInfosArrayList = realthursdayInfosArrayList;
					weekdayStr = "Thur";
					dayOfWeek = 4;
				}
				if (i == 4) {
					histweekInfosArrayList = histfridayInfosArrayList;
					realweekInfosArrayList = realfridayInfosArrayList;
					weekdayStr = "Frid";
					dayOfWeek = 5;
				}
				
				//历史数据处理
				ArrayList<String> targetLinkDirectionHistWeekInfosArrayList = new ArrayList<String>();//目标路段某方向历史的某天信息
				ArrayList<String> upstreamLinkDirectionHistWeekInfosArrayList = new ArrayList<String>();//上游路段某方向历史的某天信息
				ArrayList<String> downstreamLinkDirectionHistWeekInfosArrayList = new ArrayList<String>();//下游路段某方向历史的某天信息
				obtainInfosAccordLinkIDDirection(histweekInfosArrayList, targetLinkID, targetDirection, targetLinkDirectionHistWeekInfosArrayList,
						upstreamLinkID, upstreamDirection, upstreamLinkDirectionHistWeekInfosArrayList, downstreamLinkID, downstreamDirection, 
						downstreamLinkDirectionHistWeekInfosArrayList);
			
//				TimeOperateFunction.sortHistAccordTime(targetLinkDirectionHistWeekInfosArrayList);//按照时间顺序排序
//				TimeOperateFunction.sortHistAccordTime(upstreamLinkDirectionHistWeekInfosArrayList);//
//				TimeOperateFunction.sortHistAccordTime(downstreamLinkDirectionHistWeekInfosArrayList);
				
				//实时数据处理
				ArrayList<String> targetLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//目标路段某方向实时的某天信息
				ArrayList<String> upstreamLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//上游路段某方向实时的某天信息
				ArrayList<String> downstreamLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//下游路段某方向实时的某天信息			
				obtainInfosAccordLinkIDDirection(realweekInfosArrayList, targetLinkID, targetDirection, targetLinkDirectionRealWeekInfosArrayList, 
						upstreamLinkID, upstreamDirection, upstreamLinkDirectionRealWeekInfosArrayList, downstreamLinkID, downstreamDirection, 
						downstreamLinkDirectionRealWeekInfosArrayList);
				
				//对目标路段数据处理，获得5点到24点之间的数据，缺失的数据用历史大数据弥补
				ArrayList<String> compensateTargetLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//大数据补足之后的目标时段数据
				ArrayList<String> compensateUpstreamLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//大数据补足
				ArrayList<String> compensateDownstreamLinkDirectionRealWeekInfosArrayList = new ArrayList<String>();//大数据补足
				obtainTargetTimePeriodCompensateData(startDayTimeInstantStr, endDayTimeInstantStr, targetLinkDirectionRealWeekInfosArrayList, 
						compensateTargetLinkDirectionRealWeekInfosArrayList, targetLinkDirectionHistWeekInfosArrayList);
				obtainTargetTimePeriodCompensateData(startDayTimeInstantStr, endDayTimeInstantStr, upstreamLinkDirectionRealWeekInfosArrayList, 
						compensateUpstreamLinkDirectionRealWeekInfosArrayList, upstreamLinkDirectionHistWeekInfosArrayList);
				obtainTargetTimePeriodCompensateData(startDayTimeInstantStr, endDayTimeInstantStr, downstreamLinkDirectionRealWeekInfosArrayList, 
						compensateDownstreamLinkDirectionRealWeekInfosArrayList, downstreamLinkDirectionHistWeekInfosArrayList);
//				/**
//				 * 临时写入文件信息
//				 */				
//				String tempFilePathStr = "G:\\travelTimeProcess\\compensatedSpeedExpectationStandarDeviation" + upstreamLinkID + ".txt";
//				FileOperateFunction.writeStringArraylistToEndTxtFile(tempFilePathStr, upstreamLinkDirectionWeekInfosArrayList);
				
				double periodOfDay = 0;// 对应一天中的时段
				double outputTargetRealTravelTime = -1;//实时数据输出
				double outputTargetHistTravelTime = -1;//实时数据输出
				//当前时段以及前两个邻接时段都有信息
				//暂时不考虑太多，假定当前时刻及前两个邻接时段都有信息
				int compTargetCount = compensateTargetLinkDirectionRealWeekInfosArrayList.size();
				int recyCount = Math.round(compTargetCount/timePeriodCount);
				//要保证前面数据处理无误
				for (int j = 0; j < recyCount; j++) {
					int sIndex = j * timePeriodCount;
					int eIndex = (j + 1)* timePeriodCount;
					ArrayList<String> targetDayRealInfosInWeekArrayList = new ArrayList<String>();//目标路段一周中某一天实时信息
					ArrayList<String> upstreamDayRealInfosInWeekArrayList = new ArrayList<String>();//上游路段一周中某一天实时信息
					ArrayList<String> downstreamDayRealInfosInWeekArrayList = new ArrayList<String>();//下游路段一周中某一天实时信息
					obtainInforsAccordIndex(timeIntervalConstant, sIndex, eIndex, compensateTargetLinkDirectionRealWeekInfosArrayList, 
							targetDayRealInfosInWeekArrayList);
					obtainInforsAccordIndex(timeIntervalConstant, sIndex, eIndex, compensateUpstreamLinkDirectionRealWeekInfosArrayList, 
							upstreamDayRealInfosInWeekArrayList);
					obtainInforsAccordIndex(timeIntervalConstant, sIndex, eIndex, compensateDownstreamLinkDirectionRealWeekInfosArrayList, 
							downstreamDayRealInfosInWeekArrayList);
					
					//从第三个时段开始
					for (int k = 2; k < timePeriodCount - 1; k++) {
						String curStr = targetDayRealInfosInWeekArrayList.get(k);
						String nextStr = targetDayRealInfosInWeekArrayList.get(k + 1);
						String []tempCurStrArray = curStr.split(",");
						String curymdTimeStr = tempCurStrArray[4];// 时间
						allDateTimeArrayList.add(curymdTimeStr);//所有时间
						String curhmsTimeStr = curymdTimeStr.substring(11);
						outputTargetRealTravelTime = Double.parseDouble(tempCurStrArray[8]);//目标路段行程时间
						String[] timeStrAndTimePeriod = obtainTimePeriodOfDay(curymdTimeStr, timeIntervalConstant);
						periodOfDay = Double.parseDouble(timeStrAndTimePeriod[1]);
						String []tempNextStrArray = nextStr.split(",");
						String nextymdTimeStr = tempNextStrArray[4];// 时间
						String nexthmsTimeStr = nextymdTimeStr.substring(11);
						String preymdTimeStr = "";//前一个时段
						String prepreymdTimeStr = "";//前两个时段
						preymdTimeStr = PubClass.obtainStartTimeAccordEndTime(curymdTimeStr, timeIntervalConstant);
						String prehmsTimeStr = preymdTimeStr.substring(11);
						prepreymdTimeStr = PubClass.obtainStartTimeAccordEndTime(preymdTimeStr, timeIntervalConstant);
						String preprehmsTimeStr = prepreymdTimeStr.substring(11);
						
						String preStrTargetReal = targetDayRealInfosInWeekArrayList.get(k - 1);
						String prepreStrTargetReal = targetDayRealInfosInWeekArrayList.get(k - 2);
						String preStrUpstreamReal = upstreamDayRealInfosInWeekArrayList.get(k - 1);
						String prepreStrUpstreamReal = upstreamDayRealInfosInWeekArrayList.get(k - 2);
						
						double [] charaArray = new double[15];						
						if (k == timePeriodCount - 2) {
							obtainUpDownTargetCharacteristic(prehmsTimeStr, preprehmsTimeStr, preStrTargetReal, prepreStrTargetReal, preStrUpstreamReal, 
									prepreStrUpstreamReal, targetLinkDirectionHistWeekInfosArrayList, compensateTargetLinkDirectionRealWeekInfosArrayList, 
									upstreamLinkDirectionHistWeekInfosArrayList, compensateUpstreamLinkDirectionRealWeekInfosArrayList, 
									downstreamLinkDirectionHistWeekInfosArrayList, compensateDownstreamLinkDirectionRealWeekInfosArrayList, charaArray);
							double[] tempInput = new double[20];
							tempInput[0] = 82;
							tempInput[1] = dayOfWeek;//周几
							tempInput[2] = periodOfDay;//半小时对应一个值,1到48,00:00:00用1代表							
							//目标路段
							tempInput[3] = charaArray[0];//当前时刻前一时段历史大数据的行程时间
							tempInput[4] = charaArray[1];//当前时刻前一时段之前时段历史大数据的行程时间
							tempInput[5] = charaArray[2];//两个时段历史大数据行程时间之差
							tempInput[6] = charaArray[3];
							tempInput[7] = charaArray[4];
							tempInput[8] = charaArray[5];					
							//上游路段
							tempInput[9] = charaArray[6];
							tempInput[10] = charaArray[7];
							tempInput[11] = charaArray[8];							
							tempInput[12] = charaArray[9];
							tempInput[13] = charaArray[10];
							tempInput[14] = charaArray[11];												
							//下游路段
							tempInput[15] = charaArray[12];//当前时刻前一时段的行程时间
							tempInput[16] = charaArray[13];//当前时刻前一时段之前时段的行程时间
							tempInput[17] = charaArray[14];//两个时段行程时间之差							
							//输出
							tempInput[18] = outputTargetRealTravelTime;//当前时刻行程时间
							outputTargetHistTravelTime = obtainTravelTimeFromBigDataAccordTimeInstant(curhmsTimeStr, targetLinkDirectionHistWeekInfosArrayList);
							tempInput[19] = outputTargetHistTravelTime;
							allInputArrayList.add(tempInput);
							
							preymdTimeStr = PubClass.obtainStartTimeAccordEndTime(nextymdTimeStr, timeIntervalConstant);
							allDateTimeArrayList.add(nextymdTimeStr);
							prehmsTimeStr = preymdTimeStr.substring(11);
							prepreymdTimeStr = PubClass.obtainStartTimeAccordEndTime(preymdTimeStr, timeIntervalConstant);
							preprehmsTimeStr = prepreymdTimeStr.substring(11);
							obtainUpDownTargetCharacteristic(prehmsTimeStr, preprehmsTimeStr, preStrTargetReal, prepreStrTargetReal, preStrUpstreamReal, 
									prepreStrUpstreamReal, targetLinkDirectionHistWeekInfosArrayList, compensateTargetLinkDirectionRealWeekInfosArrayList, 
									upstreamLinkDirectionHistWeekInfosArrayList, compensateUpstreamLinkDirectionRealWeekInfosArrayList, 
									downstreamLinkDirectionHistWeekInfosArrayList, compensateDownstreamLinkDirectionRealWeekInfosArrayList, charaArray);							
							tempInput = new double[20];
							tempInput[0] = 82;
							tempInput[1] = dayOfWeek;//周几
							tempInput[2] = periodOfDay + 1;//半小时对应一个值,1到48,00:00:00用1代表							
							//目标路段
							tempInput[3] = charaArray[0];//当前时刻前一时段历史大数据的行程时间
							tempInput[4] = charaArray[1];//当前时刻前一时段之前时段历史大数据的行程时间
							tempInput[5] = charaArray[2];//两个时段历史大数据行程时间之差						
							tempInput[6] = charaArray[3];
							tempInput[7] = charaArray[4];
							tempInput[8] = charaArray[5];							
							//上游路段
							tempInput[9] = charaArray[6];
							tempInput[10] = charaArray[7];
							tempInput[11] = charaArray[8];							
							tempInput[12] = charaArray[9];
							tempInput[13] = charaArray[10];
							tempInput[14] = charaArray[11];												
							//下游路段
							tempInput[15] = charaArray[12];//当前时刻前一时段的行程时间
							tempInput[16] = charaArray[13];//当前时刻前一时段之前时段的行程时间
							tempInput[17] = charaArray[14];//两个时段行程时间之差							
							//输出
							tempInput[18] = outputTargetRealTravelTime;//当前时刻行程时间
							outputTargetHistTravelTime = obtainTravelTimeFromBigDataAccordTimeInstant(nexthmsTimeStr, targetLinkDirectionHistWeekInfosArrayList);
							tempInput[19] = outputTargetHistTravelTime;
							allInputArrayList.add(tempInput);
												
						}
						else {
							obtainUpDownTargetCharacteristic(prehmsTimeStr, preprehmsTimeStr, preStrTargetReal, prepreStrTargetReal, preStrUpstreamReal, 
									prepreStrUpstreamReal, targetLinkDirectionHistWeekInfosArrayList, compensateTargetLinkDirectionRealWeekInfosArrayList, 
									upstreamLinkDirectionHistWeekInfosArrayList, compensateUpstreamLinkDirectionRealWeekInfosArrayList, 
									downstreamLinkDirectionHistWeekInfosArrayList, compensateDownstreamLinkDirectionRealWeekInfosArrayList, charaArray);							
							double[] tempInput = new double[20];
							tempInput[0] = 82;
							tempInput[1] = dayOfWeek;//周几
							tempInput[2] = periodOfDay;//半小时对应一个值,1到48,00:00:00用1代表							
							//目标路段
							tempInput[3] = charaArray[0];//当前时刻前一时段历史大数据的行程时间
							tempInput[4] = charaArray[1];//当前时刻前一时段之前时段历史大数据的行程时间
							tempInput[5] = charaArray[2];//两个时段历史大数据行程时间之差						
							tempInput[6] = charaArray[3];
							tempInput[7] = charaArray[4];
							tempInput[8] = charaArray[5];							
							//上游路段
							tempInput[9] = charaArray[6];
							tempInput[10] = charaArray[7];
							tempInput[11] = charaArray[8];							
							tempInput[12] = charaArray[9];
							tempInput[13] = charaArray[10];
							tempInput[14] = charaArray[11];												
							//下游路段
							tempInput[15] = charaArray[12];//当前时刻前一时段的行程时间
							tempInput[16] = charaArray[13];//当前时刻前一时段之前时段的行程时间
							tempInput[17] = charaArray[14];//两个时段行程时间之差					
							//输出
							tempInput[18] = outputTargetRealTravelTime;//当前时刻行程时间
							outputTargetHistTravelTime = obtainTravelTimeFromBigDataAccordTimeInstant(curhmsTimeStr, targetLinkDirectionHistWeekInfosArrayList);
							tempInput[19] = outputTargetHistTravelTime;
							allInputArrayList.add(tempInput);
						}	
					}					
				}				
			}	
			
			String headDescriptionStr = "dateTimeStr" + "," + "linkID" + "," + "timeStr" + "," +"digitWeek" + "," + "periodOfDay" + "," + "UpHTT1" + "," + "UpHTT2" + "," + "deltUpHTT" + "," +
					"UpRTT1" + "," + "UpRTT2" + "," + "deltUpRTT" + "," + "targetHTT1" + "," + "targetHTT2" + ","  + "deltTargetHTT" + "," + "targetRTT1" + "," +
					"targetRTT2" + ","  + "deltTargetRTT" + "," + "downHTT1" + "," + "downHTT2" + "," + "deltDownHTT" + ","  + "targetRTT(t)"+ ","  + 
					"targetHTT(t)" + "\r\n";
//			FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, allInputArrayList);	
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(ANNInputPathStr));//写入文件开始处,信息覆盖
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < allInputArrayList.size(); i++) {
				String dateTimeStr = allDateTimeArrayList.get(i);
				double [] temp = allInputArrayList.get(i);
				String tempStr = "";
				for (int j = 0; j < temp.length; j++) {
					if (j == 0 ) {
						tempStr = String.valueOf(temp[0]);
					}
					else {
						tempStr = tempStr + "," + temp[j];	
					}								
				}
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(dateTimeStr + "," + tempStr + "\r\n");
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
	 * 2015-11-21
	 * @param prehmsTimeStr	前一个时段时间戳，格式如：10:30:00
	 * @param preprehmsTimeStr	前两个时段时间戳
	 * @param preStrTargetReal	目标路段前一个时段实时信息
	 * @param prepreStrTargetReal	目标路段前两个时段实时信息
	 * @param preStrUpstreamReal	上游路段前一个时段实时信息
	 * @param prepreStrUpstreamReal	上游路段前两个时段实时信息
	 * @param targetLinkDirectionHistWeekInfosArrayList	目标路段历史信息
	 * @param targetLinkDirectionRealWeekInfosArrayList	目标路段历史信息
	 * @param upstreamLinkDirectionHistWeekInfosArrayList	上游路段历史信息
	 * @param upstreamLinkDirectionRealWeekInfosArrayList	上游路段实时信息
	 * @param downstreamLinkDirectionHistWeekInfosArrayList	下游路段历史信息
	 * @param downstreamLinkDirectionRealWeekInfosArrayList	下游路段实时信息
	 */
	public void obtainUpDownTargetCharacteristic(String prehmsTimeStr, String preprehmsTimeStr,
			String preStrTargetReal,String prepreStrTargetReal, String preStrUpstreamReal,String prepreStrUpstreamReal,
			ArrayList<String> targetLinkDirectionHistWeekInfosArrayList, ArrayList<String> targetLinkDirectionRealWeekInfosArrayList,
			ArrayList<String> upstreamLinkDirectionHistWeekInfosArrayList, ArrayList<String> upstreamLinkDirectionRealWeekInfosArrayList,
			ArrayList<String> downstreamLinkDirectionHistWeekInfosArrayList, ArrayList<String> downstreamLinkDirectionRealWeekInfosArrayList,
			double [] charaArray){
		double inputHistPreTravelTime = -1;
		double inputHistPrePreTravelTime = -1;
		double inputHistTravelTimeDifference = -1;		
		double inputRealPreTravelTime = -1;
		double inputRealPrePreTravelTime = -1;
		double inputRealTravelTimeDifference = -1;
		
		double inputHistPreTravelTimeUpstream = -1;
		double inputHistPrePreTravelTimeUpstream = -1;
		double inputHistTravelTimeDifferenceUpstream = -1;		
		double inputRealPreTravelTimeUpstream = -1;
		double inputRealPrePreTravelTimeUpstream = -1;
		double inputRealTravelTimeDifferenceUpstream = -1;
		
		double inputHistPreTravelTimeDownstream = -1;
		double inputHistPrePreTravelTimeDownstream = -1;
		double inputHistTravelTimeDifferenceDownstream = -1;	
		
		try {
			/**目标路段前两个时段实时数据和历史大数据行程时间*/
			//历史大数据
			double tinputHistPreTravelTime = obtainTravelTimeFromBigDataAccordTimeInstant(prehmsTimeStr, targetLinkDirectionHistWeekInfosArrayList);
			double tinputHistPrePreTravelTime = obtainTravelTimeFromBigDataAccordTimeInstant(preprehmsTimeStr, targetLinkDirectionHistWeekInfosArrayList);
			double tinputHistTravelTimeDifference = tinputHistPreTravelTime - tinputHistPrePreTravelTime;
			inputHistPreTravelTime =  Double.parseDouble(String.format("%.2f", tinputHistPreTravelTime));
			inputHistPrePreTravelTime = Double.parseDouble(String.format("%.2f", tinputHistPrePreTravelTime));
			inputHistTravelTimeDifference = Double.parseDouble(String.format("%.2f", tinputHistTravelTimeDifference));
									
			//实时数据*/						
//			String preStrTargetReal = targetDayRealInfosInWeekArrayList.get(k - 1);
//			String prePreStrTargetReal = targetDayRealInfosInWeekArrayList.get(k - 2);
			String[]preArray = preStrTargetReal.split(",");
			String[]prePreArray = prepreStrTargetReal.split(",");
			double tinputRealPreTravelTime = Double.parseDouble(preArray[8]);
			double tinputRealPrePreTravelTime = Double.parseDouble(prePreArray[8]);
			double tinputRealTravelTimeDifference = tinputRealPreTravelTime - tinputRealPrePreTravelTime;						
			inputRealPreTravelTime =  Double.parseDouble(String.format("%.2f", tinputRealPreTravelTime));
			inputRealPrePreTravelTime = Double.parseDouble(String.format("%.2f", tinputRealPrePreTravelTime));
			inputRealTravelTimeDifference = Double.parseDouble(String.format("%.2f", tinputRealTravelTimeDifference));						
									
			//上游路段前两个时段实时数据和历史大数据行程时间
			double tinputHistPreTravelTimeUpstream = obtainTravelTimeFromBigDataAccordTimeInstant(prehmsTimeStr, upstreamLinkDirectionHistWeekInfosArrayList);
			double tinputHistPrePreTravelTimeUpstream = obtainTravelTimeFromBigDataAccordTimeInstant(preprehmsTimeStr, upstreamLinkDirectionHistWeekInfosArrayList);
			double tinputHistTravelTimeDifferenceUpstream = tinputHistPreTravelTimeUpstream - tinputHistPrePreTravelTimeUpstream;
			inputHistPreTravelTimeUpstream =  Double.parseDouble(String.format("%.2f", tinputHistPreTravelTimeUpstream));
			inputHistPrePreTravelTimeUpstream = Double.parseDouble(String.format("%.2f", tinputHistPrePreTravelTimeUpstream));
			inputHistTravelTimeDifferenceUpstream = Double.parseDouble(String.format("%.2f", tinputHistTravelTimeDifferenceUpstream));	
			
//			String preStrUpstreamReal = upstreamDayRealInfosInWeekArrayList.get(k - 1);
//			String prepreStrUpstreamReal = upstreamDayRealInfosInWeekArrayList.get(k - 2);
			String[]upstreampreArray = preStrUpstreamReal.split(",");
			String[]upstreamprePreArray = prepreStrUpstreamReal.split(",");
			double tinputRealPreTravelTimeUpstream = Double.parseDouble(upstreampreArray[8]);
			double tinputRealPrePreTravelTimeUpstream = Double.parseDouble(upstreamprePreArray[8]);
			double tinputRealTravelTimeDifferenceUpstream = tinputRealPreTravelTimeUpstream - tinputRealPrePreTravelTimeUpstream;
			inputRealPreTravelTimeUpstream =  Double.parseDouble(String.format("%.2f", tinputRealPreTravelTimeUpstream));
			inputRealPrePreTravelTimeUpstream = Double.parseDouble(String.format("%.2f", tinputRealPrePreTravelTimeUpstream));
			inputRealTravelTimeDifferenceUpstream = Double.parseDouble(String.format("%.2f", tinputRealTravelTimeDifferenceUpstream));							
						
			//下游路段前两个时段历史大数据行程时间
			double tinputHistPreTravelTimeDownstream = obtainTravelTimeFromBigDataAccordTimeInstant(prehmsTimeStr, downstreamLinkDirectionHistWeekInfosArrayList);
			double tinputHistPrePreTravelTimeDownstream = obtainTravelTimeFromBigDataAccordTimeInstant(preprehmsTimeStr, downstreamLinkDirectionHistWeekInfosArrayList);
			double tinputHistTravelTimeDifferenceDownstream = tinputHistPreTravelTimeDownstream - tinputHistPrePreTravelTimeDownstream;
			inputHistPreTravelTimeDownstream =  Double.parseDouble(String.format("%.2f", tinputHistPreTravelTimeDownstream));
			inputHistPrePreTravelTimeDownstream = Double.parseDouble(String.format("%.2f", tinputHistPrePreTravelTimeDownstream));
			inputHistTravelTimeDifferenceDownstream = Double.parseDouble(String.format("%.2f", tinputHistTravelTimeDifferenceDownstream));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		charaArray[0] = inputHistPreTravelTime;
		charaArray[1] = inputHistPrePreTravelTime;
		charaArray[2] = inputHistTravelTimeDifference;
		charaArray[3] = inputRealPreTravelTime;
		charaArray[4] = inputRealPrePreTravelTime;
		charaArray[5] = inputRealTravelTimeDifference;
		
		charaArray[6] = inputHistPreTravelTimeUpstream;
		charaArray[7] = inputHistPrePreTravelTimeUpstream;
		charaArray[8] = inputHistTravelTimeDifferenceUpstream;
		charaArray[9] = inputRealPreTravelTimeUpstream;
		charaArray[10] = inputRealPrePreTravelTimeUpstream;
		charaArray[11] = inputRealTravelTimeDifferenceUpstream;
		
		charaArray[12] = inputHistPreTravelTimeDownstream;
		charaArray[13] = inputHistPrePreTravelTimeDownstream;
		charaArray[14] = inputHistTravelTimeDifferenceDownstream;
	}
	
	/**
	 * 2015-11-18
	 * 获得补足数据中的数据
	 * @param timeIntervalConstant	时间常数
	 * @param sIndex	起始索引
	 * @param eIndex	终止索引
	 * @param infosArraylisArrayList
	 * @param procArrayList	获得处理后信息
	 */
	public void obtainInforsAccordIndex(int timeIntervalConstant, int sIndex, int eIndex, ArrayList<String> infosArraylisArrayList,
			ArrayList<String> procArrayList){
		try {
			for (int j2 = sIndex; j2 < eIndex - 1; j2++) {
				String curStr = infosArraylisArrayList.get(j2);
				String nextStr = infosArraylisArrayList.get(j2 + 1);
				String []tempStrArray = curStr.split(",");
				String ymdTimeStr = tempStrArray[4];// 时间
				String []tempNextStrArray = nextStr.split(",");
				String nextymdTimeStr = tempNextStrArray[4];// 时间
				double ttimeInterval = TimeOperateFunction.obtainTimeInterval(ymdTimeStr, nextymdTimeStr);
				if (ttimeInterval < 5 * timeIntervalConstant) {
					procArrayList.add(curStr);
				}
				if (j2 == eIndex - 2) {
					procArrayList.add(nextStr);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
	}
	
	/**
	 * 2015-11-15
	 * 分别获得目标路段以及上下游信息
	 * @param realweekInfosArrayList
	 * @param targetLinkID
	 * @param targetDirection
	 * @param targetLinkDirectionRealWeekInfosArrayList
	 * @param upstreamLinkID
	 * @param upstreamDirection
	 * @param upstreamLinkDirectionRealWeekInfosArrayList
	 * @param downstreamLinkID
	 * @param downstreamDirection
	 * @param downstreamLinkDirectionRealWeekInfosArrayList
	 */
	public void obtainInfosAccordLinkIDDirection(ArrayList<String> realweekInfosArrayList, int targetLinkID, int targetDirection, ArrayList<String> targetLinkDirectionRealWeekInfosArrayList, 
			int upstreamLinkID, int upstreamDirection, ArrayList<String> upstreamLinkDirectionRealWeekInfosArrayList, 
			int downstreamLinkID, int downstreamDirection, ArrayList<String> downstreamLinkDirectionRealWeekInfosArrayList){
		try {
			for (int j = 0; j < realweekInfosArrayList.size(); j++) {
				String str = realweekInfosArrayList.get(j);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				String directionStr = tempStrArray[3];
				int tempLinkID = Integer.parseInt(linkIDStr);
				int tempDirection = Integer.parseInt(directionStr);
				if (tempLinkID == targetLinkID && tempDirection == targetDirection) {
					targetLinkDirectionRealWeekInfosArrayList.add(str);			
				}
				if (tempLinkID == upstreamLinkID && tempDirection == upstreamDirection) {
					upstreamLinkDirectionRealWeekInfosArrayList.add(str);			
				}
				if (tempLinkID == downstreamLinkID && tempDirection == downstreamDirection) {
					downstreamLinkDirectionRealWeekInfosArrayList.add(str);			
				}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	
	/**
	 * 缺失的数据用历史大数据弥补并获得目标时段内的数据
	 * 1.先补足数据
	 * 2.获得目标时段内数据
	 * 2015-11-16
	 * @param startTimeStr	开始时间，如05:00:00
	 * @param endTimeStr	结束时间,如23:59:59
	 * @param targetInfosArrayList
	 * @param proTargetInfosArrayList	目标路段特定时段的数据
	 * @param histBigDataInfosArrayList	历史大数据信息
	 *  
	 */
	public void obtainTargetTimePeriodCompensateData(String startTimeStr,	String endTimeStr, ArrayList<String> targetRealInfosArrayList,
			ArrayList<String> proTargetInfosArrayList, ArrayList<String> histBigDataInfosArrayList ){
		int timeIntervalConstant = 1800;
		ArrayList<String> elimDuplTargetRealInfosArrayList = new ArrayList<String>();
		setOperateFunction.eliminateDuplicateData(targetRealInfosArrayList, elimDuplTargetRealInfosArrayList);
		try {			
			//大数据补足实时数据		
			//若下一个数据时间与当前数据时间之间相差不是30min，则中间缺失数据，利用大数据补足
			ArrayList<String> compensateTargetRealInfosArrayList = new ArrayList<String>();//大数据补足目标路段之后的数据			
			if (elimDuplTargetRealInfosArrayList.size() != 0) {	
				int count = 0;//控制变量，是否为一天中的初始时刻
				for (int i = 0; i < elimDuplTargetRealInfosArrayList.size() - 1; i++) {
					String curStr = elimDuplTargetRealInfosArrayList.get(i);
					String nextStr = elimDuplTargetRealInfosArrayList.get(i + 1);
					String[] curTempArray = curStr.split(",");
					String curDateTimeStr = curTempArray[4];
					if (curDateTimeStr.equals("2014-07-07 01:00:00")) {
						System.out.print("");
					}
					String[] nextTempArray = nextStr.split(",");
					String nextDateTimeStr = nextTempArray[4];					
					double timeInterval = PubClass.obtainTimeInterval(curDateTimeStr, nextDateTimeStr);					
					//时间间隔必须小于一天才能插值，否则这个周一与下个周一之间也进行插值是不正确的
					//周与周之间间隔
					if (timeInterval > 23 * 3600) {
						//如果最后一个时间不是23:30:00,则从最后一个时间到23:30:00之间利用大数据进行插值
						String ymd = curDateTimeStr.substring(0,11);//年月日
						String finalProTime = ymd + "23:30:00";
						if (!curDateTimeStr.equals(finalProTime)) {
							timeInterval = PubClass.obtainTimeInterval(curDateTimeStr, finalProTime);
							int insertCount = (int)(timeInterval/timeIntervalConstant);//插值次数,与以上计算有所不同
							compensateTargetRealInfosArrayList.add(curStr);
							bigDataCompensateInfoAccordCurrentInfos(curStr, insertCount, timeIntervalConstant, histBigDataInfosArrayList,
									compensateTargetRealInfosArrayList);								
						}
						else {
							compensateTargetRealInfosArrayList.add(curStr);//最后一个23:30:00的数据加入
						}
						count = 0;
					}
					//一天内的时间进行插值
					else {
						count++;
						//一天当中第一个时段
						if (count == 1) {
							String ymd = curDateTimeStr.substring(0,11);//年月日
							String initialProTime = ymd + "00:00:00";
							//初始时段不为00点，0点到当天第一个时段进行插值
							if (!curDateTimeStr.equals(initialProTime)) {
								double initialTtimeInterval = PubClass.obtainTimeInterval(initialProTime, curDateTimeStr);
								int initialInsertCount = (int)(initialTtimeInterval/timeIntervalConstant) - 1;//插值次数,与以上计算有所不同
								String initialString = nextTempArray[0] + "," + nextTempArray[1] + "," + nextTempArray[2] + "," + nextTempArray[3] + "," + 
								initialProTime + "," + nextTempArray[5] + "," + nextTempArray[6] + "," + nextTempArray[7] + "," + nextTempArray[8];//构造的初始字符
								compensateTargetRealInfosArrayList.add(initialString);
								bigDataCompensateInfoAccordCurrentInfos(initialString, initialInsertCount, timeIntervalConstant, histBigDataInfosArrayList,
										compensateTargetRealInfosArrayList);//包括初始时刻的插值
								compensateTargetRealInfosArrayList.add(curStr);
								//初始点时刻与后一点时刻为非连续时刻，插值
								int insertCount = (int)(timeInterval/timeIntervalConstant) - 1;
								bigDataCompensateInfoAccordCurrentInfos(curStr, insertCount, timeIntervalConstant, histBigDataInfosArrayList,
										compensateTargetRealInfosArrayList);//包括初始时刻的插值

							}
							//加入
							else {
								compensateTargetRealInfosArrayList.add(curStr);
								timeInterval = PubClass.obtainTimeInterval(curDateTimeStr, nextDateTimeStr);
								int insertCount = (int)(timeInterval/timeIntervalConstant) - 1;
								bigDataCompensateInfoAccordCurrentInfos(curStr, insertCount, timeIntervalConstant, histBigDataInfosArrayList,
										compensateTargetRealInfosArrayList);//不包括初始时刻的插值
								
							}
						}
						else {
							int insertCount = (int)(timeInterval/timeIntervalConstant - 1);//插值次数
							if (i == elimDuplTargetRealInfosArrayList.size() - 2) {
								if (timeInterval == timeIntervalConstant) {
									compensateTargetRealInfosArrayList.add(curStr);//倒数第二个加入
									compensateTargetRealInfosArrayList.add(nextStr);//最后一个加入
								}
								else {
									//否则，最后一个点舍掉，用大数据弥补当前点时刻到23:30:00之间的数据，舍掉最后一个点为了计算方便
									String ttDateString = curStr.split(",")[4];
									String ymd = ttDateString.substring(0,11);//年月日
									String finalProTime = ymd + "23:30:00";
									if (!ttDateString.equals(finalProTime)) {
										timeInterval = PubClass.obtainTimeInterval(curDateTimeStr, finalProTime);
										insertCount = (int)(timeInterval/timeIntervalConstant);//插值次数,与以上计算有所不同
										compensateTargetRealInfosArrayList.add(curStr);
										bigDataCompensateInfoAccordCurrentInfos(curStr, insertCount, timeIntervalConstant, 
												histBigDataInfosArrayList, compensateTargetRealInfosArrayList);
									}
								}									
							}
							else {
								if (insertCount == 0) {
									compensateTargetRealInfosArrayList.add(curStr);
									continue;
								}
								//补足数据
								else {
									compensateTargetRealInfosArrayList.add(curStr);
									bigDataCompensateInfoAccordCurrentInfos(curStr, insertCount, timeIntervalConstant, 
											histBigDataInfosArrayList, compensateTargetRealInfosArrayList);					
								}
							}//非倒数第二个点
						}
					}	
				}
			}
			int dataCount = compensateTargetRealInfosArrayList.size();
			for (int i = 0; i < compensateTargetRealInfosArrayList.size(); i++) {
				System.out.print("按照周期性分类：" + i + ":" + dataCount + '\n');
				String str = compensateTargetRealInfosArrayList.get(i);
				String[]tempArray = str.split(",");
				String dayTimeStr = tempArray[4].substring(11);
				if (PubClass.isTimeInstantBetweenStartEndTime(startTimeStr, endTimeStr, dayTimeStr)) {
					proTargetInfosArrayList.add(str);
				}		
			}
			System.out.print("历史大数据弥补缺失数据结束！");	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 
	 * 2015-11-16
	 * 根据当前数据缺失情况，用历史大数据补足缺失数据
	 * @param curStr	当前数据
	 * @param insertCount	插值次数
	 * @param timeIntervalConstant	插值时间常量
	 * @param histBigDataInfosArrayList	历史大数据
	 * @param compensateTargetRealInfosArrayList	补足后的数据
	 */
	public void bigDataCompensateInfoAccordCurrentInfos(String curStr, int insertCount, int timeIntervalConstant, ArrayList<String> histBigDataInfosArrayList,
			ArrayList<String> compensateTargetRealInfosArrayList){
		try {
			String[] curTempArray = curStr.split(",");
			String curDateTimeStr = curTempArray[4];
			String insertDateTimeStr = "";//插入时间
			for (int j = 0; j < insertCount; j++) {
				insertDateTimeStr = PubClass.obtainEndTimeAccordStartTime(curDateTimeStr, timeIntervalConstant);
				String linkIDStr = curTempArray[0];
				String enterNodeID = curTempArray[1];
				String exitNodeID = curTempArray[2];
				String travelDirectionStr = curTempArray[3];
				String insertDateStr = insertDateTimeStr;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");      
				Date date = simpleDateFormat.parse(insertDateStr); 
				Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    String digitWeekStr = "";//周几
			    String weekDesc = "";
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
			    	digitWeekStr = "1";
			    	weekDesc = "Mond";
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ){
			    	digitWeekStr = "2";
			    	weekDesc = "Tues";
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ){
			    	digitWeekStr = "3";
			    	weekDesc = "Wedn";
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY ){
			    	digitWeekStr = "4";
			    	weekDesc = "Thur";
				}
			    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
			    	digitWeekStr = "5";
			    	weekDesc = "Frid";
				}
			    String proRealDateStr = weekDesc + insertDateStr.substring(11);
			    String proRealTravelTime = "";
			    for (int p = 0; p < histBigDataInfosArrayList.size(); p++) {
					String str = histBigDataInfosArrayList.get(p);
					String []tempArray = str.split(",");
					String histDateStr = tempArray[4];
					if (proRealDateStr.equals(histDateStr) ) {
						double length = Double.parseDouble(tempArray[8]);
						double speed = Double.parseDouble(tempArray[5]);
						String travelTime = String.format("%.2f", length/speed);
						proRealTravelTime = travelTime;
						break;
					}
				}
			    String[] timeStrAndTimePeriod = obtainTimePeriodOfDay(insertDateStr, timeIntervalConstant);
			    String periodOfDay = timeStrAndTimePeriod[1];
			    String proInsertStr = linkIDStr + "," + enterNodeID + "," + exitNodeID + "," + travelDirectionStr + "," + insertDateStr + ","
			    + weekDesc + "," + digitWeekStr + "," + periodOfDay + "," + proRealTravelTime;
			    compensateTargetRealInfosArrayList.add(proInsertStr);
			    curDateTimeStr = insertDateTimeStr;
		}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
	}
	
	/**
	 * 2015-11-18
	 * 获得行程时间信息
	 * @param preStr
	 * @return
	 */
	public double obtainPreviousTravelTime(String preStr){
		double preTempTravelTime = 0;
		try {
			String []preTempStrArray = preStr.split(",");
			String preTempSpeedStr = preTempStrArray[5];// 速度
			double preTempSpeedDouble = Double.parseDouble(preTempSpeedStr);
			double preSpeedDouble = Double.parseDouble(String.format("%.2f", preTempSpeedDouble));
			String preTempLinkLength = preTempStrArray[8];//路段长度
			double preTempLinkLengthDouble = Double.parseDouble(preTempLinkLength);
			double preLinkLengthDouble = Double.parseDouble(String.format("%.2f", preTempLinkLengthDouble));
			preTempTravelTime = preLinkLengthDouble/preSpeedDouble;				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return preTempTravelTime;	
	}
	
	/**
	 * 根据时刻hmsTimeStr从大数据中获得行程时间信息
	 * @param preStr
	 */
	public double obtainTravelTimeFromBigDataAccordTimeInstant(String hmsTimeStr, ArrayList<String> histDayInfos){
		double histTravelTime = 0;
		try {			
			for (int i = 0; i < histDayInfos.size(); i++) {
				String str = histDayInfos.get(i);
				String [] tempArray = str.split(",");
				String timeStr = tempArray[4].substring(4);
				if (hmsTimeStr.equals(timeStr)) {
					String speedStr = tempArray[5];// 速度
					double speedDouble = Double.parseDouble(speedStr);
					String linkLength = tempArray[8];//路段长度
					double lengthDouble = Double.parseDouble(linkLength);
					double tempTravelTime = lengthDouble/speedDouble;	
					histTravelTime = Double.parseDouble(String.format("%.2f", tempTravelTime));
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return histTravelTime;	
	}
	
	/**
	 * 2015-11-18
	 * 实时数据中获得行程时间信息
	 * @param preStr
	 * @return
	 */
	public double obtainPreviousTravelTimeFromRealData(String preStr){
		double preTempTravelTime = 0;
		try {
			String []preTempStrArray = preStr.split(",");
			String preTempSpeedStr = preTempStrArray[5];// 速度
			double preTempSpeedDouble = Double.parseDouble(preTempSpeedStr);
			double preSpeedDouble = Double.parseDouble(String.format("%.2f", preTempSpeedDouble));
			String preTempLinkLength = preTempStrArray[8];//路段长度
			double preTempLinkLengthDouble = Double.parseDouble(preTempLinkLength);
			double preLinkLengthDouble = Double.parseDouble(String.format("%.2f", preTempLinkLengthDouble));
			preTempTravelTime = preLinkLengthDouble/preSpeedDouble;				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return preTempTravelTime;	
	}
	
	
}






