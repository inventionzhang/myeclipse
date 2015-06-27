package com.hoo.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;

import mapMatchingGPS.MapMatchNode;
import mapMatchingGPS.ReturnMatchNode;
import mapMatchingGPS.CorrectedNode;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.xpath.operations.Or;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import entity.readRoadName;

public class TestFunction {

	public static void main(String[] args) throws AxisFault, ParserConfigurationException {
		obtainTaxiGPSDataAndCorr();
//		obtainTaxiGPSDataAndCorrXmlRPC();	
//		createXml("d://createXml");//生成xml文件
//		parserStrtoDocument();//生成xml内容，而不是文件
//		getComplexInfofromDocument();//利用迭代，xpath取得xml的节点及其属性值		
//		readXmlByDom4j();//dom4j读取xml文件
	}
	
	public static void obtainTaxiGPSDataAndCorr() {
		try {
			//RPC方式调用
			RPCServiceClient client = new RPCServiceClient();
			Options options = client.getOptions();
			//设置调用WebService的URL,指定调用WebService的URL
			String address = "http://192.168.2.176:8080/axis2/services/GPSCoordinateCorrectService";
			EndpointReference epf = new EndpointReference(address);
			options.setTo(epf);			  
			// 指定WSDL文件的命名空间以及要调用的方法..... 
			//在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，也就是<wsdl:definitions>元素的targetNamespace属性值
			QName qname = new QName("http://mapMatchingGPS", "obtainTaxiGPSDataAndCorrection");
			Object[] requestParam = new Object[] {"MMC8000GPSANDASYN051113-18395-00000000","2013-01-01 01%", "2013-01-01 02%"}; 
	        // 指定方法返回值的数据类型的Class对象 
	        Class[] responseParam = new Class[] {CorrectedNode[].class}; 
			Object[] result = client.invokeBlocking(qname, requestParam, responseParam);
			System.out.println(result[0]);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
	}
	
	public static void obtainTaxiGPSDataAndCorrXmlURL() {
		String urlString = "http://192.168.2.176:8080/axis2/services/GPSCoordinateCorrectService/" +
				"obtainTaxiGPSDataAndCorrXml?targetIDStr=MMC8000GPSANDASYN051113-18395-00000000&startTimeStr=2013-01-01&endTimeStr=2013-01-02";    
        try{         
        	URL url = new URL(urlString);            	
        	InputStream inputStream = null; //具体webService相关 
            URLConnection urlConn = url.openConnection();//打开连接   
            urlConn.setConnectTimeout(5000);     
            urlConn.connect(); 
            inputStream = urlConn.getInputStream();//得到连接的输入流           
            SAXReader reader = new SAXReader();//得到SAXReader对象 
            org.dom4j.Document document = reader.read(inputStream);//将输入流转成一个DOM4J文档类
            inputStream.close(); 
           
            if(document != null){  
                List<org.dom4j.Element> nodesElemList = document.selectNodes("//Node[@type='originalGPSNode']");
                for(int i = 0; i < nodesElemList.size(); i++){
    			    org.dom4j.Element childElement = (org.dom4j.Element)nodesElemList.get(i);
    			    List<org.dom4j.Element> childElements = childElement.elements();
    			    for (int j = 0; j < childElements.size(); j++) {
    			    	org.dom4j.Element tElement = childElements.get(j);
    			    	String elemText = tElement.getText();
    			    	System.out.print(elemText + ",");
    				}
    		        System.out.print('\n');
    		   } 
                 
            }  
        }catch(DOMException e){  
            e.printStackTrace();  
            System.out.print(e.getMessage());  
        }catch(IOException e){  
            e.printStackTrace();  
            System.out.print(e.getMessage());  
        }catch (DocumentException e){  
            e.printStackTrace();  
            System.out.print(e.getMessage());
        }  
	}
	
	public static void createXml(String fileName) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document =builder.newDocument();
        Element root = document.createElement("root"); 
        document.appendChild(root); 
        Element node = document.createElement("OriginalGPSNode");
        Element longitude = document.createElement("longitude"); 
        longitude.appendChild(document.createTextNode("114"));
        node.appendChild(longitude); 
        Element latitude = document.createElement("latitude"); 
        latitude.appendChild(document.createTextNode("30")); 
        node.appendChild(latitude);  
        root.appendChild(node); 
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
            System.out.println("生成XML文件成功!");
        } catch (TransformerConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (TransformerException e) {
            System.out.println(e.getMessage());
        }
    }
	
