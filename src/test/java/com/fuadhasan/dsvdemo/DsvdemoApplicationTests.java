package com.fuadhasan.dsvdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

/**
 * @author Fuad Hasan
 * @since 05-Mar-2022
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DsvdemoApplicationTests {

  DsvdemoApplication application = new DsvdemoApplication();

  String outputFileName1 = "JSONL_output_test_1.jsonl";
  String outputFileName2 = "JSONL_output_test_2.jsonl";
  String originOutputFileName = "JSONL_output.jsonl";

  ClassLoader classLoader = this.getClass().getClassLoader();

  @BeforeAll
  void readAndWriteTwoDifFile() {}

  @Test
  @Order(1)
  void testFirstInputFile() throws IOException {
    var inputFileName = "DSV_input_1.txt";
    var fileDelimiter = ",";

    File inputFile = new File(classLoader.getResource(inputFileName).getFile());
    File originalOutputFile = new File(classLoader.getResource(originOutputFileName).getFile());

    application.readAndWriteFile(inputFile.getCanonicalPath(), fileDelimiter, outputFileName1);

    File outputFile = new File(outputFileName1);

    ObjectMapper objectMapper = new ObjectMapper();
    Assertions.assertEquals(
        objectMapper.readTree(originalOutputFile), objectMapper.readTree(outputFile));
  }

  @Test
  @Order(2)
  void testSecondInputFile() throws IOException {
    var inputFileName = "DSV_input_2.txt";
    var fileDelimiter = "|";

    File inputFile = new File(classLoader.getResource(inputFileName).getFile());
    File originalOutputFile = new File(classLoader.getResource(originOutputFileName).getFile());

    application.readAndWriteFile(inputFile.getCanonicalPath(), fileDelimiter, outputFileName2);

    File outputFile = new File(outputFileName2);

    ObjectMapper objectMapper = new ObjectMapper();
    Assertions.assertEquals(
        objectMapper.readTree(originalOutputFile), objectMapper.readTree(outputFile));
  }

  @Test
  @Order(3)
  void testTwoOutputFile() throws IOException {
    File outputFile1 = new File(outputFileName1);
    File outputFile2 = new File(outputFileName2);

    ObjectMapper objectMapper = new ObjectMapper();
    Assertions.assertEquals(objectMapper.readTree(outputFile1), objectMapper.readTree(outputFile2));
  }
}
