package gov.ornl.csed.cda.util;/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    12 Jul 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */


/*
making a consistent set of time series records for multiple variables sampled at different instants

- get all time series for all variables from file. no sampling, just using data as recorded in the file
- make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
- iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
- write all data to csv (time written as epoch time in millis)
 */


public class plgTOcsvParser {
}
