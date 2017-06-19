package org.lmars.network.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.lmars.network.util.FileOperateFunction;
import org.lmars.track.util.ConvertZB;

//轨迹查询服务
public class ServiceTrajectorySelect {

	public JSONArray trajectorySelect(String inputString) {
		JSONArray jsonArray = new JSONArray();
		try {			
			//读文件中gps轨迹数据
//			String originFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602_test.txt";
			String originFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602.txt";
			List<String> infosArrayList = new ArrayList<String>();
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
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Lon", lon);
				jsonObject.put("Lat", lat);
				jsonArray.add(jsonObject);
				String tempStr = String.valueOf(lon)+ "," + String.valueOf(lat)  + "," +timeStr;
				if (tempCount == 1) {
					gpsString = tempStr;
				}
				else {
					gpsString = gpsString + ";" + tempStr;
				}				
			}
            
		} catch (Exception e) {
			 System.out.println("获取信息出错");
	         e.printStackTrace();
	         return null;
		}
		 return jsonArray;
	}
	
	
}
