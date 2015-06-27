package mapMatchingGPS;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadMapMatchReadRoadData extends Thread {
	private String filePath;
	private String startIndex;
	private String endIndex;
	private String dataname3;
	private String dataname4;
	public String getPcsname() {
		return filePath;
	}
	public void setPcsname(String filePath) {
		this.filePath = filePath;
	}
	public String getDataname1() {
		return startIndex;
	}
	public void setDataname1(String startIndex) {
		this.startIndex = startIndex;
	}
	public String getDataname2() {
		return endIndex;
	}
	public void setDataname2(String endIndex) {
		this.endIndex = endIndex;
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
	

	
//	public void run()
//	{
//		readData(filePath, startIndex, endIndex);
//	}
//	
//	 public void readData(String filePath,String startIndex,String endIndex)
//	 {
//		 try {
//				float startRead=System.currentTimeMillis();	
//				System.out.print(pcsName+"开始读数据："+startRead+"\n");
//				readSDEData startReadSDEData=new readSDEData();
//				ArrayList<Node> juncArraylist=new ArrayList<Node>();//节点
//				ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();//线
//				ArrayList<surface> surfaceArrayList=new ArrayList<surface>();//面
//				ArrayList<roadName>roadNameArrayList=new ArrayList<roadName>();//道路名
//				
//				juncArraylist= roadNetworkAnalysisImpl.instance().allJuncArraylistMap.get(pcsName);
//				polylineCollArrayList=roadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.get(pcsName);
//				surfaceArrayList=roadNetworkAnalysisImpl.instance().allSurfaceArrayListMap.get(pcsName);
//				roadNameArrayList = roadNetworkAnalysisImpl.instance().allRoadNameArrayMap.get(pcsName);
//				startReadSDEData.readData(pcsName,junctionData,splitLineData, polygonData,surfaceArrayList, juncArraylist, polylineCollArrayList,roadNameArrayList);	
//				float endRead=System.currentTimeMillis();
//				System.out.print(pcsName+"结束读数据："+endRead+"\n");
//				float readTime=(endRead-startRead)/1000;
//				System.out.print(pcsName+"读数据时间:"+readTime+"\n");
//				float startTopo=System.currentTimeMillis();
//				System.out.print("开始构建拓扑："+startTopo+"\n");
//				createTopology(pcsName);
//				
//				float endTopo=System.currentTimeMillis();
//				System.out.print("拓扑构建结束："+endTopo+"\n");	
//				System.out.print("拓扑构建时间"+"\n");
//				float time=(float)((endTopo-startTopo)/1000);
//				System.out.print(time);	
//				this.stop();
//			} catch (Exception e) {
//				System.out.print(pcsName+"读取数据失败!"+"\n");
//			}		
//	 }
//	 
//	 public void createTopology(String pcsName)
//	 {
//			try {
//				ArrayList<Node> nodesArrayList=new ArrayList<Node>();
//				Map<Integer,ArrayList<Edge>> relationEdgesMap=new HashMap<Integer,ArrayList<Edge>>();
//				ArrayList<Node> juncArraylist=new ArrayList<Node>();
//				ArrayList<Edge> polylineCollArrayList=new ArrayList<Edge>();
//				nodesArrayList=roadNetworkAnalysisImpl.instance().allNodesArrayListMap.get(pcsName);
//				relationEdgesMap=roadNetworkAnalysisImpl.instance().allRelationEdgesMap.get(pcsName);
//				juncArraylist=roadNetworkAnalysisImpl.instance().allJuncArraylistMap.get(pcsName);
//				polylineCollArrayList=roadNetworkAnalysisImpl.instance().allPolylineCollArraylistMap.get(pcsName);
//				while(nodesArrayList.size()==0||relationEdgesMap.size()==0)
//				{		
//					getJunctionRelationship(pcsName,nodesArrayList, relationEdgesMap, juncArraylist, polylineCollArrayList);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}		
//	}
	 
	
	
}
