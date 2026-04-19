package com.alvarezg.expenses.service;

import com.alvarezg.expenses.dto.ExpenseRequest;
import com.alvarezg.expenses.dto.ExpenseResponse;
import com.alvarezg.expenses.dto.ExpenseSummaryResponse;
import com.alvarezg.expenses.exception.ResourceNotFoundException;
import com.alvarezg.expenses.exception.UnauthorizedException;
import com.alvarezg.expenses.model.Category;
import com.alvarezg.expenses.model.Expense;
import com.alvarezg.expenses.model.User;
import com.alvarezg.expenses.repository.CategoryRepository;
import com.alvarezg.expenses.repository.ExpenseRepository;
import com.alvarezg.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Obtener usuario autenticado
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    // Mapear Expense → ExpenseResponse
    private ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setDate(expense.getDate());
        if (expense.getCategory() != null) {
            response.setCategoryName(expense.getCategory().getName());
            response.setCategoryIcon(expense.getCategory().getIcon());
            response.setCategoryColor(expense.getCategory().getColor());
        }
        return response;
    }

    // Listar todos los gastos del usuario
    public List<ExpenseResponse> getAll() {
        User user = getCurrentUser();
        return expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Listar gastos por mes
    public List<ExpenseResponse> getByMonth(int year, int month) {
        User user = getCurrentUser();
        return expenseRepository.findByUserAndMonth(user.getId(), year, month)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Crear gasto
    public ExpenseResponse create(ExpenseRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    // Actualizar gasto
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));

        // Verificar que el gasto pertenece al usuario
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para editar este gasto");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setCategory(category);

        return toResponse(expenseRepository.save(expense));
    }

    // Eliminar gasto
    public void delete(Long id) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para eliminar este gasto");
        }

        expenseRepository.delete(expense);
    }

    // Exportar gastos a Excel por rango de fechas
    public byte[] exportToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicio no puede ser mayor que la fecha fin");
        }

        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(user.getId(), startDate, endDate);

        if (expenses.isEmpty()) {
            throw new ResourceNotFoundException("No hay gastos en el período seleccionado");
        }

        List<Object[]> categorySums = expenseRepository
                .sumByCategoryAndDateRange(user.getId(), startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Gastos");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Estilos
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalLabelStyle.setFont(boldFont);
            setBorder(totalLabelStyle);

            CellStyle totalValueStyle = workbook.createCellStyle();
            totalValueStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalValueStyle.setFont(boldFont);
            DataFormat format = workbook.createDataFormat();
            totalValueStyle.setDataFormat(format.getFormat("#,##0.00"));
            setBorder(totalValueStyle);

            CellStyle subLabelStyle = workbook.createCellStyle();
            setBorder(subLabelStyle);

            CellStyle subValueStyle = workbook.createCellStyle();
            subValueStyle.setDataFormat(format.getFormat("#,##0.00"));
            setBorder(subValueStyle);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Fila 0: Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Gastos: " + startDate.format(fmt) + " al " + endDate.format(fmt));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Fila 2: Encabezados
            Row headerRow = sheet.createRow(2);
            String[] headers = {"Fecha", "Categoría", "Descripción", "Monto"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de datos
            int rowIdx = 3;
            BigDecimal total = BigDecimal.ZERO;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(expense.getDate().format(fmt));
                row.createCell(1).setCellValue(
                        expense.getCategory() != null ? expense.getCategory().getName() : "Sin categoría");
                row.createCell(2).setCellValue(
                        expense.getDescription() != null ? expense.getDescription() : "");
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(expense.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);
                total = total.add(expense.getAmount());
            }

            // Fila vacía de separación
            rowIdx++;

            // Recuadro de totales — total general
            Row totalRow = sheet.createRow(rowIdx++);
            Cell totalLabel = totalRow.createCell(2);
            totalLabel.setCellValue("TOTAL GENERAL");
            totalLabel.setCellStyle(totalLabelStyle);
            Cell totalValue = totalRow.createCell(3);
            totalValue.setCellValue(total.doubleValue());
            totalValue.setCellStyle(totalValueStyle);

            // Subtotales por categoría
            for (Object[] row : categorySums) {
                Row subRow = sheet.createRow(rowIdx++);
                Cell subLabel = subRow.createCell(2);
                subLabel.setCellValue((String) row[0]);
                subLabel.setCellStyle(subLabelStyle);
                Cell subValue = subRow.createCell(3);
                subValue.setCellValue(((BigDecimal) row[1]).doubleValue());
                subValue.setCellStyle(subValueStyle);
            }

            // Ajustar ancho de columnas
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.setColumnWidth(3, 4000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // Resumen mensual
    public ExpenseSummaryResponse getSummary(int year, int month) {
        User user = getCurrentUser();

        List<Object[]> results = expenseRepository.sumByCategory(user.getId(), year, month);

        Map<String, BigDecimal> byCategory = results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        BigDecimal total = byCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpenseSummaryResponse(total, byCategory);
    }
}