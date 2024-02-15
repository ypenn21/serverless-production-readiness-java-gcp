# Test-driven Development with Duet for Java & Spring Boot Applications in GCP
Many times, as a developer, I have been asked to quickly add business functionality to an existing app. 
As I might not be familiar with the app, I am wondering if Duet could help me gain an understanding of the 
app, review the code, then help add new functionality.

I like to follow a test-driven development approach, and I want to explore how Duet can help me in my work.

## What's the Ask
The Quotes app is missing an endpoint to retrieve book information by book name.
This endpoint should respond on the “/quotes/book/{book}” path. 
The ask is to implement this endpoint.

## Before you start
I'll be using Duet with Cloud Code in IntelliJ on my Mac, however you can use Duet from other IDEs supported by Cloud Code, 
such as VSCode. If you prefer an online experience, you can use it in the Cloud Shell Editor or Cloud Workstations 
from Google Cloud Console. 

The [Duet](https://cloud.google.com/Duet-ai/docs/developers/overview) page provides details on how to get setup with Duet. 

Once we have Duet set up, we'll open a Terminal window and install the pre-requisites for the work.

The Quotes application uses Java 21. If you do not have it installed, it's an easy setup using [SDKMan](https://sdkman.io/):
```shell
curl -s "https://get.sdkman.io" | bash && source "$HOME/.sdkman/bin/sdkman-init.sh"
```
Install the latest OpenJDK and confirm it as the default JDK in the install:
```shell
sdk install java 21.0.2-tem && sdk use java 21.0.2-tem && java -version
```

With Java installed, if you wish to test the service in Cloud Run when done, enable the APIs you would need to deploy the Quotes app in Cloud Run, if not already installed:
```shell
gcloud services enable cloudaicompanion.googleapis.com
gcloud services enable cloudbuild.googleapis.com 
gcloud services enable run.googleapis.com
gcloud services enable logging.googleapis.com 
```
The Quotes app
Let's clone the Github repo for Quotes and switch to the /services/quotes folder, then open the codebase in the IDE, as well as a terminal window:
```shell
git clone https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp.git && cd serverless-production-readiness-java-gcp/services/quotes
```
We can validate that the project is set; and set it otherwise:
```shell
# get project 
gcloud config list
# set project
gcloud config set project [PROJECT_ID]
```
We then check that the Quotes app is building correctly and can be tested:
```shell
./mvnw package spring-boot:run
```
From a Terminal window, we execute:
```shell
curl localhost:8083/random-quote -w "\n"
```

## Explain the app
Duet can be used to explain the app, perform code reviews, suggest changes and tests

#### Use Duet to explain the Quotes app
Let's open the QuoteApplication class, then type the following prompt in the Duet Chat window and observe the response:

__Prompt__
```
I want to get details about the QuotesApplication; please provide a detailed overview of the QuotesApplication
```
__Response__

Duet will respond with a detailed overview of the QuotesApplication. Remember that responses are non-deterministic, 
therefore prompt responses provided in this blog post should be taken as an example and could vary slightly.
```
Sure. The QuotesApplication is a Spring Boot application that provides a REST API for managing quotes. The application is divided into several components:
* The QuoteController class handles requests for quotes.
* The QuoteService class provides business logic for managing quotes.
* The QuoteRepository class persists quotes to a database.
The QuotesApplication can be deployed to a variety of platforms, including Cloud Run, GKE.

Here is a more detailed overview of each component:
* The QuoteController class handles requests for quotes. The controller has several methods, including:
    * getQuote(): Returns a random quote.
    * getQuotes(): Returns all quotes.
    * getQuoteByAuthor(): Returns all quotes by a given author.
    * createQuote(): Creates a new quote.
    * updateQuote(): Updates an existing quote.
    * deleteQuote(): Deletes an existing quote.
* The QuoteService class provides business logic for managing quotes. The service has several methods, including:
    * getQuote(): Returns a random quote.
    * getQuotes(): Returns all quotes.
    * getQuoteByAuthor(): Returns all quotes by a given author.
    * createQuote(): Creates a new quote.
    * updateQuote(): Updates an existing quote.
    * deleteQuote(): Deletes an existing quote.
* The QuoteRepository class persists quotes to a database. The repository has several methods, including:
    * save(): Saves a quote to the database.
    * findAll(): Returns all quotes from the database.
    * findByAuthor(): Returns all quotes by a given author from the database.
    * delete(): Deletes a quote from the database.
```

We might want to review the code in the QuoteController before adding an endpoint. Let's open the QuoteController class, 
then type the following prompt in the Duet chat window:
```
Please perform a detailed code review of the QuoteController and suggest any improvements I can bring to the code
```
Duet will respond with code review items and actually provide suggestions for how to rewrite the code.
```shell
Sure, I can help you with that. Here are a few suggestions for improving the code in QuoteController:
* Use more descriptive variable names. For example, instead of using quotes, you could use allQuotes or listOfQuotes. This would make the code easier to read and understand.
* Use more comments to explain the code. This would help other developers understand what the code is doing and how it works.
* Use a more consistent coding style. For example, you could use the same indentation for all code blocks. This would make the code look more organized and easier to read.
* Use a linter to check for errors and inconsistencies. A linter can help you identify errors and inconsistencies in your code, which can help you improve its quality.
* Test your code regularly. This will help you catch bugs early and prevent them from causing problems in production.

Here is an example of how the code could be improved:
...
```

## Use TDD with Duet to add business logic

The Quotes app is missing an endpoint to retrieve book information by book name. This endpoint should respond on the “/quotes/book/{book}” path. 
Let’s use a test-driven approach to add this functionality, starting from writing an application test.

Start by asking Duet to recommend which types of tests you need to write:
> Which types of tests should I be writing for the QuoteController

DuetAI will reply with a list including:
```shell
There are many different types of tests that you can write for the QuoteController class. Some of the most important types of tests include:
* Unit tests: Unit tests are designed to test the individual components of a program. In the case of the QuoteController class, you could write unit tests to test the following methods:
  * getAllQuotes()
  * getQuoteById()
  * createQuote()
  * updateQuote()
  * deleteQuote()
* Integration tests: Integration tests are designed to test the interactions between different components of a program. In the case of the QuoteController class, you could write integration tests to test the following:
  * The interaction between the QuoteController class and the QuoteService class
  * The interaction between the QuoteController class and the Quote class
* System tests: System tests are designed to test the entire system, including all of its components and interactions. In the case of the QuoteController class, you could write system tests to test the following:
  * The ability of the QuoteController class to handle a variety of user requests
  * The performance of the QuoteController class
  * The reliability of the QuoteController class
In addition to these types of tests, you may also want to write some security tests to ensure that the QuoteController class is secure from attacks.

Here are some additional tips for writing tests for the QuoteController class:
* Use a test framework that supports Java. There are many different test frameworks available, such as JUnit, TestNG, and Mockito.
* Write your tests in a modular way. This will make it easier to maintain and update your tests in the future.
* Use descriptive test names. This will make it easier to understand what your tests are doing.
* Use assertions to verify the results of your tests. This will help you to catch errors early and prevent them from causing problems in production.
By following these tips, you can write comprehensive and effective tests for the QuoteController class.
```

We might be wondering whether we should also ask network tests, and can prompt Duet for it:
```Should I add tests for network failures```

Duet will provide a detailed response starting along the lines of:
```
Yes, you should add tests for network failures. Network failures can occur for a variety of reasons, such as:
* The client or server is offline
* The network connection is interrupted
* The network is congested
...
```

Let's open the **_/src/test/java/com/example/quotes_** folder and observe that several test classes have already been created:
* QuotesControllerTest
* QuotesRepositoryTest
* QuotesApplicationNetworkFailuresTests

To add the quote retrieval by book name functionality, we start writing code in true TDD fashion by adding tests 
to both the QuotesControllerTest (for the endpoint) and QuotesRepositoryTest (for data retrieval from the db).

**Step 1: Generate the web controller test first**
Open the **QuotesControllerTest** class in the **_com.example.quotes.web_** package

In the code, we add the comment, say towards the end of the file and press Enter
```
// Answer as a Software Engineer with expertise in Java. Create a test for the QuotesController for a method getByBook which responds at the HTTP endpoint /quotes/book/{book} and retrieves a quote from the book The Road
```
We then click the Duet SmartAction and select “Generate code” or use keys to generate cod: (CTRL+Enter in VSCode, OPTION+Shift+\ in IntelliJ).
Duet will make the suggestion:
```shell
  @Test
  @DisplayName("Test returns Quotes by Book")
  void shouldReturnQuoteByBook() throws Exception {
    mockMvc.perform(get("/quotes/book/The Road"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].book", Matchers.equalTo("The Road")));
  }
```
We can accept the suggestion, if it meets our requirements, with Tab or click Accept.

In the Terminal window, run the command:
```
./mvnw clean verify
```
We observe that the test fails, as expected, with a ‘404’ error, as the business logic has not been implemented:
```
[ERROR] Failures: 
[ERROR]   QuotesControllerTest.shouldReturnQuoteByBook:94 Status expected:&lt;200> but was:&lt;404>
...
```
**Step 2: Generate controller code**

Let’s add the missing controller method getByBook. Open the QuoteController class. Add the following comment towards the end of the class:
```
// generate a getByBook method which responds at the HTTP endpoint /quotes/book/{book} and retrieves a quote by book name; use the QuoteService class to retrieve the book by name, as a String
```

Duet will respond with a code block along the lines of:
```
    @GetMapping("/quotes/book/{book}")
    public ResponseEntity<List<Quote>> quoteByBook(@PathVariable("book") 
                String book) {
        try {
            List<Quote> quotes = quoteService.getByBook(book);

            if(!quotes.isEmpty()){
                return new  ResponseEntity<List<Quote>>(quotes, 
                                                        HttpStatus.OK);
            } else {
                return 
                   new ResponseEntity<List<Quote>>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 
          new ResponseEntity<List<Quote>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
```
Note that the code is missing the ‘getByBook()’ implementation in the QuoteService class, which leads you to the next step in the implementation.

**Step 3: Generate now repository access methods, starting again with a test**

Let's open the QuoteService class and observe that the getByBook method is missing. 
We generate a test for the service class first, then add methods to access the repository, and test it out.

Let's open the QuotesRepositoryTest class and add the following comment towards the bottom of the class:
```
// generate a unit test for the getByBook method in the QuoteService; create a Quote in the QuoteService first then test the getByBook method against the new Quote
```
Observe that the generated code looks like:
```
  @Test
  @DisplayName("Get a quote by book - failed")
  void testGetQuoteByBookFailed(){
    var quotes = this.quoteService.getByBook("The Glass Menagerie");
    assertThat(quotes).isEmpty();
  }
```
With the test implemented, it is time to implement the missing functionality in the QuoteRepository and the QuoteService class.

We want to get a Quote by the book name, therefore need to add a findByBook method to the JPA repository class QuoteRepository, then allow the QuoteService to use this method. 
Open the QuoteRepository class and add this comment towards the end of the class:
```
// generate a find by book method which retrieves a quote by book name; use the native query syntax
```
Duet will generate code along the lines of:
```
  @Query( nativeQuery = true, value =
            "SELECT id,quote,author,book FROM quotes WHERE book = :book")
    List<Quote> findByBook(String book);
```

With the repository method in place, we can generate the missing link, the getByBook method in the service class and test it out. 
Open the QuoteService class and add the comment:
```
// add get by book method, use the QuoteRepository
```
Duet will generate code along the lines of:
```
  public List<Quote> getByBook(String book) {
    return quoteRepository.findByBook(book);
  }
```
**Done!** with coding, let’s test the result.

## Test your code
We'll run ALL tests from the Terminal:
```
./mvnw verify
```
Quotes app can be started with the following command:
```
./mvnw spring-boot:run
```
Ask Duet to generate cURL commands to test the newly added functionality. Switch to a different terminal window to run a cURL command.

In the Duet chat window, we can prompt Duet to generate a test command:
```
Generate a curl command for the /quotes/book endpoint for a local environment at port 8083 for the book "The Road"
```

Duet will generate the cURL command, which we can run:
```
curl -X GET http://localhost:8083/quotes/book/The%20Lord%20of%20the%20Rings
```

Assume the command has not found a book and we wish to print the HTTP error code; prompt Duet with:
```
Update the curl command to print the HTTP response code
```
Run the updated command generated by Duet, which should return a 404:
```
curl -X GET http://localhost:8083/quotes/book/The%20Lord%20of%20the%20Rings -o /dev/null -s -w '%{http_code}\n'
```
Now update the prompt to generate a successful command:
```
Update the command again to use the book "The Road"
```
Run the updated command generated by Duet, which should return a 200:
```
curl -X GET http://localhost:8083/quotes/book/The%20Road -o /dev/null -s -w '%{http_code}\n'
```
**Done!** with testing.

## Test our code in the Google Cloud Run
If we wish to test our code in Cloud Run, we can follow the detailed Build and Deploy instructions in the source code's [README](https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp/tree/main/services/quotes#readme)

## What have we learned today
In this blog post, I started from an existing app and used Duet to help me get started with an overview of the codebase, 
perform code reviews, generate test code and business logic at different functionality levels and, last but not least, generate 
cURL commands to test the added functionality. All of this, following my preferred test-driven development approach.

It saved me time not only in generating code, but in the constant availability to generate the right cURL commands for testing when I forgot a parameter or suggesting areas of code improvements.
It doesn't only save me time and search effort, but removes all these small interruptions from my workflow.

For any questions or feedback, feel free to contact me on Twitter/X [@ddobrin](https://twitter.com/ddobrin).
