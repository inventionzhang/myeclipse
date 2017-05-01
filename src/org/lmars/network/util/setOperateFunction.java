/**
 * 
 */
package org.lmars.network.util;

import java.util.ArrayList;

/**
 * @author faming
 *
 */
public class setOperateFunction {
	
	/**
	 * 因为有重复的数据，在此将重复的数据剔除掉，保证记录的唯一性
	 * 相同记录基本是直接相连的,只跟下一条比较
	 * 根据记录时刻进行比较
	 * 2015-11-17
	 * @param infosArraylist	原始数据
	 * @param processInfosArraylist	去重后数据
	 */
	public static void eliminateDuplicateData(ArrayList<String> infosArraylist, ArrayList<String> processInfosArraylist){
		try {	
			if (infosArraylist.size() != 0) {
				for (int j = 0; j < infosArraylist.size(); j++) {
					String str1 = infosArraylist.get(j);
					processInfosArraylist.add(str1);
					String[] strArray1 = str1.split(",");
					String IDStr1 =  strArray1[0];
					String timeStr1 = strArray1[4];					
					if (j != infosArraylist.size() - 1) {
						String str2 = infosArraylist.get(j + 1);
						String[] strArray2 = str2.split(",");
						String IDStr2 =  strArray2[0];
						String timeStr2 = strArray2[4];
						if (IDStr1.equals(IDStr2) && timeStr1.equals(timeStr2)) {
							j++;//跳过相同记录							
						}
					}					
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}
