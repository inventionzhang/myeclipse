package stochasticTimeDependentNetwork;

import java.util.ArrayList;
import entity.Edge;

public class DijkstraNode extends BaseNode{

	private String shortestTimeStr;//����ǰ�ڵ����·��ʱ���ʱ��
	private double shortestTime;//����ǰ�ڵ����·����ʱ��
	private double g = 0;//��ǰ�㵽�����ƶ��ķ�
	private double h = 0;//��ǰ�㵽�յ�Ĺ����ƶ��ķѣ������յ������룩
	private double f = 0;//f=g+h
	private DijkstraNode parentNode;//���ڵ�
	private ArrayList<DijkstraNode> relationNodes = new ArrayList<DijkstraNode>();// ������ڵ�������Ľڵ㼯��
	private ArrayList<Edge> relationEdges = new ArrayList<Edge>(); //������ڵ�ֱ�������ı߼���
	
	public String getShortestTimeStr() {
	    return shortestTimeStr;
	}
	
	public void setShortestTimeStr(String shortestTimeStr) {
	    this.shortestTimeStr = shortestTimeStr;
	}
		
	public double getG() {
	    return g;
	}
	
	public void setG(double g) {
	    this.g = g;
	}
	
	public double getH() {
	    return h;
	}
	public void setH(double h) {
	    this.h = h;
	}
	public double getF() {
	    return f;
	}
	public void setF(double f) {
	    this.f = f;
	}
    
    public BaseNode getParentNode() {
	    return parentNode;
	}
    
	public void setParentNode(DijkstraNode parentNode) {
	    this.parentNode = parentNode;
	}
	
	public ArrayList<DijkstraNode> getRelationNodes() {  
	        return relationNodes;  
	}  
	  
    public void setRelationNodes(ArrayList<DijkstraNode> relationNodes) {  
        this.relationNodes = relationNodes;  
    }  
    
    public ArrayList<Edge> getRelationEdge(){
    	return relationEdges;
    }
    
    public void setRelationEdge(ArrayList<Edge> relationEdge) {  
        this.relationEdges = relationEdge;  
    }
}
