package com.sambhar.sambharappreport.entity;

public class ValidatorEntity {
    private boolean isValid;
    private String message;

    public boolean isValid() {
        return this.isValid;
    }

    public void setValid(boolean z) {
        this.isValid = z;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }
}
