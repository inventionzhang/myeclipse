package org.lmars.network.implement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.lmars.network.entity.*;
import org.lmars.network.util.PropertiesUtil;
import org.lmars.network.util.PropertiesUtilJAR;
import org.omg.CORBA.PUBLIC_MEMBER;


import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureWorkspace;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPointCollection;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerObjectManager;
import com.esri.arcgis.server.ServerConnection;
import com.esri.arcgis.system.ServerInitializer;
import com.sun.org.apache.bcel.internal.generic.NEW;


public class RoadNetworkAnalysisImpl {
	
	private static RoadNetworkAnalysisImpl gInstance= null; 
	public static RoadNetworkAnalysisImpl instance() {
		if(gInstance == null){
			gInstance = new RoadNetworkAnalysisImpl();
			//初始化工作
			String roadNetworkName = PropertiesUtilJAR.getProperties("roadNetworkJiangAn");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[0];//路网存储文件名
			gInstance.readRoadFile(fileName);
		}
		return gInstance;
	}
	public boolean isOK = false;
    /*存储所有路网数据
     *存储派出所名以及所对应的路网数据*/
    public Map<String, ArrayList<Node>> allJuncArraylistMap=new HashMap<String, ArrayList<Node>>();//存储节点ID以及坐标
    public Map<String, Map<Integer,ArrayList<Edge>>> allRelationEdgesMap=new HashMap<String, Map<Integer,ArrayList<Edge>>>();//存储节点ID以及相邻边
    public Map<String, ArrayList<Node>> allNodesArrayListMap=new HashMap<String, ArrayList<Node>>();//存储节点拓扑关系
    public Map<String, ArrayList<Edge>> allPolylineCollArraylistMap=new HashMap<String, ArrayList<Edge>>();//存储polyline的ID以及polyline中的点
    public Map<String, ArrayList<surface>> allSurfaceArrayListMap=new HashMap<String, ArrayList<surface>>();//存储面
    public Map<Integer, ArrayList<String>> roadNetworkNameMap=new HashMap<Integer, ArrayList<String>>();//存储派出所配置文件名字
    public Map<String, ArrayList<roadName>>allRoadNameArrayMap=new HashMap<String, ArrayList<roadName>>();//存储所有道路名
    
