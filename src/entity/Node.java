package entity;

import java.io.Serializable;
import java.util.ArrayList;  
import com.esri.arcgis.geometry.IPolyline;
  
/* ��ʾһ���ڵ��Լ�������ڵ����������нڵ� */ 
public class Node implements Serializable
{  
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5342124530667283644L;
    /*�ڵ��x��y����
     *x��Ϊ����L
     *y��Ϊγ��B,�������ģ����ø��� */
	public String name = null;
	public double x,y=0; 
	public int EID = 0; //element ID in network  
	public int EdgeID = 0;//���ڱ�ID
	public double g = 0;//��ǰ�㵽�����ƶ��ķ�
	public double h = 0;//��ǰ�㵽�յ�Ĺ����ƶ��ķѣ������յ������룩
	public double f = 0;//f=g+h
	private Node parentNode;//����ڵ�
	public ArrayList<Node> relationNodes = new ArrayList<Node>();// ������ڵ�������Ľڵ㼯��
	public ArrayList<Edge> relationEdges=new ArrayList<Edge>(); //������ڵ�ֱ�������ı߼���
    
    public Node(){
	    
	}
    
    public Node(Node parentNode){
	    this.parentNode=parentNode;
	}
    
    public Node getParentNode() {
	    return parentNode;
	}
    
	public void setParentNode(Node parentNode) {
	    this.parentNode = parentNode;
	}
    
    public void setEID(int eid){
    	this.EID=eid;
    }
    
    public int getEID(){
    	return EID;
    }
    
    public void setEdgeID(int eid){
    	this.EdgeID=eid;
    }
    
    public int getEdgeID(){
    	return EdgeID;
    }
       
    public double getX(){
    	return x;
    }
    
    public void setX(double x){
    	this.x = x;
    }
    
    public void setY(double y){
    	this.y =y;
    }
    
    public double getY(){
    	return y;
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
    
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public ArrayList<Node> getRelationNodes() {  
        return relationNodes;  
    }  
  
    public void setRelationNodes(ArrayList<Node> relationNodes) {  
        this.relationNodes = relationNodes;  
    }  
    
    public ArrayList<Edge> getRelationEdge(){
    	return relationEdges;
    }
    
    public void setRelationEdge(ArrayList<Edge> relationEdge) {  
        this.relationEdges = relationEdge;  
    } 
}  