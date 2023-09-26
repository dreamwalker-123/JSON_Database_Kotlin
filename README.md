# JSON_Database_Kotlin
what do we have here:
1) transfer from the client to the local server of objects in JSON format
2) Executors, ExecutorService
3) IO/NIO DataInputStream, DataOutputStream
4) @Serializable - Json.encodeToString(), Json.decodeFromString<>()
5) java.util.concurrent.locks.Lock     .ReadWriteLock    .ReentrantReadWriteLock
   
  A lock is a tool for controlling access to a shared resource by multiple threads.
  Commonly, a lock provides exclusive access to a shared resource: only one thread at a time
  can acquire the lock and all access to the shared resource requires that the lock be acquired first.
  However, some locks may allow concurrent access to a shared resource, such as the read lock of a ReadWriteLock.
7) File .writeText() .readText()
8) InetAddress, ServerSocket, Socket, 
