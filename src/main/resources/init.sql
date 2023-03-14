CREATE USER 'peter'@'localhost' IDENTIFIED BY 'peter';

GRANT ALL PRIVILEGES ON appointmentscheduler.* to 'peter'@'%';

GRANT PROCESS ON *.* TO  'peter'@'%';

