package com.willows.rta.service;

import com.willows.rta.model.Notice;
import com.willows.rta.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /**
     * Get all notices ordered by pinned status and date
     */
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllOrderedByPinnedAndDate();
    }

    /**
     * Get notice by ID
     */
    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    /**
     * Create new notice
     */
    @Transactional
    public Notice createNotice(String title, String content, String createdBy) {
        Notice notice = new Notice(title, content, createdBy);
        return noticeRepository.save(notice);
    }

    /**
     * Update existing notice
     */
    @Transactional
    public Notice updateNotice(Long id, String title, String content) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
        notice.setTitle(title);
        notice.setContent(content);
        return noticeRepository.save(notice);
    }

    /**
     * Toggle pinned status
     */
    @Transactional
    public void togglePinned(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
        notice.setPinned(!notice.isPinned());
        noticeRepository.save(notice);
    }

    /**
     * Delete notice
     */
    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
