package cn.uicab.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String mail;
    private String mobile;
    private boolean isAdmin;
    private Integer status;
    private Integer deptId;
}
