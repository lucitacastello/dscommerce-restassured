package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;

    private Long existingProductId, nonExistingProductId;
    private String productName;

    //para inserir produto
    private Map<String, Object> postProductInstance;


    @BeforeEach
    public void setUp() {
        //definição do endereço do endpoint da API que será testada
        //a api deve estar sendo executada, projeto rodando
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword="123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccesToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccesToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto"; // Invalid token simulation

        productName = "Macbook";

        //para adicionar um novo produto
        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 50.0);

        //para adicionar categorias ao produto
        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);

    }

    // Problema 2: Consultar produtos
    @Test
    public void findByIdShouldReturnProductWhenIdExists() {
        existingProductId = 2L;

        given()
                .get("/products/{id}", existingProductId)
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0F))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores")
                );
    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {

        given()
                .get("/products?page=0")
                .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera")
                );
    }

    @Test
    public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {

        given()
                .get("/products?name={productName}", productName)
                .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0F))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg")
                );
    }

    @Test
    public void findAllShouldReturnPagedProductsWithPriceGreaterThan2000() {

        given()
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll {it.price > 2000}.name", hasItems("Smart TV", "PC Gamer Hera", "PC Gamer Weed") // findAll função rest assured
                );
    }

    //Problema 3: Inserir produto
    @Test
    public void insertShouldReturnProductCreatedWhenAdminLogged() {
        // 1.	Inserção de produto insere produto com dados válidos quando logado como admin
        //converte o objeto postProductInstance em objeto JSON
        JSONObject newProduct = new JSONObject(postProductInstance);  // biblioteca json-simple

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                     .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Meu produto"))
                .body("price", is(50.0F))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2, 3));
    }

}
