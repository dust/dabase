package com.kmfrog.dabase.example;

import java.util.Date;

/**
 * Created by dust on 6/14/16.
 */
public class Versus {

    private String createdBy;
    private Date deadline;
    private String tagName;


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
