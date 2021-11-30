package com.github.rooneyandshadows.lightbulb.textinputview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.KeyboardUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings({"unused", "UnusedReturnValue", "FieldCanBeLocal"})
public class TextInputView extends RelativeLayout {

    private final TextInputLayout inputLayout;
    private final TextInputEditText editText;
    private final String inputLayoutTag = "inputLayout";
    private final String editTextTag = "editText";
    private String text = "";
    private String hintText;
    private String errorText;
    private String allowedCharacters;
    private Drawable startIcon;
    private Drawable endIcon;
    private Integer boxStrokeColor;
    private Integer startIconColor;
    private Integer endIconColor;
    private Integer inputBackgroundColor;
    private int hintAppearance;
    private int errorAppearance;
    private int maxLines;
    private int textSize;
    private int maxCharacters;
    private int boxStrokeWidth;
    private int inputTextAlignment;
    private int inputTextDirection;
    private int inputType;
    private int imeOptions;
    private boolean errorEnabled = false;
    private boolean enabled = true;
    private boolean endIconVisible = true;
    private boolean validationEnabled;
    /*CALLBACKS*/
    private TextWatcher textWatcher;
    private OnFocusChangeListener focusChangeListener;
    private final ArrayList<ValidationCallback> validationCallbacks = new ArrayList<>();
    private final ArrayList<TextChangedCallback> textChangedListeners = new ArrayList<>();
    /*ENUMS*/
    private ViewTypes textInputViewType;

    public TextInputView(Context context) {
        this(context, null);
    }

