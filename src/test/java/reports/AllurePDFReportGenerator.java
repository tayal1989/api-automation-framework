package reports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generates a professional PDF report from Allure test results.
 * Reads JSON files from allure-results directory and creates a consolidated PDF.
 */
public class AllurePDFReportGenerator {

    private static final String DEFAULT_RESULTS_DIR = "target/allure-results";
    private static final String DEFAULT_OUTPUT_DIR = "target/pdf-reports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Colors
    private static final Color HEADER_COLOR = new Color(41, 128, 185);
    private static final Color PASS_COLOR = new Color(39, 174, 96);
    private static final Color FAIL_COLOR = new Color(231, 76, 60);
    private static final Color SKIP_COLOR = new Color(243, 156, 18);
    private static final Color BROKEN_COLOR = new Color(155, 89, 182);
    private static final Color TABLE_HEADER_BG = new Color(52, 73, 94);
    private static final Color TABLE_ALT_ROW = new Color(236, 240, 241);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String resultsDir = args.length > 0 ? args[0] : DEFAULT_RESULTS_DIR;
        String outputDir = args.length > 1 ? args[1] : DEFAULT_OUTPUT_DIR;

        AllurePDFReportGenerator generator = new AllurePDFReportGenerator();
        try {
            String reportPath = generator.generateReport(resultsDir, outputDir);
            System.out.println("✅ PDF Report generated: " + reportPath);
        } catch (Exception e) {
            System.err.println("❌ Failed to generate PDF report: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Generates a PDF report from Allure results
     */
    public String generateReport(String resultsDir, String outputDir) throws IOException, DocumentException {
        // Parse all test results
        List<TestResult> results = parseAllureResults(resultsDir);

        if (results.isEmpty()) {
            throw new IOException("No test results found in: " + resultsDir);
        }

        // Create output directory
        new File(outputDir).mkdirs();

        // Generate PDF
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("%s/Test_Execution_Report_%s.pdf", outputDir, timestamp);

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Add content
        addHeader(document);
        addSummary(document, results);
        addTestResultsTable(document, results);
        addFailureDetails(document, results);
        addFooter(document);

        document.close();
        return fileName;
    }

    /**
     * Parse Allure result JSON files
     */
    private List<TestResult> parseAllureResults(String resultsDir) throws IOException {
        List<TestResult> results = new ArrayList<>();
        Path dirPath = Paths.get(resultsDir);

        if (!Files.exists(dirPath)) {
            throw new IOException("Results directory not found: " + resultsDir);
        }

        try (Stream<Path> files = Files.list(dirPath)) {
            files.filter(p -> p.toString().endsWith("-result.json"))
                    .forEach(path -> {
                        try {
                            JsonNode json = objectMapper.readTree(path.toFile());
                            TestResult result = new TestResult();
                            result.name = getTextValue(json, "name");
                            result.fullName = getTextValue(json, "fullName");
                            result.status = getTextValue(json, "status");
                            result.start = getLongValue(json, "start");
                            result.stop = getLongValue(json, "stop");

                            // Get error message if failed
                            if (json.has("statusDetails")) {
                                JsonNode details = json.get("statusDetails");
                                result.errorMessage = getTextValue(details, "message");
                                result.stackTrace = getTextValue(details, "trace");
                            }

                            results.add(result);
                        } catch (IOException e) {
                            System.err.println("Failed to parse: " + path + " - " + e.getMessage());
                        }
                    });
        }

        return results;
    }

    private String getTextValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : "";
    }

    private long getLongValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asLong() : 0;
    }

    private void addHeader(Document document) throws DocumentException {
        // Title
        Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, HEADER_COLOR);
        Paragraph title = new Paragraph("Test Execution Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Subtitle with date
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
        Paragraph subtitle = new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DISPLAY_FORMATTER),
                subtitleFont
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(5);
        document.add(subtitle);

