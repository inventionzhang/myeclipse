package org.lmars.network.mapMatchingGPS;

import java.util.ArrayList;

import org.lmars.network.entity.Node;
import org.lmars.network.util.PubClass;



public class CreateTopolygy {
	
	public static void createTopolygonRelation(ArrayList<MapMatchNode> juncCollArraylist, ArrayList<MapMatchEdge> polylineCollArrayList){
		try {
			//计算mapMatchEdge的长度
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				MapMatchEdge tempEdge = polylineCollArrayList.get(i);
				ArrayList<MapMatchNode> pointCollArrayLis = tempEdge.getPointCollArrayList();
				double edgeLength = 0;
				for (int j = 0; j < pointCollArrayLis.size() - 1; j++) {
					MapMatchNode tNode1 = new MapMatchNode();
					MapMatchNode tNode2 = new MapMatchNode();
					tNode1 = pointCollArrayLis.get(j);
					tNode2 = pointCollArrayLis.get(j + 1);
					edgeLength = edgeLength + PubClass.distance(tNode1,tNode2);
				}				
				tempEdge.setEdgeLength(edgeLength);
			}
			
			/*连通集合的建立较慢：需要改进，可以以线段为中心圈定一定范围内的路段进行连通集合构建
			 * 建立线的一级、二级、三级连通集合,不能包括自身
			 *线本身匹配一级一级连通集合的线，以及连通的起点与终点
			 *根据一级连通集合匹配二级连通集合以及连通的起点与终点
			 *根据二级连通集合匹配三级连通集合以及连通的起点与终点*/
			long startCreateConnLevelTime = System.currentTimeMillis();
			System.out.print("构建线的连通集合:" + '\n');
			int polylineCount = polylineCollArrayList.size();
			for (int i = 0; i < polylineCount; i++)
			{
				System.out.print("构建连通集合:" + i + ":" + polylineCount + '\n');
				MapMatchEdge ZerotempEdge = polylineCollArrayList.get(i);
				MapMatchNode tempBeginPoint = ZerotempEdge.getBeginPoint();
				MapMatchNode tempEndPoint = ZerotempEdge.getEndPoint();
				ArrayList<MapMatchEdge> tempFirstLevelConnEdgeArray = new ArrayList<MapMatchEdge>() ;//一级连通集合
				ArrayList<MapMatchEdge> tempSecoLevelConnEdgeArray = new ArrayList<MapMatchEdge>();//二级连通集合
				ArrayList<MapMatchEdge> tempThirdLevelConnEdgeArray = new ArrayList<MapMatchEdge>();//三级级连通集合
				for (int j = 0; j < polylineCount; j++)
				{
					MapMatchEdge firstTempEdge = polylineCollArrayList.get(j);
					MapMatchNode firstTempBeginPoint = firstTempEdge.getBeginPoint();
					MapMatchNode firstTempEndPoint = firstTempEdge.getEndPoint();
					//一级连通集合
					if (isTheSamePoint(tempBeginPoint,firstTempBeginPoint) && ZerotempEdge.getEdgeID() != firstTempEdge.getEdgeID())
					{
						firstTempEdge.setFirstLevelConnBeginPoint(firstTempBeginPoint);
						firstTempEdge.setFirstLevelConnEndPoint(firstTempEndPoint);
						tempFirstLevelConnEdgeArray.add(firstTempEdge);
					}
					else if (isTheSamePoint(tempBeginPoint,firstTempEndPoint) && ZerotempEdge.getEdgeID() != firstTempEdge.getEdgeID())
					{
						firstTempEdge.setFirstLevelConnBeginPoint(firstTempEndPoint);
						firstTempEdge.setFirstLevelConnEndPoint(firstTempBeginPoint);
						tempFirstLevelConnEdgeArray.add(firstTempEdge);
					}
					else if (isTheSamePoint(tempEndPoint,firstTempBeginPoint) && ZerotempEdge.getEdgeID() != firstTempEdge.getEdgeID())
					{
						firstTempEdge.setFirstLevelConnBeginPoint(firstTempBeginPoint);
						firstTempEdge.setFirstLevelConnEndPoint(firstTempEndPoint);
						tempFirstLevelConnEdgeArray.add(firstTempEdge);
					}
					else if (isTheSamePoint(tempEndPoint,firstTempEndPoint) && ZerotempEdge.getEdgeID() != firstTempEdge.getEdgeID())
					{
						firstTempEdge.setFirstLevelConnBeginPoint(firstTempEndPoint);
						firstTempEdge.setFirstLevelConnEndPoint(firstTempBeginPoint);
						tempFirstLevelConnEdgeArray.add(firstTempEdge);
					}
					else {
						continue;
					}
				}
				//二级连通集合
				//根据一级连通集合中路段一级连通线的连通终点搜寻二级连通集合,
				//二级连通集合中不能包含零级、一级连通集合中线段
				if (tempFirstLevelConnEdgeArray.size() > 0)
				{
					for (int k2 = 0; k2 < tempFirstLevelConnEdgeArray.size(); k2++)
					{
						MapMatchEdge tempFirstLevelEdge = tempFirstLevelConnEdgeArray.get(k2);
						MapMatchNode tempFirstLevelConnEndPoint = tempFirstLevelEdge.getFirstLevelConnEndPoint();
						for (int j2 = 0; j2 < polylineCount; j2++ )
						{
							MapMatchEdge secoTempEdge = polylineCollArrayList.get(j2);
							MapMatchNode secoTempBeginPoint = secoTempEdge.getBeginPoint();
							MapMatchNode secoTempEndPoint = secoTempEdge.getEndPoint();
							if (isTheSamePoint(tempFirstLevelConnEndPoint,secoTempBeginPoint) && tempFirstLevelEdge.getEdgeID() != secoTempEdge.getEdgeID() &&
									ZerotempEdge.getEdgeID() != secoTempEdge.getEdgeID())
							{
								secoTempEdge.setSecoLevelConnBeginPoint(secoTempBeginPoint);
								secoTempEdge.setSecoLevelConnEndPoint(secoTempEndPoint);
								tempSecoLevelConnEdgeArray.add(secoTempEdge);
							}
							else if (isTheSamePoint(tempFirstLevelConnEndPoint,secoTempEndPoint) && tempFirstLevelEdge.getEdgeID() != secoTempEdge.getEdgeID() &&
									ZerotempEdge.getEdgeID() != secoTempEdge.getEdgeID())
							{
								secoTempEdge.setSecoLevelConnBeginPoint(secoTempEndPoint);
								secoTempEdge.setSecoLevelConnEndPoint(secoTempBeginPoint);
								tempSecoLevelConnEdgeArray.add(secoTempEdge);
							}						
						}
					}
				}		
				//三级连通集合
				//根据二级连通集合中路段二级连通线的连通终点搜寻三级连通集合
				//三级连通集合中不能包含零级、一级、二级连通集合中线段
				if (tempSecoLevelConnEdgeArray.size() > 0)
				{
					for (int k3 = 0; k3 < tempSecoLevelConnEdgeArray.size(); k3++)
					{
						MapMatchEdge tempSeconLevelEdge = tempSecoLevelConnEdgeArray.get(k3);
						MapMatchNode tempSeconLevelConnEndPoint = tempSeconLevelEdge.getSecoLevelConnEndPoint();
						for (int j3 = 0; j3 < polylineCount; j3++)
						{
							MapMatchEdge thirdTempEdge = polylineCollArrayList.get(j3);
							MapMatchNode thirdTempBeginPoint = thirdTempEdge.getBeginPoint();
							MapMatchNode thirdTempEndPoint = thirdTempEdge.getEndPoint();
							if (isTheSamePoint(tempSeconLevelConnEndPoint,thirdTempBeginPoint) && thirdTempEdge.getEdgeID() != tempSeconLevelEdge.getEdgeID() &&
									thirdTempEdge.getEdgeID() != ZerotempEdge.getEdgeID())
							{
								boolean isOk = true;
								for (int t = 0; t < tempFirstLevelConnEdgeArray.size(); t++) {
									MapMatchEdge ttEdge = tempFirstLevelConnEdgeArray.get(t);
									if (thirdTempEdge.getEdgeID() == ttEdge.getEdgeID()) {
										isOk = false;										
									}								
								}
								if (isOk) {
									thirdTempEdge.setThirdLevelConnBeginPoint(thirdTempBeginPoint);
									thirdTempEdge.setThirdLevelConnEndPoint(thirdTempEndPoint);
									tempThirdLevelConnEdgeArray.add(thirdTempEdge);
								}
							}
							else if (isTheSamePoint(tempSeconLevelConnEndPoint,thirdTempEndPoint) && thirdTempEdge.getEdgeID() != tempSeconLevelEdge.getEdgeID() &&
									thirdTempEdge.getEdgeID() != ZerotempEdge.getEdgeID())
							{
								boolean isOk = true;
								for (int t = 0; t < tempFirstLevelConnEdgeArray.size(); t++) {
									MapMatchEdge ttEdge = tempFirstLevelConnEdgeArray.get(t);									
									if (thirdTempEdge.getEdgeID() == ttEdge.getEdgeID()) {										
										isOk = false;
									}								
								}
								if (isOk) {
									thirdTempEdge.setThirdLevelConnBeginPoint(thirdTempEndPoint);
									thirdTempEdge.setThirdLevelConnEndPoint(thirdTempBeginPoint);
									tempThirdLevelConnEdgeArray.add(thirdTempEdge);
								}
							}
							else {
								continue;
							}
						}
					}
				}
				//设置一二三级连通集合
				ZerotempEdge.setFirstLevelConnEdgeArray(tempFirstLevelConnEdgeArray);
				ZerotempEdge.setSecoLevelConnEdgeArray(tempSecoLevelConnEdgeArray);
				ZerotempEdge.setThirdLevelConnEdgeArray(tempThirdLevelConnEdgeArray);				
			}//一级、二级、三级连通集合构建结束
			System.out.print("线的连通集合构建结束:" + '\n');
			long endCreateConnLevelTime = System.currentTimeMillis();
			System.out.print("道路连通集合构建时间:" + (endCreateConnLevelTime - startCreateConnLevelTime)/1000 + "s" + '\n');
			//构建点线拓扑：与点直接相连的线
			long startCreatePointLineTopoTime = System.currentTimeMillis();
			System.out.print("开始构建点线拓扑:");
			int juncCount = juncCollArraylist.size();
			for (int i = 0; i < juncCount; i++)
			{
				System.out.print("构建点线拓扑:" + i + ":" + juncCount + '\n');
				MapMatchNode tempTopoPoint = juncCollArraylist.get(i);
				ArrayList<MapMatchEdge> tempAdjacentEdgeArrayList = new ArrayList<MapMatchEdge>();//存储节点相邻Edge
				ArrayList<MapMatchNode> tempAdjacentJuncArrayList=new ArrayList<MapMatchNode>();//存储邻接点				
				for (int j = 0; j < polylineCount; j++)
				{
					MapMatchEdge tempEdge = polylineCollArrayList.get(j);
					MapMatchNode tempBeginPoint = tempEdge.getBeginPoint();
					MapMatchNode tempEndPoint = tempEdge.getEndPoint();
					if (isTheSamePoint(tempTopoPoint,tempBeginPoint))
					{
						tempBeginPoint.setNodeID(tempTopoPoint.getNodeID());//设置ID
						tempAdjacentEdgeArrayList.add(tempEdge);
						for (int k1 = 0; k1 < juncCount; k1++) {
     	    				if (isTheSamePoint(juncCollArraylist.get(k1), tempEndPoint)) {			    	    								    	    					
     	    					tempAdjacentJuncArrayList.add(juncCollArraylist.get(k1));//相邻点
     	    					break;//找到相邻点，跳出循环
 							}		    	    				
     	    			}
					}
					else if (isTheSamePoint(tempTopoPoint, tempEndPoint)) {
						tempEndPoint.setNodeID(tempTopoPoint.getNodeID());
						tempAdjacentEdgeArrayList.add(tempEdge);//相邻边			    	    			
     	    			for (int k2 = 0; k2 < juncCount; k2++) {
     	    				if (isTheSamePoint(juncCollArraylist.get(k2), tempBeginPoint)) {
     	    					tempAdjacentJuncArrayList.add(juncCollArraylist.get(k2));//相邻点    	    					
     	    					break;//找到相邻点，跳出循环
 							}		    	    				
     	    			}		    
 					}
     	    		else {
 						continue;
 					}					
				}
				tempTopoPoint.setRelationEdges(tempAdjacentEdgeArrayList);
				tempTopoPoint.setRelationNodes(tempAdjacentJuncArrayList);
			}			
			System.out.print("点线拓扑构建结束:" + '\n');
			long endCreatePointLineTopoTime = System.currentTimeMillis();
			System.out.print("点线拓扑构建时间:" + (endCreatePointLineTopoTime - startCreatePointLineTopoTime)/1000 + "s" + '\n');
						
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
		}
	}
	
	/*判断两点是否为同一点*/
	private static Boolean isTheSamePoint(MapMatchNode point1, MapMatchNode point2)
	{
		try
		{
			if (Math.abs(point1.x - point2.x) < 0.0000001 && Math.abs(point1.y - point2.y) < 0.0000001)
			{
				return true;
			}
			else
				return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.print(e.getMessage());
			return false;
		}
	}	
}
