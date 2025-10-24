#!/usr/bin/env bash

set -euo pipefail

# Resolve project root and load .env
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

if [[ -f "${ENV_FILE}" ]]; then
  # Export all variables defined in .env
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
else
  echo "Missing .env at ${ENV_FILE}"
  exit 1
fi

# Map & validate required variables from .env
AUTH0_DOMAIN="${AUTH0_DOMAIN:?ENV AUTH0_DOMAIN is required}"
CLIENT_ID="${AUTH0_MGMT_CLIENT_ID:-${AUTH0_CLIENT_ID:-}}"
CLIENT_SECRET="${AUTH0_MGMT_CLIENT_SECRET:-${AUTH0_CLIENT_SECRET:-}}"
AUDIENCE="${AUTH0_AUDIENCE:-${AUDIENCE:-}}"
BASE_URL="${BASE_URL:-http://localhost:8005}"

missing=()
[[ -z "${CLIENT_ID}" ]] && missing+=("AUTH0_MGMT_CLIENT_ID")
[[ -z "${CLIENT_SECRET}" ]] && missing+=("AUTH0_MGMT_CLIENT_SECRET")
[[ -z "${AUDIENCE}" ]] && missing+=("AUTH0_AUDIENCE")
if (( ${#missing[@]} > 0 )); then
  echo "Missing required env vars: ${missing[*]}"
  exit 1
fi

echo "=== Getting Access Token from Auth0 ==="
TOKEN_RESPONSE=$(curl -s --request POST \
  --url "https://${AUTH0_DOMAIN}/oauth/token" \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"'${CLIENT_ID}'",
    "client_secret":"'${CLIENT_SECRET}'",
    "audience":"'${AUDIENCE}'",
    "grant_type":"client_credentials"
  }')

TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "${TOKEN:-}" ]; then
    echo "Failed to get token!"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "Token obtained successfully!"
echo "Token: ${TOKEN:0:50}..."
echo ""

echo "=== Test 1: Health Check (No Auth) ==="
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.txt "${BASE_URL}/")
cat /tmp/response.txt
echo ""
echo "Status: $HTTP_CODE"
echo ""

echo "=== Test 2: Get JWT Token Value ==="
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.txt "${BASE_URL}/jwt" \
  -H "Authorization: Bearer ${TOKEN}")
cat /tmp/response.txt
echo ""
echo "Status: $HTTP_CODE"
echo ""

echo "=== Test 3: Create New User ==="
TIMESTAMP=$(date +%s)
EMAIL="testuser${TIMESTAMP}@example.com"
echo "Creating user with email: ${EMAIL}"

CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/users" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'${EMAIL}'",
    "password": "TestPassword123!",
    "name": "Test User"
  }')

echo "$CREATE_RESPONSE"
echo ""

# Extract userId from response
USER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"userId":"[^"]*' | cut -d'"' -f4)

if [ -z "${USER_ID:-}" ]; then
    echo "Failed to extract userId from response!"
    echo "=== All tests completed! ==="
    exit 1
fi

echo "User created with ID: ${USER_ID}"
echo ""

# Wait a moment to ensure the user is fully created
sleep 1

echo "=== Test 4: Delete User ==="
# URL encode the userId (replace | with %7C)
ENCODED_USER_ID=$(echo "$USER_ID" | sed 's/|/%7C/g')
echo "Encoded userId: ${ENCODED_USER_ID}"
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.txt -X DELETE "${BASE_URL}/users/${ENCODED_USER_ID}" \
  -H "Authorization: Bearer ${TOKEN}")
cat /tmp/response.txt
echo ""
echo "Status: $HTTP_CODE"

if [ "$HTTP_CODE" = "204" ]; then
    echo "✓ User deleted successfully!"
else
    echo "✗ Failed to delete user"
fi
echo ""

echo "=== All tests completed! ==="