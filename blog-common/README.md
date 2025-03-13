# Blog Common Library

Blog Common Library là module chứa các thành phần dùng chung trong toàn bộ hệ sinh thái Blog Application. Thư viện này cung cấp các class, interface, và tiện ích được sử dụng bởi nhiều microservice khác nhau, giúp giảm thiểu code trùng lặp và đảm bảo tính nhất quán trên toàn hệ thống.

## Tính năng chính

- **Exception Handling**: Định nghĩa và xử lý các ngoại lệ chung
- **API Response Utilities**: Các lớp tiện ích để chuẩn hóa phản hồi API
- **Security Utils**: Các tiện ích bảo mật dùng chung
- **Validation Framework**: Cơ chế xác thực đầu vào chung
- **Date & Time Utilities**: Xử lý ngày giờ thống nhất
- **Constants**: Các hằng số dùng chung trên toàn hệ thống
- **Logging Framework**: Cấu hình và tiện ích ghi log
- **Pagination Utilities**: Xử lý phân trang đồng nhất
- **Data Transfer Objects (DTOs)**: Các DTO dùng chung

## Cấu trúc module

```
blog-common/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/blogApp/common/
│   │   │       ├── config/          # Cấu hình chung
│   │   │       ├── constants/       # Hằng số toàn hệ thống
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── enums/           # Các enum dùng chung
│   │   │       ├── exception/       # Xử lý ngoại lệ
│   │   │       ├── logging/         # Tiện ích ghi log
│   │   │       ├── pagination/      # Tiện ích phân trang
│   │   │       ├── security/        # Tiện ích bảo mật
│   │   │       ├── utils/           # Các tiện ích khác
│   │   │       └── validation/      # Framework xác thực
│   │   └── resources/
│   │       └── META-INF/            # Cấu hình Maven
│   └── test/                        # Unit tests
├── pom.xml                          # Cấu hình Maven
└── README.md                        # Tài liệu hướng dẫn
```

## Các API và tiện ích

### API Response Utilities

```java
// Lớp ApiResponse tiêu chuẩn
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorDetails error;
    private Meta meta;
    
    // Constructors, getters, setters...
}

// Các tiện ích tạo response
public class ResponseBuilder {
    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> error(String message, HttpStatus status) { ... }
    public static <T> ApiResponse<T> paged(Page<T> pagedData) { ... }
}
```

### Exception Handling

```java
// Base exception class
public abstract class BaseException extends RuntimeException {
    private HttpStatus status;
    private String errorCode;
    
    // Constructors, getters...
}

// Specific exceptions
public class ResourceNotFoundException extends BaseException { ... }
public class BadRequestException extends BaseException { ... }
public class UnauthorizedException extends BaseException { ... }
```

### Security Utilities

```java
// JWT utilities
public class JwtUtils {
    public static String extractUsername(String token) { ... }
    public static Date extractExpiration(String token) { ... }
    public static boolean validateToken(String token) { ... }
}

// Password utilities
public class PasswordUtils {
    public static String encode(String rawPassword) { ... }
    public static boolean matches(String rawPassword, String encodedPassword) { ... }
}
```

### Pagination Utilities

```java
// PageRequest builder
public class PageRequestBuilder {
    public static PageRequest build(int page, int size, String sort) { ... }
}

// Pagination metadata
public class PaginationMeta {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    
    // Constructors, getters, setters...
}
```

## Cài đặt và sử dụng

### 1. Thêm dependency vào pom.xml

Thêm dependency blog-common vào các microservice:

```xml
<dependency>
    <groupId>com.blogApp</groupId>
    <artifactId>blog-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Sử dụng trong code

```java
// Sử dụng ApiResponse
@RestController
public class SampleController {
    @GetMapping("/sample")
    public ResponseEntity<ApiResponse<SampleDTO>> getSample() {
        SampleDTO dto = sampleService.get();
        return ResponseEntity.ok(ResponseBuilder.success(dto));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(ResponseBuilder.error(ex.getMessage(), ex.getStatus()));
    }
}

// Sử dụng tiện ích phân trang
@Service
public class SampleService {
    public Page<SampleDTO> getAll(int page, int size, String sort) {
        PageRequest pageRequest = PageRequestBuilder.build(page, size, sort);
        return repository.findAll(pageRequest);
    }
}
```

## Quản lý phiên bản

Blog Common được xây dựng để hỗ trợ backward compatibility. Các thay đổi breaking change sẽ được đánh dấu bằng cách tăng major version. Để đảm bảo tính ổn định:

1. Luôn gắn một phiên bản cụ thể (không sử dụng SNAPSHOT cho môi trường production)
2. Xem tài liệu release notes trước khi nâng cấp phiên bản
3. Kiểm tra kỹ các thay đổi về API trước khi cập nhật

## Các tiêu chuẩn phát triển

Khi đóng góp vào Blog Common Library, vui lòng tuân thủ các tiêu chuẩn sau:

1. **Backward Compatibility**: Cố gắng duy trì khả năng tương thích ngược
2. **Testing**: Code mới phải có unit test với coverage > 80%
3. **Documentation**: Tất cả các API phải được tài liệu hóa đầy đủ
4. **No Dependencies**: Hạn chế thêm dependencies mới để tránh conflict
5. **Clean Code**: Tuân thủ các nguyên tắc SOLID và Clean Code

## Hướng dẫn phát triển

### 1. Xây dựng từ mã nguồn

```bash
# Clone repository
git clone https://github.com/your-org/blog-common.git

# Di chuyển vào thư mục
cd blog-common

# Xây dựng project
./mvnw clean install
```

### 2. Chạy tests

```bash
# Chạy tất cả các tests
./mvnw test

# Chạy với báo cáo coverage
./mvnw test jacoco:report
```

### 3. Thêm tính năng mới

1. Tạo branch mới: `git checkout -b feature/your-feature-name`
2. Thêm code và tests
3. Cập nhật tài liệu
4. Tạo pull request

## Các lưu ý khi sử dụng

1. **Thread Safety**: Các tiện ích đều được thiết kế thread-safe, an toàn cho môi trường đa luồng
2. **Performance**: Tối ưu hiệu suất cho các hoạt động thường xuyên
3. **Logging**: Sử dụng framework logging chung để đảm bảo tính nhất quán
4. **Error Handling**: Luôn xử lý ngoại lệ đúng cách khi sử dụng các tiện ích

## Thông tin thêm

Để có thông tin chi tiết về API, vui lòng tham khảo JavaDocs được tạo khi xây dựng project:

```bash
./mvnw javadoc:javadoc
```

JavaDocs có thể được truy cập tại `target/site/apidocs/index.html` sau khi chạy lệnh trên. 