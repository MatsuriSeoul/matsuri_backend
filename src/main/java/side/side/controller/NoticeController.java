package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.Notice;
import side.side.service.NoticeService;

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

    @PostMapping
    public Notice createNotice(@RequestBody Notice notice, HttpServletRequest request) {
        notice.setCreatedTime(LocalDateTime.now());
        notice.setUpdatedTime(LocalDateTime.now());
        return noticeService.createNotice(notice);
    }

    @GetMapping
    public List<Notice> getAllNotice() {
        return noticeService.getAllNotices();
    }

    @GetMapping("/{id}")
    public Notice getNoticeById(@PathVariable Long id) {
        Optional<Notice> notice = noticeService.getNoticeById(id);
        if (notice.isPresent()) {
            noticeService.increaseViewCnt(notice.get());
            return notice.get();
        } else {
            throw new RuntimeException("공지사항을 찾을 수 없습니다.");
        }
    }

}

