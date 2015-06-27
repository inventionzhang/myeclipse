package sparseData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.search.FromStringTerm;
import org.artificialNeuralNetwork.AssistFunctionNeuralNetwork;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;

import utilityPackage.FileOperateFunction;
import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import entity.PropertiesUtilJAR;

/**
 * ����ϡ�������⣺
 * �����������ϡ��·���г�ʱ��Ԥ�⣺
 * ����Ϊ������ͳ�ƣ�ͬΪ��һ���ܶ������ݰ���ÿ���Сʱ�ڡ�ͬһ��ʻ����������������뷽����ǹ����գ���һ�����壬�����Ͽɻ������48*5=240��
 * 
 * ���룺
 * �����
 * @author whu
 *
 */
public class deleteHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation {

	public static void main(String[] args) {
//		new HalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().obtainLinkTravelInfosOnWeekdays(88);//���Ŀ��·������һ���������ĳ�������
//		new HalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkTravelCharacteristicCalculation();//��������·���ٶ��������ٶȱ�׼��		
		new deleteHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().allLinkANNInputAndNormalizeInput();
		
		
		
//		//���ݹ�һ������
//		String ANNInputPathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88.txt";
//		String metaDataFilePathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88MetaData.txt";
//		String normalizefilePathStr = "C:\\travelTimeProcess\\ANNInput82AdjacentLink88Normalize.txt";
//		new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
		
		
		
//		new MainFunctionANN().mergeANNInputTxtFileAndNormalize();
//		new AssistFunctionNeuralNetwork().obtainTargetAndAdjacentLinkSpeedExpectationDeviation(77);
		System.out.print("done!");
		System.exit(0);//�������˳�����		
	}
	
	/**
	 * �������·��������������Ϣ�Լ���һ��������Ϣ
	 */
	public void allLinkANNInputAndNormalizeInput() {
//		Integer[]IDArray = {12,21,25,33,44,52,58,66,72,77,82,87,88};
		Integer[]IDArray = {82};
		for (int i = 0; i < IDArray.length; i++) {
			int linkID = IDArray[i];
			new deleteHalfHourAdaptAdaptMainFunctionANNInputLengthDegreeRatioExpectationDeviation().obtainANNInputAndNormalizeInput(linkID);
		}
		System.out.print("done!");
	}
	
