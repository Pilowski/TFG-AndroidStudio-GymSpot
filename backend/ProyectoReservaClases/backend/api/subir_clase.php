<?php

require_once '../config/db.php';
require_once '../includes/functions.php';

$nombre = $_POST['nombre'] ?? null;
$fecha = $_POST['fecha'] ?? null;
$hora = $_POST['hora'] ?? null;
$cupo_maximo = $_POST['cupo_maximo'] ?? null;

if (!$nombre || !$fecha || !$hora || !$cupo_maximo || !isset($_FILES['imagen'])) {
    response("error", "Faltan datos o imagen.");
}

$imagen = $_FILES['imagen'];

// Generar nombre único para la imagen
$nombreImagen = time() . "_" . basename($imagen['name']);
$rutaDestino = "../uploads/" . $nombreImagen;

// Subir imagen al servidor
if (move_uploaded_file($imagen['tmp_name'], $rutaDestino)) {
    // Insertar clase en base de datos
    $stmt = $pdo->prepare("INSERT INTO clases (nombre, fecha, hora, cupo_maximo, imagen_url) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$nombre, $fecha, $hora, $cupo_maximo, $nombreImagen]);

    response("success", "Clase creada con imagen correctamente.");
} else {
    response("error", "Error al guardar la imagen.");
}

