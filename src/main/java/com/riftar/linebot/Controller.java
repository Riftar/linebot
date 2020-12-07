package com.riftar.linebot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.riftar.linebot.model.DataCountry;
import com.riftar.linebot.model.EventsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

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
                    handleCovidMessage(token, msg[1]);
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !search + nama restaurant.");
                }
            } break;
            case "!recomend": {
                replyText(token, "Berikut adalah rekomendasi dari kami : \n 1. User loc "+ Constant.userLocation);
            } break;
        }
    }

    private void handleCovidMessage(String token, String query) {
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
}
