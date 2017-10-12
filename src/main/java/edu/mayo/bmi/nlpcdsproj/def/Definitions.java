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

package edu.mayo.bmi.nlpcdsproj.def;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;


import org.ohnlp.flowds.engine.*;
import org.ohnlp.cidb.core.Concept;
import org.ohnlp.cidb.core.Definition;
import org.ohnlp.cidb.core.Dictionary;
import org.ohnlp.cidb.core.Document;
import org.ohnlp.cidb.core.DocSet;
import org.ohnlp.cidb.core.Infer;
import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.io.Sql;
import org.ohnlp.cidb.io.SqlBuilder;
import org.ohnlp.cidb.io.WebService;
import org.ohnlp.cidb.io.WebServiceConfig;
import org.ohnlp.cidb.io.WebServiceQuery;
import org.ohnlp.cidb.misc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import regex_annotator.RegexRules;
//1 1.2 2 3 5 9 13 25 26 27 R23

/**
 * will instantiate concept definitions
 * @author Kavishwar B. Wagholikar and K.E. Ravikumar
 */
public class Definitions {
	static Logger logger = LoggerFactory.getLogger(Definitions.class);

	public Definitions(Session $s){

		//DEFINITIONS_START--------------------------------------------


		new Definition("SIRS",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=DocSet.newInstance($s, "SIRS");
			}
		};
		 
		
		new Definition("EDT_ClinicalNotes",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=DocSet.newInstance($s, "EDT_ClinicalNotes");
			}
		};
		

		/**
		 *  Definitions for data elements starts here 
		 */
		
		new Definition("Demographics",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d=Document.createLabelledDoc("Demographics", $s);
		 		d.put("sex", "female");
		 		d.put("dob","1960/1/30 00:00:00" );
				d.put("age", "54.6");
				d.put("isAlive", "yes");
				d.put("time", d.get("TIME"));
			}
		};

		new Definition("Labs",DocSet.class,$s){
			public void define(Session $s) throws Exception {				
					DocSet ds=DocSet.newInstance($s, "Labs");
			}
		};

		new Definition("Dispositions",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				logger.trace("returning dispositions");
				DocSet ds=DocSet.newInstance($s, "Dispositions");
			}
		};

		new Definition("PPI",DocSet.class,$s){
			public void define(Session $s) throws Exception {				
				DocSet ds=DocSet.newInstance($s, "PPI");
			}
		};
		
		new Definition("ProblemList",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				
				DocSet ds = DocSet.newInstance($s, "ProblemList_raw");				
				Dictionary.GetInstanceFromCsvFile("ProblemList_Dictionary","Dictionary_ProblemList.txt",$s,2,3);
				Infer f=new Infer("relevant_problems"){
					public String run(Document d, Session s) throws Exception {
						Dictionary dict=(Dictionary)s.getObjectNoException(Dictionary.class,"ProblemList_Dictionary");

						d.put("hysterectomy",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"hysterectomy"))?"yes":"no");
						d.put("cervicalCancer",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"cervicalca"))?"yes":"no");
						d.put("immuno-deficiency",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"immuno-deficiency"))?"yes":"no");
						d.put("hiv",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"hiv"))?"yes":"no");
						d.put("transplant",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"transplant"))?"yes":"no");
						d.put("des",(dict.doesInputContainMapToGivenNorm(d.get("PROBLEMPERSISTENCE"),"des"))?"yes":"no");

						d.put("cervicalCancerHighRisk",(
								d.get("immuno-deficiency").equals("yes")
								||d.get("hiv").equals("yes")
								||d.get("immuno-deficiency").equals("yes")
								||d.get("transplant").equals("yes")
								||d.get("des").equals("yes")
								||d.get("cervicalCancer").equals("yes")

								)?"yes":"no");


						d.put("cervCancerHighRisk",(
								d.get("des").equals("yes")
								||d.get("cervicalCancer").equals("yes")

								)?"yes":"no");

						d.put("cin23history",(dict.doesInputContainMapToGivenNorm(d.get("ProblemPersistence"),"cin23history"))?"yes":"no");

						return "-";
					}
				};
				ds.getSubset($s, "ProblemList", f);

			}
		};

	

		new Definition("MICSLW_Docs",DocSet.class,$s){
			public void define(Session $s) throws Exception {

		    DocSet ds=DocSet.newInstance($s, "PathologyNotes");
		  //replace with data fetching code---
		    Document d1=new Document();
		    d1.put("time", "2014-07-01 00:00;))");
		    d1.put("name","Cytology - Pap Smear");
		    if($s.getId().equals("average screening patient")){
		    d1.put("content", "DIAGNOSIS: "
			   +"  ThinPrep Pap Test Screen (Cervical/Endocervical):\n" 
			   +"  Satisfactory for evaluation.\n" 
			   +"   Scanty cellularity. \n" 
			   +" Negative for intraepithelial lesion or malignancy.\n" 
			  );
		    }
		    else if($s.getId().equals("patient with abnormal pap"))
		    { d1.put("content", "DIAGNOSIS: "
				   +"  ThinPrep Pap Test Screen (Cervical/Endocervical):\n" 
				   +"  Satisfactory for evaluation.\n" 
				   +"   High grade  squamous intraepithelial lesion	 \n" 
				   +"   Scanty cellularity. \n" 
				   +" high risk type 16 pcr positive.\n" 
			    );
		    }		
			ds.add(d1);


			}
		};


		new Definition("HistoryOfHysterectomy_ProblemList",Document.class,$s){
			public void define(Session $s) throws Exception {
				((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"HistoryOfHysterectomy_ProblemList",$s,"hysterectomy","yes");
			}
		};

		new Definition("CervicalCytologyNLPRuleBase",RegexRules.class,$s){
			public void define(Session $s) throws Exception {

				RegexRules rr= new RegexRules(Utils.readFile("proj/PaP_ASCCP/CervicalCytologyNLPRules.txt"));
				$s.putObject(RegexRules.class,"CervicalCytologyNLPRuleBase",rr);
			}
		};

		new Definition("CervicalCytology",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"MICSLW_Docs");
				//$s.getObject(RegexRules.class,"CervicalCytologyNLPRuleBase");//call before calling in dynamic code
				//	Infer by name before seeking content
				Infer Infer1=new Infer("CervicalCytology"){
					public String run(Document d,Session s) throws Exception{
						String val="false";
						if( d.get("name").equals("Cytology - Pap Smear")){
							d.get("content");
							regex_annotator.RegexRules rr=(regex_annotator.RegexRules)s.getObjectNoException(regex_annotator.RegexRules.class,"CervicalCytologyNLPRuleBase");
							rr.process(d.get("content"));d.putAll(rr.getParValHash());
							d.put("CervicalCytologySatisfactoryForEval",d.get("CytologyType").equals("unsatisfactory for evaluation")?"false":"true");
							val=d.get("ReportOrgan").equals("cervix") || d.get("ReportOrgan").equals("vagina")?"true":"false";
						}else{ val="false";d.put("CervicalCytologySatisfactoryForEval","false");}

						return val;
					}
				};

				Infer Infer2= new Infer("is_HSIL_ASCH_or_AGC"){
					public String run(Document d,Session s) throws Exception{
						String a = "false" ;
						if( d.get("CervicalBiopsyReport").equals("true") &&
								(d.get("HSIL").equals("true")
										||d.get("ASCH").equals("true")
										||d.get("AGC").equals("true"))) {
							a = "true" ;
						}
						return  a ;

					}
				};

				DocSet ds1=ds.getSubset($s,"CervicalCytology",Infer1);
				ds1.addInfer(Infer2);

			}

		};

		new Definition("PathologyReport",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"MICSLW_Docs");
				//	Infer by name before seeking content
				Infer Infer1=new Infer("PathologyReport"){
					public String run(Document d,Session s) throws Exception{
						if( d.get("name").equals("General Pathology Report")){
							return "true";
						}else{ return "false";}

					}
				};
				DocSet ds1=ds.getSubset($s,"PathologyReport",Infer1);

			}

		};

		new Definition("ColposcopyReport",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"MICSLW_Docs");
				//	Infer by name before seeking content
				Infer Infer1=new Infer("ColposcopyReport"){
					public String run(Document d,Session s) throws Exception{
						if( d.get("name").equals("Colposcopy")){
							return "true";
						}else{ return "false";}

					}
				};
				DocSet ds1=ds.getSubset($s,"ColposcopyReport",Infer1);
				ds1.addInfer(Infer1);
			}

		};

		new Definition("HistoryColposcopy",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HistoryColposcopy",$s);

				String val="no";
				int i=0 ;
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"ColposcopyReport");
				Document d=ds.get(i, "ColposcopyReport"+i, $s, "ColposcopyReport", "true");
				if(d.isReportFound()){
					val="yes";

				}									
				d1.put("value", val);

			}
		};

		new Definition("LastColposcopy",Document.class,$s){
			public void define(Session $s) throws Exception {

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"ColposcopyReport");
				Document d=ds.get(0, "LastColposcopy", $s, "ColposcopyReport", "true");
				if(d.isReportFound()) {
					d.put("timeMS", d.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
				}
			}
		};


		new Definition("CervicalBiopsyReport",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"PathologyReport");
				//	Infer by name before seeking content
				Infer Infer1=new Infer("CervicalBiopsyReport"){
					public String run(Document d,Session s) throws Exception{

						if( d.get("PathologyReport").equals("true")
								&& d.get("content").toLowerCase().contains("cervix")){
							return "true";
						}else{ return "false";}

					}
				};

				DocSet ds1=ds.getSubset($s,"CervicalBiopsyReport",Infer1);

				Infer Infer2=new Infer("CIN23") {
					public String run(Document d,Session s) throws Exception{

						if( d.get("CervicalBiopsyReport").equals("true")
								&& d.get("content").contains("DIAGNOSIS")
								&& ( d.get("content").contains("CIN 2")
										|| d.get("content").contains("CIN2")
										|| d.get("content").contains("CIN 3")
										|| d.get("content").contains("CIN3")
										|| d.get("content").contains("CIN II")
										|| d.get("content").contains("CIN III"))
								) {
							return "true";
						}else{ return "false";}

					}
				};
				ds1.addInfer(Infer2);

			}
		};


		new Definition("HistoryHysterectomy_ProblemList",Document.class,$s){
			public void define(Session $s) throws Exception {
				((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"HistoryHysterectomy_ProblemList",$s,"hysterectomy","yes");
			}
		};

		new Definition("LastClinicalNoteWithHistoryOfHysterectomy_EDT",Document.class,$s){
			public void define(Session $s) throws Exception {

				DocSet ds=(DocSet) $s.getObject(DocSet.class,"EDT_ClinicalNotes");
				String codeText="";
				codeText+="if(!d.get(\"sectioned\").equals(\"true\")){";
				codeText+="	org.ohnlp.cidb.misc.ParseXml p=new org.ohnlp.cidb.misc.ParseXml();d.putAll(p.getTags(d.get(\"content\"),\"section\",\"content\")); ";
				codeText+="	d.put(\"sectioned\",\"true\");";
				codeText+="	if(d.get(\"surgical history\").toLowerCase().indexOf(\"hysterectomy\")!=-1 || d.get(\"surgical history\").toLowerCase().indexOf(\"removal of uterus\")!=-1){";
				codeText+="	d.put(\"hysterectomy\",\"true\");}else{d.put(\"hysterectomy\",\"false\");}";
				codeText+="}";
				ds.setInferText(codeText);
				Document d=ds.get(0, "LastClinicalNoteWithHistoryOfHysterectomy_EDT", $s, "hysterectomy","true");
			}
		};

		new Definition("HistoryHysterectomy_ProblemList_Dictionary",Dictionary.class,$s){
			public void define(Session $s) throws Exception {

				Dictionary dict= new Dictionary("HistoryHysterectomy_ProblemList_Dictionary",$s);
				dict.put("V88.01","Hysterectomy Vaginal S/P");
				dict.put("V88.01","Hysterectomy S/P");
				dict.put("V88.01","Hysterectomy Non Malignant S/P");
				dict.put("618.50","Prolapse Vaginal Vault Post Hysterectomy");
				dict.put("V88.01","Hysterectomy Non-Malignant S/p");
			}
		};

		new Definition("HistoryHysterectomy",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a= ( ($s.equals("HistoryHysterectomy_ProblemList.hysterectomy","true"))
						||($s.equals("HistoryHysterectomy_PPI.value","true"))
						||($s.equals("PapDispositionLast.NotApplicableReason","Hysterectomy (non-cervical cancer)"))
						||($s.equals("HistoryHysterectomy_SIRS.value","true"))
						||($s.equals("LastClinicalNoteWithHistoryOfHysterectomy_EDT.hysterectomy","true")));


				
				Document d=Document.createLabelledDoc("HistoryHysterectomy",$s);
				d.put("value",a?"yes":"no");

				if(a){
					String[][] arr = {{"HistoryHysterectomy_ProblemList.hysterectomy","true"},{"HistoryHysterectomy_PPI.value","true"},{"PapDispositionLast.NotApplicableReason","Hysterectomy (non-cervical cancer)"},{"HistoryHysterectomy_SIRS.value","true"},{"LastClinicalNoteWithHistoryOfHysterectomy_EDT.hysterectomy","true"}};
					Document d1=Concept.lastOfConceptValueArray("HistoryHysterectomy_time",arr,$s);
					if(d1.isReportFound() && d1.getTime()!=null)d.put("time", d1.get("time"));
				}
			}
		};

		new Definition("HistoryHysterectomyStatus",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a= ( ($s.equals("HistoryHysterectomy_ProblemList.hysterectomy","true"))
						||($s.equals("HistoryHysterectomy_PPI.value","true"))
						||($s.equals("PapDispositionLast.NotApplicableReason","Hysterectomy (non-cervical cancer)"))
						||($s.equals("HistoryHysterectomy_SIRS.value","true"))
						||($s.equals("LastClinicalNoteWithHistoryOfHysterectomy_EDT.hysterectomy","true"))); 

				Document d=Document.createLabelledDoc("HistoryHysterectomyStatus",$s);
				d.put("value",a?"yes":"no");

			}
		};


		new Definition("HistoryHysterectomy_PPI",Document.class,$s){
			public void define(Session $s) throws Exception {

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"PPI");

				String codeText="if(!d.isDefined(\"hysterectomy\"))d.put(\"hysterectomy\",Utils.StringNormMatch(d.get(\"RESPONSE\"),(\"noihavehadahysterectomy\"))?\"true\":\"false\");";
				ds.setInferText(codeText);
				Document d=ds.get(0, "HistoryHysterectomy_PPI", $s, "hysterectomy", "true");
				d.put("value",d.get("hysterectomy"));
			}
		};

		new Definition("HistoryHysterectomy_SIRS",Document.class,$s){
			public void define(Session $s) throws Exception {

				Dictionary dict=(Dictionary)$s.getObject(Dictionary.class,"Hysterectomy_SIRS_Dictionary");

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"SIRS");
				String codeText="if(!d.isDefined(\"hysterectomy\"))d.put(\"hysterectomy\",(((Dictionary)s.getObjectNoException(Dictionary.class,\"Hysterectomy_SIRS_Dictionary\")).keyNormMatches(d.get(\"PROCEDUREDESCRIPTION\")))?\"true\":\"false\");";
				ds.setInferText(codeText);
				Document d=ds.get(0, "HistoryHysterectomy_SIRS", $s, "hysterectomy", "true");
				d.put("value",d.get("hysterectomy"));
			}

		};

		new Definition("Hysterectomy_SIRS_Dictionary",Dictionary.class,$s){
			public void define(Session $s) throws Exception {

				Dictionary dict= new Dictionary("Hysterectomy_SIRS_Dictionary",$s);
				dict.put("TOTAL ABD HYSTERECTOMY","hysterectomy");
				dict.put("OB CESAREAN HYST W/RECOVERY","hysterectomy");
				dict.put("ROBOTIC ABD HYSTERECTOMY TOT","hysterectomy");
				dict.put("ROBOTIC RAD ABD HYSTERECTOMY","hysterectomy");
				dict.put("RADICAL ABD HYSTERECTOMY","hysterectomy");
				dict.put("ROBOTIC RAD VAG HYSTERECTOMY","hysterectomy");
				dict.put("ROBOTIC VAGINAL HYSTERECTOMY","hysterectomy");
				dict.put("VAGINAL HYSTERECTOMY","hysterectomy");
				dict.put("LAPAROSCOPIC HYSTERECTOMY", "hysterectomy") ;
			}
		};
		


		new Definition("PapDispositionLast",Document.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"Dispositions");
				//convert elsewhereflg tl
				Infer f=new Infer("PapPerformedOutside"){
					public String run(Document d, Session $s) throws Exception {
						return d.get("ELSEWHERE_FLAG");
					}
				};
				ds.addToExpansionMap(f);
				Document d=ds.get(0, "PapDispositionLast", $s,"SERVICE" , "PAP");
				if(d.get("ELSEWHERE_FLAG").equals("True")){
					GregorianCalendar timeOfDisposition=Utils.getGregorianCalendarFromString(d.get("ELSEWHERE_DTM"));
					d.put("PapPerformedOutsideTimeSince",(Utils.timeSince(timeOfDisposition,$s)).toString());
					d.put("PapPerformedOutsideTimeMs",(Utils.getGregorianCalendarFromString(d.get("ELSEWHERE_DTM")).getTimeInMillis()+""));
				}
			}
		};

		new Definition("HistoryCervicalCancer",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d=null;
				boolean a= ( ((d=$s.getDocument("HistoryCervicalCancer_PL")).get("cervicalCancer").equals("true")));
				Document.createLabelledDoc("HistoryCervicalCancer",$s).put("value",a?"yes":"no");
			}
		};

		new Definition("HistoryCervicalCancer_PL",Document.class,$s){
			public void define(Session $s) throws Exception {
				((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"HistoryCervicalCancer_PL",$s,"cervicalCancer","yes");
			}
		};

		new Definition("HistoryCervicalCancer_Pathology",Document.class,$s){
			public void define(Session $s) throws Exception {

				Document d1=Document.createLabelledDoc("HistoryCervicalCancer_Pathology", $s);
				int counter =0;
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(counter, "CervicalCytology"+counter, $s, "CervicalCytology", "true");
				boolean a=false;
				while(d.isReportFound()){
					if($s.get("CervicalCytology"+counter+".CytologyType").equals("cancer")) a=true;
					counter++;
					d=ds.get(counter, "CervicalCytology"+counter, $s, "CervicalCytology", "true");
				}
				d1.put("value",a?"yes": "no");

			}
		};

		new Definition("HistoryCIN23",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HistoryCIN23",$s);

				String value="no";
				int i=0;
				DocSet ds = null ;
				Document d = null ;
				ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				d=ds.get(i, "CervicalCytologyRep"+i, $s, "CervicalCytology", "true");
				while(d.isReportFound()){
					i++;
					if(d.get("CIN23").equals("true")) {
						value="yes";
						d1.put("docTime",d.get("time"));
						d1.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
						break;
					}
					else if((d.get("content").contains("CIN 2")
							|| d.get("content").contains("CIN2")
							|| d.get("content").contains("CIN 3")
							|| d.get("content").contains("CIN3")
							|| d.get("content").contains("CIN II")
							|| d.get("content").contains("CINII")
							|| d.get("content").contains("CIN III")
							|| d.get("content").contains("CINIII")
							|| d.get("content").contains("CIN23"))) {
						value="yes";
						d1.put("docTime",d.get("time"));
						d1.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
						break;
					}
					d=ds.get(i, "CervicalCytologyRep"+i, $s, "CervicalCytology", "true");
				}

				d1.put("value", value);

			}
		};


		new Definition("HistoryCIN23Biopsy",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HistoryCIN23Biopsy",$s);

				String value="no";
				int i=1;
				DocSet ds = null ;
				Document d = null ;
				ds=(DocSet)$s.getObject(DocSet.class,"CervicalBiopsyReport");
				d=ds.get(i, "CervicalBiopsyReport"+i, $s, "CervicalBiopsyReport", "true");
				while(d.isReportFound()){
					i++;
					if(d.get("CIN23").equals("true")) {
						value="yes";
						d1.put("docTime",d.get("time"));
						d1.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
						break;
					}
					else if((d.get("content").contains("CIN 2")
							|| d.get("content").contains("CIN2")
							|| d.get("content").contains("CIN 3")
							|| d.get("content").contains("CIN3")
							|| d.get("content").contains("CIN II")
							|| d.get("content").contains("CINII")
							|| d.get("content").contains("CIN III")
							|| d.get("content").contains("CINIII")
							|| d.get("content").contains("CIN23"))) {
						value="yes";
						d1.put("docTime",d.get("time"));
						d1.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
						break;
					}
					d=ds.get(i, "CervicalBiopsyReport"+i, $s, "CervicalBiopsyReport", "true");
				}

				d1.put("value", value);

			}
		};

		new Definition("HistoryCIN23_PL",Document.class,$s){
			public void define(Session $s) throws Exception {

				Document d=((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"HistoryCIN23_PL",$s,"cin23history","yes");

				d.put("value", "no") ;
				if(d.get("cin23history").equals("yes")) {
					d.put("value","yes");
					d.put("Conditions", "cin23history");
					if (d.get("time") != null || !d.get("time").equals("notDefined")) {
						d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
						d.put("docTime", d.get("time"));
					}
				}

			}
		};

		new Definition("HistoryCIN23Status",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a= false ;
				Document d=Document.createLabelledDoc("HistoryCIN23Status",$s);
				if (($s.equals("HistoryCIN23Biopsy.value","yes"))) {
					a = true ;

					d.put("time" , $s.get("HistoryCIN23Biopsy.docTime")) ;
					d.put("timeSince" , String.format("%2.2f",$s.timeSince($s.get("HistoryCIN23Biopsy.docTime")))) ;
				} 
				else if ($s.equals("HistoryCIN23_PL.value","yes")) {
					a = true ;
					d.put("time" , $s.get("HistoryCIN23_PL.docTime")) ;
					d.put("timeSince" , String.format("%2.2f",$s.timeSince($s.get("HistoryCIN23_PL.docTime")))) ;

				}


				if (a) {
					d.put("value","yes");
				} else {
					d.put("value","no");
				}
			}
		};

		new Definition("HistoryCIN23_5Years",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a = false ;
				Document d1=Document.createLabelledDoc("HistoryCIN23_5Years", $s);
				if (a) a=(Double.parseDouble($s.get("HistoryCIN23.timeYears"))<=5.0);
				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HistoryCIN23_5_25_Years",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a = false ;
				Document d1=Document.createLabelledDoc("HistoryCIN23_5_25_Years", $s);
				if (a) a=(Double.parseDouble($s.get("HistoryCIN23.timeYears"))>=25.0);
				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HistoryCIN23_After25_Years",Document.class,$s){
			public void define(Session $s) throws Exception {
				boolean a = false ;
				Document d1=Document.createLabelledDoc("HistoryCIN23_After25_Years", $s);
				if (a) a=(Double.parseDouble($s.get("HistoryCIN23.timeYears"))>=25.0);
				d1.put("value",a?"yes": "no");
			}
		};	 

		new Definition("LastTwoCytologies_negative",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("LastTwoCytologies_negative", $s);
				boolean a=($s.equals("LastCervicalCytology.CytologyType","negative")
						& $s.equals("PreviousCervicalCytology.CytologyType","negative")
						);
				d1.put("value",a?"yes": "no");
			}
		};


		new Definition("History_HSIL_ASCH_or_AGC",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("History_HSIL_ASCH_or_AGC", $s);
				int i=0;
				int cnt=0 ;
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(i, "CervicalCytologyHist"+i, $s, "CervicalCytology", "true");

				while(d.isReportFound()){
					if (d.get("HSIL").equals("true")
							||d.get("ASCH").equals("true")
							||d.get("AGC").equals("true")) {
						cnt++ ;
						if (cnt >=1) {
							break ;
						}

					}
					i++;
					d=ds.get(i, "CervicalCytologyHist"+i, $s, "CervicalCytology", "true");
				}

				if (cnt >=1 ) {
					d1.put("value","yes");
				} else {
					d1.put("value","no");
				}

			}
		};


		/**
		 * 
		 */

		new Definition("AnyPrevious3Cytologies_HSIL_ASCH_or_AGC",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("AnyPrevious3Cytologies_HSIL_ASCH_or_AGC", $s);
				int i=0;
				int cnt=0 ;
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(i, "CervicalCytology"+i, $s, "CervicalCytology", "true");

				while(d.isReportFound()){
					if (d.get("HSIL").equals("true")
							||d.get("ASCH").equals("true")
							||d.get("AGC").equals("true")) {
						cnt++ ;
						if (cnt ==3) { break ; }

					}
					i++;
					d=ds.get(i, "CervicalCytology"+i, $s, "CervicalCytology", "true");
				}

				if (cnt >=3 ) {
					d1.put("value","yes");
				} else {
					d1.put("value","no");
				}

			}
		};

		new Definition("ThreeCotestsInLastFiveYears",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("ThreeCotestsInLastFiveYears", $s);
				boolean a=($s.equals("LastCervicalCytology.HpvTestType", "cotest")
						& $s.equals("PreviousCervicalCytology.HpvTestType", "cotest")
						& $s.equals("PreviousToPreviousCervicalCytology.HpvTestType", "cotest")
						& $s.isReportFound("PreviousToPreviousCervicalCytology")
						);

				if (a) a=(Double.parseDouble($s.get("LastCervicalCytology.timeYears"))==1.0);
				if (a) a=(Double.parseDouble($s.get("PreviousCervicalCytology.timeYears"))==2.0);
				if (a) a=(Double.parseDouble($s.get("PreviousToPreviousCervicalCytology.timeYears"))==5.0);

				d1.put("value",a?"yes": "no");
			}
		};


		new Definition("ThreeConsecutiveNegativePapInLast10years",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("ThreeConsecutiveNegativePapInLast10years", $s);
				boolean a=($s.equals("LastCervicalCytology.CytologyType", "negative")
						& $s.equals("PreviousCervicalCytology.CytologyType", "negative")
						& $s.equals("PreviousToPreviousCervicalCytology.CytologyType", "negative")
						& $s.isReportFound("PreviousToPreviousCervicalCytology")
						);
				if (a) a=(Double.parseDouble($s.get("PreviousToPreviousCervicalCytology.timeYears"))<10.0);

				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("TwoConsecutiveNegativeHpvInLast10years",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("TwoConsecutiveNegativeHpvInLast10years", $s);
				boolean a=($s.equals("HpvTestLast.HpvTest", "negative")
						& $s.equals("HpvTestPrevious.HpvTest", "negative")
						& $s.isReportFound("HpvTestPrevious"));
				if(a)a= (Double.parseDouble($s.get("HpvTestPrevious.timeYears"))<10.0);
				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("LastTwoHpv",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("LastTwoHpv", $s);
				boolean a=($s.equals("HpvLastNegative.value", "yes")
						& $s.equals("HpvPreviousNegative.value", "yes"));

				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HpvTest_Labs",DocSet.class,$s){
			public void define(Session $s) throws Exception {

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"Labs");

				Infer f= new Infer("HpvTest_Labs"){
					public String run(Document d,Session s) throws Exception{
						if( d.get("OBSERVATION").equals("HPV High Risk Types-PHDICT")){
							d.put("HpvTestType","cotest");
							return "true";
						}
						else { return "false";
						}
					}
				};

				ds.getSubset($s, "HpvTest_Labs",f);
			}
		};

		new Definition("HpvTest_LabsAndReports",DocSet.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=DocSet.newInstance($s, "HpvTest_LabsAndReports");
				ds.merge((DocSet)$s.getObject(DocSet.class,"HpvTest_Labs"));
				ds.merge((DocSet)$s.getObject(DocSet.class,"CervicalCytology"));

				Infer f= new Infer("HpvTest_LabsAndReports"){
					public String run(Document d,Session s) throws Exception{
						return ( d.get("HpvTest_Labs").equals("true")
								|| d.get("name").equals("Cytology - Pap Smear"))
								?"true":"false";
					}
				};
				ds.setInfer(f);
			}
		};

		new Definition("HpvTestLast",Document.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"HpvTest_LabsAndReports");
				Document d=ds.get(0, "HpvTestLast", $s,"HpvTest_LabsAndReports" , "true");
				if (d.isReportFound()) {
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
					d.put("timeMS", d.get("time"));
				}
			}
		};

		new Definition("HpvLastPositive",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HpvLastPositive", $s);
				boolean a=($s.equals("HpvTestLast.HpvTest", "positive")) ;

				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HpvLastNP",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HpvLastNP", $s);
				boolean a=($s.equals("HpvTestLast.HpvTest", "not_performed")) ;

				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HpvLastNegative",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HpvLastNegative", $s);
				boolean a=($s.equals("HpvTestLast.HpvTest", "negative")) ;

				d1.put("value",a?"yes": "no");
			}
		};


		new Definition("HpvTestPrevious",Document.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"HpvTest_LabsAndReports");
				Document d=ds.get(1, "HpvTestPrevious", $s,"HpvTest_LabsAndReports" , "true");
				if (d.isReportFound()) {
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
					d.put("timeMS", d.get("time"));
				}
			}
		};

		new Definition("HpvPreviousPositive",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HpvPreviousPositive", $s);
				boolean a=($s.equals("HpvTestPrevious.HpvTest", "positive")) ;
				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("HpvPreviousNegative",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("HpvPreviousNegative", $s);
				boolean a=($s.equals("HpvTestPrevious.HpvTest", "negative")) ;
				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("Hpv1618Last",Document.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"HpvTest_LabsAndReports");
				Document d=ds.get(0, "Hpv1618Last", $s,"HpvTest_LabsAndReports" , "true");
			}
		};

		new Definition("Hpv1618Positive",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d1=Document.createLabelledDoc("Hpv1618Positive", $s);
				boolean a=($s.equals("Hpv1618Last.Hpv1618", "positive")) ;

				d1.put("value",a?"yes": "no");
			}
		};

		new Definition("Hpv1618Previous",Document.class,$s){
			public void define(Session $s) throws Exception {
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"HpvTest_LabsAndReports");
				Document d=ds.get(1, "Hpv1618Previous", $s,"HpvTest_LabsAndReports" , "true");
			}
		};

		new Definition("LastCervicalCytology",Document.class,$s){
			public void define(Session $s) throws Exception {

				Document demo=(Document)$s.getObject(Document.class,"Demographics");
				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(0, "LastCervicalCytology", $s, "CervicalCytology", "true");
				if(d.isReportFound() && demo.isReportFound()) {
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(d.getTime(),dob)).toString());
					d.put("timeMS", d.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
					if (d.get("HSIL").equals("true") || d.get("ASCH").equals("true")
							|| d.get("AGC").equals("true")) {
						d.put("HSIL_ASCH_or_AGC", "yes");
					} else {
						d.put("HSIL_ASCH_or_AGC", "no");
					}

				} else {
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(demo.getTime(),dob)).toString());
					d.put("timeMS", demo.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(demo.get("time"))));
					d.put("CytologyType", "reportNotFound");
					d.put("HSIL_ASCH_or_AGC", "no");
				}

			}
		};

		new Definition( "PreviousCervicalCytology",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document demo=(Document)$s.getObject(Document.class,"Demographics");
				DocSet ds=(DocSet) $s.getObject(DocSet.class,"CervicalCytology");
				Document d = ds.get(1, "PreviousCervicalCytology", $s, "CervicalCytology", "true");
				if(d.isReportFound()){
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(demo.getTime(),dob)).toString());
					d.put("timeMS", d.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));
					if (d.get("HSIL").equals("true") || d.get("ASCH").equals("true")
							|| d.get("AGC").equals("true")) {
						d.put("HSIL_ASCH_or_AGC", "yes");
					} else {
						d.put("HSIL_ASCH_or_AGC", "no");
					}
				} else {
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(demo.getTime(),dob)).toString());
					d.put("timeMS", demo.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(demo.get("time"))));
					d.put("CytologyType", "reportNotFound");
					d.put("HSIL_ASCH_or_AGC", "no");
				}

			}
		};

		new Definition( "PreviousToPreviousCervicalCytology",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document demo=(Document)$s.getObject(Document.class,"Demographics");
				DocSet ds=(DocSet) $s.getObject(DocSet.class,"CervicalCytology");
				Document d = ds.get(2, "PreviousToPreviousCervicalCytology", $s, "CervicalCytology", "true");
				if(d.isReportFound()){
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(demo.getTime(),dob)).toString());
					d.put("timeMS", d.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(d.get("time"))));

					if (d.get("HSIL").equals("true") || d.get("ASCH").equals("true")
							|| d.get("AGC").equals("true")) {
						d.put("HSIL_ASCH_or_AGC", "yes");
					} else {
						d.put("HSIL_ASCH_or_AGC", "no");
					}
				} else {
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(demo.getTime(),dob)).toString());
					d.put("timeMS", demo.get("time")) ;
					d.put("timeYears", String.format("%2.2f",$s.timeSince(demo.get("time"))));
					d.put("CytologyType", "reportNotFound");
					d.put("HSIL_ASCH_or_AGC", "no");
				}


			}
		};


		new Definition("CervicalCytologySatisfactoryPrevious",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document demo=(Document)$s.getObject(Document.class,"Demographics");

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(1, "CervicalCytologySatisfactoryPrevious", $s, "CervicalCytologySatisfactoryForEval", "true");
				if(d.isReportFound()){
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(d.getTime(),dob)).toString());
				}
			}
		};

		new Definition("CervicalCytologySatisfactoryPreviousToPrevious",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document demo=(Document)$s.getObject(Document.class,"Demographics");

				DocSet ds=(DocSet)$s.getObject(DocSet.class,"CervicalCytology");
				Document d=ds.get(2, "CervicalCytologySatisfactoryPreviousToPrevious", $s, "CervicalCytologySatisfactoryForEval", "true");
				if(d.isReportFound()){
					GregorianCalendar dob= Utils.getGregorianCalendarFromString(demo.get("dob"));
					d.put("AgeAt",(Utils.getAgeAt(d.getTime(),dob)).toString());
				}
			}
		};

		new Definition("CervCancerHighRisk_PL",Document.class,$s){
			public void define(Session $s) throws Exception {

				Document d=((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"CervCancerHighRisk_PL",$s,"cervCancerHighRisk","yes");
				String[] arr={"des","cervicalCancer"};
				for(String condition:arr){

					if(d.get(condition).equals("yes")) {
						d.put("Conditions", condition) ;
					}
				}
				logger.trace(">>>>>>>>"+d);
			}
		};

		new Definition( "CervCancerHighRisk",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d=Document.createLabelledDoc("CervCancerHighRisk", $s);
				String value="no";//default
				String conditions="";

				if($s.equals("CervCancerHighRisk_PL.cervCancerHighRisk","yes")){
					value="yes";conditions=$s.get("CervCancerHighRisk_PL.Conditions");
				}
				d.put("value",value);//XXX change to high average
				d.put("Conditions",conditions);
			}
		};

		new Definition("CervicalCancerHighRisk_PL",Document.class,$s){
			public void define(Session $s) throws Exception {

				Document d=((DocSet)$s.getObject(DocSet.class,"ProblemList")).get(0,"CervicalCancerHighRisk_PL",$s,"cervicalCancerHighRisk","yes");
				String[] arr={"hiv","immuno-deficiency","transplant","des","cervicalCancer"};
				for(String condition:arr){

					if(d.get(condition).equals("yes")) {
						d.put("Conditions", condition) ;
					}
				}
				logger.trace(">>>>>>>>"+d);
			}
		};


		new Definition( "CervicalCancerHighRisk",Document.class,$s){
			public void define(Session $s) throws Exception {
				Document d=Document.createLabelledDoc("CervicalCancerHighRisk", $s);
				String value="no";//default
				String conditions="";

				if($s.equals("CervicalCancerHighRisk_PL.cervicalCancerHighRisk","yes")){
					value="yes";conditions=$s.get("CervicalCancerHighRisk_PL.Conditions");
				}
				d.put("value",value);//XXX change to high average
				d.put("Conditions",conditions);
			}
		};




		////DEFINITIONS_END--------------------------------------------
	}


}

