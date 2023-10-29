set schema_search_path = PUBLIC;

create table if not exists accommodation (
    accommodation_id bigint  not null auto_increment,
    name             text    not null,
    address          text    not null,
    price            bigint  not null,
    check_in_time    integer not null,
    check_out_time   integer not null,
    created_date     timestamp default current_timestamp(),
    last_updated     timestamp default current_timestamp(),
    constraint pk_accommodation_id primary key (accommodation_id)
);

create table if not exists reservation (
    reservation_id   bigint not null auto_increment,
    accommodation_id bigint not null,
    reservation_type text   not null,
    start_date       date   not null,
    end_date         date   not null,
    guest_name       text,
    created_date     timestamp default current_timestamp(),
    last_updated     timestamp default current_timestamp(),
    constraint pk_reservation_id primary key (reservation_id),
    constraint fk_accommodation_id foreign key (accommodation_id)
        references accommodation(accommodation_id)
)
