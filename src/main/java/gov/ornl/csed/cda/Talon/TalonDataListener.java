package gov.ornl.csed.cda.Talon;

/*
 *
 *  Interface:  TalonDataListener
 *
 *      Author:     whw
 *
 *      Created:    11 May 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 */

public interface TalonDataListener {
    public void TalonDataPlgFileChange();

    public void TalonDataSegmentingVariableChange();

    public void TalonDataSegmentedVariableChange();

    public void TalonDataReferenceValueChange();

    public void TalonDataImageDirectoryChange();
}
