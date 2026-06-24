# Library Management System - Java Swing + JDBC + SQL Server

## 1. Chuan bi CSDL

Mo SQL Server Management Studio va chay file:

```text
src/main/resources/sql/schema_seed.sql
```

Tai khoan mau:

```text
Manager: MaNV=NV002, password=123456
Staff:   MaNV=NV001, password=123456
Reader:  MaDG=DG001, password=123456
```

## 2. Cau hinh ket noi

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

## 3. Chay ung dung

Can Java 17+ va Maven:

```bash
mvn clean compile exec:java
```

## 4. Background

Neu co anh `1.png`, dat vao:

```text
src/main/resources/assets/1.png
```

Ung dung tu dong dung anh nay lam background. Neu khong co, giao dien van chay bang nen hien dai mac dinh.
