# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "assignementGrading" ("id" SERIAL NOT NULL PRIMARY KEY,"name" VARCHAR(254) NOT NULL,"grade" INTEGER NOT NULL);
create table "team" ("id" SERIAL NOT NULL PRIMARY KEY,"name1" VARCHAR(254) NOT NULL,"name2" VARCHAR(254) NOT NULL);
create table "userGrading" ("id" SERIAL NOT NULL PRIMARY KEY,"userId" INTEGER NOT NULL,"gradingId" INTEGER NOT NULL);

# --- !Downs

drop table "assignementGrading";
drop table "team";
drop table "userGrading";

