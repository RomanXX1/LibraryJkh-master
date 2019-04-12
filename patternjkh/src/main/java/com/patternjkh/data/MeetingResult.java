package com.patternjkh.data;

public class MeetingResult {
    private String question, allDecision, userVoice, numberOfParticipants, voicesFor, voicesAgainst, voicesAbstained,
            voicesForPercent, voicesAgainstPercent, voicesAbstainedPercent;

    public MeetingResult(String question, String allDecision, String userVoice, String numberOfParticipants,
                         String voicesFor, String voicesAgainst, String voicesAbstained, String voicesForPercent,
                         String voicesAgainstPercent, String voicesAbstainedPercent) {

        this.question = question;
        this.allDecision = allDecision;
        this.userVoice = userVoice;
        this.numberOfParticipants = numberOfParticipants;
        this.voicesFor = voicesFor;
        this.voicesAgainst = voicesAgainst;
        this.voicesAbstained = voicesAbstained;
        this.voicesForPercent = voicesForPercent;
        this.voicesAgainstPercent = voicesAgainstPercent;
        this.voicesAbstainedPercent = voicesAbstainedPercent;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAllDecision() {
        return allDecision;
    }

    public void setAllDecision(String allDecision) {
        this.allDecision = allDecision;
    }

    public String getUserVoice() {
        return userVoice;
    }

    public void setUserVoice(String userVoice) {
        this.userVoice = userVoice;
    }

    public String getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(String numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public String getVoicesFor() {
        return voicesFor;
    }

    public void setVoicesFor(String voicesFor) {
        this.voicesFor = voicesFor;
    }

    public String getVoicesAgainst() {
        return voicesAgainst;
    }

    public void setVoicesAgainst(String voicesAgainst) {
        this.voicesAgainst = voicesAgainst;
    }

    public String getVoicesAbstained() {
        return voicesAbstained;
    }

    public void setVoicesAbstained(String voicesAbstained) {
        this.voicesAbstained = voicesAbstained;
    }

    public String getVoicesForPercent() {
        return voicesForPercent;
    }

    public void setVoicesForPercent(String voicesForPercent) {
        this.voicesForPercent = voicesForPercent;
    }

    public String getVoicesAgainstPercent() {
        return voicesAgainstPercent;
    }

    public void setVoicesAgainstPercent(String voicesAgainstPercent) {
        this.voicesAgainstPercent = voicesAgainstPercent;
    }

    public String getVoicesAbstainedPercent() {
        return voicesAbstainedPercent;
    }

    public void setVoicesAbstainedPercent(String voicesAbstainedPercent) {
        this.voicesAbstainedPercent = voicesAbstainedPercent;
    }
}
