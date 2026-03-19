CREATE DATABASE IF NOT EXISTS roadmap_mvp;
CREATE USER IF NOT EXISTS 'Roadmap'@'localhost' IDENTIFIED BY 'roadmap123';
GRANT ALL PRIVILEGES ON roadmap_mvp.* TO 'Roadmap'@'localhost';
FLUSH PRIVILEGES;
