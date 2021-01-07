package cn.nihility.selector.service;

import org.springframework.beans.factory.DisposableBean;

public class OrderService implements Service, DisposableBean {
    @Override
    public void destroy() throws Exception {
        System.out.println("OrderService -> destroy.");
    }
}
