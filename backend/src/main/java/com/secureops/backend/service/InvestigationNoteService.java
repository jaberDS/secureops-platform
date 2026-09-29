package com.secureops.backend.service;

import com.secureops.backend.dto.CreateInvestigationNoteRequest;
import com.secureops.backend.dto.InvestigationNoteResponse;
import com.secureops.backend.entity.Incident;
import com.secureops.backend.entity.InvestigationNote;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.IncidentRepository;
import com.secureops.backend.repository.InvestigationNoteRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvestigationNoteService {

    private final InvestigationNoteRepository noteRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public InvestigationNoteService(
            InvestigationNoteRepository noteRepository,
            IncidentRepository incidentRepository,
            UserRepository userRepository) {

        this.noteRepository = noteRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<InvestigationNoteResponse> getNotesByIncident(
            Long incidentId) {

        if (!incidentRepository.existsById(incidentId)) {
            throw new IllegalArgumentException(
                    "Incident not found"
            );
        }

        return noteRepository
                .findByIncidentId(incidentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InvestigationNoteResponse createNote(
            Long incidentId,
            CreateInvestigationNoteRequest request,
            String analystEmail) {

        Incident incident =
                incidentRepository.findById(incidentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Incident not found"
                                ));

        User analyst =
                userRepository.findByEmail(analystEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated user not found"
                                ));

        InvestigationNote note =
                new InvestigationNote();

        note.setContent(request.getContent());
        note.setIncident(incident);
        note.setCreatedBy(analyst);

        InvestigationNote savedNote =
                noteRepository.save(note);

        return toResponse(savedNote);
    }

    private InvestigationNoteResponse toResponse(
            InvestigationNote note) {

        String createdByEmail = null;

        if (note.getCreatedBy() != null) {
            createdByEmail =
                    note.getCreatedBy().getEmail();
        }

        return new InvestigationNoteResponse(
                note.getId(),
                note.getContent(),
                createdByEmail
        );
    }
}