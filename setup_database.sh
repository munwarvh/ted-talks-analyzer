# 🔧 Git Repository Setup Guide

Complete guide for setting up the Git repository and collaborating on the TED Talks Analyzer project.

---

## 📋 Initial Repository Setup

### 1. Initialize Git Repository (if not already done)

```bash
# Navigate to project directory
cd /Users/munwar/IdeaProjects/ted-talks-analyzer

# Initialize Git repository
git init

# Check status
git status
```

### 2. Configure Git User

```bash
# Set your name
git config user.name "Your Name"

# Set your email
git config user.email "your.email@example.com"

# Verify configuration
git config --list
```

---

## 📝 First Commit

### 1. Review Files to Commit

```bash
# See what files will be added
git status

# Review .gitignore
cat .gitignore
```

### 2. Add Files to Git

```bash
# Add all files (respects .gitignore)
git add .

# Or add specific files
git add README.md pom.xml src/

# Check what's staged
git status
```

### 3. Create Initial Commit

```bash
# Commit with message
git commit -m "Initial commit: TED Talks Analyzer MVP

- Implemented Spring Boot 3.4.0 application
- Added PostgreSQL database support with Flyway migrations
- Implemented streaming CSV import with batch processing
- Added async analysis operations (top speakers, per year analysis)
- Implemented intelligent caching (100-200x performance improvement)
- Added 36 automated tests (100% pass rate for critical tests)
- Configured Swagger UI for API documentation
- Added comprehensive documentation and setup guides"

# Verify commit
git log
```

---

## 🌐 Push to GitHub

### Option 1: Create New Repository on GitHub

1. Go to https://github.com/new
2. Repository name: `ted-talks-analyzer`
3. Description: `High-performance Spring Boot application for analyzing TED talks data`
4. Make it **Public** or **Private**
5. **DO NOT** initialize with README (we already have one)
6. Click "Create repository"

### Option 2: Add Remote and Push

```bash
# Add GitHub remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/ted-talks-analyzer.git

# Verify remote
git remote -v

# Push to GitHub
git push -u origin main

# If branch is named 'master', rename to 'main'
git branch -M main
git push -u origin main
```

---

## 🔐 Using SSH (Recommended)

### 1. Generate SSH Key (if not exists)

```bash
# Check for existing SSH keys
ls -la ~/.ssh

# Generate new SSH key
ssh-keygen -t ed25519 -C "your.email@example.com"

# Start SSH agent
eval "$(ssh-agent -s)"

# Add key to agent
ssh-add ~/.ssh/id_ed25519

# Copy public key
cat ~/.ssh/id_ed25519.pub
```

### 2. Add SSH Key to GitHub

1. Go to GitHub → Settings → SSH and GPG keys
2. Click "New SSH key"
3. Paste your public key
4. Click "Add SSH key"

### 3. Update Remote to Use SSH

```bash
# Change remote URL to SSH
git remote set-url origin git@github.com:YOUR_USERNAME/ted-talks-analyzer.git

# Verify
git remote -v

# Test connection
ssh -T git@github.com
```

---

## 📂 Repository Structure

Your repository should contain:

```
ted-talks-analyzer/
├── .gitignore                          # Git ignore rules
├── README.md                           # Main documentation
├── QUICK_START_GUIDE.md               # Quick setup guide
├── setup_database.sh                   # Database setup script
├── pom.xml                            # Maven configuration
├── src/                               # Source code
│   ├── main/
│   │   ├── java/                      # Java source files
│   │   └── resources/                 # Configuration files
│   └── test/                          # Test files
├── logs/                              # Application logs (ignored)
└── target/                            # Build output (ignored)
```

---

## 🌿 Branching Strategy

### Main Branch Strategy

```bash
# Main branch (production-ready code)
main

# Development branch
git checkout -b develop

# Feature branches
git checkout -b feature/new-feature
git checkout -b bugfix/fix-issue
git checkout -b hotfix/critical-fix
```

### Working with Branches

```bash
# Create and switch to new branch
git checkout -b feature/add-export-functionality

# Make changes and commit
git add .
git commit -m "Add CSV export functionality"

# Push branch to remote
git push -u origin feature/add-export-functionality

# Switch back to main
git checkout main

# Merge feature branch
git merge feature/add-export-functionality

# Delete merged branch
git branch -d feature/add-export-functionality
```

---

## 🔄 Daily Workflow

