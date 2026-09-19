package com.secureops.backend.repository;

import com.secureops.backend.entity.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestigationNoteRepository
        extends JpaRepository<InvestigationNote, Long> {

    List<InvestigationNote> findByIncidentId(Long incidentId);
}