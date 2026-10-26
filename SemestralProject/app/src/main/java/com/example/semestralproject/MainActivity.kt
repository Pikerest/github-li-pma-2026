package com.example.semestralproject

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.semestralproject.di.AppContainer
import com.example.semestralproject.domain.model.LabelContext
import com.example.semestralproject.domain.model.LabelDevice
import com.example.semestralproject.domain.model.LabelOperation
import com.example.semestralproject.domain.model.LabelOperator
import com.example.semestralproject.domain.model.LabelPrintRequest
import com.example.semestralproject.domain.model.LabelPrintResult
import com.example.semestralproject.domain.model.LabelType
import com.example.semestralproject.domain.model.LabelWorkOrder
import com.example.semestralproject.domain.model.PrintJob
import com.example.semestralproject.ui.DatabaseSettingsDialog
import com.example.semestralproject.ui.PrintJobHistoryDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import java.text.DateFormat
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val container: AppContainer
        get() = (application as PackLabelerApplication).container

    private lateinit var deviceCodeInput: TextInputEditText
    private lateinit var operatorIdInput: TextInputEditText
    private lateinit var workOrderInput: TextInputEditText
    private lateinit var packageSizeInput: TextInputEditText
    private lateinit var copiesInput: TextInputEditText
    private lateinit var operationSpinner: Spinner

    private lateinit var pairDeviceButton: MaterialButton
    private lateinit var signInButton: MaterialButton
    private lateinit var searchWorkOrderButton: MaterialButton
    private lateinit var printButton: MaterialButton
    private lateinit var morePrintsButton: MaterialButton
    private lateinit var saveDraftButton: MaterialButton

    private lateinit var deviceStep: View
    private lateinit var operatorStep: View
    private lateinit var workOrderStep: View
    private lateinit var loadedDataCard: View
    private lateinit var printCard: View
    private lateinit var deviceSummary: View
    private lateinit var operatorSummary: View
    private lateinit var labelPreview: View
    private lateinit var alertCard: MaterialCardView
    private lateinit var alertText: TextView
    private lateinit var busyIndicator: CircularProgressIndicator

    private var stage = Stage.DEVICE
    private var isBusy = false
    private var device: LabelDevice? = null
    private var operator: LabelOperator? = null
    private var workOrder: LabelWorkOrder? = null
    private var operations: List<LabelOperation> = emptyList()
    private var editingJobId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bindViews()
        configureSystemBars()
        configureResponsiveLayout()
        configureInputs()
        configureActions()
        bindOperationChoices(emptyList())
        updateDataSourceMode()
        renderStage()
        restorePairedDevice()
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun bindViews() {
        deviceCodeInput = findViewById(R.id.deviceCodeInput)
        operatorIdInput = findViewById(R.id.operatorIdInput)
        workOrderInput = findViewById(R.id.workOrderInput)
        packageSizeInput = findViewById(R.id.packageSizeInput)
        copiesInput = findViewById(R.id.copiesInput)
        operationSpinner = findViewById(R.id.operationSpinner)

        pairDeviceButton = findViewById(R.id.pairDeviceButton)
        signInButton = findViewById(R.id.signInButton)
        searchWorkOrderButton = findViewById(R.id.searchWorkOrderButton)
        printButton = findViewById(R.id.printButton)
        morePrintsButton = findViewById(R.id.morePrintsButton)
        saveDraftButton = findViewById(R.id.saveDraftButton)

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
        busyIndicator = findViewById(R.id.busyIndicator)
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
        pageContent.setPadding(
            horizontalPadding,
            dp(if (isTablet) 20 else 10),
            horizontalPadding,
            dp(32),
        )

        val hero = findViewById<LinearLayout>(R.id.heroContent)
        val heroTitle = findViewById<LinearLayout>(R.id.heroTitleBlock)
        val summary = findViewById<LinearLayout>(R.id.deviceSummary)
        hero.orientation = if (isTablet) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        hero.setPadding(
            dp(if (isTablet) 28 else 20),
            dp(if (isTablet) 25 else 20),
            dp(if (isTablet) 28 else 20),
            dp(if (isTablet) 25 else 20),
        )
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
        arrangeGroup(findViewById(R.id.crudActions), isTablet)
    }

    private fun arrangeGroup(group: LinearLayout, horizontal: Boolean) {
        group.orientation = if (horizontal) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        for (index in 0 until group.childCount) {
            group.getChildAt(index).layoutParams = if (horizontal) {
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

    private fun configureInputs() {
        deviceCodeInput.doAfterTextChanged { updateButtons() }
        operatorIdInput.doAfterTextChanged { updateButtons() }
        workOrderInput.doAfterTextChanged { updateButtons() }
        packageSizeInput.doAfterTextChanged { updateButtons() }
        copiesInput.doAfterTextChanged { updateButtons() }

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
        pairDeviceButton.setOnClickListener { pairDevice() }
        signInButton.setOnClickListener { findOperator() }
        searchWorkOrderButton.setOnClickListener { findWorkOrder() }
        printButton.setOnClickListener { print(LabelType.PACKAGING) }
        morePrintsButton.setOnClickListener { showSpecialPrints() }
        saveDraftButton.setOnClickListener { saveDraft() }
        findViewById<MaterialButton>(R.id.savedJobsButton).setOnClickListener { showSavedJobs() }
        findViewById<MaterialButton>(R.id.savedJobsHomeButton).setOnClickListener { showSavedJobs() }
        findViewById<MaterialButton>(R.id.databaseSettingsButton).setOnClickListener { showDatabaseSettings() }
        findViewById<MaterialButton>(R.id.changeDeviceButton).setOnClickListener { resetToDevice() }
        findViewById<MaterialButton>(R.id.changeOperatorButton).setOnClickListener { resetToOperator() }
        findViewById<MaterialButton>(R.id.resetWorkOrderButton).setOnClickListener { resetWorkOrder() }
    }

    private fun pairDevice() {
        hideKeyboard()
        val code = deviceCodeInput.text?.toString()?.trim().orEmpty()
        runAsync(
            operation = { container.labelData.findDevice(code) ?: error(getString(R.string.device_not_found)) },
            onSuccess = { found ->
                device = found
                getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit()
                    .putString(KEY_DEVICE_CODE, found.deviceCode)
                    .apply()
                stage = Stage.OPERATOR
                bindDeviceSummary(found)
                renderStage()
                showSuccess(getString(R.string.device_paired_message, found.name, found.printer.name))
                operatorIdInput.requestFocus()
            },
        )
    }

    private fun findOperator() {
        hideKeyboard()
        val id = operatorIdInput.text?.toString()?.trim().orEmpty()
        runAsync(
            operation = { container.labelData.findOperator(id) ?: error(getString(R.string.operator_not_found)) },
            onSuccess = { found ->
                operator = found
                stage = Stage.WORK_ORDER
                bindOperatorSummary(found)
                renderStage()
                showSuccess(getString(R.string.operator_signed_in_message, found.displayName))
                workOrderInput.requestFocus()
            },
        )
    }

    private fun findWorkOrder() {
        hideKeyboard()
        val number = workOrderInput.text?.toString()?.trim().orEmpty()
        runAsync(
            operation = { container.labelData.findWorkOrder(number) ?: error(getString(R.string.work_order_not_found)) },
            onSuccess = { found ->
                workOrder = found
                editingJobId = null
                bindWorkOrder(found)
                stage = Stage.LOADED
                renderStage()
                showSuccess(getString(R.string.work_order_loaded_message, found.workOrder))
            },
        )
    }

    private fun saveDraft() {
        val context = currentContext() ?: return
        val request = buildRequest(LabelType.PACKAGING) ?: return
        val wasUpdate = editingJobId != null
        runAsync(
            operation = { container.printJobs.saveDraft(context, request) },
            onSuccess = { id ->
                editingJobId = id
                saveDraftButton.setText(R.string.update_draft)
                showSuccess(getString(if (wasUpdate) R.string.draft_updated else R.string.draft_saved, id))
            },
        )
    }

    private fun print(type: LabelType) {
        val request = buildRequest(type) ?: return
        hideKeyboard()
        runAsync(
            operation = { container.printing.print(request) },
            onSuccess = { result ->
                editingJobId = result.localJobId
                saveDraftButton.setText(R.string.update_draft)
                showPrintResult(result)
            },
        )
    }

    private fun showSpecialPrints() {
        val content = layoutInflater.inflate(R.layout.dialog_special_prints, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(content)
            .setNegativeButton(R.string.close, null)
            .create()
        content.findViewById<View>(R.id.qualityPrintTile).setOnClickListener {
            dialog.dismiss()
            print(LabelType.QUALITY)
        }
        content.findViewById<View>(R.id.operationPrintTile).setOnClickListener {
            dialog.dismiss()
            print(LabelType.IN_OPERATION)
        }
        dialog.show()
    }

    private fun showDatabaseSettings() {
        DatabaseSettingsDialog(this).show(
            current = container.settingsStore.load(),
            onSave = { settings ->
                container.settingsStore.save(settings)
                resetAfterDataSourceChange()
                showSuccess(getString(R.string.database_settings_saved))
            },
            onUseDemo = {
                container.settingsStore.clear()
                resetAfterDataSourceChange()
                showSuccess(getString(R.string.demo_data_enabled))
            },
        )
    }

    private fun showSavedJobs() {
        runAsync(
            operation = { container.printJobs.getAll() },
            onSuccess = { jobs ->
                PrintJobHistoryDialog(this).show(
                    jobs = jobs,
                    onEdit = ::loadJobForEditing,
                    onDelete = ::confirmDeleteJob,
                )
            },
        )
    }

    private fun loadJobForEditing(job: PrintJob) {
        runAsync(
            operation = {
                val foundDevice = container.labelData.findDevice(job.deviceCode)
                    ?: error(getString(R.string.device_not_found))
                val foundOperator = container.labelData.findOperator(job.operatorId)
                    ?: error(getString(R.string.operator_not_found))
                val foundWorkOrder = container.labelData.findWorkOrder(job.workOrder)
                    ?: error(getString(R.string.work_order_not_found))
                LabelContext(foundDevice, foundOperator, foundWorkOrder)
            },
            onSuccess = { context ->
                device = context.device
                operator = context.operator
                workOrder = context.workOrder
                editingJobId = job.id
                deviceCodeInput.setText(job.deviceCode)
                operatorIdInput.setText(job.operatorId)
                workOrderInput.setText(job.workOrder)
                bindDeviceSummary(context.device)
                bindOperatorSummary(context.operator)
                bindWorkOrder(context.workOrder)
                packageSizeInput.setText(job.packageSize.toString())
                copiesInput.setText(job.copies.toString())
                selectOperation(job.operation)
                stage = Stage.LOADED
                renderStage()
                saveDraftButton.setText(R.string.update_draft)
                showSuccess(getString(R.string.job_loaded, job.id))
            },
        )
    }

    private fun confirmDeleteJob(job: PrintJob) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_job_title)
            .setMessage(getString(R.string.delete_job_message, job.id, job.workOrder))
            .setPositiveButton(R.string.delete) { _, _ -> deleteJob(job) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteJob(job: PrintJob) {
        runAsync(
            operation = { container.printJobs.delete(job.id) },
            onSuccess = { deleted ->
                if (deleted) {
                    if (editingJobId == job.id) {
                        editingJobId = null
                        saveDraftButton.setText(R.string.save_draft)
                    }
                    showSuccess(getString(R.string.job_deleted, job.id))
                } else {
                    showError(getString(R.string.job_not_found, job.id))
                }
            },
        )
    }

    private fun buildRequest(type: LabelType): LabelPrintRequest? {
        val currentDevice = device ?: return null
        val currentOperator = operator ?: return null
        val currentWorkOrder = workOrder ?: return null
        val packageSize = packageSizeInput.text?.toString()?.toLongOrNull()
        val copies = copiesInput.text?.toString()?.toIntOrNull()
        if (packageSize == null || packageSize <= 0 || copies == null || copies !in 1..100) {
            showError(getString(R.string.invalid_print_values))
            return null
        }
        return LabelPrintRequest(
            type = type,
            deviceCode = currentDevice.deviceCode,
            operatorId = currentOperator.employeeNumber,
            workOrder = currentWorkOrder.workOrder,
            copies = copies,
            packageSize = packageSize,
            operation = selectedOperationCode(),
            draftId = editingJobId,
        )
    }

    private fun currentContext(): LabelContext? {
        val currentDevice = device ?: return null
        val currentOperator = operator ?: return null
        val currentWorkOrder = workOrder ?: return null
        return LabelContext(currentDevice, currentOperator, currentWorkOrder)
    }

    private fun bindDeviceSummary(found: LabelDevice) {
        findViewById<TextView>(R.id.deviceName).text = found.name
        findViewById<TextView>(R.id.printerName).text = found.printer.name
    }

    private fun bindOperatorSummary(found: LabelOperator) {
        findViewById<TextView>(R.id.operatorName).text = found.displayName
    }

    private fun bindWorkOrder(found: LabelWorkOrder) {
        findViewById<TextView>(R.id.loadedWorkOrder).text = found.workOrder
        findViewById<TextView>(R.id.partNumberValue).text = display(found.partNumber)
        findViewById<TextView>(R.id.descriptionValue).text = display(found.customerPartDescription)
        findViewById<TextView>(R.id.customerValue).text = display(found.customerName)
        findViewById<TextView>(R.id.shipToValue).text = display(found.shipToName)
        findViewById<TextView>(R.id.customerPartValue).text = display(found.customerPartNumber)
        findViewById<TextView>(R.id.revisionValue).text = display(found.customerRevision)
        findViewById<TextView>(R.id.expirationValue).text = expirationDate(found.shelfLifeDays)
        packageSizeInput.setText(found.defaultPackageSize.coerceAtLeast(1).toString())
        copiesInput.setText("1")
        bindOperationChoices(found.operations)
        labelPreview.visibility = View.GONE
        saveDraftButton.setText(R.string.save_draft)
    }

    private fun bindOperationChoices(values: List<LabelOperation>) {
        operations = values
        val labels = values.map(LabelOperation::label).ifEmpty { listOf("—") }
        operationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            labels,
        )
    }

    private fun selectOperation(code: String) {
        val index = operations.indexOfFirst { it.code == code }
        if (index >= 0) operationSpinner.setSelection(index)
    }

    private fun selectedOperationCode(): String = operations
        .getOrNull(operationSpinner.selectedItemPosition)
        ?.code
        .orEmpty()

    private fun showPrintResult(result: LabelPrintResult) {
        val currentWorkOrder = workOrder ?: return
        val packageSize = packageSizeInput.text?.toString()?.toLongOrNull() ?: 0
        findViewById<TextView>(R.id.previewCustomer).text = display(currentWorkOrder.customerName).uppercase()
        findViewById<TextView>(R.id.previewPart).text = display(currentWorkOrder.partNumber)
        findViewById<TextView>(R.id.previewMeta).text = getString(
            R.string.preview_meta_format,
            currentWorkOrder.workOrder,
            packageSize,
            currentWorkOrder.customerRevision,
        )
        findViewById<TextView>(R.id.previewCaption).text = getString(
            R.string.preview_caption_format,
            result.templateName,
        )
        labelPreview.visibility = View.VISIBLE
        showSuccess(getString(R.string.printed_message, result.printedCopies, result.printerName))
        labelPreview.post {
            findViewById<NestedScrollView>(R.id.pageScroll).smoothScrollTo(0, labelPreview.bottom)
        }
    }

    private fun resetToDevice() {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit().remove(KEY_DEVICE_CODE).apply()
        device = null
        operator = null
        workOrder = null
        editingJobId = null
        deviceCodeInput.text?.clear()
        operatorIdInput.text?.clear()
        workOrderInput.text?.clear()
        stage = Stage.DEVICE
        renderStage()
    }

    private fun resetToOperator() {
        operator = null
        workOrder = null
        editingJobId = null
        operatorIdInput.text?.clear()
        workOrderInput.text?.clear()
        stage = Stage.OPERATOR
        renderStage()
        operatorIdInput.requestFocus()
    }

    private fun resetWorkOrder() {
        workOrder = null
        editingJobId = null
        workOrderInput.text?.clear()
        stage = Stage.WORK_ORDER
        renderStage()
        workOrderInput.requestFocus()
    }

    private fun resetAfterDataSourceChange() {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit().remove(KEY_DEVICE_CODE).apply()
        device = null
        operator = null
        workOrder = null
        editingJobId = null
        deviceCodeInput.text?.clear()
        operatorIdInput.text?.clear()
        workOrderInput.text?.clear()
        stage = Stage.DEVICE
        updateDataSourceMode()
        renderStage()
    }

    private fun restorePairedDevice() {
        val savedCode = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            .getString(KEY_DEVICE_CODE, "")
            .orEmpty()
        if (savedCode.isNotBlank()) {
            deviceCodeInput.setText(savedCode)
            pairDevice()
        }
    }

    private fun updateDataSourceMode() {
        findViewById<TextView>(R.id.dataSourceMode).setText(
            if (container.labelData.usesDemoData) R.string.data_source_demo else R.string.data_source_sql,
        )
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
        saveDraftButton.setText(if (editingJobId == null) R.string.save_draft else R.string.update_draft)
        updateButtons()
    }

    private fun updateButtons() {
        if (!::pairDeviceButton.isInitialized) return
        pairDeviceButton.isEnabled = !isBusy && stage == Stage.DEVICE &&
            deviceCodeInput.text?.trim()?.length == DEVICE_CODE_LENGTH
        signInButton.isEnabled = !isBusy && stage == Stage.OPERATOR && !operatorIdInput.text.isNullOrBlank()
        searchWorkOrderButton.isEnabled = !isBusy && stage != Stage.DEVICE &&
            stage != Stage.OPERATOR && !workOrderInput.text.isNullOrBlank()
        val packageSize = packageSizeInput.text?.toString()?.toLongOrNull() ?: 0
        val copies = copiesInput.text?.toString()?.toIntOrNull() ?: 0
        val canSubmit = !isBusy && stage == Stage.LOADED && packageSize > 0 && copies in 1..100
        printButton.isEnabled = canSubmit
        morePrintsButton.isEnabled = canSubmit
        saveDraftButton.isEnabled = canSubmit
    }

    private fun setBusy(busy: Boolean) {
        isBusy = busy
        busyIndicator.visible(busy)
        updateButtons()
    }

    private fun <T> runAsync(operation: () -> T, onSuccess: (T) -> Unit) {
        if (isBusy) return
        setBusy(true)
        alertCard.visibility = View.GONE
        worker.execute {
            runCatching(operation).fold(
                onSuccess = { result ->
                    mainHandler.post {
                        if (isDestroyed) return@post
                        setBusy(false)
                        onSuccess(result)
                    }
                },
                onFailure = { exception ->
                    mainHandler.post {
                        if (isDestroyed) return@post
                        setBusy(false)
                        showError(exception.message ?: exception.javaClass.simpleName)
                    }
                },
            )
        }
    }

    private fun showSuccess(message: String) {
        alertCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.qnity_success_soft))
        alertText.setTextColor(ContextCompat.getColor(this, R.color.qnity_success))
        alertText.text = message
        alertCard.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        alertCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.qnity_error_soft))
        alertText.setTextColor(ContextCompat.getColor(this, R.color.qnity_error))
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

    private fun expirationDate(shelfLifeDays: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, shelfLifeDays) }
        return DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.time)
    }

    private fun display(value: String) = value.ifBlank { "—" }
    private fun View.visible(show: Boolean) { visibility = if (show) View.VISIBLE else View.GONE }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private enum class Stage { DEVICE, OPERATOR, WORK_ORDER, LOADED }

    private companion object {
        const val TABLET_BREAKPOINT_DP = 600
        const val DEVICE_CODE_LENGTH = 7
        const val PREFERENCES_NAME = "pack_labeler_state"
        const val KEY_DEVICE_CODE = "device_code"
    }
}
