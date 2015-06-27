package utilityPackage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.MapMatchNode;
import mapMatchingGPS.TaxiGPS;

public class PubClass {
	/***********************************************************************************************
	 * ����������ƽ�������ת��
	 * *********************************************************************************************/
	/* ��γ������ĵ�ת��Ϊƽ������
	 * xy:����ת������������
	 * L0:���������߾��ȣ��人��Ϊ��114
	 * ����������λת��Ϊ����
	 * */
	private static final double a = 6378137, e = 0.0066943799013;/////////���򳤰뾶�͵�һƫ���ʵ�ƽ��
	public static void coordinateTransToPlaneCoordinate(MapMatchNode tNode, double L0, double []xy){
		/////////���뾶�͵�һƫ���ʵ�ƽ��                               /////////���������߾��ȣ���λ������  
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
//        a=W*DH+a;///////��ĳ��î��Ȧ���ʰ뾶�仯���㳤����仯��ƫ���ʲ���//ΪʲôҪ�������룬��ʱ�����к�a���ΪNaN
//        System.out.print(a + ";" + '\n');
//        if (Math.abs(a - 6378137) > 0) {
//        	System.out.print("");
//		}
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
    public static double CalMeridian(double B)
    {
        
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
    
    /// <summary>
    /// �ɸ�˹ƽ���������LB
    ///���ؾ�γ������
    /// </summary>
    public static void coordinateTransToLB(double L0, double x, double y, double []LB)
    {
    	double pi = Math.PI;
    	L0 = L0 * pi/180;
        double e1 = e / (1 - e);
        double A =a*(1-e)*( 1 + 3.0 / 4.0 * e + 45.0 / 64.0 * Math.pow(e, 2) + 175.0 / 256.0 * Math.pow(e, 3) + 11025.0 / 16384.0 * Math.pow(e, 4) );
        double B = a * (1 - e) * (3.0 / 4.0 * e + 45.0 / 64.0 * Math.pow(e, 2) + 175.0 / 256.0 * Math.pow(e, 3) + 11025.0 / 16384.0 * Math.pow(e, 4));
        double C = a * (1 - e) * (15.0 / 32.0 * Math.pow(e, 2) + 175.0 / 384.0 * Math.pow(e, 3) + 3675.0 / 8192.0 * Math.pow(e, 4));
        double D = a*(1-e)*(35.0 / 96.0 * Math.pow(e, 3) + 735.0 / 2048.0 * Math.pow(e, 4) );
        double E = a*(1-e)*(315.0 / 1024.0 * Math.pow(e, 4) );
        double dBf = 10000;
        double Bf = 0;
        double Bf0 = x / A;
        while (Math.abs(dBf) > 0.00000000001)
        {
           double FBf = -B * Math.cos(Bf0) * Math.sin(Bf0)
                   - C * Math.cos(Bf0) * Math.pow(Math.sin(Bf0), 3)
                   - D * Math.cos(Bf0) * Math.pow(Math.sin(Bf0), 5)
                   - E * Math.cos(Bf0) * Math.pow(Math.sin(Bf0), 7);
             Bf = (x-FBf) / A;
            dBf = Bf - Bf0;
            Bf0 = Bf;
        }

        double Mf = a * (1 - e) * Math.pow(1 - e * Math.pow(Math.sin(Bf), 2), -1.5);
        double Nf = a * Math.pow(1 - e * Math.pow(Math.sin(Bf), 2), -0.5);
        double tf = Math.tan(Bf);
        double it2f = e1* Math.pow(Math.cos(Bf), 2);
        double b = Bf - tf / (2 * Mf * Nf) * y * y + tf / (24 * Mf * Math.pow(Nf, 3)) * (5 + 3 * tf * tf + it2f - 9 * it2f * tf * tf)*Math.pow(y,4)
            - tf / (720.0 * Mf * Math.pow(Nf, 5)) * (61 + 90 * tf * tf + 45 * Math.pow(tf, 4)) * Math.pow(y, 6);
        double l = L0 + 1.00 / (Nf * Math.cos(Bf)) * y - 1.0 / (6 * Math.pow(Nf, 3) * Math.cos(Bf)) * (1 + 2 * tf * tf + it2f) *Math.pow(y,3)+
            1.0 / (120.0 * Math.pow(Nf, 5) * Math.cos(Bf)) * (5 + 28 * tf * tf + 24 * tf * tf * tf * tf + 6 * it2f + 8 * it2f * tf * tf) * Math.pow(y, 5);
        l = l * 180 / pi;
        b = b * 180 / pi;
        LB[0] = l;
        LB[1] = b;
    }
    
    /*�жϵ�tNode�Ƿ�����centerNodeΪ���ĵĻ���뾶Ϊradius��Բ��
	 * ���ǣ�����true
	 * ���򣺷���false*/
	public static boolean isNodeInCircle(MapMatchNode centerNode, MapMatchNode tNode, double radius){
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
    
	/*�жϵ�tNode�Ƿ���������leftDownNode,rightTopNodeΪRectangle����������
	 * ���ǣ�����true
	 * ���򣺷���false*/
	public static boolean isNodeInSquare(Double[] leftDownNode, Double[] rightTopNode, MapMatchNode tNode){
		try {
			double []xy = new double[2];
			coordinateTransToPlaneCoordinate(tNode, PubParameter.wuhanL0, xy);//ת����ƽ��
			double tNodeX = xy[0];
			double tNodeY = xy[1];
			double minX = leftDownNode[0];
			double minY = leftDownNode[1];
			double maxX = rightTopNode[0];
			double maxY = rightTopNode[1];
			if(tNodeX >= minX && tNodeX <= maxX && tNodeY >= minY && tNodeY <= maxY ){
				return true;
			}
			else
				return false;		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			return false;
		}
	}
	
	/***********************************************************************************************
	 * ������μ���
	 * *********************************************************************************************/
	/*��Edge���������
	 * ���ؾ��ε����½ǵ�����Ͻǵ�����
	 * leftDownNode�����½ǵ�
	 * rightTopNode�����Ͻǵ�*/
	public static void BoundingRectangle(Double[] leftDownNode, Double[] rightTopNode, MapMatchEdge tEdge){
		try {
			ArrayList<MapMatchNode> pointCollArrayList = tEdge.getPointCollArrayList();
			if (pointCollArrayList.size() != 0) {
				double minL = pointCollArrayList.get(0).x;
				double minB = pointCollArrayList.get(0).y;
				double maxL = pointCollArrayList.get(0).x;
				double maxB = pointCollArrayList.get(0).y;
				for (int i = 0; i < pointCollArrayList.size(); i++) {
					MapMatchNode tNode = pointCollArrayList.get(i);
					if (minL > tNode.x) {
						minL = tNode.x;
					}
					if (minB > tNode.y) {
						minB = tNode.y;
					}
					if (maxL < tNode.x) {
						maxL = tNode.x;
					}
					if (maxB < tNode.y) {
						maxB = tNode.y;
					}
				}
				MapMatchNode minNode = new MapMatchNode();
				MapMatchNode maxNode = new MapMatchNode();
				minNode.setX(minL);
				minNode.setY(minB);
				maxNode.setX(maxL);
				maxNode.setY(maxB);
				double []minXY = new double[2];
				double []maxXY = new double[2];
				PubClass.coordinateTransToPlaneCoordinate(minNode, PubParameter.wuhanL0, minXY);
				PubClass.coordinateTransToPlaneCoordinate(maxNode, PubParameter.wuhanL0, maxXY);
				leftDownNode[0] = minXY[0];
				leftDownNode[1] = minXY[1];
				rightTopNode[0] = maxXY[0];
				rightTopNode[1] = maxXY[1];				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/*���Ŀ��·�ε�������Σ����ؾ��η�Χ�ľ�γ�������ʾ
	 * leftDownLB�����½ǵ�
	 * rightTopLB�����Ͻǵ�
	 * tEdge:Ŀ��·��
	 * radius������뾶*/
	public static void boundingRectangleLongLat(double[] leftDownLB, double[] rightTopLB, MapMatchEdge tEdge, double radius){
		try {
			ArrayList<MapMatchNode> pointCollArrayList = tEdge.getPointCollArrayList();
			if (pointCollArrayList.size() != 0) {
				double minL = pointCollArrayList.get(0).x;
				double minB = pointCollArrayList.get(0).y;
				double maxL = pointCollArrayList.get(0).x;
				double maxB = pointCollArrayList.get(0).y;
				for (int i = 0; i < pointCollArrayList.size(); i++) {
					MapMatchNode tNode = pointCollArrayList.get(i);
					if (minL > tNode.x) {
						minL = tNode.x;
					}
					if (minB > tNode.y) {
						minB = tNode.y;
					}
					if (maxL < tNode.x) {
						maxL = tNode.x;
					}
					if (maxB < tNode.y) {
						maxB = tNode.y;
					}
				}
				MapMatchNode minNode = new MapMatchNode();
				MapMatchNode maxNode = new MapMatchNode();
				minNode.setX(minL);
				minNode.setY(minB);
				maxNode.setX(maxL);
				maxNode.setY(maxB);
				double []minXY = new double[2];
				double []maxXY = new double[2];
				PubClass.coordinateTransToPlaneCoordinate(minNode, PubParameter.wuhanL0, minXY);
				PubClass.coordinateTransToPlaneCoordinate(maxNode, PubParameter.wuhanL0, maxXY);
				//������������
				double expandLeftDownMinXX = minXY[0] - radius;
				double expandLeftDownYY = minXY[1] - radius;
				double expandRightTopXX = maxXY[0] + radius;
				double expandRightTopYY = maxXY[1] + radius;				
				double []minLB = new double[2];
				double []maxLB = new double[2];
				PubClass.coordinateTransToLB(PubParameter.wuhanL0, expandLeftDownMinXX, expandLeftDownYY, minLB);
				PubClass.coordinateTransToLB(PubParameter.wuhanL0, expandRightTopXX, expandRightTopYY, maxLB);
				leftDownLB[0] = minLB[0];
				leftDownLB[1] = minLB[1];
				rightTopLB[0] = maxLB[0];
				rightTopLB[1] = maxLB[1];				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/*�ж�tEdge(�߶�)�Ƿ���Square�ڻ�����֮�ཻ,��������������
	 * 1.�ж�tEdge��������Ƿ��ھ��������ڣ�������Ϊ����true
	 * 2.�ж�tEdge�����������߶��Ƿ���Square�е���һ�����ཻ�����ཻ��Ϊtrue
	 *
	 * ���򣬷���false*/
	public static boolean isEdgeInOrInterSquare(Double[] leftDownNode, Double[] rightTopNode, MapMatchEdge tEdge){
		boolean isok = false;
		try {
			Double[]rightDownNode = new Double[]{leftDownNode[0],rightTopNode[1]};
			Double[]leftTopNode = new Double[]{rightTopNode[0], leftDownNode[1]};
			ArrayList<MapMatchNode> pointCollArrayList = tEdge.getPointCollArrayList();
			int pointCount = pointCollArrayList.size();
			for (int i = 0; i < pointCount - 1; i++) {
				MapMatchNode cNode = pointCollArrayList.get(i);
				MapMatchNode nextNode = pointCollArrayList.get(i + 1);
				if (isNodeInSquare(leftDownNode, rightTopNode, cNode) || isLineSegmentIntersect(cNode, nextNode, leftDownNode, rightDownNode)||isLineSegmentIntersect(cNode, nextNode, rightDownNode, rightTopNode)||
						isLineSegmentIntersect(cNode, nextNode, rightTopNode, leftTopNode) || isLineSegmentIntersect(cNode, nextNode, leftTopNode, leftDownNode)) {
					isok = true;
					break;
				}
				if (i == pointCount - 1) {
					if (isNodeInSquare(leftDownNode, rightTopNode, cNode)){
						isok = true;
					}
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isok = false;
		}
		return isok;
	}
	
	/*�ж����߶��Ƿ��ཻ
	 * ����������˻��жϣ�
	 * ������֪�߶�ab��cd�������Ƿ��ཻ����ͬʱ����
	 * 1.���ac X ad��bc X bd,�������
	 * 2.���� ca x cb��da x db���
	 * ˵�����߶��ཻ������true�����򷵻�false*/
	public static boolean isLineSegmentIntersect(MapMatchNode aNode, MapMatchNode bNode, Double[]cxy,Double []dxy){
		boolean isOk = false;
		try {
			double []axy = new double [2];
			double []bxy = new double [2];
			coordinateTransToPlaneCoordinate(aNode, PubParameter.wuhanL0, axy);
			coordinateTransToPlaneCoordinate(bNode, PubParameter.wuhanL0, bxy);
			//ac���ad
			double acXX = cxy[0] - axy[0];
			double acYY = cxy[1] - axy[1];
			double adXX = dxy[0] - axy[0];
			double adYY = dxy[1] - axy[1];
			double acXad = acXX * adYY - adXX * acYY;
			//bc���bd
			double bcXX = cxy[0] - bxy[0];
			double bcYY = cxy[1] - bxy[1];
			double bdXX = dxy[0] - bxy[0];
			double bdYY = dxy[1] - bxy[1]; 
			double bcXbd = bcXX * bdYY - bdXX * bcYY;
			//ca���cb
			double caXX = axy[0] - cxy[0];
			double caYY = axy[1] - cxy[1];
			double cbXX = bxy[0] - cxy[0];
			double cbYY = bxy[1] - cxy[1];
			double caXcb = caXX * cbYY - cbXX * caYY;
			//da���db
			double daXX = axy[0] - dxy[0];
			double daYY = axy[1] - dxy[1];
			double dbXX = bxy[0] - dxy[0];
			double dbYY = bxy[1] - dxy[1];
			double daXdb = daXX * dbYY - dbXX * daYY;
			if (acXad * bcXbd <= 0 && caXcb * daXdb <=0) {
				isOk = true;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOk = false;
		}
		return isOk;
	}	
	
	/*���node������������
	 * allGridIndexVerticesMap��������Ϣ
	 * tNode����ǰ��*/
	public static int obtainGridIndex(Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap, MapMatchNode tNode){
		//�����������
		int gridIndex = 0;
		try {
			Set keySet = allGridIndexVerticesMap.entrySet();
	        if (keySet != null) {
	        	Iterator iterator = (Iterator) keySet.iterator();
	        	while (iterator.hasNext()) {
	        		Map.Entry mapEntry = (Map.Entry) iterator.next();
	        		Object key = mapEntry.getKey();
	   	         	Object val = mapEntry.getValue();
	   	         	ArrayList<Double[]> verticesArrayList = (ArrayList<Double[]>)val;
	   	         	Double []leftDownNode = verticesArrayList.get(0);//���½ǵ�
					Double []rightTopNode = verticesArrayList.get(2);//���Ͻǵ�
	   	         	if (PubClass.isNodeInSquare(leftDownNode, rightTopNode, tNode)) {
	   	         		gridIndex = (Integer)key;
	   	         		break;
					}
				}       	
	        }
	        System.out.print("");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
        return gridIndex;
	}
	
	/******************************************************************************************
	 * �����ߡ������֮���ϵ���ж�
	 * ****************************************************************************************/
	/*��㵽ƽ���߶εľ��룺�����cNode��startNode��endNode��������߶εľ��룩
	 * ���߶ζ�����ֱ�ߣ���㵽ֱ�ߵľ��벻ͬ
	 * ��cNode��Աߵ������н�Ϊ��ǣ���ΪͶӰ����
	 * ����һ��Ϊ�۽ǣ���Ϊ�õ㵽�ö۽ǵ�ľ���
	 */
	public static double distancePointToLineSegment(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode)
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
			System.out.print(e.getMessage());
			distance = 999999;
		}
		return distance;
	}
	
	/*��㵽���ε���С���루��centerNode������edge����С���룩
	 * �ǻ��Ρ������߶Ρ�������ֱ��
	 * ������һϵ�е㹹�ɣ�һϵ���߶ι���
	 * ˼·��
	 * 1.���������еĵ㼯�ϣ������ڵĵ㹹��һ�߶�
	 * 2.��㵽�߶εľ��루���߶Σ�������ֱ�ߣ�
	 * 3.�Ƚ���õ�ÿһ�߶εľ��룬�Ӷ������С����*/
	public static double distancePointToArc(MapMatchEdge edge, MapMatchNode centerNode){
		double minDis = 999999;
		try {
			ArrayList<MapMatchNode> pointCollArrayList = new ArrayList<MapMatchNode>();
			pointCollArrayList = edge.getPointCollArrayList();
			for (int i = 0; i < pointCollArrayList.size() - 1; i++) {
				MapMatchNode curNode = pointCollArrayList.get(i);
				MapMatchNode nextNode = pointCollArrayList.get(i + 1);
				double tempDis = distancePointToLineSegment(curNode, nextNode, centerNode);
				if (minDis > tempDis) {
					minDis = tempDis;
				}				
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();
			minDis = 999999;
		}
		return minDis;
	}
	
	/*�㵽ƽ���߶ε�ͶӰ��:�����cNode��startNode��endNode��������߶ε�ͶӰ�㣩
	 * ��cNode��Աߵ������н�Ϊ��ǣ���Ϊ�������ͶӰ��.
	 *  ����ͶӰ��Ϊp��x, y��������sp = lanmda * ����se
	 * 	����se = (xe - xs, ye - ys)������sp = (x - xs, y - ys)
	 *  |sp| = lanmda * |se|,�ɴ˿���p������
	 * ����û��ͶӰ��
	 * */
	public static MapMatchNode projPointToLineSegment(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode){
		double angleStar = 0;//��㴦�н�
		double angleEnd = 0;//�յ㴦�н�
		double seNodeDis = distance(startNode, endNode);
		double scNodeDis = distance(startNode, cNode);
		MapMatchNode returNode = new MapMatchNode();
		double distance = 0;
		double cNodeX = 0;
		double cNodeY = 0;
		double starNodeX = 0;
		double starNodeY = 0;
		double endNodeX = 0;
		double endNodeY = 0;
		try {
			if (isTheSameNode(endNode, cNode)){
				angleEnd = 0;
				returNode = endNode;
			}
			else if (isTheSameNode(startNode, cNode)) {
				angleStar = 0;
				returNode = startNode;
			}
			else {				
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);	
				cNodeX = xy[0];
				cNodeY = xy[1];
				coordinateTransToPlaneCoordinate(startNode, PubParameter.wuhanL0, xy);				
				starNodeX = xy[0];
				starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				endNodeX = xy[0];
				endNodeY = xy[1];				
				//���յ����� ��ģ   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//����Լ��뵱ǰ��������ģ
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;				
				//���յ���������㵱ǰ������ �������н�
				angleStar = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//�յ��Լ��뵱ǰ��������ģ
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//������������յ㵱ǰ������ �������н�
				angleEnd = Math.acos((-seDeltX * ecDeltX + (-seDeltY) * ecDeltY )/(seNodeDis * ecNodeDis));				
			}	
			if (angleStar > Math.PI/2 || angleEnd > Math.PI/2) {
				returNode = null;
			}
			else if(angleEnd == Math.PI/2){
				returNode = endNode;
			}
			else if (angleStar == Math.PI/2) {
				returNode = startNode;
			}
			else {
				//��ͶӰ��			
				double spDis = scNodeDis * Math.cos(angleStar);
				double lamda = spDis/seNodeDis;
				double x = lamda * (endNodeX - starNodeX) + starNodeX;
				double y = lamda * (endNodeY - starNodeY) + starNodeY;
				double LB[] = new double[2];
				PubClass.coordinateTransToLB(PubParameter.wuhanL0, x, y, LB);
				returNode.setX(LB[0]);
				returNode.setY(LB[1]);
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
			returNode = null;
		}
		return returNode;		
	}
	
	/*�жϵ�cNode�Ƿ���startNode,endNode����֮��
	 * */
	public static boolean isNodeBetweenTwoNodes(MapMatchNode startNode, MapMatchNode endNode, MapMatchNode cNode){
		boolean isbetween = false;
		double angleStar = 0;//��㴦�н�
		double angleEnd = 0;//�յ㴦�н�
		double seNodeDis = distance(startNode, endNode);
		double scNodeDis = distance(startNode, cNode);
		MapMatchNode returNode = new MapMatchNode();
		double distance = 0;
		double cNodeX = 0;
		double cNodeY = 0;
		double starNodeX = 0;
		double starNodeY = 0;
		double endNodeX = 0;
		double endNodeY = 0;
		try {
			if (isTheSameNode(endNode, cNode)){
				angleEnd = 0;
				isbetween = true;
				return isbetween;
			}
			else if (isTheSameNode(startNode, cNode)) {
				angleStar = 0;
				isbetween = true;
				return isbetween;
			}
			else {
				double []xy = new double[2];
				coordinateTransToPlaneCoordinate(cNode, PubParameter.wuhanL0, xy);	
				cNodeX = xy[0];
				cNodeY = xy[1];
				coordinateTransToPlaneCoordinate(startNode, PubParameter.wuhanL0, xy);				
				starNodeX = xy[0];
				starNodeY = xy[1];
				coordinateTransToPlaneCoordinate(endNode, PubParameter.wuhanL0, xy);
				endNodeX = xy[0];
				endNodeY = xy[1];				
				//���յ����� ��ģ   
				seNodeDis = distance(startNode, endNode);
				double seDeltX = endNodeX - starNodeX;
				double seDeltY = endNodeY - starNodeY;			
				//����Լ��뵱ǰ��������ģ
				double scDeltX = cNodeX - starNodeX;
				double scDeltY = cNodeY - starNodeY;				
				//���յ���������㵱ǰ������ �������н�
				angleStar = Math.acos((seDeltX * scDeltX + seDeltY * scDeltY )/(seNodeDis * scNodeDis));
				//�յ��Լ��뵱ǰ��������ģ
				double ecDeltX = cNodeX - endNodeX;
				double ecDeltY = cNodeY - endNodeY;
				double ecNodeDis = distance(endNode, cNode);				
				//������������յ㵱ǰ������ �������н�
				angleEnd = Math.acos((-seDeltX * ecDeltX + (-seDeltY) * ecDeltY )/(seNodeDis * ecNodeDis));				
			}
			if (angleStar > Math.PI/2 || angleEnd > Math.PI/2) {
				isbetween = false;
			}
			else {
				isbetween = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			isbetween = false;
		}
		return isbetween;
	}
	
	/*�ж��Ƿ�Ϊͬһ��*/
	public static boolean isTheSameNode(MapMatchNode tNode1, MapMatchNode tNode2){
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
	
	/**
	 * �ж��Ƿ�ΪͬһGPS�㣺�����Ϸ�����ͬ����ΪGPS����ڶ�λ��ͬһ��ľ���Ҫ���
	 * @param taxiGPS1
	 * @param taxiGPS2
	 * @return
	 */
	public static boolean isTheSameTaxiGPS(TaxiGPS taxiGPS1, TaxiGPS taxiGPS2){
		boolean isTheSame = false;
		try {
			if (Math.abs(taxiGPS1.getLongitude() - taxiGPS2.getLongitude()) < PubParameter.continuousStaticLongitudeLatitudeThreshold && 
					Math.abs(taxiGPS1.getLatitude() - taxiGPS2.getLatitude()) < PubParameter.continuousStaticLongitudeLatitudeThreshold) {
				isTheSame = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.print(e.getMessage());
			e.printStackTrace();			
		}
		return isTheSame;
	}
	
	/*ƽ�����ڵ�֮��ľ���*/
	public static double distance(MapMatchNode node1, MapMatchNode node2){		
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
	
	/**
	 * ��������edge֮�����,����һ��Ϊ·�ζ˵�vertex
	 * @param startNode
	 * @param endNode
	 * @param edge
	 * @return
	 */
	public static double distanceBetweenVertexAndNodeAlongEdge(MapMatchNode vertex, MapMatchNode targetNode, MapMatchEdge targetEdge) {
		double distance = 0;
		try {
			ArrayList<MapMatchNode> pointCollArrayList = targetEdge.getPointCollArrayList();
			int count = pointCollArrayList.size();
			MapMatchNode startNode = pointCollArrayList.get(0);
			if (isTheSameNode(vertex, startNode)) {
				for (int i = 0; i < count - 1; i++) {
					MapMatchNode node1 = pointCollArrayList.get(i);
					MapMatchNode node2 = pointCollArrayList.get(i + 1);
					if (isNodeBetweenTwoNodes(node1, node2, targetNode)) {
						if (i != 0) {	
							double tempDist1 = distance(node1, targetNode);
							for (int j = 0; j <= i - 1; j++) {
								MapMatchNode tnode1 = pointCollArrayList.get(j);
								MapMatchNode tnode2 = pointCollArrayList.get(j + 1);
								double tempDist2 = distance(tnode1, tnode2);
								distance = distance + tempDist2;
							}
							distance = distance + tempDist1;
						}
						else {							
							distance = distance(vertex, targetNode);
						}
						break;
					}
				}				
			}
			else {
				for (int i = count - 1; i >= 1 ; i--) {
					MapMatchNode node1 = pointCollArrayList.get(i);
					MapMatchNode node2 = pointCollArrayList.get(i - 1);
					if (isNodeBetweenTwoNodes(node1, node2, targetNode)) {
						if (i != count - 1) {
							double tempDist1 = distance(node1, targetNode);
							for (int j = count - 1; j >= i + 1; j--) {
								MapMatchNode tnode1 = pointCollArrayList.get(j);
								MapMatchNode tnode2 = pointCollArrayList.get(j - 1);
								double tempDist2 = distance(tnode1, tnode2);
								distance = distance + tempDist2;								
							}
							distance = distance + tempDist1;
						}
						else {
							distance = distance(vertex, targetNode);
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return distance;
	}
	
	/*********************************************************************************
	 * �ַ���������
	 * *******************************************************************************/
	/*�ж�����������ַ����Ƿ�Ϊ����
	 * �磺"3.0","3"���ж�Ϊ����
	 * ��Ϊ�������򷵻�true������false��
	 * ˼·��
	 * 1.�ж��Ƿ���С����
	 * 2.����С���㣬���ж�С�������Ƿ�Ϊ�㣬��Ϊ�㣬�򷵻�true�����򷵻�false*/
	public static boolean isInteger(String str){
		try {
//			int temp = Integer.parseInt(str);
			int index = str.indexOf(".");
			if (index == -1) {
				return true;
			}
			else {
				String[]strArray = str.split("\\.");//�������"\\"
				int fractionalPart = Integer.valueOf(strArray[1]);
				if (fractionalPart == 0) {
					return true;
				}
				else {
					return false;
				}
			}			
		} catch (Exception e) {
			return false;
		}
	}
	
	/*��������ַ�������������
	 * ���룺�����ַ�����str
	 * ����������ַ�����������*/
	public static int obtainIntegerPart(String str){
		int integerPart = 0;
		try {
			int index = str.indexOf(".");
			//˵��Ϊ����
			if (index == -1) {
				integerPart = Integer.parseInt(str);				
			}
			else {
				String[]strArray = str.split("\\.");//�������"\\"
				integerPart = Integer.valueOf(strArray[0]);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return integerPart;
	}
	
	/**********************************************************************
	 * ʱ�䴦����
	 * ********************************************************************/
	/*������ֹʱ����ʱ��
	 * 1.Ҫ��֤endTimeStrʱ�����startTimeStrʱ��
	 * 2.��ֹʱ��֮��������24Сʱ��24*60�֣�
	 * ���룺
	 * startTimeStr����ʼʱ��
	 * endTimeStr����ֹʱ��
	 * �����
	 * timeInterval:ʱ��������Ϊ��λ
	 * ʱ���ʽ2013-01-01 00:00:15*/
	public static double obtainTimeInterval(String startTimeStr, String endTimeStr){
		double timeInterval = 0;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    java.util.Date endDate = df.parse(endTimeStr);
		    double tempTimeInterval = endDate.getTime() - startDate.getTime();
//		    long day = timeInterval/(24 * 60 * 60 * 1000);
//		    long hour = (timeInterval/(60 * 60 * 1000) - day * 24);
//		    long min = ((timeInterval/(60 * 1000)) - day * 24 * 60 - hour * 60);
//		    long s = (timeInterval/1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		    timeInterval = (double)(tempTimeInterval/1000);			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeInterval;
	}
	
	/*��ֹʱ��֮��������24Сʱ��24*60�֣�*/
	public double obtainTimeInterval22(String startTimeStr, String endTimeStr){
		double timeInterval = 0;
		try {
			//��ʼʱ��
			String startYearStr = startTimeStr.substring(0, 4);
			int startYearInt = Integer.valueOf(startYearStr);
			String startMonthStr = startTimeStr.substring(5, 7);
			int startMonthInt = Integer.valueOf(startMonthStr);
			String startDayStr = startTimeStr.substring(8, 10);
			int startDayInt = Integer.valueOf(startDayStr);
			String startHourStr = startTimeStr.substring(11, 13);
			int startHourInt = Integer.valueOf(startHourStr);
			String startMinStr = startTimeStr.substring(14, 16);
			int startMinInt = Integer.valueOf(startMinStr);
			String startSecStr = startTimeStr.substring(17, 19);
			int startSecInt = Integer.valueOf(startSecStr);
			//��ֹʱ��
			String endYearStr = endTimeStr.substring(0, 4);
			int endYearInt = Integer.valueOf(endYearStr);
			String endMonthStr = endTimeStr.substring(5, 7);
			int endMonthInt = Integer.valueOf(endMonthStr);
			String endDayStr = endTimeStr.substring(8, 10);
			int endDayInt = Integer.valueOf(endDayStr);
			String endHourStr = endTimeStr.substring(11, 13);
			int endHourInt = Integer.valueOf(endHourStr);
			String endMinStr = endTimeStr.substring(14, 16);
			int endMinInt = Integer.valueOf(endMinStr);
			String endSecStr = endTimeStr.substring(17, 19);
			int endSecInt = Integer.valueOf(endSecStr);		
			int secInterval = 0;//����
			int minInterval = 0;//���Ӽ��
			int hourInterval = 0;//Сʱ���
			if (endSecInt >= startSecInt) {
				secInterval = endSecInt - startSecInt;
				if (endMinInt >= startMinInt) {
					minInterval = endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
				else {
					//��λ
					minInterval = 60 + endMinInt - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
			}
			else {
				//��λ
				secInterval = 60 + endSecInt - startSecInt;
				if ((endMinInt - 1) >= startMinInt) {
					minInterval  = endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if (endHourInt >= startHourInt) {
						hourInterval = endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					
				}
				else {
					//��λ
					minInterval  = 60 + endMinInt -1 - startMinInt;
					minInterval = minInterval * 60;
					if ((endHourInt - 1) >= startHourInt) {
						hourInterval = endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
					//ʱ ��λ
					else {
						hourInterval = 24 + endHourInt - 1 - startHourInt;
						hourInterval = hourInterval * 3600;
					}
				}
			}
			timeInterval = hourInterval + minInterval + secInterval;		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return timeInterval;
	}
	
	/*�ж�time2Str�Ƿ���time1Str��
	 * ���ǣ�����true
	 * ���򣬷���false
	 * ʱ���ʽ2013-01-01 00:00:15
	 * */
	public static boolean isTime2AfterTime1(String time1Str, String time2Str){
		boolean isTrue = false;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(time1Str);
		    java.util.Date endDate = df.parse(time2Str);
		    double tempTimeInterval = endDate.getTime() - startDate.getTime();
		    if (tempTimeInterval > 0) {
		    	isTrue = true;
			}		    
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return isTrue;
	}
	
	
	/**������ʼʱ�䡢һ����ʱ����������ֹʱ��
	 * startTimeStr:��ʼʱ�䣬��ʽ2013-01-01 00:00:15
	 * endTimeArray������ʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ����������Ϊ��λ
	 * */
	public static boolean obtainEndTimeAccordStartTime(String startTimeStr, int timeInterval, String[] endTimeArray){
		boolean isOK = false;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    startDate.setSeconds(startDate.getSeconds() + timeInterval);
		    String endTimeStr = df.format(startDate);
		    endTimeArray[0] = endTimeStr;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	public static String obtainEndTimeAccordStartTime(String startTimeStr, int timeInterval){
		String endTimeStr = "";
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    startDate.setSeconds(startDate.getSeconds() + timeInterval);
		    endTimeStr = df.format(startDate);
		    
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return endTimeStr;
	}
	
	/* timeInterval��ʱ����������Ϊ��λ��<=60s*/
	public boolean obtainEndTimeAccordStartTime22(String startTimeStr, int timeInterval, String[] endTimeArray){
		boolean isOK = false;
		try {
			if (timeInterval <= 60) {
				String startYearStr = startTimeStr.substring(0, 4);
				int startYearInt = Integer.valueOf(startYearStr);
				String startMonthStr = startTimeStr.substring(5, 7);
				int startMonthInt = Integer.valueOf(startMonthStr);
				String startDayStr = startTimeStr.substring(8, 10);
				int startDayInt = Integer.valueOf(startDayStr);
				String startHourStr = startTimeStr.substring(11, 13);
				int startHourInt = Integer.valueOf(startHourStr);
				String startMinStr = startTimeStr.substring(14, 16);
				int startMinInt = Integer.valueOf(startMinStr);
				String startSecStr = startTimeStr.substring(17, 19);
				int startSecInt = Integer.valueOf(startSecStr);
				
				int endSecInt = startSecInt + timeInterval ;
				int endMinInt = startMinInt;
				int endHourInt = startHourInt;
				int endDayInt = startDayInt;
				int endMonthInt = startMonthInt;
				int endYearInt = startYearInt;
				if (endSecInt >=60) {
					endSecInt = endSecInt - 60;
					endMinInt = endMinInt + 1;
					if (endMinInt >= 60) {
						endMinInt = endMinInt - 60;
						endHourInt = endHourInt + 1;
						if (endHourInt >= 24) {
							endHourInt = endHourInt - 24;
							endDayInt = endDayInt + 1;
						}
					}				
				}			
				String endYearStr = String.valueOf(endYearInt);
				String endMonthStr = String.valueOf(endMonthInt);
				String endDayStr = String.valueOf(endDayInt);
				String endHourStr = String.valueOf(endHourInt);
				String endMinStr = String.valueOf(endMinInt);
				String endSecStr = String.valueOf(endSecInt);
				
				if (endMonthInt < 10) {
					endMonthStr = "0" + String.valueOf(endMonthInt);
				}
				if (endDayInt < 10) {
					endDayStr = "0" + String.valueOf(endDayInt);
				}
				if (endHourInt < 10) {
					endHourStr = "0" + String.valueOf(endHourInt);
				}
				if (endMinInt < 10) {
					endMinStr = "0" + String.valueOf(endMinInt);
				}
				if (endSecInt < 10) {
					endSecStr = "0" + String.valueOf(endSecInt);
				}		
				String endTimeStr = endYearStr + "-" + endMonthStr + "-" + endDayStr + " " + endHourStr  + ":" + endMinStr + ":" + endSecStr;
				endTimeArray[0] = endTimeStr;	
				isOK = true;
			}
			else {
				System.out.print("ʱ����������ΪС��60s");
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/*������ֹʱ�䡢һ����ʱ����������ʼʱ��
	 * endTimeStr:����ʱ�䣬��ʽ2013-01-01 00:00:15
	 * startTimeArray����ʼʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ����������Ϊ��λ,С�ڵ���60s*/
	public static boolean obtainStartTimeAccordEndTime(String endTimeStr, int timeInterval, String[] startTimeArray){
		boolean isOK = false;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date endDate = df.parse(endTimeStr);
		    endDate.setSeconds(endDate.getSeconds() - timeInterval);
		    String starTimeStr = df.format(endDate);
		    startTimeArray[0] = starTimeStr;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	public static String obtainStartTimeAccordEndTime(String endTimeStr, int timeInterval){
		String starTimeStr = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date endDate = df.parse(endTimeStr);
		    endDate.setSeconds(endDate.getSeconds() - timeInterval);
		    starTimeStr = df.format(endDate);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		return starTimeStr;
	}
	
	/*������ֹʱ�䡢һ����ʱ����������ʼʱ��
	 * endTimeStr:����ʱ�䣬��ʽ2013-01-01 00:00:15
	 * startTimeArray����ʼʱ��,��ʽ2013-01-01 00:00:15
	 * timeInterval��ʱ�������Ժ���Ϊ��λ,С�ڵ���60s*/
	public static boolean obtainStartTimeAccordEndTime(String endTimeStr, long millSecond, String[] startTimeArray){
		boolean isOK = false;
//		try {
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		    java.util.Date endDate = df.parse(endTimeStr);
//		    endDate.setSeconds(endDate.getSeconds() - millSecond);
//		    String starTimeStr = df.format(endDate);
//		    startTimeArray[0] = starTimeStr;
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//			isOK = false;
//		}		
		return isOK;
	}
	
	public boolean obtainStartTimeAccordEndTime22(String endTimeStr, int timeInterval, String[] startTimeArray){
		boolean isOK = false;
		try {
			String endYearStr = endTimeStr.substring(0, 4);
			int endYearInt = Integer.valueOf(endYearStr);
			String endMonthStr = endTimeStr.substring(5, 7);
			int endMonthInt = Integer.valueOf(endMonthStr);
			String endDayStr = endTimeStr.substring(8, 10);
			int endDayInt = Integer.valueOf(endDayStr);
			String endHourStr = endTimeStr.substring(11, 13);
			int endHourInt = Integer.valueOf(endHourStr);
			String endMinStr = endTimeStr.substring(14, 16);
			int endMinInt = Integer.valueOf(endMinStr);
			String endSecStr = endTimeStr.substring(17, 19);
			int endSecInt = Integer.valueOf(endSecStr);		
			int startSecInt = 0;//��
			int startMinInt = 0;//��
			int startHourInt = 0;//ʱ
			int startDayInt = endDayInt;//��
			int startMonthInt = endMonthInt;
			int startYearInt = endYearInt;
			if (endSecInt >= timeInterval) {
				startSecInt = endSecInt - timeInterval;
				startMinInt = endMinInt;
				startHourInt = endHourInt;
				startDayInt = endDayInt;
				startMonthInt = endMonthInt;
				startYearInt = endYearInt;
			}
			else {
				 //��λ
				startSecInt = 60 + endSecInt - timeInterval;
				//�����λ ֵΪ0
				if (endMinInt == 0) {
					startMinInt = 59;
					if (endHourInt == 0) {
						startHourInt = 23;
						startDayInt = endDayInt - 1;
						startMonthInt = endMonthInt;
						startYearInt = endYearInt;
					}
					else {
						startHourInt = endHourInt - 1;
						startDayInt = endDayInt;
						startMonthInt = endMonthInt;
						startYearInt = endYearInt;
					}
				}
				else {
					startMinInt = endMinInt - 1;
					startHourInt = endHourInt;
					startDayInt = endDayInt;
					startMonthInt = endMonthInt;
					startYearInt = endYearInt;
				}
			}		
			String startYearStr = String.valueOf(startYearInt);
			String startMonthStr = String.valueOf(startMonthInt);
			String startDayStr = String.valueOf(startDayInt);
			String startHourStr = String.valueOf(startHourInt);
			String startMinStr = String.valueOf(startMinInt);
			String startSecStr = String.valueOf(startSecInt);
			
			if (startMonthInt < 10) {
				startMonthStr = "0" + String.valueOf(startMonthInt);
			}
			if (startDayInt < 10) {
				startDayStr = "0" + String.valueOf(startDayInt);
			}
			if (startHourInt < 10) {
				startHourStr = "0" + String.valueOf(startHourInt);
			}
			if (startMinInt < 10) {
				startMinStr = "0" + String.valueOf(startMinInt);
			}
			if (startSecInt < 10) {
				startSecStr = "0" + String.valueOf(startSecInt);
			}		
			String startTimeStr = startYearStr + "-" + startMonthStr + "-" + startDayStr + " " + startHourStr  
			+ ":" + startMinStr + ":" + startSecStr;
			startTimeArray[0] = startTimeStr;	
			isOK = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
		}		
		return isOK;
	}
	
	/**�ж�ʱ���Ƿ�����ֹʱ��֮��
	 * startTimeStr:��ʼʱ�䣬��ʽ2013-01-01 00:00:15
	 * endTimeArray������ʱ��,��ʽ2013-01-01 00:00:15
	 * middleTimeStr�����ж�ʱ��,��ʽ2013-01-01 00:00:15
	 * */
	public static boolean isTimeBetweenStartEndTime(String startTimeStr, String endTimeStr, String middleTimeStr){
		boolean isOK = false;
		try {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    java.util.Date startDate = df.parse(startTimeStr);
		    java.util.Date middleDate = df.parse(middleTimeStr);
		    java.util.Date endDate = df.parse(endTimeStr);
		    double timeInterval1 = middleDate.getTime() - startDate.getTime();
		    double timeInterval2 = endDate.getTime() - middleDate.getTime();
		    //����Ϊ����ҿ���
		    if (timeInterval1 >= 0 && timeInterval2 > 0) {
				isOK = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			isOK = false;
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
	
	/*************************************************************
	 * ���ϣ�arraylist������������
	 *************************************************************/
	
	/**
	 * �ж�IDArraylist���Ƿ����ID
	 * ��������򷵻�true���������򷵻�false
	 * @param IDArrayList
	 * @param IDStr
	 * @return
	 */
	public static boolean isArraylistContainsID(ArrayList<String> IDArrayList, String IDStr){
		boolean isOK = false;
		try {
			for (int i = 0; i < IDArrayList.size(); i++) {
				String tempID = IDArrayList.get(i);
				if (tempID.equals(IDStr)) {
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
	
	/**
	 * ��arraylist�е�ID������������
	 * @param IDArrayList
	 * 
	 */
	public static void sortIDArraylist(ArrayList<String> IDArrayList){
		try {
			if(IDArrayList != null ) {
				for(int i = 0;i < IDArrayList.size();i++){
	                for(int j = i;j < IDArrayList.size();j++){
	                    if(Integer.parseInt(IDArrayList.get(i))> Integer.parseInt(IDArrayList.get(j))){   
	                        String temp = "";
	                    	temp = IDArrayList.get(i);
	                    	IDArrayList.set(i, IDArrayList.get(j));
	                        IDArrayList.set(j, temp);
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
	
	/*******************************************************************
	 * �ڵ�ת������
	 * *****************************************************************/
	
	/**
	 * taxiGPSת��ΪmapMatchNode
	 */
	public static MapMatchNode ConvertTaxiGPSToMapMatchNode(TaxiGPS taxiGPS){
		MapMatchNode mapMatchNode = new MapMatchNode();
		try {			
			mapMatchNode.setX(taxiGPS.getLongitude());
			mapMatchNode.setY(taxiGPS.getLatitude());
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
		return mapMatchNode;
	}
	
	/**
	 * ��������GPS��ת��ΪMapMatchNode
	 * @param taxiGPS
	 * @return
	 */
	public static MapMatchNode ConvertTaxiGPSToCorrectNode(TaxiGPS taxiGPS){
		MapMatchNode mapMatchNode = new MapMatchNode();
		try {	
			double correctLongitude = taxiGPS.getCorrectLongitude();
			double correctLatitued = taxiGPS.getCorrectLatitude();
			if (correctLongitude != -1 && correctLatitued != -1) {
				mapMatchNode.setX(correctLongitude);
				mapMatchNode.setY(correctLatitued);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}	
		return mapMatchNode;
	}
	
	/*����node1node2���������ķ�λ��lineAngle
	 *�����������ߵ�ͶӰΪ��������x���涨x����Ϊ�����Գ����ͶӰΪ��������y���涨y����Ϊ��
	 *a:����������deltX1,deltY1������������ķ���н�
	 *��aΪ���ʱ����deltX1*deltY1 > 0��lineAngle = a��
	 *             ��deltX1*deltY1 < 0, lineAngle = 2*pi - a��
	 *��aΪֱ��ʱ����deltX1 > 0, lineAngle = pi/2;
	 *			   ��deltX1 < 0��lineAngle = 3*pi/2;
	 *��aΪ�۽�ʱ����deltX1*deltY1 > 0, lineAnge = 2*pi - a��
	 *             ��deltX1*deltY1 < 0, lineAngle = a��
	 *node2:��ʾ���
	 *node2:��ʾ�յ�*/
	public static double obtainAzimuth(MapMatchNode node1, MapMatchNode node2) {
		double lineAngle = 0;//����������н�(0 - 2 * pi)
		try {			
			double []xy = new double[2];
			coordinateTransToPlaneCoordinate(node1,PubParameter.wuhanL0, xy);
			double x1 = xy[0];
			double y1 = xy[1];
			coordinateTransToPlaneCoordinate(node2,PubParameter.wuhanL0, xy);
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
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return lineAngle;
	}
	
	
}
