# Contributing to Plagiarism Detector Pro

Thank you for your interest in improving Plagiarism Detector Pro! 

## How Can I Contribute?

### 1. Feature Requests & Enhancements
We are always looking to expand the app's capabilities. If you have an idea—such as adding PDF/Word document support, integrating a database for logins, or optimizing the LCS algorithm—please open an **Issue** to discuss it before writing code.

### 2. Submitting Pull Requests
1. **Fork** the repository.
2. **Clone** your fork locally.
3. **Create a branch** for your feature: `git checkout -b feature/AddPDFSupport`
4. **Make your changes**. 
5. **Test extensively**: Ensure that the `SwingWorker` threads are not creating memory leaks and that the UI remains responsive during analysis.
6. **Commit your changes**: `git commit -m "Add feature X"`
7. **Push** to your fork: `git push origin feature/AddPDFSupport`
8. **Open a Pull Request** against the `main` branch.

## Coding Standards
* **Concurrency:** Any heavy data processing must remain inside the `doInBackground()` method of the `SwingWorker` or within the `ExecutorService`. Do not block the Event Dispatch Thread (EDT).
* **UI Updates:** All UI updates must happen within the EDT (e.g., inside the `done()` method of the `SwingWorker`).
