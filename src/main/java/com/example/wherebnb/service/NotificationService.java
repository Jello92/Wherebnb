package com.example.wherebnb.service;

import com.example.wherebnb.entity.Likes;
import com.example.wherebnb.entity.Users;
import com.example.wherebnb.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.example.wherebnb.controller.SseController.sseEmitters;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    @Scheduled(fixedRate = 60000)
    public void notifyLikeEvent(Likes likes, Users to) {
        Long kakaoId = Long.valueOf(to.getKakaoId());

        if(sseEmitters.containsKey(kakaoId)){
            SseEmitter sseEmitter = sseEmitters.get(kakaoId);
            try{
                sseEmitter.send(SseEmitter.event().name("notifyLike")
                                .data("{ \"userName\": \"" + likes.getUser().getUsername() + "\", "
                                + "\"roomName\": \"" + likes.getRooms().getRoomName() + "\", "
                                + "\"createdAt\": \"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM월 dd일 a HH시 mm분")) + "\" }"));
            } catch (Exception e) {
                sseEmitters.remove(kakaoId);
            }
        }
    }
}
