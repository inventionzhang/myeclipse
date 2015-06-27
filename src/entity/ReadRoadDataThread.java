package entity;


import implement.RoadNetworkAnalysisImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReadRoadDataThread extends Thread {
	public String getPcsname() {
		return pcsname;
	}
	public void setPcsname(String pcsname) {
		this.pcsname = pcsname;
	}
	public String getDataname1() {
		return dataname1;
	}
	public void setDataname1(String dataname1) {
		this.dataname1 = dataname1;
	}
	public String getDataname2() {
		return dataname2;
	}
	public void setDataname2(String dataname2) {
		this.dataname2 = dataname2;
	}
	public String getDataname3() {
		return dataname3;
	}
	public void setDataname3(String dataname3) {
		this.dataname3 = dataname3;
	}
	public String getDataname4() {
		return dataname4;
	}
	public void setDataname4(String dataname4) {
		this.dataname4 = dataname4;
	}
	private String pcsname;
	private String dataname1;
	private String dataname2;
	private String dataname3;
	private String dataname4;

	
	public void run()
	{
		readData(pcsname, dataname2, dataname3, dataname4);
	}
	
	 public void readData(String pcsName,String junctionData,String splitLineData,String polygonData)
	 {
		 try {
				float startRead=System.currentTimeMillis();	
				System.out.print(pcsName+"开始读数据："+startRead+"\n");
				readSDEData startReadSDEData=new readSDEData();
				ArrayList<Node> juncArraylist=new ArrayList<Node>();//节点
				ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();//线
				ArrayList<surface> surfaceArrayList=new ArrayList<surface>();//面
				ArrayList<roadName>roadNameArrayList=new ArrayList<roadName>();//道路名
				
				juncArraylist= RoadNetworkAnalysisImpl.instance().allJuncArraylistMap.get(pcsName);
				polylineCollArrayList=RoadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.get(pcsName);
				surfaceArrayList=RoadNetworkAnalysisImpl.instance().allSurfaceArrayListMap.get(pcsName);
				roadNameArrayList = RoadNetworkAnalysisImpl.instance().allRoadNameArrayMap.get(pcsName);
				startReadSDEData.readData(pcsName,junctionData,splitLineData, polygonData,surfaceArrayList, juncArraylist, polylineCollArrayList,roadNameArrayList);	
				float endRead=System.currentTimeMillis();
				System.out.print(pcsName+"结束读数据："+endRead+"\n");
				float readTime=(endRead-startRead)/1000;
				System.out.print(pcsName+"读数据时间:"+readTime+"\n");
				float startTopo=System.currentTimeMillis();
				System.out.print("开始构建拓扑："+startTopo+"\n");
				createTopology(pcsName);
				
				float endTopo=System.currentTimeMillis();
				System.out.print("拓扑构建结束："+endTopo+"\n");	
				System.out.print("拓扑构建时间"+"\n");
				float time=(float)((endTopo-startTopo)/1000);
				System.out.print(time);	
				this.stop();
			} catch (Exception e) {
				System.out.print(pcsName+"读取数据失败!"+"\n");
			}		
	 }
	 
	 public void createTopology(String pcsName)
	 {
			try {
				ArrayList<Node> nodesArrayList=new ArrayList<Node>();
				Map<Integer,ArrayList<Edge>> relationEdgesMap=new HashMap<Integer,ArrayList<Edge>>();
				ArrayList<Node> juncArraylist=new ArrayList<Node>();
				ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();
				nodesArrayList=RoadNetworkAnalysisImpl.instance().allNodesArrayListMap.get(pcsName);
				relationEdgesMap=RoadNetworkAnalysisImpl.instance().allRelationEdgesMap.get(pcsName);
				juncArraylist=RoadNetworkAnalysisImpl.instance().allJuncArraylistMap.get(pcsName);
				polylineCollArrayList=RoadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.get(pcsName);
				while(nodesArrayList.size()==0||relationEdgesMap.size()==0)
				{		
					getJunctionRelationship(pcsName,nodesArrayList, relationEdgesMap, juncArraylist, polylineCollArrayList);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}
	 
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
				
			}	   
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
	
}
