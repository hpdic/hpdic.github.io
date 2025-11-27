# C++ STL and Algorithm API Reference

## 1. Dynamic Array (std::vector)
**Header:** `<vector>`

### Initialization
```cpp
vector<int> v;                  // Default initialization (empty)
vector<int> v(n, -1);           // Size n, initialized to -1
vector<int> v = {1, 2, 3};      // Initializer list
vector<vector<int>> grid(m, vector<int>(n, 0)); // m x n Matrix initialized to 0