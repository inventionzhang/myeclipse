package mapMatchingGPS;

import java.io.Serializable;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ThreadPoolTaxiGPSSimulativeGenerator implements Runnable, Serializable {  

	/*taxiTrackMap:待纠正点
	 * GPSCorrectArrayList：纠正后的点*/
	private ArrayList<CorrectedNode> GPSCorrectArrayList;
	private Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap;
	public void setGPSCorrectArrayList(ArrayList<CorrectedNode> GPSCorrectArrayList){
		this.GPSCorrectArrayList = GPSCorrectArrayList;
	}
	
	public ArrayList<CorrectedNode> getGPSCorrectArrayList(){
		return GPSCorrectArrayList;
	}
	
	public void setTaxiTrackMap(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap){
		this.taxiTrackMap = taxiTrackMap;
	}
	
	public Map<Integer, ArrayList<TaxiGPS>> getTaxiTrackMap(){
		return taxiTrackMap;
	}	
	
    public void run() {            
    	taxiGPSSimulativeGenerator(taxiTrackMap, GPSCorrectArrayList);
    }  
  
    public void taxiGPSSimulativeGenerator(Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap, 
    		ArrayList<CorrectedNode> GPSCorrectArrayList) {  
    	try {
    		ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
    		ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
    		MapMatchAlgorithm.coordinateCorr(taxiTrackMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList,GPSCorrectArrayList);	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}   	
    }  
}  