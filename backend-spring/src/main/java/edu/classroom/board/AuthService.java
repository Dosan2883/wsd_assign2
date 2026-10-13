package edu.classroom.board;

import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Jwts;
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

  public AuthService(
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.issuer:webservice-class}") String jwtIssuer,
      @Value("${app.jwt.audience:board-api}") String jwtAudience,
      @Value("${app.jwt.ttl:90}") long tokenTtlSeconds) {
    this.jwtSecret = jwtSecret;
    this.jwtIssuer = jwtIssuer;
    this.jwtAudience = jwtAudience;
    this.tokenTtlSeconds = tokenTtlSeconds;
  }

  public String issueToken(Users.PublicUser user) {
    if (tokenTtlSeconds <= 0) {
      throw new IllegalStateException("app.jwt.ttl must be greater than zero");
    }

    final byte[] secretBytes;
    try {
      secretBytes = HexFormat.of().parseHex(jwtSecret);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("app.jwt.secret must be a hexadecimal key", e);
    }

    final SecretKey signingKey;
    try {
      signingKey = Keys.hmacShaKeyFor(secretBytes);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("app.jwt.secret must contain at least 32 bytes", e);
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
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
  }

  public long currentUserId(HttpServletRequest request) {
    // TODO BE-02: Bearer 추출 → 서명/알고리즘/만료/클레임 검증 → 실제 사용자 확인.
    // 완성 후 아래 데모 ID 반환을 제거하세요. 토큰 없음/변조/만료는 401입니다.
    return 1L;
  }

  public void assertOwner(long userId, long authorId) {
    // TODO BE-03: 다른 작성자이면 403 FORBIDDEN. 현재는 검사하지 않습니다.
  }
}
