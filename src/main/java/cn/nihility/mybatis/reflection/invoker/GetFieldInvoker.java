package cn.nihility.mybatis.reflection.invoker;

import cn.nihility.mybatis.reflection.Reflector;

import java.lang.reflect.Field;

public class GetFieldInvoker implements Invoker {

    private final Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            if (Reflector.canControlMemberAccessible()) {
                field.setAccessible(true);
                return field.get(target);
            } else {
                throw ex;
            }
        }
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

}
