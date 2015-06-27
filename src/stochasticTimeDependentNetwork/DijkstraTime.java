package stochasticTimeDependentNetwork;

import java.util.ArrayList;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.MapMatchNode;
import utilityPackage.PubClass;
import entity.PropertiesUtilJAR;

public class DijkstraTime {
	static ArrayList<DijkstraEdge> graph = null;//����ͼ
    static ArrayList<DijkstraNode> redAgg = null;//��֪���·���Ķ��㼯
    static ArrayList<DijkstraNode> blueAgg = null;//δ֪���·���Ķ��㼯
    static DijkstraEdge[] parents = null;//ÿ�����������·���еĸ���㼰����֮���Ȩ��
    static DijkstraNode[]nodes = null;
    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
	    String startTimeStr = "2013-01-01 00:00:00";//ʱ�����翪ʼʱ��
	    createDigraph();
        //��ʼ����֪���·���Ķ��㼯,ֻ���붥����Ϊ��ʼ�ڵ�
        redAgg = new ArrayList<DijkstraNode>();
        redAgg.add(nodes[0]);
        //��ʼ��δ֪���·���Ķ��㼯
        blueAgg = new ArrayList<DijkstraNode>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        //��ʼ��ÿ�����������·���еĸ���㼰����֮���Ȩ��,Ȩ��-1��ʾ����ͨ
        parents = new DijkstraEdge[nodes.length];
        nodes[0].setShortestTimeStr(startTimeStr);//���ó�ʼ�ڵ����·��ʱ��
        parents[0] = new DijkstraEdge(nodes[0], new DijkstraNode(), 0);
        for (int i = 0; i < blueAgg.size(); i++) {
        	DijkstraNode tempNode = blueAgg.get(i);
        	//�����Ƿ�ֱ����ͨ
        	if (isNodeConnected(nodes[0], tempNode)) {
        		parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, true);
			}          
        	else {
        		parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, false);
			}
        }        
        //�����㼯���ҳ�Ȩ����С���Ǹ�����,���������뵽��㼯�� 
        while (blueAgg.size() > 0) {
            MinShortPath msp = getMinPath(startTimeStr);
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            DijkstraNode node = msp.getLastNode();//Ȩ����С�Ķ���   
            redAgg.add(node); 
            // �����Ϊ�������µĶ���,���������㼯�еĶ�������·����С,��Ҫ��������
            startTimeStr = msp.getTimeStr();
            setWeight(node, startTimeStr);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("����ʱ��Ϊ��"+t+"ms");
     }
     
    /*************************************
     * ��������ͼ
     * 1.ѡ����ʼ�ڵ�
     * 2.����ÿ���ߣ������ʼ�ڵ�Զ�Ķ˵�Ϊ�����ߵĺ���ڵ㣨nextNode��
     * ���������Ķ˵�Ϊ�����ߵ�ǰ��ڵ㣨preNode��
     * 
     * ***********************************/
    public static void createDigraph(){
    	try {
    		String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
    		String roadNetworkNamecoll[] = roadNetworkName.split(",");
    		String fileName = roadNetworkNamecoll[3];//�ļ���
    		System.out.print("��ʼ�����л�����" + '\n');	
    		MapMatchAlgorithm.instance().readRoadFile(fileName);
    		System.out.print("���л����ݶ�ȡ�ɹ�" + '\n');
    		ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
    		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
    		DijkstraNode startNode = new DijkstraNode();//��ʼ�ڵ�
    		nodes = new DijkstraNode[juncCollArrayList.size()];//��ʼ�����㼯
    		graph = new ArrayList<DijkstraEdge>();
    		for (int i = 0; i < juncCollArrayList.size(); i++) {
    			MapMatchNode node = juncCollArrayList.get(i);
    			nodes[i] = new DijkstraNode();
    			nodes[i].setNodeID(node.getNodeID());
    			nodes[i].setL(node.getX());
    			nodes[i].setB(node.getY());
    			//�����ʼ�ڵ�
				if (node.getNodeID() == 1 ) {
					startNode.setNodeID(node.getNodeID());
					startNode.setL(node.getX());
					startNode.setB(node.getY());				
				}
			}
    		//ת�������
    		MapMatchNode startNodeMapMatch = new MapMatchNode();
    		startNodeMapMatch.setNodeID(startNode.getNodeID());
    		startNodeMapMatch.setX(startNode.getL());
    		startNodeMapMatch.setY(startNode.getB());
    		for (int i = 0; i < polylineCollArrayList.size(); i++) {
    			MapMatchEdge tempEdge = polylineCollArrayList.get(i);
    			if (tempEdge.getEdgeID() == 27) {
    				System.out.print(" ");
				}
    			MapMatchNode beginNode = tempEdge.getBeginPoint();
    			MapMatchNode endNode = tempEdge.getEndPoint();
    			double edgeLength = tempEdge.getEdgeLength();
    			double distanceStartBegin = PubClass.distance(startNodeMapMatch, beginNode);
    			double distanceStartEnd = PubClass.distance(startNodeMapMatch, endNode);
    			DijkstraNode preDijkstraNode = new DijkstraNode();//ǰ��ڵ�
    			DijkstraNode nextDijkstraNode = new DijkstraNode();//����ڵ�
    			//����С��Ϊ�ߵ�ǰ��ڵ㣬����Ϊ����ڵ�
    			if (distanceStartBegin > distanceStartEnd) {
    				preDijkstraNode.setNodeID(endNode.getNodeID());
					preDijkstraNode.setL(endNode.getX());
					preDijkstraNode.setB(endNode.getY());
					nextDijkstraNode.setNodeID(beginNode.getNodeID());
					nextDijkstraNode.setL(beginNode.getX());
					nextDijkstraNode.setB(beginNode.getY());
				}
    			else {
    				preDijkstraNode.setNodeID(beginNode.getNodeID());
					preDijkstraNode.setL(beginNode.getX());
					preDijkstraNode.setB(beginNode.getY());
					nextDijkstraNode.setNodeID(endNode.getNodeID());
					nextDijkstraNode.setL(endNode.getX());
					nextDijkstraNode.setB(endNode.getY());
				}
    			DijkstraEdge dijkstraEdge = new DijkstraEdge(preDijkstraNode, nextDijkstraNode, edgeLength);
    			dijkstraEdge.setConnectedProperty(true);
    			graph.add(dijkstraEdge);
			}
    		System.out.print(" " + '\n');
    		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
		}	
    }
       
     /** *//**
      * �õ�һ���ڵ�ĸ��ڵ�
      * 
      * @param parents
      * @param node
      * @return
      */
     public static DijkstraNode getParent(DijkstraEdge[] parents, DijkstraNode node) {
    	 DijkstraNode returnNode = new DijkstraNode();
    	 if (parents != null) {
             for (DijkstraEdge parent : parents) {
                 if (parent.getParentProperty() == true && parent.getNextNode().getNodeID() == node.getNodeID()) {
                	 returnNode = parent.getPreNode();
                 }
             }
         }
         return returnNode;
     }

     /** *//**
      * �����������㼯��ʣ��ڵ�����·������
      * 1.����ڵ�����·������
      * 2.�ڵ�����·��������ԭ�ȵ����·�����бȽϣ��Ӷ����ýڵ�����·������
      * @param preNode
      * @param map
      * @param blueAgg
      */
     public static void setWeight(DijkstraNode preNode, String timeStr) {
         if (graph != null && parents != null && blueAgg != null) {
             for (DijkstraNode node : blueAgg) {
                 MinShortPath msp = getCurrentMinPath(node, timeStr);
                 double w1 = msp.getWeight();//���·������
                 if (w1 == -1)//���������·��
                     continue;
                 for (DijkstraEdge edge : parents) {
                     if (edge.getNextNode().getNodeID() == node.getNodeID()) {
                    	 //�޸�ͼ�ṹ
                    	 //��Ϊ�������ڵ�֮�䲻����·���Լ�ԭ��·���������µ����·�����ȱȽ�
                         if (!isNodeConnected(edge.getPreNode(), edge.getNextNode()) || edge.getTimeWeight() > w1) {
                        	 edge.setConnectedProperty(true);//������ͨ��
                        	 edge.setTimeWeight(w1);//���·������
                        	 edge.setPreNode(preNode);//�������ö���ĸ�����
                             break;
                         }
                     }
                 }
             }
         }
     }

     /** *//**
      * �õ�����ڵ�֮���Ȩ��
      * ��������ͨ�򷵻������Ȩ�أ����򷵻�-1
      * @param map
      * @param map
      * @param preNode
      * @param node
      * @return
      */
     public static double getWeight(DijkstraNode preNode, DijkstraNode nextNode) {
         if (graph != null) {
             for (DijkstraEdge tempEdge : graph) {
            	 DijkstraNode tempPreNode = tempEdge.getPreNode();
            	 DijkstraNode tempNextNode = tempEdge.getNextNode();
                 if (tempPreNode.getNodeID() == preNode.getNodeID() && tempNextNode.getNodeID() == nextNode.getNodeID())
                     return tempEdge.getWeight();
             }
         }
         return -1;
     }

     /**
      * �õ�����֮��ĳһʱ���ʱ��Ȩ��
      * preNode:ǰ��ڵ�
      * nextNode������ڵ�
      * timeStr��ʱ�䣬��ʽΪ��"2013-01-01 00:00:00"
      * */
     public static double getTimeWeight(DijkstraNode preNode, DijkstraNode nextNode, String timeStr) {
         if (graph != null) {
             for (DijkstraEdge tempEdge : graph) {
            	 DijkstraNode tempPreNode = tempEdge.getPreNode();
            	 DijkstraNode tempNextNode = tempEdge.getNextNode();
                 if (tempPreNode.getNodeID() == preNode.getNodeID() && tempNextNode.getNodeID() == nextNode.getNodeID())
                     return tempEdge.getTimeWeight(timeStr);
             }
         }
         return -1;
     }
     
     /**
      * �ж������Ƿ���ͨ
      * preNode:ǰ��ڵ�
      * nextNode������ڵ�
      * */
     public static Boolean isNodeConnected(DijkstraNode preNode, DijkstraNode nextNode) {
         if (graph != null) {
             for (DijkstraEdge tempEdge : graph) {
            	 DijkstraNode tempPreNode = tempEdge.getPreNode();
            	 DijkstraNode tempNextNode = tempEdge.getNextNode();
                 if (tempPreNode.getNodeID() == preNode.getNodeID() && tempNextNode.getNodeID() == nextNode.getNodeID())
                     return true;
             }
         }
         return false;
     }
     
     /** *//**
      * �����㼯�����ҳ�ĳʱ��·����С���Ǹ��ڵ�
      * 1.�������㼯�е�ÿһ���㣬�ҳ���ÿһ�����е���С·��
      * 2.�Ƚ����е��е���С·�����ҳ�·����С���Ǹ��ڵ�
      * @param map
      * @param blueAgg
      * @return
      */
     public static MinShortPath getMinPath(String timeStr) {
         MinShortPath msp = null;//�����㼯�����ҳ���С��·��
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 DijkstraNode node = blueAgg.get(j);
                 MinShortPath tempMsp = getCurrentMinPath(node, timeStr);//����ǰ�ڵ���С��·��               
                 if (msp == null || tempMsp.getWeight()!=-1 &&  tempMsp.getWeight() < msp.getWeight()) {
                	 msp = tempMsp;
                     index = j;
                 }
             }
             blueAgg.remove(index);
         }
         return msp;
     }

     /** *//**
      * �õ�ĳһ�ڵ�ĳʱ�̵����·��(ʵ���Ͽ����ж���,����ֻ����һ��)
      * ���ݺ�㼯���еĽڵ���Ŀ��ڵ�֮���ϵ�õ����·��
      * 1.ÿһ����㼯���еĵ���Ϊ���ڵ㣬Ŀ��ڵ���Ϊ��ǰ�ڵ�
      * 2.���ݸ��ڵ���Ŀ��ڵ�֮��Ĺ�ϵ�����ϱ���ֱ����ʼ�ڵ�
      * @param node
      * @return
      */
     public static MinShortPath getCurrentMinPath(DijkstraNode node, String timeStr) {
         MinShortPath msp = new MinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 MinShortPath tempMsp = new MinShortPath(node);
                 DijkstraNode parent = redAgg.get(i);//���ڵ�
                 DijkstraNode curNode = node;
                 while (parent.getNodeID() > -1) {
                     //���������ͨ,��Ŀ��ڵ������Ʋ�
                     if (isNodeConnected(parent, curNode)) {
                    	 //node�ĸ��ڵ㣬�Ի����Сʱ��·����ʱ��
                    	 if (isNodeConnected(parent, node)) {
                    		 String parentMinTimeStr = parent.getShortestTimeStr();//��¼�����ڵ����Сʱ��·������Сʱ��
                        	 double weight = getTimeWeight(parent, curNode, parentMinTimeStr);//�õ����ڵ�֮���ڸ��ڵ�ȡ�����·��ʱ�̵�Ȩ��
                        	 tempMsp.addNode(parent);
                             tempMsp.addWeight(weight);
                             String[] endTimeArray = new String[1];
                             PubClass.obtainEndTimeAccordStartTime(parentMinTimeStr, (int)weight, endTimeArray);
                             tempMsp.setTimeStr(endTimeArray[0]);
                             curNode = parent;
                             parent = getParent(parents, parent);//�õ�һ���ڵ�ĸ��ڵ�
						}
                    	 else {
                    		 String parentMinTimeStr = parent.getShortestTimeStr();//��¼�����ڵ����Сʱ��·������Сʱ��
                        	 double weight = getTimeWeight(parent, curNode, parentMinTimeStr);//�õ����ڵ�֮���ڸ��ڵ�ȡ�����·��ʱ�̵�Ȩ��
                        	 tempMsp.addNode(parent);
                             tempMsp.addWeight(weight);
                             curNode = parent;
                             parent = getParent(parents, parent);//�õ�һ���ڵ�ĸ��ڵ�
						}                    	 
                     } else
                         break;
                 }
                 //���ʱ��·����Ȩֵ
                 if (msp.getWeight() == -1 || tempMsp.getWeight()!=-1 && msp.getWeight() > tempMsp.getWeight()){
                	 msp = tempMsp;
                	 node.setShortestTimeStr(msp.getTimeStr());//����ǰ�ڵ����·����ʱ��
                 }
             }
         }
         return msp;
     }
     
     
     
     
}
