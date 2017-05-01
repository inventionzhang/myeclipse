package org.lmars.network.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 交管局的服务
 * @author faming
 * 2016年10月7日
 *
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class ServiceTrajectoryCorrect {

	public JSONArray trajectoryCorrect(String inputString) {
		JSONArray jsonArray = new JSONArray();
		try {			
			String askString = inputString;
			HttpURLConnection conn = null;
			BufferedReader in = null;
			BufferedWriter out = null;
        	
//        	URL realUrl = new URL("http://192.168.106.111:10891/split/server");//访问IP
			URL realUrl = new URL("http://localhost:10891/trajectoryCorrect/server");//编码服务IP
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
            System.out.print(result);
            jsonArray = JSONArray.fromObject(result);
            
            Iterator <Object> it = jsonArray.iterator();
            while (it.hasNext()) {
                JSONObject json = (JSONObject)it.next();
                double corrLon = (Double)json.get("corrLon");
                double coorLat = (Double)json.get("coorLat");
                String timeStamp = (String)json.get("timeStamp");
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
