<?php

declare(strict_types=1);

namespace FinanceHub\Gateway\Http;

use FinanceHub\Gateway\Client\JavaBackendClient;

/**
 * Controlador de transporte HTTP do Gateway PHP (/api/v1/*).
 */
final class GatewayController
{
    private JavaBackendClient $client;

    public function __construct(JavaBackendClient $client)
    {
        $this->client = $client;
    }

    public function processarRequisicao(): void
    {
        header('Access-Control-Allow-Origin: *');
        header('Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS');
        header('Access-Control-Allow-Headers: Content-Type, Accept');

        $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
        if (strtoupper($method) === 'OPTIONS') {
            http_response_code(204);
            return;
        }

        $requestUri = $_SERVER['REQUEST_URI'] ?? '/api/v1/health';
        if (strpos($requestUri, '/api/v1/') !== 0 && $requestUri !== '/api/v1') {
            http_response_code(404);
            header('Content-Type: application/json; charset=utf-8');
            echo json_encode([
                'code'    => 'ROTA_NAO_ENCONTRADA',
                'message' => 'A API intermediaria PHP atende exclusivamente em /api/v1/*',
            ], JSON_UNESCAPED_UNICODE);
            return;
        }

        $rawBody = file_get_contents('php://input') ?: '';
        $resposta = $this->client->encaminhar($method, $requestUri, $rawBody);

        http_response_code($resposta['status']);
        header('Content-Type: ' . $resposta['contentType']);
        echo $resposta['body'];
    }
}

