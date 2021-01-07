package cn.nihility.registrar2.mapper;

import cn.nihility.registrar2.annotation.Select;
import cn.nihility.registrar2.entity.SelectEntity;

public interface SelectMapper {
    @Select(sql = "SELECT id, name, age FROM user WHERE ID = ?")
    SelectEntity selectById(Integer id);
}
