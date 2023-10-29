package com.example.bookingsapp.validation;

import com.example.bookingsapp.exceptions.InvalidRequestException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openapi.samples.gen.springbootserver.model.CreateReservationRequest;
import com.openapi.samples.gen.springbootserver.model.Reservation;
import com.openapi.samples.gen.springbootserver.model.UpdateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.UpdateReservationRequest;
import com.tej.JooQDemo.jooq.sample.model.tables.records.AccommodationRecord;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationUtils {

    public static final String CANNOT_BE_NULL_ERROR = "Cannot be null: ";
    public static final String CANNOT_BE_NULL_OR_EMPTY_ERROR = "Cannot be null or empty string: ";
    public static final String INVALID_TIME_VALUE = "Value should be between 0 and 23: ";
    public static final String CHECK_IN_AFTER_CHECK_OUT = "Check-in time should be at least %s hours after Check-out time in order to prepare accommodation for new guests";
    public static final String ACCOMMODATION_NOT_FOUND = "Accommodation not found: ";
    public static final String RESERVATION_NOT_FOUND = "Reservation not found: ";
    public static final String ANY_PARAM_REQUIRED = "At least one parameter should be provided";
    public static final String START_BEFORE_END = "StartDate should be before EndDate";
    public static final String CANNOT_RESERVE = "Cannot reserve dates. Other reservations are present: ";
    public static final String NAME_FIELD = "name";
    public static final String ADDRESS_FIELD = "address";
    public static final String PRICE_FIELD = "price";
    public static final String CHECK_IN_TIME_FIELD = "checkInTime";
    public static final String CHECK_OUT_TIME_FIELD = "checkOutTime";
    public static final String START_DATE_FIELD = "startDate";
    public static final String END_DATE_FIELD = "endDate";
    public static final String GUEST_NAME_FIELD = "guestName";
    public static final String ACCOMMODATION_ID_FIELD = "accommodationId";

    private final ObjectMapper objectMapper;

    @Value("${service.timeGapInHours}")
    private Integer timeGap;


    public void validateAccommodationRecord(AccommodationRecord record) {
        Validator.of(record)
                .validateCondition(AccommodationRecord::getName, StringUtils::isNoneEmpty, notNullOrEmpty(NAME_FIELD))
                .validateCondition(AccommodationRecord::getAddress, StringUtils::isNoneEmpty, notNullOrEmpty(ADDRESS_FIELD))
                .validateNotNull(AccommodationRecord::getPrice, notNull(PRICE_FIELD))
                .validateNotNull(AccommodationRecord::getCheckInTime, notNull(CHECK_IN_TIME_FIELD))
                .validateNotNull(AccommodationRecord::getCheckOutTime, notNull(CHECK_OUT_TIME_FIELD))
                .validateCondition(
                        AccommodationRecord::getCheckInTime,
                        val -> val >= 0 && val <= 23,
                        invalidTimeValue(CHECK_IN_TIME_FIELD)
                )
                .validateCondition(
                        AccommodationRecord::getCheckOutTime,
                        val -> val >= 0 && val <= 23,
                        invalidTimeValue(CHECK_OUT_TIME_FIELD)
                )
                .validateCondition(
                        AccommodationRecord::getCheckInTime,
                        val -> val >= record.getCheckOutTime() + timeGap,
                        new InvalidRequestException(String.format(CHECK_IN_AFTER_CHECK_OUT, timeGap))
                );
    }

    public void validateReservationRequest(CreateReservationRequest request) {
        Validator.of(request)
                .validateNotNull(CreateReservationRequest::getAccommodationId, notNull(ACCOMMODATION_ID_FIELD))
                .validateNotNull(CreateReservationRequest::getStartDate, notNull(START_DATE_FIELD))
                .validateNotNull(CreateReservationRequest::getEndDate, notNull(END_DATE_FIELD))
                .validateCondition(
                        CreateReservationRequest::getStartDate,
                        sd -> sd.isBefore(request.getEndDate()),
                        new InvalidRequestException(START_BEFORE_END)
                );
    }

    public void validateGuestName(CreateReservationRequest request) {
        Validator.of(request)
                .validateCondition(CreateReservationRequest::getGuestName, StringUtils::isNoneEmpty, notNullOrEmpty(GUEST_NAME_FIELD));
    }

    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            throw new InvalidRequestException(START_BEFORE_END);
        }
    }

    public void assertAnyNotNull(UpdateAccommodationRequest request) {
        if (new UpdateAccommodationRequest().equals(request)) {
            throw new InvalidRequestException(ANY_PARAM_REQUIRED);
        }
    }

    public void assertAnyNotNull(UpdateReservationRequest request) {
        if (new UpdateReservationRequest().equals(request)) {
            throw new InvalidRequestException(ANY_PARAM_REQUIRED);
        }
    }

    public void assertNoReservations(List<Reservation> bookings) {
        if (!bookings.isEmpty()) {
            throw new InvalidRequestException(CANNOT_RESERVE + safetyStringify(bookings));
        }
    }

    private String safetyStringify(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            return "";
        }
    }


    private static InvalidRequestException notNull(String field) {
        return new InvalidRequestException(CANNOT_BE_NULL_ERROR + field);
    }

    private static InvalidRequestException notNullOrEmpty(String field) {
        return new InvalidRequestException(CANNOT_BE_NULL_OR_EMPTY_ERROR + field);
    }

    private static InvalidRequestException invalidTimeValue(String field) {
        return new InvalidRequestException(INVALID_TIME_VALUE + field);
    }
}
