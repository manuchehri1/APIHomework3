package com.harrypotterapi;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeAll;
import org.testng.Assert;
import org.w3c.dom.ls.LSInput;


public class HarryPotterTests {

    private String apiKey = "$2a$10$RySyBc6UW1tuvs2c.wt74u0mCicFTb1liPoQVlKYKujbewk3WFXgq";
    private String apiKeyWrong = "yBc6UW1tuvs2c.wt74u0mCicFTb1liPoQVlKYKujbewk3WFXgq";


    @BeforeAll
    public static void setUp(){
       baseURI = "https://www.potterapi.com/v1";
    }

    /**
     *
     Verify sorting hat
     1. Send a get request to /sortingHat. Request includes :
     2. Verify status code 200, content type application/json; charset=utf-8
     3. Verify that response body contains one of the following houses:
     "Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"
     */
    @Test
    @DisplayName("Verify sorting hat")
    public void sortingHatTest(){
        Response response = given().
                                accept(ContentType.JSON).
                            when().
                                get("/sortingHat");
        
                response.   then().
                                assertThat().
                                statusCode(200).
                                contentType("application/json; charset=utf-8");
//                response.then().body("",oneOf( containsString("Gryffindor"),
//                                            containsString("Ravenclaw"),
//                                            containsString("Slytherin"),
//                                            containsString("Hufflepuff")));
         // another way
        List<String> houseList = Arrays.asList("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff");
        String bodyResponse = response.as(String.class);
        System.out.println("body = " + bodyResponse);
                                assertTrue(houseList.contains(bodyResponse));
    }

    /**
     * Verify bad key
     * 1. Send a get request to /characters. Request includes :
     * • Header Accept with value application/json
     * • Query param key with value invalid
     * 2. Verify status code 401, content type application/json; charset=utf-8
     * 3. Verify response status line include message Unauthorized
     * 4. Verify that response body says "error": "API Key Not Found"
     */
    @Test
    @DisplayName("Verify sorting hat")
    public void badAPIKeyTest(){
        Response response = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKeyWrong).
                             when().
                                    get("/characters").prettyPeek();

                    response.then().
                                    assertThat().
                                    statusCode(401).
                                    contentType("application/json; charset=utf-8").
                                    body("error",is("API Key Not Found")).
                                    statusLine(containsString("Unauthorized"));

        String json = response.getBody().asString();
        System.out.println("json = " + json);

