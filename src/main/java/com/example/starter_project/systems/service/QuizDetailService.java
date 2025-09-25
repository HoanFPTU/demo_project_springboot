package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.*;
import com.example.starter_project.systems.entity.*;
import com.example.starter_project.systems.repository.QuizDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizDetailService {

    @Autowired
    private QuizDetailRepository quizDetailRepository;

    private String validateSortBy(String sortBy) {
        List<String> allowed = List.of("id","name");
        return allowed.contains(sortBy)? sortBy : "name";
    }

    public QuizDetailDTO create(QuizDetailDTO dto) {
        QuizDetail quiz = toEntity(dto);

        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(q -> {
                q.setQuiz(quiz);
                if (q.getAnswers() != null) {
                    q.getAnswers().forEach(a -> a.setQuestion(q));
                }
            });
        }

        if (quiz.getCreatedAt() == null) quiz.setCreatedAt(LocalDateTime.now());
        if (quiz.getActive() == null) quiz.setActive(true);

        QuizDetail saved = quizDetailRepository.save(quiz);
        return toDTO(saved);
    }

    public QuizDetailDTO update(Long id, QuizDetailDTO dto) {
        QuizDetail quiz = quizDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setName(dto.getName());
        quiz.setDescription(dto.getDescription());
        quiz.setDurationSeconds(dto.getDurationSeconds());
        quiz.setLevel(dto.getLevel());
        quiz.setTotalQuestions(dto.getTotalQuestions());
        quiz.setActive(dto.getActive());

        quiz.getQuestions().clear();
        if (dto.getQuestions() != null) {
            List<Question> questions = dto.getQuestions().stream().map(qdto -> {
                Question q = new Question();
                q.setContent(qdto.getContent());
                q.setQuiz(quiz);
                if (qdto.getAnswers() != null) {
                    List<Answer> answers = qdto.getAnswers().stream().map(adto -> {
                        Answer a = new Answer();
                        a.setContent(adto.getContent());
                        a.setIsCorrect(adto.getIsCorrect());
                        a.setQuestion(q);
                        return a;
                    }).collect(Collectors.toList());
                    q.setAnswers(answers);
                }
                return q;
            }).collect(Collectors.toList());
            quiz.setQuestions(questions);
        }

        QuizDetail saved = quizDetailRepository.save(quiz);
        return toDTO(saved);
    }

    public void deleteQuizDetail(Long id) {
        quizDetailRepository.deleteById(id);
    }

    public QuizDetailDTO findByIdDTO(Long id) {
        return quizDetailRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public Page<QuizDetailDTO> findAllPaged(int page,int size,String sortBy,String sortDir) {
        sortBy = validateSortBy(sortBy);
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(sortDir),sortBy));
        return quizDetailRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<QuizDetailDTO> searchQuizDetails(String query,int page,int size,String sortBy,String sortDir) {
        sortBy = validateSortBy(sortBy);
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(sortDir),sortBy));
        return quizDetailRepository.findByNameContainingIgnoreCase(query,pageable).map(this::toDTO);
    }

    public long countQuizDetails() { return quizDetailRepository.count(); }

    public long countBySearchCriteria(String search) {
        return quizDetailRepository.findByNameContainingIgnoreCase(search,Pageable.unpaged()).getTotalElements();
    }

    // ===== NEW: GET ALL LIST FOR EXPORT/IMPORT =====
    public List<QuizDetailDTO> findAll() {
        return quizDetailRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===== Mapper =====
    private QuizDetailDTO toDTO(QuizDetail quiz) {
        List<QuestionDTO> questions = null;
        if (quiz.getQuestions()!=null) {
            questions = quiz.getQuestions().stream().map(q -> {
                List<AnswerDTO> answers = null;
                if (q.getAnswers()!=null)
                    answers = q.getAnswers().stream()
                            .map(a-> new AnswerDTO(a.getId(),a.getContent(),a.getIsCorrect()))
                            .collect(Collectors.toList());
                return new QuestionDTO(q.getId(),q.getContent(),answers);
            }).collect(Collectors.toList());
        }
        return new QuizDetailDTO(quiz.getId(),quiz.getName(),quiz.getDescription(),
                quiz.getTotalQuestions(),quiz.getDurationSeconds(),quiz.getLevel(),
                quiz.getCreatedBy(),quiz.getCreatedAt(),quiz.getActive(),questions);
    }

    private QuizDetail toEntity(QuizDetailDTO dto) {
        QuizDetail quiz = new QuizDetail();
        quiz.setName(dto.getName());
        quiz.setDescription(dto.getDescription());
        quiz.setDurationSeconds(dto.getDurationSeconds());
        quiz.setLevel(dto.getLevel());
        quiz.setTotalQuestions(dto.getTotalQuestions());
        quiz.setCreatedBy(dto.getCreatedBy());
        quiz.setCreatedAt(dto.getCreatedAt());
        quiz.setActive(dto.getActive());

        if (dto.getQuestions()!=null) {
            List<Question> questions = dto.getQuestions().stream().map(qdto -> {
                Question q = new Question();
                q.setContent(qdto.getContent());
                if (qdto.getAnswers()!=null) {
                    List<Answer> answers = qdto.getAnswers().stream().map(adto -> {
                        Answer a = new Answer();
                        a.setContent(adto.getContent());
                        a.setIsCorrect(adto.getIsCorrect());
                        return a;
                    }).collect(Collectors.toList());
                    q.setAnswers(answers);
                }
                return q;
            }).collect(Collectors.toList());
            quiz.setQuestions(questions);
        }
        return quiz;
    }
}
