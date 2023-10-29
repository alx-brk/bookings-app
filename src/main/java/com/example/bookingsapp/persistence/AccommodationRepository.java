package com.example.bookingsapp.persistence;

import com.tej.JooQDemo.jooq.sample.model.tables.records.AccommodationRecord;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.tej.JooQDemo.jooq.sample.model.Tables.ACCOMMODATION;
import static com.tej.JooQDemo.jooq.sample.model.Tables.RESERVATION;

@Repository
@AllArgsConstructor
public class AccommodationRepository {

    private final DSLContext dslContext;

    public AccommodationRecord createAccommodation(AccommodationRecord record) {
        return dslContext.insertInto(ACCOMMODATION)
                .set(record)
                .returning()
                .fetchOne();
    }

    public Optional<AccommodationRecord> getAccommodation(Long id) {
        return dslContext.selectFrom(ACCOMMODATION)
                .where(ACCOMMODATION.ACCOMMODATION_ID.eq(id))
                .fetchOptional();
    }

    public AccommodationRecord updateAccommodation(Long id, AccommodationRecord record) {
        return dslContext.update(ACCOMMODATION)
                .set(record)
                .where(ACCOMMODATION.ACCOMMODATION_ID.eq(id))
                .returning()
                .fetchOne();
    }

    public List<AccommodationRecord> searchAccommodations(LocalDate fromDate, LocalDate toDate) {
        return dslContext.selectFrom(ACCOMMODATION)
                .where(ACCOMMODATION.ACCOMMODATION_ID.notIn(
                        DSL.select(RESERVATION.ACCOMMODATION_ID)
                                .from(RESERVATION)
                                .where(
                                        RESERVATION.END_DATE.greaterThan(fromDate)
                                                .and(RESERVATION.START_DATE.lessThan(toDate))
                                )
                ))
                .fetch();
    }

    public void deleteAll() {
        dslContext.deleteFrom(ACCOMMODATION)
                .execute();
    }


}
