# 📱 Tastel – App Móvil de Recetas  
### Proyecto Académico – Parcial I y II  
**Materia:** Aplicaciones Móviles (Da Vinci)  
**Profesor:** Sergio Daniel Medina  
**Integrantes:** Franco Martín Schimizzi · Melina Rocío Martins

---

# Descripción general

**Tastel** es una aplicación móvil de recetas diseñada para ofrecer una experiencia completa:  
buscar, filtrar, visualizar, guardar favoritas, publicar recetas en comunidad, gestionar perfil y administrar contenido personal.

Este README documenta el **Proyecto Completo**, incluyendo todo lo desarrollado en:

- ✅ **Parcial I (versión local y catálogo de recetas)**
- ✅ **Parcial II (Firebase Auth, Firestore, Comunidad, Perfil editable, CRUD completo)**

---

# Funcionalidades implementadas

## 🟢 **Parcial I – Funcionalidades base**

### Navegación y pantallas
- Splash screen animada.  
- Login hacia Home.  
- Home con buscador, categorías dinámicas (Chips) y listado de recetas.  
- Detalle visual de receta con imagen, descripción y tiempo.  
- Drawer lateral con navegación entre secciones.

### Búsqueda y filtrado
- Búsqueda por nombre.  
- Filtros por categorías (Pastas, Carnes, Veggie, Postres, etc.).  
- Cards clickeables que llevan al detalle.

### Favoritos (versión inicial)
- Persistencia usando `SharedPreferences`.  
- Favoritos por usuario según login.

### Diseño
- Uso de Material Design Components.
- CardView, Chips, Toolbar, NavigationView.
- Layouts mixtos (Linear + Constraint).
- Paleta y estilos en `colors.xml`, `styles.xml`, `dimens.xml`.

---

# 🟣 **Parcial II – Expansión completa con Firebase**

El segundo parcial transformó por completo la app agregando **backend real (Firebase)** y **secciones dinámicas**.

## **Autenticación Firebase (Email + Usuario + Password)**

- Registro con:
  - Nombre  
  - Apellido  
  - Email  
  - Username único  
  - Password  
- Login por:
  - Email  
  - Username (búsqueda en Firestore → login real por email)
- Recuperación de contraseña (username o email).

## **Perfil del usuario**

- Edición de:
  - Nombre  
  - Apellido  
  - Email (con reautenticación y verificación obligatoria)  
  - Contraseña  
- Username fijo e inmutable.  
- Actualización sincronizada en:
  - Firebase Auth  
  - Firestore (`usuarios/{uid}`)

## **Publicación de recetas – Comunidad (Firestore)**

Los usuarios pueden:
- Crear recetas propias.
- Editar las que les pertenecen.
- Eliminar recetas con confirmación.
- Cada receta incluye:
  - Título  
  - Descripción  
  - Imagen  
  - Tiempo (control con flechas ↑↓ y mínimo 1 min)  
  - Autor (username @usuario)  
  - Datos internos para Firestore:
    - `authorId`
    - `authorEmail`
    - `createdAt`
- Se guardan tanto en:
  - `comunidad/`
  - `usuarios/{uid}/recetas/`

## **Feed dinámico de Comunidad**
- Listado en tiempo real mediante `addSnapshotListener`.  
- Ordenado por fecha (`createdAt desc`).  
- Cards con:
  - Imagen  
  - Título  
  - @autor  
  - Descripción truncada  
  - Tiempo  
- Botón favorito flotante integrado por card.

## **Detalle de receta de comunidad**
- Imagen grande  
- Autor con @username  
- Tiempo formateado  
- Descripción completa  
- Acciones:
  - Agregar a favoritos (versión local)
  - Descargar / compartir  
  - Editar si sos dueño  
  - Eliminar si sos dueño  
  - Volver

## Favoritos (actualizado)
- Persistencia por usuario  
- Compatibilidad con recetas de comunidad  
- Las recetas importadas desde comunidad agregan automáticamente:
  - "Subida por @autor"

---

# Tecnologías utilizadas

### **Frontend**
- Android Studio (Java)
- XML para layout
- Material Design Components
- jsDelivr CDN

### **Backend**
- Firebase Authentication  
- Firebase Firestore  
- Firebase Storage (opcional para imágenes, si se quisiera extender)

### **Persistencia local**
- `SharedPreferences`  
- Gson para serialización

### **Gestión del proyecto**
- GitHub  
- Conventional commits  
- Documentación clara y estructurada

---

# Estructura del proyecto

app/
├─ java/com.app.tasteit/
│ ├─ LoginActivity.java
│ ├─ RegisterActivity.java
│ ├─ ProfileActivity.java
│ ├─ MainActivity.java
│ ├─ CommunityActivity.java
│ ├─ CommunityRecipeDetailActivity.java
│ ├─ RecipeFormActivity.java
│ ├─ RecipeDetailActivity.java
│ ├─ CommunityRecipe.java
│ ├─ CommunityRecipeAdapter.java
│ ├─ RecipeAdapter.java
│ └─ AccountMenuHelper.java
├─ res/
│ ├─ layout/ (XML)
│ ├─ drawable/
│ └─ values/ (colors, dimens, strings)


---

# Cómo ejecutar el proyecto

1. Clonar repo:
git clone https://github.com/ccchimi/Tastel.git
2. Abrir en Android Studio  
3. Sincronizar con Gradle  
4. Conectar dispositivo o emulador  
5. Ejecutar app  

---

# Documentación

Incluye informes oficiales del proyecto:
- **Parcial I – Informe**
- **Parcial II – Informe**
- Mockups completos
- Screenshots actualizados
- Explicación técnica detallada

---

# Conclusión

La aplicación **Tastel** pasó de ser un catálogo local de recetas a convertirse en una **plataforma completa con autenticación, perfil, comunidad, CRUD de recetas en la nube y favoritos avanzados**.  
El proyecto cumple **todos los requisitos del Parcial I y II**, incluye documentación profesional y un desarrollo sólido.

---

# Autores
- **Franco Martín Schimizzi**
- **Melina Rocío Martins**
