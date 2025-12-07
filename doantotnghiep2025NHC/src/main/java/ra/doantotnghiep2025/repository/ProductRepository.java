package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.doantotnghiep2025.model.entity.Products;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {

    boolean existsByProductName(String productName);

    List<Products> findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(String productName, String productDescription);

    Page<Products> findAll(Pageable pageable);

    List<Products> findTop10ByOrderBySoldQuantityDesc();

    List<Products> findTop10ByOrderByCreatedAtDesc();

    List<Products> findByOrderBySoldQuantityDesc(Pageable pageable);

    // --- CATEGORY ---
    List<Products> findByCategoryCategoryId(Long categoryId, Pageable pageable);
    List<Products> findByCategoryCategoryId(Long categoryId); // Lấy tất cả theo danh mục

    Optional<Products> findById(Long productId);

    List<Products> findTop10ByCreatedAtBetweenOrderByLikesDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // --- BRAND (PHẦN SỬA LỖI) ---

    // 1. Hàm cũ (Có phân trang) - Giữ nguyên
    List<Products> findByBrandBrandId(Long brandId, Pageable pageable);

    // 2. [THÊM MỚI] Hàm lấy tất cả sản phẩm theo Brand (Không phân trang)
    // Hàm này giúp Service gọi được .getProductsByBrand(brandId)
    List<Products> findByBrandBrandId(Long brandId);
}