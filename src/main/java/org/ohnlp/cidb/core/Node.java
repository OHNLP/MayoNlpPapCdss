package org.ohnlp.cidb.core;

import java.util.ArrayList;
import java.util.Stack;

/*
 * To interpret concepts
 * @author Kavishwar Wagholikar (waghsk)
 */
public class Node {
    	
	private String id;
	/*
	 * Parameter name
	 */
	private String concept;

	
	
	public Node(String id){
	    this.id=id;
	    this.concept=null;
	}
	
	
	
	


	
	public String toString() {
	    
		 
	    
	    return "Node [id=" + id + ", concept=" + concept  + "]"	    ;
	}

	
	

	public String getConcept() {
	    return this.concept;
	}
	public String getPrefix(){
	    return this.concept.split("\\.")[0];
	}
	public String getSuffix(){
	    if(this.concept.indexOf(".")==-1) throw new IllegalArgumentException("concept should match regex \\w+.\\w:"+this.concept);
	    return this.concept.split("\\.")[1];
	}
	public void setConcept(String cnpt) {
	    if(!cnpt.matches("\\w+.\\w+")) throw new IllegalArgumentException("concept should match regex \\w+.\\w:"+this.concept);
	    this.concept = cnpt;
	}
	
	
	

	
	


	
	

	
	
	
	
	
	
	

	
	
	


}
