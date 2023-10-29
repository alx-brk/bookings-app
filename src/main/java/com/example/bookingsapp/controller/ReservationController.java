package com.example.bookingsapp.controller;

import com.example.bookingsapp.service.ReservationService;
import com.openapi.samples.gen.springbootserver.api.ReservationApi;
import com.openapi.samples.gen.springbootserver.model.CreateReservationRequest;
import com.openapi.samples.gen.springbootserver.model.Reservation;
import com.openapi.samples.gen.springbootserver.model.ReservationType;
import com.openapi.samples.gen.springbootserver.model.UpdateReservationRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
public class ReservationController implements ReservationApi {

    private final ReservationService reservationService;

    @Override
    public ResponseEntity<Reservation> blockAccommodation(CreateReservationRequest reservationRequest) {
        final Reservation reservation = reservationService.blockAccommodation(reservationRequest);
        return ResponseEntity.ok(reservation);
    }

    @Override
    public ResponseEntity<Reservation> bookAccommodation(CreateReservationRequest reservationRequest) {
        final Reservation reservation = reservationService.bookAccommodation(reservationRequest);
        return ResponseEntity.ok(reservation);
    }

    @Override
    public ResponseEntity<Void> deleteReservation(Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Reservation> getReservation(Long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        return ResponseEntity.ok(reservation);
    }

    @Override
    public ResponseEntity<Reservation> updateReservation(Long reservationId, UpdateReservationRequest reservationRequest) {
        final Reservation reservation = reservationService.updateReservation(reservationId, reservationRequest);
        return ResponseEntity.ok(reservation);
    }

    @Override
    public ResponseEntity<List<Reservation>> searchReservations(LocalDate fromDate, LocalDate toDate, ReservationType reservationType) {
        final List<Reservation> reservations = reservationService.searchReservations(fromDate, toDate, reservationType);
        return ResponseEntity.ok(reservations);
    }
}
