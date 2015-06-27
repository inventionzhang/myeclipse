package mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

public class Simulator {

	/*GPS����ģ�ⷢ����
	 * ÿtaxiGPSSimulativeConst��GPS������������
	 * allTaxiInfosMap����������GPS��Ϣ,��stringIDΪ��ֵ
	 * allGPSCorrectArrayList��������GPS�����
	 * ����˼·��
	 * 1.�켣�ʷ֣�ÿ�����⳵���ݷֳɶ����켣��
	 * 2.�ж��Ƿ�����������ʷֺ�켣��ÿ�ξ�����ĸ�����ֵ��Ĭ��10���㣩���бȽ�
	 * 	���켣�����С����ֵ����һ���Ծ���
	 * 	���켣�����������ֵ����ִν��о���
	 * 3.���ݷ���ʱ��ѡ�񣺸��ݾ�������ʱ���Լ��ʷֹ켣֮���ʱ���ȷ������ʱ��*/
	public static void taxiGPSSimulativeGenerator(Map<String, ArrayList<TaxiGPS>> allTaxiInfosMap){
		int correctedGPSCountThreshold = PubParameter.taxiGPSSimulativeConst;//ÿ�ν��о�����ĸ���
		try {
			AssistFunction assistFunction = new AssistFunction();
			Set keySet = allTaxiInfosMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		String key = (String)mapEntry.getKey();
        		ArrayList<TaxiGPS> taxiGPSArrayList = allTaxiInfosMap.get(key);
        		ArrayList<ArrayList<TaxiGPS>> subdivisionTrackArrayList = new ArrayList<ArrayList<TaxiGPS>>();
        		assistFunction.trackSubdivision(taxiGPSArrayList, PubParameter.sampleThreshold, subdivisionTrackArrayList);//�켣�ʷ�
        		for (int i = 0; i < subdivisionTrackArrayList.size(); i++) {
        			
        			ArrayList<TaxiGPS> subTrackArrayList = new ArrayList<TaxiGPS>();
        			subTrackArrayList = subdivisionTrackArrayList.get(i);
        			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
            		DatabaseFunction.eliminateZeroSpeedGPSData(subTrackArrayList, eliminateZeroSpeedGPSDataArrayList);//ȥ���ٶ�Ϊ���GPS��
            		int taxiGPSCount = eliminateZeroSpeedGPSDataArrayList.size();
            		//���С����ֵ����һ���Ծ���
            		if (taxiGPSCount <= correctedGPSCountThreshold) {
            			//�̳߳�
                		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3, 
        		                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
        		                new ThreadPoolExecutor.DiscardOldestPolicy());
            			Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
            			ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>(); 
            			taxiTrackMap.put(1, eliminateZeroSpeedGPSDataArrayList);
    					ThreadPoolTaxiGPSSimulativeGenerator threadPoolTaxiGPSSimulativeGenerator = new ThreadPoolTaxiGPSSimulativeGenerator();
    					threadPoolTaxiGPSSimulativeGenerator.setTaxiTrackMap(taxiTrackMap);
    					threadPoolTaxiGPSSimulativeGenerator.setGPSCorrectArrayList(GPSCorrectArrayList);
    					threadPool.execute(threadPoolTaxiGPSSimulativeGenerator);
    					threadPool.shutdown(); //�رպ��ܼ������̣߳������е��߳�������ִ����
    					while(threadPool.getPoolSize() != 0);  					
    					for (int j = 0; j < GPSCorrectArrayList.size() - 1; j++) {
    						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(j);
    						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(j + 1);
    						String curTimeStr = curCorrectedNode.getLocalTime();
    						if (curCorrectedNode.getCorrectLatitude() != 0) {
    							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��������GPS�㣺" + 
        								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
        								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
							}
    						else {
    							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��δ����GPS�㣺" + 
        								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
        								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
							}    						
    						String nextTimeStr = nextCorrectedNode.getLocalTime();
    						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
    						String sleepTimeStr = String.valueOf(millisecondInterval);
    						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
    						Thread.sleep(sleepTimeMillisecond);
    						if (j == GPSCorrectArrayList.size() - 2) {
    							if (nextCorrectedNode.getCorrectLatitude() != 0) {
        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��������GPS�㣺" + 
        									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
            								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
    							}
        						else {
        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��δ����GPS�㣺" + 
        									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
            								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
    							}
							}
						}
    				}
            		//���򣬷ֶ�ξ���
            		else {
    					double correctedCount = (double)taxiGPSCount/correctedGPSCountThreshold;//��������
    					String correctedCountStr = String.valueOf(correctedCount);
    					//����ܹ�������
    					if (PubClass.isInteger(correctedCountStr)) {
    						int integerPart = PubClass.obtainIntegerPart(correctedCountStr);
    						int correctedNum = 0;
    						String curNumbLastGPSTime = "";//��ǰ����������һ��GPS��ʱ��
    						for (int j = 0; j < integerPart; j++) {  
    							//�̳߳�
    		            		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3, 
    		    		                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
    		    		                new ThreadPoolExecutor.DiscardOldestPolicy());
    							double systemStartCalculateTime = System.nanoTime();   							
    							correctedNum++;
    							int downIndex = (correctedNum - 1) * correctedGPSCountThreshold;
    							int upIndex = correctedNum * correctedGPSCountThreshold;
    							ArrayList<TaxiGPS> everyCorrectGPSArrayList = new ArrayList<TaxiGPS>();
								for (int j2 = downIndex; j2 < upIndex; j2++) {
									everyCorrectGPSArrayList.add(eliminateZeroSpeedGPSDataArrayList.get(j2));									
								}
								Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	                			ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>(); 
	                			taxiTrackMap.put(1, everyCorrectGPSArrayList);
	                			ThreadPoolTaxiGPSSimulativeGenerator threadPoolTaxiGPSSimulativeGenerator = new ThreadPoolTaxiGPSSimulativeGenerator();
	        					threadPoolTaxiGPSSimulativeGenerator.setTaxiTrackMap(taxiTrackMap);
	        					threadPoolTaxiGPSSimulativeGenerator.setGPSCorrectArrayList(GPSCorrectArrayList);
	        					threadPool.execute(threadPoolTaxiGPSSimulativeGenerator);
	        					threadPool.shutdown(); //�رպ��ܼ������̣߳������е��߳�������ִ����
	        					while(threadPool.getPoolSize() != 0);
	        					double systemEndCalculateTime = System.nanoTime();
	        					double systemCalculateTime = (systemEndCalculateTime - systemStartCalculateTime)/Math.pow(10, 6);//������
	        					String sleepTimeSystemCalculateTimeStr = String.valueOf(systemCalculateTime);
        						int sleepTimeSystemCalculateTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeSystemCalculateTimeStr);
	        					if (correctedNum != 1) {
	        						Thread.sleep(sleepTimeSystemCalculateTimeMillisecond);
	        						String nextNumbFirstGPSTime = GPSCorrectArrayList.get(0).getLocalTime();//��һ�ξ���ʱ����GPS��ʱ��
	        						double millisecondInterval = PubClass.obtainTimeInterval(curNumbLastGPSTime, nextNumbFirstGPSTime) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);//���ξ����������˯��ʱ��
								}
	        					for (int k = 0; k < GPSCorrectArrayList.size() - 1; k++) {
	        						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(k);
	        						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(k + 1);
	        						String curTimeStr = curCorrectedNode.getLocalTime();
	        						if (curCorrectedNode.getCorrectLatitude() != 0) {
	        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��������GPS�㣺" + 
	            								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
	            								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						else {
	        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��δ����GPS�㣺" + 
	            								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
	            								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						String nextTimeStr = nextCorrectedNode.getLocalTime();
	        						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);
	        						if (k == GPSCorrectArrayList.size() - 2) {
	        							curNumbLastGPSTime = nextTimeStr;
	        							if (nextCorrectedNode.getCorrectLatitude() != 0) {
	            							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��������GPS�㣺" + 
	            									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
	                								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	            						else {
	            							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��δ����GPS�㣺" + 
	            									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
	                								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	    							}	        						
								}
							}
    					}
    					//���ܱ�����
    					else {
    						int correctedCountIntegerPart = PubClass.obtainIntegerPart(correctedCountStr);
    						int correctedNum = 0;//��ǰ������������1��ʼ
    						String curNumbLastGPSTime = "";
    						for (int j = 0; j < correctedCountIntegerPart; j++) {  
    							//�̳߳�
    		            		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3, 
    		    		                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),  
    		    		                new ThreadPoolExecutor.DiscardOldestPolicy());
    							double systemStartCalculateTime = System.nanoTime();   							
    							correctedNum++;
    							ArrayList<TaxiGPS> everyCorrectGPSArrayList = new ArrayList<TaxiGPS>();
    							if (j != correctedCountIntegerPart - 1) {
    								int downIndex = (correctedNum - 1) * correctedGPSCountThreshold;
        							int upIndex = correctedNum * correctedGPSCountThreshold;       							
    								for (int j2 = downIndex; j2 < upIndex; j2++) {
    									everyCorrectGPSArrayList.add(eliminateZeroSpeedGPSDataArrayList.get(j2));									
    								}   								
								}
    							else {
									int downIndex = (correctedNum - 1) * correctedGPSCountThreshold;
									int upIndex = eliminateZeroSpeedGPSDataArrayList.size();
									for (int j2 = downIndex; j2 < upIndex; j2++) {
										everyCorrectGPSArrayList.add(eliminateZeroSpeedGPSDataArrayList.get(j2));									
									}									
								}    							
								Map<Integer, ArrayList<TaxiGPS>> taxiTrackMap = new HashMap<Integer, ArrayList<TaxiGPS>>();
	                			ArrayList<CorrectedNode> GPSCorrectArrayList = new ArrayList<CorrectedNode>(); 
	                			taxiTrackMap.put(1, everyCorrectGPSArrayList);
	                			ThreadPoolTaxiGPSSimulativeGenerator threadPoolTaxiGPSSimulativeGenerator = new ThreadPoolTaxiGPSSimulativeGenerator();
	        					threadPoolTaxiGPSSimulativeGenerator.setTaxiTrackMap(taxiTrackMap);
	        					threadPoolTaxiGPSSimulativeGenerator.setGPSCorrectArrayList(GPSCorrectArrayList);
	        					threadPool.execute(threadPoolTaxiGPSSimulativeGenerator);
	        					threadPool.shutdown(); //�رպ��ܼ������̣߳������е��߳�������ִ����
	        					while(threadPool.getPoolSize() != 0);
	        					double systemEndCalculateTime = System.nanoTime();
	        					double systemCalculateTime = (systemEndCalculateTime - systemStartCalculateTime)/Math.pow(10, 6);//������
	        					String sleepTimeSystemCalculateTimeStr = String.valueOf(systemCalculateTime);
        						int sleepTimeSystemCalculateTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeSystemCalculateTimeStr);
	        					if (correctedNum != 1) {
	        						Thread.sleep(sleepTimeSystemCalculateTimeMillisecond);
	        						String nextNumbFirstGPSTime = GPSCorrectArrayList.get(0).getLocalTime();//��һ�ξ���ʱ����GPS��ʱ��
	        						double millisecondInterval = PubClass.obtainTimeInterval(curNumbLastGPSTime, nextNumbFirstGPSTime) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);//���ξ����������˯��ʱ��
								}
	        					for (int k = 0; k < GPSCorrectArrayList.size() - 1; k++) {
	        						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(k);
	        						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(k + 1);
	        						String curTimeStr = curCorrectedNode.getLocalTime();
	        						if (curCorrectedNode.getCorrectLatitude() != 0) {
	        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��������GPS�㣺" + 
	            								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
	            								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						else {
	        							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + curTimeStr + "��δ����GPS�㣺" + 
	            								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
	            								"����·��ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						String nextTimeStr = nextCorrectedNode.getLocalTime();
	        						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);
	        						if (k == GPSCorrectArrayList.size() - 2) {
	        							curNumbLastGPSTime = nextTimeStr;
	        							if (nextCorrectedNode.getCorrectLatitude() != 0) {
	            							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��������GPS�㣺" + 
	            									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
	                								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	            						else {
	            							System.out.print("GPSģ�������У�ģ��ʱ�䣺" + nextTimeStr + "��δ����GPS�㣺" + 
	            									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
	                								"����·��ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	    							}	        						
								}
							}			
    					}
    				}
				}       		
        	}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
		
		
		
		
	}
	
}
