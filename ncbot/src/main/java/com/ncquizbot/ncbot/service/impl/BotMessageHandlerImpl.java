package com.ncquizbot.ncbot.service.impl;

import com.ncquizbot.ncbot.bot.Bot;
import com.ncquizbot.ncbot.bot.MessagesPackage;
import com.ncquizbot.ncbot.model.*;
import com.ncquizbot.ncbot.pojo.HelloGoodbyeMessages;
import com.ncquizbot.ncbot.qrcode.QrCodeGenerator;
import com.ncquizbot.ncbot.qrcode.QrCodeGeneratorImpl;
import com.ncquizbot.ncbot.service.*;
import com.ncquizbot.ncbot.service.impl.dob.TextAndImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class BotMessageHandlerImpl implements BotMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    public static final String USER_SCORE = "Thank you it was last question. Your score is ";
    public static final String COMMAND_PRESENT = "present";
    public static final String COMMAND_GO = "go";
    public static final String COMMAND_NEXT = "next";
    public static final String COMMAND_FINISH = "finish";
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ScoreRangesMessengerService scoreRangesMessengerService;
    @Autowired
    private ImageForReactionService imageForReactionService;

    @Override
    public MessagesPackage handleMessage(Update update) {
        LOGGER.info("handleMessage [START]");
        Message message = update.getMessage();
        if (Objects.nonNull(message) && message.hasText()) {
            return handleInputMessage(message);
        }
        return handelCallbackQuery(update.getCallbackQuery());
    }

    private MessagesPackage handelCallbackQuery(CallbackQuery callbackQuery) {
        LOGGER.info("handelCallbackQuery [START]");
        User user = userService.findUserByTelegramId(callbackQuery.getFrom().getId());
        MessagesPackage messagesPackage = null;
        Long chatId = callbackQuery.getMessage().getChatId();
        String outputText = callbackQuery.getData();
        switch (outputText) {
            case COMMAND_GO: {
                messagesPackage = handleGoCommand(user);
                break;
            }
            case COMMAND_PRESENT: {
                messagesPackage = handlePresentCommand(user);
                break;
            }
            case COMMAND_NEXT: {
                messagesPackage = handleNextCommand(user);
                break;
            }
            case COMMAND_FINISH: {
                messagesPackage = handleFinishCommand(user);
                break;
            }
            default: {
                messagesPackage = handleAnswerAndGenerateAnswer(user, callbackQuery.getData());
                break;
            }
        }
        return messagesPackage;
    }

    private MessagesPackage handleFinishCommand(User user) {
        ScoreRangesMessenger scoreRangesMessenger = getScoreRangesMessengerByScore(user.getScore());
        String ouputMessageText = "Результат: " + user.getScore() + "\n" + scoreRangesMessenger.getText();
        byte[] outputMessageAttachment = scoreRangesMessenger.getPicture();
        MessagesPackage messagesPackage = getSendMessageForBot(ouputMessageText, user.getChatId(), null, outputMessageAttachment);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText(HelloGoodbyeMessages.GOODBYE_MESSAGE.text);
        messagesPackage.addMessageToPackage(sendMessage);
        return messagesPackage;
    }

    private ScoreRangesMessenger getScoreRangesMessengerByScore(Integer score) {
        return scoreRangesMessengerService.findScoreRangesMessangerByScore(score);
    }

    private MessagesPackage handleNextCommand(User user) {
        return getNextQuestionForUser(user);
    }

    private MessagesPackage handleStartCommand(User user) {
        LOGGER.info("handleStartCommand [START]");
        String ouputMessageText = HelloGoodbyeMessages.HELLO_MESSAGE.text;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        List<List<InlineKeyboardButton>> keyboardRowList = new ArrayList<>();
        InlineKeyboardButton keyboardButton = new InlineKeyboardButton();
        keyboardButton.setText("начать");
        keyboardButton.setCallbackData(COMMAND_GO);
        keyboardRow.add(keyboardButton);
        keyboardRowList.add(keyboardRow);
        inlineKeyboardMarkup.setKeyboard(keyboardRowList);
        return getSendMessageForBot(ouputMessageText, user.getChatId(), inlineKeyboardMarkup, null);
    }

    private MessagesPackage handleGoCommand(User user) {
        LOGGER.info("handleGoCommand [START]");
        userService.setActiveStatusTrue(user);
        return getNextQuestionForUser(user);
    }

    private MessagesPackage handleInputMessage(Message message) {
        LOGGER.info("handleInputMessage [START]");
        MessagesPackage messagesPackage = new MessagesPackage();
        if (Objects.nonNull(message) && message.hasText()) {
            User user = userService.createAndSaveUserByTelegramMessageIfCurrentDoesNotExist(message);
            if (!user.isActiveNow()) {
                messagesPackage.addMessagesToPackage(handleStartCommand(user).getMessages());
            }
        }
        return messagesPackage;
    }

    private MessagesPackage handlePresentCommand(User user) {
        LOGGER.info("handlePresentCommand [START]");
        MessagesPackage messagesPackage = new MessagesPackage();
        if (user.isGameOver()) {
            if (!user.isPresentGiven()) {
                return messagesPackage.addMessageToPackage(getQrCodeImageForPresent(user));
            }
        }
        return messagesPackage;
    }

    private MessagesPackage handleAnswerAndGenerateAnswer(User user, String currentMessageText) {
        LOGGER.info("handleAnswerAndGenerateAnswer [START]");
        LOGGER.info("handleAnswerAndGenerateAnswer input data: ", currentMessageText);
        MessagesPackage messagesPackage = new MessagesPackage();
        String ouputMessageText = "";
        InlineKeyboardMarkup inlineKeyboardMarkup = null;
        if (user.isActiveNow()) {
            userService.updateLastUserSessionDate(user);
            if (user.getQuestionNumber() > 0) {
                TextAndImage outputMessage = updateUserScore(user, currentMessageText);
                if (Objects.nonNull(outputMessage)) {
                    inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    if (user.getQuestionNumber() < questionService.findAll().size()) {
                        inlineKeyboardButton.setText("следующий");
                        inlineKeyboardButton.setCallbackData(COMMAND_NEXT);
                        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
                        keyboardButtons.add(inlineKeyboardButton);
                        List<List<InlineKeyboardButton>> keyboardRowsList = new ArrayList<>();
                        keyboardRowsList.add(keyboardButtons);
                        inlineKeyboardMarkup.setKeyboard(keyboardRowsList);
                    } else {
                        inlineKeyboardButton.setText("конец");
                        inlineKeyboardButton.setCallbackData(COMMAND_FINISH);
                        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
                        keyboardButtons.add(inlineKeyboardButton);
                        List<List<InlineKeyboardButton>> keyboardRowsList = new ArrayList<>();
                        keyboardRowsList.add(keyboardButtons);
                        inlineKeyboardMarkup.setKeyboard(keyboardRowsList);
                    }
                    messagesPackage = getSendMessageForBot(outputMessage.getText(), user.getChatId(), inlineKeyboardMarkup, outputMessage.getImage());
                }
            }
            return messagesPackage;
        } else {
            ouputMessageText = HelloGoodbyeMessages.GOODBYE_MESSAGE.text;
            return getSendMessageForBot(ouputMessageText, user.getChatId(), inlineKeyboardMarkup, null);
        }
    }

    private MessagesPackage getNextQuestionForUser(User user) {
        LOGGER.info("getNextQuestionForUser [START]");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        Question nextQuestion = getQuestionForUser(user);
        if (Objects.nonNull(nextQuestion)) {
            if (nextQuestion.getOptions().size() > 1) {
                inlineKeyboardMarkup = getQuestionWithMultipleOptions(nextQuestion.getOptions());
            }
        }
        return getSendMessageForBot(nextQuestion.getContent(), user.getChatId(), inlineKeyboardMarkup,
                nextQuestion.getAttachement());

    }

    private TextAndImage updateUserScore(User user, String userAnswerText) {
        LOGGER.info("updateUserScore [START]");
        Question lastQuestion = questionService.findQuestionById(user.getCurrentQuestionId());
        Integer questionWeight = lastQuestion.getWeight();
        Answer answer = lastQuestion.getAnswer();
        String answerText = Objects.isNull(answer) ? "" : answer.getContent();
        int answerIndex = lastQuestion.getOptions()
                .stream()
                .map(x -> x.getContent())
                .collect(Collectors.toList())
                .indexOf(answer.getContent());
        TextAndImage textAndImage = new TextAndImage();
        if (checkAnswer(Integer.parseInt(userAnswerText), answerIndex)) {
            userService.increaseUserScore(user, questionWeight);
                textAndImage.setImage(imageForReactionService.getImageByReaction("ok"));
        } else {
            textAndImage.setImage(imageForReactionService.getImageByReaction("sad"));
        }
        textAndImage.setText(lastQuestion.getOptions().get(Integer.parseInt(userAnswerText)).getReaction());
        return textAndImage;
    }

    private MessagesPackage getSendMessageForBot(String content, Long chatId, InlineKeyboardMarkup replyKeyboardMarkup, byte[] attachment) {
        LOGGER.info("getSendMessageForBot [START]");
        MessagesPackage messagesPackage = new MessagesPackage();
        SendMessage sendMessage = null;
        if (Objects.nonNull(content)) {
            sendMessage = new SendMessage();
            sendMessage.setText(content)
                    .setChatId(chatId);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        if (attachment != null) {
            InputStream photoInputStream = new ByteArrayInputStream(attachment);
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto
                    .setChatId(chatId)
                    .setNewPhoto("photo_" + chatId, photoInputStream);
            messagesPackage.addMessageToPackage(sendPhoto);
        }
        messagesPackage.addMessageToPackage(sendMessage);
        return messagesPackage;
    }

    private SendPhoto getQrCodeImageForPresent(User user) {
        QrCodeGenerator qrCodeGenerator = new QrCodeGeneratorImpl();
        qrCodeGenerator.processGeneratingQrCode(user);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qrCodeGenerator.getQrCode());
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user.getChatId())
                .setNewPhoto("qr code for present", byteArrayInputStream);
        return sendPhoto;
    }

    private Question getQuestionForUser(User user) {
        LOGGER.info("getQuestionForUser [START]");
        return userService.setNextQuestionToUser(user);
    }

    private InlineKeyboardMarkup getQuestionWithMultipleOptions(List<Option> options) {
        LOGGER.info("getQuestionWithMultipleOptions [START]");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRowList = new ArrayList<>();
        for (Option option : options) {
            List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setCallbackData(String.valueOf(options.indexOf(option)));
            inlineKeyboardButton.setText(option.getContent());
            keyboardRow.add(inlineKeyboardButton);
            keyboardRowList.add(keyboardRow);
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRowList);
        return inlineKeyboardMarkup;
    }

    private boolean checkAnswer(int userAnswer, int answer) {
        return userAnswer == answer;
    }
}
