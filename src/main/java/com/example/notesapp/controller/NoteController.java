package com.example.notesapp.controller;

import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.NoteResponseDto;
import com.example.notesapp.dto.ShareResponseDto;
import com.example.notesapp.dto.UpdateNoteRequest;
import com.example.notesapp.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // --- PROTECTED ROUTES ---
    @GetMapping("/api/notes")
    public ResponseEntity<List<NoteResponseDto>> getAllUserNotes() {
        return ResponseEntity.ok(noteService.getAllNotesForUser());
    }

    @PostMapping("/api/notes")
    public ResponseEntity<NoteResponseDto> createNote(@RequestBody NoteDto noteDto) {
        return ResponseEntity.status(201).body(noteService.createNote(noteDto));
    }

    @PutMapping("/api/notes/{id}")
    public ResponseEntity<NoteResponseDto> updateNote(@PathVariable Long id, @RequestBody UpdateNoteRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @DeleteMapping("/api/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) throws AccessDeniedException {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/notes/{id}/share")
    public ResponseEntity<ShareResponseDto> shareNote(@PathVariable Long id, HttpServletRequest request) throws AccessDeniedException {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return ResponseEntity.ok(noteService.shareNote(id, baseUrl));
    }

    // --- PUBLIC ROUTE ---
    @GetMapping("/share/{shareId}")
    public ResponseEntity<NoteResponseDto> getSharedNote(@PathVariable UUID shareId) {
        return ResponseEntity.ok(noteService.getPublicNote(shareId));
    }
}
