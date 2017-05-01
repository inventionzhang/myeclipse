package org.lmars.network.timeVaryingNetwork;

import java.util.ArrayList;

import org.lmars.network.entity.Node;
import org.lmars.network.mapMatchingGPS.MapMatchAlgorithm;
import org.lmars.network.mapMatchingGPS.MapMatchEdge;
import org.lmars.network.mapMatchingGPS.MapMatchNode;
import org.lmars.network.util.PropertiesUtilJAR;
import org.lmars.network.util.PubClass;



/*Dijkstra(迪杰斯特拉)算法是典型的单源最短路径算法，用于计算一个节点到其他所有节点的最短路径。
 * 主要特点是以起始点为中心向外层层扩展，直到扩展到终点为止。
 * */

public class Dijkstra {
	static ArrayList<DijkstraEdge> graph = null;//有向图
    static ArrayList<DijkstraNode> redAgg = null;//已知最短路径的顶点集
    static ArrayList<DijkstraNode> blueAgg = null;//未知最短路径的顶点集
    static DijkstraEdge[] parents = null;//每个顶点在最短路径中的父结点及它们之间的权重
    static DijkstraNode[]nodes = null;
    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
	    createDigraph();
        //初始化已知最短路径的顶点集,即红点集,只加入顶点初始节点
        redAgg = new ArrayList<DijkstraNode>();
        redAgg.add(nodes[0]);
        //初始化未知最短路径的顶点集,即蓝点集
        blueAgg = new ArrayList<DijkstraNode>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        //初始化每个顶点在最短路径中的父结点及它们之间的权重,权重-1表示无连通
        parents = new DijkstraEdge[nodes.length];
        parents[0] = new DijkstraEdge(new DijkstraNode(), nodes[0], 0);
        for (int i = 0; i < blueAgg.size(); i++) {
        	DijkstraNode tempNode = blueAgg.get(i);
            parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, getWeight(nodes[0], tempNode));
        }
        //从蓝点集中找出权重最小的那个顶点,并把它加入到红点集中 
        while (blueAgg.size() > 0) {
            MinShortPath msp = getMinPath();
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            DijkstraNode node = msp.getLastNode();//权重最小的顶点
            redAgg.add(node); 
            // 如果因为加入了新的顶点,而导致蓝点集中的顶点的最短路径减小,则要重新设置
            setWeight(node);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("运行时间为："+t+"ms");
     }
     
    /*************************************
     * 创建有向图
     * 对于每条边，距离起点远的端点为该条边的后向节点（nextNode）
     * 距离起点近的端点为该条边的前向节点（preNode）
     * 
     * ***********************************/
    public static void createDigraph(){
    	try {
    		String roadNetworkName = PropertiesUtilJAR.getProperties("mapMatch1");
    		String roadNetworkNamecoll[] = roadNetworkName.split(",");
    		String fileName = roadNetworkNamecoll[3];//文件名
    		System.out.print("开始读序列化数据" + '\n');	
    		MapMatchAlgorithm.instance().readRoadFile(fileName);
    		System.out.print("序列化数据读取成功" + '\n');
    		ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
    		ArrayList<MapMatchEdge> polylineCollArrayList = MapMatchAlgorithm.instance().polylineCollArrayList;
    		DijkstraNode startNode = new DijkstraNode();//起始节点
    		nodes = new DijkstraNode[juncCollArrayList.size()];//初始化顶点集
    		graph = new ArrayList<DijkstraEdge>();
    		for (int i = 0; i < juncCollArrayList.size(); i++) {
    			MapMatchNode node = juncCollArrayList.get(i);
    			nodes[i] = new DijkstraNode();
    			nodes[i].setNodeID(node.getNodeID());
    			nodes[i].setL(node.getX());
    			nodes[i].setB(node.getY());
    			//获得起始节点
				if (node.getNodeID() == 1 ) {
					startNode.setNodeID(node.getNodeID());
					startNode.setL(node.getX());
					startNode.setB(node.getY());				
				}
			}
    		//转换后起点
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
    			DijkstraNode preDijkstraNode = new DijkstraNode();//前向节点
    			DijkstraNode nextDijkstraNode = new DijkstraNode();//后向节点
    			//距离小者为边的前向节点，否则为后向节点
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
    
    //初始化顶点集
    public static void initializeNodes(DijkstraNode[]nodes){
    	try {
    		ArrayList<MapMatchNode> juncCollArrayList = MapMatchAlgorithm.instance().juncCollArrayList;
    		
    		
    		
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	
    	
    }
    
    
    
     /** *//**
      * 得到一个节点的父节点
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
      * 重新设置蓝点集中剩余节点的最短路径长度
      * 1.计算节点的最短路径长度
      * 2.节点的最短路径长度与原先的最短路径进行比较，从而设置节点的最短路径长度
      * @param preNode
      * @param map
      * @param blueAgg
      */
     public static void setWeight(DijkstraNode preNode) {
         if (graph != null && parents != null && blueAgg != null) {
             for (DijkstraNode node : blueAgg) {
                 MinShortPath msp = getCurrentMinPath(node);
                 double w1 = msp.getWeight();//最短路径长度
                 if (w1 == -1)//不存在最短路径
                     continue;
                 for (DijkstraEdge n : parents) {
                     if (n.getNextNode().getNodeID() == node.getNodeID()) {
                    	 //两点之间不存在路径以及原先路径长度与新的最短路径长度比较
                         if (n.getWeight() == -1 || n.getWeight() > w1) {
                             n.setWeight(w1);//最短路径长度
                             n.setPreNode(preNode);//重新设置顶点的父顶点
                             break;
                         }
                     }
                 }
             }
         }
     }

     /** *//**
      * 得到两点节点之间的权重
      * 若两点联通则返回两点间权重，否则返回-1
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
      * 从蓝点集合中找出路径最小的那个节点
      * 1.遍历蓝点集中的每一个点，找出到每一个点中的最小路径
      * 2.比较所有点中的最小路径，找出路径最小的那个节点
      * @param map
      * @param blueAgg
      * @return
      */
     public static MinShortPath getMinPath() {
         MinShortPath msp = null;//从蓝点集合中找出最小的路径
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 DijkstraNode node = blueAgg.get(j);
                 MinShortPath tempMsp = getCurrentMinPath(node);//到当前节点最小的路径
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
      * 得到某一节点的最短路径(实际上可能有多条,现在只考虑一条)
      * 根据红点集合中的节点与目标节点之间关系得到最短路径
      * 1.每一个红点集合中的点作为父节点，目标节点作为当前节点
      * 2.根据父节点与目标节点之间的关系，不断遍历直到起始节点
      * @param node
      * @return
      */
     public static MinShortPath getCurrentMinPath(DijkstraNode node) {
         MinShortPath msp = new MinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 MinShortPath tempMsp = new MinShortPath(node);
                 DijkstraNode parent = redAgg.get(i);//父节点
                 DijkstraNode curNode = node;
                 while (parent.getNodeID() > -1) {
                	 double weight = getWeight(parent, curNode);//得到两点节点之间的权重
                     //两点间有联通
                     if (weight > -1) {
                         tempMsp.addNode(parent);
                         tempMsp.addWeight(weight);
                         curNode = parent;
                         parent = getParent(parents, parent);//得到一个节点的父节点
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
