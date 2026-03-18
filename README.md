# 🎬 Video Story App Backend

## 📖 Overview
This repository contains the backend for the **Video Story App**, a platform for streaming and managing video stories.  
It supports both **user features** (login, content list, video playback, profile) and **admin features** (content management, user management, revenue dashboard).

---

## 🚀 Features
- **User**
  - Login / Signup
  - Browse content list
  - Play videos with episode continuation
  - View and edit profile details
- **Admin**
  - Add / edit / delete content
  - Manage users and roles
  - Revenue dashboard with analytics

---

## 🛠️ Tech Stack
- **Node.js + Express** – REST API framework  
- **MongoDB / PostgreSQL** – Database  
- **JWT** – Authentication  
- **Multer** – File uploads (thumbnails, videos)  

---

## 📂 Project Structure
backend/
│── src/
│   ├── controllers/   # API logic
│   ├── models/        # Database schemas
│   ├── routes/        # API routes
│   ├── middleware/    # Auth & validation
│   └── utils/         # Helpers
│── uploads/           # Thumbnails & videos
│── config/            # DB & environment configs
│── server.js          # Entry point
│── README.md


---

🔑 API Endpoints
User
POST /api/auth/login – Login

POST /api/auth/signup – Register

GET /api/stories – Fetch content list

GET /api/stories/:id – Get video + next episodes

GET /api/profile/:id – Get user profile

Admin
POST /api/stories/create – Add new content

PUT /api/stories/:id – Edit content

DELETE /api/stories/:id – Delete content

GET /api/admin/users – Manage users

GET /api/admin/revenue – Revenue dashboard


