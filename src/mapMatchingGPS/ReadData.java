package mapMatchingGPS;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import utilityPackage.PubClass;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureWorkspace;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPointCollection;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;


public class ReadData {

	/*读取FileGeodatabase中的数据,点、线
	 * path:FileGeodatabase中的路径
	 * juncFileNameString:点文件名
	 * polylineFileNameString:线文件名*/
	
	public void readFileGeodatabaseShape(String path, String juncFileNameString, String polylineFileNameString,
			ArrayList<MapMatchNode>juncCollArrayList,ArrayList<MapMatchEdge>polylineCollArrayList){		
		try {	
			System.out.print("开始读路网数据:");
			//Step 1: Initialize the Java Componet Object Model (COM) Interop.
	        EngineInitializer.initializeEngine();
	        //Step 2: Initialize an ArcGIS license.
	        AoInitialize aoInit = new AoInitialize();
	        initializeArcGISLicenses(aoInit);
            IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
            IFeatureWorkspace pFeatureWorkspace = (IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			IFeatureClass pointFeatureClass = pFeatureWorkspace.openFeatureClass(juncFileNameString);		
		    int juncFeatureCount = pointFeatureClass.featureCount(null);
    	    IFeatureCursor juncFeatureCursor = pointFeatureClass.search(null, false);
    	    
    	    for (int i = 0; i < juncFeatureCount; i++) {
    	    	IFeature feature = juncFeatureCursor.nextFeature();
    	    	IGeometry geometry = feature.getShape();  
	 			IPoint juncPoint = (IPoint)geometry;	
	 			IFields nodeFields = new Fields();
	 			nodeFields = feature.getFields();
				int nodeFiedIndex = nodeFields.findField("NODEID");
				int nodeID = (Integer)feature.getValue(nodeFiedIndex);//节点ID
	 	    	//一定要实例化
	 			MapMatchNode node = new MapMatchNode();
	 			node.setNodeID(nodeID);//存储点的ID
	 			node.setX(juncPoint.getX());
	 			node.setY(juncPoint.getY());
	 			juncCollArrayList.add(node);
	 			System.out.println("读取点:" + nodeID + ":" + juncFeatureCount);
    		}  
    	    System.out.println("点读取结束" + '\n');
    	    
			//get SplitLine					
			IFeatureClass plineFeatureClass = pFeatureWorkspace.openFeatureClass(polylineFileNameString); 
			int lineCount = plineFeatureClass.featureCount(null);					
			//获得polylineFeature
			//存储ID以及polyline中点的集合
			IFeatureCursor pLineFeatureCursor = plineFeatureClass.search(null, false);
			for (int i = 0; i < lineCount; i++) {
				IFeature plineFeature = pLineFeatureCursor.nextFeature();
				IGeometry plineGeometry = plineFeature.getShape(); 
				IPolyline polyline = (IPolyline)plineGeometry;
				IPointCollection plinePoints = (IPointCollection)polyline;
				MapMatchNode beginPoint = new MapMatchNode();
				MapMatchNode endPoint = new MapMatchNode();
				ArrayList<MapMatchNode>tnodeArrayList = new ArrayList<MapMatchNode>();
				for (int p = 0; p < plinePoints.getPointCount(); p++) {
					IPoint tPoint = plinePoints.getPoint(p);
					MapMatchNode tNode = new MapMatchNode();
					tNode.setX(tPoint.getX());
					tNode.setY(tPoint.getY());
					tnodeArrayList.add(tNode);
					//设置路段起终点
					if (p == 0) {
						beginPoint.setX(tPoint.getX());
						beginPoint.setY(tPoint.getY());	
						//对应路段起点nodeID
						for (int j = 0; j < juncCollArrayList.size(); j++) {
							MapMatchNode node = juncCollArrayList.get(j);
							if (PubClass.isTheSameNode(beginPoint, node)) {
								beginPoint.setNodeID(node.getNodeID());
								break;
							}
						}
					}
					else if (p == plinePoints.getPointCount() - 1) {
						endPoint.setX(tPoint.getX());
						endPoint.setY(tPoint.getY());
						//对应路段终点nodeID
						for (int j = 0; j < juncCollArrayList.size(); j++) {
							MapMatchNode node = juncCollArrayList.get(j);
							if (PubClass.isTheSameNode(endPoint, node)) {
								endPoint.setNodeID(node.getNodeID());
								break;
							}
						}
					}
				}
				//获得道路名字信息				
				IFields plineFields = new Fields();
				plineFields = plineFeature.getFields();
				int nameFiedIndex = plineFields.findField("NAME");			
				String roadName = (String)plineFeature.getValue(nameFiedIndex);//路段名
				int linkIDFiedIndex = plineFields.findField("LINKID");
				int linkID = (Integer)plineFeature.getValue(linkIDFiedIndex);//路段编号
				MapMatchEdge tEdge = new MapMatchEdge();
				tEdge.setEdgeID(linkID);
				tEdge.setPointCollArrayList(tnodeArrayList);
				tEdge.setBeginPoint(beginPoint);
				tEdge.setEndPoint(endPoint);
				tEdge.setEdgeName(roadName);
				polylineCollArrayList.add(tEdge);
								
	 			System.out.println("读取线:" + linkID + ":" + lineCount);
			}	
			System.out.println("线读取结束" + '\n');
			System.out.print("结束读路网数据" + '\n');
    	} catch (Exception e) {
		    System.err.print(e.getMessage());
		    e.printStackTrace();
		    }		
	}
	
	private void initializeArcGISLicenses(AoInitialize aoInit){
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
	
	public void InitLiscense() {
		// Get the ArcGIS Engine runtime, if it is available
		String arcObjectsHome = "D:\\software\\ArcGIS\\Desktop10.1\\";

		// If no runtime is available, exit application gracefully
//		if (arcObjectsHome == null) {
//			System.err
//					.println("You must have the ArcGIS Engine Runtime installed in order to execute this application.");
//			System.err
//					.println("Install the product above, then re-run this application.");
//			System.err.println("Exiting execution of this application...");
//			System.exit(-1);
//		}

		// Obtain the relative path to the arcobjects.jar file
		String jarPath = arcObjectsHome + "java" + File.separator + "lib"
				+ File.separator + "arcobjects.jar";

		// Create a new file
		File jarFile = new File(jarPath);

		// Test for file existence
		if (!jarFile.exists()) {
			System.err
					.println("The arcobjects.jar was not found in the following location: "
							+ jarFile.getParent());
			System.err
					.println("Verify that arcobjects.jar can be located in the specified folder.");
			System.err
					.println("If not present, try uninstalling your ArcGIS software and reinstalling it.");
			System.err.println("Exiting execution of this application...");
			System.exit(-1);
		}

		// Helps load classes and resources from a search path of URLs
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL",
					new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { jarFile.toURI().toURL() });
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			System.err
					.println("Could not add arcobjects.jar to system classloader");
			System.err.println("Exiting execution of this application...");
			System.exit(-1);
		}

	}
}
