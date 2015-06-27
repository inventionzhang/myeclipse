package org.artificialNeuralNetwork;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.jsp.tagext.TryCatchFinally;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.BufferedDataSet;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

import utilityPackage.PubClass;
import utilityPackage.PubParameter;

import entity.PropertiesUtilJAR;

public class TravelTimePredictMultiLayerPerceptron implements LearningEventListener{

	 /**
     *  Runs this sample
     */
    public static void main(String[] args) throws FileNotFoundException{
    	(new TravelTimePredictMultiLayerPerceptron()).neuralNetworkPredictAccordLinkID(8);
//    	(new TravelTimePredictMultiLayerPerceptron()).neuralNetworkPredictAllLink();
    	(new TravelTimePredictMultiLayerPerceptron()).predictTravelTimeThroughANN(8,"2014-06-01 00:00:00");//神经网络预测
    	
    	
//    	(new TravelTimePredictMultiLayerPerceptron()).testNeuralNet();//测试神经网络预测
    }
    
    /**
     * 所有路段进行神经网络
     * @throws FileNotFoundException
     * 
     */
    public void neuralNetworkPredictAllLink(){
    	try {
	    	String inputOriginalNormalizeFileName = DeleteBufferedDataSetSample.class.getResource("normalize.txt").getFile();
	        String processOutputFileName = "C:\\processNormalize.txt";//处理后归一化文件
	        ArrayList<NormalizeMetaData> metaDataArrayList = new ArrayList<NormalizeMetaData>();//存储元数据信息
	        readMetaDataDescriptionFromNormalize(inputOriginalNormalizeFileName, metaDataArrayList);
	        for (int i = 0; i < metaDataArrayList.size(); i++) {
				NormalizeMetaData normalizeMetaData = metaDataArrayList.get(i);
				ArrayList<String> infosArrayList = new ArrayList<String>();//写入处理归一化文件信息
				int linkID = normalizeMetaData.getLinkID();
				int linkSameDirection = normalizeMetaData.getLinkSameDirection();
	    		int linkAntiDirection = normalizeMetaData.getLinkAntiDirection();
	    		//路段通行数据读取、训练
	    		readOriginalNormalizeTxtFileAccordLinkIDAndDirection(linkID, linkSameDirection, inputOriginalNormalizeFileName, infosArrayList);
	    		File outputFile = new File(processOutputFileName);           
	    		if (outputFile.exists()) {
	    			outputFile.delete();
	    			writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
	    		}
	            else {
	            	writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
	    		}
	            neuralNetworkTrainning(processOutputFileName, linkID, linkSameDirection);//路段同方向信息神经网络训练
	            //路段异向通行数据读取、训练
	            infosArrayList = new ArrayList<String>();
	            readOriginalNormalizeTxtFileAccordLinkIDAndDirection(linkID, linkAntiDirection, inputOriginalNormalizeFileName, infosArrayList);
	            if (outputFile.exists()) {
	    			outputFile.delete();
	    			writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
	    		}
	            else {
	            	writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
	    		}
	            neuralNetworkTrainning(processOutputFileName, linkID, linkAntiDirection);//路段异方向信息神经网络训练
			}                    
//            // load saved neural network
//            NeuralNetwork loadedNeuralNet = NeuralNetwork.load(neuralNetName);
//            // test loaded neural network
//            System.out.println("Testing loaded perceptron");
//            String timeStr = "2014-10-15 09:52:00";
//            double[]inputVector = new double[3];
//            obtainNeuralNetInputDataSetRow(timeStr, inputVector);
//            DataSet testTrainingSet = new DataSet(3, 1);
//            double v1 = (double)(inputVector[0] - minVal)/(maxVal - minVal);
//            double v2 = (double)(inputVector[1] - minVal)/(maxVal - minVal);
//            double v3 = (double)(inputVector[2] - minVal)/(maxVal - minVal);
//            testTrainingSet.addRow(new DataSetRow(new double[]{v1,v2,v3}, new double[]{0}));        
//            testNeuralNetwork(loadedNeuralNet, testTrainingSet, maxVal, minVal);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}   
    }
    
    /**
     * 用神经网络对某路段通行时间预测
     * 1.神经网络训练
     * 2.通行时间预测
     * 
     */
    public void neuralNetworkPredictAccordLinkID(int linkID){
    	try {
	    	String inputOriginalNormalizeFileName = DeleteBufferedDataSetSample.class.getResource("normalize.txt").getFile();
	        String processOutputFileName = "C:\\processNormalize.txt";//处理后归一化文件
	        ArrayList<NormalizeMetaData> metaDataArrayList = new ArrayList<NormalizeMetaData>();//存储元数据信息
	        readMetaDataDescriptionFromNormalize(inputOriginalNormalizeFileName, metaDataArrayList);
	        for (int i = 0; i < metaDataArrayList.size(); i++) {
				NormalizeMetaData normalizeMetaData = metaDataArrayList.get(i);
				ArrayList<String> infosArrayList = new ArrayList<String>();//写入处理归一化文件信息
				int tempLinkID = normalizeMetaData.getLinkID();
				if (linkID == tempLinkID) {
					int linkSameDirection = normalizeMetaData.getLinkSameDirection();
		    		int linkAntiDirection = normalizeMetaData.getLinkAntiDirection();
		    		//路段通行数据读取、训练
		    		readOriginalNormalizeTxtFileAccordLinkIDAndDirection(linkID, linkSameDirection, inputOriginalNormalizeFileName, infosArrayList);
		    		File outputFile = new File(processOutputFileName);           
		    		if (outputFile.exists()) {
		    			outputFile.delete();
		    			writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
		    		}
		            else {
		            	writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
		    		}
		            neuralNetworkTrainning(processOutputFileName, linkID, linkSameDirection);//路段同方向信息神经网络训练
		            //路段异向通行数据读取、训练
		            infosArrayList = new ArrayList<String>();
		            readOriginalNormalizeTxtFileAccordLinkIDAndDirection(linkID, linkAntiDirection, inputOriginalNormalizeFileName, infosArrayList);
		            if (outputFile.exists()) {
		    			outputFile.delete();
		    			writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
		    		}
		            else {
		            	writeProcessNormalizeToTxtFile(processOutputFileName, infosArrayList);
		    		}
		            neuralNetworkTrainning(processOutputFileName, linkID, linkAntiDirection);//路段异方向信息神经网络训练
				}				
			}                    
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}   
    }
    
    /**
     * 对某路段进行训练
     * @param dataSetPathStr
     * @param linkID
     * @param direction
     */
    public void neuralNetworkTrainning(String dataSetPathStr, int linkID, int direction){
    	try {
    		 // create training set from file
            DataSet travelTimeDataSet = DataSet.createFromFile(dataSetPathStr, PubParameter.inputsNeurons, PubParameter.outputsNeurons, ",", false);      
            // print out normalized training set
            for (DataSetRow dataSetRow : travelTimeDataSet.getRows()) {
                System.out.print("Input: " + Arrays.toString(dataSetRow.getInput()));
                System.out.print("Output: " + Arrays.toString(dataSetRow.getDesiredOutput()) + '\n');            
            }
            // create MultiLayerPerceptron neural network
//            MultiLayerPerceptron neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH,
//            		PubParameter.inputsNeurons, PubParameter.hiddenNeurons, PubParameter.outputsNeurons);
            MultiLayerPerceptron neuralNet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
            		PubParameter.inputsNeurons, PubParameter.hiddenNeurons, PubParameter.outputsNeurons);
//            MultiLayerPerceptron multiLayerPerceptron = new MultiLayerPerceptron(arg0, arg1)
            // enable batch if using MomentumBackpropagation
            if(neuralNet.getLearningRule() instanceof MomentumBackpropagation)
            	((MomentumBackpropagation)neuralNet.getLearningRule()).setMomentum(0.7);//它的作用是测试它左边的对象是否是它右边的类的实例
            neuralNet.getLearningRule().setMaxError(0.01);
            neuralNet.getLearningRule().setLearningRate(0.2);
            neuralNet.getLearningRule().setMaxIterations(10000);
            System.out.print(neuralNet.getLearningRule());
            neuralNet.getLearningRule().addListener(this);     
            // train the network with training set
            System.out.println("Start training...");
            neuralNet.learn(travelTimeDataSet);
//            neuralNet.
            System.out.println("Done training...");
            String neuralNetName = "linkID" + linkID + "-" + direction + "-" + "MultiLayerPerceptron5min.nnet";
            neuralNet.save(neuralNetName);	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation bp = (BackPropagation)event.getSource();
        System.out.println(bp.getCurrentIteration() + ".iteration:totalNetworkError"+ bp.getTotalNetworkError());
    }
    /**
     * 用训练过的神经网络预测某个路段正反方向，某一天各时刻的行程时间
     * @param linkID 路段ID
     * @param timeStr 格式2013-01-01 00:00:00
     */
    public void predictTravelTimeThroughANN(int linkID, String timeStr){
    	try {
    		//获得输入向量
    		String travelTimePredictFolderName = PropertiesUtilJAR.getProperties("travelTimePredictFolderName");//行程时间预测文件夹
            String inputOriginalNormalizeFileName = DeleteBufferedDataSetSample.class.getResource("normalize.txt").getFile();
	        ArrayList<NormalizeMetaData> metaDataArrayList = new ArrayList<NormalizeMetaData>();//存储元数据信息
	        readMetaDataDescriptionFromNormalize(inputOriginalNormalizeFileName, metaDataArrayList);
	        for (int i = 0; i < metaDataArrayList.size(); i++) {
				NormalizeMetaData normalizeMetaData = metaDataArrayList.get(i);
				ArrayList<String> infosArrayList = new ArrayList<String>();//写入处理归一化文件信息
				int templinkID = normalizeMetaData.getLinkID();
				if (linkID == templinkID) {
					int linkSameDirection = normalizeMetaData.getLinkSameDirection();
					double linkSameDirecMaxVal = normalizeMetaData.getLinkSameDirectionMax();
		    		double linkSameDirecMinVal = normalizeMetaData.getLinkSameDirectionMin();
		    		int linkAntiDirection = normalizeMetaData.getLinkAntiDirection();
		    		double linkAntiDirecMaxVal = normalizeMetaData.getLinkAntiDirectionMax();
		    		double linkAntiDirecMinVal = normalizeMetaData.getLinkAntiDirectionMin();    				    		
		            DataSet trainingDataSet = new DataSet(3, 1);
		            ArrayList<String> intendToPredictTimeArrayList = new ArrayList<String>();//即将预测的时间序列
		            String []predictEndTimeArray = new String[1];
		            PubClass.obtainEndTimeAccordStartTime(timeStr, 3600 * 24, predictEndTimeArray);
		            String predictEndTimeStr = predictEndTimeArray[0];
		            String []endTimeArray = new String[1];
		            PubClass.obtainEndTimeAccordStartTime(timeStr, 600, endTimeArray);//时间间隔10min
		            String curTimeStr = endTimeArray[0];          
		            while (!curTimeStr.equals(predictEndTimeStr)) {
		            	intendToPredictTimeArrayList.add(curTimeStr);
		            	double []inputVector = new double[3]; 
		            	obtainNeuralNetInputDataSetRow(endTimeArray[0], inputVector);//获得输入向量
		    			double hour = inputVector[0];
		    			double preHalfHour = inputVector[1];
		    			double workDay = inputVector[2];	
		    			double v1 = (double)(hour - linkSameDirecMinVal)/(linkSameDirecMaxVal - linkSameDirecMinVal);
		    	        double v2 = (double)(preHalfHour - linkSameDirecMinVal)/(linkSameDirecMaxVal - linkSameDirecMinVal);
		    	        double v3 = (double)(workDay - linkSameDirecMinVal)/(linkSameDirecMaxVal - linkSameDirecMinVal);
		    	        trainingDataSet.addRow(new DataSetRow(new double[]{v1, v2, v3}, new double[]{0}));
		    	        endTimeArray = new String[1];
		                PubClass.obtainEndTimeAccordStartTime(curTimeStr, 600, endTimeArray);//时间间隔10min
		                curTimeStr = endTimeArray[0];
					}
		            //路段同向行程时间预测
		            System.out.println("路段同向行程时间预测！" + '\n');
		            String tempNameStr = "linkID" + linkID + "-" + linkSameDirection;
		            String neuralNetName = tempNameStr + "-MultiLayerPerceptron5min.nnet";	
		        	// load saved neural network
		            NeuralNetwork loadedNeuralNet = NeuralNetwork.load(neuralNetName);
		            //神经网络预存储文件
		            String writePredictPath = travelTimePredictFolderName + "predictTravelTime" + tempNameStr + ".txt";
		            File file1 = new File(writePredictPath);
		            if (file1.exists()) {
						file1.delete();
						writeNeuralNetPredictToPredictTxt(loadedNeuralNet, trainingDataSet, linkSameDirecMaxVal, linkSameDirecMinVal, writePredictPath, intendToPredictTimeArrayList);						
					}
		            else {
		            	writeNeuralNetPredictToPredictTxt(loadedNeuralNet, trainingDataSet, linkSameDirecMaxVal, linkSameDirecMinVal, writePredictPath, intendToPredictTimeArrayList);
					}
		            System.out.println("路段同向行程时间预测结束！" + '\n');
		            //路段异向通行时间预测
		            System.out.println("路段异向行程时间预测开始！" + '\n');
		            tempNameStr = "linkID" + linkID + "-" + linkAntiDirection;
		            neuralNetName = tempNameStr + "-MultiLayerPerceptron5min.nnet";
		            loadedNeuralNet = NeuralNetwork.load(neuralNetName);
		            writePredictPath = travelTimePredictFolderName + "predictTravelTime" + tempNameStr + ".txt";
		            File file2 = new File(writePredictPath);
		            if (file2.exists()) {
		            	file2.delete();
		            	writeNeuralNetPredictToPredictTxt(loadedNeuralNet, trainingDataSet, linkAntiDirecMaxVal, linkAntiDirecMinVal, writePredictPath, intendToPredictTimeArrayList);
					}
		            else {
		            	writeNeuralNetPredictToPredictTxt(loadedNeuralNet, trainingDataSet, linkAntiDirecMaxVal, linkAntiDirecMinVal, writePredictPath, intendToPredictTimeArrayList);
					}
		            System.out.println("路段异向行程时间预测结束！" + '\n');
		            System.out.println("neuralNet预测结束" + '\n');
				}				
	        }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}  	
    }
    
    /**
     * 预测的神经网络信息写入txt文件中
     * @param neuralNet
     * @param dataSet
     * @param max
     * @param min
     * @param writePredictPath
     * @param predictedTravelTimeArrayList
     * @param intendToPredictTimeArrayList	即将预测的通行时间
     */
    public void writeNeuralNetPredictToPredictTxt(NeuralNetwork neuralNet, DataSet dataSet, double max, double min, String writePredictPath, 
    		 ArrayList<String> intendToPredictTimeArrayList) {
    	int count = 0;
    	ArrayList<String> predictedTravelTimeArrayList = new ArrayList<String>();
    	try {
    		for(DataSetRow testSetRow : dataSet.getRows()) {
        		count++;
                neuralNet.setInput(testSetRow.getInput());
                neuralNet.calculate();
                double[] networkOutput = neuralNet.getOutput();
                double processNormalize = networkOutput[0]*(max - min) + min;
                String tempNetworkOutput = String.format("%.4f", networkOutput[0]);//保留小数点后四位，并进行四舍五入
                String tempProcessNormalize = String.format("%.4f", processNormalize);
                System.out.print(count + ":" + "Input:" + Arrays.toString(testSetRow.getInput()));
                System.out.println("Output:" + tempNetworkOutput + "：processNormalize:" + tempProcessNormalize);
                double []tempArrayDou = testSetRow.getInput();
                String []tempArrayStr = new String[4];
                tempArrayStr[0] = String.format("%.6f",tempArrayDou[0]);
                tempArrayStr[1] = String.format("%.6f",tempArrayDou[1]);
                tempArrayStr[2] = String.format("%.6f",tempArrayDou[2]);
                tempArrayStr[3] = tempProcessNormalize;
                predictedTravelTimeArrayList.add(tempProcessNormalize);
            }
        	//写入txt文件     
    		System.out.print("开始写数据到Txt文件:" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(writePredictPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String descriptionStr = "predictTime" + "," + "predictTravelTime" + "\r\n"; 
			write.append(descriptionStr);
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < predictedTravelTimeArrayList.size(); i++) {	
				String curTimeStr = intendToPredictTimeArrayList.get(i);
				String predictTravelTimeStr = predictedTravelTimeArrayList.get(i);
				String writeStr = curTimeStr + "," + predictTravelTimeStr + "\r\n"; 
				System.out.print(i + ":" + writeStr);
				write = new StringBuffer();		
				write.append(writeStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
			System.out.print("写入结束:" + '\n');		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
    /**
     * 读取normalize文件中的元数据信息
     * @param filePathStr 文件路径
     * @param metaDataArrayList 存放元数据信息
     */
    public void readMetaDataDescriptionFromNormalize(String filePathStr, ArrayList<NormalizeMetaData> metaDataArrayList){
    	try {
    		ArrayList<Integer> linkIDArrayList = new ArrayList<Integer>();//路段ID集合
    		File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();//文件标题			
				while (str != null) {
					if (str.equals("allLinkID:")) {
						str = bufferedReader.readLine();//路段ID
						String[]IDArrayStr = str.split(",");
						for (int i = 0; i < IDArrayStr.length; i++) {
							linkIDArrayList.add(Integer.parseInt(IDArrayStr[i]));
						}
						break;
					}
					else {	
						str = bufferedReader.readLine();
					}
				}
				str = bufferedReader.readLine();
				while(str != null){
					if (str.equals("allMetaData:")) {
						break;
					}
					else {
						str = bufferedReader.readLine();
					}					
				}
				str = bufferedReader.readLine();
				while (str != null) {
					if (str.equals("endMetaDataDescription")) {
						break;
					}
					else {
						String[]tempIDArrayStr = str.split(",");
						NormalizeMetaData normalizeMetaData = new NormalizeMetaData();
						normalizeMetaData.setLinkID(Integer.parseInt(tempIDArrayStr[0]));
						normalizeMetaData.setLinkSameDirectionMax(Double.parseDouble(tempIDArrayStr[2]));
						normalizeMetaData.setLinkSameDirectionMin(Double.parseDouble(tempIDArrayStr[3]));
						normalizeMetaData.setLinkSameDirectionDataCount(Integer.parseInt(tempIDArrayStr[4]));
						str = bufferedReader.readLine();
						tempIDArrayStr = str.split(",");
						normalizeMetaData.setLinkAntiDirectionMax(Double.parseDouble(tempIDArrayStr[2]));
						normalizeMetaData.setLinkAntiDirectionMin(Double.parseDouble(tempIDArrayStr[3]));
						normalizeMetaData.setLinkAntiDirectionDataCount(Integer.parseInt(tempIDArrayStr[4]));
						metaDataArrayList.add(normalizeMetaData);
						str = bufferedReader.readLine();
					}					
				}				
				reader.close();
			}
			System.out.print("元数据读取结束！" + '\n');
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
    /**
     * 读取文件中原始的归一化数据
     * @param maxMin
     * @param linkIDDirec
     * @param filePathStr
     * @param infosArrayList
     */
    public void readOriginalNormalizeTxtFileAccordLinkIDAndDirection(int linkID, int direction, String filePathStr, ArrayList<String> infosArrayList){		
		try {
			File file = new File(filePathStr);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				//定位到元数据
				while (str != null) {
					if (str.equals("metaData:") || str.equals("endtxtDescription")) {
						str = bufferedReader.readLine();//获得元数据
						String []metaDataArray = str.split(",");
						int tempLinkID = Integer.parseInt(metaDataArray[0]);
						int tempDirection = Integer.parseInt(metaDataArray[1]);
						if (tempLinkID == linkID && tempDirection == direction) {
							System.out.print(str + "\n");
							break;
						}	
					}
					else {
						str = bufferedReader.readLine();
					}					
				}	
				
				str = bufferedReader.readLine();
				while (str != null) {	
					if (str.equals("metaData:") || str.equals("endtxtDescription")) {
						break;
					}
					else {						
						infosArrayList.add(str);
						System.out.print(str + "\n");	
						str = bufferedReader.readLine();
					}
				}
				reader.close();
				System.out.print("结束读取文件" + filePathStr + "txt信息:"  + "\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}			
		}
		catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}		
	}
    
    /**
     * 将处理后归一化信息写入txt文件
     * @param filePathStr
     * @param infosArrayList
     */
    public void writeProcessNormalizeToTxtFile(String filePathStr, ArrayList<String> infosArrayList){
    	try {
    		FileOutputStream outputStream = new FileOutputStream(new String(filePathStr));//写入文件末尾处
    		BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
    		StringBuffer write = new StringBuffer();
    		String writeStr = "";
    		for (int i = 0; i < infosArrayList.size(); i++) {
    			write = new StringBuffer();
    			writeStr = infosArrayList.get(i);
    			write.append(writeStr + "\r\n");
    			bufferStream.write(write.toString().getBytes("UTF-8"));
    			System.out.print(writeStr + '\n');
			}
    		bufferStream.flush();  
    		bufferStream.close();
    		outputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
    /**
     * Prints network output for the each element from the specified training set.
     * @param neuralNet neural network
     * @param testSet test data set
     * @param max 数据归一化的最大值
     * @param min 数据归一化的最小值
     */
    public void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet, double max, double min) {
    	try {
    		int count = 0;
        	for(DataSetRow testSetRow : testSet.getRows()) {
        		count++;
                neuralNet.setInput(testSetRow.getInput());
                neuralNet.calculate();
                double[] networkOutput = neuralNet.getOutput();
                double processNormalize = networkOutput[0]*(max - min) + min;
                String tempNetworkOutput = String.format("%.4f", networkOutput[0]);//保留小数点后四位，并进行四舍五入
                String tempProcessNormalize = String.format("%.4f", processNormalize);
                System.out.print(count + ":" + "Input:" + Arrays.toString(testSetRow.getInput()) );
                System.out.println("Output:" + tempNetworkOutput + "：processNormalize:" + tempProcessNormalize);
            }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}   	
    }
    
    /**
     * 根据输入时间获得神经网络输入向量
     * @param linkID	路段ID
     * @param timeStr	时刻，格式2013-01-01 00:01:00
     * @param inputVector	输入向量
     */
    public void obtainNeuralNetInputDataSetRow(String timeStr, double[]inputVector){
    	try {
			String[]tempArrayStr = timeStr.split(" ");
			String dateStr = tempArrayStr[0];
			boolean isWorkday = true;//是否为工作日
			String[]hourMinSec = tempArrayStr[1].split(":");
			String hourStr = hourMinSec[0];//时
			String minStr = hourMinSec[1];//分				
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");      
			Date date = simpleDateFormat.parse(dateStr); 
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
		    	isWorkday = false;
			}
		    if (isWorkday) {
				inputVector[2] = 1;
			}
		    else {
		    	inputVector[2] = 0;
			}			    
			int hourInt = Integer.valueOf(hourStr);
			int minInt = Integer.valueOf(minStr);
			switch (hourInt) {
			case 0:
				if (minInt <= 30) {
					inputVector[0] = 0;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 0;
					inputVector[1] = 0;
				}
				break;
			case 1:
				if (minInt <= 30) {
					inputVector[0] = 1;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 1;
					inputVector[1] = 0;
				}
				break;					
			case 2:
				if (minInt <= 30) {
					inputVector[0] = 2;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 2;
					inputVector[1] = 0;
				}
				break;
			case 3:
				if (minInt <= 30) {
					inputVector[0] = 3;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 3;
					inputVector[1] = 0;
				}
				break;
			case 4:
				if (minInt <= 30) {
					inputVector[0] = 4;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 4;
					inputVector[1] = 0;
				}
				break;
			case 5:
				if (minInt <= 30) {
					inputVector[0] = 5;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 5;
					inputVector[1] = 0;
				}
				break;
			case 6:
				if (minInt <= 30) {
					inputVector[0] = 6;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 6;
					inputVector[1] = 0;
				}
				break;
			case 7:
				if (minInt <= 30) {
					inputVector[0] = 7;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 7;
					inputVector[1] = 0;
				}
				break;
			case 8:
				if (minInt <= 30) {
					inputVector[0] = 8;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 8;
					inputVector[1] = 0;
				}
				break;
			case 9:
				if (minInt <= 30) {
					inputVector[0] = 9;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 9;
					inputVector[1] = 0;
				}
				break;
			case 10:
				if (minInt <= 30) {
					inputVector[0] = 10;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 10;
					inputVector[1] = 0;
				}
				break;
			case 11:
				if (minInt <= 30) {
					inputVector[0] = 11;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 11;
					inputVector[1] = 0;
				}
				break;
			case 12:
				if (minInt <= 30) {
					inputVector[0] = 12;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 12;
					inputVector[1] = 0;
				}
				break;
			case 13:
				if (minInt <= 30) {
					inputVector[0] = 13;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 13;
					inputVector[1] = 0;
				}
				break;
			case 14:
				if (minInt <= 30) {
					inputVector[0] = 14;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 14;
					inputVector[1] = 0;
				}
				break;
			case 15:
				if (minInt <= 30) {
					inputVector[0] = 15;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 15;
					inputVector[1] = 0;
				}
				break;
			case 16:
				if (minInt <= 30) {
					inputVector[0] = 16;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 16;
					inputVector[1] = 0;
				}
				break;
			case 17:
				if (minInt <= 30) {
					inputVector[0] = 17;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 17;
					inputVector[1] = 0;
				}
				break;
			case 18:
				if (minInt <= 30) {
					inputVector[0] = 18;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 18;
					inputVector[1] = 0;
				}
				break;
			case 19:
				if (minInt <= 30) {
					inputVector[0] = 19;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 19;
					inputVector[1] = 0;
				}
				break;
			case 20:
				if (minInt <= 30) {
					inputVector[0] = 20;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 20;
					inputVector[1] = 0;
				}
				break;
			case 21:
				if (minInt <= 30) {
					inputVector[0] = 21;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 21;
					inputVector[1] = 0;
				}
				break;
			case 22:
				if (minInt <= 30) {
					inputVector[0] = 22;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 22;
					inputVector[1] = 0;
				}
				break;
			case 23:
				if (minInt <= 30) {
					inputVector[0] = 23;
					inputVector[1] = 1;
				}
				else {
					inputVector[0] = 23;
					inputVector[1] = 0;
				}
				break;
			default:
				break;
			}
			String vectorStr = inputVector[0] + "," + inputVector[1] + "," + 
				inputVector[2] + "\r\n"; 
			System.out.print("输入向量为：" + vectorStr + '\n');   		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}   	
    }
    
    //测试神经网络的训练结果
    public void testNeuralNet(){
    	//获得输入向量
    	String inputFileName = DeleteBufferedDataSetSample.class.getResource("discretizeVector.txt").getFile();
        String outputFileName = "C:\\normalize.txt";
        String writePredictPath ="C:\\neuralNetPrediction.txt";//神经网络预测
        DeleteNormalize normalize = new DeleteNormalize();
        normalize.NormalizeTxt(inputFileName, outputFileName);//输入向量归一化
        int max = normalize.getMax();//归一化过程中最大值
        int min = normalize.getMin();//归一化过程中最小值
        ArrayList<String[]> normalizeInfosArrayList = new ArrayList<String[]>();//存储归一化信息
        readNormalizeTxt(outputFileName, normalizeInfosArrayList);
    	// load saved neural network
        NeuralNetwork loadedNeuralNet = NeuralNetwork.load("mySampleMultiLayerPerceptron.nnet");
        // test loaded neural network
        System.out.println("Testing loaded neuralNet……");
        DataSet testTrainingSet = new DataSet(4, 1);
        for (int i = 0; i < normalizeInfosArrayList.size(); i++) {
			String []tempArrayStr = normalizeInfosArrayList.get(i);
			double linkID = Double.parseDouble(tempArrayStr[0]);
			double hour = Double.parseDouble(tempArrayStr[1]);
			double halfHour = Double.parseDouble(tempArrayStr[2]);
			double workDay = Double.parseDouble(tempArrayStr[3]);
			double travelTime = Double.parseDouble(tempArrayStr[4]);			
	        testTrainingSet.addRow(new DataSetRow(new double[]{linkID, hour, halfHour, workDay}, new double[]{0}));        	
		}   
        ArrayList<String> predictTimeArrayList = new ArrayList<String>();//神经网络预测时间
        neuralNetPredictWriteToTxt(loadedNeuralNet, testTrainingSet, max, min, writePredictPath, predictTimeArrayList);
        System.out.println("neuralNet预测结束" + '\n');
        String writePathStr = "C:\\compareStatisticsPredictTime.txt";
        compareStatisticsPredictTimeWriteToTxt(predictTimeArrayList, writePathStr);
    }
    
    /**
     * @param neuralNet	训练过的神经网络
     * @param testSet	数据集合
     * @param max	归一化的最大值
     * @param min	归一化的最小值
     * @param writePredictPath	要写入文件的路径
     * @param predictTimeArrayList	神经网络预测时间
     */
    public void neuralNetPredictWriteToTxt(NeuralNetwork neuralNet, DataSet dataSet, int max, int min, String writePredictPath, 
    		ArrayList<String> predictTimeArrayList) {
    	int count = 0;
    	ArrayList<String[]> infosArrayList = new ArrayList<String[]>();
    	try {
    		for(DataSetRow testSetRow : dataSet.getRows()) {
        		count++;
                neuralNet.setInput(testSetRow.getInput());
                neuralNet.calculate();
                double[] networkOutput = neuralNet.getOutput();
                double processNormalize = networkOutput[0]*(max - min) + min;
                String tempNetworkOutput = String.format("%.4f", networkOutput[0]);//保留小数点后四位，并进行四舍五入
                String tempProcessNormalize = String.format("%.4f", processNormalize);
                System.out.print(count + ":" + "Input:" + Arrays.toString(testSetRow.getInput()));
                System.out.println("Output:" + tempNetworkOutput + "：processNormalize:" + tempProcessNormalize);
                double []tempArrayDou = testSetRow.getInput();
                String []tempArrayStr = new String[5];
                tempArrayStr[0] = String.format("%.6f",tempArrayDou[0]);
                tempArrayStr[1] = String.format("%.6f",tempArrayDou[1]);
                tempArrayStr[2] = String.format("%.6f",tempArrayDou[2]);
                tempArrayStr[3] = String.format("%.6f",tempArrayDou[3]);
                tempArrayStr[4] = tempProcessNormalize;
                predictTimeArrayList.add(tempProcessNormalize);
                infosArrayList.add(tempArrayStr);
            }
        	//写入txt文件     
    		System.out.print("开始写数据到Txt文件:" + '\n');
			FileOutputStream outputStream = new FileOutputStream(new String(writePredictPath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			bufferStream.write(write.toString().getBytes("UTF-8"));
			for (int i = 0; i < infosArrayList.size(); i++) {
				String []tempArrayStr = infosArrayList.get(i);				
				String writeStr = tempArrayStr[0] + "," + tempArrayStr[1] + "," + tempArrayStr[2] + "," + tempArrayStr[3] + "," + tempArrayStr[4] + "\r\n"; 
				System.out.print(i + ":" + writeStr);
				write = new StringBuffer();		
				write.append(writeStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
			System.out.print("写入结束:" + '\n');		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
    /**
     * 将统计时间与神经网络预计时间合并比较并写入文件路径
     * @param predictTimeArrayList	神经网络预测时间
     * writePathStr	写入文件路径
     */
    public void compareStatisticsPredictTimeWriteToTxt(ArrayList<String> predictTimeArrayList, String writePath){
    	String path = "C:\\mergeStatisticTravelTime.txt";   
    	ArrayList<String[]> infosArrayList = new ArrayList<String[]>();
		System.out.print("开始读txt文件" + "\n");		
		try {
			File file = new File(path);
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				System.out.print(str + "\n");
				while (str != null) {
					str = bufferedReader.readLine();
					if (str != null) {
						String[]tempArrayStr = str.split(",");
						String[]arrayStr = new String[3];
						arrayStr[0] = tempArrayStr[0];
						arrayStr[1] = tempArrayStr[2];
						arrayStr[2] = tempArrayStr[3];
						infosArrayList.add(arrayStr);
						System.out.print(str+"\n");
					}							
				}
				reader.close();
				System.out.print("结束读txt文件"+"\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}			
			//往txt写数据			
			FileOutputStream outputStream = new FileOutputStream(new String(writePath));
			BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
			StringBuffer write = new StringBuffer();
			String description = "linkID" + ","  + "startTravelTime" + "," + "travelTime" + "," + "predicTime" + "\r\n";
			write.append(description);
			bufferStream.write(write.toString().getBytes("UTF-8"));			
			for (int i = 0; i < infosArrayList.size(); i++) {
				String []tempArrayStr = infosArrayList.get(i);		
				String predictTimeStr = predictTimeArrayList.get(i);
				String writeStr = tempArrayStr[0] + "," + tempArrayStr[1] + "," + tempArrayStr[2] + "," + predictTimeStr + "\r\n"; 
				System.out.print(i + ":" + writeStr);
				write = new StringBuffer();		
				write.append(writeStr);
				bufferStream.write(write.toString().getBytes("UTF-8"));
			}
			bufferStream.flush();  
			bufferStream.close();
			outputStream.close();
			System.out.print("写入结束:" + '\n');
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    /**
     * 读归一化txt文件
     * @param readPath 归一化文件路径
     * @param normalizeInfosArrayList 归一化文件信息，路段编号、时、分、工作日、通行时间
     */
    public void readNormalizeTxt(String readPath, ArrayList<String[]> normalizeInfosArrayList){
    	System.out.print("开始读txt文件:" + "\n");
		File file = new File(readPath);
		try {
			if (file.exists()) {
				String encoding = "UTF-8";//解决中文乱码问题
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file),encoding);			
				BufferedReader bufferedReader = new BufferedReader(reader);
				String str = bufferedReader.readLine();
				while (str != null) {				
					String[]tempArrayStr = str.split(",");
					normalizeInfosArrayList.add(tempArrayStr);
					str = bufferedReader.readLine();						
				}
				reader.close();
				System.out.print("结束读txt文件:" + readPath + "\n");
			}
			else {
				System.out.print("指定的文件不存在！" + '\n');
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
    }
    
   
    
    
}
