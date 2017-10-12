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

import java.sql.SQLException;
import java.util.Properties;

import org.ohnlp.cidb.core.Session;
import org.ohnlp.cidb.exception.CIDBException;
import org.ohnlp.cidb.io.Sql;

/*
 * @author Kavishwar Wagholikar (waghsk)
 */
public class SqlBuilder{



	String driverClassName;
	String connectionUrl ;
	String sql;
	String label;
	Session session;
	Properties properties;
	
	public SqlBuilder(String label,Session session){
	    this.label=label;
	    this.session=session;
	}
	public SqlBuilder setDriverClassName(String driverClassName) {
	    this.driverClassName = driverClassName;
	    return this;
	}
	public SqlBuilder setConnectionUrl(String connectionUrl) {
	    this.connectionUrl = connectionUrl;
	    return this;
	}
	
	public SqlBuilder setConnectionUrl(String connectionUrl,Properties p) {
	    this.connectionUrl = connectionUrl;
	    this.properties = p;
	    return this;
	}
	
	public SqlBuilder setSql(String sql) {
	    this.sql = sql;
	    return this;
	}
	
	public SqlBuilder setLabel(String label) {
	    this.label = label;
	    return this;
	}
	
	
	public Sql build() throws CIDBException, SQLException {
	    return new Sql(this);
	}
	
	@Override
	public String toString() {
	    return "\nBuilder [driverClassName=" + driverClassName
		    + "\n connectionUrl=" + connectionUrl + "\n sql=" + sql
		    + "\n label=" + label + "\n session=" + session + "]";
	}
}