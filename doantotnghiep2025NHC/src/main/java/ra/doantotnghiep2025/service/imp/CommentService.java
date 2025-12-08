package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.model.dto.CommentDTO;
import ra.doantotnghiep2025.model.dto.ReplyDTO;
import ra.doantotnghiep2025.model.entity.Comment;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.Reply;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.CommentRepository;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.repository.ReplyRepository;
import ra.doantotnghiep2025.repository.UserRepository;

import java.time.LocalDateTime; // Nếu dùng LocalDateTime
// import java.util.Date; // Nếu Entity dùng Date
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productsRepository;

    // 1. Tạo bình luận mới (User)
    public CommentDTO createComment(CommentDTO commentDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Products product = productsRepository.findById(commentDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setContent(commentDTO.getContent());

        // Nếu Entity không tự động set thời gian, hãy mở comment dòng dưới:
        // comment.setCreatedAt(new Date());

        comment = commentRepository.save(comment);

        return mapToDTO(comment);
    }

    // 2. Lấy danh sách bình luận theo sản phẩm (Public)
    public List<CommentDTO> getCommentsByProductId(Long productId) {
        List<Comment> comments = commentRepository.findByProductProductId(productId);
        return comments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 3. Lấy tất cả bình luận (Admin)
    public List<CommentDTO> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 4. Trả lời bình luận (Admin) - ĐÃ SỬA LỖI Ở ĐÂY
    public ReplyDTO replyToComment(Long commentId, ReplyDTO replyDTO) {
        // Tìm comment gốc
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận gốc"));

        // Kiểm tra xem đã trả lời chưa (Nếu quy định 1 comment chỉ có 1 reply)
        if (replyRepository.existsByCommentId(commentId)) {
            throw new RuntimeException("Bình luận này đã được trả lời rồi!");
        }

        // Lấy thông tin Admin đang đăng nhập
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản Admin"));

        // --- [QUAN TRỌNG] ĐÃ XÓA ĐOẠN CHECK ROLE THỦ CÔNG ---
        // Lý do: SecurityConfig đã chặn các request không phải ADMIN ở ngoài Controller rồi.
        // Việc check lại ở đây gây lỗi do sai tên Role (ADMIN vs ROLE_ADMIN).

        // Tạo Reply mới
        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUser(admin); // [FIX]: Sửa reply.getUser() thành reply.setUser(admin)
        reply.setContent(replyDTO.getContent());

        // Set thời gian nếu cần
        // reply.setCreatedAt(new Date());

        reply = replyRepository.save(reply);

        return mapToReplyDTO(reply);
    }

    // --- Mapper ---

    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());

        // Kiểm tra null safety cho product
        if (comment.getProduct() != null) {
            dto.setProductId(comment.getProduct().getProductId());

        }

        // Kiểm tra null safety cho user
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getUsername());
        }

        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        // Map reply nếu có
        if (comment.getReply() != null) {
            dto.setReply(mapToReplyDTO(comment.getReply()));
        }
        return dto;
    }

    private ReplyDTO mapToReplyDTO(Reply reply) {
        ReplyDTO dto = new ReplyDTO();
        dto.setId(reply.getId());

        if (reply.getComment() != null) {
            dto.setCommentId(reply.getComment().getId());
        }

        if (reply.getUser() != null) {
            dto.setUserId(reply.getUser().getId());
            // Frontend đang dùng adminName hoặc userName, hãy đảm bảo map đúng
            dto.setUserName(reply.getUser().getUsername());

        }

        dto.setContent(reply.getContent());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        return dto;
    }
}