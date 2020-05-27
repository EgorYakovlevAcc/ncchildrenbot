package com.ncquizbot.ncbot.pojo;

public enum HelloGoodbyeMessages {
    HELLO_MESSAGE("Ребята! Приключений час! Мы подготовили для вас веселую викторину по мотивам разных сказок.\n" +
            "Вы окунетесь в мир волшебства и встретитесь с любимыми героями. \n" +
            "Готовы проверить свою смекалку? Тогда скорее отвечайте на вопросы нашего Квиза\n"),
    GOODBYE_MESSAGE("Goodbye, Dear user!");
    public String text;

    HelloGoodbyeMessages(String text) {
        this.text = text;
    }
}
