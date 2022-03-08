package com.fuadhasan.dsvdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fuad Hasan
 * @since 05-Mar-2022
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DsvdemoApplicationTests {

  DsvdemoApplication application;
  ClassLoader classLoader;

  String originOutputFileName = "JSONL_output.jsonl";

  @BeforeAll
  void readAndWriteTwoDifFile() {
    application = new DsvdemoApplication();
    classLoader = this.getClass().getClassLoader();
  }

  @Test
  @Order(1)
  void testFirstInputFile() throws IOException {
    var inputFileName = "DSV_input_1.txt";
    var fileDelimiter = ",";
    String outputFileName1 = "JSONL_output_test_1.jsonl";

    File inputFile = new File(classLoader.getResource(inputFileName).getFile());
    File originalOutputFile = new File(classLoader.getResource(originOutputFileName).getFile());

    application.readAndWriteFile(inputFile.getCanonicalPath(), fileDelimiter, outputFileName1);

    File outputFile = new File(outputFileName1);

    ObjectMapper objectMapper = new ObjectMapper();
    assertEquals(objectMapper.readTree(originalOutputFile), objectMapper.readTree(outputFile));
  }

  @Test
  @Order(2)
  void testSecondInputFile() throws IOException {
    var inputFileName = "DSV_input_2.txt";
    var fileDelimiter = "|";
    String outputFileName2 = "JSONL_output_test_2.jsonl";

    File inputFile = new File(classLoader.getResource(inputFileName).getFile());
    File originalOutputFile = new File(classLoader.getResource(originOutputFileName).getFile());

    application.readAndWriteFile(inputFile.getCanonicalPath(), fileDelimiter, outputFileName2);

    File outputFile = new File(outputFileName2);

    ObjectMapper objectMapper = new ObjectMapper();
    assertEquals(objectMapper.readTree(originalOutputFile), objectMapper.readTree(outputFile));
  }
}
