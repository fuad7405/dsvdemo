package com.fuadhasan.dsvdemo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.*;
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
    var dataList = application.readTextFile(inputFileName, fileDelimiter);
    var outputFileName = "JSONL_output.jsonl";
    application.writeIntoJsonLFile(outputFileName, dataList);
  }

  public List<String> readTextFile(String inputFileName, String fileDelimiter) {
    log.info("InputFileName: {}, FileDelimiter: {}", inputFileName, fileDelimiter);

    List<String> dataList = new ArrayList<>();

    fileDelimiter = String.format("\\%s", fileDelimiter);
    var regex = fileDelimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    // log.info("Regex: {}", regex);

    var mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    try (var bufferedReader = new BufferedReader(new FileReader(inputFileName))) {
      String line;
      List<String> headerNames = new ArrayList<>();
      if ((line = bufferedReader.readLine()) != null) {
        headerNames = Arrays.stream(line.split(regex, -1)).collect(Collectors.toList());
      }

      while ((line = bufferedReader.readLine()) != null) {
        var itemDataList = Arrays.stream(line.split(regex, -1)).collect(Collectors.toList());

        Map<String, String> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < headerNames.size(); i++) {
          String key = headerNames.get(i);
          String value = itemDataList.get(i);
          if (key.contains("date")) {
            value = convertFormattedDateStr(value);
          } else {
            value = value.replace("\"", "");
            value = value.replace("|", "");
            value = value.replace(",", "");
            value = StringUtils.trimWhitespace(value);
          }
          dataMap.put(key, value);
        }
        var jsonString = mapper.writeValueAsString(dataMap);
        // log.info("{}", jsonString);
        dataList.add(jsonString);
      }
    } catch (IOException e) {
      log.error("An exception occurred!", e);
    }

    return dataList;
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

  public void writeIntoJsonLFile(String outputFileName, List<String> dataList) {
    log.info("OutputFileName: {}, DataSize: {}", outputFileName, dataList.size());

    try (var bufferedWriter =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)))) {
      dataList.forEach(
          data -> {
            try {
              bufferedWriter.write(data);
              bufferedWriter.newLine();
            } catch (IOException e) {
              log.error("An exception occurred!", e);
            }
          });
    } catch (IOException e) {
      log.error("An exception occurred!", e);
    }
  }
}
