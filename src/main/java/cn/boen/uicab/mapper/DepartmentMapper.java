package cn.boen.uicab.mapper;

import cn.boen.uicab.entity.Department;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DepartmentMapper {

    @Select("SELECT * FROM dept")
    List<Department> getAll();

    @Insert("INSERT INTO dept(name, placard, owner) VALUES(#{name}, #{placard}, #{owner})")
    boolean insert(Department department);

    @Update("UPDATE dept SET name=#{name},placard=#{placard} WHERE id =#{id}")
    boolean update(Department department);

    @Delete("DELETE FROM dept WHERE id =#{id}")
    boolean delete(Integer id);

}