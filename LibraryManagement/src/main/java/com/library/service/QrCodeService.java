package com.library.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrCodeService {

    /**
     * Tạo mảng byte chứa ảnh PNG của QR Code.
     *
     * @param text   Nội dung mã hóa (mã bản sách barcode)
     * @param width  Chiều rộng ảnh (pixel)
     * @param height Chiều cao ảnh (pixel)
     * @return byte[] của file ảnh PNG
     */
    public byte[] generateQrCodeImage(String text, int width, int height) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu mã hóa QR Code không được để trống!");
        }

        int finalWidth = width > 0 ? width : 250;
        int finalHeight = height > 0 ? height : 250;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Mức sửa lỗi cao (30%)
            hints.put(EncodeHintType.MARGIN, 1); // Viền nhỏ để tối ưu in ấn

            BitMatrix bitMatrix = qrCodeWriter.encode(text.trim(), BarcodeFormat.QR_CODE, finalWidth, finalHeight, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo mã QR Code: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo chuỗi Data URI Base64 cho ảnh QR Code để nhúng trực tiếp vào thẻ <img> HTML.
     *
     * @param text   Nội dung mã hóa (mã bản sách barcode)
     * @param width  Chiều rộng
     * @param height Chiều cao
     * @return Chuỗi "data:image/png;base64,..."
     */
    public String generateQrCodeBase64(String text, int width, int height) {
        byte[] imageBytes = generateQrCodeImage(text, width, height);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
}
