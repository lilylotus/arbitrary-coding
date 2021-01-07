package cn.nihility.registrar;

public interface UserMapper {

    @Select("Select name, id from user where id = #{param1}")
    User selectUserById(Integer id);

}
