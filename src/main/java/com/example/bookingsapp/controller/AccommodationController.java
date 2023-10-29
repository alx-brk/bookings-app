package com.example.bookingsapp.controller;

import com.example.bookingsapp.service.AccommodationService;
import com.openapi.samples.gen.springbootserver.api.AccommodationApi;
import com.openapi.samples.gen.springbootserver.model.Accommodation;
import com.openapi.samples.gen.springbootserver.model.CreateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.UpdateAccommodationRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
public class AccommodationController implements AccommodationApi {

    private final AccommodationService accommodationService;

    @Override
    public ResponseEntity<Accommodation> createAccommodation(CreateAccommodationRequest createAccommodationRequest) {
        final Accommodation accommodation = accommodationService.createAccommodation(createAccommodationRequest);
        return ResponseEntity.ok(accommodation);
    }

    @Override
    public ResponseEntity<Accommodation> getAccommodation(Long accommodationId) {
        final Accommodation accommodation = accommodationService.getAccommodation(accommodationId);
        return ResponseEntity.ok(accommodation);
    }

    @Override
    public ResponseEntity<Accommodation> updateAccommodation(Long accommodationId, UpdateAccommodationRequest updateAccommodationRequest) {
        final Accommodation accommodation = accommodationService.updateAccommodation(accommodationId, updateAccommodationRequest);
        return ResponseEntity.ok(accommodation);
    }

    @Override
    public ResponseEntity<List<Accommodation>> searchAccommodations(LocalDate fromDate, LocalDate toDate) {
        final List<Accommodation> accommodations = accommodationService.searchAccommodations(fromDate, toDate);
        return ResponseEntity.ok(accommodations);
    }
}
