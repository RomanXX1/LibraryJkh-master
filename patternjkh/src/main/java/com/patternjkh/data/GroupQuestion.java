package com.patternjkh.data;

public class GroupQuestion {

    public String name, id_qroup;
    public boolean isAnswered, isRead;
    public int colQuestions, colAnswered;

    public GroupQuestion(String name, String id_qroup, boolean isAnswered, int colQuestions, int colAnswered, boolean isRead) {
        this.name = name;
        this.id_qroup = id_qroup;
        this.isAnswered = isAnswered;
        this.colQuestions = colQuestions;
        this.colAnswered = colAnswered;
        this.isRead = isRead;
    }
}
