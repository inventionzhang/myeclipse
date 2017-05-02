package org.lmars.network.transportationSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.woden.tool.converter.Convert;
import org.lmars.network.util.FileOperateFunction;
import org.lmars.track.util.ConvertZB;

public class CoordinateConvert {

	
	//读取轨迹数据并将地理坐标转换为高德坐标	
	public void Functions(){
		String originFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602_test.txt";
		String convFileName = "G:\\faming\\myeclipseWorkspace\\Data\\trajectoryData\\taxi20416trajectory0602_test_conv.txt";
		List<String> infosArrayList = new ArrayList<String>();
		ArrayList<String> convInfos = new ArrayList<String>();
		FileOperateFunction.readFromTxtFile(originFileName, infosArrayList);
		for (String str : infosArrayList) {
			String []infosArray = str.split(",");
			String timeStr = infosArray[0];
			double lon = Double.parseDouble(infosArray[1]);
			double lat = Double.parseDouble(infosArray[2]);
			String convLB = ConvertZB.converZB(lat, lon);
			String []convArray = convLB.split(",");
			double conv_lat = Double.parseDouble(convArray[0]);
			double conv_lon = Double.parseDouble(convArray[1]);
			String tempStr = timeStr + "," + String.valueOf(conv_lon)+ "," + String.valueOf(conv_lat); 
			convInfos.add(tempStr);	
		}
		String headDescriptionStr = "localTime" + "," + "longitude" + "," + "latitude";
		FileOperateFunction.writeStringArraylistToTxtFile(convFileName, headDescriptionStr, convInfos);
		System.out.println("done!");	
	}

	
	//主函数
	public static void main(String[] args) {
		new CoordinateConvert().Functions();
	}
	
}
