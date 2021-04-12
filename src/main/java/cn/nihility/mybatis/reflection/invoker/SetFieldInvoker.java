package cn.nihility.mybatis.reflection.invoker;

import cn.nihility.mybatis.reflection.Reflector;

import java.lang.reflect.Field;

public class SetFieldInvoker implements Invoker {

    private final Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        try {
            field.set(target, args[0]);
        } catch (IllegalAccessException ex) {
            if (Reflector.canControlMemberAccessible()) {
                field.setAccessible(true);
                field.set(target, args[0]);
            } else {
                throw ex;
            }
        }
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

}
