# 🔥 Firebase Setup Guide

## ✅ Already Configured

Your app already has Firebase Authentication and Firestore integrated! Here's what's working:

### **RegisterActivity**
- ✅ Creates user with email & password using `FirebaseAuth.createUserWithEmailAndPassword()`
- ✅ Stores user data to Firestore in `users` collection
- ✅ Saves: uid, username, name, email, bio, age, schoolyear, gender, major, photoUrl, likes, preference

### **LoginActivity**
- ✅ Signs in users with `FirebaseAuth.signInWithEmailAndPassword()`
- ✅ Auto-redirects if already logged in
- ✅ Navigates to MainActivity on success

## 📋 Setup Steps (If Not Done)

### 1. **Create Firebase Project**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "DatingAppUMN"
4. Follow the setup wizard

### 2. **Add Android App to Firebase**
1. In Firebase Console, click "Add app" → Android icon
2. Enter package name: `com.example.datingapp`
3. Download `google-services.json`
4. Place it in: `app/google-services.json`

### 3. **Enable Authentication**
1. In Firebase Console → Authentication
2. Click "Get Started"
3. Go to "Sign-in method" tab
4. Enable "Email/Password"
5. Click "Save"

### 4. **Enable Firestore Database**
1. In Firebase Console → Firestore Database
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location (closest to your users)
5. Click "Enable"

### 5. **Firestore Security Rules (For Development)**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    match /matches/{matchId} {
      allow read, write: if request.auth != null;
    }
    match /messages/{messageId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🔧 Current Dependencies

```kotlin
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
```

## 📊 Firestore Data Structure

### **users** Collection
```json
{
  "uid": "user_id_here",
  "username": "john_doe",
  "name": "John Doe",
  "email": "john@example.com",
  "bio": "",
  "age": "",
  "schoolyear": "",
  "gender": "",
  "major": "",
  "photoUrl": "",
  "likes": [],
  "preference": {
    "gender": "All",
    "yearPreferences": "All",
    "majorPreferences": []
  }
}
```

## 🧪 Testing

### **Test Registration:**
1. Run the app
2. Click "Register"
3. Fill in: username, name, email, password
4. Click "Sign Up"
5. Check Firebase Console → Authentication (user should appear)
6. Check Firestore → users collection (document should exist)

### **Test Login:**
1. Use registered email & password
2. Click "Login"
3. Should navigate to MainActivity

## 🚨 Common Issues

### **Issue: google-services.json not found**
- **Solution**: Download from Firebase Console and place in `app/` folder

### **Issue: Authentication failed**
- **Solution**: Check if Email/Password is enabled in Firebase Console

### **Issue: Firestore permission denied**
- **Solution**: Update Firestore rules to allow read/write in test mode

### **Issue: Build fails with Firebase**
- **Solution**: Sync Gradle files and ensure `google-services` plugin is applied

## 📱 Next Steps

1. ✅ Registration with email/password - **DONE**
2. ✅ Login with email/password - **DONE**
3. ✅ Store user data to Firestore - **DONE**
4. 🔄 Update MatchListFragment to load matches from Firestore
5. 🔄 Update ChatFragment to use Firestore for messages
6. 🔄 Add profile image upload to Firebase Storage

## 🔐 Security Best Practices

1. **Never commit `google-services.json` to public repos**
2. **Use proper Firestore security rules in production**
3. **Validate email format before registration**
4. **Add password strength requirements**
5. **Implement email verification**
6. **Add password reset functionality**

---

**Your Firebase setup is complete and ready to use!** 🎉
