package com.patternjkh.data;


import com.patternjkh.enums.FileType;

public class Photo {

    public String number, foto_path, name, date, id;
    public byte[] small_image;
    private FileType mFileType;

    public Photo() {
    }

    public Photo(byte[] small_image,String foto_path) {
        this.small_image = small_image;
        this.foto_path = foto_path;
    }

    public Photo(String id, String number, byte[] small_image, String foto_path, String name, String date) {
        this.id = id;
        this.number = number;
        this.small_image = small_image;
        this.foto_path = foto_path;
        this.name = name;
        this.date = date;
    }

    public Photo(String id, String number, byte[] small_image, String foto_path) {
        this.id = id;
        this.number = number;
        this.small_image = small_image;
        this.foto_path = foto_path;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public byte[] getSmall_image() {
        return small_image;
    }

    public void setSmall_image(byte[] small_image) {
        this.small_image = small_image;
    }

    public String getFoto_path() {
        return foto_path;
    }

    public void setFoto_path(String foto_path) {
        this.foto_path = foto_path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FileType getFileType() {
        return mFileType;
    }

    public void setFileType(FileType fileType) {
        mFileType = fileType;
    }

}
