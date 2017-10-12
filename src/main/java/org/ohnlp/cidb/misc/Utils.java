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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */
public class Utils {
    static Logger logger=LoggerFactory.getLogger(Utils.class);
   
    public static void addtoClassPath(String s) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, MalformedURLException  {
	    File f = new File(s);
	    URL u = f.toURL();
	    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    Class urlClass = URLClassLoader.class;
	    Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
	    method.setAccessible(true);
	    method.invoke(urlClassLoader, new Object[]{u});
	}
    
    public static void printClassPath() {
	 
        ClassLoader cl = ClassLoader.getSystemClassLoader();
 
        URL[] urls = ((URLClassLoader)cl).getURLs();
 
        System.out.println("classpath");
        for(URL url: urls){
        	System.out.println(url.getFile());
        }
 
   }
	public static String toDate(String msString){
	    GregorianCalendar t=new GregorianCalendar();
	    t.setTimeInMillis(Long.parseLong(msString));
	    return String.format("%1$td-%1$tb-%1$tY", t);
	    
	}
	
    
    public static String getClassPath() {
	
	String str="";
        ClassLoader cl = ClassLoader.getSystemClassLoader();
 
        URL[] urls = ((URLClassLoader)cl).getURLs();
 
        
        for(URL url: urls){
        	str+="\n"+url.getFile().toString();
        }
        //System.out.println("classpath:"+str);
        return str;
   }
    
    public static GregorianCalendar getGregorianCalendarFromOdbc(String timeStamp){
	
	List<String> knownPatterns = new ArrayList<String>();
	knownPatterns.add("yyyy/MM/dd HH:mm");
	knownPatterns.add("yyyy-MM-dd HH:mm");
	knownPatterns.add("yyyy-MM-dd HH:mm:ss");
	knownPatterns.add("yyyy/MM/dd HH:mm:ss");
	knownPatterns.add("yyyy-MM-dd HH:mm:ss.SS");
	knownPatterns.add("yyyy-MM-dd HH:mm:ss.SSS");
	knownPatterns.add("yyyy-MM-dd:HH:mm:ss'.'SSSSSS");
	knownPatterns.add("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
			   
	
	
	
	for (String format : knownPatterns) {
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date d=sdf.parse(timeStamp);
		logger.trace("Date:"+d);
		System.out.println("Date:"+d);
		GregorianCalendar  gc= new GregorianCalendar();
		gc.setTime(d);
		return gc;
		
	    } catch (ParseException pe) {
	       // logger.trace(pe.getMessage());
	    }
	}
	throw new IllegalArgumentException("Could not parse time:"+timeStamp);
     }
    
    public static GregorianCalendar getGregorianCalendarFromString(String timeStamp){
	       return getGregorianCalendarFromOdbc(timeStamp);
    }
    
   public static String GregorianCalendarToString(GregorianCalendar gc){
	return String.format("%1$tY-%1$tb-%1$td %1$tH:%1$tM:%1$tS ", gc);
    }
   
   
  public static String GregorianCalendarToODBCString(GregorianCalendar gc){
	return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS ", gc);
   }
	
   public static GregorianCalendar fromSqlDateTime(String targetString){
       return getGregorianCalendarFromOdbc(targetString);
   }

