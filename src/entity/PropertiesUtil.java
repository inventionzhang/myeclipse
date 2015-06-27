package entity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


public class PropertiesUtil {
	/**
	 * 获取properties中的配置属性
	 * @param sKey
	 * @return
	 */
	public static String getProperties(String sKey) {
		String url = Thread.currentThread().getContextClassLoader().getResource("").toString();
		url = url.substring(url.indexOf("/") + 1);
		url = url.replace("%20", " ");
		Properties properties = new Properties();
		try {
			InputStreamReader insReader = new InputStreamReader(new FileInputStream(url+"protege.properties"), "UTF-8");
			properties.load(insReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(sKey);
		String s = properties.getProperty(sKey);
		//System.out.println(s);
		return s;
	}
}
