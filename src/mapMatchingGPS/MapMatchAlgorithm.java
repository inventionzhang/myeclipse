package mapMatchingGPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.artificialNeuralNetwork.DeleteBufferedDataSetSample;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.esri.arcgis.geoprocessing.tools.trackinganalysttools.ConcatenateDateAndTimeFields;
import com.esri.arcgis.interop.AutomationException;

import entity.Edge;
import entity.PropertiesUtilJAR;
import entity.ReadRoadDataThread;
import entity.RoadNetworkStorage;

public class MapMatchAlgorithm {
	/*���еķ���Ϊ�ڸ�˹ƽ������ϵ�µļ���*/
	
	private static boolean isOK = false;
	private static MapMatchAlgorithm mapMatchAlgorithmInstance = null;		
	public static MapMatchAlgorithm instance(){	
		try {
			if (mapMatchAlgorithmInstance == null) {
				mapMatchAlgorithmInstance = new MapMatchAlgorithm();
				String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
				String roadNetworkNamecoll[] = roadNetworkName.split(",");
				String fileName = roadNetworkNamecoll[3];//�ļ���
				if (mapMatchAlgorithmInstance.juncCollArrayList.size() == 0) {
					mapMatchAlgorithmInstance.readRoadFile(fileName);
				}			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		if (isOK) {
			System.out.print("��·�����ݳɹ�!" + '\n');
		}
		else {
			System.out.print("��·������ʧ��!" + '\n');
		}
		return mapMatchAlgorithmInstance;
	}	
	
	/*������ͨ�����Լ��������˹�ϵ
	 * */
	public ArrayList<MapMatchNode> juncCollArrayList = new ArrayList<MapMatchNode>();
	public ArrayList<MapMatchEdge> polylineCollArrayList = new ArrayList<MapMatchEdge>();
	//�洢������Ϣ
	public Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = new HashMap<Integer, ArrayList<Double[]>>();
	public Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = new HashMap<Integer, ArrayList<MapMatchNode>>();
	public Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = new HashMap<Integer, ArrayList<MapMatchEdge>>();
	public void createConnSetAndTopolygon(String geoDatabaseFilePath, String juncFileName, String polylineFileName){
		//��ȡfileGeodatabase�е����ļ�    		 	      
    	ReadData readData = new ReadData();
    	readData.readFileGeodatabaseShape(geoDatabaseFilePath, juncFileName, polylineFileName, juncCollArrayList, polylineCollArrayList);   	
    	//���߽�������
    	CreateTopolygy.createTopolygonRelation(juncCollArrayList, polylineCollArrayList);
    	GridDivision gridDivision = new GridDivision();
    	gridDivision.buildGridCell(juncCollArrayList, polylineCollArrayList, PubParameter.cellLength, PubParameter.bufferRadius, allGridIndexVerticesMap,
    			allGridJunctionMap, allGridPolylineMap);	
	}
	
	public void createConnSetAndTopolygon2(String geoDatabaseFilePath, String juncFileName, String polylineFileName){
		//��ȡfileGeodatabase�е����ļ�    		
    	//������һ�����߳� 
    	ReadData readData = new ReadData();
    	readData.readFileGeodatabaseShape(geoDatabaseFilePath, juncFileName, polylineFileName, juncCollArrayList, polylineCollArrayList);   	
    	//���߽�������
    	CreateTopolygy.createTopolygonRelation(juncCollArrayList, polylineCollArrayList);
    	GridDivision gridDivision = new GridDivision();
    	gridDivision.buildGridCell(juncCollArrayList, polylineCollArrayList, PubParameter.cellLength, PubParameter.bufferRadius, allGridIndexVerticesMap,
    			allGridJunctionMap, allGridPolylineMap);
    	
	}
	
	public boolean readRoadFile(String filename){   	
    	try{   		
    		ClassLoader loader = RoadNetworkStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        MapMatchRoadNetworkStorage s = new MapMatchRoadNetworkStorage();
    		File f = new File(filename);
			if(!f.exists()){
				isOK = false;
				System.out.print(filename + "������!" + '\n');
				return false;
			}
			System.out.print(filename + "��ʼ������!" + '\n');
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
	        s = (MapMatchRoadNetworkStorage)oin.readObject();	   
	        oin.close();
//	        this.juncCollArrayList = s.juncCollArrayList;
//	        this.polylineCollArrayList = s.polylineCollArrayList;
//	        this.allGridIndexVerticesMap = s.allGridIndexVerticesMap;
//	        this.allGridJunctionMap = s.allGridJunctionMap;
//	        this.allGridPolylineMap = s.allGridPolylineMap;
	        
	        mapMatchAlgorithmInstance.juncCollArrayList = s.juncCollArrayList;
	        mapMatchAlgorithmInstance.polylineCollArrayList = s.polylineCollArrayList;
	        mapMatchAlgorithmInstance.allGridIndexVerticesMap = s.allGridIndexVerticesMap;
	        mapMatchAlgorithmInstance.allGridJunctionMap = s.allGridJunctionMap;
	        mapMatchAlgorithmInstance.allGridPolylineMap = s.allGridPolylineMap;
	        System.out.print(filename + "��ȡ����!" + '\n');
	        isOK = true;
    		return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		System.out.print(e.getMessage());
    		
    		isOK = false;
    		return false;
    	}
    }
	
	public boolean saveRoadFile(String filename){     	
    	try{
    		MapMatchRoadNetworkStorage s = new MapMatchRoadNetworkStorage();
    		s.juncCollArrayList = mapMatchAlgorithmInstance.juncCollArrayList;
    		s.polylineCollArrayList = mapMatchAlgorithmInstance.polylineCollArrayList;
    		s.allGridIndexVerticesMap = mapMatchAlgorithmInstance.allGridIndexVerticesMap;
    		s.allGridJunctionMap = mapMatchAlgorithmInstance.allGridJunctionMap;
    		s.allGridPolylineMap = mapMatchAlgorithmInstance.allGridPolylineMap;
    		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    		out.writeObject(s);
    	    out.close();
    	    return true;

    	}catch(Exception e){
    		e.printStackTrace();
    		System.out.print(e.getMessage());
    		return false;
    	}
    }
	
	/*�������л�·��ͨ��ʱ��*/
	public Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();//��·��IDΪ�����洢����·�ε�ͳ��ͨ��ʱ��
	public boolean saveSeriseLinkTravelTime(String filename, Map<Integer, ArrayList<ReturnLinkTravelTime>> linkTravelTimeMap){
		try {
			LinkTravelTimeStorage linkTravelTimeStorage = new LinkTravelTimeStorage();
			linkTravelTimeStorage.allLinkTravelTimeMap = linkTravelTimeMap;
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(linkTravelTimeStorage);
    	    out.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	public boolean readSeriseLinkTravelTime(String filename){
		try {
			ClassLoader loader = LinkTravelTimeStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        LinkTravelTimeStorage linkTravelTimeStorage = new LinkTravelTimeStorage();
    		File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
				return false;
			}
			System.out.print(filename + "��ʼ������!" + '\n');
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
			linkTravelTimeStorage = (LinkTravelTimeStorage)oin.readObject();	   
	        oin.close();	        
	        mapMatchAlgorithmInstance.allLinkTravelTimeMap = linkTravelTimeStorage.allLinkTravelTimeMap;
	        System.out.print(filename + "��ȡ����!" + '\n');
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	/**parameterProc:����������,�������ɹ������ز�������
	 * ���������ʽ��
	 * targetID:18395
	 * time��20130101000015
	 * ���ݿ������ʽ��
	 * targetID:MMC8000GPSANDASYN051113-18395-00000000
	 * time��2013-01-01 00:00:15
	 * */
	public static boolean parameterProc(String targetIDStr, String startTimeStr, String endTimeStr, String[]paraArray){
		boolean isOK = false;
		try {
			targetIDStr = "MMC8000GPSANDASYN051113-" + targetIDStr + "-00000000";
			String startYearStr = startTimeStr.substring(0, 4);
			String startMonthStr = startTimeStr.substring(4, 6);
			String startDayStr = startTimeStr.substring(6, 8);
			String startHourStr = startTimeStr.substring(8, 10);
			String startMinStr = startTimeStr.substring(10, 12);
			String startSecStr = startTimeStr.substring(12, 14);
			startTimeStr = startYearStr + "-" + startMonthStr + "-" + startDayStr + " " + startHourStr + ":" + startMinStr + ":" + startSecStr;		
			String endYearStr = endTimeStr.substring(0, 4);
			String endMonthStr = endTimeStr.substring(4, 6);
			String endDayStr = endTimeStr.substring(6, 8);
			String endHourStr = endTimeStr.substring(8, 10);
			String endMinStr = endTimeStr.substring(10, 12);
			String endSecStr = endTimeStr.substring(12, 14);
			endTimeStr = endYearStr + "-" + endMonthStr + "-" + endDayStr + " " + endHourStr + ":" + endMinStr + ":" + endSecStr;
			isOK = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
			System.out.print("��������" + '\n');
		}
		if (isOK) {
			paraArray[0] = targetIDStr;
			paraArray[1] = startTimeStr;
			paraArray[2] = endTimeStr;
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
	
	/**��ͼƥ���㷨
	 * taxiTrackMap:���⳵�켣
	 * returnGPSAndPath:����ƥ��·��
	 * allPathEIDMap������·��EID��ɵ�·��
	 * */
	public static void mapMatch(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, ReturnGPSAndPath returnGPSAndPath, 
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap){
		try {
			//ѡ�����GPS��radius��Χ�ڵĺ�ѡ��·����
	    	AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
	    	ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
	    	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = MapMatchAlgorithm.instance().allGridIndexVerticesMap;
	    	Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = MapMatchAlgorithm.instance().allGridJunctionMap;
	    	Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = MapMatchAlgorithm.instance().allGridPolylineMap;
	    	System.out.print("�켣������ֹ�㴦��" + '\n');
	    	Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	    	assistFunction.continuousStaticPointProcess(taxiTrackMap, processTaxiTrackMap);
	    	double startObtainCandiRoadTime = System.nanoTime();
	    	assistFunction.obtainCandidateRoadSet(processTaxiTrackMap, allGridIndexVerticesMap, allGridPolylineMap, PubParameter.radius);
	    	double endObtainCandiRoadTime = System.nanoTime();
	    	double obtainCandiRoadTime = (endObtainCandiRoadTime - startObtainCandiRoadTime)/Math.pow(10, 9);
			System.out.print("GPS���ú�ѡ��·ʱ�䣺" + obtainCandiRoadTime + "s" + "\n");
	    	//��ѡ��·���
	    	double startObtainRoadScoreTime = System.nanoTime();
	    	assistFunction.obtainCandidatedRoadScore(processTaxiTrackMap, PubParameter.thegema, PubParameter.directWeight);
	    	double endObtainRoadScoreTime = System.nanoTime();
	    	double obtainRoadScoreTime = (endObtainRoadScoreTime - startObtainRoadScoreTime)/Math.pow(10, 9);
	    	System.out.print("GPS���ú�ѡ��·����ʱ�䣺" + obtainRoadScoreTime + "s" + "\n");
	    	//·��ѡȡ			 
			double startObtainPathTime = System.nanoTime();
			assistFunction.obtainPathBasedonCompreScore(processTaxiTrackMap, PubParameter.threeLevelConnProbability);
			double endObtainPathTime = System.nanoTime();		
			//���GPS���Լ�ƥ��·��  	
			assistFunction.exportGPSMatchPath(processTaxiTrackMap, allGridIndexVerticesMap,juncCollArrayList, polylineCollArrayList, returnGPSAndPath,allPathEIDMap);
			double obtainPathTime = (endObtainPathTime - startObtainPathTime)/Math.pow(10, 9);
			System.out.print("���GPS�켣·��ʱ�䣺" + obtainPathTime + "s" + "\n");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**GPS��������� 
	 * @param taxiTrackMap	ԭ���⳵GPS�켣��
	 * @param pathEIDArrayList	ƥ��·��������
	 * @param correctedOriginalTaxiTrackArrayList	�������ԭʼGPS�켣
	 * @param GPSCorrectArrayList	������GPS��
	 */
	public static void coordinateCorr(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, ArrayList<Integer[]> pathEIDArrayList,
			ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList,	ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {
			//ѡ�����GPS��radius��Χ�ڵĺ�ѡ��·����
	    	AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
	    	ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
	    	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = MapMatchAlgorithm.instance().allGridIndexVerticesMap;
	    	Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = MapMatchAlgorithm.instance().allGridJunctionMap;
	    	Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = MapMatchAlgorithm.instance().allGridPolylineMap;
	    	System.out.print("�켣������ֹ�㴦��" + '\n');
	    	Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	    	assistFunction.continuousStaticPointProcess(taxiTrackMap, processTaxiTrackMap);
	    	double startObtainCandiRoadTime = System.nanoTime();
	    	assistFunction.obtainCandidateRoadSet(processTaxiTrackMap, allGridIndexVerticesMap, allGridPolylineMap, PubParameter.radius);
	    	double endObtainCandiRoadTime = System.nanoTime();
	    	double obtainCandiRoadTime = (endObtainCandiRoadTime - startObtainCandiRoadTime)/Math.pow(10, 9);
			System.out.print("GPS���ú�ѡ��·ʱ�䣺" + obtainCandiRoadTime + "s" + "\n");
	    	//��ѡ��·���
	    	double startObtainRoadScoreTime = System.nanoTime();
	    	assistFunction.obtainCandidatedRoadScore(processTaxiTrackMap, PubParameter.thegema, PubParameter.directWeight);
	    	double endObtainRoadScoreTime = System.nanoTime();
	    	double obtainRoadScoreTime = (endObtainRoadScoreTime - startObtainRoadScoreTime)/Math.pow(10, 9);
	    	System.out.print("GPS���ú�ѡ��·����ʱ�䣺" + obtainRoadScoreTime + "s" + "\n");
	    	//·��ѡȡ			 
			double startObtainPathTime = System.nanoTime();
			assistFunction.obtainPathBasedonCompreScore(processTaxiTrackMap, PubParameter.threeLevelConnProbability);
			double endObtainPathTime = System.nanoTime();
			assistFunction.obtainGPSCorrectionCoord(processTaxiTrackMap,correctedOriginalTaxiTrackArrayList, pathEIDArrayList, allGridIndexVerticesMap,juncCollArrayList, polylineCollArrayList, GPSCorrectArrayList);			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
		
	/**���⳵�ؿ͹켣��ֻѡȡ���ؿͼ�¼�Ĺ켣
	 * targetIDStr:���⳵GPSid
	 * startTimeStr:��ʼʱ��
	 * endTimeStr:����ʱ��
	 * */
	public ReturnGPSAndPath obtainCarrypassGPSAndMatchPath(String targetIDStr, String startTimeStr, String endTimeStr){
		boolean isOK = false;
		String[] paraArray = new String[3];
		isOK = parameterProc(targetIDStr, startTimeStr, endTimeStr, paraArray);
		targetIDStr = paraArray[0];
		startTimeStr = paraArray[1];
		endTimeStr = paraArray[2];
    	ReturnGPSAndPath returnGPSAndPath = new ReturnGPSAndPath();
    	try {
    		if (isOK) {
    			//������ݿ���GPS����
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��   
    			/*���ݳ��⳵���ؿ��������ȡ���⳵�ؿ͵Ķ����켣*/
    			AssistFunction assistFunction = new AssistFunction();
    	    	Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
    	    	assistFunction.obtainCarryPassengerData(eliminateZeroSpeedGPSDataArrayList, carrayPassTrackMap);
    	        Map<Integer,ArrayList<Integer[]>> allPathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();
    	    	mapMatch(carrayPassTrackMap,returnGPSAndPath, allPathEIDMap);
    		}
    	}
    	catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
			System.out.print(e.getMessage());
		}    	
		return returnGPSAndPath;
	}
	
	/* ���ĳ��ʱ���ڳ��⳵�켣��
	 * �������Ƿ��ؿ�*/
	public ReturnGPSAndPath obtainAllGPSAndMatchPath(String targetIDStr, String startTimeStr, String endTimeStr){
		boolean isOK = false;
		String[] paraArray = new String[3];
		isOK = parameterProc(targetIDStr, startTimeStr, endTimeStr, paraArray);
		targetIDStr = paraArray[0];
		startTimeStr = paraArray[1];
		endTimeStr = paraArray[2];
		ReturnGPSAndPath returnGPSAndPath = new ReturnGPSAndPath();
		try {
    		if (isOK) {
    			//������ݿ���GPS����
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��   			
    			Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
    			taxiTrackMap.put(1, taxiGPSArrayList);
    			Map<Integer,ArrayList<Integer[]>> allPathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();
    			mapMatch(taxiTrackMap, returnGPSAndPath, allPathEIDMap);
    			System.out.print("");
    		}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return returnGPSAndPath;
	}
	
	/*GPS������������xml*/
	public String obtainTaxiGPSDataAndCorrXml(String targetIDStr, String startTimeStr, String endTimeStr) throws ParserConfigurationException{
//		String user = "root";
//    	String password = "123456";
//    	//������ݿ���GPS����
//		ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
//		AssistFunction assistFunction = new AssistFunction();
//		assistFunction.obtainGPSDataFromDatabase(user, password, taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
//		ArrayList<OriginCorrectNode> returnAllGPSAndCorrectGPS = obtainGPSCorrection(taxiGPSArrayList);
//		ArrayList<ReturnMatchNode> returnOriginGPSArrayList = returnAllGPSAndCorrectGPS.get(0);
//		ArrayList<ReturnMatchNode> returnCorrectGPSArraylist = returnAllGPSAndCorrectGPS.get(1);
//		org.dom4j.Document document = DocumentHelper.createDocument();
//		//���������
//		org.dom4j.Element root = document.addElement("root");
//		for (int i = 0; i < returnOriginGPSArrayList.size(); i++) {
//			ReturnMatchNode tNode = returnOriginGPSArrayList.get(i);			
//			String longitude = Double.toString(tNode.getLongitude());
//			String latitude = Double.toString(tNode.getLatitude());;
//			Element nodeElem = root.addElement("Node");
//			nodeElem.addAttribute("type", "originalGPSNode");
//	        Element longitudeElem = nodeElem.addElement("longitude"); 
//	        longitudeElem.setText(longitude);	        
//	        Element latitudeElem = nodeElem.addElement("latitude"); 
//	        latitudeElem.setText(latitude); 
//		}
//		
//		for (int i = 0; i < returnCorrectGPSArraylist.size(); i++) {
//			ReturnMatchNode tNode = returnCorrectGPSArraylist.get(i);			
//			String longitude = Double.toString(tNode.getLongitude());
//			String latitude = Double.toString(tNode.getLatitude());;		        
//	        Element nodeElem = root.addElement("Node");
//			nodeElem.addAttribute("type", "CorrectGPSNode");
//	        Element longitudeElem = nodeElem.addElement("longitude"); 
//	        longitudeElem.setText(longitude);	        
//	        Element latitudeElem = nodeElem.addElement("latitude"); 
//	        latitudeElem.setText(latitude);
//		}			
//		String xmlString = document.asXML();		    
//	    System.out.println(xmlString);
		String xmlString = "";
		return xmlString;
	}
	
	/*****************************************************************************
	 * ·���г�ʱ��ͳ�Ʒ���
	 * ˼·��
	 * 1.���ݿ�ʱ��Լ����ʱ����ռ��ϵ�Լ�����Ƚ��пռ�Լ�������ʱ��Լ��
	 * 		����link��������η�Χ�������ݿ�������������ĵ㣬Ȼ���ٸ���ʱ��Լ�����м�����
	 * 2.���ݳ��⳵ID���ࣺ���������ݰ����⳵ID���з��࣬������ʱ����С�����������
	 * 3.�켣�ʷ�
	 * 4.ʱ����չ
	 * 5.��ͼƥ��
	 * 6.Ŀ��·���жϣ����ݺ�ѡ��·ID�Ƿ����Ŀ��·��LinkID����һ���жϳ�����·����,����������GPS��
	 * 7.��ֵ���㣺�ڳ��⳵�ս���·��λ������⳵���뿪·��λ�ý��в�ֵ
	 * 8.ͳ��ÿ�����⳵ͨ��·�ε�ʱ��
	 * ***************************************************************************/

	/*���г��⳵����(�ؿ��벻�ؿ�)��ͨ��ʱ��ͳ��
	 * ���룺
	 * linkID��·��ID
	 * startTimeStr����ʼʱ��
	 * endTimeStr������ʱ��
	 * �����
	 * ���⳵ID�Լ�ʱ�����ͨ��linkID��ʱ��
	 * */
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsAll(int targetLinkID, String startTimeStr, String endTimeStr){
		double systemStartTime = System.nanoTime();
		//���г��⳵��·���г�ʱ��
		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();
		boolean isOKStartTime = false;
		boolean isOKEndTime = false;
		String[] paraArray = new String[1];
		try {		
			AssistFunction assistFunction = new AssistFunction();
			isOKStartTime = parameterProcAboutTime(startTimeStr, paraArray);
			startTimeStr = paraArray[0];
			isOKEndTime = parameterProcAboutTime(endTimeStr, paraArray);
			endTimeStr = paraArray[0];
			if (isOKStartTime && isOKEndTime) {	    	
    			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;			
    			MapMatchEdge targetEdge = new MapMatchEdge();//Ŀ��·��  			
    			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//���Ŀ��·��   			
    			double[]leftDownLB = new double[2];
    			double[]rightTopLB = new double[2];
    			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
    			double minLog = leftDownLB[0];
    			double minLat = leftDownLB[1];
    			double maxLog = rightTopLB[0];
    			double maxLat = rightTopLB[1];  			
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
    			//�������ʱ��Լ������������
    			System.out.print("��ʼ�����ݿ⣺" + '\n');
    			double startReadDatabase = System.nanoTime();
    			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
    			double endReadDatabase = System.nanoTime();
    			System.out.print("���������ݿ⣺" + '\n');
    			double readDatabaseTime = (startReadDatabase - endReadDatabase)/Math.pow(10, 9);
    			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��
    			//����ID���з���
    			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
    			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap);   			
    			linkTravelTimeProcessSingleThread(targetLinkID, targetEdge, startTimeStr, endTimeStr, taxiSortMap,linkTravelTimeArrayList);
			}
			double systemEndTime = System.nanoTime();
	    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
	    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return linkTravelTimeArrayList;
	}
	
	/*ͨ��ʱ�䴦���������߳�*/
	public void linkTravelTimeProcessSingleThread(int targetLinkID, MapMatchEdge targetEdge, String startTimeStr, String endTimeStr,
			Map<String, ArrayList<TaxiGPS>> taxiSortMap, ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList){
		try {
			AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�����г��⳵��ͨ��ʱ����Ϣ
	    	//������г��⳵��ͨ��ʱ��
	    	assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
	    			PubParameter.sampleThreshold, PubParameter.expandTime, taxiTravelTimeArrayList);
	    	for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
	    		String taxiID = taxiTravelTime.getTaxiID();//���⳵ID
	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//��ʼ����·��ʱ��
	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//·��ͨ��ʱ��
	    		Map<String, Double> meanSpeedMap = taxiTravelTime.getTaxiMeanSpeedMap();
	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//·���ϵ�GPS��
	    		Map<String, Integer> taxiTravelDirectionMap = taxiTravelTime.getTaxiTravelDirectionMap();
	    		Map<String, int[]> taxiEntranceExitNodeIDMap = taxiTravelTime.getTaxiEntranceExitNodeIDMap();
	    		for (int j = 0; j < startTravelTimeArraylist.size(); j++) {
					String startTravelTimeStr = startTravelTimeArraylist.get(j);
					double travelTime = travelTimeMap.get(startTravelTimeStr);
					double meanSpeed = meanSpeedMap.get(startTravelTimeStr);
					ArrayList<MapMatchNode> taxiTraveGPSlArrayList = GPSTravelMap.get(startTravelTimeStr);
					int taxiTravelDirection = taxiTravelDirectionMap.get(startTravelTimeStr);
					int []taxiEnterNodeID = taxiEntranceExitNodeIDMap.get(startTravelTimeStr);
					ReturnLinkTravelTime returnLinkTravelTime = new ReturnLinkTravelTime();
    	    		returnLinkTravelTime.setTaxiID(taxiID);
    	    		returnLinkTravelTime.setLinkID(targetLinkID);
    	    		returnLinkTravelTime.setTaxiTravelDirection(taxiTravelDirection);
    	    		returnLinkTravelTime.setTaxiEnterNodeID(taxiEnterNodeID[0]);
    	    		returnLinkTravelTime.setTaxiExitNodeID(taxiEnterNodeID[1]);
    	    		returnLinkTravelTime.setStartTravelTime(startTravelTimeStr);
    	    		returnLinkTravelTime.setTravelTime(travelTime);
    	    		returnLinkTravelTime.setTaxiMeanSpeed(meanSpeed);
    	    		returnLinkTravelTime.setTaxiLinkTravelArrayList(taxiTraveGPSlArrayList);
    	    		linkTravelTimeArrayList.add(returnLinkTravelTime);	
				}	
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}	
	
	/*���߳�
	 * ·���ؿͳ�����ͨ��ʱ��ͳ��
	 * �˷������������̳߳ش���*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassengerBackUps(int targetLinkID, String startTimeStr, String endTimeStr){
		double systemStartTime = System.nanoTime();
		//���г��⳵��·���г�ʱ��
		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//·��ͨ��ʱ��ļ�����
		boolean isOKStartTime = false;
		boolean isOKEndTime = false;
		String[] paraArray = new String[1];
		try {
			AssistFunction assistFunction = new AssistFunction();
			isOKStartTime = parameterProcAboutTime(startTimeStr, paraArray);
			startTimeStr = paraArray[0];
			isOKEndTime = parameterProcAboutTime(endTimeStr, paraArray);
			endTimeStr = paraArray[0];
			if (isOKStartTime && isOKEndTime) {	    	
    			ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;			
    			MapMatchEdge targetEdge = new MapMatchEdge();//Ŀ��·��
    			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//���Ŀ��·��
    			double[]leftDownLB = new double[2];
    			double[]rightTopLB = new double[2];
    			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
    			double minLog = leftDownLB[0];
    			double minLat = leftDownLB[1];
    			double maxLog = rightTopLB[0];
    			double maxLat = rightTopLB[1];  			
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
				//�������ʱ��Լ������������
				System.out.print("��ʼ�����ݿ⣺" + '\n');
				double startReadDatabase = System.nanoTime();
				DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
    			double endReadDatabase = System.nanoTime();
    			System.out.print("���������ݿ⣺" + '\n');
    			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
    			System.out.print("�����ݿ�ʱ�䣺" + readDatabaseTime + "s" + '\n');
    			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(carryPassengerGPSDataArrayList, eliminateZeroSpeedGPSDataArrayList); //ȥ���ٶ�Ϊ���GPS��
    			linkTravelTimeProcessMultiThread(targetLinkID, startTimeStr, endTimeStr, targetEdge, eliminateZeroSpeedGPSDataArrayList, linkTravelTimeArrayList);
			}
			double systemEndTime = System.nanoTime();
	    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
	    	System.out.print("��������ʱ�䣺" + processTime + "s" + '\n');		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return linkTravelTimeArrayList;
	}
	
	/*·���ؿͳ�����ͨ��ʱ��ͳ��
	 * ͨ����ȡ���л����ݵķ�ʽ*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassenger(int targetLinkID, String startTimeStr, String endTimeStr){
		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//·��ͨ��ʱ��ļ�����
		try {
			String fileName = PropertiesUtilJAR.getProperties("travelTime3");
			System.out.print("��ʼ�����л�����" + '\n');
			MapMatchAlgorithm.instance().readSeriseLinkTravelTime(fileName);
			System.out.print("���л����ݶ�ȡ�ɹ�" + '\n');
			Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = MapMatchAlgorithm.instance().allLinkTravelTimeMap;
			linkTravelTimeArrayList = allLinkTravelTimeMap.get(targetLinkID);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return linkTravelTimeArrayList;
	}
	
	/**
	 * �˷������������̳߳ش���
	 * ���·��ͨ��ʱ��
	 * eliminateZeroSpeedGPSDataArrayList��ȥ���ٶ�Ϊ���GPS����
	 * linkTravelTimeArrayList:Ŀ��·�����г��⳵��ͨ��ʱ��
	 * targetEdge��Ŀ��·��*/
	public static Map<Integer, ArrayList<TaxiTravelTime>> allTaxiTravelTimeMap = 
		new HashMap<Integer, ArrayList<TaxiTravelTime>>();//���г��⳵ͨ��ʱ��,���߳�IDΪ����
	public void linkTravelTimeProcessMultiThread(int targetLinkID, String startTimeStr, String endTimeStr, MapMatchEdge targetEdge, 
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList, ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList){
		try {
			allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
			//����ID���з���
			AssistFunction assistFunction = new AssistFunction();
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap);
			int taxiTotalCount = taxiSortMap.size();//���⳵��Ŀ
			int threadCount = 4;//�߳���Ŀ
			double taxiCountEveryThread = (double)taxiTotalCount/threadCount;
			List<ThreadTravelTimeStatistics> allThreads = new ArrayList<ThreadTravelTimeStatistics>();
			//4���߳̽��м���
			//���С��1����ÿ���̼߳���һ�����⳵ͨ��ʱ��
			if (taxiCountEveryThread < 1) {
				for (int i = 0; i < taxiTotalCount; i++) {
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
		        	while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);
		        		Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
		        		tempTaxiMap.put(key, tempTaxiGPSArrayList);
		        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
		        		allTaxiTravelTimeMap.put(i, taxiTravelTimeArrayList);
						ThreadTravelTimeStatistics threadTravelTimeStatistics = new ThreadTravelTimeStatistics();
						threadTravelTimeStatistics.setThreadID(i);
						threadTravelTimeStatistics.setTaxiSortMap(tempTaxiMap);
						threadTravelTimeStatistics.setStartTimeStr(startTimeStr);
						threadTravelTimeStatistics.setEndTimeStr(endTimeStr);
						threadTravelTimeStatistics.setTargetLinkID(targetLinkID);
						threadTravelTimeStatistics.setTargetEdge(targetEdge);
						threadTravelTimeStatistics.setSampleThreshold(PubParameter.sampleThreshold);
						threadTravelTimeStatistics.setExpandTime(PubParameter.expandTime);				
						threadTravelTimeStatistics.start();
						allThreads.add(threadTravelTimeStatistics);				        			
		        	}				
				}
			}
			//����ÿ���̼߳���������⳵
			else {
				//���Ϊ���������ܹ�������
				String taxiCountEveryThreadStr = String.valueOf(taxiCountEveryThread);
				if (PubClass.isInteger(taxiCountEveryThreadStr)) {
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;//��ʱ����������ÿ���̵߳ĳ��⳵��Ŀ
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
		        	while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);
		        		tempCount++;
		        		if (tempCount < taxiCountEveryThread ) {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
						}
		        		else {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);	
		        			threadID++;
		        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
			        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
							ThreadTravelTimeStatistics threadTravelTimeStatistics = new ThreadTravelTimeStatistics();
							threadTravelTimeStatistics.setThreadID(threadID);
							threadTravelTimeStatistics.setTaxiSortMap(tempTaxiMap);
							threadTravelTimeStatistics.setStartTimeStr(startTimeStr);
							threadTravelTimeStatistics.setEndTimeStr(endTimeStr);
							threadTravelTimeStatistics.setTargetLinkID(targetLinkID);
							threadTravelTimeStatistics.setTargetEdge(targetEdge);
							threadTravelTimeStatistics.setSampleThreshold(PubParameter.sampleThreshold);
							threadTravelTimeStatistics.setExpandTime(PubParameter.expandTime);				
							threadTravelTimeStatistics.start();
							allThreads.add(threadTravelTimeStatistics);									
		        			tempCount = 0;
		        			tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
						}			        		
		        	}							
				}
				//��Ϊ����
				else {
					int integerPart =PubClass.obtainIntegerPart(taxiCountEveryThreadStr);
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;
					int tempThreadCount = 0;//��ʱ�����������߳���Ŀ
					int taxiCount = 0;//��ʱ����������ÿ���̵߳ĳ��⳵��Ŀ
					Set keySet = taxiSortMap.entrySet();
					Iterator iterator = (Iterator) keySet.iterator();
					while (iterator.hasNext()) {
		        		Map.Entry mapEntry = (Map.Entry) iterator.next();
		        		String key = (String)mapEntry.getKey();
		        		ArrayList<TaxiGPS> tempTaxiGPSArrayList = taxiSortMap.get(key);
		        		tempCount++;
		        		taxiCount++;
		        		if (tempThreadCount == threadCount) {
		        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
		        			if (taxiCount == taxiSortMap.size()) {
		        				ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
				        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
								ThreadTravelTimeStatistics threadTravelTimeStatistics = new ThreadTravelTimeStatistics();
								threadTravelTimeStatistics.setThreadID(threadID);
								threadTravelTimeStatistics.setTaxiSortMap(tempTaxiMap);
								threadTravelTimeStatistics.setStartTimeStr(startTimeStr);
								threadTravelTimeStatistics.setEndTimeStr(endTimeStr);
								threadTravelTimeStatistics.setTargetLinkID(targetLinkID);
								threadTravelTimeStatistics.setTargetEdge(targetEdge);
								threadTravelTimeStatistics.setSampleThreshold(PubParameter.sampleThreshold);
								threadTravelTimeStatistics.setExpandTime(PubParameter.expandTime);				
								threadTravelTimeStatistics.start();
								allThreads.add(threadTravelTimeStatistics);
							}			        			
						}
		        		else {
		        			if (tempCount < integerPart ) {
			        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
							}
			        		else {
			        			tempTaxiMap.put(key, tempTaxiGPSArrayList);
			        			threadID++;				        						        			
			        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//Ŀ��·�γ��⳵��ͨ��ʱ����Ϣ
				        		allTaxiTravelTimeMap.put(threadID, taxiTravelTimeArrayList);
								ThreadTravelTimeStatistics threadTravelTimeStatistics = new ThreadTravelTimeStatistics();
								threadTravelTimeStatistics.setThreadID(threadID);
								threadTravelTimeStatistics.setTaxiSortMap(tempTaxiMap);
								threadTravelTimeStatistics.setStartTimeStr(startTimeStr);
								threadTravelTimeStatistics.setEndTimeStr(endTimeStr);
								threadTravelTimeStatistics.setTargetLinkID(targetLinkID);
								threadTravelTimeStatistics.setTargetEdge(targetEdge);
								threadTravelTimeStatistics.setSampleThreshold(PubParameter.sampleThreshold);
								threadTravelTimeStatistics.setExpandTime(PubParameter.expandTime);				
								threadTravelTimeStatistics.start();
								allThreads.add(threadTravelTimeStatistics);	
								tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
								tempCount = 0;
			        			tempThreadCount++;	
							}
						}			        		
					}						
				}
			}
			try{
	        	for(ThreadTravelTimeStatistics thread : allThreads){
	        		thread.join();
	        	}	
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}			 			
			Set keySet = allTaxiTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
			while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int key = (Integer)mapEntry.getKey();
        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = allTaxiTravelTimeMap.get(key);
        		for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
    	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
    	    		String taxiID = taxiTravelTime.getTaxiID();//���⳵ID
    	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//��ʼ����·��ʱ��
    	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//��ʼ����·��ʱ���Ӧ��·��ͨ��ʱ��
    	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//��ʼ����·��ʱ���Ӧ·���ϵ�GPS��
    	    		for (int j = 0; j < startTravelTimeArraylist.size(); j++) {
						String startTravelTimeStr = startTravelTimeArraylist.get(j);
						double travelTime = travelTimeMap.get(startTravelTimeStr);
						ArrayList<MapMatchNode> taxiTraveGPSlArrayList = GPSTravelMap.get(startTravelTimeStr);
						ReturnLinkTravelTime returnLinkTravelTime = new ReturnLinkTravelTime();
						returnLinkTravelTime.setLinkID(targetLinkID);
	    	    		returnLinkTravelTime.setTaxiID(taxiID);
	    	    		returnLinkTravelTime.setStartTravelTime(startTravelTimeStr);
	    	    		returnLinkTravelTime.setTravelTime(travelTime);
	    	    		returnLinkTravelTime.setTaxiLinkTravelArrayList(taxiTraveGPSlArrayList);
	    	    		linkTravelTimeArrayList.add(returnLinkTravelTime);	
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
