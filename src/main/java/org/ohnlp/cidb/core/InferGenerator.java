package org.ohnlp.cidb.core;

import java.util.HashMap;

import org.ohnlp.cidb.core.*;
import org.ohnlp.cidb.exception.*;
import org.ohnlp.cidb.io.*;
import org.ohnlp.cidb.misc.*;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
		
public class InferGenerator extends Infer{
    
	public InferGenerator() {
	super("tmp");
	// TODO Auto-generated constructor stub
    }

	Infer f;
	Logger logger= LoggerFactory.getLogger(this.getClass().getSimpleName());
	/**
	 * @param d
	 * @return 
	 */
	public void setName(String name){
		this.name=name;
	}
	
	public String run(Document d,Session s) throws Exception{
			return "testvalue1";
	}

}