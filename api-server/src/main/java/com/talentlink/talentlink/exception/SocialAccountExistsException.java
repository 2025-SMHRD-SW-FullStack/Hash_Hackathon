package com.talentlink.talentlink.exception;

public class SocialAccountExistsException extends RuntimeException {
    public SocialAccountExistsException(String message) {
        super(message);
    }
}