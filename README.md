# Craft-UML

A powerful **UML Diagram Editor** built with **JavaFX** that allows you to **create, edit, and manage UML diagrams** with ease.  
Design **Class Diagrams** and **Use Case Diagrams**, define relationships, generate code from diagrams, export to PDF, and save your work for future editing.

---

## 🚀 Features

- 📐 **UML Diagram Creation**: Create Class Diagrams and Use Case Diagrams
- ✏️ **Edit & Update**: Modify diagrams and their components anytime
- 🗑️ **Delete Elements**: Remove unwanted classes, use cases, or relationships
- 🔗 **Relationships**: Define and visualize various UML relationships:
  - Association
  - Aggregation
  - Composition
  - Inheritance
- 💻 **Code Generation**: Auto-generate Java code from Class Diagrams
- 💾 **Persistence**: Save diagrams and reload them for later editing
- 📄 **PDF Export**: Convert your diagrams to professional PDF documents
- 🎯 **Intuitive UI**: User-friendly JavaFX interface with drag-and-drop support
- 🖱️ **Interactive Canvas**: Easy manipulation of diagram elements

---

## 🛠️ Tech Stack

**Language:** Java  
**Framework:** JavaFX  
**Build Tool:** Maven / Gradle  
**PDF Export:** iText / Apache PDFBox  
**Serialization:** JSON / XML for diagram persistence  
**Code Generation:** Custom Java code generator  

---

## 📊 Supported UML Diagrams

### 📦 Class Diagrams
- Create classes with attributes and methods
- Define access modifiers (public, private, protected)
- Establish relationships between classes
- Generate skeleton Java code

### 👥 Use Case Diagrams
- Add actors and use cases
- Connect actors to use cases
- Define system boundaries
- Include/Extend relationships

---

## ⚙️ Installation & Setup

Follow these steps to run the project locally 👇

### 1️⃣ Prerequisites

- **Java Development Kit (JDK)** 11 or higher
- **JavaFX SDK** (if not bundled with JDK)
- **Maven** or **Gradle** (for dependency management)
- **IDE**: IntelliJ IDEA, Eclipse, or NetBeans (recommended)

### 2️⃣ Clone the Repository
```bash
git clone https://github.com/samar-2004/Craft-UML.git
cd Craft-UML
```

### 3️⃣ Configure JavaFX

If JavaFX is not included in your JDK:

1. Download JavaFX SDK from [openjfx.io](https://openjfx.io/)
2. Extract and note the path
3. Add VM options in your IDE or command line:
```bash
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

### 4️⃣ Build the Project

**Using Maven:**
```bash
mvn clean install
```

**Using Gradle:**
```bash
./gradlew build
```

### 5️⃣ Run the Application

**Using Maven:**
```bash
mvn javafx:run
```

**Using Gradle:**
```bash
./gradlew run
```

**Using IDE:**
- Open the project in your IDE
- Navigate to the main class (e.g., `Main.java` or `App.java`)
- Run the application

---

## 📂 Project Structure
```
Craft-UML/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controllers/      # JavaFX controllers
│   │   │   ├── models/           # UML diagram models
│   │   │   ├── utils/            # Utility classes
│   │   │   ├── codegen/          # Code generation logic
│   │   │   ├── export/           # PDF export functionality
│   │   │   └── Main.java         # Application entry point
│   │   └── resources/
│   │       ├── fxml/             # FXML layout files
│   │       ├── css/              # Stylesheets
│   │       └── images/           # Icons and images
│   └── test/                     # Unit tests
├── pom.xml / build.gradle        # Build configuration
└── README.md
```

---

## 🎯 Key Features Breakdown

### 📐 Diagram Editor
- **Canvas-based drawing**: Intuitive drag-and-drop interface
- **Element properties**: Edit attributes, methods, and properties
- **Alignment tools**: Snap-to-grid and alignment helpers
- **Zoom & Pan**: Navigate large diagrams easily

### 💻 Code Generation
- Generate Java class skeletons from Class Diagrams
- Include attributes with proper data types
- Generate method signatures
- Support for access modifiers
- Export to `.java` files

### 💾 Save & Load
- Save diagrams in custom format (JSON/XML)
- Load previously saved diagrams for editing
- Project management with multiple diagram support
- Auto-save functionality

### 📄 PDF Export
- High-quality PDF output
- Preserve diagram layout and styling
- Include metadata and documentation
- Multiple export options

---

## 🖥️ Usage Guide

### Creating a Class Diagram

1. Launch the application
2. Click **New Diagram** → **Class Diagram**
3. Drag and drop **Class** elements onto the canvas
4. Double-click a class to edit attributes and methods
5. Use the relationship tools to connect classes
6. Save your diagram or export to PDF

### Generating Code

1. Complete your Class Diagram
2. Click **Tools** → **Generate Code**
3. Select the output directory
4. Review and use the generated Java files

### Exporting to PDF

1. Open your diagram
2. Click **File** → **Export to PDF**
3. Choose location and filename
4. Your professional UML diagram is ready!

---

## 🤝 Contributing

We welcome contributions from developers! 💡 To contribute:

1. Fork the repository
2. Create your feature branch
```bash
git checkout -b feature-name
```

3. Commit your changes
```bash
git commit -m 'Added new feature'
```

4. Push to your branch
```bash
git push origin feature-name
```

5. Open a Pull Request

---

## 🧑‍💻 Author

👤 **Muhammad Samar Junaid**  
💼 Software Engineer | Java Developer  
📧 samarjunaid.dev@gmail.com  
🌐 [GitHub Profile](https://github.com/samar-2004)  
🔗 [LinkedIn Profile](https://www.linkedin.com/in/muhammad-samar-junaid-b955121bb/)

---

## ⭐ Show Your Support

If you liked this project, don't forget to **star the repository** 🌟  
Your support helps improve **Craft-UML**!

---

## 🙏 Acknowledgments

- Thanks to the JavaFX community for excellent documentation
- Inspired by professional UML tools like StarUML and Visual Paradigm
- Special thanks to all contributors

---

**Design Better. Code Faster. Craft UML.** 🎨✨
