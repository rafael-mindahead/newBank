<?php

declare(strict_types=1);

require_once __DIR__ . '/../src/Client/JavaBackendClient.php';
require_once __DIR__ . '/../src/Http/GatewayController.php';

use FinanceHub\Gateway\Client\JavaBackendClient;
use FinanceHub\Gateway\Http\GatewayController;

$client = new JavaBackendClient();
$controller = new GatewayController($client);
$controller->processarRequisicao();

