<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$data = json_decode(file_get_contents("php://input"), true);

$id = $data['id_usuario'] ?? null;
$nombre = $data['nombre'] ?? null;
$apellidos = $data['apellidos'] ?? null;
$edad = $data['edad'] ?? null;
$sexo = $data['sexo'] ?? null;
$email = $data['email'] ?? null;

if (!$id || !$nombre || !$apellidos || !$edad || !$sexo || !$email) {
    response("error", "Faltan datos obligatorios.");
}

$stmt = $pdo->prepare("UPDATE usuarios SET nombre = ?, apellidos = ?, edad = ?, sexo = ?, email = ? WHERE id = ?");
$exito = $stmt->execute([$nombre, $apellidos, $edad, $sexo, $email, $id]);

if ($exito) {
    response("success", "Perfil actualizado correctamente.");
} else {
    response("error", "No se pudo actualizar el perfil.");
}
