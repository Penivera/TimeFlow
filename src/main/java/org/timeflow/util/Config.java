package org.timeflow.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String USERNAME = dotenv.get("SMTP_USERNAME");
    public static final String EMAIL_PASSWORD = dotenv.get("SMTP_PASSWORD");
    public static final String SMTP_HOST = dotenv.get("SMTP_HOST");
    public static final String SMTP_PORT = dotenv.get("SMTP_PORT");
    public static final String SEND_FROM = dotenv.get("SEND_FROM");

    // Optional: fail fast if env vars are missing
    static {
        if (USERNAME == null || EMAIL_PASSWORD == null) {
            throw new IllegalStateException("Missing EMAIL_USERNAME or EMAIL_PASSWORD in .env");
        }

    }

    @Override
    public String toString() {
        return "Config{" +
                "USERNAME='" + USERNAME + '\'' +
                ", EMAIL_PASSWORD='[HIDDEN]'" +
                ", SMTP_HOST='" + SMTP_HOST + '\'' +
                ", SMTP_PORT='" + SMTP_PORT + '\'' +
                ", SEND_FROM='" + SEND_FROM + '\'' +
                '}';
    }

}
