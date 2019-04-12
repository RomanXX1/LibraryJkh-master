package com.patternjkh.data;

public class Meeting {

    private int id, questionsNumber;
    private String title, dateEnd, type;
    private boolean isAnsweredAnyQuestion, isCompleted;

    public Meeting(int id, int questionsNumber, String title, String dateEnd, boolean isAnsweredAnyQuestion, boolean isCompleted, String type) {
        this.id = id;
        this.questionsNumber = questionsNumber;
        this.title = title;
        this.dateEnd = dateEnd;
        this.isAnsweredAnyQuestion = isAnsweredAnyQuestion;
        this.isCompleted = isCompleted;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionsNumber() {
        return questionsNumber;
    }

    public void setQuestionsNumber(int questionsNumber) {
        this.questionsNumber = questionsNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public boolean getIsAnsweredAnyQuestion() {
        return isAnsweredAnyQuestion;
    }

    public void setAnsweredAnyQuestion(boolean answeredAnyQuestion) {
        isAnsweredAnyQuestion = answeredAnyQuestion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