	/**根据一个符合Document格式的字符串来生成一个Document
	* 将字符串转化为Document
	* @param str 输入的字符串
	* @return 生成的document
	* @throws DocumentException
	*/
	public static void  parserStrtoDocument() {
		try {
			org.dom4j.Document document = DocumentHelper.createDocument();
			//创建根结点
			org.dom4j.Element root = document.addElement("root");
			//为根结点添加一个book节点
			org.dom4j.Element book1 = root.addElement("book");
			book1.addAttribute("type","science");
		    org.dom4j.Element name1 = book1.addElement("Name");
		    //并设置其name为"Java"
		    name1.setText("Java");
		    book1.addElement("price").setText("100");
		    org.dom4j.Element book2 = root.addElement("book").addAttribute("type","science");
		    org.dom4j.Element name2 = book2.addElement("Name");
		    name2.setText("Oracle");
		    book2.addElement("price").setText("200");		  
		    //输出xml
		    String xmlString = document.asXML();		    
		    System.out.println(xmlString);		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**利用迭代，xpath取得xml的节点及其属性值
	* @throws DocumentException
	*/
	public static void getComplexInfofromDocument(){
		try {
			String str="<root><book type='science'><Name>Java</Name><price>100</price></book>"
			    +"<book type='science'><Name>Oracle</Name><price>120</price></book>"
			    +"<book type='society'><Name>Society security</Name><price>130</price></book>"
			    +"<author><name>chb</name></author></root>";
			org.dom4j.Document document = DocumentHelper.parseText(str);
			System.out.println(document.asXML()); 
			  
			//提取类型为"society"的书
			org.dom4j.Element society_book=(org.dom4j.Element)document.selectSingleNode("/root/book[@type='society']");
			System.out.println(society_book.asXML());			  
		    //提取价格节点的列表
		    List price=document.selectNodes("//price");
		    for(int i=0;i<price.size();i++){
			    org.dom4j.Element elem_price=(org.dom4j.Element)price.get(i);
		        System.out.println(elem_price.getText());
		   }		  
		   //循环根结点下的所有节点，若当前节点为book，则输出这本书的详细信息
		   System.out.println("-------------书目详情------------");
		   System.out.println("书名\t\t类别\t\t价格");
		   org.dom4j.Element root=document.getRootElement();
		   Iterator iterator=root.elementIterator();
		   while(iterator.hasNext()){
			   org.dom4j.Element element=(org.dom4j.Element)iterator.next();
		       if(element.getName().equals("book")){
				   System.out.print(element.element("Name").getText()+"\t");
				   System.out.print(element.attributeValue("type")+"\t\t");
				   System.out.print(element.element("price").getText()+"\n");
		       }
		   }
		   //查找作者姓名
		   org.dom4j.Element author=(org.dom4j.Element)document.selectSingleNode("//author");
		   System.out.println("---------"+author.element("name").getText()+"----------");
		   //提取作者的所有书目名称
		   Iterator iterator_book=root.elementIterator("book");  
		   while(iterator_book.hasNext()){
			   org.dom4j.Element book=(org.dom4j.Element)iterator_book.next();
		       System.out.print(book.element("Name").getText()+"\t");
		   }
		  
		   //属性迭代
		   System.out.println("\n-------属性迭代--------");
		   String str1="<book type='science' name='Java' price='100'/>";
		   org.dom4j.Document document1=DocumentHelper.parseText(str1);
		   //开始迭代
		   Iterator iterator_attribute=document1.getRootElement().attributeIterator();
		   while(iterator_attribute.hasNext()){
		       //提取当前属性
		       Attribute attribute=(Attribute)iterator_attribute.next();
		       System.out.println(attribute.getName()+":"+attribute.getValue());
		   }
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**通过dom4j读xml文件
	 * /
	 */
	public static void  readXmlByDom4j(){
		SAXReader saxReader = new SAXReader();
		String ljfString = "d:/ljf.xml";
		try {
			InputStream ljfis = new FileInputStream(ljfString);  
            org.dom4j.Document document = saxReader.read(ljfis);  //将输入流转成一个DOM4J文档类 
            List<org.dom4j.Element> nodesElemList = document.selectNodes("//Node[@type='originalGPSNode']");
            for(int i = 0; i < nodesElemList.size(); i++){
			    org.dom4j.Element childElement = (org.dom4j.Element)nodesElemList.get(i);
			    List<org.dom4j.Element> childElements = childElement.elements();
			    for (int j = 0; j < childElements.size(); j++) {
			    	org.dom4j.Element tElement = childElements.get(j);
			    	String elemText = tElement.getText();
			    	System.out.print(elemText + ",");
				}
		        System.out.print('\n');
		   }           
        ljfis.close();           
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();  
            System.out.print(e.getMessage());
		}
	}
	
	
	
}
