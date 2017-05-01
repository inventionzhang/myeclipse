//package org.lmars.network.servlet;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import javax.servlet.ServletException;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import net.sf.json.JSONObject;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.lmars.network.service.ServiceTrajectoryCorrect;
//
//import net.sf.json.JSONObject;
//
///**
// * 交管局的Servlet
// * @author faming
// *
// */
//public class ServletJG extends HttpServlet {
//
//	/**
//	 * Constructor of the object.
//	 */
//	public ServletJG() {
//		super();
//	}
//
//	/**
//	 * Destruction of the servlet. <br>
//	 */
//	public void destroy() {
//		super.destroy(); // Just puts "destroy" string in log
//		// Put your code here
//	}
//
//	/**
//	 * The doGet method of the servlet. <br>
//	 *
//	 * This method is called when a form has its tag value method equals to get.
//	 * 
//	 * @param request the request send by the client to the server
//	 * @param response the response send by the server to the client
//	 * @throws ServletException if an error occurred
//	 * @throws IOException if an error occurred
//	 */
//	public void doGet(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		doPost(request, response);
//	}
//
//	/**
//	 * The doPost method of the servlet. <br>
//	 *
//	 * This method is called when a form has its tag value method equals to post.
//	 * 
//	 * @param request the request send by the client to the server
//	 * @param response the response send by the server to the client
//	 * @throws ServletException if an error occurred
//	 * @throws IOException if an error occurred
//	 */
//	public void doPost(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//
//		try {
//			response.setHeader("Access-Control-Allow-Origin", "*");
//			request.setCharacterEncoding("UTF-8");
//	        String byValue = request.getParameter("byValue");
//	        JSONObject JSONObj = JSONObject.fromObject(byValue);
//	        
////	        double longitude=Double.valueOf(JSONObj.get("X").toString());
////	        double latitude=Double.valueOf(JSONObj.get("Y").toString());	        
////	        JSONObject jsonObjBy  = new JSONObject();      	       
////	        jsonObjBy.put("Longitude", longitude);
////	        jsonObjBy.put("Latitude", latitude);
//	        
//	        ServiceTrajectoryCorrect serviceJG = new ServiceTrajectoryCorrect();
//	        JSONObject jsonObjResult = serviceJG.getRoadSplitInfo(JSONObj);
//	        String result = jsonObjResult.toString();
//	        System.out.println(result);
//	        ServletOutputStream outputStream = response.getOutputStream();
//			String content = result;
//			outputStream.write(content.getBytes("UTF-8"));
//			outputStream.flush();
//			outputStream.close();
//		} catch (Exception e) {
//			System.out.println("获取信息错误："+e.getMessage());
//			ServletOutputStream outputStream = response.getOutputStream();
//			String content ="error";
//			outputStream.write(content.getBytes("UTF-8"));
//			outputStream.flush();
//			outputStream.close();
//		}
//	}
//
//	/**
//	 * Initialization of the servlet. <br>
//	 *
//	 * @throws ServletException if an error occurs
//	 */
//	public void init() throws ServletException {
//		// Put your code here
//	}
//
//}
