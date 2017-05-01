package org.lmars.network.util;

public class ConvertZB {
	private static double pi = 3.14159265358979324;
    private static double a = 6378245.0;
    private static double ee = 0.00669342162296594323;
    private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    
    public static  Boolean outofChina(double lat, double lon)
    {
        if (lon < 72.004 || lon > 137.8374)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }
    
    private static double transformLat(double x, double y)
    {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    
    private static double transformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;  
    }
    
    public static double[]  WGS2Mars(double wgLat, double wgLon)
    {
    	double[] result= new double[2];
    	double mgLat=0;
    	double mgLon=0;
        if (outofChina(wgLat, wgLon))
        {
            mgLat = wgLat;
            mgLon = wgLon;
            return result;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        mgLat = wgLat + dLat;
        mgLon = wgLon + dLon;
       
        result[0]=mgLat;
        result[1]=mgLon;
        return result;
    }
    
    public static String converZB(double gcj_Lat, double gcj_Lon){
		double[] result= new double[2];
    	double wgs_Lon;
    	double wgs_Lat;
        double inter_Lon;
        double inter_Lat;
        double[] inters=WGS2Mars(gcj_Lat,gcj_Lon);
        inter_Lat=inters[0];
        inter_Lon=inters[1];
        wgs_Lon = gcj_Lon*2 - inter_Lon;
        wgs_Lat = gcj_Lat*2 - inter_Lat;
        result[0]=wgs_Lat;
        result[1]=wgs_Lon;
        return result[0]+","+result[1];
	}
}
