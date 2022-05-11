package site.metacoding.mongocrud.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import site.metacoding.mongocrud.domain.Naver;

// @RequiredArgsConstructor
// 통합테스트로 만들어주는 어노테이션
// webEnvironment : 포트지정해주기
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class NaverApiControllerTest {

    // TestRestTemplate : Test 전용 RestTemplate(API용)
    // JUint5가 @RequiredArgsConstructor 말고 다른 방식으로 의존성주입을 시도하기때문에
    // @Autowired 를 사용하여 의존성을 주입해야 한다.
    @Autowired // DI
    private TestRestTemplate rt;
    private static HttpHeaders headers;

    // 이 Test파일이 실행될 때, 최초에 무조건 실행되는 메서드
    // BeforeAll 을 사용할땐 무조건 static이어야 한다.
    @BeforeAll
    public static void init() {
        headers = new HttpHeaders();
        // headers에 응답받을 데이터 타입을 지정해주기.
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void save_테스트() throws JsonProcessingException { // 메서드 전체를 try-catch로 묶어주기
        // given : 가짜 데이터 만드는 영역
        Naver naver = new Naver();
        naver.setTitle("스프링1강");
        naver.setCompany("재밌어요");
        // JSON으로 파싱하기
        ObjectMapper om = new ObjectMapper();
        // Byte를 Buffer로 읽지 않아도 JSON으로 파싱이 가능하다.
        String content = om.writeValueAsString(naver);
        // HttpEntity로 받아주기(Body데이터와 Header데이터를 순서대로 넣어준다.)
        // header도 넣어야 하기 때문에 만들어야 하는데 전역적으로 쓸것이기 때문에 위에 만들자.
        HttpEntity<String> httpEntity = new HttpEntity<>(content, headers);

        // Body에 HttpEntity를 넣어야한다. Header도 포함시켜서 넣어야 하기 때문이다.
        // when (실행)
        ResponseEntity<String> response = rt.exchange("/navers", HttpMethod.POST, httpEntity, String.class);

        // then (검증)
        // System.out.println("============================");
        // System.out.println(response.getBody());
        // System.out.println(response.getHeaders());
        // System.out.println(response.getStatusCode());
        // System.out.println("============================");
        // assertTrue(response.getStatusCode().is2xxSuccessful());

        // 더 정확한 검증을 위해 값으로써 검증하기
        DocumentContext dc = JsonPath.parse(response.getBody());
        // System.out.println(dc.jsonString());
        String title = dc.read("$.title");
        // System.out.println(title);
        assertEquals("스프링1강", title);
    }

    @Test
    public void findAll_테스트() {
        // given (SELECT라서 줄 데이터가 없다)

        // when (실행)
        ResponseEntity<String> response = rt.exchange("/navers", HttpMethod.GET, null,  String.class);

        // then
        // System.out.println(response.getBody());
        DocumentContext dc = JsonPath.parse(response.getBody());
        String title = dc.read("$.[0].title");
        // assertEquals는 데이터가 바뀌거나 없을수도 있기 때문에 사용하지 않는다.
        // 대신 상태코드를 비교해서 검증하자.
        // assertEquals("지방선거 6.1일이 곧 다가온다", title);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

}