package org.lmars.network.sparseData;

import java.util.ArrayList;

import org.lmars.network.util.FileOperateFunction;
import org.lmars.network.util.PropertiesUtilJAR;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;


public class NormalizeSparseData {
	
	/**
	 * 神经网络输入信息归一化处理
	 * @param ANNInputFilePathStr	神经网络输入文件
	 * @param metaDataFilePathStr	归一化后元数据文件
	 * @param normalizefilePathStr	归一化后文件
	 */
	public void dataNormalize(String ANNInputFilePathStr, String metaDataFilePathStr, String normalizefilePathStr) {		
		ArrayList<String> infosArrayList = new ArrayList<String>();
		FileOperateFunction.readFromTxtFile(ANNInputFilePathStr, infosArrayList);
		double []maxMinVal = new double[2];
		obtainMaxMinVal(infosArrayList, maxMinVal);
		double maxVal = maxMinVal[0];
		double minVal = maxMinVal[1];
		ArrayList<String> metaDataArrayList = new ArrayList<String>();
		String maxStr = String.valueOf(maxVal);
		String minValStr = String.valueOf(minVal);
		String str = maxStr + "," + minValStr;
		metaDataArrayList.add(str);		
		String headDescriptionStr = "maxVal" + "," + "minVal" + "\r\n";
		FileOperateFunction.writeStringArraylistToTxtFile(metaDataFilePathStr, headDescriptionStr, metaDataArrayList);
		ArrayList<String> processInfosArrayList = new ArrayList<String>();
		normalizeAccordMaxMinVal(maxVal, minVal, infosArrayList, processInfosArrayList);
		String headheadDescriptionStr = "workday" + "," + "time" + "," + "targetLinkID" + "," + "adjacentLinkID" + "," + "targetLinkLength" + "," +
		"adjacentLinkLength" + "," + "targetLinkTravelTime" + "," +"adjacentLinkTravelTime" + "," +
		"similarity" + "," + "lengthRate" + "," + "speedExpectation" + "," + "speedStandardDevi" + "," + "travelTimeRate" + "\r\n";
		FileOperateFunction.writeStringArraylistToTxtFile(normalizefilePathStr, headheadDescriptionStr, processInfosArrayList);
		
	}
	

	/**
	 * 获得最大值、最小值
	 * @param infosArrayList
	 * @param maxMinVal	maxMinVal[0]:最大值	maxMinVal[1]:最小值
	 */
	public void obtainMaxMinVal(ArrayList<String> infosArrayList, double[] maxMinVal) {
		try {
			double max = 0;
			double min = 0;
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[]tempArrayStr = str.split(",");
				int count = tempArrayStr.length;
				for (int j = 0; j < count; j++) {
					double t = Double.parseDouble(tempArrayStr[j]);
					if (t > max) {
						max = t;
					}
					if (t < min) {
						min = t;
					}
				}
			}
			maxMinVal[0] = max;
			maxMinVal[1] = min;						
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}

	/**
	 * 根据最大最小值归一化处理
	 * @param maxVal
	 * @param minVal
	 * @param infosArrayList
	 * @param processInfosArrayList
	 */
	public void normalizeAccordMaxMinVal(double maxVal, double minVal, ArrayList<String> infosArrayList, ArrayList<String> processInfosArrayList) {
		try {
			for (int i = 0; i < infosArrayList.size(); i++) {
				String str = infosArrayList.get(i);
				String[]tempArrayStr = str.split(",");
				int count = tempArrayStr.length;
				String processStr = "";
				for (int j = 0; j < count; j++) {
					double temp = Double.parseDouble(tempArrayStr[j]);
					double processTemp = (temp - minVal)/(maxVal - minVal);
					String processTempStr = String.format("%.4f", processTemp);
					if (processStr.equals("")) {
						processStr = processTempStr;
					}
					else {
						processStr = processStr + "," + processTempStr;
					}
				}
				processInfosArrayList.add(processStr);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		
	}
}
