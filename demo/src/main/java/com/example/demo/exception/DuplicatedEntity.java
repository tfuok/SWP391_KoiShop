package com.example.demo.exception;

public class DuplicatedEntity extends RuntimeException {
    public DuplicatedEntity(String msg) {
        super(msg);
    }
}
