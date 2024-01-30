package com.example.board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class AtchFile {
  @Id
  @GeneratedValue
  long id;

  String filePath;

  @ManyToOne
  @JsonIgnore
  Board board;
}
