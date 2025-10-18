package com.clem.burgerbox;

public class Audio {
    private final String title;
    private final String uri; // peut être null si c’est un son interne

    public Audio(String title) {
        this(title, null);
    }

    public Audio(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() { return title; }
    public String getUri() { return uri; }
    public boolean isFromUri() { return uri != null; }
}
