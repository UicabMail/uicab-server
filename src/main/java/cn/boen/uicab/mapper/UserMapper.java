package cn.boen.uicab.mapper;

import cn.boen.uicab.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("SELECT  id, username, mail FROM user")
    List<User> getAll();

    @Select("SELECT  id, username, mail FROM user WHERE dept = #{dept}")
    List<User> getDept( @Param(value="dept")Integer dept);

    @Select("SELECT * FROM user WHERE username = #{username} and password = #{password}")
    @Results({
            @Result(property = "isAdmin", column = "is_admin")
    })
    User getOne(User user);

    @Select("SELECT id, username, status, mail, mobile, dept, is_admin FROM user WHERE username LIKE '%${keyword}%' limit 5")
    @Results({
            @Result(property = "isAdmin", column = "is_admin")
    })
    List<User> searchUser(@Param(value="keyword") String keyword);

    @Insert("INSERT INTO user(username, password, mail, is_admin, status, dept) VALUES(#{username}, #{password}, #{mail}, #{isAdmin}, #{status}, #{dept})")
    boolean insert(User user);

    @Update({"<script>",
            "UPDATE user",
            "<set>",
            "<if test='username != null'>",
            "username = #{username} ,",
            "</if>",
            "<if test='mobile != null'>",
            "mobile = #{mobile} ,",
            "</if>",
            "<if test='status != null'>",
            "status = #{status} ,",
            "</if>",
            "<if test='dept != null'>",
            "dept = #{dept} ,",
            "</if>",
            "<if test='isAdmin != null'>",
            "is_admin = #{isAdmin} ,",
            "</if>",
            "<if test='password != null'>",
            "password = #{password} ,",
            "</if>",
            "</set>",
            "where id = #{id}",
            "</script>"})
    boolean update(User user);

    @Delete("UPDATE user set password = #{newPass} WHERE username = #{username} and password = #{password}")
    boolean updatePass( @Param(value="username")String username, @Param(value="password")String password, @Param(value="newPass") String newPass);

    @Delete("DELETE FROM user WHERE id =#{id}")
    boolean delete(User user);

}