    public TextInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (textInputViewType.equals(ViewTypes.BOXED))
            inflater.inflate(R.layout.view_text_input_boxed, this, true);
        if (textInputViewType.equals(ViewTypes.OUTLINED))
            inflater.inflate(R.layout.view_text_input_outlined, this, true);
        inputLayout = this.findViewWithTag(inputLayoutTag);
        editText = this.findViewWithTag(editTextTag);
        initView();
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TextInputView, 0, 0);
        try {
            /*strings*/
            hintText = a.getString(R.styleable.TextInputView_textInputHintText);
            if (hintText == null || hintText.equals(""))
                hintText = "";
            text = a.getString(R.styleable.TextInputView_textInputText);
            if (text == null || text.equals(""))
                text = "";
            errorText = a.getString(R.styleable.TextInputView_textInputError);
            if (errorText == null || errorText.equals(""))
                errorText = "";
            allowedCharacters = a.getString(R.styleable.TextInputView_textInputAllowedCharacters);
            /*resource reffs*/
            errorAppearance = a.getResourceId(R.styleable.TextInputView_textInputErrorAppearance, R.style.InputView_errorTextAppearance);
            hintAppearance = a.getResourceId(R.styleable.TextInputView_textInputHintAppearance, R.style.InputView_hintTextAppearance);
            /*colors*/
            inputBackgroundColor = a.getColor(R.styleable.TextInputView_textInputBackgroundColor, ResourceUtils.getColorByAttribute(context, R.attr.colorSurface));
            boxStrokeColor = ColorUtils.setAlphaComponent(a.getColor(R.styleable.TextInputView_textInputBoxStrokeColor, ResourceUtils.getColorByAttribute(context, R.attr.colorOnSurface)), 140);
            startIconColor = ColorUtils.setAlphaComponent(a.getColor(R.styleable.TextInputView_textInputStartIconColor, ResourceUtils.getColorByAttribute(context, R.attr.colorOnSurface)), 140);
            endIconColor = ColorUtils.setAlphaComponent(a.getColor(R.styleable.TextInputView_textInputEndIcon, ResourceUtils.getColorByAttribute(context, R.attr.colorOnSurface)), 140);
            /*drawables*/
            startIcon = a.getDrawable(R.styleable.TextInputView_textInputStartIcon);
            endIcon = a.getDrawable(R.styleable.TextInputView_textInputEndIcon);
            /*options*/
            validationEnabled = a.getBoolean(R.styleable.TextInputView_textInputValidationEnabled, false);
            textInputViewType = ViewTypes.valueOf(a.getInt(R.styleable.TextInputView_textInputViewType, 1));
            inputTextAlignment = a.getInteger(R.styleable.TextInputView_textInputAlignment, 0);
            inputTextDirection = a.getInteger(R.styleable.TextInputView_textInputDirection, 0);
            imeOptions = a.getInteger(R.styleable.TextInputView_textInputImeOptions, 0x00000000);
            maxLines = a.getInteger(R.styleable.TextInputView_textInputMaxLines, 1);
            textSize = a.getDimensionPixelSize(R.styleable.TextInputView_textInputTextSize, ResourceUtils.getDimenPxById(context, R.dimen.textInputView_text_size_default));
            boxStrokeWidth = a.getInteger(R.styleable.TextInputView_textInputBoxStrokeWidth, 1);
            if (boxStrokeWidth < 0)
                boxStrokeWidth = 0;
            inputType = a.getInteger(R.styleable.TextInputView_textInputType, 0x00000001);
            if (inputType == InputType.TYPE_CLASS_TEXT)
                inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        } finally {
            a.recycle();
        }
    }

    public void addValidationCheck(ValidationCallback validationCallback) {
        validationCallbacks.add(validationCallback);
    }

    public void addOrReplaceValidationCheck(ValidationCallback validationCallback) {
        validationCallbacks.remove(validationCallback);
        validationCallbacks.add(validationCallback);
    }

    public void removeValidationCheck(ValidationCallback validationCallback) {
        validationCallbacks.remove(validationCallback);
    }

    public void addTextChangedCallback(TextChangedCallback textChangedCallback) {
        textChangedListeners.add(textChangedCallback);
    }

    public void addOrReplaceTextChangedCallback(TextChangedCallback textChangedCallback) {
        textChangedListeners.remove(textChangedCallback);
        textChangedListeners.add(textChangedCallback);
    }

    public void removeTextChangedCallback(TextChangedCallback textChangedCallback) {
        textChangedListeners.remove(textChangedCallback);
    }

    public void setValidationEnabled(boolean newValue) {
        this.validationEnabled = newValue;
        validate();
    }

    private void setErrorEnabled(Boolean errorEnabled) {
        if (this.errorEnabled != errorEnabled) {
            this.errorEnabled = errorEnabled;
            inputLayout.setErrorEnabled(errorEnabled);
        }
    }

    public void setInputType(Integer inputType) {
        this.inputType = inputType;
        editText.setInputType(inputType);
    }

    public void setError(String errorText) {
        if (!Objects.equals(this.errorText, errorText)) {
            this.errorText = errorText;
            inputLayout.setError(errorText);
        }
    }

    public void setInputFilters(InputFilter[] filters) {
        ArrayList<InputFilter> filtersList = new ArrayList<>(Arrays.asList(filters));
        if (allowedCharacters != null) {
            InputFilter filter = (source, start, end, dest, dstart, dend) -> {
                if (end > start) {
                    String filteredVal = "";
                    char[] acceptedChars = allowedCharacters.toCharArray();
                    for (int index = start; index < end; index++) {
                        String currentChar = String.valueOf(source.charAt(index));
                        if (new String(acceptedChars).contains(currentChar))
                            filteredVal = filteredVal.concat(currentChar);
                    }
                    return filteredVal;
                }
                return null;
            };
            filtersList.add(filter);
        }
        InputFilter[] filtersArray = new InputFilter[filtersList.size()];
        for (int i = 0; i < filtersList.size(); i++) {
            filtersArray[i] = filtersList.get(i);
        }
        editText.setFilters(filtersArray);
    }

    public void setStartIconColor(int color) {
        startIconColor = color;
        inputLayout.setStartIconTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused}, // focused
                        new int[]{android.R.attr.state_hovered}, // hovered
                        new int[]{}  //
                },
                new int[]{
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        startIconColor
                }));
    }

    public void setStartIcon(Drawable drawable) {
        startIcon = drawable;
        inputLayout.setStartIconDrawable(startIcon);
        inputLayout.setStartIconTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused}, // focused
                        new int[]{android.R.attr.state_hovered}, // hovered
                        new int[]{}  //
                },
                new int[]{
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        startIconColor
                }));
        //inputLayout.setStartIconTintList(null);
    }

    public void setEndIconVisible(boolean newState) {
        endIconVisible = newState;
        inputLayout.setEndIconVisible(endIconVisible);
    }

    public void setEndIcon(Drawable drawable) {
        setEndIcon(drawable, null);
    }

    public void setEndIcon(Drawable drawable, OnClickListener onClickListener) {
        endIcon = drawable;
        if (onClickListener != null)
            inputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        inputLayout.setEndIconOnClickListener(onClickListener);
        inputLayout.setEndIconDrawable(endIcon);
        inputLayout.setEndIconVisible(endIconVisible);
        inputLayout.setEndIconTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused}, // focused
                        new int[]{android.R.attr.state_hovered}, // hovered
                        new int[]{}  //
                },
                new int[]{
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        endIconColor
                }));
        //inputLayout.setEndIconTintList(null);
    }

    public void setHintText(String text) {
        hintText = text;
        setupHint();
    }

    public void setText(String newVal) {
        if (newVal == null)
            newVal = "";
        String oldValue = text;
        text = newVal;
        String editTextValue = editText.getText() == null ? "" : editText.getText().toString();
        if (!editTextValue.equals(newVal))
            editText.setText(newVal);
        for (TextChangedCallback listener : textChangedListeners)
            listener.onChanged(newVal, oldValue);
        validate();
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public String getError() {
        return editText.getError().toString();
    }

    public String getText() {
        return text;
    }

    public void moveCursorToEnd() {
        if (editText != null && editText.getText() != null && editText.getText().length() > 0)
            editText.setSelection(editText.getText().length());
    }

    public void moveCursorToStart() {
        if (editText != null && editText.getText() != null && editText.getText().length() > 0)
            editText.setSelection(0);
    }

    public void moveCursorAtIndex(int index) {
        if (editText != null && editText.getText() != null && editText.getText().length() > index)
            editText.setSelection(index);
    }

    public boolean validate() {
        boolean isValid = true;
        if (validationEnabled && isEnabled()) {
            for (ValidationCallback validationCallback : validationCallbacks)
                isValid &= validationCallback.execute(getText());
        }
        if (!isValid) {
            setErrorEnabled(true);
        } else {
            setErrorEnabled(false);
            setError(null);
        }
        return isValid;
    }

    public void showKeyboard() {
        editText.post(() -> {
            TextInputView.this.requestFocus();
            KeyboardUtils.showKeyboard(editText);
        });
    }

    private void initView() {
        initInputLayout();
        initEditText();
        setEnabled(enabled);
    }

    @BindingAdapter("textInputHintText")
    public static void setHint(TextInputView view, String text) {
        view.setHintText(text);
    }

    @BindingAdapter("textInputText")
    public static void setText(TextInputView view, String newText) {
        if (!view.getText().equals(newText))
            view.setText(newText);
    }

    @InverseBindingAdapter(attribute = "textInputText", event = "textAttrChanged")
    public static String getText(TextInputView view) {
        return Objects.requireNonNull(view.editText.getText()).toString();
    }

    @BindingAdapter("textAttrChanged")
    public static void setListeners(TextInputView view, final InverseBindingListener attrChange) {
        view.editText.removeTextChangedListener(view.textWatcher);
        view.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                attrChange.onChange();
            }
        });
    }

    private void initInputLayout() {
        if (textInputViewType.equals(ViewTypes.BOXED))
            inputLayout.setBoxBackgroundColor(inputBackgroundColor);
        inputLayout.setHintTextAppearance(hintAppearance);
        inputLayout.setErrorTextAppearance(errorAppearance);
        inputLayout.setHintAnimationEnabled(false);
        inputLayout.setBoxStrokeWidth(ResourceUtils.dpToPx(boxStrokeWidth));
        inputLayout.setBoxStrokeWidthFocused(boxStrokeWidth > 0 ? ResourceUtils.dpToPx(boxStrokeWidth) : 0);
        inputLayout.setBoxStrokeColorStateList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused}, // focused
                        new int[]{android.R.attr.state_hovered}, // hovered
                        new int[]{}  //
                },
                new int[]{
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorAccent),
                        boxStrokeColor
                }));
        inputLayout.setErrorIconTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_enabled}
                },
                new int[]{
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorError),
                        ResourceUtils.getColorByAttribute(getContext(), R.attr.colorError)
                }));
        inputLayout.setErrorEnabled(errorEnabled);
        if (errorEnabled)
            inputLayout.setError(errorText);
        if (startIcon != null)
            setStartIcon(startIcon);
        if (endIcon != null)
            setEndIcon(endIcon);
        setupHint();
    }

    private void initEditText() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newText = editable.toString();
                if (!getText().equals(newText))
                    setText(editable.toString());
            }
        };
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.post(() -> {
            if (focusChangeListener != null)
                focusChangeListener.onFocusChange(this, hasFocus);
        }));
        editText.setInputType(inputType);
        setInputFilters(new InputFilter[]{});
        editText.setTextAlignment(inputTextAlignment);
        editText.setTextDirection(inputTextDirection);
        editText.setMaxLines(maxLines);
        editText.setImeOptions(imeOptions);
        editText.setTypeface(Typeface.DEFAULT);
        editText.removeTextChangedListener(textWatcher);
        editText.addTextChangedListener(textWatcher);
        editText.setShowSoftInputOnFocus(true);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        if (inputBackgroundColor == Color.TRANSPARENT)
            editText.setBackgroundColor(Color.TRANSPARENT);
        setText(text);
    }

    private void setupHint() {
        boolean hasHint = !StringUtils.isNullOrEmptyString(hintText);
        inputLayout.setHintEnabled(hasHint);
        inputLayout.setHint(hintText);
        editText.setHint(null);
    }

    private enum ViewTypes {
        BOXED(1),
        OUTLINED(2);

        private final int value;
        private static final SparseArray<ViewTypes> map = new SparseArray<>();

        ViewTypes(int value) {
            this.value = value;
        }

        static {
            for (ViewTypes editMode : ViewTypes.values()) {
                map.put(editMode.value, editMode);
            }
        }

        public static ViewTypes valueOf(int pageType) {
            return map.get(pageType);
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        focusChangeListener = l;
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        editText.setOnKeyListener(l);
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        inputLayout.setEnabled(enabled);
        editText.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.text = text;
        myState.enabled = isEnabled();
        myState.inputType = inputType;
        myState.startIconColor = startIconColor;
        myState.editTextFocused = hasFocus();
        myState.errorMessage = errorText;
        myState.errorEnabled = errorEnabled;
        myState.errorAppearance = errorAppearance;
        myState.validationEnabled = validationEnabled;
        myState.allowedCharacters = allowedCharacters;
        myState.endIconVisible = endIconVisible;
        myState.editTextState = editText.onSaveInstanceState();
        SparseArray<Parcelable> inputHierarchyState = new SparseArray<>();
        inputLayout.saveHierarchyState(inputHierarchyState);
        myState.inputLayoutState = inputHierarchyState;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        errorText = savedState.errorMessage;
        errorEnabled = savedState.errorEnabled;
        validationEnabled = savedState.validationEnabled;
        enabled = savedState.enabled;
        errorAppearance = savedState.errorAppearance;
        endIconVisible = savedState.endIconVisible;
        inputType = savedState.inputType;
        startIconColor = savedState.startIconColor;
        text = savedState.text;
        boolean editTextPreviouslyFocused = savedState.editTextFocused;
        inputLayout.restoreHierarchyState(savedState.inputLayoutState);
        editText.onRestoreInstanceState(savedState.editTextState);
        initInputLayout();
        setEnabled(enabled);
        if (editTextPreviouslyFocused)
            requestFocus();
    }

    private static class SavedState extends BaseSavedState {
        private boolean errorEnabled;
        private boolean enabled;
        private boolean validationEnabled;
        private boolean editTextFocused;
        private boolean endIconVisible;
        private int errorAppearance;
        private int startIconColor;
        private String errorMessage;
        private String allowedCharacters;
        private String text;
        private Integer inputType;
        private Parcelable editTextState;
        private SparseArray<Parcelable> inputLayoutState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            errorEnabled = in.readByte() != 0;
            enabled = in.readByte() != 0;
            validationEnabled = in.readByte() != 0;
            editTextFocused = in.readByte() != 0;
            endIconVisible = in.readByte() != 0;
            inputType = in.readInt();
            startIconColor = in.readInt();
            errorAppearance = in.readInt();
            errorMessage = in.readString();
            allowedCharacters = in.readString();
            text = in.readString();
            inputLayoutState = in.readSparseArray(getClass().getClassLoader());
            editTextState = in.readParcelable(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (errorEnabled ? 1 : 0));
            out.writeByte((byte) (validationEnabled ? 1 : 0));
            out.writeByte((byte) (enabled ? 1 : 0));
            out.writeByte((byte) (editTextFocused ? 1 : 0));
            out.writeByte((byte) (endIconVisible ? 1 : 0));
            out.writeInt(inputType);
            out.writeInt(startIconColor);
            out.writeInt(errorAppearance);
            out.writeInt(errorAppearance);
            out.writeString(text);
            out.writeString(errorMessage);
            out.writeString(allowedCharacters);
            out.writeParcelable(editTextState, flags);
            out.writeSparseArray(inputLayoutState);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public interface TextChangedCallback {
        void onChanged(String newValue, String oldValue);
    }

    public interface ValidationCallback {
        boolean execute(String text);
    }
}
