package com.leafy.wolfram.service;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class BotService {

    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;

    private void reply(ReplyMessage replyMessage) {
        try {
            lineMessagingClient.replyMessage(replyMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void reply(String replyToken, Message message) {
        reply(new ReplyMessage(replyToken, message));
    }

    public void reply(String replyToken, List<Message> messages) {
        reply(new ReplyMessage(replyToken, messages));
    }

    public void replyText(String replyToken, String messageText) {
        reply(replyToken, new TextMessage(messageText));
    }

    public UserProfileResponse getProfile(String userId) {
        try {
            return lineMessagingClient.getProfile(userId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveGroup(String groupId) {
        try {
            lineMessagingClient.leaveGroup(groupId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveRoom(String roomId) {
        try {
            lineMessagingClient.leaveRoom(roomId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
