package side.side.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import side.side.model.Notice;
import side.side.model.NoticeImage;
import side.side.repository.NoticeImageRepository;
import side.side.repository.NoticeRepository;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);

    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private NoticeImageRepository noticeImageRepository;
    @Autowired
    private NoticeImageService noticeImageService;



    public Notice createNotice(Notice notice, List<MultipartFile> images) throws IOException {
        notice.setCreatedTime(LocalDateTime.now());
        notice.setUpdatedTime(LocalDateTime.now());
        Notice savedNotice = noticeRepository.save(notice);

        //  공지사항에 들어갈 이미지 저장
        List<NoticeImage> noticeImages = images.stream()
                .map(image -> saveImage(savedNotice, image))
                .collect(Collectors.toList());

        noticeImageRepository.saveAll(noticeImages);
        return savedNotice;
    }

    // 공지사항 수정 메서드
    public Notice updateNotice(Long id, Notice updatedNotice, List<MultipartFile> newImages, List<Long> existingImageIds) throws IOException {
        Notice existingNotice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다"));

        // 제목과 내용 업데이트
        existingNotice.setTitle(updatedNotice.getTitle());
        existingNotice.setContent(updatedNotice.getContent());
        existingNotice.setUpdatedTime(LocalDateTime.now());

        // 기존 이미지를 제외한 이미지를 모두 삭제
        existingNotice.getImages().removeIf(image -> !existingImageIds.contains(image.getId()));


        // 2. 새로운 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            List<NoticeImage> noticeImages = newImages.stream()
                    .map(image -> saveImage(existingNotice, image))
                    .collect(Collectors.toList());

            // 기존 이미지에 새 이미지 추가
            existingNotice.getImages().addAll(noticeImages);
        }

        // 3. 저장 후 반환
        return noticeRepository.save(existingNotice);
    }



    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    public void increaseViewCnt(Notice notice) {
        notice.setViewcnt(notice.getViewcnt() + 1);
        noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("Notice not found"));
        List<NoticeImage> images = notice.getImages();

        // 1. 파일 시스템에서 이미지 파일 삭제
        for (NoticeImage image : images) {
            String imagePath = "src/main/resources/static" + image.getImagePath();  // 경로 확인
            Path filePath = Paths.get(imagePath);
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);  // 파일 삭제
                    System.out.println("File deleted successfully: " + filePath.toString());
                } else {
                    System.out.println("File not found: " + filePath.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image file at path: " + filePath.toString(), e);
            }
        }

        // 2. 데이터베이스에서 이미지 엔티티 삭제
        noticeImageRepository.deleteAll(images);  // 이미지 엔티티 삭제

        // 3. 공지사항 엔티티 삭제
        noticeRepository.delete(notice);  // 공지사항 엔티티 삭제
    }

    private NoticeImage saveImage(Notice notice, MultipartFile image) {

            String imagePath = noticeImageService.uploadImage(image);

            NoticeImage noticeImage = new NoticeImage();
            noticeImage.setImgName(image.getOriginalFilename());
            noticeImage.setImagePath(imagePath);
            noticeImage.setNotice(notice);

            return noticeImage;

        }


    @Transactional
    public void deleteImageById(Long imageId) throws IOException {
        NoticeImage image = noticeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다"));

        // 파일 시스템에서 이미지 삭제
        String imagePath = "src/main/resources/static" + image.getImagePath();
        Path filePath = Paths.get(imagePath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // 데이터베이스에서 이미지 삭제
        noticeImageRepository.delete(image);
    }

}

