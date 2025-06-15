<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

$nombre = $data['nombre'] ?? null;
$apellidos = $data['apellidos'] ?? null;
$edad = $data['edad'] ?? null;
$sexo = $data['sexo'] ?? null;
$email = $data['email'] ?? null;
$password = $data['password'] ?? null;

if (!$nombre || !$apellidos || !$edad || !$sexo || !$email || !$password) {
    response("error", "Faltan datos obligatorios.");
}

// Validación de contraseña segura
if (
    strlen($password) < 8 ||
    !preg_match('/[A-Z]/', $password) ||        // mayúscula
    !preg_match('/[0-9]/', $password) ||        // número
    !preg_match('/[\W]/', $password)            // símbolo especial
) {
    response("error", "La contraseña debe tener al menos una mayúscula, un número, un símbolo especial y 8 caracteres.");
}

// Verificar si el usuario ya existe
$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->fetch()) {
    response("error", "El correo ya está registrado.");
}

// Crear usuario
$password_hash = password_hash($password, PASSWORD_DEFAULT);
$stmt = $pdo->prepare("INSERT INTO usuarios (nombre, apellidos, edad, sexo, email, password, rol) VALUES (?, ?, ?, ?, ?, ?, 'usuario')");
$success = $stmt->execute([$nombre, $apellidos, $edad, $sexo, $email, $password_hash]);

if ($success) {
    response("success", "Registro exitoso.");
} else {
    response("error", "Error al registrar usuario.");
}
