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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */

//key should be raw form and value the normalized form
public class Dictionary extends HashMap<String,String> {
   static Logger logger= LoggerFactory.getLogger(Dictionary.class);
   private String label;
   private Session session;
   
    public Dictionary(String label,Session session){
	this.session=session;
	Dictionary ds=null;
	session.putObject(this.getClass(),label, this);
	this.label=label;
    }
    
    /*
     * read dictionary from file, and field and value numbers specificy the column numbers in the file (by default they are 1 and 2 respecively) 
     */
    public static Dictionary GetInstanceFromCsvFile(String label,String fileName,Session session,int keyfield,int valuefield) throws CIDBException{
	Dictionary dict=new Dictionary(label,session);
	
	try{
	    logger.trace("reading file:"+ session.getTmpDir().getAbsolutePath()+"/"+fileName);
	    String dicText=Utils.readFile(session.getTmpDir().getAbsolutePath()+"/"+fileName);
	    String docTextWithoutComments="";
	    if(dicText!=null && dicText.indexOf("\n")>-1){
		for(String line:dicText.split("\n")){
		    int x=line.indexOf("//");
		    if(x>-1){line=line.substring(0,x);}
		    if (line.length()>0) docTextWithoutComments=docTextWithoutComments+line+"\n";
		}
	    }
	    logger.trace("processing:"+ docTextWithoutComments);
	CSVReader reader = new CSVReader(new StringReader(docTextWithoutComments));
	String[] line;
	while ((line = reader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	     //System.out.println(nextLine[0] + nextLine[1] + "etc...");
	    String key=line[keyfield];
	    String value=line[valuefield];
	    dict.put(key,value);
	    logger.trace("added to dict:<"+key+">:<"+value+">");
	}
	    
	if(reader!=null)reader.close();
	}catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IOException e){
	    throw new CIDBException(e);
	}
	return dict;
    }
    
    public String toString(){
	return "label:"+this.label
		+super.toString();
    }
    
       
    //get comma separated list of all normalized forms found in the string
    public  String getValuesOfKeyNormMatches(String s2){
	ArrayList<String> arr=getArrValuesOfKeyNormMatches(s2);
	if(arr.size()==0) return "empty";
	return arr.toString();
   }
    
    public  ArrayList<String> getArrValuesOfKeyNormMatches(String s2){
	ArrayList<String> arr=new ArrayList<String>();
	s2=s2.toLowerCase().replaceAll("\\W", "");
	for(String s1: this.keySet()){
	    	String key=s1;
	       s1=s1.toLowerCase().replaceAll("\\W", "");
	      // if(s1.equals(s2)) arr.add(this.get(key));\
	       if(s1.equals(s2)) arr.add(this.get(key));
	}
	return arr;
   } 
 
    //Is the input String mapped by the dictionary to a normalized form input s2
    public  boolean doesInputContainMapToGivenNorm(String inputText,String normText){
	return getArrValuesOfKeyNormMatches(inputText).contains(normText);
   } 
   
    
    //normalized  match
    public  boolean keyNormMatches(String s2){
	s2=s2.toLowerCase().replaceAll("\\W", "");
	for(String s1: this.keySet()){
	       s1=s1.toLowerCase().replaceAll("\\W", "");
	       if(s1.equals(s2)) return true;
	}       
	       return false;
   }

    //normalized  match
    //Does the string equal a norm form in dictionary
    public  boolean valueNormMatches(String s2){
	s2=s2.toLowerCase().replaceAll("\\W", "");
	for(String key: this.keySet()){
	       String s1=this.get(key).toLowerCase().replaceAll("\\W", "");
	       if(s1.equals(s2)) return true;
	}       
	return false;
   }
    //exact match
    public  boolean keyMatches(String s2){
	for(String s1: this.keySet()){
	       if(s1.equals(s2)) return true;
	}       
	return false;
   }
    //exact match
    public  boolean valueMatches(String s2){
	for(String key: this.keySet()){
	       String s1=this.get(key);
	       if(s1.equals(s2)) return true;
	}       
	       return false;
   }

    
}
