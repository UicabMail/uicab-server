package cn.boen.uicab.entity;

import lombok.Data;

@Data
public class Department {
    private Integer id;
    private String name;
    private String placard;
    private Integer ownerId;
}
