package org.lmars.network.entity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPointCollection;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IRing;


public class surface implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3222439054382016515L;
	public int polygonID=0;//√ÊID
	public List<Double> xx = new ArrayList<Double>();
	public List<Double> yy = new ArrayList<Double>();
	
	public boolean pIn(double x, double y){
		return pointInPolygon(xx.toArray(new Double[0]),yy.toArray(new Double[0]),x,y);
	}
	private boolean pointInPolygon(Double polyX[], Double polyY[], double x, double y) {
		int polySides = polyX.length;	
		int i,j = polySides-1 ;
		boolean oddNodes= false;
		  for (i=0;i<polySides; i++) {
		    if((polyY[i]< y && polyY[j]>=y
		    ||   polyY[j]<y && polyY[i]>=y)
		    && (polyX[i]<=x || polyX[j]<=x)) {
		      oddNodes^=(polyX[i]+(y-polyY[i])/(polyY[j]-polyY[i])*(polyX[j]-polyX[i])<x);
		   }
		    j=i;
		 }
		  
		  if(oddNodes)
			  return true;
		  
		  for (i=0;i<polySides; i++) {
			  double d = Math.abs(x - polyX[i]) + Math.abs(y-polyY[i]);
			  if(d < 0.000001)
				  return true;
		  }
		  
		 return oddNodes; 
	}
	
	public void setPolygonID(int pid){
	    this.polygonID=pid;
	}
	
	public int getPolygonID(){
	    return polygonID;
	}
	
	public void setPoints(IPointCollection points){
		try{
			for(int i=0;i<points.getPointCount();i++){
				IPoint point =points.getPoint(i);
				xx.add(point.getX());
				yy.add(point.getY());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
