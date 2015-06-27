package entity;

import java.io.Serializable;

/*polyline÷–fromPoint“‘º∞toPoint*/
public class roadName implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3883691748052608467L;
	public int roadID=0;
	public String roadName=null;
	public retuNode froPoint=new retuNode();
	public retuNode toPoint=new retuNode();
	
	public void setRoadID(int readRoadID){
		this.roadID=readRoadID;
	}	
	public int getRoadID(){
		return roadID;
	}
	
	public void setRoadName(String name)
	{
		this.roadName=name;		
	}	
	public String getRoadName()
	{
		return roadName;		
	}
	
	public void setFroPoint(retuNode node)
	{
		this.froPoint=node;		
	}	
	public retuNode getFroPoint()
	{
		return froPoint;		
	}
	
	public void setToPoint(retuNode node)
	{
		this.toPoint=node;		
	}	
	public retuNode getToPoint()
	{
		return toPoint;		
	}
	
}
