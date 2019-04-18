package cn.boen.uicab.mapper;

import cn.boen.uicab.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("SELECT * FROM users")
    @Results({
            @Result(property = "nickName", column = "nick_name")
    })
    List<User> getAll();

//    @Select("SELECT * FROM users WHERE id = #{id}")
//    @Results({
//            @Result(property = "nickName", column = "nick_name")
//    })
//    User getOne(Long id);

    @Select("SELECT * FROM user WHERE username = #{username} and password = #{password}")
    User getOne(User user);

    @Select("SELECT id,username,status FROM user WHERE username LIKE '%#{keyword}%'")
    List<User> searchUser(String keyword);

    @Insert("INSERT INTO user(username, password, mail) VALUES(#{username}, #{password}, #{mail})")
    boolean insert(User user);

    @Update("UPDATE users SET userName=#{userName},nick_name=#{nickName} WHERE id =#{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id =#{id}")
    void delete(Long id);

}