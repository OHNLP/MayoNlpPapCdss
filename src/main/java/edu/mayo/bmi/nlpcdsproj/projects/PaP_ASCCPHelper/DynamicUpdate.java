/*******************************************************************************
 *  Copyright: (c)  2014  Mayo Foundation for Medical Education and 
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

package edu.mayo.bmi.nlpcdsproj.projects.PaP_ASCCPHelper;

/*
 * This batch class accepts a file name as argument
 * It will find and load the recos for patient ids given the 
 * file that are not in the db
 */
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.io.Sql;
import org.ohnlp.cidb.io.SqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ohnlp.flowds.mkdrl.MkDrl;
import org.ohnlp.cidb.misc.Utils;
import org.ohnlp.flowds.engine.DataFetchException;
import org.ohnlp.flowds.engine.Engine;
import org.ohnlp.flowds.engine.Recommend;
import org.ohnlp.flowds.knowledgebase.Loader;
import org.ohnlp.flowds.misc.StopWatch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Application class to call rule-engine for Cervical Cancer Screening
 * @author Kavishwar B. Wagholikar and K.E. Ravikumar
 *
 */
public class DynamicUpdate {


	static Logger logger = LoggerFactory.getLogger(DynamicUpdate.class);
	String dirPath=null;
	final String commondirPath="common";
	String recoType="PpN";

	public DynamicUpdate(String recoType,String dirPath) throws DataFetchException, FileNotFoundException, IOException{
		this.recoType=recoType;
		this.dirPath=dirPath;
		try {
			logger.info("running dynamic update");
			run();
		} catch ( SQLException | InterruptedException
				| CIDBException e) {
			throw new DataFetchException(e);
		}

	}

	public void run() throws InterruptedException, DataFetchException, SQLException, CIDBException, FileNotFoundException, IOException {
		logger.info("before session"+Utils.getUsedMemoryString());

		
		ArrayList<String> mrnList = getUpdateMrnListFromFile() ;
		
		while(true){
			Collections.shuffle(mrnList);

			Iterator<String>mrnItr=mrnList.iterator();
			for (int i=0;i<=mrnList.size();i++){//set of 100
				Session.singleSession=null;
				int errorCount=0;
				logger.info("before moving to next case:"+Utils.getUsedMemoryString());
				
				if(mrnItr.hasNext()){

					String mrn=mrnItr.next();
					//logger.info("Seeing recommendation for:"+mrn);
					try {
						runSingle(mrn);
					} catch (Exception e) {
						logger.error("",e);

						errorCount++;
						logger.info("sleeping due to error. Total errors:"+errorCount);
						errorCount++;
						Thread.sleep(1000 *  errorCount*errorCount );
					}
					logger.info("will sleep. MRN list has size: "+mrnList.size());
					Thread.sleep(1000);

				}else{//to Stop execution on empty list
					logger.info("CDSS exit as patient list is processed");
					Loader.resetKbHash();
					Session.cleanUpStatic();
					System.exit(1);
				}
				logger.debug("Wokeup");
			}
			logger.info("getting new list");
		}
		//after run is stopped
	}

	private void runSingle(String mrn) throws Exception{
		Session session=null;
		Recommend r=null;
		StopWatch sw=null;
		try{
			session=new Session(mrn,dirPath);
			sw=new StopWatch(session,"Dynamic Update");
			session.appendLog("prog", "DynamicUpdate");
			r=new Recommend(session.getId(),session.getEndTime(),session.getVersion());
			r=Engine.getReco(session,Utils.readFile(dirPath+"/drl.txt"));
			r.setType(recoType);
			StoreRecommendation.store(r,session);

		}catch(Exception e){
			logger.error("",e);
			String msg=e.getMessage();
			if(msg.length()>=1000) msg=msg.substring(0, 1000);
			msg=msg.replace("\"", "").replace("'", "");
			r.setExplaination("ERROR"+msg);
			logger.error("ERROR computing recommendation for:"+session.getId()+"\n"+r.getExplaination(),e);

			StoreRecommendation.store(r,session);


			logger.warn("cleaning up static elements of session due to exception in dynamic run");
			throw e;
		}finally{
			//closing session and SW
			try{
				System.out.println("log-"+session.getLogAll());
				session.dispose();
				sw.stop();
			}catch(Exception e){
				logger.error("error disposing single run  session/SW",e);
			}	
		}
	}


	
	public ArrayList<String> getUpdateMrnListFromFile() throws IOException, FileNotFoundException {
		logger.info("getting list of MRNs");
		ArrayList<String> updateMrnList= new ArrayList<String>();

		FileInputStream fstream = new FileInputStream("input_data/patient_id.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		
		String line="";
		
		//get patients from patient update index
		System.out.println("Dir Path="+dirPath) ;

		while ((line = br.readLine()) != null)   {
			if(!updateMrnList.contains(line.trim())){
				updateMrnList.add("0"+line.trim());
			}
		}
		
		logger.info("updateMrnList.size():"+ updateMrnList.size());

		return updateMrnList;
	}



}
