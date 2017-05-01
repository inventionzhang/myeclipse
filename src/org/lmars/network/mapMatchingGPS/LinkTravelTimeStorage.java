package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**/
public class LinkTravelTimeStorage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971451737029111562L;
//	public Map<String[], ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = null;//以小时为划分单位对路段出租车通行信息进行划分
	public Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = null;
	
}
