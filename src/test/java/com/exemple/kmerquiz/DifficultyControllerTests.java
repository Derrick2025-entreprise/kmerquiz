package com.exemple.kmerquiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class DifficultyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void difficultyCrudAndErrorResponses() throws Exception {
        createDifficulty("Facile");
        createDifficulty("Moyen");
        createDifficulty("Difficile");

        mockMvc.perform(post("/api/difficulties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Facile\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("existe déjà")));

        mockMvc.perform(post("/api/difficulties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.label").exists());

        mockMvc.perform(get("/api/difficulties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(put("/api/difficulties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Très facile\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Très facile"));

        mockMvc.perform(get("/api/difficulties/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("non trouvée")));
    }

    private void createDifficulty(String label) throws Exception {
        mockMvc.perform(post("/api/difficulties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"" + label + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDifficulty").exists())
                .andExpect(jsonPath("$.label").value(label));
    }
}
