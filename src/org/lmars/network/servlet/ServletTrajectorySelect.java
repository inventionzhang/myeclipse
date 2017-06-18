package org.lmars.network.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.lmars.network.service.ServiceTrajectoryCorrect;
import org.lmars.network.service.ServiceTrajectorySelect;

/**
 * 轨迹查询服务
 * @author faming
 *
 */
public class ServletTrajectorySelect extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public ServletTrajectorySelect() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			response.setHeader("Access-Control-Allow-Origin", "*");
			request.setCharacterEncoding("UTF-8");
	        String byValue = request.getParameter("byValue");
	        ServiceTrajectorySelect trajectorySelect = new ServiceTrajectorySelect();
	        JSONArray jsonArray = trajectorySelect.trajectorySelect(byValue);
//	        Iterator <Object> it = jsonArray.iterator();
//            while (it.hasNext()) {
//                JSONObject json = (JSONObject)it.next();
//                double corrLon = (Double)json.get("corrLon");
//                double corrLat = (Double)json.get("corrLat");
//                String timeStamp = (String)json.get("timeStamp");
//            } 
	        String result = jsonArray.toString();
	        System.out.println(result);
	        ServletOutputStream outputStream = response.getOutputStream();
			String content = result;
			outputStream.write(content.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			System.out.println("获取信息错误："+e.getMessage());
			ServletOutputStream outputStream = response.getOutputStream();
			String content ="error";
			outputStream.write(content.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
