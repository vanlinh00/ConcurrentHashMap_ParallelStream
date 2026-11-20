# Batch Caching with `ConcurrentHashMap`

## 1. The Problem

Imagine your program needs to process 1,000 orders at the same time. To make it fast, you split the work across many threads (different workers running in parallel).

Each order belongs to a vendor (a seller or supplier). To process an order, you need the vendor's details from a database.

* If all 1,000 orders belong to the same vendor, you do not want to ask the database for that vendor's details 1,000 times. That would be very slow!

## 2. The Solution: `computeIfAbsent`

You create a temporary `ConcurrentHashMap` just for this batch of 1,000 orders. When a thread needs a vendor, it runs this code:

```java
return cacheHashMap.computeIfAbsent(vendorId, id -> {
    // Code to get vendor details from the database
});
```

Here is what happens step by step:

1. **First thread arrives:** It looks for `vendorId` in the map. It is not there, so it fetches the vendor from the database and saves it in the map.
2. **Other threads arrive:** If another thread needs the same `vendorId`, the map already has it! It returns the saved data immediately without touching the database.
3. **Safe waiting:** Because `computeIfAbsent` is atomic (happens as one safe, unbreakable step), if two threads ask for the same new vendor at the exact same millisecond, only one thread calls the database while the other waits politely for the result.

## 3. "Use it and throw it away"

Remember earlier when we talked about `ConcurrentHashMap` filling up memory if you keep data forever? This idea avoids that problem completely!

Because you create the map only for these 1,000 orders and delete it when the job finishes, the memory is cleaned up right away. You get super-fast performance without any memory problems.