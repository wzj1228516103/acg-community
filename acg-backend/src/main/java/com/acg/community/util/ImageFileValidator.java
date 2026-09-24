package com.acg.community.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class ImageFileValidator {

    private ImageFileValidator() {
    }

    public static boolean hasMatchingImageSignature(String fileName, InputStream input) throws IOException {
        String extension = extensionOf(fileName);
        if (extension == null) {
            return false;
        }

        byte[] header = input.readNBytes(12);
        String detectedType = detectType(header);
        return detectedType != null && matchesExtension(detectedType, extension);
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean matchesExtension(String detectedType, String extension) {
        return switch (detectedType) {
            case "jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "png", "gif", "bmp", "webp" -> detectedType.equals(extension);
            default -> false;
        };
    }

    private static String detectType(byte[] header) {
        if (header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        if (header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
            return "png";
        }
        if (header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F'
                && ((header[3] == '8' && header[4] == '7' && header[5] == 'a') || (header[3] == '8' && header[4] == '9' && header[5] == 'a'))) {
            return "gif";
        }
        if (header.length >= 2 && header[0] == 'B' && header[1] == 'M') {
            return "bmp";
        }
        if (header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
