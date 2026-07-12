# 🕵️‍♂️ Plagiarism Detector Pro (Java Swing)

A robust, multithreaded desktop application built in pure Java that detects text similarity and potential plagiarism across multiple documents. 

This project was developed to demonstrate core computer engineering concepts, including algorithmic dynamic programming, multithreading for performance optimization, and graphical user interface (GUI) development.

## ✨ Key Features

* **Graphical User Interface (GUI):** A clean, responsive dashboard built with Java Swing.
* **Bulk Folder Scanning:** Automatically reads and parses up to 63 `.txt` files from a selected local directory.
* **Advanced Math Engine:** Utilizes the **Longest Common Subsequence (LCS)** algorithm via dynamic programming to accurately compare word structures and sequences, ignoring basic punctuation changes.
* **Stop-Word Filtering:** Includes a toggleable feature to filter out common English words (the, and, is, etc.) to prevent artificially inflated similarity scores.
* **Multithreaded Performance:** Leverages Java's `ExecutorService` to run heavy matrix calculations in the background, keeping the UI completely responsive.
* **CSV Export:** Generates downloadable Excel-ready `.csv` reports of the analysis.
* **Secure Login Module:** Includes a simulated authentication gateway for system access.

## ⚙️ The Algorithm (LCS)
Instead of comparing documents character-by-character, this engine tokenizes documents into word arrays. It builds an `(M+1) x (N+1)` matrix to find the longest continuous sequence of words that appear in the exact same order in both documents. 
* *Time Complexity:* `O(M * N)` per document pair.
* *Similarity Score:* Calculated as `(LCS Length / Max Document Word Count) * 100`.

## 🚀 How to Run Locally

### Prerequisites
* Java Development Kit (JDK) 8 or higher installed on your machine.

### Installation
1. Clone this repository to your local machine:
   ```bash
   git clone [https://github.com/YOUR-USERNAME/plagiarism-detector.git](https://github.com/YOUR-USERNAME/plagiarism-detector.git)
