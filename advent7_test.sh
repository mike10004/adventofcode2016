#!/bin/bash

gcc -o advent7_abba advent7_abba.c || exit $?

do_test()
{
    INPUT=$1
    EXPECTED=$2
    echo -n "testing $INPUT"
    ACTUAL=$(echo -n "$INPUT" | ./advent7_abba | wc -l)
    if [ "$EXPECTED" != "$ACTUAL" ] ; then
      echo
      echo "expected $EXPECTED != actual $ACTUAL" >&2
      exit 2
    fi
    echo " ...passed";
}

do_test "abba[mnop]qrst" 1
do_test "abcd[bddb]xyyx" 0
do_test "abba[mnop]" 1
do_test "abcdxyyx" 1
do_test "a" 0
do_test "ab" 0
do_test "abb" 0
do_test "abba" 1
do_test "aaaa[qwer]tyui" 0
do_test "ioxxoj[asdfgh]zxcvbn" 1
do_test "ioxxoj[qweruiiubcbg]bwegion" 0
do_test "abb[qewr]zxvf" 0
do_test "abbc[qewr]zxvf" 0
do_test "abbb[qewr]zxvf" 0
do_test "aiowergna[aauiwbaeuigv]ddaoppo" 1
