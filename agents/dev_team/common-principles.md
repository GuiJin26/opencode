# Common Development Principles

This file contains shared development principles referenced by multiple agents.

---

## Security Principles

Apply these security practices throughout the development lifecycle:

- **Input Validation**: Validate and sanitize all user inputs
- **Authentication & Authorization**: Implement proper auth mechanisms and role-based access control
- **SQL Injection Prevention**: Use parameterized queries or ORM to prevent injection attacks
- **XSS Prevention**: Sanitize and escape user-generated content in outputs
- **CSRF Protection**: Implement anti-CSRF tokens for state-changing operations
- **Rate Limiting**: Implement rate limiting and throttling to prevent abuse
- **Secure Data Handling**: Encrypt sensitive data at rest and in transit
- **No Hardcoded Secrets**: Never commit secrets, API keys, or credentials to code
- **Dependency Security**: Keep dependencies updated and scan for vulnerabilities
- **HTTPS Only**: Enforce HTTPS for all communications

---

## Performance Principles

Optimize for speed, efficiency, and scalability:

### Backend
- **Query Optimization**: Write efficient database queries, use proper indexing
- **Caching**: Implement caching strategies (Redis, CDN, application-level caching)
- **Connection Pooling**: Use connection pooling for database connections
- **Async Processing**: Use async/await patterns for heavy I/O operations
- **Pagination**: Implement pagination for list endpoints to avoid large payloads
- **Lazy Loading**: Load data only when needed

### Frontend
- **Code Splitting**: Split code by routes or components to reduce initial load
- **Lazy Loading**: Lazy load components, images, and routes
- **Image Optimization**: Use optimized image formats (WebP, AVIF) and sizes
- **Memoization**: Use React.memo, useMemo, useCallback to prevent unnecessary re-renders
- **Minimize Re-renders**: Optimize component render cycles
- **Bundle Optimization**: Analyze and optimize bundle sizes

### General
- **Measure First**: Profile before optimizing to identify real bottlenecks
- **CDN Usage**: Use CDNs for static assets and libraries
- **Compression**: Enable Gzip/Brotli compression for text-based assets
- **HTTP/2**: Take advantage of HTTP/2 features like multiplexing

---

## Code Quality Principles

Follow these practices for maintainable, high-quality code:

### Design Patterns & Architecture
- **SOLID Principles**: Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DRY Principle**: Don't Repeat Yourself - extract common logic into reusable functions
- **KISS Principle**: Keep It Simple, Stupid - avoid unnecessary complexity
- **Separation of Concerns**: Keep business logic, data access, and presentation separate
- **Dependency Injection**: Use DI patterns for better testability and flexibility

### Code Style & Conventions
- **Consistent Naming**: Use clear, descriptive names for variables, functions, and classes
- **Code Formatting**: Follow consistent formatting (indentation, spacing, line length)
- **Documentation**: Add comments for complex logic, but prefer self-documenting code
- **Type Safety**: Use TypeScript or static typing when available
- **Error Messages**: Provide clear, actionable error messages

### Error Handling
- **Graceful Degradation**: Handle errors gracefully and provide fallback behavior
- **Error Logging**: Log errors with sufficient context for debugging
- **User-Facing Errors**: Show user-friendly error messages, log technical details
- **Error Boundaries**: Implement error boundaries to prevent crashes
- **Consistent Error Format**: Use consistent error response structures

### Testing & Quality
- **Testability**: Write testable code with minimal external dependencies
- **Unit Tests**: Test individual functions and components in isolation
- **Integration Tests**: Test interactions between modules and systems
- **Test Coverage**: Aim for high test coverage of critical paths
- **Static Analysis**: Use linters and static analysis tools

### Maintainability
- **Modularity**: Break code into small, focused modules
- **Low Coupling**: Minimize dependencies between components
- **High Cohesion**: Group related functionality together
- **Extensibility**: Design code that's easy to extend without modifying
- **Code Review**: Peer review all code changes

---

**Note:** These principles provide a foundation for high-quality software development. Adapt and apply them based on your specific project requirements and constraints.
