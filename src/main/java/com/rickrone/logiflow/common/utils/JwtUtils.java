package com.rickrone.logiflow.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类：负责 Token 的生成、解析与校验。
 *
 * <p>JWT（JSON Web Token）由三部分组成，以 "." 分隔：{@code Header.Payload.Signature}
 * <ul>
 *   <li>Header：声明签名算法（这里是 HS256）</li>
 *   <li>Payload：claims，包含 subject（用户名）、iat（签发时间）、exp（过期时间）等</li>
 *   <li>Signature：使用密钥对前两部分签名，任何篡改都会导致验签失败</li>
 * </ul>
 *
 * <p>本类使用 HMAC-SHA256 对称签名：服务端用同一个密钥签发和校验，
 * 密钥来自 {@code application.yml} 的 {@code jwt.secret}，
 * Token 有效期来自 {@code jwt.expiration}（毫秒）。
 *
 * <p>使用流程：登录成功后调用 {@link #generateToken(UserDetails)} 签发，
 * 后续请求由 JWT 过滤器取出 Token，调用 {@link #validateToken(String, UserDetails)} 校验。
 */
@Component
public class JwtUtils {

  /** 签名密钥，对应配置 jwt.secret；HS256 要求至少 32 字节（256 位） */
  @Value("${jwt.secret}")
  private String secret;

  /** Token 有效期（毫秒），对应配置 jwt.expiration */
  @Value("${jwt.expiration}")
  private Long expiration;

  /**
   * 把字符串密钥转换为 HMAC 签名用的 Key 对象。
   *
   * <p>注意：{@link Keys#hmacShaKeyFor(byte[])} 会校验密钥长度，
   * HS256 要求密钥至少 256 位（32 字节），过短会抛 WeakKeyException。
   */
  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  /**
   * 1. 根据登录用户生成 Token。
   *
   * <p>subject 存用户名，自定义 claims 暂为空（后续可放用户 ID、角色等）。
   */
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
  }

  /**
   * 组装并签名 Token 的私有方法，所有生成入口最终都走这里。
   *
   * <p>iat 为当前时间，exp 为当前时间 + expiration 毫秒。
   */
  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 2. 从 Token 中提取用户名（payload 的 subject）。
   *
   * <p>注意：解析过程会验签并检查过期时间，Token 非法或已过期时
   * 底层会抛 JwtException（过期具体为 ExpiredJwtException），而不是返回 null。
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * 3. 从 Token 中提取过期时间（payload 的 exp）。
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * 通用的 claim 提取方法：解析出完整 payload 后，用 claimsResolver 取指定字段。
   *
   * <p>例如 {@link #extractUsername(String)} 传入的是 {@code Claims::getSubject}。
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * 解析并验签 Token，返回完整的 payload（claims）。
   *
   * <p>parserBuilder 会用同一密钥验证签名；签名错误、格式非法或已过期都会抛 JwtException
   * （过期抛 ExpiredJwtException）。
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  /**
   * 4. 判断 Token 是否已过期（exp 早于当前时间）。
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * 5. 校验 Token 是否属于指定用户且未过期。
   *
   * <p>供 JWT 过滤器在放行请求前调用：用户名一致且未过期才返回 true。
   */
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
