package mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class TaxiTravelTime {
	private int linkID;//Ŀ��·��ID
	private String taxiID;//���⳵ID
	private ArrayList<String> startTravelTimeArraylist = new ArrayList<String>();//���⳵��ʼ����·�ε�ʱ��,���⳵���ܶ�ν���ͬһ·��
	//��ʱ��Ϊ������ĳʱ�̳��⳵ͨ��·�ε�ͨ��ʱ��
	private Map<String, Double> travelTimeMap = new HashMap<String, Double>();
	//��ʱ��Ϊ������ĳʱ�̳��⳵ͨ��·�ε�GPS��
	private Map<String, ArrayList<MapMatchNode>> GPSTravelMap = new HashMap<String, ArrayList<MapMatchNode>>();
	//��ʱ��Ϊ������ĳʱ�̳��⳵��·����ƽ���ٶ�
	private Map<String, Double> taxiMeanSpeedMap = new HashMap<String, Double>();
	//��ʱ��Ϊ������ĳһʱ���ͨ��·��ʱ�����⳵��ʻ������·�εķ����ϵ
	private Map<String, Integer> taxiTravelDirectionMap = new HashMap<String, Integer>();
	//��ʱ��Ϊ������ĳһʱ���ͨ��·��ʱ�����⳵����·�ζ˵�ID��ʻ��·�ζ˵�ID,0��Ӧ����˵�������1��Ӧʻ��·�ζ˵�����
	private Map<String, int[]> taxiEntranceExitNodeIDMap = new HashMap<String, int[]>();
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getLinkID(){
		return linkID;
	}
	
	public void setTaxiID(String taxiID){
		this.taxiID = taxiID;
	}
	
	public String getTaxiID(){
		return taxiID;
	}
	
	public void setStartTravelTimeArraylist(ArrayList<String> startTravelTimeArraylist){
		this.startTravelTimeArraylist = startTravelTimeArraylist;
	}
	
	public ArrayList<String> getStartTravelTimeArraylist(){
		return startTravelTimeArraylist;
	}
	
	public void setTravelTimeMap(Map<String, Double> travelTimeMap){
		this.travelTimeMap = travelTimeMap;
	}
	
	public Map<String, Double> getTravelTimeMap(){
		return travelTimeMap;
	}
	
	public void setGPSTravelMap(Map<String, ArrayList<MapMatchNode>> GPSTravelMap){
		this.GPSTravelMap = GPSTravelMap;
	}
	
	public Map<String, ArrayList<MapMatchNode>> getGPSTravelMap(){
		return GPSTravelMap;
	}
	
	public void setTaxiMeanSpeedMap(Map<String, Double> taxiMeanSpeedMap){
		this.taxiMeanSpeedMap = taxiMeanSpeedMap;
	}
	
	public Map<String, Double> getTaxiMeanSpeedMap(){
		return taxiMeanSpeedMap;
	}
	
	public void setTaxiTravelDirectionMap(Map<String, Integer> taxiTravelDirectionMap){
		this.taxiTravelDirectionMap = taxiTravelDirectionMap;
	}
	
	public Map<String, Integer> getTaxiTravelDirectionMap(){
		return taxiTravelDirectionMap;
	}
	
	public void setTaxiEntranceExitNodeIDMap(Map<String, int[]> taxiEntranceExitNodeIDMap){
		this.taxiEntranceExitNodeIDMap = taxiEntranceExitNodeIDMap;
	}
	
	public Map<String, int[]> getTaxiEntranceExitNodeIDMap(){
		return taxiEntranceExitNodeIDMap;
	}
	
	
}
