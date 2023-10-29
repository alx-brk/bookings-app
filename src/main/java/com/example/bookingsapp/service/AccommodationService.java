package com.example.bookingsapp.service;

import com.example.bookingsapp.exceptions.NotFoundException;
import com.example.bookingsapp.mapper.ModelMapper;
import com.example.bookingsapp.persistence.AccommodationRepository;
import com.example.bookingsapp.validation.ValidationUtils;
import com.openapi.samples.gen.springbootserver.model.Accommodation;
import com.openapi.samples.gen.springbootserver.model.CreateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.UpdateAccommodationRequest;
import com.tej.JooQDemo.jooq.sample.model.tables.records.AccommodationRecord;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.example.bookingsapp.validation.ValidationUtils.ACCOMMODATION_NOT_FOUND;

@Service
@AllArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtils validationUtils;

    @Transactional
    public Accommodation createAccommodation(CreateAccommodationRequest request) {
        final AccommodationRecord accommodationRecord = modelMapper.map(request);
        validationUtils.validateAccommodationRecord(accommodationRecord);
        final AccommodationRecord result = accommodationRepository.createAccommodation(accommodationRecord);
        return modelMapper.map(result);
    }

    @Transactional
    public Accommodation updateAccommodation(Long id, UpdateAccommodationRequest request) {
        validationUtils.assertAnyNotNull(request);
        final AccommodationRecord current = accommodationRepository.getAccommodation(id)
                .orElseThrow(() -> new NotFoundException(ACCOMMODATION_NOT_FOUND + id));
        final AccommodationRecord accommodationRecord = modelMapper.map(request, current);
        validationUtils.validateAccommodationRecord(accommodationRecord);
        final AccommodationRecord result = accommodationRepository.updateAccommodation(id, accommodationRecord);
        return modelMapper.map(result);
    }

    public Accommodation getAccommodation(Long id) {
        return accommodationRepository.getAccommodation(id)
                .map(modelMapper::map)
                .orElseThrow(() -> new NotFoundException(ACCOMMODATION_NOT_FOUND + id));
    }

    public List<Accommodation> searchAccommodations(LocalDate fromDate, LocalDate toDate) {
        validationUtils.validateDates(fromDate, toDate);
        return accommodationRepository.searchAccommodations(fromDate, toDate)
                .stream()
                .map(modelMapper::map)
                .toList();
    }
}
