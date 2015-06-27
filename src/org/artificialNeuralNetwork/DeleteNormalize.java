package org.artificialNeuralNetwork;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import utilityPackage.PubClass;

import entity.PropertiesUtilJAR;


/***
 * ���ݹ�һ��
 * ��ȡͳ��ͨ��ʱ�䲢��������ɢ��Ϊ������ʽ��д��txt�ļ�
 * */
public class DeleteNormalize {

	//���ݹ�һ��ʱ�������Сֵ
	private int max;
	private int min;
	
	public int getMax(){
		return max;	
	}
	
	public void setMax(int max){
		this.max = max;	
	}
	
	public int getMin(){
		return min;	
	}
	
	public void setMin(int min){
		this.min = min;	
	}
	
	public static void main(String[] args) {
		
	}
	
	
	/**readFilePath:�������й�һ����txt�ļ�·��
	 * txt�ļ���ʽ�磺14,0,0,1,60
	 * @param readFilePath:��ȡ�ļ�·��
	 * @param writeFilePath
	 */
	public void NormalizeTxt(String readFilePath, String writeFilePath) {
		normalizeAndWriteToTxt(readFilePath, writeFilePath);
	}
	
	
	/***
	 * ��ȡͳ��ͨ��ʱ�䲢��������ɢ��Ϊ������ʽȻ��д��txt�ļ�
	 */
	private void readStatisticTravelTimeAndDiscretizeVector(String txtFilePath){		 
		 ArrayList<String[]> infosArrayList = new ArrayList<String[]>();
		 readStatisticTravelTime(infosArrayList);
		 discretizeVectorAndWriteTotxt(infosArrayList);
	 }
	
