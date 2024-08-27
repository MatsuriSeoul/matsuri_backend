package side.side.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import side.side.model.*;
import side.side.repository.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    @Autowired
    private NoticeFileRepository noticeFileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserNoticeViewRepository userNoticeViewRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public Notice createNotice(Notice notice, List<MultipartFile> images, List<MultipartFile> files, Long userId) throws IOException {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notice.setUser(user);
        notice.setCreatedTime(LocalDateTime.now());
        notice.setUpdatedTime(LocalDateTime.now());
        Notice savedNotice = noticeRepository.save(notice);


        // 이미지 저장
        List<NoticeImage> noticeImages = saveImages(savedNotice, images);
        noticeImageRepository.saveAll(noticeImages);

        // 첨부파일 저장
        List<NoticeFile> noticeFiles = saveFiles(savedNotice, files);
        noticeFileRepository.saveAll(noticeFiles);

        return savedNotice;
    }

    // 공지사항 수정 메서드
    public Notice updateNotice(Long id, Notice updatedNotice, List<MultipartFile> newImages, List<MultipartFile> newFiles, List<Long> existingImageIds, List<Long> existingFileIds) throws IOException {
        Notice existingNotice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다"));

        // 제목과 내용 업데이트
        existingNotice.setTitle(updatedNotice.getTitle());
        existingNotice.setContent(updatedNotice.getContent());
        existingNotice.setUpdatedTime(LocalDateTime.now());

        // 기존 데이터 유지 로직
        if (existingImageIds != null) {
            deleteNonExistingImages(existingNotice, existingImageIds);
        }

        if (existingFileIds != null) {
            deleteNonExistingFiles(existingNotice, existingFileIds);
        }

        // 새로운 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            List<NoticeImage> noticeImages = saveImages(existingNotice, newImages);
            existingNotice.getImages().addAll(noticeImages);
        }

        // 새로운 파일 추가 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            List<NoticeFile> noticeFiles = saveFiles(existingNotice, newFiles);
            existingNotice.getFiles().addAll(noticeFiles);
        }


        // 3. 저장 후 반환
        return noticeRepository.save(existingNotice);
    }


    //  모든 공지사항 조회 메소드
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    //  선택한 공지사항 조회 메소드 ( 디테일한 세부 내용 및 조회수 증가 )
    public Optional<Notice> getNoticeByIdAndIncreaseViewCount(Long noticeId, Long userId) {
        Optional<Notice> noticeOptional = noticeRepository.findById(noticeId);

        if (noticeOptional.isPresent()) {
            Notice notice = noticeOptional.get();
            UserInfo noticeUser = notice.getUser();

            // 작성자 정보 로그 출력
            if (noticeUser != null) {
                logger.info("Notice User ID: " + noticeUser.getId());
            } else {
                logger.warn("Notice User is null");
            }


            increaseViewCntIfEligible(notice, userId);
            return Optional.of(notice);
        }

        return Optional.empty();
    }

    //  댓글 작성관련 공지사항 조회 메소드
    public Optional<Notice> getNoticeById(Long noticeId) {
        // 공지사항을 ID로 조회
        return noticeRepository.findById(noticeId);
    }


    //  공지사항 삭제 메소드
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("Notice not found"));
        List<NoticeImage> images = notice.getImages();

        // 해당 공지사항과 연관된 UserNoticeView 레코드를 먼저 삭제
        userNoticeViewRepository.deleteByNotice(notice);

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

    //  공지사항 내의 이미지 서버에서 삭제하는 메소드
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

    // 파일 단일 삭제
    @Transactional
    public void deleteFileById(Long fileId) throws IOException {
        NoticeFile file = noticeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다"));

        // 파일 시스템에서 삭제
        Path filePath = Paths.get("src/main/resources/static" + file.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // 데이터베이스에서 삭제
        noticeFileRepository.delete(file);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // 고유한 파일명 생성 (UUID 사용)
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        // 폴더가 존재하지 않을 경우 폴더 생성
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        // 파일 저장
        Files.write(filePath, file.getBytes());
        return "/uploads/" + fileName;
    }


    // ==== PRIVATE METHODS ==== //

    // 새 이미지 저장
    private List<NoticeImage> saveImages(Notice notice, List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) return List.of();

        return images.stream()
                .map(image -> {
                    try {
                        return saveImage(notice, image);
                    } catch (IOException e) {
                        logger.error("Failed to save image", e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    // 단일 이미지 저장
    private NoticeImage saveImage(Notice notice, MultipartFile image) throws IOException {
        String imagePath = noticeImageService.uploadImage(image);
        NoticeImage noticeImage = new NoticeImage();
        noticeImage.setImgName(image.getOriginalFilename());
        noticeImage.setImagePath(imagePath);
        noticeImage.setNotice(notice);
        return noticeImage;
    }

    // 새 첨부파일 저장
    private List<NoticeFile> saveFiles(Notice notice, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return List.of();

        return files.stream()
                .map(file -> {
                    try {
                        return saveFile(notice, file);
                    } catch (IOException e) {
                        logger.error("Failed to save file", e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    // 단일 첨부파일 저장
    private NoticeFile saveFile(Notice notice, MultipartFile file) throws IOException {
        String filePath = saveFileToSystem(file);
        NoticeFile noticeFile = new NoticeFile();
        noticeFile.setFileName(file.getOriginalFilename());
        noticeFile.setFilePath(filePath);
        noticeFile.setNotice(notice);
        return noticeFile;
    }

    // 파일 시스템에 파일 저장
    private String saveFileToSystem(MultipartFile file) throws IOException {

        //  고유 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        //  폴더가 존재하지 않으면 폴더 생성
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.write(filePath, file.getBytes());
        return fileName;  // 인코딩된 파일 이름 반환
    }

    // 파일 다운로드
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다.");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("파일을 찾을 수 없습니다.", ex);
        }
    }

    // 파일 삭제
    private void deleteFile(String relativePath) throws IOException {
        Path filePath = Paths.get(uploadDir + relativePath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("File deleted successfully: " + filePath.toString());
        } else {
            logger.warn("File not found: " + filePath.toString());
        }
    }

    // 이미지 전체 삭제
    private void deleteAllImages(List<NoticeImage> images) {
        for (NoticeImage image : images) {
            try {
                deleteFile(image.getImagePath());
            } catch (IOException e) {
                logger.error("Failed to delete image file: " + image.getImagePath(), e);
            }
        }
        noticeImageRepository.deleteAll(images);
    }

    // 파일 전체 삭제
    private void deleteAllFiles(List<NoticeFile> files) {
        for (NoticeFile file : files) {
            try {
                deleteFile(file.getFilePath());
            } catch (IOException e) {
                logger.error("Failed to delete file: " + file.getFilePath(), e);
            }
        }
        noticeFileRepository.deleteAll(files);
    }

    // 기존에 남겨둔 이미지 외에 나머지 이미지 삭제
    private void deleteNonExistingImages(Notice notice, List<Long> existingImageIds) {
        List<NoticeImage> imagesToDelete = notice.getImages().stream()
                .filter(image -> !existingImageIds.contains(image.getId()))
                .collect(Collectors.toList());

        deleteAllImages(imagesToDelete);
        notice.getImages().removeAll(imagesToDelete);
    }

    // 기존에 남겨둔 파일 외에 나머지 파일 삭제
    private void deleteNonExistingFiles(Notice notice, List<Long> existingFileIds) {
        List<NoticeFile> filesToDelete = notice.getFiles().stream()
                .filter(file -> !existingFileIds.contains(file.getId()))
                .collect(Collectors.toList());

        deleteAllFiles(filesToDelete);
        notice.getFiles().removeAll(filesToDelete);
    }

    //  조회수 증가를 위한 검증 메소드
    private void increaseViewCntIfEligible(Notice notice, Long userId) {
        // 유저가 이미 해당 공지사항을 오늘 조회한 기록이 있는지 확인
        boolean canIncreaseView = checkIfCanIncreaseViewCount(userId, notice.getId());

        if (canIncreaseView) {
            // 조회수를 증가시키고 저장
            notice.setViewcnt(notice.getViewcnt() + 1);
            noticeRepository.save(notice);

            // 유저의 조회 기록을 저장
            saveUserViewRecord(userId, notice);
        }
    }

    private boolean checkIfCanIncreaseViewCount(Long userId, Long noticeId) {
        // 오늘 날짜로 해당 유저가 해당 공지사항을 이미 조회했는지 확인
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다"));

        // LocalDate 사용하여 오늘 날짜 기준으로 조회 여부 확인
        return !userNoticeViewRepository.existsByUserAndNoticeAndViewDate(user, notice, LocalDate.now());
    }

    private void saveUserViewRecord(Long userId, Notice notice) {
        // 유저와 공지사항 정보로 새로운 조회 기록을 생성하고 저장
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        UserNoticeView record = new UserNoticeView(user, notice, LocalDate.now());
        userNoticeViewRepository.save(record);
    }

}


