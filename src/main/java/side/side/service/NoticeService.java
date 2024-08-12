package side.side.service;

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

    public Notice updateNotice(Long id, Notice updatedNotice, List<MultipartFile> newImages, List<Long> deletedImageIds) throws IOException {
        Notice existingNotice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다"));

        existingNotice.setTitle(updatedNotice.getTitle());
        existingNotice.setContent(updatedNotice.getContent());
        existingNotice.setUpdatedTime(LocalDateTime.now());

        // 삭제할 이미지 처리
        if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
            for (Long imageId : deletedImageIds) {
                NoticeImage image = noticeImageRepository.findById(imageId)
                        .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다"));
                // 데이터베이스에서 이미지 삭제
                noticeImageRepository.delete(image);

                // 파일 시스템에서 이미지 삭제
                Path filePath = Paths.get("src/main/resources/static/images/" + image.getImagePath());
                Files.deleteIfExists(filePath);
            }
        }

        // 새로운 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            List<NoticeImage> noticeImages = newImages.stream()
                    .map(image -> saveImage(existingNotice, image))
                    .collect(Collectors.toList());

            existingNotice.getImages().addAll(noticeImages); // 기존 이미지에 새 이미지 추가
        }

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

    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("Notice not found"));
        List<NoticeImage> images = notice.getImages();
        for (NoticeImage image : images) {
            Path path = Paths.get(image.getImagePath().substring(1));
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image file", e);
            }
        }
        noticeImageRepository.flush();
        noticeRepository.delete(notice);
    }

    private NoticeImage saveImage(Notice notice, MultipartFile image) {

            String imagePath = noticeImageService.uploadImage(image);

            NoticeImage noticeImage = new NoticeImage();
            noticeImage.setImgName(image.getOriginalFilename());
            noticeImage.setImagePath(imagePath);
            noticeImage.setNotice(notice);
            return noticeImage;

        }


    private void deleteExistingImages(Notice notice) {
        List<NoticeImage> existingImages = notice.getImages();
        for (NoticeImage image : existingImages) {
            Path path = Paths.get(image.getImagePath().substring(1));
            try {
                Files.deleteIfExists(path);
                noticeImageRepository.delete(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image file", e);
            }
        }
        noticeImageRepository.flush();
    }

}

