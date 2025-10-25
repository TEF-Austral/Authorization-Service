#!/bin/bash

set -e

BASE_URL="http://localhost:8005"
AUTH0_DOMAIN="tef-austral.us.auth0.com"
AUTH0_AUDIENCE="https://tef-austral.com/api"
AUTH0_MGMT_CLIENT_ID="KuVymmoEvGBrTIPkIERGKaGGvphD0yaF"
AUTH0_MGMT_CLIENT_SECRET="fHRAwEdsmPb6-aioNNjZ7F2V8BZhobDGvOnWU_FIe1cWUOzAI3D6z0FL8Q5VoV3W"

echo "=========================================="
echo "Authorization Service Test Suite"
echo "=========================================="
echo ""

echo "1. Testing health endpoint (no auth required)..."
HEALTH_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE_URL/health")
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$HEALTH_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" == "200" ]; then
    echo "✓ Health check passed"
    echo "  Response: $BODY"
else
    echo "✗ Health check failed with code $HTTP_CODE"
    exit 1
fi
echo ""

echo "2. Getting M2M token from Auth0..."
TOKEN_RESPONSE=$(curl -s -X POST "https://$AUTH0_DOMAIN/oauth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "'"$AUTH0_MGMT_CLIENT_ID"'",
    "client_secret": "'"$AUTH0_MGMT_CLIENT_SECRET"'",
    "audience": "'"$AUTH0_AUDIENCE"'",
    "grant_type": "client_credentials"
  }')

M2M_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$M2M_TOKEN" ]; then
    echo "✗ Failed to get M2M token"
    echo "  Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "✓ M2M token obtained"
echo "  Token: ${M2M_TOKEN:0:50}..."
echo ""

echo "3. Testing protected endpoint without token (should fail)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user1",
    "action": "read",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)

if [ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "403" ]; then
    echo "✓ Correctly rejected request without token (HTTP $HTTP_CODE)"
else
    echo "✗ Should have rejected request without token, got HTTP $HTTP_CODE"
fi
echo ""

echo "4. Testing permission check - Owner can READ..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user1",
    "action": "read",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ Owner can read their own snippet"
    echo "  Response: $BODY"
else
    echo "✗ Owner should be able to read (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "5. Testing permission check - Owner can DELETE..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user1",
    "action": "delete",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ Owner can delete their own snippet"
    echo "  Response: $BODY"
else
    echo "✗ Owner should be able to delete (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "6. Testing permission check - Non-owner CANNOT DELETE..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "delete",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "false" ]; then
    echo "✓ Non-owner cannot delete"
    echo "  Response: $BODY"
else
    echo "✗ Non-owner should not be able to delete (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "7. Testing permission check - Non-owner CANNOT READ (without explicit permission)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "read",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "false" ]; then
    echo "✓ Non-owner cannot read without explicit permission"
    echo "  Response: $BODY"
else
    echo "✗ Non-owner should not be able to read (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "8. Granting READ permission to user2..."
GRANT_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/permissions" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requesterId": "auth0|user1",
    "ownerId": "auth0|user1",
    "granteeId": "auth0|user2",
    "snippetId": "snippet-123",
    "canRead": true,
    "canEdit": false
  }')

HTTP_CODE=$(echo "$GRANT_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$GRANT_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" == "200" ]; then
    echo "✓ Permission granted successfully"
    echo "  Response: $BODY"
else
    echo "✗ Failed to grant permission (HTTP $HTTP_CODE)"
    echo "  Response: $BODY"
fi
echo ""

echo "9. Testing permission check - Non-owner CAN READ (with explicit permission)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "read",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ Non-owner can now read with explicit permission"
    echo "  Response: $BODY"
else
    echo "✗ Non-owner should be able to read after grant (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "10. Testing permission check - Non-owner CANNOT EDIT (only has read)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "edit",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "false" ]; then
    echo "✓ Non-owner cannot edit with only read permission"
    echo "  Response: $BODY"
