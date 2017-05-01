package org.lmars.network.sparseData;

import java.util.ArrayList;

import org.lmars.network.mapMatchingGPS.MapMatchEdge;


public class PubClassSparseData {

	
	/**
	 * 返回目标路段信息
	 * @param targetLinkID	目标路段ID
	 * @param polylineCollArrayList	路段集合
	 * @return
	 */
	public static MapMatchEdge obtainTargetLinkEdge(int targetLinkID, ArrayList<MapMatchEdge> polylineCollArrayList) {
		MapMatchEdge targetEdge = new MapMatchEdge();
		try {
			for (int i = 0; i < polylineCollArrayList.size(); i++) {
				targetEdge = polylineCollArrayList.get(i);
				if (targetEdge.getEdgeID() == targetLinkID) {
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return targetEdge;
	}
	
	
	
		

}
