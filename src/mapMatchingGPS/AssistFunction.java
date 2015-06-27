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
 * ������������*/
public class AssistFunction {
	
	/*������ؿͼ�¼��GPS����
	 *���ݳ��⳵���ؿ��������ȡ���⳵�ؿ͵Ķ����켣 */
	public void obtainCarryPassengerData(ArrayList<TaxiGPS> taxiGPSArrayList,Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap){
		try {
			ArrayList<TaxiGPS> carrayPassTrack = new ArrayList<TaxiGPS>();//�洢���ؿͼ�¼�ĳ��⳵�켣��      	
        	TaxiGPS ttTaxiGPS = new TaxiGPS();
			ttTaxiGPS = taxiGPSArrayList.get(0);			
			//���׸�����Ϊ�ؿ�״̬
        	if (ttTaxiGPS.status == 262144 || ttTaxiGPS.status == 262145) {
        		carrayPassTrack.add(ttTaxiGPS);
			}
        	int trackNum = 0;//�켣��
			for (int i = 1; i < taxiGPSArrayList.size(); ++i)
			{
				TaxiGPS cTaxiGPS = new TaxiGPS();//��ǰGPS
				TaxiGPS pTaxiGPS = new TaxiGPS();//��һGPS
				cTaxiGPS = taxiGPSArrayList.get(i);
				pTaxiGPS = taxiGPSArrayList.get(i - 1);				
				//�г˿�
				if (cTaxiGPS.status == 262144 || cTaxiGPS.status == 262145)
				{
					carrayPassTrack.add(cTaxiGPS);
					//����켣��Ϊ�ؿ͵㣬����һ���켣
					if (i == taxiGPSArrayList.size()-1) {
						trackNum++;
						carrayPassTrackMap.put(trackNum, carrayPassTrack);
						//��������ָ���µ��ڴ棬Ȼ���ͷ��ڴ�,������ֱ����clear
						carrayPassTrack = new ArrayList<TaxiGPS>();
						carrayPassTrack.clear();
					}	
				}
				//��ǰδ�ؿͣ�����һ��¼�ؿ�
				else if (cTaxiGPS.status == 0 || cTaxiGPS.status == 1)
				{
					if (pTaxiGPS.status == 262144 || pTaxiGPS.status == 262145)
					{
						if (carrayPassTrack.size() > 1)
						{
							trackNum++;
							//trackSetֻ�洢�г˿͵ĳ��⳵�켣��ID����
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
	 * ��GPS�켣��������ֹ����д���:������ֹ��Ϊ������ͬ�ĵ�
	 * ����������ֹ�㣬ֻȡ������ֹ������ʱ��GPS�㣬����¼���������ֹʱ��
	 * @param taxiTrackMap	ԭʼ�켣
	 * @param processTaxiTrackMap	������ֹ�㴦����GPS�켣
	 */
	public void continuousStaticPointProcess(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap) {
		try {
			ArrayList<TaxiGPS> taxiArrayList = taxiTrackMap.get(1);
			ArrayList<TaxiGPS> processArrayList = new ArrayList<TaxiGPS>();
			int num = 0;
			int continuousStaticTime = 0;//������ֹʱ��
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
						currentTaxiGPS.setContinuousStaticTime(continuousStaticTime);//������ֹʱ��
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
						currentTaxiGPS.setContinuousStaticTime(continuousStaticTime);//������ֹʱ��
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
	
	/*ѡ�����GPS��radius��Χ�ڵĵ�·����
	 *���GPS����·���м䣬�����Ϊ��·���ϵ�ͶӰ���룻����ΪGPS�㵽·�����˵����С���� 
	 *carrayPassTrackMap:keyֵ��1��ʼ
	 *˼·��
	 *1.��GPS��Ϊ���ģ����дֹ���.
	 *2.��GPS��Ϊ���ģ��ڴֹ��˵�·���У�Ȧѡradius��Χ�ڵ�·��
	 *2.��GPS�㵽·�ε���С���룬����·��Ϊ�������
	 *3.��GPS�㵽·�εķ���н�*/
	public void obtainCandidateRoadSet(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap,	Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap, double radius){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {
					double startTime = System.nanoTime();
					TaxiGPS tempTaxiGPS = gpsTrackPointArrayList.get(j);
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = new ArrayList<MapMatchEdge>();//��ѡ��·����
					Map<Integer, Double[]> tdistScoreSetMap = new HashMap<Integer, Double[]>();//��edgeIDΪ�������洢���������÷�
					MapMatchNode cNode = new MapMatchNode();
					cNode.x = tempTaxiGPS.longitude;
					cNode.y = tempTaxiGPS.latitude;
					cNode.nodeID = -1;
//					ArrayList<MapMatchEdge> coarseFiltPlinesArrayList = new ArrayList<MapMatchEdge>();//�ֹ��˺�ѡ���·������
//					coarseFiltration(tempTaxiGPS, allGridIndexVerticesMap, allGridPolylineMap, coarseFiltPlinesArrayList);//�ֹ���						
					int gridIndex = PubClass.obtainGridIndex(allGridIndexVerticesMap, cNode);
					ArrayList<MapMatchEdge> polylineArrayList = allGridPolylineMap.get(gridIndex);					
					if (polylineArrayList != null) {
						for (int k = 0; k < polylineArrayList.size(); k++) {
							MapMatchEdge tEdge = polylineArrayList.get(k);
							MapMatchNode tbeginNode = new MapMatchNode();
							MapMatchNode tendNode = new MapMatchNode();
							tbeginNode = tEdge.getBeginPoint();
							tendNode = tEdge.getEndPoint();						
							/*�ж�·���Ƿ��������,
							 * ��˵㵽·�ξ��룺�߶ο���Ϊ����
							 *1.������һ���˵��ڻ�������Χ�ڣ����������˵㶼�ڵ����
							 *2.�˵㲻�ڻ�������Χ�ڣ���·�ξ���������,��·����Բ�ཻ������*/
							//������һ���˵��ڻ�������Χ��
							if (isNodeInCircle(cNode, tbeginNode, radius) || isNodeInCircle(cNode, tendNode, radius)) {							
								//������ľ�����ʱ��Ϊ��С����
								double minDis = distance(tbeginNode, cNode);
								Integer edgeID = tEdge.getEdgeID();
								Double []temp = new Double[2];
								temp[0] = minDis;
								tdistScoreSetMap.put(edgeID, temp);
								tempTaxiGPS.setDistScoreSetMap(tdistScoreSetMap);
								//ͶӰ����·�����յ��
								if (isProjPointBetweenSE(tbeginNode, tendNode, cNode)) {
									//��㵽·�ε���С���룺·�ο���Ϊ����								
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
								//ͶӰ����·���ӳ����ϣ������յ��ӳ���
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
							//·����Բ�ཻ�����㣺�˵㲻�ڻ�������Χ�ڣ���·�ξ���������
							//1.���߶�����ֱ�߾���С��radius,�˴��ľ�������߶εĽڵ㼯�ϣ���GPS�㵽ÿһС�εľ��룻
							//2.ͶӰ��λ���߶����˵�֮��
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
					System.out.print("GPS��" + j + ":" + gpsTrackPointArrayList.size() + ":" + tempTaxiGPS.getTargetID() + "��ú�ѡ��·����" + time + "s" + '\n');					
				}		
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}				
	}
	
	/*�ֹ��ˣ��˷���Ч�����ã�:
	 * ��GPS��Ϊ������������,���߶���㡢�յ���дֹ���
	 * ѡ���뻺�����ཻ���߶Σ�����������֮һ����
	 * 1.GPS�㵽�߶����յ����С�ڻ��建�����뾶
	 * 2.�������յ��߶���GPS��Ϊ���ĵ��������ཻ
	 * polylineCollArrayList:Ҫ���дֹ��˵��߶μ���
	 * coarseFiltPlinesArrayList���洢�ֹ��˺���߶�*/
	public void coarseFiltration(TaxiGPS taxiGPS, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap, 
			Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap, ArrayList<MapMatchEdge> coarseFiltPlinesArrayList){
		try {
			MapMatchNode tNode = new MapMatchNode();
			tNode.x = taxiGPS.longitude;
			tNode.y = taxiGPS.latitude;
			double[] GPSxy = new double[2];
			PubClass.coordinateTransToPlaneCoordinate(tNode, PubParameter.wuhanL0, GPSxy);
			Double []leftDown = new Double[]{GPSxy[0] - PubParameter.candRoadCoarseFiltratBuffer/2,GPSxy[1] - PubParameter.candRoadCoarseFiltratBuffer/2};//���½ǵ�
			Double []rightTop = new Double[]{GPSxy[0] + PubParameter.candRoadCoarseFiltratBuffer/2,GPSxy[1] + PubParameter.candRoadCoarseFiltratBuffer/2};//����
			Double []rightDown = new Double[]{leftDown[0], rightTop[1]};//���½ǵ�			
			Double []leftTop = new Double[]{rightTop[0], leftDown[1]};//����			
			//�����������
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
	
	/*�жϵ�tNode�Ƿ�����centerNodeΪ���ĵĻ���뾶Ϊradius��Բ��
	 * ���ǣ�����true
	 * ���򣺷���false*/
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
	
	/*��γ������ת��Ϊ�ռ�ֱ������
	 * a�����򳤰��ᣬWGS84����ĳ�����Ϊ6378137.000001
	 * e: �����һƫ���ʵ�ƽ����WGS84�����eΪ0.00669437999013
	 * xyz:����ת������������*/ 
	public void coordinateTransfer(MapMatchNode tNode, double []xyz){
		try {
			double L = tNode.getX();//����
			double B = tNode.getY();//γ��
		    double pi = Math.PI;
		    L = L*pi/180;
		    B = B*pi/180;
			double e = 0.00669437999013;
			double a = 6378137.000001;
			double H = 0;//�̸߳�0ֵ
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
	
	/* ��γ������ĵ�ת��Ϊƽ������
	 * xy:����ת������������
	 * L0:���������߾��ȣ��人��Ϊ��114
	 * ����������λת��Ϊ����
	 * */
	public void coordinateTransToPlaneCoordinate(MapMatchNode tNode, double L0, double []xy){
		double a = 6378137, e = 0.0066943799013;        /////////���뾶�͵�һƫ���ʵ�ƽ��                               /////////���������߾��ȣ���λ������  
        double DH = 0;                              /////////ͶӰ��̧��DH�� 
        double L = tNode.x;
        double B = tNode.y;
        double pi = Math.PI;
        L = L * pi/180;
	    B = B * pi/180;
	    L0 = L0 * pi/180;
        double e1 = e / (1 - e);
        double W = Math.sqrt(1 - e * Math.sin(B) * Math.sin(B));
        double V = Math.sqrt(1 + e1* Math.cos(B) * Math.cos(B));
        ///////////////////////////ͶӰ�߳�������ı仯
        double M = a * (1 - e) / Math.pow(W, 3);
        //B = e * Math.Sin(B) * Math.Cos(B) * (1 - e * Math.Sin(B) * Math.Sin(B))/(M*W*Math.Sqrt(1-e))*DH+B;///��ƽ�����ʰ뾶�仯���㳤����仯
        B = e * Math.sin(B) * Math.cos(B) / (M) * DH + B;
        W = Math.sqrt(1 - e * Math.sin(B) * Math.sin(B));
        V = Math.sqrt(1 + e1 * Math.cos(B) * Math.cos(B));
        a=W*DH+a;///////��ĳ��î��Ȧ���ʰ뾶�仯���㳤����仯��ƫ���ʲ���
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
    /// ���Ӧγ�ȵ������߻���
    /// </summary>
    /// <param name="B">����</param>
    /// <returns></returns>
    public double CalMeridian(double B)
    {
    	double a=6378137, e=0.0066943799013;        /////////���뾶�͵�һƫ���ʵ�ƽ��
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
	
	/*�ж��Ƿ�Ϊͬһ��*/
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
	
	/* ƽ��㵽�߶ε�ͶӰ�Ƿ����߶����˵�֮�䣺�жϵ�cNode�Ƿ���startNode��endNode����֮��
	 * ����֮�䣺true
	 * ����false
	 * ˼·��
	 * 1.��startNode��endNode�����߶κ��߶���һ�˵㡢cNode����ֱ��֮����һ���н�> pi/2,��cNode��ͶӰ���߶��ӳ����ϣ�����false
	 * 2.���򣬷���true
	 * */
	public boolean isProjPointBetweenSE(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode)
	{
		double angle1 = 0;//��㴦�н�
		double angle2 = 0;//�յ㴦�н�
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
				//���յ����� ��ģ   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//����Լ��뵱ǰ��������ģ
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;
				double scNodeDis = distance(startNode, cNode);
				//���յ���������㵱ǰ������ �������н�
				angle1 = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//�յ��Լ��뵱ǰ��������ģ
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//������������յ㵱ǰ������ �������н�
				angle2 = Math.acos((-seDeltX * ecDeltX + (-seDeltY) * ecDeltY )/(seNodeDis * ecNodeDis));				
			}	
			//�������˵��
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
	
	/*ΪGPS��ĺ�ѡ��·���
	 * 1.�������
	 * 2.�������
	 * thegema:GPS������·�η���ļн�
	 * directWeight:����Ȩ�س���
	 * ��speed < 5ʱ��directWeight = directWeight[0]
	 * ����directWeight = directWeight[1]*/
	public void obtainCandidatedRoadScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, 
			double thegema, double []directWeight){
		//����÷�
		obtainCandidatedRoadDistanceScore(carrayPassTrackMap, thegema);
		//����÷�
		obtainCandidatedRoadDirectionScore(carrayPassTrackMap, directWeight);
		//�ۺϵ÷�
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
	
	/*��ѡ��·����÷�
	 * GPS�㵽��ѡ·�ξ���ĵ÷�*/
	public void obtainCandidatedRoadDistanceScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double thegema){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {					
					TaxiGPS tempTaxiGPS = gpsTrackPointArrayList.get(j);
					System.out.print("GPS��" + j + ":" + gpsTrackPointArrayList.size() + ":" + tempTaxiGPS.getTargetID() + "��þ���÷֣�" + '\n');
//					//2014-07-15�޸ģ��ٶ�Ϊ��ĵ㣬����������н�Ϊ�㣬�������ڼ��㷽��÷�
//					if (tempTaxiGPS.getSpeed() == 0) {
//						continue;
//					}
					Map<Integer, Double[]> tdistScoreSetMap = new HashMap<Integer, Double[]>();//����÷�
					tdistScoreSetMap = tempTaxiGPS.distScoreSetMap;
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = tempTaxiGPS.candidateEdgeSetArrayList;//��ѡ��·����
					if (tcandidateEdgeSetArrayList.size() > 0) {					
						for (int k = 0; k < tcandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge tEdge = tcandidateEdgeSetArrayList.get(k);
							int edgeID = tEdge.getEdgeID();
							Double []temp = tdistScoreSetMap.get(edgeID);
							double minDis = temp[0];
							//������ʣ��������̬�ֲ�������ʾ
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
	
	/*��ѡ��·����÷�
	 * GPS���ѡ·�η���ĵ÷�,�򵥴��������յ������߶εļн�
	 * directWeight:����Ȩ��*/
	public void obtainCandidatedRoadDirectionScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double []directWeight){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 0; j < gpsTrackPointArrayList.size(); j++) {
					TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
					System.out.print("GPS��" + j + ":" + gpsTrackPointArrayList.size() + ":" + taxiGPS.getTargetID() + "��÷���÷֣�" + '\n');					
					//�ٶ�Ϊ��ĵ㣬������������н�Ϊ�㣬�ó��⳵��ʻ��������������нǴ�����⳵����heading
					if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold) {
						if (j + 1 < gpsTrackPointArrayList.size()) {
							TaxiGPS nextTaxiGPS = gpsTrackPointArrayList.get(j + 1);
							if (taxiGPS.getLocalTime().equals("2013-01-01 00:07:06")) {
								System.out.print("GPS��" );
							}
							double heading = obtainZeroSpeedHeading(taxiGPS, nextTaxiGPS);
							taxiGPS.setHeading(heading);
						}
					}
					Map<Integer, Double[]> tdirectScoreSetMap = new HashMap<Integer, Double[]>();//����÷�
					double tdirectWeight = 0;
					if (taxiGPS.speed <= PubParameter.taxiSpeed) {
						tdirectWeight = directWeight[0];
					}
					else {
						tdirectWeight = directWeight[1];
					}
					ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList = taxiGPS.candidateEdgeSetArrayList;//��ѡ��·����
					if (tcandidateEdgeSetArrayList != null && tcandidateEdgeSetArrayList.size() > 0) {					
						for (int k = 0; k < tcandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge tEdge = tcandidateEdgeSetArrayList.get(k);
							Integer edgeID = tEdge.getEdgeID();
							double []AngleAndPosition = new double[2]; 
							intersectAngleAndPosition(taxiGPS, tEdge, AngleAndPosition);
							//����÷�
							double angle = AngleAndPosition[0];
							double directScore = tdirectWeight * Math.cos(angle);
							Double []directInfo = new Double[3];
							directInfo[0] = AngleAndPosition[0];//GPS�������߶μн�
							directInfo[1] = directScore;//�������
							directInfo[2] = AngleAndPosition[1];//����һ����
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
	 * ���ݵ�ǰGPS������һGPS���ϵ������ٶ�Ϊ���GPS������������н�
	 * @param currentTaxiGPS	�ٶ�Ϊ���GPS��
	 * @param nextTaxiGPS	��һGPS��
	 * @return	�ٶ�Ϊ���GPS������������н�
	 */
	public double obtainZeroSpeedHeading(TaxiGPS currentTaxiGPS, TaxiGPS nextTaxiGPS) {
		double heading = 0;//���⳵�˶���������������н�	(0 - 360)						
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
			//������������,y�᷽��
			double deltY = 0;
			double deltX = 100;
			double dist1 = Math.sqrt(Math.pow(deltX,2) + Math.pow(deltY,2));
			double deltX1 = x2 - x1;
			double deltY1 = y2 - y1;
			double dist2 = Math.sqrt(Math.pow(deltX1,2) + Math.pow(deltY1,2));
			double a = Math.acos((deltX * deltX1 + deltY * deltY1)/(dist1 * dist2));//·�η�����������������ļн�			
			if (a < Math.PI/2)
			{
				//���޻��Ǽٶ���˳ʱ�뷽����ת
				//��һ����
				if (deltX1 * deltY1 > 0)
				{
					heading = a;
				}
				//�ڶ�����
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
				//��������
				if (deltX1 * deltY1 > 0)
				{
					heading = 2 * Math.PI - a;
				}
				//��������
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
	
	/*���س��⳵��ʻ������·�εķ���н�(����)angle = intersectAngleAndPosition[0]��Ϊ��ǣ�
	 * ��·�η���Ĺ�ϵintersectAngleAndPosition[1]
	 * ����·�ε����յ��߶δ�����üнǣ�ֻ�Ǵ���ֵ����Ҫ��һ���Ż�
	 *position:���⳵������·�η���Ĺ�ϵ��1Ϊ��·������յ�ͬ��-1Ϊ����
	 *gpsAngle��Ϊ���⳵��ʻ��������������ļнǣ���Χ��0~360��
	 *lineAngle����·������������н�
	 *�����������ߵ�ͶӰΪ��������x���涨x����Ϊ�����Գ����ͶӰΪ��������y���涨y����Ϊ��
	 *a:·�η���������deltX1,deltY1������������ķ���нǣ�˳ʱ����ת����������Ƕ�
	 *��aΪ���ʱ����deltX1*deltY1 > 0��lineAngle = a��
	 *             ��deltX1*deltY1 < 0, lineAngle = 2*pi - a��
	 *��aΪֱ��ʱ����deltX1 > 0, lineAngle = pi/2;
	 *			   ��deltX1 < 0��lineAngle = 3*pi/2;
	 *��aΪ�۽�ʱ����deltX1*deltY1 > 0, lineAnge = 2*pi - a��
	 *             ��deltX1*deltY1 < 0, lineAngle = a��
	 *��gpsAngle > lineAngel 
					��gpsAngle - lineAngle <= pi/2 , ��angle = gpsAngle - lineAngle,ͬ��position = 1
					��pi/2 < gpsAngle - lineAngle <= pi,��,angle = pi - (gpsAngle - lineAngle),����position = -1;
					��pi < gpsAngle - lineAngle <= 3*pi/2����angle =(gpsAngle - lineAngle) - pi;����position = -1;
					��3*pi/2 < gpsAngle - lineAngle <= 2*pi����angle =2*pi - (gpsAngle - lineAngle);ͬ��position = 1
	 * ��gpsAngle < lineAngel 
					��lineAngle - gpsAngle <= pi/2 , ��angle = lineAngle - gpsAngle,ͬ��position = 1
					��pi/2 < lineAngle - gpsAngle <= pi,��,angle = pi - (lineAngle - gpsAngle),����position = -1;
					��pi < lineAngle - gpsAngle <= 3*pi/2����angle =(lineAngle - gpsAngle) - pi;����position = -1;
					��3*pi/2 < lineAngle - gpsAngle <= 2*pi����angle =2*pi - (lineAngle - gpsAngle);ͬ��position = 1;
	 *��gpsAngle == lineAngel 
					angle = 0, position = 1;
	 *node1:��ʾ���
	 *node2:��ʾ�յ�*/
	public void intersectAngleAndPosition(TaxiGPS tempTaxiGPS, MapMatchEdge edge, double[]AngleAndPosition)
	{
		double angle = 0;//���⳵��ʻ������·�εķ���н�		
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
			//������������,y�᷽��
			double gpsAngle = tempTaxiGPS.getHeading();//GPS����������н�
			int position = 1;
			gpsAngle = gpsAngle * Math.PI/180;
			double deltY = 0;
			double deltX = 100;
			double dist1 = Math.sqrt(Math.pow(deltX,2) + Math.pow(deltY,2));
			double deltX1 = x2 - x1;
			double deltY1 = y2 - y1;
			double dist2 = Math.sqrt(Math.pow(deltX1,2) + Math.pow(deltY1,2));
			double a = Math.acos((deltX * deltX1 + deltY * deltY1)/(dist1 * dist2));//·�η�����������������ļн�
			double lineAngle = 0;//·������������н�	(0 - 2 * pi)					
			if (a < Math.PI/2)
			{
				//���޻��Ǽٶ���˳ʱ�뷽����ת
				//��һ����
				if (deltX1 * deltY1 > 0)
				{
					lineAngle = a;
				}
				//�ڶ�����
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
				//��������
				if (deltX1 * deltY1 > 0)
				{
					lineAngle = 2 * Math.PI - a;
				}
				//��������
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

	/*�����ۺϵ÷ֺ�ת�����ʵ�����·��ƥ��
	 *��ת�����ʾ���: ��һGPS��ת��Ϊ��ǰGPS���ѡ��·�ĸ������*/	
	public void obtainPathBasedonCompreScore(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, double[]threeLevelConnProbability){
		try {
			for (int i = 0; i < carrayPassTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = carrayPassTrackMap.get(i + 1);
				for (int j = 1; j < gpsTrackPointArrayList.size(); j++) {
					TaxiGPS previousTaxiGPS = gpsTrackPointArrayList.get(j - 1);
					TaxiGPS curTaxiGPS = gpsTrackPointArrayList.get(j);//��ǰGPS��
					//GPS��ĺ�ѡ��·����
					ArrayList<MapMatchEdge> previousCandEdgeSetArrayList = previousTaxiGPS.candidateEdgeSetArrayList;
					ArrayList<MapMatchEdge> curGPSCandidateEdgeSetArrayList = curTaxiGPS.candidateEdgeSetArrayList;
					//�������һ���뵱ǰ�ߴ��ں�ѡ��·����ת�����ʾ���������¼���
					if (previousCandEdgeSetArrayList.size() > 0 && curGPSCandidateEdgeSetArrayList.size() > 0) {
						//��ת�����ʾ���
						//��curTaxiGPS���ѡ��·����EIDΪ�������洢��һGPS��ת��Ϊ��ǰGPS���ѡ��·�ĸ������
						//ArrayList�д洢��һGPS���ѡ��·EID[0]���Լ���Ӧ��ת������ֵ[1]
						Map<Integer, ArrayList<Double[]>> transitProbaMatrixMap = new HashMap<Integer, ArrayList<Double[]>>();
						for (int k = 0; k < curGPSCandidateEdgeSetArrayList.size(); k++) {
							MapMatchEdge nextGPSEdge = curGPSCandidateEdgeSetArrayList.get(k);	
							//�洢��ǰGPS���edgeID and probability
							ArrayList<Double[]> edgeIDAndProbArrayList = new ArrayList<Double[]>();
							for (int l = 0; l < previousCandEdgeSetArrayList.size(); l++) {
								MapMatchEdge edge = new MapMatchEdge();
								edge = previousCandEdgeSetArrayList.get(l);
								ArrayList<MapMatchEdge> firstLevelConnEdgeArray = edge.getFirstLevelConnEdgeArray();
								ArrayList<MapMatchEdge> secoLevelConnEdgeArray = edge.getSecoLevelConnEdgeArray();
								ArrayList<MapMatchEdge> thirdLevelConnEdgeArray = edge.getThirdLevelConnEdgeArray();	
								Double []edgeProbDoubles = new Double[2];															
								Double connProbability = 0.0;
								//�㼶��ͨ
								if (nextGPSEdge.getEdgeID() == edge.getEdgeID()) {
									connProbability = threeLevelConnProbability[0];									
								}								
								//һ����ͨ
								else if(isEdgeArraylistContainEdge(firstLevelConnEdgeArray, nextGPSEdge)){
										connProbability = threeLevelConnProbability[1];
								}
								//��������ͨ
								else if (isEdgeArraylistContainEdge(secoLevelConnEdgeArray, nextGPSEdge)) {
									connProbability = threeLevelConnProbability[2];	
								}
								//������ͨ
								else if (isEdgeArraylistContainEdge(thirdLevelConnEdgeArray, nextGPSEdge)) {
									connProbability = threeLevelConnProbability[3];
								}
								else {
									connProbability = 0.0;
								}
								//�洢��������������ʾ����ת������һ��
								edgeProbDoubles[0] = Double.valueOf(edge.getEdgeID());
								edgeProbDoubles[1] = connProbability;
								edgeIDAndProbArrayList.add(edgeProbDoubles);																
							}
							//�洢ת�����ʾ���
							if (edgeIDAndProbArrayList.size() > 0) {
								transitProbaMatrixMap.put(nextGPSEdge.getEdgeID(), edgeIDAndProbArrayList);
							}														
						}
						//Ϊ��һ��GPS���ѡ�ߵ��ۻ��÷ָ�ֵ
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
						
						//��õ�ת������ǰGPS���ѡ�ߵ����·���Լ�score
						//����ת������ǰGPS���ѡ�ߵ��ۻ��÷�,Ȼ��ȡ�õ���ǰGPS���ѡ�ߵ����÷ֵ�·����Ϊ����ǰ��ѡ�ߵ�·��
						//���洢����ǰ��ѡ�����÷�·����edgeID����·��
						Map<Integer, Double> curEdgeAccuScoreMap = new HashMap<Integer, Double>();//����ǰGPS���ѡ�ߵ��ۻ��÷�
						Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();//����ǰ�ߵ�EID��ɵ�·��
						for (int k = 0; k < curGPSCandidateEdgeSetArrayList.size(); k++) {							
							MapMatchEdge curGPSCandidateEdge = curGPSCandidateEdgeSetArrayList.get(k);//��ǰ���ѡ��
							int curGPSCandidateEdgeEID = curGPSCandidateEdge.getEdgeID();//��ǰ���ѡ��ID
							Double curDirectDouble = curTaxiGPS.getDirectScoreSetMap().get(curGPSCandidateEdgeEID)[2];
							int curDirectInt = Integer.parseInt(new java.text.DecimalFormat("0").format(curDirectDouble));//��ǰ·�εķ�����
							ArrayList<Integer[]> transPathEdgeIDArrayList = new ArrayList<Integer[]>();//ת������ǰ��·��EID���ɵ�·��
							//������һ�ߵ��ۻ��÷��Լ�����һ�ߵ�ת�����ʣ�����ÿһ���ߵĵ÷�
							//1.ȡ����һ��ѡ�ߵ��ۻ��÷֣��Լ�ת������ǰ�ߵ�ת�����ʣ�
							//2.����ת������ǰ�ߵ��ۻ��÷�
							ArrayList<Double[]> tedgeIDAndProbArrayList = new ArrayList<Double[]>();//ת������
							double maxTransScore = 0;
							if (transitProbaMatrixMap.containsKey(curGPSCandidateEdgeEID)) {
								tedgeIDAndProbArrayList = transitProbaMatrixMap.get(curGPSCandidateEdgeEID);//ȡ��ת������
								ArrayList<Integer[]> maxScorePathEIDArrayList = new ArrayList<Integer[]>();//ת������ǰ�߻�����÷֣�����һ�ߵ�·��ID
								Integer maxSoreEdgeInfo[] = new Integer[2];//ת������ǰ���ۺϵ÷�ֵ�������Ӧ����һ�ߵ�eid������
								int maxSoreEdgeID = 0;//ֻ�洢ת������ǰ���ۺϵ÷�ֵ�������Ӧ����һ�ߵ�eid
								//����ÿһ���ߣ�ȷ��ת������һ�ߵ÷���ߵ�·��scores�Լ�eid
								for (int l = 0; l < tedgeIDAndProbArrayList.size(); l++) {
									double edgeID = tedgeIDAndProbArrayList.get(l)[0];//��һ�ߵ�ID
									double probably = tedgeIDAndProbArrayList.get(l)[1];//��һ��ת������ǰ�ߵĸ���									
									//��һ���ۻ��÷֣�Ĭ��Ϊ��һ�ߵľ����뷽���ۺϵ÷�		
									int convertEdgeID = Integer.parseInt(new java.text.DecimalFormat("0").format(edgeID));									
									double preAccuEdgeScore = previousTaxiGPS.getDistDirectScoreSetMap().get(convertEdgeID);
									//����ۻ��÷�Ϊ��ֵ����ȡ�þ��뷽���ۺϵ÷�
									//tempPathEIDArrayListΪ��һ�߱���洢��EID
									if (!previousTaxiGPS.getEdgeAccuScoreMap().containsKey(convertEdgeID)) {
										preAccuEdgeScore = previousTaxiGPS.getDistDirectScoreSetMap().get(convertEdgeID);
									}	
									//����ȡ�õ���һGPS���ѡ�ߵ��ۻ��÷��Լ��ۻ�·��ID
									else {
										preAccuEdgeScore = previousTaxiGPS.getEdgeAccuScoreMap().get(convertEdgeID);									
									}
									Map<Integer, Double> curDistDirectScoreSetMap = curTaxiGPS.getDistDirectScoreSetMap();
									double curDistDirectScore = curDistDirectScoreSetMap.get(curGPSCandidateEdgeEID);//��ǰ�ߵ÷�
									double transferTonCurScore = preAccuEdgeScore + curDistDirectScore * probably;//��һ��ת������ǰ���ۺϵ÷�									
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
								//����ȡ�õ���һGPS���ѡ�ߵ��ۻ��÷��Լ��ۻ�·��ID
								else {									
									maxScorePathEIDArrayList = previousTaxiGPS.getPathEIDMap().get(maxSoreEdgeID);										
								}	
								//���ת������ǰ��·��EID���ɵ�·��
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
						}//���˻�õ���GPS���ƥ��·��
						curTaxiGPS.setEdgeAccuScoreMap(curEdgeAccuScoreMap);
						curTaxiGPS.setPathEIDMap(pathEIDMap);
					}//��ת�����ʾ���,�Լ�����ת�����ʾ�����е�ͼƥ��
					//����if��ǰGPS��ĺ�ѡ·��Ϊ��ʱ��ƥ��·��Ϊ�䱾��
					else {
						if (j == 1) {
							if (previousCandEdgeSetArrayList.size() != 0) {
								ArrayList<MapMatchEdge> candiEdgeArrayList = previousTaxiGPS.getCandidateEdgeSetArrayList();
								Map<Integer, Double> distDirectScoreSetMap = previousTaxiGPS.getDistDirectScoreSetMap();
								previousTaxiGPS.setEdgeAccuScoreMap(distDirectScoreSetMap);
								Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();	
								Map<Integer, Double> prevEdgeAccuScoreMap = new HashMap<Integer, Double>();//ǰһGPS���ѡ�ߵ��ۻ��÷�
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
									int prevGPSCandidateEdgeEID = edge.getEdgeID();//��ǰ���ѡ��ID
									double curDistDirectScore = prevDistDirectScoreSetMap.get(prevGPSCandidateEdgeEID);//��ǰ�ߵ÷�
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
								Map<Integer, Double> curEdgeAccuScoreMap = new HashMap<Integer, Double>();//����ǰGPS���ѡ�ߵ��ۻ��÷�	
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
									int curGPSCandidateEdgeEID = edge.getEdgeID();//��ǰ���ѡ��ID
									double curDistDirectScore = curDistDirectScoreSetMap.get(curGPSCandidateEdgeEID);//��ǰ�ߵ÷�
									curEdgeAccuScoreMap.put(curGPSCandidateEdgeEID, curDistDirectScore);
								}
								curTaxiGPS.setPathEIDMap(pathEIDMap);
								curTaxiGPS.setEdgeAccuScoreMap(curEdgeAccuScoreMap);
							}						
						}
						continue;
					}			
				}//ÿһ��GPS�켣���е�ͼƥ��
			}
		} catch (Exception e) {
			// TODO: handle exception	
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * ���·�������GPS���Լ���֮ƥ���edge
	 * ����ȫ�����ţ��ۻ��÷���ߣ��������Ǿֲ����ţ��ֲ��÷���ߵı�EID�����GPS��ƥ���·
	 * �������GPS�㣬ȡ���ۻ��÷���ߵı�����Ӧ��·��ID����set1
	 * ������һ��GPS��,ȡ�ö�Ӧ��·��ID����set2
	 * ����ÿһ��·������set2�����ĳһ·��ID����set2�����ڼ���set1��˵��·��set2��set1����·��������������һ��GPS��
	 * ���ĳһGPS������·������set2��������set1�У�˵����GPS������µ���·����
	 * ȡ�õ�ǰGPS���ۻ��÷���ߵı�����Ӧ��·��ID���ϣ��ظ����Ϲ��̣�ֱ����һ��GPS��
	 * @param carrayPassTrackMap
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param returnGPSAndPath
	 * @param allPathEIDMap ����·��ID��ɵ�·��
	 */
	public void exportGPSMatchPath(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, ReturnGPSAndPath returnGPSAndPath, 
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap) {
		ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//����GPS��	
		ArrayList<ArrayList<ReturnMatchNode>> allPathArrayliList = new ArrayList<ArrayList<ReturnMatchNode>>() ;//�洢����·��				
		ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//���ص�ͼƥ��ÿһ��·��
		ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//ȥ����ͬ��EID
		obtainMatchRoadAccordOptimal(carrayPassTrackMap, uniquePathEIDArrayliList);//����ȫ�����Ż��GPS��ƥ���·
		ArrayList<Integer[]> contianShortestPathEIDArrayList = new ArrayList<Integer[]>();//�������·����EID,ƥ�������·����EID				
		postProcessing(uniquePathEIDArrayliList,contianShortestPathEIDArrayList, allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);				
		allPathEIDMap.put(1, contianShortestPathEIDArrayList);
		allPathArrayliList.add(returnMapMatchOnePArrayList);			
		returnGPSAndPath.setReturnGPSArrayList(returnGPSArrayList);
		returnGPSAndPath.setReturnMapMatchEdgeArrayList(allPathArrayliList);
		System.out.print("������GPS�켣��ƥ��·��:" + '\n');
	}
	
	/**
	 * ����ȫ�����ţ��ۻ��÷���ߣ��������Ǿֲ����ţ��ֲ��÷���ߵı�EID�����GPS��ƥ���·
	 * @param carrayPassTrackMap
	 * @param pathEIDArrayliList	�洢ÿ��GPS�켣ƥ���·��ID�Լ���GPS�ķ�����
	 */
	public void obtainMatchRoadAccordOptimal(Map<Integer, ArrayList<TaxiGPS>> carrayPassTrackMap, ArrayList<Integer[]> pathEIDArrayliList) {
		try {
			System.out.print("��ʼ��GPS�켣��ƥ��·��:" + '\n');
			ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//����GPS��					
			ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
			gpsTrackPointArrayList = carrayPassTrackMap.get(1);//GPS·���켣ID��1��ʼ���
			for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
				System.out.print("GPS��:" + taxiGPS.getTargetID() + "ƥ��·��:" + '\n');
				if (taxiGPS.getTargetID().equals("MMC8000GPSANDASYN051113-24001-00000000")) {
					System.out.print(" ");
				}
				ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//�洢·��ID�Լ���GPS�ķ����ԣ���ʱ����
				ReturnMatchNode returnGPS = new ReturnMatchNode();
				returnGPS.longitude = taxiGPS.longitude;
				returnGPS.latitude = taxiGPS.latitude;
				returnGPSArrayList.add(returnGPS);
				Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//�ߵ��ۻ��÷�
				//���һ��GPS�㣬ȡ�õ÷���ߵıߵ�EID
				if (j == gpsTrackPointArrayList.size() - 1) {				
					if (edgeAccuScoreMap.size() > 0) {
						int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);
						Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
						int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS����·�η����ԵĹ�ϵ��1��ʾͬ��-1��ʾ����
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
					//ȡ��ÿ��GPS���·��EID��ɵ�·��
					if (pathEIDMap.size() > 0) {
						for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
							int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
							tempPathEIDArrayliList = pathEIDMap.get(edgeID);
							//������ڰ�����ϵ,ǰ�߰�������
							if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {						
								isContains = true;
								break;
							}
						}					
					}
					//����������������µ�·��EID
					if (!isContains) {
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//ȡ�õ÷�����·��
							Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
							if (tempPathEIDMap.size() > 0) {
								ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
								for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
									pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//��������µ�EID
								}
							}
						}							
					}	
				}
			}//�洢һ��GPS�켣ƥ���·��ID
			
			//ȥ����ͬ��EID
			//��������GPS�����λ��ͬһ·�Σ�����ѡ��ͬһ��ѡ·�Σ����·��ID�л������ͬ��EID,������ͬ��EID�����ڵ�
			//ûȡ��һ��EID��uniquePathEIDArrayliList�е����һ��EID�Ƚϣ������ͬ�������uniquePathEIDArrayliList
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//�м����
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
	
	
	/* ����
	 * ���ݵ�ǰ·��EID����·�����������·���㷨����ȱʧ���ν�·��
	 * ���·��:
	 * ȡ��ǰһ·�ε��ն˽ڵ�preEndPoint������һ·�ε����˵�nextBeginPoint���бȽϣ���Ϊͬһ������·�������м����·��
	 * �����ڸ���ǰһ·�ε���ͨ�����в��Һ�һ·�Σ�����������֮������·������Ϊ����·��
	 * PathEIDArrayliList��·��ID,�����²��������·��ID����ü�����
	 * polylineCollArrayList:�洢��·����Ϣ
	 * returnMapMatchOnePArrayList:���ص�ͼƥ���һ��·��*/
	public void postProcessing(ArrayList<Integer[]> PathEIDArrayliList,ArrayList<Integer[]>containShortestPathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList){
		try {
			//����·��EIDȡ����Ӧ��·��
			for (int j = 0; j < PathEIDArrayliList.size() - 1; j++) {
				Integer []edgeIDInt = PathEIDArrayliList.get(j);//ȡ�õ�ǰ·��ID����GPS�ķ�����
				containShortestPathEIDArrayList.add(edgeIDInt);
				MapMatchEdge edge = new MapMatchEdge();//��ǰ��
				int edgeID = edgeIDInt[0];
				if (edgeID == 1514) {
					System.out.print("");
				}
				int direPro = edgeIDInt[1];
				Integer []nextEdgeIDInt = PathEIDArrayliList.get(j + 1);//��һ·��ID����GPS�ķ�����
				MapMatchEdge nextEdge = new MapMatchEdge();//��һ��
				int nextEdgeID = nextEdgeIDInt[0];
				int nextDirePro = nextEdgeIDInt[1];				
				ArrayList<MapMatchNode> pointCollArrayList = new ArrayList<MapMatchNode>();//��ǰ·���ϵĵ�
				ArrayList<MapMatchNode> nextPointCollArrayList = new ArrayList<MapMatchNode>();//next·���ϵĵ�
				MapMatchNode endNode = new MapMatchNode();//��ǰ·���յ�
				MapMatchNode nextBeginNode = new MapMatchNode();//next·�����
				boolean isEdgeObtain = false;//��ǰ���Ƿ���
				boolean isNextEdgeObtain = false;//��һ���Ƿ���	
						
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
					//�����߶����
					if (isEdgeObtain && isNextEdgeObtain) {
						break;
					}
				}
				if (isEdgeObtain && isNextEdgeObtain) {
					//���Ϊͬ��
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
					//����
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
					//��ǰ��ƥ���յ�����ƥ�����Ϊͬһ��
					if (isTheSameNode(endNode, nextBeginNode)) {
						//���Ϊ�����ڶ�����
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
						//����
						continue;
					}
					//���򣺸������·���㷨��·���������õ����·��
					//����ڵ�����һ��ڶ���·����������·�������а����һ��·�μ���
					else {
						//���·���㷨���������֮��ƥ������·����ǰ��ƥ���յ㵽���ƥ�����֮�����·��
						//searRoadStartNode:Ѱ·��㣬searRoadEndNodeѰ·�յ�
						//��searRoadStartNodeΪ������distanceС��Χ�ڽ������ˣ�distanceΪ���յ�֮���2������
						ArrayList<MapMatchNode> tempTopolygonArrayList = new ArrayList<MapMatchNode>();
						double distance = 2 * distance(endNode, nextBeginNode);
						for (int i = 0; i < juncCollArrayList.size(); i++) {
							MapMatchNode tNode = juncCollArrayList.get(i);
							if (isNodeInCircle(endNode, tNode, distance)) {
								tempTopolygonArrayList.add(tNode);
							}
						}
						
						//����Ѱ·��㡢�յ�
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
						//ȡ�����·����EID
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
					//���Ϊ�����ڶ�����******************************************************
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
	
	/*������֮�����·����ǰһ��ƥ���յ㵽��һ��ƥ�����֮������·��
	 * startEdge:����
	 * endEdge:�յ��
	 * pNode:��һ�ڵ�
	 * cNode:��ǰ�ڵ�
	 * sNode:���
	 * eNode���յ�
	 * returnMapMatchPathArrayList���������֮���ƥ�����·��
	 * edgeIDArraylist�����·��ID*/
	private Stack<MapMatchNode> stack = new Stack<MapMatchNode>();
	private ArrayList<MapMatchNode> openList = new ArrayList<MapMatchNode>();//�����б�
	
	/**
	 * 2014/12/27�޸ĺ��㷨
	 */
	public boolean obtainShortestPath(MapMatchNode pNode, MapMatchNode cNode, MapMatchNode sNode, MapMatchNode eNode,
			ArrayList<MapMatchNode>juncCollArrayList, ArrayList<ReturnMatchNode> returnMapMatchPathArrayList,ArrayList<Integer[]> edgeIDArrayList){
		MapMatchNode nNode = null;	
			try {
				/* ������������ж�˵�����ֻ�·��������˳�Ÿ�·������Ѱ·������false */
				if (cNode != null && pNode != null && cNode.nodeID == pNode.nodeID)
					return false;
				//��ǰ�ڵ㲻Ϊ��
				if (cNode != null) {
					int i = 0;
					/* �������ʼ�ڵ�����յ㣬˵���ҵ�һ��·�� */
					if (cNode.nodeID == eNode.nodeID)				
					{
						stack.push(cNode);
						//���յ������ص����,���ؽڵ�ID��ɵ�·��
						ArrayList<MapMatchNode> tempMapMatchPathArrayList = new ArrayList<MapMatchNode>();
						getPath(tempMapMatchPathArrayList, cNode);	
						//2014-11-14�޸�
						//�ҵ�·���󣬰ѽڵ�ĸ��ڵ㶼���
						for (int j = 0; j < tempMapMatchPathArrayList.size(); j++) {
							MapMatchNode tNode = tempMapMatchPathArrayList.get(j);
							tNode.setParentNode(null);
						}						
						ArrayList<MapMatchNode>nodeArrayList =new ArrayList<MapMatchNode>();//˳��洢��ǰ·���ڵ�
						MapMatchNode[]nodesPath=new MapMatchNode[tempMapMatchPathArrayList.size()];
						for (int l = 0; l < tempMapMatchPathArrayList.size(); l++) {
							nodesPath[l] = tempMapMatchPathArrayList.get(l);
						}						
						/*����ǿ���·����洢�����
						 * �ɽڵ�ID����relationEdgesMap��Ӧ�ߣ�������Ӧ��polyline
						 * ����·��
						 * �ۼ�·�����ȣ���tempAccuLength>=circle,��ȡ��浱ǰpolyline�������������ڼ�������
						 * ��tempAccuLength<circle:��·������������*/																									
						for (int j = 0; j < nodesPath.length-1; j++) {
							int EID=nodesPath[j].nodeID;//�ڵ�ID
							nodeArrayList.add(nodesPath[j]);
							//��Ѱ·���ڵ����ڱ�Edge
							ArrayList<MapMatchEdge> relaEdgeArraylist=new ArrayList<MapMatchEdge>();//�洢�ڵ��ڽӱ�
							for (int p = 0; p < juncCollArrayList.size(); p++) {
								if (EID == juncCollArrayList.get(p).nodeID) {
									relaEdgeArraylist = juncCollArrayList.get(p).relationEdges;
									break;
								}
							}
							int relaEdgeCount=relaEdgeArraylist.size();//�ڵ����ڱ���
							
							//�������ڱ�				
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
								
								//ȡ���ڽ�polyline��vertex
								//����뵱ǰ��ֱ�������ı�				
								if (isTheSameNode(nodesPath[j+1], froNode)||isTheSameNode(nodesPath[j+1], toNode)) {						

									//��β����ڵ��غϣ�ȥ����β�㣬ֻȡ�м��
									//����ǰ�ڵ���plinePoints���׵���ͬ���򰴴�˳������point
									//����������point
									MapMatchNode tempSNode = new MapMatchNode();//�׵�
									MapMatchNode tempENode = new MapMatchNode();//β��
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
									//��β����ͬ
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
									break;//�������ڽ�polyline������
								}				
							}				
						}	
						//���ؽ��
						for (int k = 0; k < nodeArrayList.size(); k++) {
							ReturnMatchNode returnNode = new ReturnMatchNode();
							returnNode.longitude = nodeArrayList.get(k).x;
							returnNode.latitude = nodeArrayList.get(k).y;
							returnMapMatchPathArrayList.add(returnNode);
							
						}
						return true;//Ѱ·����
					}				
					/* �������,����Ѱ· */
					else
					{		
						//��������
						if (cNode.getRelationNodes().size() <= i) {
							return false;
						}
						/* ���뵱ǰ��ʼ�ڵ�cNode�����ӹ�ϵ�Ľڵ㼯�а�˳������õ�һ���ڵ�
						 * ��Ϊ��һ�εݹ�Ѱ·ʱ����ʼ�ڵ� 
						 */
						nNode = cNode.getRelationNodes().get(i);					
						while(nNode != null){
							//����˴�ΪĿ��㣬Ӧ�����·��
							if (isTheSameDirection(sNode, eNode,180, nNode)
									&& isInSpanDistance(sNode, eNode, 1500, nNode) && isDirectProjSatis(sNode, eNode, nNode, 100)) {
								//��֤���������·
								if ( nNode.nodeID != sNode.nodeID ) {	
									//���ҿ����б����Ƿ���ڸõ�,�����ڷ��ظõ����������򷵻�-1
									int indexInOpenlist = isListContains(openList, nNode);
									int indexInStack = -1;//�ڹر��б��е�����
									if (indexInOpenlist == -1) {
										indexInStack = isStackContains(stack,nNode);
									}								
									double cost = distance(nNode, cNode);//����һ�ڵ�Ĵ���
									//������ڣ�Gֵ�Ƿ��С�����Ƿ����G��Fֵ
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
									//����ڹر��б�stack��
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
										//��ӵ������б���								
										nNode.setParentNode(cNode);//���ڵ�								
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
							//���򣬼���Ѱ�����ڵ�
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
						//�ӿ����б���ɾ��F��С��node
				        //��ӵ�stack��
						if (openList.size() >=1 ) {
							//�����б������򣬰�Fֵ��С�ķŵ����
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
	 * С��������
	 * 
	 * ****************************************************************************
	 * ****************************************************************************/
	/*��Ѱ·�ߵĵ�ǰ���Ƿ������յ���ͬһ����
	  * ��Ѱ����Ѱ��Χ�����ڵ�·�ߣ�
	  * �жϣ����յ���������㵱ǰ��������ļн�����Ѱ��Χ���ȵ�1/2�Ƚ�
	  * ���������н�angle<sxhd��Ѱ��Χ���ȣ�˵���õ��������
	  * starNode:���
	  * endNode���յ�
	  * cnode:��Ѱ��ǰ��
	  * a:��Ѱ��Χ������Ҫת��Ϊ����*/
	public boolean isTheSameDirection(MapMatchNode starNode,MapMatchNode endNode,int a,MapMatchNode cNode)
	{		
		double pi = Math.PI;
		double sxhd = a*pi/180;//��Ѱ��Χ����
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
				
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;
				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;
				//�������н�
				//�����ǰ�����յ���ͬ��angle�᷵��null
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
	
	/*��Ѱ���Ƿ�����Ѱ���յ�ֱ�߿�ȷ�Χ��
	 * ���㣺varSpan:��㵱ǰ������ģ���������յ㷽�������нǵ�����ֵ
	 * �жϣ�����Ѱ�����Ѱ����ֱ�ߵľ���varSpan<=span���򷵻�true�����򷵻�false
	 * starNode:���
	 * endNode���յ�
	 * cnode:��Ѱ��ǰ��
	 * span����Ѱ���*/
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
				
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;
				
				//�������н�
				angle=Math.acos((seDeltX*scDeltX+seDeltY*scDeltY)/(seNodeDis*scNodeDis));
				varSpan=scNodeDis*Math.sin(angle);
			}			
			//���						
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
	
	 /*�����б��Ƿ�����õ�,(-1��û���ҵ������򷵻����ڵ�����)*/
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
	  * ����ͶӰ�����Ƿ�����������ͶӰ���ȱ�����������յ�֮��
	  * ��ǰ�ڵ��뾯����ھ���㡢�յ�ֱ���ϵ�ͶӰ����С�ھ���㡢�յ�֮��ľ���+�������
	  * @param jjNode	�Ӿ���
	  * @param endNode	Ѱ·�յ�
	  * @param node	��ǰ�ڵ�
	  * @param projBuffer	�������
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
					//���յ����� ��ģ      ��㵱ǰ��������ģ
					jeNodeDis=distance(jjNode, endNode);
					double jeDeltX = endNodeX-jjNodeX;
					double jeDeltY = endNodeY-jjNodeY;
					//����Լ��뵱ǰ��������ģ
					double snDeltX = nodeX-jjNodeX;
					double snDeltY = nodeY-jjNodeY;
					double snNodeDis = distance(jjNode, node);
					
					//���յ���������㵱ǰ������ �������н�
					angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY)/(jeNodeDis*snNodeDis));
					//ͶӰ����
					projDis=snNodeDis*Math.cos(angle);
				}	
				//angle����Ϊ��ֵ,�����޶�����������
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
		
	 //����G,H,Fֵ
	 private void count(MapMatchNode node, MapMatchNode eNode, double cost){
	     countG(node, eNode, cost);
	     countH(node, eNode);
	     countF(node);
	 }
	
	 //����Gֵ
	 private void countG(MapMatchNode node, MapMatchNode eNode, double cost){
	     if(node.getParentNode()==null){
	         node.setG(cost);
	     }else{
	         node.setG(node.getParentNode().getG() + cost);
	     }
	 }
	
	 //����Hֵ
	 private void countH(MapMatchNode node, MapMatchNode eNode){
	     double dist = distance(node, eNode);
	     node.setH(dist);
	 }
	
	 //����Fֵ
	 private void countF(MapMatchNode node){
	     node.setF(node.getG()+node.getH());
	 }	
 
	 /*���ؽڵ���ɵ�·��,���յ������ص����*/
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
		 
	/*���double�����ֵ����Ӧ�Ľ�ֵkey
	 * */
	public int obtainHigestScoreEID(Map<Integer, Double> edgeAccuScoreMap){  
        Set keySet = edgeAccuScoreMap.entrySet();
        int keyVal = 0;
        if (keySet != null) {
        	Iterator iterator = (Iterator) keySet.iterator(); 
        	Map.Entry  mapEntry = (Map.Entry) iterator.next();
        	Object maxValkey = mapEntry.getKey();
	        Object maxValObject = mapEntry.getValue();//�����һ��ֵΪ���ֵ
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
	 * GPS�����:��þ������GPS��
	 * @param gpsTrackPointArrayList
	 * @param pathEIDArrayliList 
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList �洢����GPS�����������
	 * 2014/12/23 ��д
	 */
	public void obtainGPSCorrectionCoordOriginal(ArrayList<TaxiGPS> gpsTrackPointArrayList, ArrayList<Integer[]> pathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {		
			ArrayList<Integer[]> pathEIDArrayliList = new ArrayList<Integer[]>();//�洢ÿ��GPS�켣ƥ���·��ID�Լ���GPS�ķ����ԣ��м����
			ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//���ص�ͼ�������һ��GPS·��
			//��������һ��GPS�㿪ʼ  ������÷ֱ�GPS��ƥ��·��
			for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
				ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//�洢·��ID�Լ���GPS�ķ����ԣ���ʱ����
				Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//�ߵ��ۻ��÷�
				//���һ��GPS�㣬ȡ�õ÷���ߵıߵ�EID
				if (j == gpsTrackPointArrayList.size() - 1) {				
					if (edgeAccuScoreMap.size() > 0) {
						int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//�˴�������
						taxiGPS.setBelongLineID(edgeID);//GPS����·��ID
						Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
						int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS����·�η����ԵĹ�ϵ��1��ʾͬ��-1��ʾ����
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
					//ȡ��ÿ��GPS���·��EID��ɵ�·��
					if (pathEIDMap.size() > 0) {
						for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
							int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
							tempPathEIDArrayliList = pathEIDMap.get(edgeID);
							//������ڰ�����ϵ,ǰ�߰�������
							if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {
								taxiGPS.setBelongLineID(edgeID);//GPS����·��ID
								isContains = true;
								break;
							}
						}					
					}
					//����������������µ�·��EID
					if (!isContains) {
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//ȡ�õ÷�����·��ID
							taxiGPS.setBelongLineID(edgeID);
							Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
							if (tempPathEIDMap.size() > 0) {
								ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
								for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
									pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//��������µ�EID
								}
							}
						}							
					}	
				}
			}//�洢һ��GPS�켣ƥ���·��ID			
			
			//ȥ����ͬ��EID
			//��������GPS�����λ��ͬһ·�Σ�����ѡ��ͬһ��ѡ·�Σ����·��ID�л������ͬ��EID,������ͬ��EID�����ڵ�
			//ûȡ��һ��EID��uniquePathEIDArrayliList�е����һ��EID�Ƚϣ������ͬ�������uniquePathEIDArrayliList
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//�м����
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
			ArrayList<Integer[]> containShortestPathEIDArrayList = new ArrayList<Integer[]>();//������ϵ����·��·��EID
			postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
			ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
			obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
			for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
				System.out.print("���GPS�����㣺" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
				GPSCorrectArrayList.add(tGPSCorrectedNodeArraylist.get(j));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * GPS�����:��þ������GPS��
	 * @param processTaxiTrackMap
	 * @param correctedOriginalTaxiTrackArrayList
	 * @param pathEIDArrayliList 
	 * @param allGridIndexVerticesMap
	 * @param juncCollArrayList
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList �洢����GPS�����������
	 */
	public void obtainGPSCorrectionCoord(Map<Integer, ArrayList<TaxiGPS>> processTaxiTrackMap, ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList, ArrayList<Integer[]> containShortestPathEIDArrayList, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {				
			ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//���ص�ͼƥ��ÿһ��·��
			ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//ȥ����ͬ��EID
			Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
//			taxiTrackMap.put(1, gpsTrackPointArrayList);
			obtainMatchRoadAccordOptimal(processTaxiTrackMap, uniquePathEIDArrayliList);//����ȫ�����Ż��GPS��ƥ���·
			postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
			ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
			ArrayList<TaxiGPS> gpsTrackPointArrayList = processTaxiTrackMap.get(1);
			obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
			for (int i = 0; i < gpsTrackPointArrayList.size(); i++) {
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(i);
				correctedOriginalTaxiTrackArrayList.add(taxiGPS);	
			}
			for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
				System.out.print("���GPS�����㣺" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
				GPSCorrectArrayList.add(tGPSCorrectedNodeArraylist.get(j));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
	
	/*GPS�����:����������GPS��
	 * GPSCorrectArrayList���洢����GPS�����������*/
	public void obtainGPSCorrectionCoord(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {	
			ArrayList<ReturnMatchNode> returnGPSArrayList = new ArrayList<ReturnMatchNode>();//����GPS��		
			for (int i = 0; i < taxiTrackMap.size(); i++) {
				ArrayList<TaxiGPS> gpsTrackPointArrayList = new ArrayList<TaxiGPS>();
				gpsTrackPointArrayList = taxiTrackMap.get(i + 1);
				ArrayList<Integer[]> pathEIDArrayliList = new ArrayList<Integer[]>();//�洢ÿ��GPS�켣ƥ���·��ID�Լ���GPS�ķ����ԣ��м����
				ArrayList<ReturnMatchNode> returnMapMatchOnePArrayList = new ArrayList<ReturnMatchNode>();//���ص�ͼ�������һ��GPS·��
				for (int j = gpsTrackPointArrayList.size() - 1; j >= 0 ; j--) {				
					TaxiGPS taxiGPS = gpsTrackPointArrayList.get(j);
					ArrayList<Integer[]> tempPathEIDArrayliList = new ArrayList<Integer[]>();//�洢·��ID�Լ���GPS�ķ����ԣ���ʱ����
					ReturnMatchNode returnGPS = new ReturnMatchNode();
					returnGPS.longitude = taxiGPS.longitude;
					returnGPS.latitude = taxiGPS.latitude;
					returnGPSArrayList.add(returnGPS);
					Map<Integer, Double> edgeAccuScoreMap = taxiGPS.getEdgeAccuScoreMap();//�ߵ��ۻ��÷�
					//���һ��GPS�㣬ȡ�õ÷���ߵıߵ�EID
					if (j == gpsTrackPointArrayList.size() - 1) {				
						if (edgeAccuScoreMap.size() > 0) {
							int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);
							Double[]tempDouble = taxiGPS.getDirectScoreSetMap().get(edgeID);
							int direProperty = Integer.parseInt(new java.text.DecimalFormat("0").format(tempDouble[2]));//GPS����·�η����ԵĹ�ϵ��1��ʾͬ��-1��ʾ����
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
						//ȡ��ÿ��GPS���·��EID��ɵ�·��
						if (pathEIDMap.size() > 0) {
							for (int k = 0; k < candidateEdgeSetArrayList.size(); k++) {
								int edgeID = candidateEdgeSetArrayList.get(k).getEdgeID();
								tempPathEIDArrayliList = pathEIDMap.get(edgeID);
								//������ڰ�����ϵ,ǰ�߰�������
								if (isArraylistContainsArraylist(pathEIDArrayliList, tempPathEIDArrayliList)) {						
									isContains = true;
									break;
								}
							}					
						}
						//����������������µ�·��EID
						if (!isContains) {
							if (edgeAccuScoreMap.size() > 0) {
								int edgeID = obtainHigestScoreEID(edgeAccuScoreMap);//ȡ�õ÷�����·��
								Map<Integer, ArrayList<Integer[]>> tempPathEIDMap = taxiGPS.getPathEIDMap();
								if (tempPathEIDMap.size() > 0) {
									ArrayList<Integer[]> tempEIDArrayliList = tempPathEIDMap.get(edgeID);
									for (int k = tempEIDArrayliList.size() - 1; k >= 0; k--) {
										pathEIDArrayliList.add(0,tempEIDArrayliList.get(k));//��������µ�EID
									}
								}
							}							
						}	
					}
				}//�洢һ��GPS�켣ƥ���·��ID			
				
				//ȥ����ͬ��EID
				//��������GPS�����λ��ͬһ·�Σ�����ѡ��ͬһ��ѡ·�Σ����·��ID�л������ͬ��EID,������ͬ��EID�����ڵ�
				//ûȡ��һ��EID��uniquePathEIDArrayliList�е����һ��EID�Ƚϣ������ͬ�������uniquePathEIDArrayliList
				ArrayList<Integer[]> uniquePathEIDArrayliList = new ArrayList<Integer[]>();//�м����
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
				ArrayList<Integer[]> containShortestPathEIDArrayList = new ArrayList<Integer[]>();//������ϵ����·��·��EID
				postProcessing(uniquePathEIDArrayliList, containShortestPathEIDArrayList,allGridIndexVerticesMap, juncCollArrayList, polylineCollArrayList, returnMapMatchOnePArrayList);
				ArrayList<CorrectedNode> tGPSCorrectedNodeArraylist = new ArrayList<CorrectedNode>();
				obtainProjCoordAccordPathEID(gpsTrackPointArrayList, containShortestPathEIDArrayList, polylineCollArrayList, tGPSCorrectedNodeArraylist);
				for (int j = 0; j < tGPSCorrectedNodeArraylist.size(); j++) {
					System.out.print("���GPS�����㣺" + j + ":" + (tGPSCorrectedNodeArraylist.size() - 1) + '\n');
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
	 * ����·��EID���GPS����·���϶�Ӧ�ľ�����ͶӰ������
	 * 1.���������е�ÿһ��GPS��
	 * 2.�ж�ÿһ��GPS���ѡ��·EID���Ƿ���·������PathEIDArrayliList��
	 * 3.ȡ����·�������еĶ�ӦEID�ı߲���������Ӧ�ߵķ����ϵ
	 * 4.GPS����ñ�ͶӰ���������Ӧ��ͶӰ����
	 * @param gpsTrackPointArrayList GPS������
	 * @param PathEIDArrayliList ·��ID
	 * @param polylineCollArrayList
	 * @param GPSCorrectArrayList GPS�����������
	 */
	public void obtainProjCoordAccordPathEID(ArrayList<TaxiGPS> gpsTrackPointArrayList, ArrayList<Integer[]> PathEIDArrayliList, 
			ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<CorrectedNode> GPSCorrectArrayList){
		try {
			for (int i = 0; i < gpsTrackPointArrayList.size(); i++) {
				TaxiGPS taxiGPS = gpsTrackPointArrayList.get(i);
				int targetEdgeID = -1;//����������Ŀ��·��ID
				String targetEdgeName = "none";
				MapMatchNode tempGPS = new MapMatchNode();
				tempGPS.x = taxiGPS.getLongitude();
				tempGPS.y = taxiGPS.getLatitude();
				MapMatchNode correctGPSNode = null;//������GPS��
				ArrayList<MapMatchEdge> candRoadArrayList = taxiGPS.getCandidateEdgeSetArrayList();
				//ȡ�ú�ѡ��			
				boolean isOK = false;
				for (int j = 0; j < candRoadArrayList.size(); j++) {
					int edgeID = candRoadArrayList.get(j).getEdgeID();//��ѡ��ID
					MapMatchEdge edge = new MapMatchEdge();
					ArrayList<MapMatchNode> pointCollArrayList = new ArrayList<MapMatchNode>();
					int dirRela = isArraylistContainsEID(PathEIDArrayliList, edgeID);//�ж�·��EID���Ƿ������ID
					if (dirRela != 0) {	
						targetEdgeID = edgeID;
						//ȡ�ú�ѡ���ϵĵ㲢ͶӰȡ�þ�������
						for (int k = 0; k < polylineCollArrayList.size(); k++) {
							MapMatchEdge polyline = polylineCollArrayList.get(k);
							if (edgeID == polyline.getEdgeID()) {
								edge = polyline;
								targetEdgeName = polyline.getEdgeName();
								pointCollArrayList = polyline.getPointCollArrayList();
								break;
							}
						}
						//��þ�������
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
							//�������߿��ܻ���ö��ͶӰ�㣬��ʱ��Ҫ�����ж�����
							//��������GPS��ľ���ҪС�ں�ѡ�뾶
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
				//�������������Ϣ
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
				//����ֻ�洢GPS��Ϣ,û�о���������ԭʼGPS����Ϊ������
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
	
	/*��ú�ѡ��·����ΪĿ��·�ε�GPS��
	 * eliminateZeroSpeedGPSDataArrayList:ȥ���ٶ�Ϊ���GPS��
	 * eligibleGPSArrayList����ѡ��·����ΪĿ��·�ε�GPS��
	 * radius����ѡ�뾶
	 * linkID��Ŀ��·��ID
	 * targetEdge������Ŀ��·��*/
	public void obtainEligibleGPSArraylist(Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap, Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap,
			ArrayList<MapMatchEdge> polylineCollArrayList,ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList,
			ArrayList<TaxiGPS> eligibleGPSArrayList,double radius, MapMatchEdge targetEdge){
		try {
			int linkID = targetEdge.getEdgeID();
			Double leftDownNode[] = new Double[2];
			Double rightTopNode[] = new Double[2];
			PubClass.BoundingRectangle(leftDownNode, rightTopNode, targetEdge);//����������
			//������������
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
	
	/*���Ŀ��·�Σ�����·��ID���Ŀ��·��
	 * targetLinkID��·��ID
	 * targetEdge��Ŀ��·��
	 * polylineCollArrayList��·�μ���
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
	
	/*ɸѡΨһ�ĳ��⳵ID,��ȷ��Ψһ��TaxiID
	 * eligibleGPSArrayList����ѡ��·����ΪĿ��·�ε�GPS��
	 * uniqueTaxiArrayList:����������taxiID,Ψһ��taxiID*/
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
	
	/*���ݳ��⳵ID��GPS��Ϣ���з���
	 * ID��ͬ�ĳ��⳵��Ϊһ��
	 * eligibleGPSArrayList��������⳵��Ϣ
	 * taxiSortMap������ID�����ĳ��⳵��Ϣ
	 * 1.���ÿ�����⳵Ψһ��targetID
	 * 2.���targetID��ͬ�ĳ��⳵��Ϣ��Ϊһ��
	 * 3.�洢*/
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
	
	
	/*ͳ�����г��⳵��Ŀ��·�ε��г�ʱ��
	 * uniqueTaxiArrayList�����г��⳵��IDΨһ
	 * startTimeStr�����⳵��ʼʱ�䣬��ʽΪ2013-01-01 00:00:15
	 * timeInterval:Ҫ��ѯһ�γ��⳵�켣��ʱ����
	 * targetLinkID��Ŀ��·��ID
	 * timeThreshold:·�����ͨ��ʱ�����ֵ��<=60��
	 * travalTimeMap:�Գ��⳵IDΪ����,�洢��·��ͨ��ʱ��
	 * taxiLinkTravelMap:�Գ��⳵IDΪ����,�洢���⳵��GPS��
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
				double[] travelTime = new double[1];//·��ͨ��ʱ��
				ArrayList<MapMatchNode> taxiTravelArrayList = new ArrayList<MapMatchNode>();//·��ͨ��ʱ���Ӧ��GPS��
				//��õ������⳵��ͨ��ʱ��
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
	
	/*��Ҫ����ͬһ�����⳵���ܻᴩԽͬһ·�ζ��
	 * taxiSortMap����ID���й���ĳ��⳵��Ϣ
	 * startTimeStr����ʼʱ��
	 * endTimeStr����ֹʱ��
	 * targetLinkID:Ŀ��·��ID
	 * targetEdge��Ŀ��·��
	 * sampleThreshold:������ֵ��Ĭ��60s�������ڽ��й켣�ʷ�
	 * expandTime����չʱ�䣨Ĭ��60s��
	 * taxiTravelTimeArrayList:Ŀ��·�����г��⳵��ͨ����Ϣ
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
	    		ArrayList<TaxiGPS> taxiTrackArrayList = taxiSortMap.get(taxiIDStr);//ʱ��η�Χ�ڳ��⳵�Ĺ켣
	    		TaxiTravelTime taxiTravelTime = new TaxiTravelTime();
				//��õ������⳵��ͨ��ʱ�������Ϣ
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
	
	
	/*ͳ��ĳһ���⳵�г�ʱ��
	 * 1.ƥ����⳵�켣:���ݳ��⳵ID�����⳵��¼ʱ��ÿ��timeIntervalʱ���ѯһ�γ��⳵�Ĺ켣
	 * 2.�������⳵�켣ID���Ƿ����Ŀ��·�Σ�
	 * 3.�����⳵�켣�а���Ŀ��·�Σ��켣��������
	 * 		����GPS�ĺ�ѡ·�����Ƿ����Ŀ��·��ɸѡGPS�㣺����ѡ·���а���Ŀ��·�Σ�GPS����������������ж���һ��GPS�㣬
	 * 		���켣�е����е㶼����targetLinkID���������һ��ʱ����timeInterval��GPS���ѡȡ��֪��GPS���ѡ·��
	 * 		������Ŀ��·�Σ�����ѡ·�β�����Ŀ��·�Σ����GPS�㲻����������Ȼ����㵽�õ��ʱ����Ϊ�г�ʱ��
	 * 	���򣬴˳��⳵�켣������������
	 * 4.
	 * taxiIDStr:���⳵ID
	 * startStartTimeStr:����Ŀ�ʼʱ��,��ֵ����
	 * startTimeStr:��ʼʱ��
	 * endTimeStr:��ֹʱ��
	 * timeInterval:ʱ����,��ֵ����
	 * targetLinkID:Ŀ��·��ID,��ֵ����
	 * travalTime:���⳵�洢��·��ͨ��ʱ��*/
	public void obtainSingleTaxiTravelTime2(String taxiIDStr, String startStartTimeStr, String startTimeStr, 
			String endTimeStr, int timeInterval, int targetLinkID, double[] travelTime, 
			ArrayList<MapMatchNode> taxiTravelArrayList, int timeThreshold){
		try {
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, taxiIDStr, startTimeStr, endTimeStr);
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//ȥ���ٶ�Ϊ���GPS��
			ReturnGPSAndPath returnGPSAndPath = new ReturnGPSAndPath();
			Map<Integer, ArrayList<TaxiGPS>> taxiMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
			taxiMap.put(1, eliminateZeroSpeedGPSDataArrayList);
			Map<Integer,ArrayList<Integer[]>> allPathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();//����·��EID���·��,������·���ķ�����
			MapMatchAlgorithm.mapMatch(taxiMap, returnGPSAndPath,allPathEIDMap);//ƥ��·��
			ArrayList<Integer[]> pathEIDArrayList = allPathEIDMap.get(1);
			//�ж�·��ID���Ƿ����Ŀ��·��
			if (isArraylistContainsEID(pathEIDArrayList, targetLinkID) != 0) {
				int GPSCount = 0;//����ĳ��⳵GPS�����Ŀ
				for (int j = 0; j < eliminateZeroSpeedGPSDataArrayList.size(); j++) {
					TaxiGPS tempTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(j);
					ArrayList<MapMatchEdge> candidateEdgeSetArrayList = tempTaxiGPS.getCandidateEdgeSetArrayList();
					if (isEdgeArraylistContainEdgeID(candidateEdgeSetArrayList, targetLinkID)) {
						MapMatchNode tNode = new MapMatchNode();
						tNode.setX(tempTaxiGPS.getLongitude());
						tNode.setY(tempTaxiGPS.getLatitude());
						taxiTravelArrayList.add(tNode);
						GPSCount++;
						//�������GPS���ѡ·�ξ�����Ŀ��·��targetLinkID����չʱ���
						//�����չʱ��κ��ʱ�����������ʼʱ���ʱ����> ʱ���ޣ����taxi����������������ѭ��
						if (GPSCount == eliminateZeroSpeedGPSDataArrayList.size()) {
							String[] endTimeArray = new String[1];
							PubClass.obtainEndTimeAccordStartTime(endTimeStr, timeInterval, endTimeArray);
							double tiemInterval = PubClass.obtainTimeInterval(startStartTimeStr, endTimeArray[0]);
							//û�г���ʱ����ֵ
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
						//��ͨ��ʱ��
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
	
	/*�������⳵ͨ��Ŀ��·��ʱ��
	 * taxiTrackArrayList�����Թ��˵ĳ��⳵�켣
	 * startTimeStr����ʼʱ��
	 * endTimeStr����ֹʱ��
	 * targetLinkID��Ŀ��·��ID
	 * targetEdge��Ŀ��·��
	 * sampleThreshold:������ֵ��Ĭ��60s�������ڽ��й켣�ʷ�
	 * expandTime����չʱ�䣨Ĭ��60s����
	 * TaxiTravelTime�����⳵ͨ��ʱ�䣬ͬһ���⳵ͬһ·�ε�ͨ��ʱ�����Ϊ���
	 * 
	 * 1.�켣�ʷ֣���������GPS��֮��Ĳ���ʱ��С����ֵsampleDiff�����ж���Ϊͬһʱ����ڵ�ͬһ�켣��
	 * 		���ʱ��������ֵ�������Ϊ��ͬʱ�����ͨ��ͬһ·�εĲ�ͬ�켣
	 * 2.ʱ����չ������ÿһ���켣��β֮���ʱ�䣬��չʱ��expandTime��Ĭ��60s��
	 * 3.���ݼ���������targetID�Լ���ֹʱ���Լ���չ���ʱ��������ݿ⣬������Χ�ڵ�GPS����
	 * 4.��ͼƥ�䣺����GPS���ݶԳ��⳵�켣���е�ͼƥ��
	 * 5.Ŀ��·���жϣ��ж�ƥ��·�����Ƿ����Ŀ��·��
	 * 6.��ֵ���㣺�������Ŀ��·�Σ�����·�ζ˵㴦���ݾ�����в�ֵ���򵥲�ֵ��
	 * 7.��������ͨ��ʱ���Լ���Ӧ��·��*/	
	public void obtainSingleTaxiTravelTime(ArrayList<TaxiGPS> taxiTrackArrayList, String startTimeStr, String endTimeStr, 
			int targetLinkID, MapMatchEdge targetEdge, int sampleThreshold, 
			int expandTime, TaxiTravelTime taxiTravelTime){
		try {
			//�켣�ʷ�,���ܶ����켣
			String tempTaxiIDStr = taxiTrackArrayList.get(0).getTargetID();
			System.out.print(tempTaxiIDStr + "��ʼ�켣�ʷ֣�" + '\n');
			ArrayList<ArrayList<TaxiGPS>> subdivisionTrackArrayList = new ArrayList<ArrayList<TaxiGPS>>();
			if (taxiTrackArrayList.get(0).getTargetID().equals("MMC8000GPSANDASYN051113-21372-00000000")) {
				System.out.print("");
			}
			trackSubdivision(taxiTrackArrayList, sampleThreshold, subdivisionTrackArrayList);
			System.out.print(tempTaxiIDStr + "�����켣�ʷ֣�" + '\n');
			ArrayList<String> startTravelTimeArraylist = new ArrayList<String>();//���⳵��ʼ����ĳ·�ε�ʱ��
			Map<String, Double> travelTimeMap = new HashMap<String, Double>();//��ʱ��Ϊ������ĳһʱ���ͨ��·�ε�ͨ��ʱ��
			Map<String, Double> taxiMeanSpeedMap = new HashMap<String, Double>();//��ʱ��Ϊ������ĳһʱ���ͨ��·�ε�ƽ���ٶ�
			//��ʱ��Ϊ������ĳһʱ���ͨ��·��ʱ�����⳵��ʻ������·�εķ����ϵ
			Map<String, Integer> taxiTravelDirectionMap = new HashMap<String, Integer>();
			//��ʱ��Ϊ������ĳһʱ���ͨ��·��ʱ�����⳵����·�ζ˵�ID��ʻ��·�ζ˵�ID,0��Ӧ����˵�������1��Ӧʻ��·�ζ˵�����
			Map<String, int[]> taxiEntranceExitNodeIDMap = new HashMap<String, int[]>();
			//��ʱ��Ϊ������ĳһʱ���ͨ��·��¼��GPS��
			Map<String, ArrayList<MapMatchNode>> GPSTravelMap = new HashMap<String, ArrayList<MapMatchNode>>();
			for (int i = 0; i < subdivisionTrackArrayList.size(); i++) {
				ArrayList<TaxiGPS> tempGPSArrayList = subdivisionTrackArrayList.get(i);//�켣�ʷֺ�ĳ��⳵�켣  
				int count = tempGPSArrayList.size();
				TaxiGPS startTaxiGPS = tempGPSArrayList.get(0);
				TaxiGPS endTaxiGPS = tempGPSArrayList.get(count - 1);
				String time1 = startTaxiGPS.getLocalTime();
				String time2 = endTaxiGPS.getLocalTime();
				String taxiIDStr = startTaxiGPS.getTargetID();//���⳵ID
				if (taxiIDStr.equals("MMC8000GPSANDASYN051113-24710-00000000")) {
					System.out.print("");
				}				
				taxiTravelTime.setTaxiID(taxiIDStr);				
				//ʱ����չ				
				String[] startTimeArray = new String[1];				
				PubClass.obtainStartTimeAccordEndTime(time1, expandTime, startTimeArray);//ǰ��ʱ����չ
				String curStartTimeStr = startTimeArray[0];
				String[] endTimeArray = new String[1];				
				PubClass.obtainEndTimeAccordStartTime(time2, expandTime, endTimeArray);//����ʱ����չ
				String curEndTimeStr = endTimeArray[0];
				System.out.print(taxiIDStr + "��ʼ�켣��չ��" + '\n');
				ArrayList<TaxiGPS> expandTaxiGPSArrayList = new ArrayList<TaxiGPS>();//����ʱ����չ��ĳ��⳵�켣
				DatabaseFunction.obtainGPSDataFromDatabase(expandTaxiGPSArrayList, taxiIDStr, curStartTimeStr, curEndTimeStr);
				System.out.print(taxiIDStr + "�����켣��չ��" + '\n');				
				Map<Integer, ArrayList<TaxiGPS>> taxiMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
				taxiMap.put(1, expandTaxiGPSArrayList);
				ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
				ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();
				ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
				MapMatchAlgorithm.coordinateCorr(taxiMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList, GPSCorrectArrayList);//�������
				boolean isAbnormalParking = isAbnormalParkingTrack(correctedOriginalTaxiTrackArrayList);//�Ƿ�����쳣
				int linkDirection = isArraylistContainsEID(pathEIDArrayList, targetLinkID);
				MapMatchEdge nextEdge = obtainNextEdgeAccordToCurrentLinkID(pathEIDArrayList, targetLinkID);
				//���·��ID�а���Ŀ��·��
				if (linkDirection != 0 && nextEdge != null && !isAbnormalParking) {
					String[] interpolateStartTime = new String[1];
					double[] travelTime = new double[1];
					ArrayList<MapMatchNode> GPSTravelArrayList = new ArrayList<MapMatchNode>();//Ŀ��·��GPS�ϵ�
					double[] meanSpeed = new double[1];//ƽ���ٶ�
					double[]predictMinTime = new double[1];//��������ٶȹ��Ƶ���Сʱ��
					int []entranceExitNodeID = new int[2];
					obtainEntranceExitNodeID(targetEdge, linkDirection, entranceExitNodeID);//��ó��⳵���·�ζ˵�ID������·�ζ˵�ID
					//��·�ζ˵㴦����ʱ���ֵ����
					//�ڽ���ʱ���ֵ��ʱ��Ӧ�ÿ��ǵ��ٶ�Ϊ���GPS��������������ݼ���expandTaxiGPSArrayList��������eliminateZeroSpeedGPSDataArrayList
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
						System.out.print("�ѻ�õ���·���г�ʱ�䣡" + '\n');
						if (taxiIDStr.equals("MMC8000GPSANDASYN051113-22962-00000000")) {
			    			System.out.print("");
						}
					}					
				}
				else {
					System.out.print("δ��õ���·���г�ʱ�䣡" + '\n');
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
	 * ���ݳ��⳵��ʻ������·�ι�ϵ,��ó��⳵����·����ʻ��·�ζ˵��ID
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
	 * �쳣ͣ���켣�ж�
	 * ���GPS����ĳ��λ��ͣ��ʱ�䳬��continuousStaticTimeThreshold������Ϊ���쳣ͣ����Ӧ�޳���
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
	
	/*GPS���ݴ���ȥ���ٶ�Ϊ���GPS���Լ���·�ζ˵㴦��ͣ�����ݽ��г�ϡ��ͣ������ֻ����һ��GPS�㣬���˵���ٶȲ�Ϊ�㣬�Ա��ڵ�ͼƥ��
	 * ������·�ζ˵㴦����������Ȼ���ͣ�����ݳ�ϡ
	 * processedGPSDataArraylist��������GPS��
	 * taxiGPSArrayList��������GPS��
	 * targetEdge��Ŀ��·��*/
	public void processTaxiGPSData(ArrayList<TaxiGPS> processedGPSDataArraylist, ArrayList<TaxiGPS> taxiGPSArrayList, MapMatchEdge targetEdge){
		try {
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//ȥ���ٶ�Ϊ���GPS��
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
					//·�ζ˵㴦ͣ����ȡ��һ���ٶȲ�Ϊ��ĵ�
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
	
	/*�켣�ʷ�
	 * ���룺
	 * taxiTrackArrayList�����⳵GPS�켣
	 * sampleThreshold��������ֵ��Ĭ��60s��
	 * �����
	 * subdivisionTrackArrayList���ʷֹ켣
	 * ����GPS��Ĳ�����ֵ���й켣���ʷ�
	 * 1.������GPS��֮���ʱ������ֵС�ڲ�����ֵ��˵��������GPS��ΪͬһGPS�켣
	 * 		����ֵ���ڲ�����ֵ��˵��������GPS�㲻ΪͬһGPS�켣
	 * 2.�ݴˣ���GPS�켣�����ʷ֣����ɶ����켣*/
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
					//С����ֵ��˵��Ϊͬһ�켣
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
	 * �Ľ���ʱ���ֵ��:���þ��������������·���г�ʱ��
	 * 1.Ŀ��·��GPS����Ŀ�жϣ���ֻ��һ��GPS�㣬ƽ���ٶȷ�����
	 * 2.Ŀ��·���ж��GPS��
	 * 		a.����������������ж��Ƿ����ͣ���ȴ����,�����ڻ�����ͣ��ʱ���;
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
			String curLinkBeginPointTimeStr = "";//��ǰ·�����ʱ��
			String curLinkEndPointTimeStr = "";//��ǰ·���յ�ʱ��
			int linkStartGPSIndex = -1;//��ǰ·���ϵ�һ��GPS����±�
			int linkEndGPSIndex = -1;//��ǰ·�������һ��GPS����±�
			int gpsCount = taxiGPSArrayList.size();
			int []linkStartEndIndex = new int[2];
			obtainLinkStartEndGPSIndex(taxiGPSArrayList, linkID, linkStartEndIndex);
			linkStartGPSIndex = linkStartEndIndex[0];
			linkEndGPSIndex = linkStartEndIndex[1];
			//��·����ʼ�ڵ㵽�����ڵ�Ϊ�ؿ͹켣
			if (linkStartGPSIndex != -1 && linkEndGPSIndex != -1) {
				if (isCarrayPassengerTrack(taxiGPSArrayList, linkStartGPSIndex, linkEndGPSIndex)) {
					//���⳵��·����ڴ�GPS��ȷ��
					//��������ͣ�������������һ��ͣ����
					TaxiGPS entranceGPS = new TaxiGPS();//·����ڴ��ٶ�Ϊ����ʱ�����(λ�ں����ʱ��)��GPS��
					TaxiGPS exitGPS = new TaxiGPS();//·�γ��ڴ��ٶ�Ϊ����ʱ������GPS��
					MapMatchNode curBeginPoint = new MapMatchNode();//·�����
					curBeginPoint.setNodeID(-1);//Ϊ�ж�curBeginPoint�Ƿ���������֮��
					MapMatchNode curEndPoint = new MapMatchNode();//·���յ�
					curEndPoint.setNodeID(-1);
					//���Ŀ��·����GPS����
					int targetLinkGPSCount = linkEndGPSIndex - linkStartGPSIndex + 1;
					/*Ŀ��·����ֻ��һ��GPS��*/
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
					/*Ŀ��·�����ж��GPS��*/
					else {
						//���·����ڡ����ڴ��ٶ�Ϊ����ʱ�����(λ�ں����ʱ��)��GPS��
						int[] endpointIndexArray = obtainZeroSpeedMaxiTimePointAtEntranceExit(taxiGPSArrayList, entranceGPS, exitGPS, targetEdge, linkID, linkDirection, curBeginPoint, curEndPoint);
						curLinkBeginPointTimeStr = timeInterpolateEntranceProcess(taxiGPSArrayList, endpointIndexArray[0], entranceGPS, linkStartGPSIndex, linkID, curBeginPoint, targetEdge);
						curLinkEndPointTimeStr = timeInterpolateExitProcess(taxiGPSArrayList, endpointIndexArray[1], exitGPS, linkEndGPSIndex, linkID, curEndPoint, nextEdge);
						
						/*	���⳵�Ƿ��ͷ�ж�
						 *  ȡ��·���ϵ�GPS�㣬����heading���бȽϣ�����������heading�����180�����ڴ������޲����ȡ150����
						 *  ����Ϊ���⳵���˶������෴����������·���м��ͷ���� 
						 * ���̣�
						 * 1.�����·�������ͣ������ȡ����ڴ�GPS�㣬��ʱ·�������GPS����ΪendpointIndexArray[0]+1
						 * 	��û��ͣ��������ֱ��ȡ��·�������GPS����linkStartGPSIndex
						 * 2.�����·�γ�����ͣ������ȡ�ó��ڴ�GPS�㣬��ʱ·���ϵ����һ��GPS����ΪendpointIndexArray[1]
						 * 	��û��ͣ��������ֱ��ȡ��·�������һ��GPS����ΪlinkEndGPSIndex
						 * 3.�жϴ�������֮���GPS�㣬������GPS��ȥ���ٶ�Ϊ���GPS�㣩��heading֮�����180������Ϊ150�����������ֵ���������·���м��ͷ���� 
						 * */
						
						//����GPS��䷽��Ƕȵ��жϣ��Ա�֤���⳵������;�۷�
						if (linkStartGPSIndex != -1 && linkEndGPSIndex != -1 && linkStartGPSIndex < linkEndGPSIndex) {
							boolean isTravelTimeValid = true;//·��ͨ��ʱ�����Ч��
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
							//��Ŀ��·��������GPS������·��IDΪĿ��·��ʱ��������GPS�����ͨ��ʱ�������Чͨ��ʱ��
							//����ȥ��������ж������·�ε�������Ա�֤GPS��ƥ��Ψһ·��
							for (int i = linkStartGPSIndex; i < linkEndGPSIndex; i++) {
								TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
								if (taxiGPS.getBelongLineID() != linkID) {
									isTravelTimeValid = false;
									break;
								}
							}						
							//������Ŀ��·�Σ�����Ҫ��Ŀ��·�ε�����·���ϼ�����ȡ��GPS����ٶ����ֵ
							//��Ŀ��·�ο�ʼ������GPS����������ǰ�Լ������������ڵ�
							//���������·����û�е㣬��ֻ��Ŀ��·��ȡ��GPS����ٶ����ֵ
							double maxSpeed = 0;//ȡ�õ�����ٶ�
							double predictMinTime = 0;//��������ٶȹ��Ƶ���Сʱ��
							if (linkStartGPSIndex - 1 >= 0 && linkEndGPSIndex + 1 < gpsCount) {
								maxSpeed = obtainMaxSpeed(taxiGPSArrayList, linkStartGPSIndex - 1, linkEndGPSIndex + 1 );
							}
							else {
								maxSpeed = obtainMaxSpeed(taxiGPSArrayList, linkStartGPSIndex, linkEndGPSIndex);
							}
							if (maxSpeed != 0) {
								double targetEdgeLength = targetEdge.getEdgeLength();
								String predictMinTimeStr = String.format("%.2f", targetEdgeLength/maxSpeed);//����С�������λ����������������
								predictMinTime = Double.parseDouble(predictMinTimeStr);//������Сʱ��
							}							
							//���Ϊ��Чͨ��ʱ��
							if (isTravelTimeValid) {					
								if (!curLinkBeginPointTimeStr.equals("") && !curLinkEndPointTimeStr.equals("")) {
									double tempTravelTime = PubClass.obtainTimeInterval(curLinkBeginPointTimeStr, curLinkEndPointTimeStr);								
									//���ͨ��ʱ����ڹ��Ƶ���Сʱ����ܱ�֤���ͨ��ʱ�����ȷ��
									if (tempTravelTime > predictMinTime) {
										//Ŀ��·���ϵ�GPS�㣬��taxiGPSArrayList�л��
										//����ƽ���ٶ�
										travelTime[0] = tempTravelTime;
										interpolateStartTime[0] = curLinkBeginPointTimeStr;
										for (int i = linkStartGPSIndex; i <= linkEndGPSIndex; i++) {
											TaxiGPS tempTaxiGPS = taxiGPSArrayList.get(i);
											MapMatchNode tNode = new MapMatchNode();
											tNode.setX(tempTaxiGPS.getLongitude());
											tNode.setY(tempTaxiGPS.getLatitude());
											GPSTravelArrayList.add(tNode);
										}
										String meanSpeedStr = String.format("%.2f", targetEdge.getEdgeLength()/tempTravelTime);//����С�������λ����������������
										meanSpeed[0] = Double.parseDouble(meanSpeedStr);
									}
									else {
										System.out.print("ͨ��ʱ�䣺" + tempTravelTime + "������ʵ�����!");
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
	 * ��·����ڴ�����ʱ���ֵ���㴦��		
	 * @param taxiGPSArrayList	·���ϵ�GPS��
	 * @param eliminateZeroSpeedGPSDataArrayList	ȥ���ٶ�Ϊ���GPS��
	 * @param targetEdge	Ŀ��·��
	 * @param linkID	Ŀ��·��ID
	 * @param endpointEntranceIndex	��taxiGPSArrayList�л�õ�·�ζ˵㴦ͣ���ȴ������GPS������
	 * @param entranceGPS	��ڴ�ͣ���ȴ�ʱʱ�����GPS��
	 * @param exitGPS	���ڴ�ͣ���ȴ�ʱʱ�����GPS��
	 * @param linkStartGPSIndex	����GPS(eliminateZeroSpeedGPSDataArrayList)�㼯���У���ǰ·���ϵ�һ��GPS����±�
	 * @param linkEndeGPSIndex	����GPS(eliminateZeroSpeedGPSDataArrayList)�㼯���У���ǰ·�������һ��GPS����±�
	 * @param curBeginPoint	·���������
	 * @param travelTime	ͨ��ʱ��
	 * @param curLinkBeginPointTimeStr
	 */
	public String timeInterpolateEntranceProcess(ArrayList<TaxiGPS> taxiGPSArrayList, int endpointEntranceIndex, TaxiGPS entranceGPS, 
			int linkStartGPSIndex, int targetLinkID, MapMatchNode curBeginPoint, MapMatchEdge targetEdge){
		String curLinkBeginPointTimeStr = "";
		try {
			int gpsCount = taxiGPSArrayList.size();
			//·�����źŵƵ���ͣ������
			//p2���ٶ�Ϊ�㣬ͣ���ȴ�
			//��������һ��ͣ������ڣ�����������ҵ���һ��GPS��
			if (endpointEntranceIndex != -1) {
				//��������GPS��	��ڴ�GPS���Ƿ��ڶ�λƫ��Բ��
				if (entranceGPS.getIsGPSCorrected()) {						
					//�ڶ�λƫ��Բ��						
					if (isCorrectedGPSPointInLocationErrorCircle(entranceGPS, curBeginPoint)) {
						//�����һ���ٶȲ�Ϊ�㣬���øõ��ٶ��Լ�ʱ�䷴�ƾ���·��ʱ��
						String p2TimStr = entranceGPS.getLocalTime();
						int index = endpointEntranceIndex;
						if (index + 1 < taxiGPSArrayList.size()) {
							TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
							double p4Speed = p4TaxiGPS.getSpeed();
							String p4timeStr = p4TaxiGPS.getLocalTime();
							//������
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
								//�ٶ�Ϊ��
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
					//���ڶ�λƫ��Բ�ڣ����ڽ��������
					else {
						//���p3�ٶ�Ϊ�㣬����Ϊӵ�£������ȼ��ٺ��ȼ����˶�
						int index = endpointEntranceIndex;
						String p2TimStr = entranceGPS.getLocalTime();
						if (index + 1 < gpsCount) {
							TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
							//������
							if (p3TaxiGPS.getIsGPSCorrected() && p3TaxiGPS.getBelongLineID() == targetLinkID) {
								double p3speed = p3TaxiGPS.getSpeed();
								//ӵ��,�ڼ�����p4��
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
								//�ȼ���
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
			//����ڲ�����ͣ����,����GPS�������·��ID,ȷ����ǰ·���Ϸ��������ĵ�һ��GPS�������
			//����Ҫ��֤������GPS��λ��·����ڴ��˵�����࣬Ŀ����ȥ�����⳵��·����ڴ��ĵȴ�ʱ��
			//p2���ٶȲ�Ϊ��
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
						//p3���ٶȲ�Ϊ��,������ʻ
						if (p3speed > PubParameter.zeroSpeedThreshold) {						
							double timeInterval = tempDistance/p3speed;
							double approximateTimeInterval = Math.round(timeInterval); 
							curLinkBeginPointTimeStr = PubClass.obtainStartTimeAccordEndTime(p3TimeStr, (int)approximateTimeInterval);
							//��ֵʱ�䲻��p2��֮����ƽ���ٶ����¼���
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
						//p3���ٶ�Ϊ��,�ȼ���
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
			//·�����źŵƵ���ͣ������
			//p2���ٶ�Ϊ�㣬ͣ���ȴ�
			//��������һ��ͣ������ڣ�����������ҵ���һ��GPS��
			if (endpointExitIndex != -1) {
				//��������GPS��	��ڴ�GPS���Ƿ��ڶ�λƫ��Բ��
				if (exitGPS.getIsGPSCorrected()) {						
					//�ڶ�λƫ��Բ��
					if (isCorrectedGPSPointInLocationErrorCircle(exitGPS, curEndPoint)) {
						String p2TimStr = exitGPS.getLocalTime();
						//�����һ���ٶȲ�Ϊ�㣬���øõ��ٶ��Լ�ʱ�䷴�ƾ���·��ʱ��
						int index = endpointExitIndex;
						if (index + 1 < taxiGPSArrayList.size()) {
							TaxiGPS p4TaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
							double p4Speed = p4TaxiGPS.getSpeed();
							String p4timeStr = p4TaxiGPS.getLocalTime();
							//������		Ŀ��·���ڽ�·��
							if (p4TaxiGPS.getIsGPSCorrected() && p4TaxiGPS.getBelongLineID() != targetLinkID) {
								//��Ϊ��		�ȼ���
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
								//�ٶ�Ϊ�� 	ӵ��
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
					//���ڶ�λƫ��Բ�ڣ����ڽ��������
					else {
						//���p3�ٶ�Ϊ�㣬����Ϊӵ�£������ȼ��ٺ��ȼ����˶�
						String p2TimStr = exitGPS.getLocalTime();
						int index = endpointExitIndex;
						if (index + 1 < gpsCount) {
							TaxiGPS p3TaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
							//������
							if (p3TaxiGPS.getIsGPSCorrected() && p3TaxiGPS.getBelongLineID() != targetLinkID) {
								double p3speed = p3TaxiGPS.getSpeed();
								//ӵ��,�ڼ�����p4��
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
								//�ȼ���
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
			//�����ڲ�����ͣ����,����GPS�������·��ID,ȷ����ǰ·���Ϸ������������һ��GPS�������
			//����Ҫ��֤������GPS��λ��·�γ��ڿڴ��˵�����࣬Ŀ���ǰ������⳵��·�γ��ڴ��ĵȴ�ʱ��
			//p2���ٶȲ�Ϊ��
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
						//p3���ٶȲ�Ϊ��  ������ʻ
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
						//p3���ٶ�Ϊ��,�ȼ���
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
			int tempStartIndex = -1;//ȥ���ٶ�Ϊ��ĵ��GPS�㼯����(eliminateZeroSpeedGPSDataArrayList),Ŀ��·�εĿ�ʼ�������������ٶȷ���㼯�ϣ�
			int tempEndIndex = -1;
			//·����ڴ�
			if (startIndex != -1) {
				String localTimeStr = "";//�ٶȷ����Ŀ�ʼʱ��
				//�˴�+1����ʾ����ȡ�ٶ�Ϊ���GPS�㣬��ΪeliminateZeroSpeedGPSDataArrayList�����ٶȷ���ĵ�
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
	 * ����Ŀ��·�ε�һ��GPS������һ��GPS����
	 * @param taxiGPSArrayList
	 * @param linkID	Ŀ��·��ID
	 * @param linkStartEndIndex	�ֱ��ſ�ʼGPS�����������һ��GPS������
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
	 * Ŀ��·���ϵĹ켣�Ƿ�Ϊ�ؿ�״̬
	 * @param taxiGPSArrayList	
	 * @param startIndex	Ŀ��·��GPS���
	 * @param endIndex	Ŀ��·��GPS�յ�
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
	 * ���Ŀ��·����GPS����Ŀ
	 * @param taxiGPSArrayList	GPS����Ŀ
	 * @param linkID	·��ID
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
	 * �ж�GPS���Ƿ��ڶ�λƫ��Բ��
	 * @param taxiGPS	���⳵GPS��
	 * @param curPoint	·�ζ˵�
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
	 * �жϾ�����GPS���Ƿ��ڽ����������
	 * @param taxiGPS	����GPS��
	 * @param curPoint	����������Ըõ�ΪԲ��
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
	 * �ж�GPS���Ƿ��Ѿ�������
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
	
	/*ʱ���ֵ���㣺����Ŀ��·�ζ˵㴦�ٶȺ㶨���裬��Ŀ��·�ζ˵㴦����ʱ���ֵ����
	 * ����ֵ���㻹�������⣩
	 * ���룺
	 * taxiGPSArrayList��GPS�켣
	 * targetLinkNodeArrayList��Ŀ��·�ε㼯��
	 * linkID��Ŀ��·��ID
	 * linkDirection��·�εķ����ԣ�1��ʾ�뵱ǰtaxi�˶�����һ�£�-1��ʾ�෴
	 * interpolateStartTime������·�ζ˵��׵��ֵ���ʱ�̣�(����ڵ�ǰ���⳵taxi)
	 * travelTime����ǰ���⳵��·��ͨ��ʱ��
	 * GPSTravelArrayList��Ŀ��·���ϵ�GPS��·��
	 * �����
	 * interpolateStartTime��GPS��ս���·�εĲ�ֵʱ��
	 * travelTime��GPS��ͨ��·�ε�ʱ��,��λΪ��
	 * meanSpeed��·��ƽ���ٶ�
	 * predictMinTime����������ٶȹ��Ƶ���Сʱ��
	 * �˵㴦�ٶ�Ϊ���GPS���𵽹ؼ�����
	 * 1.Ŀ��·����ĩGPS��ȷ��������GPS�ĺ�ѡ·�Σ�GPS�ٶ�Ϊ��ĵ�û�к�ѡ·�Σ��Ƿ����Ŀ��·�Σ�ȷ��Ŀ��·���ϵĵ�һ��GPS�㣨startGPS��
	 * 		�Լ����һ��GPS��(endGPS)
	 * 		**Ŀ��·�ε���ڴ������˵㻺�������뾶endpointBufferRadius��
	 * 			if�������ٶ�Ϊ��GPS�㣩
	 * 				ȡ���ٶ�Ϊ����ʱ������GPS����ΪstartStartGPS���˵���Ŀ��·�����ڵ�·���ϣ������һGPS����ΪstartGPS������ʱ���ֵ//˵���ڽ������ڴ��ȴ��źŵ�
	 * 			else
	 * 				����һ�㷽������ʱ���ֵ��
	 * 		**Ŀ��·�εĳ��ڴ������˵㻺�����뾶endpointBufferRadius
	 * 			if�������ٶ�Ϊ��GPS�㣩
	 * 				ȡ���ٶ�Ϊ����ʱ������GPS����ΪendGPS���˵���Ŀ��·���ϣ������һGPS����ΪendEndGPS������ʱ���ֵ	//˵���ڽ������ڴ��ȴ��źŵ�
	 * 			else
	 * 				����һ�㷽������ʱ���ֵ��
	 * 
	 * 2.�ٽ�·��GPS��ȷ����ȷ����Ŀ��·������ĩGPS��������GPS�㣬
	 * 		����startGPS�������ĵ�ΪstartStartGPS,��endGPS�������ĵ�ΪendEndGPS
	 * 3.ʱ���ֵ���㣺��·����ĩ������ڵ�ǰ���⳵���ԣ�
	 * 	ȷ�����⳵ͨ����ǰ·���׵�ʱ�̣���·�ε�ǰ���ΪcurBeginPoint��startstartGPS��beginPoint�ľ���ΪdistSStartBegin��
	 * 		ʱ��ΪtimeSStartCurBegin��startGPS��curBeginPoint�ľ���ΪdistStartCurBegin��ʱ��ΪtimeStartCurBegin��
	 * 		startStartGPS��startGPS����ʱ��ΪtempTime,�����ٶ�ƽ���ļ��裬beginPoint��startGPS��ʱ��Ϊ��
	 * 		timeStartBegin = (distStartBegin/(distSStartBegin + distStartBegin))*tempTime;
	 * 		�ݴ�ȷ�����⳵ͨ��·������ʱ�̣��˴�·�ζ˵�����ڳ��⳵���ԣ�
	 *  ȷ�����⳵ͨ����ǰ·��ĩ��ʱ�̣���·���յ�ΪendPoint
	 *  
	 *  2014/11/06
	 *  ��������·���ϵ�GPS�㣬��·����GPS�������ٶ�
	 *  ������ٶ���·����Сͨ��ʱ�䣬������Сͨ��ʱ���ж���õ�ͨ��ʱ���Ƿ����
	 *  */
	public void timeInterpolateCalculateAccordMean(ArrayList<TaxiGPS> taxiGPSArrayList, MapMatchEdge targetEdge, int linkID, int linkDirection,
			String[]interpolateStartTime, double[] travelTime, ArrayList<MapMatchNode> GPSTravelArrayList, double[] meanSpeed){
		try {
			String curLinkBeginPointTimeStr = "";//��ǰ·�����ʱ��
			String curLinkEndPointTimeStr = "";//��ǰ·���յ�ʱ��
			int linkStartGPSIndex = 0;//����GPS(eliminateZeroSpeedGPSDataArrayList)�㼯���У���ǰ·���ϵ�һ��GPS����±�
			int linkEndGPSIndex = 0;//����GPS�㼯���У���ǰ·�������һ��GPS����±�
			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();//ȥ���ٶ�Ϊ��ĵ�
			DatabaseFunction.eliminateZeroSpeedGPSData(taxiGPSArrayList, eliminateZeroSpeedGPSDataArrayList);//ȥ���ٶ�Ϊ���GPS��,��Ϊ�ٶ�Ϊ��ĵ�û�к�ѡ��·��
			int nonZeroCount = eliminateZeroSpeedGPSDataArrayList.size();//�ٶȷ�������Ŀ
			//2014-09-24�޸�
			//���⳵��·����ڴ�GPS��ȷ��
			//��������ͣ�������������һ��ͣ����
			TaxiGPS entranceGPS = new TaxiGPS();//·����ڴ��ٶ�Ϊ����ʱ�����(λ�ں����ʱ��)��GPS��
			TaxiGPS exitGPS = new TaxiGPS();//·�γ��ڴ��ٶ�Ϊ����ʱ������GPS��
			MapMatchNode curBeginPoint = new MapMatchNode();//·�����
			curBeginPoint.setNodeID(-1);//Ϊ�ж�curBeginPoint�Ƿ���������֮��
			MapMatchNode curEndPoint = new MapMatchNode();//·���յ�
			curEndPoint.setNodeID(-1);
			//���·����ڡ����ڴ��ٶ�Ϊ����ʱ�����(λ�ں����ʱ��)��GPS��
			int[] endpointIndexArray = obtainZeroSpeedMaxiTimePointAtEntranceExit(taxiGPSArrayList, entranceGPS, exitGPS, targetEdge, linkID, linkDirection, curBeginPoint, curEndPoint);
			//·������ж�
			//·�����źŵƵ���ͣ������
			//��������һ��ͣ������ڣ�����������ҵ���һ��GPS��
			if (endpointIndexArray[0] != -1) {
				int index = endpointIndexArray[0];
				if (index + 1 < taxiGPSArrayList.size()) {
					TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
					curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(entranceGPS, nextTaxiGPS, curBeginPoint);//·����ʼ�˵��Ӧʱ��				
				}
			}
			//����ڲ�����ͣ����,��ʱ��ȥ���ٶ�Ϊ���GPS�㣨eliminateZeroSpeedGPSDataArrayList����ʱ��,��Ϊ�����ĵ���к�ѡ��·
			//ȷ����ǰ·���Ϸ��������ĵ�һ��GPS�������,����������GPS���ѡ·�ΰ���Ŀ��·�β�����һ��GPS���ѡ·��Ҳ����Ŀ��·��
			//����Ҫ��֤������GPS��λ��·����ڴ��˵�����࣬Ŀ����ȥ�����⳵��·����ڴ��ĵȴ�ʱ��
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
				//�жϵ�ǰ·���Ƿ�������һ��GPS��ֱ�����ڵ���һ��GPS�㣬�Լ���ǰ·�����һ��GPS��ֱ�����ڵ���һ��GPS��
				//·���׵�ʱ���ֵ,�����ǰ·�ε�һ��GPS�������Ϊ0�����ܽ���ʱ���ֵ����
				//ͬ����·���׵��ֵ���������1.�׵���Ŀ��·���ϣ�2.�׵���Ŀ��·�ε��ڽ�·����
				//��·����ڴ�����ǰ��ͺ���ʱ���ֵ���㣬˼·��·��ĩ���ֵ����
				if (linkStartGPSIndex != 0) {
					TaxiGPS startTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
					TaxiGPS startStarTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex - 1);//ֱ�����ڵ���һ��GPS��
					MapMatchNode tStartNode = new MapMatchNode();
					MapMatchNode tStartStartNode = new MapMatchNode();
					tStartNode.setX(startTaxiGPS.getLongitude());
					tStartNode.setY(startTaxiGPS.getLatitude());
					tStartStartNode.setX(startStarTaxiGPS.getLongitude());
					tStartStartNode.setY(startStarTaxiGPS.getLatitude());	
					//·�ζ˵�curBeginPointλ����GPS��֮��
					if (isProjPointBetweenSE(tStartStartNode, tStartNode, curBeginPoint)) {
						curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(startStarTaxiGPS, startTaxiGPS, curBeginPoint);//��ֵ����·�ζ˵��Ӧʱ��
					}
					//��ֵʱ��Ϊ�գ����ܽ���ǰ��ʱ���ֵ�����к���ʱ���ֵ
					if (curLinkBeginPointTimeStr.equals("")) {
						if (linkStartGPSIndex != nonZeroCount - 1) {
							TaxiGPS tStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
							TaxiGPS tNextStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex + 1);//����GPS��
							MapMatchNode tStartTaxiGPSNode = new MapMatchNode();
							MapMatchNode tNextStartTaxiGPSNode = new MapMatchNode();
							tStartTaxiGPSNode.setX(tStartTaxiGPS.getLongitude());
							tStartTaxiGPSNode.setY(tStartTaxiGPS.getLatitude());
							tNextStartTaxiGPSNode.setX(tNextStartTaxiGPS.getLongitude());
							tNextStartTaxiGPSNode.setY(tNextStartTaxiGPS.getLatitude());
							//·�ζ˵�curBeginPointλ����GPS��֮��
							if (isProjPointBetweenSE(tStartTaxiGPSNode, tNextStartTaxiGPSNode, curBeginPoint)) {
								curLinkBeginPointTimeStr = obtainTimeMomentAccordTimeInterpolate(tStartTaxiGPS, tNextStartTaxiGPS, curBeginPoint);//��ֵ����·�ζ˵��Ӧʱ��							
							}							
						}						
					}
				}
			}
			
			//·�γ����ж�
			//���ڴ����źŵƵ���ͣ������
			if (endpointIndexArray[1] != -1) {
				int index = endpointIndexArray[1];
				if (index + 1 < taxiGPSArrayList.size()) {
					TaxiGPS nextTaxiGPS = taxiGPSArrayList.get(index + 1);//���һ��ͣ�������һ��GPS��
					curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(exitGPS, nextTaxiGPS, curEndPoint);//·�ζ˵��Ӧʱ��//·����ʼ�˵��Ӧʱ��					
				}
			}
			//��������
			//��ʱ��ȥ���ٶ�Ϊ���GPS�㼯
			else {
				//ȷ����ǰ·���Ϸ������������һ��GPS�������������������GPS���ѡ·�ΰ���Ŀ��·�β�����һ��GPS���ѡ·��Ҳ����Ŀ��·��
				//���Ҵ�����GPS��Ҫλ��·�γ��ڴ��˵�����࣬Ŀ���Ǳ�֤����·�εĳ��⳵�ܹ�ͨ������ȥ����Щ��·����;�ۻصĳ��⳵�켣
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
				//·��ĩ���ֵ���������1.ĩ����Ŀ��·���ϣ�2.ĩ����Ŀ��·�ε��ڽ�·����
				//1.ĩ����Ŀ��·����,�����ʱ���ֵ
				//�����ǰ·�����GPS�������Ϊcount - 1�����ܽ���ʱ���ֵ����
				//2.ĩ����Ŀ��·���ڽ�·���ϣ���ǰʱ���ֵ
				//���Ƚ��к���ʱ���ֵ�������ֵΪ�գ����ٽ���ǰ��ʱ���ֵ
				//������ǰ��ֵ��������ֵ�����Զ�ѡ��һ��ʱ���ֵ
				if (linkEndGPSIndex != nonZeroCount - 1) {
					TaxiGPS endTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
					TaxiGPS endEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex + 1);
					MapMatchNode tEndNode = new MapMatchNode();
					MapMatchNode tEndEndNode = new MapMatchNode();
					tEndNode.setX(endTaxiGPS.getLongitude());
					tEndNode.setY(endTaxiGPS.getLatitude());
					tEndEndNode.setX(endEndTaxiGPS.getLongitude());
					tEndEndNode.setY(endEndTaxiGPS.getLatitude());
					//2014-11-26�޸�	
					//·�ζ˵�curEndPointλ����GPS��֮��
					if (isProjPointBetweenSE(tEndNode, tEndEndNode, curEndPoint)) {
						curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(endTaxiGPS, endEndTaxiGPS, curEndPoint);//·�ζ˵��Ӧʱ��
					}	
					//�����ܲ�ֵ������ǰ��ʱ���ֵ
					if (curLinkEndPointTimeStr.equals("")) {
						if (linkEndGPSIndex != 0) {
							TaxiGPS tEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
							TaxiGPS tBeforeEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex - 1);//ǰ��GPS��
							MapMatchNode tEndTaxiGPSNode = new MapMatchNode();
							MapMatchNode tBeforeEndTaxiGPSNode = new MapMatchNode();
							tEndTaxiGPSNode.setX(tEndTaxiGPS.getLongitude());
							tEndTaxiGPSNode.setY(tEndTaxiGPS.getLatitude());
							tBeforeEndTaxiGPSNode.setX(tBeforeEndTaxiGPS.getLongitude());
							tBeforeEndTaxiGPSNode.setY(tBeforeEndTaxiGPS.getLatitude());
							//·�ζ˵�curEndPoint�Ƿ�λ����GPS��֮��
							if (isProjPointBetweenSE(tBeforeEndTaxiGPSNode, tEndTaxiGPSNode, curEndPoint)) {
								curLinkEndPointTimeStr = obtainTimeMomentAccordTimeInterpolate(tBeforeEndTaxiGPS, tEndTaxiGPS, curEndPoint);//·�ζ˵��Ӧʱ��
							}				
						}
					}
				}
			}
			/*ȡ��·���ϵ�GPS�㣬����heading���бȽϣ�����������heading�����180�����ڴ������޲����ȡ150����
			 *����Ϊ���⳵���˶������෴����������·���м��ͷ���� 
			 * ���̣�
			 * 1.�����·�������ͣ������ȡ����ڴ�GPS�㣬���� eliminateZeroSpeedGPSDataArrayList��ȡ����Ӧ��GPS��������ʱ·�������GPS����ΪendpointIndexArray[0]+1
			 * 	��û��ͣ��������ֱ��ȡ��·�������GPS����linkStartGPSIndex
			 * 2.�����·�γ�����ͣ������ȡ�ó��ڴ�GPS�㣬���� eliminateZeroSpeedGPSDataArrayList��ȡ����Ӧ��GPS����,��ʱ·���ϵ����һ��GPS����ΪendpointIndexArray[1]
			 * 	��û��ͣ��������ֱ��ȡ��·�������һ��GPS����ΪlinkEndGPSIndex
			 * 3.�жϴ�������֮���GPS�㣬������GPS��ȥ���ٶ�Ϊ���GPS�㣩��heading֮�����180������Ϊ150�����������ֵ���������·���м��ͷ���� 
			 * */
			int tempStartIndex = -1;//ȥ���ٶ�Ϊ��ĵ��GPS�㼯����(eliminateZeroSpeedGPSDataArrayList),Ŀ��·�εĿ�ʼ�������������ٶȷ���㼯�ϣ�
			int tempEndIndex = -1;
			//·����ڴ�
			if (endpointIndexArray[0] != -1) {
				String localTimeStr = "";//�ٶȷ����Ŀ�ʼʱ��
				//�˴�+1����ʾ����ȡ�ٶ�Ϊ���GPS�㣬��ΪeliminateZeroSpeedGPSDataArrayList�����ٶȷ���ĵ�
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
			//·�γ��ڴ�,ȡ���ٶȲ�Ϊ���GPS��
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
			//����GPS��䷽��Ƕȵ��жϣ��Ա�֤���⳵������;�۷�
			if (tempStartIndex != -1 && tempEndIndex != -1 && tempStartIndex < tempEndIndex) {
				boolean isTravelTimeValid = true;//·��ͨ��ʱ�����Ч��
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
				//2014/11/26�޸�
				//��Ŀ��·��������GPS���ѡ·�ΰ���Ŀ��·��ʱ��������GPS�����ͨ��ʱ�������Чͨ��ʱ��
				//����ȥ��������ж������·�ε�������Ա�֤GPS��ƥ��Ψһ·��
				for (int i = tempStartIndex; i < tempEndIndex; i++) {
					TaxiGPS taxiGPS = eliminateZeroSpeedGPSDataArrayList.get(i);
					ArrayList<MapMatchEdge> tempCandidateEdgeSetArrayList = taxiGPS.getCandidateEdgeSetArrayList();
					if (!isEdgeArraylistContainEdgeID(tempCandidateEdgeSetArrayList, linkID)) {
						isTravelTimeValid = false;
						break;
					}
				}
				
				//������Ŀ��·�Σ�����Ҫ��Ŀ��·�ε�����·���ϼ�����ȡ��GPS����ٶ����ֵ
				//��Ŀ��·�ο�ʼ������GPS����������ǰ�Լ������������ڵ�
				//���������·����û�е㣬��ֻ��Ŀ��·��ȡ��GPS����ٶ����ֵ
				double maxSpeed = 0;//ȡ�õ�����ٶ�
				double predictMinTime = 0;//��������ٶȹ��Ƶ���Сʱ��
				if (tempStartIndex - 2 >= 0 && tempEndIndex + 2 < nonZeroCount) {
					maxSpeed = obtainMaxSpeed(eliminateZeroSpeedGPSDataArrayList, tempStartIndex - 2, tempEndIndex + 2 );
				}
				else {
					maxSpeed = obtainMaxSpeed(eliminateZeroSpeedGPSDataArrayList, tempStartIndex, tempEndIndex);
				}
				if (maxSpeed != 0) {
					double targetEdgeLength = targetEdge.getEdgeLength();
					String predictMinTimeStr = String.format("%.2f", targetEdgeLength/maxSpeed);//����С�������λ����������������
					predictMinTime = Double.parseDouble(predictMinTimeStr);//������Сʱ��
				}	
				
				//���Ϊ��Чͨ��ʱ��
				if (isTravelTimeValid) {					
					if (!curLinkBeginPointTimeStr.equals("") && !curLinkEndPointTimeStr.equals("")) {
						travelTime[0] = PubClass.obtainTimeInterval(curLinkBeginPointTimeStr, curLinkEndPointTimeStr);
						interpolateStartTime[0] = curLinkBeginPointTimeStr;
						//���ͨ��ʱ����ڹ��Ƶ���Сʱ����ܱ�֤���ͨ��ʱ�����ȷ��
						if (travelTime[0] > predictMinTime) {
							//���taxiGPSArrayList�ж�ӦlinkStartGPSIndex��linkEndGPSIndex�Ŀ�ʼ����������
							TaxiGPS linkStartTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkStartGPSIndex);
							String linkStartTimeStr = linkStartTaxiGPS.getLocalTime();
							TaxiGPS linkEndTaxiGPS = eliminateZeroSpeedGPSDataArrayList.get(linkEndGPSIndex);
							String linkEndTimeStr = linkEndTaxiGPS.getLocalTime();
							int startIndex = -1;//taxiGPSArrayList������·�εĿ�ʼ����������
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
							//Ŀ��·���ϵ�GPS�㣬��taxiGPSArrayList�������ٶ�Ϊ���GPS�㣩�л��
							//����ƽ���ٶ�
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
								String meanSpeedStr = String.format("%.2f", totalSpeed/calCount);//����С�������λ����������������
								meanSpeed[0] = Double.parseDouble(meanSpeedStr);
							}
						}
						else {
							System.out.print("ͨ��ʱ�䣺" + travelTime[0] + "������ʵ�����!");
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
	 * ��GPS�㼯��taxiGPSArrayList�л���ٶ�����
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
	
	/*���·����ڡ����ڴ��ٶ�Ϊ����ʱ�����(λ�ں����ʱ��)��GPS��
	 * �������飬��һ��ֵ��ڴ�GPS���������ڶ���ֵ���ڴ�GPS��������-1��ʾû���ҵ�·��ͣ����
	 * taxiGPSArrayList:GPS�켣��
	 * entranceGPS����ڴ��ٶ�Ϊ����ʱ������GPS��
	 * exitGPS�����ڴ��ٶ�Ϊ�㣨С��һ����ֵ����ʱ������GPS��
	 * MapMatchEdge��Ŀ��·��
	 * linkDirection��GPS����·�η����Թ�ϵ
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
			//ͬ��
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
				//�þ�����GPS��
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				MapMatchNode node = new MapMatchNode();
				node.setX(taxiGPS.getCorrectLongitude());
				node.setY(taxiGPS.getCorrectLatitude());
				//��ڴ��ٶ�Ϊ���GPS��(���ܴ����ٶ����)
				if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold && PubClass.distance(curBeginPoint, node) < PubParameter.endpointBufferRadius ) {
					entranceGPSArrayList.add(taxiGPS);
				}
				//���ڴ��ٶ�Ϊ���GPS��
				if (taxiGPS.getSpeed() < PubParameter.zeroSpeedThreshold && PubClass.distance(curEndPoint, node) < PubParameter.endpointBufferRadius) {
					exitGPSArrayList.add(taxiGPS);
				}					
			}
			//�����ڳ��ڴ��ٶ�Ϊ����ʱ������GPS��
			//��ڴ�
			if (entranceGPSArrayList.size() != 0) {
				if (entranceGPSArrayList.size() == 1) {
					TaxiGPS taxiGPS = entranceGPSArrayList.get(0);
					entranceGPS.setLongitude(taxiGPS.getCorrectLongitude());
					entranceGPS.setLatitude(taxiGPS.getCorrectLatitude());
					entranceGPS.setLocalTime(taxiGPS.getLocalTime());
					entranceGPS.setHeading(taxiGPS.getHeading());
				}
				else {
					String maxTimeStr = entranceGPSArrayList.get(0).getLocalTime();//���ʱ��
					int index = 0;//���ʱ������
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
			//���ڴ�
			if (exitGPSArrayList.size() != 0) {
				if (exitGPSArrayList.size() == 1) {
					TaxiGPS taxiGPS = exitGPSArrayList.get(0);
					exitGPS.setLongitude(taxiGPS.getLongitude());
					exitGPS.setLatitude(taxiGPS.getLatitude());
					exitGPS.setLocalTime(taxiGPS.getLocalTime());
					exitGPS.setHeading(taxiGPS.getHeading());
				}
				else {
					String maxTimeStr = exitGPSArrayList.get(0).getLocalTime();//���ʱ��
					int index = 0;//���ʱ������
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
			//�����ڴ������ڴ�GPSͣ��������
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
			//����ͣ������
			if (exitGPS.getLongitude() != 0) {
				for (int i = 0; i < taxiGPSArrayList.size(); i++) {
					TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
					String taxiTimeStr = taxiGPS.getLocalTime();
					//����ͣ������
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
	
	/*���ʱ���ֵ
	 * startTaxiGPS����ʼGPS�㣬��֪ʱ��,��Ӧ��startStartGPS
	 * endTaxiGPS���յ�GPS�㣬��֪ʱ��,��Ӧ��startGPS
	 * middleNode����Ӧ��·�ζ˵㣬����startTaxiGPS��endTaxiGPS֮�䣬�����Ӧ��ʱ��*/
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
	 * ����⳵��ʻ������·�η���֮��Ĺ�ϵ
	 * �����⳵��ʻ������·�����յ㷽������֮��н�С��90�㣬��Ϊͬ�򣬷���1��
	 * ���򣬷���-1��
	 * û�л�÷����ϵ���򷵻�0��
	 * @param targetEdge
	 * @param GPSArrayList
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public int obtainDirectionBetweenTaxiAndLink(MapMatchEdge targetEdge, ArrayList<TaxiGPS> GPSArrayList, int startIndex, int endIndex){
		int direction = 0;
		try {
			//Ŀ��·��GPS�����һ��
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
	 * arraylist���Ƿ����ĳԪ�صĺ���
	 * **********************************************************/
	/*arraylist1��Ԫ��EID�Ƿ����arraylist2�е�����Ԫ��EID
	 * ���������򷵻�true
	 * ���򣬷���false*/
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
	
	/*EdgeArraylist���Ƿ������edge
	 * ����edgeID�ж��Ƿ�Ϊͬһ��edge*/
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
	
	/*�ж�EdgeArraylist���Ƿ������edgeID
	 * ������������true
	 * ���򣬷���false*/
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
	
	/*arraylist1��Ԫ��EID�Ƿ����ĳһEID
	 * ���������򷵻ظ�EID�ķ����ϵ1����-1
	 * ���򣬷���0*/	
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
	 * ����·���Լ���ǰ·��ID���·�����뵱ǰ·��ID���ڵ���һ·��ID
	 * @param arraylist1	·��
	 * @param EID	·��ID
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
						nextMapMatchEdge = obtainTargetEdge(nextLinkID, MapMatchAlgorithm.instance().polylineCollArrayList);//���Ŀ��·��
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
	
	/*�жϳ��⳵ID�Ƿ���eligibleTaxiIDArrayList�У���ѡ��ʱ����С�ĳ��⳵ID
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
	 * ���������غ���
	 * ************************************************************/	
	/*ƽ�����ڵ�֮��ľ���*/
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
	
	/*��ƽ���߶εľ��룺�����cNode��startNode��endNode��������߶εľ��룩
	 * ���߶ζ�����ֱ�ߣ���㵽ֱ�ߵľ��벻ͬ
	 * ��cNode��Աߵ������н�Ϊ��ǣ���ΪͶӰ����
	 * ����һ��Ϊ�۽ǣ���Ϊ�õ㵽�ö۽ǵ�ľ���
	 */
	public double distancePointToLineSegment(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode)
	{
		double angleStar = 0;//��㴦�н�
		double angleEnd = 0;//�յ㴦�н�
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
				//���յ����� ��ģ   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//����Լ��뵱ǰ��������ģ
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;
				double scNodeDis = distance(startNode, cNode);
				//���յ���������㵱ǰ������ �������н�
				angleStar = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//�յ��Լ��뵱ǰ��������ģ
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//������������յ㵱ǰ������ �������н�
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
	
	/*�㵽ֱ�ߵľ��룺��㵽�߶εľ��벻ͬ*/
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
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				double seNodeDis = distance(starNode, endNode);
				double scNodeDis = distance(starNode, cNode);
				double seDeltX = endNodeX-starNodeX;
				double seDeltY = endNodeY-starNodeY;				
				double scDeltX = cNodeX-starNodeX;
				double scDeltY = cNodeY-starNodeY;				
				//�������н�
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
