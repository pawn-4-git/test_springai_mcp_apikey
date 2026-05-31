# test_springai_mcp_apikey

このプロジェクトは、Spring Boot と Spring AI を用いて構築された、APIキー認証機能付きの **Model Context Protocol (MCP) サーバー** のサンプル実装です。
MCPを使用することで、LLM（大規模言語モデル）から呼び出し可能なカスタムツールを提供することができます。

---

## ディレクトリ構成と主要ファイル

実装の本体は `src/main/java/com/example/demo` にあります。それぞれの役割は以下の通りです。

### 1. [DemoApplication.java](file:///Users/pawndeveloper/workspace/test_spring_mcp/src/main/java/com/example/demo/DemoApplication.java)
Spring Boot アプリケーションのエントリーポイントです。アプリケーションの起動を行います。

### 2. [McpServerConfiguration.java](file:///Users/pawndeveloper/workspace/test_spring_mcp/src/main/java/com/example/demo/McpServerConfiguration.java)
MCP サーバーの動作に必要な以下の各種設定を行っています。
- **セキュリティ（APIキー認証）の設定**:
  - `springaicommunity:mcp-server-security` ライブラリを使用して、すべてのエンドポイントにAPIキー認証を義務付けています。
  - インメモリのリポジトリに以下のテスト用APIキーが登録されています。
    - **ID**: `api01`
    - **シークレット (API Key)**: `mycustomapikey`
    - **キー名**: `test api key`
- **JSONデシリアライズ設定 (Jackson)**:
  - MCPの初期化リクエストなどのスキーマ定義において、未知のプロパティが含まれていてもエラーにせず無視するように `ObjectMapper` をカスタマイズし、MixIn を登録しています。
- **ツールの登録**:
  - [MyToolsService.java](file:///Users/pawndeveloper/workspace/test_spring_mcp/src/main/java/com/example/demo/MyToolsService.java) 内のメソッドを Spring AI の `ToolCallbackProvider` として登録し、MCPサーバー経由で外部（LLM等）からツールとして認識できるようにしています。

### 3. [MyToolsService.java](file:///Users/pawndeveloper/workspace/test_spring_mcp/src/main/java/com/example/demo/MyToolsService.java)
MCPサーバーから提供される実際のツール（Tool）のビジネスロジックを実装しています。
- **提供ツール**: `greeter` (挨拶ツール)
  - 引数 `language`: 挨拶の言語（`english`, `french` など。デフォルトは `english`）。
- **認証保護**:
  - メソッドに `@PreAuthorize("isAuthenticated()")` が付与されており、有効なAPIキーを持つ認証済みリクエストのみが実行可能です。
- **動作**:
  - `SecurityContextHolder` から認証済みのユーザー情報を取得し、その名前に応じた挨拶テキストを返します。
  - 例：英語なら `"Hello, [APIキー名]!"` （今回の設定では `Hello, test api key!`）、フランス語なら `"Salut [APIキー名]!"` を返します。

---

## 起動方法

### 前提条件
- Java 17 以上

### 起動コマンド
プロジェクトのルートディレクトリで以下のコマンドを実行し、Spring Boot アプリケーションを起動します。

```bash
./gradlew bootRun
```

デフォルトでは、アプリケーションは `http://localhost:8080` で起動します。

### MCPクライアントへの設定方法
Cursor や Claude Desktop などの MCP クライアントから本サーバーに接続する場合、設定ファイル（`config.json` 等）の `mcpServers` に以下の設定を追加します。

この設定により、SSE（Server-Sent Events）経由でサーバーに接続し、ヘッダーに `X-API-key` を付与して認証をパスします。

```json
"spring-mcp-server": {
  "command": "npx",
  "args": [
    "-y",
    "mcp-remote",
    "http://localhost:8080/sse",
    "--header",
    "X-API-key: api01.mycustomapikey",
    "--transport",
    "sse-only"
  ],
  "disabled": false,
  "disabledTools": []
}
```
