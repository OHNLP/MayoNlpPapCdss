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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.ohnlp.cidb.core.Infer;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.ConceptNotFoundException;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * set of documents
 * late-binding: application of infers
 * implements selection of doc s within session begin and end time implicitly
 * @author Kavishwar Wagholikar (waghsk)
 */
public class DocSet extends ArrayList<Document>{
   
    private Session session;
    Logger logger= LoggerFactory.getLogger(DocSet.class);
    
    //stores dynamic code for parameters that need to be dynamically resolved
    //Hence expands the document
    String label;
    ArrayList<String> inferArr;
    ArrayList<Infer> infer1Arr;
    
    public void addInfer(Infer f){
	infer1Arr.add(f);
    }
    
    public DocSet(Session session,String label){
	this.session=session;
	DocSet ds=null;
	session.putObject(this.getClass(),label, this);
	this.label=label;
	inferArr=new ArrayList<String>();
	infer1Arr=new ArrayList<Infer>();
    }
    
    private DocSet() {
	inferArr=new ArrayList<String>();
	infer1Arr=new ArrayList<Infer>();
    }

    static public DocSet newInstance(Session session,String label){
	DocSet ds=null;
	ds=new DocSet(session,label);
	ds.inferArr=new ArrayList<String>();
	ds.infer1Arr=new ArrayList<Infer>();
	return ds;
    }
    
    /*
     * only for use in drl where the same definition rule can be execute repeatedly till all dependencies are resolved 
     * */

    @Deprecated
    static public DocSet getInstance(Session session,String label){
	DocSet ds=null;
	
	    try {
		ds=(DocSet) session.getObject(DocSet.class, label);
		return ds;
	    } catch (ConceptNotFoundException e) {

	    }
	
	ds=new DocSet(session,label);
	ds.inferArr=new ArrayList<String>();
	ds.infer1Arr=new ArrayList<Infer>();
	return ds;
    }
    
    /*
     * get return reportNotFound default document if results is null
     * will sort but not apply infers
     */
    public Document get(int i){
	
	sort();
	if(i<0 || i>=this.size()) return new Document();//report not Found
	return super.get(i);
    }
    
    private void run_infer(Document d) throws CIDBException{
	 d.setSession(this.session);
	 for(String inferText:inferArr){
		if (inferText!=null) d.compileAndExecuteDynamicCode(inferText);
	    }
	    
	    for(Infer infer:infer1Arr){
		if (infer!=null && !d.isDefined(infer.getName()))
		    try {
			d.put(infer.getName(),infer.run(d,session));
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("fatal",e);
			e.printStackTrace();
		    }
	    }
    }
    
    /*
     * null document is an uninitialized document 
     * initiates the sort and infer operation to return Doc
     * default infer of begin and end session times
     */
    public Document getUnLabelled(int i,String par, String val) throws CIDBException {
	sort();
	Document result= new Document();
	
	//sort document
	int counter=-1;
	
	for(Document d:this){
	    d.setSession(this.session);
	    
	    //check for time constraints
	    logger.trace("processing "+d);
	    if(d.getTime().before(session.getStartTime())||
		    d.getTime().after(session.getEndTime())) continue;
	    run_infer(d);
	    
	    
	    if(!d.isDefined(par)) throw new IllegalStateException("parameter <"+par+"> is not defined in Document"+d);//check of variable is defined after running infer
	    
	    if(d.get(par).equals(val)) counter++;
	    logger.trace("counter:"+counter);
	    if(counter==i) {
		result.cloneWithOutLabel(d);
		logger.trace(":"+result);		
		return result;
	    }
	    logger.trace("not matched:"+d);
	}
	
	return result;
    }
    
    public Document get(int i,String label,Session session,String par, String val) throws CIDBException {
		Document result= Document.createLabelledDoc(label,session);
		return result.cloneWithOutLabel(getUnLabelled(i,par,val));
	}
    
    
    public void sort() {
	Collections.sort(this);
	
    }

