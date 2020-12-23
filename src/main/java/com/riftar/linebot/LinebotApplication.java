package com.riftar.linebot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class LinebotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinebotApplication.class, args);
		replyFlexMessage();
	}
	private static void replyFlexMessage() {
		try {
			ClassLoader classLoader = LinebotApplication.class.getClassLoader();
			String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_daily_covid.json"));


			ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
			FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

			JSONObject flex = objectMapper.readValue(flexTemplate, JSONObject.class);

			System.out.println("FLEX JSON "+flex);

			//ReplyMessage replyMessage = new ReplyMessage(replyToken, new FlexMessage("Covid Data", flexContainer));
			//reply(replyMessage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
