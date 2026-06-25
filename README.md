# Library Management System - Java Swing + JDBC + SQL Server

## 1. Cau hinh ket noi

Sua file:

```text
src/main/resources/config.properties
```

Theo SQL Server cua ban, vi du:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QL_THUVIEN;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=123456
```

## 2. Chay ung dung

Can Java 17+ va Maven:

```bash
mvn clean compile exec:java
```

## 3. Background

Neu co anh `1.png`, dat vao:

```text
src/main/resources/assets/1.png
```

Ung dung tu dong dung anh nay lam background. Neu khong co, giao dien van chay bang nen hien dai mac dinh.