    public String toString(){
	sort();
	String str=this.label+" has "+ this.size() +" Documents";
	str+="\nDoc----------------------------\n";
	for(Document d: this)   str+="\n"+d;
	str+="\ninfer----------------------------\n";
	for(String inferText:this.inferArr) str+="\n----------------------------\n"+inferText;
	str+="\ninfer1----------------------------\n";
	for(Infer infer:this.infer1Arr) str+="\n----------------------------\n"+infer;
	return str;
    }
    
    
    /*//equal test: this this document have a parameter meeting this test
    public boolean equaltest(String par,String val){
	return equalinfer(par,val).size()>0?true:false;// TODO optimize for single match
    }
    
   //checks if the string matches
   public DocSet equalinfer(String par,String val){
	DocSet fds=new DocSet(label+" infered by" +par+"="+val);
	for(Document d:this){
	    if (d.get(par).equals(d.get(val))) fds.add(d);
	}
	return fds;
    }
    
    //TODO
    public DocSet dateBetween(String start,String end){
	return null;
    }
    
  //TODO
    public DocSet floatBetween(String start,String end){
	return null;
    }
    */   
    
    public void setInferText(String inferText) {
        inferArr.add(inferText);
        logger.trace("added infertext"+this.inferArr);
    }

    public void setInfer(Infer infer) {
        infer1Arr.add(infer);
        logger.trace("added infer"+infer);
    }

    
    public void addToExpansionMap(String par, String codeText){
   	for(Document d:this) d.addToExpansionMap(par,codeText);
       }
   
    public void addToExpansionMap( Infer f){
   	for(Document d:this) d.addToExpansionMap(f);
       }
    
   
    public DocSet getSubset(Session session,String label,Infer infer){
	DocSet ds= newInstance( session, label);
	for(Document d: this) ds.add(d);
	ds.inferArr.addAll(this.inferArr);
	ds.infer1Arr.addAll(this.infer1Arr);
	ds.infer1Arr.add(infer);
	return ds;
    }
  
    /*
     * XXXLabel should be name of the infer
     */
    public DocSet getSubset(Session session,String label,String inferText){
  	DocSet ds= newInstance( session, label);
  	for(Document d: this) ds.add(d);
  	ds.inferArr.addAll(this.inferArr);
  	ds.inferArr.add(inferText);
  	ds.infer1Arr.addAll(this.infer1Arr);
    	return ds;
    }


       
    public static DocSet newUnLabelled() {
	DocSet ds=new DocSet();
	return ds;
    }

    
    /*
     * merges the argument docset into current docSet with the infers
     */
    public DocSet merge(DocSet ds1) throws CIDBException{
	logger.trace("before merging ds:"+this);
	for(Document d: ds1) {Document d1=new Document(); this.add(d1.cloneWithOutLabel(d));}
	for(String inferText:ds1.inferArr) this.setInferText(inferText);
	for(Infer infer:ds1.infer1Arr) this.setInfer(infer);
	logger.trace("after merging ds:"+this);
	return this;
    }

    /*
     * +/- amt of time around inputDate
     */
    public Document getAround(String docLabel,String par,String val,String inputDate,int field,int amt) throws CIDBException{
	logger.trace("inputDate:"+inputDate);
	Document d= Document.createLabelledDoc(docLabel, session);
	if(inputDate.equals("reportNotFound")) return d;
	GregorianCalendar time1=Utils.getGregorianCalendarFromString(inputDate);//input time
	time1.add(field, -amt) ;
	GregorianCalendar time2=(GregorianCalendar) time1.clone();//Bound
	time2.add(field, amt);
	
	Document indexD=this.getUnLabelled(0,  par, val);
	int count=0;
	while(indexD.isReportFound()){
	    logger.trace("c:"+count);
	    if( !indexD.getTime().before(time1)
		    && indexD.getTime().before(time2)
		) {
		d.cloneWithOutLabel(indexD);
		Double diffDays=(Utils.timeSinceInSeconds( time1,d.getGregorianCalendarTime()))/(60*60.0*24);
		d.put("timeDifference",diffDays.toString());
		return d;
	    }
	    indexD=this.getUnLabelled(++count,  par, val);
	}
	return d;
    }
    
