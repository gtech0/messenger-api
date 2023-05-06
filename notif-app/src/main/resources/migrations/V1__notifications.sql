create table public.notification
(
    id            varchar(255) not null
        primary key,
    received_date timestamp,
    status        integer,
    text          varchar(255),
    type          integer,
    user_id       varchar(255)
);

alter table public.notification
    owner to postgres;

