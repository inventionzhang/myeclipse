package org.lmars.network.sparseData;

import java.util.ArrayList;

import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.neuralNetwork.MergeAllTxtFileAndClassify;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;


/**
 * 数据处理程序
 * 独立的程序
 * 独立的程序
 * 临时函数
 * 临时函数
 * @author Administrator
 *
 */
public class ExtractInputCharacteristics {
	public static void main(String[] args) {
//		(new ExtractInputCharacteristics()).extractInputBetweenTwoLinksID(716, 1, 816, 1, 1);
		
		(new ExtractInputCharacteristics()).obtainCommonTimeSpeedExpectationBetweenTwoLinks(88, -1, 82, -1);		
		System.out.print("done!");
		
	}

	/**
	 * 从计算的速度期望标准差文件(speedExpectationStandardDeviation)中提取输入
	 * 根据两个路段ID提取输入特征
	 * 根据两个路段ID、两路段交通流向以及邻接路段数字编号提取输入特征
	 * @param targetLink	目标路段
	 * @param targetDirection	目标路段交通流方向
	 * @param adjacentLink	(邻接路段)
	 * @param adjacentDirection	邻接路段交通流方向
	 * @param adjacentlinkDigitVal	邻接路段数字编号
	 */
	public void extractInputBetweenTwoLinksID(int targetLink, int targetDirection, int adjacentLink, int adjacentDirection,int adjacentlinkDigitVal){
		try {
			String ANNInputPathStr = "E:\\travelTimeProcess\\ANNInput" + targetLink + "," + adjacentLink + "," + targetDirection + ".txt";
			String metaDataFilePathStr = "E:\\travelTimeProcess\\ANNInputMetaData" +  targetLink + "," + adjacentLink  + "," + targetDirection + ".txt";
			String normalizefilePathStr = "E:\\travelTimeProcess\\ANNInputNormalize" +  targetLink + "," + adjacentLink + "," + targetDirection + ".txt";
			String targetAdjacentLinkTravelTimeFilePathStr = "E:\\travelTimeProcess\\linkAdjacentTravelTimeInfos" +  targetLink + "," + adjacentLink + "," + targetDirection + ".txt"; 
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
			ArrayList<String> thursdayInfosArrayList = new ArrayList<String>();//周四
			ArrayList<String> fridayInfosArrayList = new ArrayList<String>();////周五
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
					if (j == 21) {
						System.out.print("");
					}
					ANNInput(targetLink, targetDirection, adjacentLink, adjacentDirection, adjacentlinkDigitVal, ptimeStr, inputTime, targetAdjacentLinkTravelTimeFilePathStr, inputArrayList, weekInfosArrayList, polylineCollArrayList);
					if (inputArrayList.size() != 0) {
						System.out.print("");
					}
					System.out.print(j + ":" + timeStrArray.length + '\n');
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

			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());			
		}
	}
		
	
	/**
	 * 根据目标路段ID、交通流方向、邻接路段ID、交通流方向获得ANN输入信息
	 * @param targetLinkID	目标路段ID
	 * @param targetDirection	目标路段方向
	 * @param adjacentLink	邻接路段ID
	 * @param adjacentDirection	邻接路段方向
	 * @param adjacentlinkDigitVal邻接路段编号数值化
	 * @param timeStr
	 * @param inputTime
	 * @param targetAdjacentLinkTravelTimeFilePathStr
	 * @param inputArrayList
	 * @param infosArrayList
	 * @param polylineCollArrayList
	 */
	public void ANNInput(int targetLinkID, int targetDirection, int adjacentLinkID, int adjacentDirection, int adjacentlinkDigitVal, 
			String timeStr, double inputTime, String targetAdjacentLinkTravelTimeFilePathStr, ArrayList<double[]> inputArrayList, 
			ArrayList<String> infosArrayList, ArrayList<MapMatchEdge> polylineCollArrayList) {
		try {
			//时刻、目标路段行程时间、邻接路段行程时间以及比值
			ArrayList<String> targetAdjacentLinkTravelTimeArrayList = new ArrayList<String>();
			MapMatchEdge targetEdge = PubClassSparseData.obtainTargetLinkEdge(targetLinkID, polylineCollArrayList);
			MapMatchEdge adjacentEdge = PubClassSparseData.obtainTargetLinkEdge(adjacentLinkID, polylineCollArrayList);
			int targetLinkDegree = targetEdge.getFirstLevelConnEdgeArray().size();
			double targetLinkLength = targetEdge.getEdgeLength();
			int adjacentLinkDegree = adjacentEdge.getFirstLevelConnEdgeArray().size();
			double adjacentLinkLength = adjacentEdge.getEdgeLength();
			double temp = (double)targetLinkDegree/adjacentLinkDegree;
			double similarity = Double.parseDouble(String.format("%.4f", temp));
			double lengthRate = Double.parseDouble(String.format("%.4f", targetLinkLength/adjacentLinkLength));
			double targetAdjacentLinkTravelTimeSameDirection[] = new double[3];//目标路段同方向信息   分别存储目标路段、邻接路段行程时间以及两者之间比值
			double[]expectationStandardDeviSameDirection = new double[2];//目标路段同方向 邻接路段link1ID速度期望与标准差
			obtainTravelTimeRate(targetLinkID,adjacentLinkID, targetDirection, adjacentDirection, timeStr, infosArrayList, targetAdjacentLinkTravelTimeSameDirection, expectationStandardDeviSameDirection);//行程时间比值
			double sameDirectiontravelTimeRate = targetAdjacentLinkTravelTimeSameDirection[2];
			double sameDirectionspeedExpectation = expectationStandardDeviSameDirection[0];				
			double sameDirectionspeedStandardDevi = expectationStandardDeviSameDirection[1];
			if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && 
					Math.abs(sameDirectiontravelTimeRate) > 0.000001 && Math.abs(sameDirectionspeedExpectation) > 0.000001 &&
					Math.abs(sameDirectionspeedStandardDevi) > 0.000001) {
				double[]inputInfos = new double[14];
				inputInfos[0] = targetLinkID;//目标路段ID
				inputInfos[1] = adjacentLinkID;//邻接路段ID					
				inputInfos[2] = Double.parseDouble(String.format("%.4f", targetLinkLength));//目标路段长度
				inputInfos[3] = Double.parseDouble(String.format("%.4f", adjacentLinkLength));//邻接路段长度
				inputInfos[4] = targetAdjacentLinkTravelTimeSameDirection[0];//目标路段行程时间
				inputInfos[5] = targetAdjacentLinkTravelTimeSameDirection[1];//邻接路段行程时间					
				inputInfos[6] = similarity;
				inputInfos[7] = lengthRate;
				inputInfos[8] = sameDirectionspeedExpectation;
				inputInfos[9] = sameDirectionspeedStandardDevi;
				inputInfos[10] = sameDirectiontravelTimeRate;
				inputInfos[11] = inputTime;//对应的小时数
				inputInfos[12] = adjacentlinkDigitVal;//邻接路段编号数值化
				inputInfos[13] = adjacentDirection;//邻接路段交通流方向，同向
				if (adjacentDirection == -1) {
					inputInfos[13] = 2;
				}
				inputArrayList.add(inputInfos);
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/**
	 * 根据目标路段ID、邻接路段ID、目标路段交通流向、邻接路段交通流向以及时间，获得相应交通流向的两路段行程时间比值
	 * 1.分别获得目标路段与邻接路段的交通信息
	 * 2.目标路段获得相应时段和方向的交通信息
	 * 3.邻接路段获得相同时段和相应方向的交通信息
	 * 4.
	 * 
	 * @param targetDirection 目标路段方向性
	 * @param adjacentDeriction	邻接路段方向
	 * @param link1ID	路段1ID 目标路段
	 * @param link2ID	路段2ID	邻接路段
	 * @param expectationStandardDevi	邻接路段速度期望与标准差
	 * @param infosArrayList	所有信息
	 */
	public void obtainTravelTimeRate(int link1ID, int link2ID, int targetDirection, int adjacentDeriction,  String timeStr, ArrayList<String> infosArrayList, 
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		try {
			CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation ANNInputLengthDegreeRatioExpectationDeviation = 
					new CopyOfHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation();
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
						//邻接路段相应方向和时段
						if (timeStr2.equals(timeStr) && direction2 == adjacentDeriction) {
							flag = ANNInputLengthDegreeRatioExpectationDeviation.functionProcess(timeStr1, timeStr2, 
									tempStrArray1, tempStrArray2, targetAdjacentLinkTravelTime, expectationStandardDevi);
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
	 * 获得路段1和路段2相应方向共同时段的速度期望值
	 * @param targetLink	目标路段编号
	 * @param targetDirection	目标路段方向
	 * @param adjacentLink	邻接路段编号
	 * @param adjacentDirection	邻接路段方向
	 */
	public void obtainCommonTimeSpeedExpectationBetweenTwoLinks(int targetLink, int targetDirection,int adjacentLink, int adjacentDirection){
		try {
			
			String targetAdjacentSpeedExpectationFilePathStr = "E:\\travelTimeProcess\\" +  targetLink + "," + adjacentLink + "speedExpectation" + targetDirection + ".txt"; 
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
			//按照天分类
			ArrayList<String> mondayInfosArrayList = new ArrayList<String>();//周一
			ArrayList<String> tuesInfosdayArrayList = new ArrayList<String>();//周二
			ArrayList<String> wednesInfosdayArrayList = new ArrayList<String>();//周三
			ArrayList<String> thursdayInfosArrayList = new ArrayList<String>();//周四
			ArrayList<String> fridayInfosArrayList = new ArrayList<String>();////周五
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
			ArrayList<String[]> allInputArrayList = new ArrayList<String[]>();
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
				ArrayList<String[]> inputArrayList = new ArrayList<String[]>();
				for (int j = 0; j < timeStrArray.length; j++) {
					timeStr = timeStrArray[j];
					String ptimeStr = weekdayStr + timeStr;
					if (j == 21) {
						System.out.print("");
					}
					double[]targetAdjacentSpeedExpectation = new double[2];//目标路段、邻接路段速度期望值
					obtainSpeedExpectation(targetLink, adjacentLink, targetDirection, adjacentDirection, ptimeStr, weekInfosArrayList, targetAdjacentSpeedExpectation);					
					if (targetAdjacentSpeedExpectation[0] != 0 && targetAdjacentSpeedExpectation[1] != 0) {
						String tempStr[] = new String[3];
						tempStr[0] = ptimeStr;
						tempStr[1] = String.valueOf(targetAdjacentSpeedExpectation[0]);
						tempStr[2] = String.valueOf(targetAdjacentSpeedExpectation[1]);	
						inputArrayList.add(tempStr);
						System.out.print(j + ":" + timeStrArray.length + '\n');
					}					
				}
				for (int j = 0; j < inputArrayList.size(); j++) {
					String[] temp = inputArrayList.get(j);
					String[] proTemp = new String[3];
					proTemp[0] = temp[0];//时间
					proTemp[1] = temp[1];//目标路段速度期望		
					proTemp[2] = temp[2];//邻接路段速度期望	
					allInputArrayList.add(proTemp);				
				}						
			}
			String targetLinkExpectationStr = targetLink + "expectation";
			String adjacentLinkExpectationStr = adjacentLink + "expectation";
			String headheadDescriptionStr = "timeStr" + "," + targetLinkExpectationStr + "," + adjacentLinkExpectationStr + "\r\n";			
			FileOperateFunction.writeStringArrayToTxtFile(targetAdjacentSpeedExpectationFilePathStr, headheadDescriptionStr, allInputArrayList);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public void obtainSpeedExpectation(int link1ID, int link2ID, int targetDirection, int adjacentDeriction, String timeStr,
			ArrayList<String> infosArrayList, double[]speedExpectation) {
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
						//邻接路段相应方向和时段
						if (timeStr2.equals(timeStr) && direction2 == adjacentDeriction) {
							double speedExpe1 = Double.parseDouble(tempStrArray1[5]);
							double speedExpe2 = Double.parseDouble(tempStrArray2[5]);
							double prospeedExpe1 = Double.parseDouble(String.format("%.2f", speedExpe1));
							double prospeedExpe2 = Double.parseDouble(String.format("%.2f", speedExpe2));
							speedExpectation[0] = prospeedExpe1;
							speedExpectation[1] = prospeedExpe2;
							flag = true;
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
	
	
	
	
}




