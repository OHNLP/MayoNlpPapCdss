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
package org.ohnlp.cidb.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/*
 * @author Kavishwar Wagholikar (waghsk)
 */

public class ParseXml {
    static Logger logger = LoggerFactory.getLogger(ParseXml.class);
    /**
     * @param args
     */
/*    public static void main(String[] args) {
	String xmlString;
	try {
	    xmlString = Global.readfile("test/edt/1.xml").replaceAll("\\<\\!DOCTYPE levelone\\[\\]\\>", "");
	
	//System.out.println(xmlString);
	    System.out.println(getSections(xmlString));
	} catch (DataFetchException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	

    }
    */
    public  HashMap<String,String> getTags(String xmlStr,String level1tag,String level2tag) {
	
   	HashMap<String,String> ht=new HashMap<String,String>();
   	try {
   	    	xmlStr=xmlStr.trim();//.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
   	    	logger.trace("parsing:",xmlStr);
   		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		InputSource inStream = new InputSource();
 
    		inStream.setCharacterStream(new java.io.StringReader(xmlStr));
		Document doc = dBuilder.parse(inStream);
		doc.getDocumentElement().normalize();
		//System.out.println(xmlStr);


   	NodeList nList = doc.getElementsByTagName(level1tag);
 	
   		for (int temp = 0; temp < nList.getLength(); temp++) {
   	 
		   Node nNode = nList.item(temp);
		   if (nNode instanceof Element) {

		      Element eElement = (Element) nNode;
		      String caption= eElement.getChildNodes().item(0).getChildNodes().item(1).getNodeValue();
		      ht.put(caption.trim(),"");
		      NodeList contentList=eElement.getElementsByTagName(level2tag);
		      for (int c = 0; c < contentList.getLength(); c++){
			  Node content=contentList.item(c).getFirstChild();
			  if(content!=null){
			      //System.out.println (caption+"->"+content.getNodeValue());
			      ht.put(caption, ht.get(caption)+content.getNodeValue().trim()+"\n");
			  }
		      } 
		   }
		}
		
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
   	return ht;
       }

}
