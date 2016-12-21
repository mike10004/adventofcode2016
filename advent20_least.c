#include <stdio.h>

#define MAX_LINE_LEN 1024
#define MAX_NUM_RANGES 1200
#define NUM_EXCEPTIONS 2

#define IP_MIN 0
#define IP_MAX 4294967295

typedef enum Boolean {
    FALSE = 0,
    TRUE = 1
} BOOLEAN;

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

BOOLEAN is_blocked(long ip, RANGE* ranges, const int num_ranges)
{
    int i;
    for (i = 0; i < num_ranges; i++) {
        if (in_range(ip, &(ranges[i]))) {
            return TRUE;
        }
    }
    return FALSE;
}

BOOLEAN are_disjoint(RANGE* a, RANGE* b) {
    long alow = a->min, blow = b->min, ahigh = a->max, bhigh = b->max;
    // return a[1] < (b[0] - 1) or b[1] < (a[0] - 1)
    return (a->max < (b->min - 1)) || (b->max < (a->min - 1));
}

long find_min(long* values, const int num_values) {
    BOOLEAN min_set = FALSE;
    int i;
    long min0 = -1;
    for (i = 0; i < num_values; i++) {
        if (!min_set || (values[i] < min0)) {
            min0 = values[i];
        }
    }
    return min0;
}

long find_max(long* values, const int num_values) {
    BOOLEAN max_set = FALSE;
    int i;
    long max0 = -1;
    for (i = 0; i < num_values; i++) {
        if (!max_set || (values[i] > max0)) {
            max0 = values[i];
        }
    }
    return max0;
}

// return TRUE if reduced, false if it was disjoint (and output not modified)
BOOLEAN reduce(RANGE* a, RANGE* b, RANGE* output)
{
    long values[] = {a->min, a->max, b->min, b->max};
    if (!are_disjoint(a, b)) {
        return FALSE;
    }
    set_range(output, find_min(values, 4), find_max(values, 4));
    return TRUE;
}

void copy_ranges(RANGE* src, const int num_ranges, RANGE* dst) {
    int i;
    for (i = 0; i < num_ranges; i++) {
        set_range(&(dst[i]), (&(src[i]))->min, (&(src[i]))->max);
    }
}

void shift_ranges(RANGE* ranges, const int num_ranges, const int except)
{
    int i;
    RANGE *src, *dst;
    for (i = except; i < (num_ranges - 1); i++) {
        src = &ranges[i+1];
        dst = &ranges[i];
        set_range(dst, src->min, src->max);
    }
}

int reduce_all(RANGE* ranges, const int init_num_ranges, RANGE* outputs)
{
    int i, j;
    int num_ranges = init_num_ranges;
    BOOLEAN all_disjoint = FALSE, istop;
    RANGE *a, *b;
    RANGE reduced;
    copy_ranges(ranges, num_ranges, outputs);
    while (!all_disjoint) {
        all_disjoint = TRUE;
        istop = FALSE;
        for (i = 0; (i < num_ranges) && !istop; i++) {
            a = &ranges[i];
            for (j = i + 1; j < num_ranges; j++) {
                b = &ranges[j];
                if (!are_disjoint(a, b)) {
                    reduce(a, b, &reduced);
                    set_range(&(outputs[i]), reduced.min, reduced.max);
                    shift_ranges(outputs, num_ranges, j);
                    num_ranges--;
                    istop = TRUE;
                    all_disjoint = FALSE;
                    break;
                }
            }
        }
    }    
    return num_ranges;
}

int main0(const long ip_min, const long ip_max) {
    int ngood;
    long range_min, range_max;
    RANGE ranges[MAX_NUM_RANGES];
    RANGE reduced_ranges[MAX_NUM_RANGES];
    int range_idx = 0, num_reduced;
    long ip;
    while ((range_idx < MAX_NUM_RANGES) 
            && (ngood = fscanf(stdin, "%ld-%ld", &range_min, &range_max)) == 2) {
        set_range(&(ranges[range_idx]), range_min, range_max);
        range_idx++;
    }
    num_reduced = reduce_all(ranges, range_idx, reduced_ranges);
    fprintf(stdout, "reduced %d ranges to %d\n", range_idx, num_reduced);
    
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
