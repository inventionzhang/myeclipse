package org.lmars.network.timeVaryingNetwork;

import java.util.ArrayList;

public class CopyDijkstra {
	static ArrayList<CopySide> graph = null;
    static ArrayList<Integer> redAgg = null;
    static ArrayList<Integer> blueAgg = null;
    static CopySide[] parents = null;

    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
        // 初始化顶点集
        int[] nodes = {0, 1, 3, 2, 4, 5,6};       
        // 初始化有向权重图
        graph = new ArrayList<CopySide>();
        graph.add(new CopySide(0, 1, 10));
        graph.add(new CopySide(0, 3, 30));
        graph.add(new CopySide(0, 4, 100));
        graph.add(new CopySide(1, 2, 50));
        graph.add(new CopySide(2, 4, 10));
        graph.add(new CopySide(3, 2, 20));
        graph.add(new CopySide(3, 4, 60));
        graph.add(new CopySide(4, 5, 50));
        graph.add(new CopySide(3, 5, 60));
        graph.add(new CopySide(5, 6, 10));
        graph.add(new CopySide(3, 6, 80));
        //初始化已知最短路径的顶点集，即红点集，只加入顶点0
        redAgg = new ArrayList<Integer>();
        redAgg.add(nodes[0]);
        // 初始化未知最短路径的顶点集,即蓝点集
        blueAgg = new ArrayList<Integer>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        // 初始化每个顶点在最短路径中的父结点,及它们之间的权重,权重-1表示无连通
        parents = new CopySide[nodes.length];
        parents[0] = new CopySide(-1, nodes[0], 0);
        for (int i = 0; i < blueAgg.size(); i++) {
            int n = blueAgg.get(i);
            parents[i + 1] = new CopySide(nodes[0], n, getWeight(nodes[0], n));
        }
        // 找从蓝点集中找出权重最小的那个顶点,并把它加入到红点集中 
        while (blueAgg.size() > 0) {
            CopyMinShortPath msp = getMinSideNode();
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            int node = msp.getLastNode();//权重最小的顶点
            redAgg.add(node); 
            // 如果因为加入了新的顶点,而导致蓝点集中的顶点的最短路径减小,则要重要设置
            setWeight(node);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("运行时间为："+t+"ms");
     }
     
     /** *//**
      * 得到一个节点的父节点
      * 
      * @param parents
      * @param node
      * @return
      */
     public static int getParent(CopySide[] parents, int node) {
         if (parents != null) {
             for (CopySide nd : parents) {
                 if (nd.getNode() == node) {
                     return nd.getPreNode();
                 }
             }
         }
         return -1;
     }

     /** *//**
      * 重新设置蓝点集中剩余节点的最短路径长度
      * 1.计算节点的最短路径长度
      * 2.节点的最短路径长度与原先的最短路径进行比较，从而设置节点的最短路径长度
      * @param preNode
      * @param map
      * @param blueAgg
      */
     public static void setWeight(int preNode) {
         if (graph != null && parents != null && blueAgg != null) {
             for (int node : blueAgg) {
                 CopyMinShortPath msp = getMinPath(node);
                 int w1 = msp.getWeight();//最短路径长度
                 if (w1 == -1)//不存在最短路径
                     continue;
                 for (CopySide n : parents) {
                     if (n.getNode() == node) {
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
      * @param map
      * @param map
      * @param preNode
      * @param node
      * @return
      */
     public static int getWeight(int preNode, int node) {
         if (graph != null) {
             for (CopySide s : graph) {
                 if (s.getPreNode() == preNode && s.getNode() == node)
                     return s.getWeight();
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
     public static CopyMinShortPath getMinSideNode() {
         CopyMinShortPath minMsp = null;//从蓝点集合中找出最小的路径
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 int nodeID = blueAgg.get(j);
                 CopyMinShortPath msp = getMinPath(blueAgg.get(j));//到当前节点最小的路径
                 if (minMsp == null || msp.getWeight()!=-1 &&  msp.getWeight() < minMsp.getWeight()) {
                     minMsp = msp;
                     index = j;
                 }
             }
             blueAgg.remove(index);
         }
         return minMsp;
     }

     /** *//**
      * 得到某一节点的最短路径(实际上可能有多条,现在只考虑一条)
      * 1.根据红点集合中的节点如目标节点之间关系得到最短路径
      * 2.每一个红点结合中的点作为父节点，目标节点作为当前节点，根据父节点与目标节点之间的关系，不断遍历到起始节点
      * @param node
      * @return
      */
     public static CopyMinShortPath getMinPath(int node) {
         CopyMinShortPath msp = new CopyMinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 CopyMinShortPath tempMsp = new CopyMinShortPath(node);
                 int parent = redAgg.get(i);//父节点
                 int curNode = node;
                 while (parent > -1) {
                     int weight = getWeight(parent, curNode);//得到两点节点之间的权重
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
