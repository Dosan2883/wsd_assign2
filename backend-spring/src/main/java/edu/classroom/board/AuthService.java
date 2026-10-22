package edu.classroom.board;

import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final String jwtSecret;
  private final String jwtIssuer;
  private final String jwtAudience;
  private final long tokenTtlSeconds;
  private final Users users;

  public AuthService(
      Users users,
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.issuer:webservice-class}") String jwtIssuer,
      @Value("${app.jwt.audience:board-api}") String jwtAudience,
      @Value("${app.jwt.ttl:90}") long tokenTtlSeconds) {
    this.users = users;
    this.jwtSecret = jwtSecret;
    this.jwtIssuer = jwtIssuer;
    this.jwtAudience = jwtAudience;
    this.tokenTtlSeconds = tokenTtlSeconds;
  }

  public String issueToken(Users.PublicUser user) {
    if (tokenTtlSeconds <= 0) {
      throw new IllegalStateException("app.jwt.ttl must be greater than zero");
    }

    Instant now = Instant.now();
    Date expiresAt = Date.from(now.plusSeconds(tokenTtlSeconds));
    return Jwts.builder()
        .subject(Long.toString(user.id()))
        .issuer(jwtIssuer)
        .audience()
        .add(jwtAudience)
        .and()
        .expiration(expiresAt)
        .signWith(signingKey(), Jwts.SIG.HS256)
        .compact();
  }

  private SecretKey signingKey() {
    final byte[] secretBytes;
    try {
      secretBytes = HexFormat.of().parseHex(jwtSecret);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("app.jwt.secret must be a hexadecimal key", e);
    }

    try {
      return Keys.hmacShaKeyFor(secretBytes);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("app.jwt.secret must contain at least 32 bytes", e);
    }
  }

  public long currentUserId(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null) {
      throw new ApiError(401, "TOKEN_REQUIRED", "로그인이 필요합니다.");
    }

    String[] parts = authorization.trim().split("\\s+", 2);
    if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer") || parts[1].isBlank()) {
      throw new ApiError(401, "TOKEN_REQUIRED", "Bearer 토큰을 보내세요.");
    }

    try {
      Jws<Claims> parsed =
          Jwts.parser()
              .verifyWith(signingKey())
              .requireIssuer(jwtIssuer)
              .requireAudience(jwtAudience)
              .build()
              .parseSignedClaims(parts[1].trim());

      if (!Jwts.SIG.HS256.getId().equals(parsed.getHeader().getAlgorithm())) {
        throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
      }

      Claims claims = parsed.getPayload();
      Object subjectClaim = claims.get("sub");
      if (!(subjectClaim instanceof String subject)
          || subject.isBlank()
          || claims.getExpiration() == null
          || !jwtIssuer.equals(claims.getIssuer())
          || claims.getAudience() == null
          || !claims.getAudience().contains(jwtAudience)
          || !subject.matches("[1-9][0-9]*")) {
        throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
      }

      final long userId;
      try {
        userId = Long.parseLong(subject);
      } catch (NumberFormatException e) {
        throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
      }

      if (users.find(userId) == null) {
        throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
      }
      return userId;
    } catch (ExpiredJwtException e) {
      throw new ApiError(401, "TOKEN_EXPIRED", "로그인 시간이 만료되었습니다. 다시 로그인하세요.");
    } catch (JwtException | IllegalArgumentException e) {
      throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
    }
  }

  public void assertOwner(long userId, long authorId) {
    if (userId != authorId) {
      throw new ApiError(403, "FORBIDDEN", "게시글 작성자만 수정하거나 삭제할 수 있습니다.");
    }
  }
}
