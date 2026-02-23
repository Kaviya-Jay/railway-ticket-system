package com.railwayticketsystem.railwayticketsystem.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Component
public class TicketPDFGenerator {

    private static final String TICKETS_DIR = "./tickets/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String generateTicketPDF(Booking booking, String qrImagePath) throws Exception {
        String fileName = "TICKET-" + booking.getTransactionId() + ".pdf";
        Path pdfPath = Paths.get(TICKETS_DIR + fileName);

        // Create directory if not exists
        Files.createDirectories(pdfPath.getParent());

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath.toFile()));
        document.open();

        // ====================== HEADER ======================
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph title = new Paragraph("RAILWAY TICKET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph subtitle = new Paragraph("Sri Lanka Railways - Confirmed Ticket", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        document.add(Chunk.NEWLINE);

        // ====================== PASSENGER DETAILS ======================
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Passenger Name : " + booking.getUser().getFullName(), boldFont));
        document.add(new Paragraph("NIC            : " + booking.getUser().getNic(), normalFont));
        document.add(new Paragraph("Mobile         : " + booking.getUser().getMobile(), normalFont));
        document.add(Chunk.NEWLINE);

        // ====================== JOURNEY DETAILS ======================
        document.add(new Paragraph("Journey Details", boldFont));
        document.add(new Paragraph("From           : " + booking.getTrain().getStartStation().getStationName(), normalFont));
        document.add(new Paragraph("To             : " + booking.getTrain().getEndStation().getStationName(), normalFont));
        document.add(new Paragraph("Date           : " + booking.getJourneyDate().format(DATE_FORMAT), normalFont));
        document.add(new Paragraph("Departure      : " + booking.getTrain().getDepartureTime(), normalFont));
        document.add(new Paragraph("Arrival        : " + booking.getTrain().getArrivalTime(), normalFont));
        document.add(new Paragraph("Class          : " + booking.getSeatClass().getClassType() + " Class", normalFont));
        document.add(new Paragraph("Price          : Rs. " + booking.getSeatClass().getPrice(), normalFont));
        document.add(Chunk.NEWLINE);

        // ====================== TRANSACTION INFO ======================
        document.add(new Paragraph("Transaction ID : " + booking.getTransactionId(), boldFont));
        document.add(new Paragraph("Booking Time   : " + booking.getBookingTime().format(DATE_FORMAT) + " "
                + booking.getBookingTime().toLocalTime().format(TIME_FORMAT), normalFont));
        document.add(Chunk.NEWLINE);

        // ====================== QR CODE ======================
        document.add(new Paragraph("Scan QR Code for Verification", boldFont));
        Image qrImage = Image.getInstance(qrImagePath);
        qrImage.scaleToFit(180, 180);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        // ====================== FOOTER ======================
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Thank you for travelling with Sri Lanka Railways | This is a computer generated ticket",
                FontFactory.getFont(FontFactory.HELVETICA, 10));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        return pdfPath.toAbsolutePath().toString();
    }
}