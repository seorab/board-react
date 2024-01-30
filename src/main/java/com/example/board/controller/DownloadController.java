package com.example.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.model.AtchFile;
import com.example.board.repository.AtchFileRepository;

@Controller
public class DownloadController {
  @Autowired
  AtchFileRepository atchFileRepository;

  @GetMapping("/download")
  public ResponseEntity<Resource> download(
    @RequestParam long id
  ) throws Exception {
    Optional<AtchFile> opt = atchFileRepository.findById(id);
    String fileInfo = "c:/files/no-image.png";

    if(opt.isPresent()) {
      AtchFile article = opt.get();
      fileInfo = article.getFilePath();
    }

    File file = new File(fileInfo);
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    return ResponseEntity.ok()
        .header("content-disposition",
            "filename=" + URLEncoder.encode(file.getName(), "utf-8"))
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType(
          "application/octet-stream"))
        .body(resource);
  }
}