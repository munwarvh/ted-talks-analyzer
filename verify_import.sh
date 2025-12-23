#!/bin/bash

# Database Verification Script
# Verifies that import statistics match actual database records

echo "============================================"
echo "TED Talks Import Verification"
echo "============================================"
echo ""

PGPASSWORD=tedtalks_password

echo "1. Counting talks in database..."
TALK_COUNT=$(psql -U tedtalks_user -d tedtalks -t -c "SELECT COUNT(*) FROM tedtalks.ted_talks;")
echo "   Talks in DB: $TALK_COUNT"
echo "   Expected: ~5,425"
echo ""

echo "2. Counting speakers in database..."
SPEAKER_COUNT=$(psql -U tedtalks_user -d tedtalks -t -c "SELECT COUNT(*) FROM tedtalks.speakers;")
echo "   Speakers in DB: $SPEAKER_COUNT"
echo "   Expected: ~4,414"
echo ""

echo "3. Checking for duplicate talks..."
DUPLICATES=$(psql -U tedtalks_user -d tedtalks -t -c "SELECT COUNT(*) - COUNT(DISTINCT (title, author)) FROM tedtalks.ted_talks;")
echo "   Duplicates: $DUPLICATES"
echo "   Expected: 0"
echo ""

echo "4. Top 5 most viewed talks..."
psql -U tedtalks_user -d tedtalks -c "SELECT title, author, views FROM tedtalks.ted_talks ORDER BY views DESC LIMIT 5;"
echo ""

echo "5. Top 5 speakers by talk count..."
psql -U tedtalks_user -d tedtalks -c "SELECT s.name, COUNT(t.id) as talk_count FROM tedtalks.speakers s JOIN tedtalks.ted_talks t ON t.author = s.name GROUP BY s.name ORDER BY talk_count DESC LIMIT 5;"
echo ""

echo "6. Sample of recent talks..."
psql -U tedtalks_user -d tedtalks -c "SELECT id, title, author, date, views FROM tedtalks.ted_talks ORDER BY id LIMIT 5;"
echo ""

echo "============================================"
echo "Verification Complete"
echo "============================================"

