package cn.nihility.rpc.service;

public class Arithmetic implements IArithmetic {

    public Integer add(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public Integer division(Integer a, Integer b) {
        return a / b;
    }

}
