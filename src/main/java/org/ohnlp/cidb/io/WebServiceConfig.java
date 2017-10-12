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

import java.util.Properties;





import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */
public  class WebServiceConfig{
    static Logger logger = LoggerFactory.getLogger(WebServiceConfig.class);
	 protected
	 
	 String label;
	 String endpoint;
	 String requestXmlPath;
	 String soapAction;
	 Session session;
	 Properties requestProps;
	 
	 public WebServiceConfig(String label,Session session){
	     this.label=label;
	     requestProps=new Properties(); 
	     this.session=session;
	 }
	 
	 private void check_session(){
	     String msg="set session first in webservice configuration";
	     if (session==null) new CIDBException(msg);
	 }
	 
	 public WebServiceConfig setEndpoint(String endpoint) {
	     logger.trace("session"+this.session);
	    check_session();
	    this.endpoint = session.substituteProps(endpoint);
	    return this;
	}
	public WebServiceConfig setRequestXmlPath(String requestXmlPath) {
	    check_session();
	    this.requestXmlPath = session.substituteProps(requestXmlPath);
	    return this;
	}
	public WebServiceConfig setSoapAction(String soapAction) {
	    check_session();
	    this.soapAction = Utils.substituteProps(session.getProps(),soapAction);
	    return this;
	}
	
	public WebServiceConfig addRequestProps(String k,String val){
	    check_session();
	    requestProps.put(k, session.substituteProps(val));
	    return this;
	}
	
	
	 
	 public WebService build(){
	     return new WebService(this);
	 }
}