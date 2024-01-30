package com.example.board.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Comment {
  @Id
  @GeneratedValue
  long id;
  String comment;

  @ManyToOne
  @JsonIgnore
  Board board;

  @ManyToOne
  @JsonIgnore
  User user;
}
