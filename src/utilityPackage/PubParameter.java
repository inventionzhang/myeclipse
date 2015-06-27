package utilityPackage;

/**
 * ��ز���������
 * @author whu
 *
 */
public class PubParameter {
	/**
	 * ���ݿ��������
	 */
	public static final String MYSQLDBTABLENAMEJANUARY_2014_STRING = "january_2014";
	public static final String MYSQLDBTABLENAMEFEBRUARY_2014_STRING = "february_2014";
	public static final String MYSQLDBTABLENAMEMARCH_2014_STRING = "march_2014";
	public static final String MYSQLDBTABLENAMEAPRIAL_2014_STRING = "april_2014";
	public static final String MYSQLDBTABLENAMEMAY_2014_STRING = "may_2014";
	public static final String MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING = "sixtoseven_2014_process";//mySQL���ݿ����
	
	public static final String MYSQLDBTABLENAME2013_STRING = "handleddata";//mySQL���ݿ�2013�����ݱ���
	public static final int taxiGPSSimulativeConst = 10;//GPS��ģ��ʱ���о�����ĸ���	
	//oracle���ݿ����Ӳ���
	public static final String ORCLDBDRIVER_STRING = "oracle.jdbc.driver.OracleDriver";//�������ݿ�����
	public static final String ORCLDBURL_STRING = "jdbc:oracle:thin:@192.168.2.148:1521:orcl";//���ݿ�����
	public static final String ORCLDBUSER_STRING = "GPS";
	public static final String ORCLDBPASSWORD_STRING = "GPS";
	public static final String ORCLIMPORTDATABASENAME = "orcl";//�����Ŀ�����ݿ������ 
	public static final String ORCLDBTABLENAME_STRING = "GPSTEST";	
	//mySQL���Ӳ���
	public static final String MYSQLDBDRIVER_STRING = "com.mysql.jdbc.Driver";//����
	public static final String MYSQLDBURL_STRING = "jdbc:mysql://192.168.2.148:3306/dbname";//���ݿ�����
	public static final String MYSQLDBUSER_STRING = "root";
	public static final String MYSQLDBPASSWORD_STRING = "123456"; 
	
	/**
	 * ��ͼƥ���������
	 */
	public static final double cellLength = 1000;//�����������С
	public static final double bufferRadius = 50;//������Ԫ��Ե��������С�����С��Ϊ������ѡ·���ľ������
//	public static final double radius = 30;//ѡ���ѡ·�εİ뾶
	public static final double radius = 45;//ѡ���ѡ·�εİ뾶
	public static final double taxiSpeed = 2;//����÷ֵ��ٶȣ�����ڴ��ٶȺ�С
//	public static final double []directWeight = {1,0.6};//����Ȩ��
	public static final double []directWeight = {0.1,0.9};//����Ȩ�أ��ڽ���ڴ�����Ȩ�ش�(�����в��������е����⣬��Ҫ�����Ų�)
	public static final double thegema = 20;//��ѡ��·����
	public static final double []threeLevelConnProbability ={1,0.9,0.8,0.4};//��ͨ����ת������
	public static final double headingDifferDownThreshold = 90;//������GPS��ķ������ֵ���ޣ����жϳ��⳵�Ƿ���·���м��ͷ(λ��90,270)
	public static final double headingDifferUpThreshold = 270;//������GPS�㷽�����ֵ����
	public static final double wuhanL0 = 114;//���������߾���
	public static final double candRoadCoarseFiltratBuffer = 100;//���к�ѡ��·��ѡȡʱ������candRoadSetBuffer�Ժ�ѡ��·���дֹ���,������ٶ�
	public static final int sampleThreshold = 90;//����GPS���������ֵ,����Ϊ��λ	90
	public static final int expandTime = 300;//���⳵���й켣��չ��ʱ���,����Ϊ��λ
	public static final int threadCount = 4;//�߳���Ŀ
	public static final double zeroSpeedThreshold = 0.1;//���ٶ���ֵ(���ٶ�С�ڸ�ֵʱ����Ϊ�ٶ�Ϊ��)
	public static final double endpointBufferRadius = 30;//�˵㻺�����뾶,�ж�·���Ƿ��ڶ˵㴦ͣ��,��ֵ��СΪ���������Χ������·�ζ˵����С�ڴ�ֵ������Ϊͣ��
	public static final double locationErrorCircleBufferRadius = 5;//��λƫ��Բ���
	public static final int linkSameDirectionConst = 1;//���⳵��ʻ������·��ͬ����
	public static final int linkAntiDirectionConst = -1;//���⳵��ʻ������·�η����෴
	public static final double continuousStaticLongitudeLatitudeThreshold = 0.0001;//������ֹ�㾭γ���������ֵ
	public static final int continuousStaticTimeThreshold = 120;//������ֹʱ����ֵ��������ֵ����Ϊ������ê���������쳣ͣ��
	public static final int vacant = 0;//���ؿ�״̬
	public static final int occupied = 262144;//�ؿ�״̬(��ȷ���ؿ�״̬)	
	//public static final int carrayPassenger2 = 262145;//�ؿ�״̬(������ؿ�״̬)
	
	/**
	 * tripPatterns��ز���
	 */
	public static final double tripTimeThreshold = 120;//����ʱ����ֵ
	public static final double tripDistanceThreshold = 500;//���о�����ֵ
	

	/**
	 * ��������ز���
	 */
	public static final int inputsNeurons = 3;//�����
	public static final int hiddenNeurons = 10;//����
	public static final int outputsNeurons = 1;//�����
	
	public static final int sameDirection = 1;
	public static final int antiDirection = -1;
	
	
	
	
}
