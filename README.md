# P2P File Sharing Platform

A peer-to-peer file sharing web application that enables secure file transfers between users without centralized storage. Users can upload files and generate unique shareable codes for download access.

## 🚀 Features

- **Peer-to-Peer File Transfer**: Direct file sharing between users without storing files on the server
- **Unique Code Generation**: Generate secure codes for file sharing
- **Real-time Communication**: WebSocket-based real-time updates for file transfer status
- **Responsive Frontend**: Modern React.js interface for seamless user experience
- **Session Management**: Robust backend services for peer coordination

## 🛠️ Technology Stack

- **Frontend**: React.js
- **Backend**: Spring Boot (Java)
- **Real-time Communication**: WebSockets
- **Build Tools**: Maven (Backend), npm (Frontend)

## 📋 Prerequisites

Before running this application, make sure you have the following installed:

- **Java JDK 11 or higher**
- **Node.js** (version 14 or higher)
- **npm** or **yarn**
- **Maven** (for Spring Boot backend)
- **Git**

## 🔧 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/shahid-43/p2pfilesharing.git
cd p2pfilesharing
```

### 2. Backend Setup (Spring Boot)

Navigate to the backend directory:
```bash
cd backend
```

Install dependencies and build the project:
```bash
mvn clean install
```

Run the Spring Boot application:
```bash
mvn spring-boot:run
```

Alternatively, you can run the JAR file:
```bash
java -jar target/p2p-filesharing-0.0.1-SNAPSHOT.jar
```

The backend server will start on `http://localhost:8080`

### 3. Frontend Setup (React.js)

Open a new terminal and navigate to the frontend directory:
```bash
cd frontend
```

Install npm dependencies:
```bash
npm install
```

Start the React development server:
```bash
npm start
```

The frontend application will start on `http://localhost:3000`

## 🎯 Usage

1. **Access the Application**: Open your browser and navigate to `http://localhost:3000`

2. **Upload a File**:
   - Click on the "Upload File" button
   - Select the file you want to share
   - A unique sharing code will be generated

3. **Share the Code**: Share the generated code with the person you want to send the file to

4. **Download a File**:
   - Enter the sharing code you received
   - Click "Download" to receive the file

## 🏗️ Project Structure

```
p2pfilesharing/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── p2p/
│   │   │   │           ├── controller/
│   │   │   │           ├── service/
│   │   │   │           ├── model/
│   │   │   │           └── config/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── README.md
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── utils/
│   │   └── App.js
│   ├── public/
│   ├── package.json
│   └── README.md
└── README.md
```

## 🔌 API Endpoints

### File Operations
- `POST /api/upload` - Upload a file and generate sharing code
- `GET /api/download/{code}` - Download file using sharing code
- `GET /api/file/{code}/info` - Get file information

### WebSocket Endpoints
- `/ws/file-transfer` - Real-time file transfer status updates

## ⚡ Development

### Running in Development Mode

**Backend (Spring Boot)**:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend (React)**:
```bash
cd frontend
npm run dev
```

### Building for Production

**Backend**:
```bash
cd backend
mvn clean package -Pprod
```

**Frontend**:
```bash
cd frontend
npm run build
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 🔒 Security Features

- Unique code generation for each file transfer
- Session-based peer coordination
- No permanent file storage on server
- Secure WebSocket connections

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch 
3. Commit your changes 
4. Push to the branch 
5. Open a Pull Request

## 👨‍💻 Author

**Shahid Shareef Mohammad**
- GitHub: [@shahid-43](https://github.com/shahid-43)
- LinkedIn: [Mohammad Shahid Shareef](https://www.linkedin.com/in/mohammad-shahid-shareef-8135b2181)

## 🙏 Acknowledgments

- Thanks to the Spring Boot and React.js communities
- WebSocket implementation inspiration from various open-source projects

---

⭐ If you found this project helpful, please give it a star!
