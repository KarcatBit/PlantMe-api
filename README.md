🌟 README – PLANTME API (BACKEND / SPRING BOOT)

☕ Java • Spring Boot • MySQL • JPA • JWT
<div align="center">
  
# 🌱 PlantMe API – Backend REST
# Servidor seguro para la gestión de plantas y autenticación JWT
</div>
📌 Descripción General

Este backend provee todos los servicios REST necesarios para que la app móvil PlantMe funcione correctamente:

- ✔ Registro y login de usuarios
- ✔ Emisión de tokens JWT
- ✔ Gestión completa de plantas
- ✔ Actualización del estado de riego

🧩  Arquitectura y Tecnologías

🛠 Tecnologías utilizadas

- Capa	Tecnología
- Lenguaje	Java 17
- Framework	Spring Boot
- Seguridad	JWT + Spring Security
- ORM	Hibernate / JPA
- Base de Datos	MySQL
- Testing	JUnit

📐 Diseño por capas

- Controller → recibe peticiones
- Service → lógica de negocio
- Repository → acceso a BD
- Entity/DTOs
- Security Filters

🌐  Endpoints Principales

🔐 Autenticación

- POST /api/auth/register
- POST /api/auth/login

🌿 Plantas

- GET    /api/plantas
- POST   /api/plantas
- PUT    /api/plantas/{id}/regar
- DELETE /api/plantas/{id}

Todos los endpoints (excepto auth) requieren header JWT:

Authorization: Bearer <token>

🛢 Configuración de MySQL

Archivo application.properties:
- spring.datasource.url=jdbc:mysql://localhost:3306/plantme_db
- spring.datasource.username=root
- spring.datasource.password=******
- spring.jpa.hibernate.ddl-auto=update

🔗 Conexión con el Frontend

➡️ Para que el FRONT funcione, este backend debe estar levantado previamente.

El frontend utiliza:
- http://10.0.2.2:8080/api/

▶️ Ejecución del Proyecto

Requisitos
- Java 17
- MySQL en ejecución
- IntelliJ IDEA (recomendado)

Pasos
- git clone https://github.com/KarcatBit/PlantMe-api.git
- Abrir proyecto en IntelliJ
- Esperar importación de Gradle

Ejecutar clase:
- PlantMeApiApplication.java

API disponible en:
- http://localhost:8080

👥 Integrantes del equipo

- Karol Giraldo	
- Paulina Campusano	
<div align="center">
  
# 💧🌿 PlantMe API — Un backend seguro para una app más verde
</div>
