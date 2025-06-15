<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

$id = $data['id_usuario'] ?? null;
$password_actual = $data['password_actual'] ?? null;
$password_nueva = $data['password_nueva'] ?? null;

if (!$id || !$password_actual || !$password_nueva) {
    response("error", "Faltan datos obligatorios.");
}

// Validación de seguridad de contraseña
if (
    strlen($password_nueva) < 8 ||
    !preg_match('/[A-Z]/', $password_nueva) ||        // mayúscula
    !preg_match('/[0-9]/', $password_nueva) ||        // número
    !preg_match('/[\W]/', $password_nueva)            // símbolo especial
) {
    response("error", "La nueva contraseña debe tener al menos una mayúscula, un número, un símbolo especial y 8 caracteres.");
}

// Buscar usuario
$stmt = $pdo->prepare("SELECT password FROM usuarios WHERE id = ?");
$stmt->execute([$id]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$usuario) {
    response("error", "Usuario no encontrado.");
}

// Verificar contraseña actual
if (!password_verify($password_actual, $usuario['password'])) {
    response("error", "La contraseña actual no es correcta.");
}

// Actualizar con nueva contraseña (encriptada)
$nueva_hash = password_hash($password_nueva, PASSWORD_DEFAULT);
$stmt = $pdo->prepare("UPDATE usuarios SET password = ? WHERE id = ?");
$success = $stmt->execute([$nueva_hash, $id]);

if ($success) {
    response("success", "Contraseña actualizada correctamente.");
} else {
    response("error", "No se pudo actualizar la contraseña.");
}
