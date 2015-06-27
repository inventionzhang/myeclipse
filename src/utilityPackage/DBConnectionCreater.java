package utilityPackage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class DBConnectionCreater {
	private static java.sql.Driver driverd=null;
	public static Connection ConnectionCreater(String querySspcs) throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		File jdbcDriverJarFileLocation = new File(PropertiesUtil.getProperties("jdbclink"));
		URL jdbcDriverURL = jdbcDriverJarFileLocation.toURL();
		URL[] urls = new URL[1];
		urls[0] = jdbcDriverURL;
		URLClassLoader urlclassLoader = new URLClassLoader(urls,ClassLoader.getSystemClassLoader());
		if(driverd==null){
			driverd = (java.sql.Driver)urlclassLoader.loadClass("oracle.jdbc.driver.OracleDriver").newInstance();
		}
		Properties props = new Properties();
	    props.setProperty("user", PropertiesUtil.getProperties(querySspcs));
	    props.setProperty("password", PropertiesUtil.getProperties(querySspcs));
	    return driverd.connect(PropertiesUtil.getProperties("databasename"), props);   	
	}
	
	public static Connection ConnectionCreaterForTG(String username, String password, String url) throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		String usernamelvguan = PropertiesUtil.getProperties(username);
		String passwordlvguan = PropertiesUtil.getProperties(password);
		String urllvguan = PropertiesUtil.getProperties(url);
		Class.forName("oracle.jdbc.driver.OracleDriver");
		return DriverManager.getConnection(urllvguan, usernamelvguan, passwordlvguan); 	
	}
	
	public static void close(Connection conn,PreparedStatement pstmt,ResultSet rs)
	{
		try
		{
			if(rs!=null)
			{
				rs.close();
				rs = null;
			}
		    if(pstmt!=null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(conn!=null&&(!conn.isClosed()))
			{
				conn.close();
				conn = null;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
