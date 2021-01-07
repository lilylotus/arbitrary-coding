package cn.nihility.registrar;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class MapperProxy {

    public static UserMapper getProxyUserMapper() {
        return (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class},
                (proxy, method, args1) -> {
                    System.out.println("proxy object : " + proxy.getClass().getName());
                    System.out.println("proxy method : " + method.getName());
                    System.out.println("proxy method args : " + Arrays.asList(args1));

                    Select annotation = method.getAnnotation(Select.class);
                    String selectSql = annotation.value()[0];
                    System.out.println("Select Sql : " + selectSql);

                    Integer arg = (Integer) args1[0];
                    System.out.println("Select param : " + arg);

                    if (1 == arg) {
                        return new User("Proxy User", 1);
                    }

                    return null;
                });
    }


    public static void main(String[] args) {
        UserMapper userMapper = getProxyUserMapper();
        User user = userMapper.selectUserById(1);
        System.out.println(user);
    }

}
