<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

$id = $data['id_usuario'] ?? null;
$nombre = $data['nombre'] ?? null;
$email = $data['email'] ?? null;
$password_actual = $data['password_actual'] ?? null;
$nueva_password = $data['nueva_password'] ?? null;

if (!$id || !$nombre || !$email) {
    response("error", "Faltan datos obligatorios");
}

// Validar que no se repita el correo con otro usuario
$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE email = ? AND id != ?");
$stmt->execute([$email, $id]);
if ($stmt->fetch()) {
    response("error", "El correo ya está en uso por otro usuario");
}

// Obtener datos actuales
$stmt = $pdo->prepare("SELECT password FROM usuarios WHERE id = ?");
$stmt->execute([$id]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$usuario) {
    response("error", "Usuario no encontrado");
}

// Si quiere cambiar la contraseña, validamos primero la actual
if (!empty($nueva_password) && !empty($password_actual)) {
    if (!password_verify($password_actual, $usuario['password'])) {
        response("error", "La contraseña actual no es correcta");
    }

    $nuevo_hash = password_hash($nueva_password, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare("UPDATE usuarios SET nombre = ?, email = ?, password = ? WHERE id = ?");
    $stmt->execute([$nombre, $email, $nuevo_hash, $id]);
} else {
    // Solo actualizar nombre y email
    $stmt = $pdo->prepare("UPDATE usuarios SET nombre = ?, email = ? WHERE id = ?");
    $stmt->execute([$nombre, $email, $id]);
}

response("success", "Datos actualizados correctamente", ["nombre" => $nombre, "email" => $email]);
