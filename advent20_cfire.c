#include <stdio.h>

#define MAX_LINE_LEN 1024
#define MAX_NUM_RANGES 1200
#define IP_MIN 0
#define IP_MAX 4294967295

#define XTRUE 1
#define XFALSE 0

int in_range(const long ip, const long range_min, const long range_max)
{
    return (ip >= range_min) && (ip <= range_max);
}

int is_blocked(long ip, long** prange_mins, long** prange_maxs, const int num_ranges)
{
    int i;
    long* range_mins = *prange_mins;
    long* range_maxs = *prange_maxs;
    for (i = 0; i < num_ranges; i++) {
        if (in_range(ip, range_mins[i], range_maxs[i])) {
            return XTRUE;
        }
    }
    return XFALSE;
}

int main(int argc, char* argv[])
{
    long range_mins[MAX_NUM_RANGES];
    long range_maxs[MAX_NUM_RANGES];
    int range_idx = 0;
    long range_min, range_max;
    int ngood;
    long ip;
    do {
        ngood = fscanf(stdin, "%ld-%ld", &range_min, &range_max);
        if (ngood == 2) {
            // fprintf(stdout, "[%l, %l]\n", range_min, range_max);
            range_mins[range_idx] = range_min;
            range_maxs[range_idx] = range_max;
            range_idx++;
        }
    } while (ngood == 2);
    for (ip = IP_MIN; ip <= IP_MAX; ip++) {
        if (!is_blocked(ip, &range_mins, &range_maxs, range_idx)) {
            fprintf(stdout, "not blocked: %ld\n", ip);
            break;
        }    
    }
    return 0;
}