package com.example.notesapp.dto;
import java.time.LocalDateTime;
public record NoteResponseDto(Long id, String content, LocalDateTime updatedAt, Integer version) {}
