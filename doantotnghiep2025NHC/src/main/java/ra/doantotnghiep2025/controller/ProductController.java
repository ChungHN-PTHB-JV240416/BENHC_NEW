package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.ProductReponseDTO; // Lưu ý: giữ nguyên tên class DTO của bạn (dù thiếu chữ s)
import ra.doantotnghiep2025.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<List<ProductReponseDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductReponseDTO> products = productService.searchProducts(keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ProductReponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(productService.getProducts(page, size, sortBy, direction));
    }

    @GetMapping("/featured-products")
    public ResponseEntity<List<ProductReponseDTO>> getFeaturedProducts() {
        List<ProductReponseDTO> featuredProducts = productService.getFeaturedProducts();
        return ResponseEntity.ok(featuredProducts);
    }

    @GetMapping("/new-products")
    public ResponseEntity<List<ProductReponseDTO>> getNewProducts() {
        List<ProductReponseDTO> newProducts = productService.getNewProducts();
        return ResponseEntity.ok(newProducts);
    }

    @GetMapping("/best-seller-products")
    public ResponseEntity<List<ProductReponseDTO>> getBestSellerProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductReponseDTO> products = productService.getBestSellerProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<Page<ProductReponseDTO>> getProductsByCategory(
            @Valid
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductReponseDTO> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(products);
    }

    // --- [QUAN TRỌNG] ĐÂY LÀ HÀM BẠN ĐANG THIẾU ---
    // Hàm này phục vụ cho trang Chi tiết thương hiệu bên React
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductReponseDTO>> getProductsByBrand(@PathVariable Long brandId) {
        // Lưu ý: Bạn cần đảm bảo ProductService có hàm getProductsByBrand
        List<ProductReponseDTO> products = productService.getProductsByBrand(brandId);
        return ResponseEntity.ok(products);
    }
    // ----------------------------------------------

    @GetMapping("/{productId}")
    public ResponseEntity<ProductReponseDTO> getProductById(@Valid @PathVariable Long productId) throws CustomerException {
        ProductReponseDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
}