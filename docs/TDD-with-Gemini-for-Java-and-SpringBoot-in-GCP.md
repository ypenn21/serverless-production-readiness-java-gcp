# Test-driven Development with Gemini for Java & Spring Boot Applications in GCP
Many times, as a developer, I have been asked to quickly add business functionality to an existing app. 
As I might not be familiar with the app, I am wondering if Gemini could help me gain an understanding of the 
app, review the code, then help me add the new functionality.

I like to follow a test-driven development approach, and I want to explore how Gemini can help me in my work.

## What's the Ask
The Quotes app is missing an endpoint to retrieve book information by book name.
This endpoint should respond on the “/quotes/book/{book}” path. 
The ask is to implement this endpoint using Gemini AI and follow a TDD approach.

## Before you start
I'll be using Gemini AI with Cloud Code in IntelliJ on my Mac, however you can use Gemini AI from other IDEs supported by Cloud Code, 
such as VSCode. If you prefer an online experience, you can use it in the Cloud Shell Editor or Cloud Workstations 
from Google Cloud Console. 

The [Gemini](https://cloud.google.com/duet-ai/docs/developers/overview) page provides details on how to get setup with Gemini. 

Once you have Gemini set up, open a Terminal window and install the pre-requisites for your work.

The Quotes application uses Java 21. If you do not have it installed, it's an easy setup using [SDKMan](https://sdkman.io/):
```shell
curl -s "https://get.sdkman.io" | bash && source "$HOME/.sdkman/bin/sdkman-init.sh"
```
Install the latest OpenJDK and confirm it as the default JDK in the install:
```shell
sdk install java 21.0.2-tem && sdk use java 21.0.2-tem && java -version
```

With Java installed, enable the APIs you would need to deploy the Quotes app in Cloud Run, if not already installed:
```shell
gcloud services enable cloudaicompanion.googleapis.com
gcloud services enable cloudbuild.googleapis.com 
gcloud services enable run.googleapis.com
gcloud services enable logging.googleapis.com 
```
The Quotes app
Clone the Github repo for Quotes and switch to the /services/quotes folder, then open the codebase in the IDE, as well as a terminal window:
```shell
git clone https://github.com/GoogleCloudPlatform/serverless-production-readiness-java-gcp.git && cd serverless-production-readiness-java-gcp/services/quotes
```
Validate that your project is set; set it otherwise:
```shell
# get project 
gcloud config list
# set project
gcloud config set project [PROJECT_ID]
```
Check that the Quotes app is building correctly and can be tested:
```shell
./mvnw package spring-boot:run
```
From a Terminal window, execute:
```shell
curl localhost:8083/random-quote -w "\n"
```

## Use Gemini to explain the Quotes app, perform code reviews, suggest changes and tests

### Use Gemini to explain the Quotes app
Open the QuoteApplication class, then type the following prompt in the Gemini Chat window and observe the response:

__Prompt__
> I want to get details about the QuotesApplication; please provide a detailed overview of the QuotesApplication

__Response__

Gemini will respond with a detailed overview of the QuotesApplication. Remember that responses  are non-deterministic, 
therefore prompt responses provided in this blog post should be taken as an example.

> Sure. The QuotesApplication is a Spring Boot application that provides a REST API for managing quotes. 
> The application is divided into several components:
> - The QuoteController class handles requests for quotes. 
> - The QuoteService class provides business logic for managing quotes. 
> - The QuoteRepository class persists quotes to a database.

> The QuotesApplication can be deployed to a variety of platforms, including Cloud Run and GKE.
Here is a more detailed overview of each component:
> The QuoteController class handles requests for quotes.
> 
> ...

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

# Task 4. Following **TDD**, use DuetAI to add business logic

The Quotes app is missing an endpoint to retrieve book information by book name.

This endpoint should respond on the “/quotes/book/{book}” path. You are being asked to implement it, with the associated business logic.

Let’s use a test-driven approach to add this functionality, starting from writing an application test.

Start by asking Duet to recommend which types of tests you need to write:


```
 Which types of test should I be writing for the QuoteController
```


DuetAI will reply with a list including:



* Unit tests
* Integration tests
* System tests
* Acceptance tests

You might be wondering whether you should also ask network tests, and you can prompt Duet for it:


```
Should I add tests for network failures
```


Duet will provide a detailed response starting along the lines of:


    Yes, you should add tests for network failures. Network failures can occur for a variety of reasons, such as:



* The client or server is offline.
* The network connection is interrupted.
* The network is congested.
* The network is being attacked.

  ….


Open the **_/src/test/java/com/example/quotes_** folder and observe that several test classes have already been created:



* QuotesControllerTest
* QuotesRepositoryTest
* QuotesApplicationNetworkFailuresTests

To add the quote retrieval by book name functionality, you start writing code in true TDD fashion by adding tests to both the QuotesControllerTest (for the endpoint) and QuotesRepositoryTest (for data retrieval from the db).

**Step 1: Generate test first**

Open the **QuotesControllerTest** class in the **_com.example.quotes.web_** package

In the code,and add the comment, say towards the end of the file and press Enter


```
// Answer as a Software Engineer with expertise in Java. Create a test for the QuotesController for a method getByBook which responds at the HTTP endpoint /quotes/book/{book} and retrieves a quote from the book The Road
```


You can click the DuetAI:Smart Action:



And select “Generate code”:


![alt_text](images/image25.png "image_tooltip")


…or use the key combination: CTRL+Enter

Duet will make the suggestion:




![alt_text](images/image26.png "image_tooltip")


You can accept the suggestion, if it meets your requirements, with Tab or click Accept.

In the Terminal window, run  the command:


```
./mvnw clean verify
```


Observe that the test fails, as expected, with a ‘404’ error, as the business logic has not been implemented:


    [ERROR] Failures: 


    [ERROR]   QuotesControllerTest.shouldReturnQuoteByBook:94 Status expected:&lt;200> but was:&lt;404>


    [INFO] 


    [ERROR] Tests run: 15, Failures: 1, Errors: 0, Skipped: 0

**Step 2: Generate controller code**

Let’s add the missing controller method getByBook. Open the QuoteController class.

Add the following comment towards the end of the class:


```
// generate a getByBook method which responds at the HTTP endpoint /quotes/book/{book} and retrieves a quote by book name; use the QuoteService class to retrieve the book by name, as a String
```


Duet will respond with a code block along the line of:


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

Open the QuoteService class and observe that the getByBook method is missing.

Let’s generate a test for the service class, add methods to access the repository, then test it out

Open the QuotesRepositoryTest class and add the following comment towards the bottom of the class:


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


With the tests implemented, it is time to implement the missing functionality in the QuoteRepository and the QuoteService class.

You want to get a Quote by the book name, therefore you need to add a findByBook method to the JPA repository class QuoteRepostiory, then allow the QuoteService to use this method

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


With the repository method in place, generate the missing link, the getByBook method in the service class and test it out. Open the QuoteService class and add the comment:


```
// add get by book method, use the QuoteRepository
```


Duet will generate code along the lines of:


```
  public List<Quote> getByBook(String book) {
    return quoteRepository.findByBook(book);
  }
```


**Done! **With coding, let’s test the result.

Right-click in the QuotesRepositoryTest and 'Run Tests'

Right-click in the QuotesControllerTest class and ‘Run Tests’

Run ALL tests from the Terminal:


```
./mvnw verify
```




Ask Duet to generate cURL commands to test the newly added functionality:

Start the app with


```
./mvnw spring-boot:run
```


Switch to a different terminal window to run a cURL command.

In the Duet AI chat window, prompt Duet to generate a test command


```
generate a curl command for the /quotes/book endpoint for a local environment at port 8083 for the book "The Road"
```


Duet will generate the cURL command, which you can run:


```
curl -X GET http://localhost:8083/quotes/book/The%20Lord%20of%20the%20Rings
```


Assume the command has not found a book and we wish to print the HTTP error code; prompt Duet with:


```
update the curl command to print the HTTP response code
```


Run the updated command generated by Duet, which should return a 404:


```
curl -X GET http://localhost:8083/quotes/book/The%20Lord%20of%20the%20Rings -o /dev/null -s -w '%{http_code}\n'
```


Now update the prompt to generate a successful command:


```
update the command again to use the book "The Road"
```


Run the updated command generated by Duet, which should return a 404:


```
curl -X GET http://localhost:8083/quotes/book/The%20Road -o /dev/null -s -w '%{http_code}\n'
```


**Done! **With testing.


# Task 5. Build and Deploy the updated Quotes app to Cloud Run

There are two options to build, tag and push the image to a container registry: a step-by-step manual commands approach and a more effective one using CloudBuild.


#### Option 5.1 Build, tag, push the image from the command line

Build a Java Docker image:


```
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=quotes
```


Check that the PROJECT_ID is set in your terminal:


```
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
echo   $PROJECT_ID
```


Configure Docker Auth to be able to push the image to the Container Registry:


```
# get credentials to push to Google Container Registry
gcloud auth configure-docker
```


If you have built the image locally, tag it first and push to a container registry:


```
# tag
docker tag quotes gcr.io/${PROJECT_ID}/quotes
```


Push to a container registry:


```
# push Java image
docker push gcr.io/${PROJECT_ID}/quotes
```



#### Option 5.2 Build image using Cloud Build

Cloud Build supports a simple build, tag, push process in a single YAML file.

Start by validating that your PROJECT_ID is set:


```
# tag the image
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
echo   $PROJECT_ID
```


Build the Quotes application image using:


```
gcloud builds submit  --machine-type E2-HIGHCPU-32
```



#### Deploy the built image

Check existing deployed Cloud Run Services:


```
export PROJECT_ID=$(gcloud config list --format 'value(core.project)')
echo   $PROJECT_ID

gcloud run services list
```


Deploy the Quotes JIT image:


```
# note the URL of the service at the end of deployment
gcloud run deploy quotes \
     --image gcr.io/${PROJECT_ID}/quotes \
     --region us-central1 \
     --memory 2Gi --allow-unauthenticated
```



# Task 6. Test the application in Cloud Run

First, list the quotes cloud run service:


```
# find the Quotes URL is you have not noted it
gcloud run services list | grep quotes
```



```
✔  quotes                    us-central1   https://quotes-...-uc.a.run.app       

# validate that app passes start-up check
```


Update the URL in the following command, and run it:


```
URL= ...assign to URL you have retrieved from "gcloud run services list | grep quotes"

curl $URL:/start
# get quotes from the app
curl $URL:/quotes
curl $URL:/random-quote
curl $URL:/quotes/book/The%20Lord%20of%20the%20Rings 

# add a new quote to the repository
curl --location '$URL:/quotes' \
--header 'Content-Type: application/json' \
--data '{
    "author" : "Isabel Allende",
    "quote" : "The longer I live, the more uninformed I feel. Only the young have an explanation for everything.",
    "book" : "City of the Beasts"
}'
```



### If you have deployed the app with security enabled, (no --allow-unauthenticated flag) you can test it with a Bearer token. You can also use as an alternative HTTPie ([HTTP test client](https://httpie.io/))

Update the following command base_url, and run it:


```
TOKEN=$(gcloud auth print-identity-token)
URL=...assign to URL you have retrieved from
gcloud run services list | grep quotes

# Get the URL of the deployed app
# Test JIT image
curl -H "Authorization: Bearer $TOKEN" https://$URL/random-quote
curl -H "Authorization: Bearer $TOKEN" https://$URL/quotes
```


**DONE!**


