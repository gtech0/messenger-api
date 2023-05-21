create table public.friends
(
    id          varchar(255) not null
        primary key,
    add_date    date,
    delete_date date,
    friend_id   varchar(255),
    friend_name varchar(255),
    user_id     varchar(255),
    constraint ukk3jl1difk6e2tixicas048c9o
        unique (user_id, friend_id)
);

alter table public.friends
    owner to postgres;

create table public.blacklist
(
    id          varchar(255) not null
        primary key,
    add_date    date,
    delete_date date,
    friend_id   varchar(255),
    friend_name varchar(255),
    user_id     varchar(255),
    constraint ukppbx62or6ccl3279ysf0la0fe
        unique (user_id, friend_id)
);

alter table public.blacklist
    owner to postgres;

