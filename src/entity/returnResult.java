package entity;

import java.util.ArrayList;
/*����·�������������
 *����·��
 *����Ȧ
 *·������*/
public class returnResult {
	public ArrayList<ArrayList<retuNode>>ljfxAllPathArrayList=new ArrayList<ArrayList<retuNode>>();
	public ArrayList<retuNode> ljCircleJuncArrayList=new ArrayList<retuNode>();
	public ArrayList<ArrayList<String>> allPathDescriptArrayList=new ArrayList<ArrayList<String>>();
	
	public void setLjfxAllPathArrayList(ArrayList<ArrayList<retuNode>>ljfxResult){
		this.ljfxAllPathArrayList=ljfxResult;	
	}
	public ArrayList<ArrayList<retuNode>> getLjfxAllPathArrayList(){
		return ljfxAllPathArrayList;	
	}
	
	public void setLjCircleJuncArrayList(ArrayList<retuNode>ljCircleResult){
		this.ljCircleJuncArrayList=ljCircleResult;	
	}
	public ArrayList<retuNode> getLjCircleJuncArrayList(){
		return ljCircleJuncArrayList;	
	}
	
	public void setAllPathDescriptArrayList(ArrayList<ArrayList<String>> ljAnalyzeResult){
		this.allPathDescriptArrayList=ljAnalyzeResult;	
	}
	public ArrayList<ArrayList<String>> getAllPathDescriptArrayList(){
		return allPathDescriptArrayList;	
	}

}
