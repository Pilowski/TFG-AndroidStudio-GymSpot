<?php

require_once '../config/db.php';
require_once '../includes/functions.php';

$id_usuario = $_GET['id_usuario'] ?? null;

if (!$id_usuario) {
    response("error", "ID de usuario no recibido");
}

// Base URL para las imágenes
$base_url = "http://10.0.2.2/ProyectoReservaClases/backend/uploads/";

// Obtener todas las clases
$stmt = $pdo->query("SELECT * FROM clases ORDER BY fecha, hora");
$clases = $stmt->fetchAll(PDO::FETCH_ASSOC);

foreach ($clases as &$clase) {
    $id_clase = $clase['id'];

    // Verificar si el usuario está inscrito en esta clase
    $stmtCheck = $pdo->prepare("SELECT COUNT(*) FROM reservas WHERE id_usuario = ? AND id_clase = ?");
    $stmtCheck->execute([$id_usuario, $id_clase]);
    $clase['inscrito'] = $stmtCheck->fetchColumn() > 0;

    // Asegurar que imagen_url sea una URL completa
    if (!empty($clase['imagen_url'])) {
        $clase['imagen_url'] = $base_url . $clase['imagen_url'];
    } else {
        // Imagen por defecto si no tiene una definida
        $clase['imagen_url'] = $base_url . "default.jpeg";
    }
}

response("success", "Lista de clases", $clases);
