#!/bin/bash

echo "🔐 Setting up Vault Transit..."

VAULT_CMD="docker exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='root' vault-esop vault"

echo "📝 Enabling Transit secrets engine..."
$VAULT_CMD secrets enable transit

echo "📝 Creating encryption key 'aes256-key'..."
$VAULT_CMD write -f transit/keys/aes256-key

echo "✅ Verifying key..."
$VAULT_CMD read transit/keys/aes256-key

echo ""
echo "✅ Vault Transit setup complete!"
echo "🚀 Now restart your Spring Boot app"