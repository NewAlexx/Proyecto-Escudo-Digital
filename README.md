# Escudo Digital

Sistema de diagnóstico y defensa en ciberseguridad para estudiantes universitarios!!!
Proyecto de Programación II (ISWZ1103), Universidad de Las Américas.

**Versión:** consola y ventanas (Java Swing).

## Requisitos
- JDK 17 o superior
- IntelliJ IDEA (o cualquier IDE de Java)
- Conexión a Internet para las funciones que consultan APIs (RF3, RF4 y RF6)

## Cómo ejecutarlo en IntelliJ
1. `File > Open` y elegir la carpeta `EscudoDigital`.
2. Si IntelliJ lo pide, elegir el JDK 17 en `File > Project Structure > Project > SDK`.
3. Elegir la configuración **Escudo Digital (ventanas)** junto al botón ▶ y presionar **Run**.
   - También se puede abrir `src/vista/MainGUI.java` y presionar ▶.
   - La versión de consola sigue disponible en `src/interfaz/Main.java` (configuración **Escudo Digital (consola)**).
4. Al abrir el programa, acepta el aviso de privacidad y regístrate con tu correo `@udla.edu.ec`. La contraseña se muestra **una sola vez**: anótala para iniciar sesión.
5. Solo se permite una cuenta por equipo. Los datos se guardan en `escudo.dat` y **solo se borran cuando el usuario lo decide**: "Limpiar mi historial" o "Eliminar mi cuenta".

## Clave de VirusTotal (opcional)
1. Crear una cuenta gratuita en VirusTotal y copiar la API Key del perfil!!!
2. Copiar `config.properties.example` como `config.properties` y pegar la clave en `vt.apikey=`.
3. Sin clave, el análisis de mensajes funciona igual, pero sin consultar la reputación de los enlaces.

`config.properties` y `escudo.dat` están en `.gitignore` para no subir la clave ni los datos a GitHub.

## Estructura
```
src/
├── modelo/     Entidades del problema (Usuario, Estudiante, Evaluacion y sus subclases)
├── negocio/    Reglas de negocio, gestores, criptografía, persistencia y clientes de API
├── interfaz/   Menús de consola y ContextoAplicacion (conecta los gestores)
└── vista/      Interfaz gráfica con Java Swing (MainGUI, ventanas y paneles)
```

## Módulos
| Módulo | Requerimientos |
|---|---|
| 1. Acceso seguro y diagnóstico de higiene digital | RF1, RF2 |
| 2. Credenciales y exposición | RF3, RF4, RF5 |
| 3. Defensa ante ingeniería social y riesgo global | RF6, RF7, RF8 |

## APIs utilizadas
- **Pwned Passwords** (gratuita, sin clave): solo recibe los 5 primeros caracteres del hash SHA-1 de la contraseña.
- **XposedOrNot** (gratuita, sin clave) y **LeakCheck** (respaldo): correos en filtraciones.
- **VirusTotal** (API pública gratuita, requiere clave): reputación de enlaces. Límite: 4 consultas por minuto y 500 por día.
