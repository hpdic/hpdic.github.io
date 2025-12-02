<h1 align="center">C++ STL and Build-in API</h1>

<h1 id="table-of-contents">Table of Contents</h1>

- [1. Primitive Arrays (C-Style Fixed Size)](#1-primitive-arrays-c-style-fixed-size)
- [2. Dynamic Array (std::vector)](#2-dynamic-array-stdvector)
- [3. String Manipulation (std::string)](#3-string-manipulation-stdstring)
- [4. Associative Containers (Hash Maps \& Sets)](#4-associative-containers-hash-maps--sets)
- [5. Container Adaptors (Stack, Queue, Deque, List)](#5-container-adaptors-stack-queue-deque-list)
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

// Get Size (C++17 style - Recommended)
int len = std::size(arr);

// Set memory (C++ Style: Element-wise)
// Works for ANY value (e.g., 5, nullptr). Requires <algorithm>.
fill(begin(arr), end(arr), 5); 
fill(begin(arr), end(arr), nullptr);
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
fill(v.begin(), v.end(), -1);   // Fill range with value -1
iota(v.begin(), v.end(), 0);    // Fill range with 0, 1, 2... (Sequential)
```

## Capacity & Performance (Crucial for Optimization)
> **Difference:** `resize` changes logical size (adds elements). `reserve` changes capacity (pre-allocates memory) to prevent reallocation.

```cpp
v.reserve(100);   // Allocates memory for 100 ints. Size is still 0.
                  // Use this before a loop of push_back() to boost speed.

v.resize(100);    // Resizes array to 100. New elements are 0. Size is 100.

v.capacity();     // Current allocated capacity
v.shrink_to_fit(); // Frees unused memory (Capacity -> Size)
```

## Core Operations (O(1))
- **Performance:** These are amortized **O(1)**. Use them whenever possible.

```cpp
v.push_back(val);           // Append element to end
v.emplace_back(key, val);   // equivalent to but faster than v.push_back({key, val});
v.pop_back();               // Remove last element
v.back();                   // Access last element reference (v[n-1])
v.front();                  // Access first element reference (v[0])
v.size();                   // Return number of elements (size_t)
v.empty();                  // Check if vector is empty (bool)
v.resize(n);                // Resize container (fills default values if growing)
v.clear();                  // Remove all elements (Capacity remains)

// Insert & Erase (O(N)), they shift elements; avoid using inside loops
v.insert(v.begin() + i, val);   // Insert val at index i, slow
v.erase(v.begin() + i);         // Erase element at index i
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
s.substr(pos, len);    // Return substring [pos, pos + len)
s.substr(pos);         // Return substring [pos, end) (Default len is npos)
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
tolower(c); // Convert char to lowercase; return c if isalpha(c) == false
toupper(c); // Convert char to uppercase; return c if isalpha(c) == false
```

## String Stream (std::stringstream)
**Header:** `<sstream>`
> **Usage:** Most efficient way for string splitting, type parsing, and formatting.

```cpp
stringstream ss(s);       // Initialize stream with string s
string word;

ss >> word;               // Extract next word (skips spaces/tabs/newlines)
ss << val;                // Insert value (int/string/etc) into stream
ss.str();                 // Return the underlying string
getline(ss, word, ',');   // Read into 'word' until delimiter ','

// Pattern 1: Split by space (Loop)
while (ss >> word) { ... }

// Pattern 2: Split by specific delimiter (Loop)
while (getline(ss, word, ',')) { ... }

// Pattern 3: Reset stream for reuse
ss.str(""); ss.clear();   // Must clear content AND error flags
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

// Find (Returns iterator)
auto it = s.find(val);
if (it != s.end()) {
    // Found! *it is the value
}
```

## Unordered Map (Key-Value Pairs)
```cpp
unordered_map<string, int> mp;

// 1. Insert / Update
mp["apple"] = 1;       // Simple but allows default creation
mp.insert({"banana", 2});

// 2. Check Existence (Two Ways)
// Way A: Simple Check (Double Hash Calculation if accessing later)
if (mp.count("apple")) { 
    int val = mp["apple"]; 
}

// Way B: Find & Use (Single Hash Calculation - Best Performance)
auto it = mp.find("apple");
if (it != mp.end()) {
    // Found!
    string key = it->first;  // Access Key
    int val = it->second;    // Access Value
}

// 3. Erase
mp.erase("apple");     // Erase by key
mp.erase(it);          // Erase by iterator (O(1))

// 4. Iteration (C++17 Structured Binding)
for (auto& [key, val] : mp) {
    // Process key and value
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 5. Container Adaptors (Stack, Queue, Deque, List)
**Header:** `<stack>`, `<queue>`, `<deque>`, `<list>`

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

## List (Doubly Linked List)
- **Use Case:** **LRU Cache** (via `splice`), frequent insertion/deletion at arbitrary positions where iterators must remain valid.
- **Key Difference:** Unlike Vector/Deque, inserting/erasing at **arbitrary positions** is **O(1)** (given an iterator). Iterators remain valid after insertion/deletion.

```cpp
list<int> lst;

// 1. Basic Ops (Same as Deque)
lst.push_back(1);
lst.push_front(2);
lst.pop_back();
lst.pop_front();
lst.front();         
lst.back();          

// 2. 🌟 O(1) Insert/Erase at Iterator (The Real Power)
auto it = lst.begin();
lst.insert(it, 10);  // Insert 10 BEFORE iterator -> O(1)
lst.erase(it);       // Remove element AT iterator -> O(1)

// 3. 🌟 Splice (Transfer Nodes without Copying)
// Moves element at 'sourceIt' from 'lst2' to 'lst1' (before 'pos')
lst1.splice(pos, lst2, sourceIt); 

// 4. Sorting (Special!)
// std::sort(lst.begin(), lst.end()) will CRASH (No random access).
lst.sort();          // O(N log N)
lst.reverse();       // O(N)

// 5. Remove specific values
lst.remove(5);       // Remove ALL elements equal to 5 -> O(N)
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
// Basic Math (<cmath>)
pow(2, 10);        // 2^10 = 1024.0 (double)
sqrt(16);          // 4.0 (double)
abs(n);            // Absolute value (int/double)
gcd(a, b);         // Greatest Common Divisor (C++17)
lcm(a, b);         // Least Common Multiple (C++17)

// Rounding (<cmath>)
ceil(2.3);         // 3.0 (Round UP)
floor(2.7);        // 2.0 (Round DOWN)
round(2.5);        // 3.0 (Round to nearest)

// Modification (<algorithm>)
reverse(v.begin(), v.end());          // In-place reverse O(N)
swap(a, b);                           // Swap values
max(a, b); / min(a, b);               // Basic
max({a, b, c}); / min({a, b, c});     // Initializer list (C++11)

// Aggregation (<numeric>)
accumulate(v.begin(), v.end(), 0);    // Sum (Initial value is 0)

// Find Max/Min Element in Vector O(N)
// Note: Returns an iterator, so use * to get value
int maxVal = *max_element(v.begin(), v.end()); 
int minVal = *min_element(v.begin(), v.end());
```

## Iterator Operations (Header: `<iterator>`)
> **Crucial for:** `std::list`, `std::set`, `std::map` where arithmetic (`it + 1`) is not allowed.

```cpp
auto it = myContainer.begin();

// Return a NEW iterator (Original 'it' stays same)
auto nextIt = next(it, 1);     // Move forward by 1 (default) or n
auto prevIt = prev(it, 1);     // Move backward by 1 (default) or n

// Modify the iterator IN-PLACE
advance(it, 2);                // Move 'it' forward by 2 steps

// Calculate distance
int dist = distance(first, last); // O(N) for list/set, O(1) for vector
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