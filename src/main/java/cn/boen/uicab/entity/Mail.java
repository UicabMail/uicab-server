package cn.boen.uicab.entity;

import lombok.Data;

@Data
public class Mail {
    private String from;
    private String fromName;
    private String to;
    private String subject;
    private String content;
    private String contentType;
    private String time;
    private boolean seen;
}
