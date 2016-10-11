# BatchCombine.sh written by William Halsey
# whw@ornl.gov
#
# created: 20 Sept. 2016
#
# Description: 
# Script to batch combine PLG and pore data
# Employs the CsvFileMerger Java Class

# PARAMETERS FOR THIS SCRIPT
# $1 - appendee file directory
# $2 - appender file directory
# $3 - CSV directory for output
# $4 - appendee key column
# $5 - appender key column

APPENDEE_DIR=$1
APPENDER_DIR=$2
CSV_DIR=$3
APPENDEE_KEY=$4
APPENDER_KEY=$5

# create the csv directory
# need to iterate over all of the PLG files in a directory
# for each file
# -> call the command line version of the parser
# ! with parameters passed to this script
# ! current PLG file in the directory
# ! corresponding CSV file

# == START == 

# create the csv directory
HOME=$(pwd)

echo "$HOME"

mkdir -p $CSV_DIR

cd $PLG_DIR

for F in *
do

echo "Processing $F ..."
java -Xmx8g -cp $HOME/../target/falcon-0.3.1-jar-with-dependencies.jar gov.ornl.csed.cda.util.PlgToCsvParser $PARSER_TYPE "$PLG_DIR/$F" "$CSV_DIR/$F.csv" "$TEMPLATE" $DURATION

done

cd $HOME
