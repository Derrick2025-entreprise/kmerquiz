package com.exemple.kmerquiz.utils;

/**
 * Partie 8.2 : masquage des données personnelles avant de les écrire dans un log.
 * maskEmail("jean.mballa@gmail.com") -> "j***@gmail.com"
 * maskPhone("699123456")             -> "*****3456"
 */
public final class LogMasker {

    private LogMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        int visible = Math.min(4, phone.length());
        return "*".repeat(phone.length() - visible) + phone.substring(phone.length() - visible);
    }
}
