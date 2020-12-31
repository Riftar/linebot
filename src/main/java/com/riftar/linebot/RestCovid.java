package com.riftar.linebot;

import com.riftar.linebot.model.covid.*;
import com.riftar.linebot.model.news.NewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

public class RestCovid {

    private static final String NEWS_API = "e1b7aef10ed5493c849100922466a4e3";

    @Autowired
    private RestTemplate restTemplate;

    public DataCountry getCountryData(String countryId)
    {
        try {
            final String uri = "https://covid19.mathdro.id/api/countries/"+countryId;

            RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
                return execution.execute(request, body);
            })).build();

            DataCountry result = restTemplate.getForObject(uri, DataCountry.class);
            return result;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Countries getCountryName()
    {
        try {
            final String uri = "https://covid19.mathdro.id/api/countries/";

            RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
                return execution.execute(request, body);
            })).build();

            Countries result = restTemplate.getForObject(uri, Countries.class);
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public DataDaily getDailyIndo()
    {
        try {
            final String uri = "https://indonesia-covid-19.mathdro.id/api/harian";

            RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
                return execution.execute(request, body);
            })).build();

            //DailyResponse data = restTemplate.getForObject(uri, DailyResponse.class);

            HttpEntity<DailyResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    DailyResponse.class);
            DailyResponse data = response.getBody();
            System.out.println(data.getData().get(0));
            DataDaily lastData = data.getData().get(data.getData().size() - 1);
            if (lastData.getJumlahKasusBaruperHari() == null){
                return data.getData().get(data.getData().size() - 2);
            } else{
                return lastData;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public NewsResponse getNews()
    {
        try {
            final String uri = "http://newsapi.org/v2/top-headlines?country=id&category=health&apiKey="+NEWS_API;

            RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
                return execution.execute(request, body);
            })).build();

            NewsResponse result = restTemplate.getForObject(uri, NewsResponse.class);
            System.out.println(result);
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

