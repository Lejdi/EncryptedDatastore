# Encrypted DataStore
Small library providing a way to securely store data in shared preferences using DataStore. 
You can insert any type of data, it is serialized using GSON and then encrypted using Android Key Store. 
The `EncryptedDataStore` class has methods for insererting data, retrieving it and removing from preferences.
Additionally you can extend this class and override the encryption algorithm which is "AES/GCM/NoPadding" by default.