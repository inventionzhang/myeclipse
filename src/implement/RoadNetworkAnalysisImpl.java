package implement;

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
import entity.*;

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

import entity.Edge;
import entity.Node;
import entity.PropertiesUtilJAR;
import entity.ReadRoadDataThread;
import entity.readRoadName;
import entity.readSDEData;
import entity.retuNode;
import entity.returnResult;
import entity.roadName;
import entity.surface;

public class RoadNetworkAnalysisImpl {
	
	private static RoadNetworkAnalysisImpl gInstance= null; 
	public static RoadNetworkAnalysisImpl instance() {
		if(gInstance == null){
			gInstance = new RoadNetworkAnalysisImpl();
			//��ʼ������
			String roadNetworkName = PropertiesUtilJAR.getProperties("roadNetworkJiangAn");
			String roadNetworkNamecoll[] = roadNetworkName.split(",");
			String fileName = roadNetworkNamecoll[0];//·���洢�ļ���
			gInstance.readRoadFile(fileName);
		}
		return gInstance;
	}
	public boolean isOK = false;
    /*�洢����·������
     *�洢�ɳ������Լ�����Ӧ��·������*/
    public Map<String, ArrayList<Node>> allJuncArraylistMap=new HashMap<String, ArrayList<Node>>();//�洢�ڵ�ID�Լ�����
    public Map<String, Map<Integer,ArrayList<Edge>>> allRelationEdgesMap=new HashMap<String, Map<Integer,ArrayList<Edge>>>();//�洢�ڵ�ID�Լ����ڱ�
    public Map<String, ArrayList<Node>> allNodesArrayListMap=new HashMap<String, ArrayList<Node>>();//�洢�ڵ����˹�ϵ
    public Map<String, ArrayList<Edge>> allPolylineCollArraylistMap=new HashMap<String, ArrayList<Edge>>();//�洢polyline��ID�Լ�polyline�еĵ�
    public Map<String, ArrayList<surface>> allSurfaceArrayListMap=new HashMap<String, ArrayList<surface>>();//�洢��
    public Map<Integer, ArrayList<String>> roadNetworkNameMap=new HashMap<Integer, ArrayList<String>>();//�洢�ɳ��������ļ�����
    public Map<String, ArrayList<roadName>>allRoadNameArrayMap=new HashMap<String, ArrayList<roadName>>();//�洢���е�·��
    
