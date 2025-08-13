package org.timeflow.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String EMAIL = dotenv.get("EMAIL");
    public static final String EMAIL_PASSWORD = dotenv.get("EMAIL_PASSWORD");
    public static final String SMTP_HOST = dotenv.get("SMTP_HOST");
    public static final String SMTP_PORT = dotenv.get("SMTP_PORT");

    // Optional: fail fast if env vars are missing
    static {
        if (EMAIL == null || EMAIL_PASSWORD == null) {
            throw new IllegalStateException("Missing EMAIL_USERNAME or EMAIL_PASSWORD in .env");
        }
    }
}
