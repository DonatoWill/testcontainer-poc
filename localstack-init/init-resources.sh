#!/bin/bash
set -e

echo "📦 Criando fila SQS..."
awslocal sqs create-queue --queue-name cliente-events

echo "📦 Criando tabela DynamoDB..."
awslocal dynamodb create-table \
  --table-name Cliente \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

echo "✅ Recursos criados com sucesso!"
