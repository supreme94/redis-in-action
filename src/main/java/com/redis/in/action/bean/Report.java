package com.redis.in.action.bean;
public class Report {

    private Long id;

    private String date;

    private String content;

    private String title;

    public Report() {
    }

    public Report(Long id, String date, String content, String title) {
        this.id = id;
        this.date = date;
        this.content = content;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}