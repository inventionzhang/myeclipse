package org.lmars.network.implement;

import java.io.IOException;

import org.lmars.network.util.PropertiesUtilJAR;


import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;

public class SerializationTest {
	public static void main(String[] args) throws AutomationException, IOException{
		//Step 1: Initialize the Java Componet Object Model (COM) Interop.
        EngineInitializer.initializeEngine();
        //Step 2: Initialize an ArcGIS license.
        AoInitialize aoInit = new AoInitialize();
        initializeArcGISLicenses(aoInit);
        try {
        	System.out.println("初始化加载");
        	System.out.println("开始读路网数据");
            RoadNetworkAnalysisImpl roadNetworkAnaly = RoadNetworkAnalysisImpl.instance();
            System.out.println("结束读路网数据");
            String roadNetworkName = PropertiesUtilJAR.getProperties("roadNetworkJiangAn1");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[0];//路网存储文件名
            roadNetworkAnaly.getAllRoadNetworkData(); 
            System.out.print("开始保存数据");
            roadNetworkAnaly.saveRoadFile(fileName);
            System.out.print("数据保存成功");
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
