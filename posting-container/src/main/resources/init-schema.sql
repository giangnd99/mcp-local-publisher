-- Schema khoi tao cho Postgres. Chay lai nhieu lan khong loi.
create schema if not exists posting;
set search_path to posting;

create table if not exists campaign (
    id            varchar(64)  primary key,
    name          varchar(255) not null,
    product_name  varchar(255) not null,
    price_amount  bigint       not null,
    warranty_text varchar(255),
    phone_number  varchar(32),
    content_objective varchar(32) not null default 'DIRECT_SALE',
    brand_name varchar(255) not null default 'Laptop GTH',
    brand_name_required boolean not null default false,
    photo_paths   text,
    status        varchar(32)  not null,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null
);

alter table campaign add column if not exists content_objective varchar(32) not null default 'DIRECT_SALE';
alter table campaign add column if not exists brand_name varchar(255) not null default 'Laptop GTH';
alter table campaign add column if not exists brand_name_required boolean not null default false;

create table if not exists fb_group (
    group_id        varchar(32) primary key,
    name            varchar(255) not null,
    member_count    integer      not null default 0,
    region          varchar(128),
    posting_allowed boolean      not null default true,
    last_post_at    timestamptz
);

create table if not exists post_job (
    id              varchar(64) primary key,
    campaign_id     varchar(64) not null,
    group_id        varchar(32) not null,
    group_name      varchar(255),
    content         text        not null,
    status          varchar(32) not null,
    attempt         integer     not null default 0,
    scheduled_at    timestamptz,
    last_attempt_at timestamptz,
    posted_at       timestamptz,
    fb_post_url     text,
    error_message   text,
    created_at      timestamptz not null,
    updated_at      timestamptz not null,
    constraint fk_post_job_campaign foreign key (campaign_id) references campaign (id)
);

create index if not exists idx_post_job_campaign_status on post_job (campaign_id, status);
create index if not exists idx_post_job_status_scheduled on post_job (status, scheduled_at);
create index if not exists idx_post_job_group on post_job (group_id);

create table if not exists job_event (
    id          varchar(64) primary key,
    post_job_id varchar(64) not null,
    event_type  varchar(32) not null,
    payload     text,
    created_at  timestamptz not null,
    constraint fk_job_event_post_job foreign key (post_job_id) references post_job (id)
);

create index if not exists idx_job_event_job on job_event (post_job_id, created_at);
