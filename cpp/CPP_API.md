<h1 align="center">C++ STL and Build-in API</h1>

<h1 id="table-of-contents">Table of Contents</h1>

- [1. Primitive Arrays (C-Style Fixed Size)](#1-primitive-arrays-c-style-fixed-size)
- [2. Dynamic Array (std::vector)](#2-dynamic-array-stdvector)
- [3. String Manipulation (std::string)](#3-string-manipulation-stdstring)
- [4. Associative Containers (Hash Maps \& Sets)](#4-associative-containers-hash-maps--sets)
- [5. Container Adaptors (Stack, Queue, Deque)](#5-container-adaptors-stack-queue-deque)
- [6. Priority Queue (Heap)](#6-priority-queue-heap)
- [7. Algorithms](#7-algorithms)
- [8. Bit Manipulation](#8-bit-manipulation)
- [9. Limits \& Constants](#9-limits--constants)

---

# 1. Primitive Arrays (C-Style Fixed Size)
**Header:** None (Built-in type)

## Initialization
- **Note:** Fixed size. Allocated on Stack (very fast). No bounds checking.

```cpp
int arr[10];             // ⚠️ Warning: Contains random garbage values!
int arr[10] = {0};       // ✅ Best Practice: Initialize all to 0 (Crucial for DP/Freq)
int arr[] = {1, 2, 3};   // Automatic size deduction
int grid[100][100];      // 2D Array (Use global/static if size is huge to avoid Stack Overflow)
```

## Operations
```cpp
arr[i] = 5;              // Access/Modify O(1)

// Get Size (Old C style)
int len = sizeof(arr) / sizeof(arr[0]); 

// Get Size (C++17 style - Recommended)
int len = std::size(arr);

// Set memory (fast reset to 0 or -1)
// Requires <cstring>
memset(arr, 0, sizeof(arr)); 
memset(arr, -1, sizeof(arr)); 
```

## ⚠️ Interview Pitfalls
1.  **Pointer Decay:** When passing `int arr[]` to a function, it decays into a pointer `int*`. **Size info is LOST.** You must pass the size `n` separately.
2.  **Variable Length Arrays (VLA):** Writing `int arr[n];` (where n is a variable) is **NOT standard C++**. Do not use it. Use `vector` instead.

[⬆️ Back to Top](#table-of-contents)
---

# 2. Dynamic Array (std::vector)
**Header:** `<vector>`

## Initialization
```cpp
vector<int> v;                  // Default initialization (empty)
vector<int> v(n, -1);           // Size n, initialized to -1
vector<int> v = {1, 2, 3};      // Initializer list
vector<vector<int>> grid(m, vector<int>(n, 0)); // m x n Matrix initialized to 0
```

## Core Operations (O(1))
- **Performance:** These are amortized **O(1)**. Use them whenever possible.

```cpp
v.push_back(val);      // Append element to end
v.pop_back();          // Remove last element
v.back();              // Access last element reference (v[n-1])
v.front();             // Access first element reference (v[0])
v.size();              // Return number of elements (size_t)
v.empty();             // Check if vector is empty (bool)
v.resize(n);           // Resize container (fills default values if growing)
v.clear();             // Remove all elements (Capacity remains)
```

## Insert & Erase (O(N))
- **⚠️ Warning:** These operations shift elements. Avoid using inside loops if possible.
- **Note:** Vector `insert` requires iterators, NOT index.

```cpp
// Insert val at index i
v.insert(v.begin() + i, val);

// Insert n copies of val at index i
v.insert(v.begin() + i, n, val); 

// Insert a range from another vector
v.insert(v.end(), v2.begin(), v2.end()); 

// Erase element at index i
v.erase(v.begin() + i);

// Erase range [start, end)
v.erase(v.begin() + i, v.begin() + j);

// Remove specific value (Erase-Remove Idiom)
v.erase(remove(v.begin(), v.end(), val), v.end());
```

[⬆️ Back to Top](#table-of-contents)
---

# 3. String Manipulation (std::string)
**Header:** `<string>`

## Operations
- **Time Complexity:** Append is **O(1)**. Insert/Erase/Substr are **O(N)**.

```cpp
s += 'c';              // Append character
s.push_back('c');      // Append character
s.insert(0, 1, 'c');   // Insert 'c' at index 0 (1 count)
s.substr(pos, len);    // Return substring starting at pos with length len
s.size();              // Return length (Same as .length(), preferred for consistency)
s.resize(n);           // Resize/Truncate to length n - O(N) or O(1) if shrinking
```

## Search & Check (Critical!)
> **⚠️ Warning:** Always check against `string::npos` to handle "not found" cases. Do NOT compare with `-1`.

```cpp
size_t pos = s.find(str); // Find first occurrence
// s.find(str, start_pos); // Find starting from index
// s.rfind(str);           // Find last occurrence

if (pos != string::npos) {
    // Found! 'pos' is the valid index.
} else {
    // Not Found!
}
```

## Type Conversions
```cpp
int n = stoi(s);         // Convert string to int
long long n = stoll(s);  // Convert string to long long
string s = to_string(n); // Convert int/long to string
```

## Character Utilities
**Header:** `<cctype>`

```cpp
isalnum(c); // Check if alphanumeric (A-Z, a-z, 0-9)
isdigit(c); // Check if digit (0-9)
isalpha(c); // Check if alphabetic character
islower(c); // Check if lowercase char
isupper(c); // Check if uppercase char
tolower(c); // Convert char to lowercase
toupper(c); // Convert char to uppercase
```

[⬆️ Back to Top](#table-of-contents)
---

# 4. Associative Containers (Hash Maps & Sets)
**Header:** `<unordered_map>`, `<unordered_set>`

## Overview
- **Time Complexity:** Average **O(1)** for insert, find, and erase. Worst case O(N).
- **Implementation:** Hash Table.

## Unordered Set (Unique Keys)
```cpp
unordered_set<int> s;
s.insert(val);         // Insert element
s.erase(val);          // Remove element
s.count(val);          // Return 1 if present, 0 otherwise
// Initialization from vector (De-duplication)
unordered_set<int> s(vec.begin(), vec.end());
```

## Unordered Map (Key-Value Pairs)
```cpp
unordered_map<string, int> mp;
mp["key"] = 1;         // Insert or update
mp.count("key");       // Check existence (Preferred over find() for boolean checks)
mp.erase("key");       // Remove key

// Iteration (C++17 Structured Binding)
for (auto& [key, val] : mp) {
    // Process key and value
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 5. Container Adaptors (Stack, Queue, Deque)
**Header:** `<stack>`, `<queue>`, `<deque>`

## Stack (LIFO - Last In First Out)
- **Use Case:** DFS, Backtracking, Monotonic Stack.

```cpp
stack<int> st;
st.push(val);
st.pop();   // Remove top element (void return)
st.top();   // Access top element
st.empty(); // Check if empty
```

## Queue (FIFO - First In First Out)
- **Use Case:** BFS (Breadth-First Search).

```cpp
queue<int> q;
q.push(val);
q.pop();    // Remove front element (void return)
q.front();  // Access front element
```

## Deque (Double-Ended Queue)
- **Use Case:** Sliding Window Maximum (Monotonic Queue).

```cpp
deque<int> dq;
dq.push_back(val);  // Insert at back
dq.push_front(val); // Insert at front
dq.pop_back();      // Remove from back
dq.pop_front();     // Remove from front
dq.front();         // Access front
dq.back();          // Access back
```

[⬆️ Back to Top](#table-of-contents)
---

# 6. Priority Queue (Heap)
**Header:** `<queue>`

## Overview
- **Time Complexity:** Push **O(log N)**, Pop **O(log N)**, Top **O(1)**.
- **Underlying Container:** Vector (default).

## Max Heap (Default Behavior)
```cpp
priority_queue<int> maxHeap;
```

## Min Heap (Memorize this syntax)
```cpp
priority_queue<int, vector<int>, greater<int>> minHeap;
```

## Custom Comparator (e.g., for Pair)
```cpp
// Comparator for Min-Heap based on the second element of a pair
struct Comp {
    bool operator()(const pair<int,int>& a, const pair<int,int>& b) {
        return a.second > b.second; // '>' means smallest comes to top
    }
};

// Declaration
priority_queue<pair<int,int>, vector<pair<int,int>>, Comp> pq;
```

[⬆️ Back to Top](#table-of-contents)
---

# 7. Algorithms
**Header:** `<algorithm>`, `<numeric>`

## Sorting & Searching
- **Time Complexity:** Sort is **O(N log N)**. Binary Search is **O(log N)**.

```cpp
sort(v.begin(), v.end());             // Ascending sort
sort(v.rbegin(), v.rend());           // Descending sort

// Custom Comparator (Lambda) - Descending
sort(v.begin(), v.end(), [](int a, int b) {
    return a > b; // Return true if 'a' should go before 'b'
});

// Sort Array of Pairs by Second Element (Common in Interviews)
vector<pair<int, int>> pairs = {{1, 5}, {2, 3}};
sort(pairs.begin(), pairs.end(), [](const auto& a, const auto& b) {
    return a.second < b.second; // Ascending based on .second
});

// Binary Search (Returns iterator)
// lower_bound: First element >= val
auto it = lower_bound(v.begin(), v.end(), val);

// ⚠️ Safety Check: Did we actually find it?
// If it == v.end(), all elements are smaller than val.
// If *it != val, we found a larger element (insertion point), but not val itself.
if (it != v.end() && *it == val) {
    int index = it - v.begin(); // Found exact match
}

// upper_bound: First element > val
auto it = upper_bound(v.begin(), v.end(), val);

// Calculate index from iterator
int index = it - v.begin();
```

## Numerical & Modification
```cpp
reverse(v.begin(), v.end());          // In-place reverse O(N)
max(a, b); / min(a, b);               // Return max/min
max({a, b, c, d}); 
min({a, b, c});
swap(a, b);                           // Swap values
abs(n);                               // Absolute value
accumulate(v.begin(), v.end(), 0);    // Calculate sum O(N)
gcd(a, b);                            // Greatest Common Divisor (C++17)
```

[⬆️ Back to Top](#table-of-contents)
---

# 8. Bit Manipulation
**Header:** None (Built-in operators), `<bit>` (C++20)

## Basic Operators
- **Time Complexity:** All bitwise operations are **O(1)**.

```cpp
a & b;   // AND
a | b;   // OR
a ^ b;   // XOR (0^0=0, 1^1=0, 1^0=1)
~a;      // NOT (Invert all bits)
a << n;  // Left Shift (Multiply by 2^n)
a >> n;  // Right Shift (Divide by 2^n)
```

## Essential Tricks
```cpp
// 1. Check if k-th bit is set
bool isSet = (n >> k) & 1;

// 2. Set k-th bit to 1
n |= (1 << k);

// 3. Clear k-th bit to 0
n &= ~(1 << k);

// 4. Toggle k-th bit
n ^= (1 << k);
```

[⬆️ Back to Top](#table-of-contents)
---

# 9. Limits & Constants
**Header:** `<climits>`

```cpp
INT_MAX   // Maximum value for 32-bit int (2147483647)
INT_MIN   // Minimum value for 32-bit int (-2147483648)
LLONG_MAX // Maximum value for 64-bit long long
```

[⬆️ Back to Top](#table-of-contents)
---