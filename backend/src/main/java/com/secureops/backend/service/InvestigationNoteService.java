package com.secureops.backend.service;

import com.secureops.backend.dto.CreateInvestigationNoteRequest;
import com.secureops.backend.entity.Incident;
import com.secureops.backend.entity.InvestigationNote;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.IncidentRepository;
import com.secureops.backend.repository.InvestigationNoteRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public List<InvestigationNote> getNotesByIncident(
            Long incidentId) {

        if (!incidentRepository.existsById(incidentId)) {
            throw new IllegalArgumentException(
                    "Incident not found"
            );
        }

        return noteRepository.findByIncidentId(incidentId);
    }

    public InvestigationNote createNote(
            Long incidentId,
            CreateInvestigationNoteRequest request,
            String analystEmail) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Incident not found"
                        ));

        User analyst = userRepository.findByEmail(analystEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        ));

        InvestigationNote note = new InvestigationNote();

        note.setContent(request.getContent());
        note.setIncident(incident);
        note.setCreatedBy(analyst);

        return noteRepository.save(note);
    }
}