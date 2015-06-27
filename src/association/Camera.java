package association;

import java.io.Serializable;

/**
 * ����ͷ��Ϣ
 * */
public class Camera implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8804034327781569064L;
	private int ID = 0;
	private double longitude = 0;
	private double latitude = 0;
	private double fieldOfView = 0;//�ӳ��ǣ�����
	private double angleDirection = 0;//�Ƕȷ�λ������
	private double angleDist = 0;//
	
	public void setID(int ID){
	    this.ID = ID;
	}
	    
	public double getID(){
	    return ID;
	}
	
	public void setLongitude(double longitude){
	    this.longitude = longitude;
	}
	    
	public double getLongitude(){
	    return longitude;
	}
	
	public void setLatitude(double latitude){
	    this.latitude = latitude;
	}
	    
	public double getLatitude(){
	    return latitude;
	}
	
	public void setFieldOfView(double fieldOfView){
	    this.fieldOfView = fieldOfView;
	}
	    
	public double getFieldOfView(){
	    return fieldOfView;
	}
	
	public void setAngleDirection(double angleDirection){
	    this.angleDirection = angleDirection;
	}
	    
	public double getAngleDirection(){
	    return angleDirection;
	}
	
	public void setAngleDist(double angleDist){
	    this.angleDist = angleDist;
	}
	    
	public double getAngleDist(){
	    return angleDist;
	}
}