	/**
	 * �ϲ������������ļ�Ϊһ���ļ�
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
	 * ���������ĳһ·��������Ϣ����һ����������Ϣ
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
		//ÿ��30min��ʱ����
		ArrayList<String> timeArraylist = new ArrayList<String>();
		String startDateTimeStr = "2014-06-01 00:00:00";
		String endDateTimeStr = "2014-06-02 00:00:00";
		String tempStartDateTimeStr = startDateTimeStr;
		int timeInterval = 1800;//ͳ��ÿ��30min
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
		ArrayList<String> infosArrayList = new ArrayList<String>();//�ٶ��������ٶȷ���
		FileOperateFunction.readFromTxtFile(filePathStr, infosArrayList);
		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList; 
		//���������
		ArrayList<String> mondayInfosArrayList = new ArrayList<String>();//��һ
		ArrayList<String> tuesInfosdayArrayList = new ArrayList<String>();//�ܶ�
		ArrayList<String> wednesInfosdayArrayList = new ArrayList<String>();//����
		ArrayList<String> thursdayInfosArrayList = new ArrayList<String>();
		ArrayList<String> fridayInfosArrayList = new ArrayList<String>();
		ArrayList<String> weekInfosArrayList = new ArrayList<String>();//�����ܷ�������Ϣ
		String weekdayStr = "";
		for (int i = 0; i < infosArrayList.size(); i++) {
			String str = infosArrayList.get(i);
			String[]tempArray = str.split(",");
			String dayStr = tempArray[2].substring(0,4);
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
		//�����գ���һ������
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
				String ptimeStr = weekdayStr + timeStr;
				ANNInput(linkID, ptimeStr, targetAdjacentLinkTravelTimeFilePathStr, inputArrayList, weekInfosArrayList, polylineCollArrayList);
				System.out.print(j + ":" + timeStrArray.length);
			}
			for (int j = 0; j < inputArrayList.size(); j++) {
				double[] temp = inputArrayList.get(j);
				allInputArrayList.add(temp);
			}						
		}
		String headheadDescriptionStr = "similarity " + "," + "lengthRate" + "," + "speedExpectation" + "," + "speedStandardDevi" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.writeANNInputToTxtFile(ANNInputPathStr, headheadDescriptionStr, allInputArrayList);
		new NormalizeSparseData().dataNormalize(ANNInputPathStr, metaDataFilePathStr, normalizefilePathStr);
		
		
	}
	
	/**
	 * ���ĳ·�Ρ�ĳʱ���������������Ϣ
	 * ���룺
	 * ·�����ڽ�·�ζ�����ֵ
	 * ·�����ڽ�·�γ��ȱ�ֵ
	 * �ڽ�·���ٶ��������ٶȱ�׼��
	 * ���н���·�η�����Ϣ�������뿪·�η�����Ϣ
	 * 
	 * �����
	 * ·�����ڽ�·���г�ʱ���ֵ
	 */
	public void ANNInput(int targetLinkID, String timeStr, String targetAdjacentLinkTravelTimeFilePathStr, ArrayList<double[]> inputArrayList, 
			ArrayList<String> infosArrayList, ArrayList<MapMatchEdge> polylineCollArrayList) {
		try {
			//ʱ�̡�Ŀ��·���г�ʱ�䡢�ڽ�·���г�ʱ���Լ���ֵ
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
				link1Degree = tEdge1.getFirstLevelConnEdgeArray().size();
				link1Length = tEdge1.getEdgeLength();
				double temp = (double)targetLinkDegree/link1Degree;
				double similarity = Double.parseDouble(String.format("%.2f", temp));
				double lengthRate = Double.parseDouble(String.format("%.2f", targetLinkLength/link1Length));
				double targetAdjacentLinkTravelTimeSameDirection[] = new double[3];//Ŀ��·��ͬ������Ϣ   �ֱ�洢Ŀ��·�Ρ��ڽ�·���г�ʱ���Լ�����֮���ֵ
				double[]expectationStandardDeviSameDirection = new double[2];//Ŀ��·��ͬ���� �ڽ�·��link1ID�ٶ��������׼��				
				int sameDirection = PubParameter.sameDirection;
				int antiDirection = PubParameter.antiDirection;
				obtainTravelTimeRate(sameDirection, targetLinkID,link1ID, timeStr, infosArrayList, targetAdjacentLinkTravelTimeSameDirection, expectationStandardDeviSameDirection);//�г�ʱ���ֵ
				double targetAdjacentLinkTravelTimeAntiDirection[] = new double[3];//Ŀ��·�η�������Ϣ   �ֱ�洢Ŀ��·�Ρ��ڽ�·���г�ʱ���Լ�����֮���ֵ
				double[]expectationStandardDeviAntiDirection = new double[2];//�ڽ�·��link1ID�ٶ��������׼��
				obtainTravelTimeRate(antiDirection, targetLinkID,link1ID, timeStr, infosArrayList, targetAdjacentLinkTravelTimeAntiDirection, expectationStandardDeviAntiDirection);
				double sameDirectiontravelTimeRate = targetAdjacentLinkTravelTimeSameDirection[2];
				double sameDirectionspeedExpectation = expectationStandardDeviSameDirection[0];				
				double sameDirectionspeedStandardDevi = expectationStandardDeviSameDirection[1];
				if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && 
						Math.abs(sameDirectiontravelTimeRate) > 0.000001 && Math.abs(sameDirectionspeedExpectation) > 0.000001 &&
						Math.abs(sameDirectionspeedStandardDevi) > 0.000001) {
					double[]inputInfos = new double[5];
					
//					inputInfos[0] = targetLinkID;//Ŀ��·��
//					inputInfos[1] = link1ID;//�ڽ�·��
//					inputInfos[2] = targetLinkLength;//Ŀ��·�γ���
//					inputInfos[3] = link1Length;//�ڽ�·�γ���
					inputInfos[0] = similarity;
					inputInfos[1] = lengthRate;
					inputInfos[2] = sameDirectionspeedExpectation;
					inputInfos[3] = sameDirectionspeedStandardDevi;
					inputInfos[4] = sameDirectiontravelTimeRate;					
					inputArrayList.add(inputInfos);
//					String str = timeStr + "," + String.valueOf(link1ID) + "," + targetAdjacentLinkTravelTimeSameDirection[0] + "," + targetAdjacentLinkTravelTimeSameDirection[1] + "," + targetAdjacentLinkTravelTimeSameDirection[2];					
//					targetAdjacentLinkTravelTimeArrayList.add(str);
				}
				
				double antiDirectiontravelTimeRate = targetAdjacentLinkTravelTimeAntiDirection[2];
				double antiDirectionspeedExpectation = expectationStandardDeviAntiDirection[0];				
				double antiDirectionspeedStandardDevi = expectationStandardDeviAntiDirection[1];
				if (Math.abs(similarity) > 0.000001 && Math.abs(lengthRate) > 0.000001 && 
						Math.abs(antiDirectiontravelTimeRate) > 0.000001 && Math.abs(antiDirectionspeedExpectation) > 0.000001 &&
						Math.abs(antiDirectionspeedStandardDevi) > 0.000001) {
					double[]inputInfos = new double[5];
					
//					inputInfos[0] = targetLinkID;//Ŀ��·��
//					inputInfos[1] = link1ID;//�ڽ�·��
//					inputInfos[2] = targetLinkLength;//Ŀ��·�γ���
//					inputInfos[3] = link1Length;//�ڽ�·�γ���
					inputInfos[0] = similarity;
					inputInfos[1] = lengthRate;
					inputInfos[2] = antiDirectionspeedExpectation;
					inputInfos[3] = antiDirectionspeedStandardDevi;
					inputInfos[4] = antiDirectiontravelTimeRate;					
					inputArrayList.add(inputInfos);
//					String str = timeStr + "," + String.valueOf(link1ID) + "," + targetAdjacentLinkTravelTimeSameDirection[0] + "," + targetAdjacentLinkTravelTimeSameDirection[1] + "," + targetAdjacentLinkTravelTimeSameDirection[2];					
//					targetAdjacentLinkTravelTimeArrayList.add(str);
				}
				
			}			
			FileOperateFunction.writeStringArraylistToEndTxtFile(targetAdjacentLinkTravelTimeFilePathStr, targetAdjacentLinkTravelTimeArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	//ĳһ·��ÿ���Сʱ���ٶ���������
	public void expectedSpeedcalculation(int linkID, ArrayList<String> infosArrayList, Map<String, Double> expectedSpeedMap) {
		try {		
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			String startDateTimeStr = "2014-06-01 00:00:00";
			String endDateTimeStr = "2014-08-01 00:00:00";
			String tempStartDateTimeStr = startDateTimeStr;
			int timeInterval = 1800;//ͳ��ÿ��30min��·�γ��⳵ͨ��ʱ��
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
				double expectedSpeed = assistFunctionNeuralNetwork.calculateLinkSpeedExpectation(linkTravelInfosInTimeIntervalArrayList);				
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
	 * ��������·���ٶ��������ٶȱ�׼��
	 * ÿ��������У�ÿ����Сʱ������ֵ
	 */
	public void allLinkTravelCharacteristicCalculation() {
		try {
//			//**��ʱ
//			int targetLinkID = 82;
//			Boolean flag = false;
//			//***
			
			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
			ArrayList<LinkTravelCharacteristic> allLinkSpeedExpectationStandardDeviationArrayList = new ArrayList<LinkTravelCharacteristic>();
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);	
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				System.out.print("·����������׼����㣺" + i + ":" + polylineCollArrayList.size() + '\n');
				MapMatchEdge edge = polylineCollArrayList.get(i);
				int linkID = edge.getEdgeID();
				int linkDegree = edge.getFirstLevelConnEdgeArray().size();
				double tempLinkLength = edge.getEdgeLength();
				String tempLinkLengthStr = String.format("%.2f", tempLinkLength);
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
			System.out.print("��Ϣ��ʼд��txt�ļ���" + '\n');
			String filePathStr = PropertiesUtilJAR.getProperties("speedExpectationStandardDeviation");
//			String filePathStr = "C:\\travelTimeProcess\\SpeedExpectationStandardDeviationLink" + targetLinkID + ".txt";
			String headDescriptionStr = "linkID" + "," + "Direction" + "," + "timeStr" + "," + "speedExpectation" + "," + "speedStandardDeviation" + "," + "linkDegree" + "," + "linkLength" + "\r\n";
			FileOperateFunction.writeToTxtFile(filePathStr, headDescriptionStr, allLinkSpeedExpectationStandardDeviationArrayList);
			System.out.print("��Ϣд��txt�ļ�������" + '\n');
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
	}
	
	/**
	 * /**
	 * �ٶȷ������
	 * ĳһ·���ٶ���������׼����㣬�����������Լ�ÿ������ÿ����Сʱ��ͬһ��ʻ�������
	 * @param linkID
	 * @param speedExpectationStandardDeviationArrayList	·��ͨ������
	 */
	public void speedExpectationAndStandardDeviationCalculation(int linkID, ArrayList<LinkTravelCharacteristic> speedExpectationStandardDeviationArrayList,ArrayList<String> infosArrayList) {
		try {			
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();//Ŀ��·��ͨ����Ϣ
			//Ŀ��·����һ���ܶ��ȵ����ݷֱ�ϲ�
			ArrayList<String> linkmondayArrayList = new ArrayList<String>();//��һ
			ArrayList<String> linktuesdayArrayList = new ArrayList<String>();//�ܶ�
			ArrayList<String> linkwednesdayArrayList = new ArrayList<String>();//����
			ArrayList<String> linkthursdayArrayList = new ArrayList<String>();
			ArrayList<String> linkfridayArrayList = new ArrayList<String>();
			//���Ŀ��·��ͨ����Ϣ
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[3].substring(0,10);
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
			int timeInterval = 1800;//ͳ��ÿ��30min��·�γ��⳵ͨ��ʱ��
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
				
				//��Ϊ���ظ������ݣ��ڴ˽��ظ��������޳�������֤��¼��Ψһ��
				//��ͬ��¼������ֱ��������,ֻ����һ���Ƚ�
				ArrayList<String> processWeekInfosArraylist = new ArrayList<String>();
				for (int j = 0; j < weekInfosArraylist.size(); j++) {
					String str1 = weekInfosArraylist.get(j);
					processWeekInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[2];
					String timeStr1 = strArray1[3];					
					if (j != weekInfosArraylist.size() - 1) {
						String str2 = weekInfosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[2];
						String timeStr2 = strArray2[3];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//������ͬ��¼							
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
					//����ͬ��������ֿ�
					ArrayList<String> sameDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>(); 
					ArrayList<String> antiDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();
					for (int j = 0; j < linkTravelInfosInTimeIntervalArrayList.size(); j++) {
						String ttStr = linkTravelInfosInTimeIntervalArrayList.get(j);
						String[]tempArrayStr = ttStr.split(",");
						int direction = Integer.parseInt(tempArrayStr[1]);
						if (direction == 1) {
							sameDirectionLinkTravelInfosInTimeIntervalArrayList.add(ttStr);
						}
						else {
							antiDirectionLinkTravelInfosInTimeIntervalArrayList.add(ttStr);
						}
					}	
					
					//����������4ʱ����������׼���������,
					if (sameDirectionLinkTravelInfosInTimeIntervalArrayList.size() >= 4) {
						ArrayList<String> proSameDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();//ȥ�����˵��ٶ��쳣ֵ֮�������
						//ȥ�����˵��ٶ��쳣ֵ
						double tempMeanSpeed = Double.parseDouble(sameDirectionLinkTravelInfosInTimeIntervalArrayList.get(0).split(",")[5]);
						double maxVal = tempMeanSpeed;
						int maxIndex = 0;//���ֵ����
						int minIndex = 0;//��Сֵ����
						double minVal = tempMeanSpeed;
						for (int j = 0; j < sameDirectionLinkTravelInfosInTimeIntervalArrayList.size(); j++) {
							String str = sameDirectionLinkTravelInfosInTimeIntervalArrayList.get(j);
							String[] tempArrayStr = str.split(",");
							double meanSpeed = Double.parseDouble(tempArrayStr[5]);
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
						double sameDirectionExpectedSpeed = assistFunctionNeuralNetwork.calculateLinkSpeedExpectation(proSameDirectionLinkTravelInfosInTimeIntervalArrayList);
						if (sameDirectionExpectedSpeed != 0) {					
							double sameDirectionSpeedStandardDeviation = assistFunctionNeuralNetwork.calculateLinkSpeedStandardDeviation(sameDirectionExpectedSpeed, proSameDirectionLinkTravelInfosInTimeIntervalArrayList);
							tempStartDateTimeStr = endTimeArray[0];
							System.out.print(sameDirectionExpectedSpeed + "," + sameDirectionSpeedStandardDeviation + '\n');
							LinkTravelCharacteristic linkTravelCharacteristic = new LinkTravelCharacteristic();
							linkTravelCharacteristic.setWeekdayStr(weekdayStr);
							linkTravelCharacteristic.setLinkID(linkID);
							linkTravelCharacteristic.setDirection(1);
							String timeStr = weekdayStr + startTimeStr;
							linkTravelCharacteristic.setTimeStr(timeStr);
							linkTravelCharacteristic.setSpeedExpectation(sameDirectionExpectedSpeed);
							linkTravelCharacteristic.setSpeedStandDeviation(sameDirectionSpeedStandardDeviation);
							speedExpectationStandardDeviationArrayList.add(linkTravelCharacteristic);
							System.out.print(timeStr + "," + sameDirectionExpectedSpeed + ":" + sameDirectionSpeedStandardDeviation + '\n');						
						}
					}
					
					if (antiDirectionLinkTravelInfosInTimeIntervalArrayList.size() >= 4) {
						double tempMeanSpeed = Double.parseDouble(antiDirectionLinkTravelInfosInTimeIntervalArrayList.get(0).split(",")[5]);
						ArrayList<String> proAntiDirectionLinkTravelInfosInTimeIntervalArrayList = new ArrayList<String>();//ȥ�����˵��ٶ��쳣ֵ֮�������
						//ȥ�����˵��ٶ��쳣ֵ
						double maxVal = tempMeanSpeed;
						int maxIndex = 0;//���ֵ����
						int minIndex = 0;//��Сֵ����
						double minVal = tempMeanSpeed;
						for (int j1 = 0; j1 < antiDirectionLinkTravelInfosInTimeIntervalArrayList.size(); j1++) {
							String str = antiDirectionLinkTravelInfosInTimeIntervalArrayList.get(j1);
							String[] tempArrayStr = str.split(",");
							double meanSpeed = Double.parseDouble(tempArrayStr[5]);
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
						double antiDirectionExpectedSpeed = assistFunctionNeuralNetwork.calculateLinkSpeedExpectation(proAntiDirectionLinkTravelInfosInTimeIntervalArrayList);						
						if (antiDirectionExpectedSpeed != 0) {					
							double antiSpeedStandardDeviation = assistFunctionNeuralNetwork.calculateLinkSpeedStandardDeviation(antiDirectionExpectedSpeed, proAntiDirectionLinkTravelInfosInTimeIntervalArrayList);
							tempStartDateTimeStr = endTimeArray[0];
							System.out.print(antiDirectionExpectedSpeed + "," + antiSpeedStandardDeviation + '\n');
							LinkTravelCharacteristic linkTravelCharacteristic = new LinkTravelCharacteristic();
							linkTravelCharacteristic.setWeekdayStr(weekdayStr);
							linkTravelCharacteristic.setLinkID(linkID);
							linkTravelCharacteristic.setDirection(-1);
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
	 * ���Ŀ��·�η���Ϊ targetDirecton����·���г�ʱ���ֵ
	 * @param targetDirection Ŀ��·�η�����
	 * @param link1ID	·��1 ID Ŀ��·��
	 * @param link2ID	·��2 ID
	 * @param expectationStandardDevi	�ڽ�·���ٶ��������׼��
	 * @param infosArrayList	������Ϣ
	 */
	public void obtainTravelTimeRate(int targetDirection, int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList, 
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		try {
			//·��1,2�����Ϣ
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
				int direction1 = Integer.parseInt(tempStrArray1[1]);
				String timeStr1 = tempStrArray1[2];			
				if (timeStr1.equals(timeStr) && direction1 == targetDirection) {
					for (int j = 0; j < link2InfosArrayList.size(); j++) {
						String str2 = link2InfosArrayList.get(j);
						String []tempStrArray2 = str2.split(",");
						int direction2 = Integer.parseInt(tempStrArray2[1]);
						String timeStr2 = tempStrArray2[2];
						//ͬһʱ�䲢��ͬһ����������Ϣ    ��ʱ�������ݺ���   ��ʱ�������ݺ���   ��ʱ�������ݺ���
						//��������·��82�����
						//��������ʻ
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
							//��������ʻ
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
	 * �����г�ʱ���ֵ
	 * @param timeStr1	Ŀ��·��ʱ��
	 * @param timeStr2	�ڽ�·��ʱ��
	 * @param tempStrArray1	Ŀ��·����Ϣ
	 * @param tempStrArray2	�ڽ�·����Ϣ
	 * @param targetAdjacentLinkTravelTime	�ڽ�·����Ϣ
	 * @return
	 */
	public boolean functionProcess(String timeStr1, String timeStr2, String []tempStrArray1, String []tempStrArray2,
			double targetAdjacentLinkTravelTime[], double[]expectationStandardDevi) {
		boolean flag = false;
		try {
			double travelTimeRate = 0;
			if (timeStr1.equals(timeStr2)) {
				String speedExpeStr1 = tempStrArray1[3];
				String linkLengthStr1 = tempStrArray1[6];
				String speedExpeStr2 = tempStrArray2[3];
				String speedStandardDeviStr2 = tempStrArray2[4];
				String linkLengthStr2 = tempStrArray2[6];				
				double speedExpe1 = Double.parseDouble(speedExpeStr1);
				double linkLength1 = Double.parseDouble(linkLengthStr1);						
				double speedExpe2 = Double.parseDouble(speedExpeStr2);
				double speedStandardDevi2 = Double.parseDouble(speedStandardDeviStr2);
				double linkLength2 = Double.parseDouble(linkLengthStr2);
				if (Math.abs(speedExpe1) > 0.000001 && Math.abs(speedExpe2) > 0.000001) {
					double temp1 = linkLength1/speedExpe1;//Ŀ��·���г�ʱ��
					double temp2 = linkLength2/speedExpe2;//�ڽ�·���г�ʱ��
					if (Math.abs(temp2) > 0.000001) {
						double tempTravelTimeRate = temp1/temp2;
						travelTimeRate = Double.parseDouble(String.format("%.2f", tempTravelTimeRate));
						double ttemp1 = Double.parseDouble(String.format("%.2f", temp1));
						double ttemp2 = Double.parseDouble(String.format("%.2f", temp2));
						targetAdjacentLinkTravelTime[0] = ttemp1;
						targetAdjacentLinkTravelTime[1] = ttemp2;
						targetAdjacentLinkTravelTime[2] = travelTimeRate;
						double prospeedExpe2 = Double.parseDouble(String.format("%.2f", speedExpe2));
						double prospeedStandardDevi2 = Double.parseDouble(String.format("%.2f", speedStandardDevi2));
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
	 * ����·��ID����г�ʱ��
	 */
	public double obtainTravelTimeAccordLinkID(int link1ID, String timeStr, ArrayList<String> infosArrayList) {
		double travelTime = 0;
		try {
			//·��1,2�����Ϣ
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
					//ͬһʱ��������Ϣ
					String speedExpeStr1 = tempStrArray1[2];
					String linkLengthStr1 = tempStrArray1[5];							
					double speedExpe1 = Double.parseDouble(speedExpeStr1);
					double linkLength1 = Double.parseDouble(linkLengthStr1);							
					if (Math.abs(speedExpe1) > 0.000001) {
						double tempTravelTime = linkLength1/speedExpe1;								
						travelTime = Double.parseDouble(String.format("%.2f", tempTravelTime));
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
	 * ����ID��ʱ�̻������ֵ����׼��ֵ
	 * @param link1ID
	 * @param timeStr
	 * @param infosArrayList
	 * @param expectationStandardDevi	�ٶ��������ٶȷ���
	 * @return
	 */
	public void obtainExpectationStandDeviAccordLinkID (int link1ID, String timeStr, ArrayList<String> infosArrayList, double []expectationStandardDevi) {
		try {
			//·��1�����Ϣ
			ArrayList<String> link1InfosArrayList = new ArrayList<String>();
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String []tempStrArray = str.split(",");
				String linkIDStr = tempStrArray[0];
				int direction = Integer.parseInt(tempStrArray[1]);
				int linkID = Integer.parseInt(linkIDStr);
				//-1�ķ���  ��ʱ -1�ķ���  ��ʱ -1�ķ���  ��ʱ
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
					//ͬһʱ��������Ϣ
					String speedExpeStr1 = tempStrArray1[3];
					String speedStandardDeviStr1 = tempStrArray1[4];
					double speedExpe1 = Double.parseDouble(speedExpeStr1);
					double speedStandardDevi1 = Double.parseDouble(speedStandardDeviStr1);
					if (Math.abs(speedExpe1) > 0.000001 && Math.abs(speedStandardDevi1) > 0.000001) {							
						double speedExpectation = Double.parseDouble(String.format("%.2f", speedExpe1));
						double standardDevi = Double.parseDouble(String.format("%.2f", speedStandardDevi1));
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
	 * ����·��֮��ĳһʱ���pearsonϵ��
	 * @param linkID1
	 * @param linkID2
	 * @param timeStr	ʱ���,��ʽΪ10:30:00
	 */
	public double pearsonCorrCalculation (int link1ID, int link2ID, String timeStr, ArrayList<String> infosArrayList) {
		double pearsonCorr = 0;
		try {			
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			//·��1,2�����Ϣ
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
						//ͬһʱ��������Ϣ
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
	 * ���Ŀ��·�ηֱ��ڹ����գ���һ���ܶ������壩ÿ���Сʱ�ڵ����ݣ���д��Txt�ļ�
	 * @param linkID
	 */
	public void obtainLinkTravelInfosOnWeekdays(int linkID) {
		try {			
			System.out.print("��Ϣ��ʼд��txt�ļ���" + '\n');
			String filePathStr = "C:\\travelTimeProcess\\workDayTravelTimeLink" + linkID + ".txt";
			String headDescriptionStr = "weekDayTime" + "," + "linkID" + "," + "direction" + "," + "taxiID" + "," + "timeStr" + "," + "travelTime" + "," + "meanSpeed" + "\r\n";			
			FileOperateFunction.writeHeadDescriptionToTxtFile(filePathStr, headDescriptionStr);
			
			String workDayTravelTimePathStr = PropertiesUtilJAR.getProperties("workDayTravelTime");
			ArrayList<String> infosArrayList = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(workDayTravelTimePathStr, infosArrayList);	
			AssistFunctionNeuralNetwork assistFunctionNeuralNetwork = new AssistFunctionNeuralNetwork();
			ArrayList<String> linkTravelInfos = new ArrayList<String>();//Ŀ��·��ͨ����Ϣ
			//Ŀ��·����һ���ܶ��ȵ����ݷֱ�ϲ����ֱ�ϲ��ֱ�ϲ�
			ArrayList<String> linkmondayArrayList = new ArrayList<String>();//��һ
			ArrayList<String> linktuesdayArrayList = new ArrayList<String>();//�ܶ�
			ArrayList<String> linkwednesdayArrayList = new ArrayList<String>();//����
			ArrayList<String> linkthursdayArrayList = new ArrayList<String>();
			ArrayList<String> linkfridayArrayList = new ArrayList<String>();
			//���Ŀ��·��ͨ����Ϣ
			assistFunctionNeuralNetwork.obtainTargetLinkTravelInfosFromTxt(linkID, infosArrayList, linkTravelInfos);
			for (int i = 0; i < linkTravelInfos.size(); i++) {
				String str = linkTravelInfos.get(i);
				String[]tempArrayStr = str.split(",");
				String tempDateStr = tempArrayStr[3].substring(0,10);
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
			int timeInterval = 1800;//ͳ��ÿ��30min��·�γ��⳵ͨ��ʱ��
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
				//��Ϊ���ظ������ݣ��ڴ˽��ظ��������޳�������֤��¼��Ψһ��
				//��ͬ��¼������ֱ��������,ֻ����һ���Ƚ�
				ArrayList<String> processWeekInfosArraylist = new ArrayList<String>();
				for (int j = 0; j < weekInfosArraylist.size(); j++) {
					String str1 = weekInfosArraylist.get(j);
					processWeekInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[2];
					String timeStr1 = strArray1[3];					
					if (j != weekInfosArraylist.size() - 1) {
						String str2 = weekInfosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[2];
						String timeStr2 = strArray2[3];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//������ͬ��¼							
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
