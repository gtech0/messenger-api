create table public.users
(
    id                varchar(255) not null
        primary key,
    avatar            varchar(255),
    birth_date        date,
    city              varchar(255),
    email             varchar(255)
        constraint uk_6dotkott2kjsp8vw4d0m25fb7
            unique,
    full_name         varchar(255),
    login             varchar(255)
        constraint uk_ow0gan20590jrb00upg3va2fn
            unique,
    password          varchar(255),
    phone_number      varchar(255),
    registration_date date
);

alter table public.users
    owner to postgres;
