package com.example.wherebnb.service;

import com.example.wherebnb.dto.ResponseDto;
import com.example.wherebnb.jwt.JwtUtil;
import com.example.wherebnb.dto.UserInfoDto;
import com.example.wherebnb.entity.Users;
import com.example.wherebnb.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${KAKAO_API_KEY}")
    private String kakaoApiKey;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ResponseDto<String> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = getToken(code);

        UserInfoDto userInfoDto = getUserInfo(accessToken);

        if(!userRepository.existsByKakaoId(userInfoDto.getKakaoId().toString())) {
            userRepository.save(new Users(userInfoDto));
        }
        String token = jwtUtil.createToken(userInfoDto.getKakaoId().toString());

        response.addHeader("Authorization", token);

        Cookie cookie = new Cookie("Authorization", token.substring(7));
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseDto.setSuccess("로그인 성공", userInfoDto.getUsername());
    }

    private String getToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoApiKey);
        body.add("redirect_uri", "http://localhost:3000/login");
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST, kakaoTokenRequest, String.class);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 토큰에서 사용자 정보 get
    private UserInfoDto getUserInfo(String token) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> UserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST, UserInfoRequest, String.class);

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String username = jsonNode.get("properties").get("nickname").asText();
        Long kakaoId = jsonNode.get("id").asLong();

        return new UserInfoDto(username, kakaoId);
    }
}
