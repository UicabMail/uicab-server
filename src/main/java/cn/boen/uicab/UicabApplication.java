package cn.boen.uicab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.boen.uicab.mapper")
public class UicabApplication {

    public static void main(String[] args) {
        SpringApplication.run(UicabApplication.class, args);
    }

}
