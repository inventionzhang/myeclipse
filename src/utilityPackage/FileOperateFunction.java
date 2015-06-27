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
 * ��д�ļ���ز���
 * @author whu
 *
 */
public class FileOperateFunction {

	/**
	 * ��ȡTxt�ļ�
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public static void readFromTxtFile(String filePathStr, ArrayList<String> infosArrayList){		
		try {
			File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//���������������
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
				System.out.print("������txt�ļ�:" + filePathStr + "\n");
			}
			else {
				System.out.print("ָ�����ļ������ڣ�" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**
	 * ��·������д��Txt�ļ�
	 * @param filePathStr	�ļ�·��
	 * @param headDescriptionStr	ͷ�ļ�������Ϣ
	 * @param infosArrayList
	 */
	public static void writeToTxtFile(String filePathStr, String headDescriptionStr, ArrayList<LinkTravelCharacteristic> infosArrayList){
		try {
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//д���ļ���ʼ��,��Ϣ����
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//д���ļ�ĩβ��
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
			System.out.print("д��txt����" + '\n');		
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
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//д���ļ���ʼ��,��Ϣ����
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//д���ļ�ĩβ��
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
			System.out.print("д��txt����" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * д��txt�ļ�����Ϣ����
	 * @param filePathStr
	 * @param headDescriptionStr
	 * @param infosArrayList
	 */
	public static void writeStringArraylistToTxtFile(String filePathStr, String headDescriptionStr, ArrayList<String> infosArrayList) {
		try {
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//д���ļ���ʼ��,��Ϣ����
//			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//д���ļ�ĩβ��
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
			System.out.print("д��txt����" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * д���ļ�ĩβ��
	 * @param filePathStr
	 * @param infosArrayList
	 */
	public static void writeStringArraylistToEndTxtFile(String filePathStr, ArrayList<String> infosArrayList) {
		try {
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr),true);//д���ļ�ĩβ��
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
			System.out.print("д��txt����" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * д�ļ����⵽�ļ���
	 * @param filePathStr	�ļ�·��
	 * @param headDescriptionStr	�ļ�����
	 */
	public static void writeHeadDescriptionToTxtFile(String filePathStr, String headDescriptionStr) {
		try {
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//д���ļ���ʼ��,��Ϣ����
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();			
			write.append(headDescriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			bufferStream.flush();   
			bufferStream.close();
			outputStream.close();
			System.out.print("д��txt����" + '\n');	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	
	/**
	 * �ϲ�txt�ļ�
	 * @param filePathStr	Ҫ�ϲ����ļ�·��
	 * @param fieNameArrayList	Ҫ�ϲ����ļ�������
	 * @param outputPathStr	�ϲ�����ļ�����·��
	 * @param headDescriptionStr	ͷ�ļ�����
	 */
	public static void mergeTxtFile(String filePathStr, ArrayList<String> fileNameArrayList, String saveOutputPathStr, String headDescriptionStr) {
		try {
			System.out.print("��ʼд��txt�ļ�");	
			FileOutputStream outputStream = new FileOutputStream(new String(saveOutputPathStr),true);//д���ļ�ĩβ��
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
