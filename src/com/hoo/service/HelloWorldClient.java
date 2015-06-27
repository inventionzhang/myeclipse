package com.hoo.service;

import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

/**
 * <b>function:</b>HelloWorldService �ͻ��˵��ô���
 * @author hoojo
 * @createDate 2011-1-7 ����03:55:05
 * @file HelloWorldClient.java
 * @package com.hoo.service
 * @project Axis2WebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */

public class HelloWorldClient {

	public static void main(String[] args) throws AxisFault {
		callServiceByJava();//ͨ��java������÷���
//		callServiceByAxis2();//ͨ��Axis2�ĸ������ߵ��÷���
	}
	
	public static void callServiceByJava() throws AxisFault{
		try {
			//RPC��ʽ����
			RPCServiceClient client = new RPCServiceClient();
			Options options = client.getOptions();
			//���õ���WebService��URL,ָ������WebService��URL
			String address = "http://localhost:8080/axis2/services/HelloWorldService";
			EndpointReference epf = new EndpointReference(address);
			options.setTo(epf);		
			/**
			 * ���ý����õķ�����http://ws.apache.org/axis2�Ƿ���
			 * Ĭ�ϣ�û��package�������ռ䣬����а���
			 * ����http://service.hoo.com ��������������
			 * sayHello���Ƿ���������
			 */		  
			// ָ��WSDL�ļ��������ռ��Լ�Ҫ���õķ���..... 
			//�ڴ���QName����ʱ��QName��Ĺ��췽���ĵ�һ��������ʾWSDL�ļ��������ռ�����Ҳ����<wsdl:definitions>Ԫ�ص�targetNamespace����ֵ
			QName qname = new QName("http://service.hoo.com", "sayHello");
			Object[] requestParam = new Object[] {"jack"}; 
	        // ָ����������ֵ���������͵�Class���� 
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
