package com.yuki.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code; // service status code success = 0
    private String message;
    private T data;


    // 第一个<E>是泛型方法的声明，表明这是一个泛型类，第二,三个是该类接受数据的类型
    public static <E> Result <E> successWithData(E data) {
        return new Result<>(0,"ok",data);
    }

    public static <E> Result <E> successWithoutData() {
        return new Result<>  (0,"ok",null);
    }

    public static <E> Result <E> error() {
        return new Result <> (1,"error",null);
    }
}
