<h1 align="center">C++ Coding Snippets</h1>

<h1 id="table-of-contents">Table of Contents</h1>

- [1. Intervals](#1-intervals)
- [2. Linked List](#2-linked-list)
- [3. Monotonic Stack](#3-monotonic-stack)
- [4. Trie (Prefix Tree)](#4-trie-prefix-tree)
- [5. Non-Recursive In-Order Tree Traversal](#5-non-recursive-in-order-tree-traversal)
- [6. Breadth-First Search (BFS) - Graph/Matrix](#6-breadth-first-search-bfs---graphmatrix)
- [7. Graph Cycle Detection](#7-graph-cycle-detection)
- [8. DFS Backtrack](#8-dfs-backtrack)
- [9. Dynamic Programming](#9-dynamic-programming)

---

# 1. Intervals

## Classification Rule

The key to solving Interval problems is the sorting strategy. Depending on the goal, there are two main approaches:

* Problem asks for **Maximum Non-overlapping Count** or **Minimum Removal**: Sort by **End Time**.
* Problem asks for **Merging**, **Coverage**, or **Resource Allocation**: Sort by **Start Time**.

## Type 1: Greedy Selection (Sort by End Time)

This usually falls under the "Activity Selection Problem" category.

* **Goal**: Select as many non conflicting intervals as possible within a limited timeframe.
* **Logic**: Greedy strategy. We want the selected interval to finish as early as possible. Finishing early leaves more space for subsequent intervals, maximizing the total count.
* **Sorting Syntax**:
    ```cpp
    auto cmp = [](const auto& a, const auto& b) { return a[1] < b[1]; };
    sort(intervals.begin(), intervals.end(), cmp);
    ```
* **Typical Problems**:
    * 435 Non overlapping Intervals (Find min removal = Find max non overlapping)
    * 452 Minimum Number of Arrows to Burst Balloons

## Type 2: Physical Merging (Sort by Start Time)

This is the general approach for handling timeline relationships.

* **Goal**: Merge intervals, calculate total coverage, insert new intervals, or arrange meeting rooms.
* **Logic**: Process events in chronological order. We must know who starts first to determine if they overlap with the previous interval or if a new resource is needed.
* **Sorting Syntax**:
    ```cpp
    sort(intervals.begin(), intervals.end()); // Default sorts by Start Time
    ```
* **Typical Problems**:
    * 56 Merge Intervals
    * 57 Insert Interval
    * 252 Meeting Rooms
    * 253 Meeting Rooms II (Sweep Line)

## Code Templates

**Template 1: Greedy (Sort by End Time)**
> The problem asks us to remove the minimum number of intervals to ensure the remaining ones are non-overlapping. This is equivalent to finding the maximum number of non-overlapping intervals, which we solve by sorting by end time and greedily keeping the ones that finish earliest.
```cpp
int eraseOverlapIntervals(vector<vector<int>>& intervals) {
    if (intervals.empty()) return 0;
    
    // 1. Sort by End Time
    sort(intervals.begin(), intervals.end(), [](const auto& a, const auto& b) {
        return a[1] < b[1];
    });

    int count = 0; // Count removals
    int end = intervals[0][1]; // Current valid end boundary

    for (int i = 1; i < intervals.size(); ++i) {
        // If current start < previous end, we have a conflict
        if (intervals[i][0] < end) {
            count++; // Remove current (Greedy: keep the one that ends earlier)
        } else {
            end = intervals[i][1]; // No conflict, update boundary
        }
    }
    return count;
}
```

**Template 2: Merging (Sort by Start Time)**

> The goal is to merge all overlapping intervals to produce a list of non-overlapping intervals that cover the entire range. This is solved by sorting the intervals by start time and extending the end time of the last merged interval whenever the current interval overlaps with it.

```cpp
vector<vector<int>> merge(vector<vector<int>>& intervals) {
    if (intervals.empty()) return {};
    
    // 1. Sort by Start Time
    sort(intervals.begin(), intervals.end());

    vector<vector<int>> merged;
    merged.push_back(intervals[0]);

    for (int i = 1; i < intervals.size(); ++i) {
        // 2. Compare: End of last merged interval vs Start of current
        if (merged.back()[1] >= intervals[i][0]) {
            // Overlap detected: Merge (Update End to the max of both)
            merged.back()[1] = max(merged.back()[1], intervals[i][1]);
        } else {
            // No overlap: Push current interval as is
            merged.push_back(intervals[i]);
        }
    }
    return merged;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 2. Linked List

## Dummy Node Strategy
**Pattern:** Creating a temporary "pre-head" node to simplify boundary conditions.
- **Time:** O(1) overhead
- **Space:** O(1)
- **Core Benefit:** Eliminates the need to check `if (head == nullptr)` or handle head changes separately.

> When to Use? (The 3 Golden Rules)
Use a Dummy Node whenever the **head of the list might change**:
1.  **Constructing a New List**: When building a list from scratch (e.g., *Merge Two Lists*, *Add Two Numbers*), you don't know the first node yet.
2.  **Deleting Nodes**: When the head node itself might be removed (e.g., *Remove Nth Node*, *Remove Elements*).
3.  **Reordering / Swapping**: When the structure changes significantly, and the new head could be any node (e.g., *Swap Pairs*, *Reverse Nodes in k-Group*).

> Universal Template
This pattern works for 90% of linked list modification problems.

```cpp
ListNode* solve(ListNode* head) {
    // 1. Create Dummy on Stack (Zero Overhead, Auto-cleanup)
    ListNode dummy(0);
    dummy.next = head;
    
    // 2. Initialize pointer to address of dummy
    ListNode* curr = &dummy;
    
    // 3. Perform Operations
    // Example: Iterate or Modify
    // while (curr->next) { ... }
    
    // 4. Return new head
    // 'dummy' is destroyed automatically, but 'dummy.next' points to the valid heap node.
    return dummy.next;
}
```

## Merge Two Sorted Lists (Recursive)
**Pattern:** Recursion / Divide & Conquer
- **Time:** O(N + M), **Space:** O(N + M) (Recursion Stack)
- **Core Logic:** Pick Smaller Head -> Recurse for Rest -> Link & Return.

```cpp
ListNode* mergeTwoLists(ListNode* l1, ListNode* l2) {
    // 1. Base Cases: If one list is empty, return the other
    if (!l1) return l2;
    if (!l2) return l1;

    // 2. Recursive Step: Pick the smaller node as the new head
    if (l1->val < l2->val) {
        // "My next node is the result of merging the rest"
        l1->next = mergeTwoLists(l1->next, l2);
        return l1;
    } else {
        l2->next = mergeTwoLists(l1, l2->next);
        return l2;
    }
}
```

## Reverse a Linked List
**Pattern:** Iterative Pointer Manipulation
- **Time:** O(N), **Space:** O(1)
- **Core Logic:** Save Next -> Point Back -> Move Forward.

```cpp
ListNode* reverseList(ListNode* head) {
    ListNode* prev = nullptr;
    ListNode* curr = head;
    
    while (curr != nullptr) {
        ListNode* next = curr->next;    // 1. Save next node
        curr->next = prev;              // 2. Reverse pointer
        prev = curr;                    // 3. Move prev forward
        curr = next;                    // 4. Move curr forward
    }
    
    return prev; // New head
}
```

## Finding the Middle of a Linked List
**Pattern:** Fast & Slow Pointers
- **Time:** O(N)
- **Space:** O(1)
- **Usage:** Splitting list for Merge Sort, Palindrome Check.

> Template: Find First Middle (Splitting Strategy)
Initialize `fast = head->next`. This ensures that for even lengths, `slow` stops at the **end of the first half** (Pre-middle), which allows proper splitting.

```cpp
ListNode* findMiddle(ListNode* head) {
    // Safety check needed because we access head->next immediately
    if (head == nullptr || head->next == nullptr) return head;

    ListNode* slow = head;
    ListNode* fast = head->next; // ⚡️ Key Difference: Fast starts ahead

    while (fast != nullptr && fast->next != nullptr) {
        slow = slow->next;
        fast = fast->next->next;
    }
    
    return slow; 
    // Even length [1,2,3,4] -> Returns 2.
    // Odd length  [1,2,3]   -> Returns 2.
}
```

## Fast & Slow Pointers (Floyd's Cycle Detection)
**Pattern:** Two pointers moving at different speeds to detect cycles or find midpoints.
- **Time:** O(N)
- **Space:** O(1)
- **Usage:** Detect cycle, Find cycle start, Find middle node, Happy Number.

> Template: Find Cycle Start Node
**Logic:**
1.  **Phase 1:** `Slow` moves 1 step, `Fast` moves 2 steps. If they meet, a cycle exists.
2.  **Phase 2:** Reset `Slow` to `Head`. Keep `Fast` at meeting point. Both move 1 step. They meet at the entry.

```cpp
ListNode* detectCycle(ListNode* head) {
    auto slow = head, fast = slow;

    // Phase 1: Determine if a cycle exists
    while (true) {
        // If fast hits the end, there is no cycle
        if (!fast || !fast->next) return nullptr;
        
        slow = slow->next;       // Move 1 step
        fast = fast->next->next; // Move 2 steps
        
        if (slow == fast) break; // Collision detected
    }

    // Phase 2: Find the entry point
    // Reset slow to head, move both at same speed
    slow = head;
    while (slow != fast) {
        slow = slow->next;
        fast = fast->next;
    }
    
    return slow; // They meet at the cycle entry
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 3. Monotonic Stack
**Pattern:** Maintain a sorted stack to find the "First Greater/Smaller Element".
- **Time:** O(N) (Each element pushed & popped max once), **Space:** O(N).
- **Usage:** Next Greater Element, Daily Temperatures, Largest Rectangle in Histogram.

## Template: Next Greater Element (Monotonic Decreasing Stack)
Finds the first element to the right that is strictly larger.

```cpp
vector<int> nextGreaterElements(vector<int>& nums) {
    int n = nums.size();
    vector<int> res(n, -1); // Default -1 if no greater element exists
    stack<int> st; // Stores INDICES, not values

    for (int i = 0; i < n; i++) {
        // While current element is stronger than the one at stack top:
        // We found the "Next Greater" for the index at st.top()!
        while (!st.empty() && nums[i] > nums[st.top()]) {
            int index = st.top();
            st.pop();
            res[index] = nums[i]; // Record the answer
        }
        st.push(i);
    }
    return res;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 4. Trie (Prefix Tree)
- **Header:** None (Must implement manually)
- **Time Complexity:** Insert/Search are **O(L)** where L is word length.
- **Usage:** Autocomplete, Spell Checker, String Search.

## Minimal Implementation Template
Standard Array implementation (fastest for a-z).

```cpp
struct TrieNode {
    TrieNode* children[26] = {nullptr};
    bool isEnd = false;
    // Or: string* word = nullptr; (For Word Search II)
    
    TrieNode() {}
};

class Trie {
    TrieNode* root;
public:
    Trie() { root = new TrieNode(); }
    
    // Insert a word - O(L)
    void insert(string word) {
        TrieNode* node = root;
        for (char c : word) {
            int idx = c - 'a';
            if (!node->children[idx]) {
                node->children[idx] = new TrieNode();
            }
            node = node->children[idx];
        }
        node->isEnd = true;
    }
    
    // Search for a word - O(L)
    bool search(string word) {
        TrieNode* node = root;
        for (char c : word) {
            int idx = c - 'a';
            if (!node->children[idx]) return false;
            node = node->children[idx];
        }
        return node->isEnd;
    }
    
    // Check if any word starts with prefix - O(L)
    bool startsWith(string prefix) {
        TrieNode* node = root;
        for (char c : prefix) {
            int idx = c - 'a';
            if (!node->children[idx]) return false;
            node = node->children[idx];
        }
        return true;
    }
};
```            

[⬆️ Back to Top](#table-of-contents)
---

# 5. Non-Recursive In-Order Tree Traversal
**Pattern:** Iterative DFS using Stack
- **Time Complexity:** **O(N)** (Each node is pushed and popped exactly once).
- **Space Complexity:** **O(H)** (Where H is tree height, for the stack).

## The "Push Left, Pop, Go Right" Template
This approach simulates the system call stack. The logic is: go as left as possible, process the node, then try to go right.

```cpp
vector<int> inorderTraversal(TreeNode* root) {
    vector<int> result;
    stack<TreeNode*> st;
    TreeNode* curr = root;

    // Loop condition: 
    // 1. 'curr != nullptr': We have a valid node to explore (go left)
    // 2. '!st.empty()': We have parents waiting to be processed
    while (curr != nullptr || !st.empty()) {
        
        // Step 1: Reach the left-most node of the current subtree
        while (curr != nullptr) {
            // [Pre-Order]: Process node here (Before going left)
            // result.push_back(curr->val);

            st.push(curr);
            curr = curr->left;
        }

        // Step 2: Pop the stack. This node is now the "root" of a subtree.
        // In-order: Left (done) -> Root (now) -> Right (next)
        curr = st.top();
        st.pop();
        
        // [In-Order]: Process node here (After returning from left)
        result.push_back(curr->val);

        // Step 3: Turn to the right child
        curr = curr->right;
    }
    
    return result;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 6. Breadth-First Search (BFS) - Graph/Matrix
**Pattern:** Queue + Visited Set + Level Loop
- **Time:** O(V + E) or O(N*M), **Space:** O(V) or O(N*M)
- **Usage:** Shortest path in unweighted graphs, level-order traversal.

```cpp
void bfs(int startNode, int n, vector<vector<int>>& adj) {
    queue<int> q;
    vector<bool> visited(n, false); // Or unordered_set for sparse graphs
    
    q.push(startNode);
    visited[startNode] = true;
    int level = 0; // Tracks distance from start

    while (!q.empty()) {
        int size = q.size(); // 1. Lock size for current level
        
        for (int i = 0; i < size; i++) {
            int curr = q.front();
            q.pop();

            // Process current node here...
            // if (curr == target) return level;

            // 2. Add unvisited neighbors
            for (int neighbor : adj[curr]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true; // Mark immediately when pushing!
                    q.push(neighbor);
                }
            }
        }
        level++; // Move to next layer
    }
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 7. Graph Cycle Detection
**Pattern:** Differentiate between Directed and Undirected graphs strategies.
- **Time:** O(V + E) for both.
- **Space:** O(V) for both.

## Template: DFS Three-Coloring on Directed Graphs
- **Note:** This algorithm assumes the graph is connected. For disconnected graphs, loop through all nodes.
- **Logic:** Use 3 states to detect back-edges (pointing to an ancestor in the current recursion stack).
* `0`: Unvisited
* `1`: Visiting (Current Path) -> **Cycle found if met**
* `2`: Visited (Safe)

```cpp
// Returns true if a cycle exists
bool hasCycle(int curr, vector<vector<int>>& adj, vector<int>& state) {
    // 1. Found a node in the current recursion stack -> Cycle!
    if (state[curr] == 1) return true;
    
    // 2. Found a processed safe node -> No cycle here, prune.
    if (state[curr] == 2) return false;
    
    // 3. Mark as "Visiting"
    state[curr] = 1;
    
    for (int next : adj[curr]) {
        if (hasCycle(next, adj, state)) return true;
    }
    
    // 4. Backtrack: Mark as "Visited" (Safe)
    state[curr] = 2;
    return false;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 8. DFS Backtrack
**Pattern:** Recursion + State Reset (Choose -> Explore -> Unchoose)
- **Time:** O(N!) (Factorial complexity)
- **Space:** O(N) (Recursion stack + Visited array)
- **Usage:** Permutations, N-Queens, Path Finding in Matrix.

## Template: Permutations (Using `visited` array)
Unlike subsets (which move forward), permutations scan from `0` every time but skip used elements.

```cpp
void backtrack(vector<int>& nums, vector<bool>& visited, vector<int>& path, vector<vector<int>>& res) {
    // 1. Base Case: Path length equals input size
    if (path.size() == nums.size()) {
        res.push_back(path);
        return;
    }

    // 2. Iterate through ALL elements
    for (int i = 0; i < nums.size(); i++) {
        
        // Skip if used in current branch
        if (visited[i]) continue;
        
        // [Pruning for Duplicates]: (Requires sorted nums)
        // if (i > 0 && nums[i] == nums[i-1] && !visited[i-1]) continue;

        // 3. Make Choice
        visited[i] = true;
        path.push_back(nums[i]);

        // 4. Recurse
        backtrack(nums, visited, path, res);

        // 5. Undo Choice (Backtrack)
        visited[i] = false;
        path.pop_back();
    }
}
```

## Template: Permutations (Swap-based, No `visited` array)
**Pattern:** In-place Swap + Backtrack
> **Space Optimization:** Saves O(N) space by using the input array itself to track state.
> **Note:** The output order might not be strictly lexicographical.

```cpp
void backtrack(vector<int>& nums, int start, vector<vector<int>>& res) {
    // Base Case: We have fixed positions for all indices
    if (start == nums.size()) {
        res.push_back(nums);
        return;
    }

    for (int i = start; i < nums.size(); i++) {
        // 1. Make Choice: Swap nums[i] to the current 'start' position
        // This effectively "marks" nums[i] as used for this level
        swap(nums[start], nums[i]);

        // 2. Recurse: Move to the next index
        backtrack(nums, start + 1, res);

        // 3. Undo Choice: Swap back to restore original state
        swap(nums[start], nums[i]);
    }
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 9. Dynamic Programming
**Pattern:** 2D Grid / State Transition
- **Time:** O(N*M)
- **Space:** O(N*M) (Can be optimized to O(N) using rolling array)
- **Usage:** Edit Distance, Longest Common Subsequence, Knapsack.

## Template: Edit Distance (Levenshtein Distance)
`dp[i][j]` represents the min operations to convert `word1[0..i-1]` to `word2[0..j-1]`.

```cpp
int minDistance(string word1, string word2) {
    int m = word1.size(), n = word2.size();
    // dp[i][j]: min operations to convert word1[0..i-1] to word2[0..j-1]
    vector<vector<int>> dp(m + 1, vector<int>(n + 1));

    // 1. Base Cases: Converting string to empty string (deletions/insertions)
    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;

    // 2. Iteration
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (word1[i - 1] == word2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1]; // Match: No new op needed
            } else {
                dp[i][j] = min({
                    dp[i - 1][j],    // Delete from word1
                    dp[i][j - 1],    // Insert into word1
                    dp[i - 1][j - 1] // Replace char
                }) + 1;
            }
        }
    }
    return dp[m][n];
}
```

## Template: Standard 0/1 Knapsack
Find max value with capacity `W`.

```cpp
int knapsack2D(int W, vector<int>& wt, vector<int>& val) {
    int n = wt.size();
    
    // dp[i][j] stores the maximum value achievable using a subset 
    // of the first i items with a maximum weight limit of j
    vector<vector<int>> dp(n + 1, vector<int>(W + 1, 0));

    // Iterate through all items from 1 to n
    for (int i = 1; i <= n; i++) {
        
        // Iterate through all possible capacity limits from 1 to W
        for (int j = 1; j <= W; j++) {
            
            // Item index in vectors is i minus 1 because vectors are zero indexed
            if (wt[i - 1] <= j) {
                // The current item can possibly fit in the current capacity limit j
                // We choose the maximum between two options:
                // Option 1: Not including the current item
                // Option 2: Including the current item and adding its value to the optimal 
                // solution for the remaining capacity
                dp[i][j] = max(dp[i - 1][j], dp[i - 1][j - wt[i - 1]] + val[i - 1]);
            } else {
                // The current item is strictly heavier than the current capacity limit j
                // We cannot include it so the optimal value is the same as without it
                dp[i][j] = dp[i - 1][j];
            }
            
        }
    }
    
    // The bottom right cell contains the maximum value for all items and full capacity
    return dp[n][W];
}

// If you want one-dimensional DP, iterate backwards to prevent overwriting states that are needed for future calculations.
int knapsack(int W, vector<int>& wt, vector<int>& val) {
    // dp[j]: Max value for capacity j
    vector<int> dp(W + 1, 0);

    for (int i = 0; i < wt.size(); i++) {
        // Iterate BACKWARDS from W down to wt[i]
        // If forward, dp[j-wt[i]] would use the current item (Unbounded Knapsack)
        for (int j = W; j >= wt[i]; j--) {
            dp[j] = max(dp[j], dp[j - wt[i]] + val[i]);
        }
    }
    
    return dp[W];
}
```

[⬆️ Back to Top](#table-of-contents)
---