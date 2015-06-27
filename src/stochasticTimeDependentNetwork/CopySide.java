package stochasticTimeDependentNetwork;

public class CopySide {
	private int preNodeID; //ǰ��ڵ�
    private int nodeID; //����ڵ�
    private int weight; //Ȩ��

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
