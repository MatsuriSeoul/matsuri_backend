package side.side.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProfileImageService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileImageService.class);

    // 프로필 이미지 파일이 저장될 디렉토리 경로
    private final String uploadDir = System.getProperty("user.home") + "/Desktop/uploads/userProfileImg/";

    public String uploadProfileImage(MultipartFile image) {
        // 파일 이름 생성
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        // 파일 경로 생성
        Path filePath = Paths.get(uploadDir + fileName);

        try {
            // 디렉토리 생성
            Files.createDirectories(filePath.getParent());
            // 파일 저장
            Files.write(filePath, image.getBytes());
            logger.info("Profile Image saved at: " + filePath.toString()); // 저장된 경로 로그 출력
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 업로드 실패", e);
        }
        // 웹 접근 경로 반환
        return "/uploads/userProfileImg/" + fileName;
    }
}
