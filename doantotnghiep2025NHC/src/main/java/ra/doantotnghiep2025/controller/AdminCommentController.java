package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.model.dto.CommentDTO;
import ra.doantotnghiep2025.model.dto.ReplyDTO;
import ra.doantotnghiep2025.service.imp.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/comments")
@CrossOrigin(origins = "*") // Đảm bảo không bị chặn CORS từ frontend
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ReplyDTO> replyToComment(
            @PathVariable Long commentId, // Bỏ @Valid ở đây (không cần thiết cho Long)
            @Valid @RequestBody ReplyDTO replyDTO // @Valid giữ ở đây là đúng
            // XÓA BindingResult để lỗi 400 tự động được ném ra
    ) {
        ReplyDTO reply = commentService.replyToComment(commentId, replyDTO);
        return ResponseEntity.ok(reply);
    }
}