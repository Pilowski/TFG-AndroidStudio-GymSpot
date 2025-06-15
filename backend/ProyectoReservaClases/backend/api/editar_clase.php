<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

header('Content-Type: application/json');

$id_clase = $_POST['id_clase'] ?? null;
$nombre = $_POST['nombre'] ?? null;
$fecha = $_POST['fecha'] ?? null;
$hora = $_POST['hora'] ?? null;
$cupo_maximo = $_POST['cupo_maximo'] ?? null;

if (!$id_clase || !$nombre || !$fecha || !$hora || !$cupo_maximo) {
    response("error", "Faltan datos obligatorios.");
}

// Obtener clase actual para saber si tiene imagen previa
$stmt = $pdo->prepare("SELECT imagen_url FROM clases WHERE id = ?");
$stmt->execute([$id_clase]);
$clase = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$clase) {
    response("error", "Clase no encontrada.");
}

// Si se sube una nueva imagen
if (isset($_FILES['imagen']) && $_FILES['imagen']['error'] === UPLOAD_ERR_OK) {
    $imagen = $_FILES['imagen'];
    $nombreImagen = time() . "_" . basename($imagen['name']);
    $rutaDestino = "../uploads/" . $nombreImagen;

    if (!move_uploaded_file($imagen['tmp_name'], $rutaDestino)) {
        response("error", "Error al guardar la nueva imagen.");
    }

    // Actualizar con nueva imagen
    $stmt = $pdo->prepare("UPDATE clases SET nombre = ?, fecha = ?, hora = ?, cupo_maximo = ?, imagen_url = ? WHERE id = ?");
    $stmt->execute([$nombre, $fecha, $hora, $cupo_maximo, $nombreImagen, $id_clase]);

} else {
    // Sin imagen nueva, solo actualizar texto
    $stmt = $pdo->prepare("UPDATE clases SET nombre = ?, fecha = ?, hora = ?, cupo_maximo = ? WHERE id = ?");
    $stmt->execute([$nombre, $fecha, $hora, $cupo_maximo, $id_clase]);
}

response("success", "Clase actualizada correctamente.");
