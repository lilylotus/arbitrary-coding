package cn.nihility.aspect;

import org.springframework.stereotype.Component;

@Component
public class Arithmetic {

    public int add(int x, int y) {
        return x + y;
    }

}
