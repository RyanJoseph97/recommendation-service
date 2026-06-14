package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowSummary {

    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
