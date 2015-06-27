package association;

import java.util.ArrayList;

import mapMatchingGPS.MapMatchNode;
import mapMatchingGPS.SerializeFunction;
import utilityPackage.PubClass;
import utilityPackage.PubParameter;

public class AssociationNetwork {
	/**
	 * ����ͷ�������磺����ͷ����Ƕ��Լ���Χ
	 * FOV���ӳ��ǣ�����ͷ���㷶Χ�Ƕȣ���,0<FOV<=180
	 * angleDirection��ˮƽ����ʱ����ת������ͷ�����Ե��һ���ߵĽǶȣ���,0<angleDirection<=360
	 */
	public ArrayList<ArrayList<double[]>> cameraAssociate(){
		String cameraFilename = "C:\\cameraInfos.bin";//���л��ļ�
		//��ʱ��洢�����������������꣬��һ����λΪ����ͷ��λ
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
			//��B��C������
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
