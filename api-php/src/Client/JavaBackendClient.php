<?php

declare(strict_types=1);

namespace FinanceHub\Gateway\Client;

/**
 * Cliente HTTP responsável por encaminhar requisições da camada intermediária PHP
 * para o monólito Java 21 (HttpServer). Nenhuma regra de negócio financeira reside aqui.
 */
final class JavaBackendClient
{
    private string $javaBaseUrl;

    public function __construct(?string $javaBaseUrl = null)
    {
        $envUrl = getenv('JAVA_API_BASE_URL');
        $this->javaBaseUrl = rtrim($javaBaseUrl ?: ($envUrl ?: 'http://localhost:8080'), '/');
    }

    /**
     * @return array{status: int, contentType: string, body: string}
     */
    public function encaminhar(string $method, string $pathAndQuery, string $rawBody = ''): array
    {
        $url = $this->javaBaseUrl . $pathAndQuery;
        $headers = [
            'Accept: application/json, text/csv, */*',
            'Content-Type: application/json; charset=utf-8',
        ];

        $contextOptions = [
            'http' => [
                'method'        => strtoupper($method),
                'header'        => implode("\r\n", $headers) . "\r\n",
                'content'       => $rawBody,
                'ignore_errors' => true,
                'timeout'       => 10,
            ],
        ];

        $context = stream_context_create($contextOptions);
        $responseBody = @file_get_contents($url, false, $context);

        if ($responseBody === false) {
            return [
                'status'      => 502,
                'contentType' => 'application/json; charset=utf-8',
                'body'        => json_encode([
                    'code'    => 'JAVA_BACKEND_UNAVAILABLE',
                    'message' => 'Nao foi possivel conectar ao monolito Java em ' . $this->javaBaseUrl,
                ], JSON_UNESCAPED_UNICODE),
            ];
        }

        $status = 200;
        $contentType = 'application/json; charset=utf-8';

        if (isset($http_response_header) && is_array($http_response_header)) {
            foreach ($http_response_header as $headerLine) {
                if (preg_match('#^HTTP/\S+\s+(\d{3})#i', $headerLine, $matches)) {
                    $status = (int) $matches[1];
                } elseif (stripos($headerLine, 'Content-Type:') === 0) {
                    $contentType = trim(substr($headerLine, strlen('Content-Type:')));
                }
            }
        }

        return [
            'status'      => $status,
            'contentType' => $contentType,
            'body'        => $responseBody,
        ];
    }
}

