package utilityPackage;

import java.text.SimpleDateFormat;

/**
 * ʱ���������
 * @author whu
 *
 */
public class TimeOperateFunction {

	/*������ֹʱ����ʱ��
	 * 1.Ҫ��֤endTimeStrʱ�����startTimeStrʱ��
	 * 2.��ֹʱ��֮��������24Сʱ��24*60�֣�
	 * ���룺
	 * startTimeStr����ʼʱ��
	 * endTimeStr����ֹʱ��
	 * �����
	 * timeInterval:ʱ��������Ϊ��λ
	 * ʱ���ʽ2013-01-01 00:00:15*/
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
	
	/*��ֹʱ��֮��������24Сʱ��24*60�֣�*/
	public double obtainTimeInterval22(String startTimeStr, String endTimeStr){
		double timeInterval = 0;
		try {
			//��ʼʱ��
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
			//��ֹʱ��
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
			int secInterval = 0;//����
			int minInterval = 0;//���Ӽ��
			int hourInterval = 0;//Сʱ���
			if (endSecInt >= startSecInt) {
				secInterval = endSecInt - startSecInt;
				if (endMinInt >= startMinInt) {
					minInterval = endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
				else {
					//��λ
					minInterval = 60 + endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
			}
			else {
				//��λ
				secInterval = 60 + endSecInt - startSecInt;
				if ((endMinInt - 1) >= startMinInt) {
					minInterval  = endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					
				}
				else {
					//��λ
					minInterval  = 60 + endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
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
	
	/*�ж�time2Str�Ƿ���time1Str��
	 * ���ǣ�����true
	 * ���򣬷���false
	 * ʱ���ʽ2013-01-01 00:00:15
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
	
	
	/**������ʼʱ�䡢һ����ʱ����������ֹʱ��
	 * startTimeStr:��ʼʱ�䣬��ʽ2013-01-01 00:00:15
	 * endTimeArray������ʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ����������Ϊ��λ
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
	
	/* timeInterval��ʱ����������Ϊ��λ��<=60s*/
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
				System.out.print("ʱ����������ΪС��60s");
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/*������ֹʱ�䡢һ����ʱ����������ʼʱ��
	 * endTimeStr:����ʱ�䣬��ʽ2013-01-01 00:00:15
	 * startTimeArray����ʼʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ����������Ϊ��λ,С�ڵ���60s*/
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
	
	/*������ֹʱ�䡢һ����ʱ����������ʼʱ��
	 * endTimeStr:����ʱ�䣬��ʽ2013-01-01 00:00:15
	 * startTimeArray����ʼʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ�������Ժ���Ϊ��λ,С�ڵ���60s*/
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
			int startSecInt = 0;//��
			int startMinInt = 0;//��
			int startHourInt = 0;//ʱ
			int startDayInt = endDayInt;//��
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
				 //��λ
				startSecInt = 60 + endSecInt - timeInterval;
				//�����λ ֵΪ0
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
	
	/**�ж�ʱ���Ƿ�����ֹʱ��֮��
	 * startTimeStr:��ʼʱ�䣬��ʽ2013-01-01 00:00:15
	 * endTimeArray������ʱ��,��ʽ2013-01-01 00:00:15
	 * middleTimeStr�����ж�ʱ��,��ʽ2013-01-01 00:00:15
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
		    //����Ϊ����ҿ���
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
	
	/** parameterProcAboutTime:��ֹʱ��Ĳ���������,�������ɹ������ز�������
	 * ���������ʽ��
	 * time��20130101000015
	 * ���ݿ������ʽ��
	 * time��2013-01-01 00:00:15
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
			System.out.print("��������" + '\n');
		}
		if (isOK) {
			paraArray[0] = timeStr;
		}
		return isOK;
	}
	
	
	
	
	
}
