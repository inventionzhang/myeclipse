package service;

import utilityPackage.JDBCConnectionCreator;
import implement.RoadNetworkAnalysisImpl;
import implement.RoadNetworkAnalysisImpl;
import entity.DataBaseType;
import entity.PropertiesUtilJAR;


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