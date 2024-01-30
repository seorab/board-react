package com.example.board;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class BoardApplicationTests {

	@Test
	void createJwt() {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		// 문자 1개 = 6bit, 24bit(문자 4개) = 1개 단위
		// 240bit 보다 큰 값의 secretKey 사용 => 44개 이상의 문자 필요
		byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
				"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqr");
		Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
		JwtBuilder builder = Jwts.builder()
				// Header
				.setHeaderParam("typ", "JWT")
				// Payload - Registered Claim
				.setSubject("제목").setIssuer("ggoreb.com").setAudience("service user")
				// Payload - Secret Claim
				.claim("username", "ggoreb").claim("password", 1234).claim("hasPermission", "ADMIN")
				// Signature
				.signWith(signingKey, signatureAlgorithm);
		long now = System.currentTimeMillis();
		builder.setExpiration(new Date(now + 1000*30));
		String token = builder.compact();
		log.info("jwt {}", token);
	}

	@Test
	void getDataFromJwt() {
		byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
				"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqr");
		String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLsoJzrqqkiLCJpc3MiOiJnZ29yZWIuY29tIiwiYXVkIjoic2VydmljZSB1c2VyIiwidXNlcm5hbWUiOiJnZ29yZWIiLCJwYXNzd29yZCI6MTIzNCwiaGFzUGVybWlzc2lvbiI6IkFETUlOIiwiZXhwIjoxNzA2MTQ2MTE5fQ.3QPwuIDF_-56ze0klWgfFGMtQKHOIX7k1Ryn2hrm13A";
		JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKeyBytes).build();
		JwsHeader<?> header = jwtParser.parseClaimsJws(jwt).getHeader();
		String algorithm = header.getAlgorithm();
		log.info("Algorithm {}", algorithm);
		String type = header.getType();
		log.info("Type {}", type);
		Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
		log.info("Subject {}", claims.getSubject());
		log.info("Issuer {}", claims.getIssuer());
		log.info("Audience {}", claims.getAudience());
		log.info("claim {}", claims.get("username"));
		log.info("claim {}", claims.get("password"));
		log.info("claim {}", claims.get("hasPermission"));
	}

}
