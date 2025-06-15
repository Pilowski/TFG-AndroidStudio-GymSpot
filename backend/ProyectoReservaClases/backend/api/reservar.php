<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['id_usuario'], $data['id_clase'])) {
    response("error", "Faltan datos.");
}

// Verificar cupo
$stmt = $pdo->prepare("SELECT cupo_maximo, cupo_actual FROM clases WHERE id = ?");
$stmt->execute([$data['id_clase']]);
$clase = $stmt->fetch(PDO::FETCH_ASSOC);

if ($clase['cupo_actual'] >= $clase['cupo_maximo']) {
    response("error", "No hay plazas disponibles.");
}

// Insertar reserva
$stmt = $pdo->prepare("INSERT INTO reservas (id_usuario, id_clase) VALUES (?, ?)");
$stmt->execute([$data['id_usuario'], $data['id_clase']]);

// Actualizar cupo
$stmt = $pdo->prepare("UPDATE clases SET cupo_actual = cupo_actual + 1 WHERE id = ?");
$stmt->execute([$data['id_clase']]);

response("success", "Reserva realizada con éxito.");
