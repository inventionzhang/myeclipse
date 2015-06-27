package mapMatchingGPS;

import java.io.Serializable;

public class CorrectedNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2737024825039730201L;
	//ԭ���㾭γ���������㾭γ��
	private double originLong;
	private double originLati;
	private double correctLong;
	private double correctLati;
	private String localTime;//������ʱ��
	private int targetEdgeID = -1;//����������Ŀ��·��ID�����Ϊ-1��ʾû�о���
	
	public double getOriginLongitude(){
		return originLong;
	}
	
	public void setOriginLongitude( double tLongitude){
		this.originLong = tLongitude;
	}
	
	public double getOriginLatitude(){
		return originLati;
	}
	
	public void setOriginLatitude(double tLatitude){
		this.originLati = tLatitude;
	}
	
	public double getCorrectLongitude(){
		return correctLong;
	}
	
	public void setCorrectLongitude( double tLongitude){
		this.correctLong = tLongitude;
	}
	
	public double getCorrectLatitude(){
		return correctLati;
	}
	
	public void setCorrectLatitude(double tLatitude){
		this.correctLati = tLatitude;
	}
	
	public String getLocalTime(){
		return localTime;
	}
	
	public void setTargetEdgeID(int targetEdgeID){
		this.targetEdgeID = targetEdgeID;
	}
	
	public int getTargetEdgeID(){
		return targetEdgeID;
	}
	
	public void setLocalTime(String localTime){
		this.localTime = localTime;
	}
}
