create table public.file
(
    id   varchar(255) not null
        primary key,
    name varchar(255),
    size bigint
);

alter table public.file
    owner to postgres;

