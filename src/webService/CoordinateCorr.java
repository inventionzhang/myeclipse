package webService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.jndi.url.corbaname.corbanameURLContextFactory;

import mapMatchingGPS.*;

public class CoordinateCorr {

	public CorrectedNode[] obtainTaxiGPSDataAndCorrection(String targetIDStr, String startTimeStr, String endTimeStr){
		boolean isOK = false;
		String[] paraArray = new String[3];
		isOK = MapMatchAlgorithm.parameterProc(targetIDStr, startTimeStr, endTimeStr, paraArray);
		targetIDStr = paraArray[0];
		startTimeStr = paraArray[1];
		endTimeStr = paraArray[2];
    	CorrectedNode[] allCorrectedGPS;
    	//���������ȷ��ִ�����´��룬���򷵻ؿ�ֵ
    	try {
    		if (isOK) {
    			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
        		DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);       		
        		if (taxiGPSArrayList.size() != 0) {
        			TaxiGPS [] taxiGPSArray = new TaxiGPS[taxiGPSArrayList.size()];
            		for (int i = 0; i < taxiGPSArrayList.size(); i++) {
            			taxiGPSArray[i] = taxiGPSArrayList.get(i);
            		}
            		allCorrectedGPS = obtainGPSCorrection(taxiGPSArray);
    			}
        		else {
        			allCorrectedGPS = null;
    			}
			}
    		else {
    			allCorrectedGPS = null;
			}
    		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			allCorrectedGPS = null;
		}		
		return allCorrectedGPS;
	}
	


	/**
	 * GPS���������GPS���������·��
	 * @param taxiGPSArray ԭGPS������
	 * @return ԭGPS�������Լ��������GPS������
	 */	
	public CorrectedNode[] obtainGPSCorrection(TaxiGPS[] taxiGPSArray){
		CorrectedNode[] allCorrectedNodesArray;		
		try {
			if (taxiGPSArray != null && taxiGPSArray.length > 0) {	
				Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				for (int i = 0; i < taxiGPSArray.length; i++) {
					taxiGPSArrayList.add(taxiGPSArray[i]);
				}	
				taxiTrackMap.put(1, taxiGPSArrayList);		
				double startObtainPathTime = System.nanoTime();
				ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>();//������GPS��
				ArrayList<Integer[]> pathEIDArrayList = new ArrayList<Integer[]>();
				ArrayList<TaxiGPS> correctedOriginalTaxiTrackArrayList = new ArrayList<TaxiGPS>();
				MapMatchAlgorithm.coordinateCorr(taxiTrackMap, pathEIDArrayList, correctedOriginalTaxiTrackArrayList, GPSCorrectArrayList);
				allCorrectedNodesArray = new CorrectedNode[GPSCorrectArrayList.size()];
				for (int i = 0; i < GPSCorrectArrayList.size(); i++) {
					allCorrectedNodesArray[i] = GPSCorrectArrayList.get(i);
				}		
				double endObtainPathTime = System.nanoTime();	
				double obtainPathTime = (endObtainPathTime - startObtainPathTime)/Math.pow(10, 9);
				System.out.print("���GPS��������ʱ�䣺" + obtainPathTime + "s" + "\n");
			}
			else {
				allCorrectedNodesArray = null;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
			allCorrectedNodesArray = null;
		}		
		return allCorrectedNodesArray;
	}
}
