package com.riftar.linebot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.riftar.linebot.Utils.NumberUtils;
import com.riftar.linebot.model.EventsModel;
import com.riftar.linebot.model.covid.DataCountry;
import com.riftar.linebot.model.covid.DataDaily;
import com.riftar.linebot.model.news.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@EnableScheduling
public class Controller {

    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Scheduled(cron = "0 * 16 * * *")
    public void dailyUpdateCovid(){
        try {
            if (!Constant.userId.equals("")){
                String date = NumberUtils.getDate();
                System.out.println("update daily data "+date);
//                String messageToUser = composeDailyData();
//                TextMessage textMessage = new TextMessage(messageToUser);
//                PushMessage pushMessage = new PushMessage(Constant.userId, textMessage);
//                push(pushMessage);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error execute daily data");
        }
    }

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            // parsing event
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);

            eventsModel.getEvents().forEach((event)->{
                if (event instanceof MessageEvent) {
                    MessageEvent messageEvent = (MessageEvent) event;
                    Constant.userId = messageEvent.getMessage().getId();
                    String token = messageEvent.getReplyToken();
                    if (messageEvent.getMessage().getClass() == TextMessageContent.class){
                        TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();
                        handleTextMessage(token, textMessageContent);
                    } else if (messageEvent.getMessage().getClass() == LocationMessageContent.class){
                        LocationMessageContent loc = (LocationMessageContent) messageEvent.getMessage();
                        replyText(token, "Lokasi anda "+ loc.getLatitude() +" "+loc.getLongitude());
                    } else if (messageEvent.getMessage().getClass() == ImageMessageContent.class){
                        ImageMessageContent img = (ImageMessageContent) messageEvent.getMessage();
                        replyText(token, "gambar yg bagus");
                    }
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void handleTextMessage(String token, TextMessageContent textMessageContent) {
        String[] msg = textMessageContent.getText().toLowerCase().split(" ", 2);
        switch (msg[0]) {
            case "!location": {
                if (msg.length > 1) {
                    Constant.userLocation = msg[1];
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !location + nama lokasi.");
                }
            } break;
            case "!covid": {
                if (msg.length > 1) {
                    handleCountryMessage(token, msg[1]);
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !search + nama restaurant.");
                }
            } break;
            case "!daily": {
                handleDailyMessage(token);
            } break;
            case "!news": {
                handleNewsMessage(token);
            } break;
        }
    }

    private void handleNewsMessage(String replyToken){
        RestCovid req = new RestCovid();
        List<Article> arr = req.getNews().getArticles();
        List<CarouselColumn> carouselColumns = new ArrayList<CarouselColumn>();

        for (int i=0; i<3; i++){
            Article article = arr.get(i);

            String myUri = article.getUrl();
            String imageUri = article.getUrlToImage();

            URIAction action = new URIAction("read more..", myUri);
            List<Action> actionList = new ArrayList<Action>();
            actionList.add(action);
            CarouselColumn col = new CarouselColumn(
                    imageUri,
                    article.getTitle().substring(0,35), //max 40
                    article.getDescription().substring(0,55), //max 60
                    actionList
            );

            carouselColumns.add(col);
        }
        CarouselTemplate carousel = new CarouselTemplate(carouselColumns);
        TemplateMessage message = new TemplateMessage("Berita terbaru seputar Covid-19", carousel);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, message);
        reply(replyMessage);
    }

    private void handleDailyMessage(String token) {
        String message = composeDailyData();
        replyText(token, message);
    }

    private String composeDailyData() {
        RestCovid restCovid = new RestCovid();
        DataDaily dataDaily = restCovid.getDailyIndo();
        String finalMsg = String.format("Total Kasus Covid19 pada tanggal %s : \n %d confirmed \n %d recovered \n %d death",
                NumberUtils.formatDate(dataDaily.getTanggal()),
                dataDaily.getJumlahKasusBaruperHari(),
                dataDaily.getJumlahKasusSembuhperHari(),
                dataDaily.getJumlahKasusMeninggalperHari());
        return finalMsg;
    }

    private void handleCountryMessage(String token, String query) {
        RestCovid restCovid = new RestCovid();
        if (restCovid.getCountryData(query) != null) {
            DataCountry dataCountry = restCovid.getCountryData(query);
            String finalMsg = String.format("Total Kasus Covid19 di %s : \n %d confirmed \n %d recovered \n %d death",
                    query,
                    dataCountry.getConfirmed().getValue(),
                    dataCountry.getRecovered().getValue(),
                    dataCountry.getDeaths().getValue());
            replyText(token, finalMsg);
        } else {
            replyText(token, "Keyword anda kurang sesuai. \n Negara" + query + " tidak ditemukan.");
        }
    }


    private void replyText(String replyToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);
        reply(replyMessage);
    }

    private void reply(ReplyMessage replyMessage) {
        try {
            lineMessagingClient.replyMessage(replyMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void push(PushMessage pushMessage){
        try {
            lineMessagingClient.pushMessage(pushMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
