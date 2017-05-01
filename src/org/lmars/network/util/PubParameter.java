package org.lmars.network.util;

/**
 * 相关参数的设置
 * @author whu
 *
 */
public class PubParameter {
	/**
	 * 数据库参数设置
	 */
	public static final String MYSQLDBTABLENAMEJANUARY_2014_STRING = "january_2014";
	public static final String MYSQLDBTABLENAMEFEBRUARY_2014_STRING = "february_2014";
	public static final String MYSQLDBTABLENAMEMARCH_2014_STRING = "march_2014";
	public static final String MYSQLDBTABLENAMEAPRIAL_2014_STRING = "april_2014";
	public static final String MYSQLDBTABLENAMEMAY_2014_STRING = "may_2014";
	public static final String MYSQLDBTABLENAMESIXTOSEVEN_2014_STRING = "sixtoseven_2014_process";//mySQL数据库表名
	
	public static final String MYSQLDBTABLENAME2013_STRING = "handleddata";//mySQL数据库2013年数据表名
	public static final int taxiGPSSimulativeConst = 10;//GPS点模拟时进行纠正点的个数	
	//oracle数据库连接参数
	public static final String ORCLDBDRIVER_STRING = "oracle.jdbc.driver.OracleDriver";//定义数据库驱动
	public static final String ORCLDBURL_STRING = "jdbc:oracle:thin:@192.168.2.148:1521:orcl";//数据库连接
	public static final String ORCLDBUSER_STRING = "GPS";
	public static final String ORCLDBPASSWORD_STRING = "GPS";
	public static final String ORCLIMPORTDATABASENAME = "orcl";//导入的目标数据库的名称 
	public static final String ORCLDBTABLENAME_STRING = "GPSTEST";	
	//mySQL连接参数
	public static final String MYSQLDBDRIVER_STRING = "com.mysql.jdbc.Driver";//驱动
	public static final String MYSQLDBURL_STRING = "jdbc:mysql://192.168.2.148:3306/dbname";//数据库连接
	public static final String MYSQLDBUSER_STRING = "root";
	public static final String MYSQLDBPASSWORD_STRING = "123456"; 
	
	/**
	 * 地图匹配参数设置
	 */
	public static final double cellLength = 1000;//正方形网格大小
	public static final double bufferRadius = 50;//格网单元边缘缓冲区大小，其大小设为构建候选路径的距离参数
//	public static final double radius = 30;//选择候选路段的半径
	public static final double radius = 45;//选择候选路段的半径
	public static final double taxiSpeed = 2;//方向得分的速度，交叉口处速度很小
//	public static final double []directWeight = {1,0.6};//方向权重
	public static final double []directWeight = {0.1,0.9};//方向权重，在交叉口处方向权重大(程序中参数设置有点问题，需要认真排查)
	public static final double thegema = 20;//候选道路方差
	public static final double []threeLevelConnProbability ={1,0.9,0.8,0.4};//连通集合转换概率
	public static final double headingDifferDownThreshold = 90;//相邻两GPS点的方向差阈值下限，以判断出租车是否在路段中间掉头(位于90,270)
	public static final double headingDifferUpThreshold = 270;//相邻两GPS点方向差阈值上限
	public static final double wuhanL0 = 114;//中央子午线经度
	public static final double candRoadCoarseFiltratBuffer = 100;//进行候选道路集选取时，先用candRoadSetBuffer对候选道路进行粗过滤,以提高速度
	public static final int sampleThreshold = 90;//相邻GPS点采样差阈值,以秒为单位	90
	public static final int expandTime = 300;//出租车进行轨迹扩展的时间差,以秒为单位
	public static final int threadCount = 4;//线程数目
	public static final double zeroSpeedThreshold = 0.1;//零速度阈值(当速度小于该值时，认为速度为零)
	public static final double endpointBufferRadius = 30;//端点缓冲区半径,判断路段是否在端点处停车,此值大小为交叉口区域范围，若到路段端点距离小于此值，则认为停车
	public static final double locationErrorCircleBufferRadius = 5;//定位偏差圆误差
	public static final int linkSameDirectionConst = 1;//出租车行驶方向与路段同方向
	public static final int linkAntiDirectionConst = -1;//出租车行驶方向与路段方向相反
	public static final double continuousStaticLongitudeLatitudeThreshold = 0.0001;//连续静止点经纬度坐标差阈值
	public static final int continuousStaticTimeThreshold = 120;//连续静止时间阈值，超过此值则认为车辆抛锚或者其他异常停车
	public static final int vacant = 0;//非载客状态
	public static final int occupied = 262144;//载客状态(正确的载客状态)	
	//public static final int carrayPassenger2 = 262145;//载客状态(错误的载客状态)
	
	/**
	 * tripPatterns相关参数
	 */
	public static final double tripTimeThreshold = 120;//出行时间阈值
	public static final double tripDistanceThreshold = 500;//出行距离阈值
	

	/**
	 * 神经网络相关参数
	 */
	public static final int inputsNeurons = 3;//输入层
	public static final int hiddenNeurons = 10;//隐层
	public static final int outputsNeurons = 1;//输出层
	
	public static final int sameDirection = 1;
	public static final int antiDirection = -1;
	
	
	
	
}
