package com.example.social_media.model.enumtype;

public enum Phase {
    APPLYING,
    ADMITTED,
    STUDYING_ABROAD,
    RETURNED;

    @Override
    public String toString() {
        return this.name();
    } 
}
