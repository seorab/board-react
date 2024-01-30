package com.example.board.controller;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.board.model.User;
import com.example.board.repository.UserRepository;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.DatatypeConverter;

@Controller
public class UserController {
	@Autowired
	UserRepository userRepository;

	@Autowired
	HttpSession session;
	
	@GetMapping("/signin")
	public String signin() {
		return "signin";
	}
	
	@PostMapping("/signin")
	public String signinPost(@ModelAttribute User user) {
		User dbUser =  
			userRepository.findByEmail(user.getEmail());
		if(dbUser != null) {
			String dbPwd = dbUser.getPwd();
			String userPwd = user.getPwd();
			boolean isMatch = passwordEncoder.matches(userPwd, dbPwd);
			if(isMatch) {
				session.setAttribute("user_info", dbUser);
			}
		}
		return "redirect:/";
	}

	@PostMapping("/api/signin")
	@CrossOrigin
	@ResponseBody
	public Map<String, Object> apiSigninPost(@ModelAttribute User user) {
		User dbUser =  
			userRepository.findByEmail(user.getEmail());

		Map<String, Object> map = new HashMap<>();
		map.put("code", 201);
		map.put("msg", "로그인 실패");

		if(dbUser != null) {
			String dbPwd = dbUser.getPwd();
			String userPwd = user.getPwd();
			boolean isMatch = passwordEncoder.matches(userPwd, dbPwd);
			if(isMatch) {
				map.put("code", 200);
				map.put("msg", "로그인 성공");
				map.put("login_user", user.getEmail());
				map.put("jwt", createJwt(
					dbUser.getId(), 
					dbUser.getEmail(), 
					dbUser.getName()));
			}
		}
		return map;
	}

	@GetMapping("/signout")
	public String signout() {
		session.invalidate();
		return "redirect:/";
	}
	
	@GetMapping("/signup") 
	public String signup() {
		return "signup";
	}

	@Autowired
	PasswordEncoder passwordEncoder;

	@PostMapping("/signup")
	@CrossOrigin
	public String signupPost(@ModelAttribute User user) {
		String userPwd = user.getPwd();
		String encodedPwd = passwordEncoder.encode(userPwd);
		user.setPwd(encodedPwd);

		userRepository.save(user);
		return "redirect:/";
	}

	@PostMapping("/api/signup")
	@CrossOrigin
	@ResponseBody
	public Map<String, Object> apiSignupPost(@ModelAttribute User user) {
		String userPwd = user.getPwd();
		String encodedPwd = passwordEncoder.encode(userPwd);
		user.setPwd(encodedPwd);

		Map<String, Object> map = new HashMap<>();
		try {
			userRepository.save(user);
			map.put("code", 200);
			map.put("msg", "회원 가입 완료");
		} catch(Exception e) {
			e.printStackTrace();
			map.put("code", 201);
			map.put("msg", "회원 가입 실패");
		}

		return map;
	}

	@Value("${jwt.secret-key}")
	String secretKey;
	
	String createJwt(Long id, String email, String name) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
				secretKey);
		Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
		JwtBuilder builder = Jwts.builder()
				.setHeaderParam("typ", "JWT")
				.claim("id", id).claim("email", email)
				.claim("name", name)
				.signWith(signingKey, signatureAlgorithm);
		long now = System.currentTimeMillis();
		builder.setExpiration(new Date(now + 1000*60*30));
		String token = builder.compact();
		return token;
	}
}