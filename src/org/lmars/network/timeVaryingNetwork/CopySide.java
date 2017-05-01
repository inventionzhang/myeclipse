package org.lmars.network.timeVaryingNetwork;

public class CopySide {
	private int preNodeID; //前向节点
    private int nodeID; //后向节点
    private int weight; //权重

    public CopySide(int preNodeID, int nodeID, int weight) {
        this.preNodeID = preNodeID;
        this.nodeID = nodeID;
        this.weight = weight;
    }

    public int getPreNode() {
        return preNodeID;
    }

    public void setPreNode(int preNodeID) {
        this.preNodeID = preNodeID;
    }

    public int getNode() {
        return nodeID;
    }

    public void setNode(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
