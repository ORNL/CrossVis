# BatchParse.sh written by William Halsey
# whw@ornl.gov
#
# created: 15 Sept. 2016
#
# Description: 
# Script to batch parse PLG files to CSV files
# Employs the PlgToCsvParser Java Class

# PARAMETERS FOR THIS SCRIPT
# $1 - parser type
# $2 - PLG directory to batch process
# $3 - CSV directory for output
# $4 - template file name
# $5 - sample duration time

PARSER_TYPE=$1
PLG_DIR=$2
CSV_DIR=$3
TEMPLATE=$4
DURATION=$5

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

echo "Parsing $F ..."
java -Xmx8g -cp $HOME/../target/falcon-0.3.1-jar-with-dependencies.jar gov.ornl.csed.cda.util.PlgToCsvParser $PARSER_TYPE "$PLG_DIR/$F" "$CSV_DIR/$F.csv" "$TEMPLATE" $DURATION

done

cd $HOME
