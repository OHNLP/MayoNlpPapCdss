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

import java.util.Arrays;

import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.ConceptNotFoundException;
import org.ohnlp.cidb.exception.CIDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Helper class
 * @author Kavishwar Wagholikar (waghsk) 
 */
public class Concept {
    static Logger logger = LoggerFactory.getLogger(Concept.class);
    
    static public Document lastOf(String newLabel,String label1,String label2,Session $s) throws ConceptNotFoundException, CIDBException{
	DocSet ds=DocSet.newUnLabelled();
	$s.get(label1+".time");$s.get(label2+".time");//for reason trace
	Document d1=$s.getDocument(label1);
	Document d2=$s.getDocument(label2);
	if(d1.isDefined("time"))ds.add(d1);
   	if(d2.isDefined("time"))ds.add(d2);
   	Document d=Document.createLabelledDoc(newLabel,$s).cloneWithOutLabel(ds.get(0));
	logger.trace("temp ds:"+ds);
	logger.trace("returning d:"+d);
	return d;
    }
    
    static public Document lastOf(String newLabel,String label1,String label2,String label3,Session $s) throws ConceptNotFoundException, CIDBException{
   	DocSet ds=DocSet.newUnLabelled();
   	$s.get(label1+".time");$s.get(label2+".time");$s.get(label3+".time");//for reason trace
   	Document d1=$s.getDocument(label1);
   	Document d2=$s.getDocument(label2);
   	Document d3=$s.getDocument(label3);
   	if(d1.isDefined("time"))ds.add(d1);
   	if(d2.isDefined("time"))ds.add(d2);
   	if(d3.isDefined("time"))ds.add(d3);
   	Document d=Document.createLabelledDoc(newLabel,$s);
   	d.cloneWithOutLabel(ds.get(0));
   	logger.trace("temp ds:"+ds);
   	logger.trace("d:"+d);
   	return d;
       }
    
    public static Document lastOf(String newLabel, String[] arr, Session $s) throws ConceptNotFoundException, CIDBException {
   	DocSet ds=DocSet.newUnLabelled();
   	
   	for(String label1: Arrays.asList(arr)){
   	    $s.get(label1+".time");
   	    Document d1=$s.getDocument(label1);
   	    if(d1.isDefined("time"))ds.add(d1);
   	}
   	Document d=Document.createLabelledDoc(newLabel,$s);
   	d.cloneWithOutLabel(ds.get(0));
   	logger.trace("temp ds:"+ds);
   	logger.trace("d:"+d);
   	return d;
 	
     }
    
    /*
     * takes array of concept-value pairs, and assigns the pair with the latest time to the document
     * if time not defined for a matching concept, that concept is ignored
     */
    public static Document lastOfConceptValueArray(String newLabel, String[][] arr, Session $s) throws ConceptNotFoundException, CIDBException {
   	DocSet ds=DocSet.newUnLabelled();
   	
   	for(String[] concept: Arrays.asList(arr)){
   	    String label= concept[0];
   	    String value= concept[1];
   	    
   	    logger.trace("seeking label:"+label);
   	    $s.get($s.getConceptPrefix(label)+".time");
   	    Document d1=$s.getDocument($s.getConceptPrefix(label));
   	    if(d1.isReportFound() && d1.isDefined("time") &&  $s.equals(label, value))ds.add(d1);
   	}
   	Document d=Document.createLabelledDoc(newLabel,$s);
   	d.cloneWithOutLabel(ds.get(0));
   	logger.trace("temp ds:"+ds);
   	logger.trace("d:"+d);
   	return d;
 	
     }
    
    static public String getConceptPrefix(String conceptLabel){
	    Node n=new Node("temp");n.setConcept(conceptLabel);
	    return n.getPrefix();
    }
    
    static public String getConceptSuffix(String conceptLabel){
	    Node n=new Node("temp");n.setConcept(conceptLabel);
	    return n.getSuffix();
}
    
    
    static public Document getDocumentWithConcept(String conceptLabel,Session $s) throws ConceptNotFoundException{
	    Node n=new Node("temp");n.setConcept(conceptLabel);
	    return $s.getDocument(n.getPrefix());
    }

    static public Document getLastOfDocsMeetingConditions(String newConceptName,String trueVal,String falseVal,Session $s,String concept1, String val1, String concept2, String val2 ) throws ConceptNotFoundException, CIDBException{
	Document d= Document.createLabelledDoc(Concept.getConceptPrefix(newConceptName), $s);
	DocSet ds= DocSet.newUnLabelled();
	if($s.equals(concept1, val1)){
	    ds.add((Document) Concept.getDocumentWithConcept(concept1, $s));
	}
	if($s.equals(concept2, val2)){
	    ds.add((Document) Concept.getDocumentWithConcept(concept2, $s));
	}
	Document result=ds.get(0);
	d.put(Concept.getConceptSuffix(newConceptName),result.isReportFound()?trueVal:falseVal);
	if(result.isReportFound())d.cloneWithOutLabel(result);
	return d;
	
	
    }

 
    
}
