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


import org.apache.tika.parser.Parser;

import java.io.*;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.WriteOutContentHandler;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.sax.*; 
import org.ohnlp.cidb.exception.CIDBException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.parser.AutoDetectParser;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */
public class RtfParser {
    private RTFParser parser;
    private static final TikaConfig defaultConfig = TikaConfig.getDefaultConfig();

  public RtfParser(){
        try{
	//parse(str);
	//File f = new File("test/test");
	//parse(f);
	}catch (Exception e){e.printStackTrace();}
    }

    public static void main(String[] arg)  {
	try{
	 RtfParser r=new RtfParser();
	System.out.print(r.parse("hi there"));
	}catch (Exception e){e.printStackTrace();}
   }

    public void  parse(File f) throws CIDBException  {
	FileInputStream in ;
	//File f = new File("test/test");
	try {
	    in = new FileInputStream(f);
	
	    byte[] buffer = new byte[(int) f.length()];
	    BufferedInputStream bis = new BufferedInputStream(in);
	    bis.read(buffer);
	    parse( new String(buffer));
	    if(in!=null) in.close();
	    if(bis!=null) bis.close();
	} catch (IOException e) {
	    throw  new CIDBException(e);
	
	}
   }
    public String  parse(String text) throws CIDBException, IOException   {
	ByteArrayInputStream bs=null;
	ContentHandler h=null;
	try {
		bs=new ByteArrayInputStream(text.getBytes("UTF-8"));
 		h = new BodyContentHandler();
		Metadata metadata = new Metadata();
    		//metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
    		Parser parser = new AutoDetectParser();
    		//Parser parser = new RTFParser();
    		
		    parser.parse(bs, h, metadata,new ParseContext());
		} catch (IOException | SAXException | TikaException e) {
		    throw  new CIDBException(e);
		}
		if(bs!=null) bs.close();
		return h.toString();
		
	
		//System.out.println("content: " + h.toString());
		
	}
}
