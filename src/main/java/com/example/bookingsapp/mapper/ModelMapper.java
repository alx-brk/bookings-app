package com.example.bookingsapp.mapper;

import com.openapi.samples.gen.springbootserver.model.Accommodation;
import com.openapi.samples.gen.springbootserver.model.CreateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.CreateReservationRequest;
import com.openapi.samples.gen.springbootserver.model.Reservation;
import com.openapi.samples.gen.springbootserver.model.ReservationType;
import com.openapi.samples.gen.springbootserver.model.UpdateAccommodationRequest;
import com.openapi.samples.gen.springbootserver.model.UpdateReservationRequest;
import com.tej.JooQDemo.jooq.sample.model.Tables;
import com.tej.JooQDemo.jooq.sample.model.tables.records.AccommodationRecord;
import com.tej.JooQDemo.jooq.sample.model.tables.records.ReservationRecord;
import org.jooq.DSLContext;
import org.jooq.impl.TableRecordImpl;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class ModelMapper {

    public static final String ACCOMMODATION_ENTITY = AccommodationRecord.class.getSimpleName();
    public static final String RESERVATION_ENTITY = ReservationRecord.class.getSimpleName();
    @Autowired
    private DSLContext dslContext;

    public abstract AccommodationRecord map(CreateAccommodationRequest request);

    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "address", source = "request.address")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "checkInTime", source = "request.checkInTime")
    @Mapping(target = "checkOutTime", source = "request.checkOutTime")
    public abstract AccommodationRecord map(UpdateAccommodationRequest request, AccommodationRecord current);

    public abstract Accommodation map(AccommodationRecord record);

    @AfterMapping
    protected void enrichAccommodation(@MappingTarget AccommodationRecord target,
                                       UpdateAccommodationRequest request,
                                       AccommodationRecord current) {
        if (Objects.isNull(request.getName())) target.setName(current.getName());
        if (Objects.isNull(request.getAddress())) target.setAddress(current.getAddress());
        if (Objects.isNull(request.getPrice())) target.setPrice(current.getPrice());
        if (Objects.isNull(request.getCheckInTime())) target.setCheckInTime(current.getCheckInTime());
        if (Objects.isNull(request.getCheckOutTime())) target.setCheckOutTime(current.getCheckOutTime());
    }

    public abstract ReservationRecord map(CreateReservationRequest request, ReservationType reservationType);
    public abstract Reservation map(ReservationRecord record);

    public abstract List<Reservation> map(List<ReservationRecord> records);

    @Mapping(target = "accommodationId", source = "current.accommodationId")
    @Mapping(target = "startDate", source = "request.startDate")
    @Mapping(target = "endDate", source = "request.endDate")
    @Mapping(target = "guestName", source = "request.guestName")
    public abstract ReservationRecord map(UpdateReservationRequest request, ReservationRecord current);

    @AfterMapping
    protected void enrichReservation(@MappingTarget ReservationRecord target,
                                     UpdateReservationRequest request,
                                     ReservationRecord current) {
        if (Objects.isNull(request.getStartDate())) target.setStartDate(current.getStartDate());
        if (Objects.isNull(request.getEndDate())) target.setEndDate(current.getEndDate());
        if (Objects.isNull(request.getGuestName())) target.setGuestName(current.getGuestName());
    }

    @ObjectFactory
    @SuppressWarnings("unchecked cast")
    protected <T extends TableRecordImpl<T>> T init(@TargetType Class<T> targetClass) {
        if (targetClass.getSimpleName().equals(ACCOMMODATION_ENTITY)) {
            return (T) dslContext.newRecord(Tables.ACCOMMODATION);
        } else if (targetClass.getSimpleName().equals(RESERVATION_ENTITY)) {
            return (T) dslContext.newRecord(Tables.RESERVATION);
        }
        throw new IllegalArgumentException();
    }
}