### Start of Day

```bash
# Pull latest changes
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/your-feature
```

### During Development

```bash
# Check status frequently
git status

# Add changes
git add .

# Commit with meaningful message
git commit -m "Descriptive commit message"

# Push to remote
git push origin feature/your-feature
```

### End of Day

```bash
# Ensure all changes are committed
git status

# Push to remote
git push origin feature/your-feature
```

---

## 📜 Commit Message Guidelines

### Format

```
<type>: <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting)
- **refactor**: Code refactoring
- **test**: Adding tests
- **chore**: Maintenance tasks

### Examples

```bash
# Good commit messages
git commit -m "feat: Add speaker export API endpoint"
git commit -m "fix: Resolve CSV import memory leak"
git commit -m "docs: Update API documentation in README"
git commit -m "test: Add integration tests for analysis service"

# With detailed body
git commit -m "feat: Add caching for speaker analysis

- Implement @Cacheable annotation
- Add cache eviction on data updates
- Configure TTL to 60 minutes
- Improves performance by 10x"
```

---

## 🏷️ Tagging Releases

### Create Version Tags

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0 - MVP"

# List tags
git tag

# Push tags to remote
git push origin v1.0.0

# Push all tags
git push origin --tags
```

### Semantic Versioning

- **v1.0.0**: Major.Minor.Patch
- **Major**: Breaking changes
- **Minor**: New features (backwards compatible)
- **Patch**: Bug fixes

---

## 🔙 Undoing Changes

### Undo Uncommitted Changes

```bash
# Discard changes in specific file
git checkout -- filename.java

# Discard all changes
git reset --hard HEAD
```

### Undo Last Commit (keep changes)

```bash
git reset --soft HEAD~1
```

### Undo Last Commit (discard changes)

```bash
git reset --hard HEAD~1
```

### Revert a Pushed Commit

```bash
# Create new commit that undoes changes
git revert <commit-hash>
git push origin main
```

---

## 🔍 Useful Git Commands

### View History

```bash
# View commit history
git log

# One-line format
git log --oneline

# Graphical view
git log --graph --oneline --all

# See changes in commits
git log -p

# Last N commits
git log -5
```

### View Changes

```bash
# See unstaged changes
git diff

# See staged changes
git diff --staged

# Compare branches
git diff main..feature-branch
```

### Stash Changes

```bash
# Save changes temporarily
git stash

# List stashes
git stash list

# Apply stashed changes
git stash apply

# Apply and remove from stash
git stash pop
```

---

## 👥 Collaboration

### Clone Repository

```bash
# Clone via HTTPS
git clone https://github.com/YOUR_USERNAME/ted-talks-analyzer.git

# Clone via SSH
git clone git@github.com:YOUR_USERNAME/ted-talks-analyzer.git

# Clone specific branch
git clone -b develop https://github.com/YOUR_USERNAME/ted-talks-analyzer.git
```

### Pull Requests

1. Fork the repository
2. Create feature branch
3. Make changes and commit
4. Push to your fork
5. Create Pull Request on GitHub
6. Wait for review
7. Address feedback
8. Merge when approved

### Sync Fork

```bash
# Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/ted-talks-analyzer.git

# Fetch upstream changes
git fetch upstream

# Merge upstream changes
git checkout main
git merge upstream/main

# Push to your fork
git push origin main
```

---

## 🚨 Common Issues

### Merge Conflicts

```bash
# When conflicts occur
git status  # Shows conflicted files

# Edit files to resolve conflicts
# Look for markers: <<<<<<<, =======, >>>>>>>

# After resolving
git add resolved-file.java
git commit -m "Resolve merge conflicts"
```

### Forgot to Pull Before Push

```bash
# Pull with rebase
git pull --rebase origin main

# Resolve any conflicts
# Then push
git push origin main
```

### Accidentally Committed Sensitive Data

```bash
# Remove file from Git history
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch path/to/sensitive-file" \
  --prune-empty --tag-name-filter cat -- --all

# Force push
git push origin --force --all
```

---

## 📊 Git Configuration

### Global Configuration

```bash
# Set default editor
git config --global core.editor "vim"

# Set default branch name
git config --global init.defaultBranch main

# Enable color output
git config --global color.ui auto

# Set merge tool
git config --global merge.tool vimdiff

# Enable credential caching (15 minutes)
git config --global credential.helper cache

# Enable credential caching (1 hour)
git config --global credential.helper 'cache --timeout=3600'
```

