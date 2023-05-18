package com.example.wherebnb.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.wherebnb.dto.ResponseDto;
import com.example.wherebnb.dto.host.HostResponseDto;
import com.example.wherebnb.dto.RoomsRequestDto;
import com.example.wherebnb.entity.ImageFile;
import com.example.wherebnb.entity.Likes;
import com.example.wherebnb.entity.Rooms;
import com.example.wherebnb.entity.Users;
import com.example.wherebnb.exception.ErrorException;
import com.example.wherebnb.exception.ExceptionEnum;
import com.example.wherebnb.repository.FilesRepository;
import com.example.wherebnb.repository.LikesRepository;
import com.example.wherebnb.repository.RoomsRepository;
import com.example.wherebnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomsService {

    private final RoomsRepository roomsRepository;
    private final LikesRepository likesRepository;
    private final NotificationService notificationService;
    private final FilesRepository filesRepository;
    private final UserRepository userRepository;
//    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String S3_BUCKET_PREFIX = "S3";


    @Value(("${cloud.aws.s3.bucket}"))
    private String bucketName;
    private final AmazonS3 amazonS3;

    // 숙소 등록
    public ResponseDto<?> roomInsert(RoomsRequestDto roomsRequestDto, Users user, List<MultipartFile> images) throws IOException {
        Rooms rooms = Rooms.builder().roomsRequestDto(roomsRequestDto).user(user).build();
        rooms.setImageFile(fileFactory(images, rooms));
        roomsRepository.save(rooms);
        return ResponseDto.setSuccess("숙소 등록을 완료하였습니다.", null);
    }

    // 숙소 수정
    public ResponseDto<?> roomUpdate(Long id, RoomsRequestDto roomsRequestDto, Users user, List<MultipartFile> images) throws IOException {
        Rooms room = roomsRepository.findById(id).orElseThrow(  // 수정할 게시글 있는지 확인
                () -> new ErrorException(ExceptionEnum.ROOM_NOT_FOUND));

        if (!room.getUser().getId().equals(user.getId())) // 권한 체크
            throw  new ErrorException(ExceptionEnum.NOT_ALLOWED_AUTHORIZATIONS);

        filesRepository.deleteByRoomId(id); // 해당되는 전체 이미지 삭제
        room.setImageFile(fileFactory(images, room));
        room.roomUpdate(roomsRequestDto); // 기본정보 업데이트
        return ResponseDto.setSuccess("숙소 수정을 완료하였습니다.", null);
    }

    // 숙소 삭제
    public ResponseDto<?> roomDelete(Long id, Users user) {
        Rooms room = roomsRepository.findById(id).orElseThrow( // 삭제할 게시글 있는지 확인
                () -> new ErrorException(ExceptionEnum.ROOM_NOT_FOUND));

        if (!room.getUser().getId().equals(user.getId()))
            throw new ErrorException(ExceptionEnum.NOT_ALLOWED_AUTHORIZATIONS);

        filesRepository.deleteByRoomId(id);
        roomsRepository.delete(room);
        return ResponseDto.setSuccess("숙소 삭제를 완료하였습니다.", null);
    }

    // 좋아요한 숙소 조회
    @Transactional(readOnly = true)
    public ResponseDto<List<HostResponseDto>> getAllLikeRooms(Users user) {
        List<HostResponseDto> likeRoomsList = likesRepository.findAllByUserId(user.getId()).stream()
                .map(x->new HostResponseDto(x.getRooms())).collect(Collectors.toList());
        return ResponseDto.setSuccess("좋아요한 숙소 조회를 완료하였습니다.", likeRoomsList);
    }
    // 좋아요 등록
//    public ResponseDto<Boolean> roomLikes(Long id, Users from) {
//        executorService.submit(()-> {
//            try {
//                processRoomLikes(id, from);
//            } catch (ApiException e){
//                log.error("좋아요 누를때 에러가 발생하였습니다", e.getMessage());
//            }
//        });
//        return ResponseDto.setSuccess("좋아요를 눌렀습니다.", true);
//    }
//    private void processRoomLikes(Long id, Users from) {
//        Rooms room = roomsRepository.findById(id).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_ROOM));
//        boolean likeStatus = true;
//
//        if (likesRepository.existsByUserIdAndRoomsId(from.getId(), room.getId())) { // 이미 좋아요한 경우 취소
//            Likes likes = likesRepository.findByUserIdAndRoomsId(from.getId(), room.getId());
//            likesRepository.delete(likes);
//            likeStatus = false;
//        } else { // 좋아요
//            Likes likes = new Likes(room, from);
//            likesRepository.save(likes);
//            notificationService.notifyLikeEvent(likes, room.getUser()); // 좋아요 알림 보내기
//        }
//        room.updateLikes(likeStatus);
//    }

    public ResponseDto<Boolean> roomLikes(Long id, Users from) {
        Rooms room = roomsRepository.findById(id).orElseThrow(() -> new ErrorException(ExceptionEnum.ROOM_NOT_FOUND));
        boolean likeStatus = true;

        if (likesRepository.existsByUserIdAndRoomsId(from.getId(), room.getId())) { // 이미 좋아요한 경우 취소
            Likes likes = likesRepository.findByUserIdAndRoomsId(from.getId(), room.getId());
            likesRepository.delete(likes);
            likeStatus = false;
        } else { // 좋아요
            Likes likes = new Likes(room, from);
            likesRepository.save(likes);
            notificationService.notifyLikeEvent(likes, room.getUser()); // 좋아요 알림 보내기
        }
        room.updateLikes(likeStatus);
        return ResponseDto.setSuccess("좋아요를 눌렀습니다.", likeStatus);
    }

    // 파일 등록 팩토리
    public List<ImageFile> fileFactory(List<MultipartFile> images, Rooms rooms) throws IOException {
        List<ImageFile> imageFileList = new ArrayList<>();

        for (MultipartFile image : images) {
            // 파일명 새로 부여를 위한 현재 시간 알아내기
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            int minute = now.getMinute();
            int second = now.getSecond();
            int millis = now.get(ChronoField.MILLI_OF_SECOND);

            String imageUrl = null;
            String newFileName = "image" + hour + minute + second + millis;
            String fileExtension = '.' + image.getOriginalFilename().replaceAll("^.*\\.(.*)$", "$1");
            String imageName = S3_BUCKET_PREFIX + newFileName + fileExtension;

            // 메타데이터 설정
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(image.getContentType());
            objectMetadata.setContentLength(image.getSize());

            InputStream inputStream = image.getInputStream();

            amazonS3.putObject(new PutObjectRequest(bucketName, imageName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imageUrl = amazonS3.getUrl(bucketName, imageName).toString();

            imageFileList.add(new ImageFile(imageUrl, image.getOriginalFilename(), imageName, rooms));
        }
        return imageFileList;
    }
}
