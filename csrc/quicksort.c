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