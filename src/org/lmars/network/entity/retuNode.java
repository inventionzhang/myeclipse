package org.lmars.network.entity;

import java.io.Serializable;

public class retuNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -339915447131140415L;
	public double L=0;
	public double B=0;
	public int ID=0;
	public void setL(double LL){
    	this.L=LL;
    }		
	public double getL()
	{
		return L;
	}
	
	public void setB(double BB){
    	this.B=BB;
    }
	public double getB(){
		return B;
    }
	
	public void setID(int id){
    	this.ID=id;
    }
	public double getID(){
		return ID;
    }
}
