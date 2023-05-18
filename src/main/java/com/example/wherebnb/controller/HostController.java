package com.example.wherebnb.controller;

import com.example.wherebnb.dto.host.HostRequestDto;
import com.example.wherebnb.dto.ResponseDto;
import com.example.wherebnb.service.HostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("main")
@RestController
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;

    @GetMapping
    public ResponseDto getAllRooms(Pageable pageable){
        return hostService.getAllRooms(pageable);
    }

    @GetMapping("/{id}")
    public ResponseDto getRoomDetail(@PathVariable Long id){
        return hostService.getRoomDetail(id);
    }

    @GetMapping("/keyword")
    public ResponseDto chooseSearch(@RequestParam("keyword") String keyword, Pageable pageable){
        return hostService.chooseSearch(keyword, pageable);
    }

    @GetMapping("/condition")
    public ResponseDto getMainRoomByCondition(HostRequestDto hostreqeuestdto, Pageable pageable){
        return hostService.getRoomsByCondition(hostreqeuestdto, pageable);
    }
}
