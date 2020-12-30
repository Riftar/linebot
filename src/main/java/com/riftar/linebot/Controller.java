package com.riftar.linebot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.riftar.linebot.model.User;
import com.riftar.linebot.model.covid.Countries;
import com.riftar.linebot.repository.UserRepository;
import com.riftar.linebot.utils.Constant;
import com.riftar.linebot.utils.NumberUtils;
import com.riftar.linebot.model.EventsModel;
import com.riftar.linebot.model.covid.DataCountry;
import com.riftar.linebot.model.covid.DataDaily;
import com.riftar.linebot.model.news.Article;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 * 17 * * *")
    public void dailyUpdateCovid(){
        try {
            List<User> users = (List<User>) userRepository.findAll();
            if (!users.isEmpty()){
                String date = NumberUtils.getDate();
                System.out.println("update daily data "+date);
                String messageToUser = composeDailyData();
                TextMessage textMessage = new TextMessage(messageToUser);
                PushMessage pushMessage = new PushMessage(Constant.userId, textMessage);
                push(pushMessage);
            } else {
                System.out.println("User ID is empty");
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error execute daily data");
        }
    }

    @RequestMapping(value="/daily", method= RequestMethod.GET)
    public ResponseEntity<String> daily() {
        try {
            List<User> users = (List<User>) userRepository.findAll();
            if (!users.isEmpty()){
                String date = NumberUtils.getDate();
                System.out.println("update daily data "+ date);
                String messageToUser = composeDailyData();
                TextMessage textMessage = new TextMessage(messageToUser);
                users.forEach( (user) -> {
                    PushMessage pushMessage = new PushMessage(user.getUserId(), textMessage);
                    push(pushMessage);
                });
            } else {
                System.out.println("User ID is empty");
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error execute daily data: "+e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(value="/countries", method= RequestMethod.GET)
    public ResponseEntity<String> tes() {
        RestCovid restCovid = new RestCovid();
        DataCountry result = restCovid.getCountryData("id");
        System.out.println(result.getConfirmed());
        return new ResponseEntity<>(HttpStatus.OK);
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
                if (event instanceof FollowEvent){
                    FollowEvent followEvent = (FollowEvent) event;
                    saveUserId(followEvent);
                    System.out.println("save user id "+Constant.userId);
                } else if (event instanceof MessageEvent) {
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
                } else if (event instanceof UnfollowEvent){
                    UnfollowEvent unfollowEvent = (UnfollowEvent) event;
                    deleteUserId(unfollowEvent);
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void deleteUserId(UnfollowEvent event) {
        try {
            String userId = event.getSource().getUserId();
            List<User> users = (List<User>) userRepository.findAll();
            users.forEach((user -> {
                if (user.getUserId() == userId){
                    userRepository.delete(user);
                    System.out.println("User has been deleted");
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveUserId(FollowEvent event) {
        try {
            String userId = event.getSource().getUserId();
            UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
            String userName = profile.getDisplayName();

            User user = new User(userId, userName);
            user = userRepository.save(user);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
            case "!country": {
                if (msg.length > 1) {
                    composeCountryMessage(token, msg[1]);
                } else{
                    replyText(token, "Keyword anda kurang sesuai. \n Gunakan !country + nama negara.");
                }
            } break;
            case "!daily": {
                handleDailyMessage(token);
//                composeDailyFlexMessage(token);
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
        String date = NumberUtils.formatDate(dataDaily.getTanggal());
        String confirmed = NumberUtils.formatNumber(dataDaily.getJumlahKasusBaruperHari());
        String recovered = NumberUtils.formatNumber(dataDaily.getJumlahKasusSembuhperHari());
        String death = NumberUtils.formatNumber(dataDaily.getJumlahKasusMeninggalperHari());
        String finalMsg = String.format("Total Kasus Covid19 pada tanggal %s : \n %s confirmed \n %s recovered \n %s death",
                date,
                confirmed,
                recovered,
                death);
        return finalMsg;
    }

    private String searchCountryName(String query) {
        RestCovid restCovid = new RestCovid();
        Countries countries = restCovid.getCountryName();
        for (int i = 0; i<countries.getCountries().size()-1 ; i++){
            if (countries.getCountries().get(i).getIso2().equalsIgnoreCase(query) ||
                    countries.getCountries().get(i).getIso3().equalsIgnoreCase(query)){
                return countries.getCountries().get(i).getName();
            }
        }
        return "";
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

    private void replyFlexMessage(String replyToken, String flexTemplate) {
        try {
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            ReplyMessage replyMessage = new ReplyMessage(replyToken, new FlexMessage("Covid Data", flexContainer));
            reply(replyMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void composeDailyFlexMessage(String replyToken) {
       try{
           RestCovid restCovid = new RestCovid();
           DataDaily dataDaily = restCovid.getDailyIndo();
           String date = NumberUtils.formatDate(dataDaily.getTanggal());
           String confirmed = NumberUtils.formatNumber(dataDaily.getJumlahKasusBaruperHari());
           String recovered = NumberUtils.formatNumber(dataDaily.getJumlahKasusSembuhperHari());
           String death = NumberUtils.formatNumber(dataDaily.getJumlahKasusMeninggalperHari());

           ClassLoader classLoader = getClass().getClassLoader();
           String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("simple_daily_covid.json"));

           flexTemplate = flexTemplate.replace("@title", "Daily Update");
           flexTemplate = flexTemplate.replace("@negara", "Indonesia");
           flexTemplate = flexTemplate.replace("@tanggal", date);
           flexTemplate = flexTemplate.replace("@positif", confirmed);
           flexTemplate = flexTemplate.replace("@sembuh", recovered);
           flexTemplate = flexTemplate.replace("@meninggal", death);
           replyFlexMessage(replyToken, flexTemplate);
       } catch (IOException e){
           throw new RuntimeException(e);
       }
    }

    private void composeCountryMessage(String token, String query) {
        RestCovid restCovid = new RestCovid();
//        if (restCovid.getCountryData(query) != null) {
            try {
                DataCountry dataCountry = restCovid.getCountryData(query);
                String countryName = searchCountryName(query);
                if (countryName.equals("")) {
                    System.out.println("query gak ketemu");
                    replyText(token, "Keyword anda kurang sesuai. \n Kode Negara " + query + " tidak ditemukan.");
                } else {
                    String date = NumberUtils.formatDateCountry(dataCountry.getLastUpdate());
                    String confirmed = NumberUtils.formatNumber(dataCountry.getConfirmed().getValue());
                    String recovered = NumberUtils.formatNumber(dataCountry.getRecovered().getValue());
                    String death = NumberUtils.formatNumber(dataCountry.getDeaths().getValue());
                    ClassLoader classLoader = getClass().getClassLoader();
                    String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("simple_daily_covid.json"));

                    flexTemplate = flexTemplate.replace("@title", "Total Cases");
                    flexTemplate = flexTemplate.replace("@negara", countryName);
                    flexTemplate = flexTemplate.replace("@tanggal", date);
                    flexTemplate = flexTemplate.replace("@positif", confirmed);
                    flexTemplate = flexTemplate.replace("@sembuh", recovered);
                    flexTemplate = flexTemplate.replace("@meninggal", death);
                    replyFlexMessage(token, flexTemplate);
                }
            } catch (IOException e){
                throw new RuntimeException(e);
            }
//        } else {
//            replyText(token, "Keyword anda kurang sesuai. \n Kode Negara " + query + " tidak ditemukan.");
//        }
    }
}
