package com.exemple.kmerquiz;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parcours complet avec de vrais jetons JWT : inscription, connexion,
 * permissions, quiz et questions.
 * N'utilise ni catégories ni difficultés pour ne pas perturber les autres tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthQuizFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authenticationQuizAndQuestionFlow() throws Exception {
        // --- inscription + connexion d'un joueur ---
        String player = """
                {"pseudo":"joueur1","email":"joueur1@test.cm","password":"secret123"}
                """;
        String registerBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(player))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value("joueur1@test.cm"))
                .andReturn().getResponse().getContentAsString();
        String playerToken = JsonPath.read(registerBody, "$.accessToken");
        String refreshToken = JsonPath.read(registerBody, "$.refreshToken");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joueur1@test.cm\",\"password\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(playerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("joueur1"));

        // --- sans jeton / jeton invalide ---
        mockMvc.perform(get("/api/quizzes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/quizzes").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
        // un refresh token ne doit pas servir d'access token
        mockMvc.perform(get("/api/quizzes").header(HttpHeaders.AUTHORIZATION, bearer(refreshToken)))
                .andExpect(status().isUnauthorized());

        // --- refresh ---
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // --- permissions : un joueur ne crée pas de catégorie ---
        mockMvc.perform(post("/api/categories").header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Interdite\"}"))
                .andExpect(status().isForbidden());

        // --- questions ---
        mockMvc.perform(post("/api/questions").header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"label1":"Sans bonne réponse ?","responses":[
                                  {"text":"A","isValid":false},{"text":"B","isValid":false}]}
                                """))
                .andExpect(status().isBadRequest());

        String questionBody = mockMvc.perform(post("/api/questions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"label1":"Quelle est la capitale du Cameroun ?","time":"00:00:30",
                                 "responses":[{"text":"Douala","isValid":false},
                                              {"text":"Yaoundé","isValid":true},
                                              {"text":"Garoua","isValid":false}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responses", hasSize(3)))
                .andReturn().getResponse().getContentAsString();
        Integer questionId = JsonPath.read(questionBody, "$.idQuestion");

        // --- quiz ---
        String quizBody = mockMvc.perform(post("/api/quizzes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"Géographie du Cameroun","nbreQuestion":10,
                                 "totalScore":100,"scoreMin":50,
                                 "beginDate":"2026-12-01","endDate":"2026-12-31"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creatorPseudo").value("joueur1"))
                .andExpect(jsonPath("$.questionCount").value(0))
                .andReturn().getResponse().getContentAsString();
        Integer quizId = JsonPath.read(quizBody, "$.idQuiz");

        mockMvc.perform(post("/api/quizzes").header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"Dates inversées","beginDate":"2026-12-31","endDate":"2026-12-01"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/quizzes/" + quizId + "/questions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionIds\":[" + questionId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/quizzes/" + quizId).header(HttpHeaders.AUTHORIZATION, bearer(playerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionCount").value(1));

        // --- un autre joueur ne peut pas modifier le quiz ---
        String other = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pseudo\":\"joueur2\",\"email\":\"joueur2@test.cm\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String otherToken = JsonPath.read(other, "$.accessToken");

        mockMvc.perform(put("/api/quizzes/" + quizId).header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Piraté\"}"))
                .andExpect(status().isForbidden());

        // --- un joueur n'a pas quiz:delete ; l'admin si ---
        mockMvc.perform(delete("/api/quizzes/" + quizId).header(HttpHeaders.AUTHORIZATION, bearer(playerToken)))
                .andExpect(status().isForbidden());

        String adminBody = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@kmerquiz.test\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminToken = JsonPath.read(adminBody, "$.accessToken");

        mockMvc.perform(delete("/api/quizzes/" + quizId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/questions/" + questionId).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