	/*��ȡͳ��ͨ��ʱ��
	 * */
	private void readStatisticTravelTime(ArrayList<String[]> infosArrayList){
		String path = "C:\\mergeStatisticTravelTime.txt";
		System.out.print("��ʼ��txt�ļ�" + "\n");
		try {
			File file = new File(path);
			if (file.exists()) {
				String encoding = "UTF-8";//���������������
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				while (str != null) {
					str = bufferedReader.readLine();
					if (str != null) {
						String[]tempArrayStr = str.split(",");
						String[]arrayStr = new String[3];
						arrayStr[0] = tempArrayStr[0];
						arrayStr[1] = tempArrayStr[2];
						arrayStr[2] = tempArrayStr[3];
						infosArrayList.add(arrayStr);
						System.out.print(str+"\n");
					}							
				}
				reader.close();
				System.out.print("������txt�ļ�"+"\n");
			}
			else {
				System.out.print("ָ�����ļ������ڣ�" + '\n');
			}			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	/**���ݹ�һ����д��txt�ļ�
	 * 1.�������ݶ���������;
	 * 2.�������������е������Сֵ;
	 * 3.��һ������x��=(x-xmin)/(xmax - xmin);
	 * readPath:Ҫ��ȡ�ļ�·��
	 * writePath:��һ����Ҫд���ļ�·��
	 * */
	private void normalizeAndWriteToTxt(String readPath, String writePath){
////		String readPath = "C:\\discretizeVector.txt";//Ҫ��ȡ�����������ļ�
////		String writePath = "C:\\normalize.txt";//Ҫд��Ĺ�һ���ļ���
//		ArrayList<ANNInputVector> processArrayList = new ArrayList<ANNInputVector>();//���洦������������
//		int max = 0;//���������Сֵ��ʼֵ
//		int min = 0;
//		try {
//			System.out.print("��ʼ��txt�ļ�:" + "\n");
//			File file = new File(readPath);
//			if (file.exists()) {
//				String encoding = "UTF-8";//���������������
//				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
//				BufferedReader bufferedReader = new BufferedReader(reader);
//				String str = bufferedReader.readLine();
//				System.out.print(str + "\n");
//				while (str != null) {
//					if (str != null) {
//						String[]tempArrayStr = str.split(",");
//						ANNInputVector processInputVector = new ANNInputVector();
//						int linkID = Integer.parseInt(tempArrayStr[0]);
//						int hour = Integer.parseInt(tempArrayStr[1]);
//						int halfHour = Integer.parseInt(tempArrayStr[2]);
//						int workDay = Integer.parseInt(tempArrayStr[3]);
//						int travelTime = Integer.parseInt(tempArrayStr[4]);
//						processInputVector.setLinkID(linkID);
//						processInputVector.setHour(hour);
//						processInputVector.setPreHalfHour(halfHour);
//						processInputVector.setWorkDay(workDay);
//						processInputVector.setTravelTime(travelTime);
//						processArrayList.add(processInputVector);
//						if (linkID > max) {
//							max = linkID;
//						}
//						if (hour > max) {
//							max = hour;
//						}
//						if (halfHour > max) {
//							max = halfHour;
//						}
//						if (workDay > max) {
//							max = workDay;
//						}
//						if (travelTime > max) {
//							max = travelTime;
//						}
//						
//						if (linkID < min) {
//							min = linkID;
//						}
//						if (hour < min) {
//							min = hour;
//						}
//						if (halfHour < min) {
//							min = halfHour;
//						}
//						if (workDay < min) {
//							min = workDay;
//						}
//						if (travelTime < min) {
//							min = travelTime;
//						}
//					}
//					str = bufferedReader.readLine();
//				}
//				reader.close();
//				System.out.print("������txt�ļ�"+"\n");
//			}
//			else {
//				System.out.print("ָ�����ļ������ڣ�" + '\n');
//			}
//			setMax(max);
//			setMin(min);
//			//��һ����д��txt�ļ�
//			File file2 = new File(writePath);
//			if (file2.exists()) {
//				file2.delete();
//				FileOutputStream outputStream = new FileOutputStream(new String(writePath));
//				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
//				StringBuffer write = new StringBuffer();
//				bufferStream.write(write.toString().getBytes("UTF-8"));
//				for (int i = 0; i < processArrayList.size(); i++) {
//					ANNInputVector tempInputVector = processArrayList.get(i);
//					int linkID = tempInputVector.getLinkID();
//					int hour = tempInputVector.getHour();
//					int preHalfHour = tempInputVector.getPreHalfHour();
//					int workDay = tempInputVector.getWorkDay();
//					int travelTime = tempInputVector.getTravelTime();
//					double normalizeLinkID = (double)(linkID - min)/(max - min);
//					String normalizeLinkIDStr = String.format("%.6f", normalizeLinkID);//����С�������λ����������������
//					double normalizeHour = (double)(hour - min)/(max - min);
//					String normalizeHourStr = String.format("%.6f", normalizeHour);
//					double normalizePreHalfHour = (double)(preHalfHour - min)/(max - min);
//					String normalizePreHalfHourStr = String.format("%.6f", normalizePreHalfHour);
//					double normalizeWorkDay = (double)(workDay - min)/(max - min);
//					String normalizeWorkDayStr = String.format("%.6f", normalizeWorkDay);
//					double normalizeTravelTime = (double)(travelTime - min)/(max -min);	
//					String normalizeTravelTimeStr = String.format("%.6f", normalizeTravelTime);
//					String normalizeStr = normalizeLinkIDStr + "," + normalizeHourStr + "," + normalizePreHalfHourStr + "," 
//						+ normalizeWorkDayStr + "," + normalizeTravelTimeStr + "\r\n"; 
//					System.out.print(normalizeStr);
//					write = new StringBuffer();		
//					write.append(normalizeStr);
//					bufferStream.write(write.toString().getBytes("UTF-8"));		
//				}
//				bufferStream.flush();  
//				bufferStream.close();
//				outputStream.close();
//				System.out.print("д�����" + '\n');
//			}
//			else {
//				FileOutputStream outputStream = new FileOutputStream(new String(writePath));
//				BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
//				StringBuffer write = new StringBuffer();
//				bufferStream.write(write.toString().getBytes("UTF-8"));
//				for (int i = 0; i < processArrayList.size(); i++) {
//					ANNInputVector tempInputVector = processArrayList.get(i);
//					int linkID = tempInputVector.getLinkID();
//					int hour = tempInputVector.getHour();
//					int preHalfHour = tempInputVector.getPreHalfHour();
//					int workDay = tempInputVector.getWorkDay();
//					int travelTime = tempInputVector.getTravelTime();
//					double normalizeLinkID = (double)(linkID - min)/(max - min);
//					String normalizeLinkIDStr = String.format("%.6f", normalizeLinkID);//����С�������λ����������������
//					double normalizeHour = (double)(hour - min)/(max - min);
//					String normalizeHourStr = String.format("%.6f", normalizeHour);
//					double normalizePreHalfHour = (double)(preHalfHour - min)/(max - min);
//					String normalizePreHalfHourStr = String.format("%.6f", normalizePreHalfHour);
//					double normalizeWorkDay = (double)(workDay - min)/(max - min);
//					String normalizeWorkDayStr = String.format("%.6f", normalizeWorkDay);
//					double normalizeTravelTime = (double)(travelTime - min)/(max -min);	
//					String normalizeTravelTimeStr = String.format("%.6f", normalizeTravelTime);
//					String normalizeStr = normalizeLinkIDStr + "," + normalizeHourStr + "," + normalizePreHalfHourStr + "," 
//						+ normalizeWorkDayStr + "," + normalizeTravelTimeStr + "\r\n"; 
//					System.out.print(normalizeStr);
//					write = new StringBuffer();		
//					write.append(normalizeStr);
//					bufferStream.write(write.toString().getBytes("UTF-8"));		
//				}
//				bufferStream.flush();  
//				bufferStream.close();
//				outputStream.close();
//				System.out.print("д�����" + '\n');
//			}		
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			System.out.print(e.getMessage());
//		}	
	}
	
	/*���ݹ�һ����д��txt�ļ�
	 * 1.������ɢ��Ϊ������ʽ
	 * 2.д��txt�ļ�*/
	private void discretizeVectorAndWriteTotxt(ArrayList<String[]> infosArrayList){
		String outPutPath = "C:\\discretizeVector.txt";//д��Txt�ļ���
		try {		
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + ","  + "hour" + "," + "firstHalfH" + "," + "workday" + "," + "travelTime" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			//������ɢ��Ϊ������ʽ	
			for (int i = 0; i < infosArrayList.size(); i++) {
				int[] inputVector = new int[5];//����������5������
				String[]arrayStr = new String[3];
				arrayStr = infosArrayList.get(i);
				int linkID = Integer.valueOf(arrayStr[0]);
				inputVector[0] = linkID;
				double tempTravelTime = Double.valueOf(arrayStr[2]);
				int travelTime = (int)tempTravelTime;
				inputVector[4] = travelTime;
				String timeStr = arrayStr[1];
				String[]tempArrayStr = timeStr.split(" ");
				String dateStr = tempArrayStr[0];
				boolean isWorkday = true;//�Ƿ�Ϊ������
				String[]hourMinSec = tempArrayStr[1].split(":");
				String hourStr = hourMinSec[0];//ʱ
				String minStr = hourMinSec[1];//��				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
				Date date = simpleDateFormat.parse(dateStr); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
			    	isWorkday = false;
				}
			    if (isWorkday) {
					inputVector[3] = 1;
				}
			    else {
			    	inputVector[3] = 0;
				}			    
				int hourInt = Integer.valueOf(hourStr);
				int minInt = Integer.valueOf(minStr);
				switch (hourInt) {
				case 0:
					if (minInt <= 30) {
						inputVector[1] = 0;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 0;
						inputVector[2] = 0;
					}
					break;
				case 1:
					if (minInt <= 30) {
						inputVector[1] = 1;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 1;
						inputVector[2] = 0;
					}
					break;					
				case 2:
					if (minInt <= 30) {
						inputVector[1] = 2;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 2;
						inputVector[2] = 0;
					}
					break;
				case 3:
					if (minInt <= 30) {
						inputVector[1] = 3;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 3;
						inputVector[2] = 0;
					}
					break;
				case 4:
					if (minInt <= 30) {
						inputVector[1] = 4;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 4;
						inputVector[2] = 0;
					}
					break;
				case 5:
					if (minInt <= 30) {
						inputVector[1] = 5;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 5;
						inputVector[2] = 0;
					}
					break;
				case 6:
					if (minInt <= 30) {
						inputVector[1] = 6;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 6;
						inputVector[2] = 0;
					}
					break;
				case 7:
					if (minInt <= 30) {
						inputVector[1] = 7;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 7;
						inputVector[2] = 0;
					}
					break;
				case 8:
					if (minInt <= 30) {
						inputVector[1] = 8;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 8;
						inputVector[2] = 0;
					}
					break;
				case 9:
					if (minInt <= 30) {
						inputVector[1] = 9;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 9;
						inputVector[2] = 0;
					}
					break;
				case 10:
					if (minInt <= 30) {
						inputVector[1] = 10;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 10;
						inputVector[2] = 0;
					}
					break;
				case 11:
					if (minInt <= 30) {
						inputVector[1] = 11;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 11;
						inputVector[2] = 0;
					}
					break;
				case 12:
					if (minInt <= 30) {
						inputVector[1] = 12;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 12;
						inputVector[2] = 0;
					}
					break;
				case 13:
					if (minInt <= 30) {
						inputVector[1] = 13;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 13;
						inputVector[2] = 0;
					}
					break;
				case 14:
					if (minInt <= 30) {
						inputVector[1] = 14;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 14;
						inputVector[2] = 0;
					}
					break;
				case 15:
					if (minInt <= 30) {
						inputVector[1] = 15;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 15;
						inputVector[2] = 0;
					}
					break;
				case 16:
					if (minInt <= 30) {
						inputVector[1] = 16;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 16;
						inputVector[2] = 0;
					}
					break;
				case 17:
					if (minInt <= 30) {
						inputVector[1] = 17;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 17;
						inputVector[2] = 0;
					}
					break;
				case 18:
					if (minInt <= 30) {
						inputVector[1] = 18;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 18;
						inputVector[2] = 0;
					}
					break;
				case 19:
					if (minInt <= 30) {
						inputVector[1] = 19;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 19;
						inputVector[2] = 0;
					}
					break;
				case 20:
					if (minInt <= 30) {
						inputVector[1] = 20;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 20;
						inputVector[2] = 0;
					}
					break;
				case 21:
					if (minInt <= 30) {
						inputVector[1] = 21;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 21;
						inputVector[2] = 0;
					}
					break;
				case 22:
					if (minInt <= 30) {
						inputVector[1] = 22;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 22;
						inputVector[2] = 0;
					}
					break;
				case 23:
					if (minInt <= 30) {
						inputVector[1] = 23;
						inputVector[2] = 1;
					}
					else {
						inputVector[1] = 23;
						inputVector[2] = 0;
					}
					break;
				default:
					break;
				}
				String vectorStr = inputVector[0] + "," + inputVector[1] + "," + inputVector[2] + "," + inputVector[3] + "," + inputVector[4] + "\r\n"; 
				System.out.print(vectorStr);
				write = new StringBuffer();		
				write.append(vectorStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));				
			}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("д�����");		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	public static void normalizeAndWriteTotxt22(ArrayList<String[]> infosArrayList){
		String outPutPath = "C:\\normalize.txt";//д��Txt�ļ���
		try {
			FileOutputStream outputStream = new FileOutputStream(new String(outPutPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String roadDescription = "linkID" + "," + "zeroH" + "," + "oneH" + "," + "twoH" + "," + "threeH" + "," + "fourH" + "," + "fiveH" + ","
			+ "sixH" + "," + "sevenH" + "," + "eightH" + "," + "nineH" + "," + "tenH" + "," + "elevenH" + "," + "twelveH" + "," + "thirteenH" + ","
			+ "fourteenH" + "," + "fifteenH" + "," + "sixteenH" + "," + "seventeenH"  + "eighteenH" + "," + "ninteenH" + "," + "twentyH" + "tweentyoneH" + "," 
			+ "tweentytwoH" + "," + "tweentythreeH" + "," + "firstHalfH" + "," + "secondHalfH" + "," + "workday" + "," + "weekend" + "," 
			+ "travelTime" + "," + "\r\n";
			write.append(roadDescription);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			//������ɢ��Ϊ������ʽ	
			for (int i = 0; i < infosArrayList.size(); i++) {
				int[] inputVector = new int[30];//����������30������
				String[]arrayStr = new String[3];
				arrayStr = infosArrayList.get(i);
				int linkID = Integer.valueOf(arrayStr[0]);
				inputVector[0] = linkID;
				int travelTime = Integer.valueOf(arrayStr[2]);
				String timeStr = arrayStr[1];
				String[]tempArrayStr = timeStr.split(" ");
				String dateStr = tempArrayStr[0];
				boolean isWorkday = true;//�Ƿ�Ϊ������
				String[]hourMinSec = tempArrayStr[1].split(":");
				String hourStr = hourMinSec[0];//ʱ
				String minStr = hourMinSec[1];//��
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");        
				String bDate = "2011-12-31"; 
				Date bdate = simpleDateFormat.parse(bDate); 
			    Calendar cal = Calendar.getInstance();
			    cal.setTime(bdate);
			    if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
			    	isWorkday = false;
				}
				else {
					isWorkday = true;
				}
			    
			    if (isWorkday) {
					inputVector[27] = 1;
					inputVector[28] = 0;
				}
			    else {
			    	inputVector[27] = 0;
			    	inputVector[28] = 1;
				}
			    
				int hourInt = Integer.valueOf(hourStr);
				int minInt = Integer.valueOf(minStr);
				switch (hourInt) {
				case 0:
					if (minInt <= 30) {
						inputVector[1] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[1] = 1;
						inputVector[26] = 1;
					}
					break;
				case 1:
					if (minInt <= 30) {
						inputVector[2] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[2] = 1;
						inputVector[26] = 1;
					}
					break;					
				case 2:
					if (minInt <= 30) {
						inputVector[3] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[3] = 1;
						inputVector[26] = 1;
					}
					break;
				case 3:
					if (minInt <= 30) {
						inputVector[4] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[4] = 1;
						inputVector[26] = 1;
					}
					break;
				case 4:
					if (minInt <= 30) {
						inputVector[5] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[5] = 1;
						inputVector[26] = 1;
					}
					break;
				case 5:
					if (minInt <= 30) {
						inputVector[6] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[6] = 1;
						inputVector[26] = 1;
					}
					break;
				case 6:
					if (minInt <= 30) {
						inputVector[7] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[7] = 1;
						inputVector[26] = 1;
					}
					break;
				case 7:
					if (minInt <= 30) {
						inputVector[8] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[8] = 1;
						inputVector[26] = 1;
					}
					break;
				case 8:
					if (minInt <= 30) {
						inputVector[9] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[9] = 1;
						inputVector[26] = 1;
					}
					break;
				case 9:
					if (minInt <= 30) {
						inputVector[10] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[10] = 1;
						inputVector[26] = 1;
					}
					break;
				case 10:
					if (minInt <= 30) {
						inputVector[11] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[11] = 1;
						inputVector[26] = 1;
					}
					break;
				case 11:
					if (minInt <= 30) {
						inputVector[12] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[12] = 1;
						inputVector[26] = 1;
					}
					break;
				case 12:
					if (minInt <= 30) {
						inputVector[13] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[13] = 1;
						inputVector[26] = 1;
					}
					break;
				case 13:
					if (minInt <= 30) {
						inputVector[14] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[14] = 1;
						inputVector[26] = 1;
					}
					break;
				case 14:
					if (minInt <= 30) {
						inputVector[15] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[15] = 1;
						inputVector[26] = 1;
					}
					break;
				case 15:
					if (minInt <= 30) {
						inputVector[16] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[16] = 1;
						inputVector[26] = 1;
					}
					break;
				case 16:
					if (minInt <= 30) {
						inputVector[17] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[17] = 1;
						inputVector[26] = 1;
					}
					break;
				case 17:
					if (minInt <= 30) {
						inputVector[18] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[18] = 1;
						inputVector[26] = 1;
					}
					break;
				case 18:
					if (minInt <= 30) {
						inputVector[19] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[19] = 1;
						inputVector[26] = 1;
					}
					break;
				case 19:
					if (minInt <= 30) {
						inputVector[20] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[20] = 1;
						inputVector[26] = 1;
					}
					break;
				case 20:
					if (minInt <= 30) {
						inputVector[21] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[21] = 1;
						inputVector[26] = 1;
					}
					break;
				case 21:
					if (minInt <= 30) {
						inputVector[22] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[22] = 1;
						inputVector[26] = 1;
					}
					break;
				case 22:
					if (minInt <= 30) {
						inputVector[23] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[23] = 1;
						inputVector[26] = 1;
					}
					break;
				case 23:
					if (minInt <= 30) {
						inputVector[24] = 1;
						inputVector[25] = 1;
					}
					else {
						inputVector[24] = 1;
						inputVector[26] = 1;
					}
					break;
				default:
					break;
				}			
			}
			bufferStream.flush();      
			bufferStream.close(); 
			outputStream.close();
			System.out.print("д�����");		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	
}
