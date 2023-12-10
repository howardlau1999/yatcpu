
void uart_send_char(char c)
{
  *((volatile unsigned int *) (0x40000010)) = c;
  // *((volatile unsigned int *) (0x40000000)) = c;
}


void waste_some_time(int cycle) {
  unsigned int a = 135;
  while (cycle--) {
    a++;
  }
}

int main() {

    // const char* s = "abcd";
    const char* s = "Never gonna give you up~ Never gonna let you down~\n\
    Never gonna run around and~ desert you~\n";

    while (1) {
      

      // for (int i = 0; i < ; i++) {
      //   uart_send_char(s[i]);
      //   waste_some_time(100);
      // }

      const char *p = s;
      while (*p != 0)
      {
        uart_send_char(*p);
        p++;
        waste_some_time(100);
      }

      waste_some_time(200);

      break;  // print once, but pressing CPU reset can print again
    }

    return 0;
}
