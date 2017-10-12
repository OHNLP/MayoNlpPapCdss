/*******************************************************************************
 * Copyright: (c)  2014  Mayo Foundation for Medical Education and 
 *  Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 *  triple-shield Mayo logo are trademarks and service marks of MFMER.
 *   
 *  Except as contained in the copyright notice above, or as used to identify 
 *  MFMER as the author of this software, the trade names, trademarks, service
 *  marks, or product names of the copyright holder shall not be used in
 *  advertising, promotion or otherwise in connection with this software without
 *  prior written authorization of the copyright holder.
 *     
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *     
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 *******************************************************************************/

package org.ohnlp.cidb.io;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.ohnlp.cidb.core.DocSet;
import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.LabelHashMap;
import org.ohnlp.cidb.core.ParValMap;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;


/*
@author Kavishwar Wagholikar (waghsk)
*/
public class Sql2 implements DataSource {
    
    static LabelHashMap<String,Sql2> allHt=new LabelHashMap<String,Sql2>();//all Sql objects 
    
    
    static Logger logger = LoggerFactory.getLogger(Sql2.class);
    Connection con;
    Statement st;
    GregorianCalendar connectionInitTime;
    String sql;
    SqlBuilder b;
    BoneCP connectionPool = null;
    int callCount=0;
    private Sql2(){}
    
    public Sql2( SqlBuilder b) throws CIDBException, SQLException {
	this.b=b;
	logger.trace("built:"+b);
	
	init();
	if(!allHt.containsKey(b.label))allHt.put(b.label,this);
	b.session.putObject(Sql2.class,b.label, allHt.get(b.label));
    }
   
    
    public Sql2 setQuery(String sql){
  	this.sql=sql;
  	return this;
    }
    
    
    public int getCallCount(){
  	return callCount;
      }
    
/*
 * resets call count
 * if connection or statement is null or closed opens it.
 */
    public void init() throws CIDBException, SQLException{
		if(b.driverClassName==null) throw new CIDBException("driverClassName is null");
		if(b.connectionUrl==null) throw new CIDBException("connectionUrl is null");
		
		
		try {
	 	    Class.forName(b.driverClassName);
	 	
	 	    	/*cpds = new ComboPooledDataSource();
		        cpds.setDriverClass(b.driverClassName); //loads the jdbc driver
		        cpds.setJdbcUrl(b.connectionUrl);
	*/

		        // the settings below are optional -- c3p0 can work with defaults
		        //cpds.setMinPoolSize(1);
		        //cpds.setAcquireIncrement(1);
		        //cpds.setMaxPoolSize(2);
		        //cpds.setMaxConnectionAge(500);
		        //cpds.setMaxStatements(100);
		        //cpds.setTestConnectionOnCheckin(true);
		        //cpds.setIdleConnectionTestPeriod(160);
		        //cpds.setMaxIdleTime(300);

		        BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(b.connectionUrl); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername("");
			config.setPassword("");
			config.setMinConnectionsPerPartition(1);
			
			config.setMaxConnectionsPerPartition(3);
			config.setPartitionCount(1);
			config.setDisableConnectionTracking(true);//XXX to see if this should be disabled for avoiding memory leak;
			config.setConnectionTimeout(100, TimeUnit.SECONDS);
			
			connectionPool = new BoneCP(config); // setup the connection pool
			
			
	 	       //con=cpds.getConnection();
			con=connectionPool.getConnection();
		       st= con.createStatement();//???Abstract method error
		    
		} catch (SQLException e) {
		    logger.error("fatal",e);
		    throw new CIDBException(e.getMessage());
		/*} catch (AbstractMethodError e) {
		    logger.error("fatal",e);
		    throw new DataFetchException(e.getMessage()+"st= "+st);
		*/} catch (ClassNotFoundException e) {
		    logger.error("fatal",e);
		    throw new CIDBException(e.getMessage());	
		}
		connectionInitTime= new GregorianCalendar(); 
		callCount=0;
		
    }
    
    public ResultSet query(String sql) throws CIDBException{
		this.sql=sql;
		
		ResultSet rs=null;
		String result="";
		callCount+=1;
		logger.trace("call #"+callCount+" to "+this.b.label);
		logger.trace("SQL:"+sql);
	    try {
		checkConnectionAndStatment();
		int timeout=120;
		try{
		    timeout=Integer.parseInt(this.b.session.GetProp("SQL_TIMEOUT_SECONDS"));
		}catch(NumberFormatException e){ timeout=120;}
		st.setQueryTimeout(timeout);
		rs= st.executeQuery(sql);
		//rs.next();
		//result+=rs.getString(1); 
	/*	for (int i=2;i<=rs.getMetaData().getColumnCount();i++){
		    result+="|"+rs.getString(i);
		}*/
		//System.out.println(result);
	    } catch (SQLException e) {
		    logger.error("fatal",e);
		    throw new CIDBException(e.getMessage());
	    }
	    logger.trace("returning");
	    return rs;
    } 
    
