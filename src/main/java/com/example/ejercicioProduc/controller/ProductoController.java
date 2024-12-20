/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ejercicioProduc.controller;

import com.example.ejercicioProduc.model.Producto;
import com.example.ejercicioProduc.servicio.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", service.listarTodos());
        return "listaProductos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "formularioProducto";
    }

    @PostMapping
    public String guardarProducto(@ModelAttribute Producto producto) {
        service.guardar(producto);
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", service.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id)));
        return "formularioProducto";
    }

    @PostMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, @ModelAttribute Producto producto) {
        producto.setId(id);
        service.guardar(producto); // Save the updated product
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        service.eliminar(id); // Call the service to delete the product
        return "redirect:/productos";
    }

    @GetMapping("/reporte/pdf")
    public void generarReportePDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=productos_reporte.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Reporte de Productos").setBold().setFontSize(18));

        Table table = new Table(4);
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Categoría");
        table.addCell("Precio");

        List<Producto> productos = service.listarTodos();
        for (Producto producto : productos) {
            table.addCell(producto.getId().toString());
            table.addCell(producto.getNombre());
            table.addCell(producto.getCategoria());
            table.addCell(String.valueOf(producto.getPrecio()));
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/reporte/excel")
    public void generarReporteExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=productos_reporte.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");
        Row headerRow = sheet.createRow(0);
        String[] columnHeaders = { "ID", "Nombre", "Categoría", "Precio" };

        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        List<Producto> productos = service.listarTodos();
        int rowIndex = 1;
        for (Producto producto : productos) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(producto.getId());
            row.createCell(1).setCellValue(producto.getNombre());
            row.createCell(2).setCellValue(producto.getCategoria());
            row.createCell(3).setCellValue(producto.getPrecio());
        }

        for (int i = 0; i < columnHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
