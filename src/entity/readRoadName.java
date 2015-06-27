package entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*��ȡ·����
 * path���ļ�·��*/
public class readRoadName {
	public void readName(String pcsName,String path,ArrayList<roadName> roadArrayList)
	{	
//		String path="C:\\sixKmRoadnetworkName.txt";
		System.out.print("��ʼ��ȡ��·��"+"\n");
		try {		
			File file=new File(path);
			String encoding="UTF-8";//���������������
			InputStreamReader reader=new InputStreamReader(new FileInputStream(file),encoding);
			
			BufferedReader bufferedReader=new BufferedReader(reader);
			String str=bufferedReader.readLine();//��ȡ��·����
			str=bufferedReader.readLine();//��ȡ��һ����·
			System.out.print(str+"\n");
			while (str!=null) {
							
				String[]temp=str.split(",");
				roadName roadInfor=new roadName();//��·��Ϣ
				
				roadInfor.setRoadID(Integer.valueOf(temp[0]));
				roadInfor.setRoadName(temp[1]);
				retuNode froPoint=new retuNode();
				retuNode toPoint=new retuNode();
				froPoint.setL(Double.valueOf(temp[2]));
				froPoint.setB(Double.valueOf(temp[3]));
				toPoint.setL(Double.valueOf(temp[4]));
				toPoint.setB(Double.valueOf(temp[5]));
				roadInfor.setFroPoint(froPoint);
				roadInfor.setToPoint(toPoint);	
				
				roadArrayList.add(roadInfor);
				str=bufferedReader.readLine();	
//				System.out.print(str+"\n");			
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}		
		
		
	}

}
