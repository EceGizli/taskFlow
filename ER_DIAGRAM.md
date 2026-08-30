# TaskFlow — ER Diagram

```mermaid
erDiagram
    USER ||--o{ PROJECT : "owns (owner)"
    USER }o--o{ PROJECT : "member of"
    USER ||--o{ PROJECT_MEMBER : "has role in"
    PROJECT ||--o{ PROJECT_MEMBER : "has members"
    PROJECT ||--o{ TASK : "contains"
    TASK }o--o{ LABEL : "tagged with"
    TASK ||--o{ TASK : "parent / subtasks"
    TASK ||--o{ CHECK_ITEM : "has checklist"
    TASK ||--o{ COMMENT : "has comments"
    TASK ||--o{ ATTACHMENT : "has files"
    USER ||--o{ TASK : "assigned to"
    USER ||--o{ COMMENT : "authors"
    USER ||--o{ ATTACHMENT : "uploads"
    USER ||--o{ NOTIFICATION : "receives"
    USER ||--o{ REFRESH_TOKEN : "has"
    USER ||--o{ PASSWORD_RESET_TOKEN : "requests"

    USER {
        Long id PK
        String name
        String email UK
        String password
        Role role
        int failedAttempt
        LocalDateTime lockTime
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    PROJECT {
        Long id PK
        String name
        String description
        ProjectStatus status
        String color
        String tag
        boolean isDeleted
        LocalDateTime deletedAt
        Long owner_id FK
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    PROJECT_MEMBER {
        Long id PK
        Long project_id FK
        Long user_id FK
        ProjectRole role "OWNER / EDITOR / VIEWER"
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    TASK {
        Long id PK
        String title
        String description
        TaskStatus status
        Priority priority
        LocalDate dueDate
        Integer estimatedHours
        boolean isDeleted
        LocalDateTime deletedAt
        Long project_id FK
        Long assignee_id FK
        Long parentTask_id FK
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    LABEL {
        Long id PK
        String name
        String color
    }

    CHECK_ITEM {
        Long id PK
        String title
        boolean isCompleted
        Long task_id FK
    }

    COMMENT {
        Long id PK
        String content
        Long task_id FK
        Long author_id FK
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    ATTACHMENT {
        Long id PK
        String fileName
        String storedFileName
        String fileType
        long fileSize
        String filePath
        Long task_id FK
        Long uploadedBy_id FK
    }

    NOTIFICATION {
        Long id PK
        Long user_id FK
        String title
        String message
        boolean isRead
        LocalDateTime createdAt
    }

    REFRESH_TOKEN {
        Long id PK
        String token
        Long user_id FK
        Instant expiryDate
        boolean revoked
    }

    PASSWORD_RESET_TOKEN {
        Long id PK
        String token
        Long user_id FK
        LocalDateTime expiryDate
        boolean used
    }
```

**Notlar**
- `PROJECT.owner_id` → tek bir sahip (OWNER rolü ile eşleşir); `PROJECT_MEMBER` tablosu ayrıca her üyenin rolünü (OWNER/EDITOR/VIEWER) tutar.
- `TASK.parentTask_id` self-referencing ilişki → subtask/duplicate task desteği.
- Soft delete `isDeleted` + `deletedAt` alanları hem `PROJECT` hem `TASK` üzerinde var; sorgular `isDeletedFalse` ile filtreleniyor.
- GitHub bu Mermaid bloğunu doğrudan render eder; farklı bir görsel istersen (PNG/draw.io) ayrıca üretebilirim.
