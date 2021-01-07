package cn.nihility.registrar.mapper;

import cn.nihility.registrar.Select;
import cn.nihility.registrar.User;

public interface UserMapper {

    @Select("Select name, id from user where id = #{param1}")
    User selectUserById(Integer id);

}
