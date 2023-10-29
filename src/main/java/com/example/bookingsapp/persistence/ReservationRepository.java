package com.example.bookingsapp.persistence;

import com.openapi.samples.gen.springbootserver.model.ReservationType;
import com.tej.JooQDemo.jooq.sample.model.tables.records.ReservationRecord;
import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.tej.JooQDemo.jooq.sample.model.Tables.RESERVATION;

@Repository
@AllArgsConstructor
public class ReservationRepository {

    private final DSLContext dslContext;

    public ReservationRecord createReservation(ReservationRecord record) {
        return dslContext.insertInto(RESERVATION)
                .set(record)
                .returning()
                .fetchOne();
    }

    public Optional<ReservationRecord> getReservation(Long id) {
        return dslContext.selectFrom(RESERVATION)
                .where(RESERVATION.RESERVATION_ID.eq(id))
                .fetchOptional();
    }

    public ReservationRecord updateReservation(Long id, ReservationRecord record) {
        return dslContext.update(RESERVATION)
                .set(record)
                .where(RESERVATION.RESERVATION_ID.eq(id))
                .returning()
                .fetchOne();
    }

    public void deleteReservation(Long id) {
        dslContext.deleteFrom(RESERVATION)
                .where(RESERVATION.RESERVATION_ID.eq(id))
                .execute();
    }

    public List<ReservationRecord> searchReservations(Long accommodationId,
                                                      ReservationType reservationType,
                                                      LocalDate fromDate,
                                                      LocalDate toDate) {
        List<Condition> conditions = new ArrayList<>();
        Optional.ofNullable(accommodationId)
                .ifPresent(id -> conditions.add(RESERVATION.ACCOMMODATION_ID.eq(id)));
        Optional.ofNullable(reservationType)
                .ifPresent(type -> conditions.add(RESERVATION.RESERVATION_TYPE.eq(type.getValue())));
        Optional.ofNullable(fromDate)
                .ifPresent(date -> conditions.add(RESERVATION.END_DATE.greaterThan(date)));
        Optional.ofNullable(toDate)
                .ifPresent(date -> conditions.add(RESERVATION.START_DATE.lessThan(date)));

        return dslContext.selectFrom(RESERVATION)
                .where(conditions)
                .fetch();
    }

    public void deleteAll() {
        dslContext.deleteFrom(RESERVATION)
                .execute();
    }


}
