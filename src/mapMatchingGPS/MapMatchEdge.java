package mapMatchingGPS;
import java.io.Serializable;
import java.util.ArrayList;

import javax.print.DocFlavor.STRING;


public class MapMatchEdge implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7399528492795010157L;
	private int edgeID = 0;
	/*�洢��ǰ���еĵ�*/
	private ArrayList<MapMatchNode> pointCollArrayList = null;
	/*�洢�ߵĳ���*/
	private double edgeLength = 0;
	private String edgeName = "none";
	//��Ϊ����ÿ��·�ε�
	private MapMatchNode beginPoint;//���
	private MapMatchNode endPoint;//�յ�
	private MapMatchNode firstLevelConnBeginPoint;//һ����ͨ���,ͨ���õ�����ֱ������
	private MapMatchNode firstLevelConnEndPoint;//һ����ͨ�յ㣬�ߵ���һ���˵�
	private MapMatchNode secoLevelConnBeginPoint;//������ͨ���
	private MapMatchNode secoLevelConnEndPoint;//������ͨ�յ�
	private MapMatchNode thirdLevelConnBeginPoint;//������ͨ���
	private MapMatchNode thirdLevelConnEndPoint;//������ͨ�յ�	
	private ArrayList<MapMatchEdge> firstLevelConnEdgeArray;//һ����ͨ��
	private ArrayList<MapMatchEdge> secoLevelConnEdgeArray;//������ͨ��
	private ArrayList<MapMatchEdge> thirdLevelConnEdgeArray;//������ͨ��
	
	public void setEdgeLength(double length){
		this.edgeLength = length;
	}
	
	public double getEdgeLength(){
		return edgeLength;		
	}
	
	public void setEdgeName(String edgeName){
		this.edgeName = edgeName;
	}
	
	public String getEdgeName(){
		return edgeName;		
	}
	
	public void setEdgeID(int eid){
	    this.edgeID = eid;
	}
	
	public int getEdgeID(){
	    return edgeID;
	}
	    
	public ArrayList<MapMatchNode> getPointCollArrayList(){
	    return pointCollArrayList;
	}
	
	public void setPointCollArrayList(ArrayList<MapMatchNode> pointColl){
	    this.pointCollArrayList = pointColl;
	}
	
	public void setBeginPoint(MapMatchNode tbeginPoint){
	    this.beginPoint = tbeginPoint;
	}
	
	public MapMatchNode getBeginPoint(){
	    return beginPoint;
	}
	
	public void setEndPoint(MapMatchNode tEndPoint){
	    this.endPoint = tEndPoint;
	}
	
	public MapMatchNode getEndPoint(){
	    return endPoint;
	}
	
	public void setFirstLevelConnBeginPoint(MapMatchNode tfirstLevelConnBeginPoint){
	    this.firstLevelConnBeginPoint = tfirstLevelConnBeginPoint;
	}
	
	public MapMatchNode getFirstLevelConnBeginPoint(){
	    return firstLevelConnBeginPoint;
	}
	
	public void setFirstLevelConnEndPoint(MapMatchNode tfirstLevelConnEndPoint){
	    this.firstLevelConnEndPoint = tfirstLevelConnEndPoint;
	}
	
	public MapMatchNode getFirstLevelConnEndPoint(){
	    return firstLevelConnEndPoint;
	}
	
	public void setSecoLevelConnBeginPoint(MapMatchNode tsecoLevelConnBeginPoint){
	    this.secoLevelConnBeginPoint = tsecoLevelConnBeginPoint;
	}
	
	public MapMatchNode getSecoLevelConnBeginPoint(){
	    return secoLevelConnBeginPoint;
	}
	
	public void setSecoLevelConnEndPoint(MapMatchNode tsecoLevelConnEndPoint){
	    this.secoLevelConnEndPoint = tsecoLevelConnEndPoint;
	}
	
	public MapMatchNode getSecoLevelConnEndPoint(){
	    return secoLevelConnEndPoint;
	}
	
	public void setThirdLevelConnBeginPoint(MapMatchNode tthirdLevelConnBeginPoint){
	    this.thirdLevelConnBeginPoint = tthirdLevelConnBeginPoint;
	}
	
	public MapMatchNode getThirdLevelConnBeginPoint(){
	    return thirdLevelConnBeginPoint;
	}
	
	public void setThirdLevelConnEndPoint(MapMatchNode tthirdLevelConnEndPoint){
	    this.thirdLevelConnEndPoint = tthirdLevelConnEndPoint;
	}
	
	public MapMatchNode getThirdLevelConnEndPoint(){
	    return thirdLevelConnEndPoint;
	}
	
	public void setFirstLevelConnEdgeArray(ArrayList<MapMatchEdge> tfirstLevelConnEdgeArray){
	    this.firstLevelConnEdgeArray = tfirstLevelConnEdgeArray;
	}
	
	public ArrayList<MapMatchEdge> getFirstLevelConnEdgeArray(){
	    return firstLevelConnEdgeArray;
	}
	
	public void setSecoLevelConnEdgeArray(ArrayList<MapMatchEdge> tsecoLevelConnEdgeArray){
	    this.secoLevelConnEdgeArray = tsecoLevelConnEdgeArray;
	}
	
	public ArrayList<MapMatchEdge> getSecoLevelConnEdgeArray(){
	    return secoLevelConnEdgeArray;
	}
	
	public void setThirdLevelConnEdgeArray(ArrayList<MapMatchEdge> tthirdLevelConnEdgeArray){
	    this.thirdLevelConnEdgeArray = tthirdLevelConnEdgeArray;
	}
	
	public ArrayList<MapMatchEdge> getThirdLevelConnEdgeArray(){
	    return thirdLevelConnEdgeArray;
	}
}
