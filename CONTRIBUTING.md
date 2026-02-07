# Contributing to API Response Library
Thank you for your interest in contributing to the API Response Library! 🎉
## 📜 License Agreement
This project is licensed under the **Apache License 2.0**. By contributing, you agree that:
1. **Your contributions will be licensed** under the Apache License 2.0
2. **You grant** a perpetual, worldwide license to use, modify, and distribute your contributions
3. **You retain copyright** to your contributions
4. **You confirm** that you have the right to submit the contribution
5. **Your work is protected** - No one can claim exclusive ownership of your contributions or publish them elsewhere as their own
### What This Means
✅ **You CAN:**
- Keep copyright to your contributions
- Have your name attributed in the project
- Use your contributions elsewhere
- Contribute freely without restrictions
🛡️ **Protection:**
- No one (including the project maintainer) can claim exclusive ownership of your work
- Your contributions cannot be copyrighted by others
- The project must remain open source under Apache 2.0
- Derivative works must maintain the Apache 2.0 License
## 🚀 Getting Started
### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Git
- IDE with Lombok plugin (IntelliJ IDEA, Eclipse, VS Code)
### Development Setup
1. **Fork the repository**
   - Visit https://github.com/pasinduog/api-response
   - Click "Fork" button in top-right corner
2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/api-response.git
   cd api-response
   ```
3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/pasinduog/api-response.git
   ```
4. **Build the project**
   ```bash
   mvn clean install
   ```
5. **Generate JavaDoc**
   ```bash
   mvn javadoc:javadoc
   ```
## 📝 Contribution Workflow
### 1. Create a Feature Branch
```bash
# Update your fork
git checkout main
git pull upstream main
# Create feature branch
git checkout -b feature/your-feature-name
```
### 2. Make Your Changes
- Follow existing code style and conventions
- Add comprehensive JavaDoc comments
- Keep changes focused and atomic
- Test your changes thoroughly
### 3. Commit Your Changes
Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
```bash
# Format: <type>(<scope>): <description>
# Examples:
git commit -m "feat(dto): add pagination support"
git commit -m "fix(exception): resolve NPE in handler"
git commit -m "docs(readme): update installation guide"
git commit -m "test(response): add unit tests"
```
**Commit Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, whitespace)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks (dependencies, build)
- `perf` - Performance improvements
## 📋 Code Quality Standards
### Java Code Style
- Follow standard Java conventions
- Use meaningful variable and method names
- Keep methods short and focused (single responsibility)
- Avoid deep nesting (max 3 levels)
- Use Java 17+ features appropriately
### JavaDoc Requirements
All public classes, methods, and fields must have JavaDoc comments with proper documentation.
### Build Validation
Before submitting your PR, verify:
```bash
# Clean build
mvn clean install
# JavaDoc generation (should have zero warnings)
mvn javadoc:javadoc
# Check for compilation errors
mvn compile
# Run tests (when available)
mvn test
```
## 🎯 What We're Looking For
### Priority Contributions
1. **Bug Fixes** 🐛 - Fix reported issues and improve error handling
2. **Documentation** 📝 - Improve README and add code examples
3. **Tests** 🧪 - Add unit and integration tests
4. **Features** ✨ - Pagination, WebFlux, i18n support
## 🤝 Code of Conduct
### Our Standards
✅ **Do:**
- Be respectful and inclusive
- Provide constructive feedback
- Welcome newcomers
- Help others learn
❌ **Don't:**
- Use offensive language
- Harass others
- Make personal attacks
## 📞 Getting Help
- 💬 [GitHub Discussions](https://github.com/pasinduog/api-response/discussions)
- 🐛 [GitHub Issues](https://github.com/pasinduog/api-response/issues)
- 📧 Email: pasinduogdev@gmail.com
## 🙏 Thank You!
Your contributions make this project better for everyone!
---
**License:** By contributing, you agree to license your contributions under the Apache License 2.0.
