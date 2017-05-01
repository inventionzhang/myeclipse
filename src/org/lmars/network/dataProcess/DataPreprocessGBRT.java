package org.lmars.network.dataProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lmars.network.util.DataProcessFunction;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.NumberOperation;
import org.lmars.network.util.TimeOperateFunction;

public class DataPreprocessGBRT {

	public static void main(String[] args) {
//		 double t = 1.0/36;
//		String t1 = String.valueOf(t);
//		boolean b = NumberOperation.isInteger(t1);
//		t = 21.0/36;
//		t1 = String.valueOf(t);
//		b = NumberOperation.isInteger(t1);
//		t1 = String.valueOf(36.0/36);
//		b = NumberOperation.isInteger(t1);
//		t1 = String.valueOf(37.0/36);
//		b = NumberOperation.isInteger(t1);
		
		
		
//		new DataPreprocessGBRT().classifyDataByDaysOfWeek();//对数据分类统计
		new DataPreprocessGBRT().STGBRTPredictTwoPeriodData();//预测两个时段之后的行程时间数据
		
	}
	
	/**
	 * 2016-1-25
	 * 根据周一、二、三、四、五对数据分类统计
	 *
	 */
	public void classifyDataByDaysOfWeek(){
		int linkID = 77;
		int index = 5;
		String filePathStr1 = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJuly.txt";
		ArrayList<String> infosArrayList = new ArrayList<String>();		
		FileOperateFunction.readFromTxtFile(filePathStr1, infosArrayList);
		Map<Integer, List<String>> infosMap = new HashMap<Integer, List<String>>();
		DataProcessFunction.classifyByWeekdays(infosArrayList, index, infosMap);
		
		List<String> linkmondayArrayList = new ArrayList<String>();//周一
		List<String> linktuesdayArrayList = new ArrayList<String>();//周二
		List<String> linkwednesdayArrayList = new ArrayList<String>();//周三
		List<String> linkthursdayArrayList = new ArrayList<String>();
		List<String> linkfridayArrayList = new ArrayList<String>();
		linkmondayArrayList = infosMap.get(1);
		linktuesdayArrayList = infosMap.get(2);
		linkwednesdayArrayList = infosMap.get(3);
		linkthursdayArrayList = infosMap.get(4);
		linkfridayArrayList = infosMap.get(5);
		
		String headDescriptionStr = "linkID" + "," + "enterNodeID" + "," + "exitNodeID" + "," + "travelDirection" + "," + "taxiID" + "," +
				"startTravelTime" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";
		String filePathStrMonday = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJulyMonday.txt";		
		FileOperateFunction.writeStringListToTxtFile(filePathStrMonday, headDescriptionStr, linkmondayArrayList);
		
		String filePathStrTuesday = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJulyTuesday.txt";		
		FileOperateFunction.writeStringListToTxtFile(filePathStrTuesday, headDescriptionStr, linktuesdayArrayList);
		
		String filePathStrWedneday = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJulyWedneday.txt";		
		FileOperateFunction.writeStringListToTxtFile(filePathStrWedneday, headDescriptionStr, linkwednesdayArrayList);
		
		String filePathStrThursday = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJulyThursday.txt";		
		FileOperateFunction.writeStringListToTxtFile(filePathStrThursday, headDescriptionStr, linkthursdayArrayList);
		
		String filePathStrFriday = "G:\\travelTimeProcess\\targetWorkDayTravelTimeSortByTime" + linkID + "MayJuneJulyFriday.txt";		
		FileOperateFunction.writeStringListToTxtFile(filePathStrFriday, headDescriptionStr, linkfridayArrayList);
		
		System.out.print("done!");
		
	}
	
	/**
	 * 2016-1-25
	 * 预测两个时段之后的行程时间数据
	 *
	 */
	public void STGBRTPredictTwoPeriodData(){
//		String filePathStr = "G:\\travelTimeProcess\\GBRTDataInput3.txt";
		String filePathStr = "G:\\travelTimeProcess\\GBRTDataInputPredictInput5.txt";
		//用于模型对未来两个时段之后的时间预测数据
//		String processFilePathStr = "G:\\travelTimeProcess\\GBRTDataInput3TwoTimePeriod.txt";
		String processFilePathStr = "G:\\travelTimeProcess\\GBRTDataInputPredictInput5TwoTimePeriod.txt";
		int timeInterval = 1800;
		double timePeriodCount = 36.0;
		ArrayList<String> infosArrayList = new ArrayList<String>();
		ArrayList<String> processInfosArrayList = new ArrayList<String>();
		FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
		for (int i = 0; i < infosArrayList.size(); i++) {
			if (i == 35) {
				System.out.print("debug!");
			}
			//是否为36的公倍数
			String temp =String.valueOf((i + 1)/timePeriodCount);
			if (NumberOperation.isInteger(temp)) {
				continue;
			}
			else {
				String tStr = infosArrayList.get(i);
				String []strArray = tStr.split(",");
				String startTimeStr = strArray[0];
				String periodOfDayStr = strArray[3];
				double proPeriodOfDay = Double.parseDouble(periodOfDayStr) + 1;
				String[] endTimeArray = new String[1];
				TimeOperateFunction.obtainEndTimeAccordStartTime(startTimeStr, timeInterval, endTimeArray);
				strArray[0] = endTimeArray[0];
				strArray[3] = String.valueOf(proPeriodOfDay);
				strArray[strArray.length - 1] = infosArrayList.get(i + 1).split(",")[strArray.length - 1];
				String pStr = "";
				for (int j = 0; j < strArray.length; j++) {
					if (j == strArray.length - 1) {
						pStr = pStr + strArray[j];
					}
					else {
						pStr = pStr + strArray[j] + ",";
					}					
				}
				processInfosArrayList.add(pStr);
			}
		}
		String headDescriptionStr = "dateTimeStr" + "," + "linkID" + "," + "," +"digitWeek" + "," + "periodOfDay" + "," + "UpHTT1" + "," + "UpHTT2" + "," + "deltUpHTT" + "," +
				"UpRTT1" + "," + "UpRTT2" + "," + "deltUpRTT" + "," + "targetHTT1" + "," + "targetHTT2" + ","  + "deltTargetHTT" + "," + "targetRTT1" + "," +
				"targetRTT2" + ","  + "deltTargetRTT" + "," + "downHTT1" + "," + "downHTT2" + "," + "deltDownHTT" + ","  + "targetRTT(t+1)"+ ","  + 
				"targetHTT(t+1)" + "\r\n";
		FileOperateFunction.writeStringArraylistToTxtFile(processFilePathStr, headDescriptionStr, processInfosArrayList);
		System.out.print("done!");
		
	}
	
}
