CREATE DATABASE IF NOT EXISTS gimnasio;
USE gimnasio;
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellidos VARCHAR(100),
    edad INT,
    sexo ENUM ('hombre', 'mujer'),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255)
);
CREATE TABLE clases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    fecha DATE,
    hora TIME,
    cupo_maximo INT,
    cupo_actual INT DEFAULT 0
);
CREATE TABLE reservas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_clase INT,
    fecha_reserva TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_clase) REFERENCES clases(id)
);