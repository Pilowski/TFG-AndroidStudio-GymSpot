<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

// Validar presencia de datos
if (!isset($data['id_usuario'], $data['id_clase'])) {
    response("error", "Faltan datos.");
}

$id_usuario = $data['id_usuario'];
$id_clase = $data['id_clase'];

// Verificar si la clase existe
$stmt = $pdo->prepare("SELECT id, cupo_actual FROM clases WHERE id = ?");
$stmt->execute([$id_clase]);
$clase = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$clase) {
    response("error", "La clase no existe.");
}

// Verificar si la reserva existe antes de eliminar
$stmt = $pdo->prepare("SELECT COUNT(*) FROM reservas WHERE id_usuario = ? AND id_clase = ?");
$stmt->execute([$id_usuario, $id_clase]);

if ($stmt->fetchColumn() == 0) {
    response("error", "No estás registrado en esta clase.");
}

// Eliminar la reserva
$stmt = $pdo->prepare("DELETE FROM reservas WHERE id_usuario = ? AND id_clase = ?");
$stmt->execute([$id_usuario, $id_clase]);

// Actualizar el cupo solo si cupo_actual > 0
if ((int)$clase['cupo_actual'] > 0) {
    $stmt = $pdo->prepare("UPDATE clases SET cupo_actual = cupo_actual - 1 WHERE id = ?");
    $stmt->execute([$id_clase]);
}

response("success", "Reserva cancelada correctamente.");