### Repository-Specific Configuration

```bash
# Use different email for this repo
git config user.email "work@company.com"

# View repo configuration
git config --local --list
```

---

## ✅ Pre-commit Checklist

Before committing, ensure:

- [ ] Code compiles: `mvn clean compile`
- [ ] Tests pass: `mvn test`
- [ ] No sensitive data in files
- [ ] Updated documentation if needed
- [ ] Meaningful commit message
- [ ] Changes are in correct branch

---

## 📚 Additional Resources

- [Git Official Documentation](https://git-scm.com/doc)
- [GitHub Guides](https://guides.github.com/)
- [Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)
- [Semantic Versioning](https://semver.org/)

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| **Clone repo** | `git clone <url>` |
| **Check status** | `git status` |
| **Add files** | `git add .` |
| **Commit** | `git commit -m "message"` |
| **Push** | `git push origin main` |
| **Pull** | `git pull origin main` |
| **Create branch** | `git checkout -b feature-name` |
| **Switch branch** | `git checkout branch-name` |
| **Merge branch** | `git merge branch-name` |
| **View log** | `git log --oneline` |
| **Undo changes** | `git reset --hard HEAD` |
| **Stash changes** | `git stash` |

---

**Last Updated**: December 23, 2025
**Version**: 1.0.0
#!/bin/bash

# TED Talks Analyzer - Quick Setup Script
# This script sets up the PostgreSQL database for the application

set -e  # Exit on error

echo "================================================"
echo "TED Talks Analyzer - Database Setup"
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DB_NAME="tedtalks_db"
DB_USER="tedtalks_user"
DB_PASSWORD="tedtalks_pass"
DB_SCHEMA="tedtalks"

echo "Configuration:"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo "  Schema: $DB_SCHEMA"
echo ""

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo -e "${RED}Error: PostgreSQL is not installed${NC}"
    echo "Please install PostgreSQL first:"
    echo "  macOS: brew install postgresql@16"
    echo "  Linux: sudo apt-get install postgresql-16"
    exit 1
fi

echo -e "${GREEN}✓ PostgreSQL is installed${NC}"

# Check if PostgreSQL is running
if ! pg_isready &> /dev/null; then
    echo -e "${YELLOW}PostgreSQL is not running. Attempting to start...${NC}"

    # Try to start PostgreSQL (macOS with Homebrew)
    if command -v brew &> /dev/null; then
        brew services start postgresql@16
        sleep 2
    else
        echo -e "${RED}Please start PostgreSQL manually${NC}"
        exit 1
    fi
fi

if pg_isready &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL is running${NC}"
else
    echo -e "${RED}Error: Could not connect to PostgreSQL${NC}"
    exit 1
fi

echo ""
echo "Setting up database..."
echo ""

# Create database and user
psql -U postgres << EOF
-- Drop database if exists (for clean setup)
DROP DATABASE IF EXISTS $DB_NAME;
DROP USER IF EXISTS $DB_USER;

-- Create database
CREATE DATABASE $DB_NAME;

-- Create user
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;

\c $DB_NAME

-- Create schema
CREATE SCHEMA IF NOT EXISTS $DB_SCHEMA;

-- Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA $DB_SCHEMA TO $DB_USER;
GRANT ALL ON ALL TABLES IN SCHEMA $DB_SCHEMA TO $DB_USER;
GRANT ALL ON ALL SEQUENCES IN SCHEMA $DB_SCHEMA TO $DB_USER;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA $DB_SCHEMA GRANT ALL ON TABLES TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA $DB_SCHEMA GRANT ALL ON SEQUENCES TO $DB_USER;

EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database setup completed successfully!${NC}"
    echo ""
    echo "Database connection details:"
    echo "  URL: jdbc:postgresql://localhost:5432/$DB_NAME"
    echo "  Username: $DB_USER"
    echo "  Password: $DB_PASSWORD"
    echo "  Schema: $DB_SCHEMA"
    echo ""
    echo "To verify the connection:"
    echo "  psql -U $DB_USER -d $DB_NAME -h localhost"
    echo ""
    echo -e "${GREEN}You can now start the application!${NC}"
    echo "  mvn spring-boot:run -Dspring-boot.run.profiles=local"
else
    echo -e "${RED}Error: Database setup failed${NC}"
    exit 1
fi

