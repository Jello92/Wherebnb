package com.example.wherebnb.controller;

import com.example.wherebnb.dto.ResponseDto;
import com.example.wherebnb.dto.RoomsRequestDto;
import com.example.wherebnb.security.UserDetailsImpl;
import com.example.wherebnb.service.RoomsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/room")
@RestController
@RequiredArgsConstructor
public class RoomsController {

    private final RoomsService roomsService;

    @PostMapping
    public ResponseDto roomInsert(@RequestParam(value = "images", required = false) List<MultipartFile> images, RoomsRequestDto roomRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception{
        return roomsService.roomInsert(roomRequestDto, userDetails.getUser(), images);
    }

    @PutMapping("/{id}")
    public ResponseDto roomUpdate(@PathVariable("id") Long id , @RequestParam(value = "images", required = false) List<MultipartFile> images, RoomsRequestDto roomRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return roomsService.roomUpdate(id, roomRequestDto, userDetails.getUser(), images);
    }

    @DeleteMapping("/{id}")
    public ResponseDto roomDelete(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return roomsService.roomDelete(id, userDetails.getUser());
    }

    @GetMapping("/like")
    public ResponseDto getAllLikeRooms(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return roomsService.getAllLikeRooms(userDetails.getUser());
    }

    @PutMapping("/like/{id}")
    public ResponseDto roomLikes(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return roomsService.roomLikes(id, userDetails.getUser());
    }
}
