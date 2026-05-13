DROP DATABASE IF EXISTS imdbload;
CREATE DATABASE imdbload;
USE imdbload;

CREATE TABLE aka_name (
    id integer NOT NULL PRIMARY KEY,
    person_id integer NOT NULL,
    name character varying,
    imdb_index character varying(3),
    name_pcode_cf character varying(11),
    name_pcode_nf character varying(11),
    surname_pcode character varying(11),
    md5sum character varying(65)
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);



CREATE TABLE aka_title (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    title character varying,
    imdb_index character varying(4),
    kind_id integer NOT NULL,
    production_year integer,
    phonetic_code character varying(5),
    episode_of_id integer,
    season_nr integer,
    episode_nr integer,
    note character varying(72),
    md5sum character varying(32)
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000);

CREATE TABLE cast_info (
    id integer NOT NULL PRIMARY KEY,
    person_id integer NOT NULL,
    movie_id integer NOT NULL,
    person_role_id integer,
    note character varying,
    nr_order integer,
    role_id integer NOT NULL
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);

CREATE TABLE char_name (
    id integer NOT NULL PRIMARY KEY,
    name character varying NOT NULL,
    imdb_index character varying(2),
    imdb_id integer,
    name_pcode_nf character varying(5),
    surname_pcode character varying(5),
    md5sum character varying(32)
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);

CREATE TABLE comp_cast_type (
    id integer NOT NULL PRIMARY KEY,
    kind character varying(32) NOT NULL
);

CREATE TABLE company_name (
    id integer NOT NULL PRIMARY KEY,
    name character varying NOT NULL,
    country_code character varying(6),
    imdb_id integer,
    name_pcode_nf character varying(5),
    name_pcode_sf character varying(5),
    md5sum character varying(32)
)partition by range values (30000),(60000),(90000),(120000),(150000),(180000),(210000),(240000);

CREATE TABLE company_type (
    id integer NOT NULL PRIMARY KEY,
    kind character varying(32)
);

CREATE TABLE complete_cast (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer,
    subject_id integer NOT NULL,
    status_id integer NOT NULL
)partition by range values (20000),(40000),(60000),(80000),(100000),(120000),(140000);

CREATE TABLE info_type (
    id integer NOT NULL PRIMARY KEY,
    info character varying(32) NOT NULL
);

CREATE TABLE keyword (
    id integer NOT NULL PRIMARY KEY,
    keyword character varying NOT NULL,
    phonetic_code character varying(5)
)partition by range values (30000),(60000),(90000),(120000);

CREATE TABLE kind_type (
    id integer NOT NULL PRIMARY KEY,
    kind character varying(15)
);

CREATE TABLE link_type (
    id integer NOT NULL PRIMARY KEY,
    link character varying(32) NOT NULL
);

CREATE TABLE movie_companies (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    company_id integer NOT NULL,
    company_type_id integer NOT NULL,
    note character varying
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE movie_info_idx (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info character varying NOT NULL,
    note character varying(1)
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE movie_keyword (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    keyword_id integer NOT NULL
)partition by range values(30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE movie_link (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    linked_movie_id integer NOT NULL,
    link_type_id integer NOT NULL
)partition by range values (5000),(10000),(15000),(20000),(25000),(30000);

CREATE TABLE name (
    id integer NOT NULL PRIMARY KEY,
    name character varying NOT NULL,
    imdb_index character varying(9),
    imdb_id integer,
    gender character varying(1),
    name_pcode_cf character varying(5),
    name_pcode_nf character varying(5),
    surname_pcode character varying(5),
    md5sum character varying(32)
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE role_type (
    id integer NOT NULL PRIMARY KEY,
    role character varying(32) NOT NULL
);

CREATE TABLE title (
    id integer NOT NULL PRIMARY KEY,
    title character varying NOT NULL,
    imdb_index character varying(5),
    kind_id integer NOT NULL,
    production_year integer,
    imdb_id integer,
    phonetic_code character varying(5),
    episode_of_id integer,
    season_nr integer,
    episode_nr integer,
    series_years character varying(49),
    md5sum character varying(32)
)partition by range values(30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE movie_info (
    id integer NOT NULL PRIMARY KEY,
    movie_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info character varying NOT NULL,
    note character varying
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);


CREATE TABLE person_info (
    id integer NOT NULL PRIMARY KEY,
    person_id integer NOT NULL,
    info_type_id integer NOT NULL,
    info character varying NOT NULL,
    note character varying
)partition by range values (30000),(60000),(90000),(120000),(150000),
(180000),(210000),(240000),(270000),(300000),
(330000),(360000),(390000),(420000),(450000),
(480000);
