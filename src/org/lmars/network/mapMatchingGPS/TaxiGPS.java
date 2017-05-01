package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.NEW;
//出租车GPS数据
public class TaxiGPS implements Serializable{		
	/**
	 * 
	 */
	private static final long serialVersionUID = -2138877665135722764L;
	public String targetID;//出租车ID
	public String localTime;
	public double longitude;
	public double latitude;
	private double correctLong = -1;
	private double correctLati = -1;
	private boolean isGPSCorrected = false;//GPS点是否已经纠正过
	public double speed;
	public double heading;
	public int status;
	public int lineID;
	private int belongLineID = -1;//地图匹配后GPS点所属路段ID
	private String belongLinkName = "default";
	private boolean isMaxTimeContinuousStaticPoint = false;//最大时间连续静止点
	private int continuousStaticTime = 0;//连续静止时间
	private int changedStatus = -1;//出租车转换状态 0：下客; 1： 上客; -1：不存在转换状态
	private double tripDirection = -1.0;//出行方向
	private double tripDistance = -1.0;//出行距离
	
	//GPS点的候选道路集合
	public ArrayList<MapMatchEdge> candidateEdgeSetArrayList;
	//路段ID以及对应的距离得分和方向得分以及总的分数
	//存储GPS点到线段的距离，以及距离分数
	public Map<Integer, Double[]> distScoreSetMap;
	//二维数组中依次存储GPS点运动方向与候选路段的夹角(弧度)、方向分数、方向一致性（路段方向：beginNode——>endNode）
	public Map<Integer, Double[]> directScoreSetMap;
	//路段的综合得分，以路段ID作为键
	public Map<Integer, Double> distDirectScoreSetMap;
	//以edgeID为索引，存储候选边ID以及对应的到该边的路径EID、与路径方向关系集合，只存储得分最高的路径ID
	public Map<Integer, ArrayList<Integer[]>> pathEIDMap = new HashMap<Integer, ArrayList<Integer[]>>();
	//以edgeID为索引，存储候选边ID以及对应的到该边的路径的累积scores
	public Map<Integer, Double> edgeAccuScoreMap = new HashMap<Integer, Double>();
	
	public ArrayList<Integer> positonSet;
	public int position;
	
	public String getTargetID(){
		return targetID;
	}
	
	public void setTargetID(String ttargetID){
		this.targetID = ttargetID;
	}
	
	public String getLocalTime(){
		return localTime;
	}
	
	public void setLocalTime(String tlocalTime){
		this.localTime = tlocalTime;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public void setLongitude( double tLongitude){
		this.longitude = tLongitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public void setLatitude(double tLatitude){
		this.latitude = tLatitude;
	}
	
	public double getCorrectLongitude(){
		return correctLong;
	}
	
	public void setCorrectLongitude( double tLongitude){
		this.correctLong = tLongitude;
	}
	
	public double getCorrectLatitude(){
		return correctLati;
	}
	
	public void setCorrectLatitude(double tLatitude){
		this.correctLati = tLatitude;
	}
	
	public boolean getIsGPSCorrected(){
		return isGPSCorrected;
	}
	
	public void setIsGPSCorrected(boolean isGPSCorrected){
		this.isGPSCorrected = isGPSCorrected;
	}
	
	public double getSpeed(){
		return speed;
	}
	
	public void setSpeed(double tSpeed){
		this.speed = tSpeed;
	}
	
	public double getHeading(){
		return heading;
	}
	
	public void setHeading(double tHeading){
		this.heading = tHeading;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setStatus(int tStatus){
		this.status = tStatus;
	}
	
	public int getLineID(){
		return lineID;
	}
	
	public void setLineID(int tlineID){
		this.lineID = tlineID;
	}
	
	public int getBelongLineID(){
		return belongLineID;
	}
	
	public void setBelongLineID(int belongLineID){
		this.belongLineID = belongLineID;
	}
	
	public String getBelongLinkName(){
		return belongLinkName;
	}
	
	public void setBelongLinkName(String belongLinkName){
		this.belongLinkName = belongLinkName;
	}
	
	public boolean getMaxTimeContinuousStaticPoint() {
		return isMaxTimeContinuousStaticPoint;
	}
	
	public void setMaxContinuousStaticPoint(boolean isMaxTimeContinuousStaticPoint) {
		this.isMaxTimeContinuousStaticPoint = isMaxTimeContinuousStaticPoint;
	}
	
	public int getContinuousStaticTime() {
		return continuousStaticTime;
	}
	
	public void setContinuousStaticTime(int continuousStaticTime) {
		this.continuousStaticTime = continuousStaticTime;
	}
	
	public int getChangedStatus() {
		return changedStatus;
	}
	
	public void setChangedStatus(int changedStatus) {
		this.changedStatus = changedStatus;
	}
	
	public int getPosition(){
		return position;
	}
	
	public void setPosition(int tPosition){
		this.position = tPosition;
	}
	
	public ArrayList<MapMatchEdge> getCandidateEdgeSetArrayList(){
		return candidateEdgeSetArrayList;
	}
	
	public void setCandidateEdgeSetArrayList(ArrayList<MapMatchEdge> tcandidateEdgeSetArrayList){
		this.candidateEdgeSetArrayList = tcandidateEdgeSetArrayList;
	}
	
	public Map<Integer, Double[]> getDistScoreSetMap(){
		return distScoreSetMap;
	}
	
	public void setDistScoreSetMap(Map<Integer, Double[]> tdistScoreSetMap){
		this.distScoreSetMap = tdistScoreSetMap;
	}
	
	public Map<Integer, Double[]> getDirectScoreSetMap(){
		return directScoreSetMap;
	}
	
	public void setDirectScoreSetMap(Map<Integer, Double[]> tdirectScoreSetMap){
		this.directScoreSetMap = tdirectScoreSetMap;
	}
	
	public Map<Integer, Double> getDistDirectScoreSetMap(){
		return distDirectScoreSetMap;
	}
	
	public void setDistDirectScoreSetMap(Map<Integer, Double> tdistDirectScoreSetMap){
		this.distDirectScoreSetMap = tdistDirectScoreSetMap;
	}
	
	public Map<Integer, ArrayList<Integer[]>> getPathEIDMap(){
		return pathEIDMap;
	}
	
	public void setPathEIDMap(Map<Integer, ArrayList<Integer[]>> tpathEIDMap){
		this.pathEIDMap = tpathEIDMap;
	}
	
	public Map<Integer, Double> getEdgeAccuScoreMap(){
		return edgeAccuScoreMap;
	}
	
	public void setEdgeAccuScoreMap(Map<Integer, Double> tpathEIDMap){
		this.edgeAccuScoreMap = tpathEIDMap;
	}
	
	public double getTripDirection(){
		return tripDirection;
	}
	
	public void setTripDirection(double tripDirection){
		this.tripDirection = tripDirection;
	}
	
	public double getTripDistance(){
		return tripDistance;
	}
	
	public void setTripDistance(double tripDistance){
		this.tripDistance = tripDistance;
	}
	
}
