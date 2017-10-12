#!/bin/sh

java -Xmx2G -Xms2G -cp target/MayoNlpPapCdss-1.0-SNAPSHOT.jar:target/MayoNlpPapCdss-1.0-SNAPSHOT-jar-with-dependencies.jar:lib_others/*.jar:lib/FlowDS-0.1.jar:lib/cidb-0.2.jar:lib/db2jcc.jar:lib/regexRule-1.0-SNAPSHOT.jar:lib/db2jcc_license_cu.jar:lib/json-lib-2.4-jdk15.jar:lib/junit-4.10.jar:resources:resources/proj/PaP_ASCCP edu.mayo.bmi.nlpcdsproj.projects.PaP_ASCCP
