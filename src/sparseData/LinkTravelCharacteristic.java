package sparseData;

/**
 * ·��ͨ�������������������
 * @author whu
 *
 */
public class LinkTravelCharacteristic {

	private int linkID = -1;
	private int enterNodeID = -1;
	private int exitNodeID = -1;
	private int direction = 0;
	private String timeStr = " ";
	private double speedExpectation = -1;
	private double speedStandDeviation = -1;
	private int linkDegree = -1;//·�ζ�����Ϊ���˵����֮��
	private double linkLength = -1;
	private String weekdayStr = "none";
	
	public int getLinkID(){
		return linkID;
	}
	
	public void setLinkID(int linkID){
		this.linkID = linkID;
	}
	
	public int getEnterNodeID(){
		return enterNodeID;
	}
	
	public void setEnterNodeID(int enterNodeID){
		this.enterNodeID = enterNodeID;
	}
	
	public int getExitNodeID(){
		return exitNodeID;
	}
	
	public void setExitNodeID(int exitNodeID){
		this.exitNodeID = exitNodeID;
	}
	
	public int getDirection(){
		return direction;
	}
	
	public void setDirection(int direction){
		this.direction = direction;
	}
	
	public String getTimeStr(){
		return timeStr;
	}
	
	public void setTimeStr( String timeStr){
		this.timeStr = timeStr;
	}
	
	public double getSpeedExpectation(){
		return speedExpectation;
	}
	
	public void setSpeedExpectation( double speedExpectation){
		this.speedExpectation = speedExpectation;
	}
	
	public double getSpeedStandDeviation(){
		return speedStandDeviation;
	}
	
	public void setSpeedStandDeviation(double speedStandDeviation){
		this.speedStandDeviation = speedStandDeviation;
	}
	
	public int getLinkDegree(){
		return linkDegree;
	}
	
	public void setLinkDegree(int linkDegree){
		this.linkDegree = linkDegree;
	}
	
	public double getLinkLength(){
		return linkLength;
	}
	
	public void setLinkLength(double linkLength){
		this.linkLength = linkLength;
	}
	
	public String getWeekdayStr(){
		return weekdayStr;
	}
	
	public void setWeekdayStr(String weekdayStr){
		this.weekdayStr = weekdayStr;
	}
}
