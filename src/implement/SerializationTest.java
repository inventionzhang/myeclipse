package implement;

import java.io.IOException;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;
import entity.PropertiesUtilJAR;

public class SerializationTest {
	public static void main(String[] args) throws AutomationException, IOException{
		//Step 1: Initialize the Java Componet Object Model (COM) Interop.
        EngineInitializer.initializeEngine();
        //Step 2: Initialize an ArcGIS license.
        AoInitialize aoInit = new AoInitialize();
        initializeArcGISLicenses(aoInit);
        try {
        	System.out.println("��ʼ������");
        	System.out.println("��ʼ��·������");
            RoadNetworkAnalysisImpl roadNetworkAnaly = RoadNetworkAnalysisImpl.instance();
            System.out.println("������·������");
            String roadNetworkName = PropertiesUtilJAR.getProperties("roadNetworkJiangAn1");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[0];//·���洢�ļ���
            roadNetworkAnaly.getAllRoadNetworkData(); 
            System.out.print("��ʼ��������");
            roadNetworkAnaly.saveRoadFile(fileName);
            System.out.print("���ݱ���ɹ�");
//            roadNetworkAnaly.readRoadFile(fileName);   	    
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
		
	}
	
	static void initializeArcGISLicenses(AoInitialize aoInit){
        try{
            if (aoInit.isProductCodeAvailable
                (esriLicenseProductCode.esriLicenseProductCodeBasic) ==
                esriLicenseStatus.esriLicenseAvailable){
                aoInit.initialize
                    (esriLicenseProductCode.esriLicenseProductCodeBasic);
            }
            else if (aoInit.isProductCodeAvailable
                (esriLicenseProductCode.esriLicenseProductCodeBasic) ==
                esriLicenseStatus.esriLicenseAvailable){
                aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic)
                    ;
            }
            else{
                System.err.println(
                    "Engine Runtime or Desktop Basic license not initialized.");
                System.err.println("Exiting application.");
                System.exit( - 1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
	}

}
