package cn.boen.uicab.service;

import cn.boen.uicab.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class MIABService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    public boolean addUser (User user){
        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(this.username, this.password).build();

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
//        request.add("email", user.getMail());
//        request.add("password", user.getPassword());

        try {
            restTemplate.postForObject("https://"+this.host+"/admin/mail/users/add", request, String.class);
        }catch (RestClientResponseException exception){
            return  false;
        }
        return  true;
    }

    public boolean removeUser (User user){
        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(this.username, this.password).build();

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
//        request.add("email", user.getMail());

        try {
            restTemplate.postForObject("https://"+this.host+"/admin/mail/users/remove", request, String.class);
        }catch (RestClientResponseException exception){
            return  false;
        }
        return  true;
    }

    public boolean addAdmin (User user){
        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(this.username, this.password).build();

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
//        request.add("email", user.getMail());
        request.add("privilege ", "admin");

        try {
            restTemplate.postForObject("https://"+this.host+"/admin/mail/users/privileges/add", request, String.class);
        }catch (RestClientResponseException exception){
            return  false;
        }
        return  true;
    }

    public boolean removeAdmin (User user){
        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(this.username, this.password).build();

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
//        request.add("email", user.getMail());

        try {
            restTemplate.postForObject("https://"+this.host+"/admin/mail/users/privileges/remove", request, String.class);
        }catch (RestClientResponseException exception){
            return  false;
        }
        return  true;
    }
}
