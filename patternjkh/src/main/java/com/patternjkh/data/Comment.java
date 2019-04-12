package com.patternjkh.data;

public class Comment {

    int number;
    String text, owner, name;
    boolean isAuthor, isHidden;

    public Comment(int _number, String _text, String _owner, String _name, Boolean _isAuthor, Boolean _isHidden) {
        number = _number;
        owner = _owner;
        name = _name;
        text = _text;
        isAuthor = _isAuthor;
        isHidden = _isHidden;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Boolean getAuthor() {
        return isAuthor;
    }

    public void setAuthor(Boolean author) {
        isAuthor = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHidden() {
        return isHidden;
    }

    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }
}
