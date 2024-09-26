package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.side.config.JwtUtils;
import side.side.model.Notice;
import side.side.model.NoticeImage;
import side.side.service.NoticeService;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class.getName());

    private static final Map<String, String> EXTENSION_TO_MIME_TYPE = new HashMap<>();

    static {
        EXTENSION_TO_MIME_TYPE.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("png", "image/png");
        EXTENSION_TO_MIME_TYPE.put("gif", "image/gif");
        EXTENSION_TO_MIME_TYPE.put("bmp", "image/bmp");
        EXTENSION_TO_MIME_TYPE.put("pdf", "application/pdf");
        EXTENSION_TO_MIME_TYPE.put("txt", "text/plain");
        EXTENSION_TO_MIME_TYPE.put("doc", "application/msword");
        EXTENSION_TO_MIME_TYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        // 추가적으로 다른 확장자에 대해 필요한 MIME 타입을 여기에 추가할 수 있습니다.
    }

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("")
    public ResponseEntity<?> createNotice(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token
    ) {
        try {
            Long userId = jwtUtils.extractUserIdFromToken(token);

            Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);

            Notice savedNotice = noticeService.createNotice(notice, images, files, userId);
            return ResponseEntity.ok(savedNotice);
        } catch (IOException e) {
            logger.error("Failed to create notice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지사항 생성 실패");
        }
    }

    // 공지사항 수정
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
            @RequestParam(value = "deletedFileIds", required = false) List<Long> deletedFileIds
    ) {
        try {
            Notice updatedNotice = new Notice();
            updatedNotice.setTitle(title);
            updatedNotice.setContent(content);

            Notice savedNotice = noticeService.updateNotice(id, updatedNotice, newImages, newFiles, deletedImageIds, deletedFileIds);
            return ResponseEntity.ok(savedNotice);
        } catch (IOException e) {
            logger.error("Failed to update notice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지사항 수정 실패");
        }
    }

    //  공지사항 삭제
    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }

    @GetMapping
    public List<Notice> getAllNotice() {
        return noticeService.getAllNotices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNoticeById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 ID 추출 (jwtUtils 사용)
            Long userId = jwtUtils.extractUserIdFromToken(token);

            // 사용자 ID를 기반으로 공지사항 조회 및 조회수 증가
            Optional<Notice> notice = noticeService.getNoticeByIdAndIncreaseViewCount(id, userId);

            if (notice.isPresent()) {
                return ResponseEntity.ok(notice.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // 예외 발생 시 500 에러 반환
            logger.error("Error retrieving notice: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving notice");
        }
    }



    @DeleteMapping("/image/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            noticeService.deleteImageById(imageId);
            return ResponseEntity.ok().body("이미지가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 삭제 실패");
        }
    }

    // 첨부파일 업로드 로직
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = noticeService.uploadFile(file);  // 파일 경로 설정
            return ResponseEntity.ok().body("파일이 성공적으로 업로드되었습니다: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }

    //  첨부파일 다운로드 로직
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // 파일을 서비스에서 로드
            Resource resource = noticeService.loadFileAsResource(fileName);

            // 파일 이름을 인코딩하여 다운로드
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String encodedFileName = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (IOException e) {
            logger.error("파일 다운로드 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return ""; // 확장자가 없을 경우 빈 문자열 반환
        }
    }

        // 첨부파일 삭제 로직 추가
        @DeleteMapping("/file/{fileId}")
        public ResponseEntity<?> deleteFile (@PathVariable Long fileId){
            try {
                noticeService.deleteFileById(fileId);
                return ResponseEntity.ok().body("첨부파일이 삭제되었습니다.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("첨부파일 삭제 실패");
            }
        }


    }