   static public String readFile(String path) throws CIDBException  {
       	String str=null;
	try {
	Path p=Paths.get(path);
	InputStream is=Utils.class.getClassLoader().getResourceAsStream(path);
	BufferedReader reader=null;
	if(is!=null) {
	    reader = new BufferedReader(new InputStreamReader(is));
	    
	    	StringBuilder  stringBuilder = new StringBuilder();
	    	String  ls = System.getProperty("line.separator");
	    	String line=null;
	    	
		    while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		    
	/*}else{
	    byte[] encoded = Files.readAllBytes(p);
		Charset encoding = StandardCharsets.UTF_8;
		str= new String(encoded, encoding);
	}*/
	str= stringBuilder.toString();      
	is.close();
	reader.close();
	}
	//if(is==null){System.out.println("->>could not read from class path:<"+path+"><<-");}
	//reading from file system
	if(is==null){
	    File f=getFile(path);
	    if(f.exists()){
		Scanner s=new Scanner(f);
		str="";
		while(s.hasNext()) str=str+s.nextLine()+"\n";
		    //System.out.println(s.nextLine());
		s.close();
		return str;
	    }
	}
	
	//if(str==null) throw new DataFetchException("could not find file in classpath and file system:"+path);
	
	} catch (IOException e) {
	    logger.error("fatal",e);
	    throw new CIDBException(e.getMessage());
	}
	return str;
}
   

   
   public static File writeFile(String path,String content) throws FileNotFoundException{
       logger.trace("writing to file:"+path);
       
       File f=new File(path);
       
       PrintWriter out= new PrintWriter(f);
       out.write(content);
       out.close();
       return f;
   }
   
   
   public static File appendFile(String path,String content) throws IOException{
       logger.trace("writing to file:"+path);
       
       File f=Utils.getFile(path);
       PrintWriter out= new PrintWriter(f);
       out.write(content);
       out.close();
       return f;
   }
/*
static public File getFile(String fileName) throws IOException{

    File f=FileUtils.toFile(Utils.class.getClassLoader().getResource(fileName));
    //logger.info(Utils.getClassPath());
    if(f!=null){
      logger.trace("found file"+f.getAbsolutePath().toString()+f.exists());
      return f;}
    else{ 
	Path p=Paths.get(fileName);
	f=p.toFile();
    }
   
    return f;
    	//System.out.println(f.getAbsolutePath());
 
}
*/
   
   /*
    * First search file system then class paths
    */
static public File getFile(String path) throws IOException {
  	
	//Path p=Paths.get(path);
	File f=new File(path);
	if (f.isFile() || f.isDirectory()||f.exists()) return f;
	f=getFileFromClassPath(path);
	logger.trace("returning file  <"+f.getAbsolutePath()+">");
	
	return f;
}


static public File getFileFromClassPath(String path) throws IOException{
    	File f;
    	URL url=Utils.class.getClassLoader().getResource(path);
    	logger.trace("seeking file from url <"+url+"> from str <"+path+"> from path <"+Paths.get(path)+">");
	f=new File(url.getFile());
	return f;
}

