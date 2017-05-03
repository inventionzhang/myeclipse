package org.lmars.network.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.lmars.network.entity.DataBaseType;




public class JDBCConnectionCreator {
	//
	private static JDBCConnectionCreator singleton=null;
	private Map<String,DataSource> dsList;
	private Context envCtx;
	private ConcurrentHashMap<String,DataBaseType> dbTypeMap = new ConcurrentHashMap<String,DataBaseType>();
	
	public static synchronized JDBCConnectionCreator getInstance(){
		try{
			if(singleton == null) {
	            singleton = new JDBCConnectionCreator();
	        }
	        return singleton;
		}catch(NamingException ne){
			ne.printStackTrace();
			return null;
		}
	}

	private JDBCConnectionCreator() throws NamingException
	{
		dsList = new HashMap<String,DataSource>();
		Context initCtx = new InitialContext();
		envCtx = (Context) initCtx.lookup("java:comp/env/");
	}
	
	public Connection create(String resourceName)
	{
		try{
			DataSource ds = this.dsList.get(resourceName);
			if(ds==null){
				ds = this.initilizeDatasource(resourceName);
			}
			return ds.getConnection();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public DataBaseType getDataBaseType(String resourceName)
	{
		DataBaseType t = dbTypeMap.get(resourceName);
		try{
			if(t == null){
				Connection conn = this.create(resourceName);
				if(conn != null){
					t = DataBaseType.getTypeFromString(conn.getMetaData().getDatabaseProductName());
					dbTypeMap.putIfAbsent(resourceName, t);
				}else{
					t = DataBaseType.NONE;
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return t;
	}
	
	private DataSource initilizeDatasource(String resourceName) throws NamingException
	{
		DataSource ds = (DataSource) envCtx.lookup(resourceName);
		this.dsList.put(resourceName,ds);
		return ds;
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