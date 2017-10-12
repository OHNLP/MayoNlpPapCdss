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


import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** used to make SOAP calls
 *specify endpoint, SOAP envelope request, and SOAP action
 *
 *Keeps a count of call made and throws exception if count exceeds global max limit defined in config
 *The call is made in a separate Thread. 
 *The Thread is inturupted if it does not return before SoapCallTimeout  defined in config
 *@author Kavishwar Wagholikar (waghsk)
 */
public class SOAPCall{
	static int count=0;
	SOAPCallRunnable SC;
	String response;
	static Logger logger = LoggerFactory.getLogger(SOAPCall.class);

	static public void resetCallCount(){
		count=0;
	}
	static public int getCallCount(){
		return count;
	}
	/**
	 * Make call
	 * @param endpoint
	 * @param requestXmlString
	 * @param soapAction
	 * @throws CIDBException
	 */
	public SOAPCall(String endpoint, String requestXmlString,String soapAction,Session session) throws CIDBException{
		count+=1;
		logger.debug("SOAP Call count:"+count);
		if (count>=Integer.parseInt(session.GetProp("MaxSoapCalls"))) {
			String msg="Exceeded maximum number of calls for the patient:"+session.GetProp("MaxSoapCalls");
			logger.error("fatal", msg);
			throw new CIDBException(msg);
		}
		SC=new SOAPCallRunnable(endpoint,requestXmlString,soapAction);
		Thread t=new Thread(SC);
		t.start();
		try{

			Integer millis=new Integer(session.GetProp("SoapCallTimeOut"));

			t.join(millis);
		}catch(Exception e){
			logger.error("",e);
			e.printStackTrace();
			//throw new DataFetchException(e.getMessage());
		}
		if (SC.getXmlString()==null) {
			throw new CIDBException("Error retriving data");
		}
	}

	static public void resetCount(){
		logger.debug("resetting SoapCall count");
		count=0;
	}
	/**retrive the xml response given by the called SoapService
	 */
	public String getXmlString() {
		return SC.getXmlString();
	}
}

