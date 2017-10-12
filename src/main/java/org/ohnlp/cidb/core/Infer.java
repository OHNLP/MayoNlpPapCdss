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

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;
import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * interface for performing inference operation on a document
 * @author Kavishwar Wagholikar (waghsk)
 */
public abstract class  Infer {
    static Logger logger = LoggerFactory.getLogger(Infer.class);
    protected String name;
    static private String dynamicTemplate=null;

    public Infer(String name) {
	this.name=name;
    }
    abstract public String  run(Document d,Session $s) throws Exception;
    
    public String toString(){return name;}
    
    public String getName() {
        return name;
    }
    
    static public Infer getInstance(String name, String codeText) throws CIDBException{
	 Object instance=null;

	    if(dynamicTemplate==null) dynamicTemplate= Utils.readFile("data/templates/DynamicInferenceClass.txt");
	    
	    if( codeText==null) throw new IllegalArgumentException("expansion snippet is null");
	   // System.out.println("expansion_sinppet:"+expansion_snippet+"\n dynamic template:"+dynamicTemplate);
	    //dynamicTemplate=dynamicTemplate.substring(0, dynamicTemplate.length()-6);
	    //String dynamicCode=dynamicTemplate+expansion_snippet +"}}";
	    String dynamicCode=dynamicTemplate.replace("<dynamic_code>",codeText);
	    System.out.println("dynamicCode"+dynamicCode);
	    
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
		     instance = compClass.newInstance();
		     
		 
		Class[] types = new Class[] {String.class};
		Method fooMethod;
		
		fooMethod =  instance.getClass().getMethod("setName",types);
	   

	     //ParValMap tmpH = null;
	     	
	     	     fooMethod.invoke(instance,name);
	     	     temp.deleteOnExit();
	     	     
	     	
		logger.trace("retrived by dynamic execution:"+instance);
		//db.putAll(this);
		
	     } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException | CompileException | IOException | ClassNotFoundException  e){
		logger.error("for doc:",e);
		throw new CIDBException(e.getMessage());
		
	     }
	     if (temp!=null) temp.delete();
	     return (Infer) instance;
    }

}
