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

int in_range(const long ip, const long range_min, const long range_max)
{
    return (ip >= range_min) && (ip <= range_max);
}

int is_blocked(long ip, long* range_mins, long* range_maxs, const int num_ranges)
{
    int i;
    for (i = 0; i < num_ranges; i++) {
        if (in_range(ip, range_mins[i], range_maxs[i])) {
            return TRUE;
        }
    }
    return FALSE;
}

int main0(const long ip_min, const long ip_max) {
    int ngood;
    long range_min, range_max;
    long range_mins[MAX_NUM_RANGES];
    long range_maxs[MAX_NUM_RANGES];
    int range_idx = 0;
    long ip;
    while ((ngood = fscanf(stdin, "%ld-%ld", &range_min, &range_max)) == 2) {
        range_mins[range_idx] = range_min;
        range_maxs[range_idx] = range_max;
        range_idx++;
    }
    for (ip = ip_min; ip <= ip_max; ip++) {
        if (!is_blocked(ip, range_mins, range_maxs, range_idx)) {
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
