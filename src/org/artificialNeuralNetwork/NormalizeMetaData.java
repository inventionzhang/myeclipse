package org.artificialNeuralNetwork;

public class NormalizeMetaData {
	private int linkID;//路段编号
	private final int linkSameDirection = 1;//出租车行驶方向与路段同向
	private double linkSameDirectionMax;//同向通行时间最大值
	private double linkSameDirectionMin;//同向通行时间最小值
	private int linkSameDirectionDataCount;//同向通行时间数据量
	private final int linkAntiDirection = -1;//出租车行驶方向与路段异向
	private double linkAntiDirectionMax;//异向通行时间最大值
	private double linkAntiDirectionMin;//异向通行时间最小值
	private int linkAntiDirectionDataCount;//异向通行时间数据量
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;
	}
	
	public int getLinkSameDirection(){
		return linkSameDirection;
	}
	
	public void setLinkSameDirectionMax(double linkSameDirectionMax){
		this.linkSameDirectionMax = linkSameDirectionMax;
	}
	
	public double getLinkSameDirectionMax(){
		return linkSameDirectionMax;
	}
	
	public void setLinkSameDirectionMin(double linkSameDirectionMin){
		this.linkSameDirectionMin = linkSameDirectionMin;
	}
	
	public double getLinkSameDirectionMin(){
		return linkSameDirectionMin;
	}
	
	public void setLinkSameDirectionDataCount(int linkSameDirectionDataCount){
		this.linkSameDirectionDataCount = linkSameDirectionDataCount;
	}
	
	public int getLinkSameDirectionDataCount(){
		return linkSameDirectionDataCount;
	}
	
	public int getLinkAntiDirection(){
		return linkAntiDirection;
	}
	
	public void setLinkAntiDirectionMax(double linkAntiDirectionMax){
		this.linkAntiDirectionMax = linkAntiDirectionMax;
	}
	
	public double getLinkAntiDirectionMax(){
		return linkAntiDirectionMax;
	}
	
	public void setLinkAntiDirectionMin(double linkAntiDirectionMin){
		this.linkAntiDirectionMin = linkAntiDirectionMin;
	}
	
	public double getLinkAntiDirectionMin(){
		return linkAntiDirectionMin;
	}
	
	public void setLinkAntiDirectionDataCount(int linkAntiDirectionDataCount){
		this.linkAntiDirectionDataCount = linkAntiDirectionDataCount;
	}
	
	public int getLinkAntiDirectionDataCount(){
		return linkAntiDirectionDataCount;
	}
}
