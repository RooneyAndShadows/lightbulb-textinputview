package com.github.rooneyandshadows.lightbulb.textinputview.inner

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText

class TextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextInputEditText(context, attrs) {
    private var pendingShow = false


    init {
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pendingShow = true
            } else {
                pendingShow = false
                val imm = context.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager
                imm.hideSoftInputFromWindow(rootView.windowToken, 0)
            }
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs)

        post {
            if (pendingShow && isAttachedToWindow && isFocused) {
                val imm = context.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager?
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        return ic
    }
}