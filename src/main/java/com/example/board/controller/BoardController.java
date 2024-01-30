package com.example.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.example.board.model.AtchFile;
import com.example.board.model.Board;
import com.example.board.model.Comment;
import com.example.board.model.User;
import com.example.board.repository.AtchFileRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.DatatypeConverter;

@Controller
public class BoardController {
	@Value("${jwt.secret-key}")
	String secretKey;

	@Autowired
	BoardRepository boardRepository;
	
	@Autowired
	HttpSession session;

	@Autowired CommentRepository commentRepository;

	@GetMapping("/board/comment/remove/{id}")
	public String commentRemove(
		@PathVariable long id
	) {
		commentRepository.deleteById(id);
		return "redirect:/board/list";
	}

	@PostMapping("/board/comment/add")
	public String commentAdd(
		@ModelAttribute Comment comment, 
		@RequestParam long boardId) {
		// 1. board_id
		Board board = new Board();
		board.setId(boardId);  // 댓글을 추가할 게시물의 번호로 Board 객체 생성
		comment.setBoard(board);
		// 2. user_id
		User user = (User) session.getAttribute("user_info");
		comment.setUser(user);

		commentRepository.save(comment);
		return "redirect:/board/" + boardId;
	}

	@GetMapping("/board/delete/{id}")
	public String boardDelete(@PathVariable("id") long id) { 
		boardRepository.deleteById(id);
		return "redirect:/board/list";
	}

	@GetMapping("/board/update/{id}")
	public String boardUpdate(Model model, @PathVariable("id") long id) {
		Optional<Board> data = boardRepository.findById(id);
		Board board = data.get();
		model.addAttribute("board", board);
		return "board/update";
	}
		
	@PostMapping("/board/update/{id}")
	public String boardUpdate(
			@ModelAttribute Board board, @PathVariable("id") long id) {
		User user = (User) session.getAttribute("user_info");
		board.setUser(user);
		board.setId(id);
		boardRepository.save(board);
		return "redirect:/board/" + id;
	}

	@GetMapping("/board/{id}")
	public String boardView(Model model, @PathVariable("id") long id) {
		Optional<Board> data = boardRepository.findById(id);
		Board board = data.get();
		model.addAttribute("board", board);
		return "board/view";
	}
	
	@GetMapping("/api/board/{id}")
	@CrossOrigin
	@ResponseBody
	public Board apiBoardView(Model model, @PathVariable("id") long id) {
		Optional<Board> data = boardRepository.findById(id);
		Board board = data.get();
		return board;
	}
	
	@GetMapping("/board/list")
	public String boardList(
		Model model,
		@RequestParam(defaultValue = "1") int page
  ) {

		Sort sort = Sort.by(Order.desc("id"));
		Pageable pageable = PageRequest.of(page - 1, 10, sort);
		Page<Board> list = boardRepository.findAll(pageable);
		
		int totalPage = list.getTotalPages();
		int start = (page - 1) / 10 * 10 + 1;
		int end = start + 9;
		// 10        2
		if(end > totalPage) {
			end = totalPage;
		}

		model.addAttribute("list", list);
		model.addAttribute("start", start);
		model.addAttribute("end", end);
		return "board/list";
	}
	
	@GetMapping("/api/board/list")
	@CrossOrigin @ResponseBody
	public List<Board> apiBoardList(
		Model model,
		@RequestParam(defaultValue = "1") int page
  ) {

		Sort sort = Sort.by(Order.desc("id"));
		Pageable pageable = PageRequest.of(page - 1, 10, sort);
		Page<Board> list = boardRepository.findAll(pageable);
		
		int totalPage = list.getTotalPages();
		int start = (page - 1) / 10 * 10 + 1;
		int end = start + 9;
		// 10        2
		if(end > totalPage) {
			end = totalPage;
		}

		// model.addAttribute("list", list);
		// model.addAttribute("start", start);
		// model.addAttribute("end", end);
		return list.getContent();
	}

	@GetMapping("/board/write")
	public String boardWrite() {
		return "board/write";
	}
	
	@Autowired AtchFileRepository atchFileRepository;
	@Autowired JdbcTemplate jdbcTemplate;

	@PostMapping("/board/write")
	@Transactional(rollbackFor = {IOException.class})
	// 메소드가 시작될 때(before) 자동 저장(auto commit) 기능을 비활성화
	// 메소드가 종료될 때(after) 수동으로 commit 실행
	// RuntimeException 계열일 때 Rollback 기능 수행
	public String boardWrite(
		@ModelAttribute Board board,
		@RequestParam("file") MultipartFile mFile
	) {
		jdbcTemplate.queryForList("select * from user");

		// 제목 또는 내용을 작성하지 않은 경우 글쓰기 기능을 실행하지 않음
		if(board.getTitle().equals("") || board.getContent().equals("")) {
			return "redirect:/board/write";
		}
		// throw new NullPointerException();
		// throw new ArithmeticException();
		// throw new IOException();
		// throw new RuntimeException();

		// new FileInputStream("c:/springboot/a.txt");

		// new File("").createNewFile();

		/* Board 데이터 입력 - 게시글 쓰기 */
		User user = (User) session.getAttribute("user_info");
		board.setUser(user);
		Board savedBoard = boardRepository.save(board);

		// throw new RuntimeException();
		// System.out.println(4 / 0); // 산술연산 예외!  Unchecked Exception
		// new File("").createNewFile();  // Checked Exception

		/* AtchFile 데이터 입력 - 파일 첨부 */
		// 1. 파일 저장 transferTo()
		String oName = mFile.getOriginalFilename();
		if(!oName.equals("")) {
			try {
				mFile.transferTo(new File("c:/files/" + oName));
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 2. 파일이 저장된 위치와 파일이름 데이터베이스에 입력
			AtchFile atchFile = new AtchFile();
			atchFile.setFilePath("c:/files/" + oName);
			atchFile.setBoard(savedBoard);
			atchFileRepository.save(atchFile);
		}
		return "redirect:/board/" + savedBoard.getId();
	}

	@PostMapping("/api/board/write")
	@ResponseBody
	@CrossOrigin
	@Transactional(rollbackFor = {IOException.class})
	public String apiBoardWrite(
		@ModelAttribute Board board,
		@RequestParam(required=false, value="file") MultipartFile mFile,
		@RequestParam(required=false, value="jwt") String jwt
	) {
		getDataFromJwt(jwt);

		/* Board 데이터 입력 - 게시글 쓰기 */
		// User user = (User) session.getAttribute("user_info");
		// board.setUser(user);
		Board savedBoard = boardRepository.save(board);

		/* AtchFile 데이터 입력 - 파일 첨부 */
		// 1. 파일 저장 transferTo()
		if(mFile != null) {

			String oName = mFile.getOriginalFilename();

			if(!oName.equals("")) {
				try {
					mFile.transferTo(new File("c:/files/" + oName));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 2. 파일이 저장된 위치와 파일이름 데이터베이스에 입력
				AtchFile atchFile = new AtchFile();
				atchFile.setFilePath("c:/files/" + oName);
				atchFile.setBoard(savedBoard);
				atchFileRepository.save(atchFile);
			}
			
		}
		
		return "{\"msg\": \"ok\"}";
	}

	void getDataFromJwt(String jwt) {
		byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
				secretKey);

		JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKeyBytes).build();
		JwsHeader<?> header = jwtParser.parseClaimsJws(jwt).getHeader();

		Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
		int id = (int) claims.get("id");
		String email = (String) claims.get("email");
		String name = (String) claims.get("name");
		System.out.println(id);
		System.out.println(email);
		System.out.println(name);
	}
}