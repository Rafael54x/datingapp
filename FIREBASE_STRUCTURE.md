# 🔥 Firebase Firestore Structure

## **Collections & Documents**

### **1. users** (Collection)
Menyimpan data semua user yang terdaftar.

**Document ID:** `{userId}` (dari Firebase Auth UID)

```json
{
  "uid": "userId123",
  "username": "john_doe",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "age": "21",
  "gender": "M",
  "schoolyear": "3",
  "major": "TI",
  "school": "UMN",
  "bio": "Hello!",
  "photoUrl": "https://...",
  "likes": [],
  "preference": {
    "gender": "F",
    "yearPreferences": "ANY",
    "majorPreferences": []
  }
}
```

---

### **2. swipes** (Collection)
Menyimpan data swipe (like/pass) setiap user.

**Document ID:** `{userId}`

```json
{
  "liked": ["userId2", "userId3", "userId5"],
  "passed": ["userId4", "userId6"]
}
```

**Penjelasan:**
- `liked`: Array berisi userId yang di-swipe kanan (like)
- `passed`: Array berisi userId yang di-swipe kiri (dislike)

---

### **3. matches** (Collection)
Menyimpan data match antara 2 user yang saling like.

**Document ID:** `{userId1}_{userId2}` (sorted alphabetically)

Contoh: Jika user A (uid: `abc`) match dengan user B (uid: `xyz`), maka matchId = `abc_xyz`

```json
{
  "users": ["userId1", "userId2"],
  "lastMessage": "Hi! How are you?",
  "timestamp": 1702123456789
}
```

**Sub-collection: chats**

Path: `matches/{matchId}/chats`

Setiap message adalah document dengan auto-generated ID:

```json
{
  "senderId": "userId1",
  "text": "Hello!",
  "timestamp": 1702123456789
}
```

---

## **Alur Kerja Fitur**

### **🔄 Swipe (Like/Dislike)**

#### **Swipe Kanan (Like):**
1. User swipe kanan pada user lain
2. Tambahkan userId target ke `swipes/{myId}/liked`
3. Cek apakah target juga sudah like kita di `swipes/{targetId}/liked`
4. Jika ya → **MATCH!**
   - Buat document di `matches/{matchId}`
   - Tampilkan dialog match
5. Jika tidak → Tunggu target swipe kita

#### **Swipe Kiri (Dislike):**
1. User swipe kiri pada user lain
2. Tambahkan userId target ke `swipes/{myId}/passed`
3. User tidak akan muncul lagi di card stack

---

### **💬 Chat Real-time**

#### **Kirim Message:**
1. User ketik pesan dan klik send
2. Tambahkan document baru ke `matches/{matchId}/chats`
3. Update `lastMessage` di `matches/{matchId}`

#### **Terima Message:**
1. ChatFragment listen ke `matches/{matchId}/chats` dengan `addSnapshotListener`
2. Setiap ada message baru, otomatis muncul di chat
3. **Real-time!** Tidak perlu refresh

---

### **📋 Match List**

1. Load semua matches dari `matches` collection
2. Filter: `whereArrayContains("users", myId)`
3. Untuk setiap match, ambil data partner dari `users` collection
4. Tampilkan list dengan nama, foto, dan lastMessage
5. **Real-time!** Menggunakan `addSnapshotListener`

---

## **Security Rules**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && request.auth.uid == userId;
    }
    
    // Swipes collection
    match /swipes/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Matches collection
    match /matches/{matchId} {
      allow read: if request.auth != null && 
                     request.auth.uid in resource.data.users;
      allow write: if request.auth != null;
      
      // Chats sub-collection
      match /chats/{chatId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null;
      }
    }
  }
}
```

---

## **Contoh Query**

### **Load Users untuk Swipe:**
```kotlin
// Exclude users yang sudah di-swipe
firestore.collection("swipes").document(myId).get()
  .addOnSuccessListener { swipeDoc ->
    val liked = swipeDoc.get("liked") as? List<String> ?: emptyList()
    val passed = swipeDoc.get("passed") as? List<String> ?: emptyList()
    val alreadySwiped = (liked + passed).toSet()
    
    firestore.collection("users").get()
      .addOnSuccessListener { result ->
        val potentialMatches = result.toObjects(User::class.java)
          .filter { it.uid != myId && !alreadySwiped.contains(it.uid) }
      }
  }
```

### **Load Matches Real-time:**
```kotlin
firestore.collection("matches")
  .whereArrayContains("users", myId)
  .orderBy("timestamp", Query.Direction.DESCENDING)
  .addSnapshotListener { snapshots, error ->
    // Update UI otomatis saat ada perubahan
  }
```

### **Load Chat Messages Real-time:**
```kotlin
firestore.collection("matches").document(matchId)
  .collection("chats")
  .orderBy("timestamp", Query.Direction.ASCENDING)
  .addSnapshotListener { snapshots, error ->
    // Update chat otomatis saat ada message baru
  }
```

---

## **Testing Checklist**

- [ ] Register 2 user berbeda
- [ ] Login sebagai User A
- [ ] Swipe kanan pada User B
- [ ] Logout, login sebagai User B
- [ ] Swipe kanan pada User A
- [ ] **Match dialog muncul!**
- [ ] Buka Match List → User A & B muncul
- [ ] Klik match → Buka chat
- [ ] Kirim message dari User A
- [ ] Login sebagai User B → Message muncul otomatis (real-time)
- [ ] Balas dari User B → User A terima otomatis

---

**Semua fitur sudah real-time menggunakan Firebase Firestore!** 🎉
