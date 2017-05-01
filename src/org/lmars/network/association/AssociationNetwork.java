package org.lmars.network.association;

import java.util.ArrayList;

import org.lmars.network.mapMatchingGPS.MapMatchNode;
import org.lmars.network.mapMatchingGPS.SerializeFunction;
import org.lmars.network.util.PubClass;
import org.lmars.network.util.PubParameter;


public class AssociationNetwork {
	/**
	 * 摄像头关联网络：摄像头拍摄角度以及范围
	 * FOV：视场角，摄像头拍摄范围角度，度,0<FOV<=180
	 * angleDirection：水平线逆时针旋转到摄像头拍摄边缘第一条线的角度，度,0<angleDirection<=360
	 */
	public ArrayList<ArrayList<double[]>> cameraAssociate(){
		String cameraFilename = "C:\\cameraInfos.bin";//序列化文件
		//逆时针存储三角形三个顶点坐标，第一个点位为摄像头点位
		ArrayList<ArrayList<double[]>> associateCoorArrayList = new ArrayList<ArrayList<double[]>>();
		ArrayList<Camera> cameraInfosArrayList = new ArrayList<Camera>();
		SerializeFunction serializeFunction = new SerializeFunction();	
		cameraInfosArrayList = serializeFunction.readSeriseCameraData(cameraFilename);
		for (int i = 0; i < cameraInfosArrayList.size(); i++) {
			Camera camera = new Camera();
			camera = cameraInfosArrayList.get(i);
			double longA = camera.getLongitude(), lattiA = camera.getLatitude();
			double FOV = camera.getFieldOfView(), angleDirection = camera.getAngleDirection();
			double thetaArc1 = FOV * Math.PI/180, thetaArc2 = angleDirection * Math.PI/180;
			double dist = camera.getAngleDist();
			double distAB = dist/Math.cos(thetaArc1/2);
			double distAC = distAB;
			MapMatchNode tNode = new MapMatchNode();
			tNode.setX(longA);
			tNode.setY(lattiA);
			double []xy = new double[2];
			PubClass.coordinateTransToPlaneCoordinate(tNode, PubParameter.wuhanL0, xy);
			double xxA = xy[0], yyA = xy[1];
			//求B、C点坐标
			ArrayList<double[]> coorArrayList = new ArrayList<double[]>();
			double xxB = xxA + distAB * Math.sin(thetaArc2);
			double yyB = yyA + distAB * Math.cos(thetaArc2);
			double xxC = xxA + distAC * Math.sin(thetaArc1 + thetaArc2);
			double yyC = yyA + distAC * Math.cos(thetaArc1 + thetaArc2);
			double []LBA = new double[2];
			LBA[0] = longA;
			LBA[1] = lattiA;
			double []LBB = new double[2];
			double []LBC = new double[2];
			PubClass.coordinateTransToLB(PubParameter.wuhanL0, xxB, yyB, LBB);
			PubClass.coordinateTransToLB(PubParameter.wuhanL0, xxC, yyC, LBC);
			coorArrayList.add(LBA);
			coorArrayList.add(LBB);
			coorArrayList.add(LBC);
			associateCoorArrayList.add(coorArrayList);
		}
		return associateCoorArrayList;
	}
	
	
	
	
	
	
	
	
	
}