    public boolean readRoadFile(String filename){
    	
    	try{   		
    		ClassLoader loader = RoadNetworkStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();    //获得父类加载器的引用  
    		}  
    		System.out.println(loader);
    		
	        System.out.println(Thread.currentThread().getContextClassLoader());
    		RoadNetworkStorage s = new RoadNetworkStorage();
    		File f = new File(filename);
			if(!f.exists()){
				return false;
			}
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
	        s = (RoadNetworkStorage)oin.readObject();	   
	        oin.close();
	        
	        this.allJuncArraylistMap = s.allJuncArraylistMap;
	        this.allRelationEdgesMap = s.allRelationEdgesMap;
	        this.allNodesArrayListMap = s.allNodesArrayListMap;
	        this.allPolylineCollArraylistMap = s.allPolylineCollArraylistMap;
	        this.allSurfaceArrayListMap = s.allSurfaceArrayListMap;
	        this.roadNetworkNameMap = s.roadNetworkNameMap;
	        this.allRoadNameArrayMap=s.allRoadNameArrayMap;
    		
	        isOK = true;
    		return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		isOK = false;
    		return false;
    	}
    }
    
    public boolean saveRoadFile(String filename){
      	
    	try{
    		RoadNetworkStorage s = new RoadNetworkStorage();
    		s.allJuncArraylistMap = this.allJuncArraylistMap;
    		s.allRelationEdgesMap = this.allRelationEdgesMap;
    		s.allNodesArrayListMap = this.allNodesArrayListMap;
    		s.allPolylineCollArraylistMap = this.allPolylineCollArraylistMap;
    		s.allSurfaceArrayListMap = this.allSurfaceArrayListMap;
    		s.roadNetworkNameMap = this.roadNetworkNameMap;
    		s.allRoadNameArrayMap=this.allRoadNameArrayMap;
    		
    		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    		out.writeObject(s);
    	    out.close();
    	    return true;

    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
    
    public void getRoadNetworkNameMap(){
    	try {
			String roadNetworkName = PropertiesUtilJAR.getProperties("roadNetworkJiangAn1");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			int pcsCount=roadNetworkNamecoll.length/5;
			ArrayList<String> pcsNameArrayList=new ArrayList<String>();
			ArrayList<String>rnwArrayList=new ArrayList<String>();
			if(roadNetworkNamecoll!=null)
		    {	
				for (int j = 1; j <=4; j++) {					
					rnwArrayList.add(roadNetworkNamecoll[j]);					
				}					
				roadNetworkNameMap.put(1, rnwArrayList);						
		    }						
		}
		catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
		}
    }       
    
    //获得所有路网数据
    public void getAllRoadNetworkData() {
    	getRoadNetworkNameMap();
    	List<ReadRoadDataThread> allThreads = new ArrayList<ReadRoadDataThread>();
    	if (roadNetworkNameMap.size()!=0) {
			for (int i = 0; i < roadNetworkNameMap.size(); i++) {	
				ArrayList<String>tempArrayList=new ArrayList<String>();
				tempArrayList=roadNetworkNameMap.get(i+1);
				String pcsName=tempArrayList.get(0);
				String junctionData=tempArrayList.get(1);
				String splitLineData=tempArrayList.get(2);
				String polygonData=tempArrayList.get(3);
				ReadRoadDataThread myReadRoadDataThread = new ReadRoadDataThread();
				myReadRoadDataThread.setPcsname(pcsName);
				myReadRoadDataThread.setDataname2(junctionData);
				myReadRoadDataThread.setDataname3(splitLineData);
				myReadRoadDataThread.setDataname4(polygonData);	
				//首先分配内存
				ArrayList<Node>tempJuncArraylist=new ArrayList<Node>();//存储节点ID以及坐标
				Map<Integer,ArrayList<Edge>> tempRelationEdgesMap=new HashMap<Integer,ArrayList<Edge>>();//存储节点ID以及相邻边
				ArrayList<Node>tempNodesArrayList=new ArrayList<Node>();//存储节点拓扑关系
				ArrayList<surface>tempSurfaceArrayList=new ArrayList<surface>();//存储面
			    ArrayList<Edge>tempPolylineCollArrayList=new ArrayList<Edge>();//存储polyline的ID以及polyline中的点
			    ArrayList<roadName> tempRoadNameArrayList=new ArrayList<roadName>();//存储道路名信息
				allJuncArraylistMap.put(pcsName, tempJuncArraylist);
				allRelationEdgesMap.put(pcsName, tempRelationEdgesMap);
				allNodesArrayListMap.put(pcsName, tempNodesArrayList);
				allSurfaceArrayListMap.put(pcsName, tempSurfaceArrayList);
				allPolylineCollArraylistMap.put(pcsName, tempPolylineCollArrayList);
				allRoadNameArrayMap.put(pcsName, tempRoadNameArrayList);
				System.out.print("开始读取路网数据："+ pcsName +"\n");
				myReadRoadDataThread.start();
				allThreads.add(myReadRoadDataThread);
			}	
		} 
    	
    	try{
        	for(ReadRoadDataThread thread : allThreads){
        		thread.join();
        	}	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
   
    public void readData(String pcsName,String junctionData,String splitLineData,String polygonData)
	{
    	isOK=false;
		try {
			float startRead=System.currentTimeMillis();	
			System.out.print(pcsName+"开始读数据："+startRead+"\n");
			readSDEData startReadSDEData=new readSDEData();
			while (!isOK) {
				ArrayList<Node> juncArraylist=new ArrayList<Node>();
				ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();
				ArrayList<surface> surfaceArrayList=new ArrayList<surface>();
				ArrayList<roadName> roadNameArrayList = new ArrayList<roadName>();
				juncArraylist=allJuncArraylistMap.get(pcsName);
				polylineCollArrayList=allPolylineCollArraylistMap.get(pcsName);
				surfaceArrayList=allSurfaceArrayListMap.get(pcsName);
				roadNameArrayList = allRoadNameArrayMap.get(pcsName);
				startReadSDEData.readData(pcsName,junctionData,splitLineData, polygonData,surfaceArrayList, juncArraylist, polylineCollArrayList, roadNameArrayList);						
				if (isOK) {
					break;
				}					
			}
			
			float endRead=System.currentTimeMillis();
			System.out.print(pcsName+"结束读数据："+endRead+"\n");
			float readTime=(endRead-startRead)/1000;
			System.out.print(pcsName+"读数据时间:"+readTime+"\n");
			isOK=true;//已读数据	
				
		} catch (Exception e) {
			e.printStackTrace();
			isOK=false;
			System.out.print(pcsName+"读取数据失败!"+"\n");
		}
		
		if (!isOK) {
			System.out.print("重新读取数据!"+"\n");
			readData(pcsName,junctionData,splitLineData,polygonData);
		}
		else {
			float startTopo=System.currentTimeMillis();
			System.out.print("开始构建拓扑："+startTopo+"\n");
			createTopology(pcsName);
			
			float endTopo=System.currentTimeMillis();
			System.out.print("拓扑构建结束："+endTopo+"\n");	
			System.out.print("拓扑构建时间"+"\n");
			float time=(float)((endTopo-startTopo)/1000);
			System.out.print(time+"\n");
		}			
	}
	
	public void createTopology(String pcsName)
	{
		try {
			ArrayList<Node> nodesArrayList=new ArrayList<Node>();
			Map<Integer,ArrayList<Edge>> relationEdgesMap=new HashMap<Integer,ArrayList<Edge>>();
			ArrayList<Node> juncArraylist=new ArrayList<Node>();
			ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();
			nodesArrayList=allNodesArrayListMap.get(pcsName);
			relationEdgesMap=allRelationEdgesMap.get(pcsName);
			juncArraylist=allJuncArraylistMap.get(pcsName);
			polylineCollArrayList=allPolylineCollArraylistMap.get(pcsName);
			
			while(nodesArrayList.size()==0||relationEdgesMap.size()==0)
			{		
				getJunctionRelationship(pcsName,nodesArrayList, relationEdgesMap, juncArraylist, polylineCollArrayList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/*relationEdgesMap:存储节点ID以及相邻Edge
     *nodesArrayList：存储节点间拓扑关系
     *surfaceArrayList:存储polygon以及ID*/			
	public void getJunctionRelationship(String pcsName,ArrayList<Node>nodesArrayList,Map<Integer,ArrayList<Edge>> relationEdgesMap,ArrayList<Node> juncArraylist,ArrayList<Edge>polylineCollArrayList)
	{		
		/* 定义节点数组 */
		int juncCount=juncArraylist.size();
		Node[]nodes=new Node[juncCount];	
		for (int i = 0; i < juncCount; i++) {		
 	    	//一定要实例化
 			nodes[i]=new Node();
 			nodes[i].setX(juncArraylist.get(i).getX());
 			nodes[i].setY(juncArraylist.get(i).getY());
 			nodes[i].setEID(juncArraylist.get(i).getEID()); 			
 		    }
		
		try {
	    	int lineCount=polylineCollArrayList.size(); 		 		
	 		//遍历点
	 		for (int i = 0; i < juncCount; i++) {   	    			
	 			ArrayList<Node> adjacentJuncArrayList=new ArrayList<Node>();//存储邻接点
	 			ArrayList<Edge> adjacentEdgeArrayList=new ArrayList<Edge>();//存储节点相邻Edge
	 				 			
	 			//遍历边
	 			for (int j = 0; j < lineCount; j++) {
	 				//边的ID，polyline起点frPoint，终点toPoint
	 				Edge tEdge=polylineCollArrayList.get(j);
 					int plineID=tEdge.getEdgeID();  
 					ArrayList<Node>tArrayList=new ArrayList<Node>();
 					tArrayList= tEdge.getPointCollArrayList();
     	    		Node frPoint=tArrayList.get(0);		    	    		
     	    		Node toPoint=tArrayList.get(tArrayList.size()-1);
     	    		
     	    		if (isTheSamePoint(juncArraylist.get(i), frPoint)) {
     	    			adjacentEdgeArrayList.add(tEdge);//相邻边			    	    			
     	    			for (int k1 = 0; k1 < juncCount; k1++) {
     	    				if (isTheSamePoint(juncArraylist.get(k1), toPoint)) {			    	    								    	    					
     	    					adjacentJuncArrayList.add(juncArraylist.get(k1));//相邻点
     	    					break;//找到相邻点，跳出循环
 							}		    	    				
     	    			}		    	    			
 					}
     	    		else if (isTheSamePoint(juncArraylist.get(i), toPoint)) {
     	    			adjacentEdgeArrayList.add(tEdge);//相邻边			    	    			
     	    			for (int k2 = 0; k2 < juncCount; k2++) {
     	    				if (isTheSamePoint(juncArraylist.get(k2), frPoint)) {
     	    					adjacentJuncArrayList.add(juncArraylist.get(k2));//相邻点    	    					
     	    					break;//找到相邻点，跳出循环
 							}		    	    				
     	    			}		    
 					}
     	    		else {
 						continue;
 					}	 						    					     	    					    	    					    	    		    							    	    			    					
	 			}	
	 				 			
	 			//存储节点关系
	 			//邻结点与邻接边数相等
	 			//必须添加nodes中的点，相互间地址的引用，否则错误
	    		int adjacentEdgeCount=adjacentEdgeArrayList.size();
				ArrayList<Node> List = new ArrayList<Node>();
				for(int k5=0;k5<adjacentJuncArrayList.size();k5++){
					
					int EID=adjacentJuncArrayList.get(k5).getEID();
					for (int k6 = 0; k6 < juncCount; k6++) {
						if (nodes[k6].getEID()==EID){
							List.add(nodes[k6]);
							break;
						}
					}
					nodes[i].setRelationNodes(List);
				}
				List = null;  //释放内存				
				nodes[i].setRelationEdge(adjacentEdgeArrayList);				
	 			relationEdgesMap.put(juncArraylist.get(i).getEID(), adjacentEdgeArrayList);//存储节点ID以及邻接边	 				 								   						    			 	    			
	 		} 		
	 		
	 		for (int q = 0; q < juncCount; q++) {
	 			nodesArrayList.add(nodes[q]);
	 		}
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
			//捕获错误便重新构建拓扑
			nodesArrayList=new ArrayList<Node>();
			relationEdgesMap=new HashMap<Integer, ArrayList<Edge>>();
			getJunctionRelationship(pcsName,nodesArrayList, relationEdgesMap, juncArraylist, polylineCollArrayList);
		}
		finally{
//			DDZHServiceImpl.nodesArrayList=nodesArrayList;
//			DDZHServiceImpl.relationEdgesMap=relationEdgesMap;
//			allNodesArrayListMap.put(pcsName, nodesArrayList);
//			allRelationEdgesMap.put(pcsName, relationEdgesMap);
		}	   
	}
	
	/*relationEdgesMap:存储节点ID,相邻Edge
     *nodesArrayList：存储节点关系
     *surfaceArrayList:存储polygon以及ID*/	
		
	/*警情点坐标：jjL，jjB经纬度
	 * speed，速度km/h
	 * time：时间，分*/
	/* 临时保存路径节点的栈 */
	private Stack<Node> stack = new Stack<Node>();
	private ArrayList<Node> openList = new ArrayList<Node>();//开启列表
	private ArrayList<Node> nodesPathArrayList = new ArrayList<Node>();//存储临时路径
	/* 存储路径集合 */
	private ArrayList<ArrayList<Node>> pathArrayLists = new ArrayList<ArrayList<Node>>();//存储拦截路径
	public ArrayList<ArrayList<retuNode>> retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//返回前台路径
	public ArrayList<Node>RoadJuncArrayList=new ArrayList<Node>();//存储道路交叉点作为拦截点
	public ArrayList<retuNode>retuRoadJuncArrayList=new ArrayList<retuNode>();//道路交叉点返回前台
	public ArrayList<ArrayList<String>> allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//所有路径描述
	public returnResult ljfxResult=new returnResult();//返回路径分析结果	
		
	public returnResult ljfx(String pcsName,double jjL,double jjB, double speed,double time) throws AutomationException, IOException
	{
		String roadNetworkPcsName = PropertiesUtilJAR.getProperties("roadNetwork");
		String roadNetworkPcsNamecoll[] = roadNetworkPcsName.split(",");
		String fjName = roadNetworkPcsNamecoll[1];
		for(int i = 1; i < roadNetworkPcsNamecoll.length; i++ ){
			String tempPcsNameString = roadNetworkPcsNamecoll[i];
			if (tempPcsNameString.equals(pcsName)) {
				pcsName = fjName;
				break;
			}
		}		
		pathArrayLists = new ArrayList<ArrayList<Node>>();
		retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//返回前台路径
		RoadJuncArrayList=new ArrayList<Node>();//存储道路交叉点作为拦截点
		retuRoadJuncArrayList=new ArrayList<retuNode>();//道路交叉点返回前台
		allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//所有路径描述
		ljfxResult=new returnResult();//返回路径分析结果
		
		if(gInstance.allNodesArrayListMap.get(pcsName).size()==0)
		{
			System.out.print("未读取路网数据");
			return null;
		}
					
		RoadJuncArrayList=new ArrayList<Node>();//清空拦截圈中的节点，以存储新的拦截圈节点
		ArrayList<Node> pathNewTopoArrayList=new ArrayList<Node>();//新的拓扑关系点，这个拓扑必须与原有全局范围内拓扑无关，全部重新赋值	
		ArrayList<Node>startArrayList=new ArrayList<Node>();//寻路起点集合
		Node jjNode=new Node(null);
		double distance=0;	
		double topoDistance = 0;
	    try {			   
			jjNode.setX(jjL);
			jjNode.setY(jjB);
			distance=Math.round(speed*1000*time/60);//拦截距	
			//如果拦截圈范围小于1000米，就在1000米范围建立新拓扑
			if(distance > 1000){
				topoDistance = distance;
			}
			else {
				topoDistance = 1000;
			}			
			/* 定义节点数组 */
			ArrayList<Node> nodesArrayList=new ArrayList<Node>();
			nodesArrayList=gInstance.allNodesArrayListMap.get(pcsName);
			int nodesCount=nodesArrayList.size();
			Node[]nodes=new Node[nodesCount];
			for (int i = 0; i < nodesCount; i++) {
				nodes[i]=nodesArrayList.get(i);
				if (nodes[i].EID==244) {
					System.out.print("244");
				}
			}		
					
			long staNewTopo=System.currentTimeMillis();	
			System.out.print("开始构建新拓扑："+staNewTopo + "ms" + "\n");				
			/*搜索拦截圈范围内的点 */
			ArrayList<Node> topoArrayList=new ArrayList<Node>();//用于构建新拓扑的点
			ArrayList<Node> tempTopoArrayList=new ArrayList<Node>();//临时变量				
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].EID==182) {
					System.out.print("182");
				}
				if (isNodeInCricle(nodes[i], topoDistance, jjNode)) {
					topoArrayList.add(nodes[i]);
				}
			}
			
			for (int i = 0; i < topoArrayList.size(); i++) {
				Node node=new Node();
				node.setEID(topoArrayList.get(i).EID);
				node.setRelationEdge(topoArrayList.get(i).relationEdges);				
				node.setX(topoArrayList.get(i).x);
				node.setY(topoArrayList.get(i).y);
				
				//2013/12/1修改
				ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();//存储邻接点
				ArrayList<Node>tempRelaNodeArrayList=new ArrayList<Node>();//临时变量
				tempRelaNodeArrayList=topoArrayList.get(i).getRelationNodes();
				for (int j = 0; j < tempRelaNodeArrayList.size(); j++) {
					Node tNode=new Node();
					tNode=tempRelaNodeArrayList.get(j);
					if (isNodeInCricle(tNode, topoDistance, jjNode)) {
						relaNodeArrayList.add(tNode);
					}
				}
				node.setRelationNodes(relaNodeArrayList);	
				tempTopoArrayList.add(node);
			}			
			
			/*拦截范围内的点建立新的拓扑
			 * 遍历拦截圈范围内的每一个点
			 * 每一个点的相邻点与拦截圈范围内的每一个点比较，以确定相邻点是否在拦截圈内
			 * 重新建立拓扑
			 * pathNewTopoArrayList：新的拓扑关系点
			 * newNode[i]:新的拓扑关系点数组*/		
			int topoCount=topoArrayList.size();
			for (int i = 0; i < topoCount; i++) {
				ArrayList<Node> relaNode=new ArrayList<Node>();
				Node t1node=new Node();
				t1node=tempTopoArrayList.get(i);
				int relaNodeCount=t1node.getRelationNodes().size();
				for (int j = 0; j < relaNodeCount; j++) {
					Node t2node=new Node();
					t2node=t1node.getRelationNodes().get(j);//t1node邻接点
					
					for (int k = 0; k < topoCount; k++) {
						Node t3node=new Node();
						t3node=topoArrayList.get(k);//必须用topoArrayList
						if (isTheSamePoint(t2node, t3node)) {
							relaNode.add(t2node);
						}
					}				
				}
				t1node.setRelationNodes(relaNode);				
				
				//存储临接边长度 2013/11/30改
				ArrayList<Edge>newRelaEdgesArrayList=new ArrayList<Edge>();//计算后的邻接边
				ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//邻接边				
				relaEdgesArrayList=t1node.getRelationEdge();
				int edgeCount=relaEdgesArrayList.size();//邻接边数
				for (int p = 0; p < edgeCount; p++) {						
					Edge tEdge=new Edge();
					ArrayList<Node>edgePointColl=new ArrayList<Node>();//邻接边上的点
					tEdge=relaEdgesArrayList.get(p);
					edgePointColl=tEdge.getPointCollArrayList();//存储边上的点
					double edgeLength=0;
					for (int q = 0; q < edgePointColl.size()-1; q++) {
						Node tNode1=new Node();
						Node tNode2=new Node();
						tNode1=edgePointColl.get(q);
						tNode2=edgePointColl.get(q+1);
						edgeLength=edgeLength+distance(tNode1,tNode2);				
					}
					tEdge.setEdgeLength(edgeLength);
					newRelaEdgesArrayList.add(tEdge);
				}					
			    t1node.setRelationEdge(newRelaEdgesArrayList);				
			    pathNewTopoArrayList.add(t1node);//建立新的拓扑，必须用pathNewTopoArrayList
    		}					
			long endNewTopo=System.currentTimeMillis();	
			System.out.print("新拓扑构建结束：" + endNewTopo + "ms" + "\n");
			long newTopoTime=(endNewTopo-staNewTopo)/1000;
			System.out.print("新拓扑构建时间：" + newTopoTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();		    	
		}
		
		int count=pathNewTopoArrayList.size();
		Node []newNode=new Node[count]; 
		long starjjNode=System.currentTimeMillis();	
		System.out.print("开始判断警情点发生面：" + starjjNode + "ms" + "\n");
		try {
			//存储新的拓扑关系
			for (int i = 0; i < count; i++) {
				newNode[i]=pathNewTopoArrayList.get(i);
				if (newNode[i].EID==244) {
					System.out.print("244");
				}
			}
				
			/*检索警情点发生面
			 * polygonID:警情所在面的ID*/				
			ArrayList<surface> surfaceArrayList=new ArrayList<surface>();
			surfaceArrayList=gInstance.allSurfaceArrayListMap.get(pcsName);
			int polygonCount=surfaceArrayList.size();
			surface findPolygon = null;//警情发生面
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jjNode.x,jjNode.y)){
					findPolygon = tempSurface;
					break;
				}
			}						
			/*在新建立的拓扑关系中，检索面内所有节点
			 *面内包含的节点作为寻路起点 
			 *startArrayList:寻路起点集合
			 *endArrayList:寻路终点集合*/		
			for (int i = 0; i < newNode.length; i++) {
				if(findPolygon.pIn(newNode[i].x,newNode[i].y)){
					startArrayList.add(newNode[i]);
				}
			}	
			long endjjNode=System.currentTimeMillis();	
			System.out.print("结束判断警情点发生面：" + endjjNode + "ms" + "\n");
			long jjNodeTime=(long)(endjjNode-starjjNode)/1000;
			System.out.print("判断警情点发生面时间：" + jjNodeTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		    e.printStackTrace();		
		}
		long starFindRoad = System.currentTimeMillis();
		try {						
			System.out.print("开始寻路："+ starFindRoad + "ms" + "\n");
			ArrayList<Node>endNodeArrayList=new ArrayList<Node>();
			endNodeArrayList=pathNewTopoArrayList;					
			
			
			//方式一：从交叉点开始的最短路径
//			/*根据寻路起终点寻路*/
//			for (int i = 0; i < startArrayList.size(); i++) {
//				Node cNode=new Node(null);//寻路起点，父节点为空
//				cNode=startArrayList.get(i);
//				double temp=distance(jjNode, cNode);//警情点与寻路起点间距离
//				//警情点与寻路起点间距离大于拦截半径，只存储道路交叉点作为拦截点
//				if (temp>distance) {
//					ArrayList<Node>tempArrayList=new ArrayList<Node>();
//					tempArrayList.add(jjNode);
//					RoadJuncArrayList.add(cNode);
//				}
//				else {
//					double searchPathDistance=distance-temp;//新的拦截距
//					/*减少寻路终点数目，提高检索速度:
//					 * 若拦截圈范围超过1000米，则使用该方法
//					 * distance/3到distance范围内的点作为寻路终点*/
//					if(distance>1000){
//						ArrayList<Node>newNodeArrayList=new ArrayList<Node>();
//						for (int j = 0; j < endNodeArrayList.size(); j++) {
//							Node node=endNodeArrayList.get(j);
//							if (!isNodeInCricle(node, distance/3, jjNode)) {
//								newNodeArrayList.add(node);								
//							}
//						}
//						for (int k = 0; k < newNodeArrayList.size(); k++) {
//							Node eNode=new Node();
//							eNode=newNodeArrayList.get(k);
//							if (eNode.EID==755) {
//								System.out.print(eNode.EID);
//							}
//							//寻路
//							openList = new ArrayList<Node>();//清空开启列表
//							openList.add(cNode);
//							stack=new Stack<Node>();//新的起始节点要清空栈
//							nodesPathArrayList = new ArrayList<Node>();//清空路径节点
//							getIntePoint(pcsName,jjNode,searchPathDistance,cNode, null, cNode, eNode);
//						}						
//					}
//					else {
//						for (int j = 0; j < endNodeArrayList.size(); j++) {
//							Node eNode=new Node(null);//寻路终点，父节点设为空
//							eNode=endNodeArrayList.get(j);
//							if (eNode.EID==755) {
//								System.out.print(eNode.EID);
//							}							
//							//寻路
//							openList = new ArrayList<Node>();//清空开启列表
//							openList.add(cNode);
//							stack=new Stack<Node>();//新的起始节点要清空栈
//							nodesPathArrayList = new ArrayList<Node>();
//							getIntePoint(pcsName,jjNode,searchPathDistance,cNode, null, cNode, eNode);
//						}
//					}				
//				}			
//			}
			
			
			//方式二：从接警点开始的最短路径
			/*根据寻路起终点寻路*/
			jjNode.setRelationNodes(startArrayList);//警情点拓扑	
			jjNode.setEID(-1);//警情点ID设为-1
			/*减少寻路终点数目，提高检索速度:
			 * 若拦截圈范围超过1000米，则使用该方法
			 * distance/3到distance范围内的点作为寻路终点*/
			if(distance > 1000){
				//寻路
				for (int i = 0; i < startArrayList.size(); i++) {
					Node cNode=new Node(null);
					cNode=startArrayList.get(i);
					double temp=distance(jjNode, cNode);//警情点与起点间距离
					//警情点与起点间距离大于拦截半径，只存储道路交叉点作为拦截点
					if (temp > distance) {
						RoadJuncArrayList.add(cNode);
					}
					else {
						ArrayList<Node>newNodeArrayList=new ArrayList<Node>();
						for (int k = 0; k < endNodeArrayList.size(); k++) {
							Node node=endNodeArrayList.get(k);
							if (!isNodeInCricle(node, distance/3, jjNode)) {
								newNodeArrayList.add(node);								
							}
						}				
						for (int k = 0; k < newNodeArrayList.size(); k++) {
							Node eNode=new Node();
							eNode=newNodeArrayList.get(k);
							if (eNode.EID==996) {
								System.out.print(eNode.EID);
							}
							if (eNode.EID==758) {
								System.out.print(eNode.EID);
							}
							openList = new ArrayList<Node>();//清空开启列表
							openList.add(jjNode);
							stack=new Stack<Node>();//新的起始节点要清空栈
							nodesPathArrayList = new ArrayList<Node>();
							getIntePoint(pcsName,jjNode,distance,jjNode, null, jjNode, eNode);
						}
					}
				}								
			}
			else {
				//寻路
				for (int i = 0; i < startArrayList.size(); i++) {
					Node cNode=new Node(null);
					cNode=startArrayList.get(i);
					double temp=distance(jjNode, cNode);//警情点与起点间距离
					//警情点与起点间距离大于拦截半径，只存储道路交叉点作为拦截点
					if (temp > distance) {
						RoadJuncArrayList.add(cNode);
					}
					else {
						for (int j = 0; j < endNodeArrayList.size(); j++) {
							Node eNode=new Node(null);//寻路终点，父节点设为空
							eNode=endNodeArrayList.get(j);
							if (eNode.EID==4190) {
								System.out.print(eNode.EID);
							}
							if (eNode.EID==758) {
								System.out.print(eNode.EID);
							}
							openList = new ArrayList<Node>();//清空开启列表
							openList.add(jjNode);
							stack=new Stack<Node>();//新的起始节点要清空栈
							nodesPathArrayList = new ArrayList<Node>();
							getIntePoint(pcsName,jjNode,distance,jjNode, null, jjNode, eNode);
						}
					}
				}				
			}
			long endFindRoad = System.currentTimeMillis();
			System.out.print("\n" + "寻路结束：" + starFindRoad + "ms" + "\n");
			long findRoadTime = (endFindRoad - starFindRoad)/1000;
			System.out.print( "寻路时间：" + findRoadTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();
		}
		
		/*取得路径以返回前台*/
		try {
			retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();	
			for (int i = 0; i < pathArrayLists.size(); i++) {
				ArrayList<Node>tempSinglePathArrayList=new ArrayList<Node>();
				ArrayList<retuNode>retuSinglePathArrayList=new ArrayList<retuNode>();
				tempSinglePathArrayList=pathArrayLists.get(i);
				for (int t = 0; t < tempSinglePathArrayList.size(); t++) {
					retuNode node=new retuNode();
					node.L=tempSinglePathArrayList.get(t).x;
					node.B=tempSinglePathArrayList.get(t).y;
					node.ID=tempSinglePathArrayList.get(t).EID;
					retuSinglePathArrayList.add(node);
				}
				retuAllPathArrayList.add(retuSinglePathArrayList);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		    e.printStackTrace();
		}
		ljfxResult.setLjfxAllPathArrayList(retuAllPathArrayList);
		ljCircle(jjNode);
		ljfxResult.setLjCircleJuncArrayList(retuRoadJuncArrayList);
		analyzePath(pcsName);
		ljfxResult.setAllPathDescriptArrayList(allPathDescriptArrayList);		
		return ljfxResult;
	}
	
	/*两点间最短路径
	 * jjL:接警点经度
	 * jjB:接警点纬度
	 * jcL:警察位置经度
	 * jcB:警察位置纬度*/
	public ArrayList<Node>endNodeArrayList=new ArrayList<Node>();//寻路终点集合
	public returnResult shortestPath(String pcsName,double jjL, double jjB, double jcL, double jcB){
//		String roadNetworkPcsName = PropertiesUtilJAR.getProperties("roadNetwork");
		String roadNetworkPcsName = PropertiesUtil.getProperties("roadNetwork");
		String roadNetworkPcsNamecoll[] = roadNetworkPcsName.split(",");
		String fjName = roadNetworkPcsNamecoll[1];
		for(int i = 1; i < roadNetworkPcsNamecoll.length; i++ ){
			String tempPcsNameString = roadNetworkPcsNamecoll[i];
			if (tempPcsNameString.equals(pcsName)) {
				pcsName = fjName;
				break;
			}
		}
		
		pathArrayLists = new ArrayList<ArrayList<Node>>();
		retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//返回前台路径
		RoadJuncArrayList=new ArrayList<Node>();//存储道路交叉点作为拦截点
		retuRoadJuncArrayList=new ArrayList<retuNode>();//道路交叉点返回前台
		allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//所有路径描述
		ljfxResult=new returnResult();//返回路径分析结果
		RoadJuncArrayList=new ArrayList<Node>();//清空拦截圈中的节点，以存储新的拦截圈节点
		if(gInstance.allNodesArrayListMap.get(pcsName).size()==0)
		{
			System.out.print("未读取路网数据");
			return null;
		}
							
		ArrayList<Node> pathNewTopoArrayList = new ArrayList<Node>();//新的拓扑关系点，这个拓扑必须与原有全局范围内拓扑无关，全部重新赋值	
		ArrayList<Node>startArrayList = new ArrayList<Node>();//寻路起点集合
		endNodeArrayList=new ArrayList<Node>();//寻路终点集合
		Node jjNode = new Node(null);
		Node jcNode = new Node(null);
		double distance = 0;	
		double topoDistance = 0;
	    try {			   
			jjNode.setX(jjL);
			jjNode.setY(jjB);
			jcNode.setX(jcL);
			jcNode.setY(jcB);
			distance=distance(jjNode, jcNode); //拦截距	
			//新拓扑构建范围
			topoDistance = distance + 500;	
			/* 定义节点数组 */
			ArrayList<Node> nodesArrayList=new ArrayList<Node>();
			nodesArrayList=gInstance.allNodesArrayListMap.get(pcsName);
			int nodesCount=nodesArrayList.size();
			Node[]nodes=new Node[nodesCount];
			for (int i = 0; i < nodesCount; i++) {
				nodes[i]=nodesArrayList.get(i);
			}		
					
			long staNewTopo=System.currentTimeMillis();	
			System.out.print("开始构建新拓扑："+staNewTopo + "ms" + "\n");				
			/*搜索拦截圈范围内的点 */
			ArrayList<Node> topoArrayList=new ArrayList<Node>();//用于构建新拓扑的点
			ArrayList<Node> tempTopoArrayList=new ArrayList<Node>();//临时变量				
			for (int i = 0; i < nodes.length; i++) {
				if (isNodeInCricle(nodes[i], topoDistance, jjNode)) {
					topoArrayList.add(nodes[i]);
				}
			}
			
			for (int i = 0; i < topoArrayList.size(); i++) {
				Node node=new Node();
				node.setEID(topoArrayList.get(i).EID);
				node.setRelationEdge(topoArrayList.get(i).relationEdges);			
				node.setX(topoArrayList.get(i).x);
				node.setY(topoArrayList.get(i).y);				
				ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();//存储邻接点
				ArrayList<Node>tempRelaNodeArrayList=new ArrayList<Node>();//临时变量
				tempRelaNodeArrayList=topoArrayList.get(i).getRelationNodes();
				for (int j = 0; j < tempRelaNodeArrayList.size(); j++) {
					Node tNode=new Node();
					tNode=tempRelaNodeArrayList.get(j);
					if (isNodeInCricle(tNode, topoDistance, jjNode)) {
						relaNodeArrayList.add(tNode);
					}
				}
				node.setRelationNodes(relaNodeArrayList);
				tempTopoArrayList.add(node);
			}			
			
			/*拦截范围内的点建立新的拓扑
			 * 遍历拦截圈范围内的每一个点
			 * 每一个点的相邻点与拦截圈范围内的每一个点比较，以确定相邻点是否在拦截圈内
			 * 重新建立拓扑
			 * pathNewTopoArrayList：新的拓扑关系点
			 * newNode[i]:新的拓扑关系点数组*/		
			int topoCount=topoArrayList.size();
			for (int i = 0; i < topoCount; i++) {
				ArrayList<Node> relaNode=new ArrayList<Node>();
				Node t1node=new Node();
				t1node=tempTopoArrayList.get(i);
				int relaNodeCount=t1node.getRelationNodes().size();
				for (int j = 0; j < relaNodeCount; j++) {
					Node t2node=new Node();
					t2node=t1node.getRelationNodes().get(j);//t1node邻接点					
					for (int k = 0; k < topoCount; k++) {
						Node t3node=new Node();
						t3node=topoArrayList.get(k);//必须用topoArrayList
						if (isTheSamePoint(t2node, t3node)) {
							relaNode.add(t2node);
						}
					}				
				}
				t1node.setRelationNodes(relaNode);				
				//存储临接边长度 
				ArrayList<Edge>newRelaEdgesArrayList=new ArrayList<Edge>();//计算后的邻接边
				ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//邻接边				
				relaEdgesArrayList=t1node.getRelationEdge();
				int edgeCount=relaEdgesArrayList.size();//邻接边数
				for (int p = 0; p < edgeCount; p++) {						
					Edge tEdge=new Edge();
					ArrayList<Node>edgePointColl=new ArrayList<Node>();//邻接边上的点
					tEdge=relaEdgesArrayList.get(p);
					edgePointColl=tEdge.getPointCollArrayList();//存储边上的点
					double edgeLength=0;
					for (int q = 0; q < edgePointColl.size()-1; q++) {
						Node tNode1=new Node();
						Node tNode2=new Node();
						tNode1=edgePointColl.get(q);
						tNode2=edgePointColl.get(q+1);
						edgeLength=edgeLength+distance(tNode1,tNode2);				
					}
					tEdge.setEdgeLength(edgeLength);
					newRelaEdgesArrayList.add(tEdge);
				}					
			    t1node.setRelationEdge(newRelaEdgesArrayList);				
			    pathNewTopoArrayList.add(t1node);//建立新的拓扑，必须用pathNewTopoArrayList
    		}					
			long endNewTopo=System.currentTimeMillis();	
			System.out.print("新拓扑构建结束：" + endNewTopo + "ms" + "\n");
			long newTopoTime=(endNewTopo-staNewTopo)/1000;
			System.out.print("新拓扑构建时间：" + newTopoTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();		    	
		}
		
		int count=pathNewTopoArrayList.size();
		Node []newNode=new Node[count]; 
		long starjjNode=System.currentTimeMillis();	
		System.out.print("开始判断警情点、警察点发生面：" + starjjNode + "ms" + "\n");
		try {
			//存储新的拓扑关系
			for (int i = 0; i < count; i++) {
				newNode[i]=pathNewTopoArrayList.get(i);
			}
				
			/*检索警情点发生面
			 * polygonID:警情所在面的ID*/				
			ArrayList<surface> surfaceArrayList=new ArrayList<surface>();
			surfaceArrayList=gInstance.allSurfaceArrayListMap.get(pcsName);
			int polygonCount=surfaceArrayList.size();
			surface findJJPolygon = null;//接情点发生面
			surface findJCPolygon = null;//警察点所在面
			//判断接情点所在面
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jjNode.x,jjNode.y)){
					findJJPolygon = tempSurface;
					break;
				}
			}			
			
			//判断警察点所在面
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jcNode.x, jcNode.y)){
					findJCPolygon = tempSurface;
					break;
				}
			}
			
			/*在新建立的拓扑关系中，检索面内所有节点
			 *面内包含的节点作为寻路起点 
			 *startArrayList:寻路起点集合
			 *endArrayList:寻路终点集合*/
			ArrayList<Node>tempStartArrayList = new ArrayList<Node>();
			ArrayList<Node>tempEndNodeArrayList = new ArrayList<Node>();	
			for (int i = 0; i < newNode.length; i++) {
				if(findJJPolygon.pIn(newNode[i].x,newNode[i].y)){
					tempStartArrayList.add(newNode[i]);
				}
			}	
			for (int i = 0; i < newNode.length; i++) {
				if (findJCPolygon.pIn(newNode[i].x,newNode[i].y)){
					tempEndNodeArrayList.add(newNode[i]);
				}
			}
			
			long endjjNode=System.currentTimeMillis();	
			System.out.print("结束判断警情点、警察点发生面：" + endjjNode + "ms" + "\n");
			long jjNodeTime=(long)(endjjNode-starjjNode)/1000;
			System.out.print("判断警情点、警察点发生面时间：" + jjNodeTime + "ms" + "\n");
			/*寻路起点、终点
			 *如果面边缘及面内的点与接警点、警察点直线间夹角为锐角
			 *该点为有效点*/
			if (tempStartArrayList.size() > 0) {
				for (int i = 0; i < tempStartArrayList.size(); i++) {
					Node node = new Node();
					node = tempStartArrayList.get(i);
					if (isDirectProjSatis(jjNode, jcNode, node)) {
						startArrayList.add(node);
					}
				}
			}
			if (tempEndNodeArrayList.size()>0) {
				for (int i = 0; i < tempEndNodeArrayList.size(); i++) {
					Node node = new Node();
					node = tempEndNodeArrayList.get(i);
					if (isDirectProjSatis(jcNode, jjNode, node)) {
						endNodeArrayList.add(node);
					}
				}
			}
				
			long starFindRoad = System.currentTimeMillis();							
			System.out.print("开始寻路："+ starFindRoad + "ms" + "\n");
			
			/*插入的警情点、警察点建立拓扑关系*/
			jjNode.setRelationNodes(startArrayList);//警情点拓扑	
			jjNode.setEID(-1);//警情点ID设为-1
			jcNode.setRelationNodes(endNodeArrayList);//警察点拓扑			
			jcNode.setEID(-2);//警察点ID设为-2
			ArrayList<Edge> relationEdge = new ArrayList<Edge>();	
			for (int q = 0; q < endNodeArrayList.size(); q++) {
				Node tNode = new Node();
				tNode = endNodeArrayList.get(q);			
				Edge tempEdge = new Edge();
				tempEdge.edgeLength = distance(tNode, jcNode);
				ArrayList<Node> pointCollArrayList = new ArrayList<Node>();
				pointCollArrayList.add(tNode);
				pointCollArrayList.add(jcNode);
				tempEdge.setPointCollArrayList(pointCollArrayList);
				relationEdge.add(tempEdge);
			}
			jcNode.setRelationEdge(relationEdge);
			
			//警察点所在面内的节点构造新拓扑关系
			ArrayList<Node>temptempEndNodeArrayList=new ArrayList<Node>();//中间交换变量
			for (int q = 0; q < endNodeArrayList.size(); q++) {
				Node tNode = new Node();
				tNode = endNodeArrayList.get(q);				
				ArrayList<Node>tempRelatNodeArrayList = new ArrayList<Node>();
				tempRelatNodeArrayList = tNode.relationNodes;
				tempRelatNodeArrayList.add(jcNode);
				tNode.setRelationNodes(tempRelatNodeArrayList);
				ArrayList<Edge> tempRelationEdgeArrayList = new ArrayList<Edge>();
				tempRelationEdgeArrayList = tNode.relationEdges;
				Edge tempEdge = new Edge();
				tempEdge.edgeLength = distance(tNode, jcNode);
				tempEdge.edgeID = -2;//标记
				ArrayList<Node> pointCollArrayList = new ArrayList<Node>();
				pointCollArrayList.add(tNode);
				pointCollArrayList.add(jcNode);
				tempEdge.setPointCollArrayList(pointCollArrayList);
				tempRelationEdgeArrayList.add(tempEdge);
				tNode.setRelationEdge(tempRelationEdgeArrayList);
				temptempEndNodeArrayList.add(tNode);
			}
			endNodeArrayList = temptempEndNodeArrayList;
			
			//寻路
			openList = new ArrayList<Node>();//清空开启列表
			openList.add(jjNode);
			stack=new Stack<Node>();//新的起始节点要清空栈
			nodesPathArrayList = new ArrayList<Node>();
			getShortestPath(pcsName,jjNode,distance,jjNode, null, jjNode, jcNode);			
			long endFindRoad = System.currentTimeMillis();
			System.out.print("\n" + "寻路结束：" + starFindRoad + "ms" + "\n");
			long findRoadTime = (endFindRoad - starFindRoad)/1000;
			System.out.print( "寻路时间：" + findRoadTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();
		}
		
		/*取得路径以返回前台*/
		try {
			retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();	
			for (int i = 0; i < pathArrayLists.size(); i++) {
				ArrayList<Node>tempSinglePathArrayList=new ArrayList<Node>();
				ArrayList<retuNode>retuSinglePathArrayList=new ArrayList<retuNode>();
				tempSinglePathArrayList=pathArrayLists.get(i);
				for (int t = 0; t < tempSinglePathArrayList.size(); t++) {
					retuNode node=new retuNode();
					node.L=tempSinglePathArrayList.get(t).x;
					node.B=tempSinglePathArrayList.get(t).y;
					node.ID=tempSinglePathArrayList.get(t).EID;
					retuSinglePathArrayList.add(node);
				}
				retuAllPathArrayList.add(retuSinglePathArrayList);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		    e.printStackTrace();
		}
		ljfxResult.setLjfxAllPathArrayList(retuAllPathArrayList);
		//新建的拓扑关系寻路结束之后要清空
		for (int i = 0; i < endNodeArrayList.size(); i++) {
			Node tNode = new Node();
			tNode = endNodeArrayList.get(i);
			ArrayList<Node>tempRelatNodeArrayList = new ArrayList<Node>();
			tempRelatNodeArrayList = tNode.relationNodes;
			int nodeIndex = -1;
			for (int j = 0; j < tempRelatNodeArrayList.size(); j++) {
				Node ttNode = new Node();
				ttNode = tempRelatNodeArrayList.get(j);
				if (ttNode.EID == -2) {
					nodeIndex =j;
					break;
				}
			}
			tempRelatNodeArrayList.remove(nodeIndex);			
			ArrayList<Edge> tempRelationEdgeArrayList = new ArrayList<Edge>();
			tempRelationEdgeArrayList = tNode.relationEdges;
			int edgeIndex = -1;
			for (int k = 0; k < tempRelationEdgeArrayList.size(); k++) {
				Edge tempEdge = tempRelationEdgeArrayList.get(k);
				if (tempEdge.edgeID == -2) {
					edgeIndex = k;
					break;
				}
			}
			tempRelationEdgeArrayList.remove(edgeIndex);
		}
		return ljfxResult;
	}
	
	
	/*解析路径名称
	 * 传入所有路径，返回路径描述*/
	public void analyzePath(String pcsName)
	{
		try {
			allPathDescriptArrayList=null;
			allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//所有路径描述
			roadNameArrayList=new ArrayList<roadName>();
			roadNameArrayList=gInstance.allRoadNameArrayMap.get(pcsName);
			if (retuAllPathArrayList.size()!=0) {
				for (int i = 0; i < retuAllPathArrayList.size(); i++) {
					ArrayList<retuNode>singlePathArrayList=new ArrayList<retuNode>();
					singlePathArrayList=retuAllPathArrayList.get(i);
					ArrayList<retuNode>junctionArrayList=new ArrayList<retuNode>();//单一路径交叉节点
					ArrayList<String>singlePathDescrip=new ArrayList<String>();//单一路径描述
					//取得交叉节点
					for (int m = 0; m < singlePathArrayList.size(); m++) {
						retuNode node=new retuNode();
						node=singlePathArrayList.get(m);
						if (node.ID!=0) {
							junctionArrayList.add(node);
						}					
					}
					
					for (int j = 0; j < junctionArrayList.size()-1; j++) {
						//取得交叉节点,交叉节点与路段比较，判断为哪个路段
						retuNode curJuncNode1=new retuNode();
						retuNode curJuncNode2=new retuNode();
						curJuncNode1=singlePathArrayList.get(j);
						curJuncNode2=singlePathArrayList.get(j+1);
						//道路交叉点，判断路段
						for (int k = 0; k < roadNameArrayList.size(); k++) {
							roadName rName=new roadName();
							rName=roadNameArrayList.get(k);
							retuNode froPoint=new retuNode();
							retuNode toPoint=new retuNode();
							froPoint=rName.getFroPoint();
							toPoint=rName.getToPoint();
							if (isTheSameJuncPoint(curJuncNode1, froPoint)||isTheSameJuncPoint(curJuncNode1, toPoint)
									&&isTheSameJuncPoint(curJuncNode2, froPoint)||isTheSameJuncPoint(curJuncNode2, toPoint)) {
								singlePathDescrip.add(rName.getRoadName());//路径名	
								break;
							}						
						}						
					}				
			    allPathDescriptArrayList.add(singlePathDescrip);				
				}			
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	/*判断是否为同一交叉节点juncPoint*/
	public boolean isTheSameJuncPoint(retuNode junc1,retuNode junc2)
	{
		try {
			double L1=junc1.getL();
    		double B1=junc1.getB();
    		double L2=junc2.getL();
    		double B2=junc2.getB();
    		if (Math.abs(L1-L2)<=0.00000001&&Math.abs(B1-B2)<=0.00000001) {
				return true;
			}
    		else {
    			return false;
			}
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
			return false;
		}		
	}
	
	/*读取路径名
	 * */
	public ArrayList<roadName> roadNameArrayList=new ArrayList<roadName>();
	public void readAllRoadName(String pcsName,String path)
	 {		 
		 readRoadName readName=new readRoadName();
		 roadNameArrayList=new ArrayList<roadName>();
		 readName.readName(pcsName, path, roadNameArrayList);
		 allRoadNameArrayMap.put(pcsName, roadNameArrayList);
		 System.out.print(pcsName+"结束读取道路名称信息"+"\n");
	 }
	
	
	/*返回构成拦截圈的边缘点 
	 * jjNode:接警点坐标
	 * 对拦截圈的交叉点进行逆时针排序
	 * 旋转扫描线法：
	 * 1.计算极角大小
	 * 2.叉积判断顺序*/
	public void ljCircle(Node jjNode)
	{			
		Node starNode=new Node();
		starNode=RoadJuncArrayList.get(0);//作为计算极角的起始线段点
		double[]polarAngleArray=new double[RoadJuncArrayList.size()];//存储极角，包含正负号
		Map<Double,Node >nodePolarAngleMap=new HashMap<Double, Node>();//存放极角以及节点
		for (int i = 0; i < RoadJuncArrayList.size(); i++) {
			double polarAngle=retuPolarAngle(jjNode, starNode, RoadJuncArrayList.get(i));
			polarAngleArray[i]=polarAngle;
			nodePolarAngleMap.put(polarAngle,RoadJuncArrayList.get(i));
		}
		//对极角冒泡排序,由小到大
		for (int j = 0; j < polarAngleArray.length-1; j++) {
			for (int k = j+1; k < polarAngleArray.length; k++) {
				if (polarAngleArray[j]>polarAngleArray[k]) {
					double temp=polarAngleArray[k];
					polarAngleArray[k]=polarAngleArray[j];
					polarAngleArray[j]=temp;
				}
			}
		}
		//排序后的节点数组
		ArrayList<Node>afterSortArrayList=new ArrayList<Node>();
		for (int i = 0; i < polarAngleArray.length; i++) {
			double polarAngle=polarAngleArray[i];
			afterSortArrayList.add(nodePolarAngleMap.get(polarAngle));
		}
	
		retuRoadJuncArrayList=null;
		retuRoadJuncArrayList=new ArrayList<retuNode>();//清空要返回的道路交叉点数组
		
		for (int i = 0; i < afterSortArrayList.size(); i++) {
			retuNode tempRoadJcunc=new retuNode();
			tempRoadJcunc.L=afterSortArrayList.get(i).x;
			tempRoadJcunc.B=afterSortArrayList.get(i).y;
			tempRoadJcunc.ID=afterSortArrayList.get(i).EID;
			retuRoadJuncArrayList.add(tempRoadJcunc);
		}	
		
	}
	
	/*计算方向余弦，返回两射线夹角（极角，弧度）,包含方向
	 * centNode:原点，接警点作为原点
	 * starNode：参考线段点
	 * endNode：终点线段点*/
	public double retuPolarAngle(Node centNode,Node starNode,Node endNode)
	{	
		double polarAngle=0;		
		try {
			if (isTheSamePoint(starNode, endNode)) {
				polarAngle=0;
			}
			else {
				double centNodeX=coordinateTransGetx(centNode);
				double centNodeY=coordinateTransGety(centNode);
				double centNodeZ=coordinateTransGetz(centNode);			
				double starNodeX=coordinateTransGetx(starNode);
				double starNodeY=coordinateTransGety(starNode);
				double starNodeZ=coordinateTransGetz(starNode);			
				double endNodeX=coordinateTransGetx(endNode);
				double endNodeY=coordinateTransGety(endNode);
				double endNodeZ=coordinateTransGetz(endNode);
							
				//参考线段向量 ，模     终点线段向量，模
				double csNodeDis=distance(centNode,starNode);
				double ceNodeDis=distance(centNode,endNode);
				
				double csDeltX=starNodeX-centNodeX;
				double csDeltY=starNodeY-centNodeY;
				double csDeltZ=starNodeZ-centNodeZ;
			
				double ceDeltX=endNodeX-centNodeX;
				double ceDeltY=endNodeY-centNodeY;
				double ceDeltZ=endNodeZ-centNodeZ;			
				//两向量夹角
				double angle=Math.acos((csDeltX*ceDeltX+csDeltY*ceDeltY+csDeltZ*ceDeltZ)/(csNodeDis*ceNodeDis));
				//三维向量叉乘，正值表示在参考线段左侧，负值表示在右侧
				double chaCheng=csDeltY*ceDeltZ-csDeltZ*ceDeltY+csDeltX*ceDeltY-csDeltY*ceDeltX+csDeltZ*ceDeltX-csDeltX*ceDeltZ;
				int signal=1;
				//只取得符号
				if (chaCheng<0) {
					signal=-1;
				}
				polarAngle=angle*signal;
			}		
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();

		}
		return polarAngle;		
	}	
		
	/*寻找拦截点(intercept)方法
	 * 约束条件：
	 * 1.距离约束：下一点距起点的距离大于上一点距离,设置20米限差
	 * 2.方向约束：搜寻与终点在同一侧的点，即搜寻点、起点连线与起终点连线夹角<90/120度，起点为警情点
	 * 3.跨度约束：搜寻点距搜寻方向直线的距离，设为500米
	 * 4.最小投影距离约束:搜寻路线点到方向直线投影距离最小
	 * 5.方向投影长度约束：搜寻方向直线
	 * distanc为外包正方形内切圆半径
	 * relationEdgesMap为点的邻接边
	 * cNode: 当前的起始节点currentNode
	 * pNode: 当前起始节点的上一节点previousNode
	 * sNode: 最初的起始节点startNode
	 * eNode: 终点endNode*/
	 public boolean haveFindPath=false;
	 public boolean getIntePoint(String pcsName,Node jjNode,double distance,Node cNode,Node pNode, Node sNode, Node eNode) {		 
		 Node nNode = null;
		/* 如果符合条件判断说明出现环路，不能再顺着该路径继续寻路，返回false */
		try {
			if (cNode != null && pNode != null && cNode.EID == pNode.EID)
				return false;
			//当前节点不为空
			if (cNode != null) {
				if (cNode.EID == 3291) {
					System.out.print(cNode.EID);
				}
				if (cNode.EID == 3180) {
					System.out.print(cNode.EID);
				}
				if (cNode.EID == 3162) {
					System.out.print(cNode.EID);
				}
				int i = 0;
				/* 如果该起始节点就是终点，说明找到一条路径 */
				if (cNode.EID == eNode.EID)				
				{
					stack.push(cNode);
					/* 如果路径长度pathLength>distance符合条件则打印输出该路径，返回true
					 * 2013/11/30修改 */
					//存储路径节点	
					getPath(nodesPathArrayList, cNode);
					int nodePathCount = nodesPathArrayList.size();
					Node[]nodesPath=new Node[nodePathCount];
					for(int p=0;p<nodePathCount;p++){
						nodesPath[p]=nodesPathArrayList.get(p);			
					}
					double pathLength = distance(nodesPath[0], nodesPath[1]) ;//存储路径长度，初始值为警情点到第一个点的距离
					boolean isPath=false;
					for(int q=1;q<nodePathCount-1;q++){
						ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//存储当前节点邻接边
						relaEdgesArrayList=nodesPath[q].relationEdges;
						int relaEdgesCount=relaEdgesArrayList.size();//存储当前节点邻接边数
						for (int r = 0; r < relaEdgesCount; r++) {
							ArrayList<Node>tempEdgeNodeArrayList=new ArrayList<Node>();//当前邻接边上点集合
							Edge relaEdge=new Edge();
							relaEdge=relaEdgesArrayList.get(r);
							double relaEdgeLength=relaEdge.edgeLength;//当前边的长度
							tempEdgeNodeArrayList=relaEdge.getPointCollArrayList();
							int edgeNodeCount=tempEdgeNodeArrayList.size();//当前邻接边上点数
							//起点、终点
							Node starNode=new Node();
							Node endNode=new Node();
							starNode=tempEdgeNodeArrayList.get(0);
							endNode=tempEdgeNodeArrayList.get(edgeNodeCount-1);
							//检索当前路径并存储当前路径长度
							if (isTheSamePoint(starNode, nodesPath[q+1])||isTheSamePoint(endNode, nodesPath[q+1])){
								pathLength=pathLength+relaEdgeLength;
								break;
							}						
						}
						//2014/1/11修改
						//取得路径上最后一个节点最长临接边，根据路径长度，判断该路径是否输出
						if (q==nodePathCount-2) {
							int index=nodePathCount-1;//最后一个节点
							relaEdgesArrayList=nodesPath[index].relationEdges;
							relaEdgesCount=relaEdgesArrayList.size();//存储当前节点邻接边数
							for (int t = 0; t < relaEdgesCount; t++) {
								Edge relaEdge=new Edge();
								relaEdge=relaEdgesArrayList.get(t);
								double relaEdgeLength=relaEdge.edgeLength;//当前边的长度
								if (pathLength+relaEdgeLength>distance) {
									isPath=true;
									haveFindPath=true;
									break;
								}								
							}							
						}
					}
					//不管该路径长度是否符合条件都要返回true，表示两点间寻路结束，
					//如果路径长度符合条件，则输出该路径
					if (isPath) {
						showAndSavePath(pcsName,distance,jjNode);
					}	
					return true;
				}				
				/* 如果不是,继续寻路 */
				else
				{		
					//索引超限
					if (cNode.getRelationNodes().size() <= i) {
						return false;
					}
					/* 从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点
					 * 作为下一次递归寻路时的起始节点 
					 */
					nNode = cNode.getRelationNodes().get(i);			
					while(nNode != null){
						if (nNode.EID == 3235) {
							System.out.print(cNode.EID);
						}
						//如果此处为目标点，应当输出路径
	
						if (isDistanceSatis(jjNode, cNode, nNode) && isTheSameDirection(jjNode, eNode,180, nNode)
								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
							//保证不会产生环路
//							if ( nNode.EID != sNode.EID && !isNodeInStack(nNode)) {	
							if ( nNode.EID != sNode.EID ) {	
								//查找开启列表中是否存在该点,若存在返回该点索引，否则返回-1
								if (nNode.EID == 3621) {
									System.out.print(nNode.EID);
								}
								int indexInOpenlist = isListContains(openList, nNode);
								int indexInStack = -1;//在关闭列表中的索引
								if (indexInOpenlist == -1) {
									indexInStack = isStackContains(stack,nNode);
								}								
								double cost = distance(nNode, cNode);//到下一节点的代价
								//如果存在，G值是否更小，即是否更新G，F值
								if (indexInOpenlist != -1){
									if((cNode.getG()+cost) < openList.get(indexInOpenlist).getG()){
							            nNode.setParentNode(cNode);
							            countG(nNode, eNode, cost);
							            countF(nNode);
							            openList.set(indexInOpenlist, nNode);  
							        }
									i++;
						            if(i >= cNode.getRelationNodes().size()){
						            	nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}
								}
								//如果在关闭列表stack中
								else if(indexInStack != -1){
									if((cNode.getG()+cost) < stack.get(indexInStack).getG()){
							            nNode.setParentNode(cNode);
							            countG(nNode, eNode, cost);
							            countF(nNode);  
							            openList.add(nNode);
							        }
									i++;
						            if(i >= cNode.getRelationNodes().size()){
						            	nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}									
								}
								else {
									//添加到开启列表中								
									nNode.setParentNode(cNode);//父节点								
							        count(nNode, eNode, cost);
							        openList.add(nNode);
							        i++;
						            if(i >= cNode.getRelationNodes().size()){
										nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}
								}								
							}							
							else {
								i++;
								if(i >= cNode.getRelationNodes().size()){
									nNode = null;
									break;
								}
								else {
									nNode = cNode.getRelationNodes().get(i);
								}				
							}					
						}
						//否则，继续寻找相邻点
						else {
							i++;
							if(i >= cNode.getRelationNodes().size()){
								nNode = null;
								break;
							}
				            else {
				            	nNode = cNode.getRelationNodes().get(i);
							}
						}					
					}
					//从开启列表中删除F最小的node
			        //添加到stack中
					if (openList.size() >=1 ) {
						stack.push(openList.remove(0));
					}					
					//开启列表中排序，把F值最小的放到最顶端
					Collections.sort(openList, new nodeFComparator());
					//取得F值最小的点
					if (openList.size() >=1 ) {
						nNode = openList.get(0);
						if (nNode.EID == 3192) {
							System.out.print("\n");
						}
					}
					if(getIntePoint(pcsName,jjNode,distance,nNode, cNode,sNode,eNode)){
						return true;
					}
					else{
						return false;
					}								
				}
			}
			else
				return false;
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;			
		}		
	 }
	 
	 /*搜寻最短路径方法*/
	 public boolean getShortestPath(String pcsName,Node jjNode,double distance,Node cNode,Node pNode, Node sNode, Node eNode) {		 
		 Node nNode = null;
		/* 如果符合条件判断说明出现环路，不能再顺着该路径继续寻路，返回false */
		try {
			if (cNode != null && pNode != null && cNode.EID == pNode.EID)
				return false;
			//当前节点不为空
			if (cNode != null) {
				if (cNode.EID == 3235) {
					System.out.print(cNode.EID);
				}
				for (int q = 0; q < endNodeArrayList.size(); q++) {
					Node tNode = new Node();
					tNode = endNodeArrayList.get(q);
					//终端节点插入拓扑关系
					if (cNode.EID == tNode.EID) {
						//此处用cNode = tNode是错误的，这里为地址引用
						cNode.setRelationEdge(tNode.relationEdges);
						cNode.setRelationNodes(tNode.relationNodes);	
					}
				}
				int i = 0;
				/* 如果该起始节点就是终点，说明找到一条路径 */
				if (cNode.EID == eNode.EID)				
				{
					stack.push(cNode);
					/* 如果路径长度pathLength>distance符合条件则打印输出该路径，返回true
					 * 2013/11/30修改 */
					//存储路径节点
					getPath(nodesPathArrayList, cNode);	
					showShortestPath(pcsName,distance,jjNode);
					return true;
				}				
				/* 如果不是,继续寻路 */
				else
				{		
					//索引超限
					if (cNode.getRelationNodes().size() <= i) {
						return false;
					}
					/* 从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点
					 * 作为下一次递归寻路时的起始节点 
					 */
					nNode = cNode.getRelationNodes().get(i);					
					while(nNode != null){
						if (nNode.EID == 3235) {
							System.out.print(cNode.EID);
						}
						//如果此处为目标点，应当输出路径
	
//						if (isDistanceSatis(jjNode, cNode, nNode) && isTheSameDirection(jjNode, eNode,180, nNode)
//								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
						if (isTheSameDirection(jjNode, eNode,180, nNode)
								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
							//保证不会产生环路
							if ( nNode.EID != sNode.EID ) {	
								//查找开启列表中是否存在该点,若存在返回该点索引，否则返回-1
								if (nNode.EID == 3621) {
									System.out.print(nNode.EID);
								}
								int indexInOpenlist = isListContains(openList, nNode);
								int indexInStack = -1;//在关闭列表中的索引
								if (indexInOpenlist == -1) {
									indexInStack = isStackContains(stack,nNode);
								}								
								double cost = distance(nNode, cNode);//到下一节点的代价
								//如果存在，G值是否更小，即是否更新G，F值
								if (indexInOpenlist != -1){
									if((cNode.getG()+cost) < openList.get(indexInOpenlist).getG()){
							            nNode.setParentNode(cNode);
							            countG(nNode, eNode, cost);
							            countF(nNode);
							            openList.set(indexInOpenlist, nNode);  
							        }
									i++;
						            if(i >= cNode.getRelationNodes().size()){
						            	nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}
								}
								//如果在关闭列表stack中
								else if(indexInStack != -1){
									if((cNode.getG()+cost) < stack.get(indexInStack).getG()){
							            nNode.setParentNode(cNode);
							            countG(nNode, eNode, cost);
							            countF(nNode);  
							            openList.add(nNode);
							        }
									i++;
						            if(i >= cNode.getRelationNodes().size()){
						            	nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}									
								}
								else {
									//添加到开启列表中								
									nNode.setParentNode(cNode);//父节点								
							        count(nNode, eNode, cost);
							        openList.add(nNode);
							        i++;
						            if(i >= cNode.getRelationNodes().size()){
										nNode = null;
										break;
									}
						            else {
						            	nNode = cNode.getRelationNodes().get(i);
									}
								}								
							}							
							else {
								i++;
								if(i >= cNode.getRelationNodes().size()){
									nNode = null;
									break;
								}
								else {
									nNode = cNode.getRelationNodes().get(i);
								}				
							}					
						}
						//否则，继续寻找相邻点
						else {
							i++;
							if(i >= cNode.getRelationNodes().size()){
								nNode = null;
								break;
							}
				            else {
				            	nNode = cNode.getRelationNodes().get(i);
							}
						}					
					}
					//从开启列表中删除F最小的node
			        //添加到stack中
					if (openList.size() >=1 ) {
						stack.push(openList.remove(0));
					}					
					//开启列表中排序，把F值最小的放到最顶端
					Collections.sort(openList, new nodeFComparator());
					//取得F值最小的点
					if (openList.size() >=1 ) {
						nNode = openList.get(0);
					}
					if(getShortestPath(pcsName,jjNode,distance,nNode, cNode,sNode,eNode)){
						return true;
					}
					else{
						return false;
					}								
				}
			}
			else
				return false;
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;			
		}		
	 }
	 
	 
	 /*返回节点组成的路径,从终点往返回到起点*/
	 private void getPath(List<Node> nodesPath,Node node){
	    if(node.getParentNode()!=null){
	        getPath(nodesPath, node.getParentNode());
	    }
	    nodesPath.add(node);
	}
	 
	 /*从当前节点到下一节点的代价
	  * cNode:当前节点
	  * node:下一节点*/
	public double calculateCost(Node cNode, Node node){
		double cost = 0;
		ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//存储当前节点邻接边
		relaEdgesArrayList=cNode.relationEdges;
		int relaEdgesCount=relaEdgesArrayList.size();//存储当前节点邻接边数
		for (int r = 0; r < relaEdgesCount; r++) {
			ArrayList<Node>tempEdgeNodeArrayList=new ArrayList<Node>();//当前邻接边上点集合
			Edge relaEdge=new Edge();
			relaEdge=relaEdgesArrayList.get(r);
			double relaEdgeLength=relaEdge.edgeLength;//当前边的长度
			tempEdgeNodeArrayList=relaEdge.getPointCollArrayList();
			int edgeNodeCount=tempEdgeNodeArrayList.size();//当前邻接边上点数
			//起点、终点
			Node starNode=new Node();
			Node endNode=new Node();
			starNode=tempEdgeNodeArrayList.get(0);
			endNode=tempEdgeNodeArrayList.get(edgeNodeCount-1);
			//检索当前路径并存储当前路径长度
			if (isTheSamePoint(starNode, node) || isTheSamePoint(endNode, node)){
				cost = relaEdgeLength;
				break;
			}						
		}
		return cost;
	 }
	
	 //计算G,H,F值
	 private void count(Node node, Node eNode, double cost){
	     countG(node, eNode, cost);
	     countH(node, eNode);
	     countF(node);
	 }
	
	 //计算G值
	 private void countG(Node node, Node eNode, double cost){
	     if(node.getParentNode()==null){
	         node.setG(cost);
	     }else{
	         node.setG(node.getParentNode().getG() + cost);
	     }
	 }
	
	 //计算H值
	 private void countH(Node node, Node eNode){
	     double dist = distance(node, eNode);
	     node.setH(dist);
	 }
	
	 //计算F值
	 private void countF(Node node){
	     node.setF(node.getG()+node.getH());
	 }	
	 
	 /*开放列表是否包含该点,(-1：没有找到，否则返回所在的索引)*/
	 public int isListContains(ArrayList<Node> openList, Node node){
		 boolean isOk = false;
		 for(int i = 0; i < openList.size(); i++){
		    Node tNode = openList.get(i);
	        if(tNode.EID == node.EID){
	        	return i;
	        }
        }
		return -1;
	}
	 
	 public int isStackContains(Stack<Node> tempStack, Node node){
		 Iterator<Node> iter = tempStack.iterator();
		 int index = -1;
		 boolean isInStack = false;
		 while (iter.hasNext()) {
			 index++;
			 Node tNode = (Node) iter.next();
			 if (node.EID == tNode.EID) {
				isInStack = true;
				return index;
			}
		 }
		 if (isInStack) {
			 return index;
		 }
		 else {			 
			return -1;
		 }		 
	  }
	 
	 
	 
	 /*回溯：当前节点的系列邻接点是否满足条件
	  * 当前节点的最优系列节点不能远离方向直线，否则此节点不符合条件
	  * jjNode:接警点
	  * eNode：寻路终点
	  * nNode：当前节点*/
	 public int tempCount=0;//计算深度 3次,控制循环次数
	 public int farAwaryCount=0;//连续远离方向直线次数
	 public boolean isRelaNodeSatisfy(Node jjNode,Node eNode,Node nNode)
	 {
		 tempCount++;
		 boolean isFarAway=false;
		 ArrayList<Node> relaNodeArrayList = new ArrayList<Node>();
		 relaNodeArrayList = nNode.getRelationNodes();
		 if (relaNodeArrayList.size() == 0 || relaNodeArrayList == null) {
			return false;
		}
		 else {
			 boolean isFindMinNode=false;//是否找到投影距离最小点
			 for (int i = 0; i < relaNodeArrayList.size(); i++) {
				 Node tempNode = new Node();
				 tempNode = relaNodeArrayList.get(i);
				 if (isMinDistance(jjNode, eNode ,nNode, tempNode)) {
					isFindMinNode = true;
					if (projDistance(jjNode, eNode, tempNode) > projDistance(jjNode, eNode, nNode)) {
						farAwaryCount++;
						isFarAway=true;
						break;
					}
				}
				 else {
					continue;
				}
			}			 								 
		}
//		 //连续三次都远离投影直线，返回false，说明不符合条件
//		 if (farAwaryCount >=3) {
//				return false;
//			}
//			 else {
//				return true;
//			}
		 if (isFarAway) {
			return false;
		}
		 else {
			return true;
		}
	 }
	 
	 /*点到方向直线的投影距离*/
	 public double projDistance(Node jjNode,Node endNode,Node node)
	 {
		 double dist=0;
		 try {
				double jjNodeX=coordinateTransGetx(jjNode);
				double jjNodeY=coordinateTransGety(jjNode);
				double jjNodeZ=coordinateTransGetz(jjNode);
				
				double endNodeX=coordinateTransGetx(endNode);
				double endNodeY=coordinateTransGety(endNode);
				double endNodeZ=coordinateTransGetz(endNode);
				
				double nodeX=coordinateTransGetx(node);
				double nodeY=coordinateTransGety(node);
				double nodeZ=coordinateTransGetz(node);
				
				//起终点向量 ，模
				double jeNodeDis=distance(jjNode, endNode);
				double jeDeltX=endNodeX-jjNodeX;
				double jeDeltY=endNodeY-jjNodeY;
				double jeDeltZ=endNodeZ-jjNodeZ;
				
				//起点以及与当前点向量，模
				double snDeltX=nodeX-jjNodeX;
				double snDeltY=nodeY-jjNodeY;
				double snDeltZ=nodeZ-jjNodeZ;
				double snNodeDis=distance(jjNode, node);
				
				//起终点向量与起点当前点向量 两向量夹角
				double angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
				//当前点到起终点距离
				dist=snNodeDis*Math.sin(angle);				
			}
			catch (Exception e) {
				e.printStackTrace();
			}		 
		 return dist;
	 }
	 
	 
	 //节点ID是否曾作为最优节点使用过
	 public boolean isNodeIDUsed(Node node,ArrayList<Integer>nodeIDArrayList)
	 {
		 boolean isUsde=false;
		 for (int i = 0; i < nodeIDArrayList.size(); i++) {
				int nodeID=nodeIDArrayList.get(i);
				if (node.EID==nodeID) {
					isUsde=true;
					break;
				}
			}
			if (isUsde) {
				return true;
			}
			else {
				return false;
			}
	 }
	 
	 /*搜寻路线的当前点是否与起终点在同一方向
	  * 搜寻在搜寻范围弧度内的路线，
	  * 判断：起终点向量与起点当前点向量间的夹角与搜寻范围弧度的1/2比较
	  * 若两向量夹角angle<sxhd搜寻范围弧度，说明该点符合条件
	  * starNode:起点
	  * endNode：终点
	  * cnode:搜寻当前点
	  * a:搜寻范围度数，要转化为弧度*/
	public boolean isTheSameDirection(Node starNode,Node endNode,int a,Node cNode)
	{		
		double pi=Math.PI;
		double sxhd=a*pi/180;//搜寻范围弧度
		try {
			if (cNode.EID == endNode.EID) {
				return true;
			}
			else {
				double starNodeX=coordinateTransGetx(starNode);
				double starNodeY=coordinateTransGety(starNode);
				double starNodeZ=coordinateTransGetz(starNode);
				double endNodeX=coordinateTransGetx(endNode);
				double endNodeY=coordinateTransGety(endNode);
				double endNodeZ=coordinateTransGetz(endNode);
				double cNodeX=coordinateTransGetx(cNode);
				double cNodeY=coordinateTransGety(cNode);
				double cNodeZ=coordinateTransGetz(cNode);
				
				//起终点向量 ，模      起点当前点向量，模
				double seNodeDis=distance(starNode, endNode);
				double scNodeDis=distance(starNode, cNode);
				double seDeltX=endNodeX-starNodeX;
				double seDeltY=endNodeY-starNodeY;
				double seDeltZ=endNodeZ-starNodeZ;
				
				double scDeltX=cNodeX-starNodeX;
				double scDeltY=cNodeY-starNodeY;
				double scDeltZ=cNodeZ-starNodeZ;
				//两向量夹角
				//如果当前点与终点相同，angle会返回null
				double angle=Math.acos((seDeltX*scDeltX+seDeltY*scDeltY+seDeltZ*scDeltZ)/(seNodeDis*scNodeDis));
				if (angle<sxhd/2) {
					return true;
				}
				else {
					return false;
				}
			}			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/*搜寻点是否在搜寻起终点直线跨度范围内
	 * 计算：varSpan:起点当前点向量模乘以与起终点方向向量夹角的正弦值
	 * 判断：若搜寻点距搜寻方向直线的距离varSpan<=span，则返回true，否则返回false
	 * starNode:起点
	 * endNode：终点
	 * cnode:搜寻当前点
	 * span：搜寻跨度*/
	public boolean isInSpanDistance(Node starNode,Node endNode,double constSpan,Node cNode)
	{
		double angle = 0;
		double varSpan = 0;
		try {
			if (endNode.EID == cNode.EID) {
				angle = 0;
				varSpan = 0;
			}
			else {
				double starNodeX=coordinateTransGetx(starNode);
				double starNodeY=coordinateTransGety(starNode);
				double starNodeZ=coordinateTransGetz(starNode);
				double endNodeX=coordinateTransGetx(endNode);
				double endNodeY=coordinateTransGety(endNode);
				double endNodeZ=coordinateTransGetz(endNode);
				double cNodeX=coordinateTransGetx(cNode);
				double cNodeY=coordinateTransGety(cNode);
				double cNodeZ=coordinateTransGetz(cNode);
				
				//起终点向量 ，模      起点当前点向量，模
				double seNodeDis=distance(starNode, endNode);
				double scNodeDis=distance(starNode, cNode);
				double seDeltX=endNodeX-starNodeX;
				double seDeltY=endNodeY-starNodeY;
				double seDeltZ=endNodeZ-starNodeZ;
				
				double scDeltX=cNodeX-starNodeX;
				double scDeltY=cNodeY-starNodeY;
				double scDeltZ=cNodeZ-starNodeZ;
				
				//两向量夹角
				angle=Math.acos((seDeltX*scDeltX+seDeltY*scDeltY+seDeltZ*scDeltZ)/(seNodeDis*scNodeDis));
				varSpan=scNodeDis*Math.sin(angle);
			}			
			//跨度						
			if (varSpan <= constSpan) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/*满足距离约束、方向约束、跨度约束的条件下
	 * 当前节点直接相邻的节点中，到方向直线的投影是否为最小
	 * starNode:起点，接警点
	 * endNode：终点
	 * cnode:搜寻当前点
	 * node：与当前点直接相邻的点*/
	public boolean isMinDistance(Node jjNode,Node endNode,Node cNode,Node node){
		try {
			double jjNodeX=coordinateTransGetx(jjNode);
			double jjNodeY=coordinateTransGety(jjNode);
			double jjNodeZ=coordinateTransGetz(jjNode);
			
			double endNodeX=coordinateTransGetx(endNode);
			double endNodeY=coordinateTransGety(endNode);
			double endNodeZ=coordinateTransGetz(endNode);
			
			double nodeX=coordinateTransGetx(node);
			double nodeY=coordinateTransGety(node);
			double nodeZ=coordinateTransGetz(node);
			
			//起终点向量 ，模      起点当前点向量，模
			double jeNodeDis=distance(jjNode, endNode);
			double jeDeltX=endNodeX-jjNodeX;
			double jeDeltY=endNodeY-jjNodeY;
			double jeDeltZ=endNodeZ-jjNodeZ;
			
			//起点以及与当前点直接相连点向量，模
			double snDeltX=nodeX-jjNodeX;
			double snDeltY=nodeY-jjNodeY;
			double snDeltZ=nodeZ-jjNodeZ;
			double snNodeDis=distance(jjNode, node);
			
			//起终点向量与起点当前点直接相连点向量 两向量夹角
			double angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
			//直接相邻点投影距离
			double projDis=snNodeDis*Math.sin(angle);
			
			ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();
			relaNodeArrayList=cNode.relationNodes;
			//判断与当前点直接相邻点到方向直线的投影在所有相邻点投影中是否为最小
			for (int i = 0; i < relaNodeArrayList.size(); i++) { 
				Node tempNode=new Node();
				tempNode=relaNodeArrayList.get(i);
				double tempNodeX=coordinateTransGetx(tempNode);
				double tempNodeY=coordinateTransGety(tempNode);
				double tempNodeZ=coordinateTransGetz(tempNode);
				double jtNodeDis=distance(jjNode, tempNode);
				double jtDeltX=tempNodeX-jjNodeX;
				double jtDeltY=tempNodeY-jjNodeY;
				double jtDeltZ=tempNodeZ-jjNodeZ;
				
				//满足距离约束20米限差、方向约束、跨度约束
				if (isDistanceSatis(jjNode, cNode, tempNode)&&isTheSameDirection(jjNode, endNode, 180, tempNode)
						&&isInSpanDistance(jjNode, endNode,1500, tempNode)) {
					//夹角以及投影
					double tempAngle=Math.acos((jeDeltX*jtDeltX+jeDeltY*jtDeltY+jeDeltZ*jtDeltZ)/(jeNodeDis*jtNodeDis));
					double tempProjDis=jtNodeDis*Math.sin(tempAngle);				
					if (tempProjDis<projDis) {				
						return false;//说明不是最小距离
					}
					else {
						continue;
					}				
				}
				else {
					continue;
				}				
			}
			return true;
		}
		catch (Exception e) {
			System.out.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/*距离是否满足约束条件
	 * 距离限差 :200米
	 * jjNode:接警点
	 * cNode：当前点
	 * relaNode：当前点直接相连点*/
	public boolean isDistanceSatis(Node jjNode,Node cNode,Node relaNode)
	{
		if (distance(jjNode, cNode) < distance(jjNode, relaNode) + 200) {
			return true;
		}
		else {
			return false;
		}		
	}

	/*方向投影长度是否满足条件，投影长度必须在起点与终点之间
	 * 当前节点与警情点在警情点、终点直线上的投影距离小于警情点、终点之间的距离
	 *jjNode:接警点
	 *endNode:寻路终点
	 *node：当前节点 */
	public boolean isDirectProjSatis(Node jjNode,Node endNode,Node node)
	{
		double angle = 0;
		double projDis = 0;
		double jeNodeDis = 0;
		try {
			if (endNode.EID == node.EID) {
				angle = 0;
				projDis = distance(jjNode, endNode);
				jeNodeDis = distance(jjNode, endNode);
			}
			else {
				double jjNodeX=coordinateTransGetx(jjNode);
				double jjNodeY=coordinateTransGety(jjNode);
				double jjNodeZ=coordinateTransGetz(jjNode);
				
				double endNodeX=coordinateTransGetx(endNode);
				double endNodeY=coordinateTransGety(endNode);
				double endNodeZ=coordinateTransGetz(endNode);
				
				double nodeX=coordinateTransGetx(node);
				double nodeY=coordinateTransGety(node);
				double nodeZ=coordinateTransGetz(node);
				
				//起终点向量 ，模      起点当前点向量，模
				jeNodeDis=distance(jjNode, endNode);
				double jeDeltX=endNodeX-jjNodeX;
				double jeDeltY=endNodeY-jjNodeY;
				double jeDeltZ=endNodeZ-jjNodeZ;
				
				//起点以及与当前点向量，模
				double snDeltX=nodeX-jjNodeX;
				double snDeltY=nodeY-jjNodeY;
				double snDeltZ=nodeZ-jjNodeZ;
				double snNodeDis=distance(jjNode, node);
				
				//起终点向量与起点当前点向量 两向量夹角
				angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
				//投影距离
				projDis=snNodeDis*Math.cos(angle);
			}	
			//angle可能为负值
			if (projDis > 0 && projDis<=jeNodeDis) {
				return true;
			}
			else {
				return false;
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
		
	/* 判断节点是否在栈中 */
	public boolean isNodeInStack(Node node)
	{
		Iterator<Node> it = stack.iterator();
		while (it.hasNext()) {
			Node node1 = (Node) it.next();
			if (node.EID == node1.EID)
				return true;
		}
		return false;
	}

	
	/* 此时栈中的节点组成一条所求路径 */
	public void showAndSavePath (String pcsName,double circle ,Node jjNode )
	{
		try {
			ArrayList<Node>nodeArrayList =new ArrayList<Node>();//顺序存储当前路径节点
			Node[]nodesPath=new Node[nodesPathArrayList.size()];//nodesPath第一个元素为jjNode
			for (int i = 0; i < nodesPathArrayList.size(); i++) {
				nodesPath[i] = nodesPathArrayList.get(i);
			}
			
			boolean isPath=false;
			//只检索到一个节点的路径，特殊处理
			if (nodesPath.length==1) {	
				isPath=true;
			}
			for (int i = 0; i < nodesPath.length-1; i++) {
				//离警情点距离逐渐增大，可行路径，距离约束设置20米限差
				if (distance(jjNode, nodesPath[i+1]) + 200 > distance(jjNode, nodesPath[i])) {
					isPath=true;
				}
				else {
					isPath=false;
					break;
				}
			} 
			/*如果是可行路径则存储并输出
			 * 由节点ID检索relationEdgesMap对应边，检索对应边polyline
			 * 计算路径
			 * 累计路径长度，若tempAccuLength>=circle,则取另存当前polyline并跳出，不必在继续检索
			 * 若tempAccuLength<circle:该路径不符合条件*/			
			int EdgeCount=gInstance.allRelationEdgesMap.get(pcsName).size();
			if (isPath) {
				//加入警情点
				nodeArrayList.add(jjNode);						
																			
				double befoCurAccuLength=0;//累计到当前polyline之前的长度
				double tempAccuLength = distance(nodesPath[0], nodesPath[1]);//累计到当前polyline长度，初始值为警情点所在面到交叉点的距离
				ArrayList<Node> pLineArrayList=new ArrayList<Node>();//存储最后一段polyline中Node信息
				for (int j = 1; j < nodesPath.length-1; j++) {
					int EID=nodesPath[j].EID;//节点ID
					nodeArrayList.add(nodesPath[j]);
					//搜寻路径节点相邻边Edge

					ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//存储节点邻接边												
					relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
					int relaEdgeCount=relaEdgeArraylist.size();//节点相邻边数
					
					//检索相邻边				
					for (int k = 0; k < relaEdgeCount; k++) {
						Edge edge=new Edge();
						edge=relaEdgeArraylist.get(k);									

						Node froNode=new Node();
						Node toNode=new Node();
						ArrayList<Node>edgePointCollArrayList=new ArrayList<Node>();
						edgePointCollArrayList=edge.getPointCollArrayList();
						int pointCount=edgePointCollArrayList.size();
						froNode.setX(edgePointCollArrayList.get(0).getX());
						froNode.setY(edgePointCollArrayList.get(0).getY());
						toNode.setX(edgePointCollArrayList.get(pointCount-1).getX());
						toNode.setY(edgePointCollArrayList.get(pointCount-1).getY());					
						
						//取得邻接polyline的vertex
						//获得与当前点直接相连的边				
						if (isTheSamePoint(nodesPath[j+1], froNode)||isTheSamePoint(nodesPath[j+1], toNode)) {
							double length=0;//当前polyline的长度
							//不能用froPoint，toPoint求距离，因可能为曲线
							for (int p = 0; p < pointCount-1; p++) {
								
								Node t1Node=new Node();
								Node t2Node=new Node();
								t1Node=edgePointCollArrayList.get(p);
								t2Node=edgePointCollArrayList.get(p+1);
								length=length+distance(t1Node, t2Node);	
															
							}
							tempAccuLength=tempAccuLength+length;
							befoCurAccuLength=tempAccuLength-length;							

							//首尾点与节点重合，去掉首尾点，只取中间点
							//若当前节点与plinePoints的首点相同，则按此顺序增加point
							//否则倒序增加point
							Node tempSNode=new Node();//首点
							Node tempENode=new Node();//尾点
							tempSNode=edgePointCollArrayList.get(0);
							tempENode=edgePointCollArrayList.get(pointCount-1);
							
							if (isTheSamePoint(nodesPath[j], tempSNode)) {
								
								if (tempAccuLength<circle) {
									for (int l = 1; l < pointCount-1; l++) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										nodeArrayList.add(tNode);	
									}																						
							    }
								//超出拦截距离，只存储最后一段polyline信息
								else {
									
									//存储道路交叉拦截点，应是拦截距以后的道路交叉拦截点
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(nodesPath[j+1]);
									}
									//去掉相同的道路交叉拦截点									
									else {
										//已存在道路交叉拦截点与新道路交叉拦截点比较，以确定是否为同一道路交叉拦截点
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//如果是同一点，则无效，跳出循环
											if (isTheSamePoint(nodesPath[j+1], ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(nodesPath[j+1]);
										}						
									}
									
									/*特列：若polyline只存在两个点，即pointCount=2,执行该句子，不再执行for循环*/
									if (pointCount==2) {
										pLineArrayList.add(tempSNode);
										pLineArrayList.add(tempENode);
										
									}
									for (int l = 1; l < pointCount-1; l++) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										pLineArrayList.add(tNode);//存储最后一段polyline信息
										//最后polyline存储所有point
										if (l==pointCount-2) {
											
											tNode=edgePointCollArrayList.get(0);
											pLineArrayList.add(0, tNode);//第一个位置插入该点
											tNode=edgePointCollArrayList.get(pointCount-1);
											pLineArrayList.add(tNode);
											
										}
									}
								}								
							}
							
							//与尾点相同
							else if (isTheSamePoint(nodesPath[j], tempENode)) {
								if (tempAccuLength<circle) {
									for (int l =pointCount-2; l>0 ; l--) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										nodeArrayList.add(tNode);
									}
								}
								//超出拦截距离，只存储最后一段polyline信息
								else  {
									
									//存储道路交叉拦截点，应是拦截距以后的道路交叉拦截点
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(nodesPath[j+1]);
									}
									//去掉相同的道路交叉拦截点									
									else {
										//已存在道路交叉拦截点与新道路交叉拦截点比较，以确定是否为同一道路交叉拦截点
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//如果是同一点，则无效，跳出循环
											if (isTheSamePoint(nodesPath[j+1], ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(nodesPath[j+1]);
										}						
									}

									
									/*特列：若polyline只存在两个点，即pointCount=2,执行该句子，不再执行for循环*/
									if (pointCount==2) {
										pLineArrayList.add(tempSNode);
										pLineArrayList.add(tempENode);
									}
									for (int l =pointCount-2; l>0 ; l--) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										pLineArrayList.add(tNode);//存储最后一段polyline信息
										
										//最后polyline存储所有point
										if (l==1) {
											tNode=edgePointCollArrayList.get(pointCount-1);
											pLineArrayList.add(0,tNode);//第一个位置插入该点
											tNode=edgePointCollArrayList.get(0);
											pLineArrayList.add(tNode);
										}
									}
								}																
							}							
							break;//检索到邻接polyline，跳出
						}				
					}
					if (tempAccuLength>=circle) {
						//当前节点满足条件，不必继续搜索，跳出循环求拦截点					
						break;
					}				
				}				
								
				/*获取与nodesPath最后一点直接相邻的边，
				 * 用距离约束搜索方向
				 * 最后一点由距离约束可能检索到多条polyline,用map分别存储*/					
				Map<Integer, ArrayList<Node>> plineMap=new HashMap<Integer, ArrayList<Node>>();//存储路径ID与路径
				Map<Integer, java.lang.Double>pathDistanceMap=new HashMap<Integer, java.lang.Double>();//存储路径ID与路径长度

				int mapInt=0;//检索到符合条件polyline数目，并作为ID与pLineArrayList关联
//				Integer mapInt=0;//检索到polyline数目，并作为ID与pLineArrayList关联
				if (tempAccuLength<circle) {
					int EID=nodesPath[nodesPath.length-1].EID;//最后节点ID 
					nodeArrayList.add(nodesPath[nodesPath.length-1]);//添加最后节点	
					
					//搜寻节点相邻边的EdgeID					
					ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//存储节点邻接边	
//					relaEdgeArraylist=relationEdgesMap.get(EID);//节点EID邻接边	
					relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
					int relaEdgeCount=relaEdgeArraylist.size();					
							
					//检索相邻边				
					for (int k = 0; k < relaEdgeCount; k++) {
						Edge edge=new Edge();
						edge=relaEdgeArraylist.get(k);	
						ArrayList<Node>edgePointCollArrayList=new ArrayList<Node>();
						edgePointCollArrayList=edge.getPointCollArrayList();
						int pointCount=edgePointCollArrayList.size();
						Node froNode=new Node();
						Node toNode=new Node();
						froNode=edgePointCollArrayList.get(0);
						toNode=edgePointCollArrayList.get(pointCount-1);
								
						//2013/12/01
						//计算机取得位数不同，可能导致错误，所以有下面的判断
						if (isTheSamePoint(froNode, nodesPath[nodesPath.length-1])) {
							froNode=nodesPath[nodesPath.length-1];
						}
						else if (isTheSamePoint(toNode, nodesPath[nodesPath.length-1])) {
							toNode=nodesPath[nodesPath.length-1];
						}
						//取得邻接polyline的vertex
						//获得与当前点直接相连的边
						//用距离对其检索方向进行约束
						if (distance(jjNode, froNode)>distance(jjNode, nodesPath[nodesPath.length-1])||distance(jjNode, toNode)>distance(jjNode, nodesPath[nodesPath.length-1])) {				
							
							double length=0;//当前polyline的长度
							double tempPathLength=tempAccuLength;
						
							//不能用froPoint，toPoint求距离，因可能为曲线
							for (int p = 0; p < pointCount-1; p++) {
								Node t1Node=new Node();
								Node t2Node=new Node();
								t1Node=edgePointCollArrayList.get(p);
								t2Node=edgePointCollArrayList.get(p+1);								
								length=length+distance(t1Node, t2Node);																
							}												
							
							//如果累计路径长度仍然小于拦截距离，路径不符合要求，继续检索下一条边
							if (tempPathLength+length<circle) {
								continue;//检索下一条Edge
							}	
							//若超出拦截距离，存储polyline信息
							//若当前节点与plinePoints的首点相同，则按此顺序增加point
							//否则倒序增加point	
							//polyline要存储所有point
							else  {	
								
								ArrayList<Node>endEdgePLineArrayList=new ArrayList<Node>();//存储polyline中所有point
								mapInt++;
								Node tempSNode=new Node();//首点
								Node tempENode=new Node();//尾点
								tempSNode=edgePointCollArrayList.get(0);
								tempENode=edgePointCollArrayList.get(pointCount-1);
															
								if (isTheSamePoint(nodesPath[nodesPath.length-1], tempSNode)) {
									//存储道路交叉拦截点，应是拦截距以后的道路交叉拦截点
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(tempENode);
									}
									//去掉相同的道路交叉拦截点									
									else {
										//已存在道路交叉拦截点与新道路交叉拦截点比较，以确定是否为同一道路交叉拦截点
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//如果是同一点，则无效，跳出循环
											if (isTheSamePoint(tempENode, ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(tempENode);
										}						
									}
																		
									for (int l = 0; l < pointCount; l++) {	
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										endEdgePLineArrayList.add(tNode);	
											
									}									
								}
								else if (isTheSamePoint(nodesPath[nodesPath.length-1], tempENode)) {
									
									//存储道路交叉拦截点
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(tempSNode);
									}
									//去掉相同的道路交叉拦截点									
									else {
										//已存在道路交叉拦截点与新道路交叉拦截点比较，以确定是否为同一道路交叉拦截点
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//如果是同一点，则无效，跳出循环
											if (isTheSamePoint(tempSNode, ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(tempSNode);
										}						
									}


									for (int l =pointCount-1; l>=0 ; l--) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										endEdgePLineArrayList.add(tNode);	
																														
									}									
						    	}						
							    plineMap.put(mapInt,endEdgePLineArrayList );	
							    pathDistanceMap.put(mapInt,tempPathLength);				
						    }										
					    }
				    }
					
					/*求交点并存储路径
					 *特例进行特殊处理:根据最后一段polyline求拦截点*/
					if (plineMap.size()>0) {
						
						for (int i = 0; i < plineMap.size(); i++) {
														
							ArrayList<Node>tempNodeArrayList=new ArrayList<Node>();//存放临时路径
							//必须逐个赋值
							for (int q = 0; q < nodeArrayList.size(); q++) {
								tempNodeArrayList.add(nodeArrayList.get(q));
							}
						
							ArrayList<Node> tempPlineArrayList=new ArrayList<Node>();
							tempPlineArrayList=plineMap.get(i+1);
							ArrayList<Node> retuTempPlineArrayList=new ArrayList<Node>();
							//求拦截点
							Node interPoint=new Node();//拦截点
							retuTempPlineArrayList=interPoint(tempPlineArrayList, tempAccuLength, circle);
							interPoint=retuTempPlineArrayList.get(retuTempPlineArrayList.size()-1);//拦截点
							for (int j = 0; j < retuTempPlineArrayList.size(); j++) {
								tempNodeArrayList.add(retuTempPlineArrayList.get(j));//存储到拦截点路径的所有点
							}
							
							/*两条不同路径的拦截点不可能相同
							 * 如果路径中存在该拦截点，去掉该路径
							 * 若pathArrayLists不存在路径，则添加路径
							 * 若pathArrayLists中存在路径，则判断拦截点是否与已存在路径中拦截点相同*/							
							if (pathArrayLists.size()==0) {
								pathArrayLists.add(tempNodeArrayList);
							}
							else {
								//已存在路径拦截点与新路径拦截点比较，以确定是否为同一拦截点
								boolean isValidPath=true;
								for (int p = 0; p < pathArrayLists.size(); p++) {
									ArrayList<Node>tArrayList=new ArrayList<Node>();
									tArrayList=pathArrayLists.get(p);
									int count=tArrayList.size();
									Node node=new Node();
									node=tArrayList.get(count-1);//每条路径的最后一点存储拦截点
									//如果是同一拦截点，则该路径无效，跳出循环
									if (isTheSamePoint(node, interPoint)) {
										isValidPath=false;
										break;
									}									
								}
								if (isValidPath) {
									pathArrayLists.add(tempNodeArrayList);
								}							
							}	
						}										
					}
				}
								
				if (tempAccuLength>=circle) {
					//求拦截点以及拦截路径	返回pLineArrayList
					//只存储最后plyline
					//返回处理后的pLineArrayList以及拦截点
					ArrayList<Node> retuTempPlineArrayList=new ArrayList<Node>();
					retuTempPlineArrayList=interPoint(pLineArrayList,befoCurAccuLength, circle);
					Node interPoint=new Node();//拦截点
					interPoint=retuTempPlineArrayList.get(retuTempPlineArrayList.size()-1);//拦截点
					for (int i = 0; i < retuTempPlineArrayList.size(); i++) {
						nodeArrayList.add(retuTempPlineArrayList.get(i));//存储到拦截点路径的所有点
					}
					
					/*两条不同路径的拦截点不可能相同
					 * 如果路径中存在该拦截点，去掉该路径
					 * 若pathArrayLists不存在路径，则添加路径
					 * 若pathArrayLists中存在路径，则判断拦截点是否与已存在路径中拦截点相同*/							
					if (pathArrayLists.size()==0) {
						pathArrayLists.add(nodeArrayList);
					}
					else {
						//已存在路径拦截点与新路径拦截点比较，以确定是否为同一拦截点
						boolean isValidPath=true;
						for (int p = 0; p < pathArrayLists.size(); p++) {
							ArrayList<Node>tArrayList=new ArrayList<Node>();
							tArrayList=pathArrayLists.get(p);
							int count=tArrayList.size();
							Node node=new Node();
							node=tArrayList.get(count-1);
							//如果是同一拦截点，则该路径无效，跳出循环
							if (isTheSamePoint(node, interPoint)) {
								isValidPath=false;
								break;	
							}							
						}
						if (isValidPath) {
							pathArrayLists.add(nodeArrayList);
						}						
					}
				}																			
			}
		} catch (Exception ae) {
			System.err.println("Caught AutomationException: " + ae.getMessage() + "\n");
		    ae.printStackTrace();
		}	    											
	}	
	
	/*求拦截点,粗略拦截点，没有精确计算
	*pLineArrayList:最后一段polyline,去掉首点
	*befoCurAccuLength：累计到当前polyline之前的长度
	*circle：拦截半径
	*tempArrayList:最后点存储拦截点*/
	public ArrayList<Node> interPoint (ArrayList<Node> pLineArrayList,double befoCurAccuLength,double circle)
	{
		ArrayList<Node> tempArrayList=new ArrayList<Node>();//存放临时点
		int nodeCount=pLineArrayList.size();
		try {
			
			for (int i = 0; i < nodeCount-1; i++) {
				double tempdis=distance(pLineArrayList.get(i), pLineArrayList.get(i+1));
				befoCurAccuLength=befoCurAccuLength+tempdis;
				/*第一个节点已存在，不需取得*/
				tempArrayList.add(pLineArrayList.get(i+1));			
				if (befoCurAccuLength>=circle) {
					break;
				}			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}				
		return tempArrayList;
	}
	
	/*输出两点间最短路径*/
	public void showShortestPath (String pcsName,double circle ,Node jjNode )
	{
		try {
			ArrayList<Node>nodeArrayList =new ArrayList<Node>();//顺序存储当前路径节点
			Node[]nodesPath=new Node[nodesPathArrayList.size()];//nodesPath第一个元素为jjNode
			for (int i = 0; i < nodesPathArrayList.size(); i++) {
				nodesPath[i] = nodesPathArrayList.get(i);
			}
			
			/*如果是可行路径则存储并输出
			 * 由节点ID检索relationEdgesMap对应边，检索对应边polyline
			 * 计算路径
			 * 累计路径长度，若tempAccuLength>=circle,则取另存当前polyline并跳出，不必在继续检索
			 * 若tempAccuLength<circle:该路径不符合条件*/			
			//加入警情点
			nodeArrayList.add(jjNode);																									
			for (int j = 1; j < nodesPath.length-1; j++) {
				int EID=nodesPath[j].EID;//节点ID
				nodeArrayList.add(nodesPath[j]);
				//搜寻路径节点相邻边Edge
				ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//存储节点邻接边												
				relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
				for (int p = 0; p < endNodeArrayList.size(); p++) {
					if (EID == endNodeArrayList.get(p).EID) {
						relaEdgeArraylist = endNodeArrayList.get(p).relationEdges;
						break;
					}
				}
				int relaEdgeCount=relaEdgeArraylist.size();//节点相邻边数
				
				//检索相邻边				
				for (int k = 0; k < relaEdgeCount; k++) {
					Edge edge=new Edge();
					edge=relaEdgeArraylist.get(k);									
					Node froNode=new Node();
					Node toNode=new Node();
					ArrayList<Node>edgePointCollArrayList=new ArrayList<Node>();
					edgePointCollArrayList=edge.getPointCollArrayList();
					int pointCount=edgePointCollArrayList.size();
					froNode.setX(edgePointCollArrayList.get(0).getX());
					froNode.setY(edgePointCollArrayList.get(0).getY());
					toNode.setX(edgePointCollArrayList.get(pointCount-1).getX());
					toNode.setY(edgePointCollArrayList.get(pointCount-1).getY());					
					
					//取得邻接polyline的vertex
					//获得与当前点直接相连的边				
					if (isTheSamePoint(nodesPath[j+1], froNode)||isTheSamePoint(nodesPath[j+1], toNode)) {						

						//首尾点与节点重合，去掉首尾点，只取中间点
						//若当前节点与plinePoints的首点相同，则按此顺序增加point
						//否则倒序增加point
						Node tempSNode=new Node();//首点
						Node tempENode=new Node();//尾点
						tempSNode=edgePointCollArrayList.get(0);
						tempENode=edgePointCollArrayList.get(pointCount-1);
						
						if (isTheSamePoint(nodesPath[j], tempSNode)) {	
							for (int l = 1; l < pointCount-1; l++) {
								Node tNode=new Node();
								tNode=edgePointCollArrayList.get(l);
								nodeArrayList.add(tNode);	
							}								
						}							
						//与尾点相同
						else if (isTheSamePoint(nodesPath[j], tempENode)) {
							for (int l =pointCount-2; l>0 ; l--) {
								Node tNode=new Node();
								tNode=edgePointCollArrayList.get(l);
								nodeArrayList.add(tNode);
							}																
						}							
						break;//检索到邻接polyline，跳出
					}				
				}				
			}	
			//加入最后一个节点
			nodeArrayList.add(nodesPath[nodesPath.length-1]);
			pathArrayLists.add(nodeArrayList);	
			
		} catch (Exception ae) {
			System.err.println("Caught AutomationException: " + ae.getMessage() + "\n");
		    ae.printStackTrace();
		}	    											
	}
			    
	/*相邻两节点之间的距离*/
	public double distance(Node node1,Node node2){		
		double nodex1=coordinateTransGetx(node1);
		double nodey1=coordinateTransGety(node1);
		double nodez1=coordinateTransGetz(node1);
		double nodex2=coordinateTransGetx(node2);
		double nodey2=coordinateTransGety(node2);
		double nodez2=coordinateTransGetz(node2);
		double dis=Math.sqrt(Math.pow(nodex1-nodex2,2)+ Math.pow(nodey1-nodey2,2)+Math.pow(nodez1-nodez2, 2));
		return dis;
	}			
	
	/*经纬度坐标转换为平面坐标*/
	 //a：椭球长半轴，WGS84椭球的长半轴为6378137.000001
   //e: 椭球第一偏心率的平方，WGS84椭球的e为0.00669437999013   	
	public static double coordinateTransGetx(Node tNode){
		double L=tNode.getX();//经度
		double B=tNode.getY();//纬度
	    double pi=Math.PI;
	    L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//高程赋0值
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
	    double x= (N + H)*Math.cos(B)*Math.cos(L);
		return x;
	}
	
	public static double coordinateTransGety(Node tNode){
		double L=tNode.getX();//经度
		double B=tNode.getY();//纬度
		double pi=Math.PI;
		L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//高程赋0值
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
		double y=(N + H)*Math.cos(B)*Math.sin(L);
		return y;		
	}
	
	public static double coordinateTransGetz(Node tNode){
		double L=tNode.getX();//经度
		double B=tNode.getY();//纬度
		double pi=Math.PI;
		L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//高程赋0值
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
	    double z = (N * (1 - e) + H) * Math.sin(B);
		return z;
	}
	
	/*判断是否为同一点*/
	public boolean isTheSamePoint(Node point1,Node point2){
		try {
			double x1=point1.getX();
    		double y1=point1.getY();
    		double x2=point2.getX();
    		double y2=point2.getY();
    		if (Math.abs(x1-x2)<=0.00000001&&Math.abs(y1-y2)<=0.00000001) {
				return true;
			}
    		else {
    			return false;
			}
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/*判断节点是否在以x，y为中心的圆内，正方形快速粗选择，圆再进一步选择
	 * jjNode为正方形中心点坐标,经纬度
	 * node为待判断节点，node节点存储坐标也为经纬度，需要转换
	 * r为正方形内切圆半径*/
	public boolean isNodeInCricle(Node node,double r,Node jjNode){
		double nodex=coordinateTransGetx(node);
		double nodey=coordinateTransGety(node);
		double nodez=coordinateTransGetz(node);
		double jjNodeX=coordinateTransGetx(jjNode);
		double jjNodeY=coordinateTransGety(jjNode);
		double jjNodeZ=coordinateTransGetz(jjNode);
		double maxX=jjNodeX+r;
		double minX=jjNodeX-r;
		double maxY=jjNodeY+r;
		double minY=jjNodeY-r;
		if(nodex>minX&&nodex<maxX&&nodey>minY&&nodey<maxY ){
			double tdis=Math.sqrt(Math.pow(nodex-jjNodeX,2)+ Math.pow(nodey-jjNodeY,2)+Math.pow(nodez-jjNodeZ, 2));
			if(tdis<=r)
			  return true;
			else return false;
		}
		else
		 return false;
	}
}