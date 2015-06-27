package stochasticTimeDependentNetwork;

import utilityPackage.PubClass;

public class DijkstraEdge {
	/*·�εķ���Ϊǰ��ڵ㵽����ڵ�*/
	private DijkstraNode preNode;//ǰ��ڵ�,-1��ʾû��ǰ��ڵ�
    private DijkstraNode nextNode;//����ڵ�
    private double weight;//Ȩ��
    private double timeWeight;//ʱ������ʱ��Ȩ��
    private boolean parentProperty = false;//ͼ��preNode�Ƿ�ΪnextNode���ڵ�
    
    public DijkstraEdge(DijkstraNode preNode, DijkstraNode nextNode, double weight) {
        this.preNode = preNode;
        this.nextNode = nextNode;
        this.weight = weight;
    }

    /*typeΪ1ʱ��Ϊʱ�����磻typeΪ0ʱ��Ϊ��ͨ����*/
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
