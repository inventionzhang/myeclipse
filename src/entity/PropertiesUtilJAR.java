package entity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


public class PropertiesUtilJAR {
	
	/**
	 * ��ȡJAR���������ļ��Ľ������:
	 * �������������YMail.jar���� ReadCfg.class ��ʱ����ʱ��ҪReadCfg.classȥ��ȡ mailcfg.yfs �ļ���
	 * ��ʱ�����ļ�������ʽ���ܹ��ﵽ���ǵ�Ŀ�ģ���ȷ�Ľ����������
	 * ��ȡproperties�е���������
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
