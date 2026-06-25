# Library Management System - Java Swing + JDBC + SQL Server

## 1. Cấu hình kết nối

Sửa file:

```text
src/main/resources/config.properties
```

Theo SQL Server của bạn, vi du:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QL_THUVIEN;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=123456
```

## 2. Chạy ứng dụng

Cần Java 17+ và Maven:

```bash
mvn clean compile exec:java
```

## 3. Background

Nếu có ảnh `1.png`, đặt vào:

```text
src/main/resources/assets/1.png
```

Ứng dụng tự động dùng ảnh này làm background. 
