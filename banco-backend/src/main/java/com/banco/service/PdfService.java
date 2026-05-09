package com.banco.service;

import com.banco.dto.ReporteItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Genera PDF con los movimientos y lo retorna en Base64
    public String generarReportePdf(List<ReporteItem> items, String cliente, String desde, String hasta) {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font tituloFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph titulo = new Paragraph("Reporte de Movimientos", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            doc.add(titulo);

            Font infoFont = new Font(Font.HELVETICA, 10);
            doc.add(new Paragraph("Cliente: " + cliente, infoFont));
            doc.add(new Paragraph("Desde: " + desde + "    Hasta: " + hasta, infoFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2.5f, 2f, 1.5f, 1.5f, 1f, 1.5f, 1.8f});

            String[] headers = {"Fecha", "Cliente", "Numero Cuenta", "Tipo",
                    "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD)));
                cell.setBackgroundColor(new Color(220, 220, 220));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (ReporteItem item : items) {
                table.addCell(cell(item.getFecha() != null ? item.getFecha().format(FECHA) : ""));
                table.addCell(cell(item.getCliente()));
                table.addCell(cell(item.getNumeroCuenta()));
                table.addCell(cell(item.getTipo()));
                table.addCell(cell(item.getSaldoInicial() != null ? item.getSaldoInicial().toString() : ""));
                table.addCell(cell(String.valueOf(item.getEstado())));
                table.addCell(cell(item.getMovimiento() != null ? item.getMovimiento().toString() : ""));
                table.addCell(cell(item.getSaldoDisponible() != null ? item.getSaldoDisponible().toString() : ""));
            }

            doc.add(table);
            doc.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF", e);
        }

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private PdfPCell cell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", new Font(Font.HELVETICA, 9)));
        cell.setPadding(4);
        return cell;
    }
}
