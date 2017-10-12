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

package org.ohnlp.cidb.core;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.ohnlp.cidb.core.Definition;
import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.LabelHashMap;

import org.ohnlp.cidb.core.ParValMap;
import org.ohnlp.cidb.exception.ConceptNotFoundException;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.io.SOAPCall;
import org.ohnlp.cidb.io.Sql;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.regex.*;


/** class for defining global/session constants
 * each AE appends info to session using session.appendHash(this.getClass().getSimpleName(), parVal);
 * @author Kavishwar Wagholikar (waghsk)
 */
public class Session{
	static Logger logger = LoggerFactory.getLogger(Session.class);
	public static Session singleSession;// only one session is allowed

	Properties props ;
	GregorianCalendar EndTime=new GregorianCalendar();
	String id=null;
	private File tmpDir=null;

	//For reuse across reporttypes.Exceptional case.
	GregorianCalendar PatientDob=null;

	private LabelHashMap<String,Object> objectDb=null;
	private LabelHashMap<String,Object> definitionDb=null;
	private Hashtable<String,String> logDb=null;
	private Hashtable<String,ArrayList<String>> computedFromDb=null;
	public Stack<String> objectLabelStack;//for getObject call
	public Stack<String> reasonStack;
	public String reasonGraph;

	//foldername in knowledgebase from where the knowledgebase files will be read
	private String KBFolderName;

	public Session(String id,String kbFolder) throws CIDBException  {
		try{
			setId(id);
			setKBFolderName(kbFolder);
			initialize();
			props = new Properties();
			try {
				props=Utils.loadProperties(this.getKBFolderName()+"/properties.txt");
				//System.setProperties(props); 
			} catch (IOException e) {
				logger.info("Using default properties as properties file not found in "+this.getKBFolderName());
				// props=Utils.loadProperies("nlpcds/defaultSessionProperties.txt");
				props.setProperty("SoapCallTimeOut","40000");//#Maximum milli seconds to wait for webservice call to complete
				props.setProperty("MaxSoapCalls","60");//#Maximum call for all webservices in a session
				props.setProperty("timeIntervalInYears","100");//# Session default time interval for fetching backlog of reports
			}
			props.put("<START_NODE>","1");
			EndTime=new GregorianCalendar();
			if(singleSession!=null) {
				logger.trace("cleaning up old session:"+singleSession);
				singleSession.dispose();
			}

			singleSession=this;

			//copy all relavant files to session tmp folder
			if(this.GetProp("SESSION_PATHS")!=null){
				String toDir=this.getTmpDir().getAbsolutePath();

				for(String fromPath:this.GetProp("SESSION_PATHS").split(",")){
					String fromDir=fromPath;
					logger.trace("copying files from <"+fromPath+"> to session tmp Dir");
					Utils.copyDirectory(fromDir,toDir);
				}
				logger.trace("session tmp Dir is <"+toDir+">");
			}



		}catch(Exception e){
			logger.error("fatal",e);
			throw new CIDBException(e.getMessage());
		}
		logger.trace("created session"+this);



	}

	private void initialize(){

		this.objectDb=new LabelHashMap<String,Object>();//parameter value Hash
		this.definitionDb=new LabelHashMap<String,Object>();//
		this.logDb=new Hashtable<String,String>();
		this.computedFromDb=new Hashtable<String,ArrayList<String>>();
		this.objectLabelStack=new Stack<String>();
		this.reasonStack=new Stack<String>();
		this.reasonGraph="";
		SOAPCall.resetCount();

	}

