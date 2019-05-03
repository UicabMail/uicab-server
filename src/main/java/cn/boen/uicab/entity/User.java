package cn.boen.uicab.entity;


import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String mail;
    private String mobile;
    private Boolean isAdmin;
    private Integer dept;
    private Integer status;
}