else
    echo "✗ Non-owner should not be able to edit (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "11. Upgrading permission to include EDIT..."
GRANT_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/permissions" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requesterId": "auth0|user1",
    "ownerId": "auth0|user1",
    "granteeId": "auth0|user2",
    "snippetId": "snippet-123",
    "canRead": true,
    "canEdit": true
  }')

HTTP_CODE=$(echo "$GRANT_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$GRANT_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" == "200" ]; then
    echo "✓ Permission upgraded successfully"
    echo "  Response: $BODY"
else
    echo "✗ Failed to upgrade permission (HTTP $HTTP_CODE)"
fi
echo ""

echo "12. Testing permission check - Non-owner CAN EDIT (after upgrade)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "edit",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ Non-owner can now edit after upgrade"
    echo "  Response: $BODY"
else
    echo "✗ Non-owner should be able to edit after upgrade (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "13. Testing permission check - EXECUTE action (requires read)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "execute",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ User with read permission can execute"
    echo "  Response: $BODY"
else
    echo "✗ User with read should be able to execute (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "14. Getting all permissions for snippet-123..."
GET_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/permissions/snippet" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requesterId": "auth0|user1",
    "snippetId": "snippet-123"
  }')

HTTP_CODE=$(echo "$GET_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$GET_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" == "200" ]; then
    echo "✓ Retrieved snippet permissions"
    echo "  Response: $BODY"
else
    echo "✗ Failed to get snippet permissions (HTTP $HTTP_CODE)"
    echo "  Response: $BODY"
fi
echo ""

echo "15. Revoking permission from user2..."
REVOKE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/permissions/revoke" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requesterId": "auth0|user1",
    "userId": "auth0|user2",
    "snippetId": "snippet-123"
  }')

HTTP_CODE=$(echo "$REVOKE_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)

if [ "$HTTP_CODE" == "204" ]; then
    echo "✓ Permission revoked successfully"
else
    echo "✗ Failed to revoke permission (HTTP $HTTP_CODE)"
    echo "  Response: $(echo "$REVOKE_RESPONSE" | sed '/HTTP_CODE/d')"
fi
echo ""

echo "16. Testing permission check - Non-owner CANNOT READ (after revoke)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user2",
    "action": "read",
    "snippetId": "snippet-123",
    "ownerId": "auth0|user1"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "false" ]; then
    echo "✓ Permission successfully revoked"
    echo "  Response: $BODY"
else
    echo "✗ Permission should be revoked (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "17. Testing CREATE action (should always be allowed)..."
CHECK_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/check" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "auth0|user3",
    "action": "create",
    "snippetId": "new-snippet",
    "ownerId": "auth0|user3"
  }')

HTTP_CODE=$(echo "$CHECK_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$CHECK_RESPONSE" | sed '/HTTP_CODE/d')
ALLOWED=$(echo "$BODY" | grep -o '"allowed":[^,}]*' | cut -d: -f2)

if [ "$HTTP_CODE" == "200" ] && [ "$ALLOWED" == "true" ]; then
    echo "✓ Any user can create"
    echo "  Response: $BODY"
else
    echo "✗ Create should always be allowed (HTTP $HTTP_CODE, allowed: $ALLOWED)"
fi
echo ""

echo "18. Testing non-owner trying to grant permission (should fail)..."
GRANT_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/authorization/permissions" \
  -H "Authorization: Bearer $M2M_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requesterId": "auth0|user2",
    "ownerId": "auth0|user1",
    "granteeId": "auth0|user3",
    "snippetId": "snippet-123",
    "canRead": true,
    "canEdit": false
  }')

HTTP_CODE=$(echo "$GRANT_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)

if [ "$HTTP_CODE" == "500" ]; then
    echo "✓ Non-owner correctly prevented from granting permissions"
else
    echo "✗ Non-owner should not be able to grant permissions (HTTP $HTTP_CODE)"
fi
echo ""

echo "=========================================="
echo "Test Suite Complete!"
echo "=========================================="