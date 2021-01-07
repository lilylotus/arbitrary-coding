package cn.nihility.selector.service;

public interface Service {

    default void function() {
        System.out.println("Service Function.");
    }

}
