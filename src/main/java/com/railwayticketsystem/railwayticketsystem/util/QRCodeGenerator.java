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
    public String generateQR(Booking booking) throws Exception {
        String data = String.format("%s|%s|%s to %s|%s %s|%s",
                booking.getUser().getFullName(),
                booking.getUser().getNic(),
                booking.getTrain().getStartStation().getStationName(),
                booking.getTrain().getEndStation().getStationName(),
                booking.getJourneyDate(),
                booking.getTrain().getDepartureTime(),
                booking.getTransactionId());

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);

        String qrFileName = "QR-" + booking.getTransactionId() + ".png";
        Path path = Paths.get("tickets/" + qrFileName);
        Files.createDirectories(path.getParent());
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return path.toString();
    }
}