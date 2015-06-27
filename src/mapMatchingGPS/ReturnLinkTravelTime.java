package mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class ReturnLinkTravelTime implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6777168833507244185L;
	private int linkID;//Ŀ��·��ID
	private String taxiID;//���⳵ID
	private String startTravelTime;//���⳵��ʼ����ĳ·�ε�ʱ��
	private double travelTime;//·��ͨ��ʱ��
	private double taxiMeanSpeed;//���⳵ƽ���ٶ�
	private int taxiTravelDirection;//���⳵ͨ�з�����·�η����ϵ
	private int taxiEnterNodeID;//���⳵ʻ��·�ζ˵�ID
	private int taxiExitNodeID;//���⳵ʻ��·�ζ˵�ID	
	private ArrayList<MapMatchNode> taxiLinkTravelArrayList = new ArrayList<MapMatchNode>();//��Ӧͨ��ʱ���GPS
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;
	}
	
	public void setTaxiID(String ttaxiID){
		this.taxiID = ttaxiID;
	}
	
	public String getTaxiID(){
		return taxiID;
	}
	
	public void setStartTravelTime(String startTravelTime){
		this.startTravelTime = startTravelTime;
	}
	
	public String getStartTravelTime(){
		return startTravelTime;
	}
	
	public void setTravelTime(double ttravelTime){
		this.travelTime = ttravelTime;
	}
	
	public double getTravelTime(){
		return travelTime;
	}
	
	public void setTaxiMeanSpeed(double taxiMeanSpeed){
		this.taxiMeanSpeed = taxiMeanSpeed;
	}
	
	public double getTaxiMeanSpeed(){
		return taxiMeanSpeed;
	}
	
	public void setTaxiTravelDirection(int taxiTravelDirection){
		this.taxiTravelDirection = taxiTravelDirection;
	}
	
	public int getTaxiTravelDirection(){
		return taxiTravelDirection;
	}
	
	public void setTaxiEnterNodeID(int taxiEnterNodeID) {
		this.taxiEnterNodeID = taxiEnterNodeID;
	}
	
	public int getTaxiEnterNodeID() {
		return taxiEnterNodeID;
	}
	
	public void setTaxiExitNodeID(int taxiExitNodeID) {
		this.taxiExitNodeID = taxiExitNodeID;
	}
	
	public int getTaxiExitNodeID() {
		return taxiExitNodeID;
	}
	
	public void setTaxiLinkTravelArrayList(ArrayList<MapMatchNode> ttaxiLinkTravelMap){
		this.taxiLinkTravelArrayList = ttaxiLinkTravelMap;
	}
	
	public ArrayList<MapMatchNode> getTaxiLinkTravelArrayList(){
		return taxiLinkTravelArrayList;
	}
}
