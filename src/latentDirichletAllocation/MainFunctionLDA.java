package latentDirichletAllocation;

import java.util.ArrayList;

import mapMatchingGPS.DatabaseFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.TaxiGPS;
import importDataToDatabase.ImportDataToMySQL;

public class MainFunctionLDA {
	
	public static void main(String[] args){
		(new MainFunctionLDA()).arrestPointProcess();
		
		System.out.print("提取完成!");
	}
		
	/**
	 * 停留点提取并地图匹配、获得停留点所在路段编号、名称等信息，写入数据库
	 */
	public void arrestPointProcess() {
		LDAAssistFunction.obtainRoadNetworkRange(MapMatchAlgorithm.instance().juncCollArrayList);
		LDAAssistFunction ldaAssistFunction = new LDAAssistFunction();
		ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();
		String startTimeStr = "2014-06-05 00:00:00";
		String endTimeStr = "2014-06-06 00:00:00";
//		String startTimeStr = "2013-01-08 00:00:00";
//		String endTimeStr = "2013-01-09 00:00:00";
		String insertTableName = "hl_copy";
		String tableName = "taxilist20140605";
		//String targetIDStr = "MMC8000GPSANDASYN051113-21640-00000000";
		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
		DatabaseFunction.obtainUniqueTaxiIDAccordTime(taxiIDArrayList, tableName);
		int taxiCount = taxiIDArrayList.size();
		for (int i = 0; i < taxiIDArrayList.size(); i++) {
			String targetIDStr = taxiIDArrayList.get(i);
			ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
			DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
			ArrayList<TaxiGPS> arrestGPSArrayList = new ArrayList<TaxiGPS>();
			ldaAssistFunction.obtainTaxiPassengerArrestGPSPoint(taxiGPSArrayList, arrestGPSArrayList);
			ArrayList<TaxiGPS> processArrestGPSArrayList = new ArrayList<TaxiGPS>();
			ldaAssistFunction.arrestGPSPointMapMatch(arrestGPSArrayList, processArrestGPSArrayList);
			importDataToMySQL.insertCorrectedTaxiDataToDatabase(processArrestGPSArrayList, insertTableName, i, taxiCount);
			//importDataToMySQL.insertCorrectedTaxiDataToDatabase(processArrestGPSArrayList, insertTableName, 1, 1);
			
		}
		System.out.print("");			
	}
	
	/**
	 * 停留点提取并地图匹配、获得停留点所在路段编号、名称等信息，写入数据库
	 * 程序终止运行时，某些出租车ID的停留点信息没有插入table中，对于没有插入的taxi用此方法
	 */
	public void arrestPointProcess2() {
		LDAAssistFunction.obtainRoadNetworkRange(MapMatchAlgorithm.instance().juncCollArrayList);
		LDAAssistFunction ldaAssistFunction = new LDAAssistFunction();
		ImportDataToMySQL importDataToMySQL = new ImportDataToMySQL();
		String startTimeStr = "2014-06-05 00:00:00";
		String endTimeStr = "2014-06-06 00:00:00";
//		String startTimeStr = "2013-01-08 00:00:00";
//		String endTimeStr = "2013-01-09 00:00:00";
		String insertTableName = "hl_copy";
//		String targetIDStr = "MMC8000GPSANDASYN051113-21640-00000000";
		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
		DatabaseFunction.obtainUniqueTaxiIDAccordTime(taxiIDArrayList, startTimeStr, endTimeStr);
		ArrayList<String> tableHLTaxiIDArrayList = new ArrayList<String>();
		DatabaseFunction.obtainUniqueTaxiIDAccordTime(tableHLTaxiIDArrayList, startTimeStr, endTimeStr,insertTableName);
		int taxiCount = taxiIDArrayList.size();
		for (int i = 0; i < taxiIDArrayList.size(); i++) {
			String targetIDStr = taxiIDArrayList.get(i);
			if (!ldaAssistFunction.isIDInArraylist(targetIDStr, tableHLTaxiIDArrayList)) {
				ArrayList<TaxiGPS> taxiGPSArrayList = new ArrayList<TaxiGPS>();
				DatabaseFunction.obtainGPSDataFromDatabase(taxiGPSArrayList, targetIDStr, startTimeStr, endTimeStr);
				ArrayList<TaxiGPS> arrestGPSArrayList = new ArrayList<TaxiGPS>();
				ldaAssistFunction.obtainTaxiPassengerArrestGPSPoint(taxiGPSArrayList, arrestGPSArrayList);
				ArrayList<TaxiGPS> processArrestGPSArrayList = new ArrayList<TaxiGPS>();
				ldaAssistFunction.arrestGPSPointMapMatch(arrestGPSArrayList, processArrestGPSArrayList);
				importDataToMySQL.insertCorrectedTaxiDataToDatabase(processArrestGPSArrayList, insertTableName, i, taxiCount);
			}
		}
		System.out.print("");			
	}
	
	
	
}
