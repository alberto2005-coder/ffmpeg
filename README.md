# FFmpegSuit

[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![FFmpeg](https://img.shields.io/badge/FFmpeg-requerido-007808?logo=ffmpeg&logoColor=white)](https://ffmpeg.org/)
[![MIT](https://img.shields.io/badge/Licencia-MIT-green)](LICENSE)

[Español](#español) · [English](#english)

---

## Español

### Índice

- [Qué es](#qué-es)
- [Características](#características)
- [Requisitos](#requisitos)
- [Compilación](#compilación)
- [Uso](#uso)
- [Modos de ejecución](#modos-de-ejecución)
- [Parámetros de conversión](#parámetros-de-conversión)
- [Licencia](#licencia)

### Qué es

**FFmpegSuit** es una aplicación de escritorio y consola escrita en **Java** que sirve de interfaz amigable para [FFmpeg](https://ffmpeg.org/). Permite convertir, recodificar y escalar vídeos arrastrando archivos a la ventana o pasando argumentos desde la terminal, sin memorizar comandos.

### Características

- **Interfaz Swing** con tema Nimbus.
- **Drag & Drop** para añadir vídeos a la cola.
- **Modo simple** con calidades predefinidas (Baja, Media, Alta, Ultra).
- **Modo avanzado** con control de códec, bitrate, resolución, FPS, CRF y preset.
- **Detección automática de FFmpeg**: lo busca en el sistema si no está en el `PATH`.
- **Modo consola** para servidores y scripts sin interfaz.
- **Multiplataforma**: Windows, Linux y macOS.
- **Salida flexible**: junto al archivo original o en una carpeta elegida.

### Requisitos

| Requisito | Versión mínima |
|---|---|
| JDK | 8 o superior |
| FFmpeg y ffprobe | Cualquier versión reciente |

`ffmpeg` y `ffprobe` deben estar en el `PATH` o en la misma carpeta que el `.class`/`.jar`. Descárgalos desde <https://ffmpeg.org/download.html>.

### Compilación

```bash
git clone https://github.com/alberto2005-coder/ffmpeg.git
cd ffmpeg
javac FFmpegSuit.java
```

### Uso

**Interfaz gráfica (por defecto):**

```bash
java FFmpegSuit
```

**Forzar consola:**

```bash
java FFmpegSuit -x
```

### Modos de ejecución

| Modo | Descripción |
|---|---|
| **GUI** | Interfaz completa; se activa si hay pantalla disponible. |
| **Consola (`-x`)** | Modo texto para entornos *headless* o scripts. |

La aplicación detecta si el entorno no tiene pantalla y cambia de modo automáticamente.

### Parámetros de conversión

**Modo simple**

| Calidad | CRF | Preset | Uso recomendado |
|---|---|---|---|
| Baja | 28 | veryfast | Compartir rápido, archivos pequeños |
| Media | 23 | medium | Uso general |
| **Alta** (defecto) | **18** | **slow** | Streaming y almacenamiento |
| Ultra | 15 | slower | Archivos de alta calidad |

**Modo avanzado**

| Parámetro | Opciones |
|---|---|
| Resolución | Original, 1920:1080, 1280:720, 854:480 |
| Extensión | mp4, webm, mkv, avi, mov, mp3 |
| Códec de vídeo | libx264, libx265, mpeg4, copy, vp9 |
| Códec de audio | aac, mp3, copy, opus |
| FPS | Original, 24, 30, 60 |
| Bitrate de vídeo | Por defecto, 500k → 8000k (editable) |
| Bitrate de audio | Por defecto, 128k, 192k, 320k (editable) |
| Frecuencia de muestreo | Por defecto, 44100, 48000 Hz |
| CRF | 15 (Ultra) → 28 (Baja) |
| Preset | ultrafast → veryslow |

### Licencia

**MIT** — consulta [LICENSE](LICENSE).

---

## English

### Table of contents

- [What it is](#what-it-is)
- [Features](#features)
- [Requirements](#requirements)
- [Building](#building)
- [Usage](#usage)
- [Execution modes](#execution-modes)
- [Conversion parameters](#conversion-parameters)
- [License](#license)

### What it is

**FFmpegSuit** is a **Java** desktop and console application that provides a friendly interface for [FFmpeg](https://ffmpeg.org/). It converts, re-encodes and scales videos by dragging files into the window or by passing arguments from the terminal, so you do not have to memorise commands.

### Features

- **Swing interface** with the Nimbus look and feel.
- **Drag & drop** to add videos to the queue.
- **Simple mode** with preset qualities (Low, Medium, High, Ultra).
- **Advanced mode** with control over codec, bitrate, resolution, FPS, CRF and preset.
- **Automatic FFmpeg detection**: scans the system when it is not on `PATH`.
- **Console mode** for headless servers and scripts.
- **Cross-platform**: Windows, Linux and macOS.
- **Flexible output**: next to the source file or in a chosen folder.

### Requirements

| Requirement | Minimum version |
|---|---|
| JDK | 8 or later |
| FFmpeg and ffprobe | Any recent version |

`ffmpeg` and `ffprobe` must be on `PATH` or in the same folder as the `.class`/`.jar`. Download them from <https://ffmpeg.org/download.html>.

### Building

```bash
git clone https://github.com/alberto2005-coder/ffmpeg.git
cd ffmpeg
javac FFmpegSuit.java
```

### Usage

**GUI (default):**

```bash
java FFmpegSuit
```

**Force console mode:**

```bash
java FFmpegSuit -x
```

### Execution modes

| Mode | Description |
|---|---|
| **GUI** | Full interface; enabled when a display is available. |
| **Console (`-x`)** | Text mode for headless environments or scripts. |

The app detects a headless environment and switches modes automatically.

### Conversion parameters

**Simple mode**

| Quality | CRF | Preset | Recommended for |
|---|---|---|---|
| Low | 28 | veryfast | Quick sharing, small files |
| Medium | 23 | medium | General use |
| **High** (default) | **18** | **slow** | Streaming and storage |
| Ultra | 15 | slower | High-quality archiving |

**Advanced mode**

| Parameter | Options |
|---|---|
| Resolution | Original, 1920:1080, 1280:720, 854:480 |
| Extension | mp4, webm, mkv, avi, mov, mp3 |
| Video codec | libx264, libx265, mpeg4, copy, vp9 |
| Audio codec | aac, mp3, copy, opus |
| Frame rate | Original, 24, 30, 60 |
| Video bitrate | Default, 500k → 8000k (editable) |
| Audio bitrate | Default, 128k, 192k, 320k (editable) |
| Sample rate | Default, 44100, 48000 Hz |
| CRF | 15 (Ultra) → 28 (Low) |
| Preset | ultrafast → veryslow |

### License

**MIT** — see [LICENSE](LICENSE).
