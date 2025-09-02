package com.example.notesapp.service;

import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.NoteResponseDto;
import com.example.notesapp.dto.ShareResponseDto;
import com.example.notesapp.dto.UpdateNoteRequest;
import com.example.notesapp.model.Note;
import com.example.notesapp.model.User;
import com.example.notesapp.repository.NoteRepository;
import com.example.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private NoteResponseDto mapToNoteResponseDto(Note note) {
        return new NoteResponseDto(note.getId(), note.getContent(), note.getUpdatedAt(), note.getVersion());
    }

    public List<NoteResponseDto> getAllNotesForUser() {
        User user = getCurrentUser();
        return noteRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToNoteResponseDto)
                .collect(Collectors.toList());
    }

    public NoteResponseDto createNote(NoteDto noteDto) {
        User user = getCurrentUser();
        Note note = new Note();
        note.setUser(user);
        note.setContent(noteDto.content());
        Note savedNote = noteRepository.save(note);
        return mapToNoteResponseDto(savedNote);
    }

    @Transactional
    public NoteResponseDto updateNote(Long noteId, UpdateNoteRequest request) throws AccessDeniedException {
        User user = getCurrentUser();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this note");
        }

        if (!note.getVersion().equals(request.version())) {
            throw new IllegalStateException("Conflict: The note has been updated by someone else. Please refresh.");
        }

        note.setContent(request.content());
        Note updatedNote = noteRepository.save(note);
        return mapToNoteResponseDto(updatedNote);
    }

    @Transactional
    public void deleteNote(Long noteId) throws AccessDeniedException {
        User user = getCurrentUser();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this note");
        }

        noteRepository.delete(note);
    }

    @Transactional
    public ShareResponseDto shareNote(Long noteId, String baseUrl) throws AccessDeniedException {
        User user = getCurrentUser();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to share this note");
        }

        if (note.getShareId() == null) {
            note.setShareId(UUID.randomUUID());
        }
        note.setPublic(true);
        noteRepository.save(note);

        String shareUrl = baseUrl + "/share/" + note.getShareId();
        return new ShareResponseDto(shareUrl);
    }

    public NoteResponseDto getPublicNote(UUID shareId) {
        Note note = noteRepository.findByShareId(shareId)
                .orElseThrow(() -> new RuntimeException("Shared note not found"));

        if (!note.isPublic()) {
            throw new RuntimeException("This note is not public");
        }

        return mapToNoteResponseDto(note);
    }
}
