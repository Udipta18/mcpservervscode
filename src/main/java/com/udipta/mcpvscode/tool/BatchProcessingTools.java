package com.udipta.mcpvscode.tool;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BatchProcessingTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessingTools.class);

    // In-memory storage for demo purposes (simulates database)
    private static final Map<String, List<Map<String, Object>>> dataStore = new ConcurrentHashMap<>();
    private static final Map<String, String> batchStatus = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> validationResults = new ConcurrentHashMap<>();

    // ==================== DATA CREATION TOOLS ====================

    @Tool(name = "createInvoiceData", description = "Create invoice data for batch processing")
    String createInvoiceData(@ToolParam(description = "Number of invoice records to create") int recordCount,
            @ToolParam(description = "Customer type (premium, standard, basic)") String customerType) {
        LOGGER.info("Creating {} invoice records for {} customers", recordCount, customerType);

        List<Map<String, Object>> invoices = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= recordCount; i++) {
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("invoiceId", "INV-" + String.format("%06d", i));
            invoice.put("customerId", "CUST-" + (1000 + i));
            invoice.put("customerType", customerType);
            invoice.put("amount", Math.round((random.nextDouble() * 5000 + 100) * 100.0) / 100.0);
            invoice.put("taxRate", customerType.equals("premium") ? 0.08 : 0.10);
            invoice.put("status", "PENDING");
            invoice.put("createdDate", LocalDateTime.now().minusDays(random.nextInt(30)));
            invoice.put("dueDate", LocalDateTime.now().plusDays(30));
            invoices.add(invoice);
        }

        dataStore.put("invoices", invoices);
        batchStatus.put("invoices", "DATA_CREATED");

        return String.format(
                "🔧 **Tool Used:** createInvoiceData\n" +
                        "📊 **Input:** %d records, %s customers\n" +
                        "✅ **Result:** ✅ Created %d invoice records for %s customers. Data ready for batch processing.\n"
                        +
                        "📈 **Progress:** 1/9 - Data Creation Phase",
                recordCount, customerType, recordCount, customerType);

        // return "Error Creating Invoice Data";
    }

    // ==================== BATCH PROCESSING TOOLS ====================

    @Tool(name = "runInvoiceBatch", description = "Run batch processing on invoice data")
    String runInvoiceBatch() {
        LOGGER.info("Starting invoice batch processing");

        if (!dataStore.containsKey("invoices")) {
            return "❌ **ERROR DETECTED!**\n🛑 **Stopping Chain Execution**\n🔧 **Failed Tool:** runInvoiceBatch\n📝 **Error Details:** No invoice data found. Please create invoice data first using createInvoiceData tool.\n💡 **Suggested Action:** Run createInvoiceData tool first, then retry batch processing.";
        }

        List<Map<String, Object>> invoices = dataStore.get("invoices");
        int processedCount = 0;
        int errorCount = 0;

        for (Map<String, Object> invoice : invoices) {
            try {
                // Simulate processing logic
                double amount = (Double) invoice.get("amount");
                double taxRate = (Double) invoice.get("taxRate");
                double taxAmount = amount * taxRate;
                double totalAmount = amount + taxAmount;

                invoice.put("taxAmount", Math.round(taxAmount * 100.0) / 100.0);
                invoice.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
                invoice.put("status", "PROCESSED");
                invoice.put("processedDate", LocalDateTime.now());

                processedCount++;

                // Simulate processing time
                Thread.sleep(100);

            } catch (Exception e) {
                invoice.put("status", "ERROR");
                invoice.put("errorMessage", e.getMessage());
                errorCount++;
            }
        }

        batchStatus.put("invoices", "BATCH_COMPLETED");

        if (errorCount > 0) {
            return String.format(
                    "❌ **ERROR DETECTED!**\n🛑 **Stopping Chain Execution**\n🔧 **Failed Tool:** runInvoiceBatch\n📝 **Error Details:** %d records failed processing\n💡 **Suggested Action:** Check data quality and retry batch processing.",
                    errorCount);
        }

        return String.format(
                "🔧 **Tool Used:** runInvoiceBatch\n📊 **Input:** Processing %d invoice records\n✅ **Result:** ✅ Invoice batch processing completed!\n📊 Processed: %d records\n❌ Errors: %d records\n📈 Success Rate: %.1f%%\n📈 **Progress:** 2/9 - Batch Processing Phase",
                invoices.size(), processedCount, errorCount,
                (processedCount * 100.0) / (processedCount + errorCount));
    }

    // ==================== VALIDATION TOOLS ====================

    @Tool(name = "validateInvoiceData", description = "Validate processed invoice data")
    String validateInvoiceData() {
        LOGGER.info("Validating invoice data");

        if (!dataStore.containsKey("invoices")) {
            return "❌ **ERROR DETECTED!**\n🛑 **Stopping Chain Execution**\n🔧 **Failed Tool:** validateInvoiceData\n📝 **Error Details:** No invoice data found for validation.\n💡 **Suggested Action:** Create and process invoice data first, then retry validation.";
        }

        List<Map<String, Object>> invoices = dataStore.get("invoices");
        List<String> validationErrors = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;

        for (Map<String, Object> invoice : invoices) {
            List<String> errors = new ArrayList<>();

            // Validate required fields
            if (invoice.get("invoiceId") == null)
                errors.add("Missing invoice ID");
            if (invoice.get("amount") == null)
                errors.add("Missing amount");
            if (invoice.get("totalAmount") == null)
                errors.add("Missing total amount");

            // Validate business rules
            if (invoice.get("amount") != null && invoice.get("totalAmount") != null) {
                double amount = (Double) invoice.get("amount");
                double totalAmount = (Double) invoice.get("totalAmount");
                if (totalAmount <= amount) {
                    errors.add("Total amount should be greater than base amount");
                }
            }

            // Validate status
            String status = (String) invoice.get("status");
            if (!Arrays.asList("PROCESSED", "ERROR").contains(status)) {
                errors.add("Invalid status: " + status);
            }

            if (errors.isEmpty()) {
                validCount++;
            } else {
                invalidCount++;
                validationErrors.addAll(errors);
            }
        }

        validationResults.put("invoices", validationErrors);

        if (invalidCount > 0) {
            return String.format(
                    "❌ **ERROR DETECTED!**\n🛑 **Stopping Chain Execution**\n🔧 **Failed Tool:** validateInvoiceData\n📝 **Error Details:** %d records failed validation\n💡 **Suggested Action:** Fix data quality issues and retry validation.",
                    invalidCount);
        }

        return String.format(
                "🔧 **Tool Used:** validateInvoiceData\n📊 **Input:** Validating %d invoice records\n✅ **Result:** ✅ Invoice data validation completed!\n✅ Valid records: %d\n❌ Invalid records: %d\n📊 Validation Rate: %.1f%%\n🔍 Errors found: %d\n📈 **Progress:** 5/9 - Validation Phase",
                invoices.size(), validCount, invalidCount,
                (validCount * 100.0) / (validCount + invalidCount),
                validationErrors.size());
    }

    // ==================== REPORTING TOOLS ====================

    @Tool(name = "generateBatchReport", description = "Generate comprehensive batch processing report")
    String generateBatchReport() {
        LOGGER.info("Generating batch processing report");

        StringBuilder report = new StringBuilder();
        report.append("📊 BATCH PROCESSING REPORT\n");
        report.append("==========================\n\n");

        // Data creation status
        report.append("📋 DATA CREATION STATUS:\n");
        report.append("------------------------\n");
        for (Map.Entry<String, String> entry : batchStatus.entrySet()) {
            String status = entry.getValue().equals("DATA_CREATED") ? "✅ Ready"
                    : entry.getValue().equals("BATCH_COMPLETED") ? "✅ Processed" : "❌ Not Ready";
            report.append(String.format("%-12s: %s\n", entry.getKey().toUpperCase(), status));
        }

        // Data counts
        report.append("\n📈 DATA COUNTS:\n");
        report.append("---------------\n");
        for (Map.Entry<String, List<Map<String, Object>>> entry : dataStore.entrySet()) {
            report.append(String.format("%-12s: %d records\n", entry.getKey().toUpperCase(), entry.getValue().size()));
        }

        // Validation summary
        report.append("\n🔍 VALIDATION SUMMARY:\n");
        report.append("----------------------\n");
        for (Map.Entry<String, List<String>> entry : validationResults.entrySet()) {
            int errorCount = entry.getValue().size();
            String status = errorCount == 0 ? "✅ All Valid" : "⚠️ " + errorCount + " Errors";
            report.append(String.format("%-12s: %s\n", entry.getKey().toUpperCase(), status));
        }

        report.append("\n📅 Report Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return report.toString();
    }

    // Helper method
    private String generateRandomName() {
        String[] firstNames = { "John", "Jane", "Mike", "Sarah", "David", "Lisa", "Chris", "Emma", "Alex", "Maria" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez" };
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }
}
