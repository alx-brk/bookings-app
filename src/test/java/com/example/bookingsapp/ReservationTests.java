package com.example.bookingsapp;

import com.example.bookingsapp.exceptions.InvalidRequestException;
import com.example.bookingsapp.exceptions.NotFoundException;
import com.example.bookingsapp.persistence.AccommodationRepository;
import com.example.bookingsapp.persistence.ReservationRepository;
import com.example.bookingsapp.service.AccommodationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openapi.samples.gen.springbootserver.model.Accommodation;
import com.openapi.samples.gen.springbootserver.model.CreateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.CreateReservationRequest;
import com.openapi.samples.gen.springbootserver.model.ReservationType;
import com.tej.JooQDemo.jooq.sample.model.tables.records.ReservationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.example.bookingsapp.validation.ValidationUtils.ACCOMMODATION_NOT_FOUND;
import static com.example.bookingsapp.validation.ValidationUtils.CANNOT_RESERVE;
import static com.example.bookingsapp.validation.ValidationUtils.START_BEFORE_END;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ReservationTests {

    public static final String NAME = "Hotel Budapest";
    public static final String ADDRESS = "Narnia";
    public static final LocalDate START_DATE = LocalDate.now();
    public static final LocalDate END_DATE = START_DATE.plusDays(3);
    public static final String GUEST_NAME = "Rick Sanchez";
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AccommodationService accommodationService;

    @BeforeEach
    public void before() {
        reservationRepository.deleteAll();
        accommodationRepository.deleteAll();
    }

    @Test
    public void blockAccommodationPositive() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        //WHEN:
        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), null);
        mvc.perform(request(HttpMethod.POST, "/reservation/block")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.accommodationId").value(accommodation.getAccommodationId()))
                .andExpect(jsonPath("$.reservationType").value(ReservationType.BLOCK.getValue()))
                .andExpect(jsonPath("$.startDate").value(START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(END_DATE.toString()));
    }

    @Test
    public void blockAccommodationOverlapPositive() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), null);
        mvc.perform(request(HttpMethod.POST, "/reservation/block")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.accommodationId").value(accommodation.getAccommodationId()))
                .andExpect(jsonPath("$.reservationType").value(ReservationType.BLOCK.getValue()))
                .andExpect(jsonPath("$.startDate").value(START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(END_DATE.toString()));

        //WHEN:
        LocalDate anotherStartDate = END_DATE.minusDays(1);
        LocalDate anotherEndDate = END_DATE.plusDays(1);
        reservationRequest.startDate(anotherStartDate)
                        .endDate(anotherEndDate);
        mvc.perform(request(HttpMethod.POST, "/reservation/block")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.accommodationId").value(accommodation.getAccommodationId()))
                .andExpect(jsonPath("$.reservationType").value(ReservationType.BLOCK.getValue()))
                .andExpect(jsonPath("$.startDate").value(anotherStartDate.toString()))
                .andExpect(jsonPath("$.endDate").value(anotherEndDate.toString()));

        //THEN:
        List<ReservationRecord> reservations = reservationRepository.searchReservations(
                accommodation.getAccommodationId(),
                ReservationType.BLOCK,
                null,
                null
        );
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
    }

    @Test
    public void bookAccommodationPositive() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        //WHEN:
        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), GUEST_NAME);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.accommodationId").value(accommodation.getAccommodationId()))
                .andExpect(jsonPath("$.reservationType").value(ReservationType.BOOKING.getValue()))
                .andExpect(jsonPath("$.startDate").value(START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(END_DATE.toString()));
    }

    @Test
    public void bookAccommodationCheckInAndCheckoutSameDayPositive() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), GUEST_NAME);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200));

        LocalDate anotherStartDate = END_DATE.plusDays(7);
        LocalDate anotherEndDate = anotherStartDate.plusDays(5);
        reservationRequest.startDate(anotherStartDate)
                .endDate(anotherEndDate);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200));
        //WHEN:
        reservationRequest.startDate(END_DATE)
                .endDate(anotherStartDate);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200));
    }

    @Test
    public void bookAccommodationOverlapNegative() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), GUEST_NAME);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.accommodationId").value(accommodation.getAccommodationId()))
                .andExpect(jsonPath("$.reservationType").value(ReservationType.BOOKING.getValue()))
                .andExpect(jsonPath("$.startDate").value(START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(END_DATE.toString()));

        //WHEN:
        LocalDate anotherStartDate = END_DATE.minusDays(1);
        LocalDate anotherEndDate = END_DATE.plusDays(1);
        reservationRequest.startDate(anotherStartDate)
                .endDate(anotherEndDate);

        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error").value(InvalidRequestException.class.getSimpleName()))
                .andExpect(jsonPath("$.message", containsString(CANNOT_RESERVE)));
    }

    @Test
    public void bookAccommodationBlockedNegative() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), null);
        mvc.perform(request(HttpMethod.POST, "/reservation/block")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200));

        //WHEN:
        LocalDate anotherStartDate = END_DATE.minusDays(1);
        LocalDate anotherEndDate = END_DATE.plusDays(1);
        reservationRequest.startDate(anotherStartDate)
                .endDate(anotherEndDate)
                .guestName(GUEST_NAME);

        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error").value(InvalidRequestException.class.getSimpleName()))
                .andExpect(jsonPath("$.message", containsString(CANNOT_RESERVE)));
    }

    @Test
    public void bookAccommodationBookedNegative() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), GUEST_NAME);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(200));

        //WHEN:
        LocalDate anotherStartDate = END_DATE.minusDays(1);
        LocalDate anotherEndDate = END_DATE.plusDays(1);
        reservationRequest.startDate(anotherStartDate)
                .endDate(anotherEndDate);

        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error").value(InvalidRequestException.class.getSimpleName()))
                .andExpect(jsonPath("$.message", containsString(CANNOT_RESERVE)));
    }

    @Test
    public void bookAccommodationInvalidDatesNegative() throws Exception {
        //GIVEN:
        Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest());
        assertNotNull(accommodation);
        assertNotNull(accommodation.getAccommodationId());

        //WHEN:
        CreateReservationRequest reservationRequest = createReservationRequest(accommodation.getAccommodationId(), GUEST_NAME)
                .startDate(END_DATE)
                .endDate(START_DATE);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.error").value(InvalidRequestException.class.getSimpleName()))
                .andExpect(jsonPath("$.message", containsString(START_BEFORE_END)));
    }

    @Test
    public void bookAccommodationNotFoundNegative() throws Exception {
        CreateReservationRequest reservationRequest = createReservationRequest(123L, GUEST_NAME);
        mvc.perform(request(HttpMethod.POST, "/reservation/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest))
                )
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.error").value(NotFoundException.class.getSimpleName()))
                .andExpect(jsonPath("$.message", containsString(ACCOMMODATION_NOT_FOUND)));
    }

    private CreateAccommodationRequest createAccommodationRequest() {
        return new CreateAccommodationRequest()
                .name(NAME)
                .address(ADDRESS)
                .checkInTime(15)
                .checkOutTime(12)
                .price(1000L);
    }

    private CreateReservationRequest createReservationRequest(Long accommodationId, String guestName) {
        return new CreateReservationRequest()
                .accommodationId(accommodationId)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .guestName(guestName);
    }
}
