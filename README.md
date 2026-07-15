<div align="center">
  <h1>🕵️‍♂️ Plagiarism Detector Pro</h1>
  <h3><em>A high-performance, multithreaded desktop application for document similarity analysis</em></h3>
</div>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11%2B-blue.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/GUI-Swing-orange.svg" alt="Java Swing">
  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License: MIT">
</p>

> **Overview:** Plagiarism Detector Pro is a standalone Java desktop application that compares large batches of text documents to detect similarities and potential plagiarism. It utilizes the Longest Common Subsequence (LCS) algorithm paired with a multithreaded architecture to process up to 63 files simultaneously without freezing the user interface.

---

## 📖 Table of Contents
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Installation & Usage](#-installation--usage)
- [System Access (Default Credentials)](#-system-access)
- [Technical Architecture](#-technical-architecture)

---

## 🚀 Features

* **Advanced Similarity Algorithm:** Uses Longest Common Subsequence (LCS) dynamic programming to compare document token arrays.
* **Multithreaded Processing:** Utilizes `ExecutorService` and `SwingWorker` to utilize all available CPU cores, ensuring the GUI remains highly responsive during heavy mathematical calculations.
* **Intelligent Text Parsing:** Automatically strips punctuation, normalizes cases, and features an optional **Stop Word Filter** to remove common syntax (e.g., "the", "and", "is") and increase accuracy.
* **Dual-View Dashboard:** View all comparisons in one table, or quickly pivot to the "Action Required" tab to see only documents with a >= 60% similarity match.
* **CSV Reporting:** One-click export of all analysis results to a `.csv` file for external auditing and record-keeping.

---

## ⚙️ Prerequisites

To run this project, you need:
* **Java Development Kit (JDK) 11 or higher** (Required for `Files.readString` API).

---

## 💻 Installation & Usage

1. Clone this repository to your local machine.
2. Open your terminal and navigate to the directory containing the source code.
3. Compile the application:
   ```bash
   javac PlagiarismDetectorGUI.java

Run the application:

Bash
java PlagiarismDetectorGUI
Log in, select a folder containing at least two .txt files, and click Run Analysis.

🔐 System Access
Upon launching the application, you will be prompted with a security login dialog. Use the following default credentials to access the system:

Username: admin

Password: password123

(Note: For production environments, it is highly recommended to replace these hardcoded credentials with a secure database authentication system).

🧠 Technical Architecture
UI Framework: Java Swing (JFrame, JTable, JTabbedPane)

Concurrency: java.util.concurrent.ExecutorService, javax.swing.SwingWorker

File I/O: java.nio.file.Files, java.io.FileWriter
