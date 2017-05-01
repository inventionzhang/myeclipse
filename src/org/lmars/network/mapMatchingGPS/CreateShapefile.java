package org.lmars.network.mapMatchingGPS;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.lmars.network.entity.Edge;
import org.lmars.network.entity.Node;
import org.lmars.network.implement.RoadNetworkAnalysisImpl;

import com.esri.arcgis.datasourcesfile.ShapefileWorkspaceFactory;
import com.esri.arcgis.geodatabase.Feature;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.FeatureCursor;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.GeometryDef;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFeatureType;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geometry.GeometryEnvironment;
import com.esri.arcgis.geometry.IGeographicCoordinateSystem;
import com.esri.arcgis.geometry.ILine;
import com.esri.arcgis.geometry.ISegment;
import com.esri.arcgis.geometry.Line;
import com.esri.arcgis.geometry.Path;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.SpatialReferenceEnvironment;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.geometry.esriSRGeoCSType;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.Cleaner;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;


/**********************************
 * 创建shapefile文件
 * ********************************/
public class CreateShapefile {

	/*create a shapefile to add polyline feature to
     * */
    private void createRoadNetworkShapefile(String outPath,String outName,ArrayList<Edge>convertRoadNetworkArrayList){
    	try{
  	        //Initialize engine console application
  	        EngineInitializer.initializeEngine();  	      
  	        //Initialize ArcGIS license
  	        AoInitialize aoInit = new AoInitialize();
  	        initializeArcGISLicenses(aoInit);
  	      
  	        boolean isFeatureBufferUsed = true;	      
	        File shapefileDir = new File(outPath);
	        shapefileDir.mkdir();
	      
	        File outShapefileFile = new File(shapefileDir, outName);
	        if (outShapefileFile.exists()) {
	            System.out.println("Output datafile already exists: " + outShapefileFile.getAbsolutePath());
	            System.out.println("Delete it (plus .shx and .dbf files) and rerun");
	            System.exit(-1);
	        }	      
		    File textFile = new File(outPath);
		    if (!textFile.canRead()) {
		        System.out.println("Cannot read input text file: " + textFile.getAbsolutePath());
		        System.out.println("Exiting...");
		        System.exit(-1);
		    }	   
		    ArrayList<Edge> edgesArrayList = new ArrayList<Edge>();
		    edgesArrayList = convertRoadNetworkArrayList;
		    CreateShapefile createShapefile = new CreateShapefile();
	        FeatureClass featureClass = createShapefile.createPolylineShapefile(outPath, outName);
	        createShapefile.addPolyline(outPath, featureClass, edgesArrayList, isFeatureBufferUsed);
	        System.out.println("Created " + outShapefileFile.getAbsolutePath());	            
	        //Ensure any ESRI libraries are unloaded in the correct order
	        aoInit.shutdown();
  	       
    	}
    	catch (Exception e) {   		
			// TODO: handle exception
    		System.out.print(e.getMessage());
			e.printStackTrace();
		}   	
    }
    
