package mapMatchingGPS;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
/***********************************
 * text文件中，信息的存储与读取
 * ********************************/
public class SaveInformationToText {
	
	/*写信息到text文件中*/
	public void writeInfosToText(MapMatchEdge targetEdge, ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList,
			String outPutPath){
//		try {
////			String outPutPath="E:\\faming\\houhuRoadNetworkName.txt";//写入Txt文件名
//			ArrayList<MapMatchNode> pointCollArrayList = targetEdge.getPointCollArrayList();
//			String pointCollStr = "";
//			for (int i = 0; i < pointCollArrayList.size(); i++) {
//				MapMatchNode tNode = pointCollArrayList.get(i);
//				pointCollStr = String.valueOf(tNode.getX());
//				
//				
//			}
//			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
//			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
//			StringBuffer write = new StringBuffer();
//			String roadDescription = "linkID" + "," + "taxiID" + "," + "startTravelTimeStr" + "," + "travelTime" + "," + "\r\n";
//			write.append(roadDescription);
//			bufferStream.write(write.toString().getBytes("UTF-8"));
//		    for (int i = 0; i < linkTravelTimeArrayList.size(); i++) {
//		    	ReturnLinkTravelTime returnLinkTravelTime = linkTravelTimeArrayList.get(i);
//		    	String linkIDStr = String.valueOf(targetEdge.getEdgeID());
//		    	String taxiIDStr = returnLinkTravelTime.getTaxiID();
//		    	String startTravelTimeStr = returnLinkTravelTime.getStartTravelTime(); 
//		    	String travelTimeStr = String.valueOf(returnLinkTravelTime.getTravelTime());
//		    	String currentLineInfos = linkIDStr + "," + taxiIDStr + "," + startTravelTimeStr 
//		    		+ "," + travelTimeStr + "\r\n";
//		    	write = new StringBuffer();				
//				write.append(currentLineInfos);
//				bufferStream.write(write.toString().getBytes("UTF-8"));
//			}
//			bufferStream.flush();      
//			bufferStream.close(); 
//			outputStream.close();
//			System.out.print("写入结束");
//		} catch (Exception e) {
//			// TODO: handle exception
//			System.out.print(e.getMessage());
//			e.printStackTrace();
//		}		
	}
	
	/*从text文件中读取信息*/
	public void readInfosFromText(String path){
//		String path="E:\\faming\\sixKmRoadnetworkName.txt";
		System.out.print("开始读text文件"+"\n");
		try {
			File file=new File(path);
			String encoding="UTF-8";//解决中文乱码问题
			InputStreamReader reader=new InputStreamReader(new FileInputStream(file),encoding);
			
			BufferedReader bufferedReader=new BufferedReader(reader);
			String str=bufferedReader.readLine();
			System.out.print(str+"\n");
			while (str!=null) {
				str=bufferedReader.readLine();								
				System.out.print(str+"\n");			
			}
			reader.close();
			System.out.print("结束读text文件"+"\n");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO: handle exception
			e.getMessage();
			e.printStackTrace();
		}		
	}
	
	
}
