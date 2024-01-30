package com.example.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.model.AtchFile;

public interface AtchFileRepository extends JpaRepository<AtchFile, Long> {
  
}