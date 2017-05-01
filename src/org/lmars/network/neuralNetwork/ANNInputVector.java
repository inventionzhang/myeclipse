package org.lmars.network.neuralNetwork;

public class ANNInputVector {
	private int linkID;//路段编号
	private int direction;//GPS行驶方向与路段方向关心
	private int hour;//时
	private int preHalfHour;//是否前半小时:1表示前半小时
	private int workDay;//是否工作日：1表示工作日
	private double travelTime;//路段通行时间
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;	
	}
	
	public void setDirection(int direction){
		this.direction = direction;
	}
	
	public int getDirection(){
		return direction;	
	}
	
	public void setHour(int hour){
		this.hour = hour;
	}
	
	public int getHour(){
		return hour;		
	}
	
	public void setPreHalfHour(int preHalfHour){
		this.preHalfHour = preHalfHour;
	}
	
	public int getPreHalfHour(){
		return preHalfHour;		
	}
	
	public void setWorkDay(int workDay){
		this.workDay = workDay;
	}
	
	public int getWorkDay(){
		return workDay;		
	}
	
	public void setTravelTime(double travelTime){
		this.travelTime = travelTime;
	}
	
	public double getTravelTime(){
		return travelTime;	
	}
}
