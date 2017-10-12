Kindly read License.txt before using the software. 

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