    public int update(String sql) throws CIDBException{
	this.sql=sql;
	int updatedRows=0;
	    callCount+=1;
	    logger.trace("sql call:"+callCount);
	    logger.trace("will update :"+sql);
	    
	try {
	    checkConnectionAndStatment();
	    updatedRows=st.executeUpdate(sql);
	
	} catch (SQLException e) {
	    logger.error("fatal:"+this.toString(),e);
	    throw new CIDBException(e.getMessage());
	}
    return updatedRows;
} 
  
    
   
    
     static public String GregorianCalendarToString(GregorianCalendar gc){
	return String.format("'%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.0'", gc);
    }


    
    private ParValMap getParValMap() throws CIDBException {
	ParValMap d=new ParValMap();
	 try {
	     	ResultSet rs=query(sql);
		rs= st.executeQuery(sql);
		rs.next();
		//if(rs.getRow()!=1) throw  new DataFetchException("number of rows returned is not 1");
		if(rs.getRow()!=1) {logger.warn("number of rows returned is not 1");return d;}
		for (int i=1;i<=rs.getMetaData().getColumnCount();i++){
		    String key=rs.getMetaData().getColumnName(i);
		    String val=rs.getString(i);
		    logger.trace("putting:"+key+":"+val);
		    d.put(key,val);
		}
		
	    } catch (SQLException e) {
		    logger.error("fatal",e);
		    throw new CIDBException(e.getMessage());
	    }
	return d;
    }


    @Override
    public DocSet getDocSet(String label, Session session) throws CIDBException {
	 logger.trace("will create DocSet from query:"+this.sql);
	DocSet ds= new DocSet(session,label);
	
	 try {
	     	ResultSet rs=query(sql);
		rs= st.executeQuery(sql);
		//logger.trace("number of rows:"+rs.)
		
		while(rs.next()){
		    Document d=new Document();
		    for (int i=1;i<=rs.getMetaData().getColumnCount();i++){
			String key=rs.getMetaData().getColumnName(i);
			String val=rs.getString(i);
			if (d.keySet().contains(key)) continue;
			if(key==null )throw new CIDBException("key is null");
			if(val!=null){
			    d.put(key,val);
			    
			}else    d.put(key,"null");
		    }
		   
		    ds.add(d);
		    logger.trace("added:"+d);
		}
		
	    } catch (SQLException e) {
		    logger.error("fatal",e);
		    throw new CIDBException(e.getMessage());
	    }
	logger.debug("returning docSet:"+ds);
	return ds;
    }
    @Override
    public Document getDocument(String label,Session session) throws CIDBException {
	Document d= Document.createLabelledDoc(label, session);
	ParValMap hm= getParValMap();
	for(String k:hm.keySet()) d.put(k,hm.get(k));
	logger.trace("returning:"+d);
	return d;
    } 
    

    
    public String toString(){
	String str= this.b.label;
	if(sql!=null) str+="\n "+sql;
	return str;
    }

    
    void checkConnectionAndStatment() throws CIDBException, SQLException{
	GregorianCalendar now=new GregorianCalendar();
	Double yearInHours=365.0*24;
	//if connection more than one tenth of hour old
	/*if(Utils.timeSinceInYears(this.connectionInitTime,now)>(0.1/yearInHours)){
	    this.con.close();
	}
	if(callCount>1000) this.con.close();*/
	//if(callCount>1000 ) cpds.close();
	/*if(cpds!=null & con!=null & con.getWarnings()!=null) {
	    logger.warn("Closing pool for reset as Connection warning:"+con.getWarnings());
	    cpds.close();
	}*/
	//init();
	if(con.isClosed()) con=connectionPool.getConnection();
	if (st.isClosed()) st=con.createStatement();
    }
  
   static public void closeAllStatements() throws SQLException{
       for(String k: allHt.keySet()){
	    Sql2 s=allHt.get(k);
	    if(s!=null && s.st!=null )s.st.close();
	}
       logger.info("Sql objects:"+allHt.size());
   }
   
   static public void closeAllConnections() throws SQLException{
      
       for(String k: allHt.keySet()){
	   Sql2 s=allHt.get(k);
	   if(s!=null && s.st!=null )s.st.close();
	   if(s!=null && s.con!=null)s.con.close();
       }
   }
   
   static public void closeAllStaticPools() throws SQLException {
       closeAllStatements();
       closeAllConnections() ;
       for(String k: allHt.keySet()){
       	    	Sql2 s=allHt.get(k);
	   	if(s!=null && s.connectionPool!=null)s.connectionPool.shutdown();
	   	allHt.remove(k);
	 }
   }



   
}
