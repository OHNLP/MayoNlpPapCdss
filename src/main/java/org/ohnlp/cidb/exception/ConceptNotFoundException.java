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

package org.ohnlp.cidb.exception; 
import java.io.*;

/**
 * Exception class for Data module Exceptions
 * @author Kavishwar Wagholikar (waghsk)
 * 
 */
public class ConceptNotFoundException extends Exception{

    String concept;
    private Class conceptClass;






/**
 * Default constructor - initializes instance variable to unknown
 */
public ConceptNotFoundException()
  {
    super();             // call superclass constructor
  }
  

//-----------------------------------------------
/**
 *  Constructor receives some kind of message that is saved in an instance variable.
 * @param err
 */

  public ConceptNotFoundException(String err)
  {
    super(err);     // call super class constructor
  }
  

  public String getConcept() {
      return concept;
  }


  public void setConcept(String concept) {
      if(!concept.matches("\\w+.\\w+")) throw new IllegalArgumentException("concept should match regex \\w+.\\w :"+concept);
      this.concept = concept;
  }
  
  	public String getPrefix(){
	    return this.concept.split("\\.")[0];
	}
	public String getSuffix(){
	    return this.concept.split("\\.")[1];
	}
	
	public Class getConceptClass() {
	        return conceptClass;
	    }


	public void setConceptClass(Class conceptClass) {
	    this.conceptClass = conceptClass;
	}
	
	public String toString(){
	    return super.toString()+" "+this.concept +"\n class:"+this.getConceptClass();
	 }
	
}
  