	/*
	 * Storing objects of any particular class required for the session
	 */
	public void putObject(Class inClass, String objectLabel, Object obj ){
		if(!this.objectDb.containsKey(inClass.getCanonicalName())) {
			objectDb.put(inClass.getCanonicalName(), new LabelHashMap<String,Object>());
		}
		LabelHashMap<String,Object> objHashMap=(LabelHashMap<String, Object>) this.objectDb.get(inClass.getCanonicalName());
		//prohibit rewrite of object
		if(objHashMap.containsKey(objectLabel)){
			throw new IllegalArgumentException("session already contains object of type:"+inClass.getCanonicalName()+" with label :"+objectLabel);
		}
		//prohibit insertion of null objects
		if(obj==null) throw new IllegalArgumentException("insertion of null object it prohibited. object of type:"+inClass.getCanonicalName()+" with label"+objectLabel);
		objHashMap.put( objectLabel,  obj);

	}

	public Object getObject(Class inClass, String objectLabel) throws ConceptNotFoundException{
		if(objectLabelStack.contains(objectLabel))
			throw new IllegalArgumentException("Infinite loop as "+objectLabel+" is already there on stack");
		this.objectLabelStack.push(objectLabel);

		LabelHashMap<Class,Object> objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());

		//search for definition and initialize it before throwing exception
		if (objHashMap==null|| objHashMap.get( objectLabel)==null) {
			try {
				Definition def=this.getDefinition(inClass,objectLabel);
				logger.trace("def:"+def);
				//logger.trace("session:"+def.getSession());
				if (def!=null) {
					logger.trace("Executing:"+def);
					def.define(def.getSession());
					objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());
					if (objHashMap.get( objectLabel)==null) throw new IllegalStateException("Object not sucessfully defined in definition:"+objectLabel);
					logger.trace("session:"+def.getSession());
				}

			} catch (Exception e)    {
				logger.error("fatal",e);
				throw new IllegalStateException(e);}

		}



		objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());
		if (objHashMap==null|| objHashMap.get( objectLabel)==null) {
			ConceptNotFoundException e=new ConceptNotFoundException(objectLabel);
			e.setConcept(objectLabel);
			e.setConceptClass(inClass);
			this.objectLabelStack.pop();
			throw e;
		}
		else {
			this.objectLabelStack.pop();
			return  objHashMap.get( objectLabel);
		}
	}
	
	public Object getObject(Class inClass, String server, String db, String objectLabel) throws ConceptNotFoundException{
		if(objectLabelStack.contains(objectLabel)) throw new IllegalArgumentException("Infinite loop as "+objectLabel+" is already there on stack");
		this.objectLabelStack.push(objectLabel);

		LabelHashMap<Class,Object> objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());

		//search for definition and initialize it before throwing exception
		if (objHashMap==null|| objHashMap.get( objectLabel)==null) {
			try {
				Definition def=this.getDefinition(inClass,objectLabel);
				logger.trace("def:"+def);
				//logger.trace("session:"+def.getSession());
				if (def!=null) {
					logger.trace("Executing:"+def);
					def.define(def.getSession());
					objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());
					if (objHashMap.get( objectLabel)==null) throw new IllegalStateException("Object not sucessfully defined in definition:"+objectLabel);
					logger.trace("session:"+def.getSession());
				}

			} catch (Exception e)    {
				logger.error("fatal",e);
				throw new IllegalStateException(e);}

		}



		objHashMap=(LabelHashMap<Class, Object>) this.objectDb.get(inClass.getCanonicalName());
		if (objHashMap==null|| objHashMap.get( objectLabel)==null) {
			ConceptNotFoundException e=new ConceptNotFoundException(objectLabel);
			e.setConcept(objectLabel);
			e.setConceptClass(inClass);
			this.objectLabelStack.pop();
			throw e;
		}
		else {
			this.objectLabelStack.pop();
			return  objHashMap.get( objectLabel);
		}
	}

	public void putDefinition(Class inClass, String defLabel, Definition def ){
		if(!this.definitionDb.containsKey(inClass.getCanonicalName())){
			definitionDb.put(inClass.getCanonicalName(), new LabelHashMap<String,Definition>());
		}
		LabelHashMap<String, Definition> defHashMap=(LabelHashMap<String, Definition>) this.definitionDb.get(inClass.getCanonicalName());
		//prohibit rewrite of object
		if(defHashMap.containsKey(defLabel)){
			throw new IllegalArgumentException("session already contains object of type:"+inClass.getCanonicalName()+" with label"+defLabel);
		}
		//prohibit insertion of null definitions
		if(def==null) throw new IllegalArgumentException("insertion of null object it prohibited. object of type:"+inClass.getCanonicalName()+" with label"+defLabel);
		defHashMap.put( defLabel,  def);
	}

	public Definition getDefinition(Class inClass, String defLabel) {
		LabelHashMap<Class,Definition> defHashMap=(LabelHashMap<Class, Definition>) this.definitionDb.get(inClass.getCanonicalName());

		//logger.trace("defDb:"+this.definitionDb);
		if (defHashMap==null|| defHashMap.get( defLabel)==null) {
			return null;
		}
		//logger.trace("defHM:"+defHashMap);
		return defHashMap.get(defLabel);
	}

	public Object getObjectNoException(Class inClass, String objectLabel){
		try {
			return getObject(inClass,objectLabel);
		} catch (ConceptNotFoundException e) {
			return null;
		}

	}

	public void setEndTime(int year, int month, int date){
		EndTime=new GregorianCalendar(year,month-1,date,23,59,59);
		logger.info("Set End Date:"+Utils.GregorianCalendarToString(EndTime));
	}

	public void setEndTime(GregorianCalendar time){
		EndTime=(GregorianCalendar) time.clone();
		logger.info("Set End Date:"+Utils.GregorianCalendarToString(EndTime));
	}

	public GregorianCalendar getEndTime(){
		return EndTime;
	}

	public GregorianCalendar getStartTime(){
		GregorianCalendar startTime=(GregorianCalendar)EndTime.clone();
		String ti=props.getProperty("timeIntervalInYears");
		if(ti==null) ti="2";
		startTime.add(Calendar.YEAR,-Integer.parseInt(ti));
		return startTime;
	}

	public GregorianCalendar getPatientDob() {
		return PatientDob;
	}
	public void setPatientDob(GregorianCalendar patientDob) {
		logger.trace("set Patient Dob"+Utils.GregorianCalendarToString(this.PatientDob));
		PatientDob = patientDob;
	}

	/*public Double getAgeAt(GregorianCalendar timeAt) throws DataFetchException {
	    	if (getPatientDob()==null){
	    	    DataFetchException e=new DataFetchException("accessed Session PatientDob, before setting it; Call registration service to set it, beforemkaing this call ");
   	   	    logger.fatal(e);
	    	    throw e;
	    	}
		Long diff=(timeAt.getTimeInMillis()-PatientDob.getTimeInMillis());
		Double age=(diff.doubleValue())/(1000.0*60.0*60.0*24.0*365.242);
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		age= Double.valueOf(twoDForm.format(age));

		logger.info("Age in year: "+(timeAt).get(Calendar.YEAR)+" is " +age);
		return age;
	}*/
	public String getId() {
		if(id==null){
			logger.error("fatal the session id is null");

		}
		while(id.length()<8)id="0"+"id";
		return id;
	}
	public void setId(String id) {
		while(id.length() <8){
			id="0"+id;
		}
		this.id = id;
	}	

	public String toString(){
		String str="";
		str+="\nid:"+getId();
		str+="\nKbFolder:"+getKBFolderName();
		str+="\nstartTime:"+Utils.GregorianCalendarToString(this.getStartTime());
		str+="\nendTime:"+Utils.GregorianCalendarToString(this.getEndTime());
		str+="\nObjectdb"+this.objectDb;
		str+="\nDefdb"+this.definitionDb;
		str+="\nProp:"+this.props;
		str+="\nrsnGrp:"+this.reasonGraph;
		str+="\n";

		return str;
	}

	public String getLog(String str) {
		return this.logDb.get(str);
	}
	public String getLogAll() {
		String msg="";
		ArrayList arr=Collections.list(logDb.keys());
		Collections.sort(arr);
		for (Enumeration e = Collections.enumeration( arr) ; e.hasMoreElements() ;) {
			String k=(String)e.nextElement();
			//msg=msg+"\n----------------\n"+k+":\n"+logDb.get(k)+"\n";
			msg=msg+"|"+k+":"+logDb.get(k);
		}		
		return msg;
	}
	public void putLog(String key, String value) {
		this.logDb.put(key, value);
	}
	public void appendLog(String key, String value) {
		String previous=getLog(key);
		if(previous==null) previous="";
		putLog(key,previous +value);
	}

	public String getVersion(){
		return "";//version+" build:"+svnversion;
	}	

	//counts to end time
	public  Double timeSince(GregorianCalendar input){
		Long diff=getEndTime().getTimeInMillis()-input.getTimeInMillis();
		//System.out.println("let:"+Global.GregorianCalendarToString(lowEffectiveTime));
		return diff.doubleValue() /(1000.0*60.0*60.0*24.0*365.0);
	} 
	public Double timeSince(String dateTime){
		
		GregorianCalendar gc=Utils.getGregorianCalendarFromString(dateTime);
		return timeSince(gc);
	} 

	public String getKBFolderName() {
		return KBFolderName;
	}
	public Session setKBFolderName(String kBFolderName) {
		KBFolderName = kBFolderName;
		return this;
	}


	public void drlInfoLog(String ruleName,String msg){
		LoggerFactory.getLogger("drl."+ruleName).info("["+ruleName+"] - "+msg);
	}

	public void drlErrorLog(String ruleName,String msg, Exception e){
		//LoggerFactory.getLogger("drl."+ruleName).error("["+ruleName+"] - "+msg,e);
		LoggerFactory.getLogger("drl."+ruleName).error("["+ruleName+"] - "+msg,e);
	}

	public String GetProp(String propName){
		if (((String) this.props.get(propName))!=null)
			return ((String) this.props.get(propName)).replaceAll("\\s+$","");
		else return null;
	}

	public Properties getProps() {
		return props;
	}

	public String substituteProps(String key){
		return Utils.substituteProps(this.props, key);
	}

	public String getConceptValue(Node n) throws ConceptNotFoundException, CIDBException{
		String val=null;
		return getConceptValue(n.getPrefix(),n.getSuffix()); 
	}

	static public String getConceptPrefix(String s) throws ConceptNotFoundException, CIDBException{
		return s.split("\\.")[0];
	}



	/**
	 * @param conceptName
	 * @return
	 * @throws ConceptNotFoundException
	 * @throws CIDBException
	 */
	public String get(String conceptName) throws ConceptNotFoundException, CIDBException{
		String value=null;
		logger.trace("seeking:<"+conceptName+">");
		if(this.reasonStack.contains(conceptName)) throw new IllegalArgumentException("Infinite Loop:as "+ conceptName +"is already on get stack");
		this.reasonStack.push(conceptName);
		Node n=new Node("temp");n.setConcept(conceptName);
		try{
			value=(String) getConceptValue(n);
		}catch (ConceptNotFoundException e){
			reasonStack.pop();
			throw e;
		}
		reasonStack.pop();
		if(!reasonStack.empty()){  addComputedFrom(reasonStack.lastElement(), conceptName);
		//logger.trace("seeking added:<"+reasonStack.lastElement()+">--<"+conceptName+">");
		}
		else{
			//logger.trace("seeking adding to Graph:<"+getComputedFromString(conceptName,0));
			reasonGraph+=getComputedFromString(conceptName,0);
		}
		return value;
	}

	private String getConceptValue(String prefix,String suffix) throws ConceptNotFoundException, CIDBException{
		String val=null;

		Document d = (Document) this.getObject(Document.class,prefix);
		if(d!=null) val=(String) d.get(suffix);
		if(val==null){ 
			ConceptNotFoundException e=new ConceptNotFoundException(prefix+"."+suffix);
			e.setConcept(prefix+"."+suffix);
			throw e;
		}
		return val;
	}


	public Document getDocument(String label) throws ConceptNotFoundException {
		return  (Document) this.getObject(Document.class,label);
	}

	public boolean equals(String conceptLabel,String value) throws ConceptNotFoundException, CIDBException{
		return get(conceptLabel).equals(value);
	}

	public boolean isDefined(String conceptLabel) throws CIDBException, ConceptNotFoundException{
		Node n=new Node("temp");n.setConcept(conceptLabel);
		return getDocument(n.getPrefix()).isDefined(n.getSuffix());
	}

	public boolean isReportFound(String conceptLabel) throws CIDBException, ConceptNotFoundException{
		Node n=new Node("temp");n.setConcept(conceptLabel);
		return getDocument(n.getPrefix()).isReportFound();
	}


	private void createTmpDir() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException{
		if(tmpDir!=null)  FileUtils.deleteDirectory(tmpDir);
		String rndStr=((Integer) new Random().nextInt(100000)).toString();
		File f=Utils.createTempDirectory("sessionTempDrl"+rndStr);
		// File f=new File("C:/sessionTempDrl"+rndStr);
		tmpDir=f;
	}

	public File getTmpDir() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException{
		if(tmpDir==null) createTmpDir();
		return tmpDir;
	} 

	public void dispose() throws CIDBException{
		try {
			if(tmpDir!=null){
				//FileUtils.cleanDirectory(tmpDir);
				FileUtils.deleteDirectory(this.tmpDir); 
			}

			//Sql.closeAllStatements();
			Sql.closeAllConnections(); 
		} catch (IOException | SQLException  e) {
			logger.error("fatal",e);
			throw new CIDBException(e);
		}
	}

	public static void cleanUpStatic() throws CIDBException, SQLException{
		Sql.closeAllStaticPools();
	}	

	public Session addProperty(String key,String value){
		this.props.setProperty(key, value);
		return this;
	}

	public String getComputedFromString(String conceptName,int level){
		if(level>5) throw new IllegalStateException("too much recursion");
		//indentation
		String indent="";
		for(int x=0;x<=level;x++) indent+="\t";
		String text="\n"+indent+"{"+conceptName;

		String value;
		Node n=new Node("temp");n.setConcept(conceptName);
		try{
			value=(String) getConceptValue(n);
		}catch (ConceptNotFoundException | CIDBException e){
			value="--";
		}
		text+="="+value;

		//text+="("+level+")";
		//recursion for children
		if(this.computedFromDb.get(conceptName)!=null){
			ArrayList<String> arr=this.computedFromDb.get(conceptName);
			//text+=arr;
			int level2=level+1;
			for(String s:arr){
				//text+="\n"+indent+getComputedFromString(s,(level2));
				text+=getComputedFromString(s,(level2));
			}
		}
		return text+"}";
	}

	public void addComputedFrom(String currentDoc,String computedFromDoc) {
		if(computedFromDb.get(currentDoc)==null)computedFromDb.put(currentDoc,new ArrayList<String>());
		ArrayList<String> arr=computedFromDb.get(currentDoc);
		arr.add(computedFromDoc);
		logger.trace("seeking after adding:"+currentDoc+"="+arr);
	}

	public String getReasonGraph(){
		return this.reasonGraph;
	}



	public void copyTempDir(){
		try {
			if(this.GetProp("TEMP_DIR")==null){
				//throw new IllegalStateException("TEMP_DIR is not defined in properties file");
			}else{
				FileUtils.copyDirectory(this.getTmpDir(), new File(this.GetProp("TEMP_DIR")));
			}
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
