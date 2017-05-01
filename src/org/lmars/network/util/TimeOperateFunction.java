package org.lmars.network.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;

/**
 * 时间操作函数
 * @author whu
 *
 */
public class TimeOperateFunction {

	/*根据起止时间求时间差：
	 * 1.要保证endTimeStr时间大于startTimeStr时间
	 * 2.起止时间之间最多相差24小时（24*60分）
	 * 输入：
	 * startTimeStr：起始时间
	 * endTimeStr：终止时间
	 * 输出：
	 * timeInterval:时间间隔以秒为单位
	 * 时间格式2013-01-01 00:00:15*/
	public static double obtainTimeInterval(String startTimeStr, String endTimeStr){
		double timeInterval = 0;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    java.util.Date endDate = df.parse(endTimeStr);
		    double tempTimeInterval = endDate.getTime() - startDate.getTime();
//		    long day = timeInterval/(24 * 60 * 60 * 1000);
//		    long hour = (timeInterval/(60 * 60 * 1000) - day * 24);
//		    long min = ((timeInterval/(60 * 1000)) - day * 24 * 60 - hour * 60);
//		    long s = (timeInterval/1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		    timeInterval = (double)(tempTimeInterval/1000);			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeInterval;
	}
	
	/*起止时间之间最多相差24小时（24*60分）*/
	public double obtainTimeInterval22(String startTimeStr, String endTimeStr){
		double timeInterval = 0;
		try {
			//起始时间
			String startYearStr = startTimeStr.substring(0, 4);
			int startYearInt = Integer.valueOf(startYearStr);
			String startMonthStr = startTimeStr.substring(5, 7);
			int startMonthInt = Integer.valueOf(startMonthStr);
			String startDayStr = startTimeStr.substring(8, 10);
			int startDayInt = Integer.valueOf(startDayStr);
			String startHourStr = startTimeStr.substring(11, 13);
			int startHourInt = Integer.valueOf(startHourStr);
			String startMinStr = startTimeStr.substring(14, 16);
			int startMinInt = Integer.valueOf(startMinStr);
			String startSecStr = startTimeStr.substring(17, 19);
			int startSecInt = Integer.valueOf(startSecStr);
			//终止时间
			String endYearStr = endTimeStr.substring(0, 4);
			int endYearInt = Integer.valueOf(endYearStr);
			String endMonthStr = endTimeStr.substring(5, 7);
			int endMonthInt = Integer.valueOf(endMonthStr);
			String endDayStr = endTimeStr.substring(8, 10);
			int endDayInt = Integer.valueOf(endDayStr);
			String endHourStr = endTimeStr.substring(11, 13);
			int endHourInt = Integer.valueOf(endHourStr);
			String endMinStr = endTimeStr.substring(14, 16);
			int endMinInt = Integer.valueOf(endMinStr);
			String endSecStr = endTimeStr.substring(17, 19);
			int endSecInt = Integer.valueOf(endSecStr);		
			int secInterval = 0;//秒间隔
			int minInterval = 0;//分钟间隔
			int hourInterval = 0;//小时间隔
			if (endSecInt >= startSecInt) {
				secInterval = endSecInt - startSecInt;
				if (endMinInt >= startMinInt) {
					minInterval = endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//时 借位
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
				else {
					//借位
					minInterval = 60 + endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//时 借位
					else {
						hourInterval = 24 + endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
			}
			else {
				//借位
				secInterval = 60 + endSecInt - startSecInt;
				if ((endMinInt - 1) >= startMinInt) {
					minInterval  = endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//时 借位
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					
				}
				else {
					//借位
					minInterval  = 60 + endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//时 借位
					else {
						hourInterval = 24 + endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
			}
			timeInterval = hourInterval + minInterval + secInterval;		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeInterval;
	}
	
	/*判断time2Str是否在time1Str后
	 * 若是，返回true
	 * 否则，返回false
	 * 时间格式2013-01-01 00:00:15
	 * */
	public static boolean isTime2AfterTime1(String time1Str, String time2Str){
		boolean isTrue = false;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(time1Str);
		    java.util.Date endDate = df.parse(time2Str);
		    double tempTimeInterval = endDate.getTime() - startDate.getTime();
		    if (tempTimeInterval > 0) {
		    	isTrue = true;
			}		    
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isTrue;
	}
	
	
	/**根据起始时间、一定的时间间隔计算终止时间
	 * startTimeStr:开始时间，格式2013-01-01 00:00:15
	 * endTimeArray：结束时间,格式2013-01-01 00:00:15
	 * timeInterval：时间间隔，以秒为单位
	 * */
	public static boolean obtainEndTimeAccordStartTime(String startTimeStr, int timeInterval, String[] endTimeArray){
		boolean isOK = false;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    startDate.setSeconds(startDate.getSeconds() + timeInterval);
		    String endTimeStr = df.format(startDate);
		    endTimeArray[0] = endTimeStr;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	public static String obtainEndTimeAccordStartTime(String startTimeStr, int timeInterval){
		String endTimeStr = "";
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    startDate.setSeconds(startDate.getSeconds() + timeInterval);
		    endTimeStr = df.format(startDate);
		    
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return endTimeStr;
	}
	
	/* timeInterval：时间间隔，以秒为单位，<=60s*/
	public boolean obtainEndTimeAccordStartTime22(String startTimeStr, int timeInterval, String[] endTimeArray){
		boolean isOK = false;
		try {
			if (timeInterval <= 60) {
				String startYearStr = startTimeStr.substring(0, 4);
				int startYearInt = Integer.valueOf(startYearStr);
				String startMonthStr = startTimeStr.substring(5, 7);
				int startMonthInt = Integer.valueOf(startMonthStr);
				String startDayStr = startTimeStr.substring(8, 10);
				int startDayInt = Integer.valueOf(startDayStr);
				String startHourStr = startTimeStr.substring(11, 13);
				int startHourInt = Integer.valueOf(startHourStr);
				String startMinStr = startTimeStr.substring(14, 16);
				int startMinInt = Integer.valueOf(startMinStr);
				String startSecStr = startTimeStr.substring(17, 19);
				int startSecInt = Integer.valueOf(startSecStr);
				
				int endSecInt = startSecInt + timeInterval ;
				int endMinInt = startMinInt;
				int endHourInt = startHourInt;
				int endDayInt = startDayInt;
				int endMonthInt = startMonthInt;
				int endYearInt = startYearInt;
				if (endSecInt >=60) {
					endSecInt = endSecInt - 60;
					endMinInt = endMinInt + 1;
					if (endMinInt >= 60) {
						endMinInt = endMinInt - 60;
						endHourInt = endHourInt + 1;
						if (endHourInt >= 24) {
							endHourInt = endHourInt - 24;
							endDayInt = endDayInt + 1;
						}
					}				
				}			
				String endYearStr = String.valueOf(endYearInt);
				String endMonthStr = String.valueOf(endMonthInt);
				String endDayStr = String.valueOf(endDayInt);
				String endHourStr = String.valueOf(endHourInt);
				String endMinStr = String.valueOf(endMinInt);
				String endSecStr = String.valueOf(endSecInt);
				
				if (endMonthInt < 10) {
					endMonthStr = "0" + String.valueOf(endMonthInt);
				}
				if (endDayInt < 10) {
					endDayStr = "0" + String.valueOf(endDayInt);
				}
				if (endHourInt < 10) {
					endHourStr = "0" + String.valueOf(endHourInt);
				}
				if (endMinInt < 10) {
					endMinStr = "0" + String.valueOf(endMinInt);
				}
				if (endSecInt < 10) {
					endSecStr = "0" + String.valueOf(endSecInt);
				}		
				String endTimeStr = endYearStr + "-" + endMonthStr + "-" + endDayStr + " " + endHourStr  + ":" + endMinStr + ":" + endSecStr;
				endTimeArray[0] = endTimeStr;	
				isOK = true;
			}
			else {
				System.out.print("时间间隔请设置为小于60s");
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/*根据终止时间、一定的时间间隔计算起始时间
	 * endTimeStr:结束时间，格式2013-01-01 00:00:15
	 * startTimeArray：开始时间,格式2013-01-01 00:00:15
	 * timeInterval：时间间隔，以秒为单位,小于等于60s*/
	public static boolean obtainStartTimeAccordEndTime(String endTimeStr, int timeInterval, String[] startTimeArray){
		boolean isOK = false;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date endDate = df.parse(endTimeStr);
		    endDate.setSeconds(endDate.getSeconds() - timeInterval);
		    String starTimeStr = df.format(endDate);
		    startTimeArray[0] = starTimeStr;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	public static String obtainStartTimeAccordEndTime(String endTimeStr, int timeInterval){
		String starTimeStr = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date endDate = df.parse(endTimeStr);
		    endDate.setSeconds(endDate.getSeconds() - timeInterval);
		    starTimeStr = df.format(endDate);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return starTimeStr;
	}
	
	/*根据终止时间、一定的时间间隔计算起始时间
	 * endTimeStr:结束时间，格式2013-01-01 00:00:15
	 * startTimeArray：开始时间,格式2013-01-01 00:00:15
	 * timeInterval：时间间隔，以毫秒为单位,小于等于60s*/
	public static boolean obtainStartTimeAccordEndTime(String endTimeStr, long millSecond, String[] startTimeArray){
		boolean isOK = false;
//		try {
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		    java.util.Date endDate = df.parse(endTimeStr);
//		    endDate.setSeconds(endDate.getSeconds() - millSecond);
//		    String starTimeStr = df.format(endDate);
//		    startTimeArray[0] = starTimeStr;
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//			isOK = false;
//		}		
		return isOK;
	}
	
	public boolean obtainStartTimeAccordEndTime22(String endTimeStr, int timeInterval, String[] startTimeArray){
		boolean isOK = false;
		try {
			String endYearStr = endTimeStr.substring(0, 4);
			int endYearInt = Integer.valueOf(endYearStr);
			String endMonthStr = endTimeStr.substring(5, 7);
			int endMonthInt = Integer.valueOf(endMonthStr);
			String endDayStr = endTimeStr.substring(8, 10);
			int endDayInt = Integer.valueOf(endDayStr);
			String endHourStr = endTimeStr.substring(11, 13);
			int endHourInt = Integer.valueOf(endHourStr);
			String endMinStr = endTimeStr.substring(14, 16);
			int endMinInt = Integer.valueOf(endMinStr);
			String endSecStr = endTimeStr.substring(17, 19);
			int endSecInt = Integer.valueOf(endSecStr);		
			int startSecInt = 0;//秒
			int startMinInt = 0;//分
			int startHourInt = 0;//时
			int startDayInt = endDayInt;//天
			int startMonthInt = endMonthInt;
			int startYearInt = endYearInt;
			if (endSecInt >= timeInterval) {
				startSecInt = endSecInt - timeInterval;
				startMinInt = endMinInt;
				startHourInt = endHourInt;
				startDayInt = endDayInt;
				startMonthInt = endMonthInt;
				startYearInt = endYearInt;
			}
			else {
				 //借位
				startSecInt = 60 + endSecInt - timeInterval;
				//被借的位 值为0
				if (endMinInt == 0) {
					startMinInt = 59;
					if (endHourInt == 0) {
						startHourInt = 23;
						startDayInt = endDayInt - 1;
						startMonthInt = endMonthInt;
						startYearInt = endYearInt;
					}
					else {
						startHourInt = endHourInt - 1;
						startDayInt = endDayInt;
						startMonthInt = endMonthInt;
						startYearInt = endYearInt;
					}
				}
				else {
					startMinInt = endMinInt - 1;
					startHourInt = endHourInt;
					startDayInt = endDayInt;
					startMonthInt = endMonthInt;
					startYearInt = endYearInt;
				}
			}		
			String startYearStr = String.valueOf(startYearInt);
			String startMonthStr = String.valueOf(startMonthInt);
			String startDayStr = String.valueOf(startDayInt);
			String startHourStr = String.valueOf(startHourInt);
			String startMinStr = String.valueOf(startMinInt);
			String startSecStr = String.valueOf(startSecInt);
			
			if (startMonthInt < 10) {
				startMonthStr = "0" + String.valueOf(startMonthInt);
			}
			if (startDayInt < 10) {
				startDayStr = "0" + String.valueOf(startDayInt);
			}
			if (startHourInt < 10) {
				startHourStr = "0" + String.valueOf(startHourInt);
			}
			if (startMinInt < 10) {
				startMinStr = "0" + String.valueOf(startMinInt);
			}
			if (startSecInt < 10) {
				startSecStr = "0" + String.valueOf(startSecInt);
			}		
			String startTimeStr = startYearStr + "-" + startMonthStr + "-" + startDayStr + " " + startHourStr  
			+ ":" + startMinStr + ":" + startSecStr;
			startTimeArray[0] = startTimeStr;	
			isOK = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/**判断时间是否在起止时间之间
	 * startTimeStr:开始时间，格式2013-01-01 00:00:15
	 * endTimeArray：结束时间,格式2013-01-01 00:00:15
	 * middleTimeStr：待判断时间,格式2013-01-01 00:00:15
	 * */
	public static boolean isTimeBetweenStartEndTime(String startTimeStr, String endTimeStr, String middleTimeStr){
		boolean isOK = false;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    java.util.Date middleDate = df.parse(middleTimeStr);
		    java.util.Date endDate = df.parse(endTimeStr);
		    double timeInterval1 = middleDate.getTime() - startDate.getTime();
		    double timeInterval2 = endDate.getTime() - middleDate.getTime();
		    //区间为左闭右开型
		    if (timeInterval1 >= 0 && timeInterval2 > 0) {
				isOK = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/** parameterProcAboutTime:起止时间的参数处理函数,如果处理成功，返回参数数组
	 * 传入参数格式：
	 * time：20130101000015
	 * 数据库参数格式：
	 * time：2013-01-01 00:00:15
	 * */
	public static boolean parameterProcAboutTime(String timeStr, String[]paraArray){
		boolean isOK = false;
		try {
			String startYearStr = timeStr.substring(0, 4);
			String startMonthStr = timeStr.substring(4, 6);
			String startDayStr = timeStr.substring(6, 8);
			String startHourStr = timeStr.substring(8, 10);
			String startMinStr = timeStr.substring(10, 12);
			String startSecStr = timeStr.substring(12, 14);
			timeStr = startYearStr + "-" + startMonthStr + "-" + startDayStr + " " + startHourStr + ":" + startMinStr + ":" + startSecStr;		
			isOK = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
			System.out.print("参数错误：" + '\n');
		}
		if (isOK) {
			paraArray[0] = timeStr;
		}
		return isOK;
	}
	
	/**
	 * 此算法有问题，需要验证
	 * 2015-11-15
	 * 按照时间先后顺序进行排序
	 * 冒泡排序
	 * timeInterval	时间间隔，单位为分，比如30min
	 * "2014-01-01 "
	 * @param targetLinkDirectionWeekInfosArrayList
	 */
	public static void sortAccordTime(ArrayList<String> targetLinkDirectionWeekInfosArrayList){
		try {
			//冒泡排序
			for (int i = 0; i < targetLinkDirectionWeekInfosArrayList.size(); i++) {
				String str1 = targetLinkDirectionWeekInfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String tempTimeStr1 = tempStrArray1[5];// 时间
				for (int j = i + 1; j < targetLinkDirectionWeekInfosArrayList.size(); j++) {
					String str2 = targetLinkDirectionWeekInfosArrayList.get(j);
					String []tempStrArray2 = str2.split(",");
					String tempTimeStr2 = tempStrArray2[5];// 时间
					if (!PubClass.isTime2AfterTime1(tempTimeStr1, tempTimeStr2)) {
						String tempStr = str1;
						str1 = str2;
						targetLinkDirectionWeekInfosArrayList.set(i, str2);
						targetLinkDirectionWeekInfosArrayList.set(j, tempStr);						
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
	 * 2015-12-7
	 * 此算法验证无误
	 * 冒泡排序通用算法，对时间排序
	 * 较大的时间放在最后
	 * "2014-01-01 " 时间格式为
	 * @param timeIndex	时间所在字符串的位置，第几个位置，从零开始
	 * @param targetLinkDirectionWeekInfosArrayList
	 */
	public static void bubbleSortAccordTimeCommonMethod(int timeIndex, ArrayList<String> infosArraylist){
		try {
			//冒泡排序
			System.out.print("正在进行时间排序：" + '\n');
			int count = infosArraylist.size();
			for (int i = 0; i < count - 1; i++) {
				for (int j = 0; j < count - 1 - i; j++) {
					String str1 = infosArraylist.get(j);
					String []tempStrArray1 = str1.split(",");
					String tempTimeStr1 = tempStrArray1[timeIndex];// 时间			
					String str2 = infosArraylist.get(j + 1);
					String []tempStrArray2 = str2.split(",");
					String tempTimeStr2 = tempStrArray2[timeIndex];// 时间
					//tempTimeStr1时间在tempTimeStr2时间之后
					if (!PubClass.isTime2AfterTime1(tempTimeStr1, tempTimeStr2)) {
						String tempStr = str1;
						str1 = str2;
						infosArraylist.set(j, str2);
						infosArraylist.set(j + 1, tempStr);						
					}
				}	
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		System.out.print("结束时间排序：" + '\n');
	}
	
	/**
	 * 
	 * 2015-11-16
	 * 历史大数据按照时间排序
	 * 按照一天的时间先后顺序进行排序
	 * 冒泡排序
	 * @param targetLinkDirectionWeekInfosArrayList
	 */
	public static void sortHistAccordTime(ArrayList<String> histweekInfosArrayList){
		try {
			//冒泡排序
			String dateStr = "2014-01-01 ";
			for (int i = 0; i < histweekInfosArrayList.size(); i++) {
				String str1 = histweekInfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String tempTimeStr1 = tempStrArray1[4];// 时间
				String proTimeStr1 = tempTimeStr1.substring(4);
				String dateTimeStr1 = dateStr + proTimeStr1;
				for (int j = i + 1; j < histweekInfosArrayList.size(); j++) {
					String str2 = histweekInfosArrayList.get(j);
					String []tempStrArray2 = str2.split(",");
					String tempTimeStr2 = tempStrArray2[4];// 时间
					String proTimeStr2 = tempTimeStr2.substring(4);
					String dateTimeStr2 = dateStr + proTimeStr2;
					if (!PubClass.isTime2AfterTime1(dateTimeStr1, dateTimeStr2)) {
						String tempStr = str1;
						str1 = str2;
						histweekInfosArrayList.set(i, str2);
						histweekInfosArrayList.set(j, tempStr);						
					}
				}				
//				String str = targetLinkDirectionWeekInfosArrayList.get(i);
//				String []tempStrArray = str.split(",");
//				String linkIDStr = tempStrArray[0];
//				String enterIDStr = tempStrArray[1];
//				String exitIDStr = tempStrArray[2];
//				String directionStr = tempStrArray[3];
//				String tempTimeStr = tempStrArray[4];// 时间
//				String proTimeStr = tempTimeStr.substring(4);
//				String speedExpectation = tempStrArray[5];
//				String speedStandardDeviationStr = tempStrArray[6];
//				String linkDegreeStr = tempStrArray[7];
//				String linkLengthStr = tempStrArray[8];			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * 
	 * 2015-11-16
	 * 实时数据按照时间排序
	 * @param realweekInfosArrayList
	 */
	public static void sortRealAccordTime(ArrayList<String> realweekInfosArrayList){
		try {
			//冒泡排序
			String dateStr = "2014-01-01 ";
			for (int i = 0; i < realweekInfosArrayList.size(); i++) {
				String str1 = realweekInfosArrayList.get(i);
				String []tempStrArray1 = str1.split(",");
				String dateTimeStr1 = tempStrArray1[4];// 时间
				for (int j = i + 1; j < realweekInfosArrayList.size(); j++) {
					String str2 = realweekInfosArrayList.get(j);
					String []tempStrArray2 = str2.split(",");
					String dateTimeStr2 = tempStrArray2[4];// 时间
					if (!PubClass.isTime2AfterTime1(dateTimeStr1, dateTimeStr2)) {
						String tempStr = str1;
						str1 = str2;
						realweekInfosArrayList.set(i, str2);
						realweekInfosArrayList.set(j, tempStr);						
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
}
