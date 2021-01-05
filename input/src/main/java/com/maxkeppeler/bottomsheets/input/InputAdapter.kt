/*
 *  Copyright (C) 2020. Maximilian Keppeler (https://www.maxkeppeler.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.maxkeppeler.bottomsheets.input

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.method.PasswordTransformationMethod
import android.util.LayoutDirection
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView.TEXT_ALIGNMENT_VIEW_START
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.maxkeppeler.bottomsheets.core.utils.getIconColor
import com.maxkeppeler.bottomsheets.core.utils.getPrimaryColor
import com.maxkeppeler.bottomsheets.core.utils.getTextColor
import com.maxkeppeler.bottomsheets.core.utils.toDp
import com.maxkeppeler.bottomsheets.core.views.BottomSheetContent
import com.maxkeppeler.bottomsheets.input.databinding.BottomSheetsInputCheckBoxItemBinding
import com.maxkeppeler.bottomsheets.input.databinding.BottomSheetsInputEditTextItemBinding
import com.maxkeppeler.bottomsheets.input.databinding.BottomSheetsInputRadioButtonsItemBinding
import com.maxkeppeler.bottomsheets.input.databinding.BottomSheetsInputSpinnerItemBinding
import com.maxkeppeler.bottomsheets.input.type.*

internal typealias InputAdapterChangeListener = () -> Unit

internal class InputAdapter(
    private val ctx: Context,
    private val input: MutableList<Input>,
    private val listener: InputAdapterChangeListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_EDIT_TEXT = 0
        private const val VIEW_TYPE_CHECK_BOX = 1
        private const val VIEW_TYPE_RADIO_BUTTONS = 2
        private const val VIEW_TYPE_SPINNER = 3
    }

    private val primaryColor = getPrimaryColor(ctx)
    private val iconsColor = getIconColor(ctx)
    private val textColor = getTextColor(ctx)

    override fun getItemViewType(i: Int): Int = when (input[i]) {
        is InputEditText -> VIEW_TYPE_EDIT_TEXT
        is InputCheckBox -> VIEW_TYPE_CHECK_BOX
        is InputRadioButtons -> VIEW_TYPE_RADIO_BUTTONS
        is InputSpinner -> VIEW_TYPE_SPINNER
        else -> throw IllegalStateException("No ViewType for this Input.")
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return when (type) {
            VIEW_TYPE_EDIT_TEXT -> {
                EditTextViewHolder(
                    BottomSheetsInputEditTextItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            VIEW_TYPE_CHECK_BOX -> {
                CheckBoxViewHolder(
                    BottomSheetsInputCheckBoxItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            VIEW_TYPE_RADIO_BUTTONS -> {
                RadioButtonsViewHolder(
                    BottomSheetsInputRadioButtonsItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            VIEW_TYPE_SPINNER -> {
                SpinnerViewHolder(
                    BottomSheetsInputSpinnerItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException("No ViewHolder for this ViewType.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val input = input[i]
        when {
            holder is EditTextViewHolder && input is InputEditText -> {
                holder.binding.buildEditText(input)
            }
            holder is CheckBoxViewHolder
                    && input is InputCheckBox -> {
                holder.binding.buildCheckBox(input)
            }
            holder is RadioButtonsViewHolder
                    && input is InputRadioButtons -> {
                holder.binding.buildRadioButtons(input)
            }
            holder is SpinnerViewHolder
                    && input is InputSpinner -> {
                holder.binding.buildSpinner(input)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun BottomSheetsInputEditTextItemBinding.buildEditText(input: InputEditText) {

        setupGeneralInputInfo(input)

        with(textInput) {

            setText(input.value)

            input.isPasswordVisible?.let {
                transformationMethod = if (it) null else PasswordTransformationMethod()
            }

            input.inputType?.let { inputType = it }
            input.maxLines?.let { maxLines = it }
            input.inputFilter?.let { filters = arrayOf(it) }

            isVerticalScrollBarEnabled = true

            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action != 0 && MotionEvent.ACTION_UP != 0 || MotionEvent.ACTION_DOWN != 0) {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    false
                } else false
            }

            addTextWatcher { text ->
                input.value = text
                listener.invoke()
            }
        }

        with(textInputLayout) {

            val hintText = input.hintRes?.let { ctx.getString(it) } ?: input.hint
            hintText?.let { hint = it }

            input.endIconMode?.let { endIconMode = it }
            input.isEndIconActivated?.let { setEndIconActivated(it) }
            input.drawableRes?.let { setStartIconDrawable(it) }
            setStartIconTintList(ColorStateList.valueOf(primaryColor))

            input.validationResultListener { result ->
                if (!result.valid) {
                    error = result.errorText
                    setErrorIconDrawable(R.drawable.bs_ic_close)
                    setErrorTextColor(ColorStateList.valueOf(Color.RED))
                    setErrorIconTintList(ColorStateList.valueOf(Color.RED))
                }
                isErrorEnabled = !result.valid
            }
        }
    }

    private fun BottomSheetsInputCheckBoxItemBinding.buildCheckBox(input: InputCheckBox) {

        setupGeneralInputInfo(input, label, icon)

        checkBox.isChecked = input.value

        val checkBoxText = input.textRes?.let { ctx.getString(it) } ?: input.text
        checkBox.text = checkBoxText
        checkBox.setTextColor(textColor)
        checkBox.buttonTintList = ColorStateList.valueOf(primaryColor)
        checkBox.setOnCheckedChangeListener { _, checked ->
            input.value = checked
            listener.invoke()
        }
    }

    private fun BottomSheetsInputRadioButtonsItemBinding.buildRadioButtons(input: InputRadioButtons) {

        setupGeneralInputInfo(input, label)

        input.radioButtonOptions?.forEachIndexed { index, radioButtonText ->
            val button = AppCompatRadioButton(ctx).apply {
                setTextAppearance(ctx, android.R.style.TextAppearance_Material_Body2)
                setTextColor(textColor)
                buttonTintList = ColorStateList.valueOf(primaryColor)
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                setPaddingRelative(16.toDp(), 0, 0, 0)
                textAlignment = TEXT_ALIGNMENT_VIEW_START
                text = radioButtonText
                id = index
            }
            radioGroup.addView(button)
        }

        radioGroup.check(input.value)
        radioGroup.setOnCheckedChangeListener { _, idIndex ->
            input.value = idIndex
            listener.invoke()
        }
    }

    private fun BottomSheetsInputSpinnerItemBinding.buildSpinner(input: InputSpinner) {

        setupGeneralInputInfo(input, label, icon)

        val spinnerOptions = input.spinnerOptions

        if (input.value == null) {
            val spinnerNoSelectionText =
                input.textRes?.let { ctx.getString(it) } ?: input.noSelectionText
            spinnerOptions?.add(spinnerNoSelectionText ?: ctx.getString(R.string.select))
        }

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            ctx,
            android.R.layout.simple_spinner_dropdown_item, spinnerOptions ?: mutableListOf()
        ) {
            override fun getCount(): Int =
                super.getCount().takeIf { it > 0 }?.minus(1) ?: super.getCount()
        }
        icon.setColorFilter(primaryColor)
        spinner.adapter = adapter
        val selectionIndex = input.value ?: spinnerOptions?.lastIndex ?: 0
        spinner.setSelection(selectionIndex)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(aV: AdapterView<*>?, v: View, i: Int, id: Long) {
                (aV?.getChildAt(0) as TextView).setTextColor(textColor)
                if (i == selectionIndex) return
                input.value = i
                listener.invoke()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) = Unit
        }
    }

    private fun setupGeneralInputInfo(
        input: Input,
        label: BottomSheetContent? = null,
        icon: ImageView? = null
    ) {

        label?.let {
            val labelText = input.labelRes?.let { ctx.getString(it) } ?: input.label
            labelText?.let {
                label.text = it.takeUnless { input.required } ?: it.plus(" *")
                label.visibility = View.VISIBLE
            }
        }

        icon?.let {
            input.drawableRes?.let { res ->
                icon.setImageDrawable(ContextCompat.getDrawable(ctx, res))
                icon.setColorFilter(iconsColor)
                icon.visibility = View.VISIBLE
            } ?: kotlin.run { icon.visibility = View.GONE }
        }
    }

    override fun getItemCount(): Int = input.size

    internal inner class EditTextViewHolder(val binding: BottomSheetsInputEditTextItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    internal inner class CheckBoxViewHolder(val binding: BottomSheetsInputCheckBoxItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    internal inner class RadioButtonsViewHolder(val binding: BottomSheetsInputRadioButtonsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    internal inner class SpinnerViewHolder(val binding: BottomSheetsInputSpinnerItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
