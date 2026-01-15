package com.nomos.store.service.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.nomos.store.service.model.CreditDocument;
import com.nomos.store.service.model.LegalEntity;
import com.nomos.store.service.model.CreditDocumentStatus;
import com.nomos.store.service.repository.CreditDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class CreditDocumentPdfService {

    @Autowired
    private CreditDocumentRepository repository;

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {

        CreditDocument doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));



        if (doc.getStatus() != CreditDocumentStatus.SIGNED) {
            throw new IllegalStateException(
                    String.format("ACCESO DENEGADO: El documento %s se encuentra en estado %s. " +
                                    "Solo se pueden descargar PDFs oficiales de documentos FIRMADOS (SIGNED).",
                            id, doc.getStatus())
            );
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            PdfWriter.getInstance(document, out);
            document.open();

            if ("PAGARE".equalsIgnoreCase(String.valueOf(doc.getType()))) {
                generatePagareContent(document, doc);
            } else {
                generateLetraContent(document, doc);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error interno al renderizar el PDF", e);
        }

        return out.toByteArray();
    }

    private void generatePagareContent(Document document, CreditDocument doc) throws DocumentException {

        LegalEntity acreedor = doc.getCreditor();

        String deudorNombre = doc.getDebtorName();
        String deudorId = doc.getDebtorIdNumber();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, java.awt.Color.GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Font moneyFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 14);

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("PAGARÉ N° " + (doc.getDocumentNumber() != null ? doc.getDocumentNumber() : "___"), titleFont));
        leftCell.addElement(new Paragraph("Vencimiento: " + formatDate(doc.getDueDate()), valueFont));
        header.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph placeP = new Paragraph("Lugar de Pago: " + (doc.getPlaceOfPayment() != null ? doc.getPlaceOfPayment() : ""), labelFont);
        placeP.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(placeP);

        Paragraph amountP = new Paragraph(formatCurrency(doc.getAmount()), moneyFont);
        amountP.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(amountP);

        header.addCell(rightCell);
        document.add(header);

        addEmptyLine(document, 2);

        Font bodyFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);

        String bodyText = String.format(
                "Yo, %s, identificado con %s N° %s, me obligo incondicionalmente a pagar a la orden de " +
                        "%s, identificado con %s N° %s, la suma de %s.",
                deudorNombre != null ? deudorNombre.toUpperCase() : "________",
                detectDocLabel(deudorId),
                deudorId != null ? deudorId : "________",
                acreedor.getLegalName().toUpperCase(),
                detectDocLabel(acreedor.getTaxId()),
                acreedor.getTaxId(),
                formatCurrency(doc.getAmount())
        );

        Paragraph p = new Paragraph(bodyText, bodyFont);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setLeading(25f);
        document.add(p);

        if (doc.getLegalNotes() != null && !doc.getLegalNotes().isEmpty()) {
            addEmptyLine(document, 1);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC);
            Paragraph clauses = new Paragraph("Cláusulas Especiales: " + doc.getLegalNotes(), smallFont);
            clauses.setIndentationLeft(20);
            document.add(clauses);
        }

        addEmptyLine(document, 4);

        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);

        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.addElement(new Paragraph("Lugar y Fecha de Emisión", labelFont));
        dateCell.addElement(new Paragraph((doc.getPlaceOfIssue() != null ? doc.getPlaceOfIssue() : "Lima") + ", " + formatDate(doc.getIssueDate()), valueFont));
        footer.addCell(dateCell);

        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);
        signCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph line = new Paragraph("___________________________");
        line.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(line);

        Paragraph signLabel = new Paragraph("Firma del Deudor / Aceptante", labelFont);
        signLabel.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(signLabel);

        Paragraph nameLabel = new Paragraph(deudorNombre, valueFont);
        nameLabel.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(nameLabel);

        Paragraph dniLabel = new Paragraph(detectDocLabel(deudorId) + ": " + deudorId, labelFont);
        dniLabel.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(dniLabel);

        footer.addCell(signCell);
        document.add(footer);
    }

    private void generateLetraContent(Document document, CreditDocument doc) throws DocumentException {
        LegalEntity acreedor = doc.getCreditor();

        String deudorNombre = doc.getDebtorName();
        String deudorId = doc.getDebtorIdNumber();
        String avalNombre = doc.getGuarantorName();
        String avalId = doc.getGuarantorIdNumber();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(2);
        cell.setPadding(20);

        PdfPTable top = new PdfPTable(2);
        top.setWidthPercentage(100);

        PdfPCell c1 = new PdfPCell(new Phrase("LETRA DE CAMBIO N° " + (doc.getDocumentNumber() != null ? doc.getDocumentNumber() : ""), titleFont));
        c1.setBorder(Rectangle.NO_BORDER);

        PdfPCell c2 = new PdfPCell(new Phrase("POR: " + formatCurrency(doc.getAmount()), titleFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        top.addCell(c1);
        top.addCell(c2);
        cell.addElement(top);

        PdfPTable grid = new PdfPTable(3);
        grid.setSpacingBefore(10);
        grid.setWidthPercentage(100);

        addGridCell(grid, "Lugar de Giro", doc.getPlaceOfIssue());
        addGridCell(grid, "Fecha de Giro", formatDate(doc.getIssueDate()));
        addGridCell(grid, "Vencimiento", formatDate(doc.getDueDate()));

        cell.addElement(grid);

        Paragraph body = new Paragraph();
        body.setSpacingBefore(15);
        body.add(new Chunk("Se servirá Ud. pagar incondicionalmente por esta LETRA DE CAMBIO a la orden de " +
                acreedor.getLegalName().toUpperCase() + "\n\n"));
        body.add(new Chunk("La cantidad de: " + formatCurrency(doc.getAmount()) + "\n"));
        body.add(new Chunk("Lugar de Pago: " + (doc.getPlaceOfPayment() != null ? doc.getPlaceOfPayment() : "")));
        cell.addElement(body);

        PdfPTable bottom = new PdfPTable(2);
        bottom.setSpacingBefore(20);
        bottom.setWidthPercentage(100);

        PdfPCell girado = new PdfPCell();
        girado.addElement(new Paragraph("A CARGO DE (GIRADO):"));
        girado.addElement(new Paragraph(deudorNombre != null ? deudorNombre : "", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        girado.addElement(new Paragraph(detectDocLabel(deudorId) + ": " + (deudorId != null ? deudorId : "")));


        girado.setBorder(Rectangle.BOX);
        girado.setPadding(5);
        bottom.addCell(girado);

        PdfPCell avalCell = new PdfPCell();
        avalCell.addElement(new Paragraph("AVAL / GARANTE:"));
        if(avalNombre != null && !avalNombre.isEmpty()) {
            avalCell.addElement(new Paragraph(avalNombre));
            avalCell.addElement(new Paragraph(detectDocLabel(avalId) + ": " + avalId));
            avalCell.addElement(new Paragraph("Firma: ___________"));
        } else {
            avalCell.addElement(new Paragraph("SIN AVAL"));
        }
        avalCell.setBorder(Rectangle.BOX);
        avalCell.setPadding(5);
        bottom.addCell(avalCell);

        cell.addElement(bottom);
        mainTable.addCell(cell);
        document.add(mainTable);
    }

    private void addGridCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.addElement(new Paragraph(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setBorder(Rectangle.BOX);
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" "));
        }
    }

    private String formatCurrency(Object amount) {
        if (amount == null) return "S/ 0.00";
        double value = (amount instanceof BigDecimal) ? ((BigDecimal) amount).doubleValue() : (Double) amount;
        return NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(value);
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "___";
        LocalDate date;
        if (dateObj instanceof LocalDate) {
            date = (LocalDate) dateObj;
        } else {
            date = LocalDate.parse(dateObj.toString());
        }
        return date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));
    }

    private String detectDocLabel(String taxId) {
        if (taxId == null) return "DOC";
        if (taxId.length() == 11) return "RUC";
        if (taxId.length() == 8) return "DNI";
        return "DOC";
    }
}