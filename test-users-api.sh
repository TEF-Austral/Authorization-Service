#!/bin/bash

# Test script for User API endpoints
# Make sure to replace YOUR_ACCESS_TOKEN with a valid token

BASE_URL="http://localhost:8005"
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkF3cjRVRjRyeG5kaTY2WDJVNzN5LSJ9.eyJ1c2VybmFtZSI6InRvbWFzbW9udGVpcm8wMDciLCJpc3MiOiJodHRwczovL3RlZi1hdXN0cmFsLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw2OTAwZGZjODMyZjlhY2QzNzY2MjFhOGQiLCJhdWQiOlsiaHR0cHM6Ly90ZWYtYXVzdHJhbC5jb20vYXBpIiwiaHR0cHM6Ly90ZWYtYXVzdHJhbC51cy5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNzYxOTU5MTQ5LCJleHAiOjE3NjIwNDU1NDksInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJhenAiOiJvT0NSWmNvNVNEWVJrWkhNSmhCeUFtQUFWUnVnS3ljaSJ9.dwKIF6YsKpF5ZE8E70nqH_19UjK_1SKvs61R92_NlXp0pHbSL5SC_Sm0jDCFVXwU91ksBokmKj0Y-KUL2LEEDJ2js9lW_OOqmaWrdMJIRfq5wwnzOA7XMpoOzxM1Xnl84IjMlIBcranGxFYv_yAo75UGZSpfjEr0USetrNbwq0BhnX6k8JjjtbK3VdSfEXscwK2w0G8zyr5fxyK56iKlJBFSVepDeakAFTy5lnViqohsJUKNO-UwrhZLF_-oVRDCYbauUsqHnfTp-aLbXHuYsnLG8kx3YGLUuf2LTGcIQ6prbLMR2SDrsP2Z-QjA6JvKbL4rCvjg5FYeprn12yPIaw"

echo "Testing User API Endpoints..."
echo "================================"
echo ""

# Test 1: Get all users (paginated)
echo "1. Getting all users (first 10)..."
curl -X GET \
  "${BASE_URL}/api/users?page=0&pageSize=10" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

# Test 2: Search by email
echo "2. Searching users by email query..."
EMAIL="example@test.com"
curl -X GET \
  "${BASE_URL}/api/users?query=email:\"${EMAIL}\"" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

# Test 3: Search by name
echo "3. Searching users by name (contains 'john')..."
curl -X GET \
  "${BASE_URL}/api/users?query=name:*john*" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

# Test 4: Get user by email (exact match endpoint)
echo "4. Getting users by exact email..."
curl -X GET \
  "${BASE_URL}/api/users/by-email?email=${EMAIL}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

# Test 5: Search with filters
echo "5. Searching users with filters (emailVerified=true)..."
curl -X GET \
  "${BASE_URL}/api/users/search?emailVerified=true" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

# Test 6: Get specific user by ID (replace with actual user ID)
echo "6. Getting user by ID..."
USER_ID="auth0|123456789"  # Replace with actual user ID
curl -X GET \
  "${BASE_URL}/api/users/${USER_ID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
echo ""
echo "--------------------------------"
echo ""

echo "Tests completed!"