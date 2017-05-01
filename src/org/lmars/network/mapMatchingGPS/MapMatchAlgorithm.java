package org.lmars.network.mapMatchingGPS;

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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.lmars.network.entity.Edge;
import org.lmars.network.entity.ReadRoadDataThread;
import org.lmars.network.entity.RoadNetworkStorage;
import org.lmars.network.util.PropertiesUtil;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;


import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.esri.arcgis.geoprocessing.tools.trackinganalysttools.ConcatenateDateAndTimeFields;
import com.esri.arcgis.interop.AutomationException;


public class MapMatchAlgorithm {
	/*所有的方法为在高斯平面坐标系下的计算*/
	
	private static boolean isOK = false;
	private static MapMatchAlgorithm mapMatchAlgorithmInstance = null;		
	public static MapMatchAlgorithm instance(){	
		try {
			if (mapMatchAlgorithmInstance == null) {
				mapMatchAlgorithmInstance = new MapMatchAlgorithm();
				String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
				String roadNetworkNamecoll[] = roadNetworkName.split(",");
				String fileName = roadNetworkNamecoll[3];//文件名
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
			System.out.print("读路网数据成功!" + '\n');
		}
		else {
			System.out.print("读路网数据失败!" + '\n');
		}
		return mapMatchAlgorithmInstance;
	}	
	
	/*构建连通集合以及点线拓扑关系
	 * */
	public ArrayList<MapMatchNode> juncCollArrayList = new ArrayList<MapMatchNode>();
	public ArrayList<MapMatchEdge> polylineCollArrayList = new ArrayList<MapMatchEdge>();
	//存储网格信息
	public Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = new HashMap<Integer, ArrayList<Double[]>>();
	public Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = new HashMap<Integer, ArrayList<MapMatchNode>>();
	public Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = new HashMap<Integer, ArrayList<MapMatchEdge>>();
	public void createConnSetAndTopolygon(String geoDatabaseFilePath, String juncFileName, String polylineFileName){
		//读取fileGeodatabase中点线文件    		 	      
    	ReadData readData = new ReadData();
    	readData.readFileGeodatabaseShape(geoDatabaseFilePath, juncFileName, polylineFileName, juncCollArrayList, polylineCollArrayList);   	
    	//点线建立拓扑
    	CreateTopolygy.createTopolygonRelation(juncCollArrayList, polylineCollArrayList);
    	GridDivision gridDivision = new GridDivision();
    	gridDivision.buildGridCell(juncCollArrayList, polylineCollArrayList, PubParameter.cellLength, PubParameter.bufferRadius, allGridIndexVerticesMap,
    			allGridJunctionMap, allGridPolylineMap);	
	}
	
	public void createConnSetAndTopolygon2(String geoDatabaseFilePath, String juncFileName, String polylineFileName){
		//读取fileGeodatabase中点线文件    		
    	//可以做一个多线程 
    	ReadData readData = new ReadData();
    	readData.readFileGeodatabaseShape(geoDatabaseFilePath, juncFileName, polylineFileName, juncCollArrayList, polylineCollArrayList);   	
    	//点线建立拓扑
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
    		    loader = loader.getParent();//获得父类加载器的引用  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        MapMatchRoadNetworkStorage s = new MapMatchRoadNetworkStorage();
    		File f = new File(filename);
			if(!f.exists()){
				isOK = false;
				System.out.print(filename + "不存在!" + '\n');
				return false;
			}
			System.out.print(filename + "开始读数据!" + '\n');
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
	        System.out.print(filename + "读取结束!" + '\n');
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
	
	/*保存序列化路段通行时间*/
	public Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();//以路段ID为索引存储所有路段的统计通行时间
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
    		    loader = loader.getParent();//获得父类加载器的引用  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        LinkTravelTimeStorage linkTravelTimeStorage = new LinkTravelTimeStorage();
    		File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "不存在!" + '\n');
				return false;
			}
			System.out.print(filename + "开始读数据!" + '\n');
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
			linkTravelTimeStorage = (LinkTravelTimeStorage)oin.readObject();	   
	        oin.close();	        
	        mapMatchAlgorithmInstance.allLinkTravelTimeMap = linkTravelTimeStorage.allLinkTravelTimeMap;
	        System.out.print(filename + "读取结束!" + '\n');
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	/**parameterProc:参数处理函数,如果处理成功，返回参数数组
	 * 传入参数格式：
	 * targetID:18395
	 * time：20130101000015
	 * 数据库参数格式：
	 * targetID:MMC8000GPSANDASYN051113-18395-00000000
	 * time：2013-01-01 00:00:15
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
			System.out.print("参数错误：" + '\n');
		}
		if (isOK) {
			paraArray[0] = targetIDStr;
			paraArray[1] = startTimeStr;
			paraArray[2] = endTimeStr;
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
	
	/**地图匹配算法
	 * taxiTrackMap:出租车轨迹
	 * returnGPSAndPath:返回匹配路径
	 * allPathEIDMap：所有路径EID组成的路径
	 * */
	public static void mapMatch(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, ReturnGPSAndPath returnGPSAndPath, 
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap){
		try {
			//选择距离GPS点radius范围内的候选道路集合
	    	AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
	    	ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
	    	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = MapMatchAlgorithm.instance().allGridIndexVerticesMap;
	    	Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = MapMatchAlgorithm.instance().allGridJunctionMap;
	    	Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = MapMatchAlgorithm.instance().allGridPolylineMap;
	    	System.out.print("轨迹连续静止点处理：" + '\n');
	    	Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	    	assistFunction.continuousStaticPointProcess(taxiTrackMap, processTaxiTrackMap);
	    	double startObtainCandiRoadTime = System.nanoTime();
	    	assistFunction.obtainCandidateRoadSet(processTaxiTrackMap, allGridIndexVerticesMap, allGridPolylineMap, PubParameter.radius);
	    	double endObtainCandiRoadTime = System.nanoTime();
	    	double obtainCandiRoadTime = (endObtainCandiRoadTime - startObtainCandiRoadTime)/Math.pow(10, 9);
			System.out.print("GPS点获得候选道路时间：" + obtainCandiRoadTime + "s" + "\n");
	    	//候选道路打分
	    	double startObtainRoadScoreTime = System.nanoTime();
	    	assistFunction.obtainCandidatedRoadScore(processTaxiTrackMap, PubParameter.thegema, PubParameter.directWeight);
	    	double endObtainRoadScoreTime = System.nanoTime();
	    	double obtainRoadScoreTime = (endObtainRoadScoreTime - startObtainRoadScoreTime)/Math.pow(10, 9);
	    	System.out.print("GPS点获得候选道路分数时间：" + obtainRoadScoreTime + "s" + "\n");
	    	//路径选取			 
			double startObtainPathTime = System.nanoTime();
			assistFunction.obtainPathBasedonCompreScore(processTaxiTrackMap, PubParameter.threeLevelConnProbability);
			double endObtainPathTime = System.nanoTime();		
			//输出GPS点以及匹配路径  	
			assistFunction.exportGPSMatchPath(processTaxiTrackMap, allGridIndexVerticesMap,juncCollArrayList, polylineCollArrayList, returnGPSAndPath,allPathEIDMap);
			double obtainPathTime = (endObtainPathTime - startObtainPathTime)/Math.pow(10, 9);
			System.out.print("获得GPS轨迹路径时间：" + obtainPathTime + "s" + "\n");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**GPS点坐标纠正 
	 * @param taxiTrackMap	原出租车GPS轨迹点
	 * @param pathEIDArrayList	匹配路径及方向
	 * @param correctedOriginalTaxiTrackArrayList	纠正后的原始GPS轨迹
	 * @param GPSCorrectArrayList	纠正后GPS点
	 */
	public static void coordinateCorr(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, ArrayList<Integer[]> pathEIDArrayList,
			ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList,	ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {
			//选择距离GPS点radius范围内的候选道路集合
	    	AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
	    	ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
	    	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap = MapMatchAlgorithm.instance().allGridIndexVerticesMap;
	    	Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap = MapMatchAlgorithm.instance().allGridJunctionMap;
	    	Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap = MapMatchAlgorithm.instance().allGridPolylineMap;
	    	System.out.print("轨迹连续静止点处理：" + '\n');
	    	Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	    	assistFunction.continuousStaticPointProcess(taxiTrackMap, processTaxiTrackMap);
	    	double startObtainCandiRoadTime = System.nanoTime();
	    	assistFunction.obtainCandidateRoadSet(processTaxiTrackMap, allGridIndexVerticesMap, allGridPolylineMap, PubParameter.radius);
	    	double endObtainCandiRoadTime = System.nanoTime();
	    	double obtainCandiRoadTime = (endObtainCandiRoadTime - startObtainCandiRoadTime)/Math.pow(10, 9);
			System.out.print("GPS点获得候选道路时间：" + obtainCandiRoadTime + "s" + "\n");
	    	//候选道路打分
	    	double startObtainRoadScoreTime = System.nanoTime();
	    	assistFunction.obtainCandidatedRoadScore(processTaxiTrackMap, PubParameter.thegema, PubParameter.directWeight);
	    	double endObtainRoadScoreTime = System.nanoTime();
	    	double obtainRoadScoreTime = (endObtainRoadScoreTime - startObtainRoadScoreTime)/Math.pow(10, 9);
	    	System.out.print("GPS点获得候选道路分数时间：" + obtainRoadScoreTime + "s" + "\n");
	    	//路径选取			 
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
		
	/**出租车载客轨迹：只选取有载客记录的轨迹
	 * targetIDStr:出租车GPSid
	 * startTimeStr:开始时间
	 * endTimeStr:结束时间
	 * */
	public static ReturnGPSAndPath obtainCarrypassGPSAndMatchPath(String targetIDStr, String startTimeStr, String endTimeStr){
		boolean isOK = false;
		String[] paraArray = new String[3];
		isOK = parameterProc(targetIDStr, startTimeStr, endTimeStr, paraArray);
		targetIDStr = paraArray[0];
		startTimeStr = paraArray[1];
		endTimeStr = paraArray[2];
    	ReturnGPSAndPath returnGPSAndPath = new ReturnGPSAndPath();
    	try {
    		if (isOK) {
    			//获得数据库中GPS数据
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点   
    			/*根据出租车的载客情况，提取出租车载客的多条轨迹*/
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
	
	/* 获得某段时间内出租车轨迹：
	 * 不考虑是否载客*/
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
    			//获得数据库中GPS数据
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点   			
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
	
	/*GPS点纠正结果生成xml*/
	public String obtainTaxiGPSDataAndCorrXml(String targetIDStr, String startTimeStr, String endTimeStr) throws ParserConfigurationException{
//		String user = "root";
//    	String password = "123456";
//    	//获得数据库中GPS数据
//		ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
//		AssistFunction assistFunction = new AssistFunction();
//		assistFunction.obtainGPSDataFromDatabase(user, password, taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
//		ArrayList<OriginCorrectNode> returnAllGPSAndCorrectGPS = obtainGPSCorrection(taxiGPSArrayList);
//		ArrayList<ReturnMatchNode> returnOriginGPSArrayList = returnAllGPSAndCorrectGPS.get(0);
//		ArrayList<ReturnMatchNode> returnCorrectGPSArraylist = returnAllGPSAndCorrectGPS.get(1);
//		org.dom4j.Document document = DocumentHelper.createDocument();
//		//创建根结点
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
	 * 路段行程时间统计分析
	 * 思路：
	 * 1.数据库时空约束：时间与空间上的约束，先进行空间约束后进行时间约束
	 * 		根据link的外包矩形范围，在数据库检索符合条件的点，然后再根据时间约束进行检索；
	 * 2.根据出租车ID分类：检索的数据按出租车ID进行分类，并按照时间由小到大进行排序；
	 * 3.轨迹剖分
	 * 4.时间扩展
	 * 5.地图匹配
	 * 6.目标路段判断：根据候选道路ID是否包含目标路段LinkID，进一步判断出现在路段上,符合条件的GPS点
	 * 7.插值计算：在出租车刚进入路段位置与出租车刚离开路段位置进行插值
	 * 8.统计每辆出租车通过路段的时间
	 * ***************************************************************************/

	/*所有出租车数据(载客与不载客)的通行时间统计
	 * 输入：
	 * linkID：路段ID
	 * startTimeStr：开始时间
	 * endTimeStr：结束时间
	 * 输出：
	 * 出租车ID以及时间段内通过linkID的时间
	 * */
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsAll(int targetLinkID, String startTimeStr, String endTimeStr){
		double systemStartTime = System.nanoTime();
		//所有车租车的路段行程时间
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
    			MapMatchEdge targetEdge = new MapMatchEdge();//目标路段  			
    			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//获得目标路段   			
    			double[]leftDownLB = new double[2];
    			double[]rightTopLB = new double[2];
    			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
    			double minLog = leftDownLB[0];
    			double minLat = leftDownLB[1];
    			double maxLog = rightTopLB[0];
    			double maxLat = rightTopLB[1];  			
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
    			//获得满足时空约束条件的数据
    			System.out.print("开始读数据库：" + '\n');
    			double startReadDatabase = System.nanoTime();
    			DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
    			double endReadDatabase = System.nanoTime();
    			System.out.print("结束读数据库：" + '\n');
    			double readDatabaseTime = (startReadDatabase - endReadDatabase)/Math.pow(10, 9);
    			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点
    			//根据ID进行分类
    			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
    			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap);   			
    			linkTravelTimeProcessSingleThread(targetLinkID, targetEdge, startTimeStr, endTimeStr, taxiSortMap,linkTravelTimeArrayList);
			}
			double systemEndTime = System.nanoTime();
	    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
	    	System.out.print("程序运行时间：" + processTime + "s" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return linkTravelTimeArrayList;
	}
	
	/*通行时间处理函数：单线程*/
	public void linkTravelTimeProcessSingleThread(int targetLinkID, MapMatchEdge targetEdge, String startTimeStr, String endTimeStr,
			Map<String, ArrayList<TaxiGPS>> taxiSortMap, ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList){
		try {
			AssistFunction assistFunction = new AssistFunction();
	    	ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段所有出租车的通行时间信息
	    	//获得所有出租车的通行时间
	    	assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
	    			PubParameter.sampleThreshold, PubParameter.expandTime, taxiTravelTimeArrayList);
	    	for (int i = 0; i < taxiTravelTimeArrayList.size(); i++) {
	    		TaxiTravelTime taxiTravelTime = taxiTravelTimeArrayList.get(i);
	    		String taxiID = taxiTravelTime.getTaxiID();//出租车ID
	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//开始进入路段时间
	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//路段通行时间
	    		Map<String, Double> meanSpeedMap = taxiTravelTime.getTaxiMeanSpeedMap();
	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//路段上的GPS点
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
	
	/*多线程
	 * 路段载客车辆的通行时间统计
	 * 此方法废弃，用线程池代替*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassengerBackUps(int targetLinkID, String startTimeStr, String endTimeStr){
		double systemStartTime = System.nanoTime();
		//所有出租车的路段行程时间
		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//路段通行时间的计算结果
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
    			MapMatchEdge targetEdge = new MapMatchEdge();//目标路段
    			targetEdge = assistFunction.obtainTargetEdge(targetLinkID, polylineCollArrayList);//获得目标路段
    			double[]leftDownLB = new double[2];
    			double[]rightTopLB = new double[2];
    			PubClass.boundingRectangleLongLat(leftDownLB, rightTopLB, targetEdge, PubParameter.radius);
    			double minLog = leftDownLB[0];
    			double minLat = leftDownLB[1];
    			double maxLog = rightTopLB[0];
    			double maxLat = rightTopLB[1];  			
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();   
				//获得满足时空约束条件的数据
				System.out.print("开始读数据库：" + '\n');
				double startReadDatabase = System.nanoTime();
				DatabaseFunction.obtainGPSDataAccordTimeSpatialFilter(taxiGPSArrayList, startTimeStr, endTimeStr, minLog, minLat, maxLog, maxLat);
    			double endReadDatabase = System.nanoTime();
    			System.out.print("结束读数据库：" + '\n');
    			double readDatabaseTime = (endReadDatabase - startReadDatabase)/Math.pow(10, 9);
    			System.out.print("读数据库时间：" + readDatabaseTime + "s" + '\n');
    			ArrayList<TaxiGPS> carryPassengerGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateNonCarryPassengerGPSData(taxiGPSArrayList, carryPassengerGPSDataArrayList);
    			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
    			DatabaseFunction.eliminateZeroSpeedGPSData(carryPassengerGPSDataArrayList, eliminateZeroSpeedGPSDataArrayList); //去除速度为零的GPS点
    			linkTravelTimeProcessMultiThread(targetLinkID, startTimeStr, endTimeStr, targetEdge, eliminateZeroSpeedGPSDataArrayList, linkTravelTimeArrayList);
			}
			double systemEndTime = System.nanoTime();
	    	double processTime = (systemEndTime - systemStartTime)/Math.pow(10, 9);
	    	System.out.print("程序运行时间：" + processTime + "s" + '\n');		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return linkTravelTimeArrayList;
	}
	
	/*路段载客车辆的通行时间统计
	 * 通过读取序列化数据的方式*/
	public ArrayList<ReturnLinkTravelTime> linkTravelTimeStatisticsCarryPassenger(int targetLinkID, String startTimeStr, String endTimeStr){
		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = new ArrayList<ReturnLinkTravelTime>();//路段通行时间的计算结果
		try {
			String fileName = PropertiesUtilJAR.getProperties("travelTime3");
			System.out.print("开始读序列化数据" + '\n');
			MapMatchAlgorithm.instance().readSeriseLinkTravelTime(fileName);
			System.out.print("序列化数据读取成功" + '\n');
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
	 * 此方法废弃，用线程池代替
	 * 获得路段通行时间
	 * eliminateZeroSpeedGPSDataArrayList：去除速度为零的GPS数据
	 * linkTravelTimeArrayList:目标路段所有出租车的通行时间
	 * targetEdge：目标路段*/
	public static Map<Integer, ArrayList<TaxiTravelTime>> allTaxiTravelTimeMap = 
		new HashMap<Integer, ArrayList<TaxiTravelTime>>();//所有出租车通行时间,以线程ID为索引
	public void linkTravelTimeProcessMultiThread(int targetLinkID, String startTimeStr, String endTimeStr, MapMatchEdge targetEdge, 
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList, ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList){
		try {
			allTaxiTravelTimeMap = new HashMap<Integer, ArrayList<TaxiTravelTime>>();
			//根据ID进行分类
			AssistFunction assistFunction = new AssistFunction();
			Map<String, ArrayList<TaxiGPS>> taxiSortMap = new HashMap<String, ArrayList<TaxiGPS>>();
			assistFunction.sortTaxiAccordID(eliminateZeroSpeedGPSDataArrayList, taxiSortMap);
			int taxiTotalCount = taxiSortMap.size();//出租车数目
			int threadCount = 4;//线程数目
			double taxiCountEveryThread = (double)taxiTotalCount/threadCount;
			List<ThreadTravelTimeStatistics> allThreads = new ArrayList<ThreadTravelTimeStatistics>();
			//4个线程进行计算
			//如果小于1，则每个线程计算一辆出租车通行时间
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
		        		ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
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
			//否则，每个线程计算多辆出租车
			else {
				//如果为整数，即能够被整除
				String taxiCountEveryThreadStr = String.valueOf(taxiCountEveryThread);
				if (PubClass.isInteger(taxiCountEveryThreadStr)) {
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;//临时变量，计算每个线程的出租车数目
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
		        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
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
				//不为整数
				else {
					int integerPart =PubClass.obtainIntegerPart(taxiCountEveryThreadStr);
					Map<String, ArrayList<TaxiGPS>> tempTaxiMap = new HashMap<String, ArrayList<TaxiGPS>>();
					int threadID = 0;
					int tempCount = 0;
					int tempThreadCount = 0;//临时变量，计算线程数目
					int taxiCount = 0;//临时变量，计算每个线程的出租车数目
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
		        				ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
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
			        			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = new ArrayList<TaxiTravelTime>();//目标路段出租车的通行时间信息
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
    	    		String taxiID = taxiTravelTime.getTaxiID();//出租车ID
    	    		ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();//开始进入路段时间
    	    		Map<String, Double> travelTimeMap = taxiTravelTime.getTravelTimeMap();//开始进入路段时间对应的路段通行时间
    	    		Map<String, ArrayList<MapMatchNode>> GPSTravelMap = taxiTravelTime.getGPSTravelMap();//开始进入路段时间对应路段上的GPS点
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
