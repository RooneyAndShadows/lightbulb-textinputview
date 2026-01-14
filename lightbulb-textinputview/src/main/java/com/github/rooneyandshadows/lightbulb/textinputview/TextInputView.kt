package com.github.rooneyandshadows.lightbulb.textinputview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.github.rooneyandshadows.lightbulb.commons.utils.ParcelableUtils
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils
import com.github.rooneyandshadows.lightbulb.textinputview.TextInputView.ViewTypes.BOXED
import com.github.rooneyandshadows.lightbulb.textinputview.TextInputView.ViewTypes.OUTLINED
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max

@Suppress("unused")
class TextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val defStyleAttr: Int = R.attr.textInputViewStyle,
    private val defStyleRes: Int = R.style.Lightbulb_TextInputView_Boxed,
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val inputLayout: TextInputLayout by lazy {
        findViewById(R.id.textInputLayout)
    }
    private val editText: TextInputEditText by lazy {
        findViewById(R.id.textInputEditText)
    }
    private var allowedCharacters: String = ""
    private var maxCharactersCountLimit: Int = 0
    private var validationEnabled: Boolean = false
    private var bindingListener: TextChangedCallback? = null
    private val inputFilters: MutableList<InputFilter> = mutableListOf()
    private val textChangedListeners: MutableList<TextChangedCallback> = mutableListOf()
    private val validationCallbacks: MutableList<ValidationCallback> = mutableListOf()
    private lateinit var allowedCharactersInputFilter: InputFilter
    private lateinit var textWatcher: TextWatcher

    init {
        val resolvedAttributes = readAttributes(context, attrs)
        inflateLayout(resolvedAttributes.viewType)
        initAllowedCharactersInputFilter()
        initTextWatcher()
        applyAttributes(resolvedAttributes)
    }

    fun setText(newVal: String) {
        val oldValue: String = editText.text?.toString() ?: ""
        if (oldValue != newVal) {
            editText.setText(newVal)
        }

        validate()
    }

    fun getText(): String {
        return editText.text?.toString() ?: ""
    }

    fun setHintText(hintText: String) {
        inputLayout.isHintEnabled = true
        inputLayout.hint = hintText
        editText.hint = null
    }

    fun getHintText(): String {
        return inputLayout.hint?.toString() ?: ""
    }

    fun setSuffixText(suffixText: String) {
        inputLayout.suffixText = suffixText
    }

    fun getSuffixText(): String {
        return inputLayout.suffixText?.toString() ?: ""
    }

    fun setAllowedCharacters(allowedCharacters: String) {
        this.allowedCharacters = allowedCharacters
        syncInputFilters()
    }

    fun getAllowedCharacters(): String {
        return this.allowedCharacters
    }

    fun setHintTextAppearance(@StyleRes resId: Int) {
        inputLayout.setHintTextAppearance(resId)
    }

    fun setErrorTextAppearance(@StyleRes resId: Int) {
        inputLayout.setErrorTextAppearance(resId)
    }

    fun setBoxStrokeColorStateList(boxStrokeColorStateList: ColorStateList) {
        inputLayout.setBoxStrokeColorStateList(boxStrokeColorStateList)
    }

    fun setBoxStrokeColor(@ColorInt boxStrokeColor: Int) {
        inputLayout.boxStrokeColor = boxStrokeColor
    }

    fun getBoxStrokeColor(): Int {
        return inputLayout.boxStrokeColor
    }

    fun setBoxStrokeWidth(width: Int) {
        var newWidth = width
        if (newWidth <= 0) {
            newWidth = 1
        }

        inputLayout.boxStrokeWidth = ResourceUtils.dpToPx(newWidth)
    }

    fun getBoxStrokeWidth(): Int {
        return inputLayout.boxStrokeWidth
    }

    fun setStartIcon(icon: Drawable?) {
        icon?.apply {
            inputLayout.isStartIconVisible = true
            inputLayout.startIconDrawable = this
        }
    }

    fun setStartIconColor(iconColor: ColorStateList) {
        inputLayout.setStartIconTintList(iconColor)
    }

    fun setEndIcon(icon: Drawable?) {
        icon?.apply {
            inputLayout.isEndIconVisible = true
            inputLayout.endIconDrawable = this
        }
    }

    fun setEndIconColor(iconColor: ColorStateList) {
        inputLayout.setEndIconTintList(iconColor)
    }

    fun setErrorText(errorText: String) {
        inputLayout.isErrorEnabled = errorText.isNotEmpty()
        inputLayout.error = errorText
    }

    fun setMaxCharacters(maxCharacters: Int) {
        var limit = maxCharacters
        if (limit < 0) limit = 0
        this.maxCharactersCountLimit = limit
        syncInputCounter()
        syncInputFilters()
    }

    fun getMaxCharacters(): Int {
        return this.maxCharactersCountLimit
    }

    fun setLines(min: Int, max: Int) {
        val minLines = max(1, min)
        var maxLines = max(1, max)

        if (minLines > maxLines) {
            maxLines = minLines
        }
        editText.maxLines = maxLines
        editText.minLines = minLines
        editText.inputType = resolveMultiLineFlags(getInputType())
    }

    fun getMaxLines(): Int {
        return editText.maxLines
    }

    fun getMinLines(): Int {
        return editText.minLines
    }

    fun setCharacterCounterEnabled(enabled: Boolean) {
        inputLayout.isCounterEnabled = enabled
        this.syncInputCounter()
    }

    fun isCharacterCounterEnabled(): Boolean {
        return inputLayout.isCounterEnabled
    }

    fun setValidationEnabled(enabled: Boolean) {
        this.validationEnabled = enabled
    }

    fun isValidationEnabled(): Boolean {
        return this.validationEnabled
    }

    fun setInputTextDirection(direction: Int) {
        editText.textDirection = direction
    }

    fun getInputTextDirection(): Int {
        return editText.textDirection
    }

    fun setInputTextAlignment(textAlignment: Int) {
        editText.textAlignment = textAlignment
    }

    fun getInputTextAlignment(): Int {
        return editText.textAlignment
    }

    fun setInputType(inputType: Int) {
        editText.inputType = resolveMultiLineFlags(inputType)
    }

    fun getInputType(): Int {
        return editText.inputType
    }

    fun setImeOptions(options: Int) {
        editText.imeOptions = options
    }

    fun getImeOptions(): Int {
        return editText.imeOptions
    }

    fun validate(): Boolean {
        var isValid = true
        if (isEnabled && validationEnabled) {
            for (validationCallback in validationCallbacks) {
                isValid = isValid and validationCallback.execute(editText.text.toString())
            }
        }
        if (!isValid) {
            inputLayout.isErrorEnabled = true
        } else {
            inputLayout.isErrorEnabled = false
            inputLayout.error = null
        }
        return isValid
    }

    fun addValidationCheck(validationCallback: ValidationCallback) {
        validationCallbacks.add(validationCallback)
    }

    fun addOrReplaceValidationCheck(validationCallback: ValidationCallback) {
        validationCallbacks.remove(validationCallback)
        validationCallbacks.add(validationCallback)
    }

    fun removeValidationCheck(validationCallback: ValidationCallback) {
        validationCallbacks.remove(validationCallback)
    }

    fun addTextChangedCallback(textChangedCallback: TextChangedCallback) {
        textChangedListeners.add(textChangedCallback)
    }

    fun addOrReplaceTextChangedCallback(textChangedCallback: TextChangedCallback) {
        textChangedListeners.remove(textChangedCallback)
        textChangedListeners.add(textChangedCallback)
    }

    fun removeTextChangedCallback(textChangedCallback: TextChangedCallback) {
        textChangedListeners.remove(textChangedCallback)
    }

    override fun setEnabled(enabled: Boolean) {
        recursiveSetEnabled(this, enabled)
        super.setEnabled(enabled)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchThawSelfOnly(container)
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.allowedCharacters = allowedCharacters
        myState.maxCharactersCountLimit = maxCharactersCountLimit
        myState.validationEnabled = validationEnabled
        myState.editTextState = editText.onSaveInstanceState()
        val inputHierarchyState = SparseArray<Parcelable>()
        inputLayout.saveHierarchyState(inputHierarchyState)
        myState.inputLayoutState = inputHierarchyState
        return myState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState: SavedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        inputLayout.restoreHierarchyState(savedState.inputLayoutState)
        editText.onRestoreInstanceState(savedState.editTextState)
        setAllowedCharacters(savedState.allowedCharacters)
        setMaxCharacters(savedState.maxCharactersCountLimit)
        setValidationEnabled(savedState.validationEnabled)
    }

    class BindingAdapters {
        companion object {
            @JvmStatic
            @BindingAdapter("tiv_text")
            fun setText(view: TextInputView, newText: String) {
                view.setText(newText)
            }

            @JvmStatic
            @InverseBindingAdapter(attribute = "tiv_text", event = "textAttrChanged")
            fun getText(view: TextInputView): String {
                return view.getText()
            }

            @JvmStatic
            @BindingAdapter("textAttrChanged")
            fun setListeners(view: TextInputView, attrChange: InverseBindingListener) {
                view.bindingListener = object : TextChangedCallback {
                    override fun onChanged(newValue: String, oldValue: String) {
                        attrChange.onChange()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private class SavedState : BaseSavedState {
        var allowedCharacters: String = ""
        var maxCharactersCountLimit: Int = -1
        var validationEnabled: Boolean = false
        var editTextState: Parcelable? = null
        var inputLayoutState: SparseArray<Parcelable>? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(inputState: Parcel) : super(inputState) {
            allowedCharacters = ParcelableUtils.readString(inputState) ?: ""
            maxCharactersCountLimit = ParcelableUtils.readInt(inputState) ?: 0
            validationEnabled = ParcelableUtils.readBoolean(inputState) ?: false
            editTextState = inputState.readParcelable(javaClass.classLoader)
            inputLayoutState = inputState.readSparseArray(javaClass.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            ParcelableUtils.writeString(out, allowedCharacters)
            ParcelableUtils.writeInt(out, maxCharactersCountLimit)
            ParcelableUtils.writeBoolean(out, validationEnabled)

            out.writeParcelable(editTextState, flags)
            out.writeSparseArray(inputLayoutState)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private data class TextInputViewAttributes(
        var enabled: Boolean = true,
        var hintText: String = "",
        var suffixText: String = "",
        var text: String = "",
        var errorText: String = "",
        var allowedCharacters: String = "",
        var boxStrokeColor: ColorStateList? = null,
        var startIcon: Drawable? = null,
        var startIconColor: ColorStateList? = null,
        var endIcon: Drawable? = null,
        var endIconColor: ColorStateList? = null,
        var maxCharacters: Int = 0,
        var maxLines: Int = -1,
        var minLines: Int = -1,
        var boxStrokeWidth: Int = 0,
        var characterCounterEnabled: Boolean = false,
        var validationEnabled: Boolean = false,
        var viewType: ViewTypes = BOXED,
        var inputDirection: Int = 0,
        var inputAlignment: Int = 0,
        var inputType: Int = 0,
        var imeOptions: Int = 0,
        var hintAppearance: Int = 0,
        var errorAppearance: Int = 0,
    )

    private fun inflateLayout(viewType: ViewTypes) {
        when (viewType) {
            BOXED -> inflate(context, R.layout.view_text_input_boxed, this)
            OUTLINED -> inflate(context, R.layout.view_text_input_outlined, this)
        }
    }

    private fun readAttributes(
        context: Context,
        attrs: AttributeSet?,
    ): TextInputViewAttributes {
        return TextInputViewAttributes().apply {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.TextInputView,
                defStyleAttr,
                defStyleRes
            )
            try {
                /* STRINGS */
                hintText = a.getString(R.styleable.TextInputView_tiv_hintText).orEmpty()
                text = a.getString(R.styleable.TextInputView_tiv_text).orEmpty()
                errorText = a.getString(R.styleable.TextInputView_tiv_errorText).orEmpty()
                suffixText = a.getString(R.styleable.TextInputView_tiv_suffixText).orEmpty()
                allowedCharacters =
                    a.getString(R.styleable.TextInputView_tiv_allowedCharacters).orEmpty()
                /*RESOURCE REFFS*/
                hintAppearance =
                    a.getResourceId(R.styleable.TextInputView_tiv_hintTextAppearance, 0)
                errorAppearance =
                    a.getResourceId(R.styleable.TextInputView_tiv_errorTextAppearance, 0)
                boxStrokeColor = a.getColorStateList(R.styleable.TextInputView_tiv_boxStrokeColor)
                startIconColor = a.getColorStateList(R.styleable.TextInputView_tiv_startIconColor)
                endIconColor = a.getColorStateList(R.styleable.TextInputView_tiv_endIconColor)
                /* DRAWABLES */
                startIcon = a.getDrawable(R.styleable.TextInputView_tiv_startIcon)
                endIcon = a.getDrawable(R.styleable.TextInputView_tiv_endIcon)
                /* OPTIONS */
                validationEnabled =
                    a.getBoolean(R.styleable.TextInputView_tiv_validationEnabled, false)
                characterCounterEnabled =
                    a.getBoolean(R.styleable.TextInputView_tiv_characterCounterEnabled, false)
                enabled = a.getBoolean(R.styleable.TextInputView_android_enabled, true)
                viewType =
                    ViewTypes.fromInt(a.getInt(R.styleable.TextInputView_tiv_viewType, BOXED.value))
                inputAlignment = a.getInteger(R.styleable.TextInputView_tiv_inputAlignment, 0)
                inputDirection = a.getInteger(R.styleable.TextInputView_tiv_inputDirection, 0)
                imeOptions = a.getInteger(R.styleable.TextInputView_tiv_imeOptions, 0x00000000)
                maxLines = a.getInteger(R.styleable.TextInputView_tiv_maxLines, 1)
                maxCharactersCountLimit =
                    a.getInteger(R.styleable.TextInputView_tiv_maxCharacters, 0)
                minLines = a.getInteger(R.styleable.TextInputView_tiv_minLines, 1)
                boxStrokeWidth = a.getInteger(R.styleable.TextInputView_tiv_boxStrokeWidth, 1)
                if (boxStrokeWidth < 0) {
                    boxStrokeWidth = 0
                }
                inputType = a.getInteger(
                    R.styleable.TextInputView_tiv_inputType,
                    InputType.TYPE_CLASS_TEXT
                )
            } finally {
                a.recycle()
            }
        }
    }

    private fun applyAttributes(resolved: TextInputViewAttributes) {
        resolved.apply {
            this@TextInputView.allowedCharacters = allowedCharacters
            this@TextInputView.maxCharactersCountLimit = maxCharacters
            this@TextInputView.initInputLayout(resolved)
            this@TextInputView.initEditText(resolved)
            this@TextInputView.isEnabled = enabled
        }
    }

    private fun initInputLayout(resolvedAttributes: TextInputViewAttributes) {
        resolvedAttributes.apply {
            if (hintAppearance != 0) {
                setHintTextAppearance(hintAppearance)
            }
            if (errorAppearance != 0) {
                setErrorTextAppearance(hintAppearance)
            }
            boxStrokeColor?.apply {
                setBoxStrokeColorStateList(this)
            }
            startIcon?.apply {
                setStartIcon(this)
            }
            startIconColor?.apply {
                setStartIconColor(this)
            }
            endIcon?.apply {
                setEndIcon(this)
            }
            endIconColor?.apply {
                setEndIconColor(this)
            }
            setBoxStrokeWidth(boxStrokeWidth)
            setCharacterCounterEnabled(characterCounterEnabled)
            setErrorText(errorText)
            setHintText(hintText)
            setSuffixText(suffixText)
            inputLayout.isHintAnimationEnabled = false
        }
    }

    private fun initEditText(resolvedAttributes: TextInputViewAttributes) {
        editText.addTextChangedListener(textWatcher)
        resolvedAttributes.apply {
            editText.gravity = Gravity.TOP or Gravity.START
            editText.setTypeface(Typeface.DEFAULT)
            editText.showSoftInputOnFocus = true
            setInputTextAlignment(inputAlignment)
            setInputTextDirection(inputDirection)
            setLines(minLines, maxLines)
            setImeOptions(imeOptions)
            setInputType(inputType)
            syncInputFilters()
            setText(text)
        }
    }

    private fun syncInputCounter() {
        if (inputLayout.isCounterEnabled) {
            inputLayout.counterMaxLength = this.maxCharactersCountLimit
        }
    }

    private fun recursiveSetEnabled(vg: ViewGroup, enabled: Boolean) {
        var i = 0
        val count = vg.childCount
        while (i < count) {
            val child = vg.getChildAt(i)
            child.isEnabled = enabled
            if (child is ViewGroup) {
                recursiveSetEnabled(child, enabled)
            }
            i++
        }
    }

    private fun syncInputFilters() {
        val inputFilters: MutableList<InputFilter> = mutableListOf()
        if (allowedCharacters.isNotEmpty()) {
            inputFilters.add(allowedCharactersInputFilter)
        }
        if (maxCharactersCountLimit > 0) {
            inputFilters.add(LengthFilter(maxCharactersCountLimit))
        }
        editText.filters = inputFilters.toTypedArray()
    }

    private fun resolveMultiLineFlags(flags: Int): Int {
        val newFlags: Int = if (editText.minLines > 1 || editText.maxLines > 1) {
            flags or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        } else {
            flags and InputType.TYPE_TEXT_FLAG_MULTI_LINE.inv()
        }

        return newFlags
    }

    private fun initAllowedCharactersInputFilter() {
        this.allowedCharactersInputFilter =
            InputFilter { source: CharSequence, start: Int, end: Int, _: Spanned?, _: Int, _: Int ->
                if (end > start) {
                    var filteredVal = ""
                    val acceptedChars = allowedCharacters.toCharArray()
                    for (index in start until end) {
                        val currentChar = source[index].toString()
                        if (String(acceptedChars).contains(currentChar)) {
                            filteredVal += currentChar
                        }
                    }
                    return@InputFilter filteredVal
                }
                null
            }
    }

    private fun initTextWatcher() {
        textWatcher = object : TextWatcher {
            private var oldValue: String = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldValue = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newVal = s?.toString() ?: ""
                if (newVal != oldValue) {
                    for (listener in textChangedListeners) {
                        listener.onChanged(newVal, oldValue)
                    }
                    bindingListener?.apply {
                        onChanged(newVal, oldValue)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
    }

    private enum class ViewTypes(val value: Int) {
        BOXED(1),
        OUTLINED(2);

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: BOXED
        }
    }

    interface TextChangedCallback {
        fun onChanged(newValue: String, oldValue: String)
    }

    interface ValidationCallback {
        fun execute(text: String?): Boolean
    }
}