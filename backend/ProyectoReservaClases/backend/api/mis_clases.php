<?php

require_once '../config/db.php';
require_once '../includes/functions.php';

$id_usuario = $_GET['id_usuario'] ?? null;

if (!$id_usuario) {
    response("error", "Falta id_usuario");
}

$base_url = "http://10.0.2.2/ProyectoReservaClases/backend/uploads/";

$stmt = $pdo->prepare("
    SELECT c.id, c.nombre, c.fecha, c.hora, c.imagen_url
    FROM reservas r
    JOIN clases c ON r.id_clase = c.id
    WHERE r.id_usuario = ?
    ORDER BY c.fecha, c.hora
");
$stmt->execute([$id_usuario]);

$clases = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Adjuntar URL completa de imagen
foreach ($clases as &$clase) {
    if (!empty($clase['imagen_url'])) {
        $clase['imagen_url'] = $base_url . $clase['imagen_url'];
    } else {
        $clase['imagen_url'] = $base_url . "default.jpeg";
    }
}

response("success", "Clases registradas", $clases);

