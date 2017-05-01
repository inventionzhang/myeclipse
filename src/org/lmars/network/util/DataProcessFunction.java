package org.lmars.network.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class DataProcessFunction {

	/**
	 * 2016-1-22
	 * 数据按照一周中每一天（周一、周二、...,周日）进行排序
	 * @param infosArrayList	存储待分类信息
	 * @param index	第几个索引为时间信息	时间信息格式为"2014-06-01 00:00:00"
	 * @param infosMap	以1到7为索引，分别存储周一、周二、……周日的信息
	 */
	public static void classifyByWeekdays(ArrayList<String> infosArrayList, int index, 
			Map<Integer, List<String>> infosMap){
		try {
			System.out.print("数据按照每周进行分类!" + '\n');
			if (infosArrayList != null) {
				List<String> linkmondayArrayList = new ArrayList<String>();//周一
				List<String> linktuesdayArrayList = new ArrayList<String>();//周二
				List<String> linkwednesdayArrayList = new ArrayList<String>();//周三
				List<String> linkthursdayArrayList = new ArrayList<String>();
				List<String> linkfridayArrayList = new ArrayList<String>();
				List<String> linksaturdaydayArrayList = new ArrayList<String>();
				List<String> linksundayArrayList = new ArrayList<String>();
				for (int i = 0; i < infosArrayList.size(); i++) {
					String str = infosArrayList.get(i);
					String[]tempArrayStr = str.split(",");
					String tempDateStr = tempArrayStr[index].substring(0,10);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
					Date date = simpleDateFormat.parse(tempDateStr); 
				    Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
				    	linkmondayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY){
				    	linktuesdayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
				    	linkwednesdayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY){
				    	linkthursdayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
				    	linkfridayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
				    	linksaturdaydayArrayList.add(str);
					}
				    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				    	linksundayArrayList.add(str);
					}
				}
				infosMap.put(1, linkmondayArrayList);
				infosMap.put(2, linktuesdayArrayList);
				infosMap.put(3, linkwednesdayArrayList);
				infosMap.put(4, linkthursdayArrayList);
				infosMap.put(5, linkfridayArrayList);
				infosMap.put(6, linksaturdaydayArrayList);
				infosMap.put(7, linksundayArrayList);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		System.out.print("数据分类结束!" + '\n');
	}
	
	
	
	
	
	
	
}
