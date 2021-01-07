package cn.nihility.entity;

public class DogWalk implements Walk {
    @Override
    public void walk() {
        System.out.println("dog walking.");
    }
}
