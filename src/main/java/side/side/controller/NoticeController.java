package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.side.config.JwtUtils;
import side.side.model.Notice;
import side.side.model.NoticeImage;
import side.side.model.Response;
import side.side.service.NoticeService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private static final Logger Log = Logger.getLogger(NoticeController.class.getName());

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("")
    public ResponseEntity<?> createNotice(@RequestParam("title") String title,
                                          @RequestParam("content") String content,
                                       @RequestParam(value= "images", required = false) List<MultipartFile> images,
                                       HttpServletRequest request) throws IOException {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);

        Notice savedNotice = noticeService.createNotice(notice, images);
        return ResponseEntity.ok(savedNotice);
    }

    @PutMapping(value = "/edit/{id}")
    public ResponseEntity<?> updateNotice(@PathVariable Long id,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content,
                               @RequestPart(value= "images", required = false) List<MultipartFile> images,
                               @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds) throws IOException {

        Notice updatedNotice = new Notice();
        updatedNotice.setTitle(title);
        updatedNotice.setContent(content);

        Notice savedNotice = noticeService.updateNotice(id, updatedNotice, images, deletedImageIds);
        return ResponseEntity.ok(savedNotice);
    }

    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }

    @GetMapping
    public List<Notice> getAllNotice() {
        return noticeService.getAllNotices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNoticeById(@PathVariable Long id) {
        Optional<Notice> notice = noticeService.getNoticeById(id);
        if (notice.isPresent()) {
            return ResponseEntity.ok(notice.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

