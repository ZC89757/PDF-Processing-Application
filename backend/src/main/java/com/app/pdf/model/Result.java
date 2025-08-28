package com.app.pdf.model;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String msg;
    private Object data;
    
    public static Result success(Object data) {
        Result result = new Result();
        result.code = 0;
        result.msg = "ok";
        result.data = data;
        return result;
    }
    
    public static Result error(String msg) {
        return error(500, msg);
    }
    
    public static Result error(int code, String msg) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        return result;
    }
}