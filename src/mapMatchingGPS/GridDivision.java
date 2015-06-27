package mapMatchingGPS;
import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GridDivision {

	/*����������ԪcellLength * cellLength��
	 * 1.��ô����־�������ı�Ե��������½Ǻ����Ͻǵĵ�,��С���ȡ�γ�ȣ���󾭶ȡ�γ��
	 * 2.���񻮷֣�
	 *     Ҫ��һ���ص����ص�����bufferRadius���㣬����������1��ʼ
	 *     �ֱ�洢��������������Χ�����������������ڵ�polyline;������Χ�洢һϵ�����꣬���½����꿪ʼ��ʱ��洢��
	 * cellLength��������Ԫ����
	 * bufferRadius��������Ԫ��Ե��������С�����С��Ϊ������ѡ·���ľ������
	 * juncCollArrayList:�ڵ�
	 * polylineCollArrayList����
	 * allGridIndexVerticesMap:���������Լ����񶥵����飬���½ǵ���Ϊ��һ������ʱ��洢
	 * allGridJunctionMap:�洢��������ID�Լ���Χ�����нڵ�
	 * allGridPolylineMap:����ID�Լ������ڵ�����·��*/
	
	public void buildGridCell(ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, 
			double cellLength, double bufferRadius, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap, Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap){
		//��ô����־��������Ե
		System.out.print("���񻮷ֿ�ʼ:" + '\n');
		double minL = juncCollArrayList.get(0).x;
		double minB = juncCollArrayList.get(0).y;
		double maxL = juncCollArrayList.get(0).x;
		double maxB = juncCollArrayList.get(0).y;
		for (int i = 0; i < juncCollArrayList.size(); i++) {
			MapMatchNode tNode = juncCollArrayList.get(i);
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
		//���񻮷�,����������1��ʼ
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
		double minX = minXY[0];
		double minY = minXY[1];
		double maxX = maxXY[0];
		double maxY = maxXY[1];
		//������Ŀ
		double deltXNum = Math.ceil((maxX - minX)/cellLength);//��λȡ����
//		int deltXNum = (Integer.parseInt(new java.text.DecimalFormat("0").format(tempdeltXNum)));
		double deltYNum = Math.ceil((maxY - minY)/cellLength);
		int gridIndex = 0;//��������
		for (int i = 0; i < deltXNum; i++) {
			int xIndex = i + 1;			
			for (int j = 0; j < deltYNum; j++) {
				int yIndex = j + 1;
				gridIndex++;
				ArrayList<MapMatchNode> junctionArrayList = new ArrayList<MapMatchNode>();
				ArrayList<MapMatchEdge> polylineArrayList = new ArrayList<MapMatchEdge>();				
				ArrayList<Double[]> verticesArrayList = new ArrayList<Double[]>();
				//���������ĸ��������꣬���½�Ϊ��һ���㣬��ʱ����ת
				Double []xy1 = new Double[2];//��һ����
				Double []xy2 = new Double[2];//�ڶ�����
				Double []xy3 = new Double[2];//��������
				Double []xy4 = new Double[2];//���ĸ���
				xy1[0] = minX + (xIndex - 1) * cellLength - bufferRadius;
				xy1[1] = minY + (yIndex - 1) * cellLength - bufferRadius;
				xy2[0] = minX + (xIndex - 1) * cellLength - bufferRadius;
				xy2[1] = minY + yIndex * cellLength + bufferRadius;
				xy3[0] = minX + xIndex * cellLength + bufferRadius;
				xy3[1] = minY + yIndex * cellLength + bufferRadius;
				xy4[0] = minX + xIndex * cellLength + bufferRadius;
				xy4[1] = minY + (yIndex - 1) * cellLength - bufferRadius;
				verticesArrayList.add(xy1);
				verticesArrayList.add(xy2);
				verticesArrayList.add(xy3);
				verticesArrayList.add(xy4);
				allGridIndexVerticesMap.put(gridIndex, verticesArrayList);
				allGridJunctionMap.put(gridIndex, junctionArrayList);
				allGridPolylineMap.put(gridIndex, polylineArrayList);
			}
		}//���񻮷ֽ���
		//�Խڵ���и�������
		int juncCount = juncCollArrayList.size();
		for (int i = 0; i < juncCount; i++) {
			System.out.print("�Խڵ�" + i + ":" + juncCount + "�������񻮷֣�" + '\n');
			MapMatchNode tNode = juncCollArrayList.get(i);			
			Set keySet = allGridIndexVerticesMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int tempGridIndex = (Integer)mapEntry.getKey();
        		ArrayList<Double[]> verticesArrayList = (ArrayList<Double[]>)mapEntry.getValue();
        		Double []leftDownNode = verticesArrayList.get(0);//���½ǵ�
				Double []rightTopNode = verticesArrayList.get(2);//���Ͻǵ�
				if (PubClass.isNodeInSquare(leftDownNode, rightTopNode, tNode)) {
					ArrayList<MapMatchNode> junctionArrayList = allGridJunctionMap.get(tempGridIndex);
					junctionArrayList.add(tNode);//��������һ���ص��ȣ����ص�����ͬһ�������������������������ڴ˴���������ѭ��
				}	
        	}			
		}//�ڵ����񻮷ֽ���
		//���߶ν��и�������
		
		int gridCount = allGridIndexVerticesMap.size();
		int gridNum = 0; 
		Set keySet = allGridIndexVerticesMap.entrySet();
		Iterator iterator = (Iterator) keySet.iterator();
    	while (iterator.hasNext()) {
    		gridNum++;
    		Map.Entry mapEntry = (Map.Entry) iterator.next();
    		int tempGridIndex = (Integer)mapEntry.getKey();
    		ArrayList<MapMatchEdge> polylineArrayList = allGridPolylineMap.get(tempGridIndex);
    		System.out.print("������" + gridNum + ":" + gridCount + "�����߶����񻮷֣�" + '\n');
    		ArrayList<Double[]> verticesArrayList = (ArrayList<Double[]>)mapEntry.getValue();
    		Double []leftDownNode = verticesArrayList.get(0);//���½ǵ�
			Double []rightTopNode = verticesArrayList.get(2);//���Ͻǵ�
			ArrayList<MapMatchEdge> filtPolyCollArrayList = new ArrayList<MapMatchEdge>();
			rectangleCoarseFiltration(leftDownNode,rightTopNode,polylineCollArrayList, filtPolyCollArrayList);//�ֹ��ˣ�������񻮷��ٶ�
			for (int i = 0; i < filtPolyCollArrayList.size(); i++) {
				MapMatchEdge tEdge = filtPolyCollArrayList.get(i);
				if (PubClass.isEdgeInOrInterSquare(leftDownNode, rightTopNode, tEdge)) {					
					polylineArrayList.add(tEdge);
				}
			}				
    	}//�߶����񻮷ֽ���
		System.out.print("���񻮷ֽ���:" + '\n');
	}
	
	/*�ֹ��ˣ�������񻮷��ٶ�
	 * ��25�������С�ľ��ζ�polyline���дֹ���
	 * �ֹ��˾��εĳ���ֱ��������5������С
	 * ���дֹ���ʱֻ��polyline��beginpoint��endPoint�����ж�
	 * leftDownNode:�������½ǵ�
	 * rightTopNode:�������Ͻǵ�
	 * polylineCollArrayList:�����˼���
	 * filtPolyCollArrayList�����˺�ļ���*/
	public void rectangleCoarseFiltration(Double[] leftDownNode, Double[] rightTopNode, 
			ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<MapMatchEdge> filtPolyCollArrayList){
		try {
			double cellLeng = PubParameter.cellLength;
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				MapMatchEdge tEdge = polylineCollArrayList.get(i);	
				MapMatchNode beginNode = tEdge.getBeginPoint();
				MapMatchNode endNode = tEdge.getEndPoint();
				Double []filtLeftDown = new Double[]{leftDownNode[0] - 2 * cellLeng,leftDownNode[1] - 2 * cellLeng};//���½ǵ�
				Double []filtRightTop = new Double[]{rightTopNode[0] + 2 * cellLeng,rightTopNode[1] + 2 * cellLeng};//����
				if (PubClass.isNodeInSquare(filtLeftDown, filtRightTop, beginNode) ||
						PubClass.isNodeInSquare(filtLeftDown, filtRightTop, endNode)) {
					filtPolyCollArrayList.add(tEdge);
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
}
