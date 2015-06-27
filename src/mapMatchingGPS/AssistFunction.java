package mapMatchingGPS;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.search.FromStringTerm;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.jibx.runtime.IAbstractMarshaller;

import com.esri.arcgis.geoprocessing.tools.analyst3dtools.Int;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.sun.org.apache.bcel.internal.generic.NEW;

import entity.Edge;
import entity.Node;
import entity.retuNode;
import utilityPackage.DBConnectionCreater;
import utilityPackage.JDBCConnectionCreator;
import utilityPackage.PropertiesUtil;
import utilityPackage.PubParameter;

import sun.beans.editors.IntEditor;
import utilityPackage.PubClass;
/**
 * 基本操作函数*/
public class AssistFunction {
	
	/*获得有载客记录的GPS数据
	 *根据出租车的载客情况，提取出租车载客的多条轨迹 */
	public void obtainCarryPassengerData(ArrayList<TaxiGPS> taxiGPSArrayList,Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap){
		try {
			ArrayList<TaxiGPS> carrayPassTrack = new ArrayList<TaxiGPS>();//存储有载客记录的出租车轨迹点      	
        	TaxiGPS ttTaxiGPS = new TaxiGPS();
			ttTaxiGPS = taxiGPSArrayList.get(0);			
			//若首个数据为载客状态
        	if (ttTaxiGPS.status == 262144 || ttTaxiGPS.status == 262145) {
        		carrayPassTrack.add(ttTaxiGPS);
			}
        	int trackNum = 0;//轨迹数
			for (int i = 1; i < taxiGPSArrayList.size(); ++i)
			{
				TaxiGPS cTaxiGPS = new TaxiGPS();//当前GPS
				TaxiGPS pTaxiGPS = new TaxiGPS();//上一GPS
				cTaxiGPS = taxiGPSArrayList.get(i);
				pTaxiGPS = taxiGPSArrayList.get(i - 1);				
				//有乘客
				if (cTaxiGPS.status == 262144 || cTaxiGPS.status == 262145)
				{
					carrayPassTrack.add(cTaxiGPS);
					//如果轨迹点为载客点，生成一条轨迹
					if (i == taxiGPSArrayList.size()-1) {
						trackNum++;
						carrayPassTrackMap.put(trackNum, carrayPassTrack);
						//让其重新指向新的内存，然后释放内存,而不能直接由clear
						carrayPassTrack = new ArrayList<TaxiGPS>();
						carrayPassTrack.clear();
					}	
				}
				//当前未载客，但上一记录载客
				else if (cTaxiGPS.status == 0 || cTaxiGPS.status == 1)
				{
					if (pTaxiGPS.status == 262144 || pTaxiGPS.status == 262145)
					{
						if (carrayPassTrack.size() > 1)
						{
							trackNum++;
							//trackSet只存储有乘客的出租车轨迹点ID集合
							carrayPassTrackMap.put(trackNum, carrayPassTrack);
							carrayPassTrack = new ArrayList<TaxiGPS>();
							carrayPassTrack.clear();														
						}
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
	 * 对GPS轨迹的连续静止点进行处理:连续静止点为坐标相同的点
	 * 对于连续静止点，只取连续静止点的最大时间GPS点，并记录最大连续静止时间
	 * @param taxiTrackMap	原始轨迹
	 * @param processTaxiTrackMap	连续静止点处理后的GPS轨迹
	 */
	public void continuousStaticPointProcess(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap) {
		try {
			ArrayList<TaxiGPS> taxiArrayList = taxiTrackMap.get(1);
			ArrayList<TaxiGPS> processArrayList = new ArrayList<TaxiGPS>();
			int num = 0;
			int continuousStaticTime = 0;//连续静止时间
			for (int i = 0; i < taxiArrayList.size() - 1; i++) {
				TaxiGPS currentTaxiGPS = taxiArrayList.get(i);
				double currentTaxiLongitude = currentTaxiGPS.getLongitude();
				double currentTaxiLatitude = currentTaxiGPS.getLatitude();				
				TaxiGPS nextTaxiGPS = taxiArrayList.get(i + 1);
				double nextTaxiLongitude = nextTaxiGPS.getLongitude();
				double nextTaxiLatitude = nextTaxiGPS.getLatitude();
				double deltLongitude = Math.abs(currentTaxiLongitude - nextTaxiLongitude);
				double deltLatitude = Math.abs(currentTaxiLatitude - nextTaxiLatitude);
				if (deltLatitude < PubParameter.continuousStaticLongitudeLatitudeThreshold && deltLongitude < PubParameter.continuousStaticLongitudeLatitudeThreshold) {					
					String currentTimeStr = currentTaxiGPS.getLocalTime();
					String nextTimeStr = nextTaxiGPS.getLocalTime();
					continuousStaticTime = continuousStaticTime + (int)PubClass.obtainTimeInterval(currentTimeStr, nextTimeStr);
				}
				else {
					if (continuousStaticTime != 0) {
						currentTaxiGPS.setContinuousStaticTime(continuousStaticTime);//连续静止时间
						currentTaxiGPS.setMaxContinuousStaticPoint(true);
					}
					processArrayList.add(currentTaxiGPS);
					continuousStaticTime = 0;
//					num ++;
//					System.out.print(num + ":" + currentTaxiGPS.getLocalTime() + "\n");
				}
				if (i == taxiArrayList.size() - 2) {
					currentTaxiGPS = taxiArrayList.get(i + 1);
					if (continuousStaticTime != 0) {
						currentTaxiGPS.setContinuousStaticTime(continuousStaticTime);//连续静止时间
						currentTaxiGPS.setMaxContinuousStaticPoint(true);
					}
					processArrayList.add(currentTaxiGPS);
				}
			}
			processTaxiTrackMap.put(1, processArrayList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/*选择距离GPS点radius范围内的道路集合
	 *如果GPS点在路段中间，则距离为在路段上的投影距离；否则，为GPS点到路段两端点的最小长度 
	 *carrayPassTrackMap:key值从1开始
	 *思路：
	 *1.以GPS点为中心，进行粗过滤.
	 *2.以GPS点为中心，在粗过滤的路段中，圈选radius范围内的路段
	 *2.求GPS点到路段的最小距离，考虑路段为曲线情况
	 *3.求GPS点到路段的方向夹角*/
	public void obtainCandidateRoadSet(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap,	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap, double radius){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {
					double startTime = System.nanoTime();
					TaxiGPS tempTaxiGPS = gpsTrackPointArrayList.get(j);
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = new ArrayList<MapMatchEdge>();//候选道路集合
					Map<Integer, Double[]> tdistScoreSetMap = new HashMap<Integer, Double[]>();//以edgeID为索引，存储距离与距离得分
					MapMatchNode cNode = new MapMatchNode();
					cNode.x = tempTaxiGPS.longitude;
					cNode.y = tempTaxiGPS.latitude;
					cNode.nodeID = -1;
//					ArrayList<MapMatchEdge> coarseFiltPlinesArrayList = new ArrayList<MapMatchEdge>();//粗过滤后选择的路径集合
//					coarseFiltration(tempTaxiGPS, allGridIndexVerticesMap, allGridPolylineMap, coarseFiltPlinesArrayList);//粗过滤						
					int gridIndex = PubClass.obtainGridIndex(allGridIndexVerticesMap, cNode);
					ArrayList<MapMatchEdge> polylineArrayList = allGridPolylineMap.get(gridIndex);					
					if (polylineArrayList != null) {
						for (int k = 0; k < polylineArrayList.size(); k++) {
							MapMatchEdge tEdge = polylineArrayList.get(k);
							MapMatchNode tbeginNode = new MapMatchNode();
							MapMatchNode tendNode = new MapMatchNode();
							tbeginNode = tEdge.getBeginPoint();
							tendNode = tEdge.getEndPoint();						
							/*判断路段是否符合条件,
							 * 求端点到路段距离：线段可能为曲线
							 *1.至少有一个端点在缓冲区范围内，包括两个端点都在的情况
							 *2.端点不在缓冲区范围内，但路段经过缓冲区,即路段与圆相交与两点*/
							//至少有一个端点在缓冲区范围内
							if (isNodeInCircle(cNode, tbeginNode, radius) || isNodeInCircle(cNode, tendNode, radius)) {							
								//与起点间的距离临时作为最小距离
								double minDis = distance(tbeginNode, cNode);
								Integer edgeID = tEdge.getEdgeID();
								Double []temp = new Double[2];
								temp[0] = minDis;
								tdistScoreSetMap.put(edgeID, temp);
								tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
								//投影点在路段起终点间
								if (isProjPointBetweenSE(tbeginNode, tendNode, cNode)) {
									//求点到路段的最小距离：路段可能为曲线								
									ArrayList<MapMatchNode> tpointCollArrayList = tEdge.getPointCollArrayList();
									for (int l = 0; l < tpointCollArrayList.size() - 1; l++) {
										MapMatchNode ttbeginNode = tpointCollArrayList.get(l);
										MapMatchNode ttendNode = tpointCollArrayList.get(l+1);
										double tDis = distancePointToLine(ttbeginNode, ttendNode, cNode);
										if (tDis < minDis) {
											minDis = tDis;
											temp[0] = minDis;
											if (tdistScoreSetMap.containsKey(edgeID)) {
												tdistScoreSetMap.remove(edgeID);
												tdistScoreSetMap.put(edgeID, temp);
												tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
											}
											else {
												tdistScoreSetMap.put(edgeID, temp);
												tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
											}										
										}
									}								
								}
								//投影点在路段延长线上，起点或终点延长线
								else {
									double tDis = distance(tendNode, cNode);
									if (tDis < minDis) {
										minDis = tDis;
										edgeID = tEdge.getEdgeID();
										temp[0] = minDis;
										tdistScoreSetMap.put(edgeID, temp);
										tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
									}
								}
								tcandidateEdgeSetArrayList.add(tEdge);						
							}
							//路段与圆相交于两点：端点不在缓冲区范围内，但路段经过缓冲区
							//1.到线段所在直线距离小于radius,此处的距离遍历线段的节点集合，求GPS点到每一小段的距离；
							//2.投影点位于线段两端点之间
							else if (PubClass.distancePointToArc(tEdge, cNode) < radius &&
									isProjPointBetweenSE(tbeginNode, tendNode, cNode)) {
								double minDis = PubClass.distancePointToArc(tEdge, cNode);
								Integer edgeID = tEdge.getEdgeID();
								Double []temp = new Double[2];
								temp[0] = minDis;
								tdistScoreSetMap.put(edgeID, temp);
								tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
								tcandidateEdgeSetArrayList.add(tEdge);	
							}						
						}
					}					
					tempTaxiGPS.setCandidateEdgeSetArrayList(tcandidateEdgeSetArrayList);
					double endTime = System.nanoTime();
					double time = (endTime - startTime)/Math.pow(10, 9);
					System.out.print("GPS点" + j + ":" + gpsTrackPointArrayList.size() + ":" + tempTaxiGPS.getTargetID() + "获得候选道路集：" + time + "s" + '\n');					
				}		
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}				
	}
	
	/*粗过滤（此方法效果不好）:
	 * 以GPS点为中心做缓冲区,用线段起点、终点进行粗过滤
	 * 选择与缓冲区相交的线段，满足两条件之一即可
	 * 1.GPS点到线段起终点距离小于缓冲缓冲区半径
	 * 2.或者起终点线段与GPS点为中心的正方形相交
	 * polylineCollArrayList:要进行粗过滤的线段集合
	 * coarseFiltPlinesArrayList：存储粗过滤后的线段*/
	public void coarseFiltration(TaxiGPS taxiGPS, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap, 
			Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap, ArrayList<MapMatchEdge> coarseFiltPlinesArrayList){
		try {
			MapMatchNode tNode = new MapMatchNode();
			tNode.x = taxiGPS.longitude;
			tNode.y = taxiGPS.latitude;
			double[] GPSxy = new double[2];
			PubClass.coordinateTransToPlaneCoordinate(tNode, PubParameter.wuhanL0, GPSxy);
			Double []leftDown = new Double[]{GPSxy[0] - PubParameter.candRoadCoarseFiltratBuffer/2,GPSxy[1] - PubParameter.candRoadCoarseFiltratBuffer/2};//左下角点
			Double []rightTop = new Double[]{GPSxy[0] + PubParameter.candRoadCoarseFiltratBuffer/2,GPSxy[1] + PubParameter.candRoadCoarseFiltratBuffer/2};//右上
			Double []rightDown = new Double[]{leftDown[0], rightTop[1]};//右下角点			
			Double []leftTop = new Double[]{rightTop[0], leftDown[1]};//左上			
			//获得网格索引
			int gridIndex = PubClass.obtainGridIndex(allGridIndexVerticesMap, tNode);
	        if (allGridPolylineMap.containsKey(gridIndex)) {
	        	ArrayList<MapMatchEdge> polylineArrayList = allGridPolylineMap.get(gridIndex);
	        	for (int i = 0; i < polylineArrayList.size(); i++) {
					MapMatchEdge edge = polylineArrayList.get(i);
					if (edge.getEdgeID() == 1805) {
						System.out.print("");
					}
					MapMatchNode beginNode = edge.getBeginPoint();
					MapMatchNode endNode = edge.getEndPoint();
					double tDistance = distancePointToLineSegment(beginNode, endNode, tNode);
					if (tDistance <= PubParameter.candRoadCoarseFiltratBuffer || PubClass.isLineSegmentIntersect(beginNode, endNode, leftDown, rightDown)||
							PubClass.isLineSegmentIntersect(beginNode, endNode, rightDown, rightTop)||PubClass.isLineSegmentIntersect(beginNode, endNode, rightTop, leftTop)||
							PubClass.isLineSegmentIntersect(beginNode, endNode, leftTop, leftDown)) {
						coarseFiltPlinesArrayList.add(edge);
					}
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/*判断点tNode是否在以centerNode为中心的缓冲半径为radius的圆内
	 * 若是：返回true
	 * 否则：返回false*/
	public boolean isNodeInCircle(MapMatchNode centerNode, MapMatchNode tNode, double radius){
		double []xy = new double[2];
		coordinateTransToPlaneCoordinate(centerNode, PubParameter.wuhanL0, xy);		
		double centerNodex = xy[0];
		double centerNodey = xy[1];
		coordinateTransToPlaneCoordinate(tNode, PubParameter.wuhanL0, xy);
		double tNodeX = xy[0];
		double tNodeY = xy[1];
		double maxX = centerNodex + radius;
		double minX = centerNodex - radius;
		double maxY = centerNodey + radius;
		double minY = centerNodey - radius;
		if(tNodeX > minX && tNodeX < maxX && tNodeY > minY && tNodeY < maxY ){
			double tdis = Math.sqrt(Math.pow(centerNodex - tNodeX,2) + Math.pow(centerNodey - tNodeY,2));
			if(tdis <= radius)
			  return true;
			else return false;
		}
		else
		 return false;		
	}
	
	/*经纬度坐标转换为空间直角坐标
	 * a：椭球长半轴，WGS84椭球的长半轴为6378137.000001
	 * e: 椭球第一偏心率的平方，WGS84椭球的e为0.00669437999013
	 * xyz:返回转换后坐标数组*/ 
	public void coordinateTransfer(MapMatchNode tNode, double []xyz){
		try {
			double L = tNode.getX();//经度
			double B = tNode.getY();//纬度
		    double pi = Math.PI;
		    L = L*pi/180;
		    B = B*pi/180;
			double e = 0.00669437999013;
			double a = 6378137.000001;
			double H = 0;//高程赋0值
			double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
		    double N = a/W;
		    double x = (N + H)*Math.cos(B)*Math.cos(L);
		    double y =(N + H)*Math.cos(B)*Math.sin(L);
		    double z = (N * (1 - e) + H) * Math.sin(B);
		    xyz[0] = x;
		    xyz[1] = y;
		    xyz[2] = z;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/* 经纬度坐标的点转换为平面坐标
	 * xy:返回转换后坐标数组
	 * L0:中央子午线经度，武汉市为：114
	 * 度数计量单位转换为弧度
	 * */
	public void coordinateTransToPlaneCoordinate(MapMatchNode tNode, double L0, double []xy){
		double a = 6378137, e = 0.0066943799013;        /////////长半径和第一偏心率的平方                               /////////中央子午线经度，单位：弧度  
        double DH = 0;                              /////////投影面抬高DH米 
        double L = tNode.x;
        double B = tNode.y;
        double pi = Math.PI;
        L = L * pi/180;
	    B = B * pi/180;
	    L0 = L0 * pi/180;
        double e1 = e / (1 - e);
        double W = Math.sqrt(1 - e * Math.sin(B) * Math.sin(B));
        double V = Math.sqrt(1 + e1* Math.cos(B) * Math.cos(B));
        ///////////////////////////投影高程面引起的变化
        double M = a * (1 - e) / Math.pow(W, 3);
        //B = e * Math.Sin(B) * Math.Cos(B) * (1 - e * Math.Sin(B) * Math.Sin(B))/(M*W*Math.Sqrt(1-e))*DH+B;///以平均曲率半径变化推算长半轴变化
        B = e * Math.sin(B) * Math.cos(B) / (M) * DH + B;
        W = Math.sqrt(1 - e * Math.sin(B) * Math.sin(B));
        V = Math.sqrt(1 + e1 * Math.cos(B) * Math.cos(B));
        a=W*DH+a;///////以某点卯酉圈曲率半径变化推算长半轴变化，偏心率不变
        //////////////////////////////////////////////////////////////
        double N = a / W;
        double it2 = e1 * Math.cos(B) * Math.cos(B);
        double t = Math.tan(B);
        double X = CalMeridian(B);
        double dl=L-L0;
        double x = X + N / 2.0 * Math.sin(B) * Math.cos(B) * dl * dl + N / 24.0 * Math.sin(B) * Math.pow(Math.cos(B), 3) * (5 - t * t + 9 * it2 + 4 * it2 * it2) * Math.pow(dl, 4)
            + N / 720.0 * Math.sin(B) * Math.pow(Math.cos(B), 5) * (61 - 58 * t * t + Math.pow(t, 4)) * Math.pow(dl, 6);
        double y = N * Math.cos(B) * dl + N / 6.0 * Math.pow(Math.cos(B), 3) * (1 - t * t + it2) * dl * dl * dl
            + N / 120.0 * Math.pow(Math.cos(B), 5) * (5 - 18 * t * t + Math.pow(t, 4) + 14 * it2 - 58 * it2 * t * t) * Math.pow(dl, 5);
		xy[0] = x;
		xy[1] = y;
	}
	
	/// <summary>
    /// 求对应纬度的子午线弧长
    /// </summary>
    /// <param name="B">弧度</param>
    /// <returns></returns>
    public double CalMeridian(double B)
    {
    	double a=6378137, e=0.0066943799013;        /////////长半径和第一偏心率的平方
        double m0 = a * (1 - e);
        double m2 = 3.0 / 2.0 * e * m0;
        double m4 = 5.0 / 4.0 * e * m2;
        double m6 = 7.0 / 6.0 * e * m4;
        double m8 = 9.0 / 8.0 * e * m6;

        double a0 = m0 + m2 / 2.0 + 3.0 / 8.0 * m4 + 5.0 / 16.0 * m6 + 35.0 / 128.0 * m8;
        double a2 = m2 / 2.0 + m4 / 2.0 + 15.0 / 32.0 * m6 + 7.0 / 16.0 * m8;
        double a4 = m4 / 8.0 + 3.0 / 16.0 * m6 + 7.0 / 32.0 * m8;
        double a6 = m6 / 32.0 + m8 / 16.0;
        double a8 = m8 / 128.0;

        double X= a0*B - a2/2.0 * Math.sin(2 * B) + a4/4.0 * Math.sin(4 * B) - a6/6.0 * Math.sin(6 * B) + a8/8.0 * Math.sin(8 * B);
        return X;
    }	
	
	/*判断是否为同一点*/
	public boolean isTheSameNode(MapMatchNode tNode1, MapMatchNode tNode2){
		boolean isTheSame = false;
		try {
			if (Math.abs(tNode1.x - tNode2.x) < 0.000001 && Math.abs(tNode1.y - tNode2.y) < 0.000001) {
				isTheSame = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();			
		}
		return isTheSame;
	}
	
	/* 平面点到线段的投影是否在线段两端点之间：判断点cNode是否在startNode、endNode两点之间
	 * 若在之间：true
	 * 否则，false
	 * 思路：
	 * 1.点startNode、endNode构成线段和线段任一端点、cNode所构直线之间任一个夹角> pi/2,则cNode的投影在线段延长线上，返回false
	 * 2.否则，返回true
	 * */
	public boolean isProjPointBetweenSE(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode)
	{
		double angle1 = 0;//起点处夹角
		double angle2 = 0;//终点处夹角
		double seNodeDis = distance(startNode, endNode);
		try {
			if (endNode.nodeID == cNode.nodeID) {
				angle1 = 0;			
			}
			else if (startNode.nodeID == cNode.nodeID) {
				angle2 = 0;
			}
			else {				
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);	
				double cNodeX = xy[0];
				double cNodeY = xy[1];
				coordinateTransToPlaneCoordinate(startNode, PubParameter.wuhanL0, xy);				
				double starNodeX = xy[0];
				double starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				double endNodeX = xy[0];
				double endNodeY = xy[1];				
				//起终点向量 ，模   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//起点以及与当前点向量，模
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;
				double scNodeDis = distance(startNode, cNode);
				//起终点向量与起点当前点向量 两向量夹角
				angle1 = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//终点以及与当前点向量，模
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//终起点向量与终点当前点向量 两向量夹角
				angle2 = Math.acos((-seDeltX * ecDeltX + (-seDeltY) * ecDeltY )/(seNodeDis * ecNodeDis));				
			}	
			//不在两端点间
			if (angle1 > Math.PI/2 || angle2 > Math.PI/2) {
				return false;
			}
			else {
				return true;
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*为GPS点的候选道路打分
	 * 1.距离概率
	 * 2.方向概率
	 * thegema:GPS方向与路段方向的夹角
	 * directWeight:方向权重常量
	 * 当speed < 5时，directWeight = directWeight[0]
	 * 否则，directWeight = directWeight[1]*/
	public void obtainCandidatedRoadScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, 
			double thegema, double []directWeight){
		//距离得分
		obtainCandidatedRoadDistanceScore(carrayPassTrackMap, thegema);
		//方向得分
		obtainCandidatedRoadDirectionScore(carrayPassTrackMap, directWeight);
		//综合得分
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {
					TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
					ArrayList<MapMatchEdge> candidateEdgeSetArrayList = taxiGPS.getCandidateEdgeSetArrayList();
					Map<Integer, Double[]> distScoreSetMap = taxiGPS.getDistScoreSetMap(); 
					Map<Integer, Double[]> directScoreSetMap = taxiGPS.getDirectScoreSetMap();
					Map<Integer, Double> distDirectScoreSetMap = new HashMap<Integer, Double>();
					for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
						MapMatchEdge edge = candidateEdgeSetArrayList.get(k);
						double distanceScore = distScoreSetMap.get(edge.getEdgeID())[1];
						double directScore = directScoreSetMap.get(edge.getEdgeID())[1];
						double distDirectScore = distanceScore + directScore;
						distDirectScoreSetMap.put(edge.getEdgeID(),distDirectScore);
					}
					taxiGPS.setDistDirectScoreSetMap(distDirectScoreSetMap);				
				}
			}				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}			
	}
	
	/*候选道路距离得分
	 * GPS点到候选路段距离的得分*/
	public void obtainCandidatedRoadDistanceScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double thegema){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {					
					TaxiGPS tempTaxiGPS = gpsTrackPointArrayList.get(j);
					System.out.print("GPS点" + j + ":" + gpsTrackPointArrayList.size() + ":" + tempTaxiGPS.getTargetID() + "获得距离得分：" + '\n');
//					//2014-07-15修改，速度为零的点，与正北方向夹角为零，不能用于计算方向得分
//					if (tempTaxiGPS.getSpeed() == 0) {
//						continue;
//					}
					Map<Integer, Double[]> tdistScoreSetMap = new HashMap<Integer, Double[]>();//距离得分
					tdistScoreSetMap = tempTaxiGPS.distScoreSetMap;
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = tempTaxiGPS.candidateEdgeSetArrayList;//候选道路集合
					if (tcandidateEdgeSetArrayList.size() > 0) {					
						for (int k = 0; k < tcandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge tEdge = tcandidateEdgeSetArrayList.get(k);
							int edgeID = tEdge.getEdgeID();
							Double []temp = tdistScoreSetMap.get(edgeID);
							double minDis = temp[0];
							//距离概率，距离的正态分布函数表示
							double distanctScore = Math.exp((-1) * Math.pow(minDis,2)/(2 * Math.pow(thegema,2)));
							temp[1] = distanctScore;
							tdistScoreSetMap.remove(edgeID);
							tdistScoreSetMap.put(edgeID, temp);
						}						
					}					
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/*候选道路方向得分
	 * GPS点候选路段方向的得分,简单处理，与起终点连接线段的夹角
	 * directWeight:方向权重*/
	public void obtainCandidatedRoadDirectionScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double []directWeight){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {
					TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
					System.out.print("GPS点" + j + ":" + gpsTrackPointArrayList.size() + ":" + taxiGPS.getTargetID() + "获得方向得分：" + '\n');					
					//速度为零的点，则与正北方向夹角为零，用出租车行驶方向与正北方向夹角代替出租车方向heading
					if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold) {
						if (j + 1 < gpsTrackPointArrayList.size()) {
							TaxiGPS nextTaxiGPS = gpsTrackPointArrayList.get(j + 1);
							if (taxiGPS.getLocalTime().equals("2013-01-01 00:07:06")) {
								System.out.print("GPS点" );
							}
							double heading = obtainZeroSpeedHeading(taxiGPS, nextTaxiGPS);
							taxiGPS.setHeading(heading);
						}
					}
					Map<Integer, Double[]> tdirectScoreSetMap = new HashMap<Integer, Double[]>();//距离得分
					double tdirectWeight = 0;
					if (taxiGPS.speed <= PubParameter.taxiSpeed) {
						tdirectWeight = directWeight[0];
					}
					else {
						tdirectWeight = directWeight[1];
					}
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = taxiGPS.candidateEdgeSetArrayList;//候选道路集合
					if (tcandidateEdgeSetArrayList != null && tcandidateEdgeSetArrayList.size() > 0) {					
						for (int k = 0; k < tcandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge tEdge = tcandidateEdgeSetArrayList.get(k);
							Integer edgeID = tEdge.getEdgeID();
							double []AngleAndPosition = new double[2]; 
							intersectAngleAndPosition(taxiGPS, tEdge, AngleAndPosition);
							//方向得分
							double angle = AngleAndPosition[0];
							double directScore = tdirectWeight * Math.cos(angle);
							Double []directInfo = new Double[3];
							directInfo[0] = AngleAndPosition[0];//GPS方向与线段夹角
							directInfo[1] = directScore;//方向分数
							directInfo[2] = AngleAndPosition[1];//方向一致性
							tdirectScoreSetMap.put(edgeID, directInfo);					
						}						
					}
					taxiGPS.setDirectScoreSetMap(tdirectScoreSetMap);
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 根据当前GPS点与下一GPS点关系，获得速度为零的GPS点与正北方向夹角
	 * @param currentTaxiGPS	速度为零的GPS点
	 * @param nextTaxiGPS	下一GPS点
	 * @return	速度为零的GPS点与正北方向夹角
	 */
	public double obtainZeroSpeedHeading(TaxiGPS currentTaxiGPS, TaxiGPS nextTaxiGPS) {
		double heading = 0;//出租车运动方向与正北方向夹角	(0 - 360)						
		try {
			MapMatchNode currentTaxiGPSNode = PubClass.ConvertTaxiGPSToMapMatchNode(currentTaxiGPS);
			MapMatchNode nextTaxiGPSNode = PubClass.ConvertTaxiGPSToMapMatchNode(nextTaxiGPS);
			double []xy = new double[2];
			coordinateTransToPlaneCoordinate(currentTaxiGPSNode,PubParameter.wuhanL0, xy);
			double x1 = xy[0];
			double y1 = xy[1];
			coordinateTransToPlaneCoordinate(nextTaxiGPSNode,PubParameter.wuhanL0, xy);
			double x2 = xy[0];
			double y2 = xy[1];
			//正北方向向量,y轴方向
			double deltY = 0;
			double deltX = 100;
			double dist1 = Math.sqrt(Math.pow(deltX,2) + Math.pow(deltY,2));
			double deltX1 = x2 - x1;
			double deltY1 = y2 - y1;
			double dist2 = Math.sqrt(Math.pow(deltX1,2) + Math.pow(deltY1,2));
			double a = Math.acos((deltX * deltX1 + deltY * deltY1)/(dist1 * dist2));//路段方向向量与正北方向的夹角			
			if (a < Math.PI/2)
			{
				//象限还是假定按顺时针方向旋转
				//第一象限
				if (deltX1 * deltY1 > 0)
				{
					heading = a;
				}
				//第二象限
				else if (deltX1 * deltY1 < 0)
				{
					heading = 2 * Math.PI - a;
				}
			}
			else if(a - Math.PI/2 < 0.000001)
			{
				if (deltY1 > 0)
				{
					heading = Math.PI/2;
				}
				else if (deltY1 < 0)
				{
					heading = 3 * Math.PI/2;
				}
			}
			else if(a > Math.PI/2)
			{
				//第三象限
				if (deltX1 * deltY1 > 0)
				{
					heading = 2 * Math.PI - a;
				}
				//第四象限
				else if (deltX1 * deltY1 < 0)
				{
					heading = a;
				}
			}			
		heading = heading * 180/Math.PI;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return heading;
	}
	
	/*返回出租车行驶方向与路段的方向夹角(弧度)angle = intersectAngleAndPosition[0]，为锐角，
	 * 与路段方向的关系intersectAngleAndPosition[1]
	 * 根据路段的起终点线段粗略求得夹角，只是粗略值，需要进一步优化
	 *position:出租车方向与路段方向的关系，1为与路段起点终点同向，-1为反向
	 *gpsAngle：为出租车行驶方向，与正北方向的夹角，范围（0~360）
	 *lineAngle：求路段与正北方向夹角
	 *以中央子午线的投影为纵坐标轴x，规定x轴向北为正；以赤道的投影为横坐标轴y，规定y轴向东为正
	 *a:路段方向向量（deltX1,deltY1）与正北方向的方向夹角，顺时针旋转到正北方向角度
	 *当a为锐角时：若deltX1*deltY1 > 0，lineAngle = a；
	 *             若deltX1*deltY1 < 0, lineAngle = 2*pi - a；
	 *当a为直角时：若deltX1 > 0, lineAngle = pi/2;
	 *			   若deltX1 < 0，lineAngle = 3*pi/2;
	 *当a为钝角时：若deltX1*deltY1 > 0, lineAnge = 2*pi - a；
	 *             若deltX1*deltY1 < 0, lineAngle = a；
	 *若gpsAngle > lineAngel 
					若gpsAngle - lineAngle <= pi/2 , 则angle = gpsAngle - lineAngle,同向position = 1
					若pi/2 < gpsAngle - lineAngle <= pi,则,angle = pi - (gpsAngle - lineAngle),异向position = -1;
					若pi < gpsAngle - lineAngle <= 3*pi/2，则angle =(gpsAngle - lineAngle) - pi;异向position = -1;
					若3*pi/2 < gpsAngle - lineAngle <= 2*pi，则angle =2*pi - (gpsAngle - lineAngle);同向position = 1
	 * 若gpsAngle < lineAngel 
					若lineAngle - gpsAngle <= pi/2 , 则angle = lineAngle - gpsAngle,同向position = 1
					若pi/2 < lineAngle - gpsAngle <= pi,则,angle = pi - (lineAngle - gpsAngle),异向position = -1;
					若pi < lineAngle - gpsAngle <= 3*pi/2，则angle =(lineAngle - gpsAngle) - pi;异向position = -1;
					若3*pi/2 < lineAngle - gpsAngle <= 2*pi，则angle =2*pi - (lineAngle - gpsAngle);同向position = 1;
	 *若gpsAngle == lineAngel 
					angle = 0, position = 1;
	 *node1:表示起点
	 *node2:表示终点*/
	public void intersectAngleAndPosition(TaxiGPS tempTaxiGPS, MapMatchEdge edge, double[]AngleAndPosition)
	{
		double angle = 0;//出租车行驶方向与路段的方向夹角		
		MapMatchNode node1 = edge.getBeginPoint();
		MapMatchNode node2 = edge.getEndPoint();		
		try {
			double []xy = new double[2];
			coordinateTransToPlaneCoordinate(node1,PubParameter.wuhanL0, xy);
			double x1 = xy[0];
			double y1 = xy[1];
			coordinateTransToPlaneCoordinate(node2,PubParameter.wuhanL0, xy);
			double x2 = xy[0];
			double y2 = xy[1];
			//正北方向向量,y轴方向
			double gpsAngle = tempTaxiGPS.getHeading();//GPS与正北方向夹角
			int position = 1;
			gpsAngle = gpsAngle * Math.PI/180;
			double deltY = 0;
			double deltX = 100;
			double dist1 = Math.sqrt(Math.pow(deltX,2) + Math.pow(deltY,2));
			double deltX1 = x2 - x1;
			double deltY1 = y2 - y1;
			double dist2 = Math.sqrt(Math.pow(deltX1,2) + Math.pow(deltY1,2));
			double a = Math.acos((deltX * deltX1 + deltY * deltY1)/(dist1 * dist2));//路段方向向量与正北方向的夹角
			double lineAngle = 0;//路段与正北方向夹角	(0 - 2 * pi)					
			if (a < Math.PI/2)
			{
				//象限还是假定按顺时针方向旋转
				//第一象限
				if (deltX1 * deltY1 > 0)
				{
					lineAngle = a;
				}
				//第二象限
				else if (deltX1 * deltY1 < 0)
				{
					lineAngle = 2 * Math.PI - a;
				}
			}
			else if(a - Math.PI/2 < 0.000001)
			{
				if (deltY1 > 0)
				{
					lineAngle = Math.PI/2;
				}
				else if (deltY1 < 0)
				{
					lineAngle = 3 * Math.PI/2;
				}
			}
			else if(a > Math.PI/2)
			{
				//第三象限
				if (deltX1 * deltY1 > 0)
				{
					lineAngle = 2 * Math.PI - a;
				}
				//第四象限
				else if (deltX1 * deltY1 < 0)
				{
					lineAngle = a;
				}
			}	
			
			if (gpsAngle > lineAngle)
			{
				if (gpsAngle - lineAngle <= Math.PI/2)
				{
					angle = gpsAngle - lineAngle;
					position = 1;
				}
				else if (Math.PI/2 < gpsAngle - lineAngle && gpsAngle - lineAngle <= Math.PI)
				{
					angle = Math.PI - (gpsAngle - lineAngle);
					position = -1;
				}
				else if (Math.PI < gpsAngle - lineAngle && gpsAngle - lineAngle <= 3 * Math.PI/2)
				{
					angle =(gpsAngle - lineAngle) - Math.PI;
					position = -1;
				}
				else
				{
					angle =2 * Math.PI - (gpsAngle - lineAngle);
					position = 1;
				}
			}
			else if (gpsAngle < lineAngle)
			{
				if (lineAngle - gpsAngle <= Math.PI/2)
				{
					angle = lineAngle - gpsAngle;
					position = 1;
				}
				else if (Math.PI/2 < lineAngle - gpsAngle && lineAngle - gpsAngle <= Math.PI)
				{
					angle = Math.PI - (lineAngle - gpsAngle);
					position = -1;
				}
				else if (Math.PI < lineAngle - gpsAngle && lineAngle - gpsAngle <= 3 * Math.PI/2)
				{
					angle =(lineAngle - gpsAngle) - Math.PI;
					position = -1;
				}
				else
				{
					angle =2 * Math.PI - (lineAngle - gpsAngle);
					position = 1;
				}
			}
			else
			{
				angle = 0;
				position = 1;
			}
			AngleAndPosition[0] = angle;
			AngleAndPosition[1] = position;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());			
		}
	}

	/*基于综合得分和转换概率的最优路径匹配
	 *求转换概率矩阵: 上一GPS点转换为当前GPS点候选道路的概率情况*/	
	public void obtainPathBasedonCompreScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double[]threeLevelConnProbability){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 1; j < gpsTrackPointArrayList.size(); j++) {
					TaxiGPS previousTaxiGPS = gpsTrackPointArrayList.get(j - 1);
					TaxiGPS curTaxiGPS = gpsTrackPointArrayList.get(j);//当前GPS点
					//GPS点的候选道路集合
					ArrayList<MapMatchEdge> previousCandEdgeSetArrayList = previousTaxiGPS.candidateEdgeSetArrayList;
					ArrayList<MapMatchEdge> curGPSCandidateEdgeSetArrayList = curTaxiGPS.candidateEdgeSetArrayList;
					//如果都上一边与当前边存在候选道路，则按转换概率矩阵进行如下计算
					if (previousCandEdgeSetArrayList.size() > 0 && curGPSCandidateEdgeSetArrayList.size() > 0) {
						//求转换概率矩阵
						//以curTaxiGPS点候选道路集合EID为索引，存储上一GPS点转换为当前GPS点候选道路的概率情况
						//ArrayList中存储上一GPS点候选道路EID[0]，以及对应的转换概率值[1]
						Map<Integer, ArrayList<Double[]>> transitProbaMatrixMap = new HashMap<Integer, ArrayList<Double[]>>();
						for (int k = 0; k < curGPSCandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge nextGPSEdge = curGPSCandidateEdgeSetArrayList.get(k);	
							//存储当前GPS点的edgeID and probability
							ArrayList<Double[]> edgeIDAndProbArrayList = new ArrayList<Double[]>();
							for (int l = 0; l < previousCandEdgeSetArrayList.size(); l++) {
								MapMatchEdge edge = new MapMatchEdge();
								edge = previousCandEdgeSetArrayList.get(l);
								ArrayList<MapMatchEdge> firstLevelConnEdgeArray = edge.getFirstLevelConnEdgeArray();
								ArrayList<MapMatchEdge> secoLevelConnEdgeArray = edge.getSecoLevelConnEdgeArray();
								ArrayList<MapMatchEdge> thirdLevelConnEdgeArray = edge.getThirdLevelConnEdgeArray();	
								Double []edgeProbDoubles = new Double[2];															
								Double connProbability = 0.0;
								//零级连通
								if (nextGPSEdge.getEdgeID() == edge.getEdgeID()) {
									connProbability = threeLevelConnProbability[0];									
								}								
								//一级连通
								else if(isEdgeArraylistContainEdge(firstLevelConnEdgeArray, nextGPSEdge)){
										connProbability = threeLevelConnProbability[1];
								}
								//二级级连通
								else if (isEdgeArraylistContainEdge(secoLevelConnEdgeArray, nextGPSEdge)) {
									connProbability = threeLevelConnProbability[2];	
								}
								//三级连通
								else if (isEdgeArraylistContainEdge(thirdLevelConnEdgeArray, nextGPSEdge)) {
									connProbability = threeLevelConnProbability[3];
								}
								else {
									connProbability = 0.0;
								}
								//存储有连接情况，零表示不能转换到下一边
								edgeProbDoubles[0] = Double.valueOf(edge.getEdgeID());
								edgeProbDoubles[1] = connProbability;
								edgeIDAndProbArrayList.add(edgeProbDoubles);																
							}
							//存储转换概率矩阵
							if (edgeIDAndProbArrayList.size() > 0) {
								transitProbaMatrixMap.put(nextGPSEdge.getEdgeID(), edgeIDAndProbArrayList);
							}														
						}
						//为第一个GPS点候选边的累积得分赋值
						if (j == 1) {
							ArrayList<MapMatchEdge> candiEdgeArrayList = previousTaxiGPS.getCandidateEdgeSetArrayList();
							Map<Integer, Double> distDirectScoreSetMap = previousTaxiGPS.getDistDirectScoreSetMap();
							previousTaxiGPS.setEdgeAccuScoreMap(distDirectScoreSetMap);
							Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();							
							for (int k = 0; k < candiEdgeArrayList.size(); k++) {
								ArrayList<Integer[]> eidArraylist = new ArrayList<Integer[]>();
								MapMatchEdge edge = candiEdgeArrayList.get(k);
								int edgeID = edge.getEdgeID();								
								Double directDouble = previousTaxiGPS.getDirectScoreSetMap().get(edgeID)[2];
								int directInt = Integer.parseInt(new java.text.DecimalFormat("0").format(directDouble));
								Integer []temp = new Integer[2];
								temp[0] = edgeID;
								temp[1] = directInt;
								eidArraylist.add(temp);
								pathEIDMap.put(edge.getEdgeID(), eidArraylist);
							}
							previousTaxiGPS.setPathEIDMap(pathEIDMap);
						}
						
						//获得到转换到当前GPS点候选边的最佳路径以及score
						//计算转换到当前GPS点候选边的累积得分,然后取得到当前GPS点候选边的最大得分的路径作为到当前候选边的路径
						//并存储到当前候选边最大得分路径的edgeID构成路径
						Map<Integer, Double> curEdgeAccuScoreMap = new HashMap<Integer, Double>();//到当前GPS点候选边的累积得分
						Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();//到当前边的EID组成的路径
						for (int k = 0; k < curGPSCandidateEdgeSetArrayList.size(); k++) {							
							MapMatchEdge curGPSCandidateEdge = curGPSCandidateEdgeSetArrayList.get(k);//当前点候选边
							int curGPSCandidateEdgeEID = curGPSCandidateEdge.getEdgeID();//当前点候选边ID
							Double curDirectDouble = curTaxiGPS.getDirectScoreSetMap().get(curGPSCandidateEdgeEID)[2];
							int curDirectInt = Integer.parseInt(new java.text.DecimalFormat("0").format(curDirectDouble));//当前路段的方向性
							ArrayList<Integer[]> transPathEdgeIDArrayList = new ArrayList<Integer[]>();//转换到当前边路径EID构成的路径
							//根据上一边的累积得分以及到下一边的转换概率，计算每一条边的得分
							//1.取得上一候选边的累积得分，以及转换到当前边的转换概率；
							//2.计算转换到当前边的累积得分
							ArrayList<Double[]> tedgeIDAndProbArrayList = new ArrayList<Double[]>();//转换概率
							double maxTransScore = 0;
							if (transitProbaMatrixMap.containsKey(curGPSCandidateEdgeEID)) {
								tedgeIDAndProbArrayList = transitProbaMatrixMap.get(curGPSCandidateEdgeEID);//取得转换概率
								ArrayList<Integer[]> maxScorePathEIDArrayList = new ArrayList<Integer[]>();//转换到当前边获得最大得分，到上一边的路径ID
								Integer maxSoreEdgeInfo[] = new Integer[2];//转换到当前边综合得分值最大所对应的上一边的eid及方向
								int maxSoreEdgeID = 0;//只存储转换到当前边综合得分值最大所对应的上一边的eid
								//遍历每一条边，确定转换到下一边得分最高的路径scores以及eid
								for (int l = 0; l < tedgeIDAndProbArrayList.size(); l++) {
									double edgeID = tedgeIDAndProbArrayList.get(l)[0];//上一边的ID
									double probably = tedgeIDAndProbArrayList.get(l)[1];//上一边转换到当前边的概率									
									//上一边累积得分，默认为上一边的距离与方向综合得分		
									int convertEdgeID = Integer.parseInt(new java.text.DecimalFormat("0").format(edgeID));									
									double preAccuEdgeScore = previousTaxiGPS.getDistDirectScoreSetMap().get(convertEdgeID);
									//如果累积得分为空值，则取得距离方向综合得分
									//tempPathEIDArrayList为上一边本身存储的EID
									if (!previousTaxiGPS.getEdgeAccuScoreMap().containsKey(convertEdgeID)) {
										preAccuEdgeScore = previousTaxiGPS.getDistDirectScoreSetMap().get(convertEdgeID);
									}	
									//否则，取得到上一GPS点候选边的累积得分以及累积路径ID
									else {
										preAccuEdgeScore = previousTaxiGPS.getEdgeAccuScoreMap().get(convertEdgeID);									
									}
									Map<Integer, Double> curDistDirectScoreSetMap = curTaxiGPS.getDistDirectScoreSetMap();
									double curDistDirectScore = curDistDirectScoreSetMap.get(curGPSCandidateEdgeEID);//当前边得分
									double transferTonCurScore = preAccuEdgeScore + curDistDirectScore * probably;//上一边转换到当前边综合得分									
									if (maxTransScore < transferTonCurScore) {
										maxTransScore = transferTonCurScore;
										maxSoreEdgeInfo[0] = convertEdgeID;
										maxSoreEdgeID = convertEdgeID;
										Double directDouble = previousTaxiGPS.getDirectScoreSetMap().get(convertEdgeID)[2];
										int directInt = Integer.parseInt(new java.text.DecimalFormat("0").format(directDouble));
										maxSoreEdgeInfo[1] = directInt;	
										if (!curEdgeAccuScoreMap.containsKey(curGPSCandidateEdgeEID)) {
											curEdgeAccuScoreMap.put(curGPSCandidateEdgeEID, transferTonCurScore);
										}
										else {
											curEdgeAccuScoreMap.remove(curGPSCandidateEdgeEID);
											curEdgeAccuScoreMap.put(curGPSCandidateEdgeEID, transferTonCurScore);
										}
									}			
								}
								
								if (!previousTaxiGPS.getEdgeAccuScoreMap().containsKey(maxSoreEdgeID)) {
									maxScorePathEIDArrayList = new ArrayList<Integer[]>();
									maxScorePathEIDArrayList.add(maxSoreEdgeInfo);
								}	
								//否则，取得到上一GPS点候选边的累积得分以及累积路径ID
								else {									
									maxScorePathEIDArrayList = previousTaxiGPS.getPathEIDMap().get(maxSoreEdgeID);										
								}	
								//获得转换到当前边路径EID构成的路径
								for (int m = 0; m < maxScorePathEIDArrayList.size(); m++) {
									transPathEdgeIDArrayList.add(maxScorePathEIDArrayList.get(m));
									if (m == maxScorePathEIDArrayList.size() -1) {
										Integer []temp = new Integer[2];
										temp[0] = curGPSCandidateEdgeEID;
										temp[1] = curDirectInt;										
										transPathEdgeIDArrayList.add(temp);
									}
								}
								pathEIDMap.put(curGPSCandidateEdgeEID, transPathEdgeIDArrayList);
							}
						}//至此获得到此GPS点的匹配路径
						curTaxiGPS.setEdgeAccuScoreMap(curEdgeAccuScoreMap);
						curTaxiGPS.setPathEIDMap(pathEIDMap);
					}//求转换概率矩阵,以及根据转换概率矩阵进行地图匹配
					//否则，if当前GPS点的候选路径为空时，匹配路径为其本身
					else {
						if (j == 1) {
							if (previousCandEdgeSetArrayList.size() != 0) {
								ArrayList<MapMatchEdge> candiEdgeArrayList = previousTaxiGPS.getCandidateEdgeSetArrayList();
								Map<Integer, Double> distDirectScoreSetMap = previousTaxiGPS.getDistDirectScoreSetMap();
								previousTaxiGPS.setEdgeAccuScoreMap(distDirectScoreSetMap);
								Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();	
								Map<Integer, Double> prevEdgeAccuScoreMap = new HashMap<Integer, Double>();//前一GPS点候选边的累积得分
								Map<Integer, Double> prevDistDirectScoreSetMap = previousTaxiGPS.getDistDirectScoreSetMap();
								for (int k = 0; k < candiEdgeArrayList.size(); k++) {
									ArrayList<Integer[]> eidArraylist = new ArrayList<Integer[]>();
									MapMatchEdge edge = candiEdgeArrayList.get(k);
									int edgeID = edge.getEdgeID();								
									Double directDouble = previousTaxiGPS.getDirectScoreSetMap().get(edgeID)[2];
									int directInt = Integer.parseInt(new java.text.DecimalFormat("0").format(directDouble));
									Integer []temp = new Integer[2];
									temp[0] = edgeID;
									temp[1] = directInt;
									eidArraylist.add(temp);
									pathEIDMap.put(edge.getEdgeID(), eidArraylist);
									int prevGPSCandidateEdgeEID = edge.getEdgeID();//当前点候选边ID
									double curDistDirectScore = prevDistDirectScoreSetMap.get(prevGPSCandidateEdgeEID);//当前边得分
									prevEdgeAccuScoreMap.put(prevGPSCandidateEdgeEID, curDistDirectScore);
								}
								previousTaxiGPS.setPathEIDMap(pathEIDMap);
								previousTaxiGPS.setEdgeAccuScoreMap(prevEdgeAccuScoreMap);
							}
						}
						else {
							if (curGPSCandidateEdgeSetArrayList.size() != 0) {
								ArrayList<MapMatchEdge> candiEdgeArrayList = curTaxiGPS.getCandidateEdgeSetArrayList();
								Map<Integer, Double> distDirectScoreSetMap = curTaxiGPS.getDistDirectScoreSetMap();
								curTaxiGPS.setEdgeAccuScoreMap(distDirectScoreSetMap);
								Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();	
								Map<Integer, Double> curEdgeAccuScoreMap = new HashMap<Integer, Double>();//到当前GPS点候选边的累积得分	
								Map<Integer, Double> curDistDirectScoreSetMap = curTaxiGPS.getDistDirectScoreSetMap();
								for (int k = 0; k < candiEdgeArrayList.size(); k++) {
									ArrayList<Integer[]> eidArraylist = new ArrayList<Integer[]>();
									MapMatchEdge edge = candiEdgeArrayList.get(k);
									int edgeID = edge.getEdgeID();								
									Double directDouble = curTaxiGPS.getDirectScoreSetMap().get(edgeID)[2];
									int directInt = Integer.parseInt(new java.text.DecimalFormat("0").format(directDouble));
									Integer []temp = new Integer[2];
									temp[0] = edgeID;
									temp[1] = directInt;
									eidArraylist.add(temp);
									pathEIDMap.put(edge.getEdgeID(), eidArraylist);									
									int curGPSCandidateEdgeEID = edge.getEdgeID();//当前点候选边ID
									double curDistDirectScore = curDistDirectScoreSetMap.get(curGPSCandidateEdgeEID);//当前边得分
									curEdgeAccuScoreMap.put(curGPSCandidateEdgeEID, curDistDirectScore);
								}
								curTaxiGPS.setPathEIDMap(pathEIDMap);
								curTaxiGPS.setEdgeAccuScoreMap(curEdgeAccuScoreMap);
							}						
						}
						continue;
					}			
				}//每一条GPS轨迹进行地图匹配
			}
		} catch (Exception e) {
			// TODO: handle exception	
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 输出路径：输出GPS点以及与之匹配的edge
	 * 根据全局最优（累积得分最高），而不是局部最优（局部得分最高的边EID）获得GPS点匹配道路
	 * 倒叙遍历GPS点，取出累积得分最高的边所对应的路径ID集合set1
	 * 遍历上一个GPS点,取得对应的路径ID集合set2
	 * 遍历每一个路径集合set2，如果某一路径ID集合set2包含于集合set1，说明路径set2是set1的子路径，继续遍历上一个GPS点
	 * 如果某一GPS点所有路径集合set2不包含于set1中，说明该GPS点产生新的子路径；
	 * 取得当前GPS点累积得分最高的边所对应的路径ID集合，重复以上过程，直到第一个GPS点
	 * @param carrayPassTrackMap
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param returnGPSAndPath
	 * @param allPathEIDMap 所有路径ID组成的路径
	 */
	public void exportGPSMatchPath(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, ReturnGPSAndPath returnGPSAndPath, 
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap) {
		ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//返回GPS点	
		ArrayList<ArrayList<ReturnMatchNode>> allPathArrayliList = new ArrayList<ArrayList<ReturnMatchNode>>() ;//存储所有路径				
		ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//返回地图匹配每一条路径
		ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//去掉相同的EID
		obtainMatchRoadAccordOptimal(carrayPassTrackMap, uniquePathEIDArrayliList);//根据全局最优获得GPS点匹配道路
		ArrayList<Integer[]> contianShortestPathEIDArrayList = new ArrayList<Integer[]>();//包含最短路径的EID,匹配的所有路径的EID				
		postProcessing(uniquePathEIDArrayliList,contianShortestPathEIDArrayList, allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);				
		allPathEIDMap.put(1, contianShortestPathEIDArrayList);
		allPathArrayliList.add(returnMapMatchOnePArrayList);			
		returnGPSAndPath.setReturnGPSArrayList(returnGPSArrayList);
		returnGPSAndPath.setReturnMapMatchEdgeArrayList(allPathArrayliList);
		System.out.print("结束对GPS轨迹点匹配路径:" + '\n');
	}
	
	/**
	 * 根据全局最优（累积得分最高），而不是局部最优（局部得分最高的边EID）获得GPS点匹配道路
	 * @param carrayPassTrackMap
	 * @param pathEIDArrayliList	存储每条GPS轨迹匹配的路径ID以及与GPS的方向性
	 */
	public void obtainMatchRoadAccordOptimal(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, ArrayList<Integer[]> pathEIDArrayliList) {
		try {
			System.out.print("开始对GPS轨迹点匹配路径:" + '\n');
			ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//返回GPS点					
			ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
			gpsTrackPointArrayList = carrayPassTrackMap.get(1);//GPS路径轨迹ID从1开始编号
			for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
				System.out.print("GPS点:" + taxiGPS.getTargetID() + "匹配路径:" + '\n');
				if (taxiGPS.getTargetID().equals("MMC8000GPSANDASYN051113-24001-00000000")) {
					System.out.print(" ");
				}
				ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//存储路径ID以及与GPS的方向性，临时变量
				ReturnMatchNode returnGPS = new ReturnMatchNode();
				returnGPS.longitude = taxiGPS.longitude;
				returnGPS.latitude = taxiGPS.latitude;
				returnGPSArrayList.add(returnGPS);
				Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//边的累积得分
				//最后一个GPS点，取得得分最高的边的EID
				if (j == gpsTrackPointArrayList.size() - 1) {				
					if (edgeAccuScoreMap.size() > 0) {
						int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);
						Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
						int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS点与路段方向性的关系，1表示同向，-1表示反向
						Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
						if (tempPathEIDMap.size() > 0) {
							ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
							for (int k = 0; k < tempEIDArrayliList.size(); k++) { 
								pathEIDArrayliList.add(tempEIDArrayliList.get(k));
							}
						}
					}
				}
				else {
					boolean isContains = false;
					Map<Integer, ArrayList<Integer[]>> pathEIDMap = taxiGPS.getPathEIDMap();
					ArrayList<MapMatchEdge> candidateEdgeSetArrayList = taxiGPS.candidateEdgeSetArrayList;					
					//取得每个GPS点的路径EID组成的路径
					if (pathEIDMap.size() > 0) {
						for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
							int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
							tempPathEIDArrayliList = pathEIDMap.get(edgeID);
							//如果存在包含关系,前者包含后者
							if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {						
								isContains = true;
								break;
							}
						}					
					}
					//如果不包含，产生新的路径EID
					if (!isContains) {
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//取得得分最大的路径
							Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
							if (tempPathEIDMap.size() > 0) {
								ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
								for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
									pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//倒叙插入新的EID
								}
							}
						}							
					}	
				}
			}//存储一条GPS轨迹匹配的路径ID
			
			//去掉相同的EID
			//由于两个GPS点可能位于同一路段，可能选择同一候选路段，造成路段ID中会存在相同的EID,并且相同的EID是相邻的
			//没取得一个EID与uniquePathEIDArrayliList中的最后一个EID比较，如果不同，则加入uniquePathEIDArrayliList
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//中间变量
			for (int k = 0; k < pathEIDArrayliList.size(); k++) {
				if (k == 0) {
					uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
				}
				else {
					int curInteger = pathEIDArrayliList.get(k)[0];
					int uniqueLastEID = uniquePathEIDArrayliList.get(uniquePathEIDArrayliList.size() - 1)[0];
					if (curInteger != uniqueLastEID) {
						uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/* 后处理：
	 * 根据当前路径EID生成路径并根据最短路径算法补足缺失的衔接路段
	 * 最短路径:
	 * 取得前一路段的终端节点preEndPoint，与下一路段的起点端点nextBeginPoint进行比较，若为同一点则在路径集合中加入该路径
	 * 否则，在根据前一路段的连通集合中查找后一路段，并生成两点之间的最短路径，作为插入路段
	 * PathEIDArrayliList：路径ID,并将新产生的最短路径ID加入该集合中
	 * polylineCollArrayList:存储的路网信息
	 * returnMapMatchOnePArrayList:返回地图匹配的一条路径*/
	public void postProcessing(ArrayList<Integer[]> PathEIDArrayliList,ArrayList<Integer[]>containShortestPathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList){
		try {
			//根据路径EID取得相应的路径
			for (int j = 0; j < PathEIDArrayliList.size() - 1; j++) {
				Integer []edgeIDInt = PathEIDArrayliList.get(j);//取得当前路段ID以与GPS的方向性
				containShortestPathEIDArrayList.add(edgeIDInt);
				MapMatchEdge edge = new MapMatchEdge();//当前边
				int edgeID = edgeIDInt[0];
				if (edgeID == 1514) {
					System.out.print("");
				}
				int direPro = edgeIDInt[1];
				Integer []nextEdgeIDInt = PathEIDArrayliList.get(j + 1);//下一路段ID以与GPS的方向性
				MapMatchEdge nextEdge = new MapMatchEdge();//下一边
				int nextEdgeID = nextEdgeIDInt[0];
				int nextDirePro = nextEdgeIDInt[1];				
				ArrayList<MapMatchNode> pointCollArrayList = new ArrayList<MapMatchNode>();//当前路段上的点
				ArrayList<MapMatchNode> nextPointCollArrayList = new ArrayList<MapMatchNode>();//next路段上的点
				MapMatchNode endNode = new MapMatchNode();//当前路段终点
				MapMatchNode nextBeginNode = new MapMatchNode();//next路段起点
				boolean isEdgeObtain = false;//当前边是否获得
				boolean isNextEdgeObtain = false;//下一边是否获得	
						
				for (int k = 0; k < polylineCollArrayList.size(); k++) {
					MapMatchEdge polyline = polylineCollArrayList.get(k);
					if (edgeID == polyline.getEdgeID()) {
						edge = polyline;
						pointCollArrayList = polyline.getPointCollArrayList();
						isEdgeObtain = true;
					}
					else if (nextEdgeID == polyline.getEdgeID()) {
						nextEdge = polyline;
						nextPointCollArrayList = polyline.getPointCollArrayList();
						isNextEdgeObtain = true;
					}
					//两条边都获得
					if (isEdgeObtain && isNextEdgeObtain) {
						break;
					}
				}
				if (isEdgeObtain && isNextEdgeObtain) {
					//如果为同向
					if (direPro == 1) {
						int pointCount = pointCollArrayList.size();
						endNode = pointCollArrayList.get(pointCount - 1);
						for (int k = 0; k < pointCount; k++) {
							ReturnMatchNode tNode = new ReturnMatchNode();
							tNode.longitude = pointCollArrayList.get(k).x;
							tNode.latitude = pointCollArrayList.get(k).y;
							returnMapMatchOnePArrayList.add(tNode);
						}
					}
					//反向
					else if(direPro == -1){
						int pointCount = pointCollArrayList.size();
						endNode = pointCollArrayList.get(0);
						for (int k = pointCount - 1; k >= 0; k--) {
							ReturnMatchNode tNode = new ReturnMatchNode();
							tNode.longitude = pointCollArrayList.get(k).x;
							tNode.latitude = pointCollArrayList.get(k).y;
							returnMapMatchOnePArrayList.add(tNode);
						}
					}
					if (nextDirePro == 1) {
						int pointCount = nextPointCollArrayList.size();
						nextBeginNode = nextPointCollArrayList.get(0);
					}
					else if (nextDirePro == -1) {
						int pointCount = nextPointCollArrayList.size();
						if (pointCount==0) {
							System.out.print("");
						}
						nextBeginNode = nextPointCollArrayList.get(pointCount - 1);
					}
					//当前边匹配终点与后边匹配起点为同一点
					if (isTheSameNode(endNode, nextBeginNode)) {
						//如果为倒数第二条边
						if (j == PathEIDArrayliList.size() - 2) {
							containShortestPathEIDArrayList.add(PathEIDArrayliList.get(j + 1));
							if (nextDirePro == 1) {
								int pointCount = nextPointCollArrayList.size();
								for (int k = 0; k < pointCount; k++) {
									ReturnMatchNode tNode = new ReturnMatchNode();
									tNode.longitude = nextPointCollArrayList.get(k).x;
									tNode.latitude = nextPointCollArrayList.get(k).y;
									returnMapMatchOnePArrayList.add(tNode);
								}
							}
							else {
								int pointCount = nextPointCollArrayList.size();
								for (int k = pointCount - 1; k >= 0; k--) {
									ReturnMatchNode tNode = new ReturnMatchNode();
									tNode.longitude = nextPointCollArrayList.get(k).x;
									tNode.latitude = nextPointCollArrayList.get(k).y;
									returnMapMatchOnePArrayList.add(tNode);
								}
							}
						}
						//继续
						continue;
					}
					//否则：根据最短路径算法，路径间插入求得的最短路径
					//如果在倒数第一与第二条路径间插入最短路径，还有把最后一个路段加入
					else {
						//最短路径算法：获得两边之间匹配的最短路径，前边匹配终点到后边匹配起点之间最短路径
						//searRoadStartNode:寻路起点，searRoadEndNode寻路终点
						//以searRoadStartNode为中心在distance小范围内建立拓扑，distance为起终点之间的2倍距离
						ArrayList<MapMatchNode> tempTopolygonArrayList = new ArrayList<MapMatchNode>();
						double distance = 2 * distance(endNode, nextBeginNode);
						for (int i = 0; i < juncCollArrayList.size(); i++) {
							MapMatchNode tNode = juncCollArrayList.get(i);
							if (isNodeInCircle(endNode, tNode, distance)) {
								tempTopolygonArrayList.add(tNode);
							}
						}
						
						//检索寻路起点、终点
						MapMatchNode searRoadStartNode = new MapMatchNode();
						MapMatchNode searRoadEndNode = new MapMatchNode();
						boolean isStartNodeFind = false;
						boolean isEndNodeFind = false;
						for (int i = 0; i < tempTopolygonArrayList.size(); i++) {
							MapMatchNode tNode = tempTopolygonArrayList.get(i);
							if (isTheSameNode(endNode, tNode)) {
//								searRoadStartNode = tNode;
//								searRoadStartNode.setParentNode(null);
								searRoadStartNode.x = tNode.x;
								searRoadStartNode.y = tNode.y;
								searRoadStartNode.nodeID = tNode.nodeID;
								searRoadStartNode.setParentNode(null);
								searRoadStartNode.relationEdges = tNode.relationEdges;
								searRoadStartNode.relationNodes = tNode.relationNodes;
								isStartNodeFind = true;
							}
							else if(isTheSameNode(nextBeginNode, tNode)) {
								searRoadEndNode = tNode;
								isEndNodeFind = true;
							}
							if (isStartNodeFind && isEndNodeFind) {
								break;
							}
						}	
						ArrayList<ReturnMatchNode> tempReturnMapMatchPathArrayList = new ArrayList<ReturnMatchNode>();
						//取得最短路径的EID
						ArrayList<Integer[]> tempEidArrayList = new ArrayList<Integer[]>();
						if (searRoadStartNode.nodeID == 4538) {
							System.out.print("");
						}
						stack = new Stack<MapMatchNode>();
						openList = new ArrayList<MapMatchNode>();
						obtainShortestPath(null,searRoadStartNode, searRoadStartNode, searRoadEndNode, 
								juncCollArrayList,tempReturnMapMatchPathArrayList, tempEidArrayList);
						for (int i = 0; i < tempEidArrayList.size(); i++) {
							containShortestPathEIDArrayList.add(tempEidArrayList.get(i));
						}
						for (int i = 0; i < tempReturnMapMatchPathArrayList.size(); i++) {
							returnMapMatchOnePArrayList.add(tempReturnMapMatchPathArrayList.get(i));
						}
					}					
					//如果为倒数第二条边******************************************************
					if (j == PathEIDArrayliList.size() - 2) {
						if (nextDirePro == 1) {
							int pointCount = nextPointCollArrayList.size();
							for (int k = 0; k < pointCount; k++) {
								ReturnMatchNode tNode = new ReturnMatchNode();
								tNode.longitude = nextPointCollArrayList.get(k).x;
								tNode.latitude = nextPointCollArrayList.get(k).y;
								returnMapMatchOnePArrayList.add(tNode);
							}
						}
						else {
							int pointCount = nextPointCollArrayList.size();
							for (int k = pointCount - 1; k >= 0; k--) {
								ReturnMatchNode tNode = new ReturnMatchNode();
								tNode.longitude = nextPointCollArrayList.get(k).x;
								tNode.latitude = nextPointCollArrayList.get(k).y;
								returnMapMatchOnePArrayList.add(tNode);
							}
						}
					}					
				}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/*两条边之间最短路径：前一边匹配终点到后一边匹配起点之间的最短路径
	 * startEdge:起点边
	 * endEdge:终点边
	 * pNode:上一节点
	 * cNode:当前节点
	 * sNode:起点
	 * eNode：终点
	 * returnMapMatchPathArrayList：获得两边之间的匹配最短路径
	 * edgeIDArraylist：最短路径ID*/
	private Stack<MapMatchNode> stack = new Stack<MapMatchNode>();
	private ArrayList<MapMatchNode> openList = new ArrayList<MapMatchNode>();//开启列表
	
	/**
	 * 2014/12/27修改后算法
	 */
	public boolean obtainShortestPath(MapMatchNode pNode, MapMatchNode cNode, MapMatchNode sNode, MapMatchNode eNode,
			ArrayList<MapMatchNode>juncCollArrayList, ArrayList<ReturnMatchNode> returnMapMatchPathArrayList,ArrayList<Integer[]> edgeIDArrayList){
		MapMatchNode nNode = null;	
			try {
				/* 如果符合条件判断说明出现环路，不能再顺着该路径继续寻路，返回false */
				if (cNode != null && pNode != null && cNode.nodeID == pNode.nodeID)
					return false;
				//当前节点不为空
				if (cNode != null) {
					int i = 0;
					/* 如果该起始节点就是终点，说明找到一条路径 */
					if (cNode.nodeID == eNode.nodeID)				
					{
						stack.push(cNode);
						//从终点往返回到起点,返回节点ID组成的路径
						ArrayList<MapMatchNode> tempMapMatchPathArrayList = new ArrayList<MapMatchNode>();
						getPath(tempMapMatchPathArrayList, cNode);	
						//2014-11-14修改
						//找到路径后，把节点的父节点都清空
						for (int j = 0; j < tempMapMatchPathArrayList.size(); j++) {
							MapMatchNode tNode = tempMapMatchPathArrayList.get(j);
							tNode.setParentNode(null);
						}						
						ArrayList<MapMatchNode>nodeArrayList =new ArrayList<MapMatchNode>();//顺序存储当前路径节点
						MapMatchNode[]nodesPath=new MapMatchNode[tempMapMatchPathArrayList.size()];
						for (int l = 0; l < tempMapMatchPathArrayList.size(); l++) {
							nodesPath[l] = tempMapMatchPathArrayList.get(l);
						}						
						/*如果是可行路径则存储并输出
						 * 由节点ID检索relationEdgesMap对应边，检索对应边polyline
						 * 计算路径
						 * 累计路径长度，若tempAccuLength>=circle,则取另存当前polyline并跳出，不必在继续检索
						 * 若tempAccuLength<circle:该路径不符合条件*/																									
						for (int j = 0; j < nodesPath.length-1; j++) {
							int EID=nodesPath[j].nodeID;//节点ID
							nodeArrayList.add(nodesPath[j]);
							//搜寻路径节点相邻边Edge
							ArrayList<MapMatchEdge> relaEdgeArraylist=new ArrayList<MapMatchEdge>();//存储节点邻接边
							for (int p = 0; p < juncCollArrayList.size(); p++) {
								if (EID == juncCollArrayList.get(p).nodeID) {
									relaEdgeArraylist = juncCollArrayList.get(p).relationEdges;
									break;
								}
							}
							int relaEdgeCount=relaEdgeArraylist.size();//节点相邻边数
							
							//检索相邻边				
							for (int k = 0; k < relaEdgeCount; k++) {
								MapMatchEdge edge = new MapMatchEdge();
								edge = relaEdgeArraylist.get(k);									
								MapMatchNode froNode = new MapMatchNode();
								MapMatchNode toNode = new MapMatchNode();
								ArrayList<MapMatchNode> edgePointCollArrayList=new ArrayList<MapMatchNode>();
								edgePointCollArrayList = edge.getPointCollArrayList();
								int pointCount = edgePointCollArrayList.size();
								froNode.setX(edgePointCollArrayList.get(0).getX());
								froNode.setY(edgePointCollArrayList.get(0).getY());
								toNode.setX(edgePointCollArrayList.get(pointCount-1).getX());
								toNode.setY(edgePointCollArrayList.get(pointCount-1).getY());					
								
								//取得邻接polyline的vertex
								//获得与当前点直接相连的边				
								if (isTheSameNode(nodesPath[j+1], froNode)||isTheSameNode(nodesPath[j+1], toNode)) {						

									//首尾点与节点重合，去掉首尾点，只取中间点
									//若当前节点与plinePoints的首点相同，则按此顺序增加point
									//否则倒序增加point
									MapMatchNode tempSNode = new MapMatchNode();//首点
									MapMatchNode tempENode = new MapMatchNode();//尾点
									tempSNode = edgePointCollArrayList.get(0);
									tempENode = edgePointCollArrayList.get(pointCount-1);
									
									if (isTheSameNode(nodesPath[j], tempSNode)) {	
										Integer[] temp = new Integer[2];
										temp[0] = edge.getEdgeID();
										temp[1] = 1;
										edgeIDArrayList.add(temp);
										for (int l = 1; l < pointCount-1; l++) {
											MapMatchNode tNode = new MapMatchNode();
											tNode=edgePointCollArrayList.get(l);
											nodeArrayList.add(tNode);	
										}								
									}							
									//与尾点相同
									else if (isTheSameNode(nodesPath[j], tempENode)) {
										Integer[] temp = new Integer[2];
										temp[0] = edge.getEdgeID();
										temp[1] = -1;
										edgeIDArrayList.add(temp);
										for (int l = pointCount-2; l>0 ; l--) {
											MapMatchNode tNode = new MapMatchNode();
											tNode = edgePointCollArrayList.get(l);
											nodeArrayList.add(tNode);
										}																
									}							
									break;//检索到邻接polyline，跳出
								}				
							}				
						}	
						//返回结果
						for (int k = 0; k < nodeArrayList.size(); k++) {
							ReturnMatchNode returnNode = new ReturnMatchNode();
							returnNode.longitude = nodeArrayList.get(k).x;
							returnNode.latitude = nodeArrayList.get(k).y;
							returnMapMatchPathArrayList.add(returnNode);
							
						}
						return true;//寻路结束
					}				
					/* 如果不是,继续寻路 */
					else
					{		
						//索引超限
						if (cNode.getRelationNodes().size() <= i) {
							return false;
						}
						/* 从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点
						 * 作为下一次递归寻路时的起始节点 
						 */
						nNode = cNode.getRelationNodes().get(i);					
						while(nNode != null){
							//如果此处为目标点，应当输出路径
							if (isTheSameDirection(sNode, eNode,180, nNode)
									&& isInSpanDistance(sNode, eNode, 1500, nNode) && isDirectProjSatis(sNode, eNode, nNode, 100)) {
								//保证不会产生环路
								if ( nNode.nodeID != sNode.nodeID ) {	
									//查找开启列表中是否存在该点,若存在返回该点索引，否则返回-1
									int indexInOpenlist = isListContains(openList, nNode);
									int indexInStack = -1;//在关闭列表中的索引
									if (indexInOpenlist == -1) {
										indexInStack = isStackContains(stack,nNode);
									}								
									double cost = distance(nNode, cNode);//到下一节点的代价
									//如果存在，G值是否更小，即是否更新G，F值
									if (indexInOpenlist != -1){
										if((cNode.getG()+cost) < openList.get(indexInOpenlist).getG()){
								            nNode.setParentNode(cNode);
								            countG(nNode, eNode, cost);
								            countF(nNode);
								            openList.set(indexInOpenlist, nNode);  
								        }
										i++;
							            if(i >= cNode.getRelationNodes().size()){
							            	nNode = null;
											break;
										}
							            else {
							            	nNode = cNode.getRelationNodes().get(i);
										}
									}
									//如果在关闭列表stack中
									else if(indexInStack != -1){
										if((cNode.getG()+cost) < stack.get(indexInStack).getG()){
								            nNode.setParentNode(cNode);
								            countG(nNode, eNode, cost);
								            countF(nNode);  
								            openList.add(nNode);
								        }
										i++;
							            if(i >= cNode.getRelationNodes().size()){
							            	nNode = null;
											break;
										}
							            else {
							            	nNode = cNode.getRelationNodes().get(i);
										}									
									}
									else {
										//添加到开启列表中								
										nNode.setParentNode(cNode);//父节点								
								        count(nNode, eNode, cost);
								        openList.add(nNode);
								        i++;
							            if(i >= cNode.getRelationNodes().size()){
											nNode = null;
											break;
										}
							            else {
							            	nNode = cNode.getRelationNodes().get(i);
										}
									}								
								}							
								else {
									i++;
									if(i >= cNode.getRelationNodes().size()){
										nNode = null;
										break;
									}
									else {
										nNode = cNode.getRelationNodes().get(i);
									}				
								}					
							}
							//否则，继续寻找相邻点
							else {
								i++;
								if(i >= cNode.getRelationNodes().size()){
									nNode = null;
									break;
								}
					            else {
					            	nNode = cNode.getRelationNodes().get(i);
								}
							}					
						}
						//从开启列表中删除F最小的node
				        //添加到stack中
						if (openList.size() >=1 ) {
							//开启列表中排序，把F值最小的放到最顶端
							Collections.sort(openList, new MapMatchComparator());
							nNode = openList.get(0);
							stack.push(openList.remove(0));
						}											
						if(obtainShortestPath(cNode,nNode, sNode,eNode,juncCollArrayList,returnMapMatchPathArrayList,edgeIDArrayList)){
							return true;
						}
						else{
							return false;
						}								
					}
				}
				else
					return false;
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();
				return false;			
			}
	}
	
	
	
	
	/******************************************************************************
	 * ****************************************************************************
	 * 小辅助函数
	 * 
	 * ****************************************************************************
	 * ****************************************************************************/
	/*搜寻路线的当前点是否与起终点在同一方向
	  * 搜寻在搜寻范围弧度内的路线，
	  * 判断：起终点向量与起点当前点向量间的夹角与搜寻范围弧度的1/2比较
	  * 若两向量夹角angle<sxhd搜寻范围弧度，说明该点符合条件
	  * starNode:起点
	  * endNode：终点
	  * cnode:搜寻当前点
	  * a:搜寻范围度数，要转化为弧度*/
	public boolean isTheSameDirection(MapMatchNode starNode,MapMatchNode endNode,int a,MapMatchNode cNode)
	{		
		double pi = Math.PI;
		double sxhd = a*pi/180;//搜寻范围弧度
		try {
			if (cNode.nodeID == endNode.nodeID) {
				return true;
			}
			else {
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(starNode, PubParameter.wuhanL0, xy);
				double starNodeX = xy[0];
				double starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				double endNodeX = xy[0];
				double endNodeY = xy[1];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);				
				double cNodeX = xy[0];
				double cNodeY = xy[1];
				
				//起终点向量 ，模      起点当前点向量，模
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;
				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;
				//两向量夹角
				//如果当前点与终点相同，angle会返回null
				double angle = Math.acos((seDeltX*scDeltX+seDeltY*scDeltY)/(seNodeDis*scNodeDis));
				if (angle < sxhd/2) {
					return true;
				}
				else {
					return false;
				}
			}			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/*搜寻点是否在搜寻起终点直线跨度范围内
	 * 计算：varSpan:起点当前点向量模乘以与起终点方向向量夹角的正弦值
	 * 判断：若搜寻点距搜寻方向直线的距离varSpan<=span，则返回true，否则返回false
	 * starNode:起点
	 * endNode：终点
	 * cnode:搜寻当前点
	 * span：搜寻跨度*/
	public boolean isInSpanDistance(MapMatchNode starNode,MapMatchNode endNode,double constSpan,MapMatchNode cNode)
	{
		double angle = 0;
		double varSpan = 0;
		try {
			if (endNode.nodeID == cNode.nodeID) {
				angle = 0;
				varSpan = 0;
			}
			else {
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(starNode, PubParameter.wuhanL0, xy);
				double starNodeX = xy[0];
				double starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				double endNodeX = xy[0];
				double endNodeY = xy[1];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);				
				double cNodeX = xy[0];
				double cNodeY = xy[1];
				
				//起终点向量 ，模      起点当前点向量，模
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;
				
				//两向量夹角
				angle=Math.acos((seDeltX*scDeltX+seDeltY*scDeltY)/(seNodeDis*scNodeDis));
				varSpan=scNodeDis*Math.sin(angle);
			}			
			//跨度						
			if (varSpan <= constSpan) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	 /*开放列表是否包含该点,(-1：没有找到，否则返回所在的索引)*/
	 public int isListContains(ArrayList<MapMatchNode> openList, MapMatchNode node){
		 boolean isOk = false;
		 for(int i = 0; i < openList.size(); i++){
			 MapMatchNode tNode = openList.get(i);
	        if(tNode.nodeID == node.nodeID){
	        	return i;
	        }
       }
		return -1;
	}
	
	 public int isStackContains(Stack<MapMatchNode> tempStack, MapMatchNode node){
		 Iterator<MapMatchNode> iter = tempStack.iterator();
		 int index = -1;
		 boolean isInStack = false;
		 while (iter.hasNext()) {
			 index++;
			 MapMatchNode tNode = (MapMatchNode) iter.next();
			 if (node.nodeID == tNode.nodeID) {
				isInStack = true;
				return index;
			}
		 }
		 if (isInStack) {
			 return index;
		 }
		 else {			 
			return -1;
		 }		 
	  }
	 
	 /**
	  * 方向投影长度是否满足条件，投影长度必须在起点与终点之间
	  * 当前节点与警情点在警情点、终点直线上的投影距离小于警情点、终点之间的距离+缓冲距离
	  * @param jjNode	接警点
	  * @param endNode	寻路终点
	  * @param node	当前节点
	  * @param projBuffer	缓冲距离
	  * @return
	  */
		public boolean isDirectProjSatis(MapMatchNode jjNode,MapMatchNode endNode,MapMatchNode node, double projBuffer)
		{
			double angle = 0;
			double projDis = 0;
			double jeNodeDis = 0;
			try {
				if (endNode.nodeID == node.nodeID) {
					angle = 0;
					projDis = distance(jjNode, endNode);
					jeNodeDis = distance(jjNode, endNode);
				}
				else {					
					double []xy = new double[2];
					coordinateTransToPlaneCoordinate(jjNode, PubParameter.wuhanL0, xy);
					double jjNodeX = xy[0];
					double jjNodeY = xy[1];
					coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
					double endNodeX = xy[0];
					double endNodeY = xy[1];
					coordinateTransToPlaneCoordinate(node, PubParameter.wuhanL0, xy);
					double nodeX = xy[0];
					double nodeY = xy[1];					
					//起终点向量 ，模      起点当前点向量，模
					jeNodeDis=distance(jjNode, endNode);
					double jeDeltX = endNodeX-jjNodeX;
					double jeDeltY = endNodeY-jjNodeY;
					//起点以及与当前点向量，模
					double snDeltX = nodeX-jjNodeX;
					double snDeltY = nodeY-jjNodeY;
					double snNodeDis = distance(jjNode, node);
					
					//起终点向量与起点当前点向量 两向量夹角
					angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY)/(jeNodeDis*snNodeDis));
					//投影距离
					projDis=snNodeDis*Math.cos(angle);
				}	
				//angle可能为负值,必须限定大于零条件
				if (projDis > 0 && projDis <= (jeNodeDis + projBuffer)) {
					return true;
				}
				else {
					return false;
				}			
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
	 //计算G,H,F值
	 private void count(MapMatchNode node, MapMatchNode eNode, double cost){
	     countG(node, eNode, cost);
	     countH(node, eNode);
	     countF(node);
	 }
	
	 //计算G值
	 private void countG(MapMatchNode node, MapMatchNode eNode, double cost){
	     if(node.getParentNode()==null){
	         node.setG(cost);
	     }else{
	         node.setG(node.getParentNode().getG() + cost);
	     }
	 }
	
	 //计算H值
	 private void countH(MapMatchNode node, MapMatchNode eNode){
	     double dist = distance(node, eNode);
	     node.setH(dist);
	 }
	
	 //计算F值
	 private void countF(MapMatchNode node){
	     node.setF(node.getG()+node.getH());
	 }	
 
	 /*返回节点组成的路径,从终点往返回到起点*/
	 public void getPath(ArrayList<MapMatchNode> nodesPath, MapMatchNode node){
	    try {
	    	if(node.getParentNode() != null){
	 	        getPath(nodesPath, node.getParentNode());
	 	    }
	 	    nodesPath.add(node);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
	}
		 
	/*获得double中最大值所对应的健值key
	 * */
	public int obtainHigestScoreEID(Map<Integer, Double> edgeAccuScoreMap){  
        Set keySet = edgeAccuScoreMap.entrySet();
        int keyVal = 0;
        if (keySet != null) {
        	Iterator iterator = (Iterator) keySet.iterator(); 
        	Map.Entry  mapEntry = (Map.Entry) iterator.next();
        	Object maxValkey = mapEntry.getKey();
	        Object maxValObject = mapEntry.getValue();//假设第一个值为最大值
        	while (iterator.hasNext()) {
        		mapEntry = (Map.Entry) iterator.next();
        		Object key = mapEntry.getKey();
   	         	Object val = mapEntry.getValue();
   	         	if ((Double)maxValObject < (Double)val) {
   	         		maxValObject = val;
   	         		maxValkey = key;
				}
			}
        	keyVal = (Integer)maxValkey;        	
        }          
        return keyVal;
	}
	
	/**
	 * GPS点纠正:获得纠正后的GPS点
	 * @param gpsTrackPointArrayList
	 * @param pathEIDArrayliList 
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList 存储所有GPS点纠正后坐标
	 * 2014/12/23 重写
	 */
	public void obtainGPSCorrectionCoordOriginal(ArrayList<TaxiGPS> gpsTrackPointArrayList, ArrayList<Integer[]> pathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {		
			ArrayList<Integer[]> pathEIDArrayliList = new ArrayList<Integer[]>();//存储每条GPS轨迹匹配的路径ID以及与GPS的方向性，中间变量
			ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//返回地图纠正后的一条GPS路径
			//倒叙从最后一个GPS点开始  获得最大得分边GPS点匹配路径
			for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
				ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//存储路径ID以及与GPS的方向性，临时变量
				Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//边的累积得分
				//最后一个GPS点，取得得分最高的边的EID
				if (j == gpsTrackPointArrayList.size() - 1) {				
					if (edgeAccuScoreMap.size() > 0) {
						int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//此处不合理
						taxiGPS.setBelongLineID(edgeID);//GPS所属路段ID
						Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
						int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS点与路段方向性的关系，1表示同向，-1表示反向
						Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
						if (tempPathEIDMap.size() > 0) {
							ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
							for (int k = 0; k < tempEIDArrayliList.size(); k++) {
								pathEIDArrayliList.add(tempEIDArrayliList.get(k));
							}
						}
					}
				}
				else {
					boolean isContains = false;
					Map<Integer, ArrayList<Integer[]>> pathEIDMap = taxiGPS.getPathEIDMap();
					ArrayList<MapMatchEdge> candidateEdgeSetArrayList = taxiGPS.candidateEdgeSetArrayList;					
					//取得每个GPS点的路径EID组成的路径
					if (pathEIDMap.size() > 0) {
						for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
							int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
							tempPathEIDArrayliList = pathEIDMap.get(edgeID);
							//如果存在包含关系,前者包含后者
							if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {
								taxiGPS.setBelongLineID(edgeID);//GPS所属路段ID
								isContains = true;
								break;
							}
						}					
					}
					//如果不包含，产生新的路径EID
					if (!isContains) {
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//取得得分最大的路径ID
							taxiGPS.setBelongLineID(edgeID);
							Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
							if (tempPathEIDMap.size() > 0) {
								ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
								for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
									pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//倒叙插入新的EID
								}
							}
						}							
					}	
				}
			}//存储一条GPS轨迹匹配的路径ID			
			
			//去掉相同的EID
			//由于两个GPS点可能位于同一路段，可能选择同一候选路段，造成路段ID中会存在相同的EID,并且相同的EID是相邻的
			//没取得一个EID与uniquePathEIDArrayliList中的最后一个EID比较，如果不同，则加入uniquePathEIDArrayliList
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//中间变量
			for (int k = 0; k < pathEIDArrayliList.size(); k++) {
				if (k == 0) {
					uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
				}
				else {
					int curInteger = pathEIDArrayliList.get(k)[0];
					int uniqueLastEID = uniquePathEIDArrayliList.get(uniquePathEIDArrayliList.size() - 1)[0];
					if (curInteger != uniqueLastEID) {
						uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
					}
				}
			}	
			ArrayList<Integer[]> containShortestPathEIDArrayList = new ArrayList<Integer[]>();//包含拟合的最短路径路径EID
			postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
			ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
			obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
			for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
				System.out.print("获得GPS纠正点：" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
				GPSCorrectArrayList.add(tGPSCorrectedNodeArraylist.get(j));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * GPS点纠正:获得纠正后的GPS点
	 * @param processTaxiTrackMap
	 * @param correctedOriginalTaxiTrackArrayList
	 * @param pathEIDArrayliList 
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList 存储所有GPS点纠正后坐标
	 */
	public void obtainGPSCorrectionCoord(Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap, ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList, ArrayList<Integer[]> containShortestPathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {				
			ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//返回地图匹配每一条路径
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//去掉相同的EID
			Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
//			taxiTrackMap.put(1, gpsTrackPointArrayList);
			obtainMatchRoadAccordOptimal(processTaxiTrackMap, uniquePathEIDArrayliList);//根据全局最优获得GPS点匹配道路
			postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
			ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
			ArrayList<TaxiGPS> gpsTrackPointArrayList = processTaxiTrackMap.get(1);
			obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
			for (int i = 0; i < gpsTrackPointArrayList.size(); i++) {
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(i);
				correctedOriginalTaxiTrackArrayList.add(taxiGPS);	
			}
			for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
				System.out.print("获得GPS纠正点：" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
				GPSCorrectArrayList.add(tGPSCorrectedNodeArraylist.get(j));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/*GPS点纠正:输出纠正后的GPS点
	 * GPSCorrectArrayList：存储所有GPS点纠正后坐标*/
	public void obtainGPSCorrectionCoord(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {	
			ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//返回GPS点		
			for (int i = 0; i < taxiTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = taxiTrackMap.get(i + 1);
				ArrayList<Integer[]> pathEIDArrayliList = new ArrayList<Integer[]>();//存储每条GPS轨迹匹配的路径ID以及与GPS的方向性，中间变量
				ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//返回地图纠正后的一条GPS路径
				for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
					TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
					ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//存储路径ID以及与GPS的方向性，临时变量
					ReturnMatchNode returnGPS = new ReturnMatchNode();
					returnGPS.longitude = taxiGPS.longitude;
					returnGPS.latitude = taxiGPS.latitude;
					returnGPSArrayList.add(returnGPS);
					Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//边的累积得分
					//最后一个GPS点，取得得分最高的边的EID
					if (j == gpsTrackPointArrayList.size() - 1) {				
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);
							Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
							int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS点与路段方向性的关系，1表示同向，-1表示反向
							Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
							if (tempPathEIDMap.size() > 0) {
								ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
								for (int k = 0; k < tempEIDArrayliList.size(); k++) {
									pathEIDArrayliList.add(tempEIDArrayliList.get(k));
								}
							}
						}
					}
					else {
						boolean isContains = false;
						Map<Integer, ArrayList<Integer[]>> pathEIDMap = taxiGPS.getPathEIDMap();
						ArrayList<MapMatchEdge> candidateEdgeSetArrayList = taxiGPS.candidateEdgeSetArrayList;					
						//取得每个GPS点的路径EID组成的路径
						if (pathEIDMap.size() > 0) {
							for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
								int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
								tempPathEIDArrayliList = pathEIDMap.get(edgeID);
								//如果存在包含关系,前者包含后者
								if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {						
									isContains = true;
									break;
								}
							}					
						}
						//如果不包含，产生新的路径EID
						if (!isContains) {
							if (edgeAccuScoreMap.size() > 0) {
								int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//取得得分最大的路径
								Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
								if (tempPathEIDMap.size() > 0) {
									ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
									for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
										pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//倒叙插入新的EID
									}
								}
							}							
						}	
					}
				}//存储一条GPS轨迹匹配的路径ID			
				
				//去掉相同的EID
				//由于两个GPS点可能位于同一路段，可能选择同一候选路段，造成路段ID中会存在相同的EID,并且相同的EID是相邻的
				//没取得一个EID与uniquePathEIDArrayliList中的最后一个EID比较，如果不同，则加入uniquePathEIDArrayliList
				ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//中间变量
				for (int k = 0; k < pathEIDArrayliList.size(); k++) {
					if (k == 0) {
						uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
					}
					else {
						int curInteger = pathEIDArrayliList.get(k)[0];
						int uniqueLastEID = uniquePathEIDArrayliList.get(uniquePathEIDArrayliList.size() - 1)[0];
						if (curInteger != uniqueLastEID) {
							uniquePathEIDArrayliList.add(pathEIDArrayliList.get(k));
						}
					}
				}	
				ArrayList<Integer[]> containShortestPathEIDArrayList = new ArrayList<Integer[]>();//包含拟合的最短路径路径EID
				postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
				ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
				obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
				for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
					System.out.print("获得GPS纠正点：" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
					GPSCorrectArrayList.add(tGPSCorrectedNodeArraylist.get(j));
				}
			}
		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * 根据路径EID获得GPS点在路径上对应的纠正（投影）坐标
	 * 1.遍历集合中的每一个GPS点
	 * 2.判断每一个GPS点候选道路EID，是否在路径集合PathEIDArrayliList中
	 * 3.取得在路径集合中的对应EID的边并返回与相应边的方向关系
	 * 4.GPS点向该边投影，并求得相应的投影坐标
	 * @param gpsTrackPointArrayList GPS点坐标
	 * @param PathEIDArrayliList 路径ID
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList GPS点纠正后坐标
	 */
	public void obtainProjCoordAccordPathEID(ArrayList<TaxiGPS> gpsTrackPointArrayList, ArrayList<Integer[]> PathEIDArrayliList, 
			ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {
			for (int i = 0; i < gpsTrackPointArrayList.size(); i++) {
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(i);
				int targetEdgeID = -1;//纠正点所在目标路段ID
				String targetEdgeName = "none";
				MapMatchNode tempGPS = new MapMatchNode();
				tempGPS.x = taxiGPS.getLongitude();
				tempGPS.y = taxiGPS.getLatitude();
				MapMatchNode correctGPSNode = null;//纠正后GPS点
				ArrayList<MapMatchEdge> candRoadArrayList = taxiGPS.getCandidateEdgeSetArrayList();
				//取得候选边			
				boolean isOK = false;
				for (int j = 0; j < candRoadArrayList.size(); j++) {
					int edgeID = candRoadArrayList.get(j).getEdgeID();//候选边ID
					MapMatchEdge edge = new MapMatchEdge();
					ArrayList<MapMatchNode> pointCollArrayList = new ArrayList<MapMatchNode>();
					int dirRela = isArraylistContainsEID(PathEIDArrayliList, edgeID);//判断路径EID中是否包含此ID
					if (dirRela != 0) {	
						targetEdgeID = edgeID;
						//取得候选边上的点并投影取得纠正坐标
						for (int k = 0; k < polylineCollArrayList.size(); k++) {
							MapMatchEdge polyline = polylineCollArrayList.get(k);
							if (edgeID == polyline.getEdgeID()) {
								edge = polyline;
								targetEdgeName = polyline.getEdgeName();
								pointCollArrayList = polyline.getPointCollArrayList();
								break;
							}
						}
						//获得纠正坐标
						int pointCount = pointCollArrayList.size();
						for (int k = 0; k < pointCount - 1; k++) {
							if (k == 28) {
								System.out.print("");
							}
							MapMatchNode sNode = new MapMatchNode();
							MapMatchNode eNode = new MapMatchNode();
							sNode = pointCollArrayList.get(k);
							eNode = pointCollArrayList.get(k + 1);
							correctGPSNode = PubClass.projPointToLineSegment(sNode, eNode, tempGPS);
							//复杂曲线可能会求得多个投影点，此时需要增加判断条件
							//纠正点与GPS点的距离要小于候选半径
							if (correctGPSNode != null && PubClass.distance(correctGPSNode, tempGPS) <= PubParameter.radius) {
								isOK = true;
								break;
							}
							else {
								continue;
							}							
						}						
					}
					if (isOK) {
						break;
					}
				}
				//纠正后坐标点信息
				if (correctGPSNode != null) {
					CorrectedNode tNode = new CorrectedNode();
					tNode.setOriginLongitude(taxiGPS.longitude);
					tNode.setOriginLatitude(taxiGPS.latitude);
					tNode.setLocalTime(taxiGPS.getLocalTime());
					tNode.setCorrectLongitude(correctGPSNode.getX());
					tNode.setCorrectLatitude(correctGPSNode.getY());
					tNode.setTargetEdgeID(targetEdgeID);
					taxiGPS.setCorrectLongitude(correctGPSNode.getX());
					taxiGPS.setCorrectLatitude(correctGPSNode.getY());
					taxiGPS.setBelongLineID(targetEdgeID);
					taxiGPS.setBelongLinkName(targetEdgeName);
					taxiGPS.setIsGPSCorrected(true);
					GPSCorrectArrayList.add(tNode);
				}
				//否则，只存储GPS信息,没有纠正，则用原始GPS点作为纠正点
				else {
					CorrectedNode tNode = new CorrectedNode();
					tNode.setOriginLongitude(taxiGPS.longitude);
					tNode.setOriginLatitude(taxiGPS.latitude);
					tNode.setLocalTime(taxiGPS.getLocalTime());
					tNode.setTargetEdgeID(targetEdgeID);
					tNode.setCorrectLongitude(taxiGPS.getLongitude());
					tNode.setCorrectLatitude(taxiGPS.getLatitude());
					taxiGPS.setCorrectLongitude(taxiGPS.longitude);
					taxiGPS.setCorrectLatitude(taxiGPS.getLatitude());
					taxiGPS.setBelongLineID(targetEdgeID);
					taxiGPS.setBelongLinkName(targetEdgeName);
					taxiGPS.setIsGPSCorrected(true);
					GPSCorrectArrayList.add(tNode);
				}		
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/*获得候选道路可能为目标路段的GPS点
	 * eliminateZeroSpeedGPSDataArrayList:去掉速度为零的GPS点
	 * eligibleGPSArrayList：候选道路可能为目标路段的GPS点
	 * radius：候选半径
	 * linkID：目标路段ID
	 * targetEdge：返回目标路段*/
	public void obtainEligibleGPSArraylist(Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap, Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap,
			ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList,
			ArrayList<TaxiGPS> eligibleGPSArrayList,double radius, MapMatchEdge targetEdge){
		try {
			int linkID = targetEdge.getEdgeID();
			Double leftDownNode[] = new Double[2];
			Double rightTopNode[] = new Double[2];
			PubClass.BoundingRectangle(leftDownNode, rightTopNode, targetEdge);//获得外包矩形
			//矩形区域扩充
			leftDownNode[0] = leftDownNode[0] - radius;
			leftDownNode[1] = leftDownNode[1] - radius;
			rightTopNode[0] = rightTopNode[0] + radius;
			rightTopNode[1] = rightTopNode[1] + radius;
			ArrayList<TaxiGPS> tempGPSArrayList = new ArrayList<TaxiGPS>();
			for (int i = 0; i < eliminateZeroSpeedGPSDataArrayList.size(); i++) {
				TaxiGPS taxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
				MapMatchNode tNode = new MapMatchNode();
				tNode.setX(taxiGPS.getLongitude());
				tNode.setY(taxiGPS.getLatitude());
				if (PubClass.isNodeInSquare(leftDownNode, rightTopNode, tNode)) {
					tempGPSArrayList.add(taxiGPS);
				}
			}
			Map<Integer, ArrayList<TaxiGPS>> tempGPSTaxiMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
			tempGPSTaxiMap.put(1, tempGPSArrayList);			
			obtainCandidateRoadSet(tempGPSTaxiMap, allGridIndexVerticesMap, allGridPolylineMap, radius);		
			Set keySet = tempGPSTaxiMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
	    	while (iterator.hasNext()) {
	    		Map.Entry mapEntry = (Map.Entry) iterator.next();
	    		int key = (Integer)mapEntry.getKey();
	    		ArrayList<TaxiGPS> tempTaxiGPSArrayList = tempGPSTaxiMap.get(key);
	    		for (int i = 0; i < tempTaxiGPSArrayList.size(); i++) {
					TaxiGPS taxiGPS = tempTaxiGPSArrayList.get(i);
					ArrayList<MapMatchEdge> candidateEdgeSet = taxiGPS.getCandidateEdgeSetArrayList();
					for (int j = 0; j < candidateEdgeSet.size(); j++) {
						MapMatchEdge tEdge = candidateEdgeSet.get(j);
						if (tEdge.getEdgeID() == linkID) {
							eligibleGPSArrayList.add(taxiGPS);
							break;
						}
					}
				}
	    	}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/*获得目标路段：根据路段ID获得目标路段
	 * targetLinkID：路段ID
	 * targetEdge：目标路段
	 * polylineCollArrayList：路段集合
	 * */
	public MapMatchEdge obtainTargetEdge(int targetLinkID, ArrayList<MapMatchEdge> polylineCollArrayList){
		MapMatchEdge targetEdge = new MapMatchEdge();
		try {
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				MapMatchEdge tEdge = polylineCollArrayList.get(i);
				if (tEdge.getEdgeID() == targetLinkID) {
					targetEdge = tEdge;
					break;
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return targetEdge;
	}
	
	/*筛选唯一的出租车ID,以确定唯一的TaxiID
	 * eligibleGPSArrayList：候选道路可能为目标路段的GPS点
	 * uniqueTaxiArrayList:符合条件的taxiID,唯一的taxiID*/
	public void obtainUniqueTaxiAccordGPS(ArrayList<TaxiGPS> eligibleGPSArrayList, ArrayList<TaxiGPS> uniqueTaxiArrayList){
		try {
			for (int i = 0; i < eligibleGPSArrayList.size(); i++) {
				TaxiGPS curTaxiGPS = eligibleGPSArrayList.get(i);
				if (!isContainsTaxiID(uniqueTaxiArrayList, curTaxiGPS)) {
					uniqueTaxiArrayList.add(curTaxiGPS);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());			
		}	
	}
	
	/*根据出租车ID对GPS信息进行分类
	 * ID相同的出租车归为一类
	 * eligibleGPSArrayList：输入出租车信息
	 * taxiSortMap：根据ID分类后的出租车信息
	 * 1.获得每个出租车唯一的targetID
	 * 2.与此targetID相同的出租车信息归为一类
	 * 3.存储*/
	public void sortTaxiAccordID(ArrayList<TaxiGPS> eligibleGPSArrayList,Map<String, ArrayList<TaxiGPS>> taxiSortMap){
		try {
			ArrayList<TaxiGPS> uniqueTaxiArrayList = new ArrayList<TaxiGPS>();
			obtainUniqueTaxiAccordGPS(eligibleGPSArrayList, uniqueTaxiArrayList);
			for (int i = 0; i < uniqueTaxiArrayList.size(); i++) {
				TaxiGPS taxiGPS = uniqueTaxiArrayList.get(i);
				String targetID = taxiGPS.getTargetID();
				ArrayList<TaxiGPS> tempArrayList = new ArrayList<TaxiGPS>();
				for (int j = 0; j < eligibleGPSArrayList.size(); j++) {
					TaxiGPS tempTaxiGPS = eligibleGPSArrayList.get(j);
					if (targetID.equals(tempTaxiGPS.getTargetID())) {
						tempArrayList.add(tempTaxiGPS);
					}
				}
				taxiSortMap.put(targetID, tempArrayList);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}	
	
	
	/*统计所有出租车在目标路段的行程时间
	 * uniqueTaxiArrayList：所有出租车的ID唯一
	 * startTimeStr：出租车开始时间，格式为2013-01-01 00:00:15
	 * timeInterval:要查询一次出租车轨迹的时间间隔
	 * targetLinkID：目标路段ID
	 * timeThreshold:路段最大通行时间的阈值，<=60分
	 * travalTimeMap:以出租车ID为索引,存储的路段通行时间
	 * taxiLinkTravelMap:以出租车ID为索引,存储出租车的GPS点
	 * */
	public void obtainAllTaxiTravelTime2(ArrayList<TaxiGPS> uniqueTaxiArrayList, int timeInterval, int targetLinkID,
			int timeThreshold, Map<String, Double> travelTimeMap, Map<String, ArrayList<MapMatchNode>> taxiLinkTravelMap){
		try {
			for (int i = 0; i < uniqueTaxiArrayList.size(); i++) {
				TaxiGPS taxiGPS = uniqueTaxiArrayList.get(i);
				String taxiIDStr = taxiGPS.getTargetID();
				String startTimeStr = taxiGPS.getLocalTime();				
				String[] timeArray = new String[1];
				PubClass.obtainEndTimeAccordStartTime(startTimeStr, timeInterval, timeArray);
				String endTimeStr = timeArray[0];
				double[] travelTime = new double[1];//路段通行时间
				ArrayList<MapMatchNode> taxiTravelArrayList = new ArrayList<MapMatchNode>();//路段通行时间对应的GPS点
				//获得单个出租车的通行时间
				obtainSingleTaxiTravelTime2(taxiIDStr, startTimeStr, startTimeStr, endTimeStr, 
						timeInterval, targetLinkID, travelTime, taxiTravelArrayList, timeThreshold);
				travelTimeMap.put(taxiIDStr, travelTime[0]);
				taxiLinkTravelMap.put(taxiIDStr, taxiTravelArrayList);
					
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/*主要考虑同一辆出租车可能会穿越同一路段多次
	 * taxiSortMap：按ID进行归类的出租车信息
	 * startTimeStr：开始时间
	 * endTimeStr：终止时间
	 * targetLinkID:目标路段ID
	 * targetEdge：目标路段
	 * sampleThreshold:采样阈值（默认60s），用于进行轨迹剖分
	 * expandTime：扩展时间（默认60s）
	 * taxiTravelTimeArrayList:目标路段所有出租车的通行信息
	 * */
	public void obtainAllTaxiTravelTime(Map<String, ArrayList<TaxiGPS>> taxiSortMap, String startTimeStr, String endTimeStr, int targetLinkID,
			MapMatchEdge targetEdge, int sampleThreshold, int expandTime, ArrayList<TaxiTravelTime> taxiTravelTimeArrayList){
		try {
			Set keySet = taxiSortMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
	    	while (iterator.hasNext()) {
	    		Map.Entry mapEntry = (Map.Entry) iterator.next();
	    		String taxiIDStr = (String)mapEntry.getKey();
	    		if (taxiIDStr.equals("MMC8000GPSANDASYN051113-22962-00000000")) {
	    			System.out.print("");
				}
	    		ArrayList<TaxiGPS> taxiTrackArrayList = taxiSortMap.get(taxiIDStr);//时间段范围内出租车的轨迹
	    		TaxiTravelTime taxiTravelTime = new TaxiTravelTime();
				//获得单个出租车的通行时间相关信息
				obtainSingleTaxiTravelTime(taxiTrackArrayList, startTimeStr, endTimeStr, targetLinkID, targetEdge,
						sampleThreshold, expandTime, taxiTravelTime);
				ArrayList<String> startTravelTimeArraylist = taxiTravelTime.getStartTravelTimeArraylist();
				if (startTravelTimeArraylist != null && startTravelTimeArraylist.size() != 0) {
					taxiTravelTimeArrayList.add(taxiTravelTime);
				}								
	    	}	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
	/*统计某一出租车行程时间
	 * 1.匹配出租车轨迹:根据出租车ID、出租车记录时间每隔timeInterval时间查询一次出租车的轨迹
	 * 2.检索出租车轨迹ID中是否包含目标路段；
	 * 3.若出租车轨迹中包含目标路段，轨迹符合条件
	 * 		根据GPS的候选路段中是否包含目标路段筛选GPS点：若候选路段中包含目标路段，GPS点符合条件，继续判断下一个GPS点，
	 * 		若轨迹中的所有点都包含targetLinkID，则进行下一个时间间隔timeInterval的GPS点的选取，知道GPS点候选路段
	 * 		不包含目标路段，若候选路段不包含目标路段，则此GPS点不符合条件，然后计算到该点的时间作为行程时间
	 * 	否则，此出租车轨迹不符合条件；
	 * 4.
	 * taxiIDStr:出租车ID
	 * startStartTimeStr:最初的开始时间,此值不变
	 * startTimeStr:开始时间
	 * endTimeStr:终止时间
	 * timeInterval:时间间隔,此值不变
	 * targetLinkID:目标路段ID,此值不变
	 * travalTime:出租车存储的路段通行时间*/
	public void obtainSingleTaxiTravelTime2(String taxiIDStr, String startStartTimeStr, String startTimeStr, 
			String endTimeStr, int timeInterval, int targetLinkID, double[] travelTime, 
			ArrayList<MapMatchNode> taxiTravelArrayList, int timeThreshold){
		try {
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, taxiIDStr, startTimeStr, endTimeStr);
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//去掉速度为零的GPS点
			ReturnGPSAndPath returnGPSAndPath = new ReturnGPSAndPath();
			Map<Integer, ArrayList<TaxiGPS>> taxiMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
			taxiMap.put(1, eliminateZeroSpeedGPSDataArrayList);
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();//所有路径EID组成路径,包含与路径的方向性
			MapMatchAlgorithm.mapMatch(taxiMap, returnGPSAndPath,allPathEIDMap);//匹配路径
			ArrayList<Integer[]> pathEIDArrayList = allPathEIDMap.get(1);
			//判断路径ID中是否包含目标路段
			if (isArraylistContainsEID(pathEIDArrayList, targetLinkID) != 0) {
				int GPSCount = 0;//检验的出租车GPS点的数目
				for (int j = 0; j < eliminateZeroSpeedGPSDataArrayList.size(); j++) {
					TaxiGPS tempTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(j);
					ArrayList<MapMatchEdge> candidateEdgeSetArrayList = tempTaxiGPS.getCandidateEdgeSetArrayList();
					if (isEdgeArraylistContainEdgeID(candidateEdgeSetArrayList, targetLinkID)) {
						MapMatchNode tNode = new MapMatchNode();
						tNode.setX(tempTaxiGPS.getLongitude());
						tNode.setY(tempTaxiGPS.getLatitude());
						taxiTravelArrayList.add(tNode);
						GPSCount++;
						//如果所有GPS点候选路段均包含目标路段targetLinkID，扩展时间段
						//如果扩展时间段后的时间与最初的起始时间的时间间隔> 时间限，则此taxi不符合条件，跳出循环
						if (GPSCount == eliminateZeroSpeedGPSDataArrayList.size()) {
							String[] endTimeArray = new String[1];
							PubClass.obtainEndTimeAccordStartTime(endTimeStr, timeInterval, endTimeArray);
							double tiemInterval = PubClass.obtainTimeInterval(startStartTimeStr, endTimeArray[0]);
							//没有超出时间阈值
							if (tiemInterval < timeInterval) {
								obtainSingleTaxiTravelTime2(taxiIDStr, startStartTimeStr, endTimeStr, endTimeArray[0], timeInterval, 
										targetLinkID, travelTime, taxiTravelArrayList, timeThreshold);
							}						
						}
					}
					else {
						MapMatchNode tNode = new MapMatchNode();
						tNode.setX(tempTaxiGPS.getLongitude());
						tNode.setY(tempTaxiGPS.getLatitude());
						taxiTravelArrayList.add(tNode);
						String tempEndTimeStr = tempTaxiGPS.getLocalTime();
						//求通行时间
						travelTime[0] = PubClass.obtainTimeInterval(startStartTimeStr, tempEndTimeStr);	
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
	
	/*单个出租车通过目标路段时间
	 * taxiTrackArrayList：粗略过滤的出租车轨迹
	 * startTimeStr：开始时间
	 * endTimeStr：终止时间
	 * targetLinkID：目标路段ID
	 * targetEdge：目标路段
	 * sampleThreshold:采样阈值（默认60s），用于进行轨迹剖分
	 * expandTime：扩展时间（默认60s），
	 * TaxiTravelTime：出租车通行时间，同一出租车同一路段的通行时间可能为多个
	 * 
	 * 1.轨迹剖分：根据相邻GPS点之间的采样时间差（小于阈值sampleDiff），判断是为同一时间段内的同一轨迹；
	 * 		如果时间差大于阈值，则可能为不同时间段内通过同一路段的不同轨迹
	 * 2.时间扩展：根据每一条轨迹首尾之间的时间，扩展时间expandTime（默认60s）
	 * 3.数据检索：根据targetID以及起止时间以及扩展后的时间检索数据库，检索范围内的GPS数据
	 * 4.地图匹配：根据GPS数据对出租车轨迹进行地图匹配
	 * 5.目标路段判断：判断匹配路径中是否包含目标路段
	 * 6.插值计算：如果包含目标路段，则在路段端点处根据距离进行插值（简单插值）
	 * 7.求结果：求通行时间以及对应的路径*/	
	public void obtainSingleTaxiTravelTime(ArrayList<TaxiGPS> taxiTrackArrayList, String startTimeStr, String endTimeStr, 
			int targetLinkID, MapMatchEdge targetEdge, int sampleThreshold, 
			int expandTime, TaxiTravelTime taxiTravelTime){
		try {
			//轨迹剖分,可能多条轨迹
			String tempTaxiIDStr = taxiTrackArrayList.get(0).getTargetID();
			System.out.print(tempTaxiIDStr + "开始轨迹剖分：" + '\n');
			ArrayList<ArrayList<TaxiGPS>> subdivisionTrackArrayList = new ArrayList<ArrayList<TaxiGPS>>();
			if (taxiTrackArrayList.get(0).getTargetID().equals("MMC8000GPSANDASYN051113-21372-00000000")) {
				System.out.print("");
			}
			trackSubdivision(taxiTrackArrayList, sampleThreshold, subdivisionTrackArrayList);
			System.out.print(tempTaxiIDStr + "结束轨迹剖分：" + '\n');
			ArrayList<String> startTravelTimeArraylist = new ArrayList<String>();//出租车开始进入某路段的时刻
			Map<String, Double> travelTimeMap = new HashMap<String, Double>();//以时间为索引，某一时间点通过路段的通行时间
			Map<String, Double> taxiMeanSpeedMap = new HashMap<String, Double>();//以时间为索引，某一时间点通过路段的平均速度
			//以时间为索引，某一时间点通过路段时，出租车行驶方向与路段的方向关系
			Map<String, Integer> taxiTravelDirectionMap = new HashMap<String, Integer>();
			//以时间为索引，某一时间点通过路段时，出租车进入路段端点ID与驶出路段端点ID,0对应进入端点索引，1对应驶出路段端点索引
			Map<String, int[]> taxiEntranceExitNodeIDMap = new HashMap<String, int[]>();
			//以时间为索引，某一时间点通过路记录的GPS点
			Map<String, ArrayList<MapMatchNode>> GPSTravelMap = new HashMap<String, ArrayList<MapMatchNode>>();
			for (int i = 0; i < subdivisionTrackArrayList.size(); i++) {
				ArrayList<TaxiGPS> tempGPSArrayList = subdivisionTrackArrayList.get(i);//轨迹剖分后的出租车轨迹  
				int count = tempGPSArrayList.size();
				TaxiGPS startTaxiGPS = tempGPSArrayList.get(0);
				TaxiGPS endTaxiGPS = tempGPSArrayList.get(count - 1);
				String time1 = startTaxiGPS.getLocalTime();
				String time2 = endTaxiGPS.getLocalTime();
				String taxiIDStr = startTaxiGPS.getTargetID();//出租车ID
				if (taxiIDStr.equals("MMC8000GPSANDASYN051113-24710-00000000")) {
					System.out.print("");
				}				
				taxiTravelTime.setTaxiID(taxiIDStr);				
				//时间扩展				
				String[] startTimeArray = new String[1];				
				PubClass.obtainStartTimeAccordEndTime(time1, expandTime, startTimeArray);//前向时间扩展
				String curStartTimeStr = startTimeArray[0];
				String[] endTimeArray = new String[1];				
				PubClass.obtainEndTimeAccordStartTime(time2, expandTime, endTimeArray);//后向时间扩展
				String curEndTimeStr = endTimeArray[0];
				System.out.print(taxiIDStr + "开始轨迹扩展：" + '\n');
				ArrayList<TaxiGPS> expandTaxiGPSArrayList = new ArrayList<TaxiGPS>();//进行时间扩展后的出租车轨迹
				DatabaseFunction.obtainGPSDataFromDatabase(expandTaxiGPSArrayList, taxiIDStr, curStartTimeStr, curEndTimeStr);
				System.out.print(taxiIDStr + "结束轨迹扩展：" + '\n');				
				Map<Integer, ArrayList<TaxiGPS>> taxiMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
				taxiMap.put(1, expandTaxiGPSArrayList);
				ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
				ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();
				ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
				MapMatchAlgorithm.coordinateCorr(taxiMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList, GPSCorrectArrayList);//坐标纠正
				boolean isAbnormalParking = isAbnormalParkingTrack(correctedOriginalTaxiTrackArrayList);//是否存在异常
				int linkDirection = isArraylistContainsEID(pathEIDArrayList, targetLinkID);
				MapMatchEdge nextEdge = obtainNextEdgeAccordToCurrentLinkID(pathEIDArrayList, targetLinkID);
				//如果路径ID中包含目标路段
				if (linkDirection != 0 && nextEdge != null && !isAbnormalParking) {
					String[] interpolateStartTime = new String[1];
					double[] travelTime = new double[1];
					ArrayList<MapMatchNode> GPSTravelArrayList = new ArrayList<MapMatchNode>();//目标路段GPS上点
					double[] meanSpeed = new double[1];//平均速度
					double[]predictMinTime = new double[1];//根据最大速度估计的最小时间
					int []entranceExitNodeID = new int[2];
					obtainEntranceExitNodeID(targetEdge, linkDirection, entranceExitNodeID);//获得出租车入口路段端点ID、出口路段端点ID
					//在路段端点处进行时间插值计算
					//在进行时间插值的时候，应该考虑到速度为零的GPS点情况，所以数据集用expandTaxiGPSArrayList而不是用eliminateZeroSpeedGPSDataArrayList
					improveTimeInterpolate(correctedOriginalTaxiTrackArrayList, targetEdge, nextEdge, targetLinkID, linkDirection, 
							interpolateStartTime, travelTime, GPSTravelArrayList, meanSpeed);	
					if (interpolateStartTime[0] != null && GPSTravelArrayList.size() != 0) {
						if (interpolateStartTime[0].equals("2013-01-01 06:14:03") || travelTime[0] == 163 ) {
							System.out.print("");
						}
						startTravelTimeArraylist.add(interpolateStartTime[0]);
						travelTimeMap.put(interpolateStartTime[0], travelTime[0]);
						GPSTravelMap.put(interpolateStartTime[0], GPSTravelArrayList);
						taxiMeanSpeedMap.put(interpolateStartTime[0], meanSpeed[0]);
						taxiTravelDirectionMap.put(interpolateStartTime[0], linkDirection);
						taxiEntranceExitNodeIDMap.put(interpolateStartTime[0], entranceExitNodeID);						
						System.out.print("已获得单车路段行程时间！" + '\n');
						if (taxiIDStr.equals("MMC8000GPSANDASYN051113-22962-00000000")) {
			    			System.out.print("");
						}
					}					
				}
				else {
					System.out.print("未获得单车路段行程时间！" + '\n');
					continue;
				}
			}
			if (startTravelTimeArraylist.size() != 0) {
				taxiTravelTime.setStartTravelTimeArraylist(startTravelTimeArraylist);
				taxiTravelTime.setTravelTimeMap(travelTimeMap);
				taxiTravelTime.setGPSTravelMap(GPSTravelMap);
				taxiTravelTime.setTaxiMeanSpeedMap(taxiMeanSpeedMap);
				taxiTravelTime.setTaxiTravelDirectionMap(taxiTravelDirectionMap);
				taxiTravelTime.setTaxiEntranceExitNodeIDMap(taxiEntranceExitNodeIDMap);
				
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 根据出租车行驶方向与路段关系,获得出租车进入路段与驶出路段端点的ID
	 * @param edge
	 * @param linkDirection
	 * @param entranceExitNodeID
	 */
	public void obtainEntranceExitNodeID(MapMatchEdge edge, int linkDirection, int []entranceExitNodeID) {
		try {
			int entranceNodeID = -1;
			int exitNodeID = -1;
			if (linkDirection == 1) {
				entranceNodeID = edge.getBeginPoint().getNodeID();
				exitNodeID = edge.getEndPoint().getNodeID();
				entranceExitNodeID[0] = entranceNodeID;
				entranceExitNodeID[1] = exitNodeID;
			}
			else if (linkDirection == -1) {
				entranceNodeID = edge.getEndPoint().getNodeID();
				exitNodeID = edge.getBeginPoint().getNodeID();
				entranceExitNodeID[0] = entranceNodeID;
				entranceExitNodeID[1] = exitNodeID;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 异常停车轨迹判断
	 * 如果GPS点在某个位置停车时间超过continuousStaticTimeThreshold，则认为是异常停车，应剔除掉
	 * @param correctedOriginalTaxiTrackArrayList
	 * @return
	 */
	public boolean isAbnormalParkingTrack(ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList) {
		boolean isAbnormalParkingTrack = false;
		try {
			for (int i = 0; i < correctedOriginalTaxiTrackArrayList.size(); i++) {
				TaxiGPS taxiGPS = correctedOriginalTaxiTrackArrayList.get(i);
				int continuousStaticTime = taxiGPS.getContinuousStaticTime();
				if (continuousStaticTime > PubParameter.continuousStaticTimeThreshold) {
					isAbnormalParkingTrack = true;
					break;
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isAbnormalParkingTrack;
	}
	
	/*GPS数据处理：去掉速度为零的GPS点以及在路段端点处对停车数据进行抽稀，停车数据只保留一个GPS点，而此点的速度不为零，以便于地图匹配
	 * 首先在路段端点处做缓冲区，然后对停车数据抽稀
	 * processedGPSDataArraylist：处理后的GPS点
	 * taxiGPSArrayList：待处理GPS点
	 * targetEdge：目标路段*/
	public void processTaxiGPSData(ArrayList<TaxiGPS> processedGPSDataArraylist, ArrayList<TaxiGPS> taxiGPSArrayList, MapMatchEdge targetEdge){
		try {
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//去掉速度为零的GPS点
			MapMatchNode beginPoint = targetEdge.getBeginPoint();
			MapMatchNode endPoint = targetEdge.getEndPoint();
			int beginIndex = -1;
			int endIndex = -1;
			for (int i = 0; i < eliminateZeroSpeedGPSDataArrayList.size(); i++) {
				TaxiGPS taxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
				MapMatchNode node = new MapMatchNode();
				node.setX(taxiGPS.getLongitude());
				node.setY(taxiGPS.getLatitude());				
				if (PubClass.distance(beginPoint, node) < PubParameter.endpointBufferRadius ) {
					//路段端点处停车，取得一个速度不为零的点
					if (beginIndex == -1 && taxiGPS.getSpeed() > PubParameter.zeroSpeedThreshold) {
						beginIndex = i;
						processedGPSDataArraylist.add(taxiGPS);
					}
				}
				else if (PubClass.distance(endPoint, node) < PubParameter.endpointBufferRadius ) {
					if (endIndex == -1 && taxiGPS.getSpeed() > PubParameter.zeroSpeedThreshold) {
						endIndex = i;
						processedGPSDataArraylist.add(taxiGPS);
					}
				}
				else {
					processedGPSDataArraylist.add(taxiGPS);
				}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/*轨迹剖分
	 * 输入：
	 * taxiTrackArrayList：出租车GPS轨迹
	 * sampleThreshold：采样阈值（默认60s）
	 * 输出：
	 * subdivisionTrackArrayList：剖分轨迹
	 * 根据GPS点的采样阈值进行轨迹的剖分
	 * 1.求相邻GPS点之间的时间差，若差值小于采样阈值，说明此相邻GPS点为同一GPS轨迹
	 * 		若差值大于采样阈值，说明此相邻GPS点不为同一GPS轨迹
	 * 2.据此，对GPS轨迹进行剖分，生成多条轨迹*/
	public void trackSubdivision(ArrayList<TaxiGPS> taxiTrackArrayList, int sampleThreshold, 
			ArrayList<ArrayList<TaxiGPS>> subdivisionTrackArrayList){
		try {
			if (taxiTrackArrayList.size() == 1) {
				subdivisionTrackArrayList.add(taxiTrackArrayList);
			}
			else {
				ArrayList<TaxiGPS> trackArrayList = new ArrayList<TaxiGPS>();
				for (int i = 0; i < taxiTrackArrayList.size() - 1; i++) {
					TaxiGPS curTaxiGPS = taxiTrackArrayList.get(i);
					String curLocalTimeStr = curTaxiGPS.getLocalTime();
					TaxiGPS nextTaxiGPS = taxiTrackArrayList.get(i + 1);
					String nextLoacalTimeStr = nextTaxiGPS.getLocalTime();
					double timeInterval = PubClass.obtainTimeInterval(curLocalTimeStr, nextLoacalTimeStr);
					//小于阈值，说明为同一轨迹
					if (timeInterval <= sampleThreshold) {
						trackArrayList.add(curTaxiGPS);
						if (i == taxiTrackArrayList.size() - 2) {
							trackArrayList.add(nextTaxiGPS);
							subdivisionTrackArrayList.add(trackArrayList);
						}
					}
					else {
						trackArrayList.add(curTaxiGPS);
						subdivisionTrackArrayList.add(trackArrayList);
						trackArrayList = new ArrayList<TaxiGPS>();
						if (i == taxiTrackArrayList.size() - 2) {
							trackArrayList.add(nextTaxiGPS);
							subdivisionTrackArrayList.add(trackArrayList);
						}
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
	 * 改进的时间插值法:利用纠正后点的坐标计算路段行程时间
	 * 1.目标路段GPS点数目判断，若只有一个GPS点，平均速度法计算
	 * 2.目标路段有多个GPS点
	 * 		a.交叉口区域搜索，判断是否存在停车等待情况,若存在获得最大停车时间点;
	 * 
	 * 		b.
	 * 		
	 * 
	 * 3.
	 * @param taxiGPSArrayList	
	 * @param targetEdge	
	 * @param linkID	
	 * @param linkDirection	
	 * @param interpolateStartTime	
	 * @param travelTime	
	 * @param GPSTravelArrayList	
	 * @param meanSpeed	
	 */
	public void improveTimeInterpolate(ArrayList<TaxiGPS> taxiGPSArrayList, MapMatchEdge targetEdge, MapMatchEdge nextEdge, int linkID, int linkDirection,
			String[]interpolateStartTime, double[] travelTime, ArrayList<MapMatchNode> GPSTravelArrayList, double[] meanSpeed){
		try {	
			String curLinkBeginPointTimeStr = "";//当前路段起点时间
			String curLinkEndPointTimeStr = "";//当前路段终点时间
			int linkStartGPSIndex = -1;//当前路段上第一个GPS点的下标
			int linkEndGPSIndex = -1;//当前路段上最后一个GPS点的下标
			int gpsCount = taxiGPSArrayList.size();
			int []linkStartEndIndex = new int[2];
			obtainLinkStartEndGPSIndex(taxiGPSArrayList, linkID, linkStartEndIndex);
			linkStartGPSIndex = linkStartEndIndex[0];
			linkEndGPSIndex = linkStartEndIndex[1];
			//从路段起始节点到结束节点为载客轨迹
			if (linkStartGPSIndex != -1 && linkEndGPSIndex != -1) {
				if (isCarrayPassengerTrack(taxiGPSArrayList, linkStartGPSIndex, linkEndGPSIndex)) {
					//出租车在路段入口处GPS点确定
					//如果入口有停车情况，获得最后一个停车点
					TaxiGPS entranceGPS = new TaxiGPS();//路段入口处速度为零且时间最大(位于后面的时间)的GPS点
					TaxiGPS exitGPS = new TaxiGPS();//路段出口处速度为零且时间最大的GPS点
					MapMatchNode curBeginPoint = new MapMatchNode();//路段起点
					curBeginPoint.setNodeID(-1);//为判断curBeginPoint是否落在两点之间
					MapMatchNode curEndPoint = new MapMatchNode();//路段终点
					curEndPoint.setNodeID(-1);
					//获得目标路段上GPS点数
					int targetLinkGPSCount = linkEndGPSIndex - linkStartGPSIndex + 1;
					/*目标路段上只有一个GPS点*/
					if (targetLinkGPSCount == 1) {
						double speed = 0;
						for (int i = 0; i < taxiGPSArrayList.size(); i++) {
							TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
							if (taxiGPS.getBelongLineID() == linkID) {
								speed = taxiGPS.getSpeed();
								break;
							}
						}
						double length = targetEdge.getEdgeLength();
						travelTime[0] = length/speed;
					}
					/*目标路段上有多个GPS点*/
					else {
						//获得路段入口、出口处速度为零且时间最大(位于后面的时间)的GPS点
						int[] endpointIndexArray = obtainZeroSpeedMaxiTimePointAtEntranceExit(taxiGPSArrayList, entranceGPS, exitGPS, targetEdge, linkID, linkDirection, curBeginPoint, curEndPoint);
						curLinkBeginPointTimeStr = timeInterpolateEntranceProcess(taxiGPSArrayList, endpointIndexArray[0], entranceGPS, linkStartGPSIndex, linkID, curBeginPoint, targetEdge);
						curLinkEndPointTimeStr = timeInterpolateExitProcess(taxiGPSArrayList, endpointIndexArray[1], exitGPS, linkEndGPSIndex, linkID, curEndPoint, nextEdge);
						
						/*	出租车是否掉头判断
						 *  取得路段上的GPS点，根据heading进行比较，若相邻两点heading间相差180（由于存在误差，限差可以取150），
						 *  则认为出租车的运动方向相反，即存在在路段中间掉头现象 
						 * 流程：
						 * 1.如果在路段入口有停车现象，取得入口处GPS点，此时路段上起点GPS索引为endpointIndexArray[0]+1
						 * 	若没有停车现象，则直接取得路段上起点GPS索引linkStartGPSIndex
						 * 2.如果在路段出口有停车现象，取得出口处GPS点，此时路段上点最后一个GPS索引为endpointIndexArray[1]
						 * 	若没有停车现象，则直接取得路段上最后一个GPS索引为linkEndGPSIndex
						 * 3.判断此两索引之间的GPS点，相邻两GPS（去掉速度为零的GPS点）点heading之差不大于180（设置为150），若差超出阈值，则存在在路段中间掉头现象 
						 * */
						
						//相邻GPS点间方向角度的判断，以保证出租车不会中途折返
						if (linkStartGPSIndex != -1 && linkEndGPSIndex != -1 && linkStartGPSIndex < linkEndGPSIndex) {
							boolean isTravelTimeValid = true;//路段通行时间的有效性
							for (int i = linkStartGPSIndex; i < linkEndGPSIndex; i++) {
								TaxiGPS curTaxiGPS = taxiGPSArrayList.get(i);
								double curHeading = curTaxiGPS.getHeading();
								TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(i + 1);
								double nextHeading = nextTaxiGPS.getHeading();
								double headingDiffer = Math.abs(curHeading - nextHeading);
								if (headingDiffer > PubParameter.headingDifferDownThreshold && headingDiffer < PubParameter.headingDifferUpThreshold) {
									isTravelTimeValid = false;
									break;
								}					
							}
							//当目标路段上所有GPS点所属路段ID为目标路段时，这样的GPS点求得通行时间才是有效通行时间
							//这样去掉两点间有多个相邻路段的情况，以保证GPS点匹配唯一路段
							for (int i = linkStartGPSIndex; i < linkEndGPSIndex; i++) {
								TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
								if (taxiGPS.getBelongLineID() != linkID) {
									isTravelTimeValid = false;
									break;
								}
							}						
							//不仅在目标路段，而且要在目标路段的相邻路段上检索，取得GPS点的速度最大值
							//从目标路段开始、结束GPS点索引，向前以及向后各推两个节点
							//如果在相邻路段上没有点，则只在目标路段取得GPS点的速度最大值
							double maxSpeed = 0;//取得的最大速度
							double predictMinTime = 0;//根据最大速度估计的最小时间
							if (linkStartGPSIndex - 1 >= 0 && linkEndGPSIndex + 1 < gpsCount) {
								maxSpeed = obtainMaxSpeed(taxiGPSArrayList, linkStartGPSIndex - 1, linkEndGPSIndex + 1 );
							}
							else {
								maxSpeed = obtainMaxSpeed(taxiGPSArrayList, linkStartGPSIndex, linkEndGPSIndex);
							}
							if (maxSpeed != 0) {
								double targetEdgeLength = targetEdge.getEdgeLength();
								String predictMinTimeStr = String.format("%.2f", targetEdgeLength/maxSpeed);//保留小数点后两位，并进行四舍五入
								predictMinTime = Double.parseDouble(predictMinTimeStr);//估计最小时间
							}							
							//如果为有效通行时间
							if (isTravelTimeValid) {					
								if (!curLinkBeginPointTimeStr.equals("") && !curLinkEndPointTimeStr.equals("")) {
									double tempTravelTime = PubClass.obtainTimeInterval(curLinkBeginPointTimeStr, curLinkEndPointTimeStr);								
									//求得通行时间大于估计的最小时间才能保证求得通行时间的正确性
									if (tempTravelTime > predictMinTime) {
										//目标路段上的GPS点，从taxiGPSArrayList中获得
										//并求平均速度
										travelTime[0] = tempTravelTime;
										interpolateStartTime[0] = curLinkBeginPointTimeStr;
										for (int i = linkStartGPSIndex; i <= linkEndGPSIndex; i++) {
											TaxiGPS tempTaxiGPS = taxiGPSArrayList.get(i);
											MapMatchNode tNode = new MapMatchNode();
											tNode.setX(tempTaxiGPS.getLongitude());
											tNode.setY(tempTaxiGPS.getLatitude());
											GPSTravelArrayList.add(tNode);
										}
										String meanSpeedStr = String.format("%.2f", targetEdge.getEdgeLength()/tempTravelTime);//保留小数点后两位，并进行四舍五入
										meanSpeed[0] = Double.parseDouble(meanSpeedStr);
									}
									else {
										System.out.print("通行时间：" + tempTravelTime + "不符合实际情况!");
									}	
								}
							}
						}
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
	 * 在路段入口处进行时间插值计算处理		
	 * @param taxiGPSArrayList	路段上的GPS点
	 * @param eliminateZeroSpeedGPSDataArrayList	去掉速度为零的GPS点
	 * @param targetEdge	目标路段
	 * @param linkID	目标路段ID
	 * @param endpointEntranceIndex	在taxiGPSArrayList中获得的路段端点处停车等待的最大GPS点索引
	 * @param entranceGPS	入口处停车等待时时刻最大GPS点
	 * @param exitGPS	出口处停车等待时时刻最大GPS点
	 * @param linkStartGPSIndex	非零GPS(eliminateZeroSpeedGPSDataArrayList)点集合中，当前路段上第一个GPS点的下标
	 * @param linkEndeGPSIndex	非零GPS(eliminateZeroSpeedGPSDataArrayList)点集合中，当前路段上最后一个GPS点的下标
	 * @param curBeginPoint	路段起点坐标
	 * @param travelTime	通行时间
	 * @param curLinkBeginPointTimeStr
	 */
	public String timeInterpolateEntranceProcess(ArrayList<TaxiGPS> taxiGPSArrayList, int endpointEntranceIndex, TaxiGPS entranceGPS, 
			int linkStartGPSIndex, int targetLinkID, MapMatchNode curBeginPoint, MapMatchEdge targetEdge){
		String curLinkBeginPointTimeStr = "";
		try {
			int gpsCount = taxiGPSArrayList.size();
			//路口有信号灯导致停车现象
			//p2点速度为零，停车等待
			//若入口最后一个停车点存在，则根据索引找到下一个GPS点
			if (endpointEntranceIndex != -1) {
				//纠正过的GPS点	入口处GPS点是否在定位偏差圆内
				if (entranceGPS.getIsGPSCorrected()) {						
					//在定位偏差圆内						
					if (isCorrectedGPSPointInLocationErrorCircle(entranceGPS, curBeginPoint)) {
						//如果下一点速度不为零，则用该点速度以及时间反推经过路口时间
						String p2TimStr = entranceGPS.getLocalTime();
						int index = endpointEntranceIndex;
						if (index + 1 < taxiGPSArrayList.size()) {
							TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
							double p4Speed = p4TaxiGPS.getSpeed();
							String p4timeStr = p4TaxiGPS.getLocalTime();
							//纠正过
							if (p4TaxiGPS.getIsGPSCorrected() && p4TaxiGPS.getBelongLineID() == targetLinkID) {
								if (p4Speed > PubParameter.zeroSpeedThreshold) {									
									MapMatchNode p4correctTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p4TaxiGPS);
									double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curBeginPoint, p4correctTaxiNode, targetEdge);
									double timeInterval = 2 * tempDistance/p4Speed;
									double approximateTimeInterval = Math.round(timeInterval); 
									curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p4timeStr, (int)approximateTimeInterval);
									if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
										curLinkBeginPointTimeStr = "";
									}
								}
								//速度为零
								else {
									if (index + 2 < taxiGPSArrayList.size()) {
										TaxiGPS p5TaxiGPS = taxiGPSArrayList.get(index + 2);
										if (p5TaxiGPS.getIsGPSCorrected() && p5TaxiGPS.getBelongLineID() == targetLinkID) {
											double p5Speed = p5TaxiGPS.getSpeed();
											if (p5Speed > PubParameter.zeroSpeedThreshold) {
												MapMatchNode p4correctTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p4TaxiGPS);
												double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curBeginPoint, p4correctTaxiNode, targetEdge);
												double timeInterval = 2 * tempDistance/p5Speed;
												double approximateTimeInterval = Math.round(timeInterval);
												curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p4timeStr, (int)approximateTimeInterval);
												if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
													curLinkBeginPointTimeStr = "";
												}
											}
										}										
									}									
								}
							}							
						}						
					}
					//不在定位偏差圆内，而在交叉口区域
					else {
						//如果p3速度为零，则认为拥堵，否则匀减速后匀加速运动
						int index = endpointEntranceIndex;
						String p2TimStr = entranceGPS.getLocalTime();
						if (index + 1 < gpsCount) {
							TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
							//纠正过
							if (p3TaxiGPS.getIsGPSCorrected() && p3TaxiGPS.getBelongLineID() == targetLinkID) {
								double p3speed = p3TaxiGPS.getSpeed();
								//拥堵,在继续找p4点
								if (p3speed < PubParameter.zeroSpeedThreshold) {
									if (index + 2 < taxiGPSArrayList.size()) {
										TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 2);
										if (p4TaxiGPS.getIsGPSCorrected() && p4TaxiGPS.getBelongLineID() == targetLinkID) {
											double p4speed = p4TaxiGPS.getSpeed();
											if (p4speed > PubParameter.zeroSpeedThreshold) {
												String timeStr = p4TaxiGPS.getLocalTime();
												MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
												double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curBeginPoint, p3correctedTaxiNode, targetEdge);
												double timeInterval = 2 * tempDistance/p4speed;
												double approximateTimeInterval = Math.round(timeInterval);
												curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(timeStr, (int)approximateTimeInterval);
												if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
													curLinkBeginPointTimeStr = "";
												}												
											}											
										}																				
									}
								}
								//匀加速
								else {
									String timeStr = p3TaxiGPS.getLocalTime();
									MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
									double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curBeginPoint, p3correctedTaxiNode, targetEdge);
									double timeInterval = 2 * tempDistance/p3speed;
									double approximateTimeInterval = Math.round(timeInterval);
									curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(timeStr, (int)approximateTimeInterval);
									if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
										curLinkBeginPointTimeStr = "";
									}
								}
							}							
						}
					}
				}	
			}
			//若入口不存在停车点,根据GPS点的所属路段ID,确定当前路段上符合条件的第一个GPS点的索引
			//并且要保证此两个GPS点位于路段入口处端点的两侧，目的是去除出租车在路段入口处的等待时间
			//p2点速度不为零
			else {
				TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(linkStartGPSIndex);
				double p3speed = p3TaxiGPS.getSpeed();
				String p3TimeStr = p3TaxiGPS.getLocalTime();
				if (linkStartGPSIndex - 1 >= 0) {
					TaxiGPS p2TaxiGPS = taxiGPSArrayList.get(linkStartGPSIndex - 1);
					String p2TimStr = p2TaxiGPS.getLocalTime();
					double p2Speed = p2TaxiGPS.getSpeed();			
					if (p3TaxiGPS.getIsGPSCorrected() && p3TaxiGPS.getBelongLineID() == targetLinkID) {
						MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
						double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curBeginPoint, p3correctedTaxiNode, targetEdge);
						//p3点速度不为零,匀速行驶
						if (p3speed > PubParameter.zeroSpeedThreshold) {						
							double timeInterval = tempDistance/p3speed;
							double approximateTimeInterval = Math.round(timeInterval); 
							curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3TimeStr, (int)approximateTimeInterval);
							//插值时间不在p2点之后，用平均速度重新计算
							if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
								double tMeanSpeed = (p2Speed + p3speed)/2;								
								timeInterval = tempDistance/tMeanSpeed;
								approximateTimeInterval = Math.round(timeInterval); 
								curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3TimeStr, (int)approximateTimeInterval);
								if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
									curLinkBeginPointTimeStr = "";
								}
							}
						}
						//p3点速度为零,匀减速
						else {
							double timeInterval = 2 * tempDistance/p2Speed;
							double approximateTimeInterval = Math.round(timeInterval); 
							curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3TimeStr, (int)approximateTimeInterval);
							if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkBeginPointTimeStr)) {
								curLinkBeginPointTimeStr = "";
							}
						}			
					}
				}				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
		return curLinkBeginPointTimeStr;
	}
	
	
	public String timeInterpolateExitProcess(ArrayList<TaxiGPS> taxiGPSArrayList, int endpointExitIndex, TaxiGPS exitGPS, 
			int linkEndGPSIndex, int targetLinkID, MapMatchNode curEndPoint, MapMatchEdge targetEdge){
		String curLinkEndPointTimeStr = "";
		try {
			int gpsCount = taxiGPSArrayList.size();
			//路口有信号灯导致停车现象
			//p2点速度为零，停车等待
			//若入口最后一个停车点存在，则根据索引找到下一个GPS点
			if (endpointExitIndex != -1) {
				//纠正过的GPS点	入口处GPS点是否在定位偏差圆内
				if (exitGPS.getIsGPSCorrected()) {						
					//在定位偏差圆内
					if (isCorrectedGPSPointInLocationErrorCircle(exitGPS, curEndPoint)) {
						String p2TimStr = exitGPS.getLocalTime();
						//如果下一点速度不为零，则用该点速度以及时间反推经过路口时间
						int index = endpointExitIndex;
						if (index + 1 < taxiGPSArrayList.size()) {
							TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
							double p4Speed = p4TaxiGPS.getSpeed();
							String p4timeStr = p4TaxiGPS.getLocalTime();
							//纠正过		目标路段邻接路段
							if (p4TaxiGPS.getIsGPSCorrected() && p4TaxiGPS.getBelongLineID() != targetLinkID) {
								//不为零		匀加速
								if (p4Speed > PubParameter.zeroSpeedThreshold) {									
									MapMatchNode p4correctTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p4TaxiGPS);
									double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curEndPoint, p4correctTaxiNode, targetEdge);
									double timeInterval = 2 * tempDistance/p4Speed;
									double approximateTimeInterval = Math.round(timeInterval); 
									curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p4timeStr, (int)approximateTimeInterval);
									if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
										curLinkEndPointTimeStr = "";
									}
								}
								//速度为零 	拥堵
								else {
									if (index + 2 < taxiGPSArrayList.size()) {
										TaxiGPS p5TaxiGPS = taxiGPSArrayList.get(index + 2);
										if (p5TaxiGPS.getIsGPSCorrected() && p5TaxiGPS.getBelongLineID() != targetLinkID) {
											double p5Speed = p5TaxiGPS.getSpeed();
											if (p5Speed > PubParameter.zeroSpeedThreshold) {
												MapMatchNode p4correctTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p4TaxiGPS);
												double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curEndPoint, p4correctTaxiNode, targetEdge);
												double timeInterval = 2 * tempDistance/p5Speed;
												double approximateTimeInterval = Math.round(timeInterval);
												curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p4timeStr, (int)approximateTimeInterval);
												if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
													curLinkEndPointTimeStr = "";
												}
											}
										}										
									}									
								}
							}							
						}						
					}
					//不在定位偏差圆内，而在交叉口区域
					else {
						//如果p3速度为零，则认为拥堵，否则匀减速后匀加速运动
						String p2TimStr = exitGPS.getLocalTime();
						int index = endpointExitIndex;
						if (index + 1 < gpsCount) {
							TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
							//纠正过
							if (p3TaxiGPS.getIsGPSCorrected() && p3TaxiGPS.getBelongLineID() != targetLinkID) {
								double p3speed = p3TaxiGPS.getSpeed();
								//拥堵,在继续找p4点
								if (p3speed < PubParameter.zeroSpeedThreshold) {
									if (index + 2 < taxiGPSArrayList.size()) {
										TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 2);
										if (p4TaxiGPS.getIsGPSCorrected() && p4TaxiGPS.getBelongLineID() != targetLinkID) {
											double p4speed = p4TaxiGPS.getSpeed();
											if (p4speed > PubParameter.zeroSpeedThreshold) {
												String timeStr = p4TaxiGPS.getLocalTime();
												MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
												double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curEndPoint, p3correctedTaxiNode, targetEdge);
												double timeInterval = 2 * tempDistance/p4speed;
												double approximateTimeInterval = Math.round(timeInterval);
												curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(timeStr, (int)approximateTimeInterval);	
												if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
													curLinkEndPointTimeStr = "";
												}
											}											
										}																				
									}
								}
								//匀加速
								else {
									String timeStr = p3TaxiGPS.getLocalTime();
									MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
									double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curEndPoint, p3correctedTaxiNode, targetEdge);
									double timeInterval = 2 * tempDistance/p3speed;
									double approximateTimeInterval = Math.round(timeInterval);
									curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(timeStr, (int)approximateTimeInterval);	
									if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
										curLinkEndPointTimeStr = "";
									}
								}
							}							
						}
					}
				}	
			}
			//若出口不存在停车点,根据GPS点的所属路段ID,确定当前路段上符合条件的最后一个GPS点的索引
			//并且要保证此两个GPS点位于路段出口口处端点的两侧，目的是包含出租车在路段出口处的等待时间
			//p2点速度不为零
			else {
				if (linkEndGPSIndex + 1 < taxiGPSArrayList.size()) {
					TaxiGPS p2TaxiGPS = taxiGPSArrayList.get(linkEndGPSIndex);
					String p2TimStr = p2TaxiGPS.getLocalTime();
					double p2Speed = p2TaxiGPS.getSpeed();
					TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(linkEndGPSIndex + 1);
					double p3speed = p3TaxiGPS.getSpeed();
					String p3timeStr = p3TaxiGPS.getLocalTime();
					if (p3TaxiGPS.getIsGPSCorrected()) {
						MapMatchNode p3correctedTaxiNode = PubClass.ConvertTaxiGPSToCorrectNode(p3TaxiGPS);
						double tempDistance = PubClass.distanceBetweenVertexAndNodeAlongEdge(curEndPoint, p3correctedTaxiNode, targetEdge);
						//p3点速度不为零  匀速行驶
						if (p3speed > PubParameter.zeroSpeedThreshold ) {						
							double timeInterval = tempDistance/p3speed;
							double approximateTimeInterval = Math.round(timeInterval); 
							curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3timeStr, (int)approximateTimeInterval);
							if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
								double meanSpeed = (p2Speed + p3speed)/2;
								timeInterval = tempDistance/meanSpeed;
								approximateTimeInterval = Math.round(timeInterval); 
								curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3timeStr, (int)approximateTimeInterval);
								if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
									curLinkEndPointTimeStr = "";
								}								
							}
						}
						//p3点速度为零,匀减速
						else {
							double timeInterval = 2 * tempDistance/p2Speed;
							double approximateTimeInterval = Math.round(timeInterval); 
							curLinkEndPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3timeStr, (int)approximateTimeInterval);
							if (!PubClass.isTime2AfterTime1(p2TimStr, curLinkEndPointTimeStr)) {
								curLinkEndPointTimeStr = "";
							}
						}			
					}
				}			
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
		return curLinkEndPointTimeStr;
	}

	
	public void obtainCorrespondingIndex(ArrayList<TaxiGPS> taxiGPSArrayList, ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList, 
			int startIndex, int linkStartGPSIndex) {
		try {
			int tempStartIndex = -1;//去掉速度为零的点的GPS点集合中(eliminateZeroSpeedGPSDataArrayList),目标路段的开始、结束索引（速度非零点集合）
			int tempEndIndex = -1;
			//路段入口处
			if (startIndex != -1) {
				String localTimeStr = "";//速度非零点的开始时间
				//此处+1，表示不能取速度为零的GPS点，因为eliminateZeroSpeedGPSDataArrayList都是速度非零的点
				for (int j = startIndex + 1; j < taxiGPSArrayList.size(); j++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(j);
					if (taxiGPS.getSpeed() > PubParameter.zeroSpeedThreshold) {
						localTimeStr = taxiGPS.getLocalTime();
						break;
					}
					else {
						continue;
					}	
				}
				for (int i = 0; i < eliminateZeroSpeedGPSDataArrayList.size(); i++) {
					TaxiGPS tempTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					String tempLocalTimStr = tempTaxiGPS.getLocalTime();
					if (localTimeStr.equals(tempLocalTimStr)) {
						tempStartIndex = i;
						break;
					}
				}
			}
			else {
				tempStartIndex = linkStartGPSIndex;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 返回目标路段第一个GPS点和最后一个GPS索引
	 * @param taxiGPSArrayList
	 * @param linkID	目标路段ID
	 * @param linkStartEndIndex	分别存放开始GPS点索引，最后一个GPS点索引
	 */
	public void obtainLinkStartEndGPSIndex(ArrayList<TaxiGPS> taxiGPSArrayList, int linkID, int []linkStartEndIndex) {
		try {
			int count = taxiGPSArrayList.size();
			int linkStartGPSIndex = -1;
			int linkEndGPSIndex = -1;
			for (int i = 0; i < count; i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				int belongLinkID = taxiGPS.getBelongLineID();
				if (belongLinkID == linkID) {
					linkStartGPSIndex = i;
					break;
				}								
			}	
			if (linkStartGPSIndex != -1) {
				for (int i = linkStartGPSIndex; i < count - 1; i++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
					int belongLinkID = taxiGPS.getBelongLineID();
					if (belongLinkID != linkID) {
						linkEndGPSIndex = i - 1;
						break;
					}			
				}
			}			
			linkStartEndIndex[0] = linkStartGPSIndex;
			linkStartEndIndex[1] = linkEndGPSIndex;	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 目标路段上的轨迹是否都为载客状态
	 * @param taxiGPSArrayList	
	 * @param startIndex	目标路段GPS起点
	 * @param endIndex	目标路段GPS终点
	 * @return
	 */
	public boolean isCarrayPassengerTrack(ArrayList<TaxiGPS> taxiGPSArrayList, int startIndex, int endIndex) {
		boolean isCarrayPassengerTrack = true;
		try {
			for (int i = startIndex; i <= endIndex; i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				if (taxiGPS.getStatus() == 0) {
					isCarrayPassengerTrack = false;
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isCarrayPassengerTrack;
	}
	
	/**
	 * 获得目标路段上GPS点数目
	 * @param taxiGPSArrayList	GPS点数目
	 * @param linkID	路段ID
	 * @return
	 */
	public int obtainTargetLinkGPSCount(ArrayList<TaxiGPS> taxiGPSArrayList, int linkID){
		int count = 0;
		try {
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				int belongLinkID = taxiGPS.getBelongLineID();
				if (belongLinkID == linkID) {
					count ++;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return count;
	}
	
	/**
	 * 判断GPS点是否在定位偏差圆内
	 * @param taxiGPS	出租车GPS点
	 * @param curPoint	路段端点
	 * @return
	 */
	public boolean isCorrectedGPSPointInLocationErrorCircle(TaxiGPS taxiGPS, MapMatchNode curPoint){
		boolean isInLocationErrorCircle = false;
		try {
			double correctlongitude = taxiGPS.getCorrectLongitude();
			double correctlatitude = taxiGPS.getCorrectLatitude();
			MapMatchNode tNode = new MapMatchNode();
			tNode.setX(correctlongitude);
			tNode.setY(correctlatitude);
			if (distance(tNode, curPoint) < PubParameter.locationErrorCircleBufferRadius) {
				isInLocationErrorCircle = true;
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isInLocationErrorCircle;
	}
	
	/**
	 * 判断纠正的GPS点是否在交叉口区域内
	 * @param taxiGPS	纠正GPS点
	 * @param curPoint	交叉口区域以该点为圆心
	 * @return
	 */
	public boolean isGPSCorrectedPointInIntersectionCircle(TaxiGPS taxiGPS, MapMatchNode curPoint){
		boolean GPSCorrectedPointInIntersectionCircle = false;
		try {
			double correctlongitude = taxiGPS.getCorrectLongitude();
			double correctlatitude = taxiGPS.getCorrectLatitude();
			MapMatchNode tNode = new MapMatchNode();
			tNode.setX(correctlongitude);
			tNode.setY(correctlatitude);
			if (distance(tNode, curPoint) < PubParameter.endpointBufferRadius) {
				GPSCorrectedPointInIntersectionCircle = true;
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return GPSCorrectedPointInIntersectionCircle;
	}
	
	/**
	 * 判断GPS点是否已经纠正过
	 * @param taxiGPS	
	 * @return
	 */
	public boolean isGPSPointCorrected(TaxiGPS taxiGPS){
		boolean isGPSPointCorrected = false;
		try {
			double correctlongitude = taxiGPS.getCorrectLongitude();
			double correctlatitude = taxiGPS.getCorrectLatitude();
			if (correctlongitude != -1 && correctlatitude != -1) {
				isGPSPointCorrected = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isGPSPointCorrected;
	}
	
	/*时间插值计算：根据目标路段端点处速度恒定假设，在目标路段端点处进行时间插值计算
	 * （插值计算还是有问题）
	 * 输入：
	 * taxiGPSArrayList：GPS轨迹
	 * targetLinkNodeArrayList：目标路段点集合
	 * linkID：目标路段ID
	 * linkDirection：路段的方向性，1表示与当前taxi运动方向一致，-1表示相反
	 * interpolateStartTime：返回路段端点首点插值后的时刻，(相对于当前出租车taxi)
	 * travelTime：当前出租车的路段通行时间
	 * GPSTravelArrayList：目标路段上的GPS点路径
	 * 输出：
	 * interpolateStartTime：GPS点刚进入路段的插值时间
	 * travelTime：GPS点通过路段的时间,单位为秒
	 * meanSpeed：路段平均速度
	 * predictMinTime：根据最大速度估计的最小时间
	 * 端点处速度为零的GPS点起到关键作用
	 * 1.目标路段首末GPS点确定：根据GPS的候选路段（GPS速度为零的点没有候选路段）是否包含目标路段，确定目标路段上的第一个GPS点（startGPS）
	 * 		以及最后一个GPS点(endGPS)
	 * 		**目标路段的入口处，做端点缓冲区，半径endpointBufferRadius，
	 * 			if（存在速度为零GPS点）
	 * 				取得速度为零且时间最大的GPS点作为startStartGPS，此点在目标路段相邻的路段上，获得下一GPS点作为startGPS，进行时间插值//说明在交叉点入口处等待信号灯
	 * 			else
	 * 				（按一般方法进行时间插值）
	 * 		**目标路段的出口处，做端点缓冲区半径endpointBufferRadius
	 * 			if（存在速度为零GPS点）
	 * 				取得速度为零且时间最大的GPS点作为endGPS，此点在目标路段上，获得下一GPS点作为endEndGPS，进行时间插值	//说明在交叉点出口处等待信号灯
	 * 			else
	 * 				（按一般方法进行时间插值）
	 * 
	 * 2.临接路段GPS点确定：确定与目标路段上首末GPS点相连的GPS点，
	 * 		设与startGPS点相连的点为startStartGPS,与endGPS点相连的点为endEndGPS
	 * 3.时间插值计算：（路段首末点相对于当前出租车而言）
	 * 	确定出租车通过当前路段首点时刻：设路段当前起点为curBeginPoint，startstartGPS到beginPoint的距离为distSStartBegin，
	 * 		时间为timeSStartCurBegin，startGPS到curBeginPoint的距离为distStartCurBegin，时间为timeStartCurBegin，
	 * 		startStartGPS与startGPS点间的时间为tempTime,根据速度平均的假设，beginPoint到startGPS的时间为：
	 * 		timeStartBegin = (distStartBegin/(distSStartBegin + distStartBegin))*tempTime;
	 * 		据此确定出租车通过路段起点的时刻（此处路段端点相对于出租车而言）
	 *  确定出租车通过当前路段末点时刻：设路段终点为endPoint
	 *  
	 *  2014/11/06
	 *  根据所在路段上的GPS点，求路段上GPS点的最大速度
	 *  由最大速度求路段最小通行时间，根据最小通行时间判断求得的通行时间是否合理
	 *  */
	public void timeInterpolateCalculateAccordMean(ArrayList<TaxiGPS> taxiGPSArrayList, MapMatchEdge targetEdge, int linkID, int linkDirection,
			String[]interpolateStartTime, double[] travelTime, ArrayList<MapMatchNode> GPSTravelArrayList, double[] meanSpeed){
		try {
			String curLinkBeginPointTimeStr = "";//当前路段起点时间
			String curLinkEndPointTimeStr = "";//当前路段终点时间
			int linkStartGPSIndex = 0;//非零GPS(eliminateZeroSpeedGPSDataArrayList)点集合中，当前路段上第一个GPS点的下标
			int linkEndGPSIndex = 0;//非零GPS点集合中，当前路段上最后一个GPS点的下标
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();//去掉速度为零的点
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//去掉速度为零的GPS点,因为速度为零的点没有候选道路集
			int nonZeroCount = eliminateZeroSpeedGPSDataArrayList.size();//速度非零点的数目
			//2014-09-24修改
			//出租车在路段入口处GPS点确定
			//如果入口有停车情况，获得最后一个停车点
			TaxiGPS entranceGPS = new TaxiGPS();//路段入口处速度为零且时间最大(位于后面的时间)的GPS点
			TaxiGPS exitGPS = new TaxiGPS();//路段出口处速度为零且时间最大的GPS点
			MapMatchNode curBeginPoint = new MapMatchNode();//路段起点
			curBeginPoint.setNodeID(-1);//为判断curBeginPoint是否落在两点之间
			MapMatchNode curEndPoint = new MapMatchNode();//路段终点
			curEndPoint.setNodeID(-1);
			//获得路段入口、出口处速度为零且时间最大(位于后面的时间)的GPS点
			int[] endpointIndexArray = obtainZeroSpeedMaxiTimePointAtEntranceExit(taxiGPSArrayList, entranceGPS, exitGPS, targetEdge, linkID, linkDirection, curBeginPoint, curEndPoint);
			//路段入口判断
			//路口有信号灯导致停车现象
			//若入口最后一个停车点存在，则根据索引找到下一个GPS点
			if (endpointIndexArray[0] != -1) {
				int index = endpointIndexArray[0];
				if (index + 1 < taxiGPSArrayList.size()) {
					TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
					curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(entranceGPS, nextTaxiGPS, curBeginPoint);//路段起始端点对应时间				
				}
			}
			//若入口不存在停车点,此时用去掉速度为零的GPS点（eliminateZeroSpeedGPSDataArrayList）求时间,因为这样的点才有候选道路
			//确定当前路段上符合条件的第一个GPS点的索引,符合条件的GPS点候选路段包含目标路段并且下一个GPS点候选路段也包含目标路段
			//并且要保证此两个GPS点位于路段入口处端点的两侧，目的是去除出租车在路段入口处的等待时间
			else {
				for (int i = 0; i < nonZeroCount; i++) {
					if (i != nonZeroCount - 1) {
						TaxiGPS curTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
						TaxiGPS nextTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i + 1);
						ArrayList<MapMatchEdge> curCandidateEdgeSetArrayList = curTaxiGPS.getCandidateEdgeSetArrayList();
						ArrayList<MapMatchEdge> nextCandidateEdgeSetArrayList = nextTaxiGPS.getCandidateEdgeSetArrayList();
						if (isEdgeArraylistContainEdgeID(curCandidateEdgeSetArrayList, linkID) &&
								isEdgeArraylistContainEdgeID(nextCandidateEdgeSetArrayList, linkID)) {						
							linkStartGPSIndex = i;
							break;
						}
					}								
				}
				//判断当前路段是否存在与第一个GPS点直接相邻的上一个GPS点，以及当前路段最后一个GPS点直接相邻的下一个GPS点
				//路段首点时间插值,如果当前路段第一个GPS点的索引为0，则不能进行时间插值计算
				//同样，路段首点插值分两种情况1.首点在目标路段上；2.首点在目标路段的邻接路段上
				//在路段入口处进行前向和后向时间插值计算，思路与路段末点插值类似
				if (linkStartGPSIndex != 0) {
					TaxiGPS startTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
					TaxiGPS startStarTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex - 1);//直接相邻的上一个GPS点
					MapMatchNode tStartNode = new MapMatchNode();
					MapMatchNode tStartStartNode = new MapMatchNode();
					tStartNode.setX(startTaxiGPS.getLongitude());
					tStartNode.setY(startTaxiGPS.getLatitude());
					tStartStartNode.setX(startStarTaxiGPS.getLongitude());
					tStartStartNode.setY(startStarTaxiGPS.getLatitude());	
					//路段端点curBeginPoint位于两GPS点之间
					if (isProjPointBetweenSE(tStartStartNode, tStartNode, curBeginPoint)) {
						curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(startStarTaxiGPS, startTaxiGPS, curBeginPoint);//插值计算路段端点对应时间
					}
					//插值时间为空，则不能进行前向时间插值，进行后向时间插值
					if (curLinkBeginPointTimeStr.equals("")) {
						if (linkStartGPSIndex != nonZeroCount - 1) {
							TaxiGPS tStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
							TaxiGPS tNextStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex + 1);//后向GPS点
							MapMatchNode tStartTaxiGPSNode = new MapMatchNode();
							MapMatchNode tNextStartTaxiGPSNode = new MapMatchNode();
							tStartTaxiGPSNode.setX(tStartTaxiGPS.getLongitude());
							tStartTaxiGPSNode.setY(tStartTaxiGPS.getLatitude());
							tNextStartTaxiGPSNode.setX(tNextStartTaxiGPS.getLongitude());
							tNextStartTaxiGPSNode.setY(tNextStartTaxiGPS.getLatitude());
							//路段端点curBeginPoint位于两GPS点之间
							if (isProjPointBetweenSE(tStartTaxiGPSNode, tNextStartTaxiGPSNode, curBeginPoint)) {
								curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(tStartTaxiGPS, tNextStartTaxiGPS, curBeginPoint);//插值计算路段端点对应时间							
							}							
						}						
					}
				}
			}
			
			//路段出口判断
			//出口处有信号灯导致停车现象
			if (endpointIndexArray[1] != -1) {
				int index = endpointIndexArray[1];
				if (index + 1 < taxiGPSArrayList.size()) {
					TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(index + 1);//最后一个停车点的下一个GPS点
					curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(exitGPS, nextTaxiGPS, curEndPoint);//路段端点对应时间//路段起始端点对应时间					
				}
			}
			//若不存在
			//此时用去掉速度为零的GPS点集
			else {
				//确定当前路段上符合条件的最后一个GPS点的索引，符合条件的GPS点候选路段包含目标路段并且上一个GPS点候选路段也包含目标路段
				//并且此两个GPS点要位于路段出口处端点的两侧，目的是保证进入路段的出租车能够通过出口去除那些在路段中途折回的出租车轨迹
				for (int i = nonZeroCount - 1; i >= 0; i--) {
					if (i != 0) {
						TaxiGPS curTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
						TaxiGPS preTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i - 1);
						ArrayList<MapMatchEdge> curCandidateEdgeSetArrayList = curTaxiGPS.getCandidateEdgeSetArrayList();
						ArrayList<MapMatchEdge> preCandidateEdgeSetArrayList = preTaxiGPS.getCandidateEdgeSetArrayList();
						if (isEdgeArraylistContainEdgeID(curCandidateEdgeSetArrayList, linkID) && 
								isEdgeArraylistContainEdgeID(preCandidateEdgeSetArrayList, linkID)) {
							linkEndGPSIndex = i;
							break;
						}
					}		
				}
				//路段末点插值分两种情况1.末点在目标路段上；2.末点在目标路段的邻接路段上
				//1.末点在目标路段上,则向后时间插值
				//如果当前路段最后GPS点的索引为count - 1，则不能进行时间插值计算
				//2.末点在目标路段邻接路段上，向前时间插值
				//首先进行后向时间插值，如果插值为空，则再进行前向时间插值
				//无论向前插值还是向后插值，会自动选择一种时间插值
				if (linkEndGPSIndex != nonZeroCount - 1) {
					TaxiGPS endTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
					TaxiGPS endEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex + 1);
					MapMatchNode tEndNode = new MapMatchNode();
					MapMatchNode tEndEndNode = new MapMatchNode();
					tEndNode.setX(endTaxiGPS.getLongitude());
					tEndNode.setY(endTaxiGPS.getLatitude());
					tEndEndNode.setX(endEndTaxiGPS.getLongitude());
					tEndEndNode.setY(endEndTaxiGPS.getLatitude());
					//2014-11-26修改	
					//路段端点curEndPoint位于两GPS点之间
					if (isProjPointBetweenSE(tEndNode, tEndEndNode, curEndPoint)) {
						curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(endTaxiGPS, endEndTaxiGPS, curEndPoint);//路段端点对应时间
					}	
					//后向不能插值，进行前向时间插值
					if (curLinkEndPointTimeStr.equals("")) {
						if (linkEndGPSIndex != 0) {
							TaxiGPS tEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
							TaxiGPS tBeforeEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex - 1);//前向GPS点
							MapMatchNode tEndTaxiGPSNode = new MapMatchNode();
							MapMatchNode tBeforeEndTaxiGPSNode = new MapMatchNode();
							tEndTaxiGPSNode.setX(tEndTaxiGPS.getLongitude());
							tEndTaxiGPSNode.setY(tEndTaxiGPS.getLatitude());
							tBeforeEndTaxiGPSNode.setX(tBeforeEndTaxiGPS.getLongitude());
							tBeforeEndTaxiGPSNode.setY(tBeforeEndTaxiGPS.getLatitude());
							//路段端点curEndPoint是否位于两GPS点之间
							if (isProjPointBetweenSE(tBeforeEndTaxiGPSNode, tEndTaxiGPSNode, curEndPoint)) {
								curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(tBeforeEndTaxiGPS, tEndTaxiGPS, curEndPoint);//路段端点对应时间
							}				
						}
					}
				}
			}
			/*取得路段上的GPS点，根据heading进行比较，若相邻两点heading间相差180（由于存在误差，限差可以取150），
			 *则认为出租车的运动方向相反，即存在在路段中间掉头现象 
			 * 流程：
			 * 1.如果在路段入口有停车现象，取得入口处GPS点，并在 eliminateZeroSpeedGPSDataArrayList中取得相应的GPS索引，此时路段上起点GPS索引为endpointIndexArray[0]+1
			 * 	若没有停车现象，则直接取得路段上起点GPS索引linkStartGPSIndex
			 * 2.如果在路段出口有停车现象，取得出口处GPS点，并在 eliminateZeroSpeedGPSDataArrayList中取得相应的GPS索引,此时路段上点最后一个GPS索引为endpointIndexArray[1]
			 * 	若没有停车现象，则直接取得路段上最后一个GPS索引为linkEndGPSIndex
			 * 3.判断此两索引之间的GPS点，相邻两GPS（去掉速度为零的GPS点）点heading之差不大于180（设置为150），若差超出阈值，则存在在路段中间掉头现象 
			 * */
			int tempStartIndex = -1;//去掉速度为零的点的GPS点集合中(eliminateZeroSpeedGPSDataArrayList),目标路段的开始、结束索引（速度非零点集合）
			int tempEndIndex = -1;
			//路段入口处
			if (endpointIndexArray[0] != -1) {
				String localTimeStr = "";//速度非零点的开始时间
				//此处+1，表示不能取速度为零的GPS点，因为eliminateZeroSpeedGPSDataArrayList都是速度非零的点
				for (int j = endpointIndexArray[0] + 1; j < taxiGPSArrayList.size(); j++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(j);
					if (taxiGPS.getSpeed() > PubParameter.zeroSpeedThreshold) {
						localTimeStr = taxiGPS.getLocalTime();
						break;
					}
					else {
						continue;
					}	
				}
				for (int i = 0; i < eliminateZeroSpeedGPSDataArrayList.size(); i++) {
					TaxiGPS tempTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					String tempLocalTimStr = tempTaxiGPS.getLocalTime();
					if (localTimeStr.equals(tempLocalTimStr)) {
						tempStartIndex = i;
						break;
					}
				}
			}
			else {
				tempStartIndex = linkStartGPSIndex;
			}
			//路段出口处,取得速度不为零的GPS点
			if (endpointIndexArray[1] != -1) {
				String localTimeStr = "";
				for (int j = endpointIndexArray[1] - 1; j >= 0; j--) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(j);
					if (taxiGPS.getSpeed() > PubParameter.zeroSpeedThreshold) {
						localTimeStr = taxiGPS.getLocalTime();
						break;
					}
					else {
						continue;
					}					
				}
				for (int i = 0; i < eliminateZeroSpeedGPSDataArrayList.size(); i++) {
					TaxiGPS tempTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					String tempLocalTimStr = tempTaxiGPS.getLocalTime();
					if (localTimeStr.equals(tempLocalTimStr)) {
						tempEndIndex = i;
						break;
					}
				}
			}
			else {
				tempEndIndex = linkEndGPSIndex;
			}
			//相邻GPS点间方向角度的判断，以保证出租车不会中途折返
			if (tempStartIndex != -1 && tempEndIndex != -1 && tempStartIndex < tempEndIndex) {
				boolean isTravelTimeValid = true;//路段通行时间的有效性
				for (int i = tempStartIndex; i < tempEndIndex; i++) {
					TaxiGPS curTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					double curHeading = curTaxiGPS.getHeading();
					TaxiGPS nextTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i + 1);
					double nextHeading = nextTaxiGPS.getHeading();
					double headingDiffer = Math.abs(curHeading - nextHeading);
					if (headingDiffer > PubParameter.headingDifferDownThreshold && headingDiffer < PubParameter.headingDifferUpThreshold) {
						isTravelTimeValid = false;
						break;
					}					
				}
				//2014/11/26修改
				//当目标路段上所有GPS点候选路段包括目标路段时，这样的GPS点求得通行时间才是有效通行时间
				//这样去掉两点间有多个相邻路段的情况，以保证GPS点匹配唯一路段
				for (int i = tempStartIndex; i < tempEndIndex; i++) {
					TaxiGPS taxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					ArrayList<MapMatchEdge> tempCandidateEdgeSetArrayList = taxiGPS.getCandidateEdgeSetArrayList();
					if (!isEdgeArraylistContainEdgeID(tempCandidateEdgeSetArrayList, linkID)) {
						isTravelTimeValid = false;
						break;
					}
				}
				
				//不仅在目标路段，而且要在目标路段的相邻路段上检索，取得GPS点的速度最大值
				//从目标路段开始、结束GPS点索引，向前以及向后各推两个节点
				//如果在相邻路段上没有点，则只在目标路段取得GPS点的速度最大值
				double maxSpeed = 0;//取得的最大速度
				double predictMinTime = 0;//根据最大速度估计的最小时间
				if (tempStartIndex - 2 >= 0 && tempEndIndex + 2 < nonZeroCount) {
					maxSpeed = obtainMaxSpeed(eliminateZeroSpeedGPSDataArrayList, tempStartIndex - 2, tempEndIndex + 2 );
				}
				else {
					maxSpeed = obtainMaxSpeed(eliminateZeroSpeedGPSDataArrayList, tempStartIndex, tempEndIndex);
				}
				if (maxSpeed != 0) {
					double targetEdgeLength = targetEdge.getEdgeLength();
					String predictMinTimeStr = String.format("%.2f", targetEdgeLength/maxSpeed);//保留小数点后两位，并进行四舍五入
					predictMinTime = Double.parseDouble(predictMinTimeStr);//估计最小时间
				}	
				
				//如果为有效通行时间
				if (isTravelTimeValid) {					
					if (!curLinkBeginPointTimeStr.equals("") && !curLinkEndPointTimeStr.equals("")) {
						travelTime[0] = PubClass.obtainTimeInterval(curLinkBeginPointTimeStr, curLinkEndPointTimeStr);
						interpolateStartTime[0] = curLinkBeginPointTimeStr;
						//求得通行时间大于估计的最小时间才能保证求得通行时间的正确性
						if (travelTime[0] > predictMinTime) {
							//获得taxiGPSArrayList中对应linkStartGPSIndex、linkEndGPSIndex的开始、结束索引
							TaxiGPS linkStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
							String linkStartTimeStr = linkStartTaxiGPS.getLocalTime();
							TaxiGPS linkEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
							String linkEndTimeStr = linkEndTaxiGPS.getLocalTime();
							int startIndex = -1;//taxiGPSArrayList的所在路段的开始、结束索引
							int endIndex = -1;
							boolean startValid = false;
							boolean endValid = false;
							for (int i = 0; i < taxiGPSArrayList.size(); i++) {
								TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
								String timeStr = taxiGPS.getLocalTime();
								if (linkStartTimeStr.equals(timeStr)) {
									startIndex = i;
									startValid = true;
									continue;
								}
								if (linkEndTimeStr.equals(timeStr)) {
									endIndex = i;
									endValid = true;
									continue;
								}
								if (startValid && endValid) {
									break;
								}
							}
							//目标路段上的GPS点，从taxiGPSArrayList（包括速度为零的GPS点）中获得
							//并求平均速度
							double totalSpeed = 0;
							for (int i = startIndex; i <= endIndex; i++) {
								TaxiGPS tempTaxiGPS = taxiGPSArrayList.get(i);
								double speed = tempTaxiGPS.getSpeed();
								totalSpeed = totalSpeed + speed;
								MapMatchNode tNode = new MapMatchNode();
								tNode.setX(tempTaxiGPS.getLongitude());
								tNode.setY(tempTaxiGPS.getLatitude());
								GPSTravelArrayList.add(tNode);
							}
							int calCount = (endIndex - startIndex + 1);
							if (calCount != 0) {
								String meanSpeedStr = String.format("%.2f", totalSpeed/calCount);//保留小数点后两位，并进行四舍五入
								meanSpeed[0] = Double.parseDouble(meanSpeedStr);
							}
						}
						else {
							System.out.print("通行时间：" + travelTime[0] + "不符合实际情况!");
						}	
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
	 * 在GPS点集合taxiGPSArrayList中获得速度最大点
	 * @param taxiGPSArrayList
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public double obtainMaxSpeed(ArrayList<TaxiGPS> taxiGPSArrayList, int startIndex, int endIndex){
		double maxSpeed = 0;
		try {
			for (int i = startIndex; i <= endIndex; i++) {
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				double tempSpeed = taxiGPS.getSpeed();
				if (tempSpeed >= maxSpeed) {
					maxSpeed = tempSpeed;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return maxSpeed;
	}
	
	/*获得路段入口、出口处速度为零且时间最大(位于后面的时间)的GPS点
	 * 返回数组，第一个值入口处GPS点索引、第二个值出口处GPS点索引，-1表示没有找到路口停车点
	 * taxiGPSArrayList:GPS轨迹点
	 * entranceGPS：入口处速度为零且时间最大的GPS点
	 * exitGPS：出口处速度为零（小于一定阈值）且时间最大的GPS点
	 * MapMatchEdge：目标路段
	 * linkDirection：GPS点与路段方向性关系
	 * */
	public int[] obtainZeroSpeedMaxiTimePointAtEntranceExit(ArrayList<TaxiGPS> taxiGPSArrayList, TaxiGPS entranceGPS, TaxiGPS exitGPS,
			MapMatchEdge targetEdge, int linkID, int linkDirection, MapMatchNode curBeginPoint, MapMatchNode curEndPoint){
		int[] indexArray = new int[2];
		indexArray[0] = -1;
		indexArray[1] = -1;
		try {
			ArrayList<TaxiGPS> entranceGPSArrayList = new ArrayList<TaxiGPS>();
			ArrayList<TaxiGPS> exitGPSArrayList = new ArrayList<TaxiGPS>();
			int count = taxiGPSArrayList.size();
			//同向
			if (linkDirection == 1) {
				curBeginPoint.setX(targetEdge.getBeginPoint().getX());
				curBeginPoint.setY(targetEdge.getBeginPoint().getY());
				curEndPoint.setX(targetEdge.getEndPoint().getX());
				curEndPoint.setY(targetEdge.getEndPoint().getY());
			}
			else {
				curBeginPoint.setX(targetEdge.getEndPoint().getX());
				curBeginPoint.setY(targetEdge.getEndPoint().getY());
				curEndPoint.setX(targetEdge.getBeginPoint().getX());
				curEndPoint.setY(targetEdge.getBeginPoint().getY());
			}
			for (int i = 0; i < count; i++) {
				//用纠正的GPS点
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				MapMatchNode node = new MapMatchNode();
				node.setX(taxiGPS.getCorrectLongitude());
				node.setY(taxiGPS.getCorrectLatitude());
				//入口处速度为零的GPS点(可能存在速度误差)
				if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold && PubClass.distance(curBeginPoint, node) < PubParameter.endpointBufferRadius ) {
					entranceGPSArrayList.add(taxiGPS);
				}
				//出口处速度为零的GPS点
				if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold && PubClass.distance(curEndPoint, node) < PubParameter.endpointBufferRadius) {
					exitGPSArrayList.add(taxiGPS);
				}					
			}
			//获得入口出口处速度为零且时间最大的GPS点
			//入口处
			if (entranceGPSArrayList.size() != 0) {
				if (entranceGPSArrayList.size() == 1) {
					TaxiGPS taxiGPS = entranceGPSArrayList.get(0);
					entranceGPS.setLongitude(taxiGPS.getCorrectLongitude());
					entranceGPS.setLatitude(taxiGPS.getCorrectLatitude());
					entranceGPS.setLocalTime(taxiGPS.getLocalTime());
					entranceGPS.setHeading(taxiGPS.getHeading());
				}
				else {
					String maxTimeStr = entranceGPSArrayList.get(0).getLocalTime();//最大时间
					int index = 0;//最大时间索引
					for (int i = 1; i < entranceGPSArrayList.size(); i++) {
						TaxiGPS taxiGPS = entranceGPSArrayList.get(i);
						String taxiTimeStr = taxiGPS.getLocalTime();
						if (PubClass.isTime2AfterTime1(maxTimeStr, taxiTimeStr)) {
							maxTimeStr = taxiTimeStr;
							index = i;
						}						
					}
					TaxiGPS taxiGPS = entranceGPSArrayList.get(index);
					entranceGPS.setLongitude(taxiGPS.getLongitude());
					entranceGPS.setLatitude(taxiGPS.getLatitude());
					entranceGPS.setLocalTime(taxiGPS.getLocalTime());
					entranceGPS.setHeading(taxiGPS.getHeading());
				}
			}
			//出口处
			if (exitGPSArrayList.size() != 0) {
				if (exitGPSArrayList.size() == 1) {
					TaxiGPS taxiGPS = exitGPSArrayList.get(0);
					exitGPS.setLongitude(taxiGPS.getLongitude());
					exitGPS.setLatitude(taxiGPS.getLatitude());
					exitGPS.setLocalTime(taxiGPS.getLocalTime());
					exitGPS.setHeading(taxiGPS.getHeading());
				}
				else {
					String maxTimeStr = exitGPSArrayList.get(0).getLocalTime();//最大时间
					int index = 0;//最大时间索引
					for (int i = 1; i < exitGPSArrayList.size(); i++) {
						TaxiGPS taxiGPS = exitGPSArrayList.get(i);
						String taxiTimeStr = taxiGPS.getLocalTime();
						if (PubClass.isTime2AfterTime1(maxTimeStr, taxiTimeStr)) {
							maxTimeStr = taxiTimeStr;
							index = i;
						}						
					}
					TaxiGPS taxiGPS = exitGPSArrayList.get(index);
					exitGPS.setLongitude(taxiGPS.getLongitude());
					exitGPS.setLatitude(taxiGPS.getLatitude());
					exitGPS.setLocalTime(taxiGPS.getLocalTime());
					exitGPS.setHeading(taxiGPS.getHeading());
				}
			}
			//获得入口处、出口处GPS停车的索引
			if (entranceGPS.getLongitude() != 0) {
				for (int i = 0; i < taxiGPSArrayList.size(); i++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
					String taxiTimeStr = taxiGPS.getLocalTime();
					if (entranceGPS.getLocalTime().equals(taxiTimeStr)) {
						indexArray[0] = i;
						break;
					}
				}
			}
			else {
				indexArray[0] = -1;
			}
			//出口停车存在
			if (exitGPS.getLongitude() != 0) {
				for (int i = 0; i < taxiGPSArrayList.size(); i++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
					String taxiTimeStr = taxiGPS.getLocalTime();
					//出口停车存在
					if (exitGPS.getLocalTime().equals(taxiTimeStr)) {
						indexArray[1] = i;
						break;
					}
				}
			}
			else {
				indexArray[1] = -1;
			}	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return indexArray;
	}
	
	/*获得时间插值
	 * startTaxiGPS：起始GPS点，已知时刻,对应着startStartGPS
	 * endTaxiGPS：终点GPS点，已知时刻,对应着startGPS
	 * middleNode：对应着路段端点，介于startTaxiGPS，endTaxiGPS之间，求其对应的时刻*/
	public String obtainTimeMomentAccordTimeInterpolate(TaxiGPS startTaxiGPS, TaxiGPS endTaxiGPS, MapMatchNode middleNode){
		String timeStr = "";
		try {
			MapMatchNode startNode = new MapMatchNode();
			startNode.setX(startTaxiGPS.getLongitude());
			startNode.setY(startTaxiGPS.getLatitude());
			String startGPSTimeStr = startTaxiGPS.getLocalTime();
			MapMatchNode endNode = new MapMatchNode();
			endNode.setX(endTaxiGPS.getLongitude());
			endNode.setY(endTaxiGPS.getLatitude());
			String endGPSTimeStr = endTaxiGPS.getLocalTime();
			double startEndGPSInverval = PubClass.obtainTimeInterval(startGPSTimeStr, endGPSTimeStr);
			double startMiddDistance = distance(startNode, middleNode);
			double endMiddDistance = distance(endNode, middleNode);
			double endMiddInterval = (endMiddDistance/(startMiddDistance + endMiddDistance)) * startEndGPSInverval;
			long temp = Math.round(endMiddInterval);
			int endMiddIntInberval = (Integer)Math.round(temp);		
			String[] startTimeArray = new String[1];
			PubClass.obtainStartTimeAccordEndTime(endGPSTimeStr, endMiddIntInberval, startTimeArray);
			timeStr = startTimeArray[0];		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeStr;
	}
	
	/**
	 * 求出租车行驶方向与路段方向之间的关系
	 * 若出租车行驶方向与路段起终点方向向量之间夹角小于90°，认为同向，返回1；
	 * 反向，返回-1；
	 * 没有获得方向关系，则返回0；
	 * @param targetEdge
	 * @param GPSArrayList
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public int obtainDirectionBetweenTaxiAndLink(MapMatchEdge targetEdge, ArrayList<TaxiGPS> GPSArrayList, int startIndex, int endIndex){
		int direction = 0;
		try {
			//目标路段GPS点多余一个
			if (startIndex < endIndex) {
				
			}
			else {
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return 0;
	}
	
	/************************************************************
	 * arraylist中是否包含某元素的函数
	 * **********************************************************/
	/*arraylist1中元素EID是否包含arraylist2中的所有元素EID
	 * 若包含，则返回true
	 * 否则，返回false*/
	public boolean isArraylistContainsArraylist(ArrayList<Integer[]> arraylist1, ArrayList<Integer[]> arraylist2){
		boolean isContain = true;
		ArrayList<Integer> convertArraylist1 = new ArrayList<Integer>();
		for (int i = 0; i < arraylist1.size(); i++) {
			convertArraylist1.add(arraylist1.get(i)[0]);
		}
		ArrayList<Integer> convertArraylist2 = new ArrayList<Integer>();
		for (int i = 0; i < arraylist2.size(); i++) {
			convertArraylist2.add(arraylist2.get(i)[0]);
		}
		for (int i = 0; i < convertArraylist2.size(); i++) {
			int t2 = convertArraylist2.get(i);
			if (!convertArraylist1.contains(t2)) {
				isContain = false;
				break;
			}
		}
		return isContain;
	}
	
	/*EdgeArraylist中是否包含该edge
	 * 根据edgeID判断是否为同一个edge*/
	public boolean isEdgeArraylistContainEdge(ArrayList<MapMatchEdge> edgeArraylist, MapMatchEdge edge){
		boolean isContains = false;
		try {
			for (int i = 0; i < edgeArraylist.size(); i++) {
				MapMatchEdge tEdge = edgeArraylist.get(i);
				if (tEdge.getEdgeID() == edge.getEdgeID()) {
					isContains = true;
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isContains = false;
		}
		return isContains;
	}
	
	/*判断EdgeArraylist中是否包含该edgeID
	 * 若包含，返回true
	 * 否则，返回false*/
	public boolean isEdgeArraylistContainEdgeID(ArrayList<MapMatchEdge> edgeArraylist, int edgeID){
		boolean isContains = false;
		try {
			if (edgeArraylist != null) {
				for (int i = 0; i < edgeArraylist.size(); i++) {
					MapMatchEdge tEdge = edgeArraylist.get(i);
					if (tEdge.getEdgeID() == edgeID) {
						isContains = true;
						break;
					}
				}
			}		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isContains = false;
		}
		return isContains;
	}
	
	/*arraylist1中元素EID是否包含某一EID
	 * 若包含，则返回该EID的方向关系1，或-1
	 * 否则，返回0*/	
	public int isArraylistContainsEID(ArrayList<Integer[]> arraylist1, int EID){
		int direRela = 0;
		try {
			if (arraylist1 != null && arraylist1.size() != 0) {
				for (int i = 0; i < arraylist1.size(); i++) {
					int tEID = arraylist1.get(i)[0];
					if (tEID == EID) {
						direRela = arraylist1.get(i)[1];
						break;
					}
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
		return direRela;
	}
	
	/**
	 * 根据路径以及当前路段ID获得路径中与当前路段ID相邻的下一路段ID
	 * @param arraylist1	路径
	 * @param EID	路径ID
	 * @return
	 */
	public MapMatchEdge obtainNextEdgeAccordToCurrentLinkID(ArrayList<Integer[]> arraylist1, int EID ) {		
		MapMatchEdge nextMapMatchEdge = null;
		try {						
			if (arraylist1 != null && arraylist1.size() != 0) {
				int targetLinkIDIndex = -1;
				int nextLinkID = -1;
				for (int i = 0; i < arraylist1.size(); i++) {
					int tEID = arraylist1.get(i)[0];
					if (tEID == EID) {
						targetLinkIDIndex = i;
						break;
					}
				}
				if (targetLinkIDIndex != -1) {
					for (int i = targetLinkIDIndex; i < arraylist1.size(); i++) {
						int tEID = arraylist1.get(i)[0];
						if (tEID != EID) {
							nextLinkID = tEID;
							break;
						}
					}
					if (nextLinkID != -1) {
						nextMapMatchEdge = obtainTargetEdge(nextLinkID, MapMatchAlgorithm.instance().polylineCollArrayList);//获得目标路段
					}					
				}			
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();			
		}
		return nextMapMatchEdge;
	}
	
	/*判断出租车ID是否在eligibleTaxiIDArrayList中，并选择时间最小的出租车ID
	 * */
	public boolean isContainsTaxiID(ArrayList<TaxiGPS> taxiArrayList, TaxiGPS taxiGPS){
		boolean isOK = false;
		try {
			for (int i = 0; i < taxiArrayList.size(); i++) {
				TaxiGPS tempTaxiGPS = taxiArrayList.get(i);
				String tempTaxiIDStr = tempTaxiGPS.getTargetID();
				if (tempTaxiIDStr.equals(taxiGPS.getTargetID())) {
					isOK = true;
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}
		return isOK;	
	}
	
	/**************************************************************
	 * 距离计算相关函数
	 * ************************************************************/	
	/*平面两节点之间的距离*/
	public double distance(MapMatchNode node1, MapMatchNode node2){		
		try {
			double []xy = new double[2];
			coordinateTransToPlaneCoordinate(node1, PubParameter.wuhanL0, xy);		
			double nodex1 = xy[0];
			double nodey1 = xy[1];	
			coordinateTransToPlaneCoordinate(node2, PubParameter.wuhanL0, xy);		
			double nodex2 = xy[0];
			double nodey2 = xy[1];	
			double dis = Math.sqrt(Math.pow(nodex1-nodex2,2)+ Math.pow(nodey1-nodey2,2));
			return dis;
		} catch (Exception e) {
			// TODO: handle exception\
			e.printStackTrace();
			System.out.print(e.getMessage());
			return 999999;
		}		
	}
	
	/*求到平面线段的距离：（求点cNode到startNode与endNode两点组成线段的距离）
	 * 是线段而不是直线，与点到直线的距离不同
	 * 若cNode与对边的两个夹角为锐角，则为投影距离
	 * 若有一角为钝角，则为该点到该钝角点的距离
	 */
	public double distancePointToLineSegment(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode)
	{
		double angleStar = 0;//起点处夹角
		double angleEnd = 0;//终点处夹角
		double seNodeDis = distance(startNode, endNode);
		double distance = 0;
		try {
			if (isTheSameNode(endNode, cNode)){
				angleStar = 0;			
			}
			else if (isTheSameNode(startNode, cNode)) {
				angleEnd = 0;
			}
			else {				
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);	
				double cNodeX = xy[0];
				double cNodeY = xy[1];
				coordinateTransToPlaneCoordinate(startNode, PubParameter.wuhanL0, xy);				
				double starNodeX = xy[0];
				double starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				double endNodeX = xy[0];
				double endNodeY = xy[1];				
				//起终点向量 ，模   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//起点以及与当前点向量，模
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;
				double scNodeDis = distance(startNode, cNode);
				//起终点向量与起点当前点向量 两向量夹角
				angleStar = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//终点以及与当前点向量，模
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//终起点向量与终点当前点向量 两向量夹角
				angleEnd = Math.acos((-seDeltX * ecDeltX + (-seDeltY) * ecDeltY )/(seNodeDis * ecNodeDis));				
			}	
			if (angleStar > Math.PI/2) {
				distance = distance(startNode, cNode);
			}
			else if(angleEnd > Math.PI/2){
				distance = distance(endNode, cNode);
			}
			else {
				distance = distance(startNode, cNode)*Math.sin(angleStar);
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			distance = 999999;
		}
		return distance;
	}
	
	/*点到直线的距离：与点到线段的距离不同*/
	public double distancePointToLine(MapMatchNode starNode, MapMatchNode endNode, MapMatchNode cNode){
		double angle = 0;
		double varSpan = 0;
		try {
			if (endNode.nodeID == cNode.nodeID || starNode.nodeID == cNode.nodeID) {
				angle = 0;
				varSpan = 0;
			}
			else {
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);		
				double cNodeX = xy[0];
				double cNodeY = xy[1];			
				coordinateTransToPlaneCoordinate(starNode, PubParameter.wuhanL0, xy);				
				double starNodeX = xy[0];
				double starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				double endNodeX = xy[0];
				double endNodeY = xy[1];								
				//起终点向量 ，模      起点当前点向量，模
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;				
				//两向量夹角
				angle = Math.acos((seDeltX*scDeltX+seDeltY*scDeltY)/(seNodeDis*scNodeDis));
				varSpan = scNodeDis*Math.sin(angle);
			}			
				return varSpan;
		}
		catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return 99999999;
		}
	}
	
	
}
