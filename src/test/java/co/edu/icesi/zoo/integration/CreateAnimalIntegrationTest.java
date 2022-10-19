package co.edu.icesi.zoo.integration;

import co.edu.icesi.zoo.constant.AnimalErrorCode;
import co.edu.icesi.zoo.constant.AnimalErrorMsgs;
import co.edu.icesi.zoo.constant.AnimalTestConstants;
import co.edu.icesi.zoo.constant.BurmesePython;
import co.edu.icesi.zoo.dto.AnimalDTO;
import co.edu.icesi.zoo.error.exception.AnimalError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
public class CreateAnimalIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

    }

    //Crear consultar listar


    @SneakyThrows
    @Test
    public void createAnimalTest(){

        String body = parseResourceToString("createAnimal.json");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isOk())
                .andReturn();

        AnimalDTO animalDTO = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalDTO.class);

        assertThat(animalDTO, hasProperty(BurmesePython.NAME_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_NAME)));
        assertThat(animalDTO, hasProperty(BurmesePython.SEX_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_SEX)));
        assertThat(animalDTO, hasProperty(BurmesePython.WEIGHT_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_WEIGHT)));
        assertThat(animalDTO, hasProperty(BurmesePython.HEIGHT_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_HEIGHT)));
        assertThat(animalDTO, hasProperty(BurmesePython.AGE_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_AGE)));
        assertThat(animalDTO, hasProperty(BurmesePython.ARRIVAL_DATE_ATTRIBUTE, is(AnimalTestConstants.ANIMAL_TEST_ARRIVAL_DATE)));
        assertThat(animalDTO, hasProperty(BurmesePython.MOTHER_ID, is(AnimalTestConstants.ANIMAL_TEST_MOTHER_ID)));
        assertThat(animalDTO, hasProperty(BurmesePython.FATHER_ID, is(AnimalTestConstants.ANIMAL_TEST_FATHER_ID)));

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidNameFormatTest(){

        //Set an invalid name format
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setName(animalDTO.getName()+"-Burmese");
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_NAME_FORMAT_MSG, AnimalErrorCode.CODE_01, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidNameLengthTest(){

        //Set an invalid name length
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setName("Asmodeussssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_NAME_FORMAT_MSG, AnimalErrorCode.CODE_01, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalImpossibleArrivalDateTest(){

        long futureArrivalTime = System.currentTimeMillis() + 86400000L;
        LocalDateTime futureArrivalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(futureArrivalTime), ZoneId.systemDefault());

        //Set an invalid arrival date
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setArrivalDate(futureArrivalDate.toString());
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.IMPOSSIBLE_DATE_MSG, AnimalErrorCode.CODE_02, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidDateFormatTest(){

        //Set an invalid arrival date format (not ISO 8601)
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setArrivalDate("2022.10.27 14:15pm");
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_DATE_FORMAT_MSG, AnimalErrorCode.CODE_02, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidAgeTest(){

        //Set an invalid age (age>30)
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setAge(31.0);
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_PYTHON_CHARACTERISTICS_MSG, AnimalErrorCode.CODE_03, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidHeightTest(){

        //Set an invalid height (height>8)
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setHeight(9.0);
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_PYTHON_CHARACTERISTICS_MSG, AnimalErrorCode.CODE_03, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidWeightTest(){

        //Set an invalid weight (weight>180)
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setWeight(181.0);
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.WRONG_PYTHON_CHARACTERISTICS_MSG, AnimalErrorCode.CODE_03, animalError);

    }

    @SneakyThrows
    @Test
    public void createAnimalInvalidParentUUIDFormat(){

        //Set an invalid weight (weight>180)
        AnimalDTO animalDTO = baseAnimal();
        animalDTO.setMotherId("a00347293");
        String body = objectMapper.writeValueAsString(animalDTO);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andExpect(status().isBadRequest())
                .andReturn();

        AnimalError animalError = objectMapper.readValue(result.getResponse().getContentAsString(), AnimalError.class);
        verifyAnimalError(AnimalErrorMsgs.INVALID_ID, AnimalErrorCode.CODE_06, animalError);

    }






    /*
     * UTILS
     */
    @SneakyThrows
    private AnimalDTO baseAnimal(){
        String body = parseResourceToString("createAnimal.json");
        return objectMapper.readValue(body, AnimalDTO.class);
    }
    @SneakyThrows
    private String parseResourceToString(String classpath) {
        Resource resource = new ClassPathResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public void verifyAnimalError(String correctMSG, AnimalErrorCode correctCode, AnimalError animalError) {

        assertNotNull(animalError);
        assertEquals(correctMSG, animalError.getMessage());
        assertEquals(correctCode, animalError.getCode());

    }


}
