package com.fuadhasan.dsvdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

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
public class DsvdemoApplication {

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static void main(String[] args) {
    if (args == null || args.length < 1) {
      System.err.println("Please pass input file name in cmd");
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
    System.out.println(
        "InputFileName: "
            + inputFileName
            + ", OutputFileName: "
            + outputFileName
            + ", FileDelimiter: "
            + fileDelimiter);

    if (StringUtils.isBlank(inputFileName)
        || StringUtils.isBlank(fileDelimiter)
        || StringUtils.isBlank(outputFileName)) {
      System.err.println("Please pass InputFileName, OutputFileName and FileDelimiter");
      return;
    }

    try {
      Files.deleteIfExists(new File(outputFileName).toPath());
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
    }

    fileDelimiter = String.format("\\%s", fileDelimiter);
    var regex = fileDelimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    // System.out.println("Regex: " + regex);

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
        // System.out.println(jsonString);

        writeIntoJsonLFile(outputFileName, jsonString);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
    }
    System.out.println("Converts DSV files into JSONL formatted file.");
  }

  private String convertFormattedDateStr(String value) {
    // System.out.println("DateStr: " + value);
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
    // System.out.println("OutputFileName: " + outputFileName + ", Data: " + data);

    File outputFile = new File(outputFileName);
    try (var printWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)))) {
      printWriter.println(data);
      printWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(e);
    }
  }
}
