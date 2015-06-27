package mapMatchingGPS;
import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GridDivision {

	/*构建格网单元cellLength * cellLength：
	 * 1.获得待划分矩形区域的边缘：获得左下角和右上角的点,最小经度、纬度，最大经度、纬度
	 * 2.网格划分：
	 *     要有一定重叠，重叠区域按bufferRadius计算，网格索引从1开始
	 *     分别存储网格索引与网格范围，网格索引与网格内的polyline;索引范围存储一系列坐标，左下角坐标开始逆时针存储；
	 * cellLength：格网单元长度
	 * bufferRadius：格网单元边缘缓冲区大小，其大小设为构建候选路径的距离参数
	 * juncCollArrayList:节点
	 * polylineCollArrayList：边
	 * allGridIndexVerticesMap:网格索引以及网格顶点数组，左下角点作为第一个点逆时针存储
	 * allGridJunctionMap:存储网格网格ID以及范围内所有节点
	 * allGridPolylineMap:网格ID以及网格内的所有路段*/
	
	public void buildGridCell(ArrayList<MapMatchNode> juncCollArrayList, ArrayList<MapMatchEdge> polylineCollArrayList, 
			double cellLength, double bufferRadius, Map<Integer, ArrayList<Double[]>> allGridIndexVerticesMap,
			Map<Integer, ArrayList<MapMatchNode>> allGridJunctionMap, Map<Integer, ArrayList<MapMatchEdge>> allGridPolylineMap){
		//获得待划分矩形区域边缘
		System.out.print("网格划分开始:" + '\n');
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
		//网格划分,网格索引从1开始
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
		//网格数目
		double deltXNum = Math.ceil((maxX - minX)/cellLength);//进位取整数
//		int deltXNum = (Integer.parseInt(new java.text.DecimalFormat("0").format(tempdeltXNum)));
		double deltYNum = Math.ceil((maxY - minY)/cellLength);
		int gridIndex = 0;//网格索引
		for (int i = 0; i < deltXNum; i++) {
			int xIndex = i + 1;			
			for (int j = 0; j < deltYNum; j++) {
				int yIndex = j + 1;
				gridIndex++;
				ArrayList<MapMatchNode> junctionArrayList = new ArrayList<MapMatchNode>();
				ArrayList<MapMatchEdge> polylineArrayList = new ArrayList<MapMatchEdge>();				
				ArrayList<Double[]> verticesArrayList = new ArrayList<Double[]>();
				//矩形区域四个顶点坐标，左下角为第一个点，逆时针旋转
				Double []xy1 = new Double[2];//第一个点
				Double []xy2 = new Double[2];//第二个点
				Double []xy3 = new Double[2];//第三个点
				Double []xy4 = new Double[2];//第四个点
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
		}//网格划分结束
		//对节点进行格网划分
		int juncCount = juncCollArrayList.size();
		for (int i = 0; i < juncCount; i++) {
			System.out.print("对节点" + i + ":" + juncCount + "进行网格划分：" + '\n');
			MapMatchNode tNode = juncCollArrayList.get(i);			
			Set keySet = allGridIndexVerticesMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		int tempGridIndex = (Integer)mapEntry.getKey();
        		ArrayList<Double[]> verticesArrayList = (ArrayList<Double[]>)mapEntry.getValue();
        		Double []leftDownNode = verticesArrayList.get(0);//左下角点
				Double []rightTopNode = verticesArrayList.get(2);//右上角点
				if (PubClass.isNodeInSquare(leftDownNode, rightTopNode, tNode)) {
					ArrayList<MapMatchNode> junctionArrayList = allGridJunctionMap.get(tempGridIndex);
					junctionArrayList.add(tNode);//格网间有一定重叠度，在重叠区域，同一个点可能属于两个格网，因此在此处不能跳出循环
				}	
        	}			
		}//节点网格划分结束
		//对线段进行格网划分
		
		int gridCount = allGridIndexVerticesMap.size();
		int gridNum = 0; 
		Set keySet = allGridIndexVerticesMap.entrySet();
		Iterator iterator = (Iterator) keySet.iterator();
    	while (iterator.hasNext()) {
    		gridNum++;
    		Map.Entry mapEntry = (Map.Entry) iterator.next();
    		int tempGridIndex = (Integer)mapEntry.getKey();
    		ArrayList<MapMatchEdge> polylineArrayList = allGridPolylineMap.get(tempGridIndex);
    		System.out.print("对网格" + gridNum + ":" + gridCount + "进行线段网格划分：" + '\n');
    		ArrayList<Double[]> verticesArrayList = (ArrayList<Double[]>)mapEntry.getValue();
    		Double []leftDownNode = verticesArrayList.get(0);//左下角点
			Double []rightTopNode = verticesArrayList.get(2);//右上角点
			ArrayList<MapMatchEdge> filtPolyCollArrayList = new ArrayList<MapMatchEdge>();
			rectangleCoarseFiltration(leftDownNode,rightTopNode,polylineCollArrayList, filtPolyCollArrayList);//粗过滤，提高网格划分速度
			for (int i = 0; i < filtPolyCollArrayList.size(); i++) {
				MapMatchEdge tEdge = filtPolyCollArrayList.get(i);
				if (PubClass.isEdgeInOrInterSquare(leftDownNode, rightTopNode, tEdge)) {					
					polylineArrayList.add(tEdge);
				}
			}				
    	}//线段网格划分结束
		System.out.print("网格划分结束:" + '\n');
	}
	
	/*粗过滤：提高网格划分速度
	 * 用25倍网格大小的矩形对polyline进行粗过滤
	 * 粗过滤矩形的长宽分别是网格的5倍数大小
	 * 进行粗过滤时只用polyline的beginpoint和endPoint进行判断
	 * leftDownNode:网格左下角点
	 * rightTopNode:网格右上角点
	 * polylineCollArrayList:待过滤集合
	 * filtPolyCollArrayList：过滤后的集合*/
	public void rectangleCoarseFiltration(Double[] leftDownNode, Double[] rightTopNode, 
			ArrayList<MapMatchEdge> polylineCollArrayList, ArrayList<MapMatchEdge> filtPolyCollArrayList){
		try {
			double cellLeng = PubParameter.cellLength;
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				MapMatchEdge tEdge = polylineCollArrayList.get(i);	
				MapMatchNode beginNode = tEdge.getBeginPoint();
				MapMatchNode endNode = tEdge.getEndPoint();
				Double []filtLeftDown = new Double[]{leftDownNode[0] - 2 * cellLeng,leftDownNode[1] - 2 * cellLeng};//左下角点
				Double []filtRightTop = new Double[]{rightTopNode[0] + 2 * cellLeng,rightTopNode[1] + 2 * cellLeng};//右上
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
