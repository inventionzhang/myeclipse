package org.lmars.network.timeVaryingNetwork;

import java.util.ArrayList;

public class CopyMinShortPath {
	private ArrayList<Integer> nodeList;//最短路径集
    private int weight;//最短路径

    //构造函数：初始节点加入最短路径中
    public CopyMinShortPath(int node) {
        nodeList = new ArrayList<Integer>();
        nodeList.add(node);
        weight = -1;
    }

    public ArrayList<Integer> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Integer> nodeList) {
        this.nodeList = nodeList;
    }

    //节点放在第一个索引位置
    public void addNode(int node) {
        if (nodeList == null)
            nodeList = new ArrayList<Integer>();
        nodeList.add(0, node);
    }

    public int getLastNode() { 
        int size = nodeList.size();
        return nodeList.get(size - 1);

    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void outputPath() {
        outputPath(-1);
    }

    public void outputPath(int srcNode) {
     /*   String result = "[";
        if (srcNode != -1)
            nodeList.add(srcNode);
        for (int i = 0; i < nodeList.size(); i++) {
            result += "" + nodeList.get(i);
            if (i < nodeList.size() - 1)
                result += ",";
        }
        result += "]:" + weight;  */
      String result="";
      if (srcNode != -1)
          nodeList.add(srcNode);
      for (int i = 0; i < nodeList.size(); i++) {
          result += "" + nodeList.get(i);
          if (i < nodeList.size() - 1)
              result += "-->";
      }
      result += "  最短路径为:"+weight; 
    	
    	
    	
      
    	System.out.println(result);
    	//System.out.println(endTime);
    	//System.out.println(startTime);
    }

    public void addWeight(int w) {
        if (weight == -1)
            weight = w;
        else
            weight += w;
    }

}
