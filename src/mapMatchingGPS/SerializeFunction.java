package mapMatchingGPS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import association.Camera;
import association.StorageCameraData;

import com.esri.arcgis.system.Set;

import entity.PropertiesUtilJAR;

/************************************************
 * ���л�����
 * *********************************************/
public class SerializeFunction {

	public ArrayList<String> taxiIDArrayList = new ArrayList<String>();//���⳵ID��Ϣ
	Map<String, ArrayList<TaxiGPS>> allTaxiInfosMap = new HashMap<String, ArrayList<TaxiGPS>>();//�Գ��⳵idΪ���������г��⳵��Ϣ
	public Map<Integer, ArrayList<ReturnLinkTravelTime>> allLinkTravelTimeMap = new HashMap<Integer, ArrayList<ReturnLinkTravelTime>>();//��·��IDΪ�����洢����·�ε�ͳ��ͨ��ʱ��
	private static SerializeFunction serializeFunctionInstance = null;
	public static SerializeFunction instance(){	
		try {
			if (serializeFunctionInstance == null) {
				serializeFunctionInstance = new SerializeFunction();				
				String metadataFileName = PropertiesUtilJAR.getProperties("metadataFileName");
				if (serializeFunctionInstance.taxiIDArrayList.size() == 0) {
					serializeFunctionInstance.readSeriseTaxiMetadata(metadataFileName);
				}			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return serializeFunctionInstance;
	}
	
	public static SerializeFunction instance(String IDStr, String fileName){	
		try {
			if (IDStr.equals("taxiMetadata")) {
				if (serializeFunctionInstance == null) {
					serializeFunctionInstance = new SerializeFunction();				
					String metadataFileName = PropertiesUtilJAR.getProperties("metadataFileName");
					if (serializeFunctionInstance.taxiIDArrayList.size() == 0) {
						serializeFunctionInstance.readSeriseTaxiMetadata(metadataFileName);
					}			
				}
			}
			else if (IDStr.equals("linkTravelTime")) {
				if (serializeFunctionInstance == null) {
					serializeFunctionInstance = new SerializeFunction();				
					if (serializeFunctionInstance.allLinkTravelTimeMap.size() == 0) {
						serializeFunctionInstance.readSeriseLinkTravelTime(fileName);
					}		
				}				
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		return serializeFunctionInstance;
	}
	
	/*�������л�TaxiGPS����*/
	public boolean saveSeriseTaxiInfos(String filename, Map<String, ArrayList<TaxiGPS>> taxiInfosMap){
		try {
			File f = new File(filename);
			if(f.exists()){
				System.out.print(filename + "�Ѵ���!" + '\n');
//				f.delete();
				return false;
			}else {
				StorageTaxiInfos storageTaxiInfos = new StorageTaxiInfos();
				storageTaxiInfos.taxiInfosMap = taxiInfosMap;
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
				out.writeObject(storageTaxiInfos);
	    	    out.close();
				return true;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	public boolean readSeriseTaxiInfos(String filename){
		try {
			ClassLoader loader = LinkTravelTimeStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        StorageTaxiInfos storageTaxiInfos = new StorageTaxiInfos();
    		File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
				return false;
			}
			System.out.print(filename + "��ʼ������!" + '\n');
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
			storageTaxiInfos = (StorageTaxiInfos)oin.readObject();	   
	        oin.close();	        
	        Map<String, ArrayList<TaxiGPS>> taxiInfosMap = storageTaxiInfos.taxiInfosMap;
	        java.util.Set keySet = taxiInfosMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		String key = (String)mapEntry.getKey();
        		ArrayList<TaxiGPS> taxiGPSArrayList = taxiInfosMap.get(key);
        		serializeFunctionInstance.allTaxiInfosMap.put(key, taxiGPSArrayList);
        	}       
	        System.out.print(filename + "��ȡ����!" + '\n');
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	/*�洢���⳵Ԫ������Ϣ�����л����⳵ID��*/
	public boolean saveSeriseTaxiMetadata(String filename, ArrayList<String> taxiIDArrayList){
		try {
			File f = new File(filename);
			if(f.exists()){
				System.out.print(filename + "�Ѵ���!" + '\n');
				return false;
			}else {
				StorageTaxiMetadata storageTaxiMetadata = new StorageTaxiMetadata();
				storageTaxiMetadata.taxiIDArrayList = taxiIDArrayList;
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
				out.writeObject(storageTaxiMetadata);
	    	    out.close();
				return true;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	/*��ȡ���⳵Ԫ������Ϣ�������л����⳵ID��*/
	public boolean readSeriseTaxiMetadata(String filename){
		try {
			ClassLoader loader = LinkTravelTimeStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        StorageTaxiMetadata storageTaxiMetadata = new StorageTaxiMetadata();
			File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
				return false;
			}else {
				System.out.print(filename + "��ʼ������!" + '\n');
				ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
				storageTaxiMetadata = (StorageTaxiMetadata)oin.readObject();	   
		        oin.close();
		        serializeFunctionInstance.taxiIDArrayList = storageTaxiMetadata.taxiIDArrayList;
		        System.out.print(filename + "��ȡ����!" + '\n');	
				return true;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	/*��ȡ���⳵Ԫ������Ϣ�������л����⳵ID��*/
	public ArrayList<String> readSeriseTaxiMetadataArraylist(String filename){
		ArrayList<String> taxiIDArrayList = new ArrayList<String>();
		try {
			ClassLoader loader = LinkTravelTimeStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        StorageTaxiMetadata storageTaxiMetadata = new StorageTaxiMetadata();
			File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
			}else {
				System.out.print(filename + "��ʼ������!" + '\n');
				ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
				storageTaxiMetadata = (StorageTaxiMetadata)oin.readObject();	   
		        oin.close();
		        taxiIDArrayList = storageTaxiMetadata.taxiIDArrayList;
		        System.out.print(filename + "��ȡ����!" + '\n');	
			}
			return taxiIDArrayList;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
    		return taxiIDArrayList;
		}
	}
	
	/*�������л�camera����*/
	public boolean saveSeriseCameraData(String filename, ArrayList<Camera> cameraInfosArrayList){
		try {
			File f = new File(filename);
			if(f.exists()){
				System.out.print(filename + "�Ѵ���!" + '\n');
				return false;
			}else {
				StorageCameraData storageCameraData = new StorageCameraData();
				storageCameraData.cameraInfosArrayList = cameraInfosArrayList;
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
				out.writeObject(storageCameraData);
	    	    out.close();
	    	    System.out.print("���л��ļ�д�����" + '\n');
				return true;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}		
	}
	
	/*��ȡ���л�����ͷ����*/
	public ArrayList<Camera> readSeriseCameraData(String filename){
		ArrayList<Camera> cameraInfosArrayList = new ArrayList<Camera>();
		try {			
			ClassLoader loader = StorageCameraData.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        StorageCameraData storageCameraData = new StorageCameraData();
			File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
			}else {
				System.out.print(filename + "��ʼ������!" + '\n');
				ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
				storageCameraData = (StorageCameraData)oin.readObject();	   
		        oin.close();
		        cameraInfosArrayList = storageCameraData.cameraInfosArrayList;
		        System.out.print(filename + "��ȡ����!" + '\n');	
			}	
			return cameraInfosArrayList;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
    		return cameraInfosArrayList;
		}
	}
		
	public boolean readSeriseLinkTravelTime(String filename){
		try {
			ClassLoader loader = LinkTravelTimeStorage.class.getClassLoader();
    		while(loader != null) {  
    		    System.out.println(loader);  
    		    loader = loader.getParent();//��ø��������������  
    		}  
    		System.out.println(loader);   		
	        System.out.println(Thread.currentThread().getContextClassLoader());
	        LinkTravelTimeStorage linkTravelTimeStorage = new LinkTravelTimeStorage();
    		File f = new File(filename);
			if(!f.exists()){
				System.out.print(filename + "������!" + '\n');
				return false;
			}
			System.out.print(filename + "��ʼ������!" + '\n');
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(filename));
			linkTravelTimeStorage = (LinkTravelTimeStorage)oin.readObject();	   
	        oin.close();     
	        Map<Integer, ArrayList<ReturnLinkTravelTime>> linkTravelTimeMap = linkTravelTimeStorage.allLinkTravelTimeMap;
	        java.util.Set keySet = linkTravelTimeMap.entrySet();
			Iterator iterator = (Iterator) keySet.iterator();
        	while (iterator.hasNext()) {
        		Map.Entry mapEntry = (Map.Entry) iterator.next();
        		Integer key = (Integer)mapEntry.getKey();
        		ArrayList<ReturnLinkTravelTime> linkTravelTimeArrayList = linkTravelTimeMap.get(key);
        		serializeFunctionInstance.allLinkTravelTimeMap.put(key, linkTravelTimeArrayList);
        	} 
	        System.out.print(filename + "��ȡ����!" + '\n');
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
    		System.out.print(e.getMessage());
			return false;
		}
	}
	
	
	
}
