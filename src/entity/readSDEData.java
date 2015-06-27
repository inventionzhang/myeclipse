package entity;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.datasourcesGDB.SdeWorkspaceFactory;
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
import com.esri.arcgis.geometry.Ring;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerContext;
import com.esri.arcgis.server.IServerObjectManager;
import com.esri.arcgis.server.ServerConnection;
import com.esri.arcgis.system.IPropertySet;
import com.esri.arcgis.system.PropertySet;
import com.esri.arcgis.system.ServerInitializer;

import implement.*;



public class readSDEData {	
	/*juncArrayList:存储节点信息*/    
	public void readData(String pcsName,String junctionData,String splitLineData,String polygonData,ArrayList<surface>surfaceArrayList,
			ArrayList<Node>juncArrayList,ArrayList<Edge>polylineCollArrayList,ArrayList<roadName>roadNameArrayList)throws AutomationException, IOException
    {
		String junctionDatatype=junctionData;
		String splitLineDatatype=splitLineData;
		String polygonDatatype=polygonData;
    	try {			
    		String path = "E:\\faming\\esri\\roadNetwork.gdb";
            IWorkspaceFactory gdbFileWorkspaceFactory = new FileGDBWorkspaceFactory();
            IFeatureWorkspace pFeatureWorkspace=(IFeatureWorkspace)gdbFileWorkspaceFactory.openFromFile(path, 0);
			IFeatureClass pointFeatureClass = pFeatureWorkspace.openFeatureClass(junctionDatatype);		
		    int juncFeatureCount=pointFeatureClass.featureCount(null);
    	    IFeatureCursor juncFeatureCursor=pointFeatureClass.search(null, false); 	   
    	    
    	    for (int i = 0; i < juncFeatureCount; i++) {
    	    	IFeature feature=juncFeatureCursor.nextFeature();
    	    	IGeometry geometry=feature.getShape();  
	 			IPoint juncPoint=(IPoint)geometry;	 			
	 	    	//一定要实例化
	 			Node node=new Node();
	 			node.setEID(feature.getOID());//存储点的ID
	 			node.setX(juncPoint.getX());
	 			node.setY(juncPoint.getY()); 	 			
	 			juncArrayList.add(node);
	 			System.out.println(pcsName+","+"读取点:" + i + ":" + juncFeatureCount);
    		}  
	    	      	      	    
	    	//get polygon    	    
    	    IFeatureClass polygonFeatureClass = pFeatureWorkspace.openFeatureClass(polygonDatatype);
    		int featureCount=polygonFeatureClass.featureCount(null);	
    		IFeatureCursor polygonFeatureCursor=polygonFeatureClass.search(null, false);
			for (int i = 0; i < featureCount; i++) {						
				
				IFeature feature=polygonFeatureCursor.nextFeature();
				IGeometry geometry=feature.getShape();
				IPolygon polygon=(IPolygon)geometry;
				
				int pOID=feature.getOID();
				surface tempSurface=new surface();
				tempSurface.setPolygonID(pOID);
				IPointCollection points = (IPointCollection)polygon;
				tempSurface.setPoints(points);
				surfaceArrayList.add(tempSurface);		
				
	 			System.out.println(pcsName+","+"读取面:" + i + ":" + featureCount);
			}										   	    		
				
			//get SplitLine					
			IFeatureClass plineFeatureClass = pFeatureWorkspace.openFeatureClass(splitLineDatatype); 
			int lineCount=plineFeatureClass.featureCount(null);		
			
			//获得polylineFeature
			//存储ID以及polyline中点的集合
			IFeatureCursor pLineFeatureCursor=plineFeatureClass.search(null, false);
			for (int i = 0; i < lineCount; i++) {
				IFeature plineFeature=pLineFeatureCursor.nextFeature();				
				IGeometry plineGeometry=plineFeature.getShape(); 
				IPolyline polyline=(IPolyline)plineGeometry;
				IPointCollection plinePoints= (IPointCollection)polyline;
				ArrayList<Node>tnodeArrayList=new ArrayList<Node>();
				for (int p = 0; p < plinePoints.getPointCount(); p++) {
					IPoint tPoint=plinePoints.getPoint(p);
					Node tNode=new Node();
					tNode.setX(tPoint.getX());
					tNode.setY(tPoint.getY());
					tnodeArrayList.add(tNode);
				}				
				Edge tEdge=new Edge();
				tEdge.setEdgeID(plineFeature.getOID());
				tEdge.setPointCollArrayList(tnodeArrayList);
				polylineCollArrayList.add(tEdge);
				
				//获得道路名字信息				
				IFields fields=new Fields();
				fields=plineFeature.getFields();
				int fiedIndex=fields.findField("NAME");			
				String roadName=(String)plineFeature.getValue(fiedIndex);//路段名
				int roadID=plineFeature.getOID();//路段ID
				
				IPoint froPoint=polyline.getFromPoint();
				retuNode froNode = new retuNode();
				froNode.L = froPoint.getX();//经度
				froNode.B = froPoint.getY();//维度
				IPoint toPoint = polyline.getToPoint();
				retuNode toNode = new retuNode();
				toNode.L = toPoint.getX();
				toNode.B = toPoint.getY();				
				roadName roadInfor=new roadName();//道路信息
				roadInfor.setRoadID(roadID);
				roadInfor.setRoadName(roadName);
				roadInfor.setFroPoint(froNode);
				roadInfor.setToPoint(toNode);
				roadNameArrayList.add(roadInfor);
								
	 			System.out.println(pcsName+","+"读取线:"+ roadName +"," + i + ":" + lineCount);
			}	
			System.out.print(pcsName+","+"读数据成功");
    	} catch (Exception e) {
		    System.err.print(e.getMessage());
		    e.printStackTrace();
		    System.out.print(pcsName+","+"读数据失败"+"\n");
		    System.out.print(pcsName+","+"重新读数据"+"\n");
		    //重新分配内存
		    juncArrayList=new ArrayList<Node>();
		    polylineCollArrayList=new ArrayList<Edge>();
		    surfaceArrayList=new ArrayList<surface>();
		    roadNameArrayList = new ArrayList<roadName>();
		    RoadNetworkAnalysisImpl.instance().allJuncArraylistMap.remove(pcsName);
		    RoadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.remove(pcsName);
		    RoadNetworkAnalysisImpl.instance().allSurfaceArrayListMap.remove(pcsName);
		    RoadNetworkAnalysisImpl.instance().allRoadNameArrayMap.remove(pcsName);
		    
		    RoadNetworkAnalysisImpl.instance().allJuncArraylistMap.put(pcsName, juncArrayList);
		    RoadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.put(pcsName, polylineCollArrayList);
		    RoadNetworkAnalysisImpl.instance().allSurfaceArrayListMap.put(pcsName, surfaceArrayList);
		    RoadNetworkAnalysisImpl.instance().allRoadNameArrayMap.put(pcsName,roadNameArrayList);
		    readData(pcsName,junctionData,splitLineData, polygonData,surfaceArrayList, juncArrayList, polylineCollArrayList, roadNameArrayList);			
	    }
    	finally{
    		
    	}
    }
}
  
