package latentDirichletAllocation;

import java.util.ArrayList;

import mapMatchingGPS.DatabaseFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import mapMatchingGPS.TaxiGPS;
import importDataToDatabase.ImportDataToMySQL;

public class MainFunctionLDA {
	
	public static void main(String[] args){
		(new MainFunctionLDA()).arrestPointProcess();
		
		System.out.print("��ȡ���!");
	}
		
	/**
	 * ͣ������ȡ����ͼƥ�䡢���ͣ��������·�α�š����Ƶ���Ϣ��д�����ݿ�
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
	 * ͣ������ȡ����ͼƥ�䡢���ͣ��������·�α�š����Ƶ���Ϣ��д�����ݿ�
	 * ������ֹ����ʱ��ĳЩ���⳵ID��ͣ������Ϣû�в���table�У�����û�в����taxi�ô˷���
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
