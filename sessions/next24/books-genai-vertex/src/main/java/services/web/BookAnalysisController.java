/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;

import services.actuator.StartupCheck;
import services.ai.VertexAIClient;
import services.domain.BooksService;
import services.utility.PromptUtility;
import services.web.data.BookRequest;

@RestController
@RequestMapping("/analysis")
public class BookAnalysisController {
  private static final Logger logger = LoggerFactory.getLogger(BookAnalysisController.class);

  private BooksService booksService;
  private VertexAIClient vertexAIClient;
  public BookAnalysisController(BooksService booksService, VertexAIClient vertexAIClient) {
    this.booksService = booksService;
    this.vertexAIClient = vertexAIClient;
  }

  @Value("${spring.cloud.config.modelAnalysisName}")
  private String model;

  @PostConstruct
  public void init() {
    logger.info("BookImagesApplication: BookAnalysisController Post Construct Initializer " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())));
    logger.info("BookImagesApplication: BookAnalysisController Post Construct - StartupCheck can be enabled");

    StartupCheck.up();
  }

  @PostMapping("")
  public ResponseEntity<String> processUserRequest(@RequestBody BookRequest bookRequest, @RequestParam(name = "contentCharactersLimit", defaultValue = "6000") Integer contentCharactersLimit){

    // Prompt AlloyDB for the embeddings for the book in the request
    List<Map<String, Object>> responseBook = booksService.prompt(bookRequest, contentCharactersLimit);

    // build prompt to query LLM with the augmented context
    String promptWithContext = PromptUtility.formatPromptBookAnalysis(responseBook, bookRequest);

    System.out.println("Model: " + model);
    // submit prompt to the LLM via framework
    String response = vertexAIClient.promptModel(promptWithContext, model);

    // return the response to the caller
    return new ResponseEntity<String>(response, HttpStatus.OK);
  }
}
