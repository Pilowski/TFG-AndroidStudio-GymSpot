<?php
require_once '../config/db.php';
require_once '../includes/functions.php';

$id_usuario = $_GET['id_usuario'] ?? null;

if (!$id_usuario) {
    response("error", "ID de usuario no proporcionado.");
}

$stmt = $pdo->prepare("SELECT id, nombre, apellidos, edad, sexo, email, rol FROM usuarios WHERE id = ?");
$stmt->execute([$id_usuario]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);

if ($usuario) {
    response("success", "Datos del usuario obtenidos.", $usuario);
} else {
    response("error", "Usuario no encontrado.");
}
