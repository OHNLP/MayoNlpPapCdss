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
import javax.xml.soap.*;
import java.io.FileInputStream;
import java.io.StringBufferInputStream;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.Runnable;
import java.lang.NullPointerException;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */

public class SOAPCallRunnable implements Runnable{
	String response;
	String requestXmlString;
	String endpoint;
	String soapAction;
	static Logger logger = LoggerFactory.getLogger(SOAPCallRunnable.class);

	/**
	 * @param args
	 */
	
	public SOAPCallRunnable(String endpoint,String requestXmlString,String soapAction){
		response=null;
		this.requestXmlString=requestXmlString;
		this.endpoint=endpoint;
		this.soapAction=soapAction;
	}
	
	public String getXmlString() {
		return this.response;
	}
	public void run() {//throws SOAPException,TransformerConfigurationException,TransformerException {
		// TODO Auto-generated method stub
		          //First create the connection
		       try{
		          SOAPConnectionFactory soapConnFactory =  SOAPConnectionFactory.newInstance();
		          SOAPConnection connection = soapConnFactory.createConnection();

		          //Next, create the actual message
		          MessageFactory messageFactory = MessageFactory.newInstance();
		          SOAPMessage message = messageFactory.createMessage();
		          
		          SOAPPart soapPart =     message.getSOAPPart();
		          SOAPEnvelope envelope = soapPart.getEnvelope();
		          SOAPBody body =         envelope.getBody();
		          MimeHeaders hd = message.getMimeHeaders();
		          hd.addHeader("SOAPAction", soapAction);

		          //Populate the Message
		         //StreamSource preppedMsgSrc = new StreamSource( new FileInputStream("MICSxml.txt"));
		         StringBufferInputStream sbis=new StringBufferInputStream (requestXmlString);
		         StreamSource preppedMsgSrc = new StreamSource(sbis );
		         soapPart.setContent(preppedMsgSrc);

		         
		          //Save the message
		          message.saveChanges();
		          
		          //Send the message and get a reply   
		          logger.debug("SendingCall to"+endpoint+"\n"+ this.requestXmlString);
		            
		          //Set the destination
		          SOAPMessage reply = connection.call(message, endpoint);

		          //Check the output
		          //System.out.println("\nRESPONSE:\n");
		          //Create the transformer
		          TransformerFactory transformerFactory = TransformerFactory.newInstance();
		          Transformer transformer = transformerFactory.newTransformer();
		          
		          //Extract the content of the reply
		          Source sourceContent = reply.getSOAPPart().getContent();
		          //Set the output for the transformation
		         StringWriter sWriter= new StringWriter(); 
			 StreamResult result = new StreamResult(sWriter);
		          transformer.transform(sourceContent, result);
		          //System.out.println(">>"+sWriter.toString());
		          response=sWriter.toString();
		          
		          if(sWriter!=null)sWriter.close();
		          if(sbis!=null)sbis.close();
		          logger.debug("Response:"+response);
		         //Close the connection            
		          connection.close();
			}
			catch(NullPointerException e){
				logger.debug("NullPointerException");
			}
			catch(Exception e){logger.debug("ERROR:",e);}
	}

}

