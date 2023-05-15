package com.example.wherebnb.service;

import com.example.wherebnb.dto.host.HostDetailResponseDto;
import com.example.wherebnb.dto.host.HostRequestDto;
import com.example.wherebnb.dto.host.HostResponseDto;
import com.example.wherebnb.dto.ResponseDto;
import com.example.wherebnb.entity.Rooms;
import com.example.wherebnb.entity.Users;
import com.example.wherebnb.exception.ApiException;
import com.example.wherebnb.exception.ExceptionEnum;
import com.example.wherebnb.repository.LikesRepository;
import com.example.wherebnb.repository.RoomsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostService {

    private final RoomsRepository roomsRepository;
    private final LikesRepository likesRepository;

    // 숙소 전체 검색
    public ResponseDto getAllRooms(Pageable pageable) {
        List<HostResponseDto> roomList = roomsRepository.findAll(pageable).getContent()
                .stream().map(HostResponseDto::new).collect(Collectors.toList());
        return ResponseDto.setSuccess("전체 조회 성공", roomList);
    }

    public ResponseDto getUsersAllRooms(Pageable pageable, Users user) {
        List<HostResponseDto> roomList = roomsRepository.findAll(pageable).getContent().stream()
                .map(x-> new HostResponseDto(x,likesRepository.existsByUserIdAndRoomsId(user.getId(), x.getId())))
                .collect(Collectors.toList());
        return ResponseDto.setSuccess("전체 조회 성공", roomList);
    }

    // 숙소 상세조회
    public ResponseDto getRoomDetail(Long id) {
        Rooms room = roomsRepository.findById(id).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_ROOM));
        HostDetailResponseDto hostDetailResponseDto = new HostDetailResponseDto().toHostResponseDto(room);
        return ResponseDto.setSuccess("상세 조회 성공", hostDetailResponseDto);
    }

    // 숙소 키워드 검색 (비회원)
    public ResponseDto chooseSearch(String keyword1, String keyword2, Pageable pageable){
        List<HostResponseDto> roomList =  roomsRepository.findAllByKeyword1OrKeyword2(keyword1, keyword2, pageable).stream().map(HostResponseDto::new).collect(Collectors.toList());
        return ResponseDto.setSuccess("키워드 검색 성공", roomList);
    }

    // 숙소 키워드 검색 (회원)
    public ResponseDto chooseUsersSearch(String keyword1, String keyword2, Users user, Pageable pageable) {
        List<HostResponseDto> roomList = roomsRepository.findAllByKeyword1OrKeyword2(keyword1, keyword2, pageable).stream()
                .map(x->new HostResponseDto(x, likesRepository.existsByUserIdAndRoomsId(user.getId(), x.getId())))
                .collect(Collectors.toList());
        return ResponseDto.setSuccess("키워드 검색 성공", roomList);
    }

    //숙소 조건검색 (비회원)
    public ResponseDto getRoomsByCondition(HostRequestDto hostreqeuestdto, Pageable pageable) {
        List<HostResponseDto> roomsListByCondition = ConditionCheck(hostreqeuestdto, pageable).stream().map(HostResponseDto::new).collect(Collectors.toList());
        return ResponseDto.setSuccess("조건 검색 성공",roomsListByCondition);
    }

    public ResponseDto getRoomsByUserAndCondition(HostRequestDto hostreqeuestdto, Users user, Pageable pageable) {
        List<HostResponseDto> roomsListByCondition =  ConditionCheck(hostreqeuestdto, pageable).stream()
                .map(x-> new HostResponseDto(x, likesRepository.existsByUserIdAndRoomsId(user.getId(), x.getId()))).collect(Collectors.toList());
        return ResponseDto.setSuccess("조건 검색 성공",roomsListByCondition);
    }

    public List<Rooms> ConditionCheck(HostRequestDto hostreqeuestdto, Pageable pageable){
        LocalDate startDateTime = hostreqeuestdto.getStartDate();
        LocalDate endDateTime = hostreqeuestdto.getEndDate();
        int guestNum = hostreqeuestdto.getAdults() + hostreqeuestdto.getChildren();
        List<Rooms> rooms;
        int period = 0;
        if (!"not_flexible".equals(hostreqeuestdto.getFlexibleTripLengths())) {
            if ("one_month".equals(hostreqeuestdto.getFlexibleTripLengths())) {
                period = 30;
            } else if ("one_week".equals(hostreqeuestdto.getFlexibleTripLengths())) {
                period = 7;
            } else {
                throw new ApiException(ExceptionEnum.NOT_FOUND_CONDITION);
            }
            rooms = roomsRepository.findAllByPeriodGreaterThanEqualAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndGuestNumGreaterThanEqualAndInfantAndPet(period, startDateTime, endDateTime, guestNum, hostreqeuestdto.isInfant(), hostreqeuestdto.isPet(), pageable);
        } else {
            rooms = roomsRepository.findAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndGuestNumGreaterThanEqualAndInfantAndPet(startDateTime, endDateTime, guestNum, hostreqeuestdto.isInfant(), hostreqeuestdto.isPet(), pageable);
        }
        return rooms;
    }
}