package com.riftar.linebot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Field;
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
        String[] msg = textMessageContent.getText().toLowerCase().split(" ");
        switch (msg[0]) {
            case "!location": {
                if (textMessageContent.getText().toLowerCase().split(" ", 2)[1] != null) {
                    Constant.userLocation = textMessageContent.getText().toLowerCase().split(" ", 2)[1];
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !location + nama lokasi.");
                }
            } break;
            case "!search": {
                if (textMessageContent.getText().toLowerCase().split(" ", 2)[1] != null) {
                    String query = (textMessageContent.getText().toLowerCase().split(" ", 2)[1]);
                    replyText(token, "anda mencari restaurant "+query);
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !search + nama restaurant.");
                }
            } break;
            case "!recomend": {
                replyText(token, "Berikut adalah rekomendasi dari kami : \n 1. User loc "+ Constant.userLocation);
            } break;
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
