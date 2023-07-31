package com.example.dividend.config;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class Appconfig {

    @Bean
    public Trie<String, String> trie() {
        return new PatriciaTrie<>();
    }

    // 사용자 정보처럼 민감한 정보들 그대로 노출되지 않도록 인코딩하기 위한 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
