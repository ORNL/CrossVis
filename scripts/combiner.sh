# CsvFileMerger Usage
# ====================
#
# java -Xmx8g -cp ./falcon-0.2.3-jar-with-dependencies.jar gov.ornl.csed.cda.util.CsvFileMerger  {Appendee File}.csv  {Appender File}.csv  {Merged Filename}.csv  [Appendee Key Column]  [Appender Key Column]
#
#
# Command Line Argument Descriptions
#
# Appendee File - CSV file from PLG data
# Appender File - CSV file of porosity data
# Merged Filename - Name of merged output file
# Appendee Key Column - Column number of build height values (1-indexed)
# Appender Key Column - Column number of build indices (1-indexed)

usage=

if [ $# -ne 5 ]
then
	java -Xmx8g -cp ../target/falcon-0.3.0-jar-with-dependencies.jar gov.ornl.csed.cda.util.CsvFileMerger
	exit
fi

java -Xmx8g -cp ../target/falcon-0.3.0-jar-with-dependencies.jar gov.ornl.csed.cda.util.CsvFileMerger $1 $2 $3 $4 $5

