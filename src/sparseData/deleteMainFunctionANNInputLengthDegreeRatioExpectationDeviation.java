package sparseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.mail.search.FromStringTerm;

import org.artificialNeuralNetwork.AssistFunctionNeuralNetwork;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;

import utilityPackage.FileOperateFunction;
import utilityPackage.PubClass;

import entity.PropertiesUtilJAR;

/**
 * 数据稀疏性问题：
 * 用神经网络进行稀疏路段形成时间预测
 * @author whu
 *
 */
public class deleteMainFunctionANNInputLengthDegreeRatioExpectationDeviation {

	public static void main(String[] args) {
		new deleteMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkTravelCharacteristicCalculation();
//		new MainFunctionANN().ANNInput(12, "10:30:00", "");
		
		new deleteMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkANNInputAndNormalizeInput();
		
//		new MainFunctionANN().mergeANNInputTxtFileAndNormalize();
//		new AssistFunctionNeuralNetwork().obtainTargetAndAdjacentLinkSpeedExpectationDeviation(77);
		System.out.print("done!");
		System.exit(0);//是正常退出程序
		
	}
	
	/**
	 * 获得所有路段神经网络输入信息以及归一化输入信息
	 */
	public void allLinkANNInputAndNormalizeInput() {
//		Integer[]IDArray = {12,21,25,33,44,52,58,66,72,77,82,87,88};
		Integer[]IDArray = {82};
		for (int i = 0; i < IDArray.length; i++) {
			int linkID = IDArray[i];
			new deleteMainFunctionANNInputLengthDegreeRatioExpectationDeviation().obtainANNInputAndNormalizeInput(linkID);
		}
		System.out.print("done!");
	}
	
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
		String ANNInputPathStr = "C:\\travelTimeProcess\\ANNInput" + linkID + ".txt";
		String metaDataFilePathStr = "C:\\travelTimeProcess\\ANNInputMetaData" + linkID + ".txt";
		String normalizefilePathStr = "C:\\travelTimeProcess\\ANNInputNormalize" + linkID + ".txt";
		String targetAdjacentLinkTravelTimeFilePathStr = "C:\\travelTimeProcess\\linkAdjacentTravelTimeInfos" + linkID + ".txt"; 
		String headDescriptionStr = "timeStr" + "," + "adjacentLinkID" + "," + "linkTravelTime" + "," + "adjacentLinkTravelTime" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.writeHeadDescriptionToTxtFile(targetAdjacentLinkTravelTimeFilePathStr, headDescriptionStr);
		String timeStr = "";
		String []timeStrArray = {"00:00:00","00:30:00","01:00:00","01:30:00","02:00:00","02:30:00",
								 "03:00:00","03:30:00","04:00:00","04:30:00","05:00:00","05:30:00",
								 "06:00:00","06:30:00","07:00:00","07:30:00","08:00:00","08:30:00",
								 "09:00:00","09:30:00","10:00:00","10:30:00","11:00:00","11:30:00",
								 "12:00:00","12:30:00","13:00:00","13:30:00","14:00:00","14:30:00",
								 "15:00:00","15:30:00","16:00:00","16:30:00","17:00:00","17:30:00",
								 "18:00:00","18:30:00","19:00:00","19:30:00","20:00:00","20:30:00",
								 "21:00:00","21:30:00","22:00:00","22:30:00","23:00:00","23:30:00"};
		ArrayList<double[]> inputArrayList = new ArrayList<double[]>();
		for (int i = 0; i < timeStrArray.length; i++) {
			timeStr = timeStrArray[i];
			ANNInput(linkID, timeStr, targetAdjacentLinkTravelTimeFilePathStr, inputArrayList);
		}
		String headheadDescriptionStr = "similarity " + "," + "lengthRate" + "," + "speedExpectation" + "," + "speedStandardDevi" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, inputArrayList);
		new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
	}
	
	/**
	 * 获得某路段、某时间神经网络的输入信息
	 * 输入：
	 * 路段与邻接路段度数比值
	 * 路段与邻接路段长度比值
	 * 邻接路段速度期望、速度标准差
	 * 输出：
	 * 路段与邻接路段行程时间比值
	 */
	public void ANNInput(int targetLinkID, String timeStr, String targetAdjacentLinkTravelTimeFilePathStr, ArrayList<double[]> inputArrayList) {
		try {
			//时刻、目标路段行程时间、邻接路段行程时间以及比值
			ArrayList<String> targetAdjacentLinkTravelTimeArrayList = new ArrayList<String>();
			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList; 
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
				MapMatchEdge tEdge2 = new MapMatchEdge();
				int link1ID = -1;
				int link1Degree = -1;
				double link1Length = -1;
				int link2ID = -1;
				int link2Degree = -1;
				double link2Length = -1;
				double pearsonCorr = -1;
				if (i == relaEdgeCount - 1) {
					tEdge1 = relaEdgeArrayList.get(relaEdgeCount - 1);
					tEdge2 = relaEdgeArrayList.get(0);
					link1ID = tEdge1.getEdgeID();
					link1Degree = tEdge1.getFirstLevelConnEdgeArray().size();
					link1Length = tEdge1.getEdgeLength();
					link2ID = tEdge2.getEdgeID();
					link2Degree = tEdge2.getFirstLevelConnEdgeArray().size();
					link2Length = tEdge2.getEdgeLength();			
					pearsonCorr = pearsonCorrCalculation(link1ID, link2ID, timeStr, infosArrayList);
					
				}
				else {
					tEdge1 = relaEdgeArrayList.get(i);
					tEdge2 = relaEdgeArrayList.get(i + 1);
					link1ID = tEdge1.getEdgeID();
					link1Degree = tEdge1.getFirstLevelConnEdgeArray().size();
					link1Length = tEdge1.getEdgeLength();
					link2ID = tEdge2.getEdgeID();
					link2Degree = tEdge2.getFirstLevelConnEdgeArray().size();
					link2Length = tEdge2.getEdgeLength();			
					pearsonCorr = pearsonCorrCalculation(link1ID, link2ID, timeStr, infosArrayList);
				}
				double temp = (double)targetLinkDegree/link1Degree;
				double similarity = Double.parseDouble(String.format("%.4f", temp));
				double lengthRate = Double.parseDouble(String.format("%.4f", targetLinkLength/link1Length));
				double targetAdjacentLinkTravelTime[] = new double[3];//目标路段、邻接路段行程时间以及两者之间比值
				double travelTimeRate = obtainTravelTimeRate(targetLinkID,link1ID, timeStr, infosArrayList, targetAdjacentLinkTravelTime);//行程时间比值
				double[]expectationStandardDevi = new double[2];//速度期望与标准差
				obtainExpectationStandDeviAccordLinkID(link1ID, timeStr, infosArrayList, expectationStandardDevi);
				double speedExpectation = expectationStandardDevi[0];
				double speedStandardDevi = expectationStandardDevi[1];
				if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && Math.abs(pearsonCorr) > 0.000001 && 
						Math.abs(travelTimeRate) > 0.000001 && Math.abs(speedExpectation) > 0.000001 && Math.abs(speedStandardDevi) > 0.000001) {
					double[]inputInfos = new double[5];
					inputInfos[0] = similarity;
					inputInfos[1] = lengthRate;
					inputInfos[2] = speedExpectation;
					inputInfos[3] = speedStandardDevi;
					inputInfos[4] = travelTimeRate;					
					inputArrayList.add(inputInfos);
					String str = timeStr + "," + String.valueOf(link1ID) + "," + targetAdjacentLinkTravelTime[0] + "," + targetAdjacentLinkTravelTime[1] + "," + targetAdjacentLinkTravelTime[2];					
					targetAdjacentLinkTravelTimeArrayList.add(str);
				}
			}			
			FileOperateFunction.writeStringArraylistToEndTxtFile(targetAdjacentLinkTravelTimeFilePathStr, targetAdjacentLinkTravelTimeArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	//某一路段的速度期望计算
	public void expectedSpeedcalculation(int linkID, ArrayList<String> infosArrayList, Map<String, Double> expectedSpeedMap) {
		try {		
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			String startDateTimeStr = "2013-06-01 00:00:00";
			String endDateTimeStr = "2013-06-02 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
			int timeInterval = 1800;//统计每隔6小时的路段出租车通行时间
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
				assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(linkTravelInfos, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);		
				double expectedSpeed = assistFunctionNeuralNetwork.calculateLinkSpeedExpectation(linkTravelInfosInTimeIntervalArrayList);
				System.out.print(startTimeStr + ",");
				tempStartDateTimeStr = endTimeArray[0];
				expectedSpeedMap.put(startTimeStr, expectedSpeed);
				System.out.print(expectedSpeed + ":" + '\n');
			}
			System.out.print("");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 计算所有路段速度期望、速度方差
	 */
	public void allLinkTravelCharacteristicCalculation() {
		try {
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			ArrayList<LinkTravelCharacteristic> allLinkSpeedExpectationStandardDeviationArrayList = new ArrayList<LinkTravelCharacteristic>();
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);				
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				System.out.print("路段期望、标准差计算：" + i + ":" + polylineCollArrayList.size() + '\n');
				MapMatchEdge edge = polylineCollArrayList.get(i);
				int linkID = edge.getEdgeID();
				if (linkID == 87) {
					System.out.print("");
				}
				int linkDegree = edge.getFirstLevelConnEdgeArray().size();
				double tempLinkLength = edge.getEdgeLength();
				String tempLinkLengthStr = String.format("%.2f", tempLinkLength);
				double processLinkLength = Double.parseDouble(tempLinkLengthStr);				
				ArrayList<LinkTravelCharacteristic> linkSpeedExpectationStandardDeviationArrayList = new ArrayList<LinkTravelCharacteristic>();
				speedDeviationCalculation(linkID, linkSpeedExpectationStandardDeviationArrayList,infosArrayList);
				for (int j = 0; j < linkSpeedExpectationStandardDeviationArrayList.size(); j++) {
					LinkTravelCharacteristic linkTravelCharacteristic = linkSpeedExpectationStandardDeviationArrayList.get(j);
					linkTravelCharacteristic.setLinkDegree(linkDegree);
					linkTravelCharacteristic.setLinkLength(processLinkLength);
					allLinkSpeedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
				}
			}
			System.out.print("信息开始写入txt文件：" + '\n');
			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
			String headDescriptionStr = "linkID" + "," + "timeStr" + "," + "speedExpectation" + "," + "speedStandardDeviation" + "," + "linkDegree" + "," + "linkLength" + "\r\n";
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
	 * 某一路段速度期望、方差计算
	 * @param linkID
	 * @param speedExpectationStandardDeviationArrayList	路段通行特征
	 */
	public void speedDeviationCalculation(int linkID, ArrayList<LinkTravelCharacteristic> speedExpectationStandardDeviationArrayList,ArrayList<String> infosArrayList) {
		try {
			Map<String, Double> expectedSpeedMap = new HashMap<String, Double>();
			expectedSpeedcalculation(linkID,infosArrayList,expectedSpeedMap);
			ArrayList<String> linkTravelInfos = new ArrayList<String>();
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID,infosArrayList,linkTravelInfos);
			String startDateTimeStr = "2013-06-01 00:00:00";
			String endDateTimeStr = "2013-06-02 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
			while (!tempStartDateTimeStr.equals(endDateTimeStr)) {
				LinkTravelCharacteristic linkTravelCharacteristic = new LinkTravelCharacteristic();
				int timeInterval = 1800;//统计每隔半小时的路段出租车通行时间
				String[] endTimeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(tempStartDateTimeStr, timeInterval, endTimeArray);
				String startTimeStr = tempStartDateTimeStr.substring(11);
				String endTimeStr = endTimeArray[0].substring(11);
				if (startTimeStr.equals("23:30:00")) {
					endTimeStr = "23:59:59";					
				}
				ArrayList<String> linkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
				assistFunctionNeuralNetwork.obtainTravelInfosBetweenStartAndEndTime(linkTravelInfos, linkTravelInfosInTimeIntervalArrayList, startTimeStr, endTimeStr);	
				double expectedSpeed = expectedSpeedMap.get(startTimeStr);
				double speedStandardDeviation = assistFunctionNeuralNetwork.calculateLinkSpeedStandardDeviation(expectedSpeed, linkTravelInfosInTimeIntervalArrayList);
				System.out.print(startTimeStr + ",");
				tempStartDateTimeStr = endTimeArray[0];
				System.out.print(expectedSpeed + "," + speedStandardDeviation + '\n');
				linkTravelCharacteristic.setLinkID(linkID);
				linkTravelCharacteristic.setTimeStr(startTimeStr);
				linkTravelCharacteristic.setSpeedExpectation(expectedSpeed);
				linkTravelCharacteristic.setSpeedStandDeviation(speedStandardDeviation);
				speedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 获得两路段行程时间比值
	 * @param link1ID	路段1 ID
	 * @param link2ID	路段2 ID
	 * @param infosArrayList	所有信息
	 */
	public double obtainTravelTimeRate(int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList, double targetAdjacentLinkTravelTime[]) {
		double travelTimeRate = 0;
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
				String timeStr1 = tempStrArray1[1];
				if (timeStr1.equals(timeStr)) {
					for (int j = 0; j < link2InfosArrayList.size(); j++) {
						String str2 = link2InfosArrayList.get(j);
						String []tempStrArray2 = str2.split(",");
						String timeStr2 = tempStrArray2[1];
						//同一时间的相关信息
						if (timeStr1.equals(timeStr2)) {
							String speedExpeStr1 = tempStrArray1[2];
							String linkLengthStr1 = tempStrArray1[5];
							String speedExpeStr2 = tempStrArray2[2];
							String linkLengthStr2 = tempStrArray2[5];							
							double speedExpe1 = Double.parseDouble(speedExpeStr1);
							double linkLength1 = Double.parseDouble(linkLengthStr1);							
							double speedExpe2 = Double.parseDouble(speedExpeStr2);
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
									flag = true;
									break;
								}
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
		return travelTimeRate;
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
					String speedStandardDeviStr1 = tempStrArray1[3];
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
	
}
