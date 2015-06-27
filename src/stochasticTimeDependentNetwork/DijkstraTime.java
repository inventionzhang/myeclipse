package stochasticTimeDependentNetwork;

import java.util.ArrayList;

import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.MapMatchEdge;
import mapMatchingGPS.MapMatchNode;
import utilityPackage.PubClass;
import entity.PropertiesUtilJAR;

public class DijkstraTime {
	static ArrayList<DijkstraEdge> graph = null;//有向图
    static ArrayList<DijkstraNode> redAgg = null;//已知最短路径的顶点集
    static ArrayList<DijkstraNode> blueAgg = null;//未知最短路径的顶点集
    static DijkstraEdge[] parents = null;//每个顶点在最短路径中的父结点及它们之间的权重
    static DijkstraNode[]nodes = null;
    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
	    String startTimeStr = "2013-01-01 00:00:00";//时变网络开始时间
	    createDigraph();
        //初始化已知最短路径的顶点集,只加入顶点作为初始节点
        redAgg = new ArrayList<DijkstraNode>();
        redAgg.add(nodes[0]);
        //初始化未知最短路径的顶点集
        blueAgg = new ArrayList<DijkstraNode>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        //初始化每个顶点在最短路径中的父结点及它们之间的权重,权重-1表示无连通
        parents = new DijkstraEdge[nodes.length];
        nodes[0].setShortestTimeStr(startTimeStr);//设置初始节点最短路径时间
        parents[0] = new DijkstraEdge(nodes[0], new DijkstraNode(), 0);
        for (int i = 0; i < blueAgg.size(); i++) {
        	DijkstraNode tempNode = blueAgg.get(i);
        	//两点是否直接连通
        	if (isNodeConnected(nodes[0], tempNode)) {
        		parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, true);
			}          
        	else {
        		parents[i + 1] = new DijkstraEdge(nodes[0], tempNode, false);
			}
        }        
        //从蓝点集中找出权重最小的那个顶点,并把它加入到红点集中 
        while (blueAgg.size() > 0) {
            MinShortPath msp = getMinPath(startTimeStr);
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            DijkstraNode node = msp.getLastNode();//权重最小的顶点   
            redAgg.add(node); 
            // 如果因为加入了新的顶点,而导致蓝点集中的顶点的最短路径减小,则要重新设置
            startTimeStr = msp.getTimeStr();
            setWeight(node, startTimeStr);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("运行时间为："+t+"ms");
     }
     
    /*************************************
     * 创建有向图
     * 1.选定初始节点
     * 2.对于每条边，距离初始节点远的端点为该条边的后向节点（nextNode）
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
    			if (tempEdge.getEdgeID() == 27) {
    				System.out.print(" ");
				}
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
      * 得到一个节点的父节点
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
      * 重新设置蓝点集中剩余节点的最短路径长度
      * 1.计算节点的最短路径长度
      * 2.节点的最短路径长度与原先的最短路径进行比较，从而设置节点的最短路径长度
      * @param preNode
      * @param map
      * @param blueAgg
      */
     public static void setWeight(DijkstraNode preNode, String timeStr) {
         if (graph != null && parents != null && blueAgg != null) {
             for (DijkstraNode node : blueAgg) {
                 MinShortPath msp = getCurrentMinPath(node, timeStr);
                 double w1 = msp.getWeight();//最短路径长度
                 if (w1 == -1)//不存在最短路径
                     continue;
                 for (DijkstraEdge edge : parents) {
                     if (edge.getNextNode().getNodeID() == node.getNodeID()) {
                    	 //修改图结构
                    	 //作为父子两节点之间不存在路径以及原先路径长度与新的最短路径长度比较
                         if (!isNodeConnected(edge.getPreNode(), edge.getNextNode()) || edge.getTimeWeight() > w1) {
                        	 edge.setConnectedProperty(true);//设置连通性
                        	 edge.setTimeWeight(w1);//最短路径长度
                        	 edge.setPreNode(preNode);//重新设置顶点的父顶点
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

     /**
      * 得到两点之间某一时间的时间权重
      * preNode:前向节点
      * nextNode：后向节点
      * timeStr：时间，格式为："2013-01-01 00:00:00"
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
      * 判断两点是否联通
      * preNode:前向节点
      * nextNode：后向节点
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
      * 从蓝点集合中找出某时刻路径最小的那个节点
      * 1.遍历蓝点集中的每一个点，找出到每一个点中的最小路径
      * 2.比较所有点中的最小路径，找出路径最小的那个节点
      * @param map
      * @param blueAgg
      * @return
      */
     public static MinShortPath getMinPath(String timeStr) {
         MinShortPath msp = null;//从蓝点集合中找出最小的路径
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 DijkstraNode node = blueAgg.get(j);
                 MinShortPath tempMsp = getCurrentMinPath(node, timeStr);//到当前节点最小的路径               
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
      * 得到某一节点某时刻的最短路径(实际上可能有多条,现在只考虑一条)
      * 根据红点集合中的节点与目标节点之间关系得到最短路径
      * 1.每一个红点集合中的点作为父节点，目标节点作为当前节点
      * 2.根据父节点与目标节点之间的关系，不断遍历直到起始节点
      * @param node
      * @return
      */
     public static MinShortPath getCurrentMinPath(DijkstraNode node, String timeStr) {
         MinShortPath msp = new MinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 MinShortPath tempMsp = new MinShortPath(node);
                 DijkstraNode parent = redAgg.get(i);//父节点
                 DijkstraNode curNode = node;
                 while (parent.getNodeID() > -1) {
                     //两点间有联通,从目标节点往后推测
                     if (isNodeConnected(parent, curNode)) {
                    	 //node的父节点，以获得最小时间路径的时刻
                    	 if (isNodeConnected(parent, node)) {
                    		 String parentMinTimeStr = parent.getShortestTimeStr();//记录到父节点的最小时间路径的最小时刻
                        	 double weight = getTimeWeight(parent, curNode, parentMinTimeStr);//得到两节点之间在父节点取得最短路径时刻的权重
                        	 tempMsp.addNode(parent);
                             tempMsp.addWeight(weight);
                             String[] endTimeArray = new String[1];
                             PubClass.obtainEndTimeAccordStartTime(parentMinTimeStr, (int)weight, endTimeArray);
                             tempMsp.setTimeStr(endTimeArray[0]);
                             curNode = parent;
                             parent = getParent(parents, parent);//得到一个节点的父节点
						}
                    	 else {
                    		 String parentMinTimeStr = parent.getShortestTimeStr();//记录到父节点的最小时间路径的最小时刻
                        	 double weight = getTimeWeight(parent, curNode, parentMinTimeStr);//得到两节点之间在父节点取得最短路径时刻的权重
                        	 tempMsp.addNode(parent);
                             tempMsp.addWeight(weight);
                             curNode = parent;
                             parent = getParent(parents, parent);//得到一个节点的父节点
						}                    	 
                     } else
                         break;
                 }
                 //最短时间路径的权值
                 if (msp.getWeight() == -1 || tempMsp.getWeight()!=-1 && msp.getWeight() > tempMsp.getWeight()){
                	 msp = tempMsp;
                	 node.setShortestTimeStr(msp.getTimeStr());//到当前节点最短路径的时刻
                 }
             }
         }
         return msp;
     }
     
     
     
     
}
