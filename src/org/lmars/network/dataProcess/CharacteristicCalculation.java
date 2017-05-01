package org.lmars.network.dataProcess;

import java.util.ArrayList;

/**
 * 特征的计算，包括
 * 1.速度期望
 * 2.速度方差
 * @author Administrator
 *
 */
public class CharacteristicCalculation {
	
	/**
	 * 根据路段ID计算某路段的速度期望值
	 * 根据时间加权，求速度期望值
	 * @param linkTravelInfos	路段通行信息
	 * @return
	 */
	public static double calculateLinkSpeedExpectation(ArrayList<String> linkTravelInfos){
		double expectedSpeed = -1;
		double tempExpectedSpeed = 0;
		try {
			double totalTime = 0;
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				double travelTime = Double.parseDouble(tempArrayStr[6]);
				totalTime = totalTime + travelTime;
			}
			if (totalTime != 0) {
				for (int i = 0; i < linkTravelInfos.size(); i++) {
					String str = linkTravelInfos.get(i);
					String[]tempArrayStr = str.split(",");
					double travelTime = Double.parseDouble(tempArrayStr[6]);
					double meanSpeed = Double.parseDouble(tempArrayStr[7]);
					String tempMeanSpeed = String.format("%.2f", meanSpeed*(travelTime/totalTime));//保留小数点后两位，并进行四舍五入
					tempExpectedSpeed = tempExpectedSpeed + Double.parseDouble(tempMeanSpeed);					
				}
				String temp = String.format("%.2f", tempExpectedSpeed);//只保留整数部分
				expectedSpeed = Double.parseDouble(temp);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
		return expectedSpeed;
	}
	
	/**
	 * 所有速度平均值作为速度期望值
	 * @param linkTravelInfos	路段通行信息
	 * @return
	 */
	public static double calculateLinkSpeedExpectation22(ArrayList<String> linkTravelInfos){
		double expectedSpeed = 0;
		double tempExpectedSpeed = 0;
		int count = linkTravelInfos.size();
		try {
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				double meanSpeed = Double.parseDouble(tempArrayStr[7]);
				String tempMeanSpeed = String.format("%.2f", meanSpeed);//保留小数点后两位，并进行四舍五入
				tempExpectedSpeed = tempExpectedSpeed + Double.parseDouble(tempMeanSpeed);							
			}
			String temp = String.format("%.2f", tempExpectedSpeed/count);//只保留整数部分
			expectedSpeed = Double.parseDouble(temp);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
		return expectedSpeed;
	}
		
	/**
	 * * 计算速度标准差
	 * 根据时间加权，求速度标准差
	 * @param expectedSpeed
	 * @param linkTravelInfos	路段通行信息
	 * @return
	 */
	public static double calculateLinkSpeedStandardDeviation(double expectedSpeed, ArrayList<String> linkTravelInfos){
		double speedStandardDeviation = 0;
		double speedDeviation = 0;
		try {
			double totalTime = 0;
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				double travelTime = Double.parseDouble(tempArrayStr[6]);
				totalTime = totalTime + travelTime;
			}
			if (totalTime != 0) {
				for (int i = 0; i < linkTravelInfos.size(); i++) {
					String str = linkTravelInfos.get(i);
					String[]tempArrayStr = str.split(",");
					double travelTime = Double.parseDouble(tempArrayStr[6]);
					double meanSpeed = Double.parseDouble(tempArrayStr[7]);
					double tempSpeedDeviation = Math.pow((meanSpeed - expectedSpeed),2)*(travelTime/totalTime);					
					speedDeviation = speedDeviation + tempSpeedDeviation;					
				}
				String temp = String.format("%.2f", Math.sqrt(speedDeviation));//保留小数点后两位，并进行四舍五入
				speedStandardDeviation = Double.parseDouble(temp);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
		return speedStandardDeviation;
	}
	
	/**
	 * 计算速度标准差
	 * 根据时间加权，求速度标准差
	 * @param expectedSpeed
	 * @param linkTravelInfos	路段通行信息
	 * @return
	 */
	public static double calculateLinkSpeedStandardDeviation22(double expectedSpeed, ArrayList<String> linkTravelInfos){
		double speedStandardDeviation = 0;
		double speedDeviation = 0;
		int count = linkTravelInfos.size();
		try {
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				double meanSpeed = Double.parseDouble(tempArrayStr[7]);
				double tempSpeedDeviation = Math.pow((meanSpeed - expectedSpeed),2);
				speedDeviation = speedDeviation + tempSpeedDeviation;
			}
			String temp = String.format("%.2f", Math.sqrt(speedDeviation)/count);//保留小数点后两位，并进行四舍五入
			speedStandardDeviation = Double.parseDouble(temp);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
		return speedStandardDeviation;
	}
	

}