    /**
	  * Checks to see if an ArcGIS Engine Runtime license or an Basic License
	  * is available. If so, then the appropriate ArcGIS License is initialized.
	  * 
	  * @param aoInit The AoInitialize object instantiated in the main method.
      */
    private static void initializeArcGISLicenses(AoInitialize aoInit) {
	    try {
	      if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) 
	          == esriLicenseStatus.esriLicenseAvailable)
	        aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
	      else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeBasic) 
	          == esriLicenseStatus.esriLicenseAvailable)
	        aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic);
	      else{
	        System.err.println("Could not initialize an Engine or Basic License. Exiting application.");
	        System.exit(-1);
	      }
	    } catch (Exception e) {e.printStackTrace();}
	  }
    
    /**
	   * Create a shapefile to add point features to.
	   *
	   * @param shapefilePath path to the shapefile being created.
	   * @param shapefileName name of the shapefile being created.
	   * @return FeatureClass the feature class to which polyline may be added.
	   * @throws IOException when the shapefile cannot be created.
	   */
	private FeatureClass createPolylineShapefile(String shapefilePath, String shapefileName) throws IOException {
	    try {
	        String geometryShapeFieldName = "Shape";	      
	        // Get a feature workspace from the specified shapefile location
	        ShapefileWorkspaceFactory shapefileWorkspaceFactory = new ShapefileWorkspaceFactory();
	        Workspace workspace = (Workspace) shapefileWorkspaceFactory.openFromFile(shapefilePath, 0);
	        
	        // Create a GeometryDef object to hold geometry information
	        GeometryDef geometryDef = new GeometryDef();
	        geometryDef.setGeometryType(esriGeometryType.esriGeometryPolyline);

	        // Create spatial reference information, and add it to the geometry definition
	        SpatialReferenceEnvironment spatialReferenceEnvironment = new SpatialReferenceEnvironment();
	        IGeographicCoordinateSystem geographicCoordinateSystem = spatialReferenceEnvironment.createGeographicCoordinateSystem(
	            esriSRGeoCSType.esriSRGeoCS_WGS1984);	      
	        geometryDef.setSpatialReferenceByRef(geographicCoordinateSystem);

	        // Create a geometry shape field and add the geometry definition to it.
	        Field geometryShapeField = new Field();
	        geometryShapeField.setName(geometryShapeFieldName);
	        geometryShapeField.setType(esriFieldType.esriFieldTypeGeometry);
	        geometryShapeField.setGeometryDefByRef(geometryDef);

	        // Create point coordinate fields: Longitude, Latitude, and Value.
	        // Note: We need not explicitly add latitude and longitude fields because
	        // the shape field holds this type of data.  It is sometimes useful to
	        // have these fields in the table, however.
	        Field roadName = new Field();
//	        lngCoordField.setLength(30);
	        roadName.setName("Name");
	        roadName.setType(esriFieldType.esriFieldTypeString);

	        // Create a Fields object and add the fields to it.
	        Fields fields = new Fields();
	        fields.addField(roadName);
	        fields.addField(geometryShapeField);

	        // Create a feature class defined by the shapefile name and the defined fields.
	        // The creation of this feature class also causes a shapefile to be created.
	        FeatureClass featureClass = new FeatureClass(workspace.createFeatureClass(shapefileName, fields, null, null,
	            esriFeatureType.esriFTSimple, geometryShapeFieldName, ""));

	        // Return the feature class for adding points.
	        return featureClass;
	    }catch (IOException e) {
	        System.out.println("Could not create feature class for shapefile named: " + shapefileName);
	        throw e;
	    }
	}
    
	 /**
	   * Add the polyline to the shapefile.
	   * @param filePath path for the text file containing point coordinates.
	   * @param featureClass the part of the shapefile to add point features to.
	   * @param edgesArraylist containing information of roadNetwork
	   * @param useFeatureBuffer whether to add points to a feature buffer or to individual features.
	   * @throws IOException if cannot add points to the feature class.
	   */
	private void addPolyline(String filePath, FeatureClass featureClass, ArrayList<Edge> edgesArrayList, boolean useFeatureBuffer) throws IOException 
	{
	    System.out.println("Creating Point features...");
	    try {
	        if (useFeatureBuffer) {
	            // Get an insert cursor and a feature buffer for the feature class, so we can add features
	            // in a buffered manner.
	            FeatureCursor featureCursor = new FeatureCursor(featureClass.IFeatureClass_insert(true));
	            Feature featureBuffer = (Feature) featureClass.createFeatureBuffer();

	            // Get the column indicies for the coordinate fields to be used
	            // when setting values fort these fields.
	            Fields fields = (Fields) featureBuffer.getFields();
	            int nameIndex = fields.findField("Name");
	            
	            int featureCount = 0;
	            //取得每一条边的信息写入shapefile文件中
	            for (int i = 0; i < edgesArrayList.size(); i++) {
	            	ArrayList<Node> pointCollArrayList = new ArrayList<Node>();
	            	pointCollArrayList = edgesArrayList.get(i).pointCollArrayList;
	            	ArrayList<Point> esriPointCollArrayList = new ArrayList<Point>();//ESRI的点      
	            	for (int j = 0; j < pointCollArrayList.size(); j++) {
	            		Node tNode = pointCollArrayList.get(j);
	            		double lng = tNode.x;
			            double lat = tNode.y;
			            // Create a new point and set its X,Y coordinates from the longitude,latitude values.
			            Point point = new Point();
			            point.setX(lng);
			            point.setY(lat);
			            esriPointCollArrayList.add(point);
	            	}
	            	
	            	// 创建IPolyline对象：
	            	Polyline pPolyline = new Polyline();
	            	ArrayList<Path> pathArrayList = new ArrayList<Path>();
	            	for (int k = 0; k < esriPointCollArrayList.size()-1; k++) {
	            		
//			            // *********************************************************
//			            // THE SPATIAL REFERENCE SHOULD BE SET HERE ON THE POLYLINE
//			            // Here the spatial reference is created in memory but could also come from various sources:
//			            // IMap, IGeodataset, IGeometry etc...
//			            UnknownCoordinateSystem pspref = new UnknownCoordinateSystem(); 
//
//			            // Set the false origin and units.
//			            // The XYUnits value is equivalent to the precision specified when creating a feature class
//			            pspref.setFalseOriginAndUnits(-10000, -10000, 100000);
//			            pPolyline.setSpatialReferenceByRef(pspref);
			              
			            Point froPoint = esriPointCollArrayList.get(k);
		            	Point toPoint = esriPointCollArrayList.get(k+1);
				        ILine pLine = new Line();
				        // 设置LIne对象的起始终止点
				        pLine.putCoords(froPoint, toPoint);
				        Path pPath = new Path();
				        pPath.addSegment((ISegment)pLine, null, null);
				        pathArrayList.add(pPath);
	            	}
	            	Path[] pathColl = new Path[pathArrayList.size()];
	            	for (int p = 0; p < pathArrayList.size(); p++) {
						pathColl[p] = pathArrayList.get(p);
					}
				   
			        // Add all the paths to the polyline using AddGeometries method of GeometryEnvironment
				    GeometryEnvironment gBridge = new GeometryEnvironment();
				    gBridge.addGeometries(pPolyline, pathColl);
		            
		            // Add the point's geometry to the feature buffer's shape field.
		            featureBuffer.setShapeByRef(pPolyline);
		            featureBuffer.setValue(nameIndex, "");
		            // Add the feature at the current/cursor location in the shapefile
		            featureCursor.insertFeature(featureBuffer);		
		            // Flush the insert cursor every 100 features
		            if ((featureCount+1) % 100 == 0) {
			            System.out.println("Flushing...");
			            featureCursor.flush();
		            }
				}
            	
	            // Flush the insert cursor at the end of the file
		        featureCursor.flush();
		        Cleaner.release(featureCursor);
	            
	            
	            
	            
//	            for (String textLine = bufferedReader.readLine(); textLine != null; textLine = bufferedReader.readLine(), featureCount++)
//	            {
//		            String[] tokens = textLine.split(", ");
//		            double lng = Double.parseDouble(tokens[0]);
//		            double lat = Double.parseDouble(tokens[1]);
//		            double val = Double.parseDouble(tokens[2]);
//		            System.out.println("Longitude: " + lng + " Latitude: " + lat + " Value: " + val);
//
//		            // Create a new point and set its X,Y coordinates from the longitude,latitude values.
//		            Point point = new Point();
//		            point.setX(lng);
//		            point.setY(lat);
//		            
//		            // Add the point's geometry to the feature buffer's shape field.
//		            featureBuffer.setShapeByRef(point);
//	
//		            // Add longitude, latitude, and value values to their individual fields
//		            featureBuffer.setValue(lngIndex, new Double(lng));
//		            featureBuffer.setValue(latIndex, new Double(lat));
//		            featureBuffer.setValue(valIndex, new Double(val));
//	
//	                // Add the feature at the current/cursor location in the shapefile
//		            featureCursor.insertFeature(featureBuffer);
//	
//		            // Flush the insert cursor every 100 features
//		            if ((featureCount+1) % 100 == 0) {
//			            System.out.println("Flushing...");
//			            featureCursor.flush();
//		            }
//	            }
		        
		        
	        }
	    }catch (IOException e) {
	        System.out.println("Could not add points to shapefile");
	        throw e;
	    }
	}	
       
    /**
	   * Create a shapefile to add point features to.
	   *
	   * @param shapefilePath path to the shapefile being created.
	   * @param shapefileName name of the shapefile being created.
	   * @return FeatureClass the feature class to which points may be added.
	   * @throws IOException when the shapefile cannot be created.
	   */
	private FeatureClass createPointShapefile(String shapefilePath, String shapefileName) throws IOException {
	    try {
	        String geometryShapeFieldName = "Shape";	      
	        // Get a feature workspace from the specified shapefile location
	        ShapefileWorkspaceFactory shapefileWorkspaceFactory = new ShapefileWorkspaceFactory();
	        Workspace workspace = (Workspace) shapefileWorkspaceFactory.openFromFile(shapefilePath, 0);
	        
	        // Create a GeometryDef object to hold geometry information
	        GeometryDef geometryDef = new GeometryDef();
	        geometryDef.setGeometryType(esriGeometryType.esriGeometryPoint);

	        // Create spatial reference information, and add it to the geometry definition
	        SpatialReferenceEnvironment spatialReferenceEnvironment = new SpatialReferenceEnvironment();
	        IGeographicCoordinateSystem geographicCoordinateSystem = spatialReferenceEnvironment.createGeographicCoordinateSystem(
	            esriSRGeoCSType.esriSRGeoCS_NAD1983);	      
	        geometryDef.setSpatialReferenceByRef(geographicCoordinateSystem);

	        // Create a geometry shape field and add the geometry definition to it.
	        Field geometryShapeField = new Field();
	        geometryShapeField.setName(geometryShapeFieldName);
	        geometryShapeField.setType(esriFieldType.esriFieldTypeGeometry);
	        geometryShapeField.setGeometryDefByRef(geometryDef);

	        // Create point coordinate fields: Longitude, Latitude, and Value.
	        // Note: We need not explicitly add latitude and longitude fields because
	        // the shape field holds this type of data.  It is sometimes useful to
	        // have these fields in the table, however.
	        Field lngCoordField = new Field();
	        lngCoordField.setLength(30);
	        lngCoordField.setName("Longitude");
	        lngCoordField.setType(esriFieldType.esriFieldTypeDouble);

	        Field latCoordField = new Field();
	        latCoordField.setLength(30);
	        latCoordField.setName("Latitude");
	        latCoordField.setType(esriFieldType.esriFieldTypeDouble);

	        Field valCoordField = new Field();
	        valCoordField.setLength(30);
	        valCoordField.setName("Value");
	        valCoordField.setType(esriFieldType.esriFieldTypeDouble);

	        // Create a Fields object and add the fields to it.
	        Fields fields = new Fields();
	        fields.addField(lngCoordField);
	        fields.addField(latCoordField);
	        fields.addField(valCoordField);
	        fields.addField(geometryShapeField);

	        // Create a feature class defined by the shapefile name and the defined fields.
	        // The creation of this feature class also causes a shapefile to be created.
	        FeatureClass featureClass = new FeatureClass(workspace.createFeatureClass(shapefileName, fields, null, null,
	            esriFeatureType.esriFTSimple, geometryShapeFieldName, ""));

	        // Return the feature class for adding points.
	        return featureClass;
	    }catch (IOException e) {
	        System.out.println("Could not create feature class for shapefile named: " + shapefileName);
	        throw e;
	    }
	}  
	
	 /**
	   * Add the points from the text file to the shapefile.
	   * @param textFilePath path for the text file containing point coordinates.
	   * @param featureClass the part of the shapefile to add point features to.
	   * @param useFeatureBuffer whether to add points to a feature buffer or to individual features.
	   * @throws IOException if cannot add points to the feature class.
	   */
	private void addPoints(String filePath, FeatureClass featureClass, boolean useFeatureBuffer) throws IOException 
	{
	    System.out.println("Creating Point features...");
	    try {
	        if (useFeatureBuffer) {
	            // Get an insert cursor and a feature buffer for the feature class, so we can add features
	            // in a buffered manner.
	            FeatureCursor featureCursor = new FeatureCursor(featureClass.IFeatureClass_insert(true));
	            Feature featureBuffer = (Feature) featureClass.createFeatureBuffer();

	            // Get the column indicies for the coordinate fields to be used
	            // when setting values fort these fields.
	            Fields fields = (Fields) featureBuffer.getFields();
	            int lngIndex = fields.findField("Longitude");
	            int latIndex = fields.findField("Latitude");
	            int valIndex = fields.findField("Value");
	       
	            // Read in a line at a time, parsing the coordinate values for each point
	            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
	            int featureCount = 0;
	            for (String textLine = bufferedReader.readLine(); textLine != null; textLine = bufferedReader.readLine(), featureCount++)
	            {
		            String[] tokens = textLine.split(", ");
		            double lng = Double.parseDouble(tokens[0]);
		            double lat = Double.parseDouble(tokens[1]);
		            double val = Double.parseDouble(tokens[2]);
		            System.out.println("Longitude: " + lng + " Latitude: " + lat + " Value: " + val);

		            // Create a new point and set its X,Y coordinates from the longitude,latitude values.
		            Point point = new Point();
		            point.setX(lng);
		            point.setY(lat);
		            
		            // Add the point's geometry to the feature buffer's shape field.
		            featureBuffer.setShapeByRef(point);
	
		            // Add longitude, latitude, and value values to their individual fields
		            featureBuffer.setValue(lngIndex, new Double(lng));
		            featureBuffer.setValue(latIndex, new Double(lat));
		            featureBuffer.setValue(valIndex, new Double(val));
	
	                // Add the feature at the current/cursor location in the shapefile
		            featureCursor.insertFeature(featureBuffer);
	
		            // Flush the insert cursor every 100 features
		            if ((featureCount+1) % 100 == 0) {
			            System.out.println("Flushing...");
			            featureCursor.flush();
		            }
	            }
		        // Flush the insert cursor at the end of the file
		        featureCursor.flush();
		        Cleaner.release(featureCursor);
	      }else {
	          //Use createFeature() and store()
	          //Get the column indicies for the coordinate fields to be used
	          //when setting values fort these fields.
	          Fields fields = (Fields) featureClass.getFields();
	          int lngIndex = fields.findField("Longitude");
	          int latIndex = fields.findField("Latitude");
	          int valIndex = fields.findField("Value");

	          //Read in a line at a time, parsing the coordinate values for each point
	          BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
	          for (String textLine = bufferedReader.readLine(); textLine != null; textLine = bufferedReader.readLine()) 
	          {
		          String[] tokens = textLine.split(", ");
		          double lng = Double.parseDouble(tokens[0]);
		          double lat = Double.parseDouble(tokens[1]);
		          double val = Double.parseDouble(tokens[2]);	
		          System.out.println("Longitude: " + lng + " Latitude: " + lat + " Value: " + val);
	
		          //Create a new point and set its X,Y coordinates from the longitude,latitude values.
		          Point point = new Point();
		          point.setX(lng);
		          point.setY(lat);
	
		          //Create a new feature and add the point's geometry to it
		          Feature feature = (Feature) featureClass.createFeature();
		          feature.setShapeByRef(point);
	
		          //Add longitude, latitude, and value values to their individual fields
		          feature.setValue(lngIndex, new Double(lng));
		          feature.setValue(latIndex, new Double(lat));
		          feature.setValue(valIndex, new Double(val));
	
		          //Persist changes by calling store()
		          feature.store();
	          }
	      }
	    }catch (IOException e) {
	        System.out.println("Could not add points to shapefile");
	        throw e;
	    }
	}
}
