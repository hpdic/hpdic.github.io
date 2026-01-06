/**
 * Author: Dongfang Zhao (dzhao@uw.edu)
 */

#include <iostream>
#include <vector>
#include <algorithm>
#include <cassert>

using namespace std;

// Standard 2D DP Knapsack implementation
int knapsack_2d(int W, const vector<int>& wt, const vector<int>& val) {
  int n = wt.size();
  // dp[i][j]: Max value using first i items with capacity j
  vector<vector<int>> dp(n + 1, vector<int>(W + 1, 0));

  for (int i = 1; i <= n; i++) {
    for (int j = 0; j <= W; j++) {
      // If the item is heavier than current capacity j, skip it
      if (j < wt[i - 1]) {
        dp[i][j] = dp[i - 1][j];
      } else {
        // Choice: Either skip item i, or take item i
        dp[i][j] = max(dp[i - 1][j], dp[i - 1][j - wt[i - 1]] + val[i - 1]);
      }
    }
  }
  return dp[n][W];
}

int main() {
  // Test Case
  vector<int> wt = {1, 3, 4};
  vector<int> val = {15, 20, 30};
  int W = 4;

  int result = knapsack_2d(W, wt, val);

  // Verify logic: Item #0 (15) + Item #1 (20) = 35. Weight 1+3=4.
  assert(result == 35);

  cout << "Test Passed: Result is " << result << endl;

  return 0;
}

/**
 g++ knapsack_2d.cpp -o knapsack_2d.bin
 ./knapsack_2d.bin
 */