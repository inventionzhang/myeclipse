package stochasticTimeDependentNetwork;

public class BaseNode {

	private String name;
	private double L = -1, B = -1;//�ڵ�ľ�γ�����꣬��ʼֵΪ-1
	private int nodeID = -1;//�ڵ�ID����ʼֵΪ-1
    
	public String getName() {
        return name;
    }  
  
    public void setName(String name) {
        this.name = name;
    } 
    
    public void setNodeID(int nodeID){
    	this.nodeID = nodeID;
    }
    
    public int getNodeID(){
    	return nodeID;
    }
       
    public double getL(){
    	return L;
    }
    
    public void setL(double L){
    	this.L = L;
    }
    
    public void setB(double B){
    	this.B = B;
    }
    
    public double getB(){
    	return B;
    }  
}
