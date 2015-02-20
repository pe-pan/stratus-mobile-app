package com.hp.dsg.stratus;

import android.view.View;
import android.widget.EditText;

/**
 * Created by panuska on 20.2.2015.
 */
public class ViewUtils {

    public static final View.OnFocusChangeListener SELECT_LOCAL_PART_OF_EMAIL_ADDRESS = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                EditText text = (EditText)v;
                String oldValue = text.getText().toString();
                int index = oldValue.indexOf('@');
                if (index < 0) index = oldValue.length();
                text.setSelection(0, index); // select up to the @ char or whole string

            }
        }
    };
}
