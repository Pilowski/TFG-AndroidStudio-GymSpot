<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$id_clase = $_POST['id_clase'] ?? null;

if (!$id_clase) {
    response("error", "Falta el ID de la clase.");
}

// Obtener nombre de la imagen antes de eliminar
$stmt = $pdo->prepare("SELECT imagen_url FROM clases WHERE id = ?");
$stmt->execute([$id_clase]);
$clase = $stmt->fetch();

if (!$clase) {
    response("error", "Clase no encontrada.");
}

// Eliminar primero las reservas asociadas
$stmt = $pdo->prepare("DELETE FROM reservas WHERE id_clase = ?");
$stmt->execute([$id_clase]);

// Eliminar imagen del servidor si no es default
$imagen = $clase['imagen_url'];
$ruta = "../uploads/" . $imagen;
if ($imagen !== "clase_default.jpeg" && file_exists($ruta)) {
    unlink($ruta);
}

// Finalmente eliminar la clase
$stmt = $pdo->prepare("DELETE FROM clases WHERE id = ?");
$stmt->execute([$id_clase]);

response("success", "Clase eliminada correctamente.");

