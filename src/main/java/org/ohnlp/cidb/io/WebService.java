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


import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.ohnlp.cidb.core.DocSet;
import org.ohnlp.cidb.core.LabelHashMap;
import org.ohnlp.cidb.core.ParValMap;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
/*
 * will initate a webservice call to return doc/docSet
 * @author Kavishwar Wagholikar (waghsk)
 * @author Hongfang Liu (hol7001)
 */
public class WebService implements DataSource{
    //?will store all labelled response calls in docSet 
    Logger logger= LoggerFactory.getLogger(WebService.class);
    
    String responseText;
    WebServiceConfig config;
    WebServiceQuery query;
    
    
    public WebService(WebServiceConfig config) {
	this.config=config;
	
    }
    public WebService setQuery(WebServiceQuery q){
	 this.query=q;
	 return this;
    }
    
    public String processRequest() throws CIDBException{
	String response;
	
	    String requestXml=Utils.readFile(config.requestXmlPath);
	    
	    requestXml=Utils.substituteProps(config.session.getProps(), requestXml);
	    requestXml=Utils.substituteProps(config.requestProps, requestXml);
	    logger.trace("sending webservice request"+requestXml);
	    response = new SOAPCall(config.endpoint,requestXml,config.soapAction,config.session).getXmlString();
	    logger.trace("response"+response);
	    if(response.toLowerCase().contains("<status>failure</status>")) 
		throw new CIDBException("Failure response in SOAP Call"); 
	return response;
    }
    
    @Override
    public DocSet getDocSet(String label, Session inSession) throws CIDBException {
	if(this.query==null){
	    throw new IllegalStateException("set query before calling getDocSet");
	}
	 this.responseText=processRequest();
	 if( this.responseText.toLowerCase().indexOf("faultcode")>-1)
		throw new CIDBException("Fault response in the SOAP call");
	    
	DocSet ds= new DocSet(inSession,label);
	org.json.JSONObject jO;
	try {
	    jO = XML.toJSONObject(this.responseText);
	    System.out.println(jO);
	    
	    List<Object> records=new ArrayList<Object>();
	 try{
	  try{
	      logger.trace(""+JsonPath.read(jO.toString(), query.getRecXPath()));
	      records = JsonPath.read(jO.toString(), query.getRecXPath());
	  }catch(InvalidPathException e){
	      
	      logger.warn("NO RECORDS FOUND OR PATH NOT FOUND",e);
	      //throw new CIDBException(e);
	   }
	  
	  for(Object rec:records){
	      org.ohnlp.cidb.core.Document d=new org.ohnlp.cidb.core.Document();
	      System.out.println(JsonPath.read(rec, "$.[*]"));
	      logger.trace("varPath"+query.varPathMap.keySet());
	      for(String par:query.varPathMap.keySet()){
		  logger.trace("par:"+par+"->"+query.varPathMap.get(par)+":"+JsonPath.read(rec.toString(), "$."+query.varPathMap.get(par)+"[*]").toString());
		  
		  d.put(par, JsonPath.read(rec.toString(), "$."+query.varPathMap.get(par)+"[*]").toString());
	      }
	      System.out.println(d);
	      ds.add(d);
	  }
	  
	 //to catch single record document
	 }catch(java.lang.ClassCastException e){
	      org.ohnlp.cidb.core.Document d=new org.ohnlp.cidb.core.Document();
	      Object rec=JsonPath.read(jO.toString(), query.getRecXPath());
	      for(String par:query.varPathMap.keySet()){
		  logger.trace("par:"+par+"->"+query.varPathMap.get(par)+":"+JsonPath.read(rec.toString(), "$."+query.varPathMap.get(par)+"[*]").toString());
		  d.put(par, JsonPath.read(rec.toString(), "$."+query.varPathMap.get(par)+"[*]").toString());
	      }
	      ds.add(d);
	  }
	
	} catch (JSONException e) {
	    logger.error("",e);
	    throw new CIDBException(e.getMessage());
	}
	
	return ds;
    }
    
    
    
    public ParValMap getParValMap() throws CIDBException {
	this.responseText=processRequest();
	 if( this.responseText.toLowerCase().indexOf("faultcode")>-1)
		throw new CIDBException("Fault response in the SOAP call");
	    
	ParValMap hm= new ParValMap();
	org.json.JSONObject jO;
	try {
	    jO = XML.toJSONObject(this.responseText);
	    Object jO1= JsonPath.read(jO.toString(),this.query.recXPath);
	    System.out.println(jO1);
	    if(jO1==null) throw new CIDBException("more than one record in this Webservice call: Check record locator path");
	    for(String par:query.varPathMap.keySet()){
		String val=JsonPath.read(jO1.toString(), "$."+query.varPathMap.get(par)+"[*]").toString();
		logger.trace("<"+par+">:<"+val+">"+" "+val.indexOf("http"));
		if(val.indexOf("http")<0) hm.put(par, val);//skip populating non matched
	    }
	    
	 } catch (JSONException e) {
	    logger.error("e",e);
	    throw new CIDBException(e.getMessage());
	}
	return hm;
    }

    @Override
    public org.ohnlp.cidb.core.Document getDocument(String label, Session session) throws CIDBException {
	org.ohnlp.cidb.core.Document d= org.ohnlp.cidb.core.Document.createLabelledDoc(label,session);
	d.putAll(getParValMap());
	return d;
	
    }
    
    
    

    
	 
	
	
    }
    

   

