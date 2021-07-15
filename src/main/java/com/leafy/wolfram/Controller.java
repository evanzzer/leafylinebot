package com.leafy.wolfram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leafy.wolfram.service.BotService;
import com.leafy.wolfram.service.BotTemplate;
import com.leafy.wolfram.service.Wolfram;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@SuppressWarnings({"rawtypes", "SpringJavaAutowiredFieldsWarningInspection"})
public class Controller {

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    private BotService botService;

    @Autowired
    private BotTemplate botTemplate;

    @Autowired
    private Wolfram wolfram;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload
    ) {
        try {
            // Validate Line Signature
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            System.out.println(eventsPayload);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);

            eventsModel.getEvents().forEach( event -> {
                if (event instanceof JoinEvent || event instanceof FollowEvent) {
                    String replyToken = ((ReplyEvent) event).getReplyToken();
                    handleJoinOrFollowEvent(replyToken, event.getSource());
                } else if (event instanceof MessageEvent) {
                    handleMessageEvent((MessageEvent) event);
                }
            });
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void greetingMessage(String replyToken, Source source, String additionalMessage) {
        Message greetingMessage = botTemplate.greetingMessage(source);

        if (additionalMessage != null) {
            List<Message> messages = new ArrayList<>();
            messages.add(new TextMessage(additionalMessage));
            messages.add(greetingMessage);
            botService.reply(replyToken, messages);
        } else {
            botService.reply(replyToken, greetingMessage);
        }
    }

    private void handleJoinOrFollowEvent(String replyToken, Source source) {
        greetingMessage(replyToken, source, null);
    }

    private void handleMessageEvent(MessageEvent event) {
        String replyToken      = event.getReplyToken();
        MessageContent content = event.getMessage();
        Source source          = event.getSource();

        if (content instanceof TextMessageContent)
            handleTextMessage(replyToken, (TextMessageContent) content, source);
        else greetingMessage(replyToken, source, null);
    }

    private void handleTextMessage(String replyToken, TextMessageContent content, Source source) {
        if (source instanceof GroupSource || source instanceof RoomSource)
            handleGroupOrRoomChats(replyToken, content.getText(), source);
        else if (source instanceof UserSource)
            handleOneOnOneChat(replyToken, content.getText(), source);
        else botService.replyText(replyToken, "Unknown Message Source!");
    }

    private void handleGroupOrRoomChats(String replyToken, String textMessage, Source source) {
        String msg = textMessage.toLowerCase();
        if (msg.startsWith("bot leave")) {
            if (source.getUserId().isEmpty())
                botService.replyText(replyToken, "Seems that you haven't add me as a friend. Add me first :(");
            else {
                if (source instanceof GroupSource) botService.leaveGroup(((GroupSource) source).getGroupId());
                else if (source instanceof RoomSource) botService.leaveRoom(((RoomSource) source).getRoomId());
            }
        } else if (msg.startsWith("calc ")
//                || msg.startsWith("calcimg ")
                || msg.startsWith("calcsolve ")) {
            processMessage(replyToken, textMessage);
        }
    }

    private void handleOneOnOneChat(String replyToken, String textMessage, Source source) {
        String msg = textMessage.toLowerCase();
        if (msg.startsWith("calc ")
//                || msg.startsWith("calcimg ")
                || msg.startsWith("calcsolve ")) {
            processMessage(replyToken, textMessage);
        } else handleFallbackMessage(replyToken, new UserSource(source.getUserId()));
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source,
                "Hello " + botService.getProfile(source.getUserId()).getDisplayName() +
                        ", I don't understand what you are trying to do. Kindly read the instructions below..."
        );
    }

    private void processMessage(String replyToken, String messageText) {
        String msg = messageText.toLowerCase();
        if (msg.startsWith("calc "))
            botService.reply(replyToken, wolfram.simpleApi(messageText.replaceAll("(?i)calc ", "").trim()));
//        else if (msg.startsWith("calcimg "))
//            botService.reply(replyToken, wolfram.imageApi(messageText.replaceAll("(?i)calcimg ", "").trim()));
        else if (msg.startsWith("calcsolve "))
            botService.reply(replyToken, wolfram.completeApi(messageText.replaceAll("(?i)calcsolve ", "").trim()));
    }

}
