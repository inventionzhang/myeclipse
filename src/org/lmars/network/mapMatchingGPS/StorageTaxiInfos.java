package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class StorageTaxiInfos implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2168688835870182482L;
	public Map<String, ArrayList<TaxiGPS>> taxiInfosMap = null;//出租车ID已经对应的信息
	
}
