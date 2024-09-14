package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationHandler {
    //định nghĩa cho nó chạy mỗi khi gặp 1 cái exception nào đó

    //MethodArgumentNotValidException: là lỗi khi nhập sai, nếu gặp -> hàm này đc chạy
    @ExceptionHandler(MethodArgumentNotValidException.class)
//input đầu vào sai, FE check lại
    public ResponseEntity handleValidation(MethodArgumentNotValidException exception) {
        String message = "";
        //cứ mỗi thuộc tính lỗi -> xử lý
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            //
            message += fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return new ResponseEntity(message, HttpStatus.BAD_REQUEST);//input đầu vào sai, FE check lại
    }
}
