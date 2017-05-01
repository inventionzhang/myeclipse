package org.lmars.network.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;



public class PropertiesUtilJAR {
	
	/**
	 * 读取tomcat服务中配置文件的解决方法，与读取myeclipse的方式不同:
	 * 当其他程序调用YMail.jar包的 ReadCfg.class 类时，此时需要ReadCfg.class去读取 mailcfg.yfs文件。
	 * 此时按照文件操作方式不能够达到我们的目的，正确的解决方法如下
	 * 获取properties中的配置属性
	 * @param sKey
	 * @return
	 */
	public static String getProperties(String sKey) {
		Properties properties = new Properties();
		try {		
			InputStream insReader = PropertiesUtil.class.getClassLoader().getResourceAsStream("protege.properties");
			properties.load(insReader);			
		} catch (IOException e) {
			e.printStackTrace();
		}
		String s = properties.getProperty(sKey);
		return s;
	}
	
}