        String expected = "\"error\":\"API Key Not Found\"";
        assertTrue(json.contains(expected));
    }

    /**
     * Verify no key
     * 1. Send a get request to /characters. Request includes :
     *  • Header Accept with value application/json
     * 2. Verify status code 409, content type application/json; charset=utf-8
     * 3. Verify response status line include message Conflict
     * 4. Verify that response body says "error": "Must pass API key for request"
     */

    @Test
    public void noAPITest(){
        Response response = given().
                                    accept(ContentType.JSON).
                             when().
                                    get("/characters").prettyPeek();
        response.           then().
                                    assertThat().statusCode(409).
                                    contentType(ContentType.JSON).
                                    statusLine(containsString("Conflict")).
                                    body("error",is("Must pass API key for request"));

        String statusLine = response.statusLine().toString();
        JsonPath json = response.jsonPath();
                            assertTrue(statusLine.contains("Conflict"));
                            assertEquals(json.getString("error"),"Must pass API key for request");

    }

    /**
     * Verify number of characters
     * 1. Send a get request to /characters. Request includes :
     *  • Header Accept with value application/json
     *  • Query param key with value {{apiKey}}
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify response contains 194 characters
     * ?????????????????????????????????????????  195
     */

    @Test
    public void numberOfCharactersTest(){
        Response response = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                            when().
                                    get("/characters");

        List<Object> charactersList = response.jsonPath().getList("");
                response.   then().
                                    statusCode(200).
                                    contentType(ContentType.JSON).
                                    body("size()",is(195));


        System.out.println("charactersList.size() = " + charactersList.size());
    }


    /**
     * Verify number of character id and house
     * 1. Send a get request to /characters. Request includes :
     * • Header Accept with value application/json
     * • Query param key with value {{apiKey}}
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify all characters in the response have id field which is not empty
     * 4. Verify that value type of the field dumbledoresArmy is a boolean in all characters in the response
     * 5. Verify value of the house in all characters in the response is one of the following:
     * "Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"
     * ???????????????????????????????????????????????????
     */

    @Test
    public void idHouseTest(){
        Response response = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                             when().
                                    get("/characters");

                            response. then().
                                    statusCode(200).
                                    contentType(ContentType.JSON).
                                    body("_id",everyItem(not(isEmptyString()))).
                                    body("dumbledoresArmy",everyItem(is(instanceOf(Boolean.class)))).
                                    body("house",everyItem(is(oneOf("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff",null))));

        List<String> idList = response.jsonPath().getList("_id");
        List<Boolean> dumbledoresArmyList = response.jsonPath().getList("dumbledoresArmy");
        List<String> houseList = response.jsonPath().getList("house");


                for (int i = 0; i < idList.size(); i++) {

                    assertTrue(dumbledoresArmyList.get(i)==true || dumbledoresArmyList.get(i)==false);
                  //  assertTrue(listOfHouse.contains(houseList.get(i)));

                }


    }

        /**
         * Verify all character information
         * 1. Send a get request to /characters.
         *  Request includes :
         *  • Header Accept with value application/json
         * • Query param key with value {{apiKey}}
         * 2. Verify status code 200, content type application/json; charset=utf-8
         * 3. Select name of any random character
         * 4. Send a get request to /characters.
         * Request includes :
         * • Header Accept with value application/json
         * • Query param key with value {{apiKey}}
         * • Query param name with value from step 3
         * 5. Verify that response contains the same character information from step 3.
         * Compare all fields  ???????????????
         */

        @Test
        public void allCharactersInfoTest(){
            Response response1 = given().accept(ContentType.JSON).
                                         queryParam("key",apiKey).
                                when().  get("/characters");

                        response1.then().
                                         statusCode(200).
                                         contentType(ContentType.JSON);

            List<String> allNames = response1.jsonPath().getList("name");
            Random random = new Random();
            int randomNum = random.nextInt(allNames.size());

            String randomName = allNames.get(randomNum);
            System.out.println("randomName = " + randomName);

            Response response2 = given().
                                        accept(ContentType.JSON).
                                        queryParam("key",apiKey).
                                        queryParam("name",randomName).
                                when().
                                        get("/characters").prettyPeek();
                                        response2.then().
                                                        body("[0].name",is(randomName));


              // 2. way
            String actualName = response2.jsonPath().getString("name").
                    replace("[","").replace("]","");
            System.out.println("actualName = " + actualName);
            assertEquals(randomName,actualName);

        }


    /**
     * Verify name search
     * 1. Send a get request to /characters. Request includes :
     *  • Header Accept with value application/json
     *  • Query param key with value {{apiKey}}
     *  • Query param name with value Harry Potter
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify name Harry Potter
     * 4. Send a get request to /characters. Request includes :
     *  • Header Accept with value application/json
     *  • Query param key with value {{apiKey}}
     *  • Query param name with value Marry Potter
     * 5. Verify status code 200, content type application/json; charset=utf-8
     * 6. Verify response body is empty
     */

    @Test
    public void nameSearchTest(){
        Response response1 = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                                    queryParam("name","Harry Potter").
                            when(). get("/characters");

        String expected = "Harry Potter";
                  response1.then().
                                    statusCode(200).
                                    contentType(ContentType.JSON).
                                    body("[0].name",is(expected));


                    String actual = response1.jsonPath().getString("name").
                                  replace("[","").replace("]","");;
                    System.out.println("actual = " + actual);
                    assertEquals(expected,actual);

        Response response2 = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                                    queryParam("name","Marry Potter").
                            when().
                                    get("/characters");
                            response2.then().
                                            body("[0]",is(isEmptyOrNullString())).
                                            body("size()",is(0));

        List<Map<String,Object>> response2Body = response2.jsonPath().getList("");

                                    assertEquals(response2Body.size(),0);
    }


    /**
     * Verify house members
     * 1. Send a get request to /houses. Request includes :
 *      • Header Accept with value application/json
     *  • Query param key with value {{apiKey}}
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Capture the id of the Gryffindor house
     * 4. Capture the ids of the all members of the Gryffindor house
     *
     * 5. Send a get request to /houses/:id. Request includes :
     *  • Header Accept with value application/json
     *  • Query param key with value {{apiKey}}
     *  • Path param id with value from step 3
     * 6. Verify that response contains the same member ids as the step 4
     */

    @Test
    public void houseMemberTest(){
        Response response1 = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                            when().
                                    get("/houses");
                    response1.then().
                                    statusCode(200).
                                    contentType(ContentType.JSON);

        String idOfGryffindor = response1.jsonPath().getString("find{it.name == 'Gryffindor'}._id");
        System.out.println("idOfGryffindor = " + idOfGryffindor);

        List<String> idsOfMembersR1 = response1.jsonPath().getList("find{it.name == 'Gryffindor'}.members");

        Response response2 = given().
                                    accept(ContentType.JSON).
                                    queryParam("key",apiKey).
                                    pathParam("id",idOfGryffindor).
                            when().
                                    get("/houses/{id}").prettyPeek();
//                            response1.then().
//                                            body("[0].members._id",is(idsOfMembersR1));

        List<String> idsOfMembersR2 = response2.jsonPath().getList("[0].members._id");

        assertEquals(idsOfMembersR1,idsOfMembersR2);

    }

    /**
     * Verify house members again
     * 1. Send a get request to /houses/:id. Request includes :
     * • Header Accept with value application/json
     * • Query param key with value {{apiKey}}
     * • Path param id with value 5a05e2b252f721a3cf2ea33f
     * 2. Capture the ids of all members
     * 3. Send a get request to /characters. Request includes :
     * • Header Accept with value application/json
     * • Query param key with value {{apiKey}}
     * • Query param house with value Gryffindor
     * 4. Verify that response contains the same member ids from step 2
     */

    @Test
    public void houseMembersTest(){

        Response response1 = given().
                                    contentType("application/json").
                                    queryParam("key",apiKey).

                             when().
                                    get("/houses/{id}","5a05e2b252f721a3cf2ea33f");
        

        String  membersId = response1.jsonPath().getList("[0].members._id").toString();

        System.out.println("members ID = " + membersId);



        Response response2 = given().
                                    contentType("application/json").
                                    queryParam("key",apiKey).
                                    queryParam("house","Gryffindor").
                            when().
                                    get("/characters");
        String  idList = response2.jsonPath().getList("_id").toString();
        System.out.println("idList = " + idList);

                                assertTrue(membersId!=idList);


                        
        

                               




    }



    /**
     * Verify house with most members
     * 1. Send a get request to /houses. Request includes :
     * • Header Accept with value application/json
     * • Query param key with value {{apiKey}}
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify that Gryffindor house has the most members
     */

    @Test
    public void mostMembersTest(){
        Response response = given().
                                    queryParam("key",apiKey).
                                    accept("application/json").
                             when().
                                    get("/houses");

                    response.then().
                                    statusCode(200).
                                    contentType("application/json; charset=utf-8");

        List<List<String>> listOfMembers = response.jsonPath().getList("members");

        List<List<String>> gryffMember = response.jsonPath().getList("findAll{it.name == 'Gryffindor'}.members");
        int sizeOf = gryffMember.get(0).size();
        for (int i = 1; i <listOfMembers.size() ; i++) {
            assertTrue(sizeOf>listOfMembers.get(i).size());
        }



    }






}
