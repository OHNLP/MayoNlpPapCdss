Kindly read License.txt before using the software. 

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
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *     
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 

The software uses third party libraries and the users are required to verify the individual 
licenses.The software is provided AS IS and does not provide any guarantee for performances.

Instructions to compile the code using maven
********************************************

1) Cd to the project directory MayoNlpPapCdss
2) mvn package

NOTE: Pre-compiled version of CDSS already available in "target" folder

Instructions to run the CDSS code
*********************************

Shell script runPapASCPP.sh can be used to run the CDSS pipeline.
List of patient ids needs to be provided in the file patient_id.txt inside "input_data" folder.

Run the following command in bash shell.

$sh runPapASCPP.sh
