package com.nomos.store.service.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.nomos.store.service.model.SaleDetail;
import com.nomos.store.service.model.SalesDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class SalesDocumentPdfService {

    public byte[] generatePdf(SalesDocument doc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, doc);

            addDocumentTitle(document, doc);

            addDetailsTable(document, doc);

            addTotals(document, doc);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de venta", e);
        }

        return out.toByteArray();
    }

    private void addHeader(Document document, SalesDocument doc) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.addElement(new Paragraph("MI EMPRESA S.A.C.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        companyCell.addElement(new Paragraph("RUC: 20123456789"));
        companyCell.addElement(new Paragraph("Av. Principal 123, Lima"));
        table.addCell(companyCell);

        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        clientCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        clientCell.addElement(new Paragraph("CLIENTE ID: " + doc.getSale().getClientId()));
        clientCell.addElement(new Paragraph("Fecha Emisión: " + doc.getIssueDate().format(DateTimeFormatter.ISO_DATE)));
        table.addCell(clientCell);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addDocumentTitle(Document document, SalesDocument doc) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        String typeName = doc.getType().name().replace("_", " ");
        String fullNumber = doc.getSeries() + "-" + doc.getNumber();

        Paragraph p = new Paragraph(typeName + " ELECTRÓNICA\n" + fullNumber, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        p.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(p);
        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addDetailsTable(Document document, SalesDocument doc) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1, 4, 2, 2});
        table.setWidthPercentage(100);

        String[] headers = {"Cant.", "Descripción / Producto", "P. Unit", "Total"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            c.setBackgroundColor(java.awt.Color.WHITE);
            c.setBorderWidthBottom(2);
            table.addCell(c);
        }

        for (SaleDetail detail : doc.getSale().getDetails()) {
            table.addCell(String.valueOf(detail.getQuantity()));

            table.addCell("Producto ID: " + detail.getProductId());
            table.addCell(formatCurrency(detail.getUnitPrice()));
            table.addCell(formatCurrency(detail.getSubtotal()));
        }

        document.add(table);
    }

    private void addTotals(Document document, SalesDocument doc) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        PdfPCell label = new PdfPCell(new Phrase("TOTAL A PAGAR:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        label.setBorder(Rectangle.NO_BORDER);
        label.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell amount = new PdfPCell(new Phrase(formatCurrency(doc.getTotalAmount()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        amount.setBorder(Rectangle.NO_BORDER);
        amount.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(label);
        table.addCell(amount);
        document.add(table);
    }

    private String formatCurrency(Double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(amount);
    }
}