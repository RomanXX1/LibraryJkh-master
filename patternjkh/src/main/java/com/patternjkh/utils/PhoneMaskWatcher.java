package com.patternjkh.utils;

import android.text.Editable;
import android.text.TextWatcher;

public class PhoneMaskWatcher implements TextWatcher {
    private boolean isRunning = false;
    private boolean isDeleting = false;
    private final String mask;

    public PhoneMaskWatcher(String mask) {
        this.mask = mask;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        isDeleting = count > after;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() == 1) {
            String text = editable.toString();
            if (!text.equals("+")) {
                int pos = 0;
                editable.replace(pos, pos + 1, "+7");
            }
        } else {
            if (isRunning || isDeleting) {
                return;
            }
            isRunning = true;

            int editableLength = editable.length();
            if (editableLength < mask.length()) {
                if (mask.charAt(editableLength) != '#') {
                    editable.append(mask.charAt(editableLength));
                } else if (mask.charAt(editableLength-1) != '#') {
                    editable.insert(editableLength-1, mask, editableLength-1, editableLength);
                }
            }

            isRunning = false;
        }
    }
}