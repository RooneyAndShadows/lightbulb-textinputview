package com.github.RooneyAndShadows.lightbulb.textinputview.watchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * An extension of TextWatcher which stops further callbacks being called as
 * a result of a change happening within the callbacks themselves.
 */
public abstract class EditableTextWatcher implements TextWatcher {

    private EditText editText;
    private boolean editing;

    public EditableTextWatcher(EditText editText) {
        this.editText = editText;
    }

    protected abstract void afterTextChange(Editable s);

    protected abstract void beforeTextChange(CharSequence s, int start, int count, int after);

    protected abstract void onTextChange(CharSequence s, int start, int before, int count);

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (editing)
            return;
        editing = true;
        try {
            beforeTextChange(s, start, count, after);
        } finally {
            editing = false;
        }
    }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
        if (editing)
            return;
        editing = true;
        try {
            onTextChange(s, start, before, count);
        } finally {
            editing = false;
        }
    }

    @Override
    public final void afterTextChanged(Editable s) {
        if (editing)
            return;
        editing = true;
        try {
            afterTextChange(s);
        } finally {
            editing = false;
        }
    }

    public boolean isEditing() {
        return editing;
    }

}