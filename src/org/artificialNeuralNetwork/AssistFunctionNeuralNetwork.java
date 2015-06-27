package org.artificialNeuralNetwork;

import java.io.File;
import java.util.ArrayList;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import utilityPackage.FileOperateFunction;
import utilityPackage.PubClass;
import utilityPackage.TimeOperateFunction;

import entity.PropertiesUtilJAR;

public class AssistFunctionNeuralNetwork {

	/**
	 * ����·��ID����ĳ·�ε��ٶ�����ֵ
	 * @param linkTravelInfos	·��ͨ����Ϣ
	 * @return
	 */
	public double calculateLinkSpeedExpectation(ArrayList<String> linkTravelInfos){
		double expectedSpeed = 0;
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
					String tempMeanSpeed = String.format("%.2f", meanSpeed*(travelTime/totalTime));//����С�������λ����������������
					tempExpectedSpeed = tempExpectedSpeed + Double.parseDouble(tempMeanSpeed);					
				}
				String temp = String.format("%.2f", tempExpectedSpeed);//ֻ������������
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
	 * �����ٶȱ�׼��
	 * @param linkTravelInfos
	 * @return
	 */
	public double calculateLinkSpeedStandardDeviation(double expectedSpeed, ArrayList<String> linkTravelInfos){
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
				String temp = String.format("%.2f", Math.sqrt(speedDeviation));//����С�������λ����������������
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
	 * ��������������ȼ�����������pearsonϵ��
	 * @param expectation1
	 * @param deviation1
	 * @param degree1
	 * @param expectation2
	 * @param deviation2
	 * @param degree2
	 */
	public double calculatePearsonCorrelation(double expectation1, double deviation1, int degree1, double expectation2, double deviation2, int degree2) {
		double pearsonCorr = 0;
		try {
			double meanVal1 = (expectation1 + deviation1 + degree1)/3;
			double meanVal2 = (expectation2 + deviation2 + degree2)/3;
			double temp1 = (expectation1 - meanVal1) * (expectation2 - meanVal2) + (deviation1 - meanVal1) * (deviation2 - meanVal2) + (degree1 - meanVal1)*(degree2 - meanVal2);
			double temp2 = Math.sqrt(Math.pow(expectation1 - meanVal1, 2) + Math.pow(deviation1 - meanVal1, 2) + Math.pow(degree1 - meanVal1, 2));
			double temp3 = Math.sqrt(Math.pow(expectation2 - meanVal2, 2) + Math.pow(deviation2 - meanVal2, 2) + Math.pow(degree2 - meanVal2, 2));
			double tempPearsonCorr = temp1/(temp2 * temp3);
			String tempPearsonCorrStr = String.format("%.2f", tempPearsonCorr);//����С�������λ����������������
			pearsonCorr = Double.parseDouble(tempPearsonCorrStr);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return pearsonCorr;
	}
	
	/**
	 * ����·��ID���·��ͨ�������Ϣ
	 * @param linkID
	 */
	public void obtainTargetLinkTravelInfosFromTxt(int linkID,ArrayList<String> infosArrayList, ArrayList<String> linkTravelInfos) {
		try {		
			obtainLinkTravelInfosAccordLinkID(infosArrayList, linkTravelInfos, linkID);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ���Ŀ�����ڣ�ĳ�죩����Ϣ
	 * @param dateStr	���ڣ���2014-06-02
	 * @param infosArrayList
	 * @param targetTimeTravelInfos
	 */
	public void obtainTargetDateTravelInfosFromTxt(String dateStr, ArrayList<String> infosArrayList, ArrayList<String> targetDateTravelInfos) {
		try {	
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[3].substring(0,10);
				if (dateStr.equals(tempDateStr)) {
					targetDateTravelInfos.add(str);
				}			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/**
	 * ����·��ID���·��ͨ����Ϣ
	 * @param infosArrayList	·������ͨ����Ϣ
	 * @param linkTravelInfos	����ĳ·�ε�ͨ����Ϣ
	 * @param linkID
	 */
	public void obtainLinkTravelInfosAccordLinkID(ArrayList<String> infosArrayList, ArrayList<String> linkTravelInfos, 
			int linkID) {
		try {
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[]tempArrayStr = str.split(",");
				int tempLinkID = Integer.parseInt(tempArrayStr[0]);
				if (linkID == tempLinkID) {
					linkTravelInfos.add(str);	
					System.out.print(str + "\n");
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ����ʱ����������򣬰�ʱ���������У���"2013-06-01 00:00:00","2013-06-01 01:00:00"
	 * @param infosArrayList	ԭʼ����
	 * @param sortInfosArrayList	���������
	 */
	public void sortByTime(ArrayList<String> infosArrayList, ArrayList<String> sortInfosArrayList){
		try {
			int count = infosArrayList.size();
			String minTimeStr = infosArrayList.get(0);
			String maxTimeStr = infosArrayList.get(0);
			for (int i = 0; i < count; i++) {
				
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/**
	 * ���ʱ�����ڵ���Ϣ
	 * @param linkTravelInfos
	 * @param linkTimeIntervalInfosArrayList	����ʱ��������Ϣ
	 * @param startTimeStr	��ʼʱ�䣬��ʽΪ��ʱ���֣���,�磺08:10:10
	 * @param endTimeStr	����ʱ��
	 */
	public void obtainTravelInfosBetweenStartAndEndTime(ArrayList<String> linkTravelInfos, ArrayList<String> linkTimeIntervalInfosArrayList, 
			String startTimeStr, String endTimeStr){
		try {
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String middleTimeStr = tempArrayStr[5];
				String yearMonthDayStr = middleTimeStr.substring(0, 10);
				String tempStartTimeStr = yearMonthDayStr + " " + startTimeStr;
				String tempEndTimeStr = yearMonthDayStr + " " + endTimeStr;
				if (TimeOperateFunction.isTimeBetweenStartEndTime(tempStartTimeStr, tempEndTimeStr, middleTimeStr)) {
					linkTimeIntervalInfosArrayList.add(str);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ���ļ��ж�ȡĳ·�������뷽����Ϣ��д��txt�ļ�
	 * @param readFilePathStr	���ļ�·��
	 * @param linkID	·��ID
	 * @param writeFilePathStr	д�ļ�·��
	 */
	public void obtainTargetAndAdjacentLinkSpeedExpectationDeviation(int targetLinkID) {
		try {
			String readFilePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
			
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList; 
			MapMatchEdge targetEdge = new MapMatchEdge();
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				targetEdge = polylineCollArrayList.get(i);
				if (targetEdge.getEdgeID() == targetLinkID) {
					break;
				}
			}
			ArrayList<MapMatchEdge> relaEdgeArrayList = targetEdge.getFirstLevelConnEdgeArray();
			int relaEdgeCount = relaEdgeArrayList.size();
			ArrayList<String> targetAdjacentInfosArrayList = new ArrayList<String>();//Ŀ��·�κ��ڽ�·����Ϣ
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(readFilePathStr, infosArrayList);
			String targetLinkIDStr = String.valueOf(targetLinkID);
			for (int j = 0; j < infosArrayList.size(); j++) {
				String str = infosArrayList.get(j);
				String []tempArray = str.split(",");
				String tempLinkIDStr = tempArray[0];
				if (tempLinkIDStr.equals(targetLinkIDStr)) {
					targetAdjacentInfosArrayList.add(str);	
				}
			}
			String writeFilePathStr = "C:\\travelTimeProcess\\speedExpectationStandardDeviation" + targetLinkID + ".txt";
			String headDescriptionStr = "linkID" + "," + "timeStr" + "," + "speedExpectation" + "," + "speedStandardDeviation" + "," + "linkDegree" + "," + "linkLength" + "\r\n";
			FileOperateFunction.writeStringArraylistToTxtFile(writeFilePathStr, headDescriptionStr, targetAdjacentInfosArrayList);
			for (int i = 0; i < relaEdgeCount; i++) {
				int linkID = relaEdgeArrayList.get(i).getEdgeID();
				String linkIDStr = String.valueOf(linkID);
				targetAdjacentInfosArrayList = new ArrayList<String>();
				for (int j = 0; j < infosArrayList.size(); j++) {
					String str = infosArrayList.get(j);
					String []tempArray = str.split(",");
					String tempLinkIDStr = tempArray[0];
					if (tempLinkIDStr.equals(linkIDStr)) {
						targetAdjacentInfosArrayList.add(str);	
					}
				}
				writeFilePathStr = "C:\\travelTimeProcess\\speedExpectationStandardDeviation" + linkID + ".txt";
				headDescriptionStr = "linkID" + "," + "timeStr" + "," + "speedExpectation" + "," + "speedStandardDeviation" + "," + "linkDegree" + "," + "linkLength" + "\r\n";
				FileOperateFunction.writeStringArraylistToTxtFile(writeFilePathStr, headDescriptionStr, targetAdjacentInfosArrayList);
			}
			
			
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public void name() {
		
	}
	
}
