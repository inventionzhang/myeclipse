package stochasticTimeDependentNetwork;

import utilityPackage.PubClass;

public class DijkstraEdge {
	/*路段的方向为前向节点到后向节点*/
	private DijkstraNode preNode;//前向节点,-1表示没有前向节点
    private DijkstraNode nextNode;//后向节点
    private double weight;//权重
    private double timeWeight;//时变网络时间权重
    private boolean parentProperty = false;//图中preNode是否为nextNode父节点
    
    public DijkstraEdge(DijkstraNode preNode, DijkstraNode nextNode, double weight) {
        this.preNode = preNode;
        this.nextNode = nextNode;
        this.weight = weight;
    }

    /*type为1时，为时变网络；type为0时，为普通网络*/
    public DijkstraEdge(DijkstraNode preNode, DijkstraNode nextNode, double weight, int type) {
        this.preNode = preNode;
        this.nextNode = nextNode;
        if (type == 0) {
        	this.weight = weight;
		}
        if (type == 1) {
			this.timeWeight = weight;
		}      
    }
    
    public DijkstraEdge(DijkstraNode preNode, DijkstraNode nextNode, Boolean parentProperty) {
        this.preNode = preNode;
        this.nextNode = nextNode;
        this.parentProperty = parentProperty;
    }
    
    public DijkstraNode getPreNode() {
        return preNode;
    }

    public void setPreNode(DijkstraNode preNode) {
        this.preNode = preNode;
    }

    public DijkstraNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(DijkstraNode nextNode) {
        this.nextNode = nextNode;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public double getTimeWeight() {
        return timeWeight;
    }

    public void setTimeWeight(double timeWeight) {
        this.timeWeight = timeWeight;
    }
    
    public boolean getParentProperty(){
    	return parentProperty;
    }
    
    public void setConnectedProperty(boolean parentProperty){
    	this.parentProperty = parentProperty;
    }
    
    public double getTimeWeight(String timeStr){
    	String startTimeStr;
    	String endTimeStr;
    	try {
    		startTimeStr = "2013-01-01 00:00:00";
    		endTimeStr = "2013-01-01 00:30:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 10 * 60;
			}
			startTimeStr = "2013-01-01 00:30:00";
    		endTimeStr = "2013-01-01 01:00:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 20 * 60;
			}
			startTimeStr = "2013-01-01 01:00:00";
    		endTimeStr = "2013-01-01 01:30:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 20 * 60;
			}
			startTimeStr = "2013-01-01 01:30:00";
    		endTimeStr = "2013-01-01 02:00:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 15 * 60;
			}
			startTimeStr = "2013-01-01 02:00:00";
    		endTimeStr = "2013-01-01 02:30:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 15 * 60;
			}
			startTimeStr = "2013-01-01 02:30:00";
    		endTimeStr = "2013-01-01 03:00:00";
			if (PubClass.isTimeBetweenStartEndTime(startTimeStr, endTimeStr, timeStr)) {
				this.timeWeight = 15 * 60;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
		}
    	return timeWeight;
    }
    
    
    
    
    
    
}
