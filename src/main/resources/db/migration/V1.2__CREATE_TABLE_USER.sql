-- CREATE TABLE --
CREATE TABLE users
(
    IDT_USER   BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT 'User unique ID. Auto increment',
    EMAIL      VARCHAR(255) NOT NULL UNIQUE COMMENT 'Email of the user',
    PASSWORD   VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
    CREATED_AT TIMESTAMP NOT NULL COMMENT 'Date and time of user creation'
) ENGINE=InnoDB;