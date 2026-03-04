<div align="center">

# 🎬 FFmpegSuit — Fast Video Converter

**Conversor de video rápido basado en FFmpeg | FFmpeg-powered fast video converter**

[![Java](https://img.shields.io/badge/Java-SE%208%2B-orange?logo=openjdk&logoColor=white)](https://www.java.com/)
[![FFmpeg](https://img.shields.io/badge/FFmpeg-required-green?logo=ffmpeg&logoColor=white)](https://ffmpeg.org/)
[![License](https://img.shields.io/badge/License-See%20LICENSE-blue)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)]()

</div>

---

## 📋 Índice / Table of Contents

- [🇪🇸 Español](#-español)
  - [¿Qué es?](#qué-es)
  - [Características](#características)
  - [Requisitos](#requisitos)
  - [Cómo compilar](#cómo-compilar)
  - [Cómo usar](#cómo-usar)
  - [Modos de ejecución](#modos-de-ejecución)
  - [Parámetros de conversión](#parámetros-de-conversión)
- [🇬🇧 English](#-english)
  - [What is it?](#what-is-it)
  - [Features](#features)
  - [Requirements](#requirements)
  - [How to build](#how-to-build)
  - [How to use](#how-to-use)
  - [Execution modes](#execution-modes)
  - [Conversion parameters](#conversion-parameters)

---

## 🇪🇸 Español

### ¿Qué es?

**FFmpegSuit** es una aplicación Java de escritorio (y consola) que actúa como interfaz gráfica amigable para [FFmpeg](https://ffmpeg.org/), la herramienta de procesamiento multimedia más potente del mundo. Permite convertir, recodificar y escalar videos de forma sencilla, ya sea arrastrando archivos a la interfaz gráfica o ejecutándola directamente desde la terminal.

### Características

- 🖥️ **Interfaz gráfica (GUI)** moderna usando Java Swing con tema Nimbus
- 🖱️ **Drag & Drop** — arrastra tus videos directamente a la cola
- ⚙️ **Modo Simple** — elige solo una calidad predefinida (Baja / Media / Alta / Ultra)
- 🔧 **Modo Avanzado** — control total sobre codec, bitrate, resolución, FPS, CRF, preset, etc.
- 🔍 **Búsqueda automática de FFmpeg** — busca el binario en todo el sistema si no está en el PATH
- 💻 **Modo consola** — funciona en entornos sin interfaz gráfica (servidores, scripts)
- 🌐 **Multiplataforma** — Windows, Linux y macOS
- 📁 **Salida flexible** — guarda en la misma carpeta del archivo o en una carpeta personalizada

### Requisitos

| Requisito | Versión mínima |
|-----------|---------------|
| Java JDK  | 8 o superior  |
| FFmpeg    | Cualquier versión reciente |

> **Nota:** `ffmpeg` y `ffprobe` deben estar en el PATH del sistema, o bien colocados en la misma carpeta que el `.jar`/`.class`.
> Descarga FFmpeg desde: https://ffmpeg.org/download.html

### Cómo compilar

```bash
javac FFmpegSuit.java
```

### Cómo usar

**Modo gráfico (por defecto):**
```bash
java FFmpegSuit
```

**Modo consola (forzado):**
```bash
java FFmpegSuit -x
```

### Modos de ejecución

| Modo | Descripción |
|------|-------------|
| **GUI** | Interfaz gráfica completa. Se activa automáticamente si hay pantalla disponible. |
| **Consola (`-x`)** | Modo texto para entornos sin pantalla o scripts automatizados. |

La aplicación detecta automáticamente si el entorno es "headless" (sin pantalla) y ajusta el modo de ejecución.

### Parámetros de conversión

#### Modo Simple (calidades predefinidas)

| Calidad | CRF | Preset | Uso recomendado |
|---------|-----|--------|-----------------|
| Baja    | 28  | veryfast | Compartir rápido, archivos pequeños |
| Media   | 23  | medium   | Uso general |
| **Alta** (defecto) | **18** | **slow** | **Streaming, almacenamiento** |
| Ultra   | 15  | slower   | Archivos de alta calidad |

#### Modo Avanzado

| Parámetro | Opciones disponibles |
|-----------|---------------------|
| Resolución | Original, 1920:1080, 1280:720, 854:480 |
| Extensión  | mp4, webm, mkv, avi, mov, mp3 |
| Codec Video | libx264, libx265, mpeg4, copy, vp9 |
| Codec Audio | aac, mp3, copy, opus |
| Frame Rate | Original, 24, 30, 60 fps |
| Video Bitrate | Default, 500k → 8000k (editable) |
| Audio Bitrate | Default, 128k, 192k, 320k (editable) |
| Sample Rate | Default, 44100, 48000 Hz |
| CRF | 15 (Ultra) → 28 (Low) |
| Preset | ultrafast → veryslow |

---

## 🇬🇧 English

### What is it?

**FFmpegSuit** is a Java desktop (and console) application that serves as a friendly graphical interface for [FFmpeg](https://ffmpeg.org/), the world's most powerful multimedia processing tool. It allows you to convert, re-encode, and scale videos easily — either by dragging files into the GUI or running it directly from the terminal.

### Features

- 🖥️ **Modern GUI** built with Java Swing and the Nimbus Look & Feel
- 🖱️ **Drag & Drop** — drag your videos directly into the queue
- ⚙️ **Simple Mode** — just pick a preset quality (Low / Medium / High / Ultra)
- 🔧 **Advanced Mode** — full control over codec, bitrate, resolution, FPS, CRF, preset, and more
- 🔍 **Auto-detect FFmpeg** — scans the entire system for the binary if not found in PATH
- 💻 **Console Mode** — works in headless environments (servers, scripts)
- 🌐 **Cross-platform** — Windows, Linux, and macOS
- 📁 **Flexible output** — save to the same folder as the source or a custom directory

### Requirements

| Requirement | Minimum Version |
|-------------|----------------|
| Java JDK    | 8 or higher    |
| FFmpeg      | Any recent version |

> **Note:** `ffmpeg` and `ffprobe` must be in your system PATH, or placed in the same directory as the `.jar`/`.class`.
> Download FFmpeg at: https://ffmpeg.org/download.html

### How to build

```bash
javac FFmpegSuit.java
```

### How to use

**GUI mode (default):**
```bash
java FFmpegSuit
```

**Console mode (forced):**
```bash
java FFmpegSuit -x
```

### Execution modes

| Mode | Description |
|------|-------------|
| **GUI** | Full graphical interface. Activates automatically when a display is available. |
| **Console (`-x`)** | Text mode for headless environments or automated scripts. |

The application automatically detects if the environment is headless and adjusts accordingly.

### Conversion parameters

#### Simple Mode (preset qualities)

| Quality | CRF | Preset | Recommended for |
|---------|-----|--------|-----------------|
| Low     | 28  | veryfast | Quick sharing, small files |
| Medium  | 23  | medium   | General use |
| **High** (default) | **18** | **slow** | **Streaming, storage** |
| Ultra   | 15  | slower   | High-quality archiving |

#### Advanced Mode

| Parameter | Available Options |
|-----------|-----------------|
| Resolution | Original, 1920:1080, 1280:720, 854:480 |
| Extension  | mp4, webm, mkv, avi, mov, mp3 |
| Video Codec | libx264, libx265, mpeg4, copy, vp9 |
| Audio Codec | aac, mp3, copy, opus |
| Frame Rate | Original, 24, 30, 60 fps |
| Video Bitrate | Default, 500k → 8000k (editable) |
| Audio Bitrate | Default, 128k, 192k, 320k (editable) |
| Sample Rate | Default, 44100, 48000 Hz |
| CRF | 15 (Ultra) → 28 (Low) |
| Preset | ultrafast → veryslow |

---

<div align="center">

**Made with ❤️ in Java · Powered by [FFmpeg](https://ffmpeg.org/)**

</div>
