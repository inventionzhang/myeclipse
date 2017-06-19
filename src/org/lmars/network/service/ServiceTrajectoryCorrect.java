package org.lmars.network.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lmars.track.util.ConvertZB;
import org.lmars.network.util.FileOperateFunction;

public class ServiceTrajectoryCorrect {

	public JSONArray trajectoryCorrect(String inputString) {
		JSONArray jsonArray = new JSONArray();
		JSONArray jsonArrayConvGD = new JSONArray();//转换为高德坐标的GPS 
		try {			
			//读文件中gps轨迹数据
//			String originFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602_test.txt";
			String originFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602.txt";
			List<String> infosArrayList = new ArrayList<String>();
			ArrayList<String> convInfos = new ArrayList<String>();
			FileOperateFunction.readFromTxtFile(originFileName, infosArrayList);
			String gpsString = "";
			int tempCount = 0;
			int gpsCount = infosArrayList.size(); 
			for (String str : infosArrayList) {
				tempCount ++;
				String []infosArray = str.split(",");
				String timeStr = infosArray[0];
				double lon = Double.parseDouble(infosArray[1]);
				double lat = Double.parseDouble(infosArray[2]);
				String tempStr = String.valueOf(lon)+ "," + String.valueOf(lat)  + "," +timeStr;
				if (tempCount == 1) {
					gpsString = tempStr;
				}
				else {
					gpsString = gpsString + ";" + tempStr;
				}
				
			}
			
//			inputString = "114.314983,30.525748,2014-06-02 00:04:40;114.3155,30.527016,2014-06-02 00:04:50;" +
//					"114.31704,30.528831,2014-06-02 00:05:10";
			inputString = gpsString;
			String askString = inputString;
			HttpURLConnection conn = null;
			BufferedReader in = null;
			BufferedWriter out = null;
			URL realUrl = new URL("http://localhost:10891/trajectoryCorrect/server");//轨迹纠正服务IP
            conn=(HttpURLConnection)realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");  
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");   
            conn.setConnectTimeout(3000);
            conn.connect();
            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));    
            out.write(askString);
            out.flush();
            out.close(); 
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String result = new String();
            String line="";
            while ((line = in.readLine()) != null) {
                result += line;
            }
            System.out.print("纠正坐标：" + result + '\n');       
            jsonArray = JSONArray.fromObject(result);
            
            System.out.print("高德坐标：");
            Iterator <Object> it = jsonArray.iterator();
            while (it.hasNext()) {
                JSONObject json = (JSONObject)it.next();
                double corrLon = (Double)json.get("corrLon");
                double corrLat = (Double)json.get("corrLat");
                String timeStamp = (String)json.get("timeStamp");
                
                String convLB = ConvertZB.converZB(corrLat, corrLon);
    			String []convArray = convLB.split(",");
    			double conv_lat = Double.parseDouble(convArray[0]);
    			double conv_lon = Double.parseDouble(convArray[1]);
    			JSONObject jsonConv = new JSONObject();
    			jsonConv.put("corrLon", conv_lon);
    			jsonConv.put("corrLat", conv_lat);
    			jsonConv.put("timeStamp", timeStamp);
    			jsonArrayConvGD.add(jsonConv);
            } 
            
		} catch (Exception e) {
			 System.out.println("获取信息出错");
	         e.printStackTrace();
	         return null;
		}
		 return jsonArray;
	}
	
	public static void main(String[] args){
		String inputStr = "114.314983,30.525748,2014-06-02 00:04:40;114.3155,30.527016,2014-06-02 00:04:50;" +
				"114.31704,30.528831,2014-06-02 00:05:10";
		new ServiceTrajectoryCorrect().trajectoryCorrect(inputStr);
	}
	
}
