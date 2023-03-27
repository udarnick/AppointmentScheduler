CREATE USER 'peter'@'172.26.0.3' IDENTIFIED BY 'peter' WITH PASSWORD 'peter';

CREATE USER 'peter'@'%' IDENTIFIED BY 'peter' WITH PASSWORD 'peter';

CREATE DATABASE appointmentscheduler;

GRANT ALL PRIVILEGES ON appointmentscheduler.* to 'peter'@'172.26.0.3';

GRANT ALL PRIVILEGES ON appointmentscheduler.* to 'peter'@'%';