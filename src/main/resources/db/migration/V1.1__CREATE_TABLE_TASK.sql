-- CREATE TABLE --

CREATE TABLE task
(
    IDT_TASK  BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT 'Task unique ID. Auto increment.',
    CONTENT   VARCHAR(400) NOT NULL COMMENT 'Content of the task.',
    COMPLETED BOOLEAN      NOT NULL COMMENT 'Mark as the taks is completed or not. 0 = not completed; 1 = completed.'
) ENGINE=InnoDB;
