package mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** taxiSortMap����ID���й���ĳ��⳵��Ϣ
 	 * threadID���߳�ID
	 * startTimeStr����ʼʱ��
	 * endTimeStr����ֹʱ��
	 * targetLinkID:Ŀ��·��ID
	 * targetEdge��Ŀ��·��
	 * sampleThreshold:������ֵ��Ĭ��60s�������ڽ��й켣�ʷ�
	 * expandTime����չʱ�䣨Ĭ��60s��
	 * taxiTravelTimeArrayList:Ŀ��·�γ��⳵��ͨ����Ϣ
	 * allTaxiTravelTimeArrayList:Ŀ��·�����г��⳵��ͨ����Ϣ*/
public class ThreadTravelTimeStatistics extends Thread {
	private int threadID;
	private Map<String, ArrayList<TaxiGPS>> taxiSortMap;
	private String startTimeStr;
	private String endTimeStr;
	private int targetLinkID;
	private MapMatchEdge targetEdge;
	private int sampleThreshold;
	private int expandTime;
//	private ArrayList<TaxiTravelTime> taxiTravelTimeArrayList;
	
	public void setThreadID(int threadID){
		this.threadID = threadID;
	}
	
	public int getThreadID(){
		return threadID;
	}
	
	public void setTaxiSortMap(Map<String, ArrayList<TaxiGPS>> taxiSortMap){
		this.taxiSortMap = taxiSortMap;
	}
	
	public Map<String, ArrayList<TaxiGPS>> getTaxiSortMap(){
		return taxiSortMap;
	}
	
	public void setStartTimeStr(String startTimeStr){
		this.startTimeStr = startTimeStr;
	}
	
	public String getStartTimeStr(){
		return startTimeStr;
	}	
	
	public void setEndTimeStr(String endTimeStr){
		this.endTimeStr = endTimeStr;
	}
	
	public String getEndTimeStr(){
		return endTimeStr;
	}
	
	public void setTargetLinkID(int targetLinkID){
		this.targetLinkID = targetLinkID;
	}
	
	public int getTargetLinkID(){
		return targetLinkID;
	}
	
	public void setTargetEdge(MapMatchEdge targetEdge){
		this.targetEdge = targetEdge;
	}
	
	public MapMatchEdge getTargetEdge(){
		return targetEdge;
	}
	
	public void setSampleThreshold(int sampleThreshold){
		this.sampleThreshold = sampleThreshold;
	}
	
	public int getSampleThreshold(){
		return sampleThreshold;
	}
	
	public void setExpandTime(int expandTime){
		this.expandTime = expandTime;
	}
	
	public int getExpandTime(){
		return expandTime;
	}
	
//	public void setTaxiTravelTimeArrayList(ArrayList<TaxiTravelTime> taxiTravelTimeArrayList){
//		this.taxiTravelTimeArrayList = taxiTravelTimeArrayList;
//	}
//	
//	public ArrayList<TaxiTravelTime>  getTaxiTravelTimeArrayList(){
//		return taxiTravelTimeArrayList;
//	}
	
	public void run()
	{
		obtainTaxiTravelTime(threadID, taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
    			sampleThreshold, expandTime);
	}
	
	public void obtainTaxiTravelTime(int threadID, Map<String, ArrayList<TaxiGPS>> taxiSortMap, String startTimeStr, String endTimeStr, int targetLinkID,
			MapMatchEdge targetEdge, int sampleThreshold, int expandTime){
		try {
			System.out.print("�̣߳�" + threadID + "����ʼ����" + '\n');
			ArrayList<TaxiTravelTime> taxiTravelTimeArrayList = MapMatchAlgorithm.allTaxiTravelTimeMap.get(threadID);
			AssistFunction assistFunction = new AssistFunction();
			assistFunction.obtainAllTaxiTravelTime(taxiSortMap, startTimeStr, endTimeStr, targetLinkID, targetEdge, 
	    			sampleThreshold, expandTime, taxiTravelTimeArrayList);	
			System.out.print("�̣߳�" + threadID + "�����н���" + '\n');
//			this.stop();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
}
