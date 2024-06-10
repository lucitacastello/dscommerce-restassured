package com.devsuperior.dscommerce.tests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class TokenUtil {

    public static String obtainAccesToken(String usename, String password){
        Response response = autRequest(usename, password);
        JsonPath jsonBody = response.jsonPath();
        return jsonBody.getString("access_token");
    }

    private static Response autRequest(String username, String password) {
        return
                given()
                        .auth().preemptive()
                        .basic("myclientid", "myclientsecret") // dados da api - dados environment no application
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("username", username)
                        .formParam("password", password)
                        .when()
                        .post("/oauth2/token");
    }
}
