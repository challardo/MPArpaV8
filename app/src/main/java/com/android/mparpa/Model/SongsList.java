package com.android.mparpa.Model;

public class SongsList {

    private String title;
    private String subTitle;
    private String path;
    private String album;
    private String duration;
    private String id;
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SongsList(String path, String title, String artist, String album, String duration, String id) {
        this.path = path;
        this.title = title;
        this.subTitle = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
