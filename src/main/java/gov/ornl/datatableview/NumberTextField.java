package gov.ornl.datatableview;

import javafx.scene.control.TextField;

/**
 * Created by csg on 8/25/16.
 */
public class NumberTextField extends TextField {
    @Override
    public void replaceText(int start, int end, String text) {
        if (validate(text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (validate(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text) {
        // TODO: Allow negative numbers; way to allow negative but not require negative
        return text.matches("[0-9]*");
    }
}