    public boolean readRoadFile(String filename){
    	
    	try{   		
    		ClassLoader loader = RoadNetworkStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();    //��ø��������������  
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
    
    //�������·������
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
				//���ȷ����ڴ�
				ArrayList<Node>tempJuncArraylist=new ArrayList<Node>();//�洢�ڵ�ID�Լ�����
				Map<Integer,ArrayList<Edge>> tempRelationEdgesMap=new HashMap<Integer,ArrayList<Edge>>();//�洢�ڵ�ID�Լ����ڱ�
				ArrayList<Node>tempNodesArrayList=new ArrayList<Node>();//�洢�ڵ����˹�ϵ
				ArrayList<surface>tempSurfaceArrayList=new ArrayList<surface>();//�洢��
			    ArrayList<Edge>tempPolylineCollArrayList=new ArrayList<Edge>();//�洢polyline��ID�Լ�polyline�еĵ�
			    ArrayList<roadName> tempRoadNameArrayList=new ArrayList<roadName>();//�洢��·����Ϣ
				allJuncArraylistMap.put(pcsName, tempJuncArraylist);
				allRelationEdgesMap.put(pcsName, tempRelationEdgesMap);
				allNodesArrayListMap.put(pcsName, tempNodesArrayList);
				allSurfaceArrayListMap.put(pcsName, tempSurfaceArrayList);
				allPolylineCollArraylistMap.put(pcsName, tempPolylineCollArrayList);
				allRoadNameArrayMap.put(pcsName, tempRoadNameArrayList);
				System.out.print("��ʼ��ȡ·�����ݣ�"+ pcsName +"\n");
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
			System.out.print(pcsName+"��ʼ�����ݣ�"+startRead+"\n");
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
			System.out.print(pcsName+"���������ݣ�"+endRead+"\n");
			float readTime=(endRead-startRead)/1000;
			System.out.print(pcsName+"������ʱ��:"+readTime+"\n");
			isOK=true;//�Ѷ�����	
				
		} catch (Exception e) {
			e.printStackTrace();
			isOK=false;
			System.out.print(pcsName+"��ȡ����ʧ��!"+"\n");
		}
		
		if (!isOK) {
			System.out.print("���¶�ȡ����!"+"\n");
			readData(pcsName,junctionData,splitLineData,polygonData);
		}
		else {
			float startTopo=System.currentTimeMillis();
			System.out.print("��ʼ�������ˣ�"+startTopo+"\n");
			createTopology(pcsName);
			
			float endTopo=System.currentTimeMillis();
			System.out.print("���˹���������"+endTopo+"\n");	
			System.out.print("���˹���ʱ��"+"\n");
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
	
	/*relationEdgesMap:�洢�ڵ�ID�Լ�����Edge
     *nodesArrayList���洢�ڵ�����˹�ϵ
     *surfaceArrayList:�洢polygon�Լ�ID*/			
	public void getJunctionRelationship(String pcsName,ArrayList<Node>nodesArrayList,Map<Integer,ArrayList<Edge>> relationEdgesMap,ArrayList<Node> juncArraylist,ArrayList<Edge>polylineCollArrayList)
	{		
		/* ����ڵ����� */
		int juncCount=juncArraylist.size();
		Node[]nodes=new Node[juncCount];	
		for (int i = 0; i < juncCount; i++) {		
 	    	//һ��Ҫʵ����
 			nodes[i]=new Node();
 			nodes[i].setX(juncArraylist.get(i).getX());
 			nodes[i].setY(juncArraylist.get(i).getY());
 			nodes[i].setEID(juncArraylist.get(i).getEID()); 			
 		    }
		
		try {
	    	int lineCount=polylineCollArrayList.size(); 		 		
	 		//������
	 		for (int i = 0; i < juncCount; i++) {   	    			
	 			ArrayList<Node> adjacentJuncArrayList=new ArrayList<Node>();//�洢�ڽӵ�
	 			ArrayList<Edge> adjacentEdgeArrayList=new ArrayList<Edge>();//�洢�ڵ�����Edge
	 				 			
	 			//������
	 			for (int j = 0; j < lineCount; j++) {
	 				//�ߵ�ID��polyline���frPoint���յ�toPoint
	 				Edge tEdge=polylineCollArrayList.get(j);
 					int plineID=tEdge.getEdgeID();  
 					ArrayList<Node>tArrayList=new ArrayList<Node>();
 					tArrayList= tEdge.getPointCollArrayList();
     	    		Node frPoint=tArrayList.get(0);		    	    		
     	    		Node toPoint=tArrayList.get(tArrayList.size()-1);
     	    		
     	    		if (isTheSamePoint(juncArraylist.get(i), frPoint)) {
     	    			adjacentEdgeArrayList.add(tEdge);//���ڱ�			    	    			
     	    			for (int k1 = 0; k1 < juncCount; k1++) {
     	    				if (isTheSamePoint(juncArraylist.get(k1), toPoint)) {			    	    								    	    					
     	    					adjacentJuncArrayList.add(juncArraylist.get(k1));//���ڵ�
     	    					break;//�ҵ����ڵ㣬����ѭ��
 							}		    	    				
     	    			}		    	    			
 					}
     	    		else if (isTheSamePoint(juncArraylist.get(i), toPoint)) {
     	    			adjacentEdgeArrayList.add(tEdge);//���ڱ�			    	    			
     	    			for (int k2 = 0; k2 < juncCount; k2++) {
     	    				if (isTheSamePoint(juncArraylist.get(k2), frPoint)) {
     	    					adjacentJuncArrayList.add(juncArraylist.get(k2));//���ڵ�    	    					
     	    					break;//�ҵ����ڵ㣬����ѭ��
 							}		    	    				
     	    			}		    
 					}
     	    		else {
 						continue;
 					}	 						    					     	    					    	    					    	    		    							    	    			    					
	 			}	
	 				 			
	 			//�洢�ڵ��ϵ
	 			//�ڽ�����ڽӱ������
	 			//�������nodes�еĵ㣬�໥���ַ�����ã��������
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
				List = null;  //�ͷ��ڴ�				
				nodes[i].setRelationEdge(adjacentEdgeArrayList);				
	 			relationEdgesMap.put(juncArraylist.get(i).getEID(), adjacentEdgeArrayList);//�洢�ڵ�ID�Լ��ڽӱ�	 				 								   						    			 	    			
	 		} 		
	 		
	 		for (int q = 0; q < juncCount; q++) {
	 			nodesArrayList.add(nodes[q]);
	 		}
		} catch (Exception e) {
			System.err.print(e.getMessage());
			e.printStackTrace();
			//�����������¹�������
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
	
	/*relationEdgesMap:�洢�ڵ�ID,����Edge
     *nodesArrayList���洢�ڵ��ϵ
     *surfaceArrayList:�洢polygon�Լ�ID*/	
		
	/*��������꣺jjL��jjB��γ��
	 * speed���ٶ�km/h
	 * time��ʱ�䣬��*/
	/* ��ʱ����·���ڵ��ջ */
	private Stack<Node> stack = new Stack<Node>();
	private ArrayList<Node> openList = new ArrayList<Node>();//�����б�
	private ArrayList<Node> nodesPathArrayList = new ArrayList<Node>();//�洢��ʱ·��
	/* �洢·������ */
	private ArrayList<ArrayList<Node>> pathArrayLists = new ArrayList<ArrayList<Node>>();//�洢����·��
	public ArrayList<ArrayList<retuNode>> retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//����ǰ̨·��
	public ArrayList<Node>RoadJuncArrayList=new ArrayList<Node>();//�洢��·�������Ϊ���ص�
	public ArrayList<retuNode>retuRoadJuncArrayList=new ArrayList<retuNode>();//��·����㷵��ǰ̨
	public ArrayList<ArrayList<String>> allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//����·������
	public returnResult ljfxResult=new returnResult();//����·���������	
		
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
		retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//����ǰ̨·��
		RoadJuncArrayList=new ArrayList<Node>();//�洢��·�������Ϊ���ص�
		retuRoadJuncArrayList=new ArrayList<retuNode>();//��·����㷵��ǰ̨
		allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//����·������
		ljfxResult=new returnResult();//����·���������
		
		if(gInstance.allNodesArrayListMap.get(pcsName).size()==0)
		{
			System.out.print("δ��ȡ·������");
			return null;
		}
					
		RoadJuncArrayList=new ArrayList<Node>();//�������Ȧ�еĽڵ㣬�Դ洢�µ�����Ȧ�ڵ�
		ArrayList<Node> pathNewTopoArrayList=new ArrayList<Node>();//�µ����˹�ϵ�㣬������˱�����ԭ��ȫ�ַ�Χ�������޹أ�ȫ�����¸�ֵ	
		ArrayList<Node>startArrayList=new ArrayList<Node>();//Ѱ·��㼯��
		Node jjNode=new Node(null);
		double distance=0;	
		double topoDistance = 0;
	    try {			   
			jjNode.setX(jjL);
			jjNode.setY(jjB);
			distance=Math.round(speed*1000*time/60);//���ؾ�	
			//�������Ȧ��ΧС��1000�ף�����1000�׷�Χ����������
			if(distance > 1000){
				topoDistance = distance;
			}
			else {
				topoDistance = 1000;
			}			
			/* ����ڵ����� */
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
			System.out.print("��ʼ���������ˣ�"+staNewTopo + "ms" + "\n");				
			/*��������Ȧ��Χ�ڵĵ� */
			ArrayList<Node> topoArrayList=new ArrayList<Node>();//���ڹ��������˵ĵ�
			ArrayList<Node> tempTopoArrayList=new ArrayList<Node>();//��ʱ����				
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
				
				//2013/12/1�޸�
				ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();//�洢�ڽӵ�
				ArrayList<Node>tempRelaNodeArrayList=new ArrayList<Node>();//��ʱ����
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
			
			/*���ط�Χ�ڵĵ㽨���µ�����
			 * ��������Ȧ��Χ�ڵ�ÿһ����
			 * ÿһ��������ڵ�������Ȧ��Χ�ڵ�ÿһ����Ƚϣ���ȷ�����ڵ��Ƿ�������Ȧ��
			 * ���½�������
			 * pathNewTopoArrayList���µ����˹�ϵ��
			 * newNode[i]:�µ����˹�ϵ������*/		
			int topoCount=topoArrayList.size();
			for (int i = 0; i < topoCount; i++) {
				ArrayList<Node> relaNode=new ArrayList<Node>();
				Node t1node=new Node();
				t1node=tempTopoArrayList.get(i);
				int relaNodeCount=t1node.getRelationNodes().size();
				for (int j = 0; j < relaNodeCount; j++) {
					Node t2node=new Node();
					t2node=t1node.getRelationNodes().get(j);//t1node�ڽӵ�
					
					for (int k = 0; k < topoCount; k++) {
						Node t3node=new Node();
						t3node=topoArrayList.get(k);//������topoArrayList
						if (isTheSamePoint(t2node, t3node)) {
							relaNode.add(t2node);
						}
					}				
				}
				t1node.setRelationNodes(relaNode);				
				
				//�洢�ٽӱ߳��� 2013/11/30��
				ArrayList<Edge>newRelaEdgesArrayList=new ArrayList<Edge>();//�������ڽӱ�
				ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//�ڽӱ�				
				relaEdgesArrayList=t1node.getRelationEdge();
				int edgeCount=relaEdgesArrayList.size();//�ڽӱ���
				for (int p = 0; p < edgeCount; p++) {						
					Edge tEdge=new Edge();
					ArrayList<Node>edgePointColl=new ArrayList<Node>();//�ڽӱ��ϵĵ�
					tEdge=relaEdgesArrayList.get(p);
					edgePointColl=tEdge.getPointCollArrayList();//�洢���ϵĵ�
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
			    pathNewTopoArrayList.add(t1node);//�����µ����ˣ�������pathNewTopoArrayList
    		}					
			long endNewTopo=System.currentTimeMillis();	
			System.out.print("�����˹���������" + endNewTopo + "ms" + "\n");
			long newTopoTime=(endNewTopo-staNewTopo)/1000;
			System.out.print("�����˹���ʱ�䣺" + newTopoTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();		    	
		}
		
		int count=pathNewTopoArrayList.size();
		Node []newNode=new Node[count]; 
		long starjjNode=System.currentTimeMillis();	
		System.out.print("��ʼ�жϾ���㷢���棺" + starjjNode + "ms" + "\n");
		try {
			//�洢�µ����˹�ϵ
			for (int i = 0; i < count; i++) {
				newNode[i]=pathNewTopoArrayList.get(i);
				if (newNode[i].EID==244) {
					System.out.print("244");
				}
			}
				
			/*��������㷢����
			 * polygonID:�����������ID*/				
			ArrayList<surface> surfaceArrayList=new ArrayList<surface>();
			surfaceArrayList=gInstance.allSurfaceArrayListMap.get(pcsName);
			int polygonCount=surfaceArrayList.size();
			surface findPolygon = null;//���鷢����
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jjNode.x,jjNode.y)){
					findPolygon = tempSurface;
					break;
				}
			}						
			/*���½��������˹�ϵ�У������������нڵ�
			 *���ڰ����Ľڵ���ΪѰ·��� 
			 *startArrayList:Ѱ·��㼯��
			 *endArrayList:Ѱ·�յ㼯��*/		
			for (int i = 0; i < newNode.length; i++) {
				if(findPolygon.pIn(newNode[i].x,newNode[i].y)){
					startArrayList.add(newNode[i]);
				}
			}	
			long endjjNode=System.currentTimeMillis();	
			System.out.print("�����жϾ���㷢���棺" + endjjNode + "ms" + "\n");
			long jjNodeTime=(long)(endjjNode-starjjNode)/1000;
			System.out.print("�жϾ���㷢����ʱ�䣺" + jjNodeTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		    e.printStackTrace();		
		}
		long starFindRoad = System.currentTimeMillis();
		try {						
			System.out.print("��ʼѰ·��"+ starFindRoad + "ms" + "\n");
			ArrayList<Node>endNodeArrayList=new ArrayList<Node>();
			endNodeArrayList=pathNewTopoArrayList;					
			
			
			//��ʽһ���ӽ���㿪ʼ�����·��
//			/*����Ѱ·���յ�Ѱ·*/
//			for (int i = 0; i < startArrayList.size(); i++) {
//				Node cNode=new Node(null);//Ѱ·��㣬���ڵ�Ϊ��
//				cNode=startArrayList.get(i);
//				double temp=distance(jjNode, cNode);//�������Ѱ·�������
//				//�������Ѱ·�������������ذ뾶��ֻ�洢��·�������Ϊ���ص�
//				if (temp>distance) {
//					ArrayList<Node>tempArrayList=new ArrayList<Node>();
//					tempArrayList.add(jjNode);
//					RoadJuncArrayList.add(cNode);
//				}
//				else {
//					double searchPathDistance=distance-temp;//�µ����ؾ�
//					/*����Ѱ·�յ���Ŀ����߼����ٶ�:
//					 * ������Ȧ��Χ����1000�ף���ʹ�ø÷���
//					 * distance/3��distance��Χ�ڵĵ���ΪѰ·�յ�*/
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
//							//Ѱ·
//							openList = new ArrayList<Node>();//��տ����б�
//							openList.add(cNode);
//							stack=new Stack<Node>();//�µ���ʼ�ڵ�Ҫ���ջ
//							nodesPathArrayList = new ArrayList<Node>();//���·���ڵ�
//							getIntePoint(pcsName,jjNode,searchPathDistance,cNode, null, cNode, eNode);
//						}						
//					}
//					else {
//						for (int j = 0; j < endNodeArrayList.size(); j++) {
//							Node eNode=new Node(null);//Ѱ·�յ㣬���ڵ���Ϊ��
//							eNode=endNodeArrayList.get(j);
//							if (eNode.EID==755) {
//								System.out.print(eNode.EID);
//							}							
//							//Ѱ·
//							openList = new ArrayList<Node>();//��տ����б�
//							openList.add(cNode);
//							stack=new Stack<Node>();//�µ���ʼ�ڵ�Ҫ���ջ
//							nodesPathArrayList = new ArrayList<Node>();
//							getIntePoint(pcsName,jjNode,searchPathDistance,cNode, null, cNode, eNode);
//						}
//					}				
//				}			
//			}
			
			
			//��ʽ�����ӽӾ��㿪ʼ�����·��
			/*����Ѱ·���յ�Ѱ·*/
			jjNode.setRelationNodes(startArrayList);//���������	
			jjNode.setEID(-1);//�����ID��Ϊ-1
			/*����Ѱ·�յ���Ŀ����߼����ٶ�:
			 * ������Ȧ��Χ����1000�ף���ʹ�ø÷���
			 * distance/3��distance��Χ�ڵĵ���ΪѰ·�յ�*/
			if(distance > 1000){
				//Ѱ·
				for (int i = 0; i < startArrayList.size(); i++) {
					Node cNode=new Node(null);
					cNode=startArrayList.get(i);
					double temp=distance(jjNode, cNode);//��������������
					//��������������������ذ뾶��ֻ�洢��·�������Ϊ���ص�
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
							openList = new ArrayList<Node>();//��տ����б�
							openList.add(jjNode);
							stack=new Stack<Node>();//�µ���ʼ�ڵ�Ҫ���ջ
							nodesPathArrayList = new ArrayList<Node>();
							getIntePoint(pcsName,jjNode,distance,jjNode, null, jjNode, eNode);
						}
					}
				}								
			}
			else {
				//Ѱ·
				for (int i = 0; i < startArrayList.size(); i++) {
					Node cNode=new Node(null);
					cNode=startArrayList.get(i);
					double temp=distance(jjNode, cNode);//��������������
					//��������������������ذ뾶��ֻ�洢��·�������Ϊ���ص�
					if (temp > distance) {
						RoadJuncArrayList.add(cNode);
					}
					else {
						for (int j = 0; j < endNodeArrayList.size(); j++) {
							Node eNode=new Node(null);//Ѱ·�յ㣬���ڵ���Ϊ��
							eNode=endNodeArrayList.get(j);
							if (eNode.EID==4190) {
								System.out.print(eNode.EID);
							}
							if (eNode.EID==758) {
								System.out.print(eNode.EID);
							}
							openList = new ArrayList<Node>();//��տ����б�
							openList.add(jjNode);
							stack=new Stack<Node>();//�µ���ʼ�ڵ�Ҫ���ջ
							nodesPathArrayList = new ArrayList<Node>();
							getIntePoint(pcsName,jjNode,distance,jjNode, null, jjNode, eNode);
						}
					}
				}				
			}
			long endFindRoad = System.currentTimeMillis();
			System.out.print("\n" + "Ѱ·������" + starFindRoad + "ms" + "\n");
			long findRoadTime = (endFindRoad - starFindRoad)/1000;
			System.out.print( "Ѱ·ʱ�䣺" + findRoadTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();
		}
		
		/*ȡ��·���Է���ǰ̨*/
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
	
	/*��������·��
	 * jjL:�Ӿ��㾭��
	 * jjB:�Ӿ���γ��
	 * jcL:����λ�þ���
	 * jcB:����λ��γ��*/
	public ArrayList<Node>endNodeArrayList=new ArrayList<Node>();//Ѱ·�յ㼯��
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
		retuAllPathArrayList=new ArrayList<ArrayList<retuNode>>();//����ǰ̨·��
		RoadJuncArrayList=new ArrayList<Node>();//�洢��·�������Ϊ���ص�
		retuRoadJuncArrayList=new ArrayList<retuNode>();//��·����㷵��ǰ̨
		allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//����·������
		ljfxResult=new returnResult();//����·���������
		RoadJuncArrayList=new ArrayList<Node>();//�������Ȧ�еĽڵ㣬�Դ洢�µ�����Ȧ�ڵ�
		if(gInstance.allNodesArrayListMap.get(pcsName).size()==0)
		{
			System.out.print("δ��ȡ·������");
			return null;
		}
							
		ArrayList<Node> pathNewTopoArrayList = new ArrayList<Node>();//�µ����˹�ϵ�㣬������˱�����ԭ��ȫ�ַ�Χ�������޹أ�ȫ�����¸�ֵ	
		ArrayList<Node>startArrayList = new ArrayList<Node>();//Ѱ·��㼯��
		endNodeArrayList=new ArrayList<Node>();//Ѱ·�յ㼯��
		Node jjNode = new Node(null);
		Node jcNode = new Node(null);
		double distance = 0;	
		double topoDistance = 0;
	    try {			   
			jjNode.setX(jjL);
			jjNode.setY(jjB);
			jcNode.setX(jcL);
			jcNode.setY(jcB);
			distance=distance(jjNode, jcNode); //���ؾ�	
			//�����˹�����Χ
			topoDistance = distance + 500;	
			/* ����ڵ����� */
			ArrayList<Node> nodesArrayList=new ArrayList<Node>();
			nodesArrayList=gInstance.allNodesArrayListMap.get(pcsName);
			int nodesCount=nodesArrayList.size();
			Node[]nodes=new Node[nodesCount];
			for (int i = 0; i < nodesCount; i++) {
				nodes[i]=nodesArrayList.get(i);
			}		
					
			long staNewTopo=System.currentTimeMillis();	
			System.out.print("��ʼ���������ˣ�"+staNewTopo + "ms" + "\n");				
			/*��������Ȧ��Χ�ڵĵ� */
			ArrayList<Node> topoArrayList=new ArrayList<Node>();//���ڹ��������˵ĵ�
			ArrayList<Node> tempTopoArrayList=new ArrayList<Node>();//��ʱ����				
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
				ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();//�洢�ڽӵ�
				ArrayList<Node>tempRelaNodeArrayList=new ArrayList<Node>();//��ʱ����
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
			
			/*���ط�Χ�ڵĵ㽨���µ�����
			 * ��������Ȧ��Χ�ڵ�ÿһ����
			 * ÿһ��������ڵ�������Ȧ��Χ�ڵ�ÿһ����Ƚϣ���ȷ�����ڵ��Ƿ�������Ȧ��
			 * ���½�������
			 * pathNewTopoArrayList���µ����˹�ϵ��
			 * newNode[i]:�µ����˹�ϵ������*/		
			int topoCount=topoArrayList.size();
			for (int i = 0; i < topoCount; i++) {
				ArrayList<Node> relaNode=new ArrayList<Node>();
				Node t1node=new Node();
				t1node=tempTopoArrayList.get(i);
				int relaNodeCount=t1node.getRelationNodes().size();
				for (int j = 0; j < relaNodeCount; j++) {
					Node t2node=new Node();
					t2node=t1node.getRelationNodes().get(j);//t1node�ڽӵ�					
					for (int k = 0; k < topoCount; k++) {
						Node t3node=new Node();
						t3node=topoArrayList.get(k);//������topoArrayList
						if (isTheSamePoint(t2node, t3node)) {
							relaNode.add(t2node);
						}
					}				
				}
				t1node.setRelationNodes(relaNode);				
				//�洢�ٽӱ߳��� 
				ArrayList<Edge>newRelaEdgesArrayList=new ArrayList<Edge>();//�������ڽӱ�
				ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//�ڽӱ�				
				relaEdgesArrayList=t1node.getRelationEdge();
				int edgeCount=relaEdgesArrayList.size();//�ڽӱ���
				for (int p = 0; p < edgeCount; p++) {						
					Edge tEdge=new Edge();
					ArrayList<Node>edgePointColl=new ArrayList<Node>();//�ڽӱ��ϵĵ�
					tEdge=relaEdgesArrayList.get(p);
					edgePointColl=tEdge.getPointCollArrayList();//�洢���ϵĵ�
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
			    pathNewTopoArrayList.add(t1node);//�����µ����ˣ�������pathNewTopoArrayList
    		}					
			long endNewTopo=System.currentTimeMillis();	
			System.out.print("�����˹���������" + endNewTopo + "ms" + "\n");
			long newTopoTime=(endNewTopo-staNewTopo)/1000;
			System.out.print("�����˹���ʱ�䣺" + newTopoTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();		    	
		}
		
		int count=pathNewTopoArrayList.size();
		Node []newNode=new Node[count]; 
		long starjjNode=System.currentTimeMillis();	
		System.out.print("��ʼ�жϾ���㡢����㷢���棺" + starjjNode + "ms" + "\n");
		try {
			//�洢�µ����˹�ϵ
			for (int i = 0; i < count; i++) {
				newNode[i]=pathNewTopoArrayList.get(i);
			}
				
			/*��������㷢����
			 * polygonID:�����������ID*/				
			ArrayList<surface> surfaceArrayList=new ArrayList<surface>();
			surfaceArrayList=gInstance.allSurfaceArrayListMap.get(pcsName);
			int polygonCount=surfaceArrayList.size();
			surface findJJPolygon = null;//����㷢����
			surface findJCPolygon = null;//�����������
			//�жϽ����������
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jjNode.x,jjNode.y)){
					findJJPolygon = tempSurface;
					break;
				}
			}			
			
			//�жϾ����������
			for (int i = 0; i < polygonCount; i++) {
				surface tempSurface=new surface();
				tempSurface=surfaceArrayList.get(i);
				if(tempSurface.pIn(jcNode.x, jcNode.y)){
					findJCPolygon = tempSurface;
					break;
				}
			}
			
			/*���½��������˹�ϵ�У������������нڵ�
			 *���ڰ����Ľڵ���ΪѰ·��� 
			 *startArrayList:Ѱ·��㼯��
			 *endArrayList:Ѱ·�յ㼯��*/
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
			System.out.print("�����жϾ���㡢����㷢���棺" + endjjNode + "ms" + "\n");
			long jjNodeTime=(long)(endjjNode-starjjNode)/1000;
			System.out.print("�жϾ���㡢����㷢����ʱ�䣺" + jjNodeTime + "ms" + "\n");
			/*Ѱ·��㡢�յ�
			 *������Ե�����ڵĵ���Ӿ��㡢�����ֱ�߼�н�Ϊ���
			 *�õ�Ϊ��Ч��*/
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
			System.out.print("��ʼѰ·��"+ starFindRoad + "ms" + "\n");
			
			/*����ľ���㡢����㽨�����˹�ϵ*/
			jjNode.setRelationNodes(startArrayList);//���������	
			jjNode.setEID(-1);//�����ID��Ϊ-1
			jcNode.setRelationNodes(endNodeArrayList);//���������			
			jcNode.setEID(-2);//�����ID��Ϊ-2
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
			
			//������������ڵĽڵ㹹�������˹�ϵ
			ArrayList<Node>temptempEndNodeArrayList=new ArrayList<Node>();//�м佻������
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
				tempEdge.edgeID = -2;//���
				ArrayList<Node> pointCollArrayList = new ArrayList<Node>();
				pointCollArrayList.add(tNode);
				pointCollArrayList.add(jcNode);
				tempEdge.setPointCollArrayList(pointCollArrayList);
				tempRelationEdgeArrayList.add(tempEdge);
				tNode.setRelationEdge(tempRelationEdgeArrayList);
				temptempEndNodeArrayList.add(tNode);
			}
			endNodeArrayList = temptempEndNodeArrayList;
			
			//Ѱ·
			openList = new ArrayList<Node>();//��տ����б�
			openList.add(jjNode);
			stack=new Stack<Node>();//�µ���ʼ�ڵ�Ҫ���ջ
			nodesPathArrayList = new ArrayList<Node>();
			getShortestPath(pcsName,jjNode,distance,jjNode, null, jjNode, jcNode);			
			long endFindRoad = System.currentTimeMillis();
			System.out.print("\n" + "Ѱ·������" + starFindRoad + "ms" + "\n");
			long findRoadTime = (endFindRoad - starFindRoad)/1000;
			System.out.print( "Ѱ·ʱ�䣺" + findRoadTime + "s" + "\n");
		} catch (Exception e) {
			System.err.println("Caught AutomationException: " + e.getMessage() + "\n");
		    e.printStackTrace();
		}
		
		/*ȡ��·���Է���ǰ̨*/
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
		//�½������˹�ϵѰ·����֮��Ҫ���
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
	
	
	/*����·������
	 * ��������·��������·������*/
	public void analyzePath(String pcsName)
	{
		try {
			allPathDescriptArrayList=null;
			allPathDescriptArrayList=new ArrayList<ArrayList<String>>();//����·������
			roadNameArrayList=new ArrayList<roadName>();
			roadNameArrayList=gInstance.allRoadNameArrayMap.get(pcsName);
			if (retuAllPathArrayList.size()!=0) {
				for (int i = 0; i < retuAllPathArrayList.size(); i++) {
					ArrayList<retuNode>singlePathArrayList=new ArrayList<retuNode>();
					singlePathArrayList=retuAllPathArrayList.get(i);
					ArrayList<retuNode>junctionArrayList=new ArrayList<retuNode>();//��һ·������ڵ�
					ArrayList<String>singlePathDescrip=new ArrayList<String>();//��һ·������
					//ȡ�ý���ڵ�
					for (int m = 0; m < singlePathArrayList.size(); m++) {
						retuNode node=new retuNode();
						node=singlePathArrayList.get(m);
						if (node.ID!=0) {
							junctionArrayList.add(node);
						}					
					}
					
					for (int j = 0; j < junctionArrayList.size()-1; j++) {
						//ȡ�ý���ڵ�,����ڵ���·�αȽϣ��ж�Ϊ�ĸ�·��
						retuNode curJuncNode1=new retuNode();
						retuNode curJuncNode2=new retuNode();
						curJuncNode1=singlePathArrayList.get(j);
						curJuncNode2=singlePathArrayList.get(j+1);
						//��·����㣬�ж�·��
						for (int k = 0; k < roadNameArrayList.size(); k++) {
							roadName rName=new roadName();
							rName=roadNameArrayList.get(k);
							retuNode froPoint=new retuNode();
							retuNode toPoint=new retuNode();
							froPoint=rName.getFroPoint();
							toPoint=rName.getToPoint();
							if (isTheSameJuncPoint(curJuncNode1, froPoint)||isTheSameJuncPoint(curJuncNode1, toPoint)
									&&isTheSameJuncPoint(curJuncNode2, froPoint)||isTheSameJuncPoint(curJuncNode2, toPoint)) {
								singlePathDescrip.add(rName.getRoadName());//·����	
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
	
	/*�ж��Ƿ�Ϊͬһ����ڵ�juncPoint*/
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
	
	/*��ȡ·����
	 * */
	public ArrayList<roadName> roadNameArrayList=new ArrayList<roadName>();
	public void readAllRoadName(String pcsName,String path)
	 {		 
		 readRoadName readName=new readRoadName();
		 roadNameArrayList=new ArrayList<roadName>();
		 readName.readName(pcsName, path, roadNameArrayList);
		 allRoadNameArrayMap.put(pcsName, roadNameArrayList);
		 System.out.print(pcsName+"������ȡ��·������Ϣ"+"\n");
	 }
	
	
	/*���ع�������Ȧ�ı�Ե�� 
	 * jjNode:�Ӿ�������
	 * ������Ȧ�Ľ���������ʱ������
	 * ��תɨ���߷���
	 * 1.���㼫�Ǵ�С
	 * 2.����ж�˳��*/
	public void ljCircle(Node jjNode)
	{			
		Node starNode=new Node();
		starNode=RoadJuncArrayList.get(0);//��Ϊ���㼫�ǵ���ʼ�߶ε�
		double[]polarAngleArray=new double[RoadJuncArrayList.size()];//�洢���ǣ�����������
		Map<Double,Node >nodePolarAngleMap=new HashMap<Double, Node>();//��ż����Լ��ڵ�
		for (int i = 0; i < RoadJuncArrayList.size(); i++) {
			double polarAngle=retuPolarAngle(jjNode, starNode, RoadJuncArrayList.get(i));
			polarAngleArray[i]=polarAngle;
			nodePolarAngleMap.put(polarAngle,RoadJuncArrayList.get(i));
		}
		//�Լ���ð������,��С����
		for (int j = 0; j < polarAngleArray.length-1; j++) {
			for (int k = j+1; k < polarAngleArray.length; k++) {
				if (polarAngleArray[j]>polarAngleArray[k]) {
					double temp=polarAngleArray[k];
					polarAngleArray[k]=polarAngleArray[j];
					polarAngleArray[j]=temp;
				}
			}
		}
		//�����Ľڵ�����
		ArrayList<Node>afterSortArrayList=new ArrayList<Node>();
		for (int i = 0; i < polarAngleArray.length; i++) {
			double polarAngle=polarAngleArray[i];
			afterSortArrayList.add(nodePolarAngleMap.get(polarAngle));
		}
	
		retuRoadJuncArrayList=null;
		retuRoadJuncArrayList=new ArrayList<retuNode>();//���Ҫ���صĵ�·���������
		
		for (int i = 0; i < afterSortArrayList.size(); i++) {
			retuNode tempRoadJcunc=new retuNode();
			tempRoadJcunc.L=afterSortArrayList.get(i).x;
			tempRoadJcunc.B=afterSortArrayList.get(i).y;
			tempRoadJcunc.ID=afterSortArrayList.get(i).EID;
			retuRoadJuncArrayList.add(tempRoadJcunc);
		}	
		
	}
	
	/*���㷽�����ң����������߼нǣ����ǣ����ȣ�,��������
	 * centNode:ԭ�㣬�Ӿ�����Ϊԭ��
	 * starNode���ο��߶ε�
	 * endNode���յ��߶ε�*/
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
							
				//�ο��߶����� ��ģ     �յ��߶�������ģ
				double csNodeDis=distance(centNode,starNode);
				double ceNodeDis=distance(centNode,endNode);
				
				double csDeltX=starNodeX-centNodeX;
				double csDeltY=starNodeY-centNodeY;
				double csDeltZ=starNodeZ-centNodeZ;
			
				double ceDeltX=endNodeX-centNodeX;
				double ceDeltY=endNodeY-centNodeY;
				double ceDeltZ=endNodeZ-centNodeZ;			
				//�������н�
				double angle=Math.acos((csDeltX*ceDeltX+csDeltY*ceDeltY+csDeltZ*ceDeltZ)/(csNodeDis*ceNodeDis));
				//��ά������ˣ���ֵ��ʾ�ڲο��߶���࣬��ֵ��ʾ���Ҳ�
				double chaCheng=csDeltY*ceDeltZ-csDeltZ*ceDeltY+csDeltX*ceDeltY-csDeltY*ceDeltX+csDeltZ*ceDeltX-csDeltX*ceDeltZ;
				int signal=1;
				//ֻȡ�÷���
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
		
	/*Ѱ�����ص�(intercept)����
	 * Լ��������
	 * 1.����Լ������һ������ľ��������һ�����,����20���޲�
	 * 2.����Լ������Ѱ���յ���ͬһ��ĵ㣬����Ѱ�㡢������������յ����߼н�<90/120�ȣ����Ϊ�����
	 * 3.���Լ������Ѱ�����Ѱ����ֱ�ߵľ��룬��Ϊ500��
	 * 4.��СͶӰ����Լ��:��Ѱ·�ߵ㵽����ֱ��ͶӰ������С
	 * 5.����ͶӰ����Լ������Ѱ����ֱ��
	 * distancΪ�������������Բ�뾶
	 * relationEdgesMapΪ����ڽӱ�
	 * cNode: ��ǰ����ʼ�ڵ�currentNode
	 * pNode: ��ǰ��ʼ�ڵ����һ�ڵ�previousNode
	 * sNode: �������ʼ�ڵ�startNode
	 * eNode: �յ�endNode*/
	 public boolean haveFindPath=false;
	 public boolean getIntePoint(String pcsName,Node jjNode,double distance,Node cNode,Node pNode, Node sNode, Node eNode) {		 
		 Node nNode = null;
		/* ������������ж�˵�����ֻ�·��������˳�Ÿ�·������Ѱ·������false */
		try {
			if (cNode != null && pNode != null && cNode.EID == pNode.EID)
				return false;
			//��ǰ�ڵ㲻Ϊ��
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
				/* �������ʼ�ڵ�����յ㣬˵���ҵ�һ��·�� */
				if (cNode.EID == eNode.EID)				
				{
					stack.push(cNode);
					/* ���·������pathLength>distance�����������ӡ�����·��������true
					 * 2013/11/30�޸� */
					//�洢·���ڵ�	
					getPath(nodesPathArrayList, cNode);
					int nodePathCount = nodesPathArrayList.size();
					Node[]nodesPath=new Node[nodePathCount];
					for(int p=0;p<nodePathCount;p++){
						nodesPath[p]=nodesPathArrayList.get(p);			
					}
					double pathLength = distance(nodesPath[0], nodesPath[1]) ;//�洢·�����ȣ���ʼֵΪ����㵽��һ����ľ���
					boolean isPath=false;
					for(int q=1;q<nodePathCount-1;q++){
						ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//�洢��ǰ�ڵ��ڽӱ�
						relaEdgesArrayList=nodesPath[q].relationEdges;
						int relaEdgesCount=relaEdgesArrayList.size();//�洢��ǰ�ڵ��ڽӱ���
						for (int r = 0; r < relaEdgesCount; r++) {
							ArrayList<Node>tempEdgeNodeArrayList=new ArrayList<Node>();//��ǰ�ڽӱ��ϵ㼯��
							Edge relaEdge=new Edge();
							relaEdge=relaEdgesArrayList.get(r);
							double relaEdgeLength=relaEdge.edgeLength;//��ǰ�ߵĳ���
							tempEdgeNodeArrayList=relaEdge.getPointCollArrayList();
							int edgeNodeCount=tempEdgeNodeArrayList.size();//��ǰ�ڽӱ��ϵ���
							//��㡢�յ�
							Node starNode=new Node();
							Node endNode=new Node();
							starNode=tempEdgeNodeArrayList.get(0);
							endNode=tempEdgeNodeArrayList.get(edgeNodeCount-1);
							//������ǰ·�����洢��ǰ·������
							if (isTheSamePoint(starNode, nodesPath[q+1])||isTheSamePoint(endNode, nodesPath[q+1])){
								pathLength=pathLength+relaEdgeLength;
								break;
							}						
						}
						//2014/1/11�޸�
						//ȡ��·�������һ���ڵ���ٽӱߣ�����·�����ȣ��жϸ�·���Ƿ����
						if (q==nodePathCount-2) {
							int index=nodePathCount-1;//���һ���ڵ�
							relaEdgesArrayList=nodesPath[index].relationEdges;
							relaEdgesCount=relaEdgesArrayList.size();//�洢��ǰ�ڵ��ڽӱ���
							for (int t = 0; t < relaEdgesCount; t++) {
								Edge relaEdge=new Edge();
								relaEdge=relaEdgesArrayList.get(t);
								double relaEdgeLength=relaEdge.edgeLength;//��ǰ�ߵĳ���
								if (pathLength+relaEdgeLength>distance) {
									isPath=true;
									haveFindPath=true;
									break;
								}								
							}							
						}
					}
					//���ܸ�·�������Ƿ����������Ҫ����true����ʾ�����Ѱ·������
					//���·�����ȷ����������������·��
					if (isPath) {
						showAndSavePath(pcsName,distance,jjNode);
					}	
					return true;
				}				
				/* �������,����Ѱ· */
				else
				{		
					//��������
					if (cNode.getRelationNodes().size() <= i) {
						return false;
					}
					/* ���뵱ǰ��ʼ�ڵ�cNode�����ӹ�ϵ�Ľڵ㼯�а�˳������õ�һ���ڵ�
					 * ��Ϊ��һ�εݹ�Ѱ·ʱ����ʼ�ڵ� 
					 */
					nNode = cNode.getRelationNodes().get(i);			
					while(nNode != null){
						if (nNode.EID == 3235) {
							System.out.print(cNode.EID);
						}
						//����˴�ΪĿ��㣬Ӧ�����·��
	
						if (isDistanceSatis(jjNode, cNode, nNode) && isTheSameDirection(jjNode, eNode,180, nNode)
								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
							//��֤���������·
//							if ( nNode.EID != sNode.EID && !isNodeInStack(nNode)) {	
							if ( nNode.EID != sNode.EID ) {	
								//���ҿ����б����Ƿ���ڸõ�,�����ڷ��ظõ����������򷵻�-1
								if (nNode.EID == 3621) {
									System.out.print(nNode.EID);
								}
								int indexInOpenlist = isListContains(openList, nNode);
								int indexInStack = -1;//�ڹر��б��е�����
								if (indexInOpenlist == -1) {
									indexInStack = isStackContains(stack,nNode);
								}								
								double cost = distance(nNode, cNode);//����һ�ڵ�Ĵ���
								//������ڣ�Gֵ�Ƿ��С�����Ƿ����G��Fֵ
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
								//����ڹر��б�stack��
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
									//��ӵ������б���								
									nNode.setParentNode(cNode);//���ڵ�								
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
						//���򣬼���Ѱ�����ڵ�
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
					//�ӿ����б���ɾ��F��С��node
			        //��ӵ�stack��
					if (openList.size() >=1 ) {
						stack.push(openList.remove(0));
					}					
					//�����б������򣬰�Fֵ��С�ķŵ����
					Collections.sort(openList, new nodeFComparator());
					//ȡ��Fֵ��С�ĵ�
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
	 
	 /*��Ѱ���·������*/
	 public boolean getShortestPath(String pcsName,Node jjNode,double distance,Node cNode,Node pNode, Node sNode, Node eNode) {		 
		 Node nNode = null;
		/* ������������ж�˵�����ֻ�·��������˳�Ÿ�·������Ѱ·������false */
		try {
			if (cNode != null && pNode != null && cNode.EID == pNode.EID)
				return false;
			//��ǰ�ڵ㲻Ϊ��
			if (cNode != null) {
				if (cNode.EID == 3235) {
					System.out.print(cNode.EID);
				}
				for (int q = 0; q < endNodeArrayList.size(); q++) {
					Node tNode = new Node();
					tNode = endNodeArrayList.get(q);
					//�ն˽ڵ�������˹�ϵ
					if (cNode.EID == tNode.EID) {
						//�˴���cNode = tNode�Ǵ���ģ�����Ϊ��ַ����
						cNode.setRelationEdge(tNode.relationEdges);
						cNode.setRelationNodes(tNode.relationNodes);	
					}
				}
				int i = 0;
				/* �������ʼ�ڵ�����յ㣬˵���ҵ�һ��·�� */
				if (cNode.EID == eNode.EID)				
				{
					stack.push(cNode);
					/* ���·������pathLength>distance�����������ӡ�����·��������true
					 * 2013/11/30�޸� */
					//�洢·���ڵ�
					getPath(nodesPathArrayList, cNode);	
					showShortestPath(pcsName,distance,jjNode);
					return true;
				}				
				/* �������,����Ѱ· */
				else
				{		
					//��������
					if (cNode.getRelationNodes().size() <= i) {
						return false;
					}
					/* ���뵱ǰ��ʼ�ڵ�cNode�����ӹ�ϵ�Ľڵ㼯�а�˳������õ�һ���ڵ�
					 * ��Ϊ��һ�εݹ�Ѱ·ʱ����ʼ�ڵ� 
					 */
					nNode = cNode.getRelationNodes().get(i);					
					while(nNode != null){
						if (nNode.EID == 3235) {
							System.out.print(cNode.EID);
						}
						//����˴�ΪĿ��㣬Ӧ�����·��
	
//						if (isDistanceSatis(jjNode, cNode, nNode) && isTheSameDirection(jjNode, eNode,180, nNode)
//								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
						if (isTheSameDirection(jjNode, eNode,180, nNode)
								&& isInSpanDistance(jjNode, eNode, 1500, nNode) && isDirectProjSatis(jjNode, eNode, nNode)) {
							//��֤���������·
							if ( nNode.EID != sNode.EID ) {	
								//���ҿ����б����Ƿ���ڸõ�,�����ڷ��ظõ����������򷵻�-1
								if (nNode.EID == 3621) {
									System.out.print(nNode.EID);
								}
								int indexInOpenlist = isListContains(openList, nNode);
								int indexInStack = -1;//�ڹر��б��е�����
								if (indexInOpenlist == -1) {
									indexInStack = isStackContains(stack,nNode);
								}								
								double cost = distance(nNode, cNode);//����һ�ڵ�Ĵ���
								//������ڣ�Gֵ�Ƿ��С�����Ƿ����G��Fֵ
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
								//����ڹر��б�stack��
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
									//��ӵ������б���								
									nNode.setParentNode(cNode);//���ڵ�								
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
						//���򣬼���Ѱ�����ڵ�
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
					//�ӿ����б���ɾ��F��С��node
			        //��ӵ�stack��
					if (openList.size() >=1 ) {
						stack.push(openList.remove(0));
					}					
					//�����б������򣬰�Fֵ��С�ķŵ����
					Collections.sort(openList, new nodeFComparator());
					//ȡ��Fֵ��С�ĵ�
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
	 
	 
	 /*���ؽڵ���ɵ�·��,���յ������ص����*/
	 private void getPath(List<Node> nodesPath,Node node){
	    if(node.getParentNode()!=null){
	        getPath(nodesPath, node.getParentNode());
	    }
	    nodesPath.add(node);
	}
	 
	 /*�ӵ�ǰ�ڵ㵽��һ�ڵ�Ĵ���
	  * cNode:��ǰ�ڵ�
	  * node:��һ�ڵ�*/
	public double calculateCost(Node cNode, Node node){
		double cost = 0;
		ArrayList<Edge>relaEdgesArrayList=new ArrayList<Edge>();//�洢��ǰ�ڵ��ڽӱ�
		relaEdgesArrayList=cNode.relationEdges;
		int relaEdgesCount=relaEdgesArrayList.size();//�洢��ǰ�ڵ��ڽӱ���
		for (int r = 0; r < relaEdgesCount; r++) {
			ArrayList<Node>tempEdgeNodeArrayList=new ArrayList<Node>();//��ǰ�ڽӱ��ϵ㼯��
			Edge relaEdge=new Edge();
			relaEdge=relaEdgesArrayList.get(r);
			double relaEdgeLength=relaEdge.edgeLength;//��ǰ�ߵĳ���
			tempEdgeNodeArrayList=relaEdge.getPointCollArrayList();
			int edgeNodeCount=tempEdgeNodeArrayList.size();//��ǰ�ڽӱ��ϵ���
			//��㡢�յ�
			Node starNode=new Node();
			Node endNode=new Node();
			starNode=tempEdgeNodeArrayList.get(0);
			endNode=tempEdgeNodeArrayList.get(edgeNodeCount-1);
			//������ǰ·�����洢��ǰ·������
			if (isTheSamePoint(starNode, node) || isTheSamePoint(endNode, node)){
				cost = relaEdgeLength;
				break;
			}						
		}
		return cost;
	 }
	
	 //����G,H,Fֵ
	 private void count(Node node, Node eNode, double cost){
	     countG(node, eNode, cost);
	     countH(node, eNode);
	     countF(node);
	 }
	
	 //����Gֵ
	 private void countG(Node node, Node eNode, double cost){
	     if(node.getParentNode()==null){
	         node.setG(cost);
	     }else{
	         node.setG(node.getParentNode().getG() + cost);
	     }
	 }
	
	 //����Hֵ
	 private void countH(Node node, Node eNode){
	     double dist = distance(node, eNode);
	     node.setH(dist);
	 }
	
	 //����Fֵ
	 private void countF(Node node){
	     node.setF(node.getG()+node.getH());
	 }	
	 
	 /*�����б��Ƿ�����õ�,(-1��û���ҵ������򷵻����ڵ�����)*/
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
	 
	 
	 
	 /*���ݣ���ǰ�ڵ��ϵ���ڽӵ��Ƿ���������
	  * ��ǰ�ڵ������ϵ�нڵ㲻��Զ�뷽��ֱ�ߣ�����˽ڵ㲻��������
	  * jjNode:�Ӿ���
	  * eNode��Ѱ·�յ�
	  * nNode����ǰ�ڵ�*/
	 public int tempCount=0;//������� 3��,����ѭ������
	 public int farAwaryCount=0;//����Զ�뷽��ֱ�ߴ���
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
			 boolean isFindMinNode=false;//�Ƿ��ҵ�ͶӰ������С��
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
//		 //�������ζ�Զ��ͶӰֱ�ߣ�����false��˵������������
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
	 
	 /*�㵽����ֱ�ߵ�ͶӰ����*/
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
				
				//���յ����� ��ģ
				double jeNodeDis=distance(jjNode, endNode);
				double jeDeltX=endNodeX-jjNodeX;
				double jeDeltY=endNodeY-jjNodeY;
				double jeDeltZ=endNodeZ-jjNodeZ;
				
				//����Լ��뵱ǰ��������ģ
				double snDeltX=nodeX-jjNodeX;
				double snDeltY=nodeY-jjNodeY;
				double snDeltZ=nodeZ-jjNodeZ;
				double snNodeDis=distance(jjNode, node);
				
				//���յ���������㵱ǰ������ �������н�
				double angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
				//��ǰ�㵽���յ����
				dist=snNodeDis*Math.sin(angle);				
			}
			catch (Exception e) {
				e.printStackTrace();
			}		 
		 return dist;
	 }
	 
	 
	 //�ڵ�ID�Ƿ�����Ϊ���Žڵ�ʹ�ù�
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
	 
	 /*��Ѱ·�ߵĵ�ǰ���Ƿ������յ���ͬһ����
	  * ��Ѱ����Ѱ��Χ�����ڵ�·�ߣ�
	  * �жϣ����յ���������㵱ǰ��������ļн�����Ѱ��Χ���ȵ�1/2�Ƚ�
	  * ���������н�angle<sxhd��Ѱ��Χ���ȣ�˵���õ��������
	  * starNode:���
	  * endNode���յ�
	  * cnode:��Ѱ��ǰ��
	  * a:��Ѱ��Χ������Ҫת��Ϊ����*/
	public boolean isTheSameDirection(Node starNode,Node endNode,int a,Node cNode)
	{		
		double pi=Math.PI;
		double sxhd=a*pi/180;//��Ѱ��Χ����
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
				
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				double seNodeDis=distance(starNode, endNode);
				double scNodeDis=distance(starNode, cNode);
				double seDeltX=endNodeX-starNodeX;
				double seDeltY=endNodeY-starNodeY;
				double seDeltZ=endNodeZ-starNodeZ;
				
				double scDeltX=cNodeX-starNodeX;
				double scDeltY=cNodeY-starNodeY;
				double scDeltZ=cNodeZ-starNodeZ;
				//�������н�
				//�����ǰ�����յ���ͬ��angle�᷵��null
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
	
	/*��Ѱ���Ƿ�����Ѱ���յ�ֱ�߿�ȷ�Χ��
	 * ���㣺varSpan:��㵱ǰ������ģ���������յ㷽�������нǵ�����ֵ
	 * �жϣ�����Ѱ�����Ѱ����ֱ�ߵľ���varSpan<=span���򷵻�true�����򷵻�false
	 * starNode:���
	 * endNode���յ�
	 * cnode:��Ѱ��ǰ��
	 * span����Ѱ���*/
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
				
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				double seNodeDis=distance(starNode, endNode);
				double scNodeDis=distance(starNode, cNode);
				double seDeltX=endNodeX-starNodeX;
				double seDeltY=endNodeY-starNodeY;
				double seDeltZ=endNodeZ-starNodeZ;
				
				double scDeltX=cNodeX-starNodeX;
				double scDeltY=cNodeY-starNodeY;
				double scDeltZ=cNodeZ-starNodeZ;
				
				//�������н�
				angle=Math.acos((seDeltX*scDeltX+seDeltY*scDeltY+seDeltZ*scDeltZ)/(seNodeDis*scNodeDis));
				varSpan=scNodeDis*Math.sin(angle);
			}			
			//���						
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
	
	/*�������Լ��������Լ�������Լ����������
	 * ��ǰ�ڵ�ֱ�����ڵĽڵ��У�������ֱ�ߵ�ͶӰ�Ƿ�Ϊ��С
	 * starNode:��㣬�Ӿ���
	 * endNode���յ�
	 * cnode:��Ѱ��ǰ��
	 * node���뵱ǰ��ֱ�����ڵĵ�*/
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
			
			//���յ����� ��ģ      ��㵱ǰ��������ģ
			double jeNodeDis=distance(jjNode, endNode);
			double jeDeltX=endNodeX-jjNodeX;
			double jeDeltY=endNodeY-jjNodeY;
			double jeDeltZ=endNodeZ-jjNodeZ;
			
			//����Լ��뵱ǰ��ֱ��������������ģ
			double snDeltX=nodeX-jjNodeX;
			double snDeltY=nodeY-jjNodeY;
			double snDeltZ=nodeZ-jjNodeZ;
			double snNodeDis=distance(jjNode, node);
			
			//���յ���������㵱ǰ��ֱ������������ �������н�
			double angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
			//ֱ�����ڵ�ͶӰ����
			double projDis=snNodeDis*Math.sin(angle);
			
			ArrayList<Node>relaNodeArrayList=new ArrayList<Node>();
			relaNodeArrayList=cNode.relationNodes;
			//�ж��뵱ǰ��ֱ�����ڵ㵽����ֱ�ߵ�ͶӰ���������ڵ�ͶӰ���Ƿ�Ϊ��С
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
				
				//�������Լ��20���޲����Լ�������Լ��
				if (isDistanceSatis(jjNode, cNode, tempNode)&&isTheSameDirection(jjNode, endNode, 180, tempNode)
						&&isInSpanDistance(jjNode, endNode,1500, tempNode)) {
					//�н��Լ�ͶӰ
					double tempAngle=Math.acos((jeDeltX*jtDeltX+jeDeltY*jtDeltY+jeDeltZ*jtDeltZ)/(jeNodeDis*jtNodeDis));
					double tempProjDis=jtNodeDis*Math.sin(tempAngle);				
					if (tempProjDis<projDis) {				
						return false;//˵��������С����
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
	
	/*�����Ƿ�����Լ������
	 * �����޲� :200��
	 * jjNode:�Ӿ���
	 * cNode����ǰ��
	 * relaNode����ǰ��ֱ��������*/
	public boolean isDistanceSatis(Node jjNode,Node cNode,Node relaNode)
	{
		if (distance(jjNode, cNode) < distance(jjNode, relaNode) + 200) {
			return true;
		}
		else {
			return false;
		}		
	}

	/*����ͶӰ�����Ƿ�����������ͶӰ���ȱ�����������յ�֮��
	 * ��ǰ�ڵ��뾯����ھ���㡢�յ�ֱ���ϵ�ͶӰ����С�ھ���㡢�յ�֮��ľ���
	 *jjNode:�Ӿ���
	 *endNode:Ѱ·�յ�
	 *node����ǰ�ڵ� */
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
				
				//���յ����� ��ģ      ��㵱ǰ��������ģ
				jeNodeDis=distance(jjNode, endNode);
				double jeDeltX=endNodeX-jjNodeX;
				double jeDeltY=endNodeY-jjNodeY;
				double jeDeltZ=endNodeZ-jjNodeZ;
				
				//����Լ��뵱ǰ��������ģ
				double snDeltX=nodeX-jjNodeX;
				double snDeltY=nodeY-jjNodeY;
				double snDeltZ=nodeZ-jjNodeZ;
				double snNodeDis=distance(jjNode, node);
				
				//���յ���������㵱ǰ������ �������н�
				angle=Math.acos((jeDeltX*snDeltX+jeDeltY*snDeltY+jeDeltZ*snDeltZ)/(jeNodeDis*snNodeDis));
				//ͶӰ����
				projDis=snNodeDis*Math.cos(angle);
			}	
			//angle����Ϊ��ֵ
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
	
		
	/* �жϽڵ��Ƿ���ջ�� */
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

	
	/* ��ʱջ�еĽڵ����һ������·�� */
	public void showAndSavePath (String pcsName,double circle ,Node jjNode )
	{
		try {
			ArrayList<Node>nodeArrayList =new ArrayList<Node>();//˳��洢��ǰ·���ڵ�
			Node[]nodesPath=new Node[nodesPathArrayList.size()];//nodesPath��һ��Ԫ��ΪjjNode
			for (int i = 0; i < nodesPathArrayList.size(); i++) {
				nodesPath[i] = nodesPathArrayList.get(i);
			}
			
			boolean isPath=false;
			//ֻ������һ���ڵ��·�������⴦��
			if (nodesPath.length==1) {	
				isPath=true;
			}
			for (int i = 0; i < nodesPath.length-1; i++) {
				//�뾯�����������󣬿���·��������Լ������20���޲�
				if (distance(jjNode, nodesPath[i+1]) + 200 > distance(jjNode, nodesPath[i])) {
					isPath=true;
				}
				else {
					isPath=false;
					break;
				}
			} 
			/*����ǿ���·����洢�����
			 * �ɽڵ�ID����relationEdgesMap��Ӧ�ߣ�������Ӧ��polyline
			 * ����·��
			 * �ۼ�·�����ȣ���tempAccuLength>=circle,��ȡ��浱ǰpolyline�������������ڼ�������
			 * ��tempAccuLength<circle:��·������������*/			
			int EdgeCount=gInstance.allRelationEdgesMap.get(pcsName).size();
			if (isPath) {
				//���뾯���
				nodeArrayList.add(jjNode);						
																			
				double befoCurAccuLength=0;//�ۼƵ���ǰpolyline֮ǰ�ĳ���
				double tempAccuLength = distance(nodesPath[0], nodesPath[1]);//�ۼƵ���ǰpolyline���ȣ���ʼֵΪ����������浽�����ľ���
				ArrayList<Node> pLineArrayList=new ArrayList<Node>();//�洢���һ��polyline��Node��Ϣ
				for (int j = 1; j < nodesPath.length-1; j++) {
					int EID=nodesPath[j].EID;//�ڵ�ID
					nodeArrayList.add(nodesPath[j]);
					//��Ѱ·���ڵ����ڱ�Edge

					ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//�洢�ڵ��ڽӱ�												
					relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
					int relaEdgeCount=relaEdgeArraylist.size();//�ڵ����ڱ���
					
					//�������ڱ�				
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
						
						//ȡ���ڽ�polyline��vertex
						//����뵱ǰ��ֱ�������ı�				
						if (isTheSamePoint(nodesPath[j+1], froNode)||isTheSamePoint(nodesPath[j+1], toNode)) {
							double length=0;//��ǰpolyline�ĳ���
							//������froPoint��toPoint����룬�����Ϊ����
							for (int p = 0; p < pointCount-1; p++) {
								
								Node t1Node=new Node();
								Node t2Node=new Node();
								t1Node=edgePointCollArrayList.get(p);
								t2Node=edgePointCollArrayList.get(p+1);
								length=length+distance(t1Node, t2Node);	
															
							}
							tempAccuLength=tempAccuLength+length;
							befoCurAccuLength=tempAccuLength-length;							

							//��β����ڵ��غϣ�ȥ����β�㣬ֻȡ�м��
							//����ǰ�ڵ���plinePoints���׵���ͬ���򰴴�˳������point
							//����������point
							Node tempSNode=new Node();//�׵�
							Node tempENode=new Node();//β��
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
								//�������ؾ��룬ֻ�洢���һ��polyline��Ϣ
								else {
									
									//�洢��·�������ص㣬Ӧ�����ؾ��Ժ�ĵ�·�������ص�
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(nodesPath[j+1]);
									}
									//ȥ����ͬ�ĵ�·�������ص�									
									else {
										//�Ѵ��ڵ�·�������ص����µ�·�������ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ��·�������ص�
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//�����ͬһ�㣬����Ч������ѭ��
											if (isTheSamePoint(nodesPath[j+1], ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(nodesPath[j+1]);
										}						
									}
									
									/*���У���polylineֻ���������㣬��pointCount=2,ִ�иþ��ӣ�����ִ��forѭ��*/
									if (pointCount==2) {
										pLineArrayList.add(tempSNode);
										pLineArrayList.add(tempENode);
										
									}
									for (int l = 1; l < pointCount-1; l++) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										pLineArrayList.add(tNode);//�洢���һ��polyline��Ϣ
										//���polyline�洢����point
										if (l==pointCount-2) {
											
											tNode=edgePointCollArrayList.get(0);
											pLineArrayList.add(0, tNode);//��һ��λ�ò���õ�
											tNode=edgePointCollArrayList.get(pointCount-1);
											pLineArrayList.add(tNode);
											
										}
									}
								}								
							}
							
							//��β����ͬ
							else if (isTheSamePoint(nodesPath[j], tempENode)) {
								if (tempAccuLength<circle) {
									for (int l =pointCount-2; l>0 ; l--) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										nodeArrayList.add(tNode);
									}
								}
								//�������ؾ��룬ֻ�洢���һ��polyline��Ϣ
								else  {
									
									//�洢��·�������ص㣬Ӧ�����ؾ��Ժ�ĵ�·�������ص�
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(nodesPath[j+1]);
									}
									//ȥ����ͬ�ĵ�·�������ص�									
									else {
										//�Ѵ��ڵ�·�������ص����µ�·�������ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ��·�������ص�
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//�����ͬһ�㣬����Ч������ѭ��
											if (isTheSamePoint(nodesPath[j+1], ttNode)) {
												isValidJunc=false;
												break;	
											}							
										}
										if (isValidJunc) {
											RoadJuncArrayList.add(nodesPath[j+1]);
										}						
									}

									
									/*���У���polylineֻ���������㣬��pointCount=2,ִ�иþ��ӣ�����ִ��forѭ��*/
									if (pointCount==2) {
										pLineArrayList.add(tempSNode);
										pLineArrayList.add(tempENode);
									}
									for (int l =pointCount-2; l>0 ; l--) {
										Node tNode=new Node();
										tNode=edgePointCollArrayList.get(l);
										pLineArrayList.add(tNode);//�洢���һ��polyline��Ϣ
										
										//���polyline�洢����point
										if (l==1) {
											tNode=edgePointCollArrayList.get(pointCount-1);
											pLineArrayList.add(0,tNode);//��һ��λ�ò���õ�
											tNode=edgePointCollArrayList.get(0);
											pLineArrayList.add(tNode);
										}
									}
								}																
							}							
							break;//�������ڽ�polyline������
						}				
					}
					if (tempAccuLength>=circle) {
						//��ǰ�ڵ��������������ؼ�������������ѭ�������ص�					
						break;
					}				
				}				
								
				/*��ȡ��nodesPath���һ��ֱ�����ڵıߣ�
				 * �þ���Լ����������
				 * ���һ���ɾ���Լ�����ܼ���������polyline,��map�ֱ�洢*/					
				Map<Integer, ArrayList<Node>> plineMap=new HashMap<Integer, ArrayList<Node>>();//�洢·��ID��·��
				Map<Integer, java.lang.Double>pathDistanceMap=new HashMap<Integer, java.lang.Double>();//�洢·��ID��·������

				int mapInt=0;//��������������polyline��Ŀ������ΪID��pLineArrayList����
//				Integer mapInt=0;//������polyline��Ŀ������ΪID��pLineArrayList����
				if (tempAccuLength<circle) {
					int EID=nodesPath[nodesPath.length-1].EID;//���ڵ�ID 
					nodeArrayList.add(nodesPath[nodesPath.length-1]);//������ڵ�	
					
					//��Ѱ�ڵ����ڱߵ�EdgeID					
					ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//�洢�ڵ��ڽӱ�	
//					relaEdgeArraylist=relationEdgesMap.get(EID);//�ڵ�EID�ڽӱ�	
					relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
					int relaEdgeCount=relaEdgeArraylist.size();					
							
					//�������ڱ�				
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
						//�����ȡ��λ����ͬ�����ܵ��´���������������ж�
						if (isTheSamePoint(froNode, nodesPath[nodesPath.length-1])) {
							froNode=nodesPath[nodesPath.length-1];
						}
						else if (isTheSamePoint(toNode, nodesPath[nodesPath.length-1])) {
							toNode=nodesPath[nodesPath.length-1];
						}
						//ȡ���ڽ�polyline��vertex
						//����뵱ǰ��ֱ�������ı�
						//�þ����������������Լ��
						if (distance(jjNode, froNode)>distance(jjNode, nodesPath[nodesPath.length-1])||distance(jjNode, toNode)>distance(jjNode, nodesPath[nodesPath.length-1])) {				
							
							double length=0;//��ǰpolyline�ĳ���
							double tempPathLength=tempAccuLength;
						
							//������froPoint��toPoint����룬�����Ϊ����
							for (int p = 0; p < pointCount-1; p++) {
								Node t1Node=new Node();
								Node t2Node=new Node();
								t1Node=edgePointCollArrayList.get(p);
								t2Node=edgePointCollArrayList.get(p+1);								
								length=length+distance(t1Node, t2Node);																
							}												
							
							//����ۼ�·��������ȻС�����ؾ��룬·��������Ҫ�󣬼���������һ����
							if (tempPathLength+length<circle) {
								continue;//������һ��Edge
							}	
							//���������ؾ��룬�洢polyline��Ϣ
							//����ǰ�ڵ���plinePoints���׵���ͬ���򰴴�˳������point
							//����������point	
							//polylineҪ�洢����point
							else  {	
								
								ArrayList<Node>endEdgePLineArrayList=new ArrayList<Node>();//�洢polyline������point
								mapInt++;
								Node tempSNode=new Node();//�׵�
								Node tempENode=new Node();//β��
								tempSNode=edgePointCollArrayList.get(0);
								tempENode=edgePointCollArrayList.get(pointCount-1);
															
								if (isTheSamePoint(nodesPath[nodesPath.length-1], tempSNode)) {
									//�洢��·�������ص㣬Ӧ�����ؾ��Ժ�ĵ�·�������ص�
									//2013/11/30
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(tempENode);
									}
									//ȥ����ͬ�ĵ�·�������ص�									
									else {
										//�Ѵ��ڵ�·�������ص����µ�·�������ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ��·�������ص�
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//�����ͬһ�㣬����Ч������ѭ��
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
									
									//�洢��·�������ص�
									if (RoadJuncArrayList.size()==0) {
										RoadJuncArrayList.add(tempSNode);
									}
									//ȥ����ͬ�ĵ�·�������ص�									
									else {
										//�Ѵ��ڵ�·�������ص����µ�·�������ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ��·�������ص�
										boolean isValidJunc=true;
										for (int p = 0; p < RoadJuncArrayList.size(); p++) {
											Node ttNode=new Node();
											ttNode=RoadJuncArrayList.get(p);
											//�����ͬһ�㣬����Ч������ѭ��
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
					
					/*�󽻵㲢�洢·��
					 *�����������⴦��:�������һ��polyline�����ص�*/
					if (plineMap.size()>0) {
						
						for (int i = 0; i < plineMap.size(); i++) {
														
							ArrayList<Node>tempNodeArrayList=new ArrayList<Node>();//�����ʱ·��
							//���������ֵ
							for (int q = 0; q < nodeArrayList.size(); q++) {
								tempNodeArrayList.add(nodeArrayList.get(q));
							}
						
							ArrayList<Node> tempPlineArrayList=new ArrayList<Node>();
							tempPlineArrayList=plineMap.get(i+1);
							ArrayList<Node> retuTempPlineArrayList=new ArrayList<Node>();
							//�����ص�
							Node interPoint=new Node();//���ص�
							retuTempPlineArrayList=interPoint(tempPlineArrayList, tempAccuLength, circle);
							interPoint=retuTempPlineArrayList.get(retuTempPlineArrayList.size()-1);//���ص�
							for (int j = 0; j < retuTempPlineArrayList.size(); j++) {
								tempNodeArrayList.add(retuTempPlineArrayList.get(j));//�洢�����ص�·�������е�
							}
							
							/*������ͬ·�������ص㲻������ͬ
							 * ���·���д��ڸ����ص㣬ȥ����·��
							 * ��pathArrayLists������·���������·��
							 * ��pathArrayLists�д���·�������ж����ص��Ƿ����Ѵ���·�������ص���ͬ*/							
							if (pathArrayLists.size()==0) {
								pathArrayLists.add(tempNodeArrayList);
							}
							else {
								//�Ѵ���·�����ص�����·�����ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ���ص�
								boolean isValidPath=true;
								for (int p = 0; p < pathArrayLists.size(); p++) {
									ArrayList<Node>tArrayList=new ArrayList<Node>();
									tArrayList=pathArrayLists.get(p);
									int count=tArrayList.size();
									Node node=new Node();
									node=tArrayList.get(count-1);//ÿ��·�������һ��洢���ص�
									//�����ͬһ���ص㣬���·����Ч������ѭ��
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
					//�����ص��Լ�����·��	����pLineArrayList
					//ֻ�洢���plyline
					//���ش�����pLineArrayList�Լ����ص�
					ArrayList<Node> retuTempPlineArrayList=new ArrayList<Node>();
					retuTempPlineArrayList=interPoint(pLineArrayList,befoCurAccuLength, circle);
					Node interPoint=new Node();//���ص�
					interPoint=retuTempPlineArrayList.get(retuTempPlineArrayList.size()-1);//���ص�
					for (int i = 0; i < retuTempPlineArrayList.size(); i++) {
						nodeArrayList.add(retuTempPlineArrayList.get(i));//�洢�����ص�·�������е�
					}
					
					/*������ͬ·�������ص㲻������ͬ
					 * ���·���д��ڸ����ص㣬ȥ����·��
					 * ��pathArrayLists������·���������·��
					 * ��pathArrayLists�д���·�������ж����ص��Ƿ����Ѵ���·�������ص���ͬ*/							
					if (pathArrayLists.size()==0) {
						pathArrayLists.add(nodeArrayList);
					}
					else {
						//�Ѵ���·�����ص�����·�����ص�Ƚϣ���ȷ���Ƿ�Ϊͬһ���ص�
						boolean isValidPath=true;
						for (int p = 0; p < pathArrayLists.size(); p++) {
							ArrayList<Node>tArrayList=new ArrayList<Node>();
							tArrayList=pathArrayLists.get(p);
							int count=tArrayList.size();
							Node node=new Node();
							node=tArrayList.get(count-1);
							//�����ͬһ���ص㣬���·����Ч������ѭ��
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
	
	/*�����ص�,�������ص㣬û�о�ȷ����
	*pLineArrayList:���һ��polyline,ȥ���׵�
	*befoCurAccuLength���ۼƵ���ǰpolyline֮ǰ�ĳ���
	*circle�����ذ뾶
	*tempArrayList:����洢���ص�*/
	public ArrayList<Node> interPoint (ArrayList<Node> pLineArrayList,double befoCurAccuLength,double circle)
	{
		ArrayList<Node> tempArrayList=new ArrayList<Node>();//�����ʱ��
		int nodeCount=pLineArrayList.size();
		try {
			
			for (int i = 0; i < nodeCount-1; i++) {
				double tempdis=distance(pLineArrayList.get(i), pLineArrayList.get(i+1));
				befoCurAccuLength=befoCurAccuLength+tempdis;
				/*��һ���ڵ��Ѵ��ڣ�����ȡ��*/
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
	
	/*�����������·��*/
	public void showShortestPath (String pcsName,double circle ,Node jjNode )
	{
		try {
			ArrayList<Node>nodeArrayList =new ArrayList<Node>();//˳��洢��ǰ·���ڵ�
			Node[]nodesPath=new Node[nodesPathArrayList.size()];//nodesPath��һ��Ԫ��ΪjjNode
			for (int i = 0; i < nodesPathArrayList.size(); i++) {
				nodesPath[i] = nodesPathArrayList.get(i);
			}
			
			/*����ǿ���·����洢�����
			 * �ɽڵ�ID����relationEdgesMap��Ӧ�ߣ�������Ӧ��polyline
			 * ����·��
			 * �ۼ�·�����ȣ���tempAccuLength>=circle,��ȡ��浱ǰpolyline�������������ڼ�������
			 * ��tempAccuLength<circle:��·������������*/			
			//���뾯���
			nodeArrayList.add(jjNode);																									
			for (int j = 1; j < nodesPath.length-1; j++) {
				int EID=nodesPath[j].EID;//�ڵ�ID
				nodeArrayList.add(nodesPath[j]);
				//��Ѱ·���ڵ����ڱ�Edge
				ArrayList<Edge> relaEdgeArraylist=new ArrayList<Edge>();//�洢�ڵ��ڽӱ�												
				relaEdgeArraylist=gInstance.allRelationEdgesMap.get(pcsName).get(EID);
				for (int p = 0; p < endNodeArrayList.size(); p++) {
					if (EID == endNodeArrayList.get(p).EID) {
						relaEdgeArraylist = endNodeArrayList.get(p).relationEdges;
						break;
					}
				}
				int relaEdgeCount=relaEdgeArraylist.size();//�ڵ����ڱ���
				
				//�������ڱ�				
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
					
					//ȡ���ڽ�polyline��vertex
					//����뵱ǰ��ֱ�������ı�				
					if (isTheSamePoint(nodesPath[j+1], froNode)||isTheSamePoint(nodesPath[j+1], toNode)) {						

						//��β����ڵ��غϣ�ȥ����β�㣬ֻȡ�м��
						//����ǰ�ڵ���plinePoints���׵���ͬ���򰴴�˳������point
						//����������point
						Node tempSNode=new Node();//�׵�
						Node tempENode=new Node();//β��
						tempSNode=edgePointCollArrayList.get(0);
						tempENode=edgePointCollArrayList.get(pointCount-1);
						
						if (isTheSamePoint(nodesPath[j], tempSNode)) {	
							for (int l = 1; l < pointCount-1; l++) {
								Node tNode=new Node();
								tNode=edgePointCollArrayList.get(l);
								nodeArrayList.add(tNode);	
							}								
						}							
						//��β����ͬ
						else if (isTheSamePoint(nodesPath[j], tempENode)) {
							for (int l =pointCount-2; l>0 ; l--) {
								Node tNode=new Node();
								tNode=edgePointCollArrayList.get(l);
								nodeArrayList.add(tNode);
							}																
						}							
						break;//�������ڽ�polyline������
					}				
				}				
			}	
			//�������һ���ڵ�
			nodeArrayList.add(nodesPath[nodesPath.length-1]);
			pathArrayLists.add(nodeArrayList);	
			
		} catch (Exception ae) {
			System.err.println("Caught AutomationException: " + ae.getMessage() + "\n");
		    ae.printStackTrace();
		}	    											
	}
			    
	/*�������ڵ�֮��ľ���*/
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
	
	/*��γ������ת��Ϊƽ������*/
	 //a�����򳤰��ᣬWGS84����ĳ�����Ϊ6378137.000001
   //e: �����һƫ���ʵ�ƽ����WGS84�����eΪ0.00669437999013   	
	public static double coordinateTransGetx(Node tNode){
		double L=tNode.getX();//����
		double B=tNode.getY();//γ��
	    double pi=Math.PI;
	    L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//�̸߳�0ֵ
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
	    double x= (N + H)*Math.cos(B)*Math.cos(L);
		return x;
	}
	
	public static double coordinateTransGety(Node tNode){
		double L=tNode.getX();//����
		double B=tNode.getY();//γ��
		double pi=Math.PI;
		L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//�̸߳�0ֵ
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
		double y=(N + H)*Math.cos(B)*Math.sin(L);
		return y;		
	}
	
	public static double coordinateTransGetz(Node tNode){
		double L=tNode.getX();//����
		double B=tNode.getY();//γ��
		double pi=Math.PI;
		L=L*pi/180;
	    B=B*pi/180;
		double e=0.00669437999013;
		double a=6378137.000001;
		double H=0;//�̸߳�0ֵ
		double W = Math.sqrt((1-e*Math.pow(Math.sin(B),2)));
	    double N = a/W;
	    double z = (N * (1 - e) + H) * Math.sin(B);
		return z;
	}
	
	/*�ж��Ƿ�Ϊͬһ��*/
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
	
	/*�жϽڵ��Ƿ�����x��yΪ���ĵ�Բ�ڣ������ο��ٴ�ѡ��Բ�ٽ�һ��ѡ��
	 * jjNodeΪ���������ĵ�����,��γ��
	 * nodeΪ���жϽڵ㣬node�ڵ�洢����ҲΪ��γ�ȣ���Ҫת��
	 * rΪ����������Բ�뾶*/
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