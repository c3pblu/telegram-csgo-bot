CREATE USER 'bot'@'%' IDENTIFIED BY 'changeme';
ALTER USER 'bot'@'%' PASSWORD EXPIRE NEVER;
CREATE SCHEMA `bot` CHARACTER SET utf8 COLLATE utf8_general_ci;
GRANT ALL ON `bot`.* TO `bot`@'%';