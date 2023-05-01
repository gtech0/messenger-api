create table public.attachment
(
    id         varchar(255) not null
        primary key,
    file_id    varchar(255),
    file_name  varchar(255),
    message_id varchar(255)
);

alter table public.attachment
    owner to postgres;

create table public.chat
(
    id            varchar(255) not null
        primary key,
    admin_id      varchar(255),
    avatar        varchar(255),
    creation_date date,
    friend_id     varchar(255),
    name          varchar(255),
    type          varchar(255),
    user_id       varchar(255),
    constraint ukqewc45mvlfdmbpjl2e0f57hhr
        unique (user_id, friend_id)
);

alter table public.chat
    owner to postgres;

create table public.chat_user
(
    id      varchar(255) not null
        primary key,
    chat_id varchar(255),
    user_id varchar(255)
);

alter table public.chat_user
    owner to postgres;

create table public.message
(
    id        varchar(255) not null
        primary key,
    avatar    varchar(255),
    chat_id   varchar(255),
    full_name varchar(255),
    message   varchar(255),
    sent_date date,
    user_id   varchar(255)
);

alter table public.message
    owner to postgres;

