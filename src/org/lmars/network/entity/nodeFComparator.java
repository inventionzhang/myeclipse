package org.lmars.network.entity;

import java.util.Comparator;

import org.lmars.network.entity.Node;

public class nodeFComparator implements Comparator<Node>{
	@Override
	public int compare(Node o1, Node o2){
		if (o1.getF() < o2.getF()) {
			return -1;
		}
		else if (o1.getF() > o2.getF()) {
			return 1;
		}
		else {
			return 0;
		}
    }
}
