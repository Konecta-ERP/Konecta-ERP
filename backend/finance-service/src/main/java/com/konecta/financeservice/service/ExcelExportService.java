package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] exportTrialBalance(TrialBalanceReportDTO report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trial Balance");

            int rowIdx = 0;

            // Header
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Account ID");
            header.createCell(1).setCellValue("Account Name");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Debit");
            header.createCell(4).setCellValue("Credit");
            header.createCell(5).setCellValue("Abnormal?");

            // Rows
            for (TrialBalanceRowDTO row : report.getRows()) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(row.getAccountId());
                r.createCell(1).setCellValue(row.getAccountName());
                r.createCell(2).setCellValue(row.getAccountType().toString());
                r.createCell(3).setCellValue(
                        row.getDebitBalance() != null ? row.getDebitBalance().doubleValue() : 0);
                r.createCell(4).setCellValue(
                        row.getCreditBalance() != null ? row.getCreditBalance().doubleValue() : 0);
                r.createCell(5).setCellValue(row.isAbnormal() ? "Yes" : "No");
            }

            // Totals
            Row totals = sheet.createRow(rowIdx + 1);
            totals.createCell(2).setCellValue("TOTALS");
            totals.createCell(3).setCellValue(report.getTotalDebits().doubleValue());
            totals.createCell(4).setCellValue(report.getTotalCredits().doubleValue());

            // Auto-size columns
            for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportGl(GLResponseDTO response) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 100, true, true)) {
            SXSSFSheet sheet = wb.createSheet("General Ledger");

            // IMPORTANT: enable tracking of columns for autosizing (must be called BEFORE writing rows)
            sheet.trackAllColumnsForAutoSizing();

            // Styles
            Workbook wbRef = sheet.getWorkbook();
            CellStyle headerStyle = wbRef.createCellStyle();
            Font bold = wbRef.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            CellStyle currencyStyle = wbRef.createCellStyle();
            DataFormat df = wbRef.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            CellStyle abnormalStyle = wbRef.createCellStyle();
            abnormalStyle.cloneStyleFrom(currencyStyle);
            abnormalStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            abnormalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;

            // Header info (filters)
            Row meta = sheet.createRow(rowIdx++);
            meta.createCell(0).setCellValue("GL Report");
            meta.getCell(0).setCellStyle(headerStyle);

            Row filter = sheet.createRow(rowIdx++);
            filter.createCell(0).setCellValue("From:");
            filter.createCell(1).setCellValue(response.getFromDate().format(DATE_FMT));
            filter.createCell(3).setCellValue("To:");
            filter.createCell(4).setCellValue(response.getToDate().format(DATE_FMT));
            if (response.getFilteredAccounts() != null && !response.getFilteredAccounts().isEmpty()) {
                Row accRow = sheet.createRow(rowIdx++);
                accRow.createCell(0).setCellValue("Accounts:");
                accRow.createCell(1).setCellValue(String.join(", ", response.getFilteredAccounts()));
            } else {
                rowIdx++;
            }
            rowIdx++; // blank row

            // Column headers — create at the current rowIdx (not at 0)
            String[] cols = {"Account ID", "Account Name", "Tx Date", "Tx ID", "Entry ID",
                    "Description", "Debit", "Credit", "Running Balance"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            List<GLRowDTO> rows = response.getEntries();
            for (GLRowDTO r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(r.getAccountPK());
                row.createCell(c++).setCellValue(r.getAccountName());
                row.createCell(c++).setCellValue(r.getTransactionDate().toString());
                row.createCell(c++).setCellValue(r.getTransactionId() != null ? r.getTransactionId() : 0);
                row.createCell(c++).setCellValue(r.getEntryId() != null ? r.getEntryId() : 0);
                row.createCell(c++).setCellValue(r.getDescription() != null ? r.getDescription() : "");

                // Debit
                Cell debitCell = row.createCell(c++);
                BigDecimal debit = r.getDebitAmount() != null ? r.getDebitAmount() : BigDecimal.ZERO;
                debitCell.setCellValue(debit.doubleValue());
                debitCell.setCellStyle(currencyStyle);

                // Credit
                Cell creditCell = row.createCell(c++);
                BigDecimal credit = r.getCreditAmount() != null ? r.getCreditAmount() : BigDecimal.ZERO;
                creditCell.setCellValue(credit.doubleValue());
                creditCell.setCellStyle(currencyStyle);

                // Running balance
                Cell runningCell = row.createCell(c++);
                BigDecimal running = r.getRunningBalance() != null ? r.getRunningBalance() : BigDecimal.ZERO;
                runningCell.setCellValue(running.doubleValue());
                runningCell.setCellStyle(currencyStyle);

                // Optionally set abnormal style if you have that flag
                // if (r.isAbnormal()) { debitCell.setCellStyle(abnormalStyle); ... }
            }

            // Autosize columns (wrap in try to be defensive)
            for (int i = 0; i < cols.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (IllegalStateException ex) {
                    // If auto-size still fails for a column, set a reasonable fallback width
                    sheet.setColumnWidth(i, 20 * 256); // ~20 characters
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            // dispose of temporary files
            wb.dispose();
            return out.toByteArray();
        }
    }

    public byte[] exportIncomeStatement(IncomeStatementDTO dto) throws IOException {
        // Use SXSSF as in your example
        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 100, true, true)) {
            SXSSFSheet sheet = wb.createSheet("Income Statement");

            // IMPORTANT: enable tracking for autosizing
            sheet.trackAllColumnsForAutoSizing();

            // --- Define Styles ---
            Map<String, CellStyle> styles = createExcelStyles(wb);

            int rowIdx = 0;

            // Header info (Period)
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Income Statement (Profit & Loss)");
            titleCell.setCellStyle(styles.get("title"));

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period:");
            periodRow.createCell(1).setCellValue(dto.getPeriodLabel());
            periodRow.createCell(3).setCellValue("From:");
            periodRow.createCell(4).setCellValue(dto.getStartDate().format(DATE_FMT));
            periodRow.createCell(6).setCellValue("To:");
            periodRow.createCell(7).setCellValue(dto.getEndDate().format(DATE_FMT));

            rowIdx += 2; // Blank rows

            // Column headers
            String[] cols = {"Line Item", "Actual", "Budget", "Variance", "Variance %"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(styles.get("header"));
            }

            // --- Data Rows ---
            rowIdx = addExcelRow(sheet, rowIdx, "Revenue", dto.getRevenueActual(), dto.getRevenueBudget(),
                    dto.getRevenueVariance(), dto.getRevenueVariancePct(), styles, "data");

            rowIdx = addExcelRow(sheet, rowIdx, "Cost of Goods Sold (COGS)", dto.getCogsActual(), dto.getCogsBudget(),
                    dto.getCogsVariance(), null, styles, "data");

            // Gross Profit (Bold)
            rowIdx++; // Spacer
            rowIdx = addExcelRow(sheet, rowIdx, "Gross Profit", dto.getGrossProfitActual(), dto.getGrossProfitBudget(),
                    dto.getGrossProfitVariance(), dto.getGrossProfitVariancePct(), styles, "bold");
            rowIdx++; // Spacer

            // OpEx
            rowIdx = addExcelRow(sheet, rowIdx, "Operating Expenses (OpEx)", dto.getOpexActual(), dto.getOpexBudget(),
                    null, null, styles, "data");

            // EBIT (Bold)
            rowIdx = addExcelRow(sheet, rowIdx, "EBIT", dto.getEbitActual(), dto.getEbitBudget(),
                    dto.getEbitVariance(), dto.getEbitVariancePct(), styles, "bold");
            rowIdx++; // Spacer

            // Other Income/Expense
            rowIdx = addExcelRow(sheet, rowIdx, "Other Income", dto.getOtherIncomeActual(), dto.getOtherIncomeBudget(),
                    null, null, styles, "data");
            rowIdx = addExcelRow(sheet, rowIdx, "Other Expense", dto.getOtherExpenseActual(), dto.getOtherExpenseBudget(),
                    null, null, styles, "data");
            rowIdx++; // Spacer

            // Net Income (Bold)
            rowIdx = addExcelRow(sheet, rowIdx, "Net Income", dto.getNetIncomeActual(), dto.getNetIncomeBudget(),
                    dto.getNetIncomeVariance(), dto.getNetIncomeVariancePct(), styles, "bold");


            // Autosize columns
            for (int i = 0; i < cols.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Exception e) {
                    sheet.setColumnWidth(i, 20 * 256); // Fallback
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose(); // dispose of temporary files
            return out.toByteArray();
        }
    }

    public byte[] exportBalanceSheet(BalanceSheetReportDTO dto) throws IOException {

        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 100, true, true)) {
            SXSSFSheet sheet = wb.createSheet("Balance Sheet");

            // IMPORTANT: enable tracking for autosizing
            sheet.trackAllColumnsForAutoSizing();

            // --- [INLINED] Define Styles ---
            Map<String, CellStyle> styles = new HashMap<>();
            DataFormat df = wb.createDataFormat();

            Font boldFont = wb.createFont();
            boldFont.setBold(true);

            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font sectionFont = wb.createFont();
            sectionFont.setBold(true);
            sectionFont.setColor(IndexedColors.BLUE.getIndex());

            // Title style
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            styles.put("title", titleStyle);

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(boldFont);
            styles.put("header", headerStyle);

            // Section Header style
            CellStyle sectionHeaderStyle = wb.createCellStyle();
            sectionHeaderStyle.setFont(sectionFont);
            styles.put("section_header", sectionHeaderStyle);

            // Data label style (indented)
            CellStyle dataLabelStyle = wb.createCellStyle();
            dataLabelStyle.setIndention((short) 1);
            styles.put("data_label", dataLabelStyle);

            // Total label style (bold)
            CellStyle totalLabelStyle = wb.createCellStyle();
            totalLabelStyle.setFont(boldFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            styles.put("total_label", totalLabelStyle);

            // Currency style
            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)"));
            styles.put("data_currency", currencyStyle);

            // Total currency style (bold)
            CellStyle totalCurrencyStyle = wb.createCellStyle();
            totalCurrencyStyle.setFont(boldFont);
            totalCurrencyStyle.setDataFormat(currencyStyle.getDataFormat());
            styles.put("total_currency", totalCurrencyStyle);

            // Status - Balanced (Green)
            Font balancedFont = wb.createFont();
            balancedFont.setBold(true);
            balancedFont.setColor(IndexedColors.GREEN.getIndex());
            CellStyle balancedStyle = wb.createCellStyle();
            balancedStyle.setFont(balancedFont);
            styles.put("status_balanced", balancedStyle);

            // Status - Unbalanced (Red)
            Font unbalancedFont = wb.createFont();
            unbalancedFont.setBold(true);
            unbalancedFont.setColor(IndexedColors.RED.getIndex());
            CellStyle unbalancedStyle = wb.createCellStyle();
            unbalancedStyle.setFont(unbalancedFont);
            styles.put("status_unbalanced", unbalancedStyle);
            // --- End [INLINED] Styles ---


            int rowIdx = 0;

            // Header info
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Balance Sheet");
            titleCell.setCellStyle(styles.get("title"));

            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue("As-of Date:");
            dateRow.createCell(1).setCellValue(dto.getAsOfDate().format(DATE_FMT));

            // Validation Status
            Row statusRow = sheet.createRow(rowIdx++);
            statusRow.createCell(0).setCellValue("Validation Status:");
            Cell statusCell = statusRow.createCell(1);
            statusCell.setCellValue(dto.getValidationStatus());

            boolean isBalanced = "Balanced".equalsIgnoreCase(dto.getValidationStatus());
            statusCell.setCellStyle(isBalanced ? styles.get("status_balanced") : styles.get("status_unbalanced"));

            rowIdx += 2; // Blank rows

            // Column headers
            String[] cols = {"Account ID", "Account Name", "Balance"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(styles.get("header"));
            }

            // --- [INLINED] Data Rows ---

            // === Assets ===
            // [INLINED] Section Header: Current Assets
            Row assetCurrentHeader = sheet.createRow(rowIdx++);
            Cell assetCurrentCell = assetCurrentHeader.createCell(0);
            assetCurrentCell.setCellValue("Current Assets");
            assetCurrentCell.setCellStyle(styles.get("section_header"));

            // [INLINED] Account Rows
            for (BalanceSheetAccountRowDTO acctRow : dto.getAssetsCurrent()) {
                Row row = sheet.createRow(rowIdx++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(acctRow.getAccountId());
                idCell.setCellStyle(styles.get("data_label"));

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(acctRow.getAccountName());

                Cell balanceCell = row.createCell(2);
                if (acctRow.getSignedBalance() != null) {
                    balanceCell.setCellValue(acctRow.getSignedBalance().doubleValue());
                }
                balanceCell.setCellStyle(styles.get("data_currency"));
            }

            // [INLINED] Section Header: Non-Current Assets
            Row assetNonCurrentHeader = sheet.createRow(rowIdx++);
            Cell assetNonCurrentCell = assetNonCurrentHeader.createCell(0);
            assetNonCurrentCell.setCellValue("Non-Current Assets");
            assetNonCurrentCell.setCellStyle(styles.get("section_header"));

            // [INLINED] Account Rows
            for (BalanceSheetAccountRowDTO acctRow : dto.getAssetsNonCurrent()) {
                Row row = sheet.createRow(rowIdx++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(acctRow.getAccountId());
                idCell.setCellStyle(styles.get("data_label"));

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(acctRow.getAccountName());

                Cell balanceCell = row.createCell(2);
                if (acctRow.getSignedBalance() != null) {
                    balanceCell.setCellValue(acctRow.getSignedBalance().doubleValue());
                }
                balanceCell.setCellStyle(styles.get("data_currency"));
            }

            // [INLINED] Total Row: Total Assets
            Row totalAssetRow = sheet.createRow(rowIdx++);

            Cell assetLabelCell = totalAssetRow.createCell(1);
            assetLabelCell.setCellValue("Total Assets");
            assetLabelCell.setCellStyle(styles.get("total_label"));

            Cell assetTotalCell = totalAssetRow.createCell(2);
            if (dto.getTotalAssets() != null) {
                assetTotalCell.setCellValue(dto.getTotalAssets().doubleValue());
            }
            assetTotalCell.setCellStyle(styles.get("total_currency"));

            rowIdx++; // Spacer

            // === Liabilities ===
            // [INLINED] Section Header: Current Liabilities
            Row liabCurrentHeader = sheet.createRow(rowIdx++);
            Cell liabCurrentCell = liabCurrentHeader.createCell(0);
            liabCurrentCell.setCellValue("Current Liabilities");
            liabCurrentCell.setCellStyle(styles.get("section_header"));

            // [INLINED] Account Rows
            for (BalanceSheetAccountRowDTO acctRow : dto.getLiabilitiesCurrent()) {
                Row row = sheet.createRow(rowIdx++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(acctRow.getAccountId());
                idCell.setCellStyle(styles.get("data_label"));

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(acctRow.getAccountName());

                Cell balanceCell = row.createCell(2);
                if (acctRow.getSignedBalance() != null) {
                    balanceCell.setCellValue(acctRow.getSignedBalance().doubleValue());
                }
                balanceCell.setCellStyle(styles.get("data_currency"));
            }

            // [INLINED] Section Header: Non-Current Liabilities
            Row liabNonCurrentHeader = sheet.createRow(rowIdx++);
            Cell liabNonCurrentCell = liabNonCurrentHeader.createCell(0);
            liabNonCurrentCell.setCellValue("Non-Current Liabilities");
            liabNonCurrentCell.setCellStyle(styles.get("section_header"));

            // [INLINED] Account Rows
            for (BalanceSheetAccountRowDTO acctRow : dto.getLiabilitiesNonCurrent()) {
                Row row = sheet.createRow(rowIdx++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(acctRow.getAccountId());
                idCell.setCellStyle(styles.get("data_label"));

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(acctRow.getAccountName());

                Cell balanceCell = row.createCell(2);
                if (acctRow.getSignedBalance() != null) {
                    balanceCell.setCellValue(acctRow.getSignedBalance().doubleValue());
                }
                balanceCell.setCellStyle(styles.get("data_currency"));
            }

            // [INLINED] Total Row: Total Liabilities
            Row totalLiabRow = sheet.createRow(rowIdx++);

            Cell liabLabelCell = totalLiabRow.createCell(1);
            liabLabelCell.setCellValue("Total Liabilities");
            liabLabelCell.setCellStyle(styles.get("total_label"));

            Cell liabTotalCell = totalLiabRow.createCell(2);
            if (dto.getTotalLiabilities() != null) {
                liabTotalCell.setCellValue(dto.getTotalLiabilities().doubleValue());
            }
            liabTotalCell.setCellStyle(styles.get("total_currency"));

            rowIdx++; // Spacer

            // === Equity ===
            // [INLINED] Section Header: Equity
            Row equityHeader = sheet.createRow(rowIdx++);
            Cell equityCell = equityHeader.createCell(0);
            equityCell.setCellValue("Equity");
            equityCell.setCellStyle(styles.get("section_header"));

            // [INLINED] Account Rows
            for (BalanceSheetAccountRowDTO acctRow : dto.getEquity()) {
                Row row = sheet.createRow(rowIdx++);

                Cell idCell = row.createCell(0);
                idCell.setCellValue(acctRow.getAccountId());
                idCell.setCellStyle(styles.get("data_label"));

                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(acctRow.getAccountName());

                Cell balanceCell = row.createCell(2);
                if (acctRow.getSignedBalance() != null) {
                    balanceCell.setCellValue(acctRow.getSignedBalance().doubleValue());
                }
                balanceCell.setCellStyle(styles.get("data_currency"));
            }

            // [INLINED] Total Row: Total Equity
            Row totalEquityRow = sheet.createRow(rowIdx++);

            Cell equityLabelCell = totalEquityRow.createCell(1);
            equityLabelCell.setCellValue("Total Equity");
            equityLabelCell.setCellStyle(styles.get("total_label"));

            Cell equityTotalCell = totalEquityRow.createCell(2);
            if (dto.getTotalEquity() != null) {
                equityTotalCell.setCellValue(dto.getTotalEquity().doubleValue());
            }
            equityTotalCell.setCellStyle(styles.get("total_currency"));

            rowIdx++; // Spacer

            // [INLINED] Total Row: Total Liabilities + Equity
            BigDecimal totalLE = dto.getTotalLiabilities().add(dto.getTotalEquity());
            Row totalLERow = sheet.createRow(rowIdx++);

            Cell leLabelCell = totalLERow.createCell(1);
            leLabelCell.setCellValue("Total Liabilities + Equity");
            leLabelCell.setCellStyle(styles.get("total_label"));

            Cell leTotalCell = totalLERow.createCell(2);
            if (totalLE != null) {
                leTotalCell.setCellValue(totalLE.doubleValue());
            }
            leTotalCell.setCellStyle(styles.get("total_currency"));
            // --- End [INLINED] Data Rows ---


            // Autosize columns
            for (int i = 0; i < cols.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Exception e) {
                    sheet.setColumnWidth(i, 25 * 256); // Fallback (25 chars)
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose(); // dispose of temporary files
            return out.toByteArray();
        }
    }

    public byte[] exportCashFlow(CashFlowReportDTO report) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            SXSSFSheet sheet = wb.createSheet("Cash Flow Statement");

            Workbook wbRef = sheet.getWorkbook();
            CellStyle headerStyle = wbRef.createCellStyle();
            Font bold = wbRef.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            DataFormat df = wbRef.createDataFormat();
            CellStyle currencyStyle = wbRef.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            int rowIdx = 0;

            // Header
            Row title = sheet.createRow(rowIdx++);
            title.createCell(0).setCellValue("Cash Flow Statement");
            title.getCell(0).setCellStyle(headerStyle);

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period:");
            periodRow.createCell(1).setCellValue(report.getPeriodLabel());

            sheet.createRow(rowIdx++); // blank

            // Main Data
            String[][] data = {
                    {"Opening Cash Balance", report.getOpeningCash().toString()},
                    {"Cash Flow from Operations (CFO)", report.getCfo().toString()},
                    {"Cash Flow from Investing (CFI)", report.getCfi().toString()},
                    {"Cash Flow from Financing (CFF)", report.getCff().toString()},
                    {"Net Change in Cash", report.getNetChange().toString()},
                    {"Ending Cash Balance", report.getEndingCash().toString()},
                    {"Cash (from Balance Sheet)", report.getBalanceSheetCash().toString()},
                    {"Reconciled", report.isReconciled() ? "Yes" : "No"}
            };

            for (String[] line : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(line[0]);
                Cell val = row.createCell(1);
                if (line[0].equals("Reconciled")) {
                    val.setCellValue(line[1]);
                } else {
                    val.setCellValue(new BigDecimal(line[1]).doubleValue());
                    val.setCellStyle(currencyStyle);
                }
            }

            // Autosize
            sheet.trackAllColumnsForAutoSizing();
            for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose();
            return out.toByteArray();
        }
    }

    /**
     * Excel Helper to create all necessary cell styles.
     */
    private Map<String, CellStyle> createExcelStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<>();
        DataFormat df = wb.createDataFormat();

        Font boldFont = wb.createFont();
        boldFont.setBold(true);

        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);

        // Title style
        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);

        // Header style
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(boldFont);
        styles.put("header", headerStyle);

        // Data label style
        CellStyle dataLabelStyle = wb.createCellStyle();
        dataLabelStyle.setIndention((short) 1);
        styles.put("data_label", dataLabelStyle);

        // Bold label style
        CellStyle boldLabelStyle = wb.createCellStyle();
        boldLabelStyle.setFont(boldFont);
        styles.put("bold_label", boldLabelStyle);

        // Currency style
        CellStyle currencyStyle = wb.createCellStyle();
        // Using accounting format: _(#,##0.00);_((#,##0.00);_("-")
        currencyStyle.setDataFormat(df.getFormat("_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)"));
        styles.put("data_currency", currencyStyle);

        // Bold currency style
        CellStyle boldCurrencyStyle = wb.createCellStyle();
        boldCurrencyStyle.setFont(boldFont);
        boldCurrencyStyle.setDataFormat(currencyStyle.getDataFormat());
        styles.put("bold_currency", boldCurrencyStyle);

        // Percentage style
        CellStyle pctStyle = wb.createCellStyle();
        pctStyle.setDataFormat(df.getFormat("0.00%"));
        styles.put("data_pct", pctStyle);

        // Bold percentage style
        CellStyle boldPctStyle = wb.createCellStyle();
        boldPctStyle.setFont(boldFont);
        boldPctStyle.setDataFormat(pctStyle.getDataFormat());
        styles.put("bold_pct", boldPctStyle);

        return styles;
    }

    /**
     * Excel Helper to add a formatted data row.
     */
    private int addExcelRow(SXSSFSheet sheet, int rowIdx, String label, BigDecimal actual, BigDecimal budget,
                            BigDecimal variance, BigDecimal variancePct, Map<String, CellStyle> styles, String stylePrefix) {

        Row row = sheet.createRow(rowIdx);
        int c = 0;

        // Label
        Cell labelCell = row.createCell(c++);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.get(stylePrefix + "_label"));

        // Actual
        Cell actualCell = row.createCell(c++);
        if (actual != null) actualCell.setCellValue(actual.doubleValue());
        actualCell.setCellStyle(styles.get(stylePrefix + "_currency"));

        // Budget
        Cell budgetCell = row.createCell(c++);
        if (budget != null) budgetCell.setCellValue(budget.doubleValue());
        budgetCell.setCellStyle(styles.get(stylePrefix + "_currency"));

        // Variance
        Cell varianceCell = row.createCell(c++);
        if (variance != null) varianceCell.setCellValue(variance.doubleValue());
        varianceCell.setCellStyle(styles.get(stylePrefix + "_currency"));

        // Variance %
        Cell pctCell = row.createCell(c++);
        if (variancePct != null) {
            // POI % format expects a decimal (e.g., 0.5 for 50%)
            pctCell.setCellValue(variancePct.doubleValue() / 100.0);
        }
        pctCell.setCellStyle(styles.get(stylePrefix + "_pct"));

        return rowIdx + 1;
    }
}
