package com.devsuperior.dscommerce.controllers;


import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;

    private Long existingOrderId, nonExistingOrderId;

    @BeforeEach
    public void setUp() {
        //definição do endereço do endpoint da API que será testada
        //a api deve estar sendo executada, projeto rodando
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword="123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        existingOrderId = 1L;
        nonExistingOrderId = 100L;

        clientToken = TokenUtil.obtainAccesToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccesToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto"; // Invalid token simulation
    }

    // Problema 5: Consultar pedido por id
    @Test
    public void findByIdShouldReturnOrderWhenIdExistisAndAdminLogged(){
        //1.	Busca de pedido por id retorna pedido existente quando logado como admin

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
           .when()
                .get("/orders/{id}", existingOrderId)
           .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", is("2022-07-25T13:00:00Z"))
                .body("status", is("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.name", hasItems( "The Lord of the Rings","Macbook Pro"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnOrderWhenIdExistisAndClientLogged(){
        //2.	Busca de pedido por id retorna pedido existente
        // quando logado como cliente e o pedido pertence ao usuário

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", is("2022-07-25T13:00:00Z"))
                .body("status", is("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.name", hasItems( "The Lord of the Rings","Macbook Pro"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistisAndClientLoggedAndOrderDoesNotBelongUser403(){
        //3.	Busca de pedido por id retorna 403 quando pedido não pertence ao usuário
        Long otherOrderId = 2L;
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
          .when()
                .get("/orders/{id}", otherOrderId)
                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistisAndAdminLogged404(){
        //4.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como admin

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado"));
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistisAndClientLogged404(){
        //5.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como cliente

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado"));
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenInvalidToken401(){
        //6.	Busca de pedido por id retorna 401 quando não logado como admin ou cliente

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(401);
    }

}