    public Document getClosestToTime(String par,String val,String inputTimeText,int before_field,int before_amt,int after_field,int after_amt) throws CIDBException{
	logger.trace("inputTimeTxt:"+inputTimeText+"\n"+this);
	GregorianCalendar inputTime=Utils.getGregorianCalendarFromString(inputTimeText);//input time
	GregorianCalendar before_time=(GregorianCalendar) inputTime.clone();//input time
	before_time.add(before_field, -before_amt) ;
	GregorianCalendar after_time=(GregorianCalendar) inputTime.clone();//Bound
	after_time.add(after_field, after_amt);
	
	Document d= new Document();
	if(this.isEmpty()) return d;
	Document indexD=getClosestToTime(inputTime,1);
	int count=1;
	while(indexD.isReportFound()){
	    
	    logger.trace("c:"+count);
	    run_infer(indexD);
	    
	    if(  indexD.getTime().after(before_time) & indexD.getTime().before(after_time)){
		if((par==null && val==null)|| //par and val are non null args
			(par!=null && val!=null && indexD.get(par).equals(val) )){ //ignoring par and val 
		    d.cloneWithOutLabel(indexD);
		    Double diffDays=(Utils.timeSinceInSeconds( inputTime,d.getGregorianCalendarTime()))/(60*60.0*24);
		    d.put("timeDifference",diffDays.toString());
		    return d;
		}
	    }
	    
	    indexD=getClosestToTime(inputTime,++count);
	}
	return d;
    }
    
    public Document getImmediatelyAfter(String docLabel,String par,String val,String inputDate,int field,int amt) throws CIDBException{
	logger.trace("inputDate:"+inputDate);
	GregorianCalendar time1=Utils.getGregorianCalendarFromString(inputDate); //input time
	GregorianCalendar time2=(GregorianCalendar) time1.clone();//Bound
	time2.add(field, amt);
	Document d= Document.createLabelledDoc(docLabel, session);
	Document indexD=this.getUnLabelled(0,  par, val);
	int count=0;
	while(indexD.isReportFound()){
	    logger.trace("c:"+count);
	    if( !indexD.getTime().before(time1)
		    && indexD.getTime().before(time2)
		) {
		d.cloneWithOutLabel(indexD);
		Double diffDays=(Utils.timeSinceInSeconds( time1,d.getGregorianCalendarTime()))/(60*60.0*24);
		d.put("timeDifference",diffDays.toString());
		return d;
	    }
	    indexD=this.getUnLabelled(++count,  par, val);
	}
	return d;
    }
    
    /*
     * Rank is represents the proximity of the doc in time
     * The closest doc has rank 1 then the other closest has rank 2 and so on.
     */
    public Document getClosestToTime(GregorianCalendar time1,int rank){
	if(time1==null) throw new IllegalArgumentException("time cannot be null");
	if(rank<=0) throw new IllegalArgumentException("rank must be > 0");
	Document d=new Document();
	ArrayList<Document> rankArr=new ArrayList<Document>();
	Hashtable<Document,Long> hsh=new Hashtable<Document,Long>();
	//Hashtable<Document,Integer> hsh=new Hashtable<Document,Integer>();
	//init timeDiff hash
	Long t2=time1.getTimeInMillis();
	Long timeDiff;
	for(Document di:this){
	    Long t1=  di.getTime().getTimeInMillis();
	    if (t2>=t1) {timeDiff=t2-t1;} else {timeDiff=t1-t2;}
	    hsh.put(di,timeDiff);
	}

	boolean flag;
	for(Document dk:this){
	    for(Document di:this){
		if (rankArr.contains(di)) continue;
		flag=true;
		for(Document dj:this){
		    if (rankArr.contains(dj) ) {}
		    else if(hsh.get(di)>hsh.get(dj)) {
			flag=false;
		    }
		}
		if(flag==true){
		    rankArr.add(di);
		    if (rankArr.size()==rank) return di;
		}
	}
	}
	/*for(Document x:rankArr){
	   	try {
		    logger.trace("arr:"+x.get("id"));
		} catch (DataFetchException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}*/
	return d;
	
    }

    /*
     * Concat values, where index key has particular value
     */
    public List<String> concat(String key1, String indexKey, String indexKeyVal) throws CIDBException{
	ArrayList<String> arr= new ArrayList<String>();
	for(Document d:this){
	    	if(d.get(indexKey).equals(indexKeyVal))
	    	    arr.add(d.get(key1));
	}
	return arr;
    }
    
}
