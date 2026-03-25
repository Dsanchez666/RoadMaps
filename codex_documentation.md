
# Function Comment Templates for Spring Boot and Angular

This document provides standardized comment templates for functions, classes, and methods used in **Spring Boot (Java)** and **Angular (TypeScript)**. It is designed for integration into **VS Code** with **Codex** as a reference or internal documentation file.

---
## Objectives
- Establish a unified format for comment documentation.
- Improve clarity, maintainability, and onboarding experience.
- Ensure consistent documentation quality across all projects.

---
## Spring Boot (Java – Javadoc Standard)

### Template for Service Methods
```java
/**
 * <Brief description of the method>
 *
 * @param param1 Description of the parameter.
 * @param param2 Description of the parameter.
 * @return <Type> Description of the return value.
 *
 * @throws <ExceptionType> Description of the conditions that trigger the exception.
 *
 * Example:
 * <pre>
 *     ReturnType result = service.myMethod(value1, value2);
 * </pre>
 *
 * Additional Details:
 * This method <explain any rules, validations, or side effects>.
 */
```

### Template for REST Controllers
```java
/**
 * <Description of the REST operation>
 *
 * @param id Identifier of the requested resource.
 * @return ResponseEntity<?> HTTP response containing the result or an error message.
 *
 * @apiNote
 *   Method used to <explain purpose>.
 *
 * @see RelatedClassName
 */
```

### Template for Classes
```java
/**
 * <Class summary>
 *
 * <Detailed description of the class purpose and its role in the application>.
 *
 * @author
 * @since <version or date>
 */
```

---
## Angular (TypeScript – TypeDoc/JSDoc Standard)

### Template for Functions
```ts
/**
 * <Description of what the function does>
 *
 * @param param1 Description of the parameter.
 * @param param2 Description of the parameter.
 *
 * @returns Type Description of the returned value.
 *
 * @example
 * const result = myFunction(1, 2);
 * console.log(result);
 */
```

### Template for Service Methods
```ts
/**
 * <General description of the method>
 *
 * Performs <operation> using data from <source>.
 *
 * @param id Resource identifier.
 * @returns Observable<Type> Description of the returned observable.
 */
```

### Template for Components
```ts
/**
 * Component <ComponentName>
 *
 * <Long description of the component's purpose>
 *
 * @selector 'app-component'
 * @styleUrls ['./component.scss']
 */
export class ComponentName {}
```

### Template for Services
```ts
/**
 * Service responsible for <description>.
 *
 * This service provides methods for <main operations>.
 *
 * @providedIn 'root'
 */
@Injectable({ providedIn: 'root' })
export class MyService {}
```

---
## Benefits
- Enhances code readability and understanding.
- Supports automatic documentation generation.
- Encourages consistent development practices.
- Reduces errors due to unclear behavior.

