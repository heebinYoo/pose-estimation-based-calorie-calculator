package com.example.capstone2;

public class ListData {
    int Category;
    String Title;
    String Content;

    public ListData(int category, String title, String content1, String content2) {
        Category = category;
        Title = title;
        Content = content1;
        Content = content2;
    }

    public int getCategory() {
        return Category;
    }

    public void setCategory(int category) {
        Category = category;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
