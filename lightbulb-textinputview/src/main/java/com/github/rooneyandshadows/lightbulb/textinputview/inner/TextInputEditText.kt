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

    fun requestFocusWithKeyboard() {
        pendingShow = true
        requestFocus()
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs)

        if (pendingShow && isFocused) {
            post {
                val imm = context.getSystemService(InputMethodManager::class.java)
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                pendingShow = false
            }
        }

        return ic
    }
}