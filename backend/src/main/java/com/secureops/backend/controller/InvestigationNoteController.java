package com.secureops.backend.controller;

import com.secureops.backend.dto.CreateInvestigationNoteRequest;
import com.secureops.backend.dto.InvestigationNoteResponse;
import com.secureops.backend.service.InvestigationNoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/notes")
public class InvestigationNoteController {

    private final InvestigationNoteService noteService;

    public InvestigationNoteController(
            InvestigationNoteService noteService) {

        this.noteService = noteService;
    }

    @GetMapping
    public List<InvestigationNoteResponse> getNotes(
            @PathVariable Long incidentId) {

        return noteService.getNotesByIncident(
                incidentId
        );
    }

    @PostMapping
    public InvestigationNoteResponse createNote(
            @PathVariable Long incidentId,
            @Valid @RequestBody CreateInvestigationNoteRequest request,
            Authentication authentication) {

        String analystEmail =
                authentication.getName();

        return noteService.createNote(
                incidentId,
                request,
                analystEmail
        );
    }
}