package org.lmars.network.neuralNetwork;

import java.io.File;
import java.util.ArrayList;

import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.TimeOperateFunction;


import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;



public class AssistFunctionNeuralNetwork {

	
	
	
	/**
	 * 根据期望、方差、度计算两向量间pearson系数
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
			String tempPearsonCorrStr = String.format("%.2f", tempPearsonCorr);//保留小数点后四位，并进行四舍五入
			pearsonCorr = Double.parseDouble(tempPearsonCorrStr);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return pearsonCorr;
	}
	
	/**
	 * 根据路段ID获得路段通行情况信息
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
	 * 获得目标日期（某天）的信息
	 * @param dateStr	日期，如2014-06-02
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
	 * 根据路段ID获得路段通行信息
	 * @param infosArrayList	路段所有通行信息
	 * @param linkTravelInfos	返回某路段的通行信息
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
	 * 根据时间对数据排序，按时间升序排列，如"2013-06-01 00:00:00","2013-06-01 01:00:00"
	 * @param infosArrayList	原始数据
	 * @param sortInfosArrayList	排序后数据
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
	 * 获得时间间隔内的信息
	 * @param linkTravelInfos
	 * @param linkTimeIntervalInfosArrayList	返回时间间隔内信息
	 * @param startTimeStr	开始时间，格式为：时：分：秒,如：08:10:10
	 * @param endTimeStr	结束时间
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
	 * 从文件中读取某路段期望与方差信息并写入txt文件
	 * @param readFilePathStr	读文件路径
	 * @param linkID	路段ID
	 * @param writeFilePathStr	写文件路径
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
			ArrayList<String> targetAdjacentInfosArrayList = new ArrayList<String>();//目标路段和邻接路段信息
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
