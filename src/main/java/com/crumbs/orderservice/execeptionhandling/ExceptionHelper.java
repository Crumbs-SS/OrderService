package com.crumbs.orderservice.execeptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class ExceptionHelper {
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    public static class BusyDriverException extends RuntimeException{ }
    public static class OrderNoLongerAvailableException extends RuntimeException{ }

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<Object> handleException(NoSuchElementException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusyDriverException.class)
    public ResponseEntity<Object> handleBusyDriverException(){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(MESSAGE, "Driver is already delivering an order");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(OrderNoLongerAvailableException.class)
    public ResponseEntity<Object> handleOrderUnavailableException(){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(MESSAGE, "Order no longer available");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

}
