package org.lmars.network.mapMatchingGPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;

import com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;


public class Simulator {

	/*GPS数据模拟发送器
	 * 每taxiGPSSimulativeConst个GPS点进行坐标纠正
	 * allTaxiInfosMap：待纠正的GPS信息,以stringID为键值
	 * allGPSCorrectArrayList：纠正后GPS坐标点
	 * 总体思路：
	 * 1.轨迹剖分：每辆出租车数据分成多条轨迹；
	 * 2.判断是否分批纠正：剖分后轨迹与每次纠正点的个数阈值（默认10个点）进行比较
	 * 	若轨迹点个数小于阈值，则一次性纠正
	 * 	若轨迹点个数大于阈值，则分次进行纠正
	 * 3.数据发送时间选择：根据纠正计算时间以及剖分轨迹之间的时间差确定发送时间*/
	public static void taxiGPSSimulativeGenerator(Map<String, ArrayList<TaxiGPS>> allTaxiInfosMap){
		int correctedGPSCountThreshold = PubParameter.taxiGPSSimulativeConst;//每次进行纠正点的个数
		try {
			AssistFunction assistFunction = new AssistFunction();
			Set keySet = allTaxiInfosMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		String key = (String)mapEntry.getKey();
        		ArrayList<TaxiGPS> taxiGPSArrayList = allTaxiInfosMap.get(key);
        		ArrayList<ArrayList<TaxiGPS>> subdivisionTrackArrayList = new ArrayList<ArrayList<TaxiGPS>>();
        		assistFunction.trackSubdivision(taxiGPSArrayList, PubParameter.sampleThreshold, subdivisionTrackArrayList);//轨迹剖分
        		for (int i = 0; i < subdivisionTrackArrayList.size(); i++) {
        			
        			ArrayList<TaxiGPS> subTrackArrayList = new ArrayList<TaxiGPS>();
        			subTrackArrayList = subdivisionTrackArrayList.get(i);
        			ArrayList<TaxiGPS> eliminateZeroSpeedGPSDataArrayList = new ArrayList<TaxiGPS>();
            		DatabaseFunction.eliminateZeroSpeedGPSData(subTrackArrayList, eliminateZeroSpeedGPSDataArrayList);//去掉速度为零的GPS点
            		int taxiGPSCount = eliminateZeroSpeedGPSDataArrayList.size();
            		//如果小于阈值，则一次性纠正
            		if (taxiGPSCount <= correctedGPSCountThreshold) {
            			//线程池
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
    					threadPool.shutdown(); //关闭后不能加入新线程，队列中的线程则依次执行完
    					while(threadPool.getPoolSize() != 0);  					
    					for (int j = 0; j < GPSCorrectArrayList.size() - 1; j++) {
    						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(j);
    						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(j + 1);
    						String curTimeStr = curCorrectedNode.getLocalTime();
    						if (curCorrectedNode.getCorrectLatitude() != 0) {
    							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：纠正后GPS点：" + 
        								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
        								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
							}
    						else {
    							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：未纠正GPS点：" + 
        								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
        								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
							}    						
    						String nextTimeStr = nextCorrectedNode.getLocalTime();
    						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
    						String sleepTimeStr = String.valueOf(millisecondInterval);
    						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
    						Thread.sleep(sleepTimeMillisecond);
    						if (j == GPSCorrectArrayList.size() - 2) {
    							if (nextCorrectedNode.getCorrectLatitude() != 0) {
        							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：纠正后GPS点：" + 
        									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
            								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
    							}
        						else {
        							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：未纠正GPS点：" + 
        									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
            								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
    							}
							}
						}
    				}
            		//否则，分多次纠正
            		else {
    					double correctedCount = (double)taxiGPSCount/correctedGPSCountThreshold;//纠正次数
    					String correctedCountStr = String.valueOf(correctedCount);
    					//如果能够被整除
    					if (PubClass.isInteger(correctedCountStr)) {
    						int integerPart = PubClass.obtainIntegerPart(correctedCountStr);
    						int correctedNum = 0;
    						String curNumbLastGPSTime = "";//当前计算次数最好一个GPS点时间
    						for (int j = 0; j < integerPart; j++) {  
    							//线程池
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
	        					threadPool.shutdown(); //关闭后不能加入新线程，队列中的线程则依次执行完
	        					while(threadPool.getPoolSize() != 0);
	        					double systemEndCalculateTime = System.nanoTime();
	        					double systemCalculateTime = (systemEndCalculateTime - systemStartCalculateTime)/Math.pow(10, 6);//毫秒数
	        					String sleepTimeSystemCalculateTimeStr = String.valueOf(systemCalculateTime);
        						int sleepTimeSystemCalculateTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeSystemCalculateTimeStr);
	        					if (correctedNum != 1) {
	        						Thread.sleep(sleepTimeSystemCalculateTimeMillisecond);
	        						String nextNumbFirstGPSTime = GPSCorrectArrayList.get(0).getLocalTime();//下一次纠正时，首GPS的时间
	        						double millisecondInterval = PubClass.obtainTimeInterval(curNumbLastGPSTime, nextNumbFirstGPSTime) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);//两次纠正次数间的睡眠时间
								}
	        					for (int k = 0; k < GPSCorrectArrayList.size() - 1; k++) {
	        						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(k);
	        						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(k + 1);
	        						String curTimeStr = curCorrectedNode.getLocalTime();
	        						if (curCorrectedNode.getCorrectLatitude() != 0) {
	        							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：纠正后GPS点：" + 
	            								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
	            								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						else {
	        							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：未纠正GPS点：" + 
	            								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
	            								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						String nextTimeStr = nextCorrectedNode.getLocalTime();
	        						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);
	        						if (k == GPSCorrectArrayList.size() - 2) {
	        							curNumbLastGPSTime = nextTimeStr;
	        							if (nextCorrectedNode.getCorrectLatitude() != 0) {
	            							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：纠正后GPS点：" + 
	            									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
	                								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	            						else {
	            							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：未纠正GPS点：" + 
	            									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
	                								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	    							}	        						
								}
							}
    					}
    					//不能被整除
    					else {
    						int correctedCountIntegerPart = PubClass.obtainIntegerPart(correctedCountStr);
    						int correctedNum = 0;//当前纠正次数，从1开始
    						String curNumbLastGPSTime = "";
    						for (int j = 0; j < correctedCountIntegerPart; j++) {  
    							//线程池
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
	        					threadPool.shutdown(); //关闭后不能加入新线程，队列中的线程则依次执行完
	        					while(threadPool.getPoolSize() != 0);
	        					double systemEndCalculateTime = System.nanoTime();
	        					double systemCalculateTime = (systemEndCalculateTime - systemStartCalculateTime)/Math.pow(10, 6);//毫秒数
	        					String sleepTimeSystemCalculateTimeStr = String.valueOf(systemCalculateTime);
        						int sleepTimeSystemCalculateTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeSystemCalculateTimeStr);
	        					if (correctedNum != 1) {
	        						Thread.sleep(sleepTimeSystemCalculateTimeMillisecond);
	        						String nextNumbFirstGPSTime = GPSCorrectArrayList.get(0).getLocalTime();//下一次纠正时，首GPS的时间
	        						double millisecondInterval = PubClass.obtainTimeInterval(curNumbLastGPSTime, nextNumbFirstGPSTime) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);//两次纠正次数间的睡眠时间
								}
	        					for (int k = 0; k < GPSCorrectArrayList.size() - 1; k++) {
	        						CorrectedNode curCorrectedNode = GPSCorrectArrayList.get(k);
	        						CorrectedNode nextCorrectedNode = GPSCorrectArrayList.get(k + 1);
	        						String curTimeStr = curCorrectedNode.getLocalTime();
	        						if (curCorrectedNode.getCorrectLatitude() != 0) {
	        							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：纠正后GPS点：" + 
	            								curCorrectedNode.getCorrectLongitude() + "," + curCorrectedNode.getCorrectLatitude() + 
	            								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						else {
	        							System.out.print("GPS模拟器运行：模拟时间：" + curTimeStr + "：未纠正GPS点：" + 
	            								curCorrectedNode.getOriginLongitude() + "," + curCorrectedNode.getOriginLatitude() + 
	            								"所在路段ID:" + curCorrectedNode.getTargetEdgeID() +'\n');
	    							}
	        						String nextTimeStr = nextCorrectedNode.getLocalTime();
	        						double millisecondInterval = PubClass.obtainTimeInterval(curTimeStr, nextTimeStr) * 1000;
	        						String sleepTimeStr = String.valueOf(millisecondInterval);
	        						int sleepTimeMillisecond = PubClass.obtainIntegerPart(sleepTimeStr);
	        						Thread.sleep(sleepTimeMillisecond);
	        						if (k == GPSCorrectArrayList.size() - 2) {
	        							curNumbLastGPSTime = nextTimeStr;
	        							if (nextCorrectedNode.getCorrectLatitude() != 0) {
	            							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：纠正后GPS点：" + 
	            									nextCorrectedNode.getCorrectLongitude() + "," + nextCorrectedNode.getCorrectLatitude() + 
	                								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
	        							}
	            						else {
	            							System.out.print("GPS模拟器运行：模拟时间：" + nextTimeStr + "：未纠正GPS点：" + 
	            									nextCorrectedNode.getOriginLongitude() + "," + nextCorrectedNode.getOriginLatitude() + 
	                								"所在路段ID:" + nextCorrectedNode.getTargetEdgeID() +'\n');
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
