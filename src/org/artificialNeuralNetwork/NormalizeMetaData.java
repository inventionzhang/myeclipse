package org.artificialNeuralNetwork;

public class NormalizeMetaData {
	private int linkID;//·�α��
	private final int linkSameDirection = 1;//���⳵��ʻ������·��ͬ��
	private double linkSameDirectionMax;//ͬ��ͨ��ʱ�����ֵ
	private double linkSameDirectionMin;//ͬ��ͨ��ʱ����Сֵ
	private int linkSameDirectionDataCount;//ͬ��ͨ��ʱ��������
	private final int linkAntiDirection = -1;//���⳵��ʻ������·������
	private double linkAntiDirectionMax;//����ͨ��ʱ�����ֵ
	private double linkAntiDirectionMin;//����ͨ��ʱ����Сֵ
	private int linkAntiDirectionDataCount;//����ͨ��ʱ��������
	
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
