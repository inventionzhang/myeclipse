package com.hoo.service;

import java.util.Random;

/**
 * <b>function:</b> WebService HelloWorld����ʾ��
 * @author hoojo
 * @createDate 2011-1-5 ����03:35:06
 * @file HelloWorldService.java
 * @package com.hoo.service
 * @project Axis2WebService
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class HelloWorldService {

	public String sayHello(String name) {
		return name + " say: hello [axis2]";
	}
	
	public int getAge(int i) {
		return i + new Random().nextInt(100);
	}
	
	public String[][]getTwoArrayStrings(){
		String[][] tempStrings = new String[][] { { "�й�", "����" }, { "�ձ�", "����" }, { "�й�", "�Ϻ�", "�Ͼ�" } };
		return tempStrings;
	}

}