    static public InputStreamReader getInputStreamReader(String path) throws Exception{
	    return new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(path));
   }

    
    
    public static Properties loadProperties(String path) throws FileNotFoundException, IOException, org.ohnlp.cidb.exception.CIDBException {
	path=path.replace("//", "/");
	Properties props = new Properties();
	File pf=Utils.getFile(path);
	
	/*if(pf.isFile()) logger.trace("read properties from: <"+pf.getAbsolutePath()+">");
	else logger.trace("could not read properties from: <"+pf.getAbsolutePath()+"> path <"+path+">");
	FileInputStream is=new FileInputStream(pf);
	*/
	
	String str = Utils.readFile(path);
	InputStream is = new ByteArrayInputStream(str.getBytes());
	
	props.load(is);
	for(Object key:props.keySet()){
	    props.setProperty((String) key, ((String)props.getProperty((String) key)).replaceAll("#[\\W|\\S]+$",""));
	}
	if(is!=null) is.close();
	return props;
   	
   }
    
    static public String substituteProps(Properties props,String x){
	if(x==null){return null;}
	logger.trace("input:"+x);
	    for(Object k:props.keySet()){
		x=x.replaceAll((String) k, props.getProperty((String) k).replaceAll("\\s+$",""));
	    }
	    return x;
	}
    
  //counts to end time
  	/**
  	 * @param input
  	 * @param session
  	 * @return
  	 */
    public static Double timeSince(GregorianCalendar input,Session session){
  	    Long diff=session.getEndTime().getTimeInMillis()-input.getTimeInMillis();
  		//System.out.println("let:"+Global.GregorianCalendarToString(lowEffectiveTime));
  		return diff.doubleValue() /(1000.0*60.0*60.0*24.0*365.0);
    	} 
  	
  public static File createTempDirectory(String tempPath) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException	{
      logger.trace("creating dir:"+tempPath);
  	      
  	      
  	    	File temp = File.createTempFile(tempPath, Long.toString(System.nanoTime()));
  	    	
  	      
  	      if(!(temp.delete())) throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	   

  	      if(!(temp.mkdir())) {
  		  throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
  	      }
  	    logger.trace("created dir:"+temp.getAbsolutePath());
  	    temp.deleteOnExit();
  	      return (temp);
  	      
  	}
  
   public static void initializeDefaultLoggerConfiguration(){
       //System.out.println("initialize log4j");
       org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
       if (!rootLogger.getAllAppenders().hasMoreElements()){
	   PropertyConfigurator.configure(Utils.class.getClassLoader().getResource("config/log4j.config"));
       }
      
   }
   
   public static boolean StringNormMatch(String s1,String s2){
       s1=s1.toLowerCase().replaceAll("\\W", "");
       s2=s2.toLowerCase().replaceAll("\\W", "");
       if(s1.equals(s2)) return true;
       
       return false;
   }
   
   public static String getTextFromBinary(String binTxt){
       return new String(Base64.decodeBase64(binTxt.getBytes()));
   }
   
   public static String getTextFromRtf(String rtfTxt) throws CIDBException, IOException{
       return new org.ohnlp.cidb.misc.RtfParser().parse(rtfTxt);
   }
   
   public static String trimBeginAndEndQuotes(String s){
       if(s.matches("[\\W|\\w]+\"$")) s=s.substring(0,s.length()-1);
       if(s.matches("^\"[\\W|\\w]+")) s=s.substring(1,s.length());
       return s;
   }
   
   public static Double getAgeAt(GregorianCalendar timeAt,GregorianCalendar dob) throws CIDBException {
       logger.trace("timeAt:"+Utils.GregorianCalendarToString(timeAt));
       logger.trace("dob:"+Utils.GregorianCalendarToString(dob));
	Long diff=(timeAt.getTimeInMillis()-dob.getTimeInMillis());
	Double age=(diff.doubleValue())/(1000.0*60.0*60.0*24.0*365.242);
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	age= Double.valueOf(twoDForm.format(age));

	logger.info("Age in year: "+(timeAt).get(Calendar.YEAR)+" is " +age);
	
	return age;
}
   
   public static Double timeSinceInYears(GregorianCalendar time1,GregorianCalendar refTime){
       
       Double years=timeSinceInSeconds(time1, refTime)/(60.0*60.0*24.0*365.242);
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	years= Double.valueOf(twoDForm.format(years));
	logger.trace("time since in years: "+(time1).get(Calendar.YEAR)+" is " +years);
	return years;
   }
   
   public static Double timeSinceInSeconds(GregorianCalendar time1,GregorianCalendar refTime){
       Long diff=(refTime.getTimeInMillis()-time1.getTimeInMillis());
       Double years=(diff.doubleValue())/(1000.0);
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	years= Double.valueOf(twoDForm.format(years));
	logger.trace("time since in years: "+(time1).get(Calendar.YEAR)+" is " +years);
	return years;
   }
   
   public static Long getUsedMemory(){
	Runtime rt = Runtime.getRuntime();
	rt.gc();
	rt.gc();
	rt.gc();
	//long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024/ 1024 ;
	//return usedMB;
	long usedbytes = (rt.totalMemory() - rt.freeMemory())  ;
	return usedbytes;
	
   }
   
   public static String getUsedMemoryString(){
      // return "mem(MB):"+String.format("%2.3f",getUsedMemory().floatValue()/1024);
       return "mem(bytes):"+NumberFormat.getNumberInstance(Locale.US).format(getUsedMemory());
   }



public static void copyDirectory(String fromDir, String toDir) throws IOException, CIDBException {
    File srcDir=getFile(fromDir);
    File destDir=getFile(toDir);
    if(!srcDir.isDirectory())throw new IllegalArgumentException("<"+fromDir+"> is not a directory");
    if(!destDir.isDirectory())throw new IllegalArgumentException("<"+toDir+"> is not a directory");
    
    
    
    for(File f : srcDir.listFiles()){
	FileUtils.copyFileToDirectory(f, destDir);
	logger.trace("copied <"+f.getAbsolutePath()+"> to dir <"+destDir.getAbsolutePath()+">");
    }
}


  	
}
