package com.hoo.service;

import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

/**
 * <b>function:</b>HelloWorldService 客户端调用代码
 * @author hoojo
 * @createDate 2011-1-7 下午03:55:05
 * @file HelloWorldClient.java
 * @package com.hoo.service
 * @project Axis2WebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */

public class HelloWorldClient {

	public static void main(String[] args) throws AxisFault {
		callServiceByJava();//通过java代码调用服务
//		callServiceByAxis2();//通过Axis2的辅助工具调用服务
	}
	
	public static void callServiceByJava() throws AxisFault{
		try {
			//RPC方式调用
			RPCServiceClient client = new RPCServiceClient();
			Options options = client.getOptions();
			//设置调用WebService的URL,指定调用WebService的URL
			String address = "http://localhost:8080/axis2/services/HelloWorldService";
			EndpointReference epf = new EndpointReference(address);
			options.setTo(epf);		
			/**
			 * 设置将调用的方法，http://ws.apache.org/axis2是方法
			 * 默认（没有package）命名空间，如果有包名
			 * 就是http://service.hoo.com 包名倒过来即可
			 * sayHello就是方法名称了
			 */		  
			// 指定WSDL文件的命名空间以及要调用的方法..... 
			//在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，也就是<wsdl:definitions>元素的targetNamespace属性值
			QName qname = new QName("http://service.hoo.com", "sayHello");
			Object[] requestParam = new Object[] {"jack"}; 
	        // 指定方法返回值的数据类型的Class对象 
	        Class[] responseParam = new Class[] {String.class}; 
			Object[] result = client.invokeBlocking(qname, requestParam, responseParam);
			System.out.println(result[0]);
			
			qname = new QName("http://service.hoo.com", "getAge");
			result = client.invokeBlocking(qname, new Object[] { new Integer(22) }, new Class[] { int.class });
			System.out.println(result[0]);
			
			qname = new QName("http://service.hoo.com", "getTwoArrayStrings");
			result = client.invokeBlocking(qname, requestParam, new Class[]{ String[][].class });
	        String[][] user = (String[][]) result[0];
			System.out.println("User: " + user);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}				
	}

	public static void callServiceByAxis2() throws AxisFault{
		String target = "http://localhost:8080/axis2/services/HelloWorldService";
		
	}
}
