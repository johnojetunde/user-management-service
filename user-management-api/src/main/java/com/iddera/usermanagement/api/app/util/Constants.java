package com.iddera.usermanagement.api.app.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Constants {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd",
            new Locale.Builder().setRegion("NG").setLanguage("en").build()
    );
    public static final String BASE_TEMPLATE_PATH = "566";

    public static final String ACTIVATE_USER_TEMPLATE = "welcome-template";

//    public static final String FORGOT_PASSWORD_TEMPLATE = "";




    private Constants() {
    }
}
