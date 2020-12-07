package com.riftar.linebot;

import com.riftar.linebot.model.DataCountry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

public class RestCovid {
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value="/covid/{id}", method= RequestMethod.GET)
    public DataCountry getCountryData(
            @PathVariable("id") String countryId
            )
    {
        try {
            final String uri = "https://covid19.mathdro.id/api/countries/"+countryId;

            RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
                return execution.execute(request, body);
            })).build();

            DataCountry result = restTemplate.getForObject(uri, DataCountry.class);
            System.out.println("confirm:" + result.getConfirmed().getValue());
            System.out.println("recover:" + result.getRecovered().getValue());
            System.out.println("death:" + result.getDeath().getValue());
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

