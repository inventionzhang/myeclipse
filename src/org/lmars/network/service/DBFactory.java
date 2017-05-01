package org.lmars.network.service;

import org.lmars.network.entity.DataBaseType;
import org.lmars.network.implement.RoadNetworkAnalysisImpl;
import org.lmars.network.util.JDBCConnectionCreator;
import org.lmars.network.util.PropertiesUtilJAR;



public class DBFactory {

	public static RoadNetworkAnalysisImpl roadNetworkAnaly(){
		DataBaseType type = JDBCConnectionCreator.getInstance().getDataBaseType(PropertiesUtilJAR.getProperties("resourceName"));
		if(type == DataBaseType.ORACLE){
			return new RoadNetworkAnalysisImpl();
		}else{
			return null;
		}
	}
	
}