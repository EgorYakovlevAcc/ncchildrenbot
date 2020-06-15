package com.ncquizbot.ncbot.pojo;

public enum HelloGoodbyeMessages {
    HELLO_MESSAGE("Приключения продолжаются! В загадочном мире животных много необычного и интересного." + 
                  "Проверить свои знания о нем и узнать больше мы предлагаем в этой познавательной викторине. \n"),
    GOODBYE_MESSAGE("Спасибо за участие в викторине. До новых встреч !!!");
    public String text;

    HelloGoodbyeMessages(String text) {
        this.text = text;
    }
}
