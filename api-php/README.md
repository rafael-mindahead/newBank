# API intermediária PHP

Camada de transporte entre os clientes (web, Android Kotlin e iOS Swift) e o monólito Java.

Planejado:
- `public/`: ponto de entrada HTTP e roteamento em `/api/v1`.
- `src/Http/`: tratamento de request/response e códigos HTTP.
- `src/Client/`: cliente HTTP para o servidor Java.
- `tests/`: testes do gateway.

Somente validações de transporte/autenticação quando previstas; as regras financeiras devem permanecer em Java. Esta pasta ainda não implementa endpoints.
