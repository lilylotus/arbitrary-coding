package cn.nihility.registrar;

import org.springframework.beans.factory.FactoryBean;

//@Component
public class MyFactoryBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        return MapperProxy.getProxyUserMapper();
    }

    @Override
    public Class<?> getObjectType() {
        return UserMapper.class;
    }
}
