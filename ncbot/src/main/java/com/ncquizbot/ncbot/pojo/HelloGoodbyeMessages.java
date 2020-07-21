package com.ncquizbot.ncbot.pojo;

public enum HelloGoodbyeMessages {
    HELLO_MESSAGE("Готов окунуться в мир волшебства аниме? Тогда жми старт!\n"),
    GOODBYE_MESSAGE("Спасибо за участие в викторине. До новых встреч !!!");
    public String text;

    HelloGoodbyeMessages(String text) {
        this.text = text;
    }
}
