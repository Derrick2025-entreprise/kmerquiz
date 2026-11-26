package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.QuestionDto;
import com.exemple.kmerquiz.dto.QuizDto;
import com.exemple.kmerquiz.dto.QuizQuestionsRequest;
import com.exemple.kmerquiz.dto.QuizRequest;
import com.exemple.kmerquiz.entity.Question;
import com.exemple.kmerquiz.entity.Quiz;
import com.exemple.kmerquiz.entity.QuizQuestion;
import com.exemple.kmerquiz.entity.QuizQuestionId;
import com.exemple.kmerquiz.entity.User;
import com.exemple.kmerquiz.entity.enums.StatusQuiz;
import com.exemple.kmerquiz.exceptions.BusinessRuleException;
import com.exemple.kmerquiz.exceptions.ResourceNotFoundException;
import com.exemple.kmerquiz.repository.QuizQuestionRepository;
import com.exemple.kmerquiz.repository.QuizRepository;
import com.exemple.kmerquiz.repository.UserRepository;
import com.exemple.kmerquiz.security.CurrentUser;
import com.exemple.kmerquiz.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;

    public QuizDto create(QuizRequest request) {
        log.debug("Création d'un quiz titre='{}' nbQuestions={}", request.title(), request.nbreQuestion());
        validate(request);
        Quiz quiz = new Quiz();
        apply(quiz, request);
        UserPrincipal current = CurrentUser.get();
        if (current != null) {
            User creator = userRepository.getReferenceById(current.getIdUser());
            quiz.setCreatedBy(creator);
        }
        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz créé idQuiz={} idUser={} statut={} nbQuestions={}",
                saved.getIdQuiz(),
                saved.getCreatedBy() == null ? null : saved.getCreatedBy().getIdUser(),
                saved.getStatus(), saved.getNbreQuestion());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<QuizDto> getAll(StatusQuiz status, String title, Long creatorId) {
        String titleFilter = (title == null || title.isBlank()) ? null : title.trim();
        List<QuizDto> result = quizRepository.search(status, titleFilter, creatorId).stream()
                .map(this::toDto)
                .toList();
        log.debug("Recherche de quiz statut={} titre='{}' idCreateur={} -> {} résultat(s)",
                status, titleFilter, creatorId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public QuizDto getById(Long id) {
        return toDto(findQuiz(id));
    }

    public QuizDto update(Long id, QuizRequest request) {
        validate(request);
        Quiz quiz = findQuiz(id);
        checkCanEdit(quiz);
        apply(quiz, request);
        log.info("Quiz modifié idQuiz={}", id);
        return toDto(quiz);
    }

    public void delete(Long id) {
        Quiz quiz = findQuiz(id);
        checkCanEdit(quiz);
        quizQuestionRepository.deleteByQuizId(id);
        quizRepository.delete(quiz);
        log.info("Quiz supprimé idQuiz={}", id);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(Long idQuiz) {
        findQuiz(idQuiz);
        return quizQuestionRepository.findByQuizId(idQuiz).stream()
                .map(qq -> questionService.toDto(qq.getQuestion()))
                .toList();
    }

    public List<QuestionDto> addQuestions(Long idQuiz, QuizQuestionsRequest request) {
        Quiz quiz = findQuiz(idQuiz);
        checkCanEdit(quiz);

        List<Long> ids = request.questionIds().stream().distinct().toList();
        long current = quizQuestionRepository.countByQuizId(idQuiz);
        long toAdd = ids.stream()
                .filter(idQ -> !quizQuestionRepository.existsById(key(idQuiz, idQ)))
                .count();
        if (quiz.getNbreQuestion() != null && current + toAdd > quiz.getNbreQuestion()) {
            throw new BusinessRuleException("Ce quiz est limité à " + quiz.getNbreQuestion()
                    + " questions (" + current + " déjà ajoutées)");
        }

        for (Long idQuestion : ids) {
            QuizQuestionId key = key(idQuiz, idQuestion);
            if (quizQuestionRepository.existsById(key)) {
                continue;
            }
            Question question = questionService.findQuestion(idQuestion);
            QuizQuestion link = new QuizQuestion();
            link.setId(key);
            link.setQuiz(quiz);
            link.setQuestion(question);
            link.setStatus(true);
            quizQuestionRepository.save(link);
        }
        log.info("Questions ajoutées au quiz idQuiz={} demandées={} nouvelles={} total={}",
                idQuiz, ids.size(), toAdd, current + toAdd);
        return getQuestions(idQuiz);
    }

    public void removeQuestion(Long idQuiz, Long idQuestion) {
        Quiz quiz = findQuiz(idQuiz);
        checkCanEdit(quiz);
        QuizQuestionId key = key(idQuiz, idQuestion);
        if (!quizQuestionRepository.existsById(key)) {
            throw new ResourceNotFoundException("La question " + idQuestion + " n'est pas dans le quiz " + idQuiz);
        }
        quizQuestionRepository.deleteById(key);
        log.info("Question retirée du quiz idQuiz={} idQuestion={}", idQuiz, idQuestion);
    }
    private Quiz findQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz non trouvé avec l'id " + id));
    }
    private void checkCanEdit(Quiz quiz) {
        if (CurrentUser.isAdmin()) {
            return;
        }
        UserPrincipal current = CurrentUser.get();
        boolean owner = current != null && quiz.getCreatedBy() != null
                && current.getIdUser().equals(quiz.getCreatedBy().getIdUser());
        if (!owner) {
            throw new AccessDeniedException("Vous n'êtes pas le créateur de ce quiz");
        }
    }

    private void validate(QuizRequest r) {
        if (r.beginDate() != null && r.endDate() != null && r.endDate().isBefore(r.beginDate())) {
            throw new BusinessRuleException("La date de fin doit être après la date de début");
        }
        if (r.scoreMin() != null && r.totalScore() != null && r.scoreMin() > r.totalScore()) {
            throw new BusinessRuleException("Le score minimum ne peut pas dépasser le score total");
        }
    }

    private void apply(Quiz quiz, QuizRequest r) {
        quiz.setTitle(r.title());
        quiz.setDescription(r.description());
        quiz.setBeginDate(r.beginDate());
        quiz.setEndDate(r.endDate());
        quiz.setTotalScore(r.totalScore());
        quiz.setScoreMin(r.scoreMin());
        quiz.setNbreQuestion(r.nbreQuestion());
        quiz.setTime(r.time());
        if (r.status() != null) {
            quiz.setStatus(r.status());
        } else if (quiz.getStatus() == null && StatusQuiz.values().length > 0) {
            quiz.setStatus(StatusQuiz.values()[0]); // premier statut de l'enum par défaut
        }
    }

    private static QuizQuestionId key(Long idQuiz, Long idQuestion) {
        QuizQuestionId id = new QuizQuestionId();
        id.setIdQuiz(idQuiz);
        id.setIdQuestion(idQuestion);
        return id;
    }

    private QuizDto toDto(Quiz q) {
        User creator = q.getCreatedBy();
        return new QuizDto(
                q.getIdQuiz(),
                q.getTitle(),
                q.getDescription(),
                q.getBeginDate(),
                q.getEndDate(),
                q.getTotalScore(),
                q.getScoreMin(),
                q.getNbreQuestion(),
                q.getTime(),
                q.getStatus(),
                creator == null ? null : creator.getIdUser(),
                creator == null ? null : creator.getPseudo(),
                q.getIdQuiz() == null ? 0 : quizQuestionRepository.countByQuizId(q.getIdQuiz()));
    }
}
