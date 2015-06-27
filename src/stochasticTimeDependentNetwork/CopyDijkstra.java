package stochasticTimeDependentNetwork;

import java.util.ArrayList;

public class CopyDijkstra {
	static ArrayList<CopySide> graph = null;
    static ArrayList<Integer> redAgg = null;
    static ArrayList<Integer> blueAgg = null;
    static CopySide[] parents = null;

    public static void main(String[] args) {
	    long startTime=System.currentTimeMillis();
	    System.out.println(startTime);
	    long t;
        // ��ʼ�����㼯
        int[] nodes = {0, 1, 3, 2, 4, 5,6};       
        // ��ʼ������Ȩ��ͼ
        graph = new ArrayList<CopySide>();
        graph.add(new CopySide(0, 1, 10));
        graph.add(new CopySide(0, 3, 30));
        graph.add(new CopySide(0, 4, 100));
        graph.add(new CopySide(1, 2, 50));
        graph.add(new CopySide(2, 4, 10));
        graph.add(new CopySide(3, 2, 20));
        graph.add(new CopySide(3, 4, 60));
        graph.add(new CopySide(4, 5, 50));
        graph.add(new CopySide(3, 5, 60));
        graph.add(new CopySide(5, 6, 10));
        graph.add(new CopySide(3, 6, 80));
        //��ʼ����֪���·���Ķ��㼯������㼯��ֻ���붥��0
        redAgg = new ArrayList<Integer>();
        redAgg.add(nodes[0]);
        // ��ʼ��δ֪���·���Ķ��㼯,�����㼯
        blueAgg = new ArrayList<Integer>();
        for (int i = 1; i < nodes.length; i++)
            blueAgg.add(nodes[i]);
        // ��ʼ��ÿ�����������·���еĸ����,������֮���Ȩ��,Ȩ��-1��ʾ����ͨ
        parents = new CopySide[nodes.length];
        parents[0] = new CopySide(-1, nodes[0], 0);
        for (int i = 0; i < blueAgg.size(); i++) {
            int n = blueAgg.get(i);
            parents[i + 1] = new CopySide(nodes[0], n, getWeight(nodes[0], n));
        }
        // �Ҵ����㼯���ҳ�Ȩ����С���Ǹ�����,���������뵽��㼯�� 
        while (blueAgg.size() > 0) {
            CopyMinShortPath msp = getMinSideNode();
            if(msp.getWeight()==-1)
                msp.outputPath(nodes[0]);
            else
                msp.outputPath();
            int node = msp.getLastNode();//Ȩ����С�Ķ���
            redAgg.add(node); 
            // �����Ϊ�������µĶ���,���������㼯�еĶ�������·����С,��Ҫ��Ҫ����
            setWeight(node);
        }
        long endTime=System.currentTimeMillis();
        System.out.println(endTime);
        t = endTime-startTime;
        System.out.println("����ʱ��Ϊ��"+t+"ms");
     }
     
     /** *//**
      * �õ�һ���ڵ�ĸ��ڵ�
      * 
      * @param parents
      * @param node
      * @return
      */
     public static int getParent(CopySide[] parents, int node) {
         if (parents != null) {
             for (CopySide nd : parents) {
                 if (nd.getNode() == node) {
                     return nd.getPreNode();
                 }
             }
         }
         return -1;
     }

     /** *//**
      * �����������㼯��ʣ��ڵ�����·������
      * 1.����ڵ�����·������
      * 2.�ڵ�����·��������ԭ�ȵ����·�����бȽϣ��Ӷ����ýڵ�����·������
      * @param preNode
      * @param map
      * @param blueAgg
      */
     public static void setWeight(int preNode) {
         if (graph != null && parents != null && blueAgg != null) {
             for (int node : blueAgg) {
                 CopyMinShortPath msp = getMinPath(node);
                 int w1 = msp.getWeight();//���·������
                 if (w1 == -1)//���������·��
                     continue;
                 for (CopySide n : parents) {
                     if (n.getNode() == node) {
                    	 //����֮�䲻����·���Լ�ԭ��·���������µ����·�����ȱȽ�
                         if (n.getWeight() == -1 || n.getWeight() > w1) {
                             n.setWeight(w1);//���·������
                             n.setPreNode(preNode);//�������ö���ĸ�����
                             break;
                         }
                     }
                 }
             }
         }
     }

     /** *//**
      * �õ�����ڵ�֮���Ȩ��
      * @param map
      * @param map
      * @param preNode
      * @param node
      * @return
      */
     public static int getWeight(int preNode, int node) {
         if (graph != null) {
             for (CopySide s : graph) {
                 if (s.getPreNode() == preNode && s.getNode() == node)
                     return s.getWeight();
             }
         }
         return -1;
     }

     /** *//**
      * �����㼯�����ҳ�·����С���Ǹ��ڵ�
      * 1.�������㼯�е�ÿһ���㣬�ҳ���ÿһ�����е���С·��
      * 2.�Ƚ����е��е���С·�����ҳ�·����С���Ǹ��ڵ�
      * @param map
      * @param blueAgg
      * @return
      */
     public static CopyMinShortPath getMinSideNode() {
         CopyMinShortPath minMsp = null;//�����㼯�����ҳ���С��·��
         if (blueAgg.size() > 0) {
             int index = 0;
             for (int j = 0; j < blueAgg.size(); j++) {
            	 int nodeID = blueAgg.get(j);
                 CopyMinShortPath msp = getMinPath(blueAgg.get(j));//����ǰ�ڵ���С��·��
                 if (minMsp == null || msp.getWeight()!=-1 &&  msp.getWeight() < minMsp.getWeight()) {
                     minMsp = msp;
                     index = j;
                 }
             }
             blueAgg.remove(index);
         }
         return minMsp;
     }

     /** *//**
      * �õ�ĳһ�ڵ�����·��(ʵ���Ͽ����ж���,����ֻ����һ��)
      * 1.���ݺ�㼯���еĽڵ���Ŀ��ڵ�֮���ϵ�õ����·��
      * 2.ÿһ��������еĵ���Ϊ���ڵ㣬Ŀ��ڵ���Ϊ��ǰ�ڵ㣬���ݸ��ڵ���Ŀ��ڵ�֮��Ĺ�ϵ�����ϱ�������ʼ�ڵ�
      * @param node
      * @return
      */
     public static CopyMinShortPath getMinPath(int node) {
         CopyMinShortPath msp = new CopyMinShortPath(node);
         if (parents != null && redAgg != null) {
             for (int i = 0; i < redAgg.size(); i++) {
                 CopyMinShortPath tempMsp = new CopyMinShortPath(node);
                 int parent = redAgg.get(i);//���ڵ�
                 int curNode = node;
                 while (parent > -1) {
                     int weight = getWeight(parent, curNode);//�õ�����ڵ�֮���Ȩ��
                     //���������ͨ
                     if (weight > -1) {
                         tempMsp.addNode(parent);
                         tempMsp.addWeight(weight);
                         curNode = parent;
                         parent = getParent(parents, parent);//�õ�һ���ڵ�ĸ��ڵ�
                     } else
                         break;
                 }
                 if (msp.getWeight() == -1 || tempMsp.getWeight()!=-1 && msp.getWeight() > tempMsp.getWeight())
                     msp = tempMsp;
             }
         }
         return msp;
     }
     
     
     
     
 }
