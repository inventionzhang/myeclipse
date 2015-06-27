package entity;
import implement.RoadNetworkAnalysisImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import mapMatchingGPS.MainFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import entity.*;

/**
* 初始化加载
*/
public class MyServletContextListener implements ServletContextListener {
	private java.util.Timer timer = null; 
	public void contextInitialized(ServletContextEvent event) {
        System.out.println("初始化加载：");
        try {
//        	RoadNetworkAnalysisImpl.instance();
//          System.out.print("所有路网数据读取结束："+"\n");
        	
            System.out.print("开始读数据：" + '\n');
            MapMatchAlgorithm.instance();		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		System.out.print("结束读数据：" + '\n');     
    }
	public void contextDestroyed(ServletContextEvent sce) {

	}
}