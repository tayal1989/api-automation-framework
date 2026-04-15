#!/bin/bash

# Change to the root directory of the project regardless of where script is run from
cd "$(dirname "$0")/.."

# A simple runner script to start the json-server mock API
echo "Starting json-server for db.json..."
echo "To use a custom port, you can pass arguments like: ./scripts/start_db.sh --port 8080"
echo "--------------------------------------------------------"

PATH=/opt/homebrew/bin:$PATH npx json-server --watch src/test/resources/json-files/db.json "$@"
