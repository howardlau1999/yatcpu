// Copyright 2021 Howard Lau
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

void quicksort(int *arr, int l, int r) {
  if (l >= r) return;
  int pivot = arr[l];
  int i = l, j = r;
  while (i < j) {
    while(arr[j] >= pivot && i < j) --j;
    arr[i] = arr[j];
    while(arr[i] < pivot && i < j) ++i;
    arr[j] = arr[i];
  }
  arr[i] = pivot;
  quicksort(arr, l, i - 1);
  quicksort(arr, i + 1, r);
}

int main() {
  int nums[10];

  nums[0] = 6;
  nums[1] = 2;
  nums[2] = 4;
  nums[3] = 5;
  nums[4] = 3;
  nums[5] = 1;
  nums[6] = 0;
  nums[7] = 9;
  nums[8] = 7;
  nums[9] = 8;


  quicksort(nums, 0, 9);

  for (int i = 1; i <= 10; ++i) {
    *(int *)(i * 4) = nums[i - 1];
  }
}