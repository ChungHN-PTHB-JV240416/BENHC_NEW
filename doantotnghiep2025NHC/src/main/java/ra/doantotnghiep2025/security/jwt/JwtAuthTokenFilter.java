package ra.doantotnghiep2025.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ra.doantotnghiep2025.security.UserDetailService;
import ra.doantotnghiep2025.service.TokenService;

import java.io.IOException;

@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);

            if (token != null) {
                // [DEBUG] In ra để kiểm tra
                System.out.println("------------------------------------------------");
                System.out.println("🔍 [FILTER] Đang kiểm tra request: " + request.getRequestURI());

                boolean isValid = jwtProvider.validateToken(token);
                if (!isValid) {
                    System.err.println("❌ [FILTER] Token KHÔNG hợp lệ (Hết hạn hoặc sai chữ ký)!");
                } else {
                    if (tokenService.isTokenInvalidated(token)) {
                        System.err.println("❌ [FILTER] Token nằm trong Blacklist (Đã logout)");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Invalidated");
                        return;
                    }

                    String username = jwtProvider.getUserNameFromToken(token);
                    System.out.println("✅ [FILTER] Token hợp lệ. Username: " + username);

                    UserDetails userDetails = userDetailService.loadUserByUsername(username);
                    if (userDetails != null) {
                        System.out.println("🛡️ [FILTER] Quyền (Authorities) trong DB: " + userDetails.getAuthorities());

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        System.out.println("🚀 [FILTER] Xác thực thành công -> Chuyển tiếp request");
                    } else {
                        System.err.println("❌ [FILTER] Không tìm thấy UserDetails cho: " + username);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [FILTER] LỖI NGOẠI LỆ (CRITICAL ERROR):");
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}