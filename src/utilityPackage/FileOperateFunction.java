package utilityPackage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sparseData.LinkTravelCharacteristic;

/**
 * 读写文件相关操作
 * @author whu
 *
 */
public class FileOperateFunction {

	/**
	 * 读取Txt文件
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public static void readFromTxtFile(String filePathStr, ArrayList<String> infosArrayList){		
		try {
			File file = new File(filePathStr);
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
				System.out.print("结束读txt文件:" + filePathStr + "\n");
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
	
	/**
	 * 将路段特征写入Txt文件
	 * @param filePathStr	文件路径
	 * @param headDescriptionStr	头文件描述信息
	 * @param infosArrayList
	 */
	public static void writeToTxtFile(String filePathStr, String headDescriptionStr, ArrayList<LinkTravelCharacteristic> infosArrayList){
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件开始处,信息覆盖
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < infosArrayList.size(); i++) {
				LinkTravelCharacteristic linkTravelCharacteristic = infosArrayList.get(i);
				int linkID = linkTravelCharacteristic.getLinkID();
				int enterNodeID = linkTravelCharacteristic.getEnterNodeID();
				int exitNodeID = linkTravelCharacteristic.getExitNodeID();
				int direction = linkTravelCharacteristic.getDirection();
				String startTimeStr = linkTravelCharacteristic.getTimeStr();
				double speedExpectation = linkTravelCharacteristic.getSpeedExpectation();
				double speedStandardDeviation = linkTravelCharacteristic.getSpeedStandDeviation();
				int linkDegree = linkTravelCharacteristic.getLinkDegree();
				double linkLength = linkTravelCharacteristic.getLinkLength();
				String tempStr = linkID + "," + enterNodeID + "," + exitNodeID + "," + direction + "," + startTimeStr + "," + speedExpectation + "," + speedStandardDeviation + "," + linkDegree + "," + linkLength;
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("写入txt结束" + '\n');		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param filePathStr
	 * @param headDescriptionStr
	 * @param infosArrayList
	 */
	public static void writeANNInputToTxtFile(String filePathStr, String headDescriptionStr, ArrayList<double[]> infosArrayList) {
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件开始处,信息覆盖
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < infosArrayList.size(); i++) {
				double [] temp = infosArrayList.get(i);
				String tempStr = "";
				for (int j = 0; j < temp.length; j++) {
					if (j == 0 ) {
						tempStr = String.valueOf(temp[0]);
					}
					else {
						tempStr = tempStr + "," + temp[j];	
					}								
				}
//				double output0 = temp[0];
//				double output1 = temp[1];
//				double output2 = temp[2];
//				double output3 = temp[3];
//				String tempStr = output0 + "," + output1 + "," + output2 + "," + output3;
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("写入txt结束" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 写入txt文件，信息覆盖
	 * @param filePathStr
	 * @param headDescriptionStr
	 * @param infosArrayList
	 */
	public static void writeStringArraylistToTxtFile(String filePathStr, String headDescriptionStr, ArrayList<String> infosArrayList) {
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件开始处,信息覆盖
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[] tempArray = str.split(",");
				String tempStr = "";
				for (int j = 0; j < tempArray.length; j++) {
					String output = tempArray[j];
					if (tempStr.equals("")) {
						tempStr = output;						
					}
					else {
						tempStr = tempStr + "," + output;
					}
				}				
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("写入txt结束" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 写到文件末尾处
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public static void writeStringArraylistToEndTxtFile(String filePathStr, ArrayList<String> infosArrayList) {
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[] tempArray = str.split(",");
				String tempStr = "";
				for (int j = 0; j < tempArray.length; j++) {
					String output = tempArray[j];
					if (tempStr.equals("")) {
						tempStr = output;						
					}
					else {
						tempStr = tempStr + "," + output;
					}
				}				
				System.out.print(tempStr + '\n');
				write = new StringBuffer();		
				write.append(tempStr + "\r\n");
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("写入txt结束" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * 写文件标题到文件中
	 * @param filePathStr	文件路径
	 * @param headDescriptionStr	文件标题
	 */
	public static void writeHeadDescriptionToTxtFile(String filePathStr, String headDescriptionStr) {
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件开始处,信息覆盖
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("写入txt结束" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/**
	 * 合并txt文件
	 * @param filePathStr	要合并的文件路径
	 * @param fieNameArrayList	要合并的文件名集合
	 * @param outputPathStr	合并后的文件保存路径
	 * @param headDescriptionStr	头文件标题
	 */
	public static void mergeTxtFile(String filePathStr, ArrayList<String> fileNameArrayList, String saveOutputPathStr, String headDescriptionStr) {
		try {
			System.out.print("开始写入txt文件");	
			FileOutputStream outputStream = new FileOutputStream(new String(saveOutputPathStr),true);//写入文件末尾处
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();	
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			for (int i = 0; i < fileNameArrayList.size(); i++) {
				String fileNameStr = fileNameArrayList.get(i);
				String filePathNameStr = filePathStr + "\\" + fileNameStr + ".txt";
				ArrayList<String> infosArrayList = new ArrayList<String>();
				readFromTxtFile(filePathNameStr, infosArrayList);
				writeStringArraylistToEndTxtFile(saveOutputPathStr, infosArrayList);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
}
