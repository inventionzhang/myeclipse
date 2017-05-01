package org.lmars.network.timeVaryingNetwork;

import java.util.ArrayList;

public class MinShortPath {
	private ArrayList<DijkstraNode> nodeList;// 最短路径集
    private double weight;// 最短路径累计权值
    private String timeStr;//到某点路径最短的时刻

    //构造函数：初始节点加入最短路径中
    public MinShortPath(DijkstraNode node) {
        nodeList = new ArrayList<DijkstraNode>();
        nodeList.add(node);
        weight = -1;
    }

    public ArrayList<DijkstraNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<DijkstraNode> nodeList) {
        this.nodeList = nodeList;
    }

  //节点放在第一个索引位置
    public void addNode(DijkstraNode node) {
        if (nodeList == null)
            nodeList = new ArrayList<DijkstraNode>();
        nodeList.add(0, node);
    }

    public DijkstraNode getLastNode() { 
        int size = nodeList.size();
        return nodeList.get(size - 1);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }    

    public void outputPath() {
        outputPath(new DijkstraNode());
    }

    public void outputPath(DijkstraNode srcNode) {
    	String result="";
    	if (srcNode.getNodeID() != -1)
    		nodeList.add(srcNode);
    	for (int i = 0; i < nodeList.size(); i++) {
    		result += "" + nodeList.get(i).getNodeID();
    		if (i < nodeList.size() - 1)
    			result += "-->";
    	}
    	result += "  最短时间路径为:"+weight;     
    	System.out.println(result);
    }

    //最短路径累计权值
    public void addWeight(double w) {
        if (weight == -1)
            weight = w;
        else
            weight += w;
    }

}
