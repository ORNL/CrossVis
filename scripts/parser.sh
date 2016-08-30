# PlgToCsvParser Usage
# ====================
# 
# java -Xmx8g -cp ./falcon-0.2.3-jar-with-dependencies.jar gov.ornl.csed.cda.util.PlgToCsvParser  [Parser Type]  {PLG file path}.plg  {CSV file path}.csv  {Variables names file path}.txt  [Sample Duration in ms]
#
#
# Command Line Argument Descriptions
#
# Parser Type - 1 (Sampled): Features constructed by regularly sampling PLG values, 2 (Lossless): Features constructed every time a value is updated
# PLG file path - Full path to the desired PLG input file
# CSV file path - Full path to the desired CSV output file
# Variables name file path - Full path to text file containing desired variable names; one variable name per line
# Sample Duration in ms - Duration in between regular sampling. Must be a whole number. This value is disregarded for Parser Type 2

if [ $# -ne 5 ]
then
	java -Xmx8g -cp ../target/falcon-0.3.1-jar-with-dependencies.jar gov.ornl.csed.cda.util.PlgToCsvParser

else
    java -Xmx8g -cp ../target/falcon-0.3.1-jar-with-dependencies.jar gov.ornl.csed.cda.util.PlgToCsvParser $1 $2 $3 $4 $5

fi


