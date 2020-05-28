package com.ncquizbot.ncbot.pojo;

public enum HelloGoodbyeMessages {
    HELLO_MESSAGE("Ребята! Приключений час! Мы подготовили для вас веселую викторину по мотивам разных сказок.\n" +
            "Вы окунетесь в мир волшебства и встретитесь с любимыми героями. \n" +
            "Готовы проверить свою смекалку? Тогда скорее отвечайте на вопросы нашего квиза.\n"),
    GOODBYE_MESSAGE("Спасибо за участие в викторине. До новых встреч !!!");
    public String text;

    HelloGoodbyeMessages(String text) {
        this.text = text;
    }
}
