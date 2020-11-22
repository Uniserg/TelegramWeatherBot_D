package telegramweatherbot;

import com.github.prominence.openweathermap.api.OpenWeatherMapManager;
import com.github.prominence.openweathermap.api.WeatherRequester;
import com.github.prominence.openweathermap.api.constants.Accuracy;
import com.github.prominence.openweathermap.api.constants.Language;
import com.github.prominence.openweathermap.api.constants.Unit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 *
 * @author Serg
 */
public class Bot extends TelegramLongPollingBot {
    HashMap<String, String> subscribes;
    Calendar c1;
    Calendar c2;
    Timer timer;
    boolean isChangeSettings = false;
    
    public static void main(String[] args) {
        ApiContextInitializer.init();
        
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            bot.initBot();
            telegramBotsApi.registerBot(bot);
            bot.sendOn20();
            bot.sendOn9();
            
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
        
    }
    
    public void sendOn9() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                broadcast();
            }
        },c1.getTime(), 30000);
    }
    
    public void sendOn20() {
        timer.schedule(new TimerTask() {
        @Override
        public void run() {
            broadcast();
            }
        }, c2.getTime(), 86400000);
    }

    public void initBot() {
        subscribes = new HashMap<>();
        
        //ВЫНЕСТИ В ОТДЕЛЬНЫЙ ПОТОК
        c1 = Calendar.getInstance();
        c2 = Calendar.getInstance();
        timer = new Timer();
        
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 44);
        c1.set(Calendar.SECOND, 00);

        c2.set(Calendar.HOUR_OF_DAY, 20);
        c2.set(Calendar.MINUTE, 00);
        c1.set(Calendar.SECOND, 00);
    }
    
    public String getWeather(String city) {
        System.out.println("ds");
        OpenWeatherMapManager openWeatherMapManager = new OpenWeatherMapManager("1946b0c3abfe50a3352de413456b55fd");
        WeatherRequester weatherRequester = openWeatherMapManager.getWeatherRequester();
        return weatherRequester
        .setLanguage(Language.RUSSIAN)
        .setUnitSystem(Unit.METRIC_SYSTEM)
        .setAccuracy(Accuracy.ACCURATE)
        .getByCityName(city).toString();
        
    }
    
    public void setLocation() {
        
    }
    
    @Override
    public String getBotToken() {
        return "1473191847:AAHgGFs1F9IPKPEJxJAfd0VQwZ0Ne9Rn9nk";
    }
    
    public void sendMsg(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public void sendSettings(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        
        try {
            setSettingsKeyboard(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        
        
        
    }
    
    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
       
        
        try {
            setKeyboardSubscribe(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            String text = message.getText();
            System.out.println(text);
            
            String chatId = message.getChatId().toString();
            switch (text) {
                case "/help":
                    sendMsg(message, "Чем могу помочь?");
                    break;
                case "/settings":
                    sendSettings(message, "Что будем настраивать?");
                    break;
                case "Сменить город":
                    isChangeSettings = true;
                    sendMsg(message, "Укажите город.");
                    break;
                case "/start":
                    sendMsg(message, "Здравствуйте! Ввведите в чат город, чтобы получить инормацию о погоде на сегодня!");
                    break;
                case "/subscribe":
                case "Подписаться на рассылку":
                    if (!subscribes.containsKey(chatId)){
                        subscribes.put(chatId, "Москва");
                        sendMsg(message, "Отлично! Вы подписались на рассылку погоды. По умолчанию, информация о погоде по городу Москва. Вы сменить город, написав команду /settings. Вам будет приходить уведомление круглосуточно в 9:00 и в 20:00 по МСК");
                    }
                    else {
                        sendMsg(message, "Вы уже подписались на рассылку");
                    }   break;
                case "/unsubscribe":
                case "Отписаться от рассылки":
                    if (subscribes.containsKey(chatId)) {
                        subscribes.remove(chatId);
                        sendMsg(message, "Вы отписались от рассылки погоды.");
                    }
                    else {
                        sendMsg(message, "Вы не подписывались на рассылку!");
                    }   break;
                default:
                    try {
                        if (isChangeSettings) {
                            subscribes.put(message.getChatId().toString(), text);
                            sendMsg(message, "Проверка...");
                            sendMsg(message, getWeather(text));
                            isChangeSettings = false;
                        }
                    } catch (Exception e) {
                        sendMsg(message, "К сожалению, мы не нашли такой город. Попробуйте еще раз!");
                    }
            }
        }
    }
    
    public void setSettingsKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        
        
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton key = new KeyboardButton();
        
        key.setText("Сменить город");
        
        keyboardFirstRow.add(key);
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }
    
    public void setKeyboardSubscribe(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton key = new KeyboardButton();
        
        if (!subscribes.containsKey(sendMessage.getChatId()))
            key.setText("Подписаться на рассылку");
        else
            key.setText("Отписаться от рассылки");
        
        keyboardFirstRow.add(key);
        
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        
    }
    
    public void broadcast() {
        for (Map.Entry<String, String> subscribe : subscribes.entrySet()) {
            sendMsg(subscribe.getKey(), getWeather(subscribe.getValue()));
        }
    }
    
    @Override
    public String getBotUsername() {
        return "weather_uni_bot";
    }
    
}
