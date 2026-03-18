package com.QuoocCuongwf.EFEWallet.wallet_system_api.security;

import com.QuoocCuongwf.EFEWallet.wallet_system_api.entity.User;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.repository.UserReponsitory;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserReponsitory userReponsitory;
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse
                                    ,FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt=getJwtFromRequest(httpServletRequest);
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)){
                Long userId=jwtService.getUserIdFromJWT(jwt);
                User user=userReponsitory.findById(userId).orElse(null);
                if (user!=null){
                    CustomUserDetails customUserDetails=new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken
                            authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception ex) {
            log.error("failed on set user authentication", ex);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem header Authorization có chứa thông tin jwt không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
