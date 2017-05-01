package org.lmars.network.mapMatchingGPS;

import java.io.Serializable;

public class ReturnMatchNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3382937632886501030L;
	public double longitude;
	public double latitude;
	
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

}
