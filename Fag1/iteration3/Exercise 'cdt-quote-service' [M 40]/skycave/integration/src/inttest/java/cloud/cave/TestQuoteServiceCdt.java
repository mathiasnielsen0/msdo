package example;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class TestHelloSpark {

  public static final int SERVER_PORT = 4567;
  @Container
  public GenericContainer<?> helloSpark =
          new GenericContainer(DockerImageName.parse("henrikbaerbak/quote:msdo_2_3"))
                  .withExposedPorts(SERVER_PORT);
  private String serverRootUrl;

  @BeforeEach
  public void setup()
  {
    String address = helloSpark.getHost();
    Integer port = helloSpark.getMappedPort(SERVER_PORT);
    serverRootUrl = "http://" + address + ":" + port + "/";
  }

  @Test
  public void shouldGETonPathHello() {
    HttpResponse<String> reply =
            Unirest.get(serverRootUrl + "GOLF").asString();
    System.out.println("** ROOT: " + reply.getBody());
    assertThat(reply.getStatus(), is(200));

    assertThat(reply.getBody(),
            containsString("Hello to you GOLF"));
  }

  @Test
  public void shouldGet200OnQuotesEndpoint() {
    HttpResponse<String> reply =
            Unirest.get(serverRootUrl + "msdo/vq/quotes").asString();
    System.out.println("** ROOT: " + reply.getBody());
    assertThat(reply.getStatus(), is(200));

    //assertThat(reply.getBody(),
    //        containsString("Hello to you GOLF"));
  }
  
  @Test
  public void shouldGet400OnWrongQuotesEndpoint() {
    HttpResponse<String> reply =
            Unirest.get(serverRootUrl + "msdo/vq/quote1s").asString();
    System.out.println("** ROOT: " + reply.getBody());
    assertThat(reply.getStatus(), is(400));

    //assertThat(reply.getBody(),
    //        containsString("Hello to you GOLF"));
  }
}
  
