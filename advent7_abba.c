/******************************************************************************

While snooping around the local network of EBHQ, you compile a list of IP 
addresses (they're IPv7, of course; IPv6 is much too limited). You'd like 
to figure out which IPs support TLS (transport-layer snooping).

An IP supports TLS if it has an Autonomous Bridge Bypass Annotation, or ABBA. 
An ABBA is any four-character sequence which consists of a pair of two 
different characters followed by the reverse of that pair, such as xyyx or 
abba. However, the IP also must not have an ABBA within any hypernet sequences,
which are contained by square brackets.

For example:

* abba[mnop]qrst supports TLS (abba outside square brackets).
* abcd[bddb]xyyx does not support TLS (bddb is within square brackets, even though xyyx is outside square brackets).
* aaaa[qwer]tyui does not support TLS (aaaa is invalid; the interior characters must be different).
* ioxxoj[asdfgh]zxcvbn supports TLS (oxxo is outside square brackets, even though it's within a larger string).

*******************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#define MAX_LINE_LEN 4096
#define NEWLINE '\n'
#define PROG "advent7_abba"
#define START_HYPERNET '['
#define STOP_HYPERNET ']'
#define NPOS -1
#ifndef VERBOSE
#define VERBOSE 0
#endif

typedef enum boolean { FALSE = 0, TRUE = 1 } BOOLEAN;

typedef struct Matcher
{
    char a;
    char b;
    char c;
    char d;
} MATCHER;


/*
 * Reads a line from a stream, copies it into a buffer, adds a null
 * terminator after the copied data in the buffer, and returns
 * the count of the characters copied. If there are more
 * charactes than `buffer_len - 1`, returns -1.
 */
int read_line(FILE* stream, char* buffer, const int buffer_len)
{
    int c = 0;
    int pos = 0;
    while ((c = getc(stream)) != EOF) {
        if (c == NEWLINE) {
            break;
        }
        if (pos >= (buffer_len - 1)) {
            fprintf(stderr, "%s: line too long (%d characters)\n", PROG, pos);
            return -1;
        }
        buffer[pos] = (char) c;
        pos++;
    }
    if (pos < buffer_len) {
        buffer[pos] = '\0';
    }
    if ((pos == 0) && (c != EOF)) {
        fprintf(stderr, "%s: warning: read zero characters from stream\n", PROG);
    }
    return pos;
}

void reset_matcher(MATCHER* matcher) 
{
    matcher->a = '\0';
    matcher->b = '\0';
    matcher->c = '\0';
    matcher->d = '\0';
}

BOOLEAN is_match(MATCHER* matcher) {
    return ((matcher->a == matcher->d)
    && (matcher->b == matcher->c) 
    && (matcher->a != matcher->b)
    && (matcher->a != '\0')
    && (matcher->b != '\0')) ? TRUE : FALSE;
}

void consume(MATCHER* matcher, char d) {
    matcher->a = matcher->b;
    matcher->b = matcher->c;
    matcher->c = matcher->d;
    matcher->d = d;
}

int index_of(char* str, char ch, int start, int len) 
{
    const char *region = str + start;
    const char *p = strchr(region, ch);
    int pos;
    if (p) {
        pos = (p - region) + start;
        if (pos >= (start + len)) {
            pos = NPOS;
        }
    } else {
        pos = NPOS;
    }
    if (VERBOSE) fprintf(stderr, "%s: index_of(\"%s\", %c, %d, %d) = %d\n", PROG, str, ch, start, len, pos);
    return pos;
}

BOOLEAN check_region_for_match(char* line, const int start, const int end)
{
    MATCHER m;
    char ch;
    int offset = start;
    if (VERBOSE) fprintf(stderr, "%s: check_region_for_match(line, %d, %d)\n", PROG, start, end);
    reset_matcher(&m);
    while ((offset < end) && (ch = line[offset])) {
        consume(&m, ch);
        offset++;
        if (is_match(&m)) {
            return TRUE;
        }
    }
    return FALSE;
}

typedef enum Result {
    NONE, REJECTED, MATCHED
} RESULT;

char* to_string(RESULT result)
{
    switch (result) {
        case NONE: return "NONE";
        case REJECTED: return "REJECTED";
        case MATCHED: return "MATCHED";
        default: return "<UNKNOWN>";
    }
} 

void process_line(char* line, const int start, const int len, RESULT* result)
{
    int pos = 0;
    int i, hstart = NPOS, hstop = NPOS;
    int last;
    BOOLEAN hypernet;
    assert(line != NULL);
    assert(start >= 0);
    assert(len >= 0);
    assert(result != NULL);
    if (VERBOSE) fprintf(stderr, "%s: process_line(line, %d, %d, %s)\n", PROG, start, len, to_string(*result));
    if (((*result) == REJECTED) || (len == 0)) {
        if (VERBOSE) fprintf(stderr, "%s: process_line: breaking with result=%s, len=%d\n", PROG, to_string(*result), len);
        return;
    }
    hstart = index_of(line, START_HYPERNET, start, len);
    hstop = index_of(line, STOP_HYPERNET, start, len);
    if ((hstart == NPOS) && (hstop == NPOS)) {
        hypernet = FALSE;
        last = start + len;
    } else if ((hstart != NPOS) && (hstop != NPOS)) {
        hypernet = hstop < hstart;
        last = (hstop < hstart ? hstop : hstart) + 1;
    } else {
        hypernet = (hstart == NPOS);
        if (hstart != NPOS) { // => hstop >= 0
            last = hstart + 1;
        } else if (hstop != NPOS) {
            last = hstop + 1;
        } else {
            fprintf(stderr, "%s: process_line: bug: hstart=%d, hstop=%d\n", PROG, hstart, hstop);
            exit(3);
        }
    }
    if (check_region_for_match(line, start, last)) {
        *result = hypernet ? REJECTED : MATCHED;
        if (VERBOSE) fprintf(stderr, "%s: process_line: region [%d,%d) hypernet = %s (result=%s)\n", PROG, start, last, hypernet ? "TRUE" : "FALSE", to_string(*result));
    }
    assert(start != last);
    process_line(line, last, len - (last - start), result);
}

/*
 * Reads from a stream and reprints to stdout all lines that 
 * represent "TLS" (defined above). Returns the count of matching
 * lines 
 */  
int filter_by_abba(FILE* stream, FILE* out)
{
    MATCHER matcher;
    char buffer[MAX_LINE_LEN];
    int count = 0;
    int total = 0;
    int line_len;
    RESULT result;
    while ((line_len = read_line(stream, buffer, MAX_LINE_LEN)) > 0) {
        result = NONE;
        process_line(buffer, 0, line_len, &result);
        if (result == MATCHED) {
            count++;
            fprintf(out, "%4d %s\n", count, buffer);
        }
        ++total;
    }
    if (VERBOSE) fprintf(stderr, "%d lines read from input\n", total);
    return count;
}

int main(int argc, char* argv[])
{
    int count = filter_by_abba(stdin, stdout);
    if (VERBOSE) fprintf(stderr, "%d matching lines in input\n", count);
    return 0;
}
