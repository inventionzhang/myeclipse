package org.artificialNeuralNetwork;

public class ANNInputVector {
	private int linkID;//·�α��
	private int direction;//GPS��ʻ������·�η������
	private int hour;//ʱ
	private int preHalfHour;//�Ƿ�ǰ��Сʱ:1��ʾǰ��Сʱ
	private int workDay;//�Ƿ����գ�1��ʾ������
	private double travelTime;//·��ͨ��ʱ��
	
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
