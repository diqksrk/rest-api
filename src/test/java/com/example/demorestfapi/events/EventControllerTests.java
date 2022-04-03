package com.example.demorestfapi.events;

import com.example.demorestfapi.common.RestDocsCongifuration;
import com.example.demorestfapi.common.TestDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(RestDocsCongifuration.class)
//@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .apply(documentationConfiguration(restDocumentation))
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("yoonjoy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("yoonjoy.me").removePort(), prettyPrint()))
                .build();
    }

    @Test
    @TestDescription("입력받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("sdadasd")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 11, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 11, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 11, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 11, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("sdadasd")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 11, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 11, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 11, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 11, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
//                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("offline").value(Matchers.not(false)))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query"),
                                linkWithRel("update-event").description("link to update")
//                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                            headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new events"),
                                fieldWithPath("description").description("description of new events"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new events"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new events"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new events"),
                                fieldWithPath("location").description("location of new events"),
                                fieldWithPath("basePrice").description("basePrice of new events"),
                                fieldWithPath("maxPrice").description("maxPrice of new events"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new events"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new events")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location hedaer"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("id").description("id of new events"),
                                fieldWithPath("name").description("Name of new events"),
                                fieldWithPath("description").description("description of new events"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new events"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new events"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new events"),
                                fieldWithPath("location").description("location of new events"),
                                fieldWithPath("basePrice").description("basePrice of new events"),
                                fieldWithPath("maxPrice").description("maxPrice of new events"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new events"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new events"),
                                fieldWithPath("free").description("free of new events"),
                                fieldWithPath("offline").description("offline of new events"),
                                fieldWithPath("eventStatus").description("eventStatus of new events"),
                                //optional fields
                                fieldWithPath("_links.self.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.query-events.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.update-event.href").type(JsonFieldType.STRING).description("my href").optional()
                        )
                ));
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Wrong_Input() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("sdadasd")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 26, 11, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 25, 11, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 11, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 11, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
        ;
    }

    @ParameterizedTest
    @MethodSource("paramsForTestFree")
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // when
        event.update();

        // then
        assertThat(event.isFree(), is(isFree));
    }

    @ParameterizedTest
    @MethodSource("paramsForTestOffline")
    void testOffline(String location, boolean isOffline) {
        // given
        Event event = Event.builder()
                .location(location)
                .build();

        // when
        event.update();

        // then
        assertThat(event.isOffline(), is(isOffline));
    }

    private static Stream<Arguments> paramsForTestFree() { // argument source method
        return Stream.of(
                Arguments.of(0,0, true),
                Arguments.of(100, 0, false),
                Arguments.of(0, 100, false),
                Arguments.of(100, 200, false)
        );
    }

    private static Stream<Arguments> paramsForTestOffline() { // argument source method
        return Stream.of(
                Arguments.of("강남", true),
                Arguments.of(null, false),
                Arguments.of("        ", false)
        );
    }
}
