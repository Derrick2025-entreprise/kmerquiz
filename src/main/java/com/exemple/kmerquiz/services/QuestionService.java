package com.exemple.kmerquiz.services;

import com.exemple.kmerquiz.dto.AnswerDto;
import com.exemple.kmerquiz.dto.AnswerRequest;
import com.exemple.kmerquiz.dto.QuestionDto;
import com.exemple.kmerquiz.dto.QuestionRequest;
import com.exemple.kmerquiz.entity.Category;
import com.exemple.kmerquiz.entity.Difficulty;
import com.exemple.kmerquiz.entity.Question;
import com.exemple.kmerquiz.entity.Response;
import com.exemple.kmerquiz.exceptions.BusinessRuleException;
import com.exemple.kmerquiz.exceptions.ResourceNotFoundException;
import com.exemple.kmerquiz.mappers.CategoryMapper;
import com.exemple.kmerquiz.mappers.DifficultyMapper;
import com.exemple.kmerquiz.repository.CategoryRepository;
import com.exemple.kmerquiz.repository.DifficultyRepository;
import com.exemple.kmerquiz.repository.QuestionRepository;
import com.exemple.kmerquiz.repository.QuizQuestionRepository;
import com.exemple.kmerquiz.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final CategoryRepository categoryRepository;
    private final DifficultyRepository difficultyRepository;
    private final CategoryMapper categoryMapper;
    private final DifficultyMapper difficultyMapper;

    public QuestionDto create(QuestionRequest request) {
        validateAnswers(request.responses());
        Question question = new Question();
        apply(question, request);
        question = questionRepository.save(question);
        saveAnswers(question, request.responses());
        log.info("Question créée idQuestion={} nbReponses={} idDifficulte={}",
                question.getIdQuestion(), request.responses().size(), request.idDifficulty());
        return toDto(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getAll(Long categoryId, Long difficultyId) {
        List<QuestionDto> result = questionRepository.search(categoryId, difficultyId).stream()
                .map(this::toDto)
                .toList();
        log.debug("Recherche de questions idCategorie={} idDifficulte={} -> {} résultat(s)",
                categoryId, difficultyId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public QuestionDto getById(Long id) {
        return toDto(findQuestion(id));
    }

    public QuestionDto update(Long id, QuestionRequest request) {
        validateAnswers(request.responses());
        Question question = findQuestion(id);
        apply(question, request);
        // les réponses sont remplacées entièrement
        responseRepository.deleteByQuestionId(id);
        saveAnswers(question, request.responses());
        log.info("Question modifiée idQuestion={} nbReponses={}", id, request.responses().size());
        return toDto(question);
    }

    public void delete(Long id) {
        Question question = findQuestion(id);
        quizQuestionRepository.deleteByQuestionId(id);
        responseRepository.deleteByQuestionId(id);
        questionRepository.delete(question);
        log.info("Question supprimée idQuestion={}", id);
    }

    Question findQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question non trouvée avec l'id " + id));
    }

    private void validateAnswers(List<AnswerRequest> answers) {
        boolean hasValid = answers.stream().anyMatch(a -> Boolean.TRUE.equals(a.isValid()));
        if (!hasValid) {
            throw new BusinessRuleException("Une question doit avoir au moins une bonne réponse");
        }
    }

    private void apply(Question question, QuestionRequest request) {
        question.setLabel1(request.label1());
        question.setLabel2(request.label2());
        question.setLabel3(request.label3());
        question.setTime(request.time());
        question.setDifficulty(resolveDifficulty(request.idDifficulty()));
        question.setCategories(resolveCategories(request.categoryIds()));
    }

    private Difficulty resolveDifficulty(Long idDifficulty) {
        if (idDifficulty == null) {
            return null;
        }
        return difficultyRepository.findById(idDifficulty)
                .orElseThrow(() -> new ResourceNotFoundException("Difficulté non trouvée avec l'id " + idDifficulty));
    }

    private Set<Category> resolveCategories(Set<Long> ids) {
        Set<Category> categories = new HashSet<>();
        if (ids == null) {
            return categories;
        }
        for (Long id : ids) {
            categories.add(categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id " + id)));
        }
        return categories;
    }

    private void saveAnswers(Question question, List<AnswerRequest> answers) {
        for (AnswerRequest a : answers) {
            Response response = new Response();
            response.setText(a.text());
            response.setIsValid(a.isValid());
            response.setQuestion(question);
            responseRepository.save(response);
        }
    }

    QuestionDto toDto(Question q) {
        List<AnswerDto> answers = responseRepository.findByQuestionIdQuestionOrderByIdResponse(q.getIdQuestion())
                .stream()
                .map(r -> new AnswerDto(r.getIdResponse(), r.getText(), r.getIsValid()))
                .toList();
        return new QuestionDto(
                q.getIdQuestion(),
                q.getLabel1(),
                q.getLabel2(),
                q.getLabel3(),
                q.getTime(),
                q.getDifficulty() == null ? null : difficultyMapper.toDto(q.getDifficulty()),
                q.getCategories().stream()
                        .sorted(Comparator.comparing(Category::getIdCategory))
                        .map(categoryMapper::toDto)
                        .toList(),
                answers);
    }
}
