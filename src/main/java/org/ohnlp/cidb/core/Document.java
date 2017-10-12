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
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ohnlp.cidb.core.Infer;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.ConceptNotFoundException;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;














import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/*
 * HashMap of key value pairs that cannot be overwritten
 * unless a document has some information, all calls to get will result in report not found.
 * time has to be defined for using the document
 * @author Kavishwar Wagholikar (waghsk)
 */


public class Document implements Comparable<Document> {
	static Logger logger = LoggerFactory.getLogger(Document.class);
	static private String dynamicTemplate=null;


	//stores dynamic code/or compiled object for parameters that need to be dynamically resolved;Hence expands the document
	private HashMap<String,Object> expansionhm;
	private HashMap<String, Infer> expansionFilterhm;
	private String label=null;
	private ParValMap db=null;
	private GregorianCalendar time;
	private Session session=null;
	private boolean reportFound;


	public Document(){
		initDocument();	
	}

	public void initDocument(){
		db=new ParValMap();
		expansionhm=new HashMap<String,Object>();
		expansionFilterhm=new HashMap<String,Infer>();
		reportFound=false;// for reports with no values
	}

	public static Document createLabelledDoc(String label,Session session){
		Document d=new Document();
		d.label=label;
		d.session=session;
		session.putObject(Document.class,label, d);
		return d;
	}
	/*
	 * fetch existing document
	 */
	static public Document getDocument(String label,Session session){
		Document d= null;
		try{d=(Document) session.getObject(Document.class,label);}
		catch(ConceptNotFoundException e){
			throw new IllegalArgumentException("Document with label "+label+" does not exist in the Session");}
		return d;

	}



	public void addToExpansionMap(String par, String codeText){
		expansionhm.put(par,codeText);
	}

	public void addToExpansionMap( Infer f) {
		expansionFilterhm.put(f.getName(),f);
		this.reportFound=true;
	}
	/*
    public void addToExpansionMap(String par, Filter f) {
	expansionFilterhm.put(par,f);
 	}
	 */


