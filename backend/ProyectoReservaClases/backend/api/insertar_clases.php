<?php
require_once '../config/db.php';

$clases = [
    ['nombre' => 'Yoga Matutino', 'fecha' => '2025-05-15', 'hora' => '09:00:00', 'cupo_maximo' => 10],
    ['nombre' => 'Spinning Intenso', 'fecha' => '2025-05-16', 'hora' => '18:00:00', 'cupo_maximo' => 15],
    ['nombre' => 'Pilates Suave', 'fecha' => '2025-05-17', 'hora' => '10:30:00', 'cupo_maximo' => 8],
];

foreach ($clases as $clase) {
    $stmt = $pdo->prepare("INSERT INTO clases (nombre, fecha, hora, cupo_maximo) VALUES (?, ?, ?, ?)");
    $stmt->execute([$clase['nombre'], $clase['fecha'], $clase['hora'], $clase['cupo_maximo']]);
}

echo "Clases insertadas correctamente.";
