package com.fuadhasan.dsvdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fuad Hasan
 * @since 05-Mar-2022
 */
@Slf4j
@SpringBootApplication
public class DsvdemoApplication {

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static void main(String[] args) {
    SpringApplication.run(DsvdemoApplication.class, args);
    if (args == null || args.length < 1) {
      log.info("Please pass input file name in cmd");
      return;
    }
    var inputFileName = args[0];
    var fileDelimiter = ",";

    if (args.length > 1) {
      fileDelimiter = args[1];
    }
    var application = new DsvdemoApplication();
    var outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf(".")) + ".jsonl";
    application.readAndWriteFile(inputFileName, fileDelimiter, outputFileName);
  }

  public void readAndWriteFile(String inputFileName, String fileDelimiter, String outputFileName) {
    log.info(
        "InputFileName: {}, OutputFileName: {}, FileDelimiter: {}",
        inputFileName,
        outputFileName,
        fileDelimiter);

    try {
      Files.deleteIfExists(new File(outputFileName).toPath());
    } catch (IOException e) {
      log.error("An exception occurred!", e);
    }

    fileDelimiter = String.format("\\%s", fileDelimiter);
    var regex = fileDelimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    // log.info("Regex: {}", regex);

    var mapper = new ObjectMapper();
    mapper.configOverride(String.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.ANY));
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    File inputFile = new File(inputFileName);
    try (var bufferedReader =
        new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8))) {
      String line;
      List<String> headerNames = new ArrayList<>();
      if ((line = bufferedReader.readLine()) != null) {
        headerNames = Arrays.stream(line.split(regex, -1)).collect(Collectors.toList());
      }

      while ((line = bufferedReader.readLine()) != null) {
        var itemDataList = Arrays.stream(line.split(regex, -1)).collect(Collectors.toList());

        Map<String, Object> dataMap = new HashMap<>();
        for (int i = 0; i < headerNames.size(); i++) {
          String key = headerNames.get(i);
          String value = itemDataList.get(i);
          if (key.contains("date")) {
            value = convertFormattedDateStr(value);
            dataMap.put(key, value);
          } else {
            if (StringUtils.isNumeric(value)) {
              dataMap.put(key, Integer.parseInt(value));
            } else {
              value = value.replace("\"", "");
              value = value.replace(" |", "");
              value = value.replace("|", ",");
              if (value.endsWith(",")) {
                value = value.substring(0, value.length() - 1);
              }
              value = StringUtils.trimToEmpty(value);
              dataMap.put(key, value);
            }
          }
        }
        var jsonString = mapper.writeValueAsString(dataMap);
        // log.info("{}", jsonString);

        writeIntoJsonLFile(outputFileName, jsonString);
      }
    } catch (IOException e) {
      log.error("An exception occurred!", e);
    }
  }

  private String convertFormattedDateStr(String value) {
    // log.info("DateStr: {}", value);
    LocalDate localDate;
    try {
      localDate = LocalDate.parse(value);
    } catch (Exception e) {
      var df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
      try {
        localDate = LocalDate.parse(value, df);
      } catch (Exception e1) {
        df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
          localDate = LocalDate.parse(value, df);
        } catch (Exception e2) {
          df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
          localDate = LocalDate.parse(value, df);
        }
      }
    }
    if (localDate != null) {
      value = localDate.format(dateTimeFormatter);
    }
    return value;
  }

  private void writeIntoJsonLFile(String outputFileName, String data) {
    // log.info("OutputFileName: {}, Data: {}", outputFileName, data);

    File outputFile = new File(outputFileName);
    try (var printWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)))) {
      printWriter.println(data);
      printWriter.flush();
    } catch (IOException e) {
      log.error("An exception occurred!", e);
    }
  }
}
