package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.model.dto.CommentDTO;
import ra.doantotnghiep2025.service.imp.CommentService;

import java.util.List;

@CrossOrigin(origins = "*") // Cho phép gọi từ mọi nguồn (tránh lỗi CORS)
@RestController
@RequestMapping("/api/v1/user/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // API tạo bình luận (Cần đăng nhập - Token sẽ được filter xử lý)
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        CommentDTO createdComment = commentService.createComment(commentDTO);
        return ResponseEntity.ok(createdComment);
    }

    // API xem bình luận (Công khai - Đã được thêm vào EXCLUDED_PATHS)
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByProductId(@Valid @PathVariable Long productId) {
        List<CommentDTO> comments = commentService.getCommentsByProductId(productId);
        return ResponseEntity.ok(comments);
    }
}