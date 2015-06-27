package entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*读取路径名
 * path：文件路径*/
public class readRoadName {
	public void readName(String pcsName,String path,ArrayList<roadName> roadArrayList)
	{	
//		String path="C:\\sixKmRoadnetworkName.txt";
		System.out.print("开始读取道路名"+"\n");
		try {		
			File file=new File(path);
			String encoding="UTF-8";//解决中文乱码问题
			InputStreamReader reader=new InputStreamReader(new FileInputStream(file),encoding);
			
			BufferedReader bufferedReader=new BufferedReader(reader);
			String str=bufferedReader.readLine();//读取道路描述
			str=bufferedReader.readLine();//读取第一条道路
			System.out.print(str+"\n");
			while (str!=null) {
							
				String[]temp=str.split(",");
				roadName roadInfor=new roadName();//道路信息
				
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
