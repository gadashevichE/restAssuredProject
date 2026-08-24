package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.restassured.http.ContentType;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;


public class ReqresTest {
    private static final String BASE_URL = "https://reqres.in/";


    @Test
    public void checkAvatarAndIdTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        users.forEach(x -> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));

        Assert.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x -> x.getId().toString()).collect(Collectors.toList());

        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }


    @Test
    public void successRegisterTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecOK200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);
        Assert.assertNull(successReg.getId());
        Assert.assertNull(successReg.getToken());

        Assert.assertEquals(id, successReg.getId());
        Assert.assertEquals(token, successReg.getToken());
    }

    @Test
    public void unSuccessRegisterTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecError400());
        Register user = new Register("eve.holt@reqres.in", "");
        unSuccessReg unSuccessReg = given()
                .body(user)
                .post("api/register")
                .then().log().all()
                .extract().as(unSuccessReg.class);
        Assert.assertEquals("Missing password", unSuccessReg.getError());
    }

    @Test
    public void sortedYearsTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecOK200());
        List<ColorsData> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(sortedYears, years);

    }


    @Test
    public void deleteUserTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecUniq(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();

    }


    @Test
    public void timeTest() {
        Specifications.installSpecification(Specifications.requestSpecification(BASE_URL), Specifications.responseSpecOK200());
        UserTime user = new UserTime("morpheus", "zion resident");

        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);
        String regex = "(.{8})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll("(.{14})$","");
        Assert.assertEquals(currentTime,response.getUpdatedAt().replaceAll(regex,""));
    }

}
