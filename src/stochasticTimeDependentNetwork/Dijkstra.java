package stochasticTimeDependentNetwork;

import java.util.ArrayList;

import utilityPackage.PubClass;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.MapMatchNode;
import entity.Node;
import entity.PropertiesUtilJAR;

/*Dijkstra(�Ͻ�˹����)�㷨�ǵ��͵ĵ�Դ���·���㷨�����ڼ���һ���ڵ㵽�������нڵ�����·����
 * ��Ҫ�ص�������ʼ��Ϊ������������չ��ֱ����չ���յ�Ϊֹ��
 * */

public class Dijkstra {
	static ArrayList<DijkstraEdge> graph = null;//����ͼ
    static ArrayList<DijkstraNode> redAgg = null;//��֪���·���Ķ��㼯
    static ArrayList<DijkstraNode> blueAgg = null;//δ֪���·���Ķ��㼯
    static DijkstraEdge[] parents = null;//ÿ�����������·���еĸ���㼰����֮���Ȩ��
    static DijkstraNode[]nodes = null;
    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
	    createDigraph();
        //��ʼ����֪���·���Ķ��㼯,����㼯,ֻ���붥���ʼ�ڵ�
        redAgg = new ArrayList<DijkstraNode>();
        redAgg.add(nodes[0]);
        //��ʼ��δ֪���·���Ķ��㼯,�����㼯
        blueAgg = new ArrayList<DijkstraNode>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        //��ʼ��ÿ�����������·���еĸ���㼰����֮���Ȩ��,Ȩ��-1��ʾ����ͨ
        parents = new DijkstraEdge[nodes.length];
        parents[0] = new DijkstraEdge(new DijkstraNode(), nodes[0], 0);
        for (int i = 0; i < blueAgg.size(); i++) {
        	DijkstraNode tempNode = blueAgg.get(i);
            parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, getWeight(nodes[0], tempNode));
        }
        //�����㼯���ҳ�Ȩ����С���Ǹ�����,���������뵽��㼯�� 
        while (blueAgg.size() > 0) {
            MinShortPath msp = getMinPath();
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            DijkstraNode node = msp.getLastNode();//Ȩ����С�Ķ���
            redAgg.add(node); 
            // �����Ϊ�������µĶ���,���������㼯�еĶ�������·����С,��Ҫ��������
            setWeight(node);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("����ʱ��Ϊ��"+t+"ms");
     }
     
    /*************************************
     * ��������ͼ
     * ����ÿ���ߣ��������Զ�Ķ˵�Ϊ�����ߵĺ���ڵ㣨nextNode��
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
    			graph.add(dijkstraEdge);
			}
    		System.out.print(" " + '\n');
    		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
		}	
    }
    
    //��ʼ�����㼯
    public static void initializeNodes(DijkstraNode[]nodes){
    	try {
    		ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
    		
    		
    		
		} catch (Exception e) {
			// TODO: handle exception
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
             for (DijkstraEdge nd : parents) {
                 if (nd.getNextNode().getNodeID() == node.getNodeID()) {
                	 returnNode = nd.getPreNode();
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
     public static void setWeight(DijkstraNode preNode) {
         if (graph != null && parents != null && blueAgg != null) {
             for (DijkstraNode node : blueAgg) {
                 MinShortPath msp = getCurrentMinPath(node);
                 double w1 = msp.getWeight();//���·������
                 if (w1 == -1)//���������·��
                     continue;
                 for (DijkstraEdge n : parents) {
                     if (n.getNextNode().getNodeID() == node.getNodeID()) {
                    	 //����֮�䲻����·���Լ�ԭ��·���������µ����·�����ȱȽ�
                         if (n.getWeight() == -1 || n.getWeight() > w1) {
                             n.setWeight(w1);//���·������
                             n.setPreNode(preNode);//�������ö���ĸ�����
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

     /** *//**
      * �����㼯�����ҳ�·����С���Ǹ��ڵ�
      * 1.�������㼯�е�ÿһ���㣬�ҳ���ÿһ�����е���С·��
      * 2.�Ƚ����е��е���С·�����ҳ�·����С���Ǹ��ڵ�
      * @param map
      * @param blueAgg
      * @return
      */
     public static MinShortPath getMinPath() {
         MinShortPath msp = null;//�����㼯�����ҳ���С��·��
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 DijkstraNode node = blueAgg.get(j);
                 MinShortPath tempMsp = getCurrentMinPath(node);//����ǰ�ڵ���С��·��
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
      * �õ�ĳһ�ڵ�����·��(ʵ���Ͽ����ж���,����ֻ����һ��)
      * ���ݺ�㼯���еĽڵ���Ŀ��ڵ�֮���ϵ�õ����·��
      * 1.ÿһ����㼯���еĵ���Ϊ���ڵ㣬Ŀ��ڵ���Ϊ��ǰ�ڵ�
      * 2.���ݸ��ڵ���Ŀ��ڵ�֮��Ĺ�ϵ�����ϱ���ֱ����ʼ�ڵ�
      * @param node
      * @return
      */
     public static MinShortPath getCurrentMinPath(DijkstraNode node) {
         MinShortPath msp = new MinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 MinShortPath tempMsp = new MinShortPath(node);
                 DijkstraNode parent = redAgg.get(i);//���ڵ�
                 DijkstraNode curNode = node;
                 while (parent.getNodeID() > -1) {
                	 double weight = getWeight(parent, curNode);//�õ�����ڵ�֮���Ȩ��
                     //���������ͨ
                     if (weight > -1) {
                         tempMsp.addNode(parent);
                         tempMsp.addWeight(weight);
                         curNode = parent;
                         parent = getParent(parents, parent);//�õ�һ���ڵ�ĸ��ڵ�
                     } else
                         break;
                 }
                 if (msp.getWeight() == -1 || tempMsp.getWeight()!=-1 && msp.getWeight() > tempMsp.getWeight())
                     msp = tempMsp;
             }
         }
         return msp;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 }
