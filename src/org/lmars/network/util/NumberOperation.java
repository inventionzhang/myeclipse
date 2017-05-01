package org.lmars.network.util;

public class NumberOperation {

	/**
	 * 2015年12月27日
	 * 判断输入的数字字符串是否为整数
	 * 如："3.0","3"都判断为整数
	 * 若为整数，则返回true；否则，false。
	 * 思路：
	 * 1.判断是否有小数点
	 * 2.若有小数点，则判断小数部分是否为零，若为零，则返回true，否则返回false
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str){
		try {
//			int temp = Integer.parseInt(str);
			int index = str.indexOf(".");
			if (index == -1) {
				return true;
			}
			else {
				String[]strArray = str.split("\\.");//必须加上"\\"
				int fractionalPart = Integer.valueOf(strArray[1]);
				if (fractionalPart == 0) {
					return true;
				}
				else {
					return false;
				}
			}			
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 2015年12月27日
	 * 获得数字字符串的整数部分
	 * 输入：数字字符串，str
	 * 输出：数字字符串整数部分
	 * @param str
	 * @return
	 */
	public static int obtainIntegerPart(String str){
		int integerPart = 0;
		try {
			int index = str.indexOf(".");
			//说明为整数
			if (index == -1) {
				integerPart = Integer.parseInt(str);				
			}
			else {
				String[]strArray = str.split("\\.");//必须加上"\\"
				integerPart = Integer.valueOf(strArray[0]);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return integerPart;
	}
}
