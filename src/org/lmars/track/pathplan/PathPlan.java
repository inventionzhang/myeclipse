package org.lmars.track.pathplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.lmars.track.match.MapNode;
import org.lmars.track.match.ReturnMatchNode;
import org.lmars.track.match.*;

/*
 * @author faming date : 2017年6月19日 上午11:11:15
 */
public class PathPlan {
	
	/**
	 * 根据寻路起点、终点获得两点间的最短路径
	 * @param sNode 起点
	 * @param eNode 终点
	 * @return
	 * 2017年6月19日
	 */
	public boolean shortestPath(MapNode sNode, MapNode eNode){
		Stack<MapNode> stack = new Stack<MapNode>();
		ArrayList<MapNode> openList = new ArrayList<MapNode>();//开启列表
		int iteraCount = 0;
		iteraCount = 0;
		iteraCount = 0;
		ArrayList<ReturnMatchNode> tempReturnMapMatchPathArrayList = new ArrayList<ReturnMatchNode>();
		ArrayList<Integer[]> tempEidArrayList = new ArrayList<Integer[]>();
		new RoadNetwork().obtainShortestPath(stack,openList,null,sNode, sNode, eNode,
				tempReturnMapMatchPathArrayList, tempEidArrayList, iteraCount);
		return true;
	}

	
}
