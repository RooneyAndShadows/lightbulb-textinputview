package com.rooneyandshadows.lightbulb.textinputview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils
import com.github.rooneyandshadows.lightbulb.textinputview.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rooneyandshadows.lightbulb.textinputview.TextInputView.ViewTypes.BOXED
import com.rooneyandshadows.lightbulb.textinputview.TextInputView.ViewTypes.OUTLINED

class TextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,//R.attr.easyRecyclerViewStyle,
    defStyleRes: Int = 0//R.style.EasyRecyclerViewDefaultStyle,
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val inputLayout: TextInputLayout by lazy {
        findViewById(R.id.textInputLayout)
    }
    private val editText: TextInputEditText by lazy {
        findViewById(R.id.textInputEditText)
    }
    private var allowedCharacters: String = ""
    private var maxCharactersCountLimit: Int = -1
    private var validationEnabled: Boolean = false
    private var bindingListener: TextChangedCallback? = null
    private val inputFilters: MutableList<InputFilter> = mutableListOf()
    private val textChangedListeners: MutableList<TextChangedCallback> = mutableListOf()
    private val validationCallbacks: MutableList<ValidationCallback> = mutableListOf()

    init {
        val resolvedAttributes = readAttributes(context, attrs)
        inflateLayout(resolvedAttributes.viewType)
        applyAttributes(resolvedAttributes)
    }

    private fun inflateLayout(viewType: ViewTypes) {
        when (viewType) {
            BOXED -> inflate(context, R.layout.view_text_input_boxed, this)
            OUTLINED -> inflate(context, R.layout.view_text_input_outlined, this)
        }
    }

    private fun applyAttributes(resolved: TextInputViewAttributes) {
        resolved.apply {
            this@TextInputView.allowedCharacters = allowedCharacters
            this@TextInputView.maxCharactersCountLimit = maxCharacters
            this@TextInputView.initInputLayout(resolved)
            this@TextInputView.initEditText(resolved)
        }
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?): TextInputViewAttributes {
        return TextInputViewAttributes().apply {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.TextInputView, 0, 0)
            try {
                /* STRINGS */
                hintText = a.getString(
                    R.styleable.TextInputView_tiv_hintText
                ).orEmpty()
                text = a.getString(
                    R.styleable.TextInputView_tiv_text
                ).orEmpty()
                errorText = a.getString(
                    R.styleable.TextInputView_tiv_errorText
                ).orEmpty()
                suffixText = a.getString(
                    R.styleable.TextInputView_tiv_suffixText
                ).orEmpty()
                allowedCharacters = a.getString(
                    R.styleable.TextInputView_tiv_allowedCharacters
                ).orEmpty()
                /*RESOURCE REFFS*/
                hintAppearance = a.getResourceId(
                    R.styleable.TextInputView_tiv_hintTextAppearance,
                    0
                )
                errorAppearance = a.getResourceId(
                    R.styleable.TextInputView_tiv_errorTextAppearance,
                    0
                )
                boxStrokeColor = a.getColorStateList(
                    R.styleable.TextInputView_tiv_boxStrokeColor
                )
                startIconColor = a.getColorStateList(
                    R.styleable.TextInputView_tiv_startIconColor
                )
                endIconColor = a.getColorStateList(
                    R.styleable.TextInputView_tiv_endIconColor
                )
                /* DRAWABLES */
                startIcon = a.getDrawable(R.styleable.TextInputView_tiv_startIcon)
                endIcon = a.getDrawable(R.styleable.TextInputView_tiv_endIcon)
                /* OPTIONS */
                validationEnabled = a.getBoolean(
                    R.styleable.TextInputView_tiv_validationEnabled,
                    false
                )
                singleLine = a.getBoolean(
                    R.styleable.TextInputView_tiv_isSingleLine,
                    true
                )
                characterCounterEnabled = a.getBoolean(
                    R.styleable.TextInputView_tiv_characterCounterEnabled,
                    false
                )
                enabled = a.getBoolean(
                    R.styleable.TextInputView_android_enabled,
                    true
                )
                viewType = ViewTypes.fromInt(
                    a.getInt(
                        R.styleable.TextInputView_tiv_viewType,
                        BOXED.value
                    )
                )
                inputAlignment = a.getInteger(
                    R.styleable.TextInputView_tiv_inputAlignment,
                    0
                )
                inputDirection = a.getInteger(
                    R.styleable.TextInputView_tiv_inputDirection,
                    0
                )
                imeOptions = a.getInteger(
                    R.styleable.TextInputView_tiv_imeOptions,
                    0x00000000
                )
                maxLines = a.getInteger(
                    R.styleable.TextInputView_tiv_maxLines,
                    1
                )
                maxCharactersCountLimit = a.getInteger(
                    R.styleable.TextInputView_tiv_maxCharacters,
                    -1
                )
                minLines = a.getInteger(
                    R.styleable.TextInputView_tiv_minLines,
                    1
                )
                boxStrokeWidth = a.getInteger(
                    R.styleable.TextInputView_tiv_boxStrokeWidth,
                    1
                )
                if (boxStrokeWidth < 0) {
                    boxStrokeWidth = 0
                }
                inputType = a.getInteger(R.styleable.TextInputView_tiv_inputType, 0x00000001)
                if (inputType == InputType.TYPE_CLASS_TEXT) {
                    inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                }
            } finally {
                a.recycle()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        inputLayout.isEnabled = enabled
        editText.isEnabled = enabled
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
        resolvedAttributes.apply {
            editText.doOnTextChanged { text, _, _, _ ->
                val newVal = text?.toString() ?: ""
                val oldVal = getText()

                if (newVal != oldVal) {
                    for (listener in textChangedListeners) {
                        listener.onChanged(newVal, oldVal)
                    }
                    bindingListener?.apply {
                        onChanged(newVal, oldVal)
                    }
                }
            }
            editText.setTypeface(Typeface.DEFAULT)
            editText.showSoftInputOnFocus = true

            setInputTextAlignment(inputAlignment)
            setInputTextDirection(inputDirection)
            setMinLines(minLines)
            setSingleLine(singleLine)
            setMaxLines(maxLines)
            setImeOptions(imeOptions)
            setInputType(inputType)
            syncInputFilters()
            setText(text)
        }
    }

    private fun syncInputFilters() {
        val inputFilters: MutableList<InputFilter> = mutableListOf()
        allowedCharacters.isset {
            val filter =
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
            inputFilters.add(filter)
        }
        maxCharactersCountLimit.isset {
            inputFilters.add(LengthFilter(maxCharactersCountLimit))
        }
        editText.filters = inputFilters.toTypedArray()
    }

    private enum class ViewTypes(val value: Int) {
        BOXED(1),
        OUTLINED(2);

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: BOXED
        }
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
        this.maxCharactersCountLimit = maxCharacters
        syncInputFilters()
    }

    fun getMaxCharacters(): Int {
        return this.maxCharactersCountLimit
    }

    fun setMaxLines(maxLines: Int) {
        editText.maxLines = maxLines
    }

    fun getMaxLines(): Int {
        return editText.maxLines
    }

    fun setMinLines(minLines: Int) {
        editText.minLines = minLines
    }

    fun getMinLines(): Int {
        return editText.minLines
    }

    fun setSingleLine(value: Boolean) {
        editText.isSingleLine = value
    }

    fun setCharacterCounterEnabled(enabled: Boolean) {
        inputLayout.isCounterEnabled = enabled
        if (maxCharactersCountLimit >= 0 && enabled)
            inputLayout.counterMaxLength = maxCharactersCountLimit
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
        editText.inputType = inputType
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
        var maxCharacters: Int = -1,
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
        var singleLine: Boolean = false
    )

    private fun Int.isset(callback: (value: Int) -> Unit) {
        if (this > 0) {
            callback.invoke(this)
        }
    }

    private fun String.isset(callback: (value: String) -> Unit) {
        if (this != "") {
            callback.invoke(this)
        }
    }

    interface TextChangedCallback {
        fun onChanged(newValue: String, oldValue: String)
    }

    interface ValidationCallback {
        fun execute(text: String?): Boolean
    }
}