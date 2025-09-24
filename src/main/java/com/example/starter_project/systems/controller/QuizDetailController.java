package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.QuizDetailDTO;
import com.example.starter_project.systems.entity.QuizDetail;
import com.example.starter_project.systems.service.QuizDetailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizDetails")
public class QuizDetailController {

    @Autowired
    private QuizDetailService quizDetailService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody QuizDetailDTO dto) {
        try {
            QuizDetailDTO created = quizDetailService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        quizDetailService.deleteQuizDetail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailDTO> getById(@PathVariable Long id){
        QuizDetailDTO quiz = quizDetailService.findByIdDTO(id);
        if(quiz==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(quiz);
    }

    @GetMapping
    public ResponseEntity<Page<QuizDetailDTO>> getAll(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "name,asc") String sort,
                                                      @RequestParam(required = false) String search){
        String[] arr = sort.split(",");
        String sortBy = arr[0];
        String sortDir = arr.length>1? arr[1]:"asc";
        if(search!=null && !search.isEmpty()) {
            return ResponseEntity.ok(quizDetailService.searchQuizDetails(search,page,size,sortBy,sortDir));
        }
        return ResponseEntity.ok(quizDetailService.findAllPaged(page,size,sortBy,sortDir));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(@RequestParam(required = false) String search){
        long count = (search!=null && !search.isEmpty())?
                quizDetailService.countBySearchCriteria(search):quizDetailService.countQuizDetails();
        return ResponseEntity.ok(count);
    }
}
