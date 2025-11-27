<h1 align="center">C++ Coding Snippets</h1>

<h1 id="table-of-contents">Table of Contents</h1>

- [1. Reverse a Linked List](#1-reverse-a-linked-list)
- [2. Monotonic Stack](#2-monotonic-stack)
- [3. Trie (Prefix Tree)](#3-trie-prefix-tree)
- [4. Non-Recursive In-Order Tree Traversal](#4-non-recursive-in-order-tree-traversal)
- [5. Breadth-First Search (BFS) - Graph/Matrix](#5-breadth-first-search-bfs---graphmatrix)
- [6. DFS Backtrack](#6-dfs-backtrack)
- [7. Dynamic Programming](#7-dynamic-programming)

---

# 1. Reverse a Linked List
**Pattern:** Iterative Pointer Manipulation
- **Time:** O(N), **Space:** O(1)
- **Core Logic:** Save Next -> Point Back -> Move Forward.

```cpp
ListNode* reverseList(ListNode* head) {
    ListNode* prev = nullptr;
    ListNode* curr = head;
    
    while (curr != nullptr) {
        ListNode* nextTemp = curr->next; // 1. Save next node
        curr->next = prev;               // 2. Reverse pointer
        prev = curr;                     // 3. Move prev forward
        curr = nextTemp;                 // 4. Move curr forward
    }
    
    return prev; // New head
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 2. Monotonic Stack
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

# 3. Trie (Prefix Tree)
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

# 4. Non-Recursive In-Order Tree Traversal
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

# 5. Breadth-First Search (BFS) - Graph/Matrix
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

# 6. DFS Backtrack
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

# 7. Dynamic Programming
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
**Crucial:** Inner loop must iterate **backwards** to avoid using the same item twice.

```cpp
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