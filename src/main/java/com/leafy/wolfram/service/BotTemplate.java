package com.leafy.wolfram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class BotTemplate {

    @Autowired
    private BotService botService;

    public Message greetingMessage(Source source) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String encoding         = StandardCharsets.UTF_8.name();
            InputStream is          = classLoader.getResourceAsStream("flex_event.json");

            String flexTemplate = is != null ? IOUtils.toString(is, encoding) : "Hello %s";

            if (source instanceof GroupSource)
                flexTemplate = String.format(flexTemplate, "Group");
            else if (source instanceof RoomSource)
                flexTemplate = String.format(flexTemplate, "Room");
            else if (source instanceof UserSource)
                flexTemplate = String.format(flexTemplate, botService.getProfile(source.getSenderId()).getDisplayName());
            else {
                flexTemplate = "Unknown Message Source!";
                return new TextMessage(flexTemplate);
            }

            if (flexTemplate.startsWith("Hello"))
                return new TextMessage(flexTemplate);

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

            return new FlexMessage("Math Solver", flexContainer);
        } catch (IOException e) {
            e.printStackTrace();
            return new TextMessage("An unexpected error has occurred.");
        }
    }
}
