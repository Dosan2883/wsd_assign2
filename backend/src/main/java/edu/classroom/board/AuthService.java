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
  private final String jwtIssuer;
  private final String jwtAudience;
  private final long tokenTtlSeconds;
  private final Users users;
  private final SecretKey jwtSigningKey;

  public AuthService(
      Users users,
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.issuer:webservice-class}") String jwtIssuer,
      @Value("${app.jwt.audience:board-api}") String jwtAudience,
      @Value("${app.jwt.ttl:90}") long tokenTtlSeconds) {
    this.users = users;
    this.jwtIssuer = jwtIssuer;
    this.jwtAudience = jwtAudience;
    this.tokenTtlSeconds = tokenTtlSeconds;
    if (jwtSecret == null
        || jwtSecret.isBlank()
        || "REPLACE_WITH_RANDOM_64_HEX_CHARACTERS".equals(jwtSecret)
        || !jwtSecret.matches("[0-9a-fA-F]{64}")) {
      throw new IllegalStateException(
          "JWT_SECRET must be a newly generated 64-character hexadecimal string");
    }
    if (tokenTtlSeconds <= 0) {
      throw new IllegalStateException("TOKEN_TTL must be greater than zero");
    }
    this.jwtSigningKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(jwtSecret));
  }

  public String issueToken(Users.PublicUser user) {
    Instant now = Instant.now();
    Date expiresAt = Date.from(now.plusSeconds(tokenTtlSeconds));
    return Jwts.builder()
        .subject(Long.toString(user.id()))
        .issuer(jwtIssuer)
        .audience()
        .add(jwtAudience)
        .and()
        .expiration(expiresAt)
        .signWith(jwtSigningKey, Jwts.SIG.HS256)
        .compact();
  }

  public long currentUserId(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null) {
      throw new ApiError(401, "TOKEN_REQUIRED", "로그인이 필요합니다.");
    }

    String[] parts = authorization.trim().split("\\s+");
    if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer")) {
      throw new ApiError(401, "TOKEN_REQUIRED", "Bearer 토큰을 보내세요.");
    }

    try {
      Jws<Claims> parsed =
          Jwts.parser()
              .verifyWith(jwtSigningKey)
              .sig()
              .clear()
              .add(Jwts.SIG.HS256)
              .and()
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
