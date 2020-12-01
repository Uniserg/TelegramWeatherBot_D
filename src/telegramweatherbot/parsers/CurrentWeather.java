package telegramweatherbot.parsers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static telegramweatherbot.parsers.Forecast.API_KEY_TEMPLATE;

public class CurrentWeather extends Forecast{
    
    protected final static String API_CALL_TEMPLATE = "https://api.openweathermap.org/data/2.5/weather?q=";

    public CurrentWeather(String city, String apiKey) {
        this.city = city;
        this.apiKey = apiKey;
    }
    
    @Override
    public String get() {
        String json = JsonReader.readJsonFromUrl(API_CALL_TEMPLATE + city + API_KEY_TEMPLATE + apiKey);
        
        if ("404".equals(json))
            return "404";
        
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jo = jsonElement.getAsJsonObject();
        JsonObject weather = jo.get("weather").getAsJsonArray().get(0).getAsJsonObject();
        String wmain = weather.get("main").getAsString();
        JsonObject main = jo.get("main").getAsJsonObject();
        String visibility = jo.get("visibility").getAsString();
        JsonObject wind = jo.get("wind").getAsJsonObject();
        String country = jo.get("sys").getAsJsonObject().get("country").getAsString();
        long dt = jo.get("dt").getAsLong();
        long timizone = jo.get("timezone").getAsLong();
        
        
        String utc = getUtc(timizone); //to sb
        Date date = NormalizedDate.getNormalizedDate(dt, timizone);
        
        SimpleDateFormat formater = new SimpleDateFormat("EEEE, dd MMMM HH:mm YYYY", Locale.US);
        String fdate = formater.format(date); // to sb
        
        int hours = date.getHours();
        
        StringBuilder sb = new StringBuilder();
        sb.append(getFlag(country)).append(" ")
          .append('(').append(country).append(") ")
          .append(city).append('\n')
          .append(getIconTime(hours)).append(' ')
          .append(fdate)
          .append(" ").append(utc).append('\n');
        
        sb.append("Temperature: ").append(getFormatTemp(main.get("temp").getAsDouble())).append(' ')
          .append(WeatherUtils.weatherIconsCodes.get("Temperature")).append('\n')
          .append("Feels like: ").append(getFormatTemp(main.get("feels_like").getAsDouble())).append('\n')
          .append(wmain).append(' ').append(WeatherUtils.weatherIconsCodes.get(wmain)).append('\n')
          .append("Visibility: ").append(visibility).append(" m ")
          .append(WeatherUtils.weatherIconsCodes.get("Visibility")).append('\n')
          .append("Wind: ").append(wind.get("speed")).append(" met/sec").append(' ')
          .append(WeatherUtils.weatherIconsCodes.get("Wind"));
        
        return sb.toString();
    }
    

}
