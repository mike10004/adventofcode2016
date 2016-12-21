#include <stdio.h>

#define MAX_LINE_LEN 1024
#define MAX_NUM_RANGES 1200
#define IP_MIN 0
#define IP_MAX 4294967295

#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif

typedef struct Range {
    long min;
    long max;
} RANGE;

void set_range(RANGE* range, const long min, const long max)
{
    range->min = min;
    range->max = max;
}

int in_range(const long ip, RANGE* range)
{
    return (ip >= range->min) && (ip <= range->max);
}

int is_blocked(long ip, RANGE* ranges, const int num_ranges)
{
    int i;
    for (i = 0; i < num_ranges; i++) {
        if (in_range(ip, &(ranges[i]))) {
            return TRUE;
        }
    }
    return FALSE;
}

int main0(const long ip_min, const long ip_max) {
    int ngood;
    long range_min, range_max;
    RANGE ranges[MAX_NUM_RANGES];
    int range_idx = 0;
    long ip;
    while ((range_idx < MAX_NUM_RANGES) 
            && (ngood = fscanf(stdin, "%ld-%ld", &range_min, &range_max)) == 2) {
        set_range(&(ranges[range_idx]), range_min, range_max);
        range_idx++;
    }
    for (ip = ip_min; ip <= ip_max; ip++) {
        if (!is_blocked(ip, ranges, range_idx)) {
            fprintf(stdout, "least unblocked: %ld\n", ip);
            break;
        }    
    }
    return 0;
}

int main(int argc, char* argv[])
{
    int ngood;
    long ip_min = IP_MIN, ip_max = IP_MAX;
    if (argc > 1) {
        if (argc != 3) {
            fprintf(stderr, "must provide exactly two arguments: <ip_min> <ip_max>\n");
            return 1;
        }
        ngood = sscanf(argv[1], "%ld", &ip_min);
        if (ngood != 1) {
            fprintf(stderr, "bad argument: %s\n", argv[1]);
            return 1;
        }
        ngood = sscanf(argv[2], "%ld", &ip_max);
        if (ngood != 1) {
            fprintf(stderr, "bad argument: %s\n", argv[2]);
            return 1;
        }
    }
    return main0(ip_min, ip_max);
}