        // Project name
        Font projectFont = new Font(Font.HELVETICA, 11, Font.ITALIC, Color.DARK_GRAY);
        Paragraph project = new Paragraph("API Automation Framework", projectFont);
        project.setAlignment(Element.ALIGN_CENTER);
        project.setSpacingAfter(30);
        document.add(project);
    }

    private void addSummary(Document document, List<TestResult> results) throws DocumentException {
        // Section title
        Font sectionFont = new Font(Font.HELVETICA, 16, Font.BOLD, HEADER_COLOR);
        Paragraph sectionTitle = new Paragraph("Executive Summary", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        // Calculate stats
        long passed = results.stream().filter(r -> "passed".equalsIgnoreCase(r.status)).count();
        long failed = results.stream().filter(r -> "failed".equalsIgnoreCase(r.status)).count();
        long skipped = results.stream().filter(r -> "skipped".equalsIgnoreCase(r.status)).count();
        long broken = results.stream().filter(r -> "broken".equalsIgnoreCase(r.status)).count();
        int total = results.size();
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        long totalDuration = results.stream().mapToLong(r -> r.stop - r.start).sum();

        // Summary table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingAfter(20);

        addSummaryRow(table, "Total Tests", String.valueOf(total), Color.BLACK);
        addSummaryRow(table, "Passed", String.valueOf(passed), PASS_COLOR);
        addSummaryRow(table, "Failed", String.valueOf(failed), FAIL_COLOR);
        addSummaryRow(table, "Skipped", String.valueOf(skipped), SKIP_COLOR);
        addSummaryRow(table, "Broken", String.valueOf(broken), BROKEN_COLOR);
        addSummaryRow(table, "Pass Rate", String.format("%.1f%%", passRate),
                passRate >= 80 ? PASS_COLOR : (passRate >= 50 ? SKIP_COLOR : FAIL_COLOR));
        addSummaryRow(table, "Total Duration", formatDuration(totalDuration), Color.BLACK);

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Color valueColor) {
        Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
        Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, valueColor);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setBackgroundColor(TABLE_ALT_ROW);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(valueCell);
    }

    private void addTestResultsTable(Document document, List<TestResult> results) throws DocumentException {
        // Section title
        Font sectionFont = new Font(Font.HELVETICA, 16, Font.BOLD, HEADER_COLOR);
        Paragraph sectionTitle = new Paragraph("Test Results Details", sectionFont);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        // Results table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 1.5f, 1.5f});

        // Header
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        addTableHeader(table, "#", headerFont);
        addTableHeader(table, "Test Name", headerFont);
        addTableHeader(table, "Status", headerFont);
        addTableHeader(table, "Duration", headerFont);

        // Data rows
        Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        int index = 1;
        boolean alternate = false;

        for (TestResult result : results) {
            Color bgColor = alternate ? TABLE_ALT_ROW : Color.WHITE;

            addTableCell(table, String.valueOf(index++), dataFont, bgColor, Element.ALIGN_CENTER);
            addTableCell(table, result.name, dataFont, bgColor, Element.ALIGN_LEFT);
            addStatusCell(table, result.status, bgColor);
            addTableCell(table, formatDuration(result.stop - result.start), dataFont, bgColor, Element.ALIGN_CENTER);

            alternate = !alternate;
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(TABLE_HEADER_BG);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.DARK_GRAY);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addStatusCell(PdfPTable table, String status, Color bgColor) {
        Color statusColor = switch (status.toLowerCase()) {
            case "passed" -> PASS_COLOR;
            case "failed" -> FAIL_COLOR;
            case "skipped" -> SKIP_COLOR;
            case "broken" -> BROKEN_COLOR;
            default -> Color.BLACK;
        };

        Font statusFont = new Font(Font.HELVETICA, 9, Font.BOLD, statusColor);
        PdfPCell cell = new PdfPCell(new Phrase(status.toUpperCase(), statusFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addFailureDetails(Document document, List<TestResult> results) throws DocumentException {
        List<TestResult> failures = results.stream()
                .filter(r -> "failed".equalsIgnoreCase(r.status) || "broken".equalsIgnoreCase(r.status))
                .toList();

        if (failures.isEmpty()) {
            return;
        }

        // New page for failures
        document.newPage();

        // Section title
        Font sectionFont = new Font(Font.HELVETICA, 16, Font.BOLD, FAIL_COLOR);
        Paragraph sectionTitle = new Paragraph("Failure Details", sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(15);
        document.add(sectionTitle);

        Font testNameFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
        Font errorFont = new Font(Font.HELVETICA, 10, Font.NORMAL, FAIL_COLOR);
        Font stackFont = new Font(Font.COURIER, 8, Font.NORMAL, Color.DARK_GRAY);

        for (TestResult result : failures) {
            // Test name
            Paragraph testName = new Paragraph("❌ " + result.name, testNameFont);
            testName.setSpacingBefore(15);
            testName.setSpacingAfter(5);
            document.add(testName);

            // Error message
            if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
                Paragraph error = new Paragraph("Error: " + result.errorMessage, errorFont);
                error.setIndentationLeft(20);
                error.setSpacingAfter(5);
                document.add(error);
            }

            // Stack trace (truncated)
            if (result.stackTrace != null && !result.stackTrace.isEmpty()) {
                String truncatedStack = result.stackTrace.length() > 500
                        ? result.stackTrace.substring(0, 500) + "\n... (truncated)"
                        : result.stackTrace;

                Paragraph stack = new Paragraph(truncatedStack, stackFont);
                stack.setIndentationLeft(20);
                stack.setSpacingAfter(10);
                document.add(stack);
            }
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(40);

        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);
        Chunk footerText = new Chunk(
                "Generated by API Automation Framework | Confidential - For Internal Use Only",
                footerFont
        );
        footer.add(footerText);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 60000) {
            return String.format("%.2f s", millis / 1000.0);
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return String.format("%d min %d s", minutes, seconds);
        }
    }

    /**
     * Internal class to hold test result data
     */
    private static class TestResult {
        String name;
        String fullName;
        String status;
        long start;
        long stop;
        String errorMessage;
        String stackTrace;
    }
}
