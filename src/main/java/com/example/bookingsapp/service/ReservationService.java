package com.example.bookingsapp.service;

import com.example.bookingsapp.exceptions.InvalidRequestException;
import com.example.bookingsapp.exceptions.NotFoundException;
import com.example.bookingsapp.mapper.ModelMapper;
import com.example.bookingsapp.persistence.AccommodationRepository;
import com.example.bookingsapp.persistence.ReservationRepository;
import com.example.bookingsapp.validation.ValidationUtils;
import com.openapi.samples.gen.springbootserver.model.CreateReservationRequest;
import com.openapi.samples.gen.springbootserver.model.Reservation;
import com.openapi.samples.gen.springbootserver.model.ReservationType;
import com.openapi.samples.gen.springbootserver.model.UpdateReservationRequest;
import com.tej.JooQDemo.jooq.sample.model.tables.records.ReservationRecord;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.example.bookingsapp.validation.ValidationUtils.ACCOMMODATION_NOT_FOUND;
import static com.example.bookingsapp.validation.ValidationUtils.RESERVATION_NOT_FOUND;

@Service
@AllArgsConstructor
public class ReservationService {

    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtils validationUtils;


    @Transactional
    public Reservation blockAccommodation(CreateReservationRequest request) {
        return reserveAccommodationInternal(request, ReservationType.BLOCK);
    }

    @Transactional
    public Reservation bookAccommodation(CreateReservationRequest request) {
        validationUtils.validateGuestName(request);
        return reserveAccommodationInternal(request, ReservationType.BOOKING);
    }

    private Reservation reserveAccommodationInternal(CreateReservationRequest request, ReservationType reservationType) {
        validationUtils.validateReservationRequest(request);
        accommodationRepository.getAccommodation(request.getAccommodationId())
                .orElseThrow(() -> new NotFoundException(ACCOMMODATION_NOT_FOUND + request.getAccommodationId()));
        final List<ReservationRecord> bookings = reservationRepository.searchReservations(
                request.getAccommodationId(),
                (reservationType == ReservationType.BLOCK) ? ReservationType.BOOKING : null,
                request.getStartDate(),
                request.getEndDate()
        );

        validationUtils.assertNoReservations(modelMapper.map(bookings));
        final ReservationRecord reservationRecord = modelMapper.map(request, reservationType);
        final ReservationRecord result = reservationRepository.createReservation(reservationRecord);
        return modelMapper.map(result);
    }

    @Transactional
    public Reservation updateReservation(Long reservationId, UpdateReservationRequest reservationRequest) {
        validationUtils.assertAnyNotNull(reservationRequest);
        final ReservationRecord current = reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new InvalidRequestException(RESERVATION_NOT_FOUND + reservationId));
        final ReservationRecord reservationRecord = modelMapper.map(reservationRequest, current);
        final ReservationRecord result = reservationRepository.updateReservation(reservationId, reservationRecord);
        return modelMapper.map(result);
    }

    @Transactional
    public void deleteReservation(Long reservationId) {
        reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new InvalidRequestException(RESERVATION_NOT_FOUND + reservationId));
        reservationRepository.deleteReservation(reservationId);
    }

    public Reservation getReservation(Long reservationId) {
        return reservationRepository.getReservation(reservationId)
                .map(modelMapper::map)
                .orElseThrow(() -> new InvalidRequestException(RESERVATION_NOT_FOUND + reservationId));
    }

    public List<Reservation> searchReservations(LocalDate fromDate, LocalDate toDate, ReservationType reservationType) {
        return reservationRepository.searchReservations(null, reservationType, fromDate, toDate)
                .stream()
                .map(modelMapper::map)
                .toList();
    }
}
