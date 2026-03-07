package com.railwayticketsystem.railwayticketsystem.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;

@Component
public class TicketPDFGenerator {

    public String generateTicketPDF(Booking booking, String qrPath) throws Exception {
        // PDF එක සේව් කළ යුතු ෆෝල්ඩරයේ මාර්ගය
        String directoryPath = "src/main/resources/static/tickets";

        // අදාල ෆෝල්ඩරය නොමැති නම් එය ස්වයංක්‍රීයව නිර්මාණය කිරීම
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String pdfPath = directoryPath + "/TICKET-" + booking.getTransactionId() + ".pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Sri Lanka Railways - E-Ticket", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // හිස් පේළියක්
        document.add(new Paragraph("-------------------------------------------------------------------------------------------"));

        // Booking Details
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Transaction ID: " + booking.getTransactionId(), normalFont));
        document.add(new Paragraph("Passenger Name: " + booking.getUser().getFullName(), normalFont));
        document.add(new Paragraph("NIC: " + booking.getUser().getNic(), normalFont));
        document.add(new Paragraph("Train: " + booking.getTrain().getTrainName(), boldFont));
        document.add(new Paragraph("Route: " + booking.getTrain().getStartStation().getStationName() + " to " + booking.getTrain().getEndStation().getStationName(), normalFont));
        document.add(new Paragraph("Journey Date: " + booking.getJourneyDate().toString(), normalFont));
        document.add(new Paragraph("Class Type: " + booking.getSeatClass().getClassType(), normalFont));

        document.add(new Paragraph("-------------------------------------------------------------------------------------------"));

        // Price Calculation
        BigDecimal unitPrice = booking.getSeatClass().getPrice();
        int quantity = booking.getQuantity();
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));

        document.add(new Paragraph("Unit Price: Rs. " + unitPrice, normalFont));
        document.add(new Paragraph("Tickets (Quantity): " + quantity + " Seats", normalFont));

        Font priceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.RED);
        document.add(new Paragraph("Total Paid Amount: Rs. " + totalPrice, priceFont));

        document.add(new Paragraph("-------------------------------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        // QR Code එක ඇතුලත් කිරීම
        if (qrPath != null && !qrPath.isEmpty()) {
            try {
                Image qrImage = Image.getInstance(qrPath);
                qrImage.scaleAbsolute(120, 120);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                document.add(qrImage);
            } catch (Exception e) {
                System.out.println("Could not load QR Code image for PDF");
            }
        }

        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Thank you for traveling with Sri Lanka Railways. Have a safe journey!", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return pdfPath;
    }
}