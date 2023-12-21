CREATE DATABASE IF NOT EXISTS provlegisto CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE provlegisto;

CREATE TABLE user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    username CHAR(32) UNIQUE NOT NULL
        COMMENT 'Username used to login',
    display_name CHAR(32) NOT NULL
        COMMENT 'Name that display on UI',
    password_md5 CHAR(32) NOT NULL,
    real_name CHAR(64) NULL
        COMMENT 'Save real name of user if need, nullable',
    supervisor INT NOT NULL DEFAULT 0
        COMMENT 'Boolean value, supervisor can enter other room without password and hide from online users'
);


CREATE TRIGGER before_insert_user
    BEFORE INSERT ON user
    FOR EACH ROW
    SET NEW.display_name = COALESCE(NEW.display_name, CONCAT('anonymous', CEIL(RAND() * 100)));