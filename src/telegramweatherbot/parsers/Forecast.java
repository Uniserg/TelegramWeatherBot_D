package telegramweatherbot.parsers;


import java.io.IOException;

public abstract class Forecast {

    protected final static String API_KEY_TEMPLATE = "&units=metric&appid=";

    String city;
    String apiKey;

    public abstract String get() throws IOException;

    protected String getFormatTemp(double temp) {
        return String.format("%.0f C°", temp);
    }

    public String getFlag(String country) {
        int flagOffset = 0x1F1E6;
        int asciiOffset = 0x41;

        int firstChar = Character.codePointAt(country, 0) - asciiOffset + flagOffset;
        int secondChar = Character.codePointAt(country, 1) - asciiOffset + flagOffset;

        String flag = new String(Character.toChars(firstChar))
                + new String(Character.toChars(secondChar));
        return flag;

    }

}