	/*The class should be 
	i) named dynamic.A, should have
	ii) should have function with signature "map<String,String> execute(String input)"
	 */    public void compileAndExecuteDynamicCode(String expansion_snippet) throws CIDBException{

		 if(this.dynamicTemplate==null) dynamicTemplate= Utils.readFile("data/templates/DynamicCompileClass.txt");

		 if( expansion_snippet==null) throw new IllegalArgumentException("expansion snippet is null");
		 // System.out.println("expansion_sinppet:"+expansion_snippet+"\n dynamic template:"+dynamicTemplate);
		 //dynamicTemplate=dynamicTemplate.substring(0, dynamicTemplate.length()-6);
		 //String dynamicCode=dynamicTemplate+expansion_snippet +"}}";
		 String dynamicCode=dynamicTemplate.replace("<dynamic_code>",expansion_snippet);
		 //System.out.println("dynamicCode"+dynamicCode);

		 File temp = null;
		 try{

			 temp = File.createTempFile("pattern", ".suffix");
			 PrintWriter out=new PrintWriter(temp);
			 //out.print(expansionhm.get(key));out.close();
			 out.print(dynamicCode);
			 out.close();

			 //System.out.println("filePath:"+temp.getPath().toString());
			 String className = "p.A";
			 SimpleCompiler compiler =   new SimpleCompiler(temp.getPath().toString());

			 ClassLoader loader = compiler.getClassLoader();
			 Class compClass = loader.loadClass(className);
			 Object instance = compClass.newInstance();


			 Class[] types = new Class[] {Document.class,Session.class};
			 Method fooMethod;

			 fooMethod =  instance.getClass().getMethod("execute",types);


			 //ParValMap tmpH = null;

			 fooMethod.invoke(instance,this,session);
			 temp.deleteOnExit();


			 logger.trace("retrived by dynamic execution:"+this);
			 //db.putAll(this);

		 } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | CompileException | IOException | ClassNotFoundException  e){
			 logger.error("for doc:"+this,e);
			 throw new CIDBException(e.getMessage());

		 }
		 if (temp!=null) temp.delete();
	 }

	 public GregorianCalendar getGregorianCalendarTime() throws CIDBException  {
		 if(time==null)  time=Utils.fromSqlDateTime(this.get("time"));
		 return time;
	 }




	 public String get(String key) throws CIDBException  {
		 logger.trace("get:"+key);
		 String val=null;
		 logger.trace("This:"+this);
		 //if parameter is null and either of the expansion maps are defined
		 if(isReportFound() && db.get(key)==null && (expansionFilterhm.get(key)!=null || expansionhm.get(key)!=null)){
			 //)){


			 //try expansion maps first (check if object is filter first)
			 if (expansionFilterhm.get(key)!=null){
				 logger.trace("running expansion Filter for "+key);
				 try {
					 Infer f=((Infer)expansionFilterhm.get(key));
					 put(f.getName(),f.run(this, this.getSession()));
				 } catch (Exception e) {
					 logger.error("fatal",e);
					 throw new CIDBException(e);
				 }
			 }else if (expansionhm.get(key)!=null){
				 logger.trace("running expansion dynamic for "+key);
				 compileAndExecuteDynamicCode((String)expansionhm.get(key));
			 }
			 if(db.get(key)==null) throw new CIDBException("The expansion map did not fetch the parameter:"+key+"\n for doc"+this);

		 }


		 if(key.equals("isReportFound")){return reportFound?"yes":"no";}

		 if(reportFound==false)return "reportNotFound";// returns report not found for all parameters
		 if(key.equals("timeMs")&& this.time!=null) return Long.toString(this.time.getTimeInMillis());
		 else if(key.equals("timeSince")) return getTimeSince();
		 else if(key.equals("sinceYears")) return sinceYears(this.session).toString();
		 else if(key.equals("sinceMonths")) return sinceMonths(this.session).toString();
		 else if(key.equals("sinceDays")) return sinceDays(this.session).toString();
		 else if(key.equals("sinceHours")) return sinceHours(this.session).toString();
		 else if(key.equals("sinceSeconds")) return sinceSeconds(this.session).toString();
		 else if(key.equals("time") && this.time==null) return "notDefined";
		 else val=db.get(key);

		 if(val==null) val="notDefined";
		 return val;
	 }

	 private String getTimeSince() {
		 return Utils.timeSince(this.getTime(), session).toString();
	 }

	 public void put(String key,String val)  {
		 if(key==null) throw new IllegalArgumentException("key is null");
		 if(db==null) throw new IllegalArgumentException("db is null");
		 if(db.containsKey(key)) throw new IllegalArgumentException("Attempting to overwrite "+key+" which is forbidden"); 
		 //if(!key.equals(key.toLowerCase())) throw new DataFetchException("key name has to be a lower case alphabet");
		 if(key.equals("time") && !val.matches("[\\D]*([0-9]{4})[-|/]([0-9]{2})[-|/]([0-9]{2})[\\D]*(\\d{2}):(\\d{2}):*(\\d{2})*[\\W|\\S]*")) throw new IllegalArgumentException("date has to be in the format YYYY-MM-DD HH:MM:SS:<"+val+">");
		 if(key.equals("time") ) {
			 this.time=Utils.getGregorianCalendarFromString(val);
			 val=Utils.GregorianCalendarToODBCString(this.time);
			 logger.trace(val+" converted to time"+Utils.GregorianCalendarToString(this.time));	
		 }

		 if (val==null || val.equals("")) val="notDefined";
		 reportFound=true;
		 this.db.put(key,val);


	 }

	 public int compareTo(Document d) {
		 //System.out.println("TIME="+d.getTime()+":"+this.getTime());
		 //System.out.println("TIME="+this.getTime());
		 if(d.getTime().equals(this.getTime())) return 0;
		 return d.getTime().after(this.getTime())?1:-1;

	 }

	 public void putAll(ParValMap mp) throws CIDBException{
		 for(String k:mp.keySet()) this.put(k,mp.get(k));
	 }

	 public void putAll(HashMap<String,String> mp) throws CIDBException{
		 for(String k:mp.keySet()) this.put(k,mp.get(k));
	 }


	 public String toString(){
		 return ((this.label!=null)?this.label:"no-label")+","
				 +alphaHash(db)+"\tE:"+expansionhm.toString()+ "," 
				 +"\tF:"+expansionFilterhm.toString()+ ","
				 + (this.time==null?"nulltime":Utils.GregorianCalendarToString(this.getTime()))+","
				 +((this.reportFound)?"report found":"");
	 }

	 private String alphaHash(ParValMap db2) {
		 String str="";
		 ArrayList arr=new ArrayList<String>();
		 for(String k:db2.keySet()) arr.add(k);
		 Collections.sort(arr);
		 for (Enumeration e = Collections.enumeration( arr) ; e.hasMoreElements() ;) {
			 String k=(String)e.nextElement();
			 str+=k+"="+db2.get(k)+", ";
		 }    
		 return str;
	 }

	 public String dbToString(){
		 ArrayList<String> arr=new ArrayList<String>();
		 for(String k:db.keySet()){
			 arr.add(k+"="+db.get(k));
		 }
		 return arr.toString();
	 }


	 public String toShortString(){
		 return db.toString() 
				 +((this.reportFound)?"report found":"");
	 }

	 public Session getSession() {
		 if(session==null) throw new IllegalStateException("session is null for this document"+this);
		 return session;
	 }

	 public Set<String> keySet(){
		 return this.db.keySet();
	 }

	 public GregorianCalendar getTime() {
		 if(time==null) throw new IllegalStateException("time  has not been initialized for this document:"+this);
		 return time;
	 }

	 public void setSession(Session session) {
		 this.session=session;

	 }

	 //should be more appropriately called copyFrom
	 public Document cloneWithOutLabel(Document d) throws CIDBException{
		 initDocument();
		 for(String key:d.db.keySet()) put(key, d.db.get(key));
		 for(String key:d.expansionhm.keySet())  addToExpansionMap(key, (String)d.expansionhm.get(key));
		 for(String key:d.expansionFilterhm.keySet())  addToExpansionMap((Infer)d.expansionFilterhm.get(key));
		 this.time=d.time;
		 this.reportFound=d.reportFound;
		 //this.label=d.label;
		 return this;
	 }

	 public boolean isReportFound(){
		 return this.reportFound;
	 }


	 /*
	  * Levels: 
i)report not found
ii) report Found but not defined
iii)report Found and defined
	  */
	 public boolean isDefined(String key) throws CIDBException{
		 return (this.get(key).equals("notDefined")|| (!this.reportFound))?false:true; 
	 }

	 /*
	  * returns time elapsed from document time to session end time
	  */
	 public Double sinceYears(Session s){
		 if(session==null)throw new IllegalArgumentException("session for document has not been defined");
		 return Utils.timeSince(this.getTime(), s);
	 }

	 public Double sinceMonths(Session s){
		 return sinceYears(s)*12.0;

	 }

	 public Double sinceDays(Session s){
		 return sinceYears(s)*365.0;
	 }

	 public Double sinceHours(Session s){
		 return sinceYears(s)*365.0*24.0;
	 }

	 public Double sinceMinutes(Session s){
		 return sinceYears(s)*365.0*24.0*60.0;
	 }

	 public Double sinceSeconds(Session s){
		 return sinceYears(s)*365.0*24.0*60.0*60.0;
	 }

	 public boolean after(Document d2) throws CIDBException{
		 return ((GregorianCalendar)this.getGregorianCalendarTime()).after((GregorianCalendar)d2.getGregorianCalendarTime());
	 }

	 public String getLabel() {
		 return this.label;
	 }


}
