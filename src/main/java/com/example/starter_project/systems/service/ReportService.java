package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.ReportDTO;
import com.example.starter_project.systems.entity.Report;
import com.example.starter_project.systems.mapper.ReportMapper;
import com.example.starter_project.systems.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportMapper reportMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Report> findById(Long id) {
        return reportRepository.findById(id);
    }

    public Optional<Report> findByName(String name) {
        return reportRepository.findByName(name);
    }

    public List<ReportDTO> findAll() {
        List<Report> reports = reportRepository.findAll();
        return reportMapper.toDTOList(reports);
    }

    public Page<ReportDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Report> reports = reportRepository.findAll(pageable);
        return reports.map(reportMapper::toDTO);
    }

    public Page<ReportDTO> searchReports(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Report> reports = reportRepository.findByNameContainingIgnoreCase(query, pageable);
        return reports.map(reportMapper::toDTO);
    }

    public ReportDTO create(ReportDTO dto) {
        Report entity = reportMapper.toEntity(dto);
        Report saved = reportRepository.save(entity);
        return reportMapper.toDTO(saved);
    }

    public ReportDTO update(Long id, ReportDTO dto) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        reportMapper.updateEntityFromDto(dto, report);
        Report updated = reportRepository.save(report);
        return reportMapper.toDTO(updated);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    public long countReports() {
        return reportRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return reportRepository.countByNameContainingIgnoreCase(search);
    }
}