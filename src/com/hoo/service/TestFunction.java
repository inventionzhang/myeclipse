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
//		createXml("d://createXml");//����xml�ļ�
//		parserStrtoDocument();//����xml���ݣ��������ļ�
//		getComplexInfofromDocument();//���õ�����xpathȡ��xml�Ľڵ㼰������ֵ		
//		readXmlByDom4j();//dom4j��ȡxml�ļ�
	}
	
	public static void obtainTaxiGPSDataAndCorr() {
		try {
			//RPC��ʽ����
			RPCServiceClient client = new RPCServiceClient();
			Options options = client.getOptions();
			//���õ���WebService��URL,ָ������WebService��URL
			String address = "http://192.168.2.176:8080/axis2/services/GPSCoordinateCorrectService";
			EndpointReference epf = new EndpointReference(address);
			options.setTo(epf);			  
			// ָ��WSDL�ļ��������ռ��Լ�Ҫ���õķ���..... 
			//�ڴ���QName����ʱ��QName��Ĺ��췽���ĵ�һ��������ʾWSDL�ļ��������ռ�����Ҳ����<wsdl:definitions>Ԫ�ص�targetNamespace����ֵ
			QName qname = new QName("http://mapMatchingGPS", "obtainTaxiGPSDataAndCorrection");
			Object[] requestParam = new Object[] {"MMC8000GPSANDASYN051113-18395-00000000","2013-01-01 01%", "2013-01-01 02%"}; 
	        // ָ����������ֵ���������͵�Class���� 
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
        	InputStream inputStream = null; //����webService��� 
            URLConnection urlConn = url.openConnection();//������   
            urlConn.setConnectTimeout(5000);     
            urlConn.connect(); 
            inputStream = urlConn.getInputStream();//�õ����ӵ�������           
            SAXReader reader = new SAXReader();//�õ�SAXReader���� 
            org.dom4j.Document document = reader.read(inputStream);//��������ת��һ��DOM4J�ĵ���
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
            System.out.println("����XML�ļ��ɹ�!");
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
	
	/**����һ������Document��ʽ���ַ���������һ��Document
	* ���ַ���ת��ΪDocument
	* @param str ������ַ���
	* @return ���ɵ�document
	* @throws DocumentException
	*/
	public static void  parserStrtoDocument() {
		try {
			org.dom4j.Document document = DocumentHelper.createDocument();
			//���������
			org.dom4j.Element root = document.addElement("root");
			//Ϊ��������һ��book�ڵ�
			org.dom4j.Element book1 = root.addElement("book");
			book1.addAttribute("type","science");
		    org.dom4j.Element name1 = book1.addElement("Name");
		    //��������nameΪ"Java"
		    name1.setText("Java");
		    book1.addElement("price").setText("100");
		    org.dom4j.Element book2 = root.addElement("book").addAttribute("type","science");
		    org.dom4j.Element name2 = book2.addElement("Name");
		    name2.setText("Oracle");
		    book2.addElement("price").setText("200");		  
		    //���xml
		    String xmlString = document.asXML();		    
		    System.out.println(xmlString);		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
	
	/**���õ�����xpathȡ��xml�Ľڵ㼰������ֵ
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
			  
			//��ȡ����Ϊ"society"����
			org.dom4j.Element society_book=(org.dom4j.Element)document.selectSingleNode("/root/book[@type='society']");
			System.out.println(society_book.asXML());			  
		    //��ȡ�۸�ڵ���б�
		    List price=document.selectNodes("//price");
		    for(int i=0;i<price.size();i++){
			    org.dom4j.Element elem_price=(org.dom4j.Element)price.get(i);
		        System.out.println(elem_price.getText());
		   }		  
		   //ѭ��������µ����нڵ㣬����ǰ�ڵ�Ϊbook��������Ȿ�����ϸ��Ϣ
		   System.out.println("-------------��Ŀ����------------");
		   System.out.println("����\t\t���\t\t�۸�");
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
		   //������������
		   org.dom4j.Element author=(org.dom4j.Element)document.selectSingleNode("//author");
		   System.out.println("---------"+author.element("name").getText()+"----------");
		   //��ȡ���ߵ�������Ŀ����
		   Iterator iterator_book=root.elementIterator("book");  
		   while(iterator_book.hasNext()){
			   org.dom4j.Element book=(org.dom4j.Element)iterator_book.next();
		       System.out.print(book.element("Name").getText()+"\t");
		   }
		  
		   //���Ե���
		   System.out.println("\n-------���Ե���--------");
		   String str1="<book type='science' name='Java' price='100'/>";
		   org.dom4j.Document document1=DocumentHelper.parseText(str1);
		   //��ʼ����
		   Iterator iterator_attribute=document1.getRootElement().attributeIterator();
		   while(iterator_attribute.hasNext()){
		       //��ȡ��ǰ����
		       Attribute attribute=(Attribute)iterator_attribute.next();
		       System.out.println(attribute.getName()+":"+attribute.getValue());
		   }
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**ͨ��dom4j��xml�ļ�
	 * /
	 */
	public static void  readXmlByDom4j(){
		SAXReader saxReader = new SAXReader();
		String ljfString = "d:/ljf.xml";
		try {
			InputStream ljfis = new FileInputStream(ljfString);  
            org.dom4j.Document document = saxReader.read(ljfis);  //��������ת��һ��DOM4J�ĵ��� 
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
