package com.example.semestralproject

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var deviceCodeInput: TextInputEditText
    private lateinit var operatorIdInput: TextInputEditText
    private lateinit var workOrderInput: TextInputEditText
    private lateinit var packageSizeInput: TextInputEditText
    private lateinit var copiesInput: TextInputEditText

    private lateinit var pairDeviceButton: MaterialButton
    private lateinit var signInButton: MaterialButton
    private lateinit var searchWorkOrderButton: MaterialButton
    private lateinit var printButton: MaterialButton

    private lateinit var deviceStep: View
    private lateinit var operatorStep: View
    private lateinit var workOrderStep: View
    private lateinit var loadedDataCard: View
    private lateinit var printCard: View
    private lateinit var deviceSummary: View
    private lateinit var operatorSummary: View
    private lateinit var labelPreview: View
    private lateinit var alertCard: View
    private lateinit var alertText: TextView
    private lateinit var loadedWorkOrder: TextView
    private lateinit var previewMeta: TextView

    private var stage = Stage.DEVICE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bindViews()
        configureSystemBars()
        configureResponsiveLayout()
        configureOperationPicker()
        configureInputs()
        configureActions()

        stage = Stage.entries.getOrElse(savedInstanceState?.getInt(STATE_STAGE) ?: 0) {
            Stage.DEVICE
        }
        renderStage()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_STAGE, stage.ordinal)
        super.onSaveInstanceState(outState)
    }

    private fun bindViews() {
        deviceCodeInput = findViewById(R.id.deviceCodeInput)
        operatorIdInput = findViewById(R.id.operatorIdInput)
        workOrderInput = findViewById(R.id.workOrderInput)
        packageSizeInput = findViewById(R.id.packageSizeInput)
        copiesInput = findViewById(R.id.copiesInput)

        pairDeviceButton = findViewById(R.id.pairDeviceButton)
        signInButton = findViewById(R.id.signInButton)
        searchWorkOrderButton = findViewById(R.id.searchWorkOrderButton)
        printButton = findViewById(R.id.printButton)

        deviceStep = findViewById(R.id.deviceStep)
        operatorStep = findViewById(R.id.operatorStep)
        workOrderStep = findViewById(R.id.workOrderStep)
        loadedDataCard = findViewById(R.id.loadedDataCard)
        printCard = findViewById(R.id.printCard)
        deviceSummary = findViewById(R.id.deviceSummary)
        operatorSummary = findViewById(R.id.operatorSummary)
        labelPreview = findViewById(R.id.labelPreview)
        alertCard = findViewById(R.id.alertCard)
        alertText = findViewById(R.id.alertText)
        loadedWorkOrder = findViewById(R.id.loadedWorkOrder)
        previewMeta = findViewById(R.id.previewMeta)
    }

    private fun configureSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun configureResponsiveLayout() {
        val isTablet = resources.configuration.smallestScreenWidthDp >= TABLET_BREAKPOINT_DP
        val screenWidth = resources.configuration.screenWidthDp
        val pageContent = findViewById<LinearLayout>(R.id.pageContent)
        val horizontalPadding = dp(if (isTablet) 28 else 10)
        pageContent.setPadding(horizontalPadding, dp(if (isTablet) 20 else 10), horizontalPadding, dp(32))

        val hero = findViewById<LinearLayout>(R.id.heroContent)
        val heroTitle = findViewById<LinearLayout>(R.id.heroTitleBlock)
        val summary = findViewById<LinearLayout>(R.id.deviceSummary)
        hero.orientation = if (isTablet) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        hero.setPadding(dp(if (isTablet) 28 else 20), dp(if (isTablet) 25 else 20), dp(if (isTablet) 28 else 20), dp(if (isTablet) 25 else 20))
        heroTitle.layoutParams = if (isTablet) {
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        summary.layoutParams = if (isTablet) {
            LinearLayout.LayoutParams(dp(310), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(24)
            }
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(18)
            }
        }

        findViewById<GridLayout>(R.id.dataGrid).columnCount = when {
            isTablet -> 4
            screenWidth >= 520 -> 2
            else -> 1
        }
        arrangeGroup(findViewById(R.id.operatorActions), isTablet)
        arrangeGroup(findViewById(R.id.workOrderActions), isTablet)
        arrangeGroup(findViewById(R.id.printFields), isTablet)
        arrangeGroup(findViewById(R.id.printActions), isTablet)
    }

    private fun arrangeGroup(group: LinearLayout, horizontal: Boolean) {
        group.orientation = if (horizontal) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        for (index in 0 until group.childCount) {
            val child = group.getChildAt(index)
            child.layoutParams = if (horizontal) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (index > 0) marginStart = dp(10)
                }
            } else {
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    if (index > 0) topMargin = dp(8)
                }
            }
        }
    }

    private fun configureOperationPicker() {
        val operations = listOf(
            "010 - Vstupní kontrola",
            "020 - Montáž",
            "030 - Balení"
        )
        findViewById<Spinner>(R.id.operationSpinner).adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            operations
        )
    }

    private fun configureInputs() {
        deviceCodeInput.doAfterTextChanged {
            pairDeviceButton.isEnabled = it?.trim()?.length == DEVICE_CODE_LENGTH
        }
        operatorIdInput.doAfterTextChanged {
            signInButton.isEnabled = !it.isNullOrBlank()
        }
        workOrderInput.doAfterTextChanged {
            searchWorkOrderButton.isEnabled = !it.isNullOrBlank()
        }
        packageSizeInput.doAfterTextChanged { updatePrintButton() }
        copiesInput.doAfterTextChanged { updatePrintButton() }

        deviceCodeInput.setOnEditorActionListener { _, _, _ ->
            if (pairDeviceButton.isEnabled) pairDeviceButton.performClick()
            true
        }
        operatorIdInput.setOnEditorActionListener { _, _, _ ->
            if (signInButton.isEnabled) signInButton.performClick()
            true
        }
        workOrderInput.setOnEditorActionListener { _, _, _ ->
            if (searchWorkOrderButton.isEnabled) searchWorkOrderButton.performClick()
            true
        }
    }

    private fun configureActions() {
        pairDeviceButton.setOnClickListener {
            hideKeyboard()
            stage = Stage.OPERATOR
            renderStage()
            showAlert(getString(R.string.device_paired_message, getString(R.string.demo_device_name), getString(R.string.demo_printer_name)))
            operatorIdInput.requestFocus()
        }

        findViewById<MaterialButton>(R.id.changeDeviceButton).setOnClickListener {
            stage = Stage.DEVICE
            operatorIdInput.text?.clear()
            workOrderInput.text?.clear()
            clearLoadedWorkOrder()
            renderStage()
            deviceCodeInput.requestFocus()
        }

        signInButton.setOnClickListener {
            hideKeyboard()
            stage = Stage.WORK_ORDER
            renderStage()
            showAlert(getString(R.string.operator_signed_in_message, getString(R.string.demo_operator_name)))
            workOrderInput.requestFocus()
        }

        findViewById<MaterialButton>(R.id.changeOperatorButton).setOnClickListener {
            stage = Stage.OPERATOR
            operatorIdInput.text?.clear()
            workOrderInput.text?.clear()
            clearLoadedWorkOrder()
            renderStage()
            operatorIdInput.requestFocus()
        }

        searchWorkOrderButton.setOnClickListener {
            hideKeyboard()
            val workOrder = workOrderInput.text?.toString()?.trim().orEmpty()
            loadedWorkOrder.text = workOrder
            updatePreview(workOrder)
            stage = Stage.LOADED
            renderStage()
            showAlert(getString(R.string.work_order_loaded_message, workOrder))
            loadedDataCard.post { findViewById<View>(R.id.pageScroll).requestFocus() }
        }

        findViewById<MaterialButton>(R.id.resetWorkOrderButton).setOnClickListener {
            stage = Stage.WORK_ORDER
            workOrderInput.text?.clear()
            clearLoadedWorkOrder()
            renderStage()
            workOrderInput.requestFocus()
        }

        printButton.setOnClickListener { printLabel() }
        findViewById<MaterialButton>(R.id.morePrintsButton).setOnClickListener { showSpecialPrints() }
    }

    private fun renderStage() {
        deviceStep.visible(stage == Stage.DEVICE)
        operatorStep.visible(stage == Stage.OPERATOR)
        workOrderStep.visible(stage == Stage.WORK_ORDER || stage == Stage.LOADED)
        loadedDataCard.visible(stage == Stage.LOADED)
        printCard.visible(stage == Stage.LOADED)
        deviceSummary.visible(stage != Stage.DEVICE)
        operatorSummary.visible(stage == Stage.WORK_ORDER || stage == Stage.LOADED)
        if (stage != Stage.LOADED) labelPreview.visibility = View.GONE
        alertCard.visibility = View.GONE
        updatePrintButton()
    }

    private fun updatePrintButton() {
        if (!::printButton.isInitialized) return
        val packageSize = packageSizeInput.text?.toString()?.toLongOrNull() ?: 0L
        val copies = copiesInput.text?.toString()?.toIntOrNull() ?: 0
        printButton.isEnabled = stage == Stage.LOADED && packageSize > 0 && copies in 1..100
    }

    private fun printLabel() {
        hideKeyboard()
        val copies = copiesInput.text?.toString()?.toIntOrNull() ?: return
        showPrintResult(copies)
    }

    private fun showSpecialPrints() {
        val content = layoutInflater.inflate(R.layout.dialog_special_prints, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(content)
            .setNegativeButton(R.string.close, null)
            .create()

        content.findViewById<View>(R.id.qualityPrintTile).setOnClickListener {
            dialog.dismiss()
            showPrintResult(1)
        }
        content.findViewById<View>(R.id.operationPrintTile).setOnClickListener {
            dialog.dismiss()
            showPrintResult(1)
        }
        dialog.show()
    }

    private fun showPrintResult(copies: Int) {
        labelPreview.visibility = View.VISIBLE
        showAlert(getString(R.string.printed_message, copies, getString(R.string.demo_printer_name)))
        labelPreview.post {
            findViewById<androidx.core.widget.NestedScrollView>(R.id.pageScroll).smoothScrollTo(0, labelPreview.bottom)
        }
    }

    private fun updatePreview(workOrder: String) {
        val quantity = packageSizeInput.text?.toString().orEmpty().ifBlank { "120" }
        previewMeta.text = "WO $workOrder  ·  QTY $quantity  ·  REV B03"
    }

    private fun clearLoadedWorkOrder() {
        loadedWorkOrder.setText(R.string.demo_work_order)
        labelPreview.visibility = View.GONE
    }

    private fun showAlert(message: String) {
        alertText.text = message
        alertCard.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        currentFocus?.let { focused ->
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(focused.windowToken, 0)
            focused.clearFocus()
        }
    }

    private fun View.visible(show: Boolean) {
        visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private enum class Stage {
        DEVICE,
        OPERATOR,
        WORK_ORDER,
        LOADED
    }

    private companion object {
        const val STATE_STAGE = "stage"
        const val DEVICE_CODE_LENGTH = 7
        const val TABLET_BREAKPOINT_DP = 600
    }
}
