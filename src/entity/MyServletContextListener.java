package entity;
import implement.RoadNetworkAnalysisImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import mapMatchingGPS.MainFunction;
import mapMatchingGPS.MapMatchAlgorithm;
import entity.*;

/**
* ��ʼ������
*/
public class MyServletContextListener implements ServletContextListener {
	private java.util.Timer timer = null; 
	public void contextInitialized(ServletContextEvent event) {
        System.out.println("��ʼ�����أ�");
        try {
//        	RoadNetworkAnalysisImpl.instance();
//          System.out.print("����·�����ݶ�ȡ������"+"\n");
        	
            System.out.print("��ʼ�����ݣ�" + '\n');
            MapMatchAlgorithm.instance();		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		System.out.print("���������ݣ�" + '\n');     
    }
	public void contextDestroyed(ServletContextEvent sce) {

	}
}