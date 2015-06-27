package org.artificialNeuralNetwork;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 多个txt文件合并为一个txt文件
 */
public class DeleteMergeTxtFile {
	public static void main(String[] args) {
		ArrayList<String> infosArrayList = new ArrayList<String>();
		readStatisticTravelTime(infosArrayList);
		writeTotxt(infosArrayList);
		
	}
	
	public static void readStatisticTravelTime(ArrayList<String> infosArrayList){
		String tempPath = "F:\\faming\\experimentResult\\travleTime\\201407";	
		for (int i = 1; i <= 31; i++) {
			String path = "";
			String tempDayStr = "";
			if (i < 10) {
				tempDayStr = "0" + String.valueOf(i);
			}
			else {
				tempDayStr = String.valueOf(i);
			}			
			for (int j = 0; j < 4; j++) {
				String fileName = "allLinkTravelTime" + String.valueOf(j) + ".txt";
				path = tempPath + tempDayStr + "-14\\" + fileName;
				System.out.print("开始读txt文件：" + path + "\n");	
				try {
					File file = new File(path);
					if (file.exists()) {
						String encoding = "UTF-8";//解决中文乱码问题
						InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
						BufferedReader bufferedReader = new BufferedReader(reader);
						String str = bufferedReader.readLine();
						System.out.print(str + "\n");
						while (str != null) {
							str = bufferedReader.readLine();
							if (str != null) {
								infosArrayList.add(str);
								System.out.print(str + "\n");
							}							
						}
						reader.close();
						System.out.print("结束读txt文件:" + path + "\n");
					}
					else {
						System.out.print("指定的文件不存在！" + '\n');
					}			
				}
				catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
					System.out.print(e.getMessage());
				}
			}			
		}
		System.out.print("所有文件读取结束");
		
	}
	
	/*写入到txt文件*/
	public static void writeTotxt(ArrayList<String> infosArrayList){
		String outPutPath = "F:\\faming\\experimentResult\\travleTime\\mergeStatisticTravelTime0701-31.txt";//写入Txt文件名
		try {
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + ","  + "taxiID" + "," + "startTravelTime" + "," + "travelTime" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < infosArrayList.size(); i++) {
				String tempStr = infosArrayList.get(i);
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("写入结束");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	

}
