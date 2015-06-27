package mapMatchingGPS;

import java.util.ArrayList;

import utilityPackage.PubClass;

import entity.Node;

public class CreateTopolygy {
	
	public static void createTopolygonRelation(ArrayList<MapMatchNode> juncCollArraylist, ArrayList<MapMatchEdge> polylineCollArrayList){
		try {
			//����mapMatchEdge�ĳ���
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
			
			/*��ͨ���ϵĽ�����������Ҫ�Ľ����������߶�Ϊ����Ȧ��һ����Χ�ڵ�·�ν�����ͨ���Ϲ���
			 * �����ߵ�һ����������������ͨ����,���ܰ�������
			 *�߱���ƥ��һ��һ����ͨ���ϵ��ߣ��Լ���ͨ��������յ�
			 *����һ����ͨ����ƥ�������ͨ�����Լ���ͨ��������յ�
			 *���ݶ�����ͨ����ƥ��������ͨ�����Լ���ͨ��������յ�*/
			long startCreateConnLevelTime = System.currentTimeMillis();
			System.out.print("�����ߵ���ͨ����:" + '\n');
			int polylineCount = polylineCollArrayList.size();
			for (int i = 0; i < polylineCount; i++)
			{
				System.out.print("������ͨ����:" + i + ":" + polylineCount + '\n');
				MapMatchEdge ZerotempEdge = polylineCollArrayList.get(i);
				MapMatchNode tempBeginPoint = ZerotempEdge.getBeginPoint();
				MapMatchNode tempEndPoint = ZerotempEdge.getEndPoint();
				ArrayList<MapMatchEdge> tempFirstLevelConnEdgeArray = new ArrayList<MapMatchEdge>() ;//һ����ͨ����
				ArrayList<MapMatchEdge> tempSecoLevelConnEdgeArray = new ArrayList<MapMatchEdge>();//������ͨ����
				ArrayList<MapMatchEdge> tempThirdLevelConnEdgeArray = new ArrayList<MapMatchEdge>();//��������ͨ����
				for (int j = 0; j < polylineCount; j++)
				{
					MapMatchEdge firstTempEdge = polylineCollArrayList.get(j);
					MapMatchNode firstTempBeginPoint = firstTempEdge.getBeginPoint();
					MapMatchNode firstTempEndPoint = firstTempEdge.getEndPoint();
					//һ����ͨ����
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
				//������ͨ����
				//����һ����ͨ������·��һ����ͨ�ߵ���ͨ�յ���Ѱ������ͨ����,
				//������ͨ�����в��ܰ����㼶��һ����ͨ�������߶�
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
				//������ͨ����
				//���ݶ�����ͨ������·�ζ�����ͨ�ߵ���ͨ�յ���Ѱ������ͨ����
				//������ͨ�����в��ܰ����㼶��һ����������ͨ�������߶�
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
				//����һ��������ͨ����
				ZerotempEdge.setFirstLevelConnEdgeArray(tempFirstLevelConnEdgeArray);
				ZerotempEdge.setSecoLevelConnEdgeArray(tempSecoLevelConnEdgeArray);
				ZerotempEdge.setThirdLevelConnEdgeArray(tempThirdLevelConnEdgeArray);				
			}//һ����������������ͨ���Ϲ�������
			System.out.print("�ߵ���ͨ���Ϲ�������:" + '\n');
			long endCreateConnLevelTime = System.currentTimeMillis();
			System.out.print("��·��ͨ���Ϲ���ʱ��:" + (endCreateConnLevelTime - startCreateConnLevelTime)/1000 + "s" + '\n');
			//�����������ˣ����ֱ����������
			long startCreatePointLineTopoTime = System.currentTimeMillis();
			System.out.print("��ʼ������������:");
			int juncCount = juncCollArraylist.size();
			for (int i = 0; i < juncCount; i++)
			{
				System.out.print("������������:" + i + ":" + juncCount + '\n');
				MapMatchNode tempTopoPoint = juncCollArraylist.get(i);
				ArrayList<MapMatchEdge> tempAdjacentEdgeArrayList = new ArrayList<MapMatchEdge>();//�洢�ڵ�����Edge
				ArrayList<MapMatchNode> tempAdjacentJuncArrayList=new ArrayList<MapMatchNode>();//�洢�ڽӵ�				
				for (int j = 0; j < polylineCount; j++)
				{
					MapMatchEdge tempEdge = polylineCollArrayList.get(j);
					MapMatchNode tempBeginPoint = tempEdge.getBeginPoint();
					MapMatchNode tempEndPoint = tempEdge.getEndPoint();
					if (isTheSamePoint(tempTopoPoint,tempBeginPoint))
					{
						tempBeginPoint.setNodeID(tempTopoPoint.getNodeID());//����ID
						tempAdjacentEdgeArrayList.add(tempEdge);
						for (int k1 = 0; k1 < juncCount; k1++) {
     	    				if (isTheSamePoint(juncCollArraylist.get(k1), tempEndPoint)) {			    	    								    	    					
     	    					tempAdjacentJuncArrayList.add(juncCollArraylist.get(k1));//���ڵ�
     	    					break;//�ҵ����ڵ㣬����ѭ��
 							}		    	    				
     	    			}
					}
					else if (isTheSamePoint(tempTopoPoint, tempEndPoint)) {
						tempEndPoint.setNodeID(tempTopoPoint.getNodeID());
						tempAdjacentEdgeArrayList.add(tempEdge);//���ڱ�			    	    			
     	    			for (int k2 = 0; k2 < juncCount; k2++) {
     	    				if (isTheSamePoint(juncCollArraylist.get(k2), tempBeginPoint)) {
     	    					tempAdjacentJuncArrayList.add(juncCollArraylist.get(k2));//���ڵ�    	    					
     	    					break;//�ҵ����ڵ㣬����ѭ��
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
			System.out.print("�������˹�������:" + '\n');
			long endCreatePointLineTopoTime = System.currentTimeMillis();
			System.out.print("�������˹���ʱ��:" + (endCreatePointLineTopoTime - startCreatePointLineTopoTime)/1000 + "s" + '\n');
						
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();			
		}
	}
	
	/*�ж������Ƿ�Ϊͬһ��*/
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
