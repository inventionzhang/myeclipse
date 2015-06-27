package mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

public class ThreadPoolObtainEligibleGPSPoint implements Runnable, Serializable {

	private int threadID;
	private MapMatchEdge targetEdge;
	private MapMatchNode centerNode;
	private ArrayList<TaxiGPS> taxiGPSArrayList;//�����GPS����
	private ArrayList<TaxiGPS> eligibleGPSArrayList;//��������������GPS��
	private double radius;
	
	public void setThreadID(int threadID){
		this.threadID = threadID;
	}
	
	public int getThreadID(){
		return threadID;
	}
	
	public void setTargetEdge(MapMatchEdge targetEdge){
		this.targetEdge = targetEdge;
	}
	
	public MapMatchEdge getTargetEdge(){
		return targetEdge;
	}
	
	public void setTaxiGPSArrayList(ArrayList<TaxiGPS> taxiGPSArrayList){
		this.taxiGPSArrayList = taxiGPSArrayList;
	}
	
	public ArrayList<TaxiGPS> getTaxiGPSArrayList(){
		return taxiGPSArrayList;
	}
	
	public void setEligibleGPSArrayList(ArrayList<TaxiGPS> eligibleGPSArrayList){
		this.eligibleGPSArrayList = eligibleGPSArrayList;
	}
	
	public ArrayList<TaxiGPS> getEligibleGPSArrayList(){
		return eligibleGPSArrayList;
	}
	
		
	public void run()
	{
		eligibleTaxiGPSPointToArc(threadID, targetEdge, taxiGPSArrayList, eligibleGPSArrayList);
	}
	
	public void eligibleTaxiGPSPointToArc(int threadID, MapMatchEdge targetEdge, ArrayList<TaxiGPS> taxiGPSArrayList,
			ArrayList<TaxiGPS> eligibleGPSArrayList){
		try {
			System.out.print("�̣߳�" + threadID + "����ʼ����" + '\n');
			int taxiGPSIndexCount = taxiGPSArrayList.size();
			for (int i = 0; i < taxiGPSArrayList.size(); i++) {
				System.out.print("�̣߳�" + threadID + "��TaxiGPS��" + i + "����:" + taxiGPSIndexCount + '\n');
				TaxiGPS taxiGPS = taxiGPSArrayList.get(i);
				MapMatchNode tNode = new MapMatchNode();
				tNode.setX(taxiGPS.getLongitude());
				tNode.setY(taxiGPS.getLatitude());
				if (PubClass.distancePointToArc(targetEdge, tNode) <= 2 * PubParameter.radius) {
					eligibleGPSArrayList.add(taxiGPS);
				}
			}
			System.out.print("�̣߳�" + threadID + "�����н���" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
	
}
