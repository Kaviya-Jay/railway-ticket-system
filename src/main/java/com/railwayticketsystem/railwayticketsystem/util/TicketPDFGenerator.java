package com.railwayticketsystem.railwayticketsystem.util;

import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class TicketPDFGenerator {
    public String generateTicketPDF(Booking b, String qrPath) throws Exception {
        String pdfName = "TICKET-" + b.getTransactionId() + ".pdf";
        Path pdfPath = Paths.get("tickets/" + pdfName);
        Files.createDirectories(pdfPath.getParent());

        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath.toFile()));
            document.open();

            // Header, Passenger info, Route, QR Image, etc.
            Image qrImage = Image.getInstance(qrPath);
            qrImage.scaleToFit(150, 150);
            document.add(qrImage);

            document.close();
        }
        return pdfPath.toString();
    }
}