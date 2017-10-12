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
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *     
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 *******************************************************************************/

/****
 * @author Kavishwar B. Wagholikar and K.E. Ravikumar
 * Last updated: April 10th 2017
 * 
 */
package edu.mayo.bmi.nlpcdsproj.projects;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.apache.log4j.PropertyConfigurator;
import org.ohnlp.cidb.core.DocSet;
import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.exception.ConceptNotFoundException;
import org.ohnlp.flowds.engine.DataFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.mayo.bmi.nlpcdsproj.def.Definitions;
import edu.mayo.bmi.nlpcdsproj.projects.PaP_ASCCPHelper.StoreRecommendation;

public class PaP_ASCCP {
	static Logger logger= LoggerFactory.getLogger(PaP_ASCCP.class);
	String dirPath;

	public PaP_ASCCP(String name) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
				NoSuchMethodException, SecurityException, MalformedURLException {
		dirPath="proj/PaP_ASCCP/";
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("config/log4jtest.config"));
		logger.trace("created logger");
		// TODO Auto-generated constructor stub
	}

	public void dynamicUpdate() throws InterruptedException{
		int errorCount=0;
		String path = "proj/PaP_ASCCP/" ;
		
		while(true){
			
			try {
										
				try {
					new edu.mayo.bmi.nlpcdsproj.projects.PaP_ASCCPHelper.DynamicUpdate("PaP_ASCCP","proj/PaP_ASCCP");
				} catch (DataFetchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch(Exception e){
				logger.error("Sleeping for 30 mins due to fatal error:"+errorCount,e);
				Thread.sleep(1000*60*30); //
			}
			errorCount+=1;
			if(errorCount>10) System.exit(1);
		}

	}

	public static void main(String[] args) throws DataFetchException, ConceptNotFoundException, InterruptedException, CIDBException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, MalformedURLException{
		PaP_ASCCP p=new PaP_ASCCP("-"); 
		p.dynamicUpdate();
		return;
	}


}
