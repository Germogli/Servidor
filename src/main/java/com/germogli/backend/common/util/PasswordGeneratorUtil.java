package com.germogli.backend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGeneratorUtil {
    public static void main(String[] args) {
        // Usa BCryptPasswordEncoder con la configuración por defecto (strength 10)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Si se pasa un argumento, se usa como contraseña, si no se usa "admin" por defecto
        String password = args.length > 0 ? args[0] : "admin";

        // Genera el hash de la contraseña
        String hashedPassword = encoder.encode(password);

        // Imprime la contraseña y su hash
        System.out.println("Contraseña: " + password);
        System.out.println("Hash generado: " + hashedPassword);
    }
}
