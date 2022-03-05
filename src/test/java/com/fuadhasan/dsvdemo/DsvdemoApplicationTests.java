package com.fuadhasan.dsvdemo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

  @BeforeAll
  void readAndWriteTwoDifFile() {
    var inputFileName = "DSV input 1.txt";
    var fileDelimiter = ",";
    readAndWriteFile(inputFileName, fileDelimiter, outputFileName1);

    inputFileName = "DSV input 2.txt";
    fileDelimiter = "|";
    readAndWriteFile(inputFileName, fileDelimiter, outputFileName2);
  }

  @Test
  @Order(1)
  void testTwoOutputFile() throws IOException {
    File outputFile1 = new File(outputFileName1);
    File outputFile2 = new File(outputFileName2);
    Assertions.assertEquals(
        Files.readAllBytes(outputFile1.toPath()).length,
        Files.readAllBytes(outputFile2.toPath()).length);

    // Files.deleteIfExists(outputFile1.toPath());
    // Files.deleteIfExists(outputFile2.toPath());
  }

  private void readAndWriteFile(String inputFileName, String fileDelimiter, String outputFileName) {
    var dataList = application.readTextFile(inputFileName, fileDelimiter);

    application.writeIntoJsonLFile(outputFileName, dataList);
  }
}
