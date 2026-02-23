package com.railwayticketsystem.railwayticketsystem.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class QRCodeGenerator {

    private static final String TICKETS_DIR = "./tickets/";

    public String generateQR(Booking booking) throws Exception {
        // QR Data as per requirement: Name|NIC|Route|Date & Time|TransactionID
        String qrData = String.format("%s|%s|%s → %s|%s %s|%s",
                booking.getUser().getFullName(),
                booking.getUser().getNic(),
                booking.getTrain().getStartStation().getStationName(),
                booking.getTrain().getEndStation().getStationName(),
                booking.getJourneyDate(),
                booking.getTrain().getDepartureTime(),
                booking.getTransactionId());

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

        String fileName = "QR-" + booking.getTransactionId() + ".png";
        Path qrPath = Paths.get(TICKETS_DIR + fileName);

        // Create directory if not exists
        Files.createDirectories(qrPath.getParent());

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrPath);

        return qrPath.toAbsolutePath().toString();
    }
}