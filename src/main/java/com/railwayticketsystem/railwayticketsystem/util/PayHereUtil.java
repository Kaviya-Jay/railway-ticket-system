package com.railwayticketsystem.railwayticketsystem.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

public class PayHereUtil {

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 1. Checkout සඳහා Hash එක සෑදීම (පාරිභෝගිකයා PayHere වෙත යැවීමේදී)
    public static String generateHash(String merchantId, String orderId, double amount, String currency, String merchantSecret) {
        DecimalFormat df = new DecimalFormat("0.00");
        String amountFormatted = df.format(amount);
        return getMd5(merchantId + orderId + amountFormatted + currency + getMd5(merchantSecret));
    }

    // 2. Webhook (Notify) එකෙන් එන දත්ත Verify කිරීම සඳහා Hash එක සෑදීම (Background Server Callback එක සඳහා)
    public static String generateWebhookHash(String merchantId, String orderId, String payhereAmount, String payhereCurrency, int statusCode, String merchantSecret) {
        // මෙහිදී PayHere එකෙන් එවන Amount එක String එකක් ලෙස කෙලින්ම යොදා ගනී
        return getMd5(merchantId + orderId + payhereAmount + payhereCurrency + statusCode + getMd5(merchantSecret));
    }
}